package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.queue.SpscLinkedArrayQueue;
import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class ObservableZip<T, R> extends Observable<R> {
    final int bufferSize;
    final boolean delayError;
    final ObservableSource<? extends T>[] sources;
    final Iterable<? extends ObservableSource<? extends T>> sourcesIterable;
    final Function<? super Object[], ? extends R> zipper;

    public ObservableZip(ObservableSource<? extends T>[] sources, Iterable<? extends ObservableSource<? extends T>> sourcesIterable, Function<? super Object[], ? extends R> zipper, int bufferSize, boolean delayError) {
        this.sources = sources;
        this.sourcesIterable = sourcesIterable;
        this.zipper = zipper;
        this.bufferSize = bufferSize;
        this.delayError = delayError;
    }

    @Override // io.reactivex.rxjava3.core.Observable
    public void subscribeActual(Observer<? super R> observer) {
        ObservableSource<? extends T>[] sources = this.sources;
        int count = 0;
        if (sources == null) {
            sources = new ObservableSource[8];
            for (ObservableSource<? extends T> p : this.sourcesIterable) {
                if (count == sources.length) {
                    ObservableSource<? extends T>[] b = new ObservableSource[(count >> 2) + count];
                    System.arraycopy(sources, 0, b, 0, count);
                    sources = b;
                }
                sources[count] = p;
                count++;
            }
        } else {
            count = sources.length;
        }
        if (count == 0) {
            EmptyDisposable.complete(observer);
            return;
        }
        ZipCoordinator<T, R> zc = new ZipCoordinator<>(observer, this.zipper, count, this.delayError);
        zc.subscribe(sources, this.bufferSize);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ZipCoordinator<T, R> extends AtomicInteger implements Disposable {
        private static final long serialVersionUID = 2983708048395377667L;
        volatile boolean cancelled;
        final boolean delayError;
        final Observer<? super R> downstream;
        final ZipObserver<T, R>[] observers;
        final T[] row;
        final Function<? super Object[], ? extends R> zipper;

        ZipCoordinator(Observer<? super R> actual, Function<? super Object[], ? extends R> zipper, int count, boolean delayError) {
            this.downstream = actual;
            this.zipper = zipper;
            this.observers = new ZipObserver[count];
            this.row = (T[]) new Object[count];
            this.delayError = delayError;
        }

        public void subscribe(ObservableSource<? extends T>[] sources, int bufferSize) {
            ZipObserver<T, R>[] s = this.observers;
            int len = s.length;
            for (int i = 0; i < len; i++) {
                s[i] = new ZipObserver<>(this, bufferSize);
            }
            lazySet(0);
            this.downstream.onSubscribe(this);
            for (int i2 = 0; i2 < len && !this.cancelled; i2++) {
                sources[i2].subscribe(s[i2]);
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            if (!this.cancelled) {
                this.cancelled = true;
                cancelSources();
                if (getAndIncrement() == 0) {
                    clear();
                }
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void cancel() {
            clear();
            cancelSources();
        }

        void cancelSources() {
            for (ZipObserver<T, R> zipObserver : this.observers) {
                zipObserver.dispose();
            }
        }

        void clear() {
            for (ZipObserver<T, R> zipObserver : this.observers) {
                zipObserver.queue.clear();
            }
        }

        public void drain() {
            Throwable ex;
            if (getAndIncrement() != 0) {
                return;
            }
            ZipObserver<T, R>[] zs = this.observers;
            Observer<? super R> a = this.downstream;
            T[] os = this.row;
            boolean delayError = this.delayError;
            int missing = 1;
            while (true) {
                int i = 0;
                int emptyCount = 0;
                for (ZipObserver<T, R> z : zs) {
                    if (os[i] == null) {
                        boolean d = z.done;
                        T v = z.queue.poll();
                        boolean empty = v == null;
                        if (checkTerminated(d, empty, a, delayError, z)) {
                            return;
                        }
                        if (!empty) {
                            os[i] = v;
                        } else {
                            emptyCount++;
                        }
                    } else if (z.done && !delayError && (ex = z.error) != null) {
                        this.cancelled = true;
                        cancel();
                        a.onError(ex);
                        return;
                    }
                    i++;
                }
                if (emptyCount != 0) {
                    missing = addAndGet(-missing);
                    if (missing == 0) {
                        return;
                    }
                } else {
                    try {
                        Object obj = (R) this.zipper.apply(os.clone());
                        Objects.requireNonNull(obj, "The zipper returned a null value");
                        a.onNext(obj);
                        Arrays.fill(os, (Object) null);
                    } catch (Throwable ex2) {
                        Exceptions.throwIfFatal(ex2);
                        cancel();
                        a.onError(ex2);
                        return;
                    }
                }
            }
        }

        boolean checkTerminated(boolean d, boolean empty, Observer<? super R> a, boolean delayError, ZipObserver<?, ?> source) {
            if (this.cancelled) {
                cancel();
                return true;
            } else if (d) {
                if (delayError) {
                    if (empty) {
                        Throwable e = source.error;
                        this.cancelled = true;
                        cancel();
                        if (e != null) {
                            a.onError(e);
                        } else {
                            a.onComplete();
                        }
                        return true;
                    }
                    return false;
                }
                Throwable e2 = source.error;
                if (e2 != null) {
                    this.cancelled = true;
                    cancel();
                    a.onError(e2);
                    return true;
                } else if (empty) {
                    this.cancelled = true;
                    cancel();
                    a.onComplete();
                    return true;
                } else {
                    return false;
                }
            } else {
                return false;
            }
        }
    }

    /* loaded from: classes.dex */
    public static final class ZipObserver<T, R> implements Observer<T> {
        volatile boolean done;
        Throwable error;
        final ZipCoordinator<T, R> parent;
        final SpscLinkedArrayQueue<T> queue;
        final AtomicReference<Disposable> upstream = new AtomicReference<>();

        ZipObserver(ZipCoordinator<T, R> parent, int bufferSize) {
            this.parent = parent;
            this.queue = new SpscLinkedArrayQueue<>(bufferSize);
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onSubscribe(Disposable d) {
            DisposableHelper.setOnce(this.upstream, d);
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            this.parent.drain();
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onError(Throwable t) {
            this.error = t;
            this.done = true;
            this.parent.drain();
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onComplete() {
            this.done = true;
            this.parent.drain();
        }

        public void dispose() {
            DisposableHelper.dispose(this.upstream);
        }
    }
}
