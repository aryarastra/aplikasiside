package com.google.android.gms.internal.location;

import android.os.IBinder;
import android.os.IInterface;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.AbstractSafeParcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelWriter;

/* compiled from: com.google.android.gms:play-services-location@@18.0.0 */
/* loaded from: classes.dex */
public final class zzl extends AbstractSafeParcelable {
    public static final Parcelable.Creator<zzl> CREATOR = new zzm();
    final int zza;
    final zzj zzb;
    final com.google.android.gms.location.zzax zzc;
    final zzai zzd;

    /* JADX INFO: Access modifiers changed from: package-private */
    public zzl(int i, zzj zzjVar, IBinder iBinder, IBinder iBinder2) {
        this.zza = i;
        this.zzb = zzjVar;
        zzai zzaiVar = null;
        this.zzc = iBinder == null ? null : com.google.android.gms.location.zzaw.zzb(iBinder);
        if (iBinder2 != null) {
            IInterface queryLocalInterface = iBinder2.queryLocalInterface("com.google.android.gms.location.internal.IFusedLocationProviderCallback");
            if (queryLocalInterface instanceof zzai) {
                zzaiVar = (zzai) queryLocalInterface;
            } else {
                zzaiVar = new zzag(iBinder2);
            }
        }
        this.zzd = zzaiVar;
    }

    @Override // android.os.Parcelable
    public final void writeToParcel(Parcel parcel, int i) {
        int beginObjectHeader = SafeParcelWriter.beginObjectHeader(parcel);
        SafeParcelWriter.writeInt(parcel, 1, this.zza);
        SafeParcelWriter.writeParcelable(parcel, 2, this.zzb, i, false);
        com.google.android.gms.location.zzax zzaxVar = this.zzc;
        SafeParcelWriter.writeIBinder(parcel, 3, zzaxVar == null ? null : zzaxVar.asBinder(), false);
        zzai zzaiVar = this.zzd;
        SafeParcelWriter.writeIBinder(parcel, 4, zzaiVar != null ? zzaiVar.asBinder() : null, false);
        SafeParcelWriter.finishObjectHeader(parcel, beginObjectHeader);
    }
}
