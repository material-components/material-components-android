/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.shadow;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.Shader;
import android.graphics.Shader.TileMode;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.ColorUtils;

/**
 * A helper class to draw linear or radial shadows using gradient shaders.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ShadowRenderer {

  /** Gradient start color of 68 which evaluates to approximately 26% opacity. */
  private static final int COLOR_ALPHA_START = 0x44;
  /** Gradient start color of 20 which evaluates to approximately 8% opacity. */
  private static final int COLOR_ALPHA_MIDDLE = 0x14;

  private static final int COLOR_ALPHA_END = 0;

  @NonNull private final Paint shadowPaint;
  @NonNull private final Paint cornerShadowPaint;
  @NonNull private final Paint edgeShadowPaint;

  private int shadowStartColor;
  private int shadowMiddleColor;
  private int shadowEndColor;

  private static final int[] edgeColors = new int[3];
  /** Start, middle of shadow, and end of shadow positions */
  private static final float[] edgePositions = new float[] {0f, .5f, 1f};

  private static final int[] cornerColors = new int[4];
  /** Start, beginning of corner, middle of shadow, and end of shadow positions */
  private static final float[] cornerPositions = new float[] {0f, 0f, .5f, 1f};

  private final Path scratch = new Path();
  private final Paint transparentPaint = new Paint();

  public ShadowRenderer() {
    this(Color.BLACK);
  }

  public ShadowRenderer(int color) {
    shadowPaint = new Paint();
    setShadowColor(color);

    transparentPaint.setColor(Color.TRANSPARENT);
    cornerShadowPaint = new Paint(Paint.DITHER_FLAG);
    cornerShadowPaint.setStyle(Paint.Style.FILL);

    edgeShadowPaint = new Paint(cornerShadowPaint);
  }

  public void setShadowColor(int color) {
    shadowStartColor = ColorUtils.setAlphaComponent(color, COLOR_ALPHA_START);
    shadowMiddleColor = ColorUtils.setAlphaComponent(color, COLOR_ALPHA_MIDDLE);
    shadowEndColor = ColorUtils.setAlphaComponent(color, COLOR_ALPHA_END);
    shadowPaint.setColor(shadowStartColor);
  }

  /** Draws an edge shadow on the canvas in the current bounds with the matrix transform applied. */
  public void drawEdgeShadow(
      @NonNull Canvas canvas, @Nullable Matrix transform, @NonNull RectF bounds, int elevation) {
    bounds.bottom += elevation;
    bounds.offset(0, -elevation);

    edgeColors[0] = shadowEndColor;
    edgeColors[1] = shadowMiddleColor;
    edgeColors[2] = shadowStartColor;

    edgeShadowPaint.setShader(
        new LinearGradient(
            bounds.left,
            bounds.top,
            bounds.left,
            bounds.bottom,
            edgeColors,
            edgePositions,
            Shader.TileMode.CLAMP));

    canvas.save();
    canvas.concat(transform);
    canvas.drawRect(bounds, edgeShadowPaint);
    canvas.restore();
  }

  /**
   * Draws a corner shadow on the canvas in the current bounds with the matrix transform applied.
   */
  public void drawCornerShadow(
      @NonNull Canvas canvas,
      @Nullable Matrix matrix,
      @NonNull RectF bounds,
      int elevation,
      float startAngle,
      float sweepAngle) {

    boolean drawShadowInsideBounds = sweepAngle < 0;

    Path arcBounds = scratch;

    if (drawShadowInsideBounds) {
      cornerColors[0] = 0;
      cornerColors[1] = shadowEndColor;
      cornerColors[2] = shadowMiddleColor;
      cornerColors[3] = shadowStartColor;
    } else {
      // Calculate the arc bounds to prevent drawing shadow in the same part of the arc.
      arcBounds.rewind();
      arcBounds.moveTo(bounds.centerX(), bounds.centerY());
      arcBounds.arcTo(bounds, startAngle, sweepAngle);
      arcBounds.close();

      bounds.inset(-elevation, -elevation);
      cornerColors[0] = 0;
      cornerColors[1] = shadowStartColor;
      cornerColors[2] = shadowMiddleColor;
      cornerColors[3] = shadowEndColor;
    }

    float radius = bounds.width() / 2f;
    // The shadow is not big enough to draw.
    if (radius <= 0) {
      return;
    }

    float startRatio = 1f - (elevation / radius);
    float midRatio = startRatio + ((1f - startRatio) / 2f);
    cornerPositions[1] = startRatio;
    cornerPositions[2] = midRatio;
    RadialGradient shader =
        new RadialGradient(
            bounds.centerX(),
            bounds.centerY(),
            radius,
            cornerColors,
            cornerPositions,
            TileMode.CLAMP);
    cornerShadowPaint.setShader(shader);
    canvas.save();
    canvas.concat(matrix);
    canvas.scale(1, bounds.height() / bounds.width());

    if (!drawShadowInsideBounds) {
      canvas.clipPath(arcBounds, Op.DIFFERENCE);
      // This line is required for the next drawArc to work correctly, I think.
      canvas.drawPath(arcBounds, transparentPaint);
    }

    canvas.drawArc(bounds, startAngle, sweepAngle, true, cornerShadowPaint);
    canvas.restore();
  }

  public void drawInnerCornerShadow(
      @NonNull Canvas canvas,
      @Nullable Matrix matrix,
      @NonNull RectF bounds,
      int elevation,
      float startAngle,
      float sweepAngle,
      @NonNull float[] cornerPosition) {
    // Draws the radial gradient corner shadow.
    if (sweepAngle > 0) {
      startAngle += sweepAngle;
      sweepAngle = -sweepAngle;
    }
    drawCornerShadow(canvas, matrix, bounds, elevation, startAngle, sweepAngle);
    // Draws the patched area between the corner and the arc.
    Path shapeBounds = scratch;
    shapeBounds.rewind();
    shapeBounds.moveTo(cornerPosition[0], cornerPosition[1]);
    shapeBounds.arcTo(bounds, startAngle, sweepAngle);
    shapeBounds.close();

    canvas.save();
    canvas.concat(matrix);
    canvas.scale(1, bounds.height() / bounds.width());

    canvas.drawPath(shapeBounds, transparentPaint);
    canvas.drawPath(shapeBounds, shadowPaint);
    canvas.restore();
  }

  @NonNull
  public Paint getShadowPaint() {
    return shadowPaint;
  }
}
