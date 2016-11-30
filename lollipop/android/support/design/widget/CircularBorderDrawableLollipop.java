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
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;

/**
 * Lollipop version of {@link CircularBorderDrawable} which accepts tint calls.
 */
class CircularBorderDrawableLollipop extends CircularBorderDrawable {

    private ColorStateList mTint;
    private PorterDuff.Mode mTintMode = PorterDuff.Mode.SRC_IN;
    private PorterDuffColorFilter mTintFilter;

    @Override
    public void draw(Canvas canvas) {
        boolean clearColorFilter;
        if (mTintFilter != null && mPaint.getColorFilter() == null) {
            mPaint.setColorFilter(mTintFilter);
            clearColorFilter = true;
        } else {
            clearColorFilter = false;
        }

        super.draw(canvas);

        if (clearColorFilter) {
            mPaint.setColorFilter(null);
        }
    }

    @Override
    public void setTintList(ColorStateList tint) {
        mTint = tint;
        mTintFilter = updateTintFilter(tint, mTintMode);
        invalidateSelf();
    }

    @Override
    public void setTintMode(PorterDuff.Mode tintMode) {
        mTintMode = tintMode;
        mTintFilter = updateTintFilter(mTint, tintMode);
        invalidateSelf();
    }

    @Override
    public void getOutline(Outline outline) {
        copyBounds(mRect);
        outline.setOval(mRect);
    }

    /**
     * Ensures the tint filter is consistent with the current tint color and
     * mode.
     */
    private PorterDuffColorFilter updateTintFilter(ColorStateList tint, PorterDuff.Mode tintMode) {
        if (tint == null || tintMode == null) {
            return null;
        }

        final int color = tint.getColorForState(getState(), Color.TRANSPARENT);
        return new PorterDuffColorFilter(color, tintMode);
    }
}
