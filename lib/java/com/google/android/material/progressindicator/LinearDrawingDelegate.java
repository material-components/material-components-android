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
import static com.google.android.material.progressindicator.BaseProgressIndicator.HIDE_OUTWARD;
import static com.google.android.material.progressindicator.BaseProgressIndicator.SHOW_INWARD;
import static com.google.android.material.progressindicator.DeterminateDrawable.GAP_RAMP_DOWN_THRESHOLD;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.Pair;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.core.math.MathUtils;
import com.google.android.material.color.MaterialColors;

/** A delegate class to help draw the graphics for {@link LinearProgressIndicator}. */
final class LinearDrawingDelegate extends DrawingDelegate<LinearProgressIndicatorSpec> {

  // The length (horizontal) of the track in px.
  private float trackLength = 300f;
  private float displayedTrackThickness;
  private float displayedCornerRadius;
  private float displayedInnerCornerRadius;
  private float displayedAmplitude;
  private float adjustedWavelength;
  private int cachedWavelength;
  private boolean drawingDeterminateIndicator;

  // This will be used in the ESCAPE hide animation. The start and end fraction in track will be
  // scaled by this fraction with a pivot of 1.0f.
  @FloatRange(from = 0.0f, to = 1.0f)
  private float totalTrackLengthFraction;

  // Pre-allocates objects used in draw().
  Pair<PathPoint, PathPoint> endPoints = new Pair<>(new PathPoint(), new PathPoint());

  /** Instantiates LinearDrawingDelegate with the current spec. */
  LinearDrawingDelegate(@NonNull LinearProgressIndicatorSpec spec) {
    super(spec);
  }

  @Override
  int getPreferredWidth() {
    return -1;
  }

  @Override
  int getPreferredHeight() {
    return spec.trackThickness + spec.waveAmplitude * 2;
  }

