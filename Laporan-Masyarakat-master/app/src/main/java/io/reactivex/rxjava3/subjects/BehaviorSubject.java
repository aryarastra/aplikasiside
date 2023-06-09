package io.reactivex.rxjava3.subjects;

import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.util.AppendOnlyLinkedArrayList;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.internal.util.NotificationLite;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/* loaded from: classes.dex */
public final class BehaviorSubject<T> extends Subject<T> {
    static final BehaviorDisposable[] EMPTY = new BehaviorDisposable[0];
    static final BehaviorDisposable[] TERMINATED = new BehaviorDisposable[0];
    long index;
    final ReadWriteLock lock;
    final AtomicReference<BehaviorDisposable<T>[]> observers;
    final Lock readLock;
    final AtomicReference<Throwable> terminalEvent;
    final AtomicReference<Object> value;
    final Lock writeLock;

    @CheckReturnValue
    public static <T> BehaviorSubject<T> create() {
        return new BehaviorSubject<>(null);
    }

    @CheckReturnValue
    public static <T> BehaviorSubject<T> createDefault(T defaultValue) {
        Objects.requireNonNull(defaultValue, "defaultValue is null");
        return new BehaviorSubject<>(defaultValue);
    }

    BehaviorSubject(T defaultValue) {
        ReentrantReadWriteLock reentrantReadWriteLock = new ReentrantReadWriteLock();
        this.lock = reentrantReadWriteLock;
        this.readLock = reentrantReadWriteLock.readLock();
        this.writeLock = reentrantReadWriteLock.writeLock();
        this.observers = new AtomicReference<>(EMPTY);
        this.value = new AtomicReference<>(defaultValue);
        this.terminalEvent = new AtomicReference<>();
    }

