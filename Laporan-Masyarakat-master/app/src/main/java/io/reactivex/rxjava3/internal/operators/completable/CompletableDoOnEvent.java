package io.reactivex.rxjava3.internal.operators.completable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Consumer;

/* loaded from: classes.dex */
public final class CompletableDoOnEvent extends Completable {
    final Consumer<? super Throwable> onEvent;
    final CompletableSource source;

    public CompletableDoOnEvent(final CompletableSource source, final Consumer<? super Throwable> onEvent) {
        this.source = source;
        this.onEvent = onEvent;
    }

    @Override // io.reactivex.rxjava3.core.Completable
    protected void subscribeActual(final CompletableObserver observer) {
        this.source.subscribe(new DoOnEvent(observer));
    }

    /* loaded from: classes.dex */
    final class DoOnEvent implements CompletableObserver {
        private final CompletableObserver observer;

        DoOnEvent(CompletableObserver observer) {
            CompletableDoOnEvent.this = this$0;
            this.observer = observer;
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onComplete() {
            try {
                CompletableDoOnEvent.this.onEvent.accept(null);
                this.observer.onComplete();
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.observer.onError(e);
            }
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onError(Throwable e) {
            try {
                CompletableDoOnEvent.this.onEvent.accept(e);
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                e = new CompositeException(e, ex);
            }
            this.observer.onError(e);
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onSubscribe(final Disposable d) {
            this.observer.onSubscribe(d);
        }
    }
}
