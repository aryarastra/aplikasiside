package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface CompletableOperator {
    CompletableObserver apply(CompletableObserver observer) throws Throwable;
}
