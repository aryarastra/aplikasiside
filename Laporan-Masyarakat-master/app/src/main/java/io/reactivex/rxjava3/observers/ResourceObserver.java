package io.reactivex.rxjava3.observers;

import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.disposables.ListCompositeDisposable;
import io.reactivex.rxjava3.internal.util.EndConsumerHelper;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public abstract class ResourceObserver<T> implements Observer<T>, Disposable {
    private final AtomicReference<Disposable> upstream = new AtomicReference<>();
    private final ListCompositeDisposable resources = new ListCompositeDisposable();

    public final void add(Disposable resource) {
        Objects.requireNonNull(resource, "resource is null");
        this.resources.add(resource);
    }

    @Override // io.reactivex.rxjava3.core.Observer
    public final void onSubscribe(Disposable d) {
        if (EndConsumerHelper.setOnce(this.upstream, d, getClass())) {
            onStart();
        }
    }

    protected void onStart() {
    }

    @Override // io.reactivex.rxjava3.disposables.Disposable
    public final void dispose() {
        if (DisposableHelper.dispose(this.upstream)) {
            this.resources.dispose();
        }
    }

    @Override // io.reactivex.rxjava3.disposables.Disposable
    public final boolean isDisposed() {
        return DisposableHelper.isDisposed(this.upstream.get());
    }
}
