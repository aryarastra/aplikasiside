package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.exceptions.MissingBackpressureException;
import io.reactivex.rxjava3.flowables.GroupedFlowable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.rxjava3.internal.subscriptions.BasicIntQueueSubscription;
import io.reactivex.rxjava3.internal.subscriptions.EmptySubscription;
import io.reactivex.rxjava3.internal.subscriptions.SubscriptionHelper;
import io.reactivex.rxjava3.internal.util.BackpressureHelper;
import io.reactivex.rxjava3.internal.util.EmptyComponent;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
public final class FlowableGroupBy<T, K, V> extends AbstractFlowableWithUpstream<T, GroupedFlowable<K, V>> {
    final int bufferSize;
    final boolean delayError;
    final Function<? super T, ? extends K> keySelector;
    final Function<? super Consumer<Object>, ? extends Map<K, Object>> mapFactory;
    final Function<? super T, ? extends V> valueSelector;

    public FlowableGroupBy(Flowable<T> source, Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector, int bufferSize, boolean delayError, Function<? super Consumer<Object>, ? extends Map<K, Object>> mapFactory) {
        super(source);
        this.keySelector = keySelector;
        this.valueSelector = valueSelector;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
        this.mapFactory = mapFactory;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super GroupedFlowable<K, V>> s) {
        ConcurrentLinkedQueue concurrentLinkedQueue;
        Map<Object, GroupedUnicast<K, V>> groups;
        try {
            if (this.mapFactory == null) {
                concurrentLinkedQueue = null;
                groups = new ConcurrentHashMap<>();
            } else {
                concurrentLinkedQueue = new ConcurrentLinkedQueue();
                Consumer<Object> evictionAction = new EvictionAction<>(concurrentLinkedQueue);
                groups = this.mapFactory.apply(evictionAction);
            }
            GroupBySubscriber<T, K, V> subscriber = new GroupBySubscriber<>(s, this.keySelector, this.valueSelector, this.bufferSize, this.delayError, groups, concurrentLinkedQueue);
            this.source.subscribe((FlowableSubscriber) subscriber);
        } catch (Throwable e) {
            Exceptions.throwIfFatal(e);
            s.onSubscribe(EmptyComponent.INSTANCE);
            s.onError(e);
        }
    }

    /* loaded from: classes.dex */
    public static final class GroupBySubscriber<T, K, V> extends AtomicLong implements FlowableSubscriber<T>, Subscription {
        static final Object NULL_KEY = new Object();
        private static final long serialVersionUID = -3688291656102519502L;
        final int bufferSize;
        final boolean delayError;
        boolean done;
        final Subscriber<? super GroupedFlowable<K, V>> downstream;
        long emittedGroups;
        final Queue<GroupedUnicast<K, V>> evictedGroups;
        final Map<Object, GroupedUnicast<K, V>> groups;
        final Function<? super T, ? extends K> keySelector;
        final int limit;
        Subscription upstream;
        final Function<? super T, ? extends V> valueSelector;
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicInteger groupCount = new AtomicInteger(1);
        final AtomicLong groupConsumed = new AtomicLong();

        public GroupBySubscriber(Subscriber<? super GroupedFlowable<K, V>> actual, Function<? super T, ? extends K> keySelector, Function<? super T, ? extends V> valueSelector, int bufferSize, boolean delayError, Map<Object, GroupedUnicast<K, V>> groups, Queue<GroupedUnicast<K, V>> evictedGroups) {
            this.downstream = actual;
            this.keySelector = keySelector;
            this.valueSelector = valueSelector;
            this.bufferSize = bufferSize;
            this.limit = bufferSize - (bufferSize >> 2);
            this.delayError = delayError;
            this.groups = groups;
            this.evictedGroups = evictedGroups;
        }

