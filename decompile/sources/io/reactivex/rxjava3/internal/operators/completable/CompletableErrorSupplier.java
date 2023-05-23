package io.reactivex.rxjava3.internal.operators.completable;

import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import java.util.Objects;

/* loaded from: classes.dex */
public final class CompletableErrorSupplier extends Completable {
    final Supplier<? extends Throwable> errorSupplier;

    public CompletableErrorSupplier(Supplier<? extends Throwable> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Completable
    protected void subscribeActual(CompletableObserver observer) {
        try {
            Throwable th = this.errorSupplier.get();
            Objects.requireNonNull(th, "The error returned is null");
            e = th;
        } catch (Throwable th2) {
            e = th2;
            Exceptions.throwIfFatal(e);
        }
        EmptyDisposable.error(e, observer);
    }
}
