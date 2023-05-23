package androidx.room.rxjava3;

import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.core.FlowableEmitter;
import io.reactivex.rxjava3.core.FlowableOnSubscribe;
import io.reactivex.rxjava3.core.Maybe;
import io.reactivex.rxjava3.core.MaybeSource;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.core.ObservableEmitter;
import io.reactivex.rxjava3.core.ObservableOnSubscribe;
import io.reactivex.rxjava3.core.Scheduler;
import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Action;
import io.reactivex.rxjava3.functions.Function;
import io.reactivex.rxjava3.schedulers.Schedulers;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

/* loaded from: classes.dex */
public final class RxRoom {
    public static final Object NOTHING = new Object();

    public static Flowable<Object> createFlowable(final RoomDatabase database, final String... tableNames) {
        return Flowable.create(new FlowableOnSubscribe() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda0
            @Override // io.reactivex.rxjava3.core.FlowableOnSubscribe
            public final void subscribe(FlowableEmitter flowableEmitter) {
                RxRoom.lambda$createFlowable$1(tableNames, database, flowableEmitter);
            }
        }, BackpressureStrategy.LATEST);
    }

    public static /* synthetic */ void lambda$createFlowable$1(String[] tableNames, final RoomDatabase database, final FlowableEmitter emitter) throws Throwable {
        final InvalidationTracker.Observer observer = new InvalidationTracker.Observer(tableNames) { // from class: androidx.room.rxjava3.RxRoom.1
            @Override // androidx.room.InvalidationTracker.Observer
            public void onInvalidated(Set<String> tables) {
                if (!emitter.isCancelled()) {
                    emitter.onNext(RxRoom.NOTHING);
                }
            }
        };
        if (!emitter.isCancelled()) {
            database.getInvalidationTracker().addObserver(observer);
            emitter.setDisposable(Disposable.CC.fromAction(new Action() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda3
                @Override // io.reactivex.rxjava3.functions.Action
                public final void run() {
                    RoomDatabase.this.getInvalidationTracker().removeObserver(observer);
                }
            }));
        }
        if (!emitter.isCancelled()) {
            emitter.onNext(NOTHING);
        }
    }

    public static <T> Flowable<T> createFlowable(final RoomDatabase database, final boolean inTransaction, final String[] tableNames, final Callable<T> callable) {
        Scheduler scheduler = Schedulers.from(getExecutor(database, inTransaction));
        final Maybe<T> maybe = Maybe.fromCallable(callable);
        return (Flowable<T>) createFlowable(database, tableNames).subscribeOn(scheduler).unsubscribeOn(scheduler).observeOn(scheduler).flatMapMaybe(new Function() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda5
            @Override // io.reactivex.rxjava3.functions.Function
            public final Object apply(Object obj) {
                return RxRoom.lambda$createFlowable$2(Maybe.this, obj);
            }
        });
    }

    public static /* synthetic */ MaybeSource lambda$createFlowable$2(Maybe maybe, Object o) throws Throwable {
        return maybe;
    }

    public static Observable<Object> createObservable(final RoomDatabase database, final String... tableNames) {
        return Observable.create(new ObservableOnSubscribe() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda1
            @Override // io.reactivex.rxjava3.core.ObservableOnSubscribe
            public final void subscribe(ObservableEmitter observableEmitter) {
                RxRoom.lambda$createObservable$4(tableNames, database, observableEmitter);
            }
        });
    }

    public static /* synthetic */ void lambda$createObservable$4(String[] tableNames, final RoomDatabase database, final ObservableEmitter emitter) throws Throwable {
        final InvalidationTracker.Observer observer = new InvalidationTracker.Observer(tableNames) { // from class: androidx.room.rxjava3.RxRoom.2
            @Override // androidx.room.InvalidationTracker.Observer
            public void onInvalidated(Set<String> tables) {
                emitter.onNext(RxRoom.NOTHING);
            }
        };
        database.getInvalidationTracker().addObserver(observer);
        emitter.setDisposable(Disposable.CC.fromAction(new Action() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda4
            @Override // io.reactivex.rxjava3.functions.Action
            public final void run() {
                RoomDatabase.this.getInvalidationTracker().removeObserver(observer);
            }
        }));
        emitter.onNext(NOTHING);
    }

    public static <T> Observable<T> createObservable(final RoomDatabase database, final boolean inTransaction, final String[] tableNames, final Callable<T> callable) {
        Scheduler scheduler = Schedulers.from(getExecutor(database, inTransaction));
        final Maybe<T> maybe = Maybe.fromCallable(callable);
        return (Observable<T>) createObservable(database, tableNames).subscribeOn(scheduler).unsubscribeOn(scheduler).observeOn(scheduler).flatMapMaybe(new Function() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda6
            @Override // io.reactivex.rxjava3.functions.Function
            public final Object apply(Object obj) {
                return RxRoom.lambda$createObservable$5(Maybe.this, obj);
            }
        });
    }

    public static /* synthetic */ MaybeSource lambda$createObservable$5(Maybe maybe, Object o) throws Throwable {
        return maybe;
    }

    public static <T> Single<T> createSingle(final Callable<T> callable) {
        return Single.create(new SingleOnSubscribe() { // from class: androidx.room.rxjava3.RxRoom$$ExternalSyntheticLambda2
            @Override // io.reactivex.rxjava3.core.SingleOnSubscribe
            public final void subscribe(SingleEmitter singleEmitter) {
                RxRoom.lambda$createSingle$6(callable, singleEmitter);
            }
        });
    }

    public static /* synthetic */ void lambda$createSingle$6(Callable callable, SingleEmitter emitter) throws Throwable {
        try {
            emitter.onSuccess(callable.call());
        } catch (EmptyResultSetException e) {
            emitter.tryOnError(e);
        }
    }

    private static Executor getExecutor(RoomDatabase database, boolean inTransaction) {
        if (inTransaction) {
            return database.getTransactionExecutor();
        }
        return database.getQueryExecutor();
    }

    private RxRoom() {
    }
}
