package io.reactivex.rxjava3.internal.operators.mixed;

import io.reactivex.rxjava3.core.MaybeObserver;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.internal.disposables.DisposableHelper;
import io.reactivex.rxjava3.internal.fuseable.SimplePlainQueue;
import io.reactivex.rxjava3.internal.queue.SpscLinkedArrayQueue;
import io.reactivex.rxjava3.internal.util.AtomicThrowable;
import io.reactivex.rxjava3.internal.util.ErrorMode;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class ObservableConcatMapMaybe<T, R> extends Observable<R> {
    final ErrorMode errorMode;
    final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
    final int prefetch;
    final Observable<T> source;

    public ObservableConcatMapMaybe(Observable<T> source, Function<? super T, ? extends MaybeSource<? extends R>> mapper, ErrorMode errorMode, int prefetch) {
        this.source = source;
        this.mapper = mapper;
        this.errorMode = errorMode;
        this.prefetch = prefetch;
    }

    @Override // io.reactivex.rxjava3.core.Observable
    protected void subscribeActual(Observer<? super R> observer) {
        if (!ScalarXMapZHelper.tryAsMaybe(this.source, this.mapper, observer)) {
            this.source.subscribe(new ConcatMapMaybeMainObserver(observer, this.mapper, this.prefetch, this.errorMode));
        }
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    /* loaded from: classes.dex */
    public static final class ConcatMapMaybeMainObserver<T, R> extends AtomicInteger implements Observer<T>, Disposable {
        static final int STATE_ACTIVE = 1;
        static final int STATE_INACTIVE = 0;
        static final int STATE_RESULT_VALUE = 2;
        private static final long serialVersionUID = -9140123220065488293L;
        volatile boolean cancelled;
        volatile boolean done;
        final Observer<? super R> downstream;
        final ErrorMode errorMode;
        final AtomicThrowable errors = new AtomicThrowable();
        final ConcatMapMaybeObserver<R> inner = new ConcatMapMaybeObserver<>(this);
        R item;
        final Function<? super T, ? extends MaybeSource<? extends R>> mapper;
        final SimplePlainQueue<T> queue;
        volatile int state;
        Disposable upstream;

        ConcatMapMaybeMainObserver(Observer<? super R> downstream, Function<? super T, ? extends MaybeSource<? extends R>> mapper, int prefetch, ErrorMode errorMode) {
            this.downstream = downstream;
            this.mapper = mapper;
            this.errorMode = errorMode;
            this.queue = new SpscLinkedArrayQueue(prefetch);
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onSubscribe(Disposable d) {
            if (DisposableHelper.validate(this.upstream, d)) {
                this.upstream = d;
                this.downstream.onSubscribe(this);
            }
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onNext(T t) {
            this.queue.offer(t);
            drain();
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onError(Throwable t) {
            if (this.errors.tryAddThrowableOrReport(t)) {
                if (this.errorMode == ErrorMode.IMMEDIATE) {
                    this.inner.dispose();
                }
                this.done = true;
                drain();
            }
        }

        @Override // io.reactivex.rxjava3.core.Observer
        public void onComplete() {
            this.done = true;
            drain();
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public void dispose() {
            this.cancelled = true;
            this.upstream.dispose();
            this.inner.dispose();
            this.errors.tryTerminateAndReport();
            if (getAndIncrement() == 0) {
                this.queue.clear();
                this.item = null;
            }
        }

        @Override // io.reactivex.rxjava3.disposables.Disposable
        public boolean isDisposed() {
            return this.cancelled;
        }

        void innerSuccess(R item) {
            this.item = item;
            this.state = 2;
            drain();
        }

        void innerComplete() {
            this.state = 0;
            drain();
        }

        void innerError(Throwable ex) {
            if (this.errors.tryAddThrowableOrReport(ex)) {
                if (this.errorMode != ErrorMode.END) {
                    this.upstream.dispose();
                }
                this.state = 0;
                drain();
            }
        }

        void drain() {
            if (getAndIncrement() != 0) {
                return;
            }
            int missed = 1;
            Observer<? super R> downstream = this.downstream;
            ErrorMode errorMode = this.errorMode;
            SimplePlainQueue<T> queue = this.queue;
            AtomicThrowable errors = this.errors;
            while (true) {
                if (this.cancelled) {
                    queue.clear();
                    this.item = null;
                } else {
                    int s = this.state;
                    if (errors.get() == null || (errorMode != ErrorMode.IMMEDIATE && (errorMode != ErrorMode.BOUNDARY || s != 0))) {
                        if (s == 0) {
                            boolean d = this.done;
                            T v = queue.poll();
                            boolean empty = v == null;
                            if (d && empty) {
                                errors.tryTerminateConsumer(downstream);
                                return;
                            } else if (!empty) {
                                try {
                                    MaybeSource<? extends R> apply = this.mapper.apply(v);
                                    Objects.requireNonNull(apply, "The mapper returned a null MaybeSource");
                                    MaybeSource<? extends R> ms = apply;
                                    this.state = 1;
                                    ms.subscribe(this.inner);
                                } catch (Throwable ex) {
                                    Exceptions.throwIfFatal(ex);
                                    this.upstream.dispose();
                                    queue.clear();
                                    errors.tryAddThrowableOrReport(ex);
                                    errors.tryTerminateConsumer(downstream);
                                    return;
                                }
                            }
                        } else if (s == 2) {
                            this.item = null;
                            downstream.onNext((R) this.item);
                            this.state = 0;
                        }
                    }
                }
                missed = addAndGet(-missed);
                if (missed == 0) {
                    return;
                }
            }
            queue.clear();
            this.item = null;
            errors.tryTerminateConsumer(downstream);
        }

        /* loaded from: classes.dex */
        public static final class ConcatMapMaybeObserver<R> extends AtomicReference<Disposable> implements MaybeObserver<R> {
            private static final long serialVersionUID = -3051469169682093892L;
            final ConcatMapMaybeMainObserver<?, R> parent;

            ConcatMapMaybeObserver(ConcatMapMaybeMainObserver<?, R> parent) {
                this.parent = parent;
            }

            @Override // io.reactivex.rxjava3.core.MaybeObserver, io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
            public void onSubscribe(Disposable d) {
                DisposableHelper.replace(this, d);
            }

            @Override // io.reactivex.rxjava3.core.MaybeObserver, io.reactivex.rxjava3.core.SingleObserver
            public void onSuccess(R t) {
                this.parent.innerSuccess(t);
            }

            @Override // io.reactivex.rxjava3.core.MaybeObserver, io.reactivex.rxjava3.core.SingleObserver, io.reactivex.rxjava3.core.CompletableObserver
            public void onError(Throwable e) {
                this.parent.innerError(e);
            }

            @Override // io.reactivex.rxjava3.core.MaybeObserver, io.reactivex.rxjava3.core.CompletableObserver
            public void onComplete() {
                this.parent.innerComplete();
            }

            void dispose() {
                DisposableHelper.dispose(this);
            }
        }
    }
}
