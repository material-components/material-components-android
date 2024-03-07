/*
 * Copyright (C) 2014 The Android Open Source Project
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

import com.google.android.material.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import androidx.appcompat.graphics.drawable.DrawableWrapperCompat;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

/**
 * A {@link android.graphics.drawable.Drawable} which wraps another drawable and draws a shadow
 * around it.
 *
 * @deprecated Use {@link com.google.android.material.shape.MaterialShapeDrawable} instead.
 */
@Deprecated
public class ShadowDrawableWrapper extends DrawableWrapperCompat {
  // used to calculate content padding
  static final double COS_45 = Math.cos(Math.toRadians(45));

  static final float SHADOW_MULTIPLIER = 1.5f;

  static final float SHADOW_TOP_SCALE = 0.25f;
  static final float SHADOW_HORIZ_SCALE = 0.5f;
  static final float SHADOW_BOTTOM_SCALE = 1f;

  @NonNull final Paint cornerShadowPaint;
  @NonNull final Paint edgeShadowPaint;

  @NonNull final RectF contentBounds;

  float cornerRadius;

  Path cornerShadowPath;

  // updated value with inset
  float maxShadowSize;
  // actual value set by developer
  float rawMaxShadowSize;

  // multiplied value to account for shadow offset
  float shadowSize;
  // actual value set by developer
  float rawShadowSize;

  private boolean dirty = true;

  private final int shadowStartColor;
  private final int shadowMiddleColor;
  private final int shadowEndColor;

  private boolean addPaddingForCorners = true;

  private float rotation;

  /** If shadow size is set to a value above max shadow, we print a warning */
  private boolean printedShadowClipWarning = false;

  public ShadowDrawableWrapper(
      Context context, Drawable content, float radius, float shadowSize, float maxShadowSize) {
    super(content);

    shadowStartColor = ContextCompat.getColor(context, R.color.design_fab_shadow_start_color);
    shadowMiddleColor = ContextCompat.getColor(context, R.color.design_fab_shadow_mid_color);
    shadowEndColor = ContextCompat.getColor(context, R.color.design_fab_shadow_end_color);

    cornerShadowPaint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
    cornerShadowPaint.setStyle(Paint.Style.FILL);
    cornerRadius = Math.round(radius);
    contentBounds = new RectF();
    edgeShadowPaint = new Paint(cornerShadowPaint);
    edgeShadowPaint.setAntiAlias(false);
    setShadowSize(shadowSize, maxShadowSize);
  }

  /** Casts the value to an even integer. */
  private static int toEven(float value) {
    int i = Math.round(value);
    return (i % 2 == 1) ? i - 1 : i;
  }

  public void setAddPaddingForCorners(boolean addPaddingForCorners) {
    this.addPaddingForCorners = addPaddingForCorners;
    invalidateSelf();
  }

  @Override
  public void setAlpha(int alpha) {
    super.setAlpha(alpha);
    cornerShadowPaint.setAlpha(alpha);
    edgeShadowPaint.setAlpha(alpha);
  }

  @Override
  protected void onBoundsChange(Rect bounds) {
    dirty = true;
  }

  public void setShadowSize(float shadowSize, float maxShadowSize) {
    if (shadowSize < 0 || maxShadowSize < 0) {
      throw new IllegalArgumentException("invalid shadow size");
    }
    shadowSize = toEven(shadowSize);
    maxShadowSize = toEven(maxShadowSize);
    if (shadowSize > maxShadowSize) {
      shadowSize = maxShadowSize;
      if (!printedShadowClipWarning) {
        printedShadowClipWarning = true;
      }
    }
    if (rawShadowSize == shadowSize && rawMaxShadowSize == maxShadowSize) {
      return;
    }
    rawShadowSize = shadowSize;
    rawMaxShadowSize = maxShadowSize;
    this.shadowSize = Math.round(shadowSize * SHADOW_MULTIPLIER);
    this.maxShadowSize = maxShadowSize;
    dirty = true;
    invalidateSelf();
  }

  public void setShadowSize(float size) {
    setShadowSize(size, rawMaxShadowSize);
  }

  public float getShadowSize() {
    return rawShadowSize;
  }

