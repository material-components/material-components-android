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

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;

import junit.framework.Assert;

public class TestUtils {
    /**
     * Checks whether all the pixels in the specified drawable are of the same specified color.
     *
     * In case there is a color mismatch, the behavior of this method depends on the
     * <code>throwExceptionIfFails</code> parameter. If it is <code>true</code>, this method will
     * throw an <code>Exception</code> describing the mismatch. Otherwise this method will call
     * <code>Assert.fail</code> with detailed description of the mismatch.
     */
    public static void assertAllPixelsOfColor(String failMessagePrefix, @NonNull Drawable drawable,
            int drawableWidth, int drawableHeight, boolean callSetBounds, @ColorInt int color,
            int allowedComponentVariance, boolean throwExceptionIfFails) {
        assertAllPixelsOfColor(failMessagePrefix, drawable, drawableWidth, drawableHeight,
                callSetBounds, color, null, allowedComponentVariance, throwExceptionIfFails);
    }

    public static void assertAllPixelsOfColor(String failMessagePrefix, @NonNull Drawable drawable,
            int drawableWidth, int drawableHeight, boolean callSetBounds, @ColorInt int color,
            Rect checkArea, int allowedComponentVariance, boolean throwExceptionIfFails) {

        // Create a bitmap
        Bitmap bitmap = Bitmap.createBitmap(drawableWidth, drawableHeight, Bitmap.Config.ARGB_8888);
        // Create a canvas that wraps the bitmap
        Canvas canvas = new Canvas(bitmap);
        if (callSetBounds) {
            // Configure the drawable to have bounds that match the passed size
            drawable.setBounds(0, 0, drawableWidth, drawableHeight);
        }

        // And ask the drawable to draw itself to the canvas / bitmap
        drawable.draw(canvas);

        try {
            int[] rowPixels = new int[drawableWidth];

            final int firstRow = checkArea != null ? checkArea.top : 0;
            final int lastRow = checkArea != null ? checkArea.bottom : drawableHeight - 1;
            final int firstCol = checkArea != null ? checkArea.left : 0;
            final int lastCol = checkArea != null ? checkArea.right : drawableWidth - 1;

            final int expectedAlpha = Color.alpha(color);
            final int expectedRed = Color.red(color);
            final int expectedGreen = Color.green(color);
            final int expectedBlue = Color.blue(color);

            for (int row = firstRow; row <= lastRow; row++) {
                bitmap.getPixels(rowPixels, 0, drawableWidth, 0, row, drawableWidth, 1);

                for (int column = firstCol; column <= lastCol; column++) {
                    int sourceAlpha = Color.alpha(rowPixels[column]);
                    int sourceRed = Color.red(rowPixels[column]);
                    int sourceGreen = Color.green(rowPixels[column]);
                    int sourceBlue = Color.blue(rowPixels[column]);

                    int varianceAlpha = Math.abs(sourceAlpha - expectedAlpha);
                    int varianceRed = Math.abs(sourceRed - expectedRed);
                    int varianceGreen = Math.abs(sourceGreen - expectedGreen);
                    int varianceBlue = Math.abs(sourceBlue - expectedBlue);

                    boolean isColorMatch = (varianceAlpha <= allowedComponentVariance)
                            && (varianceRed <= allowedComponentVariance)
                            && (varianceGreen <= allowedComponentVariance)
                            && (varianceBlue <= allowedComponentVariance);

                    if (!isColorMatch) {
                        String mismatchDescription = failMessagePrefix
                                + ": expected all drawable colors to be ["
                                + expectedAlpha + "," + expectedRed + ","
                                + expectedGreen + "," + expectedBlue
                                + "] but at position (" + row + "," + column + ") found ["
                                + sourceAlpha + "," + sourceRed + ","
                                + sourceGreen + "," + sourceBlue + "]";
                        if (throwExceptionIfFails) {
                            throw new RuntimeException(mismatchDescription);
                        } else {
                            Assert.fail(mismatchDescription);
                        }
                    }
                }
            }
        } finally {
            bitmap.recycle();
        }
    }

    public static int getThemeAttrColor(Context context, final int attr) {
        TypedArray a = null;
        try {
            a = context.obtainStyledAttributes(new int[]{attr});
            return a.getColor(0, 0);
        } finally {
            if (a != null) {
                a.recycle();
            }
        }
    }
}