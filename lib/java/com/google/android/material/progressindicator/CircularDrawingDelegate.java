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

/** A delegate class to help draw the graphics for {@link ProgressIndicator} in circular types. */
final class CircularDrawingDelegate implements DrawingDelegate {

  private RectF arcBound = new RectF();
  // This is a factor effecting the positive direction to draw the arc. -1 if inverse; +1 otherwise.
  private int arcInverseFactor = 1;

  @Override
  public int getPreferredWidth(@NonNull ProgressIndicatorSpec spec) {
    return getSize(spec);
  }

  @Override
  public int getPreferredHeight(@NonNull ProgressIndicatorSpec spec) {
    return getSize(spec);
  }

  private static int getSize(@NonNull ProgressIndicatorSpec spec) {
    return spec.circularRadius * 2 + spec.indicatorWidth + spec.circularInset * 2;
  }

  /**
   * Adjusts the canvas for drawing circular progress indicator. It rotates the canvas -90 degrees
   * to keep the 0 at the top. The canvas is clipped to a square with the size just includes the
   * inset. It will also pre-calculate the bound for drawing the arc based on the indicate radius
   * and current indicator width.
   *
   * @param canvas Canvas to draw.
   * @param spec The spec of the component currently being served.
   * @param widthFraction A fraction representing how wide the arc stroke should be.
   */
  @Override
  public void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull ProgressIndicatorSpec spec,
      @FloatRange(from = 0.0, to = 1.0) float widthFraction) {
    int outerRadiusWithInset = spec.circularRadius + spec.indicatorWidth / 2 + spec.circularInset;
    canvas.translate(outerRadiusWithInset, outerRadiusWithInset);
    // Rotates canvas so that arc starts at top.
    canvas.rotate(-90f);

    // Clip all drawing to the designated area, so it doesn't draw outside of its bounds (which can
    // happen in certain configuration of clipToPadding and clipChildren)
    canvas.clipRect(
        -outerRadiusWithInset, -outerRadiusWithInset, outerRadiusWithInset, outerRadiusWithInset);

    // Adjusts the bounds of the arc.
    float adjustedRadius = spec.circularRadius;
    if (spec.growMode == ProgressIndicator.GROW_MODE_INCOMING) {
      // Increases the radius by half of the full width, then reduces it half way of the displayed
      // width to match the outer edges of the displayed indicator and the full indicator.
      adjustedRadius += (1 - widthFraction) * spec.indicatorWidth / 2;
    } else if (spec.growMode == ProgressIndicator.GROW_MODE_OUTGOING) {
      // Decreases the radius by half of the full width, then raises it half way of the displayed
      // width to match the inner edges of the displayed indicator and the full indicator.
      adjustedRadius -= (1 - widthFraction) * spec.indicatorWidth / 2;
    }

    // These are set for the drawing the indicator and track in fillTrackWithColor().
    arcBound = new RectF(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
    arcInverseFactor = spec.inverse ? -1 : 1;
  }

  /**
   * Fills a part of the track with input color. The filling part is defined with two fractions
   * normalized to [0, 1] representing the start degree and end degree from 0 deg (top) in clockwise
   * direction (counter-clockwise if inverse). If start fraction is larger than the end fraction, it
   * will draw the arc across 0 deg.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param color The filled color.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param trackWidth The width of the track in px.
   * @param cornerRadius The radius of corners in px, if rounded corners are applied.
   */
  @Override
  public void fillTrackWithColor(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      float trackWidth,
      float cornerRadius) {
    // No need to draw if startFraction and endFraction are same.
    if (startFraction == endFraction) {
      return;
    }

    // Initializes Paint object.
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Cap.BUTT);
    paint.setAntiAlias(true);
    paint.setColor(color);
    paint.setStrokeWidth(trackWidth);

    // Draws the stroke arc without rounded corners.
    float startDegree = startFraction * 360 * arcInverseFactor;
    float arcDegree =
        endFraction >= startFraction
            ? (endFraction - startFraction) * 360 * arcInverseFactor
            : (1 + endFraction - startFraction) * 360 * arcInverseFactor;
    canvas.drawArc(arcBound, startDegree, arcDegree, false, paint);

    // Draws rounded corners if needed.
    if (cornerRadius > 0 && Math.abs(arcDegree) < 360) {
      paint.setStyle(Style.FILL);
      RectF cornerPatternRectBound =
          new RectF(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius);
      drawRoundedEnd(
          canvas, paint, trackWidth, cornerRadius, startDegree, true, cornerPatternRectBound);
      drawRoundedEnd(
          canvas,
          paint,
          trackWidth,
          cornerRadius,
          startDegree + arcDegree,
          false,
          cornerPatternRectBound);
    }
  }

  private void drawRoundedEnd(
      Canvas canvas,
      Paint paint,
      float trackWidth,
      float cornerRadius,
      float positionInDeg,
      boolean isStartPosition,
      RectF cornerPatternRectBound) {
    float startOrEndFactor = isStartPosition ? -1 : 1;
    canvas.save();
    canvas.rotate(positionInDeg);
    canvas.drawRect(
        arcBound.right - trackWidth / 2 + cornerRadius,
        Math.min(0, startOrEndFactor * cornerRadius * arcInverseFactor),
        arcBound.right + trackWidth / 2 - cornerRadius,
        Math.max(0, startOrEndFactor * cornerRadius * arcInverseFactor),
        paint);
    canvas.translate(arcBound.right - trackWidth / 2 + cornerRadius, 0);
    canvas.drawArc(
        cornerPatternRectBound, 180, -startOrEndFactor * 90 * arcInverseFactor, true, paint);
    canvas.translate(trackWidth - 2 * cornerRadius, 0);
    canvas.drawArc(
        cornerPatternRectBound, 0, startOrEndFactor * 90 * arcInverseFactor, true, paint);
    canvas.restore();
  }
}
