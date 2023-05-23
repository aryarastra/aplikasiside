package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.internal.observers.BasicQueueDisposable;
import java.util.Objects;

/* loaded from: classes.dex */
public final class ObservableFromArray<T> extends Observable<T> {
    final T[] array;

    public ObservableFromArray(T[] array) {
        this.array = array;
    }

    @Override // io.reactivex.rxjava3.core.Observable
    public void subscribeActual(Observer<? super T> observer) {
        FromArrayDisposable<T> d = new FromArrayDisposable<>(observer, this.array);
        observer.onSubscribe(d);
        if (d.fusionMode) {
            return;
        }
        d.run();
    }

    /* loaded from: classes.dex */
    static final class FromArrayDisposable<T> extends BasicQueueDisposable<T> {
        final T[] array;
        volatile boolean disposed;
        final Observer<? super T> downstream;
        boolean fusionMode;
        int index;

        FromArrayDisposable(Observer<? super T> actual, T[] array) {
            this.downstream = actual;
            this.array = array;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            if ((mode & 1) != 0) {
                this.fusionMode = true;
                return 1;
            }
            return 0;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public T poll() {
            int i = this.index;
            T[] a = this.array;
            if (i != a.length) {
                this.index = i + 1;
                T t = a[i];
                Objects.requireNonNull(t, "The array element is null");
                return t;
            }
            return null;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public boolean isEmpty() {
            return this.index == this.array.length;
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public void clear() {
            this.index = this.array.length;
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            this.disposed = true;
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.disposed;
        }

        void run() {
            T[] a = this.array;
            int n = a.length;
            for (int i = 0; i < n && !isDisposed(); i++) {
                T value = a[i];
                if (value == null) {
                    Observer<? super T> observer = this.downstream;
                    observer.onError(new NullPointerException("The element at index " + i + " is null"));
                    return;
                }
                this.downstream.onNext(value);
            }
            if (!isDisposed()) {
                this.downstream.onComplete();
            }
        }
    }
}
