package com.karumi.dexter.listener.single;

import android.view.View;
import com.google.android.material.snackbar.Snackbar;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.SettingsClickListener;
import com.karumi.dexter.listener.SnackbarUtils;

/* loaded from: classes.dex */
public class SnackbarOnDeniedPermissionListener extends BasePermissionListener {
    private final String buttonText;
    private final int duration;
    private final View.OnClickListener onButtonClickListener;
    private final Snackbar.Callback snackbarCallback;
    private final String text;
    private final View view;

    /* loaded from: classes.dex */
    public static class Builder {
        private String buttonText;
        private int duration = 0;
        private View.OnClickListener onClickListener;
        private Snackbar.Callback snackbarCallback;
        private final String text;
        private final View view;

        private Builder(View view, String str) {
            this.view = view;
            this.text = str;
        }

        public static Builder with(View view, int i) {
            return with(view, view.getContext().getString(i));
        }

        public static Builder with(View view, String str) {
            return new Builder(view, str);
        }

        public SnackbarOnDeniedPermissionListener build() {
            return new SnackbarOnDeniedPermissionListener(this.view, this.text, this.buttonText, this.onClickListener, this.snackbarCallback, this.duration);
        }

        public Builder withButton(int i, View.OnClickListener onClickListener) {
            return withButton(this.view.getContext().getString(i), onClickListener);
        }

        public Builder withButton(String str, View.OnClickListener onClickListener) {
            this.buttonText = str;
            this.onClickListener = onClickListener;
            return this;
        }

        public Builder withCallback(Snackbar.Callback callback) {
            this.snackbarCallback = callback;
            return this;
        }

        public Builder withDuration(int i) {
            this.duration = i;
            return this;
        }

        public Builder withOpenSettingsButton(int i) {
            return withOpenSettingsButton(this.view.getContext().getString(i));
        }

        public Builder withOpenSettingsButton(String str) {
            this.buttonText = str;
            this.onClickListener = new SettingsClickListener();
            return this;
        }
    }

    private SnackbarOnDeniedPermissionListener(View view, String str, String str2, View.OnClickListener onClickListener, Snackbar.Callback callback, int i) {
        this.view = view;
        this.text = str;
        this.buttonText = str2;
        this.onButtonClickListener = onClickListener;
        this.snackbarCallback = callback;
        this.duration = i;
    }

    @Override // com.karumi.dexter.listener.single.BasePermissionListener, com.karumi.dexter.listener.single.PermissionListener
    public void onPermissionDenied(PermissionDeniedResponse permissionDeniedResponse) {
        super.onPermissionDenied(permissionDeniedResponse);
        SnackbarUtils.show(this.view, this.text, this.duration, this.buttonText, this.onButtonClickListener, this.snackbarCallback);
    }
}
