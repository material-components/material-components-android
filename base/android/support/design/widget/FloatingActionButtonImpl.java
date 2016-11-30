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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.Nullable;
import android.support.design.R;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Interpolator;

abstract class FloatingActionButtonImpl {

    static final Interpolator ANIM_INTERPOLATOR = AnimationUtils.FAST_OUT_LINEAR_IN_INTERPOLATOR;
    static final long PRESSED_ANIM_DURATION = 100;
    static final long PRESSED_ANIM_DELAY = 100;

    static final int ANIM_STATE_NONE = 0;
    static final int ANIM_STATE_HIDING = 1;
    static final int ANIM_STATE_SHOWING = 2;

    int mAnimState = ANIM_STATE_NONE;

    Drawable mShapeDrawable;
    Drawable mRippleDrawable;
    CircularBorderDrawable mBorderDrawable;
    Drawable mContentBackground;

    float mElevation;
    float mPressedTranslationZ;

    interface InternalVisibilityChangedListener {
        public void onShown();
        public void onHidden();
    }

    static final int SHOW_HIDE_ANIM_DURATION = 200;

    static final int[] PRESSED_ENABLED_STATE_SET = {android.R.attr.state_pressed,
            android.R.attr.state_enabled};
    static final int[] FOCUSED_ENABLED_STATE_SET = {android.R.attr.state_focused,
            android.R.attr.state_enabled};
    static final int[] ENABLED_STATE_SET = {android.R.attr.state_enabled};
    static final int[] EMPTY_STATE_SET = new int[0];

    final VisibilityAwareImageButton mView;
    final ShadowViewDelegate mShadowViewDelegate;
    final ValueAnimatorCompat.Creator mAnimatorCreator;

    private final Rect mTmpRect = new Rect();
    private ViewTreeObserver.OnPreDrawListener mPreDrawListener;

    FloatingActionButtonImpl(VisibilityAwareImageButton view,
            ShadowViewDelegate shadowViewDelegate, ValueAnimatorCompat.Creator animatorCreator) {
        mView = view;
        mShadowViewDelegate = shadowViewDelegate;
        mAnimatorCreator = animatorCreator;
    }

    abstract void setBackgroundDrawable(ColorStateList backgroundTint,
            PorterDuff.Mode backgroundTintMode, int rippleColor, int borderWidth);

    abstract void setBackgroundTintList(ColorStateList tint);

    abstract void setBackgroundTintMode(PorterDuff.Mode tintMode);

    abstract void setRippleColor(int rippleColor);

    final void setElevation(float elevation) {
        if (mElevation != elevation) {
            mElevation = elevation;
            onElevationsChanged(elevation, mPressedTranslationZ);
        }
    }

    abstract float getElevation();

    final void setPressedTranslationZ(float translationZ) {
        if (mPressedTranslationZ != translationZ) {
            mPressedTranslationZ = translationZ;
            onElevationsChanged(mElevation, translationZ);
        }
    }

    abstract void onElevationsChanged(float elevation, float pressedTranslationZ);

    abstract void onDrawableStateChanged(int[] state);

    abstract void jumpDrawableToCurrentState();

    abstract void hide(@Nullable InternalVisibilityChangedListener listener, boolean fromUser);

    abstract void show(@Nullable InternalVisibilityChangedListener listener, boolean fromUser);

    final Drawable getContentBackground() {
        return mContentBackground;
    }

    abstract void onCompatShadowChanged();

    final void updatePadding() {
        Rect rect = mTmpRect;
        getPadding(rect);
        onPaddingUpdated(rect);
        mShadowViewDelegate.setShadowPadding(rect.left, rect.top, rect.right, rect.bottom);
    }

    abstract void getPadding(Rect rect);

    void onPaddingUpdated(Rect padding) {}

    void onAttachedToWindow() {
        if (requirePreDrawListener()) {
            ensurePreDrawListener();
            mView.getViewTreeObserver().addOnPreDrawListener(mPreDrawListener);
        }
    }

    void onDetachedFromWindow() {
        if (mPreDrawListener != null) {
            mView.getViewTreeObserver().removeOnPreDrawListener(mPreDrawListener);
            mPreDrawListener = null;
        }
    }

    boolean requirePreDrawListener() {
        return false;
    }

    CircularBorderDrawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
        final Resources resources = mView.getResources();
        CircularBorderDrawable borderDrawable = newCircularDrawable();
        borderDrawable.setGradientColors(
                resources.getColor(R.color.design_fab_stroke_top_outer_color),
                resources.getColor(R.color.design_fab_stroke_top_inner_color),
                resources.getColor(R.color.design_fab_stroke_end_inner_color),
                resources.getColor(R.color.design_fab_stroke_end_outer_color));
        borderDrawable.setBorderWidth(borderWidth);
        borderDrawable.setBorderTint(backgroundTint);
        return borderDrawable;
    }

    CircularBorderDrawable newCircularDrawable() {
        return new CircularBorderDrawable();
    }

    void onPreDraw() {
    }

    private void ensurePreDrawListener() {
        if (mPreDrawListener == null) {
            mPreDrawListener = new ViewTreeObserver.OnPreDrawListener() {
                @Override
                public boolean onPreDraw() {
                    FloatingActionButtonImpl.this.onPreDraw();
                    return true;
                }
            };
        }
    }

    GradientDrawable createShapeDrawable() {
        GradientDrawable d = new GradientDrawable();
        d.setShape(GradientDrawable.OVAL);
        d.setColor(Color.WHITE);
        return d;
    }

    boolean isOrWillBeShown() {
        if (mView.getVisibility() != View.VISIBLE) {
            // If we not currently visible, return true if we're animating to be shown
            return mAnimState == ANIM_STATE_SHOWING;
        } else {
            // Otherwise if we're visible, return true if we're not animating to be hidden
            return mAnimState != ANIM_STATE_HIDING;
        }
    }

    boolean isOrWillBeHidden() {
        if (mView.getVisibility() == View.VISIBLE) {
            // If we currently visible, return true if we're animating to be hidden
            return mAnimState == ANIM_STATE_HIDING;
        } else {
            // Otherwise if we're not visible, return true if we're not animating to be shown
            return mAnimState != ANIM_STATE_SHOWING;
        }
    }
}
