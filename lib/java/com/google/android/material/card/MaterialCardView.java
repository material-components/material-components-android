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

import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;
import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import com.google.android.material.internal.ThemeEnforcement;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
import android.util.Log;
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

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView;
  private static final String LOG_TAG = "MaterialCardView";

  private final MaterialCardViewHelper cardViewHelper;
  private final FrameLayout contentLayout;

  /**
   * Keep track of when {@link CardView} is done initializing because we don't want to use the
   * {@link Drawable} that it passes to {@link #setBackground(Drawable)}.
   */
  private final boolean isParentCardViewDoneInitializing;

  public MaterialCardView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialCardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    isParentCardViewDoneInitializing = true;
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialCardView, defStyleAttr, DEF_STYLE_RES);

    // Loads and sets background drawable attributes.
    cardViewHelper = new MaterialCardViewHelper(this);
    // Get the card background color that CardView read from the attributes.
    cardViewHelper.setCardBackgroundColor(super.getCardBackgroundColor());
    cardViewHelper.loadFromAttributes(attributes);

    // Add a content view to allow the border to be drawn outside the outline.
    contentLayout = new FrameLayout(context);
    super.addView(contentLayout, -1, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    updateContentLayout();

    attributes.recycle();
  }

  private void updateContentLayout() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      cardViewHelper.createOutlineProvider(contentLayout);
    }
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
    cardViewHelper.updateCornerRadius();
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
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    cardViewHelper.updateClickable();
  }

  @Override
  public void setCardBackgroundColor(@ColorInt int color) {
    cardViewHelper.setCardBackgroundColor(ColorStateList.valueOf(color));
  }

  @Override
  public void setCardBackgroundColor(@Nullable ColorStateList color) {
    cardViewHelper.setCardBackgroundColor(color);;
  }

  @Override
  public ColorStateList getCardBackgroundColor() {
    return cardViewHelper.getCardBackgroundColor();
  }

  @Override
  public void setCardElevation(float elevation) {
    super.setCardElevation(elevation);
    cardViewHelper.updateElevation();
  }

  @Override
  public void setMaxCardElevation(float maxCardElevation) {
    super.setMaxCardElevation(maxCardElevation);
    cardViewHelper.updateInsets();
  }

  @Override
  public void setUseCompatPadding(boolean useCompatPadding) {
    super.setUseCompatPadding(useCompatPadding);
    cardViewHelper.updateInsets();
  }

  @Override
  public void setPreventCornerOverlap(boolean preventCornerOverlap) {
    super.setPreventCornerOverlap(preventCornerOverlap);
    cardViewHelper.updateInsets();
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

  @Override
  public void setBackground(Drawable drawable) {
    setBackgroundDrawable(drawable);
  }

  @Override
  public void setBackgroundDrawable(Drawable drawable) {
    if (isParentCardViewDoneInitializing) {
      if (!cardViewHelper.isBackgroundOverwritten()) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.");
        cardViewHelper.setBackgroundOverwritten(true);
      }
      super.setBackgroundDrawable(drawable);
    }
    // Do nothing if CardView isn't done initializing because we don't want to use its background.
  }

  /** Allows {@link MaterialCardViewHelper} to set the background. */
  void setBackgroundInternal(Drawable drawable) {
    super.setBackgroundDrawable(drawable);
  }

}
