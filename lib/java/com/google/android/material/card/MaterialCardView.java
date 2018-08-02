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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import com.google.android.material.internal.ThemeEnforcement;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Provides a Material card.
 *
 * <p>This class supplies Material styles for the card in the constructor. The widget will display
 * the correct default Material styles without the use of a style flag.
 *
 * <p>Stroke width can be set using the {@code strokeWidth} attribute. Set the stroke color using
 * the {@code strokeColor} attribute. Without a {@code strokeColor}, the card will not render a
 * stroked border, regardless of the {@code strokeWidth} value.
 *
 * * <p><strong>Note:</strong> Avoid setting {@link View#setClipToOutline} to true. There is an
 * intermediate view to clip the content, setting this will have negative performance consequences.
 *
 * <p><strong>Note:</strong> The actual view hierarchy present under MaterialCardView is
 * <strong>NOT</strong> guaranteed to match the view hierarchy as written in XML. As a result, calls
 * to getParent() on children of the MaterialCardView, will not return the MaterialCardView itself,
 * but rather an intermediate View. If you need to access a MaterialCardView directly,
 * set an {@code android:id} and use {@link View#findViewById(int)}.
 */
public class MaterialCardView extends CardView {

  private final MaterialCardViewHelper cardViewHelper;
  private final FrameLayout contentLayout;

  public MaterialCardView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialCardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(ThemeEnforcement.createThemedContext(context, attrs, defStyleAttr), attrs, defStyleAttr);

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
    // Add a content view to allow the border to be drawn outside the outline.
    contentLayout = new FrameLayout(context);
    contentLayout.setMinimumHeight(getContentMinimumHeight());
    contentLayout.setMinimumWidth(getContentMinimumWidth());
    super.addView(contentLayout, -1, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    updateContentLayout();

    attributes.recycle();
  }

  private void updateContentLayout() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      cardViewHelper.createOutlineProvider(contentLayout);
    }
  }

  private int getContentMinimumWidth() {
    return ViewCompat.getMinimumWidth(this) - getContentPaddingLeft() - getContentPaddingRight();
  }

  private int getContentMinimumHeight() {
    return ViewCompat.getMinimumHeight(this) - getContentPaddingBottom() - getContentPaddingTop();
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
  public void setStrokeWidth(@Dimension int strokeWidth) {
    cardViewHelper.setStrokeWidth(strokeWidth);
    updateContentLayout();
  }

  /** Returns the stroke width of this card view. */
  @Dimension
  public int getStrokeWidth() {
    return cardViewHelper.getStrokeWidth();
  }

  @Override
  public void setRadius(float radius) {
    super.setRadius(radius);
    cardViewHelper.updateForeground();
    updateContentLayout();
  }

  @Override
  public void setLayoutParams(ViewGroup.LayoutParams params) {
    super.setLayoutParams(params);
    LayoutParams layoutParams = (LayoutParams) contentLayout.getLayoutParams();
    if (params instanceof LayoutParams) {
      layoutParams.gravity = ((LayoutParams) params).gravity;
      contentLayout.requestLayout();
    }
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    contentLayout.addView(child, index, params);
  }

  @Override
  public void removeAllViews() {
    contentLayout.removeAllViews();
  }

  @Override
  public void removeView(View view) {
    contentLayout.removeView(view);
  }

  @Override
  public void removeViewInLayout(View view) {
    contentLayout.removeViewInLayout(view);
  }

  @Override
  public void removeViewsInLayout(int start, int count) {
    contentLayout.removeViewsInLayout(start, count);
  }

  @Override
  public void removeViewAt(int index) {
    contentLayout.removeViewAt(index);
  }

  @Override
  public void removeViews(int start, int count) {
    contentLayout.removeViews(start, count);
  }
}
