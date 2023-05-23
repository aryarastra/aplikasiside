package io.reactivex.rxjava3.core;

import org.reactivestreams.Publisher;

@FunctionalInterface
/* loaded from: classes.dex */
public interface FlowableTransformer<Upstream, Downstream> {
    Publisher<Downstream> apply(Flowable<Upstream> upstream);
}
