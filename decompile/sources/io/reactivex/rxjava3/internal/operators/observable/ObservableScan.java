package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.BiFunction;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Objects;

/* loaded from: classes.dex */
public final class ObservableScan<T> extends AbstractObservableWithUpstream<T, T> {
    final BiFunction<T, T, T> accumulator;

    public ObservableScan(ObservableSource<T> source, BiFunction<T, T, T> accumulator) {
        super(source);
        this.accumulator = accumulator;
    }

    @Override // io.reactivex.rxjava3.core.Observable
    public void subscribeActual(Observer<? super T> t) {
        this.source.subscribe(new ScanObserver(t, this.accumulator));
    }

    /* loaded from: classes.dex */
    static final class ScanObserver<T> implements Observer<T>, Disposable {
        final BiFunction<T, T, T> accumulator;
        boolean done;
        final Observer<? super T> downstream;
        Disposable upstream;
        T value;

        ScanObserver(Observer<? super T> actual, BiFunction<T, T, T> accumulator) {
            this.downstream = actual;
            this.accumulator = accumulator;
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            this.upstream.dispose();
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.upstream.isDisposed();
        }

        /* JADX WARN: Type inference failed for: r2v2, types: [T, java.lang.Object] */
        @Override // io.reactivex.rxjava3.core.Observer
        public void onNext(T t) {
            if (this.done) {
                return;
            }
            Observer<? super T> a = this.downstream;
            T v = this.value;
            if (v == null) {
                this.value = t;
                a.onNext(t);
                return;
            }
            try {
                T apply = this.accumulator.apply(v, t);
                Objects.requireNonNull(apply, "The value returned by the accumulator is null");
                this.value = apply;
                a.onNext(apply);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.upstream.dispose();
                onError(e);
            }
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onError(Throwable t) {
            if (this.done) {
                RxJavaPlugins.onError(t);
                return;
            }
            this.done = true;
            this.downstream.onError(t);
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onComplete() {
            if (this.done) {
                return;
            }
            this.done = true;
            this.downstream.onComplete();
        }
    }
}
