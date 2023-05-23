package io.reactivex.rxjava3.subjects;

import io.reactivex.rxjava3.annotations.CheckReturnValue;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class PublishSubject<T> extends Subject<T> {
    Throwable error;
    final AtomicReference<PublishDisposable<T>[]> subscribers = new AtomicReference<>(EMPTY);
    static final PublishDisposable[] TERMINATED = new PublishDisposable[0];
    static final PublishDisposable[] EMPTY = new PublishDisposable[0];

    @CheckReturnValue
    public static <T> PublishSubject<T> create() {
        return new PublishSubject<>();
    }

    PublishSubject() {
    }

    @Override // io.reactivex.rxjava3.core.Observable
    protected void subscribeActual(Observer<? super T> t) {
        PublishDisposable<T> ps = new PublishDisposable<>(t, this);
        t.onSubscribe(ps);
        if (add(ps)) {
            if (ps.isDisposed()) {
                remove(ps);
                return;
            }
            return;
        }
        Throwable ex = this.error;
        if (ex != null) {
            t.onError(ex);
        } else {
            t.onComplete();
        }
    }

    boolean add(PublishDisposable<T> ps) {
        PublishDisposable<T>[] a;
        PublishDisposable<T>[] b;
        do {
            a = this.subscribers.get();
            if (a == TERMINATED) {
                return false;
            }
            int n = a.length;
            b = new PublishDisposable[n + 1];
            System.arraycopy(a, 0, b, 0, n);
            b[n] = ps;
        } while (!this.subscribers.compareAndSet(a, b));
        return true;
    }

    /* JADX WARN: Multi-variable type inference failed */
    void remove(PublishDisposable<T> ps) {
        PublishDisposable<T>[] a;
        PublishDisposable<T>[] b;
        do {
            a = this.subscribers.get();
            if (a == TERMINATED || a == EMPTY) {
                return;
            }
            int n = a.length;
            int j = -1;
            int i = 0;
            while (true) {
                if (i >= n) {
                    break;
                } else if (a[i] != ps) {
                    i++;
                } else {
                    j = i;
                    break;
                }
            }
            if (j < 0) {
                return;
            }
            if (n == 1) {
                b = EMPTY;
            } else {
                PublishDisposable<T>[] b2 = new PublishDisposable[n - 1];
                System.arraycopy(a, 0, b2, 0, j);
                System.arraycopy(a, j + 1, b2, j, (n - j) - 1);
                b = b2;
            }
        } while (!this.subscribers.compareAndSet(a, b));
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onSubscribe(Disposable d) {
        if (this.subscribers.get() == TERMINATED) {
            d.dispose();
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onNext(T t) {
        PublishDisposable<T>[] publishDisposableArr;
        ExceptionHelper.nullCheck(t, "onNext called with a null value.");
        for (PublishDisposable<T> pd : this.subscribers.get()) {
            pd.onNext(t);
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onError(Throwable t) {
        PublishDisposable<T>[] andSet;
        ExceptionHelper.nullCheck(t, "onError called with a null Throwable.");
        PublishDisposable<T>[] publishDisposableArr = this.subscribers.get();
        PublishDisposable<T>[] publishDisposableArr2 = TERMINATED;
        if (publishDisposableArr == publishDisposableArr2) {
            RxJavaPlugins.onError(t);
            return;
        }
        this.error = t;
        for (PublishDisposable<T> pd : this.subscribers.getAndSet(publishDisposableArr2)) {
            pd.onError(t);
        }
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public void onComplete() {
        PublishDisposable<T>[] andSet;
        PublishDisposable<T>[] publishDisposableArr = this.subscribers.get();
        PublishDisposable<T>[] publishDisposableArr2 = TERMINATED;
        if (publishDisposableArr == publishDisposableArr2) {
            return;
        }
        for (PublishDisposable<T> pd : this.subscribers.getAndSet(publishDisposableArr2)) {
            pd.onComplete();
        }
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasObservers() {
        return this.subscribers.get().length != 0;
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public Throwable getThrowable() {
        if (this.subscribers.get() == TERMINATED) {
            return this.error;
        }
        return null;
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasThrowable() {
        return this.subscribers.get() == TERMINATED && this.error != null;
    }

    @Override // io.reactivex.rxjava3.subjects.Subject
    @CheckReturnValue
    public boolean hasComplete() {
        return this.subscribers.get() == TERMINATED && this.error == null;
    }

    /* loaded from: classes.dex */
    public static final class PublishDisposable<T> extends AtomicBoolean implements Disposable {
        private static final long serialVersionUID = 3562861878281475070L;
        final Observer<? super T> downstream;
        final PublishSubject<T> parent;

        PublishDisposable(Observer<? super T> actual, PublishSubject<T> parent) {
            this.downstream = actual;
            this.parent = parent;
        }

        public void onNext(T t) {
            if (!get()) {
                this.downstream.onNext(t);
            }
        }

        public void onError(Throwable t) {
            if (get()) {
                RxJavaPlugins.onError(t);
            } else {
                this.downstream.onError(t);
            }
        }

        public void onComplete() {
            if (!get()) {
                this.downstream.onComplete();
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            if (compareAndSet(false, true)) {
                this.parent.remove(this);
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return get();
        }
    }
}
