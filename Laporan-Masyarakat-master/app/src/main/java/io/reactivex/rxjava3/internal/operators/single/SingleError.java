package io.reactivex.rxjava3.internal.operators.single;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleObserver;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;

/* loaded from: classes.dex */
public final class SingleError<T> extends Single<T> {
    final Supplier<? extends Throwable> errorSupplier;

    public SingleError(Supplier<? extends Throwable> errorSupplier) {
        this.errorSupplier = errorSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Single
    protected void subscribeActual(SingleObserver<? super T> observer) {
        try {
            e = (Throwable) ExceptionHelper.nullCheck(this.errorSupplier.get(), "Supplier returned a null Throwable.");
        } catch (Throwable th) {
            e = th;
            Exceptions.throwIfFatal(e);
        }
        EmptyDisposable.error(e, observer);
    }
}
