package io.reactivex.rxjava3.internal.operators.mixed;

import io.reactivex.rxjava3.core.CompletableObserver;
import io.reactivex.rxjava3.core.CompletableSource;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Observer;
import io.reactivex.rxjava3.core.SingleSource;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.functions.Supplier;
import io.reactivex.rxjava3.internal.disposables.EmptyDisposable;
import io.reactivex.rxjava3.internal.operators.maybe.MaybeToObservable;
import io.reactivex.rxjava3.internal.operators.single.SingleToObservable;
import java.util.Objects;

/* loaded from: classes.dex */
final class ScalarXMapZHelper {
    private ScalarXMapZHelper() {
        throw new IllegalStateException("No instances!");
    }

    public static <T> boolean tryAsCompletable(Object source, Function<? super T, ? extends CompletableSource> mapper, CompletableObserver observer) {
        if (source instanceof Supplier) {
            Supplier<T> supplier = (Supplier) source;
            CompletableSource cs = null;
            try {
                Object obj = (Object) supplier.get();
                if (obj != 0) {
                    CompletableSource apply = mapper.apply(obj);
                    Objects.requireNonNull(apply, "The mapper returned a null CompletableSource");
                    cs = apply;
                }
                if (cs == null) {
                    EmptyDisposable.complete(observer);
                } else {
                    cs.subscribe(observer);
                }
                return true;
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }
        }
        return false;
    }

    public static <T, R> boolean tryAsMaybe(Object source, Function<? super T, ? extends MaybeSource<? extends R>> mapper, Observer<? super R> observer) {
        if (source instanceof Supplier) {
            Supplier<T> supplier = (Supplier) source;
            MaybeSource<? extends R> cs = null;
            try {
                Object obj = (Object) supplier.get();
                if (obj != 0) {
                    MaybeSource<? extends R> apply = mapper.apply(obj);
                    Objects.requireNonNull(apply, "The mapper returned a null MaybeSource");
                    cs = apply;
                }
                if (cs == null) {
                    EmptyDisposable.complete(observer);
                } else {
                    cs.subscribe(MaybeToObservable.create(observer));
                }
                return true;
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }
        }
        return false;
    }

    public static <T, R> boolean tryAsSingle(Object source, Function<? super T, ? extends SingleSource<? extends R>> mapper, Observer<? super R> observer) {
        if (source instanceof Supplier) {
            Supplier<T> supplier = (Supplier) source;
            SingleSource<? extends R> cs = null;
            try {
                Object obj = (Object) supplier.get();
                if (obj != 0) {
                    SingleSource<? extends R> apply = mapper.apply(obj);
                    Objects.requireNonNull(apply, "The mapper returned a null SingleSource");
                    cs = apply;
                }
                if (cs == null) {
                    EmptyDisposable.complete(observer);
                } else {
                    cs.subscribe(SingleToObservable.create(observer));
                }
                return true;
            } catch (Throwable ex) {
                Exceptions.throwIfFatal(ex);
                EmptyDisposable.error(ex, observer);
                return true;
            }
        }
        return false;
    }
}
