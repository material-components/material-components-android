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

import static java.lang.Math.max;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;

/** A delegate class to help draw the graphics for {@link LinearProgressIndicator}. */
final class LinearDrawingDelegate extends DrawingDelegate<LinearProgressIndicatorSpec> {

  // The length (horizontal) of the track in px.
  private float trackLength = 300f;
  private float displayedTrackThickness;
  private float displayedCornerRadius;

  /** Instantiates LinearDrawingDelegate with the current spec. */
  public LinearDrawingDelegate(@NonNull LinearProgressIndicatorSpec spec) {
    super(spec);
  }

  @Override
  public int getPreferredWidth() {
    return -1;
  }

  @Override
  public int getPreferredHeight() {
    return spec.trackThickness;
  }

  /**
   * Adjusts the canvas for linear progress indicator drawables. It flips the canvas horizontally if
   * it's inverted. It flips the canvas vertically if outgoing grow mode is applied.
   *
   * @param canvas Canvas to draw.
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing.
   */
  @Override
  public void adjustCanvas(
      @NonNull Canvas canvas, @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction) {
    // Gets clip bounds from canvas.
    Rect clipBounds = canvas.getClipBounds();
    trackLength = clipBounds.width();
    float trackSize = spec.trackThickness;

    // Positions canvas to center of the clip bounds.
    canvas.translate(
        clipBounds.width() / 2f,
        clipBounds.height() / 2f + max(0f, (clipBounds.height() - spec.trackThickness) / 2f));

    // Flips canvas horizontally if need to draw right to left.
    if (spec.drawHorizontallyInverse) {
      canvas.scale(-1f, 1f);
    }
    // Flips canvas vertically if need to anchor to the bottom edge.
    if ((drawable.isShowing() && spec.showAnimationBehavior == LinearProgressIndicator.SHOW_OUTWARD)
        || (drawable.isHiding()
            && spec.hideAnimationBehavior == LinearProgressIndicator.HIDE_INWARD)) {
      canvas.scale(1f, -1f);
    }
    // Offsets canvas vertically while showing or hiding.
    if (drawable.isShowing() || drawable.isHiding()) {
      canvas.translate(0f, spec.trackThickness * (trackThicknessFraction - 1) / 2f);
    }

    // Clips all drawing to the track area, so it doesn't draw outside of its bounds (which can
    // happen in certain configurations of clipToPadding and clipChildren)
    canvas.clipRect(-trackLength / 2, -trackSize / 2, trackLength / 2, trackSize / 2);

    // These are set for the drawing the indicator and track.
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius = spec.trackCornerRadius * trackThicknessFraction;
  }

  /**
   * Fills a part of the track with the designated indicator color. The filling part is defined with
   * two fractions normalized to [0, 1] representing the start position and the end position on the
   * track. The rest of the track will be filled with the track color.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param color The color used to draw the indicator.
   */
  @Override
  public void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @ColorInt int color) {
    // No need to draw if startFraction and endFraction are same.
    if (startFraction == endFraction) {
      return;
    }

    // Horizontal position of the start adjusted based on the rounded corner radius.
    float adjustedStartX =
        -trackLength / 2
            + displayedCornerRadius
            + startFraction * (trackLength - 2 * displayedCornerRadius);
    // Horizontal position of the end adjusted based on the rounded corner radius.
    float adjustedEndX =
        -trackLength / 2
            + displayedCornerRadius
            + endFraction * (trackLength - 2 * displayedCornerRadius);

    // Sets up the paint.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(color);

    // Draws the rectangle as the indicator and the rounded corners.
    canvas.drawRect(
        adjustedStartX,
        -displayedTrackThickness / 2,
        adjustedEndX,
        displayedTrackThickness / 2,
        paint);
    RectF cornerPatternRectBound =
        new RectF(
            -displayedCornerRadius,
            -displayedCornerRadius,
            displayedCornerRadius,
            displayedCornerRadius);
    drawRoundedEnd(
        canvas,
        paint,
        displayedTrackThickness,
        displayedCornerRadius,
        adjustedStartX,
        true,
        cornerPatternRectBound);
    drawRoundedEnd(
        canvas,
        paint,
        displayedTrackThickness,
        displayedCornerRadius,
        adjustedEndX,
        false,
        cornerPatternRectBound);
  }

  /**
   * Fills the whole track with track color.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   */
  @Override
  void fillTrack(@NonNull Canvas canvas, @NonNull Paint paint) {
    int trackColor = MaterialColors.compositeARGBWithAlpha(spec.trackColor, drawable.getAlpha());
    float adjustedStartX = -trackLength / 2 + displayedCornerRadius;
    float adjustedEndX = -adjustedStartX;

    // Sets up the paint.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(trackColor);

    canvas.drawRect(
        adjustedStartX,
        -displayedTrackThickness / 2,
        adjustedEndX,
        displayedTrackThickness / 2,
        paint);
    RectF cornerPatternRectBound =
        new RectF(
            -displayedCornerRadius,
            -displayedCornerRadius,
            displayedCornerRadius,
            displayedCornerRadius);
    drawRoundedEnd(
        canvas,
        paint,
        displayedTrackThickness,
        displayedCornerRadius,
        adjustedStartX,
        true,
        cornerPatternRectBound);
    drawRoundedEnd(
        canvas,
        paint,
        displayedTrackThickness,
        displayedCornerRadius,
        adjustedEndX,
        false,
        cornerPatternRectBound);
  }

  // The rounded corners are drawn in steps, since drawRoundRect() is only available in Api 21+.
  private static void drawRoundedEnd(
      Canvas canvas,
      Paint paint,
      float trackSize,
      float cornerRadius,
      float x,
      boolean isStartPosition,
      RectF cornerPatternRectBound) {
    canvas.save();
    canvas.translate(x, 0);
    if (!isStartPosition) {
      canvas.rotate(180);
    }
    // Draws the tiny rectangle between the two corners.
    canvas.drawRect(
        -cornerRadius, -trackSize / 2 + cornerRadius, 0, trackSize / 2 - cornerRadius, paint);
    // Draws the upper corner.
    canvas.save();
    canvas.translate(0, -trackSize / 2 + cornerRadius);
    canvas.drawArc(cornerPatternRectBound, 180, 90, true, paint);
    canvas.restore();
    // Draws the lower corner.
    canvas.translate(0, trackSize / 2 - cornerRadius);
    canvas.drawArc(cornerPatternRectBound, 180, -90, true, paint);
    canvas.restore();
  }
}
