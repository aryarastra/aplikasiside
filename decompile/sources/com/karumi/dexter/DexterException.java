package com.karumi.dexter;

import com.karumi.dexter.listener.DexterError;

/* loaded from: classes.dex */
final class DexterException extends IllegalStateException {
    final DexterError error;

    /* JADX INFO: Access modifiers changed from: package-private */
    public DexterException(String str, DexterError dexterError) {
        super(str);
        this.error = dexterError;
    }
}
