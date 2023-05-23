package io.reactivex.rxjava3.parallel;

@FunctionalInterface
/* loaded from: classes.dex */
public interface ParallelTransformer<Upstream, Downstream> {
    ParallelFlowable<Downstream> apply(ParallelFlowable<Upstream> upstream);
}
