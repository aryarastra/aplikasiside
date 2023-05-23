package io.reactivex.rxjava3.internal.operators.completable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes.dex */
public final class CompletableMergeIterable extends Completable {
    final Iterable<? extends CompletableSource> sources;

    public CompletableMergeIterable(Iterable<? extends CompletableSource> sources) {
        this.sources = sources;
    }

    @Override // io.reactivex.rxjava3.core.Completable
    public void subscribeActual(final CompletableObserver observer) {
        CompositeDisposable set = new CompositeDisposable();
        AtomicInteger wip = new AtomicInteger(1);
        MergeCompletableObserver shared = new MergeCompletableObserver(observer, set, wip);
        observer.onSubscribe(shared);
        try {
            Iterator<? extends CompletableSource> it = this.sources.iterator();
            Objects.requireNonNull(it, "The source iterator returned is null");
            Iterator<? extends CompletableSource> iterator = it;
            while (!set.isDisposed()) {
                try {
                    boolean b = iterator.hasNext();
                    if (b) {
                        if (set.isDisposed()) {
                            return;
                        }
                        try {
                            CompletableSource next = iterator.next();
                            Objects.requireNonNull(next, "The iterator returned a null CompletableSource");
                            CompletableSource c = next;
                            if (set.isDisposed()) {
                                return;
                            }
                            wip.getAndIncrement();
                            c.subscribe(shared);
                        } catch (Throwable e) {
                            Exceptions.throwIfFatal(e);
                            set.dispose();
                            shared.onError(e);
                            return;
                        }
                    } else {
                        shared.onComplete();
                        return;
                    }
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    set.dispose();
                    shared.onError(e2);
                    return;
                }
            }
        } catch (Throwable e3) {
            Exceptions.throwIfFatal(e3);
            observer.onError(e3);
        }
    }

    /* loaded from: classes.dex */
    static final class MergeCompletableObserver extends AtomicBoolean implements CompletableObserver, Disposable {
        private static final long serialVersionUID = -7730517613164279224L;
        final CompletableObserver downstream;
        final CompositeDisposable set;
        final AtomicInteger wip;

        MergeCompletableObserver(CompletableObserver actual, CompositeDisposable set, AtomicInteger wip) {
            this.downstream = actual;
            this.set = set;
            this.wip = wip;
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onSubscribe(Disposable d) {
            this.set.add(d);
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onError(Throwable e) {
            this.set.dispose();
            if (compareAndSet(false, true)) {
                this.downstream.onError(e);
            } else {
                RxJavaPlugins.onError(e);
            }
        }

        @Override // io.reactivex.rxjava3.core.CompletableObserver
        public void onComplete() {
            if (this.wip.decrementAndGet() == 0) {
                this.downstream.onComplete();
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            this.set.dispose();
            set(true);
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.set.isDisposed();
        }
    }
}
