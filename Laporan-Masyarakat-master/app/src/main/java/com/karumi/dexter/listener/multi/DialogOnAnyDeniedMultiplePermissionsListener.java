package com.karumi.dexter.listener.multi;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.OnDialogButtonClickListener;

/* loaded from: classes.dex */
public class DialogOnAnyDeniedMultiplePermissionsListener extends BaseMultiplePermissionsListener {
    private final Context context;
    private final Drawable icon;
    private final String message;
    private final OnDialogButtonClickListener onDialogButtonClickListener;
    private final String positiveButtonText;
    private final String title;

    /* loaded from: classes.dex */
    public static class Builder {
        private String buttonText;
        private final Context context;
        private Drawable icon;
        private String message;
        private OnDialogButtonClickListener onDialogButtonClickListener;
        private String title;

        private Builder(Context context) {
            this.context = context;
        }

        public static Builder withContext(Context context) {
            return new Builder(context);
        }

        public DialogOnAnyDeniedMultiplePermissionsListener build() {
            String str = this.title;
            String str2 = str == null ? "" : str;
            String str3 = this.message;
            String str4 = str3 == null ? "" : str3;
            String str5 = this.buttonText;
            String str6 = str5 == null ? "" : str5;
            OnDialogButtonClickListener onDialogButtonClickListener = this.onDialogButtonClickListener;
            if (onDialogButtonClickListener == null) {
                onDialogButtonClickListener = new OnDialogButtonClickListener() { // from class: com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener.Builder.1
                    @Override // com.karumi.dexter.listener.OnDialogButtonClickListener
                    public void onClick() {
                    }
                };
            }
            return new DialogOnAnyDeniedMultiplePermissionsListener(this.context, str2, str4, str6, this.icon, onDialogButtonClickListener);
        }

        public Builder withButtonText(int i) {
            this.buttonText = this.context.getString(i);
            return this;
        }

        public Builder withButtonText(int i, OnDialogButtonClickListener onDialogButtonClickListener) {
            this.buttonText = this.context.getString(i);
            this.onDialogButtonClickListener = onDialogButtonClickListener;
            return this;
        }

        public Builder withButtonText(String str) {
            this.buttonText = str;
            return this;
        }

        public Builder withButtonText(String str, OnDialogButtonClickListener onDialogButtonClickListener) {
            this.buttonText = str;
            this.onDialogButtonClickListener = onDialogButtonClickListener;
            return this;
        }

        public Builder withIcon(int i) {
            this.icon = this.context.getResources().getDrawable(i);
            return this;
        }

        public Builder withIcon(Drawable drawable) {
            this.icon = drawable;
            return this;
        }

        public Builder withMessage(int i) {
            this.message = this.context.getString(i);
            return this;
        }

        public Builder withMessage(String str) {
            this.message = str;
            return this;
        }

        public Builder withTitle(int i) {
            this.title = this.context.getString(i);
            return this;
        }

        public Builder withTitle(String str) {
            this.title = str;
            return this;
        }
    }

    private DialogOnAnyDeniedMultiplePermissionsListener(Context context, String str, String str2, String str3, Drawable drawable, OnDialogButtonClickListener onDialogButtonClickListener) {
        this.context = context;
        this.title = str;
        this.message = str2;
        this.positiveButtonText = str3;
        this.icon = drawable;
        this.onDialogButtonClickListener = onDialogButtonClickListener;
    }

    private void showDialog() {
        new AlertDialog.Builder(this.context).setTitle(this.title).setMessage(this.message).setPositiveButton(this.positiveButtonText, new DialogInterface.OnClickListener() { // from class: com.karumi.dexter.listener.multi.DialogOnAnyDeniedMultiplePermissionsListener.1
            @Override // android.content.DialogInterface.OnClickListener
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
                DialogOnAnyDeniedMultiplePermissionsListener.this.onDialogButtonClickListener.onClick();
            }
        }).setIcon(this.icon).show();
    }

    @Override // com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener, com.karumi.dexter.listener.multi.MultiplePermissionsListener
    public void onPermissionsChecked(MultiplePermissionsReport multiplePermissionsReport) {
        super.onPermissionsChecked(multiplePermissionsReport);
        if (multiplePermissionsReport.areAllPermissionsGranted()) {
            return;
        }
        showDialog();
    }
}
