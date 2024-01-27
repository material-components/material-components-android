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
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.Math.toRadians;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import com.google.android.material.color.MaterialColors;

/** A delegate class to help draw the graphics for {@link CircularProgressIndicator}. */
final class CircularDrawingDelegate extends DrawingDelegate<CircularProgressIndicatorSpec> {
  // When the progress is bigger than 99%, the arc will overshoot to hide the round caps.
  private static final float ROUND_CAP_RAMP_DOWN_THRESHHOLD = 0.01f;

  private float displayedTrackThickness;
  private float displayedCornerRadius;
  private float adjustedRadius;
  // For full round ends, the stroke ROUND cap is used to prevent artifacts like (b/319309456).
  private boolean useStrokeCap;

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
   * @param canvas Canvas to draw
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

    // Rotates canvas so that arc starts at top.
    canvas.rotate(-90f);
    // Flips canvas in Y axis (horizontal after rotation) if rotate counterclockwise.
    canvas.scale(scaleX, scaleY);
    if (spec.indicatorDirection != CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE) {
      canvas.scale(1, -1);
    }

    // Clip all drawing to the designated area, so it doesn't draw outside of its bounds (which can
    // happen in certain configuration of clipToPadding and clipChildren)
    canvas.clipRect(
        -outerRadiusWithInset, -outerRadiusWithInset, outerRadiusWithInset, outerRadiusWithInset);

    // These are used when drawing the indicator and track.
    useStrokeCap = spec.trackThickness / 2 <= spec.trackCornerRadius;
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius =
        min(spec.trackThickness / 2, spec.trackCornerRadius) * trackThicknessFraction;
    adjustedRadius = (spec.indicatorSize - spec.trackThickness) / 2f;

    // Further adjusts the radius for animated visibility change.
    if (isShowing || isHiding) {
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
    int color = MaterialColors.compositeARGBWithAlpha(activeIndicator.color, drawableAlpha);
    drawArc(
        canvas,
        paint,
        activeIndicator.startFraction,
        activeIndicator.endFraction,
        color,
        activeIndicator.gapSize,
        activeIndicator.gapSize);
  }

