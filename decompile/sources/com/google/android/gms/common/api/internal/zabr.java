package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.common.api.internal.GoogleApiManager;
import com.google.android.gms.common.internal.BaseGmsClient;
import com.google.android.gms.common.internal.ConnectionTelemetryConfiguration;
import com.google.android.gms.common.internal.RootTelemetryConfigManager;
import com.google.android.gms.common.internal.RootTelemetryConfiguration;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
/* loaded from: classes.dex */
public final class zabr<T> implements OnCompleteListener<T> {
    private final GoogleApiManager zaa;
    private final int zab;
    private final ApiKey<?> zac;
    private final long zad;

    private zabr(GoogleApiManager googleApiManager, int i, ApiKey<?> apiKey, long j) {
        this.zaa = googleApiManager;
        this.zab = i;
        this.zac = apiKey;
        this.zad = j;
    }

    @Override // com.google.android.gms.tasks.OnCompleteListener
    public final void onComplete(Task<T> task) {
        int i;
        int i2;
        int i3;
        int i4;
        int i5;
        long j;
        long j2;
        if (!this.zaa.zad()) {
            return;
        }
        boolean z = true;
        boolean z2 = this.zad > 0;
        RootTelemetryConfiguration config = RootTelemetryConfigManager.getInstance().getConfig();
        if (config != null) {
            if (!config.getMethodInvocationTelemetryEnabled()) {
                return;
            }
            z2 &= config.getMethodTimingTelemetryEnabled();
            i = config.getBatchPeriodMillis();
            int maxMethodInvocationsInBatch = config.getMaxMethodInvocationsInBatch();
            int version = config.getVersion();
            GoogleApiManager.zaa zaa = this.zaa.zaa(this.zac);
            if (zaa != null && zaa.zab().isConnected() && (zaa.zab() instanceof BaseGmsClient)) {
                ConnectionTelemetryConfiguration zaa2 = zaa(zaa, this.zab);
                if (zaa2 == null) {
                    return;
                }
                z = (!zaa2.getMethodTimingTelemetryEnabled() || this.zad <= 0) ? false : false;
                maxMethodInvocationsInBatch = zaa2.getMaxMethodInvocationsLogged();
                z2 = z;
            }
            i2 = version;
            i3 = maxMethodInvocationsInBatch;
        } else {
            i = 5000;
            i2 = 0;
            i3 = 100;
        }
        GoogleApiManager googleApiManager = this.zaa;
        if (task.isSuccessful()) {
            i4 = 0;
            i5 = 0;
        } else if (task.isCanceled()) {
            i4 = 100;
            i5 = -1;
        } else {
            Exception exception = task.getException();
            if (exception instanceof ApiException) {
                Status status = ((ApiException) exception).getStatus();
                int statusCode = status.getStatusCode();
                ConnectionResult connectionResult = status.getConnectionResult();
                i5 = connectionResult == null ? -1 : connectionResult.getErrorCode();
                i4 = statusCode;
            } else {
                i4 = 101;
                i5 = -1;
            }
        }
        if (!z2) {
            j = 0;
            j2 = 0;
        } else {
            j = this.zad;
            j2 = System.currentTimeMillis();
        }
        googleApiManager.zaa(new com.google.android.gms.common.internal.zao(this.zab, i4, i5, j, j2), i2, i, i3);
    }

    private static ConnectionTelemetryConfiguration zaa(GoogleApiManager.zaa<?> zaaVar, int i) {
        int[] methodInvocationMethodKeyAllowlist;
        ConnectionTelemetryConfiguration telemetryConfiguration = ((BaseGmsClient) zaaVar.zab()).getTelemetryConfiguration();
        if (telemetryConfiguration != null) {
            boolean z = false;
            if (telemetryConfiguration.getMethodInvocationTelemetryEnabled() && ((methodInvocationMethodKeyAllowlist = telemetryConfiguration.getMethodInvocationMethodKeyAllowlist()) == null || ArrayUtils.contains(methodInvocationMethodKeyAllowlist, i))) {
                z = true;
            }
            if (!z || zaaVar.zam() >= telemetryConfiguration.getMaxMethodInvocationsLogged()) {
                return null;
            }
            return telemetryConfiguration;
        }
        return null;
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public static <T> zabr<T> zaa(GoogleApiManager googleApiManager, int i, ApiKey<?> apiKey) {
        if (googleApiManager.zad()) {
            boolean z = true;
            RootTelemetryConfiguration config = RootTelemetryConfigManager.getInstance().getConfig();
            if (config != null) {
                if (!config.getMethodInvocationTelemetryEnabled()) {
                    return null;
                }
                z = config.getMethodTimingTelemetryEnabled();
                GoogleApiManager.zaa zaa = googleApiManager.zaa(apiKey);
                if (zaa != null && zaa.zab().isConnected() && (zaa.zab() instanceof BaseGmsClient)) {
                    ConnectionTelemetryConfiguration zaa2 = zaa(zaa, i);
                    if (zaa2 == null) {
                        return null;
                    }
                    zaa.zan();
                    z = zaa2.getMethodTimingTelemetryEnabled();
                }
            }
            return new zabr<>(googleApiManager, i, apiKey, z ? System.currentTimeMillis() : 0L);
        }
        return null;
    }
}
