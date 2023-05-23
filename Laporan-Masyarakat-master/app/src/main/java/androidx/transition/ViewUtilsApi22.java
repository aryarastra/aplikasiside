package androidx.transition;

import android.view.View;

/* JADX INFO: Access modifiers changed from: package-private */
/* loaded from: classes.dex */
public class ViewUtilsApi22 extends ViewUtilsApi21 {
    private static boolean sTryHiddenSetLeftTopRightBottom = true;

    @Override // androidx.transition.ViewUtilsBase
    public void setLeftTopRightBottom(View v, int left, int top, int right, int bottom) {
        if (sTryHiddenSetLeftTopRightBottom) {
            try {
                v.setLeftTopRightBottom(left, top, right, bottom);
            } catch (NoSuchMethodError e) {
                sTryHiddenSetLeftTopRightBottom = false;
            }
        }
    }
}