  @Override
  public boolean getPadding(@NonNull Rect padding) {
    int vOffset =
        (int)
            Math.ceil(
                calculateVerticalPadding(rawMaxShadowSize, cornerRadius, addPaddingForCorners));
    int hOffset =
        (int)
            Math.ceil(
                calculateHorizontalPadding(rawMaxShadowSize, cornerRadius, addPaddingForCorners));
    padding.set(hOffset, vOffset, hOffset, vOffset);
    return true;
  }

  public static float calculateVerticalPadding(
      float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
    if (addPaddingForCorners) {
      return (float) (maxShadowSize * SHADOW_MULTIPLIER + (1 - COS_45) * cornerRadius);
    } else {
      return maxShadowSize * SHADOW_MULTIPLIER;
    }
  }

  public static float calculateHorizontalPadding(
      float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
    if (addPaddingForCorners) {
      return (float) (maxShadowSize + (1 - COS_45) * cornerRadius);
    } else {
      return maxShadowSize;
    }
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  public void setCornerRadius(float radius) {
    radius = Math.round(radius);
    if (cornerRadius == radius) {
      return;
    }
    cornerRadius = radius;
    dirty = true;
    invalidateSelf();
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    if (dirty) {
      buildComponents(getBounds());
      dirty = false;
    }
    drawShadow(canvas);

    super.draw(canvas);
  }

  public final void setRotation(float rotation) {
    if (this.rotation != rotation) {
      this.rotation = rotation;
      invalidateSelf();
    }
  }

  private void drawShadow(@NonNull Canvas canvas) {
    final int rotateSaved = canvas.save();
    canvas.rotate(rotation, contentBounds.centerX(), contentBounds.centerY());

    final float edgeShadowTop = -cornerRadius - shadowSize;
    final float shadowOffset = cornerRadius;
    final boolean drawHorizontalEdges = contentBounds.width() - 2 * shadowOffset > 0;
    final boolean drawVerticalEdges = contentBounds.height() - 2 * shadowOffset > 0;

    final float shadowOffsetTop = rawShadowSize - (rawShadowSize * SHADOW_TOP_SCALE);
    final float shadowOffsetHorizontal = rawShadowSize - (rawShadowSize * SHADOW_HORIZ_SCALE);
    final float shadowOffsetBottom = rawShadowSize - (rawShadowSize * SHADOW_BOTTOM_SCALE);

    final float shadowScaleHorizontal = shadowOffset / (shadowOffset + shadowOffsetHorizontal);
    final float shadowScaleTop = shadowOffset / (shadowOffset + shadowOffsetTop);
    final float shadowScaleBottom = shadowOffset / (shadowOffset + shadowOffsetBottom);

    // LT
    int saved = canvas.save();
    canvas.translate(contentBounds.left + shadowOffset, contentBounds.top + shadowOffset);
    canvas.scale(shadowScaleHorizontal, shadowScaleTop);
    canvas.drawPath(cornerShadowPath, cornerShadowPaint);
    if (drawHorizontalEdges) {
      // TE
      canvas.scale(1f / shadowScaleHorizontal, 1f);
      canvas.drawRect(
          0,
          edgeShadowTop,
          contentBounds.width() - 2 * shadowOffset,
          -cornerRadius,
          edgeShadowPaint);
    }
    canvas.restoreToCount(saved);
    // RB
    saved = canvas.save();
    canvas.translate(contentBounds.right - shadowOffset, contentBounds.bottom - shadowOffset);
    canvas.scale(shadowScaleHorizontal, shadowScaleBottom);
    canvas.rotate(180f);
    canvas.drawPath(cornerShadowPath, cornerShadowPaint);
    if (drawHorizontalEdges) {
      // BE
      canvas.scale(1f / shadowScaleHorizontal, 1f);
      canvas.drawRect(
          0,
          edgeShadowTop,
          contentBounds.width() - 2 * shadowOffset,
          -cornerRadius + shadowSize,
          edgeShadowPaint);
    }
    canvas.restoreToCount(saved);
    // LB
    saved = canvas.save();
    canvas.translate(contentBounds.left + shadowOffset, contentBounds.bottom - shadowOffset);
    canvas.scale(shadowScaleHorizontal, shadowScaleBottom);
    canvas.rotate(270f);
    canvas.drawPath(cornerShadowPath, cornerShadowPaint);
    if (drawVerticalEdges) {
      // LE
      canvas.scale(1f / shadowScaleBottom, 1f);
      canvas.drawRect(
          0,
          edgeShadowTop,
          contentBounds.height() - 2 * shadowOffset,
          -cornerRadius,
          edgeShadowPaint);
    }
    canvas.restoreToCount(saved);
    // RT
    saved = canvas.save();
    canvas.translate(contentBounds.right - shadowOffset, contentBounds.top + shadowOffset);
    canvas.scale(shadowScaleHorizontal, shadowScaleTop);
    canvas.rotate(90f);
    canvas.drawPath(cornerShadowPath, cornerShadowPaint);
    if (drawVerticalEdges) {
      // RE
      canvas.scale(1f / shadowScaleTop, 1f);
      canvas.drawRect(
          0,
          edgeShadowTop,
          contentBounds.height() - 2 * shadowOffset,
          -cornerRadius,
          edgeShadowPaint);
    }
    canvas.restoreToCount(saved);

    canvas.restoreToCount(rotateSaved);
  }

  private void buildShadowCorners() {
    RectF innerBounds = new RectF(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius);
    RectF outerBounds = new RectF(innerBounds);
    outerBounds.inset(-shadowSize, -shadowSize);

    if (cornerShadowPath == null) {
      cornerShadowPath = new Path();
    } else {
      cornerShadowPath.reset();
    }
    cornerShadowPath.setFillType(Path.FillType.EVEN_ODD);
    cornerShadowPath.moveTo(-cornerRadius, 0);
    cornerShadowPath.rLineTo(-shadowSize, 0);
    // outer arc
    cornerShadowPath.arcTo(outerBounds, 180f, 90f, false);
    // inner arc
    cornerShadowPath.arcTo(innerBounds, 270f, -90f, false);
    cornerShadowPath.close();

    float shadowRadius = -outerBounds.top;
    if (shadowRadius > 0f) {
      float startRatio = cornerRadius / shadowRadius;
      float midRatio = startRatio + ((1f - startRatio) / 2f);
      cornerShadowPaint.setShader(
          new RadialGradient(
              0,
              0,
              shadowRadius,
              new int[] {0, shadowStartColor, shadowMiddleColor, shadowEndColor},
              new float[] {0f, startRatio, midRatio, 1f},
              Shader.TileMode.CLAMP));
    }

    // we offset the content shadowSize/2 pixels up to make it more realistic.
    // this is why edge shadow shader has some extra space
    // When drawing bottom edge shadow, we use that extra space.
    edgeShadowPaint.setShader(
        new LinearGradient(
            0,
            innerBounds.top,
            0,
            outerBounds.top,
            new int[] {shadowStartColor, shadowMiddleColor, shadowEndColor},
            new float[] {0f, .5f, 1f},
            Shader.TileMode.CLAMP));
    edgeShadowPaint.setAntiAlias(false);
  }

  private void buildComponents(@NonNull Rect bounds) {
    // Card is offset SHADOW_MULTIPLIER * maxShadowSize to account for the shadow shift.
    // We could have different top-bottom offsets to avoid extra gap above but in that case
    // center aligning Views inside the CardView would be problematic.
    final float verticalOffset = rawMaxShadowSize * SHADOW_MULTIPLIER;
    contentBounds.set(
        bounds.left + rawMaxShadowSize,
        bounds.top + verticalOffset,
        bounds.right - rawMaxShadowSize,
        bounds.bottom - verticalOffset);

    getDrawable()
        .setBounds(
            (int) contentBounds.left,
            (int) contentBounds.top,
            (int) contentBounds.right,
            (int) contentBounds.bottom);

    buildShadowCorners();
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public void setMaxShadowSize(float size) {
    setShadowSize(rawShadowSize, size);
  }

  public float getMaxShadowSize() {
    return rawMaxShadowSize;
  }

  public float getMinWidth() {
    final float content = 2 * Math.max(rawMaxShadowSize, cornerRadius + rawMaxShadowSize / 2);
    return content + rawMaxShadowSize * 2;
  }

  public float getMinHeight() {
    final float content =
        2 * Math.max(rawMaxShadowSize, cornerRadius + rawMaxShadowSize * SHADOW_MULTIPLIER / 2);
    return content + (rawMaxShadowSize * SHADOW_MULTIPLIER) * 2;
  }
}
