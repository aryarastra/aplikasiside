package com.google.android.gms.common.config;

import com.google.android.gms.common.config.GservicesValue;
import com.google.android.gms.common.internal.Preconditions;

/* compiled from: com.google.android.gms:play-services-basement@@17.5.0 */
/* loaded from: classes.dex */
final class zzb extends GservicesValue<Boolean> {
    /* JADX INFO: Access modifiers changed from: package-private */
    public zzb(String str, Boolean bool) {
        super(str, bool);
    }

    @Override // com.google.android.gms.common.config.GservicesValue
    protected final /* synthetic */ Boolean zza(String str) {
        return ((GservicesValue.zza) Preconditions.checkNotNull(null)).zza(this.zza, (Boolean) this.zzb);
    }
}
