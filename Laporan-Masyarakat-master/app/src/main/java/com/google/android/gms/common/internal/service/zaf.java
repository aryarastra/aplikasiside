package com.google.android.gms.common.internal.service;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.internal.BaseImplementation;

/* JADX INFO: Access modifiers changed from: package-private */
/* compiled from: com.google.android.gms:play-services-base@@17.5.0 */
/* loaded from: classes.dex */
public abstract class zaf<R extends Result> extends BaseImplementation.ApiMethodImpl<R, zah> {
    public zaf(GoogleApiClient googleApiClient) {
        super(Common.API, googleApiClient);
    }
}
