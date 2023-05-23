package com.google.android.gms.common;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-basement@@17.5.0 */
/* loaded from: classes.dex */
public enum zzo {
    DEFAULT(0),
    UNKNOWN_CERT(1),
    TEST_KEYS_REJECTED(2),
    PACKAGE_NOT_FOUND(3),
    GENERIC_ERROR(4);
    
    final int zzb;

    zzo(int i) {
        this.zzb = i;
    }

    public static zzo zza(int i) {
        zzo[] values;
        for (zzo zzoVar : values()) {
            if (zzoVar.zzb == i) {
                return zzoVar;
            }
        }
        return DEFAULT;
    }
}
