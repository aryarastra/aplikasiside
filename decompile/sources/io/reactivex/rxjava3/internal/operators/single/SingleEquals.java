package io.reactivex.rxjava3.internal.operators.single;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes.dex */
public final class SingleEquals<T> extends Single<Boolean> {
    final SingleSource<? extends T> first;
    final SingleSource<? extends T> second;

    public SingleEquals(SingleSource<? extends T> first, SingleSource<? extends T> second) {
        this.first = first;
        this.second = second;
    }

    @Override // io.reactivex.rxjava3.core.Single
    protected void subscribeActual(final SingleObserver<? super Boolean> observer) {
        AtomicInteger count = new AtomicInteger();
        Object[] values = {null, null};
        CompositeDisposable set = new CompositeDisposable();
        observer.onSubscribe(set);
        this.first.subscribe(new InnerObserver(0, set, values, observer, count));
        this.second.subscribe(new InnerObserver(1, set, values, observer, count));
    }

    /* loaded from: classes.dex */
    static class InnerObserver<T> implements SingleObserver<T> {
        final AtomicInteger count;
        final SingleObserver<? super Boolean> downstream;
        final int index;
        final CompositeDisposable set;
        final Object[] values;

        InnerObserver(int index, CompositeDisposable set, Object[] values, SingleObserver<? super Boolean> observer, AtomicInteger count) {
            this.index = index;
            this.set = set;
            this.values = values;
            this.downstream = observer;
            this.count = count;
        }

        @Override // io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // io.reactivex.rxjava3.core.SingleObserver
        public void onSuccess(T value) {
            this.values[this.index] = value;
            if (this.count.incrementAndGet() == 2) {
                SingleObserver<? super Boolean> singleObserver = this.downstream;
                Object[] objArr = this.values;
                singleObserver.onSuccess(Boolean.valueOf(Objects.equals(objArr[0], objArr[1])));
            }
        }

        @Override // io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
        public void onError(Throwable e) {
            int state = this.count.getAndSet(-1);
            if (state == 0 || state == 1) {
                this.set.dispose();
                this.downstream.onError(e);
                return;
            }
            RxJavaPlugins.onError(e);
        }
    }
}