  @Override
  void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float startFraction,
      float endFraction,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha,
      int gapSize) {
    color = MaterialColors.compositeARGBWithAlpha(color, drawableAlpha);
    drawArc(canvas, paint, startFraction, endFraction, color, gapSize, gapSize);
  }

  /**
   * Draws a part of the full circle track with the designated details.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track. It
   *     starts from the top as 0 and grows clockwise at pace of 1 per cycle.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param paintColor The color used to draw the indicator.
   * @param startGapSize The gap size applied to the start (rotating behind) of the drawing part.
   * @param endGapSize The gap size applied to the end (rotating ahead) of the drawing part.
   */
  private void drawArc(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float startFraction,
      float endFraction,
      @ColorInt int paintColor,
      @Px int startGapSize,
      @Px int endGapSize) {
    float arcFraction =
        endFraction >= startFraction
            ? (endFraction - startFraction)
            : (1 + endFraction - startFraction);
    startFraction %= 1;

    if (totalTrackLengthFraction < 1 && startFraction + arcFraction > 1) {
      // Breaks the arc at 0 degree for ESCAPE animation.
      drawArc(canvas, paint, startFraction, 1, paintColor, startGapSize, 0);
      drawArc(canvas, paint, 1, startFraction + arcFraction, paintColor, 0, endGapSize);
      return;
    }

    float displayedCornerRadiusInDegree = (float) toDegrees(displayedCornerRadius / adjustedRadius);

    if (startFraction == 0 && arcFraction >= 1 - ROUND_CAP_RAMP_DOWN_THRESHHOLD) {
      // Increases the arc length to hide the round cap at the ends when the active indicator is
      // forming a full circle.
      arcFraction +=
          (arcFraction - (1 - ROUND_CAP_RAMP_DOWN_THRESHHOLD))
              * (2 * displayedCornerRadiusInDegree / 360)
              / ROUND_CAP_RAMP_DOWN_THRESHHOLD;
    }

    // Scale start and arc fraction for ESCAPE animation.
    startFraction = lerp(1 - totalTrackLengthFraction, 1f, startFraction);
    arcFraction = lerp(0f, totalTrackLengthFraction, arcFraction);

    float startGapSizeInDegrees = (float) toDegrees(startGapSize / adjustedRadius);
    float endGapSizeInDegrees = (float) toDegrees(endGapSize / adjustedRadius);
    float arcDegree = arcFraction * 360 - startGapSizeInDegrees - endGapSizeInDegrees;
    float startDegree = startFraction * 360 + startGapSizeInDegrees;

    // No need to draw if arc length is negative.
    if (arcDegree <= 0) {
      return;
    }

    // Sets up the paint.
    paint.setAntiAlias(true);
    paint.setColor(paintColor);
    paint.setStrokeWidth(displayedTrackThickness);

    // The arc will be drawn as three parts: 1) start rounded block (a rounded rectangle or a round
    // cap), 2) end rounded block (a rounded rectangle or a round cap), and 3) a arc in between, if
    // needed.
    if (arcDegree < displayedCornerRadiusInDegree * 2) {
      // Draws a scaled round rectangle, if the start and end are too close to draw the arc.
      float shrinkRatio = arcDegree / (displayedCornerRadiusInDegree * 2);
      paint.setStyle(Style.FILL);
      drawRoundedBlock(
          canvas,
          paint,
          startDegree + displayedCornerRadiusInDegree * shrinkRatio,
          displayedCornerRadius * 2,
          displayedTrackThickness,
          shrinkRatio);
    } else {
      // Draws the arc without rounded corners.
      RectF arcBound = new RectF(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
      paint.setStyle(Style.STROKE);
      // Draws the arc with ROUND cap if the corner radius is half of the track thickness.
      paint.setStrokeCap(useStrokeCap ? Cap.ROUND : Cap.BUTT);
      canvas.drawArc(
          arcBound,
          startDegree + displayedCornerRadiusInDegree,
          arcDegree - displayedCornerRadiusInDegree * 2,
          false,
          paint);

      // Draws rounded rectangles if ROUND cap is not used and the corner radius is bigger than 0.
      if (!useStrokeCap && displayedCornerRadius > 0) {
        paint.setStyle(Style.FILL);
        drawRoundedBlock(
            canvas,
            paint,
            startDegree + displayedCornerRadiusInDegree,
            displayedCornerRadius * 2,
            displayedTrackThickness);
        drawRoundedBlock(
            canvas,
            paint,
            startDegree + arcDegree - displayedCornerRadiusInDegree,
            displayedCornerRadius * 2,
            displayedTrackThickness);
      }
    }
  }

  @Override
  void drawStopIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    // No stop indicator is used in circular type.
  }

  private int getSize() {
    return spec.indicatorSize + spec.indicatorInset * 2;
  }

  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float positionInDeg,
      float markWidth,
      float markHeight) {
    drawRoundedBlock(canvas, paint, positionInDeg, markWidth, markHeight, 1);
  }

  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float positionInDeg,
      float markWidth,
      float markHeight,
      float scale) {
    markHeight = (int) min(markHeight, displayedTrackThickness);
    float markCornerSize = markHeight * displayedCornerRadius / displayedTrackThickness;
    markCornerSize = min(markWidth / 2, markCornerSize);
    RectF roundedBlock = new RectF(-markHeight / 2, -markWidth / 2, markHeight / 2, markWidth / 2);
    canvas.save();
    canvas.translate(
        (float) (adjustedRadius * cos(toRadians(positionInDeg))),
        (float) (adjustedRadius * sin(toRadians(positionInDeg))));
    canvas.rotate(positionInDeg);
    canvas.scale(scale, scale);
    canvas.drawRoundRect(roundedBlock, markCornerSize, markCornerSize, paint);
    canvas.restore();
  }
}
