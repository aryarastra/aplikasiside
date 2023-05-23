package io.reactivex.rxjava3.internal.operators.completable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.internal.operators.completable.CompletableMergeArrayDelayError;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/* loaded from: classes.dex */
public final class CompletableMergeDelayErrorIterable extends Completable {
    final Iterable<? extends CompletableSource> sources;

    public CompletableMergeDelayErrorIterable(Iterable<? extends CompletableSource> sources) {
        this.sources = sources;
    }

    @Override // io.reactivex.rxjava3.core.Completable
    public void subscribeActual(final CompletableObserver observer) {
        CompositeDisposable set = new CompositeDisposable();
        observer.onSubscribe(set);
        try {
            Iterator<? extends CompletableSource> it = this.sources.iterator();
            Objects.requireNonNull(it, "The source iterator returned is null");
            Iterator<? extends CompletableSource> iterator = it;
            AtomicInteger wip = new AtomicInteger(1);
            AtomicThrowable errors = new AtomicThrowable();
            set.add(new CompletableMergeArrayDelayError.TryTerminateAndReportDisposable(errors));
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
                            c.subscribe(new CompletableMergeArrayDelayError.MergeInnerCompletableObserver(observer, set, errors, wip));
                        } catch (Throwable e) {
                            Exceptions.throwIfFatal(e);
                            errors.tryAddThrowableOrReport(e);
                        }
                    }
                } catch (Throwable e2) {
                    Exceptions.throwIfFatal(e2);
                    errors.tryAddThrowableOrReport(e2);
                }
                if (wip.decrementAndGet() == 0) {
                    errors.tryTerminateConsumer(observer);
                    return;
                }
                return;
            }
        } catch (Throwable e3) {
            Exceptions.throwIfFatal(e3);
            observer.onError(e3);
        }
    }
}
