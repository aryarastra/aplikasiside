package io.reactivex.rxjava3.internal.operators.flowable;

import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableSubscriber;
import io.reactivex.rxjava3.exceptions.CompositeException;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.subscribers.SinglePostCompleteSubscriber;
import java.util.Objects;
import org.reactivestreams.Subscriber;

/* loaded from: classes.dex */
public final class FlowableMapNotification<T, R> extends AbstractFlowableWithUpstream<T, R> {
    final Supplier<? extends R> onCompleteSupplier;
    final Function<? super Throwable, ? extends R> onErrorMapper;
    final Function<? super T, ? extends R> onNextMapper;

    public FlowableMapNotification(Flowable<T> source, Function<? super T, ? extends R> onNextMapper, Function<? super Throwable, ? extends R> onErrorMapper, Supplier<? extends R> onCompleteSupplier) {
        super(source);
        this.onNextMapper = onNextMapper;
        this.onErrorMapper = onErrorMapper;
        this.onCompleteSupplier = onCompleteSupplier;
    }

    @Override // io.reactivex.rxjava3.core.Flowable
    protected void subscribeActual(Subscriber<? super R> s) {
        this.source.subscribe((FlowableSubscriber) new MapNotificationSubscriber(s, this.onNextMapper, this.onErrorMapper, this.onCompleteSupplier));
    }

    /* loaded from: classes.dex */
    static final class MapNotificationSubscriber<T, R> extends SinglePostCompleteSubscriber<T, R> {
        private static final long serialVersionUID = 2757120512858778108L;
        final Supplier<? extends R> onCompleteSupplier;
        final Function<? super Throwable, ? extends R> onErrorMapper;
        final Function<? super T, ? extends R> onNextMapper;

        MapNotificationSubscriber(Subscriber<? super R> actual, Function<? super T, ? extends R> onNextMapper, Function<? super Throwable, ? extends R> onErrorMapper, Supplier<? extends R> onCompleteSupplier) {
            super(actual);
            this.onNextMapper = onNextMapper;
            this.onErrorMapper = onErrorMapper;
            this.onCompleteSupplier = onCompleteSupplier;
        }

        @Override // org.reactivestreams.Subscriber
        public void onNext(T t) {
            try {
                R p = this.onNextMapper.apply(t);
                Objects.requireNonNull(p, "The onNext publisher returned is null");
                this.produced++;
                this.downstream.onNext(p);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(e);
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onError(Throwable t) {
            try {
                R p = this.onErrorMapper.apply(t);
                Objects.requireNonNull(p, "The onError publisher returned is null");
                complete(p);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(new CompositeException(t, e));
            }
        }

        @Override // org.reactivestreams.Subscriber
        public void onComplete() {
            try {
                R p = this.onCompleteSupplier.get();
                Objects.requireNonNull(p, "The onComplete publisher returned is null");
                complete(p);
            } catch (Throwable e) {
                Exceptions.throwIfFatal(e);
                this.downstream.onError(e);
            }
        }
    }
}
