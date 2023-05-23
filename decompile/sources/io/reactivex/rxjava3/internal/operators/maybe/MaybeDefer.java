package io.reactivex.rxjava3.internal.operators.maybe;

import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import java.util.Objects;

/* loaded from: classes.dex */
public final class MaybeDefer<T> extends Maybe<T> {
    final Supplier<? extends MaybeSource<? extends T>> maybeSupplier;

    public MaybeDefer(Supplier<? extends MaybeSource<? extends T>> maybeSupplier) {
        this.maybeSupplier = maybeSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Maybe
    protected void subscribeActual(MaybeObserver<? super T> observer) {
        try {
            MaybeSource<? extends T> maybeSource = this.maybeSupplier.get();
            Objects.requireNonNull(maybeSource, "The maybeSupplier returned a null MaybeSource");
            MaybeSource<? extends T> source = maybeSource;
            source.subscribe(observer);
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptyDisposable.error(ex, observer);
        }
    }
}
