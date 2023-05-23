package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.BooleanSupplier;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.internal.util.QueueDrainHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableBuffer<T, C extends Collection<? super T>> extends AbstractFlowableWithUpstream<T, C> {
    final Supplier<C> bufferSupplier;
    final int size;
    final int skip;

    public FlowableBuffer(Flowable<T> source, int size, int skip, Supplier<C> bufferSupplier) {
        super(source);
        this.size = size;
        this.skip = skip;
        this.bufferSupplier = bufferSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    public void subscribeActual(Subscriber<? super C> s) {
        int i = this.size;
        int i2 = this.skip;
        if (i == i2) {
            this.source.subscribe((FlowableSubscriber) new PublisherBufferExactSubscriber(s, this.size, this.bufferSupplier));
        } else if (i2 > i) {
            this.source.subscribe((FlowableSubscriber) new PublisherBufferSkipSubscriber(s, this.size, this.skip, this.bufferSupplier));
        } else {
            this.source.subscribe((FlowableSubscriber) new PublisherBufferOverlappingSubscriber(s, this.size, this.skip, this.bufferSupplier));
        }
    }

    /* loaded from: classes.dex */
    static final class PublisherBufferExactSubscriber<T, C extends Collection<? super T>> implements FlowableSubscriber<T>, Subscription {
        C buffer;
        final Supplier<C> bufferSupplier;
        boolean done;
        final Subscriber<? super C> downstream;
        int index;
        final int size;
        Subscription upstream;

        PublisherBufferExactSubscriber(Subscriber<? super C> actual, int size, Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.bufferSupplier = bufferSupplier;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                this.upstream.request(BackpressureHelper.multiplyCap(n, this.size));
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (this.done) {
                return;
            }
            C b = this.buffer;
            if (b == null) {
                try {
                    C c = this.bufferSupplier.get();
                    Objects.requireNonNull(c, "The bufferSupplier returned a null buffer");
                    b = c;
                    this.buffer = b;
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }
            }
            b.add(t);
            int i = this.index + 1;
            if (i == this.size) {
                this.index = 0;
                this.buffer = null;
                this.downstream.onNext(b);
                return;
            }
            this.index = i;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.buffer = null;
            this.done = true;
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.done) {
                return;
            }
            this.done = true;
            C b = this.buffer;
            this.buffer = null;
            if (b != null) {
                this.downstream.onNext(b);
            }
            this.downstream.onComplete();
        }
    }

    /* loaded from: classes.dex */
    static final class PublisherBufferSkipSubscriber<T, C extends Collection<? super T>> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        private static final long serialVersionUID = -5616169793639412593L;
        C buffer;
        final Supplier<C> bufferSupplier;
        boolean done;
        final Subscriber<? super C> downstream;
        int index;
        final int size;
        final int skip;
        Subscription upstream;

        PublisherBufferSkipSubscriber(Subscriber<? super C> actual, int size, int skip, Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                if (get() == 0 && compareAndSet(0, 1)) {
                    long u = BackpressureHelper.multiplyCap(n, this.size);
                    long v = BackpressureHelper.multiplyCap(this.skip - this.size, n - 1);
                    this.upstream.request(BackpressureHelper.addCap(u, v));
                    return;
                }
                this.upstream.request(BackpressureHelper.multiplyCap(this.skip, n));
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.upstream.cancel();
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (this.done) {
                return;
            }
            C b = this.buffer;
            int i = this.index;
            int i2 = i + 1;
            if (i == 0) {
                try {
                    C c = this.bufferSupplier.get();
                    Objects.requireNonNull(c, "The bufferSupplier returned a null buffer");
                    b = c;
                    this.buffer = b;
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }
            }
            if (b != null) {
                b.add(t);
                if (b.size() == this.size) {
                    this.buffer = null;
                    this.downstream.onNext(b);
                }
            }
            if (i2 == this.skip) {
                i2 = 0;
            }
            this.index = i2;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.buffer = null;
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.done) {
                return;
            }
            this.done = true;
            C b = this.buffer;
            this.buffer = null;
            if (b != null) {
                this.downstream.onNext(b);
            }
            this.downstream.onComplete();
        }
    }

    /* loaded from: classes.dex */
    static final class PublisherBufferOverlappingSubscriber<T, C extends Collection<? super T>> extends AtomicLong implements FlowableSubscriber<T>, Subscription, BooleanSupplier {
        private static final long serialVersionUID = -7370244972039324525L;
        final Supplier<C> bufferSupplier;
        volatile boolean cancelled;
        boolean done;
        final Subscriber<? super C> downstream;
        int index;
        long produced;
        final int size;
        final int skip;
        Subscription upstream;
        final AtomicBoolean once = new AtomicBoolean();
        final ArrayDeque<C> buffers = new ArrayDeque<>();

        PublisherBufferOverlappingSubscriber(Subscriber<? super C> actual, int size, int skip, Supplier<C> bufferSupplier) {
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.bufferSupplier = bufferSupplier;
        }

        @Override // io.reactivex.rxjava3.functions.BooleanSupplier
        public boolean getAsBoolean() {
            return this.cancelled;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (!SubscriptionHelper.validate(n) || QueueDrainHelper.postCompleteRequest(n, this.downstream, this.buffers, this, this)) {
                return;
            }
            if (!this.once.get() && this.once.compareAndSet(false, true)) {
                long u = BackpressureHelper.multiplyCap(this.skip, n - 1);
                long r = BackpressureHelper.addCap(this.size, u);
                this.upstream.request(r);
                return;
            }
            long r2 = BackpressureHelper.multiplyCap(this.skip, n);
            this.upstream.request(r2);
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            this.upstream.cancel();
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (this.done) {
                return;
            }
            ArrayDeque<C> bs = this.buffers;
            int i = this.index;
            int i2 = i + 1;
            if (i == 0) {
                try {
                    C b = this.bufferSupplier.get();
                    Objects.requireNonNull(b, "The bufferSupplier returned a null buffer");
                    bs.offer(b);
                } catch (Throwable e) {
                    Exceptions.throwIfFatal(e);
                    cancel();
                    onError(e);
                    return;
                }
            }
            C b2 = bs.peek();
            if (b2.size() + 1 == this.size) {
                bs.poll();
                b2.add(t);
                this.produced++;
                this.downstream.onNext(b2);
            }
            Iterator<C> it = bs.iterator();
            while (it.hasNext()) {
                C b0 = it.next();
                b0.add(t);
            }
            if (i2 == this.skip) {
                i2 = 0;
            }
            this.index = i2;
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.buffers.clear();
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (this.done) {
                return;
            }
            this.done = true;
            long p = this.produced;
            if (p != 0) {
                BackpressureHelper.produced(this, p);
            }
            QueueDrainHelper.postComplete(this.downstream, this.buffers, this, this);
        }
    }
}