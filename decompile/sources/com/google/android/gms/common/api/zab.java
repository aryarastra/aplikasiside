package com.google.android.gms.common.api;

import com.google.android.gms.common.api.PendingResult;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
/* loaded from: classes.dex */
public final class zab implements PendingResult.StatusListener {
    private final /* synthetic */ Batch zaa;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zab(Batch batch) {
        this.zaa = batch;
    }

    @Override // com.google.android.gms.common.api.PendingResult.StatusListener
    public final void onComplete(Status status) {
        Object obj;
        int i;
        boolean z;
        boolean z2;
        Status status2;
        PendingResult[] pendingResultArr;
        obj = this.zaa.zaf;
        synchronized (obj) {
            if (this.zaa.isCanceled()) {
                return;
            }
            if (status.isCanceled()) {
                Batch.zaa(this.zaa, true);
            } else if (!status.isSuccess()) {
                Batch.zab(this.zaa, true);
            }
            Batch.zab(this.zaa);
            i = this.zaa.zab;
            if (i == 0) {
                z = this.zaa.zad;
                if (z) {
                    super/*com.google.android.gms.common.api.internal.BasePendingResult*/.cancel();
                } else {
                    z2 = this.zaa.zac;
                    if (z2) {
                        status2 = new Status(13);
                    } else {
                        status2 = Status.RESULT_SUCCESS;
                    }
                    Batch batch = this.zaa;
                    pendingResultArr = batch.zae;
                    batch.setResult(new BatchResult(status2, pendingResultArr));
                }
            }
        }
    }
}
