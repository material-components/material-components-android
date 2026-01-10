/*
 * Copyright (C) 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.loadingindicator;

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.graphics.shapes.Morph;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.Shapes_androidKt;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.math.MathUtils;
import com.google.android.material.shape.MaterialShapes;

class LoadingIndicatorDrawingDelegate {

  @NonNull LoadingIndicatorSpec specs;
  @NonNull final Path indicatorPath = new Path();
  @NonNull final Matrix indicatorPathTransform = new Matrix();

  public LoadingIndicatorDrawingDelegate(@NonNull LoadingIndicatorSpec specs) {
    this.specs = specs;
  }

  /**
   * Returns the preferred width, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  int getPreferredWidth() {
    return max(specs.containerHeight, specs.indicatorSize);
  }

  /**
   * Returns the preferred height, in pixels, of the drawable based on the drawing type. Returns a
   * negative value if it depends on the {@link android.view.View}.
   */
  int getPreferredHeight() {
    return max(specs.containerWidth, specs.indicatorSize);
  }

  void adjustCanvas(@NonNull Canvas canvas, @NonNull Rect bounds) {
    // Moves the origin (0, 0) of the canvas to the center of the container.
    canvas.translate(bounds.centerX(), bounds.centerY());
    if (specs.scaleToFit) {
      // Scales the actual drawing by the ratio of the given bounds to the preferred size, while
      // keeping the aspect ratio.
      float scaleX = (float) bounds.width() / getPreferredWidth();
      float scaleY = (float) bounds.height() / getPreferredHeight();
      float scale = min(scaleX, scaleY);
      canvas.scale(scale, scale);
    }

    // Clip all drawing to the designated area, so it doesn't draw outside of its bounds (which can
    // happen in certain configuration of clipToPadding and clipChildren)
    canvas.clipRect(
        -getPreferredWidth() / 2f,
        -getPreferredHeight() / 2f,
        getPreferredWidth() / 2f,
        getPreferredHeight() / 2f);

    // Rotates canvas 90 degrees so that 0 degree at the top.
    canvas.rotate(-90f);
  }

  void drawContainer(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    float radius = min(specs.containerWidth, specs.containerHeight) / 2f;
    color = MaterialColors.compositeARGBWithAlpha(color, drawableAlpha);
    paint.setColor(color);
    paint.setStyle(Style.FILL);
    canvas.drawRoundRect(
        new RectF(
            -specs.containerWidth / 2f,
            -specs.containerHeight / 2f,
            specs.containerWidth / 2f,
            specs.containerHeight / 2f),
        radius,
        radius,
        paint);
  }

  void drawIndicator(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @NonNull IndicatorState indicatorState,
      @IntRange(from = 0, to = 255) int drawableAlpha) {
    int color = MaterialColors.compositeARGBWithAlpha(indicatorState.color, drawableAlpha);
    paint.setColor(color);
    paint.setStyle(Style.FILL);
    canvas.save();
    canvas.rotate(indicatorState.rotationDegree);
    // Draws the shape morph.
    indicatorPath.rewind();
    int shapeMorphFraction = (int) Math.floor(indicatorState.morphFraction);
    int fractionAmongAllShapes =
        MathUtils.floorMod(shapeMorphFraction, INDETERMINATE_MORPH_SEQUENCE.length);
    float fractionPerShape = indicatorState.morphFraction - shapeMorphFraction;
    Shapes_androidKt.toPath(
        INDETERMINATE_MORPH_SEQUENCE[fractionAmongAllShapes], fractionPerShape, indicatorPath);
    // We need to apply the scaling to the path directly, instead of on the canvas, to avoid the
    // limitation of hardware accelerated rendering.
    indicatorPathTransform.setScale(specs.indicatorSize / 2f, specs.indicatorSize / 2f);
    indicatorPath.transform(indicatorPathTransform);
    canvas.drawPath(indicatorPath, paint);
    canvas.restore();
  }

  private static final RoundedPolygon[] INDETERMINATE_SHAPES =
      new RoundedPolygon[] {
        MaterialShapes.normalize(MaterialShapes.SOFT_BURST, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.COOKIE_9, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.PENTAGON, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.PILL, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.SUNNY, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.COOKIE_4, true, new RectF(-1, -1, 1, 1)),
        MaterialShapes.normalize(MaterialShapes.OVAL, true, new RectF(-1, -1, 1, 1))
      };

  private static final Morph[] INDETERMINATE_MORPH_SEQUENCE =
      new Morph[INDETERMINATE_SHAPES.length];

  static {
    for (int i = 0; i < INDETERMINATE_SHAPES.length; i++) {
      INDETERMINATE_MORPH_SEQUENCE[i] =
          new Morph(
              INDETERMINATE_SHAPES[i], INDETERMINATE_SHAPES[(i + 1) % INDETERMINATE_SHAPES.length]);
    }
  }

  protected static class IndicatorState {

    // The color of the indicator without applying the drawable's alpha.
    @ColorInt int color;

    // The fraction controlling the shape morph.
    float morphFraction;

    // Initial rotation applied on the indicator in degrees.
    float rotationDegree;
  }
}
