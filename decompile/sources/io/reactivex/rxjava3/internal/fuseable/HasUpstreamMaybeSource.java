package io.reactivex.rxjava3.internal.fuseable;

import io.reactivex.rxjava3.core.MaybeSource;

/* loaded from: classes.dex */
public interface HasUpstreamMaybeSource<T> {
    MaybeSource<T> source();
}
