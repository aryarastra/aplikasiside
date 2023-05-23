package io.reactivex.rxjava3.core;

/* loaded from: classes.dex */
public interface Emitter<T> {
    void onComplete();

    void onError(Throwable error);

    void onNext(T value);
}