    @Override // io.reactivex.rxjava3.core.Observable
    protected void subscribeActual(Observer<? super T> observer) {
        BehaviorDisposable<T> bs = new BehaviorDisposable<>(observer, this);
        observer.onSubscribe(bs);
        if (add(bs)) {
            if (bs.cancelled) {
                remove(bs);
                return;
            } else {
                bs.emitFirst();
                return;
            }
        }
        Throwable ex = this.terminalEvent.get();
        if (ex == ExceptionHelper.TERMINATED) {
            observer.onComplete();
        } else {
            observer.onError(ex);
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onSubscribe(Disposable d) {
        if (this.terminalEvent.get() != null) {
            d.dispose();
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onNext(T t) {
        BehaviorDisposable<T>[] behaviorDisposableArr;
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        if (this.terminalEvent.get() != null) {
            return;
        }
        Object o = NotificationLite.next(t);
        setCurrent(o);
        for (BehaviorDisposable<T> bs : this.observers.get()) {
            bs.emitNext(o, this.index);
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onError(Throwable t) {
        BehaviorDisposable<T>[] terminate;
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        if (!this.terminalEvent.compareAndSet(null, t)) {
            RxJavaPlugins.onError(t);
            return;
        }
        Object o = NotificationLite.error(t);
        for (BehaviorDisposable<T> bs : terminate(o)) {
            bs.emitNext(o, this.index);
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onComplete() {
        BehaviorDisposable<T>[] terminate;
        if (!this.terminalEvent.compareAndSet(null, ExceptionHelper.TERMINATED)) {
            return;
        }
        Object o = NotificationLite.complete();
        for (BehaviorDisposable<T> bs : terminate(o)) {
            bs.emitNext(o, this.index);
        }
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasObservers() {
        return this.observers.get().length != 0;
    }

    @CheckReturnValue
    int subscriberCount() {
        return this.observers.get().length;
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public Throwable getThrowable() {
        Object o = this.value.get();
        if (NotificationLite.isError(o)) {
            return NotificationLite.getError(o);
        }
        return null;
    }

    @CheckReturnValue
    public T getValue() {
        Object o = this.value.get();
        if (NotificationLite.isComplete(o) || NotificationLite.isError(o)) {
            return null;
        }
        return (T) NotificationLite.getValue(o);
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasComplete() {
        Object o = this.value.get();
        return NotificationLite.isComplete(o);
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasThrowable() {
        Object o = this.value.get();
        return NotificationLite.isError(o);
    }

    @CheckReturnValue
    public boolean hasValue() {
        Object o = this.value.get();
        return (o == null || NotificationLite.isComplete(o) || NotificationLite.isError(o)) ? false : true;
    }

    boolean add(BehaviorDisposable<T> rs) {
        BehaviorDisposable<T>[] a;
        BehaviorDisposable<T>[] b;
        do {
            a = this.observers.get();
            if (a == TERMINATED) {
                return false;
            }
            int len = a.length;
            b = new BehaviorDisposable[len + 1];
            System.arraycopy(a, 0, b, 0, len);
            b[len] = rs;
        } while (!this.observers.compareAndSet(a, b));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    void remove(BehaviorDisposable<T> rs) {
        BehaviorDisposable<T>[] a;
        BehaviorDisposable<T>[] b;
        do {
            a = this.observers.get();
            int len = a.length;
            if (len == 0) {
                return;
            }
            int j = -1;
            int i = 0;
            while (true) {
                if (i >= len) {
                    break;
                } else if (a[i] != rs) {
                    i++;
                } else {
                    j = i;
                    break;
                }
            }
            if (j < 0) {
                return;
            }
            if (len == 1) {
                b = EMPTY;
            } else {
                BehaviorDisposable<T>[] b2 = new BehaviorDisposable[len - 1];
                System.arraycopy(a, 0, b2, 0, j);
                System.arraycopy(a, j + 1, b2, j, (len - j) - 1);
                b = b2;
            }
        } while (!this.observers.compareAndSet(a, b));
    }

    BehaviorDisposable<T>[] terminate(Object terminalValue) {
        setCurrent(terminalValue);
        return this.observers.getAndSet(TERMINATED);
    }

    void setCurrent(Object o) {
        this.writeLock.lock();
        this.index++;
        this.value.lazySet(o);
        this.writeLock.unlock();
    }

    /* loaded from: classes.dex */
    public static final class BehaviorDisposable<T> implements Disposable, AppendOnlyLinkedArrayList.NonThrowingPredicate<Object> {
        volatile boolean cancelled;
        final Observer<? super T> downstream;
        boolean emitting;
        boolean fastPath;
        long index;
        boolean next;
        AppendOnlyLinkedArrayList<Object> queue;
        final BehaviorSubject<T> state;

        BehaviorDisposable(Observer<? super T> actual, BehaviorSubject<T> state) {
            this.downstream = actual;
            this.state = state;
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                this.state.remove(this);
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void emitFirst() {
            if (this.cancelled) {
                return;
            }
            synchronized (this) {
                if (this.cancelled) {
                    return;
                }
                if (this.next) {
                    return;
                }
                BehaviorSubject<T> s = this.state;
                Lock lock = s.readLock;
                lock.lock();
                this.index = s.index;
                Object o = s.value.get();
                lock.unlock();
                this.emitting = o != null;
                this.next = true;
                if (o == null || test(o)) {
                    return;
                }
                emitLoop();
            }
        }

        void emitNext(Object value, long stateIndex) {
            if (this.cancelled) {
                return;
            }
            if (!this.fastPath) {
                synchronized (this) {
                    if (this.cancelled) {
                        return;
                    }
                    if (this.index == stateIndex) {
                        return;
                    }
                    if (this.emitting) {
                        AppendOnlyLinkedArrayList<Object> q = this.queue;
                        if (q == null) {
                            q = new AppendOnlyLinkedArrayList<>(4);
                            this.queue = q;
                        }
                        q.add(value);
                        return;
                    }
                    this.next = true;
                    this.fastPath = true;
                }
            }
            test(value);
        }

        @Override // io.reactivex.rxjava3.internal.util.AppendOnlyLinkedArrayList.NonThrowingPredicate, io.reactivex.rxjava3.functions.Predicate
        public boolean test(Object o) {
            return this.cancelled || NotificationLite.accept(o, this.downstream);
        }

        void emitLoop() {
            AppendOnlyLinkedArrayList<Object> q;
            while (!this.cancelled) {
                synchronized (this) {
                    q = this.queue;
                    if (q == null) {
                        this.emitting = false;
                        return;
                    }
                    this.queue = null;
                }
                q.forEachWhile(this);
            }
        }
    }
}
