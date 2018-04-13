/*
 * Copyright (C) 2015 The Android Open Source Project
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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.support.annotation.RestrictTo;
import android.support.v4.graphics.ColorUtils;

/** A drawable which draws an oval 'border'. */
@RestrictTo(LIBRARY_GROUP)
public class CircularBorderDrawable extends Drawable {

  /**
   * We actually draw the stroke wider than the border size given. This is to reduce any potential
   * transparent space caused by anti-aliasing and padding rounding. This value defines the
   * multiplier used to determine to draw stroke width.
   */
  private static final float DRAW_STROKE_WIDTH_MULTIPLE = 1.3333f;

  final Paint paint;
  final Rect rect = new Rect();
  final RectF rectF = new RectF();

  float borderWidth;

  private int topOuterStrokeColor;
  private int topInnerStrokeColor;
  private int bottomOuterStrokeColor;
  private int bottomInnerStrokeColor;

  private ColorStateList borderTint;
  private int currentBorderTintColor;

  private boolean invalidateShader = true;

  private float rotation;

  public CircularBorderDrawable() {
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.STROKE);
  }

  public void setGradientColors(
      int topOuterStrokeColor,
      int topInnerStrokeColor,
      int bottomOuterStrokeColor,
      int bottomInnerStrokeColor) {
    this.topOuterStrokeColor = topOuterStrokeColor;
    this.topInnerStrokeColor = topInnerStrokeColor;
    this.bottomOuterStrokeColor = bottomOuterStrokeColor;
    this.bottomInnerStrokeColor = bottomInnerStrokeColor;
  }

  /** Set the border width */
  public void setBorderWidth(float width) {
    if (borderWidth != width) {
      borderWidth = width;
      paint.setStrokeWidth(width * DRAW_STROKE_WIDTH_MULTIPLE);
      invalidateShader = true;
      invalidateSelf();
    }
  }

  @Override
  public void draw(Canvas canvas) {
    if (invalidateShader) {
      paint.setShader(createGradientShader());
      invalidateShader = false;
    }

    final float halfBorderWidth = paint.getStrokeWidth() / 2f;
    final RectF rectF = this.rectF;

    // We need to inset the oval bounds by half the border width. This is because stroke draws
    // the center of the border on the dimension. Whereas we want the stroke on the inside.
    copyBounds(rect);
    rectF.set(rect);
    rectF.left += halfBorderWidth;
    rectF.top += halfBorderWidth;
    rectF.right -= halfBorderWidth;
    rectF.bottom -= halfBorderWidth;

    canvas.save();
    canvas.rotate(rotation, rectF.centerX(), rectF.centerY());
    // Draw the oval
    canvas.drawOval(rectF, paint);
    canvas.restore();
  }

  @Override
  public boolean getPadding(Rect padding) {
    final int borderWidth = Math.round(this.borderWidth);
    padding.set(borderWidth, borderWidth, borderWidth, borderWidth);
    return true;
  }

  @Override
  public void setAlpha(int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  public void setBorderTint(ColorStateList tint) {
    if (tint != null) {
      currentBorderTintColor = tint.getColorForState(getState(), currentBorderTintColor);
    }
    borderTint = tint;
    invalidateShader = true;
    invalidateSelf();
  }

  @Override
  public void setColorFilter(ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return borderWidth > 0 ? PixelFormat.TRANSLUCENT : PixelFormat.TRANSPARENT;
  }

  public final void setRotation(float rotation) {
    if (rotation != this.rotation) {
      this.rotation = rotation;
      invalidateSelf();
    }
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    invalidateShader = true;
  }

  @Override
  public boolean isStateful() {
    return (borderTint != null && borderTint.isStateful()) || super.isStateful();
  }

  @Override
  protected boolean onStateChange(int[] state) {
    if (borderTint != null) {
      final int newColor = borderTint.getColorForState(state, currentBorderTintColor);
      if (newColor != currentBorderTintColor) {
        invalidateShader = true;
        currentBorderTintColor = newColor;
      }
    }
    if (invalidateShader) {
      invalidateSelf();
    }
    return invalidateShader;
  }

  /**
   * Creates a vertical {@link LinearGradient}
   *
   * @return
   */
  private Shader createGradientShader() {
    final Rect rect = this.rect;
    copyBounds(rect);

    final float borderRatio = borderWidth / rect.height();

    final int[] colors = new int[6];
    colors[0] = ColorUtils.compositeColors(topOuterStrokeColor, currentBorderTintColor);
    colors[1] = ColorUtils.compositeColors(topInnerStrokeColor, currentBorderTintColor);
    colors[2] =
        ColorUtils.compositeColors(
            ColorUtils.setAlphaComponent(topInnerStrokeColor, 0), currentBorderTintColor);
    colors[3] =
        ColorUtils.compositeColors(
            ColorUtils.setAlphaComponent(bottomInnerStrokeColor, 0), currentBorderTintColor);
    colors[4] = ColorUtils.compositeColors(bottomInnerStrokeColor, currentBorderTintColor);
    colors[5] = ColorUtils.compositeColors(bottomOuterStrokeColor, currentBorderTintColor);

    final float[] positions = new float[6];
    positions[0] = 0f;
    positions[1] = borderRatio;
    positions[2] = 0.5f;
    positions[3] = 0.5f;
    positions[4] = 1f - borderRatio;
    positions[5] = 1f;

    return new LinearGradient(
        0, rect.top, 0, rect.bottom, colors, positions, Shader.TileMode.CLAMP);
  }
}
