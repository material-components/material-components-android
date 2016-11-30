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
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.design.R;
import android.view.View;

abstract class FloatingActionButtonImpl {

    static final int SHOW_HIDE_ANIM_DURATION = 200;

    static final int[] PRESSED_ENABLED_STATE_SET = {android.R.attr.state_pressed,
            android.R.attr.state_enabled};
    static final int[] FOCUSED_ENABLED_STATE_SET = {android.R.attr.state_focused,
            android.R.attr.state_enabled};
    static final int[] EMPTY_STATE_SET = new int[0];

    final View mView;
    final ShadowViewDelegate mShadowViewDelegate;

    FloatingActionButtonImpl(View view, ShadowViewDelegate shadowViewDelegate) {
        mView = view;
        mShadowViewDelegate = shadowViewDelegate;
    }

    abstract void setBackgroundDrawable(Drawable originalBackground, ColorStateList backgroundTint,
            PorterDuff.Mode backgroundTintMode, int rippleColor, int borderWidth);

    abstract void setBackgroundTintList(ColorStateList tint);

    abstract void setBackgroundTintMode(PorterDuff.Mode tintMode);

    abstract void setRippleColor(int rippleColor);

    abstract void setElevation(float elevation);

    abstract void setPressedTranslationZ(float translationZ);

    abstract void onDrawableStateChanged(int[] state);

    abstract void jumpDrawableToCurrentState();

    abstract void hide();

    abstract void show();

    Drawable createBorderDrawable(int borderWidth, ColorStateList backgroundTint) {
        final Resources resources = mView.getResources();
        CircularBorderDrawable borderDrawable = newCircularDrawable();
        borderDrawable.setGradientColors(
                resources.getColor(R.color.design_fab_stroke_top_outer_color),
                resources.getColor(R.color.design_fab_stroke_top_inner_color),
                resources.getColor(R.color.design_fab_stroke_end_inner_color),
                resources.getColor(R.color.design_fab_stroke_end_outer_color));
        borderDrawable.setBorderWidth(borderWidth);
        borderDrawable.setTintColor(backgroundTint.getDefaultColor());
        return borderDrawable;
    }

    CircularBorderDrawable newCircularDrawable() {
        return new CircularBorderDrawable();
    }
}
