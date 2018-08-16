/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.card;

import com.google.android.material.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import com.google.android.material.ripple.RippleUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;
import java.util.Arrays;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialCardViewHelper {

  private static final int DEFAULT_STROKE_VALUE = -1;

  private final MaterialCardView materialCardView;

  private @ColorInt int strokeColor;
  private @ColorInt int rippleColor;
  private @Dimension int strokeWidth;

  public MaterialCardViewHelper(MaterialCardView card) {
    materialCardView = card;
  }

  public void loadFromAttributes(TypedArray attributes) {
    strokeColor =
        attributes.getColor(R.styleable.MaterialCardView_strokeColor, DEFAULT_STROKE_VALUE);
    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0);
    rippleColor = getRippleColor();
    updateForeground();
    adjustContentPadding(strokeWidth);
  }

  void setStrokeColor(@ColorInt int strokeColor) {
    this.strokeColor = strokeColor;
    updateForeground();
  }

  @TargetApi(VERSION_CODES.LOLLIPOP)
  void createOutlineProvider(@Nullable View contentView) {
    if (contentView == null) {
      return;
    }
    // To draw the stroke outside the outline, call {@link View#setClipToOutline} on the child
    // rather than on the card view.
    materialCardView.setClipToOutline(false);
    contentView.setClipToOutline(true);
    contentView.setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            outline.setRoundRect(
                0,
                0,
                view.getWidth(),
                view.getHeight(),
                materialCardView.getRadius() - strokeWidth);
          }
        });
  }

  @ColorInt
  int getStrokeColor() {
    return strokeColor;
  }

  void setStrokeWidth(@Dimension int strokeWidth) {
    if (strokeWidth == this.strokeWidth) {
      return;
    }

    int strokeWidthDelta = strokeWidth - this.strokeWidth;
    this.strokeWidth = strokeWidth;
    updateForeground();
    adjustContentPadding(strokeWidthDelta);
  }

  @Dimension
  int getStrokeWidth() {
    return strokeWidth;
  }

  /**
   * Recreates a foreground drawable based on the current card radius, stroke color and width and
   * sets it as the foreground.
   */
  void updateForeground() {
    materialCardView.setForeground(createForegroundDrawable());
  }

  /**
   * Creates a drawable foreground for the card in order to handle a stroke outline.
   *
   * @return drawable representing foreground for a card.
   */
  private Drawable createForegroundDrawable() {
    GradientDrawable fgDrawable = new GradientDrawable();
    float radius = materialCardView.getRadius();
    fgDrawable.setCornerRadius(radius);

    // In order to set a stroke, a size and color both need to be set. We default to a zero-width
    // width size, but won't set a default color. This prevents drawing a stroke that blends in with
    // the card but that could affect card spacing.
    if (strokeColor != DEFAULT_STROKE_VALUE) {
      fgDrawable.setStroke(strokeWidth, strokeColor);
    }

    if (!materialCardView.isClickable()) {
      return fgDrawable;
    }

    Drawable rippleDrawable;
    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      //noinspection NewApi
      rippleDrawable =
          new RippleDrawable(
              ColorStateList.valueOf(rippleColor), null, createForegroundShape(radius));
    } else {
      rippleDrawable = new StateListDrawable();
      Drawable foregroundShape = createForegroundShape(radius);
      DrawableCompat.setTint(foregroundShape, rippleColor);
      ((StateListDrawable) rippleDrawable)
          .addState(new int[] {android.R.attr.state_pressed}, foregroundShape);
    }

    return new LayerDrawable(
        new Drawable[] {
          rippleDrawable, fgDrawable,
        });
  }

  /** Guarantee at least enough content padding to account for the stroke width. */
  private void adjustContentPadding(int strokeWidthDelta) {
    int contentPaddingLeft = materialCardView.getContentPaddingLeft() + strokeWidthDelta;
    int contentPaddingTop = materialCardView.getContentPaddingTop() + strokeWidthDelta;
    int contentPaddingRight = materialCardView.getContentPaddingRight() + strokeWidthDelta;
    int contentPaddingBottom = materialCardView.getContentPaddingBottom() + strokeWidthDelta;
    materialCardView.setContentPadding(
        contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom);
  }

  private int getRippleColor() {
    Context context = materialCardView.getContext();
    TypedValue value = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.colorControlHighlight, value, true);
    return value.data;
  }

  private Drawable createForegroundShape(float radius) {
    float[] radii = new float[8];
    Arrays.fill(radii, radius);
    RoundRectShape shape = new RoundRectShape(radii, null, null);
    ShapeDrawable shapeDrawable = new ShapeDrawable(shape);
    return shapeDrawable;
  }
}
