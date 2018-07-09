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

import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.RestrictTo;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialCardViewHelper {

  private static final int DEFAULT_STROKE_VALUE = -1;
  private final MaterialCardView materialCardView;

  private int strokeColor;
  private int strokeWidth;

  public MaterialCardViewHelper(MaterialCardView card) {
    materialCardView = card;
  }

  public void loadFromAttributes(TypedArray attributes) {
    strokeColor =
        attributes.getColor(R.styleable.MaterialCardView_strokeColor, DEFAULT_STROKE_VALUE);
    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0);
    updateForeground();
    adjustContentPadding();
  }

  void setStrokeColor(@ColorInt int strokeColor) {
    this.strokeColor = strokeColor;
    updateForeground();
  }

  @ColorInt
  int getStrokeColor() {
    return strokeColor;
  }

  void setStrokeWidth(@Dimension int strokeWidth) {
    this.strokeWidth = strokeWidth;
    updateForeground();
    adjustContentPadding();
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
    fgDrawable.setCornerRadius(materialCardView.getRadius());

    // In order to set a stroke, a size and color both need to be set. We default to a zero-width
    // width size, but won't set a default color. This prevents drawing a stroke that blends in with
    // the card but that could affect card spacing.
    if (strokeColor != DEFAULT_STROKE_VALUE) {
      fgDrawable.setStroke(strokeWidth, strokeColor);
    }

    return fgDrawable;
  }

  /** Guarantee at least enough content padding to account for the stroke width. */
  private void adjustContentPadding() {
    int contentPaddingLeft = materialCardView.getContentPaddingLeft() + strokeWidth;
    int contentPaddingTop = materialCardView.getContentPaddingTop() + strokeWidth;
    int contentPaddingRight = materialCardView.getContentPaddingRight() + strokeWidth;
    int contentPaddingBottom = materialCardView.getContentPaddingBottom() + strokeWidth;
    materialCardView.setContentPadding(
        contentPaddingLeft, contentPaddingTop, contentPaddingRight, contentPaddingBottom);
  }
}
