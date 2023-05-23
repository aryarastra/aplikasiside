package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface CompletableConverter<R> {
    R apply(Completable upstream);
}
