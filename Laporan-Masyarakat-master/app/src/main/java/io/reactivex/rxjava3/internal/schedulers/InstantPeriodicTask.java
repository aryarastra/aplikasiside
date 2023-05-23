package io.reactivex.rxjava3.internal.schedulers;

import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.exceptions.Exceptions;
import io.reactivex.rxjava3.internal.functions.Functions;
import io.reactivex.rxjava3.plugins.RxJavaPlugins;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.atomic.AtomicReference;

/* loaded from: classes.dex */
public final class InstantPeriodicTask implements Callable<Void>, Disposable {
    static final FutureTask<Void> CANCELLED = new FutureTask<>(Functions.EMPTY_RUNNABLE, null);
    final ExecutorService executor;
    final AtomicReference<Future<?>> first = new AtomicReference<>();
    final AtomicReference<Future<?>> rest = new AtomicReference<>();
    Thread runner;
    final Runnable task;

    public InstantPeriodicTask(Runnable task, ExecutorService executor) {
        this.task = task;
        this.executor = executor;
    }

    @Override // java.util.concurrent.Callable
    public Void call() {
        this.runner = Thread.currentThread();
        try {
            this.task.run();
            setRest(this.executor.submit(this));
            this.runner = null;
        } catch (Throwable ex) {
            Exceptions.throwIfFatal(ex);
            this.runner = null;
            RxJavaPlugins.onError(ex);
        }
        return null;
    }

    @Override // io.reactivex.rxjava3.disposables.Disposable
    public void dispose() {
        AtomicReference<Future<?>> atomicReference = this.first;
        FutureTask<Void> futureTask = CANCELLED;
        Future<?> current = atomicReference.getAndSet(futureTask);
        if (current != null && current != futureTask) {
            current.cancel(this.runner != Thread.currentThread());
        }
        Future<?> current2 = this.rest.getAndSet(futureTask);
        if (current2 != null && current2 != futureTask) {
            current2.cancel(this.runner != Thread.currentThread());
        }
    }

    @Override // io.reactivex.rxjava3.disposables.Disposable
    public boolean isDisposed() {
        return this.first.get() == CANCELLED;
    }

    public void setFirst(Future<?> f) {
        Future<?> current;
        do {
            current = this.first.get();
            if (current == CANCELLED) {
                f.cancel(this.runner != Thread.currentThread());
                return;
            }
        } while (!this.first.compareAndSet(current, f));
    }

    void setRest(Future<?> f) {
        Future<?> current;
        do {
            current = this.rest.get();
            if (current == CANCELLED) {
                f.cancel(this.runner != Thread.currentThread());
                return;
            }
        } while (!this.rest.compareAndSet(current, f));
    }
}
