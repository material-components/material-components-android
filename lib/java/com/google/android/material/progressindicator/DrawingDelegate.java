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

/**
 * A delegate interface for drawing the graphics in different drawable classes used in {@link
 * ProgressIndicator}.
 */
interface DrawingDelegate {
  /**
   * Returns the preferred width, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   *
   * @param spec The spec of the component where to draw.
   */
  int getPreferredWidth(@NonNull ProgressIndicatorSpec spec);

  /**
   * Returns the preferred height, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   *
   * @param spec The spec of the component where to draw.
   */
  int getPreferredHeight(@NonNull ProgressIndicatorSpec spec);

  /**
   * Prepares the bound of the canvas for the actual drawing. Should be called before any drawing.
   *
   * @param canvas Canvas to draw.
   * @param spec The spec of the component currently being served.
   * @param indicatorSizeFraction A fraction representing how much portion of the indicator size
   * should be used in the drawing.
   */
  void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull ProgressIndicatorSpec spec,
      @FloatRange(from = 0.0, to = 1.0) float indicatorSizeFraction);

  /**
   * Fills a part of the track with input color. The filling part is defined with two fractions
   * normalized to [0, 1] representing the start and the end of the track.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param color The filled color.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param trackSize The size of the track in px.
   * @param cornerRadius The radius of corners in px, if rounded corners are applied.
   */
  void fillTrackWithColor(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      float trackSize,
      float cornerRadius);
}
