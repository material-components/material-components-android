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

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.StateListAnimator;
import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class FloatingActionButtonLollipop extends FloatingActionButtonImpl {

    private Drawable mShapeDrawable;
    private RippleDrawable mRippleDrawable;

    private final Interpolator mInterpolator;

    FloatingActionButtonLollipop(View view, ShadowViewDelegate shadowViewDelegate) {
        super(view, shadowViewDelegate);

        mInterpolator = AnimationUtils.loadInterpolator(
                mView.getContext(), android.R.interpolator.fast_out_slow_in);
    }

    @Override
    void setBackgroundDrawable(Drawable originalBackground, ColorStateList backgroundTint,
            PorterDuff.Mode backgroundTintMode, int rippleColor) {
        mShapeDrawable = DrawableCompat.wrap(originalBackground);

        DrawableCompat.setTintList(mShapeDrawable, backgroundTint);
        if (backgroundTintMode != null) {
            DrawableCompat.setTintMode(mShapeDrawable, backgroundTintMode);
        }

        mRippleDrawable = new RippleDrawable(ColorStateList.valueOf(rippleColor),
                mShapeDrawable, null);

        mShadowViewDelegate.setBackgroundDrawable(mRippleDrawable);
        mShadowViewDelegate.setShadowPadding(0, 0, 0, 0);
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
    public void setElevation(float elevation) {
        ViewCompat.setElevation(mView, elevation);
    }

    @Override
    void setPressedTranslationZ(float translationZ) {
        StateListAnimator stateListAnimator = new StateListAnimator();

        // Animate translationZ to our value when pressed or focused
        stateListAnimator.addState(PRESSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        stateListAnimator.addState(FOCUSED_ENABLED_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", translationZ)));
        // Animate translationZ to 0 otherwise
        stateListAnimator.addState(EMPTY_STATE_SET,
                setupAnimator(ObjectAnimator.ofFloat(mView, "translationZ", 0f)));

        mView.setStateListAnimator(stateListAnimator);
    }

    @Override
    void onDrawableStateChanged(int[] state) {
        // no-op
    }

    @Override
    void jumpDrawableToCurrentState() {
        // no-op
    }

    private Animator setupAnimator(Animator animator) {
        animator.setInterpolator(mInterpolator);
        return animator;
    }
}