        @Override // io.reactivex.rxjava3.core.FlowableSubscriber, org.reactivestreams.Subscriber
        public void onSubscribe(Subscription s) {
            if (SubscriptionHelper.validate(this.upstream, s)) {
                this.upstream = s;
                this.downstream.onSubscribe(this);
                s.request(this.bufferSize);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            if (this.done) {
                return;
            }
            try {
                K key = this.keySelector.apply(t);
                boolean newGroup = false;
                Object mapKey = key != null ? key : NULL_KEY;
                GroupedUnicast<K, V> group = this.groups.get(mapKey);
                if (group == null) {
                    if (this.cancelled.get()) {
                        return;
                    }
                    group = GroupedUnicast.createWith(key, this.bufferSize, this, this.delayError);
                    this.groups.put(mapKey, group);
                    this.groupCount.getAndIncrement();
                    newGroup = true;
                }
                try {
                    V v = (V) ExceptionHelper.nullCheck(this.valueSelector.apply(t), "The valueSelector returned a null value.");
                    group.onNext(v);
                    completeEvictions();
                    if (newGroup) {
                        if (this.emittedGroups != get()) {
                            this.emittedGroups++;
                            this.downstream.onNext(group);
                            if (group.state.tryAbandon()) {
                                cancel(key);
                                group.onComplete();
                                requestGroup(1L);
                                return;
                            }
                            return;
                        }
                        this.upstream.cancel();
                        onError(new MissingBackpressureException(groupHangWarning(this.emittedGroups)));
                    }
                } catch (Throwable ex) {
                    Exceptions.throwIfFatal(ex);
                    this.upstream.cancel();
                    if (newGroup) {
                        if (this.emittedGroups != get()) {
                            this.downstream.onNext(group);
                        } else {
                            MissingBackpressureException mbe = new MissingBackpressureException(groupHangWarning(this.emittedGroups));
                            mbe.initCause(ex);
                            onError(mbe);
                            return;
                        }
                    }
                    onError(ex);
                }
            } catch (Throwable ex2) {
                Exceptions.throwIfFatal(ex2);
                this.upstream.cancel();
                onError(ex2);
            }
        }

        static String groupHangWarning(long n) {
            return "Unable to emit a new group (#" + n + ") due to lack of requests. Please make sure the downstream can always accept a new group as well as each group is consumed in order for the whole operator to be able to proceed.";
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            for (GroupedUnicast<K, V> g : this.groups.values()) {
                g.onError(t);
            }
            this.groups.clear();
            Queue<GroupedUnicast<K, V>> queue = this.evictedGroups;
            if (queue != null) {
                queue.clear();
            }
            this.downstream.onError(t);
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                for (GroupedUnicast<K, V> g : this.groups.values()) {
                    g.onComplete();
                }
                this.groups.clear();
                Queue<GroupedUnicast<K, V>> queue = this.evictedGroups;
                if (queue != null) {
                    queue.clear();
                }
                this.done = true;
                this.downstream.onComplete();
            }
        }

        @Override // org.reactivestreams.Subscription
        public void request(long n) {
            if (SubscriptionHelper.validate(n)) {
                BackpressureHelper.add(this, n);
            }
        }

        @Override // org.reactivestreams.Subscription
        public void cancel() {
            if (this.cancelled.compareAndSet(false, true)) {
                completeEvictions();
                if (this.groupCount.decrementAndGet() == 0) {
                    this.upstream.cancel();
                }
            }
        }

        private void completeEvictions() {
            if (this.evictedGroups != null) {
                int count = 0;
                while (true) {
                    GroupedUnicast<K, V> evictedGroup = this.evictedGroups.poll();
                    if (evictedGroup == null) {
                        break;
                    }
                    evictedGroup.onComplete();
                    count++;
                }
                if (count != 0) {
                    this.groupCount.addAndGet(-count);
                }
            }
        }

        public void cancel(K key) {
            Object mapKey = key != null ? key : NULL_KEY;
            this.groups.remove(mapKey);
            if (this.groupCount.decrementAndGet() == 0) {
                this.upstream.cancel();
            }
        }

