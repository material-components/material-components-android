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

import static androidx.core.math.MathUtils.clamp;
import static com.google.android.material.math.MathUtils.lerp;
import static com.google.android.material.progressindicator.BaseProgressIndicator.HIDE_ESCAPE;
import static java.lang.Math.PI;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.toDegrees;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Pair;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.List;

/** A delegate class to help draw the graphics for {@link CircularProgressIndicator}. */
final class CircularDrawingDelegate extends DrawingDelegate<CircularProgressIndicatorSpec> {
  // When the progress is bigger than 99%, the arc will overshoot to hide the round caps.
  private static final float ROUND_CAP_RAMP_DOWN_THRESHHOLD = 0.01f;
  // Constant for approximating a quarter circle with cubic bezier.
  private static final float QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH = 0.5522848f;

  private float displayedTrackThickness;
  private float displayedCornerRadius;
  private float displayedAmplitude;
  private float adjustedRadius;
  private float adjustedWavelength;
  private float cachedAmplitude;
  private int cachedWavelength;
  private float cachedRadius;
  private boolean drawingDeterminateIndicator;

  // This will be used in the ESCAPE hide animation. The start and end fraction in track will be
  // scaled by this fraction with a pivot of 1.0f.
  @FloatRange(from = 0.0f, to = 1.0f)
  private float totalTrackLengthFraction;

