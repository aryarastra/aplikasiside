package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.fuseable.QueueSubscription;
import io.reactivex.rxjava3.internal.fuseable.SimpleQueue;
import io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableConcatMapScheduler<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int prefetch;
    final Scheduler scheduler;

    public FlowableConcatMapScheduler(Flowable<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, ErrorMode errorMode, Scheduler scheduler) {
        super(source);
        this.mapper = mapper;
        this.prefetch = prefetch;
        this.errorMode = errorMode;
        this.scheduler = scheduler;
    }

    /* renamed from: io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapScheduler$1  reason: invalid class name */
    /* loaded from: classes.dex */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$io$reactivex$rxjava3$internal$util$ErrorMode;

        static {
            int[] iArr = new int[ErrorMode.values().length];
            $SwitchMap$io$reactivex$rxjava3$internal$util$ErrorMode = iArr;
            try {
                iArr[ErrorMode.BOUNDARY.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$io$reactivex$rxjava3$internal$util$ErrorMode[ErrorMode.END.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
        }
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super R> s) {
        switch (AnonymousClass1.$SwitchMap$io$reactivex$rxjava3$internal$util$ErrorMode[this.errorMode.ordinal()]) {
            case 1:
                this.source.subscribe((FlowableSubscriber) new ConcatMapDelayed(s, this.mapper, this.prefetch, false, this.scheduler.createWorker()));
                return;
            case 2:
                this.source.subscribe((FlowableSubscriber) new ConcatMapDelayed(s, this.mapper, this.prefetch, true, this.scheduler.createWorker()));
                return;
            default:
                this.source.subscribe((FlowableSubscriber) new ConcatMapImmediate(s, this.mapper, this.prefetch, this.scheduler.createWorker()));
                return;
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static abstract class BaseConcatMapSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, FlowableConcatMap.ConcatMapSupport<R>, Subscription, Runnable {
        private static final long serialVersionUID = -3511336836796789179L;
        volatile boolean active;
        volatile boolean cancelled;
        int consumed;
        volatile boolean done;
        final int limit;
        final Function<? super T, ? extends Publisher<? extends R>> mapper;
        final int prefetch;
        SimpleQueue<T> queue;
        int sourceMode;
        Subscription upstream;
        final Scheduler.Worker worker;
        final FlowableConcatMap.ConcatMapInner<R> inner = new FlowableConcatMap.ConcatMapInner<>(this);
        final AtomicThrowable errors = new AtomicThrowable();

        abstract void schedule();

        abstract void subscribeActual();

        BaseConcatMapSubscriber(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, Scheduler.Worker worker) {
            this.mapper = mapper;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
            this.worker = worker;
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public final void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                if (s instanceof QueueSubscription) {
                    QueueSubscription<T> f = (QueueSubscription) s;
                    int m = f.requestFusion(7);
                    if (m == 1) {
                        this.sourceMode = m;
                        this.queue = f;
                        this.done = true;
                        subscribeActual();
                        schedule();
                        return;
                    } else if (m == 2) {
                        this.sourceMode = m;
                        this.queue = f;
                        subscribeActual();
                        s.request(this.prefetch);
                        return;
                    }
                }
                this.queue = new SpscArrayQueue(this.prefetch);
                subscribeActual();
                s.request(this.prefetch);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public final void onNext(T t) {
            if (this.sourceMode != 2 && !this.queue.offer(t)) {
                this.upstream.cancel();
                onError(new IllegalStateException("Queue full?!"));
                return;
            }
            schedule();
        }

        @Override // org.reactivestreams.Subscriber
        public final void onComplete() {
            this.done = true;
            schedule();
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public final void innerComplete() {
            this.active = false;
            schedule();
        }
    }

    /* loaded from: classes.dex */
    static final class ConcatMapImmediate<T, R> extends BaseConcatMapSubscriber<T, R> {
        private static final long serialVersionUID = 7898995095634264146L;
        final Subscriber<? super R> downstream;
        final AtomicInteger wip;

        ConcatMapImmediate(Subscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, Scheduler.Worker worker) {
            super(mapper, prefetch, worker);
            this.downstream = actual;
            this.wip = new AtomicInteger();
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapScheduler.BaseConcatMapSubscriber
        void subscribeActual() {
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.errors.tryAddThrowableOrReport(t)) {
                this.inner.cancel();
                if (getAndIncrement() == 0) {
                    this.errors.tryTerminateConsumer(this.downstream);
                    this.worker.dispose();
                }
            }
        }

        boolean tryEnter() {
            return get() == 0 && compareAndSet(0, 1);
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerNext(R value) {
            if (tryEnter()) {
                this.downstream.onNext(value);
                if (compareAndSet(1, 0)) {
                    return;
                }
                this.errors.tryTerminateConsumer(this.downstream);
                this.worker.dispose();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerError(Throwable e) {
            if (this.errors.tryAddThrowableOrReport(e)) {
                this.upstream.cancel();
                if (getAndIncrement() == 0) {
                    this.errors.tryTerminateConsumer(this.downstream);
                    this.worker.dispose();
                }
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            this.inner.request(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.inner.cancel();
                this.upstream.cancel();
                this.worker.dispose();
                this.errors.tryTerminateAndReport();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapScheduler.BaseConcatMapSubscriber
        void schedule() {
            if (this.wip.getAndIncrement() == 0) {
                this.worker.schedule(this);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            while (!this.cancelled) {
                if (!this.active) {
                    boolean d = this.done;
                    try {
                        T v = this.queue.poll();
                        boolean empty = v == null;
                        if (d && empty) {
                            this.downstream.onComplete();
                            this.worker.dispose();
                            return;
                        } else if (!empty) {
                            try {
                                Publisher<? extends R> apply = this.mapper.apply(v);
                                Objects.requireNonNull(apply, "The mapper returned a null Publisher");
                                Publisher<? extends R> p = apply;
                                if (this.sourceMode != 1) {
                                    int c = this.consumed + 1;
                                    if (c == this.limit) {
                                        this.consumed = 0;
                                        this.upstream.request(c);
                                    } else {
                                        this.consumed = c;
                                    }
                                }
                                if (p instanceof Supplier) {
                                    Supplier<R> supplier = (Supplier) p;
                                    try {
                                        Object obj = supplier.get();
                                        if (obj != null && !this.cancelled) {
                                            if (this.inner.isUnbounded()) {
                                                if (tryEnter()) {
                                                    this.downstream.onNext(obj);
                                                    if (!compareAndSet(1, 0)) {
                                                        this.errors.tryTerminateConsumer(this.downstream);
                                                        this.worker.dispose();
                                                        return;
                                                    }
                                                } else {
                                                    continue;
                                                }
                                            } else {
                                                this.active = true;
                                                this.inner.setSubscription(new FlowableConcatMap.WeakScalarSubscription(obj, this.inner));
                                            }
                                        }
                                    } catch (Throwable e) {
                                        Exceptions.throwIfFatal(e);
                                        this.upstream.cancel();
                                        this.errors.tryAddThrowableOrReport(e);
                                        this.errors.tryTerminateConsumer(this.downstream);
                                        this.worker.dispose();
                                        return;
                                    }
                                } else {
                                    this.active = true;
                                    p.subscribe(this.inner);
                                }
                            } catch (Throwable e2) {
                                Exceptions.throwIfFatal(e2);
                                this.upstream.cancel();
                                this.errors.tryAddThrowableOrReport(e2);
                                this.errors.tryTerminateConsumer(this.downstream);
                                this.worker.dispose();
                                return;
                            }
                        }
                    } catch (Throwable e3) {
                        Exceptions.throwIfFatal(e3);
                        this.upstream.cancel();
                        this.errors.tryAddThrowableOrReport(e3);
                        this.errors.tryTerminateConsumer(this.downstream);
                        this.worker.dispose();
                        return;
                    }
                }
                if (this.wip.decrementAndGet() == 0) {
                    return;
                }
            }
        }
    }

    /* loaded from: classes.dex */
    static final class ConcatMapDelayed<T, R> extends BaseConcatMapSubscriber<T, R> {
        private static final long serialVersionUID = -2945777694260521066L;
        final Subscriber<? super R> downstream;
        final boolean veryEnd;

        ConcatMapDelayed(Subscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, boolean veryEnd, Scheduler.Worker worker) {
            super(mapper, prefetch, worker);
            this.downstream = actual;
            this.veryEnd = veryEnd;
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapScheduler.BaseConcatMapSubscriber
        void subscribeActual() {
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.errors.tryAddThrowableOrReport(t)) {
                this.done = true;
                schedule();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerNext(R value) {
            this.downstream.onNext(value);
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerError(Throwable e) {
            if (this.errors.tryAddThrowableOrReport(e)) {
                if (!this.veryEnd) {
                    this.upstream.cancel();
                    this.done = true;
                }
                this.active = false;
                schedule();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            this.inner.request(n);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.inner.cancel();
                this.upstream.cancel();
                this.worker.dispose();
                this.errors.tryTerminateAndReport();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMapScheduler.BaseConcatMapSubscriber
        void schedule() {
            if (getAndIncrement() == 0) {
                this.worker.schedule(this);
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            Object obj;
            while (!this.cancelled) {
                if (!this.active) {
                    boolean d = this.done;
                    if (d && !this.veryEnd) {
                        Throwable ex = this.errors.get();
                        if (ex != null) {
                            this.errors.tryTerminateConsumer(this.downstream);
                            this.worker.dispose();
                            return;
                        }
                    }
                    try {
                        T v = this.queue.poll();
                        boolean empty = v == null;
                        if (d && empty) {
                            this.errors.tryTerminateConsumer(this.downstream);
                            this.worker.dispose();
                            return;
                        } else if (!empty) {
                            try {
                                Publisher<? extends R> apply = this.mapper.apply(v);
                                Objects.requireNonNull(apply, "The mapper returned a null Publisher");
                                Publisher<? extends R> p = apply;
                                if (this.sourceMode != 1) {
                                    int c = this.consumed + 1;
                                    if (c == this.limit) {
                                        this.consumed = 0;
                                        this.upstream.request(c);
                                    } else {
                                        this.consumed = c;
                                    }
                                }
                                if (p instanceof Supplier) {
                                    Supplier<R> supplier = (Supplier) p;
                                    try {
                                        obj = supplier.get();
                                    } catch (Throwable e) {
                                        Exceptions.throwIfFatal(e);
                                        this.errors.tryAddThrowableOrReport(e);
                                        if (!this.veryEnd) {
                                            this.upstream.cancel();
                                            this.errors.tryTerminateConsumer(this.downstream);
                                            this.worker.dispose();
                                            return;
                                        }
                                        obj = null;
                                    }
                                    if (obj != null && !this.cancelled) {
                                        if (this.inner.isUnbounded()) {
                                            this.downstream.onNext(obj);
                                        } else {
                                            this.active = true;
                                            this.inner.setSubscription(new FlowableConcatMap.WeakScalarSubscription(obj, this.inner));
                                        }
                                    }
                                } else {
                                    this.active = true;
                                    p.subscribe(this.inner);
                                }
                            } catch (Throwable e2) {
                                Exceptions.throwIfFatal(e2);
                                this.upstream.cancel();
                                this.errors.tryAddThrowableOrReport(e2);
                                this.errors.tryTerminateConsumer(this.downstream);
                                this.worker.dispose();
                                return;
                            }
                        }
                    } catch (Throwable e3) {
                        Exceptions.throwIfFatal(e3);
                        this.upstream.cancel();
                        this.errors.tryAddThrowableOrReport(e3);
                        this.errors.tryTerminateConsumer(this.downstream);
                        this.worker.dispose();
                        return;
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            }
        }
    }
}