        void requestGroup(long n) {
            long currentConsumed;
            long newConsumed;
            AtomicLong groupConsumed = this.groupConsumed;
            int limit = this.limit;
            do {
                currentConsumed = groupConsumed.get();
                newConsumed = BackpressureHelper.addCap(currentConsumed, n);
            } while (!groupConsumed.compareAndSet(currentConsumed, newConsumed));
            while (newConsumed >= limit) {
                long newConsumedAfterLimit = newConsumed - limit;
                if (groupConsumed.compareAndSet(newConsumed, newConsumedAfterLimit)) {
                    this.upstream.request(limit);
                }
                newConsumed = groupConsumed.get();
            }
        }
    }

    /* loaded from: classes.dex */
    static final class EvictionAction<K, V> implements Consumer<GroupedUnicast<K, V>> {
        final Queue<GroupedUnicast<K, V>> evictedGroups;

        @Override // io.reactivex.rxjava3.functions.Consumer
        public /* bridge */ /* synthetic */ void accept(Object value) throws Throwable {
            accept((GroupedUnicast) ((GroupedUnicast) value));
        }

        EvictionAction(Queue<GroupedUnicast<K, V>> evictedGroups) {
            this.evictedGroups = evictedGroups;
        }

        public void accept(GroupedUnicast<K, V> value) {
            this.evictedGroups.offer(value);
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class GroupedUnicast<K, T> extends GroupedFlowable<K, T> {
        final State<T, K> state;

        public static <T, K> GroupedUnicast<K, T> createWith(K key, int bufferSize, GroupBySubscriber<?, K, T> parent, boolean delayError) {
            State<T, K> state = new State<>(bufferSize, parent, key, delayError);
            return new GroupedUnicast<>(key, state);
        }

        protected GroupedUnicast(K key, State<T, K> state) {
            super(key);
            this.state = state;
        }

        @Override // io.reactivex.rxjava3.core.Flowable
        protected void subscribeActual(Subscriber<? super T> s) {
            this.state.subscribe(s);
        }

        public void onNext(T t) {
            this.state.onNext(t);
        }

        public void onError(Throwable e) {
            this.state.onError(e);
        }

        public void onComplete() {
            this.state.onComplete();
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class State<T, K> extends BasicIntQueueSubscription<T> implements Publisher<T> {
        static final int ABANDONED = 2;
        static final int ABANDONED_HAS_SUBSCRIBER = 3;
        static final int FRESH = 0;
        static final int HAS_SUBSCRIBER = 1;
        private static final long serialVersionUID = -3852313036005250360L;
        final boolean delayError;
        volatile boolean done;
        Throwable error;
        final K key;
        boolean outputFused;
        final GroupBySubscriber<?, K, T> parent;
        int produced;
        final SpscLinkedArrayQueue<T> queue;
        final AtomicLong requested = new AtomicLong();
        final AtomicBoolean cancelled = new AtomicBoolean();
        final AtomicReference<Subscriber<? super T>> actual = new AtomicReference<>();
        final AtomicInteger once = new AtomicInteger();

        State(int bufferSize, GroupBySubscriber<?, K, T> parent, K key, boolean delayError) {
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
            this.parent = parent;
            this.key = key;
            this.delayError = delayError;
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
            if (this.cancelled.compareAndSet(false, true)) {
                cancelParent();
                drain();
            }
        }

        @Override // org.reactivestreams.Publisher
        public void subscribe(Subscriber<? super T> subscriber) {
            int s;
            int u;
            do {
                s = this.once.get();
                if ((s & 1) == 0) {
                    u = s | 1;
                } else {
                    EmptySubscription.error(new IllegalStateException("Only one Subscriber allowed!"), subscriber);
                    return;
                }
            } while (!this.once.compareAndSet(s, u));
            subscriber.onSubscribe(this);
            this.actual.lazySet(subscriber);
            if (this.cancelled.get()) {
                this.actual.lazySet(null);
            } else {
                drain();
            }
        }

        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        public void onError(Throwable e) {
            this.error = e;
            this.done = true;
            drain();
        }

        public void onComplete() {
            this.done = true;
            drain();
        }

        void cancelParent() {
            if ((this.once.get() & 2) == 0) {
                this.parent.cancel(this.key);
            }
        }

        boolean tryAbandon() {
            return this.once.get() == 0 && this.once.compareAndSet(0, 2);
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            if (this.outputFused) {
                drainFused();
            } else {
                drainNormal();
            }
        }

        void drainFused() {
            Throwable ex;
            int missed = 1;
            SpscLinkedArrayQueue<T> q = this.queue;
            Subscriber<? super T> a = this.actual.get();
            while (true) {
                if (a != null) {
                    if (this.cancelled.get()) {
                        return;
                    }
                    boolean d = this.done;
                    if (d && !this.delayError && (ex = this.error) != null) {
                        q.clear();
                        a.onError(ex);
                        return;
                    }
                    a.onNext(null);
                    if (d) {
                        Throwable ex2 = this.error;
                        if (ex2 != null) {
                            a.onError(ex2);
                            return;
                        } else {
                            a.onComplete();
                            return;
                        }
                    }
                }
                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
                if (a == null) {
                    Subscriber<? super T> a2 = this.actual.get();
                    a = a2;
                }
            }
        }

        void drainNormal() {
            int missed;
            SpscLinkedArrayQueue<T> q = this.queue;
            boolean delayError = this.delayError;
            int missed2 = 1;
            Subscriber<? super T> a = this.actual.get();
            while (true) {
                if (a == null) {
                    missed = missed2;
                } else {
                    long r = this.requested.get();
                    long e = 0;
                    while (true) {
                        if (e == r) {
                            missed = missed2;
                            break;
                        }
                        boolean d = this.done;
                        Object obj = (T) q.poll();
                        boolean empty = obj == null;
                        missed = missed2;
                        if (checkTerminated(d, empty, a, delayError, e)) {
                            return;
                        }
                        if (empty) {
                            break;
                        }
                        a.onNext(obj);
                        e++;
                        missed2 = missed;
                    }
                    if (e == r && checkTerminated(this.done, q.isEmpty(), a, delayError, e)) {
                        return;
                    }
                    if (e != 0) {
                        BackpressureHelper.produced(this.requested, e);
                        requestParent(e);
                    }
                }
                missed2 = addAndGet(-missed);
                if (missed2 != 0) {
                    if (a == null) {
                        Subscriber<? super T> a2 = this.actual.get();
                        a = a2;
                    }
                } else {
                    return;
                }
            }
        }

        void requestParent(long e) {
            if ((this.once.get() & 2) == 0) {
                this.parent.requestGroup(e);
            }
        }

        boolean checkTerminated(boolean d, boolean empty, Subscriber<? super T> a, boolean delayError, long emitted) {
            if (this.cancelled.get()) {
                while (this.queue.poll() != null) {
                    emitted++;
                }
                if (emitted != 0) {
                    requestParent(emitted);
                }
                return true;
            } else if (d) {
                if (delayError) {
                    if (empty) {
                        Throwable e = this.error;
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        return true;
                    }
                    return false;
                }
                Throwable e2 = this.error;
                if (e2 != null) {
                    this.queue.clear();
                    a.onError(e2);
                    return true;
                } else if (empty) {
                    a.onComplete();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 2) != 0) {
                this.outputFused = true;
                return 2;
            }
            return 0;
        }

        void tryReplenish() {
            int p = this.produced;
            if (p != 0) {
                this.produced = 0;
                requestParent(p);
            }
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public T poll() {
            T v = this.queue.poll();
            if (v != null) {
                this.produced++;
                return v;
            }
            tryReplenish();
            return null;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            if (this.queue.isEmpty()) {
                tryReplenish();
                return true;
            }
            tryReplenish();
            return false;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public void clear() {
            SpscLinkedArrayQueue<T> q = this.queue;
            while (q.poll() != null) {
                this.produced++;
            }
            tryReplenish();
        }
    }
}
