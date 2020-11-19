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
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;

/** A delegate class to help draw the graphics for {@link CircularProgressIndicator}. */
final class CircularDrawingDelegate extends DrawingDelegate<CircularProgressIndicatorSpec> {

  // This is a factor effecting the positive direction to draw the arc. +1 for clockwise; -1 for
  // counter-clockwise.
  private int arcDirectionFactor = 1;
  private float displayedTrackThickness;
  private float displayedCornerRadius;
  private float adjustedRadius;

  /** Instantiates CircularDrawingDelegate with the current spec. */
  public CircularDrawingDelegate(@NonNull CircularProgressIndicatorSpec spec) {
    super(spec);
  }

  @Override
  public int getPreferredWidth() {
    return getSize();
  }

  @Override
  public int getPreferredHeight() {
    return getSize();
  }

  /**
   * Adjusts the canvas for drawing circular progress indicator. It rotates the canvas -90 degrees
   * to keep the 0 at the top. The canvas is clipped to a square with the size just includes the
   * inset. It will also pre-calculate the bound for drawing the arc based on the spinner radius and
   * current track thickness.
   *
   * @param canvas Canvas to draw.
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing.
   */
  @Override
  public void adjustCanvas(
      @NonNull Canvas canvas, @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction) {
    float outerRadiusWithInset = spec.indicatorSize / 2f + spec.indicatorInset;
    canvas.translate(outerRadiusWithInset, outerRadiusWithInset);
    // Rotates canvas so that arc starts at top.
    canvas.rotate(-90f);

    // Clip all drawing to the designated area, so it doesn't draw outside of its bounds (which can
    // happen in certain configuration of clipToPadding and clipChildren)
    canvas.clipRect(
        -outerRadiusWithInset, -outerRadiusWithInset, outerRadiusWithInset, outerRadiusWithInset);

    // These are used when drawing the indicator and track.
    arcDirectionFactor =
        spec.indicatorDirection == CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE ? 1 : -1;
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius = spec.trackCornerRadius * trackThicknessFraction;
    adjustedRadius = (spec.indicatorSize - spec.trackThickness) / 2f;
    if ((drawable.isShowing()
            && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_INWARD)
        || (drawable.isHiding()
            && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_OUTWARD)) {
      // Increases the radius by half of the full thickness, then reduces it half way of the
      // displayed thickness to match the outer edges of the displayed indicator and the full
      // indicator.
      adjustedRadius += (1 - trackThicknessFraction) * spec.trackThickness / 2;
    } else if ((drawable.isShowing()
            && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_OUTWARD)
        || (drawable.isHiding()
            && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_INWARD)) {
      // Decreases the radius by half of the full thickness, then raises it half way of the
      // displayed thickness to match the inner edges of the displayed indicator and the full
      // indicator.
      adjustedRadius -= (1 - trackThicknessFraction) * spec.trackThickness / 2;
    }
  }

  /**
   * Fills a part of the track with the designated indicator color. The filling part is defined with
   * two fractions normalized to [0, 1] representing the start degree and the end degree from 0 deg
   * (top). If start fraction is larger than the end fraction, it will draw the arc across 0 deg.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param color The color used to draw the indicator.
   */
  @Override
  void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @ColorInt int color) {
    // No need to draw if startFraction and endFraction are same.
    if (startFraction == endFraction) {
      return;
    }

    // Sets up the paint.
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Cap.BUTT);
    paint.setAntiAlias(true);
    paint.setColor(color);
    paint.setStrokeWidth(displayedTrackThickness);

    // Calculates the start and end in degrees.
    float startDegree = startFraction * 360 * arcDirectionFactor;
    float arcDegree =
        endFraction >= startFraction
            ? (endFraction - startFraction) * 360 * arcDirectionFactor
            : (1 + endFraction - startFraction) * 360 * arcDirectionFactor;

    // Draws the indicator arc without rounded corners.
    RectF arcBound = new RectF(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
    canvas.drawArc(arcBound, startDegree, arcDegree, false, paint);

    // Draws rounded corners if needed.
    if (displayedCornerRadius > 0 && Math.abs(arcDegree) < 360) {
      paint.setStyle(Style.FILL);
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
          startDegree,
          true,
          cornerPatternRectBound);
      drawRoundedEnd(
          canvas,
          paint,
          displayedTrackThickness,
          displayedCornerRadius,
          startDegree + arcDegree,
          false,
          cornerPatternRectBound);
    }
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

    // Sets up the paint.
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Cap.BUTT);
    paint.setAntiAlias(true);
    paint.setColor(trackColor);
    paint.setStrokeWidth(displayedTrackThickness);

    RectF arcBound = new RectF(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
    canvas.drawArc(arcBound, 0, 360, false, paint);
  }

  private int getSize() {
    return spec.indicatorSize + spec.indicatorInset * 2;
  }

  private void drawRoundedEnd(
      Canvas canvas,
      Paint paint,
      float trackSize,
      float cornerRadius,
      float positionInDeg,
      boolean isStartPosition,
      RectF cornerPatternRectBound) {
    float startOrEndFactor = isStartPosition ? -1 : 1;
    canvas.save();
    canvas.rotate(positionInDeg);
    canvas.drawRect(
        adjustedRadius - trackSize / 2 + cornerRadius,
        Math.min(0, startOrEndFactor * cornerRadius * arcDirectionFactor),
        adjustedRadius + trackSize / 2 - cornerRadius,
        Math.max(0, startOrEndFactor * cornerRadius * arcDirectionFactor),
        paint);
    canvas.translate(adjustedRadius - trackSize / 2 + cornerRadius, 0);
    canvas.drawArc(
        cornerPatternRectBound, 180, -startOrEndFactor * 90 * arcDirectionFactor, true, paint);
    canvas.translate(trackSize - 2 * cornerRadius, 0);
    canvas.drawArc(
        cornerPatternRectBound, 0, startOrEndFactor * 90 * arcDirectionFactor, true, paint);
    canvas.restore();
  }
}
