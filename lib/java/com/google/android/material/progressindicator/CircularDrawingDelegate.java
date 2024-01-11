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
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
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

  // This will be used in the ESCAPE hide animation. The start and end fraction in track will be
  // scaled by this fraction with a pivot of 1.0f.
  @FloatRange(from = 0.0f, to = 1.0f)
  private float totalTrackLengthFraction;

  /** Instantiates CircularDrawingDelegate with the current spec. */
  CircularDrawingDelegate(@NonNull CircularProgressIndicatorSpec spec) {
    super(spec);
  }

  @Override
  int getPreferredWidth() {
    return getSize();
  }

  @Override
  int getPreferredHeight() {
    return getSize();
  }

  /**
   * Adjusts the canvas for drawing circular progress indicator. It rotates the canvas -90 degrees
   * to keep the 0 at the top. The canvas is clipped to a square with the size just includes the
   * inset. It will also pre-calculate the bound for drawing the arc based on the spinner radius and
   * current track thickness.
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
    // Scales the actual drawing by the ratio of the given bounds to the preferred size.
    float scaleX = (float) bounds.width() / getPreferredWidth();
    float scaleY = (float) bounds.height() / getPreferredHeight();

    // Calculates the scaled progress circle radius in x- and y-coordinate respectively.
    float outerRadiusWithInset = spec.indicatorSize / 2f + spec.indicatorInset;
    float scaledOuterRadiusWithInsetX = outerRadiusWithInset * scaleX;
    float scaledOuterRadiusWithInsetY = outerRadiusWithInset * scaleY;

    // Move the origin (0, 0) of the canvas to the center of the progress circle.
    canvas.translate(
        scaledOuterRadiusWithInsetX + bounds.left, scaledOuterRadiusWithInsetY + bounds.top);

    canvas.scale(scaleX, scaleY);
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
    if ((isShowing && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_INWARD)
        || (isHiding && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_OUTWARD)) {
      // Increases the radius by half of the full thickness, then reduces it half way of the
      // displayed thickness to match the outer edges of the displayed indicator and the full
      // indicator.
      adjustedRadius += (1 - trackThicknessFraction) * spec.trackThickness / 2;
    } else if ((isShowing && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_OUTWARD)
        || (isHiding && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_INWARD)) {
      // Decreases the radius by half of the full thickness, then raises it half way of the
      // displayed thickness to match the inner edges of the displayed indicator and the full
      // indicator.
      adjustedRadius -= (1 - trackThicknessFraction) * spec.trackThickness / 2;
    }
    // Sets the total track length fraction if ESCAPE hide animation is used.
    if (isHiding && spec.hideAnimationBehavior == HIDE_ESCAPE) {
      totalTrackLengthFraction = trackThicknessFraction;
    } else {
      totalTrackLengthFraction = 1f;
    }
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

    // Sets up the paint.
    paint.setStyle(Style.STROKE);
    paint.setStrokeCap(Cap.BUTT);
    paint.setAntiAlias(true);
    paint.setColor(color);
    paint.setStrokeWidth(displayedTrackThickness);

    float arcFraction =
        endFraction >= startFraction
            ? (endFraction - startFraction)
            : (1 + endFraction - startFraction);
    startFraction %= 1;
    if (totalTrackLengthFraction < 1 && startFraction + arcFraction > 1) {
      // Breaks the arc at 0 degree for ESCAPE animation.
      ActiveIndicator firstPart = new ActiveIndicator();
      firstPart.startFraction = startFraction;
      firstPart.endFraction = 1f;
      firstPart.color = color;
      fillIndicator(canvas, paint, firstPart, drawableAlpha);
      ActiveIndicator secondPart = new ActiveIndicator();
      secondPart.startFraction = 1f;
      secondPart.endFraction = startFraction + arcFraction;
      secondPart.color = color;
      fillIndicator(canvas, paint, secondPart, drawableAlpha);
      return;
    }
    // Scale start and arc fraction for ESCAPE animation.
    startFraction = lerp(1 - totalTrackLengthFraction, 1f, startFraction);
    arcFraction = lerp(0f, totalTrackLengthFraction, arcFraction);
    // Calculates the start and end in degrees.
    float startDegree = startFraction * 360 * arcDirectionFactor;
    float arcDegree = arcFraction * 360 * arcDirectionFactor;

    // Draws the gaps if needed.
    if (spec.indicatorTrackGapSize > 0) {
      float gapSize =
          min(spec.getIndicatorTrackGapSizeDegree(), Math.abs(startDegree)) * arcDirectionFactor;
      // No need to draw if the indicator is shorter than gap.
      if (Math.abs(arcDegree) <= Math.abs(gapSize) * 2) {
        return;
      }
      startDegree += gapSize;
      arcDegree -= gapSize * 2;
    }

    // Draws the indicator arc without rounded corners.
    RectF arcBound = new RectF(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
    canvas.drawArc(arcBound, startDegree, arcDegree, false, paint);

    // Draws rounded corners if needed.
    if (displayedCornerRadius > 0 && Math.abs(arcDegree) > 0 && Math.abs(arcDegree) < 360) {
      paint.setStyle(Style.FILL);
      drawRoundedEnd(canvas, paint, displayedTrackThickness, displayedCornerRadius, startDegree);
      drawRoundedEnd(
          canvas, paint, displayedTrackThickness, displayedCornerRadius, startDegree + arcDegree);
    }
  }

  @Override
  void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    int trackColor = MaterialColors.compositeARGBWithAlpha(spec.trackColor, drawableAlpha);

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
      Canvas canvas, Paint paint, float trackSize, float cornerRadius, float positionInDeg) {
    canvas.save();
    canvas.rotate(positionInDeg);

    RectF cornersBound =
        new RectF(
            adjustedRadius - trackSize / 2,
            cornerRadius,
            adjustedRadius + trackSize / 2,
            -cornerRadius);
    canvas.drawRoundRect(cornersBound, cornerRadius, cornerRadius, paint);
    canvas.restore();
  }
}
