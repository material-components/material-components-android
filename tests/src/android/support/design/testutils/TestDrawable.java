/*
 * Copyright (C) 2016 The Android Open Source Project
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

package android.support.design.testutils;

import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;

/**
 * Custom drawable class that provides a reliable way for testing various tinting scenarios
 * across a range of platform versions. ColorDrawable doesn't support tinting on Kitkat and
 * below, and BitmapDrawable (PNG sources) appears to slightly alter green and blue channels
 * by a few units on some of the older platform versions (Gingerbread). Using GradientDrawable
 * allows doing reliable tests at the level of individual channels (alpha / red / green / blue)
 * for tinted and untinted icons in the testIconTinting method.
 */
public class TestDrawable extends GradientDrawable {
    private int mWidth;
    private int mHeight;

    public TestDrawable(@ColorInt int color, int width, int height) {
        super(Orientation.TOP_BOTTOM, new int[] { color, color });
        mWidth = width;
        mHeight = height;
    }

    @Override
    public int getIntrinsicWidth() {
        return mWidth;
    }

    @Override
    public int getIntrinsicHeight() {
        return mHeight;
    }
}
