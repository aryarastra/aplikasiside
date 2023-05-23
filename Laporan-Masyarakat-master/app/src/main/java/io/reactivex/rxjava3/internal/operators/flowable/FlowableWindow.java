package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.processors.UnicastProcessor;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.reactivestreams.Processor;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableWindow<T> extends AbstractFlowableWithUpstream<T, Flowable<T>> {
    final int bufferSize;
    final long size;
    final long skip;

    public FlowableWindow(Flowable<T> source, long size, long skip, int bufferSize) {
        super(source);
        this.size = size;
        this.skip = skip;
        this.bufferSize = bufferSize;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    public void subscribeActual(Subscriber<? super Flowable<T>> s) {
        long j = this.skip;
        long j2 = this.size;
        if (j == j2) {
            this.source.subscribe((FlowableSubscriber) new WindowExactSubscriber(s, this.size, this.bufferSize));
        } else if (j > j2) {
            this.source.subscribe((FlowableSubscriber) new WindowSkipSubscriber(s, this.size, this.skip, this.bufferSize));
        } else {
            this.source.subscribe((FlowableSubscriber) new WindowOverlapSubscriber(s, this.size, this.skip, this.bufferSize));
        }
    }

    /* loaded from: classes.dex */
    static final class WindowExactSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = -2365647875069161133L;
        final int bufferSize;
        final Subscriber<? super Flowable<T>> downstream;
        long index;
        final AtomicBoolean once;
        final long size;
        Subscription upstream;
        UnicastProcessor<T> window;

        WindowExactSubscriber(Subscriber<? super Flowable<T>> actual, long size, int bufferSize) {
            super(1);
            this.downstream = actual;
            this.size = size;
            this.once = new AtomicBoolean();
            this.bufferSize = bufferSize;
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
            long i = this.index;
            UnicastProcessor<T> w = this.window;
            FlowableWindowSubscribeIntercept<T> intercept = null;
            if (i == 0) {
                getAndIncrement();
                w = UnicastProcessor.create(this.bufferSize, this);
                this.window = w;
                intercept = new FlowableWindowSubscribeIntercept<>(w);
                this.downstream.onNext(intercept);
            }
            long i2 = i + 1;
            w.onNext(t);
            if (i2 == this.size) {
                this.index = 0L;
                this.window = null;
                w.onComplete();
            } else {
                this.index = i2;
            }
            if (intercept != null && intercept.tryAbandon()) {
                intercept.window.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            Processor<T, T> w = this.window;
            if (w != null) {
                this.window = null;
                w.onError(t);
            }
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            Processor<T, T> w = this.window;
            if (w != null) {
                this.window = null;
                w.onComplete();
            }
            this.downstream.onComplete();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                long u = BackpressureHelper.multiplyCap(this.size, n);
                this.upstream.request(u);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.once.compareAndSet(false, true)) {
                run();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (decrementAndGet() == 0) {
                this.upstream.cancel();
            }
        }
    }

    /* loaded from: classes.dex */
    static final class WindowSkipSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = -8792836352386833856L;
        final int bufferSize;
        final Subscriber<? super Flowable<T>> downstream;
        final AtomicBoolean firstRequest;
        long index;
        final AtomicBoolean once;
        final long size;
        final long skip;
        Subscription upstream;
        UnicastProcessor<T> window;

        WindowSkipSubscriber(Subscriber<? super Flowable<T>> actual, long size, long skip, int bufferSize) {
            super(1);
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.once = new AtomicBoolean();
            this.firstRequest = new AtomicBoolean();
            this.bufferSize = bufferSize;
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
            long i = this.index;
            FlowableWindowSubscribeIntercept<T> intercept = null;
            UnicastProcessor<T> w = this.window;
            if (i == 0) {
                getAndIncrement();
                w = UnicastProcessor.create(this.bufferSize, this);
                this.window = w;
                intercept = new FlowableWindowSubscribeIntercept<>(w);
                this.downstream.onNext(intercept);
            }
            long i2 = i + 1;
            if (w != null) {
                w.onNext(t);
            }
            if (i2 == this.size) {
                this.window = null;
                w.onComplete();
            }
            if (i2 == this.skip) {
                this.index = 0L;
            } else {
                this.index = i2;
            }
            if (intercept != null && intercept.tryAbandon()) {
                intercept.window.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            Processor<T, T> w = this.window;
            if (w != null) {
                this.window = null;
                w.onError(t);
            }
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            Processor<T, T> w = this.window;
            if (w != null) {
                this.window = null;
                w.onComplete();
            }
            this.downstream.onComplete();
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                if (!this.firstRequest.get() && this.firstRequest.compareAndSet(false, true)) {
                    long u = BackpressureHelper.multiplyCap(this.size, n);
                    long v = BackpressureHelper.multiplyCap(this.skip - this.size, n - 1);
                    long w = BackpressureHelper.addCap(u, v);
                    this.upstream.request(w);
                    return;
                }
                long u2 = BackpressureHelper.multiplyCap(this.skip, n);
                this.upstream.request(u2);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.once.compareAndSet(false, true)) {
                run();
            }
        }

        @Override // java.lang.Runnable
        public void run() {
            if (decrementAndGet() == 0) {
                this.upstream.cancel();
            }
        }
    }

    /* loaded from: classes.dex */
    static final class WindowOverlapSubscriber<T> extends AtomicInteger implements FlowableSubscriber<T>, Subscription, Runnable {
        private static final long serialVersionUID = 2428527070996323976L;
        final int bufferSize;
        volatile boolean cancelled;
        volatile boolean done;
        final Subscriber<? super Flowable<T>> downstream;
        Throwable error;
        final AtomicBoolean firstRequest;
        long index;
        final AtomicBoolean once;
        long produced;
        final SpscLinkedArrayQueue<UnicastProcessor<T>> queue;
        final AtomicLong requested;
        final long size;
        final long skip;
        Subscription upstream;
        final ArrayDeque<UnicastProcessor<T>> windows;
        final AtomicInteger wip;

        WindowOverlapSubscriber(Subscriber<? super Flowable<T>> actual, long size, long skip, int bufferSize) {
            super(1);
            this.downstream = actual;
            this.size = size;
            this.skip = skip;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
            this.windows = new ArrayDeque<>();
            this.once = new AtomicBoolean();
            this.firstRequest = new AtomicBoolean();
            this.requested = new AtomicLong();
            this.wip = new AtomicInteger();
            this.bufferSize = bufferSize;
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
            long i = this.index;
            UnicastProcessor<T> newWindow = null;
            if (i == 0 && !this.cancelled) {
                getAndIncrement();
                newWindow = UnicastProcessor.create(this.bufferSize, this);
                this.windows.offer(newWindow);
            }
            long i2 = i + 1;
            Iterator<UnicastProcessor<T>> it = this.windows.iterator();
            while (it.hasNext()) {
                it.next().onNext(t);
            }
            if (newWindow != null) {
                this.queue.offer(newWindow);
                drain();
            }
            long p = this.produced + 1;
            if (p == this.size) {
                this.produced = p - this.skip;
                Processor<T, T> w = this.windows.poll();
                if (w != null) {
                    w.onComplete();
                }
            } else {
                this.produced = p;
            }
            if (i2 == this.skip) {
                this.index = 0L;
            } else {
                this.index = i2;
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            Iterator<UnicastProcessor<T>> it = this.windows.iterator();
            while (it.hasNext()) {
                Processor<T, T> w = it.next();
                w.onError(t);
            }
            this.windows.clear();
            this.error = t;
            this.done = true;
            drain();
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            Iterator<UnicastProcessor<T>> it = this.windows.iterator();
            while (it.hasNext()) {
                Processor<T, T> w = it.next();
                w.onComplete();
            }
            this.windows.clear();
            this.done = true;
            drain();
        }

        /* JADX WARN: Code restructure failed: missing block: B:122:0x000e, code lost:
            continue;
         */
        /*
            Code decompiled incorrectly, please refer to instructions dump.
            To view partially-correct code enable 'Show inconsistent code' option in preferences
        */
        void drain() {
            /*
                r13 = this;
                java.util.concurrent.atomic.AtomicInteger r0 = r13.wip
                int r0 = r0.getAndIncrement()
                if (r0 == 0) goto L9
                return
            L9:
                org.reactivestreams.Subscriber<? super io.reactivex.rxjava3.core.Flowable<T>> r0 = r13.downstream
                io.reactivex.rxjava3.internal.queue.SpscLinkedArrayQueue<io.reactivex.rxjava3.processors.UnicastProcessor<T>> r1 = r13.queue
                r2 = 1
            Le:
                boolean r3 = r13.cancelled
                if (r3 == 0) goto L22
                r3 = 0
            L13:
                java.lang.Object r4 = r1.poll()
                io.reactivex.rxjava3.processors.UnicastProcessor r4 = (io.reactivex.rxjava3.processors.UnicastProcessor) r4
                r3 = r4
                if (r4 == 0) goto L20
                r3.onComplete()
                goto L13
            L20:
                goto L8a
            L22:
                java.util.concurrent.atomic.AtomicLong r3 = r13.requested
                long r3 = r3.get()
                r5 = 0
            L2a:
                int r7 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1))
                if (r7 == 0) goto L5f
                boolean r7 = r13.done
                java.lang.Object r8 = r1.poll()
                io.reactivex.rxjava3.processors.UnicastProcessor r8 = (io.reactivex.rxjava3.processors.UnicastProcessor) r8
                if (r8 != 0) goto L3a
                r9 = 1
                goto L3b
            L3a:
                r9 = 0
            L3b:
                boolean r10 = r13.cancelled
                if (r10 == 0) goto L40
                goto Le
            L40:
                boolean r10 = r13.checkTerminated(r7, r9, r0, r1)
                if (r10 == 0) goto L47
                return
            L47:
                if (r9 == 0) goto L4a
                goto L5f
            L4a:
                io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowSubscribeIntercept r10 = new io.reactivex.rxjava3.internal.operators.flowable.FlowableWindowSubscribeIntercept
                r10.<init>(r8)
                r0.onNext(r10)
                boolean r11 = r10.tryAbandon()
                if (r11 == 0) goto L5b
                r8.onComplete()
            L5b:
                r11 = 1
                long r5 = r5 + r11
                goto L2a
            L5f:
                int r7 = (r5 > r3 ? 1 : (r5 == r3 ? 0 : -1))
                if (r7 != 0) goto L75
                boolean r7 = r13.cancelled
                if (r7 == 0) goto L68
                goto Le
            L68:
                boolean r7 = r13.done
                boolean r8 = r1.isEmpty()
                boolean r7 = r13.checkTerminated(r7, r8, r0, r1)
                if (r7 == 0) goto L75
                return
            L75:
                r7 = 0
                int r9 = (r5 > r7 ? 1 : (r5 == r7 ? 0 : -1))
                if (r9 == 0) goto L8a
                r7 = 9223372036854775807(0x7fffffffffffffff, double:NaN)
                int r9 = (r3 > r7 ? 1 : (r3 == r7 ? 0 : -1))
                if (r9 == 0) goto L8a
                java.util.concurrent.atomic.AtomicLong r7 = r13.requested
                long r8 = -r5
                r7.addAndGet(r8)
            L8a:
                java.util.concurrent.atomic.AtomicInteger r3 = r13.wip
                int r4 = -r2
                int r2 = r3.addAndGet(r4)
                if (r2 != 0) goto L95
            L94:
                return
            L95:
                goto Le
            */
            throw new UnsupportedOperationException("Method not decompiled: io.reactivex.rxjava3.internal.operators.flowable.FlowableWindow.WindowOverlapSubscriber.drain():void");
        }

        boolean checkTerminated(boolean d, boolean empty, Subscriber<?> a, SpscLinkedArrayQueue<?> q) {
            if (d) {
                Throwable e = this.error;
                if (e != null) {
                    q.clear();
                    a.onError(e);
                    return true;
                } else if (empty) {
                    a.onComplete();
                    return true;
                } else {
                    return false;
                }
            }
            return false;
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this.requested, n);
                if (!this.firstRequest.get() && this.firstRequest.compareAndSet(false, true)) {
                    long u = BackpressureHelper.multiplyCap(this.skip, n - 1);
                    long v = BackpressureHelper.addCap(this.size, u);
                    this.upstream.request(v);
                } else {
                    long u2 = BackpressureHelper.multiplyCap(this.skip, n);
                    this.upstream.request(u2);
                }
                drain();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            this.cancelled = true;
            if (this.once.compareAndSet(false, true)) {
                run();
            }
            drain();
        }

        @Override // java.lang.Runnable
        public void run() {
            if (decrementAndGet() == 0) {
                this.upstream.cancel();
            }
        }
    }
}
