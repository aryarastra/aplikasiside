package com.karumi.dexter;

import android.content.Context;
import android.content.Intent;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class IntentProvider {
    public Intent get(Context context, Class<?> cls) {
        return new Intent(context, cls);
    }
}
