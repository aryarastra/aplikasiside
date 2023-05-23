package com.google.android.gms.common.api.internal;

import android.app.Activity;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
/* loaded from: classes.dex */
public final class zaa extends ActivityLifecycleObserver {
    private final WeakReference<C0008zaa> zaa;

    public zaa(Activity activity) {
        this(C0008zaa.zab(activity));
    }

    private zaa(C0008zaa c0008zaa) {
        this.zaa = new WeakReference<>(c0008zaa);
    }

    @Override // com.google.android.gms.common.api.internal.ActivityLifecycleObserver
    public final ActivityLifecycleObserver onStopCallOnce(Runnable runnable) {
        C0008zaa c0008zaa = this.zaa.get();
        if (c0008zaa == null) {
            throw new IllegalStateException("The target activity has already been GC'd");
        }
        c0008zaa.zaa(runnable);
        return this;
    }

    /* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
    /* renamed from: com.google.android.gms.common.api.internal.zaa$zaa  reason: collision with other inner class name */
    /* loaded from: classes.dex */
    static class C0008zaa extends LifecycleCallback {
        private List<Runnable> zaa;

        /* JADX INFO: Access modifiers changed from: private */
        public static C0008zaa zab(Activity activity) {
            C0008zaa c0008zaa;
            synchronized (activity) {
                LifecycleFragment fragment = getFragment(activity);
                c0008zaa = (C0008zaa) fragment.getCallbackOrNull("LifecycleObserverOnStop", C0008zaa.class);
                if (c0008zaa == null) {
                    c0008zaa = new C0008zaa(fragment);
                }
            }
            return c0008zaa;
        }

        private C0008zaa(LifecycleFragment lifecycleFragment) {
            super(lifecycleFragment);
            this.zaa = new ArrayList();
            this.mLifecycleFragment.addCallback("LifecycleObserverOnStop", this);
        }

        /* JADX INFO: Access modifiers changed from: private */
        public final synchronized void zaa(Runnable runnable) {
            this.zaa.add(runnable);
        }

        @Override // com.google.android.gms.common.api.internal.LifecycleCallback
        public void onStop() {
            List<Runnable> list;
            synchronized (this) {
                list = this.zaa;
                this.zaa = new ArrayList();
            }
            for (Runnable runnable : list) {
                runnable.run();
            }
        }
    }
}
