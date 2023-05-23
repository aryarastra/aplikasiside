package com.google.android.material.progressindicator;

import android.animation.Animator;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat;

/* loaded from: classes.dex */
public abstract class IndeterminateAnimatorDelegate<T extends Animator> {
    protected IndeterminateDrawable drawable;
    protected final int[] segmentColors;
    protected final float[] segmentPositions;

    public abstract void cancelAnimatorImmediately();

    public abstract void invalidateSpecValues();

    public abstract void registerAnimatorsCompleteCallback(Animatable2Compat.AnimationCallback animationCallback);

    public abstract void requestCancelAnimatorAfterCurrentCycle();

    public abstract void startAnimator();

    public abstract void unregisterAnimatorsCompleteCallback();

    public IndeterminateAnimatorDelegate(int segmentCount) {
        this.segmentPositions = new float[segmentCount * 2];
        this.segmentColors = new int[segmentCount];
    }

    public void registerDrawable(IndeterminateDrawable drawable) {
        this.drawable = drawable;
    }

    public float getFractionInRange(int playtime, int start, int duration) {
        return (playtime - start) / duration;
    }
}
