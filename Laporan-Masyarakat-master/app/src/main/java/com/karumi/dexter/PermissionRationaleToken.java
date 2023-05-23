package com.karumi.dexter;

/* loaded from: classes.dex */
public final class PermissionRationaleToken implements PermissionToken {
    private final DexterInstance dexterInstance;
    private boolean isTokenResolved = false;

    public PermissionRationaleToken(DexterInstance dexterInstance) {
        this.dexterInstance = dexterInstance;
    }

    @Override // com.karumi.dexter.PermissionToken
    public void cancelPermissionRequest() {
        if (this.isTokenResolved) {
            return;
        }
        this.dexterInstance.onCancelPermissionRequest();
        this.isTokenResolved = true;
    }

    @Override // com.karumi.dexter.PermissionToken
    public void continuePermissionRequest() {
        if (this.isTokenResolved) {
            return;
        }
        this.dexterInstance.onContinuePermissionRequest();
        this.isTokenResolved = true;
    }
}
