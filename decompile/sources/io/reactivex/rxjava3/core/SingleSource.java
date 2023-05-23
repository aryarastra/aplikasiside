package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface SingleSource<T> {
    void subscribe(SingleObserver<? super T> observer);
}
