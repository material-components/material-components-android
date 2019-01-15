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
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ThemeEnforcement;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import android.widget.FrameLayout;
import androidx.cardview.widget.CardView;

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
 * <p><strong>Note:</strong> Avoid setting {@link View#setClipToOutline} to true. There is an
 * intermediate view to clip the content, setting this will have negative performance consequences.
 *
 * <p><strong>Note:</strong> The actual view hierarchy present under MaterialCardView is
 * <strong>NOT</strong> guaranteed to match the view hierarchy as written in XML. As a result, calls
 * to getParent() on children of the MaterialCardView, will not return the MaterialCardView itself,
 * but rather an intermediate View. If you need to access a MaterialCardView directly, set an {@code
 * android:id} and use {@link View#findViewById(int)}.
 */
public class MaterialCardView extends CardView implements Checkable {

  /**
   * Interface definition for a callback to be invoked when the card checked state changes.
   */
  public interface OnCheckedChangeListener {
    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param card The Material Card View whose state has changed.
     * @param isChecked The new checked state of MaterialCardView.
     */
    void onCheckedChanged(MaterialCardView card, boolean isChecked);
  }

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView;
  private static final String LOG_TAG = "MaterialCardView";

  private final MaterialCardViewHelper cardViewHelper;
  private final FrameLayout contentLayout;

  /**
   * Keep track of when {@link CardView} is done initializing because we don't want to use the
   * {@link Drawable} that it passes to {@link #setBackground(Drawable)}.
   */
  private final boolean isParentCardViewDoneInitializing;

  private boolean checked = false;
  private OnCheckedChangeListener onCheckedChangeListener;

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
    cardViewHelper = new MaterialCardViewHelper(this, attrs, defStyleAttr, DEF_STYLE_RES);
    // Get the card background color and content padding that CardView read from the attributes.
    cardViewHelper.setCardBackgroundColor(super.getCardBackgroundColor());
    cardViewHelper.setUserContentPadding(
        super.getContentPaddingLeft(),
        super.getContentPaddingTop(),
        super.getContentPaddingRight(),
        super.getContentPaddingBottom());
    cardViewHelper.loadFromAttributes(attributes);

    // Add a content view to allow the border to be drawn outside the outline.
    contentLayout = new FrameLayout(context);
    super.addView(contentLayout, -1, new LayoutParams(MATCH_PARENT, MATCH_PARENT));
    updateContentLayout();

    attributes.recycle();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(MaterialCardView.class.getName());
    info.setCheckable(isCheckable());
    info.setLongClickable(isCheckable());
    info.setClickable(isClickable());
  }

  private void updateContentLayout() {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      cardViewHelper.createOutlineProvider(contentLayout);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    cardViewHelper.onMeasure(getMeasuredWidth(), getMeasuredHeight());
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
    cardViewHelper.setCornerRadius(radius);
    updateContentLayout();
  }

  @Override
  public float getRadius() {
    return cardViewHelper.getCornerRadius();
  }

  float getCardViewRadius() {
    return MaterialCardView.super.getRadius();
  }

  @Override
  public void setContentPadding(int left, int top, int right, int bottom) {
    cardViewHelper.setUserContentPadding(left, top, right, bottom);
  }

  @Override
  public int getContentPaddingLeft() {
    return cardViewHelper.getUserContentPadding().left;
  }

  @Override
  public int getContentPaddingTop() {
    return cardViewHelper.getUserContentPadding().top;
  }

  @Override
  public int getContentPaddingRight() {
    return cardViewHelper.getUserContentPadding().right;
  }

  @Override
  public int getContentPaddingBottom() {
    return cardViewHelper.getUserContentPadding().bottom;
  }

  @Override
  public void setCardBackgroundColor(@ColorInt int color) {
    cardViewHelper.setCardBackgroundColor(ColorStateList.valueOf(color));
  }

  @Override
  public void setCardBackgroundColor(@Nullable ColorStateList color) {
    cardViewHelper.setCardBackgroundColor(color);
  }

  @Override
  public ColorStateList getCardBackgroundColor() {
    return cardViewHelper.getCardBackgroundColor();
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
    cardViewHelper.updateContentPadding();
  }

  @Override
  public void setPreventCornerOverlap(boolean preventCornerOverlap) {
    super.setPreventCornerOverlap(preventCornerOverlap);
    cardViewHelper.updateInsets();
    cardViewHelper.updateContentPadding();
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

  void setContentPaddingInternal(int left, int top, int right, int bottom) {
    super.setContentPadding(left, top, right, bottom);
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void setChecked(boolean checked) {
    if (this.checked != checked) {
      toggle();
    }
  }

  /**
   * Returns whether this Card is checkable.
   *
   * @see #setCheckable(boolean)
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_android_checkable
   */
  public boolean isCheckable() {
    return cardViewHelper.isCheckable();
  }

  /**
   * Sets whether this Card is checkable.
   *
   * @param checkable Whether this chip is checkable.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_android_checkable
   */
  public void setCheckable(boolean checkable) {
    cardViewHelper.setCheckable(checkable);
  }

  @Override
  public void toggle() {
    if (isCheckable() && isEnabled()) {
      checked = !checked;
      refreshDrawableState();
      if (VERSION.SDK_INT > VERSION_CODES.O) {
        cardViewHelper.forceRippleRedraw();
      }
      if (onCheckedChangeListener != null) {
        onCheckedChangeListener.onCheckedChanged(this, checked);
      }
    }
  }

  @Override
  public boolean performLongClick() {
    toggle();
    return super.performLongClick();
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 1);
    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }
    return drawableState;
  }

  /**
   * Register a callback to be invoked when the checked state of this Card changes.
   *
   * @param listener the callback to call on checked state change
   */
  public void setOnCheckedChangeListener(@Nullable OnCheckedChangeListener listener) {
    onCheckedChangeListener = listener;
  }

  /**
   * Sets the ripple color for this card.
   *
   * @param rippleColor Color to use for the ripple.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
   * @see #setRippleColorResource(int)
   * @see #getRippleColor()
   */
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    cardViewHelper.setRippleColor(rippleColor);
  }

  /**
   * Sets the ripple color resource for this card.
   *
   * @param rippleColorResourceId Color resource to use for the ripple.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
   * @see #setRippleColor(ColorStateList)
   * @see #getRippleColor()
   */
  public void setRippleColorResource(@ColorRes int rippleColorResourceId) {
    cardViewHelper.setRippleColor(
        AppCompatResources.getColorStateList(getContext(), rippleColorResourceId));
  }

  /**
   * Gets the ripple color for this card.
   *
   * @return The color used for the ripple.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_rippleColor
   * @see #setRippleColor(ColorStateList)
   * @see #setRippleColorResource(int)
   */
  public ColorStateList getRippleColor() {
    return cardViewHelper.getRippleColor();
  }

  /**
   * Returns this cards's checked icon.
   *
   * @see #setCheckedIcon(Drawable)
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
   */
  @Nullable
  public Drawable getCheckedIcon() {
    return cardViewHelper.getCheckedIcon();
  }

  /**
   * Sets this card's checked icon using a resource id.
   *
   * @param id The resource id of this Card's checked icon.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
   */
  public void setCheckedIconResource(@DrawableRes int id) {
    cardViewHelper.setCheckedIcon(AppCompatResources.getDrawable(getContext(), id));
  }

  /**
   * Sets this card's checked icon.
   *
   * @param checkedIcon This card's checked icon.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIcon
   */
  public void setCheckedIcon(@Nullable Drawable checkedIcon) {
    cardViewHelper.setCheckedIcon(checkedIcon);
  }
}
