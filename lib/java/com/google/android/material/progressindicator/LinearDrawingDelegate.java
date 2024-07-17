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
import android.graphics.Matrix;
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
  private float displayedAmplitude;
  private float adjustedWavelength;
  private int cachedWavelength;
  private boolean useStrokeCap;
  private boolean drawingDeterminateIndicator;

  // This will be used in the ESCAPE hide animation. The start and end fraction in track will be
  // scaled by this fraction with a pivot of 1.0f.
  @FloatRange(from = 0.0f, to = 1.0f)
  private float totalTrackLengthFraction;

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
    useStrokeCap = spec.trackThickness / 2f <= spec.trackCornerRadius;
    displayedTrackThickness = spec.trackThickness * trackThicknessFraction;
    displayedCornerRadius =
        min(spec.trackThickness / 2f, spec.trackCornerRadius) * trackThicknessFraction;
    displayedAmplitude = spec.waveAmplitude * trackThicknessFraction;

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
      float startBlockCenterX = startPx + displayedCornerRadius;
      float endBlockCenterX = endPx - displayedCornerRadius;
      float blockWidth = displayedCornerRadius * 2;

      paint.setColor(paintColor);
      paint.setAntiAlias(true);
      paint.setStrokeWidth(displayedTrackThickness);

      Pair<PathPoint, PathPoint> endPoints = new Pair<>(new PathPoint(), new PathPoint());
      endPoints.first.translate(startBlockCenterX + originX, 0);
      endPoints.second.translate(endBlockCenterX + originX, 0);
      if (startBlockCenterX >= endBlockCenterX) {
        // Draws the start rounded block clipped by the end rounded block.
        drawRoundedBlock(
            canvas, paint, endPoints.first, endPoints.second, blockWidth, displayedTrackThickness);
      } else {
        // Draws the path with ROUND cap if the corner radius is half of the track
        // thickness.
        paint.setStyle(Style.STROKE);
        paint.setStrokeCap(useStrokeCap ? Cap.ROUND : Cap.BUTT);

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
          endPoints =
              getDisplayedPath(
                  activePathMeasure,
                  displayedActivePath,
                  startBlockCenterX / trackLength,
                  endBlockCenterX / trackLength,
                  amplitudeFraction,
                  phaseFraction);
          canvas.drawPath(displayedActivePath, paint);
        }
        if (!useStrokeCap && displayedCornerRadius > 0) {
          if (startBlockCenterX > 0) {
            // Draws the start rounded block.
            drawRoundedBlock(canvas, paint, endPoints.first, blockWidth, displayedTrackThickness);
          }
          if (endBlockCenterX < trackLength) {
            // Draws the end rounded block.
            drawRoundedBlock(canvas, paint, endPoints.second, blockWidth, displayedTrackThickness);
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
    if (spec.trackStopIndicatorSize > 0 && paintColor != Color.TRANSPARENT) {
      // Draws the stop indicator at the end of the track if needed.
      paint.setStyle(Style.FILL);
      paint.setColor(paintColor);
      drawRoundedBlock(
          canvas,
          paint,
          new PathPoint(
              new float[] {trackLength / 2 - displayedTrackThickness / 2, 0}, new float[] {1, 0}),
          spec.trackStopIndicatorSize,
          spec.trackStopIndicatorSize);
    }
  }

  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull PathPoint drawCenter,
      float markWidth,
      float markHeight) {
    drawRoundedBlock(canvas, paint, drawCenter, null, markWidth, markHeight);
  }

  private void drawRoundedBlock(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull PathPoint drawCenter,
      @Nullable PathPoint clipCenter,
      float markWidth,
      float markHeight) {
    markHeight = min(markHeight, displayedTrackThickness);
    float markCornerSize = markHeight * displayedCornerRadius / displayedTrackThickness;
    markCornerSize = min(markWidth / 2, markCornerSize);
    RectF roundedBlock =
        new RectF(-markWidth / 2f, -markHeight / 2f, markWidth / 2f, markHeight / 2f);
    paint.setStyle(Style.FILL);
    canvas.save();
    if (clipCenter != null) {
      // Clipping!
      canvas.translate(clipCenter.posVec[0], clipCenter.posVec[1]);
      canvas.rotate(vectorToCanvasRotation(clipCenter.tanVec));
      Path clipPath = new Path();
      clipPath.addRoundRect(roundedBlock, markCornerSize, markCornerSize, Direction.CCW);
      canvas.clipPath(clipPath);
      canvas.rotate(-vectorToCanvasRotation(clipCenter.tanVec));
      canvas.translate(-clipCenter.posVec[0], -clipCenter.posVec[1]);
    }
    canvas.translate(drawCenter.posVec[0], drawCenter.posVec[1]);
    canvas.rotate(vectorToCanvasRotation(drawCenter.tanVec));
    canvas.drawRoundRect(roundedBlock, markCornerSize, markCornerSize, paint);
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
      Matrix transformMatrix = new Matrix();
      transformMatrix.setScale(adjustedWavelength / 2, -2);
      transformMatrix.postTranslate(0, 1);
      cachedActivePath.transform(transformMatrix);
    } else {
      cachedActivePath.lineTo(trackLength, 0);
    }
    activePathMeasure.setPath(cachedActivePath, /* forceNewPath= */ false);
  }

  @NonNull
  private Pair<PathPoint, PathPoint> getDisplayedPath(
      @NonNull PathMeasure pathMeasure,
      @NonNull Path displayedPath,
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
    PathPoint startPoint = new PathPoint();
    pathMeasure.getPosTan(startDistance, startPoint.posVec, startPoint.tanVec);
    PathPoint endPoint = new PathPoint();
    pathMeasure.getPosTan(endDistance, endPoint.posVec, endPoint.tanVec);
    // Transforms the result path to match the canvas.
    Matrix transform = new Matrix();
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
    return new Pair<>(startPoint, endPoint);
  }
}
