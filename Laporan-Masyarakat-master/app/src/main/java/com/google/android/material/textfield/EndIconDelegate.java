package com.google.android.material.textfield;

import android.content.Context;
import com.google.android.material.internal.CheckableImageButton;

/* loaded from: classes.dex */
public abstract class EndIconDelegate {
    Context context;
    CheckableImageButton endIconView;
    TextInputLayout textInputLayout;

    public abstract void initialize();

    public EndIconDelegate(TextInputLayout textInputLayout) {
        this.textInputLayout = textInputLayout;
        this.context = textInputLayout.getContext();
        this.endIconView = textInputLayout.getEndIconView();
    }

    public boolean shouldTintIconOnError() {
        return false;
    }

    public boolean isBoxBackgroundModeSupported(int boxBackgroundMode) {
        return true;
    }

    public void onSuffixVisibilityChanged(boolean visible) {
    }
}
