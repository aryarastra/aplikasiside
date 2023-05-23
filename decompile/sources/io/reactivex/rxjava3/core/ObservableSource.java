package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface ObservableSource<T> {
    void subscribe(Observer<? super T> observer);
}
