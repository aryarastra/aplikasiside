package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.fuseable.QueueSubscription;
import io.reactivex.rxjava3.internal.fuseable.SimpleQueue;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionArbiter;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import io.reactivex.rxjava3.internal.util.HalfSerializer;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableConcatMap<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends Publisher<? extends R>> mapper;
    final int prefetch;

    /* loaded from: classes.dex */
    interface ConcatMapSupport<T> {
        void innerComplete();

        void innerError(Throwable e);

        void innerNext(T value);
    }

    public FlowableConcatMap(Flowable<T> source, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, ErrorMode errorMode) {
        super(source);
        this.mapper = mapper;
        this.prefetch = prefetch;
        this.errorMode = errorMode;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* renamed from: io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap$1  reason: invalid class name */
    /* loaded from: classes.dex */
    public static /* synthetic */ class AnonymousClass1 {
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

    public static <T, R> Subscriber<T> subscribe(Subscriber<? super R> s, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, ErrorMode errorMode) {
        switch (AnonymousClass1.$SwitchMap$io$reactivex$rxjava3$internal$util$ErrorMode[errorMode.ordinal()]) {
            case 1:
                return new ConcatMapDelayed(s, mapper, prefetch, false);
            case 2:
                return new ConcatMapDelayed(s, mapper, prefetch, true);
            default:
                return new ConcatMapImmediate(s, mapper, prefetch);
        }
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super R> s) {
        if (FlowableScalarXMap.tryScalarXMapSubscribe(this.source, s, this.mapper)) {
            return;
        }
        this.source.subscribe(subscribe(s, this.mapper, this.prefetch, this.errorMode));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static abstract class BaseConcatMapSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, ConcatMapSupport<R>, Subscription {
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
        final ConcatMapInner<R> inner = new ConcatMapInner<>(this);
        final AtomicThrowable errors = new AtomicThrowable();

        abstract void drain();

        abstract void subscribeActual();

        BaseConcatMapSubscriber(Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch) {
            this.mapper = mapper;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
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
                        drain();
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
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public final void onComplete() {
            this.done = true;
            drain();
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public final void innerComplete() {
            this.active = false;
            drain();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ConcatMapImmediate<T, R> extends BaseConcatMapSubscriber<T, R> {
        private static final long serialVersionUID = 7898995095634264146L;
        final Subscriber<? super R> downstream;
        final AtomicInteger wip;

        ConcatMapImmediate(Subscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch) {
            super(mapper, prefetch);
            this.downstream = actual;
            this.wip = new AtomicInteger();
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.BaseConcatMapSubscriber
        void subscribeActual() {
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.inner.cancel();
            HalfSerializer.onError(this.downstream, t, this, this.errors);
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerNext(R value) {
            HalfSerializer.onNext(this.downstream, value, this, this.errors);
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.ConcatMapSupport
        public void innerError(Throwable e) {
            this.upstream.cancel();
            HalfSerializer.onError(this.downstream, e, this, this.errors);
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
                this.errors.tryTerminateAndReport();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.BaseConcatMapSubscriber
        void drain() {
            if (this.wip.getAndIncrement() == 0) {
                while (!this.cancelled) {
                    if (!this.active) {
                        boolean d = this.done;
                        try {
                            T v = this.queue.poll();
                            boolean empty = v == null;
                            if (d && empty) {
                                this.downstream.onComplete();
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
                                            R vr = supplier.get();
                                            if (vr == null) {
                                                continue;
                                            } else if (this.inner.isUnbounded()) {
                                                if (!HalfSerializer.onNext(this.downstream, vr, this, this.errors)) {
                                                    return;
                                                }
                                            } else {
                                                this.active = true;
                                                this.inner.setSubscription(new WeakScalarSubscription(vr, this.inner));
                                            }
                                        } catch (Throwable e) {
                                            Exceptions.throwIfFatal(e);
                                            this.upstream.cancel();
                                            this.errors.tryAddThrowableOrReport(e);
                                            this.errors.tryTerminateConsumer(this.downstream);
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
                                    return;
                                }
                            }
                        } catch (Throwable e3) {
                            Exceptions.throwIfFatal(e3);
                            this.upstream.cancel();
                            this.errors.tryAddThrowableOrReport(e3);
                            this.errors.tryTerminateConsumer(this.downstream);
                            return;
                        }
                    }
                    if (this.wip.decrementAndGet() == 0) {
                        return;
                    }
                }
            }
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class WeakScalarSubscription<T> implements Subscription {
        final Subscriber<? super T> downstream;
        boolean once;
        final T value;

        /* JADX INFO: Access modifiers changed from: package-private */
        public WeakScalarSubscription(T value, Subscriber<? super T> downstream) {
            this.value = value;
            this.downstream = downstream;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (n > 0 && !this.once) {
                this.once = true;
                Subscriber<? super T> a = this.downstream;
                a.onNext((T) this.value);
                a.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ConcatMapDelayed<T, R> extends BaseConcatMapSubscriber<T, R> {
        private static final long serialVersionUID = -2945777694260521066L;
        final Subscriber<? super R> downstream;
        final boolean veryEnd;

        ConcatMapDelayed(Subscriber<? super R> actual, Function<? super T, ? extends Publisher<? extends R>> mapper, int prefetch, boolean veryEnd) {
            super(mapper, prefetch);
            this.downstream = actual;
            this.veryEnd = veryEnd;
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.BaseConcatMapSubscriber
        void subscribeActual() {
            this.downstream.onSubscribe(this);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.errors.tryAddThrowableOrReport(t)) {
                this.done = true;
                drain();
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
                drain();
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
                this.errors.tryTerminateAndReport();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.flowable.FlowableConcatMap.BaseConcatMapSubscriber
        void drain() {
            Object obj;
            if (getAndIncrement() == 0) {
                while (!this.cancelled) {
                    if (!this.active) {
                        boolean d = this.done;
                        if (d && !this.veryEnd) {
                            Throwable ex = this.errors.get();
                            if (ex != null) {
                                this.errors.tryTerminateConsumer(this.downstream);
                                return;
                            }
                        }
                        try {
                            T v = this.queue.poll();
                            boolean empty = v == null;
                            if (d && empty) {
                                this.errors.tryTerminateConsumer(this.downstream);
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
                                                return;
                                            }
                                            obj = null;
                                        }
                                        if (obj == null) {
                                            continue;
                                        } else if (this.inner.isUnbounded()) {
                                            this.downstream.onNext(obj);
                                        } else {
                                            this.active = true;
                                            this.inner.setSubscription(new WeakScalarSubscription(obj, this.inner));
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
                                    return;
                                }
                            }
                        } catch (Throwable e3) {
                            Exceptions.throwIfFatal(e3);
                            this.upstream.cancel();
                            this.errors.tryAddThrowableOrReport(e3);
                            this.errors.tryTerminateConsumer(this.downstream);
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

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ConcatMapInner<R> extends SubscriptionArbiter implements FlowableSubscriber<R> {
        private static final long serialVersionUID = 897683679971470653L;
        final ConcatMapSupport<R> parent;
        long produced;

        /* JADX INFO: Access modifiers changed from: package-private */
        public ConcatMapInner(ConcatMapSupport<R> parent) {
            super(false);
            this.parent = parent;
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            setSubscription(s);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(R t) {
            this.produced++;
            this.parent.innerNext(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            long p = this.produced;
            if (p != 0) {
                this.produced = 0L;
                produced(p);
            }
            this.parent.innerError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            long p = this.produced;
            if (p != 0) {
                this.produced = 0L;
                produced(p);
            }
            this.parent.innerComplete();
        }
    }
}
