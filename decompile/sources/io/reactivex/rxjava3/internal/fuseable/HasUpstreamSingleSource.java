package io.reactivex.rxjava3.internal.fuseable;

import io.reactivex.rxjava3.core.SingleSource;

/* loaded from: classes.dex */
public interface HasUpstreamSingleSource<T> {
    SingleSource<T> source();
}
