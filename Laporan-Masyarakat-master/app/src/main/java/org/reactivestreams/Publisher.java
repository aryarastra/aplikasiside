package org.reactivestreams;

/* loaded from: classes11.dex */
public interface Publisher<T> {
    void subscribe(Subscriber<? super T> subscriber);
}
