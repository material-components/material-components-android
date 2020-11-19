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
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

/** A delegate abstract class for drawing the graphics in different drawable classes. */
abstract class DrawingDelegate<S extends BaseProgressIndicatorSpec> {

  S spec;

  public DrawingDelegate(S spec) {
    this.spec = spec;
  }

  protected DrawableWithAnimatedVisibilityChange drawable;

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
   * @param canvas Canvas to draw.
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing.
   */
  abstract void adjustCanvas(
      @NonNull Canvas canvas, @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction);

  /**
   * Fills a part of the track with the designated indicator color. The filling part is defined with
   * two fractions normalized to [0, 1] representing the start and the end of the track.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param color The color used to draw the indicator.
   */
  abstract void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @ColorInt int color);

  /**
   * Fills the whole track with track color.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   */
  abstract void fillTrack(@NonNull Canvas canvas, @NonNull Paint paint);

  protected void registerDrawable(@NonNull DrawableWithAnimatedVisibilityChange drawable) {
    this.drawable = drawable;
  }

  void validateSpecAndAdjustCanvas(
      @NonNull Canvas canvas, @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction) {
    spec.validateSpec();
    adjustCanvas(canvas, trackThicknessFraction);
  }
}
