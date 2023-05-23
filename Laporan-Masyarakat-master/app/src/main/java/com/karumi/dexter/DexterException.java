package com.karumi.dexter;

import com.karumi.dexter.listener.DexterError;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public final class DexterException extends IllegalStateException {
    final DexterError error;

    public DexterException(String str, DexterError dexterError) {
        super(str);
        this.error = dexterError;
    }
}
