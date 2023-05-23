package io.reactivex.rxjava3.internal.operators.observable;

import io.reactivex.rxjava3.core.ObservableSource;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.internal.observers.LambdaObserver;
import io.reactivex.rxjava3.internal.util.BlockingHelper;
import io.reactivex.rxjava3.internal.util.BlockingIgnoringReceiver;
import io.reactivex.rxjava3.internal.util.ExceptionHelper;
import java.util.Objects;

/* loaded from: classes.dex */
public final class ObservableBlockingSubscribe {
    private ObservableBlockingSubscribe() {
        throw new IllegalStateException("No instances!");
    }

    /* JADX WARN: Removed duplicated region for block: B:6:0x0017  */
    /*
        Code decompiled incorrectly, please refer to instructions dump.
        To view partially-correct code enable 'Show inconsistent code' option in preferences
    */
    public static <T> void subscribe(io.reactivex.rxjava3.core.ObservableSource<? extends T> r4, io.reactivex.rxjava3.core.Observer<? super T> r5) {
        /*
            java.util.concurrent.LinkedBlockingQueue r0 = new java.util.concurrent.LinkedBlockingQueue
            r0.<init>()
            io.reactivex.rxjava3.internal.observers.BlockingObserver r1 = new io.reactivex.rxjava3.internal.observers.BlockingObserver
            r1.<init>(r0)
            r5.onSubscribe(r1)
            r4.subscribe(r1)
        L10:
            boolean r2 = r1.isDisposed()
            if (r2 == 0) goto L17
            goto L3d
        L17:
            java.lang.Object r2 = r0.poll()
            if (r2 != 0) goto L2b
            java.lang.Object r3 = r0.take()     // Catch: java.lang.InterruptedException -> L23
            r2 = r3
            goto L2b
        L23:
            r3 = move-exception
            r1.dispose()
            r5.onError(r3)
            return
        L2b:
            boolean r3 = r1.isDisposed()
            if (r3 != 0) goto L3d
            java.lang.Object r3 = io.reactivex.rxjava3.internal.observers.BlockingObserver.TERMINATED
            if (r2 == r3) goto L3d
            boolean r3 = io.reactivex.rxjava3.internal.util.NotificationLite.acceptFull(r2, r5)
            if (r3 == 0) goto L3c
            goto L3d
        L3c:
            goto L10
        L3d:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: io.reactivex.rxjava3.internal.operators.observable.ObservableBlockingSubscribe.subscribe(io.reactivex.rxjava3.core.ObservableSource, io.reactivex.rxjava3.core.Observer):void");
    }

    public static <T> void subscribe(ObservableSource<? extends T> o) {
        BlockingIgnoringReceiver callback = new BlockingIgnoringReceiver();
        LambdaObserver<T> ls = new LambdaObserver<>(Functions.emptyConsumer(), callback, callback, Functions.emptyConsumer());
        o.subscribe(ls);
        BlockingHelper.awaitForComplete(callback, ls);
        Throwable e = callback.error;
        if (e != null) {
            throw ExceptionHelper.wrapOrThrow(e);
        }
    }

    public static <T> void subscribe(ObservableSource<? extends T> o, final Consumer<? super T> onNext, final Consumer<? super Throwable> onError, final Action onComplete) {
        Objects.requireNonNull(onNext, "onNext is null");
        Objects.requireNonNull(onError, "onError is null");
        Objects.requireNonNull(onComplete, "onComplete is null");
        subscribe(o, new LambdaObserver(onNext, onError, onComplete, Functions.emptyConsumer()));
    }
}
