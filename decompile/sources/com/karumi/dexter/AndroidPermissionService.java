package com.karumi.dexter;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.PermissionChecker;

/* loaded from: classes.dex */
class AndroidPermissionService {
    /* JADX INFO: Access modifiers changed from: package-private */
    public int checkSelfPermission(Context context, String str) {
        return PermissionChecker.checkSelfPermission(context, str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean isPermissionPermanentlyDenied(Activity activity, String str) {
        if (Build.VERSION.SDK_INT < 23) {
            return false;
        }
        return !shouldShowRequestPermissionRationale(activity, str);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public void requestPermissions(Activity activity, String[] strArr, int i) {
        if (activity == null) {
            return;
        }
        ActivityCompat.requestPermissions(activity, strArr, i);
    }

    /* JADX INFO: Access modifiers changed from: package-private */
    public boolean shouldShowRequestPermissionRationale(Activity activity, String str) {
        if (activity == null) {
            return false;
        }
        return ActivityCompat.shouldShowRequestPermissionRationale(activity, str);
    }
}
