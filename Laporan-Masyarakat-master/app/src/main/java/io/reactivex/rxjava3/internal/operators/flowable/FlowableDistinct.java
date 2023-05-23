package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber;
import io.reactivex.rxjava3.internal.subscriptions.EmptySubscription;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.Collection;
import java.util.Objects;
import org.reactivestreams.Subscriber;

/* loaded from: classes.dex */
public final class FlowableDistinct<T, K> extends AbstractFlowableWithUpstream<T, T> {
    final Supplier<? extends Collection<? super K>> collectionSupplier;
    final Function<? super T, K> keySelector;

    public FlowableDistinct(Flowable<T> source, Function<? super T, K> keySelector, Supplier<? extends Collection<? super K>> collectionSupplier) {
        super(source);
        this.keySelector = keySelector;
        this.collectionSupplier = collectionSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super T> subscriber) {
        try {
            Collection<? super K> collection = (Collection) ExceptionHelper.nullCheck(this.collectionSupplier.get(), "The collectionSupplier returned a null Collection.");
            this.source.subscribe((FlowableSubscriber) new DistinctSubscriber(subscriber, this.keySelector, collection));
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            EmptySubscription.error(ex, subscriber);
        }
    }

    /* loaded from: classes.dex */
    static final class DistinctSubscriber<T, K> extends BasicFuseableSubscriber<T, T> {
        final Collection<? super K> collection;
        final Function<? super T, K> keySelector;

        DistinctSubscriber(Subscriber<? super T> actual, Function<? super T, K> keySelector, Collection<? super K> collection) {
            super(actual);
            this.keySelector = keySelector;
            this.collection = collection;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T value) {
            if (this.done) {
                return;
            }
            if (this.sourceMode == 0) {
                try {
                    K key = this.keySelector.apply(value);
                    Objects.requireNonNull(key, "The keySelector returned a null key");
                    boolean b = this.collection.add(key);
                    if (b) {
                        this.downstream.onNext(value);
                        return;
                    } else {
                        this.upstream.request(1L);
                        return;
                    }
                } catch (Throwable ex) {
                    fail(ex);
                    return;
                }
            }
            this.downstream.onNext(null);
        }

        @Override // io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber, org.reactivestreams.Subscriber
        public void onError(Throwable e) {
            if (this.done) {
                RxJavaPlugins.onError(e);
                return;
            }
            this.done = true;
            this.collection.clear();
            this.downstream.onError(e);
        }

        @Override // io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber, org.reactivestreams.Subscriber
        public void onComplete() {
            if (!this.done) {
                this.done = true;
                this.collection.clear();
                this.downstream.onComplete();
            }
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.QueueFuseable
        public int requestFusion(int mode) {
            return transitiveBoundaryFusion(mode);
        }

        @Override // io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public T poll() throws Throwable {
            T v;
            while (true) {
                v = this.qs.poll();
                if (v == null) {
                    break;
                }
                Collection<? super K> collection = this.collection;
                Object obj = (K) this.keySelector.apply(v);
                Objects.requireNonNull(obj, "The keySelector returned a null key");
                if (collection.add(obj)) {
                    break;
                } else if (this.sourceMode == 2) {
                    this.upstream.request(1L);
                }
            }
            return v;
        }

        @Override // io.reactivex.rxjava3.internal.subscribers.BasicFuseableSubscriber, io.reactivex.rxjava3.internal.fuseable.SimpleQueue
        public void clear() {
            this.collection.clear();
            super.clear();
        }
    }
}
