/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.widget;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.Transformation;

class FloatingActionButtonEclairMr1 extends FloatingActionButtonImpl {

    private static final Interpolator INTERPOLATOR = new FastOutSlowInInterpolator();

    private Drawable mShapeDrawable;
    private Drawable mRippleDrawable;

    private float mElevation;
    private float mPressedTranslationZ;
    private int mAnimationDuration;

    private StateListAnimator mStateListAnimator;

    ShadowDrawableWrapper mShadowDrawable;

    FloatingActionButtonEclairMr1(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);

        mAnimationDuration = view.getResources().getInteger(android.R.integer.config_shortAnimTime);

        mStateListAnimator = new StateListAnimator();
        mStateListAnimator.setTarget(view);

        // Elevate with translationZ when pressed or focused
        mStateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        mStateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimation(new ElevateToTranslationZAnimation()));
        // Reset back to elevation by default
        mStateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimation(new ResetElevationAnimation()));
    }

    @Override
    void setBackgroundDrawable(Drawable originalBackground, ColorStateList backgroundTint,
            PorterDuff.Mode backgroundTintMode, int rippleColor) {
        // First we need to tint the original background with the tint
        mShapeDrawable = DrawableCompat.wrap(originalBackground);
        DrawableCompat.setTintList(mShapeDrawable, backgroundTint);
        if (backgroundTintMode != null) {
            DrawableCompat.setTintMode(mShapeDrawable, backgroundTintMode);
        }

        // Now we created a mask Drawable which will be used for touch feedback.
        // As we don't know the actual outline of mShapeDrawable, we'll just guess that it's a
        // circle
        GradientDrawable touchFeedbackShape = new GradientDrawable();
        touchFeedbackShape.setShape(GradientDrawable.OVAL);
        touchFeedbackShape.setColor(Color.WHITE);
        touchFeedbackShape.setCornerRadius(mShadowViewDelegate.getRadius());

        // We'll now wrap that touch feedback mask drawable with a ColorStateList
        mRippleDrawable = DrawableCompat.wrap(touchFeedbackShape);
        DrawableCompat.setTintList(mRippleDrawable, createColorStateList(rippleColor));
        DrawableCompat.setTintMode(mRippleDrawable, PorterDuff.Mode.MULTIPLY);

        mShadowDrawable = new ShadowDrawableWrapper(
                mView.getResources(),
                new LayerDrawable(new Drawable[] {mShapeDrawable, mRippleDrawable}),
                mShadowViewDelegate.getRadius(),
                mElevation,
                mElevation + mPressedTranslationZ);
        mShadowDrawable.setAddPaddingForCorners(false);

        mShadowViewDelegate.setBackgroundDrawable(mShadowDrawable);

        updatePadding();
    }

    @Override
    void setBackgroundTintList(ColorStateList tint) {
        DrawableCompat.setTintList(mShapeDrawable, tint);
    }

    @Override
    void setBackgroundTintMode(PorterDuff.Mode tintMode) {
        DrawableCompat.setTintMode(mShapeDrawable, tintMode);
    }

    @Override
    void setRippleColor(int rippleColor) {
        DrawableCompat.setTint(mRippleDrawable, rippleColor);
    }

    @Override
    void setElevation(float elevation) {
        if (mElevation != elevation && mShadowDrawable != null) {
            mShadowDrawable.setShadowSize(elevation, elevation + mPressedTranslationZ);
            mElevation = elevation;
            updatePadding();
        }
    }

    @Override
    void setPressedTranslationZ(float translationZ) {
        if (mPressedTranslationZ != translationZ && mShadowDrawable != null) {
            mPressedTranslationZ = translationZ;
            mShadowDrawable.setMaxShadowSize(mElevation + translationZ);
            updatePadding();
        }
    }

    @Override
    void onDrawableStateChanged(int[] state) {
        mStateListAnimator.setState(state);
    }

    @Override
    void jumpDrawableToCurrentState() {
        mStateListAnimator.jumpToCurrentState();
    }

    private void updatePadding() {
        Rect rect = new Rect();
        mShadowDrawable.getPadding(rect);
        mShadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
    }

    private Animation setupAnimation(Animation animation) {
        animation.setInterpolator(INTERPOLATOR);
        animation.setDuration(mAnimationDuration);
        return animation;
    }

    private abstract class BaseShadowAnimation extends Animation {
        private float mShadowSizeStart;
        private float mShadowSizeDiff;

        @Override
        public void reset() {
            super.reset();

            mShadowSizeStart = mShadowDrawable.getShadowSize();
            mShadowSizeDiff = getTargetShadowSize() - mShadowSizeStart;
        }

        @Override
        protected void applyTransformation(float interpolatedTime, Transformation t) {
            mShadowDrawable.setShadowSize(mShadowSizeStart + (mShadowSizeDiff * interpolatedTime));
        }

        /**
         * @return the shadow size we want to animate to.
         */
        protected abstract float getTargetShadowSize();
    }

    private class ResetElevationAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation;
        }
    }

    private class ElevateToTranslationZAnimation extends BaseShadowAnimation {
        @Override
        protected float getTargetShadowSize() {
            return mElevation + mPressedTranslationZ;
        }
    }

    private static ColorStateList createColorStateList(int selectedColor) {
        final int[][] states = new int[3][];
        final int[] colors = new int[3];
        int i = 0;

        states[i] = FOCUSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        states[i] = PRESSED_ENABLED_STATE_SET;
        colors[i] = selectedColor;
        i++;

        // Default enabled state
        states[i] = new int[0];
        colors[i] = Color.TRANSPARENT;
        i++;

        return new ColorStateList(states, colors);
    }
}