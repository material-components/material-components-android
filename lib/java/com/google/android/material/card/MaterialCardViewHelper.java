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
import android.graphics.Color;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.Dimension;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewOutlineProvider;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialCardViewHelper {

  private static final int DEFAULT_STROKE_VALUE = -1;

  // used to calculate content padding
  private static final double COS_45 = Math.cos(Math.toRadians(45));

  /**
   * Multiplier for {@link MaterialCardView#getMaxCardElevation()} to calculate vertical shadow
   * padding. Horizontal shadow padding is equal to getMaxCardElevation(). Shadow padding is the
   * padding around the visible card that {@link CardView} adds in order to have space to render
   * shadows pre-Lollipop.
   *
   * <p>CardView's pre-Lollipop shadow is getMaxCardElevation() larger than the card on all sides
   * and offset down by 0.5 x getMaxCardElevation(). Thus, the additional padding required is:
   *
   * <ul>
   *   <li>Left & Right: getMaxCardElevation()
   *   <li>Top: 0.5 x getMaxCardElevation()
   *   <li>Bottom: 1.5 x getMaxCardElevation()
   * </ul>
   *
   * <p>In order to keep content that is centered in the center, extra padding is added on top to
   * match the necessary bottom padding.
   */
  private static final float CARD_VIEW_SHADOW_MULTIPLIER = 1.5f;

  private static final float SHADOW_RADIUS_MULTIPLIER = .75f;
  private static final float SHADOW_OFFSET_MULTIPLIER = .25f;

  private final MaterialCardView materialCardView;

  @ColorInt private int strokeColor;
  @ColorInt private int rippleColor;
  @Dimension private int strokeWidth;

  private final ShapeAppearanceModel shapeAppearanceModel; // Shared by background, stroke & ripple
  private final MaterialShapeDrawable bgDrawable; // Will always wrapped in an InsetDrawable
  private final MaterialShapeDrawable strokeDrawable; // Will always wrapped in an InsetDrawable
  /** Either a {@link RippleDrawable} when using framework ripple or a {@link StateListDrawable}. */
  @Nullable private Drawable rippleDrawable;
  /** Layers stroke and ripple drawables. */
  @Nullable private LayerDrawable clickableForegroundDrawable;
  /**
   * When not using framework drawable, this is used for the {@link StateListDrawable} stored in
   * {@link #rippleDrawable}.
   */
  @Nullable private MaterialShapeDrawable compatRippleDrawable;

  // If card is clickable, this is the clickableForegroundDrawable otherwise it is the
  // strokeDrawable
  private Drawable fgDrawable;

  private boolean isBackgroundOverwritten = false;

  public MaterialCardViewHelper(MaterialCardView card) {
    materialCardView = card;
    shapeAppearanceModel = new ShapeAppearanceModel();
    bgDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    bgDrawable.setShadowColor(Color.DKGRAY);
    strokeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    strokeDrawable.setFillColor(ColorStateList.valueOf(Color.TRANSPARENT));
    fgDrawable = materialCardView.isClickable() ? getClickableForeground() : strokeDrawable;
  }

  public void loadFromAttributes(TypedArray attributes) {
    strokeColor =
        attributes.getColor(R.styleable.MaterialCardView_strokeColor, DEFAULT_STROKE_VALUE);
    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0);
    rippleColor = getRippleColor();
    updateRippleColor();

    updateCornerRadius();
    updateElevation();
    updateStroke();

    materialCardView.setBackgroundInternal(insetDrawable(bgDrawable));
    materialCardView.setForeground(insetDrawable(fgDrawable));

    adjustContentPadding(strokeWidth);
  }

  boolean isBackgroundOverwritten() {
    return isBackgroundOverwritten;
  }

  void setBackgroundOverwritten(boolean isBackgroundOverwritten) {
    this.isBackgroundOverwritten = isBackgroundOverwritten;
  }

  void setStrokeColor(@ColorInt int strokeColor) {
    if (this.strokeColor == strokeColor) {
      return;
    }

    this.strokeColor = strokeColor;
    updateStroke();
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
    updateStroke();
    adjustContentPadding(strokeWidthDelta);
  }

  @Dimension
  int getStrokeWidth() {
    return strokeWidth;
  }

  void setCardBackgroundColor(ColorStateList color) {
    bgDrawable.setFillColor(color);
  }

  ColorStateList getCardBackgroundColor() {
    return bgDrawable.getFillColor();
  }

  void updateClickable() {
    Drawable previousFgDrawable = fgDrawable;
    fgDrawable = materialCardView.isClickable() ? getClickableForeground() : strokeDrawable;
    if (previousFgDrawable != fgDrawable) {
      updateInsetForeground(fgDrawable);
    }
  }

  void updateCornerRadius() {
    shapeAppearanceModel.setCornerRadius(materialCardView.getRadius());
    bgDrawable.invalidateSelf();
    fgDrawable.invalidateSelf();
  }

  void updateElevation() {
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      bgDrawable.setShadowElevation((int) materialCardView.getCardElevation());
      // TODO: Remove once radius and offset are changed by setShadowElevation.
      bgDrawable.setShadowRadius(
          (int) Math.ceil(materialCardView.getCardElevation() * SHADOW_RADIUS_MULTIPLIER));
      bgDrawable.setShadowVerticalOffset(
          (int) Math.ceil(materialCardView.getCardElevation() * SHADOW_OFFSET_MULTIPLIER));
    }
  }

  void updateInsets() {
    // No way to update the inset amounts for an InsetDrawable, so recreate insets as needed.
    if (!isBackgroundOverwritten()) {
      materialCardView.setBackgroundInternal(insetDrawable(bgDrawable));
    }
    materialCardView.setForeground(insetDrawable(fgDrawable));
  }

  void updateStroke() {
    // In order to set a stroke, a size and color both need to be set. We default to a zero-width
    // width size, but won't set a default color. This prevents drawing a stroke that blends in with
    // the card but that could affect card spacing.
    if (strokeColor != DEFAULT_STROKE_VALUE) {
      strokeDrawable.setStroke(strokeWidth, strokeColor);
    }
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
        // TODO: Derive outline from ShapeAppearanceModel
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

  /**
   * Attempts to update the {@link InsetDrawable} foreground to use the given {@link Drawable}.
   * Changing the Drawable is only available in M+, so earlier versions will create a new
   * InsetDrawable.
   */
  private void updateInsetForeground(Drawable insetForeground) {
    if (VERSION.SDK_INT >= VERSION_CODES.M
        && materialCardView.getForeground() instanceof InsetDrawable) {
      ((InsetDrawable) materialCardView.getForeground()).setDrawable(insetForeground);
    } else {
      materialCardView.setForeground(insetDrawable(insetForeground));
    }
  }

  /**
   * Returns a {@link Drawable} that insets the given drawable by the amount of padding CardView
   * would add for the shadow. This will always use an {@link InsetDrawable} even if there is no
   * inset.
   *
   * <p>Always use an InsetDrawable even when the insets are 0 instead of only wrapping in an
   * InsetDrawable when there is an inset. Replacing the background (or foreground) of a {@link
   * View} with the same Drawable wrapped into an InsetDrawable will result in the View clearing the
   * original Drawable's callback which should refer to the InsetDrawable.
   */
  private Drawable insetDrawable(Drawable originalDrawable) {
    int insetVertical = 0;
    int insetHorizontal = 0;
    boolean isPreLollipop = Build.VERSION.SDK_INT < VERSION_CODES.LOLLIPOP;
    if (isPreLollipop || materialCardView.getUseCompatPadding()) {
      // Calculate the shadow padding used by CardView
      boolean addInsetForCorners = materialCardView.getPreventCornerOverlap() && !isPreLollipop;
      insetVertical =
          (int)
              Math.ceil(
                  calculateVerticalPadding(
                      materialCardView.getMaxCardElevation(),
                      materialCardView.getRadius(),
                      addInsetForCorners));
      insetHorizontal =
          (int)
              Math.ceil(
                  calculateHorizontalPadding(
                      materialCardView.getMaxCardElevation(),
                      materialCardView.getRadius(),
                      addInsetForCorners));
    }
    return new InsetDrawable(
        originalDrawable, insetHorizontal, insetVertical, insetHorizontal, insetVertical) {
      @Override
      public boolean getPadding(Rect padding) {
        // Our very own special InsetDrawable that pretends it does not have padding so that
        // using it as the background will *not* change the padding of the view.
        return false;
      }
    };
  }

  /** Copied from {@link CardView} implementation. */
  static float calculateVerticalPadding(
      float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
    if (addPaddingForCorners) {
      return (float) (maxShadowSize * CARD_VIEW_SHADOW_MULTIPLIER + (1 - COS_45) * cornerRadius);
    } else {
      return maxShadowSize * CARD_VIEW_SHADOW_MULTIPLIER;
    }
  }

  /** Copied from {@link CardView} implementation. */
  static float calculateHorizontalPadding(
      float maxShadowSize, float cornerRadius, boolean addPaddingForCorners) {
    if (addPaddingForCorners) {
      return (float) (maxShadowSize + (1 - COS_45) * cornerRadius);
    } else {
      return maxShadowSize;
    }
  }

  private Drawable getClickableForeground() {
    if (rippleDrawable == null) {
      rippleDrawable = createForegroundRippleDrawable();
    }

    if (clickableForegroundDrawable == null) {
      clickableForegroundDrawable =
          new LayerDrawable(new Drawable[] {rippleDrawable, strokeDrawable});
    }
    return clickableForegroundDrawable;
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

  private Drawable createForegroundRippleDrawable() {
    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      //noinspection NewApi
      return new RippleDrawable(
          ColorStateList.valueOf(rippleColor), null, createForegroundShapeDrawable());
    }

    return createCompatRippleDrawable();
  }

  private Drawable createCompatRippleDrawable() {
    StateListDrawable rippleDrawable = new StateListDrawable();
    compatRippleDrawable = createForegroundShapeDrawable();
    compatRippleDrawable.setFillColor(ColorStateList.valueOf(rippleColor));
    rippleDrawable.addState(new int[] {android.R.attr.state_pressed}, compatRippleDrawable);
    return rippleDrawable;
  }

  private void updateRippleColor() {
    //noinspection NewApi
    if (RippleUtils.USE_FRAMEWORK_RIPPLE && rippleDrawable != null) {
      ((RippleDrawable) rippleDrawable).setColor(ColorStateList.valueOf(rippleColor));
    } else if (compatRippleDrawable != null) {
      compatRippleDrawable.setFillColor(ColorStateList.valueOf(rippleColor));
    }
  }

  private MaterialShapeDrawable createForegroundShapeDrawable() {
    return new MaterialShapeDrawable(shapeAppearanceModel);
  }
}
