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

package com.google.android.material.testutils;

import static android.content.res.Configuration.ORIENTATION_LANDSCAPE;
import static android.content.res.Configuration.ORIENTATION_PORTRAIT;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.test.platform.app.InstrumentationRegistry;
import junit.framework.Assert;

public class TestUtils {
  /**
   * Checks whether all the pixels in the specified drawable are of the same specified color.
   *
   * <p>In case there is a color mismatch, the behavior of this method depends on the <code>
   * throwExceptionIfFails</code> parameter. If it is <code>true</code>, this method will throw an
   * <code>Exception</code> describing the mismatch. Otherwise this method will call <code>
   * Assert.fail</code> with detailed description of the mismatch.
   */
  public static void assertAllPixelsOfColor(
      String failMessagePrefix,
      @NonNull Drawable drawable,
      int drawableWidth,
      int drawableHeight,
      boolean callSetBounds,
      @ColorInt int color,
      int allowedComponentVariance,
      boolean throwExceptionIfFails) {
    assertAllPixelsOfColor(
        failMessagePrefix,
        drawable,
        drawableWidth,
        drawableHeight,
        callSetBounds,
        color,
        null,
        allowedComponentVariance,
        throwExceptionIfFails);
  }

  public static void assertAllPixelsOfColor(
      String failMessagePrefix,
      @NonNull Drawable drawable,
      int drawableWidth,
      int drawableHeight,
      boolean callSetBounds,
      @ColorInt int color,
      Rect checkArea,
      int allowedComponentVariance,
      boolean throwExceptionIfFails) {

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

          boolean isColorMatch =
              (varianceAlpha <= allowedComponentVariance)
                  && (varianceRed <= allowedComponentVariance)
                  && (varianceGreen <= allowedComponentVariance)
                  && (varianceBlue <= allowedComponentVariance);

          if (!isColorMatch) {
            String mismatchDescription =
                failMessagePrefix
                    + ": expected all drawable colors to be ["
                    + expectedAlpha
                    + ","
                    + expectedRed
                    + ","
                    + expectedGreen
                    + ","
                    + expectedBlue
                    + "] but at position ("
                    + row
                    + ","
                    + column
                    + ") found ["
                    + sourceAlpha
                    + ","
                    + sourceRed
                    + ","
                    + sourceGreen
                    + ","
                    + sourceBlue
                    + "]";
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
      a = context.obtainStyledAttributes(new int[] {attr});
      return a.getColor(0, 0);
    } finally {
      if (a != null) {
        a.recycle();
      }
    }
  }

  /** Returns the current screen orientation. */
  public static int getScreenOrientation(Activity activity) {
    return activity.getResources().getConfiguration().orientation;
  }

  /**
   * Sets the screen orientation.
   *
   * <p>Tests that change the screen orientation should also use {@link
   * #getScreenOrientation(Activity)} and {@link #resetScreenOrientation(Activity, int)} to ensure
   * that it leaves the device in a known good state.
   */
  public static void setScreenOrientation(final Activity activity, final int orientation) {
    activity.setRequestedOrientation(
        orientation == ORIENTATION_PORTRAIT
            ? ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
            : ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }

  /**
   * Toggles the screen orientation between landscape and portrait.
   *
   * <p>Tests that change the screen orientation should also use {@link
   * #getScreenOrientation(Activity)} and {@link #resetScreenOrientation(Activity, int)} to ensure
   * that it leaves the device in a known good state.
   */
  public static void switchScreenOrientation(Activity activity) {
    if (getScreenOrientation(activity) == ORIENTATION_LANDSCAPE) {
      setScreenOrientation(activity, ORIENTATION_PORTRAIT);
    } else {
      setScreenOrientation(activity, ORIENTATION_LANDSCAPE);
    }
  }

  /**
   * Resets the device orientation and waits for it to settle to the old orientation. Tests should
   * call {@link #getScreenOrientation(Activity)} at the beginning of the test to save the old
   * orientation.
   */
  public static void resetScreenOrientation(final Activity activity, final int oldOrientation) {
    activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();
  }
}
