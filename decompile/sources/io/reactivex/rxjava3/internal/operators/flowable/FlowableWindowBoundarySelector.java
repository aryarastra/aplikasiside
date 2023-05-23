package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue;
import io.reactivex.rxjava3.internal.queue.MpscLinkedQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import kotlin.jvm.internal.LongCompanionObject;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableWindowBoundarySelector<T, B, V> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final int bufferSize;
    final Function<? super B, ? extends Publisher<V>> closingIndicator;
    final Publisher<B> open;

    public FlowableWindowBoundarySelector(Flowable<T> source, Publisher<B> open, Function<? super B, ? extends Publisher<V>> closingIndicator, int bufferSize) {
        super(source);
        this.open = open;
        this.closingIndicator = closingIndicator;
        this.bufferSize = bufferSize;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super Flowable<T>> s) {
        this.source.subscribe((FlowableSubscriber) new WindowBoundaryMainSubscriber(s, this.open, this.closingIndicator, this.bufferSize));
    }

    /* loaded from: classes.dex */
    static final class WindowBoundaryMainSubscriber<T, B, V> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = 8646217640096099753L;
        final int bufferSize;
        final Function<? super B, ? extends Publisher<V>> closingIndicator;
        final Subscriber<? super Flowable<T>> downstream;
        long emitted;
        final Publisher<B> open;
        volatile boolean openDone;
        Subscription upstream;
        volatile boolean upstreamCanceled;
        volatile boolean upstreamDone;
        final SimplePlainQueue<Object> queue = new MpscLinkedQueue();
        final CompositeDisposable resources = new CompositeDisposable();
        final List<UnicastProcessor<T>> windows = new ArrayList();
        final AtomicLong windowCount = new AtomicLong(1);
        final AtomicBoolean downstreamCancelled = new AtomicBoolean();
        final AtomicThrowable error = new AtomicThrowable();
        final WindowStartSubscriber<B> startSubscriber = new WindowStartSubscriber<>(this);
        final AtomicLong requested = new AtomicLong();

        WindowBoundaryMainSubscriber(Subscriber<? super Flowable<T>> actual, Publisher<B> open, Function<? super B, ? extends Publisher<V>> closingIndicator, int bufferSize) {
            this.downstream = actual;
            this.open = open;
            this.closingIndicator = closingIndicator;
            this.bufferSize = bufferSize;
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
                this.open.subscribe(this.startSubscriber);
                s.request(LongCompanionObject.MAX_VALUE);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.startSubscriber.cancel();
            this.resources.dispose();
            if (this.error.tryAddThrowableOrReport(t)) {
                this.upstreamDone = true;
                drain();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.startSubscriber.cancel();
            this.resources.dispose();
            this.upstreamDone = true;
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.downstreamCancelled.compareAndSet(false, true)) {
                if (this.windowCount.decrementAndGet() == 0) {
                    this.upstream.cancel();
                    this.startSubscriber.cancel();
                    this.resources.dispose();
                    this.error.tryTerminateAndReport();
                    this.upstreamCanceled = true;
                    drain();
                    return;
                }
                this.startSubscriber.cancel();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (this.windowCount.decrementAndGet() == 0) {
                this.upstream.cancel();
                this.startSubscriber.cancel();
                this.resources.dispose();
                this.error.tryTerminateAndReport();
                this.upstreamCanceled = true;
                drain();
            }
        }

        void open(B startValue) {
            this.queue.offer(new WindowStartItem(startValue));
            drain();
        }

        void openError(Throwable t) {
            this.upstream.cancel();
            this.resources.dispose();
            if (this.error.tryAddThrowableOrReport(t)) {
                this.upstreamDone = true;
                drain();
            }
        }

        void openComplete() {
            this.openDone = true;
            drain();
        }

        void close(WindowEndSubscriberIntercept<T, V> what) {
            this.queue.offer(what);
            drain();
        }

        void closeError(Throwable t) {
            this.upstream.cancel();
            this.startSubscriber.cancel();
            this.resources.dispose();
            if (this.error.tryAddThrowableOrReport(t)) {
                this.upstreamDone = true;
                drain();
            }
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            Subscriber<? super Flowable<T>> downstream = this.downstream;
            SimplePlainQueue<Object> queue = this.queue;
            List<UnicastProcessor<T>> windows = this.windows;
            while (true) {
                if (this.upstreamCanceled) {
                    queue.clear();
                    windows.clear();
                } else {
                    boolean isDone = this.upstreamDone;
                    Object o = queue.poll();
                    boolean isEmpty = o == null;
                    if (isDone && (isEmpty || this.error.get() != null)) {
                        terminateDownstream(downstream);
                        this.upstreamCanceled = true;
                    } else if (!isEmpty) {
                        if (o instanceof WindowStartItem) {
                            if (!this.downstreamCancelled.get()) {
                                long emitted = this.emitted;
                                if (this.requested.get() != emitted) {
                                    this.emitted = 1 + emitted;
                                    B startItem = ((WindowStartItem) o).item;
                                    try {
                                        Publisher<V> apply = this.closingIndicator.apply(startItem);
                                        Objects.requireNonNull(apply, "The closingIndicator returned a null Publisher");
                                        Publisher<V> endSource = apply;
                                        this.windowCount.getAndIncrement();
                                        UnicastProcessor<T> newWindow = UnicastProcessor.create(this.bufferSize, this);
                                        WindowEndSubscriberIntercept<T, V> endSubscriber = new WindowEndSubscriberIntercept<>(this, newWindow);
                                        downstream.onNext(endSubscriber);
                                        if (endSubscriber.tryAbandon()) {
                                            newWindow.onComplete();
                                        } else {
                                            windows.add(newWindow);
                                            this.resources.add(endSubscriber);
                                            endSource.subscribe(endSubscriber);
                                        }
                                    } catch (Throwable ex) {
                                        Exceptions.throwIfFatal(ex);
                                        this.upstream.cancel();
                                        this.startSubscriber.cancel();
                                        this.resources.dispose();
                                        Exceptions.throwIfFatal(ex);
                                        this.error.tryAddThrowableOrReport(ex);
                                        this.upstreamDone = true;
                                    }
                                } else {
                                    this.upstream.cancel();
                                    this.startSubscriber.cancel();
                                    this.resources.dispose();
                                    this.error.tryAddThrowableOrReport(new MissingBackpressureException(FlowableWindowTimed.missingBackpressureMessage(emitted)));
                                    this.upstreamDone = true;
                                }
                            }
                        } else if (o instanceof WindowEndSubscriberIntercept) {
                            UnicastProcessor<T> w = ((WindowEndSubscriberIntercept) o).window;
                            windows.remove(w);
                            this.resources.delete((Disposable) o);
                            w.onComplete();
                        } else {
                            for (UnicastProcessor<T> w2 : windows) {
                                w2.onNext(o);
                            }
                        }
                    } else if (this.openDone && windows.size() == 0) {
                        this.upstream.cancel();
                        this.startSubscriber.cancel();
                        this.resources.dispose();
                        terminateDownstream(downstream);
                        this.upstreamCanceled = true;
                    }
                }
                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            }
        }

        void terminateDownstream(Subscriber<?> downstream) {
            Throwable ex = this.error.terminate();
            if (ex == null) {
                for (UnicastProcessor<T> w : this.windows) {
                    w.onComplete();
                }
                downstream.onComplete();
            } else if (ex != ExceptionHelper.TERMINATED) {
                for (UnicastProcessor<T> w2 : this.windows) {
                    w2.onError(ex);
                }
                downstream.onError(ex);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public static final class WindowStartItem<B> {
            final B item;

            WindowStartItem(B item) {
                this.item = item;
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public static final class WindowStartSubscriber<B> extends AtomicReference<Subscription> implements FlowableSubscriber<B> {
            private static final long serialVersionUID = -3326496781427702834L;
            final WindowBoundaryMainSubscriber<?, B, ?> parent;

            WindowStartSubscriber(WindowBoundaryMainSubscriber<?, B, ?> parent) {
                this.parent = parent;
            }

            @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
            public void onSubscribe(Subscription s) {
                if (SubscriptionHelper.setOnce(this, s)) {
                    s.request(LongCompanionObject.MAX_VALUE);
                }
            }

            @Override // org.reactivestreams.Subscriber
            public void onNext(B t) {
                this.parent.open(t);
            }

            @Override // org.reactivestreams.Subscriber
            public void onError(Throwable t) {
                this.parent.openError(t);
            }

            @Override // org.reactivestreams.Subscriber
            public void onComplete() {
                this.parent.openComplete();
            }

            void cancel() {
                SubscriptionHelper.cancel(this);
            }
        }

        /* JADX INFO: Access modifiers changed from: package-private */
        /* loaded from: classes.dex */
        public static final class WindowEndSubscriberIntercept<T, V> extends Flowable<T> implements FlowableSubscriber<V>, Disposable {
            final WindowBoundaryMainSubscriber<T, ?, V> parent;
            final UnicastProcessor<T> window;
            final AtomicReference<Subscription> upstream = new AtomicReference<>();
            final AtomicBoolean once = new AtomicBoolean();

            WindowEndSubscriberIntercept(WindowBoundaryMainSubscriber<T, ?, V> parent, UnicastProcessor<T> window) {
                this.parent = parent;
                this.window = window;
            }

            @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
            public void onSubscribe(Subscription s) {
                if (SubscriptionHelper.setOnce(this.upstream, s)) {
                    s.request(LongCompanionObject.MAX_VALUE);
                }
            }

            @Override // org.reactivestreams.Subscriber
            public void onNext(V t) {
                if (SubscriptionHelper.cancel(this.upstream)) {
                    this.parent.close(this);
                }
            }

            @Override // org.reactivestreams.Subscriber
            public void onError(Throwable t) {
                if (isDisposed()) {
                    RxJavaPlugins.onError(t);
                } else {
                    this.parent.closeError(t);
                }
            }

            @Override // org.reactivestreams.Subscriber
            public void onComplete() {
                this.parent.close(this);
            }

            @Override // io.reactivex.rxjava3.disposables.Disposable
            public void dispose() {
                SubscriptionHelper.cancel(this.upstream);
            }

            @Override // io.reactivex.rxjava3.disposables.Disposable
            public boolean isDisposed() {
                return this.upstream.get() == SubscriptionHelper.CANCELLED;
            }

            @Override // io.reactivex.rxjava3.core.Flowable
            protected void subscribeActual(Subscriber<? super T> s) {
                this.window.subscribe(s);
                this.once.set(true);
            }

            boolean tryAbandon() {
                return !this.once.get() && this.once.compareAndSet(false, true);
            }
        }
    }
}
