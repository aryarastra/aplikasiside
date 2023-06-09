package com.google.android.gms.common.api.internal;

import com.google.android.gms.common.api.internal.GoogleApiManager;
import com.google.android.gms.common.internal.BaseGmsClient;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
/* loaded from: classes.dex */
public final class zabg implements BaseGmsClient.SignOutCallbacks {
    final /* synthetic */ GoogleApiManager.zaa zaa;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zabg(GoogleApiManager.zaa zaaVar) {
        this.zaa = zaaVar;
    }

    @Override // com.google.android.gms.common.internal.BaseGmsClient.SignOutCallbacks
    public final void onSignOutComplete() {
        GoogleApiManager.this.zat.post(new zabi(this));
    }
}
