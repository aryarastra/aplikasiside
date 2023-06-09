package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.fuseable.ScalarSupplier;

/* loaded from: classes.dex */
public final class ObservableEmpty extends Observable<Object> implements ScalarSupplier<Object> {
    public static final Observable<Object> INSTANCE = new ObservableEmpty();

    private ObservableEmpty() {
    }

    @Override // io.reactivex.rxjava3.core.Observable
    protected void subscribeActual(Observer<? super Object> o) {
        EmptyDisposable.complete(o);
    }

    @Override // io.reactivex.rxjava3.internal.fuseable.ScalarSupplier, io.reactivex.rxjava3.functions.Supplier
    public Object get() {
        return null;
    }
}
