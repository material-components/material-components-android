/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.progressindicator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

/** A delegate abstract class for drawing the graphics in different drawable classes. */
abstract class DrawingDelegate<S extends BaseProgressIndicatorSpec> {

  S spec;

  public DrawingDelegate(S spec) {
    this.spec = spec;
  }

  /**
   * Returns the preferred width, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  abstract int getPreferredWidth();

  /**
   * Returns the preferred height, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  abstract int getPreferredHeight();

  /**
   * Prepares the bound of the canvas for the actual drawing. Should be called before any drawing
   * (per frame).
   *
   * @param canvas Canvas to draw
   * @param bounds Bounds that the drawable is supposed to be drawn within
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing
   * @param isShowing Whether the drawable is currently animating to show
   * @param isHiding Whether the drawable is currently animating to hide
   */
  abstract void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull Rect bounds,
      @FloatRange(from = -1.0, to = 1.0) float trackThicknessFraction,
      boolean isShowing,
      boolean isHiding);

  /**
   * Fills a part of the track as an active indicator.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param activeIndicator The ActiveIndicator object of the current active indicator being drawn.
   * @param drawableAlpha The alpha [0, 255] from the caller drawable.
   */
  abstract void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull ActiveIndicator activeIndicator,
      @IntRange(from = 0, to = 255) int drawableAlpha);

  /**
   * Fills the whole track with track color.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param drawableAlpha The alpha [0, 255] from the caller drawable.
   */
  abstract void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @IntRange(from = 0, to = 255) int drawableAlpha);

  void validateSpecAndAdjustCanvas(
      @NonNull Canvas canvas,
      @NonNull Rect bounds,
      @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction,
      boolean isShowing,
      boolean isHiding) {
    spec.validateSpec();
    adjustCanvas(canvas, bounds, trackThicknessFraction, isShowing, isHiding);
  }

  protected static class ActiveIndicator {
    // The fraction [0, 1] of the start position on the full track.
    @FloatRange(from = 0.0, to = 1.0)
    float startFraction;

    // The fraction [0, 1] of the end position on the full track.
    @FloatRange(from = 0.0, to = 1.0)
    float endFraction;

    // The color of the indicator without applying the drawable's alpha.
    @ColorInt int color;
  }
}
