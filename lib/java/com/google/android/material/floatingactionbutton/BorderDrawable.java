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

package com.google.android.material.floatingactionbutton;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.LinearGradient;
import android.graphics.Outline;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.ColorUtils;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;

/**
 * A Drawable that draws borders for {@link FloatingActionButton}
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
class BorderDrawable extends Drawable {

  /**
   * We actually draw the stroke wider than the border size given. This is to reduce any potential
   * transparent space caused by anti-aliasing and padding rounding. This value defines the
   * multiplier used to determine to draw stroke width.
   */
  private static final float DRAW_STROKE_WIDTH_MULTIPLE = 1.3333f;

  private final ShapeAppearancePathProvider pathProvider =
      ShapeAppearancePathProvider.getInstance();

  @NonNull private final Paint paint;
  private final Path shapePath = new Path();
  private final Rect rect = new Rect();
  private final RectF rectF = new RectF();
  private final RectF boundsRectF = new RectF();
  private final BorderState state = new BorderState();

  @Dimension float borderWidth;
  @ColorInt private int topOuterStrokeColor;
  @ColorInt private int topInnerStrokeColor;
  @ColorInt private int bottomOuterStrokeColor;
  @ColorInt private int bottomInnerStrokeColor;
  @ColorInt private int currentBorderTintColor;

  private boolean invalidateShader = true;
  private ShapeAppearanceModel shapeAppearanceModel;

  @Nullable private ColorStateList borderTint;

  BorderDrawable(ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(Paint.Style.STROKE);
  }

  public void setBorderWidth(@Dimension float width) {
    if (borderWidth != width) {
      borderWidth = width;
      paint.setStrokeWidth(width * DRAW_STROKE_WIDTH_MULTIPLE);
      invalidateShader = true;
      invalidateSelf();
    }
  }

  void setBorderTint(@Nullable ColorStateList tint) {
    if (tint != null) {
      currentBorderTintColor = tint.getColorForState(getState(), currentBorderTintColor);
    }
    borderTint = tint;
    invalidateShader = true;
    invalidateSelf();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  void setGradientColors(
      @ColorInt int topOuterStrokeColor,
      @ColorInt int topInnerStrokeColor,
      @ColorInt int bottomOuterStrokeColor,
      @ColorInt int bottomInnerStrokeColor) {
    this.topOuterStrokeColor = topOuterStrokeColor;
    this.topInnerStrokeColor = topInnerStrokeColor;
    this.bottomOuterStrokeColor = bottomOuterStrokeColor;
    this.bottomInnerStrokeColor = bottomInnerStrokeColor;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (invalidateShader) {
      paint.setShader(createGradientShader());
      invalidateShader = false;
    }

    final float halfBorderWidth = paint.getStrokeWidth() / 2f;
    copyBounds(rect);
    rectF.set(rect);

    // We need to inset the oval bounds by half the border width. This is because stroke draws
    // the center of the border on the dimension. Whereas we want the stroke on the inside.
    float cornerSize =
        shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(getBoundsAsRectF());
    float radius = Math.min(cornerSize, rectF.width() / 2f);
    if (shapeAppearanceModel.isRoundRect(getBoundsAsRectF())) {
      rectF.inset(halfBorderWidth, halfBorderWidth);
      canvas.drawRoundRect(rectF, radius, radius, paint);
    }
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void getOutline(@NonNull Outline outline) {
    if (shapeAppearanceModel.isRoundRect(getBoundsAsRectF())) {
      float radius = shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(getBoundsAsRectF());
      outline.setRoundRect(getBounds(), radius);
      return;
    }

    copyBounds(rect);
    rectF.set(rect);
    pathProvider.calculatePath(shapeAppearanceModel, 1f, rectF, shapePath);
    if (shapePath.isConvex()) {
      outline.setConvexPath(shapePath);
    }
  }

  @Override
  public boolean getPadding(@NonNull Rect padding) {
    if (shapeAppearanceModel.isRoundRect(getBoundsAsRectF())) {
      final int borderWidth = Math.round(this.borderWidth);
      padding.set(borderWidth, borderWidth, borderWidth, borderWidth);
    }
    return true;
  }

  @NonNull
  protected RectF getBoundsAsRectF() {
    boundsRectF.set(getBounds());
    return boundsRectF;
  }

  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
  }

  public void setShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    invalidateSelf();
  }

  @Override
  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    paint.setAlpha(alpha);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return borderWidth > 0 ? PixelFormat.TRANSLUCENT : PixelFormat.TRANSPARENT;
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

  @NonNull
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

  @Nullable
  @Override
  public ConstantState getConstantState() {
    return state;
  }

  /**
   * Dummy implementation of constant state. This drawable doesn't have shared state. Implementing
   * so that calls to getConstantState().newDrawable() don't crash on L and M.
   */
  private class BorderState extends ConstantState {

    @NonNull
    @Override
    public Drawable newDrawable() {
      return BorderDrawable.this;
    }

    @Override
    public int getChangingConfigurations() {
      return 0;
    }
  }
}
