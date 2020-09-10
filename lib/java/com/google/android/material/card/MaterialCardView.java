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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;

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
 * <p>Cards implement {@link Checkable}, a default way to switch to {@code android:checked_state} is
 * not provided. Clients have to call {@link #setChecked(boolean)}. This shows the {@link
 * R.attr#checkedIcon app:checkedIcon} and changes the overlay color.
 *
 * <p>Cards also have a custom state meant to be used when a card is draggable {@code
 * app:dragged_state}. It's used by calling {@link #setDragged(boolean)}. This changes the overlay
 * color and elevates the card to convey motion.
 *
 * <p><strong>Note:</strong> The actual view hierarchy present under MaterialCardView is
 * <strong>NOT</strong> guaranteed to match the view hierarchy as written in XML. As a result, calls
 * to getParent() on children of the MaterialCardView, will not return the MaterialCardView itself,
 * but rather an intermediate View. If you need to access a MaterialCardView directly, set an {@code
 * android:id} and use {@link View#findViewById(int)}.
 */
public class MaterialCardView extends CardView implements Checkable, Shapeable {

  /** Interface definition for a callback to be invoked when the card checked state changes. */
  public interface OnCheckedChangeListener {
    /**
     * Called when the checked state of a compound button has changed.
     *
     * @param card The Material Card View whose state has changed.
     * @param isChecked The new checked state of MaterialCardView.
     */
    void onCheckedChanged(MaterialCardView card, boolean isChecked);
  }

  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DRAGGED_STATE_SET = {R.attr.state_dragged};

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CardView;
  private static final String LOG_TAG = "MaterialCardView";
  private static final String ACCESSIBILITY_CLASS_NAME = "androidx.cardview.widget.CardView";

  @NonNull private final MaterialCardViewHelper cardViewHelper;

  /**
   * Keep track of when {@link CardView} is done initializing because we don't want to use the
   * {@link Drawable} that it passes to {@link #setBackground(Drawable)}.
   */
  private boolean isParentCardViewDoneInitializing;

  private boolean checked = false;
  private boolean dragged = false;
  private OnCheckedChangeListener onCheckedChangeListener;

  public MaterialCardView(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialCardView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialCardViewStyle);
  }

  public MaterialCardView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    isParentCardViewDoneInitializing = true;
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialCardView, defStyleAttr, DEF_STYLE_RES);

    // Loads and sets background drawable attributes.
    cardViewHelper = new MaterialCardViewHelper(this, attrs, defStyleAttr, DEF_STYLE_RES);
    cardViewHelper.setCardBackgroundColor(super.getCardBackgroundColor());
    cardViewHelper.setUserContentPadding(
        super.getContentPaddingLeft(),
        super.getContentPaddingTop(),
        super.getContentPaddingRight(),
        super.getContentPaddingBottom());
    // Zero out the AppCompat CardView's content padding, the padding will be added to the internal
    // contentLayout.
    cardViewHelper.loadFromAttributes(attributes);

    attributes.recycle();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(ACCESSIBILITY_CLASS_NAME);
    info.setCheckable(isCheckable());
    info.setClickable(isClickable());
    info.setChecked(isChecked());
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent accessibilityEvent) {
    super.onInitializeAccessibilityEvent(accessibilityEvent);
    accessibilityEvent.setClassName(ACCESSIBILITY_CLASS_NAME);
    accessibilityEvent.setChecked(isChecked());
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
    cardViewHelper.setStrokeColor(ColorStateList.valueOf(strokeColor));
  }

  /**
   * Sets the stroke color of this card view.
   *
   * @param strokeColor The ColorStateList of the stroke.
   */
  public void setStrokeColor(ColorStateList strokeColor) {
    cardViewHelper.setStrokeColor(strokeColor);
  }

  /** @deprecated use {@link #getStrokeColorStateList()} */
  @ColorInt
  @Deprecated
  public int getStrokeColor() {
    return cardViewHelper.getStrokeColor();
  }

  /** Returns the stroke ColorStateList of this card view. */
  @Nullable
  public ColorStateList getStrokeColorStateList() {
    return cardViewHelper.getStrokeColorStateList();
  }

  /**
   * Sets the stroke width of this card view.
   *
   * @param strokeWidth The width in pixels of the stroke.
   */
  public void setStrokeWidth(@Dimension int strokeWidth) {
    cardViewHelper.setStrokeWidth(strokeWidth);
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
  }

  @Override
  public float getRadius() {
    return cardViewHelper.getCornerRadius();
  }

  float getCardViewRadius() {
    return MaterialCardView.super.getRadius();
  }


  /**
   * Sets the interpolation on the Shape Path of the card. Useful for animations.
   * @see MaterialShapeDrawable#setInterpolation(float)
   * @see ShapeAppearanceModel
   */
  public void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    cardViewHelper.setProgress(progress);
  }


  /**
   * Returns the interpolation on the Shape Path of the card.
   * @see MaterialShapeDrawable#getInterpolation()
   * @see ShapeAppearanceModel
   */
  @FloatRange(from = 0f, to = 1f)
  public float getProgress() {
    return cardViewHelper.getProgress();
  }

  @Override
  public void setContentPadding(int left, int top, int right, int bottom) {
    cardViewHelper.setUserContentPadding(left, top, right, bottom);
  }

  void setAncestorContentPadding(int left, int top, int right, int bottom) {
    super.setContentPadding(left, top, right, bottom);
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

  @NonNull
  @Override
  public ColorStateList getCardBackgroundColor() {
    return cardViewHelper.getCardBackgroundColor();
  }

  /**
   * Sets the foreground color for this card.
   *
   * @param foregroundColor Color to use for the foreground.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_cardForegroundColor
   * @see #getCardForegroundColor()
   */
  public void setCardForegroundColor(@Nullable ColorStateList foregroundColor) {
    cardViewHelper.setCardForegroundColor(foregroundColor);
  }

  /**
   * Sets the ripple color for this card.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_cardForegroundColor
   * @see #setCardForegroundColor(ColorStateList)
   */
  @NonNull
  public ColorStateList getCardForegroundColor() {
    return cardViewHelper.getCardForegroundColor();
  }

  @Override
  public void setClickable(boolean clickable) {
    super.setClickable(clickable);
    if (cardViewHelper != null){
      cardViewHelper.updateClickable();
    }
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this, cardViewHelper.getBackground());
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
   * Call this when the Card is being dragged to apply the right color and elevation changes.
   *
   * @param dragged whether the card is currently being dragged or at rest.
   */
  public void setDragged(boolean dragged) {
    if (this.dragged != dragged) {
      this.dragged = dragged;
      refreshDrawableState();
      forceRippleRedrawIfNeeded();
      invalidate();
    }
  }

  public boolean isDragged() {
    return dragged;
  }

  /**
   * Returns whether this Card is checkable.
   *
   * @see #setCheckable(boolean)
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_android_checkable
   */
  public boolean isCheckable() {
    return cardViewHelper != null && cardViewHelper.isCheckable();
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
      forceRippleRedrawIfNeeded();
      if (onCheckedChangeListener != null) {
        onCheckedChangeListener.onCheckedChanged(this, checked);
      }
    }
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 3);
    if (isCheckable()) {
      mergeDrawableStates(drawableState, CHECKABLE_STATE_SET);
    }

    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }

    if (isDragged()) {
      mergeDrawableStates(drawableState, DRAGGED_STATE_SET);
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

  /**
   * Returns the {@link android.content.res.ColorStateList} used to tint the checked icon.
   *
   * @see #setCheckedIconTint(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconTint
   */
  @Nullable
  public ColorStateList getCheckedIconTint() {
    return cardViewHelper.getCheckedIconTint();
  }

  /**
   * Sets this checked icon color tint using the specified {@link
   * android.content.res.ColorStateList}.
   *
   * @param checkedIconTint The tint color of this chip's icon.
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconTint
   */
  public void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    cardViewHelper.setCheckedIconTint(checkedIconTint);
  }

  @Dimension
  public int getCheckedIconSize() {
    return cardViewHelper.getCheckedIconSize();
  }

  /**
   * Sets the size of the checked icon
   *
   * @param checkedIconSize checked icon size
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconSize
   */
  public void setCheckedIconSize(@Dimension int checkedIconSize) {
    cardViewHelper.setCheckedIconSize(checkedIconSize);
  }

  /**
   * Sets the size of the checked icon using a resource id.
   *
   * @param checkedIconSizeResId The resource id of this Card's checked icon size
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconSize
   */
  public void setCheckedIconSizeResource(@DimenRes int checkedIconSizeResId) {
    if (checkedIconSizeResId != 0) {
      cardViewHelper.setCheckedIconSize(getResources().getDimensionPixelSize(checkedIconSizeResId));
    }
  }

  @Dimension
  public int getCheckedIconMargin() {
    return cardViewHelper.getCheckedIconMargin();
  }

  public void setCheckedIconMargin(@Dimension int checkedIconMargin) {
    cardViewHelper.setCheckedIconMargin(checkedIconMargin);
  }

  /**
   * Sets the margin of the checked icon using a resource id.
   *
   * @param checkedIconMarginResId The resource id of this Card's checked icon margin
   * @attr ref com.google.android.material.R.styleable#MaterialCardView_checkedIconMargin
   */
  public void setCheckedIconMarginResource(@DimenRes int checkedIconMarginResId) {
    if (checkedIconMarginResId != NO_ID) {
      cardViewHelper.setCheckedIconMargin(
          getResources().getDimensionPixelSize(checkedIconMarginResId));
    }
  }

  @NonNull
  private RectF getBoundsAsRectF() {
    RectF boundsRectF = new RectF();
    boundsRectF.set(cardViewHelper.getBackground().getBounds());
    return boundsRectF;
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      setClipToOutline(shapeAppearanceModel.isRoundRect(getBoundsAsRectF()));
    }
    cardViewHelper.setShapeAppearanceModel(shapeAppearanceModel);
  }

  /**
   * Due to limitations in the current implementation, if you modify the returned object
   * call {@link #setShapeAppearanceModel(ShapeAppearanceModel)} again with the modified value
   * to propagate the required changes.
   */
  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return cardViewHelper.getShapeAppearanceModel();
  }

  private void forceRippleRedrawIfNeeded() {
    if (VERSION.SDK_INT > VERSION_CODES.O) {
      cardViewHelper.forceRippleRedraw();
    }
  }
}
