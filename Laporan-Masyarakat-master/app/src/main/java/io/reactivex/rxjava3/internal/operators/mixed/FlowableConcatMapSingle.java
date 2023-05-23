package io.reactivex.rxjava3.internal.operators.mixed;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableConcatMapSingle<T, R> extends Flowable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends SingleSource<? extends R>> mapper;
    final int prefetch;
    final Flowable<T> source;

    public FlowableConcatMapSingle(Flowable<T> source, Function<? super T, ? extends SingleSource<? extends R>> mapper, ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super R> s) {
        this.source.subscribe((FlowableSubscriber) new ConcatMapSingleSubscriber(s, this.mapper, this.prefetch, this.errorMode));
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ConcatMapSingleSubscriber<T, R> extends AtomicInteger implements FlowableSubscriber<T>, Subscription {
        static final int STATE_ACTIVE = 1;
        static final int STATE_INACTIVE = 0;
        static final int STATE_RESULT_VALUE = 2;
        private static final long serialVersionUID = -9140123220065488293L;
        volatile boolean cancelled;
        int consumed;
        volatile boolean done;
        final Subscriber<? super R> downstream;
        long emitted;
        final ErrorMode errorMode;
        R item;
        final Function<? super T, ? extends SingleSource<? extends R>> mapper;
        final int prefetch;
        final SimplePlainQueue<T> queue;
        volatile int state;
        Subscription upstream;
        final AtomicLong requested = new AtomicLong();
        final AtomicThrowable errors = new AtomicThrowable();
        final ConcatMapSingleObserver<R> inner = new ConcatMapSingleObserver<>(this);

        public ConcatMapSingleSubscriber(Subscriber<? super R> downstream, Function<? super T, ? extends SingleSource<? extends R>> mapper, int prefetch, ErrorMode errorMode) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.prefetch = prefetch;
            this.errorMode = errorMode;
            this.queue = new SpscArrayQueue(prefetch);
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
                s.request(this.prefetch);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (!this.queue.offer(t)) {
                this.upstream.cancel();
                onError(new MissingBackpressureException("queue full?!"));
                return;
            }
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.errors.tryAddThrowableOrReport(t)) {
                if (this.errorMode == ErrorMode.IMMEDIATE) {
                    this.inner.dispose();
                }
                this.done = true;
                drain();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            BackpressureHelper.add(this.requested, n);
            drain();
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            this.upstream.cancel();
            this.inner.dispose();
            this.errors.tryTerminateAndReport();
            if (getAndIncrement() == 0) {
                this.queue.clear();
                this.item = null;
            }
        }

        void innerSuccess(R item) {
            this.item = item;
            this.state = 2;
            drain();
        }

        void innerError(Throwable ex) {
            if (this.errors.tryAddThrowableOrReport(ex)) {
                if (this.errorMode != ErrorMode.END) {
                    this.upstream.cancel();
                }
                this.state = 0;
                drain();
            }
        }

        /* JADX WARN: Code restructure failed: missing block: B:78:0x0038, code lost:
            r4.clear();
            r17.item = null;
            r5.tryTerminateConsumer(r2);
         */
        /* JADX WARN: Code restructure failed: missing block: B:79:0x0040, code lost:
            return;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drain() {
            /*
                r17 = this;
                r1 = r17
                int r0 = r17.getAndIncrement()
                if (r0 == 0) goto L9
                return
            L9:
                r0 = 1
                org.reactivestreams.Subscriber<? super R> r2 = r1.downstream
                io.reactivex.rxjava3.internal.util.ErrorMode r3 = r1.errorMode
                io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue<T> r4 = r1.queue
                io.reactivex.rxjava3.internal.util.AtomicThrowable r5 = r1.errors
                java.util.concurrent.atomic.AtomicLong r6 = r1.requested
                int r7 = r1.prefetch
                int r8 = r7 >> 1
                int r7 = r7 - r8
                r8 = r0
            L1a:
                boolean r0 = r1.cancelled
                r9 = 0
                if (r0 == 0) goto L26
                r4.clear()
                r1.item = r9
                goto Lb6
            L26:
                int r10 = r1.state
                java.lang.Object r0 = r5.get()
                if (r0 == 0) goto L41
                io.reactivex.rxjava3.internal.util.ErrorMode r0 = io.reactivex.rxjava3.internal.util.ErrorMode.IMMEDIATE
                if (r3 == r0) goto L38
                io.reactivex.rxjava3.internal.util.ErrorMode r0 = io.reactivex.rxjava3.internal.util.ErrorMode.BOUNDARY
                if (r3 != r0) goto L41
                if (r10 != 0) goto L41
            L38:
                r4.clear()
                r1.item = r9
                r5.tryTerminateConsumer(r2)
                return
            L41:
                r0 = 0
                if (r10 != 0) goto L99
                boolean r9 = r1.done
                java.lang.Object r11 = r4.poll()
                r12 = 1
                if (r11 != 0) goto L4f
                r13 = 1
                goto L50
            L4f:
                r13 = 0
            L50:
                if (r9 == 0) goto L58
                if (r13 == 0) goto L58
                r5.tryTerminateConsumer(r2)
                return
            L58:
                if (r13 == 0) goto L5b
                goto Lb6
            L5b:
                int r14 = r1.consumed
                int r14 = r14 + r12
                if (r14 != r7) goto L6b
                r1.consumed = r0
                org.reactivestreams.Subscription r0 = r1.upstream
                r16 = r13
                long r12 = (long) r7
                r0.request(r12)
                goto L6f
            L6b:
                r16 = r13
                r1.consumed = r14
            L6f:
                io.reactivex.rxjava3.functions.Function<? super T, ? extends io.reactivex.rxjava3.core.SingleSource<? extends R>> r0 = r1.mapper     // Catch: java.lang.Throwable -> L86
                java.lang.Object r0 = r0.apply(r11)     // Catch: java.lang.Throwable -> L86
                java.lang.String r12 = "The mapper returned a null SingleSource"
                java.util.Objects.requireNonNull(r0, r12)     // Catch: java.lang.Throwable -> L86
                io.reactivex.rxjava3.core.SingleSource r0 = (io.reactivex.rxjava3.core.SingleSource) r0     // Catch: java.lang.Throwable -> L86
                r12 = 1
                r1.state = r12
                io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingle$ConcatMapSingleSubscriber$ConcatMapSingleObserver<R> r12 = r1.inner
                r0.subscribe(r12)
                goto Lb6
            L86:
                r0 = move-exception
                io.reactivex.rxjava3.exceptions.Exceptions.throwIfFatal(r0)
                org.reactivestreams.Subscription r12 = r1.upstream
                r12.cancel()
                r4.clear()
                r5.tryAddThrowableOrReport(r0)
                r5.tryTerminateConsumer(r2)
                return
            L99:
                r11 = 2
                if (r10 != r11) goto Lb6
                long r11 = r1.emitted
                long r13 = r6.get()
                int r15 = (r11 > r13 ? 1 : (r11 == r13 ? 0 : -1))
                if (r15 == 0) goto Lb6
                R r13 = r1.item
                r1.item = r9
                r2.onNext(r13)
                r14 = 1
                long r14 = r14 + r11
                r1.emitted = r14
                r1.state = r0
                goto L1a
            Lb6:
                int r0 = -r8
                int r8 = r1.addAndGet(r0)
                if (r8 != 0) goto Lbf
            Lbe:
                return
            Lbf:
                goto L1a
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.rxjava3.internal.operators.mixed.FlowableConcatMapSingle.ConcatMapSingleSubscriber.drain():void");
        }

        /* loaded from: classes.dex */
        public static final class ConcatMapSingleObserver<R> extends AtomicReference<Disposable> implements SingleObserver<R> {
            private static final long serialVersionUID = -3051469169682093892L;
            final ConcatMapSingleSubscriber<?, R> parent;

            ConcatMapSingleObserver(ConcatMapSingleSubscriber<?, R> parent) {
                this.parent = parent;
            }

            @Override // io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override // io.reactivex.rxjava3.core.SingleObserver
            public void onSuccess(R t) {
                this.parent.innerSuccess(t);
            }

            @Override // io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
            public void onError(Throwable e) {
                this.parent.innerError(e);
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
