package com.google.android.gms.maps.model;

import android.os.Parcel;
import android.os.Parcelable;
import com.google.android.gms.common.internal.safeparcel.SafeParcelReader;

/* compiled from: com.google.android.gms:play-services-maps@@18.0.0 */
/* loaded from: classes.dex */
public final class zzg implements Parcelable.Creator<LatLng> {
    @Override // android.os.Parcelable.Creator
    public final /* bridge */ /* synthetic */ LatLng createFromParcel(Parcel parcel) {
        int validateObjectHeader = SafeParcelReader.validateObjectHeader(parcel);
        double d = 0.0d;
        double d2 = 0.0d;
        while (parcel.dataPosition() < validateObjectHeader) {
            int readHeader = SafeParcelReader.readHeader(parcel);
            switch (SafeParcelReader.getFieldId(readHeader)) {
                case 2:
                    d = SafeParcelReader.readDouble(parcel, readHeader);
                    break;
                case 3:
                    d2 = SafeParcelReader.readDouble(parcel, readHeader);
                    break;
                default:
                    SafeParcelReader.skipUnknownField(parcel, readHeader);
                    break;
            }
        }
        SafeParcelReader.ensureAtEnd(parcel, validateObjectHeader);
        return new LatLng(d, d2);
    }

    @Override // android.os.Parcelable.Creator
    public final /* synthetic */ LatLng[] newArray(int i) {
        return new LatLng[i];
    }
}
