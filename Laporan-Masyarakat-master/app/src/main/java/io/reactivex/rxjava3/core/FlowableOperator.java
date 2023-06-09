package io.reactivex.rxjava3.core;

import org.reactivestreams.Subscriber;

@FunctionalInterface
/* loaded from: classes.dex */
public interface FlowableOperator<Downstream, Upstream> {
    Subscriber<? super Upstream> apply(Subscriber<? super Downstream> subscriber) throws Throwable;
}
