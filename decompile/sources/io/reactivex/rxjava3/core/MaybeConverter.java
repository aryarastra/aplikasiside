package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface MaybeConverter<T, R> {
    R apply(Maybe<T> upstream);
}
