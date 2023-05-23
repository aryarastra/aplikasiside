package com.google.android.material.progressindicator;

import android.graphics.Canvas;
import android.graphics.Paint;
import com.google.android.material.progressindicator.BaseProgressIndicatorSpec;

/* loaded from: classes.dex */
public abstract class DrawingDelegate<S extends BaseProgressIndicatorSpec> {
    protected DrawableWithAnimatedVisibilityChange drawable;
    S spec;

    abstract void adjustCanvas(Canvas canvas, float f);

    public abstract void fillIndicator(Canvas canvas, Paint paint, float f, float f2, int i);

    public abstract void fillTrack(Canvas canvas, Paint paint);

    public abstract int getPreferredHeight();

    public abstract int getPreferredWidth();

    public DrawingDelegate(S spec) {
        this.spec = spec;
    }

    public void registerDrawable(DrawableWithAnimatedVisibilityChange drawable) {
        this.drawable = drawable;
    }

    public void validateSpecAndAdjustCanvas(Canvas canvas, float trackThicknessFraction) {
        this.spec.validateSpec();
        adjustCanvas(canvas, trackThicknessFraction);
    }
}
