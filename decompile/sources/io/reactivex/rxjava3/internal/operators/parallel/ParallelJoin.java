package io.reactivex.rxjava3.internal.operators.parallel;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue;
import io.reactivex.rxjava3.internal.queue.SpscArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.parallel.ParallelFlowable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import kotlin.jvm.internal.LongCompanionObject;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class ParallelJoin<T> extends Flowable<T> {
    final boolean delayErrors;
    final int prefetch;
    final ParallelFlowable<? extends T> source;

    public ParallelJoin(ParallelFlowable<? extends T> source, int prefetch, boolean delayErrors) {
        this.source = source;
        this.prefetch = prefetch;
        this.delayErrors = delayErrors;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super T> s) {
        JoinSubscriptionBase<T> parent;
        if (this.delayErrors) {
            parent = new JoinSubscriptionDelayError<>(s, this.source.parallelism(), this.prefetch);
        } else {
            parent = new JoinSubscription<>(s, this.source.parallelism(), this.prefetch);
        }
        s.onSubscribe(parent);
        this.source.subscribe(parent.subscribers);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static abstract class JoinSubscriptionBase<T> extends AtomicInteger implements Subscription {
        private static final long serialVersionUID = 3100232009247827843L;
        volatile boolean cancelled;
        final Subscriber<? super T> downstream;
        final JoinInnerSubscriber<T>[] subscribers;
        final AtomicThrowable errors = new AtomicThrowable();
        final AtomicLong requested = new AtomicLong();
        final AtomicInteger done = new AtomicInteger();

        abstract void drain();

        abstract void onComplete();

        abstract void onError(Throwable e);

        abstract void onNext(JoinInnerSubscriber<T> inner, T value);

        JoinSubscriptionBase(Subscriber<? super T> actual, int n, int prefetch) {
            this.downstream = actual;
            JoinInnerSubscriber<T>[] a = new JoinInnerSubscriber[n];
            for (int i = 0; i < n; i++) {
                a[i] = new JoinInnerSubscriber<>(this, prefetch);
            }
            this.subscribers = a;
            this.done.lazySet(n);
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelAll();
                if (getAndIncrement() == 0) {
                    cleanup();
                }
            }
        }

        void cancelAll() {
            JoinInnerSubscriber<T>[] joinInnerSubscriberArr;
            for (JoinInnerSubscriber<T> s : this.subscribers) {
                s.cancel();
            }
        }

        void cleanup() {
            JoinInnerSubscriber<T>[] joinInnerSubscriberArr;
            for (JoinInnerSubscriber<T> s : this.subscribers) {
                s.queue = null;
            }
        }
    }

    /* loaded from: classes.dex */
    static final class JoinSubscription<T> extends JoinSubscriptionBase<T> {
        private static final long serialVersionUID = 6312374661811000451L;

        JoinSubscription(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(value);
                    if (this.requested.get() != LongCompanionObject.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    inner.request(1L);
                } else {
                    SimplePlainQueue<T> q = inner.getQueue();
                    if (!q.offer(value)) {
                        cancelAll();
                        MissingBackpressureException missingBackpressureException = new MissingBackpressureException("Queue full?!");
                        if (this.errors.compareAndSet(null, missingBackpressureException)) {
                            this.downstream.onError(missingBackpressureException);
                            return;
                        } else {
                            RxJavaPlugins.onError(missingBackpressureException);
                            return;
                        }
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q2 = inner.getQueue();
                if (!q2.offer(value)) {
                    cancelAll();
                    onError(new MissingBackpressureException("Queue full?!"));
                    return;
                } else if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onError(Throwable e) {
            if (this.errors.compareAndSet(null, e)) {
                cancelAll();
                drain();
            } else if (e != this.errors.get()) {
                RxJavaPlugins.onError(e);
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        public void onComplete() {
            this.done.decrementAndGet();
            drain();
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            drainLoop();
        }

        /* JADX WARN: Code restructure failed: missing block: B:29:0x005e, code lost:
            if (r12 == false) goto L76;
         */
        /* JADX WARN: Code restructure failed: missing block: B:30:0x0060, code lost:
            if (r13 == false) goto L73;
         */
        /* JADX WARN: Code restructure failed: missing block: B:31:0x0062, code lost:
            r4.onComplete();
         */
        /* JADX WARN: Code restructure failed: missing block: B:32:0x0065, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:33:0x0066, code lost:
            if (r13 == false) goto L3;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                r20 = this;
                r0 = r20
                r1 = 1
                io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin$JoinInnerSubscriber<T>[] r2 = r0.subscribers
                int r3 = r2.length
                org.reactivestreams.Subscriber<? super T> r4 = r0.downstream
            L8:
                java.util.concurrent.atomic.AtomicLong r5 = r0.requested
                long r5 = r5.get()
                r7 = 0
            L10:
                int r11 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r11 == 0) goto L6a
                boolean r11 = r0.cancelled
                if (r11 == 0) goto L1c
                r20.cleanup()
                return
            L1c:
                io.reactivex.rxjava3.internal.util.AtomicThrowable r11 = r0.errors
                java.lang.Object r11 = r11.get()
                java.lang.Throwable r11 = (java.lang.Throwable) r11
                if (r11 == 0) goto L2d
                r20.cleanup()
                r4.onError(r11)
                return
            L2d:
                java.util.concurrent.atomic.AtomicInteger r12 = r0.done
                int r12 = r12.get()
                if (r12 != 0) goto L37
                r12 = 1
                goto L38
            L37:
                r12 = 0
            L38:
                r13 = 1
                r14 = 0
            L3a:
                int r15 = r2.length
                if (r14 >= r15) goto L5e
                r15 = r2[r14]
                io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue<T> r9 = r15.queue
                if (r9 == 0) goto L5b
                java.lang.Object r10 = r9.poll()
                if (r10 == 0) goto L5b
                r13 = 0
                r4.onNext(r10)
                r15.requestOne()
                r17 = 1
                long r17 = r7 + r17
                r7 = r17
                int r19 = (r17 > r5 ? 1 : (r17 == r5 ? 0 : -1))
                if (r19 != 0) goto L5b
                goto L6a
            L5b:
                int r14 = r14 + 1
                goto L3a
            L5e:
                if (r12 == 0) goto L66
                if (r13 == 0) goto L66
                r4.onComplete()
                return
            L66:
                if (r13 == 0) goto L69
                goto L6a
            L69:
                goto L10
            L6a:
                int r9 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r9 != 0) goto Lb3
                boolean r9 = r0.cancelled
                if (r9 == 0) goto L76
                r20.cleanup()
                return
            L76:
                io.reactivex.rxjava3.internal.util.AtomicThrowable r9 = r0.errors
                java.lang.Object r9 = r9.get()
                java.lang.Throwable r9 = (java.lang.Throwable) r9
                if (r9 == 0) goto L87
                r20.cleanup()
                r4.onError(r9)
                return
            L87:
                java.util.concurrent.atomic.AtomicInteger r10 = r0.done
                int r10 = r10.get()
                if (r10 != 0) goto L92
                r16 = 1
                goto L94
            L92:
                r16 = 0
            L94:
                r10 = r16
                r11 = 1
                r12 = 0
            L98:
                if (r12 >= r3) goto Lab
                r13 = r2[r12]
                io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue<T> r14 = r13.queue
                if (r14 == 0) goto La8
                boolean r15 = r14.isEmpty()
                if (r15 != 0) goto La8
                r11 = 0
                goto Lab
            La8:
                int r12 = r12 + 1
                goto L98
            Lab:
                if (r10 == 0) goto Lb3
                if (r11 == 0) goto Lb3
                r4.onComplete()
                return
            Lb3:
                r9 = 0
                int r11 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
                if (r11 == 0) goto Lbe
                java.util.concurrent.atomic.AtomicLong r9 = r0.requested
                io.reactivex.rxjava3.internal.util.BackpressureHelper.produced(r9, r7)
            Lbe:
                int r9 = -r1
                int r1 = r0.addAndGet(r9)
                if (r1 != 0) goto Lc7
            Lc6:
                return
            Lc7:
                goto L8
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscription.drainLoop():void");
        }
    }

    /* loaded from: classes.dex */
    static final class JoinSubscriptionDelayError<T> extends JoinSubscriptionBase<T> {
        private static final long serialVersionUID = -5737965195918321883L;

        JoinSubscriptionDelayError(Subscriber<? super T> actual, int n, int prefetch) {
            super(actual, n, prefetch);
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onNext(JoinInnerSubscriber<T> inner, T value) {
            if (get() == 0 && compareAndSet(0, 1)) {
                if (this.requested.get() != 0) {
                    this.downstream.onNext(value);
                    if (this.requested.get() != LongCompanionObject.MAX_VALUE) {
                        this.requested.decrementAndGet();
                    }
                    inner.request(1L);
                } else {
                    SimplePlainQueue<T> q = inner.getQueue();
                    if (!q.offer(value)) {
                        inner.cancel();
                        this.errors.tryAddThrowableOrReport(new MissingBackpressureException("Queue full?!"));
                        this.done.decrementAndGet();
                        drainLoop();
                        return;
                    }
                }
                if (decrementAndGet() == 0) {
                    return;
                }
            } else {
                SimplePlainQueue<T> q2 = inner.getQueue();
                if (!q2.offer(value)) {
                    inner.cancel();
                    this.errors.tryAddThrowableOrReport(new MissingBackpressureException("Queue full?!"));
                    this.done.decrementAndGet();
                }
                if (getAndIncrement() != 0) {
                    return;
                }
            }
            drainLoop();
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onError(Throwable e) {
            if (this.errors.tryAddThrowableOrReport(e)) {
                this.done.decrementAndGet();
                drain();
            }
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void onComplete() {
            this.done.decrementAndGet();
            drain();
        }

        @Override // io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionBase
        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            drainLoop();
        }

        /* JADX WARN: Code restructure failed: missing block: B:24:0x004d, code lost:
            if (r11 == false) goto L68;
         */
        /* JADX WARN: Code restructure failed: missing block: B:25:0x004f, code lost:
            if (r12 == false) goto L65;
         */
        /* JADX WARN: Code restructure failed: missing block: B:26:0x0051, code lost:
            r19.errors.tryTerminateConsumer(r4);
         */
        /* JADX WARN: Code restructure failed: missing block: B:27:0x0056, code lost:
            return;
         */
        /* JADX WARN: Code restructure failed: missing block: B:28:0x0057, code lost:
            if (r12 == false) goto L3;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drainLoop() {
            /*
                r19 = this;
                r0 = r19
                r1 = 1
                io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin$JoinInnerSubscriber<T>[] r2 = r0.subscribers
                int r3 = r2.length
                org.reactivestreams.Subscriber<? super T> r4 = r0.downstream
            L8:
                java.util.concurrent.atomic.AtomicLong r5 = r0.requested
                long r5 = r5.get()
                r7 = 0
            L10:
                r10 = 1
                int r11 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r11 == 0) goto L5b
                boolean r11 = r0.cancelled
                if (r11 == 0) goto L1d
                r19.cleanup()
                return
            L1d:
                java.util.concurrent.atomic.AtomicInteger r11 = r0.done
                int r11 = r11.get()
                if (r11 != 0) goto L27
                r11 = 1
                goto L28
            L27:
                r11 = 0
            L28:
                r12 = 1
                r13 = 0
            L2a:
                if (r13 >= r3) goto L4d
                r14 = r2[r13]
                io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue<T> r15 = r14.queue
                if (r15 == 0) goto L4a
                java.lang.Object r9 = r15.poll()
                if (r9 == 0) goto L4a
                r12 = 0
                r4.onNext(r9)
                r14.requestOne()
                r16 = 1
                long r16 = r7 + r16
                r7 = r16
                int r18 = (r16 > r5 ? 1 : (r16 == r5 ? 0 : -1))
                if (r18 != 0) goto L4a
                goto L5b
            L4a:
                int r13 = r13 + 1
                goto L2a
            L4d:
                if (r11 == 0) goto L57
                if (r12 == 0) goto L57
                io.reactivex.rxjava3.internal.util.AtomicThrowable r9 = r0.errors
                r9.tryTerminateConsumer(r4)
                return
            L57:
                if (r12 == 0) goto L5a
                goto L5b
            L5a:
                goto L10
            L5b:
                int r9 = (r7 > r5 ? 1 : (r7 == r5 ? 0 : -1))
                if (r9 != 0) goto L91
                boolean r9 = r0.cancelled
                if (r9 == 0) goto L67
                r19.cleanup()
                return
            L67:
                java.util.concurrent.atomic.AtomicInteger r9 = r0.done
                int r9 = r9.get()
                if (r9 != 0) goto L71
                r9 = 1
                goto L72
            L71:
                r9 = 0
            L72:
                r10 = 1
                r11 = 0
            L74:
                if (r11 >= r3) goto L87
                r12 = r2[r11]
                io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue<T> r13 = r12.queue
                if (r13 == 0) goto L84
                boolean r14 = r13.isEmpty()
                if (r14 != 0) goto L84
                r10 = 0
                goto L87
            L84:
                int r11 = r11 + 1
                goto L74
            L87:
                if (r9 == 0) goto L91
                if (r10 == 0) goto L91
                io.reactivex.rxjava3.internal.util.AtomicThrowable r11 = r0.errors
                r11.tryTerminateConsumer(r4)
                return
            L91:
                r9 = 0
                int r11 = (r7 > r9 ? 1 : (r7 == r9 ? 0 : -1))
                if (r11 == 0) goto L9c
                java.util.concurrent.atomic.AtomicLong r9 = r0.requested
                io.reactivex.rxjava3.internal.util.BackpressureHelper.produced(r9, r7)
            L9c:
                int r9 = -r1
                int r1 = r0.addAndGet(r9)
                if (r1 != 0) goto La5
            La4:
                return
            La5:
                goto L8
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.rxjava3.internal.operators.parallel.ParallelJoin.JoinSubscriptionDelayError.drainLoop():void");
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class JoinInnerSubscriber<T> extends AtomicReference<Subscription> implements FlowableSubscriber<T> {
        private static final long serialVersionUID = 8410034718427740355L;
        final int limit;
        final JoinSubscriptionBase<T> parent;
        final int prefetch;
        long produced;
        volatile SimplePlainQueue<T> queue;

        JoinInnerSubscriber(JoinSubscriptionBase<T> parent, int prefetch) {
            this.parent = parent;
            this.prefetch = prefetch;
            this.limit = prefetch - (prefetch >> 2);
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            SubscriptionHelper.setOnce(this, s, this.prefetch);
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            this.parent.onNext(this, t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            this.parent.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            this.parent.onComplete();
        }

        public void requestOne() {
            long p = this.produced + 1;
            if (p == this.limit) {
                this.produced = 0L;
                get().request(p);
                return;
            }
            this.produced = p;
        }

        public void request(long n) {
            long p = this.produced + n;
            if (p >= this.limit) {
                this.produced = 0L;
                get().request(p);
                return;
            }
            this.produced = p;
        }

        public boolean cancel() {
            return SubscriptionHelper.cancel(this);
        }

        SimplePlainQueue<T> getQueue() {
            SimplePlainQueue<T> q = this.queue;
            if (q == null) {
                SimplePlainQueue<T> q2 = new SpscArrayQueue<>(this.prefetch);
                this.queue = q2;
                return q2;
            }
            return q;
        }
    }
}
