package io.reactivex.rxjava3.internal.fuseable;

import org.reactivestreams.Publisher;

/* loaded from: classes.dex */
public interface HasUpstreamPublisher<T> {
    Publisher<T> source();
}