  // Pre-allocates objects used in draw().
  private final RectF arcBounds = new RectF();
  private final Pair<PathPoint, PathPoint> endPoints = new Pair<>(new PathPoint(), new PathPoint());

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
      if (VERSION.SDK_INT == VERSION_CODES.Q) {
        // There's some issue to rotate and flip the canvas on API 29. The workaround is to rotate
        // the canvas an extra 0.1 degree.
        canvas.rotate(0.1f);
      }
    }

    // Clip all drawing to the designated area, so it doesn't draw outside of its bounds (which can
    // happen in certain configuration of clipToPadding and clipChildren)
    canvas.clipRect(
        -outerRadiusWithInset, -outerRadiusWithInset, outerRadiusWithInset, outerRadiusWithInset);

    // These are used when drawing the indicator and track.
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius =
        min(spec.trackThickness / 2, spec.getTrackCornerRadiusInPx()) * trackThicknessFraction;
    displayedAmplitude = spec.waveAmplitude * trackThicknessFraction;

    // Further adjusts the radius for animated visibility change.
    adjustedRadius = (spec.indicatorSize - spec.trackThickness) / 2f;
    if (isShowing || isHiding) {
      // This is the delta of the radius between the track matching the central line and the
      // track matching the inner/outer edge with the full width track.
      float deltaRadius = (1 - trackThicknessFraction) * spec.trackThickness / 2;
      if ((isShowing && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_INWARD)
          || (isHiding && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_OUTWARD)) {
        // Increases the radius by the radius delta to match the outer edges of the displayed
        // track and the full width track.
        adjustedRadius += deltaRadius;
      } else if ((isShowing && spec.showAnimationBehavior == CircularProgressIndicator.SHOW_OUTWARD)
          || (isHiding && spec.hideAnimationBehavior == CircularProgressIndicator.HIDE_INWARD)) {
        // Decreases the radius by the radius delta to match the inner edges of the displayed
        // track and the full width track.
        adjustedRadius -= deltaRadius;
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
    canvas.save();
    canvas.rotate(activeIndicator.rotationDegree);
    drawingDeterminateIndicator = activeIndicator.isDeterminate;
    drawArc(
        canvas,
        paint,
        activeIndicator.startFraction,
        activeIndicator.endFraction,
        color,
        activeIndicator.gapSize,
        activeIndicator.gapSize,
        activeIndicator.amplitudeFraction,
        activeIndicator.phaseFraction,
        /* shouldDrawActiveIndicator= */ true);
    canvas.restore();
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
    drawingDeterminateIndicator = false;
    drawArc(
        canvas,
        paint,
        startFraction,
        endFraction,
        color,
        gapSize,
        gapSize,
        /* amplitudeFraction= */ 0f,
        /* phaseFraction= */ 0f,
        /* shouldDrawActiveIndicator= */ false);
  }

  /**
   * Draws a part of the full circle (or wavy circle) track with the designated details.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track. It
   *     starts from the top as 0 and grows clockwise at pace of 1 per cycle.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param paintColor The color used to draw the indicator.
   * @param startGapSize The gap size applied to the start (rotating behind) of the drawing part.
   * @param endGapSize The gap size applied to the end (rotating ahead) of the drawing part.
   * @param amplitudeFraction The fraction [0, 1] of amplitude applied to the part.
   * @param phaseFraction The fraction [0, 1] of initial phase in one cycle.
   * @param shouldDrawActiveIndicator Whether this part should be drawn as an active indicator.
   */
  private void drawArc(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float startFraction,
      float endFraction,
      @ColorInt int paintColor,
      @Px int startGapSize,
      @Px int endGapSize,
      float amplitudeFraction,
      float phaseFraction,
      boolean shouldDrawActiveIndicator) {
    float arcFraction =
        endFraction >= startFraction
            ? (endFraction - startFraction)
            : (1 + endFraction - startFraction);
    startFraction %= 1;
    if (startFraction < 0) {
      startFraction += 1f;
    }

    if (totalTrackLengthFraction < 1 && startFraction + arcFraction > 1) {
      // Breaks the arc at 0 degree for ESCAPE animation.
      drawArc(
          canvas,
          paint,
          startFraction,
          /* endFraction= */ 1f,
          paintColor,
          startGapSize,
          /* endGapSize= */ 0,
          amplitudeFraction,
          phaseFraction,
          shouldDrawActiveIndicator);
      drawArc(
          canvas,
          paint,
          /* startFraction= */ 1f,
          startFraction + arcFraction,
          paintColor,
          /* startGapSize= */ 0,
          endGapSize,
          amplitudeFraction,
          phaseFraction,
          shouldDrawActiveIndicator);
      return;
    }

    float displayedCornerRadiusInDegree = (float) toDegrees(displayedCornerRadius / adjustedRadius);
    float arcFractionOverRoundCapThreshold = arcFraction - (1 - ROUND_CAP_RAMP_DOWN_THRESHHOLD);
    if (arcFractionOverRoundCapThreshold >= 0) {
      // Increases the arc length to hide the round cap at the ends when the active indicator is
      // forming a full circle.
      float increasedArcFraction =
          arcFractionOverRoundCapThreshold
              * displayedCornerRadiusInDegree
              / 180
              / ROUND_CAP_RAMP_DOWN_THRESHHOLD;
      arcFraction += increasedArcFraction;
      // Offsets the start fraction to make the inactive track's ends connect at 0%.
      if (!shouldDrawActiveIndicator) {
        startFraction -= increasedArcFraction / 2;
      }
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

    boolean shouldDrawWavyPath =
        spec.hasWavyEffect(drawingDeterminateIndicator)
            && shouldDrawActiveIndicator
            && amplitudeFraction > 0f;

    // Sets up the paint.
    paint.setAntiAlias(true);
    paint.setColor(paintColor);
    paint.setStrokeWidth(displayedTrackThickness);

    // The arc will be drawn as three parts: 1) start rounded block (a rounded rectangle or a round
    // cap), 2) end rounded block (a rounded rectangle or a round cap), and 3) a arc in between, if
    // needed.
    float blockWidth = displayedCornerRadius * 2;
    if (arcDegree < displayedCornerRadiusInDegree * 2) {
      // Draws a scaled round rectangle, if the start and end are too close to draw the arc.
      float shrinkRatio = arcDegree / (displayedCornerRadiusInDegree * 2);
      float centerDegree = startDegree + displayedCornerRadiusInDegree * shrinkRatio;
      PathPoint center = new PathPoint();
      if (!shouldDrawWavyPath) {
        center.rotate(centerDegree + 90);
        center.moveAcross(-adjustedRadius);
      } else {
        float centerDistance = centerDegree / 360 * activePathMeasure.getLength() / 2;
        float amplitude = displayedAmplitude * amplitudeFraction;
        if (adjustedRadius != cachedRadius || amplitude != cachedAmplitude) {
          cachedAmplitude = amplitude;
          cachedRadius = adjustedRadius;
          invalidateCachedPaths();
        }
        activePathMeasure.getPosTan(centerDistance, center.posVec, center.tanVec);
      }
      paint.setStyle(Style.FILL);
      drawRoundedBlock(canvas, paint, center, blockWidth, displayedTrackThickness, shrinkRatio);
    } else {
      // Draws the arc with ROUND cap if the corner radius is half of the track thickness.
      paint.setStyle(Style.STROKE);
      paint.setStrokeCap(spec.useStrokeCap() ? Cap.ROUND : Cap.BUTT);
      // Draws the arc without rounded corners.
      float startDegreeWithoutCorners = startDegree + displayedCornerRadiusInDegree;
      float arcDegreeWithoutCorners = arcDegree - displayedCornerRadiusInDegree * 2;
      endPoints.first.reset();
      endPoints.second.reset();
      if (!shouldDrawWavyPath) {
        endPoints.first.rotate(startDegreeWithoutCorners + 90);
        endPoints.first.moveAcross(-adjustedRadius);
        endPoints.second.rotate(startDegreeWithoutCorners + arcDegreeWithoutCorners + 90);
        endPoints.second.moveAcross(-adjustedRadius);
        arcBounds.set(-adjustedRadius, -adjustedRadius, adjustedRadius, adjustedRadius);
        canvas.drawArc(arcBounds, startDegreeWithoutCorners, arcDegreeWithoutCorners, false, paint);
      } else {
        calculateDisplayedPath(
            activePathMeasure,
            displayedActivePath,
            endPoints,
            startDegreeWithoutCorners / 360,
            arcDegreeWithoutCorners / 360,
            amplitudeFraction,
            phaseFraction);
        canvas.drawPath(displayedActivePath, paint);
      }

      // Draws rounded rectangles if ROUND cap is not used and the corner radius is bigger than 0.
      if (!spec.useStrokeCap() && displayedCornerRadius > 0) {
        paint.setStyle(Style.FILL);
        drawRoundedBlock(canvas, paint, endPoints.first, blockWidth, displayedTrackThickness);
        drawRoundedBlock(canvas, paint, endPoints.second, blockWidth, displayedTrackThickness);
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
      @NonNull PathPoint center,
      float markWidth,
      float markHeight) {
    drawRoundedBlock(canvas, paint, center, markWidth, markHeight, 1);
  }

  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull PathPoint center,
      float markWidth,
      float markHeight,
      float scale) {
    markHeight = min(markHeight, displayedTrackThickness);
    float markCornerSize = markHeight * displayedCornerRadius / displayedTrackThickness;
    markCornerSize = min(markWidth / 2, markCornerSize);
    RectF roundedBlock =
        new RectF(-markWidth / 2f, -markHeight / 2f, markWidth / 2f, markHeight / 2f);
    canvas.save();
    canvas.translate(center.posVec[0], center.posVec[1]);
    canvas.rotate(vectorToCanvasRotation(center.tanVec));
    canvas.scale(scale, scale);
    canvas.drawRoundRect(roundedBlock, markCornerSize, markCornerSize, paint);
    canvas.restore();
  }

  @Override
  void invalidateCachedPaths() {
    cachedActivePath.rewind();
    // Generates base path as two circles (two copies of the exact same circle without closing the
    // path). Two circles are needed for drawing an arc starting from an arbitrary position on the
    // circle. The start point will be always reduced to fall into the first circle. The arc can
    // never (not necessarily) be longer than a full circle. So the end point will always fall on
    // the second circle.
    cachedActivePath.moveTo(1, 0);
    for (int i = 0; i < 2; i++) {
      cachedActivePath.cubicTo(
          1, QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH, QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH, 1, 0, 1);
      cachedActivePath.cubicTo(
          -QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH,
          1,
          -1,
          QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH,
          -1,
          0);
      cachedActivePath.cubicTo(
          -1,
          -QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH,
          -QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH,
          -1,
          0,
          -1);
      cachedActivePath.cubicTo(
          QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH, -1, 1, -QUARTER_CIRCLE_CONTROL_HANDLE_LENGTH, 1, 0);
    }
    // Scales the circle to its radius.
    transform.reset();
    transform.setScale(adjustedRadius, adjustedRadius);
    cachedActivePath.transform(transform);
    if (spec.hasWavyEffect(drawingDeterminateIndicator)) {
      activePathMeasure.setPath(cachedActivePath, false);
      createWavyPath(activePathMeasure, cachedActivePath, cachedAmplitude);
    }
    activePathMeasure.setPath(cachedActivePath, false);
  }

  private void createWavyPath(
      @NonNull PathMeasure basePathMeasure, @NonNull Path outPath, float amplitude) {
    outPath.rewind();
    // Calculates anchor points.
    float basePathLength = basePathMeasure.getLength();
    int wavelength =
        drawingDeterminateIndicator ? spec.wavelengthDeterminate : spec.wavelengthIndeterminate;
    int cycleCountInPath = 2 * max(3, (int) (basePathLength / wavelength / 2));
    adjustedWavelength = basePathLength / cycleCountInPath;
    // For each cycle, there will be 2 cubic beziers, which need 3 anchors (startAnchor and
    // midAnchor for the 1st cubic bezier; midAnchor and startAnchor of the next cycle for the 2nd
    // cubic bezier). We'll need the coordinates and tangents for each anchor. This List will also
    // save the first anchor at the end in favor of the ease of calculation.
    List<PathPoint> anchors = new ArrayList<>();
    for (int i = 0; i < cycleCountInPath; i++) {
      PathPoint startAnchor = new PathPoint();
      basePathMeasure.getPosTan(adjustedWavelength * i, startAnchor.posVec, startAnchor.tanVec);
      PathPoint midAnchor = new PathPoint();
      basePathMeasure.getPosTan(
          adjustedWavelength * i + adjustedWavelength / 2, midAnchor.posVec, midAnchor.tanVec);
      anchors.add(startAnchor);
      // Shifts anchors to create amplitude (inward).
      midAnchor.moveAcross(amplitude * 2);
      anchors.add(midAnchor);
    }
    // There are (2 * cycleCount + 1) anchors in total. The last is the same as the first for the
    // sake of cubic bezier calculation.
    anchors.add(anchors.get(0));
    // Calculates the controls cubic beziers.
    PathPoint startAnchor = anchors.get(0);
    outPath.moveTo(startAnchor.posVec[0], startAnchor.posVec[1]);
    for (int i = 1; i < anchors.size(); i++) {
      PathPoint endAnchor = anchors.get(i);
      appendCubicPerHalfCycle(outPath, startAnchor, endAnchor);
      startAnchor = endAnchor;
    }
  }

  private void appendCubicPerHalfCycle(
      @NonNull Path outPath, @NonNull PathPoint anchor1, @NonNull PathPoint anchor2) {
    float controlLength = adjustedWavelength / 2 * WAVE_SMOOTHNESS;
    PathPoint control1 = new PathPoint(anchor1);
    PathPoint control2 = new PathPoint(anchor2);
    control1.moveAlong(controlLength);
    control2.moveAlong(-controlLength);
    outPath.cubicTo(
        control1.posVec[0],
        control1.posVec[1],
        control2.posVec[0],
        control2.posVec[1],
        anchor2.posVec[0],
        anchor2.posVec[1]);
  }

  private void calculateDisplayedPath(
      @NonNull PathMeasure pathMeasure,
      @NonNull Path displayedPath,
      @NonNull Pair<PathPoint, PathPoint> endPoints,
      float start,
      float span,
      float amplitudeFraction,
      float phaseFraction) {
    float amplitude = displayedAmplitude * amplitudeFraction;
    int wavelength =
        drawingDeterminateIndicator ? spec.wavelengthDeterminate : spec.wavelengthIndeterminate;
    if (adjustedRadius != cachedRadius
        || (pathMeasure == activePathMeasure
            && (amplitude != cachedAmplitude || wavelength != cachedWavelength))) {
      cachedAmplitude = amplitude;
      cachedWavelength = wavelength;
      cachedRadius = adjustedRadius;
      invalidateCachedPaths();
    }
    displayedPath.rewind();
    span = clamp(span, 0, 1);
    float resultRotation = 0;
    if (spec.hasWavyEffect(drawingDeterminateIndicator)) {
      float cycleCount = (float) (2 * PI * adjustedRadius / adjustedWavelength);
      float phaseFractionInOneCycle = phaseFraction / cycleCount;
      start += phaseFractionInOneCycle;
      resultRotation -= phaseFractionInOneCycle * 360;
    }
    start %= 1;
    float startDistance = start * pathMeasure.getLength() / 2;
    float endDistance = (start + span) * pathMeasure.getLength() / 2;
    pathMeasure.getSegment(startDistance, endDistance, displayedPath, true);
    // Gathers the position and tangent of the start and end.
    PathPoint startPoint = endPoints.first;
    startPoint.reset();
    pathMeasure.getPosTan(startDistance, startPoint.posVec, startPoint.tanVec);
    PathPoint endPoint = endPoints.second;
    endPoint.reset();
    pathMeasure.getPosTan(endDistance, endPoint.posVec, endPoint.tanVec);
    // Transforms the result path to match the canvas.
    transform.reset();
    transform.setRotate(resultRotation);
    startPoint.rotate(resultRotation);
    endPoint.rotate(resultRotation);
    displayedPath.transform(transform);
  }
}
