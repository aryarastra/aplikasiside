package com.karumi.dexter;

import android.os.Handler;
import android.os.Looper;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class MainThread implements Thread {
    private static boolean runningMainThread() {
        return Looper.getMainLooper() == Looper.myLooper();
    }

    @Override // com.karumi.dexter.Thread
    public void execute(Runnable runnable) {
        if (runningMainThread()) {
            runnable.run();
        } else {
            new Handler(Looper.getMainLooper()).post(runnable);
        }
    }

    @Override // com.karumi.dexter.Thread
    public void loop() {
    }
}
