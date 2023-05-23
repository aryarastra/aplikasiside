package io.reactivex.rxjava3.core;

@FunctionalInterface
/* loaded from: classes.dex */
public interface CompletableTransformer {
    CompletableSource apply(Completable upstream);
}
