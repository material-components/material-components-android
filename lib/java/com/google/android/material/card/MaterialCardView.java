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

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import com.google.android.material.internal.ThemeEnforcement;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;

/**
 * Provides a Material card.
 *
 * <p>This class supplies Material styles for the card in the constructor. The widget will
 * display the correct default Material styles without the use of a style flag.
 *
 * <p>Stroke width can be set using the {@code strokeWidth} attribute. Set the stroke color using
 * the {@code strokeColor} attribute. Without a {@code strokeColor}, the card will not render a
 * stroked border, regardless of the {@code strokeWidth} value.
 */
public class MaterialCardView extends CardView {

  private final MaterialCardViewHelper cardViewHelper;

  public MaterialCardView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialCardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.MaterialCardView,
            defStyleAttr,
            R.style.Widget_MaterialComponents_CardView);

    // Loads and sets background drawable attributes
    cardViewHelper = new MaterialCardViewHelper(this);
    cardViewHelper.loadFromAttributes(attributes);

    attributes.recycle();
  }

  /**
   * Sets the stroke color of this card view.
   *
   * @param strokeColor The color of the stroke.
   */
  public void setStrokeColor(@ColorInt int strokeColor) {
    cardViewHelper.setStrokeColor(strokeColor);
  }

  /** Returns the stroke color of this card view. */
  @ColorInt
  public int getStrokeColor() {
    return cardViewHelper.getStrokeColor();
  }

  /**
   * Sets the stroke width of this card view.
   *
   * @param strokeWidth The width in pixels of the stroke.
   */
  public void setStrokeWidth(@Dimension(unit = Dimension.PX) int strokeWidth) {
    cardViewHelper.setStrokeWidth(strokeWidth);
  }

  /** Returns the stroke width of this card view. */
  @Dimension(unit = Dimension.PX)
  public int getStrokeWidth() {
    return cardViewHelper.getStrokeWidth();
  }

  @Override
  public void setRadius(float radius) {
    super.setRadius(radius);
    cardViewHelper.updateForeground();
  }
}