  /**
   * Adjusts the canvas for linear progress indicator drawables. It flips the canvas horizontally if
   * it's inverted. It flips the canvas vertically if outgoing grow mode is applied.
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
    if (trackLength != bounds.width()) {
      trackLength = bounds.width();
      invalidateCachedPaths();
    }
    float trackSize = getPreferredHeight();

    // Positions canvas to center of the clip bounds.
    canvas.translate(
        bounds.left + bounds.width() / 2f,
        bounds.top + bounds.height() / 2f + max(0f, (bounds.height() - trackSize) / 2f));

    // Flips canvas horizontally if need to draw right to left.
    if (spec.drawHorizontallyInverse) {
      canvas.scale(-1f, 1f);
    }

    // Clips all drawing to the track area, so it doesn't draw outside of its bounds (which can
    // happen in certain configurations of clipToPadding and clipChildren)
    float halfTrackLength = trackLength / 2;
    float halfTrackSize = trackSize / 2;
    canvas.clipRect(-halfTrackLength, -halfTrackSize, halfTrackLength, halfTrackSize);

    // These are set for the drawing the indicator and track.
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius =
        min(spec.trackThickness / 2, spec.getTrackCornerRadiusInPx()) * trackThicknessFraction;
    displayedAmplitude = spec.waveAmplitude * trackThicknessFraction;
    displayedInnerCornerRadius =
        min(spec.trackThickness / 2f, spec.getTrackInnerCornerRadiusInPx())
            * trackThicknessFraction;

    // Further adjusts the canvas for animated visibility change.
    if (isShowing || isHiding) {
      // Flips canvas vertically if need to anchor to the bottom edge.
      if ((isShowing && spec.showAnimationBehavior == SHOW_INWARD)
          || (isHiding && spec.hideAnimationBehavior == HIDE_OUTWARD)) {
        canvas.scale(1f, -1f);
      }
      // Offsets canvas vertically while showing or hiding.
      if (isShowing || (isHiding && spec.hideAnimationBehavior != HIDE_ESCAPE)) {
        canvas.translate(0f, spec.trackThickness * (1 - trackThicknessFraction) / 2f);
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
      int drawableAlpha) {
    int color = MaterialColors.compositeARGBWithAlpha(activeIndicator.color, drawableAlpha);
    drawingDeterminateIndicator = activeIndicator.isDeterminate;
    drawLine(
        canvas,
        paint,
        activeIndicator.startFraction,
        activeIndicator.endFraction,
        color,
        activeIndicator.gapSize,
        activeIndicator.gapSize,
        activeIndicator.amplitudeFraction,
        activeIndicator.phaseFraction,
        /* drawingActiveIndicator= */ true);
  }

  @Override
  void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float startFraction,
      float endFraction,
      int color,
      int drawableAlpha,
      @Px int gapSize) {
    color = MaterialColors.compositeARGBWithAlpha(color, drawableAlpha);
    drawingDeterminateIndicator = false;
    drawLine(
        canvas,
        paint,
        startFraction,
        endFraction,
        color,
        gapSize,
        gapSize,
        /* amplitudeFraction= */ 0f,
        /* phaseFraction= */ 0f,
        /* drawingActiveIndicator= */ false);
  }

  /**
   * Draws a part of the full track with specified details.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param paintColor The color used to draw the indicator.
   * @param startGapSize The gap size applied to the start (left) of the drawing part.
   * @param endGapSize The gap size applied to the end (right) of the drawing part.
   * @param amplitudeFraction The fraction [0, 1] of amplitude applied to the part.
   * @param phaseFraction The fraction [0, 1] of initial phase in one cycle.
   * @param drawingActiveIndicator Whether this part should be drawn as an active indicator.
   */
  private void drawLine(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      float startFraction,
      float endFraction,
      @ColorInt int paintColor,
      @Px int startGapSize,
      @Px int endGapSize,
      float amplitudeFraction,
      float phaseFraction,
      boolean drawingActiveIndicator) {
    startFraction = clamp(startFraction, 0f, 1f);
    endFraction = clamp(endFraction, 0f, 1f);
    // Scale start and end fraction if ESCAPE animation is used.
    startFraction = lerp(1 - totalTrackLengthFraction, 1f, startFraction);
    endFraction = lerp(1 - totalTrackLengthFraction, 1f, endFraction);

    startGapSize =
        (int)
            (startGapSize
                * MathUtils.clamp(startFraction, 0f, GAP_RAMP_DOWN_THRESHOLD)
                / GAP_RAMP_DOWN_THRESHOLD);
    endGapSize =
        (int)
            (endGapSize
                * (1 - MathUtils.clamp(endFraction, 1 - GAP_RAMP_DOWN_THRESHOLD, 1f))
                / GAP_RAMP_DOWN_THRESHOLD);

    // Offsets start and end by the requested gap sizes.
    int startPx = (int) (startFraction * trackLength + startGapSize);
    int endPx = (int) (endFraction * trackLength - endGapSize);
    float startCornerRadius = displayedCornerRadius;
    float endCornerRadius = displayedCornerRadius;
    // Morph corners when outer and inner corner radius are different.
    if (displayedCornerRadius != displayedInnerCornerRadius) {
      float cornerRampDownThreshold =
          max(displayedCornerRadius, displayedInnerCornerRadius) / trackLength;
      startCornerRadius =
          lerp(
              displayedCornerRadius,
              displayedInnerCornerRadius,
              MathUtils.clamp((float) startPx / trackLength, 0, cornerRampDownThreshold)
                  / cornerRampDownThreshold);
      endCornerRadius =
          lerp(
              displayedCornerRadius,
              displayedInnerCornerRadius,
              MathUtils.clamp((trackLength - endPx) / trackLength, 0, cornerRampDownThreshold)
                  / cornerRampDownThreshold);
    }
    // Adjusts start/end X so the progress indicator will start from 0 when startFraction == 0.
    float originX = -trackLength / 2;

    boolean drawWavyPath =
        spec.hasWavyEffect(drawingDeterminateIndicator)
            && drawingActiveIndicator
            && amplitudeFraction > 0f;

    // No need to draw on track if start and end are out of visible range.
    if (startPx <= endPx) {
      // The track part will be drawn as three parts: 1) start rounded block (a rounded rectangle),
      // 2) end rounded block (a rounded rectangle), and 3) a path in between, if needed.
      float startBlockCenterX = startPx + startCornerRadius;
      float endBlockCenterX = endPx - endCornerRadius;
      float startBlockWidth = startCornerRadius * 2;
      float endBlockWidth = endCornerRadius * 2;

      paint.setColor(paintColor);
      paint.setAntiAlias(true);
      paint.setStrokeWidth(displayedTrackThickness);

      endPoints.first.reset();
      endPoints.second.reset();
      endPoints.first.translate(startBlockCenterX + originX, 0);
      endPoints.second.translate(endBlockCenterX + originX, 0);

      if (startPx == 0
          && endBlockCenterX + endCornerRadius < startBlockCenterX + startCornerRadius) {
        drawRoundedBlock(
            canvas,
            paint,
            endPoints.first,
            startBlockWidth,
            displayedTrackThickness,
            startCornerRadius,
            endPoints.second,
            endBlockWidth,
            displayedTrackThickness,
            endCornerRadius,
            true);
      } else if (startBlockCenterX - startCornerRadius > endBlockCenterX - endCornerRadius) {
        drawRoundedBlock(
            canvas,
            paint,
            endPoints.second,
            endBlockWidth,
            displayedTrackThickness,
            endCornerRadius,
            endPoints.first,
            startBlockWidth,
            displayedTrackThickness,
            startCornerRadius,
            false);
      } else {
        // Draws the path with ROUND cap if the corner radius is half of the track
        // thickness.
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(spec.useStrokeCap() ? Cap.ROUND : Cap.BUTT);

        // If start rounded block is on the left of end rounded block, draws the path with the
        // start and end rounded blocks.
        if (!drawWavyPath) {
          // Draws a straight line directly.
          canvas.drawLine(
              endPoints.first.posVec[0],
              endPoints.first.posVec[1],
              endPoints.second.posVec[0],
              endPoints.second.posVec[1],
              paint);
        } else {
          // Draws a portion of the cached wavy path.
          calculateDisplayedPath(
              activePathMeasure,
              displayedActivePath,
              endPoints,
              startBlockCenterX / trackLength,
              endBlockCenterX / trackLength,
              amplitudeFraction,
              phaseFraction);
          canvas.drawPath(displayedActivePath, paint);
        }
        if (!spec.useStrokeCap()) {
          if (startBlockCenterX > 0 && startCornerRadius > 0) {
            // Draws the start rounded block.
            drawRoundedBlock(
                canvas,
                paint,
                endPoints.first,
                startBlockWidth,
                displayedTrackThickness,
                startCornerRadius);
          }
          if (endBlockCenterX < trackLength && endCornerRadius > 0) {
            // Draws the end rounded block.
            drawRoundedBlock(
                canvas,
                paint,
                endPoints.second,
                endBlockWidth,
                displayedTrackThickness,
                endCornerRadius);
          }
        }
      }
    }
  }

  @Override
  void drawStopIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    int paintColor = MaterialColors.compositeARGBWithAlpha(color, drawableAlpha);
    drawingDeterminateIndicator = false;
    int trackStopIndicatorSize = spec.getActualTrackStopIndicatorSize();
    if (trackStopIndicatorSize > 0 && paintColor != Color.TRANSPARENT) {
      // Draws the stop indicator at the end of the track if needed.
      paint.setStyle(Style.FILL);
      paint.setColor(paintColor);
      float stopIndicatorCenterX =
          spec.trackStopIndicatorPadding != null
              ? spec.trackStopIndicatorPadding.floatValue() + spec.trackStopIndicatorSize / 2f
              : displayedTrackThickness / 2;
      drawRoundedBlock(
          canvas,
          paint,
          new PathPoint(
              new float[] {trackLength / 2 - stopIndicatorCenterX, 0}, new float[] {1, 0}),
          trackStopIndicatorSize,
          trackStopIndicatorSize,
          displayedCornerRadius * trackStopIndicatorSize / displayedTrackThickness);
    }
  }

  /** Draws a single rounded block for one of the track ends. */
  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull PathPoint drawCenter,
      float drawWidth,
      float drawHeight,
      float drawCornerSize) {
    drawRoundedBlock(
        canvas, paint, drawCenter, drawWidth, drawHeight, drawCornerSize, null, 0, 0, 0, false);
  }

  /** Drawas the merged rounded block when two track ends are collapsed. */
  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull PathPoint drawCenter,
      float drawWidth,
      float drawHeight,
      float drawCornerSize,
      @Nullable PathPoint clipCenter,
      float clipWidth,
      float clipHeight,
      float clipCornerSize,
      boolean clipRight) {
    drawHeight = min(drawHeight, displayedTrackThickness);
    RectF drawRect = new RectF(-drawWidth / 2f, -drawHeight / 2f, drawWidth / 2f, drawHeight / 2f);
    paint.setStyle(Style.FILL);
    canvas.save();
    // Clipping!
    if (clipCenter != null) {
      clipHeight = min(clipHeight, displayedTrackThickness);
      clipCornerSize = min(clipWidth / 2, clipCornerSize * clipHeight / displayedTrackThickness);
      RectF patchRect = new RectF();
      if (clipRight) {
        float leftEdgeDiff =
            (clipCenter.posVec[0] - clipCornerSize) - (drawCenter.posVec[0] - drawCornerSize);
        if (leftEdgeDiff > 0) {
          // Clip block is too small. Expand it to include the left edge of the draw block.
          clipCenter.translate(-leftEdgeDiff / 2, 0);
          clipWidth += leftEdgeDiff;
        }
        // Draw the patch rectangle to fill the gap from the draw block center to its right edge.
        patchRect.set(0, -drawHeight / 2f, drawWidth / 2f, drawHeight / 2f);
      } else {
        float rightEdgeDiff =
            (clipCenter.posVec[0] + clipCornerSize) - (drawCenter.posVec[0] + drawCornerSize);
        if (rightEdgeDiff < 0) {
          // Clip block is too small. Expand it to include the right edge of the draw block.
          clipCenter.translate(-rightEdgeDiff / 2, 0);
          clipWidth -= rightEdgeDiff;
        }
        // Draw the patch rectangle to fill the gap from the draw block center to its left edge.
        patchRect.set(-drawWidth / 2f, -drawHeight / 2f, 0, drawHeight / 2f);
      }
      RectF clipRect =
          new RectF(-clipWidth / 2f, -clipHeight / 2f, clipWidth / 2f, clipHeight / 2f);
      canvas.translate(clipCenter.posVec[0], clipCenter.posVec[1]);
      canvas.rotate(vectorToCanvasRotation(clipCenter.tanVec));
      Path clipPath = new Path();
      clipPath.addRoundRect(clipRect, clipCornerSize, clipCornerSize, Direction.CCW);
      canvas.clipPath(clipPath);
      // Manually restore to the original canvas transform.
      canvas.rotate(-vectorToCanvasRotation(clipCenter.tanVec));
      canvas.translate(-clipCenter.posVec[0], -clipCenter.posVec[1]);
      // Transform to the draw block center and rotation.
      canvas.translate(drawCenter.posVec[0], drawCenter.posVec[1]);
      canvas.rotate(vectorToCanvasRotation(drawCenter.tanVec));
      canvas.drawRect(patchRect, paint);
      // Draw the draw block.
      canvas.drawRoundRect(drawRect, drawCornerSize, drawCornerSize, paint);
    } else {
      // Transform to the draw block center and rotation.
      canvas.translate(drawCenter.posVec[0], drawCenter.posVec[1]);
      canvas.rotate(vectorToCanvasRotation(drawCenter.tanVec));
      // Draw the draw block.
      canvas.drawRoundRect(drawRect, drawCornerSize, drawCornerSize, paint);
    }
    canvas.restore();
  }

  @Override
  void invalidateCachedPaths() {
    cachedActivePath.rewind();
    if (spec.hasWavyEffect(drawingDeterminateIndicator)) {
      int wavelength =
          drawingDeterminateIndicator ? spec.wavelengthDeterminate : spec.wavelengthIndeterminate;
      int cycleCount = (int) (trackLength / wavelength);
      adjustedWavelength = trackLength / cycleCount;
      float smoothness = WAVE_SMOOTHNESS;
      for (int i = 0; i <= cycleCount; i++) {
        cachedActivePath.cubicTo(2 * i + smoothness, 0, 2 * i + 1 - smoothness, 1, 2 * i + 1, 1);
        cachedActivePath.cubicTo(
            2 * i + 1 + smoothness, 1, 2 * i + 2 - smoothness, 0, 2 * i + 2, 0);
      }
      // Transforms the wavy path from y = -1/2 * cos(PI * x) + 1/2, as calculated above,
      // to y = cos(2 * PI * x / wavelength), as required in spec.
      transform.reset();
      transform.setScale(adjustedWavelength / 2, -2);
      transform.postTranslate(0, 1);
      cachedActivePath.transform(transform);
    } else {
      cachedActivePath.lineTo(trackLength, 0);
    }
    activePathMeasure.setPath(cachedActivePath, /* forceNewPath= */ false);
  }

  private void calculateDisplayedPath(
      @NonNull PathMeasure pathMeasure,
      @NonNull Path displayedPath,
      @NonNull Pair<PathPoint, PathPoint> endPoints,
      float start,
      float end,
      float amplitudeFraction,
      float phaseFraction) {
    int wavelength =
        drawingDeterminateIndicator ? spec.wavelengthDeterminate : spec.wavelengthIndeterminate;
    if (pathMeasure == activePathMeasure && wavelength != cachedWavelength) {
      cachedWavelength = wavelength;
      invalidateCachedPaths();
    }
    displayedPath.rewind();
    float resultTranslationX = -trackLength / 2;
    boolean hasWavyEffect = spec.hasWavyEffect(drawingDeterminateIndicator);
    if (hasWavyEffect) {
      float cycleCount = trackLength / adjustedWavelength;
      float phaseFractionInPath = phaseFraction / cycleCount;
      float ratio = cycleCount / (cycleCount + 1);
      start = (start + phaseFractionInPath) * ratio;
      end = (end + phaseFractionInPath) * ratio;
      resultTranslationX -= phaseFraction * adjustedWavelength;
    }
    float startDistance = start * pathMeasure.getLength();
    float endDistance = end * pathMeasure.getLength();
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
    transform.setTranslate(resultTranslationX, 0);
    startPoint.translate(resultTranslationX, 0);
    endPoint.translate(resultTranslationX, 0);
    if (hasWavyEffect) {
      float scaleY = displayedAmplitude * amplitudeFraction;
      transform.postScale(1, scaleY);
      startPoint.scale(1, scaleY);
      endPoint.scale(1, scaleY);
    }
    displayedPath.transform(transform);
  }
}
