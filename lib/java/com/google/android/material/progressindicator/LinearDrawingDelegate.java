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

import static com.google.android.material.math.MathUtils.lerp;
import static com.google.android.material.progressindicator.BaseProgressIndicator.HIDE_ESCAPE;
import static com.google.android.material.progressindicator.BaseProgressIndicator.HIDE_INWARD;
import static com.google.android.material.progressindicator.BaseProgressIndicator.SHOW_OUTWARD;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;

/** A delegate class to help draw the graphics for {@link LinearProgressIndicator}. */
final class LinearDrawingDelegate extends DrawingDelegate<LinearProgressIndicatorSpec> {

  // The length (horizontal) of the track in px.
  private float trackLength = 300f;
  private float displayedTrackThickness;
  private float displayedCornerRadius;
  private final Path displayedTrackPath;

  // This will be used in the ESCAPE hide animation. The start and end fraction in track will be
  // scaled by this fraction with a pivot of 1.0f.
  @FloatRange(from = 0.0f, to = 1.0f)
  private float totalTrackLengthFraction;

  /** Instantiates LinearDrawingDelegate with the current spec. */
  LinearDrawingDelegate(@NonNull LinearProgressIndicatorSpec spec) {
    super(spec);

    displayedTrackPath = new Path();
  }

  @Override
  int getPreferredWidth() {
    return -1;
  }

  @Override
  int getPreferredHeight() {
    return spec.trackThickness;
  }

  /**
   * Adjusts the canvas for linear progress indicator drawables. It flips the canvas horizontally if
   * it's inverted. It flips the canvas vertically if outgoing grow mode is applied.
   *
   * @param canvas Canvas to draw.
   * @param bounds Bounds that the drawable is supposed to be drawn within
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing
   * @param isShowing Whether the drawable is currently animating to show
   * @param isHiding Whether the drawable is currently animating to hide
   */
  @Override
  void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull Rect bounds,
      @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction,
      boolean isShowing,
      boolean isHiding) {
    trackLength = bounds.width();

    // Positions canvas to center of the clip bounds.
    canvas.translate(
        bounds.left + bounds.width() / 2f,
        bounds.top + bounds.height() / 2f + max(0f, (bounds.height() - spec.trackThickness) / 2f));

    // Flips canvas horizontally if need to draw right to left.
    if (spec.drawHorizontallyInverse) {
      canvas.scale(-1f, 1f);
    }
    // Flips canvas vertically if need to anchor to the bottom edge.
    if ((isShowing && spec.showAnimationBehavior == SHOW_OUTWARD)
        || (isHiding && spec.hideAnimationBehavior == HIDE_INWARD)) {
      canvas.scale(1f, -1f);
    }
    // Offsets canvas vertically while showing or hiding.
    if (isShowing || (isHiding && spec.hideAnimationBehavior != HIDE_ESCAPE)) {
      canvas.translate(0f, spec.trackThickness * (trackThicknessFraction - 1) / 2f);
    }
    // Sets the total track length fraction if ESCAPE hide animation is used.
    if (isHiding && spec.hideAnimationBehavior == HIDE_ESCAPE) {
      totalTrackLengthFraction = trackThicknessFraction;
    } else {
      totalTrackLengthFraction = 1f;
    }

    // These are set for the drawing the indicator and track.
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius = spec.trackCornerRadius * trackThicknessFraction;

    // Clips all drawing to the track area, so it doesn't draw outside of its bounds (which can
    // happen in certain configurations of clipToPadding and clipChildren)
    float right = trackLength / 2;
    float left = right - trackLength * totalTrackLengthFraction;
    float bottom = displayedTrackThickness / 2;
    displayedTrackPath.rewind();
    displayedTrackPath.addRoundRect(
        new RectF(left, -bottom, right, bottom),
        displayedCornerRadius,
        displayedCornerRadius,
        Path.Direction.CCW);
    canvas.clipPath(displayedTrackPath);
  }

  @Override
  void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull ActiveIndicator activeIndicator,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    float startFraction = activeIndicator.startFraction;
    float endFraction = activeIndicator.endFraction;
    // No need to draw if startFraction and endFraction are same.
    if (startFraction == endFraction) {
      return;
    }
    int color = MaterialColors.compositeARGBWithAlpha(activeIndicator.color, drawableAlpha);
    // Scale start and end fraction if ESCAPE animation is used.
    startFraction = lerp(1 - totalTrackLengthFraction, 1f, startFraction);
    endFraction = lerp(1 - totalTrackLengthFraction, 1f, endFraction);

    float originX = -trackLength / 2;

    // Adjusts start/end X so the progress indicator will start from 0 when startFraction == 0.
    float startPx = startFraction * trackLength;
    float endPx = endFraction * trackLength;
    float gapSize = min(spec.indicatorTrackGapSize, startPx);

    float adjustedStartX = originX + startPx + gapSize;
    // TODO: workaround to maintain pixel-perfect compatibility with drawing logic
    //  not using indicatorTrackGapSize.
    //  See https://github.com/material-components/material-components-android/commit/0ce6ae4.
    if (spec.indicatorTrackGapSize == 0) {
      adjustedStartX -= displayedCornerRadius * 2;
    }
    float adjustedEndX = originX + endPx;

    // Sets up the paint.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(color);

    canvas.save();
    // Avoid the indicator being drawn out of the track.
    RectF indicatorBounds =
        new RectF(
            adjustedStartX,
            -displayedTrackThickness / 2,
            adjustedEndX,
            displayedTrackThickness / 2);
    canvas.drawRoundRect(indicatorBounds, displayedCornerRadius, displayedCornerRadius, paint);
    canvas.restore();
  }

  @Override
  void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    int trackColor = MaterialColors.compositeARGBWithAlpha(spec.trackColor, drawableAlpha);

    // Sets up the paint.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(trackColor);

    canvas.drawPath(displayedTrackPath, paint);
  }

  @Override
  void drawStopIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    int paintColor = MaterialColors.compositeARGBWithAlpha(color, drawableAlpha);
    if (spec.trackStopIndicatorSize > 0 && paintColor != Color.TRANSPARENT) {
      // Draws the stop indicator at the end of the track if needed.
      paint.setStyle(Style.FILL);
      paint.setAntiAlias(true);
      paint.setColor(paintColor);
      canvas.save();
      // Avoid the indicator being drawn out of the track.
      Rect trackBounds = canvas.getClipBounds();
      float offset = max(0, displayedTrackThickness - spec.trackStopIndicatorSize);
      RectF stopBounds =
          new RectF(
              trackBounds.right - displayedTrackThickness + offset / 2,
              -(displayedTrackThickness - offset) / 2,
              trackBounds.right - offset / 2,
              (displayedTrackThickness - offset) / 2);
      canvas.drawRoundRect(stopBounds, displayedCornerRadius, displayedCornerRadius, paint);
      canvas.restore();
    }
  }
}
