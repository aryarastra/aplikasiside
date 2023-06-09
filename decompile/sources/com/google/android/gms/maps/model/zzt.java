package com.google.android.gms.maps.model;

import android.os.RemoteException;
import com.google.android.gms.internal.maps.zzaj;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-maps@@18.0.0 */
/* loaded from: classes.dex */
public final class zzt implements TileProvider {
    final /* synthetic */ TileOverlayOptions zza;
    private final zzaj zzb;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzt(TileOverlayOptions tileOverlayOptions) {
        zzaj zzajVar;
        this.zza = tileOverlayOptions;
        zzajVar = tileOverlayOptions.zza;
        this.zzb = zzajVar;
    }

    @Override // com.google.android.gms.maps.model.TileProvider
    public final Tile getTile(int i, int i2, int i3) {
        try {
            return this.zzb.zzb(i, i2, i3);
        } catch (RemoteException e) {
            return null;
        }
    }
}
