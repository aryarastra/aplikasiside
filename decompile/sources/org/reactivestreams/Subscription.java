package org.reactivestreams;

/* loaded from: classes11.dex */
public interface Subscription {
    void cancel();

    void request(long j);
}
