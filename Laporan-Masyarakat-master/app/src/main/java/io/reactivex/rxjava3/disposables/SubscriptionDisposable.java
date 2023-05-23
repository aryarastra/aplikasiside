package io.reactivex.rxjava3.disposables;

import org.reactivestreams.Subscription;

/* loaded from: classes.dex */
final class SubscriptionDisposable extends ReferenceDisposable<Subscription> {
    private static final long serialVersionUID = -707001650852963139L;

    public SubscriptionDisposable(Subscription value) {
        super(value);
    }

    @Override // io.reactivex.rxjava3.disposables.ReferenceDisposable
    public void onDisposed(Subscription value) {
        value.cancel();
    }
}
