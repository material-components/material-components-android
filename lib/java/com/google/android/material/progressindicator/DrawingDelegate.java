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

import static java.lang.Math.PI;
import static java.lang.Math.atan2;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.toDegrees;
import static java.lang.System.arraycopy;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import java.util.Arrays;

/** A delegate abstract class for drawing the graphics in different drawable classes. */
abstract class DrawingDelegate<S extends BaseProgressIndicatorSpec> {
  // The length of the control handles of the cubic bezier curve simulating half cycle (from peak to
  // trough) with wavelength = 2.
  static final float WAVE_SMOOTHNESS = 0.48f;

  S spec;

  final Path cachedActivePath = new Path();
  final Path displayedActivePath = new Path();

  final PathMeasure activePathMeasure = new PathMeasure(cachedActivePath, /* forceClosed= */ false);

  // Pre-allocates a Matrix to transform this point.
  final Matrix transform;

  public DrawingDelegate(S spec) {
    this.spec = spec;
    this.transform = new Matrix();
  }

  /**
   * Returns the preferred width, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  abstract int getPreferredWidth();

  /**
   * Returns the preferred height, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  abstract int getPreferredHeight();

  /**
   * Prepares the bound of the canvas for the actual drawing. Should be called before any drawing
   * (per frame).
   *
   * @param canvas Canvas to draw
   * @param bounds Bounds that the drawable is supposed to be drawn within
   * @param trackThicknessFraction A fraction representing how much portion of the track thickness
   *     should be used in the drawing
   * @param isShowing Whether the drawable is currently animating to show
   * @param isHiding Whether the drawable is currently animating to hide
   */
  abstract void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull Rect bounds,
      @FloatRange(from = -1.0, to = 1.0) float trackThicknessFraction,
      boolean isShowing,
      boolean isHiding);

  /**
   * Fills a part of the track as an active indicator.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param activeIndicator The ActiveIndicator object of the current active indicator being drawn.
   * @param drawableAlpha The alpha [0, 255] from the caller drawable.
   */
  abstract void fillIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull ActiveIndicator activeIndicator,
      @IntRange(from = 0, to = 255) int drawableAlpha);

  /**
   * Fills a part of the track with specified parameters.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param color The color used to draw the part without applying the alpha from drawable.
   * @param drawableAlpha The alpha [0, 255] from the caller drawable.
   * @param gapSize The size of the gap applied on the ends of the drawn part.
   */
  abstract void fillTrack(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha,
      @Px int gapSize);

  /**
   * Draws the stop indicator on the track. Only implemented in linear type.
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param color The color used to draw the part without applying the alpha from drawable.
   * @param drawableAlpha The alpha [0, 255] from the caller drawable.
   */
  abstract void drawStopIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha);

  void validateSpecAndAdjustCanvas(
      @NonNull Canvas canvas,
      @NonNull Rect bounds,
      @FloatRange(from = 0.0, to = 1.0) float trackThicknessFraction,
      boolean isShowing,
      boolean isHiding) {
    spec.validateSpec();
    adjustCanvas(canvas, bounds, trackThicknessFraction, isShowing, isHiding);
  }

  /** Recreate cached paths based on existing spec. */
  abstract void invalidateCachedPaths();

  /** Return the degrees to rotate the canvas, so that x+ aligns with the vector. */
  float vectorToCanvasRotation(float[] vector) {
    return (float) toDegrees(atan2(vector[1], vector[0]));
  }

  protected static class ActiveIndicator {
    // The fraction [0, 1] of the start position on the full track.
    @FloatRange(from = 0.0, to = 1.0)
    float startFraction;

    // The fraction [0, 1] of the end position on the full track.
    @FloatRange(from = 0.0, to = 1.0)
    float endFraction;

    // The color of the indicator without applying the drawable's alpha.
    @ColorInt int color;

    // Additional gap size around active indicator. Usually we don't need to consider gaps around
    // active indicator. But for linear contiguous indeterminate mode, the indicators are connecting
    // to each other. Gaps are needed in this case.
    @Px int gapSize;

    // The fraction [0, 1] of the amplitude on indicator.
    @FloatRange(from = 0.0, to = 1.0)
    float amplitudeFraction = 1;

    // The fraction [0, 1] of the initial phase [0, 2 * PI] on indicator.
    @FloatRange(from = 0.0, to = 1.0)
    float phaseFraction;

    // Initial rotation applied on the indicator in degrees.
    float rotationDegree;

    // Whether the indicator is for determinate mode.
    boolean isDeterminate;
  }

  /** An entity class for a point on a path, with the support of fundamental operations. */
  protected class PathPoint {
    // The vector to the position of the point.
    float[] posVec = new float[2];
    // The tangent vector of this point on a path. The length is not guaranteed.
    float[] tanVec = new float[2];
    // Pre-allocates a Matrix for transform this point.
    final Matrix transform;

    public PathPoint() {
      tanVec[0] = 1;
      transform = new Matrix();
    }

    public PathPoint(PathPoint other) {
      this(other.posVec, other.tanVec);
    }

    public PathPoint(float[] pos, float[] tan) {
      arraycopy(pos, 0, this.posVec, 0, 2);
      arraycopy(tan, 0, this.tanVec, 0, 2);
      transform = new Matrix();
    }

    /** Moves this point by (x, y). */
    void translate(float x, float y) {
      posVec[0] += x;
      posVec[1] += y;
    }

    /** Updates the coordinates by scaling the path at (0, 0). */
    void scale(float x, float y) {
      posVec[0] *= x;
      posVec[1] *= y;
      tanVec[0] *= x;
      tanVec[1] *= y;
    }

    /** Returns the distance between this point and the other. */
    float distance(PathPoint other) {
      return (float) Math.hypot(other.posVec[0] - posVec[0], other.posVec[1] - posVec[1]);
    }

    /** Updates the coordinates by moving the point along tangent vector by the given distance. */
    void moveAlong(float distance) {
      float angle = (float) atan2(tanVec[1], tanVec[0]);
      posVec[0] = (float) (posVec[0] + distance * cos(angle));
      posVec[1] = (float) (posVec[1] + distance * sin(angle));
    }

    /**
     * Updates the coordinates by moving the point across the tangent vector by the given distance.
     * If the given distance is positive, the point is moved to the right side by facing towards the
     * tangent vector; otherwise, to the left side.
     */
    void moveAcross(float distance) {
      float angle = (float) (atan2(tanVec[1], tanVec[0]) + PI / 2);
      posVec[0] = (float) (posVec[0] + distance * cos(angle));
      posVec[1] = (float) (posVec[1] + distance * sin(angle));
    }

    /** Rotates the coordinates by the given degrees around (0, 0). */
    public void rotate(float rotationDegrees) {
      transform.reset();
      transform.setRotate(rotationDegrees);
      transform.mapPoints(posVec);
      transform.mapPoints(tanVec);
    }

    public void reset() {
      Arrays.fill(posVec, 0);
      Arrays.fill(tanVec, 0);
      tanVec[0] = 1;
      transform.reset();
    }
  }
}
