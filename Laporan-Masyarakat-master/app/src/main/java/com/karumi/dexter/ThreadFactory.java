package com.karumi.dexter;

import android.os.Looper;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class ThreadFactory {
    ThreadFactory() {
    }

    public static Thread makeMainThread() {
        return new MainThread();
    }

    public static Thread makeSameThread() {
        return runningMainThread() ? new MainThread() : new WorkerThread();
    }

    private static boolean runningMainThread() {
        return Looper.getMainLooper().getThread() == java.lang.Thread.currentThread();
    }
}
