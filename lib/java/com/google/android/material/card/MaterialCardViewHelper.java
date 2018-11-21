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
import android.support.annotation.StyleRes;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;
import android.support.v7.widget.CardView;
import android.util.AttributeSet;
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
  private final Rect userContentPadding = new Rect();

  private final ShapeAppearanceModel shapeAppearanceModel; // Shared by background, stroke & ripple
  private final MaterialShapeDrawable bgDrawable; // Will always wrapped in an InsetDrawable
  private final MaterialShapeDrawable strokeDrawable; // Will always wrapped in an InsetDrawable
  @Nullable private Drawable rippleDrawable;
  @Nullable private LayerDrawable clickableForegroundDrawable;
  @Nullable private MaterialShapeDrawable compatRippleDrawable;

  private final ShapeAppearanceModel shapeAppearanceModelInsetByStroke;
  private final MaterialShapeDrawable drawableInsetByStroke;
  private final Rect temporaryBounds = new Rect();

  // If card is clickable, this is the clickableForegroundDrawable otherwise it is the
  // strokeDrawable
  private Drawable fgDrawable;

  private boolean isBackgroundOverwritten = false;

  public MaterialCardViewHelper(
      MaterialCardView card, AttributeSet attrs, int defStyleAttr, @StyleRes int defStyleRes) {
    materialCardView = card;
    bgDrawable = new MaterialShapeDrawable(card.getContext(), attrs, defStyleAttr, defStyleRes);
    shapeAppearanceModel = bgDrawable.getShapeAppearanceModel();
    bgDrawable.setShadowColor(Color.DKGRAY);
    strokeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    strokeDrawable.setFillColor(ColorStateList.valueOf(Color.TRANSPARENT));
    fgDrawable = materialCardView.isClickable() ? getClickableForeground() : strokeDrawable;

    TypedArray cardViewAttributes =
        card.getContext()
            .obtainStyledAttributes(attrs, R.styleable.CardView, defStyleAttr, R.style.CardView);
    if (cardViewAttributes.hasValue(R.styleable.CardView_cardCornerRadius)) {
      shapeAppearanceModel.setCornerRadius(
          cardViewAttributes.getDimension(R.styleable.CardView_cardCornerRadius, 0));
    }

    shapeAppearanceModelInsetByStroke = new ShapeAppearanceModel(shapeAppearanceModel);
    drawableInsetByStroke = new MaterialShapeDrawable(shapeAppearanceModelInsetByStroke);
  }

  public void loadFromAttributes(TypedArray attributes) {
    // If cardCornerRadius is set, let it override the shape appearance.
    strokeColor =
        attributes.getColor(R.styleable.MaterialCardView_strokeColor, DEFAULT_STROKE_VALUE);
    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0);
    adjustShapeAppearanceModelInsetByStroke();
    rippleColor = getRippleColor();
    updateRippleColor();

    updateElevation();
    updateStroke();

    materialCardView.setBackgroundInternal(insetDrawable(bgDrawable));
    materialCardView.setForeground(insetDrawable(fgDrawable));

    updateContentPadding();
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

    this.strokeWidth = strokeWidth;
    adjustShapeAppearanceModelInsetByStroke();
    updateStroke();
    updateContentPadding();
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

  void setUserContentPadding(int left, int top, int right, int bottom) {
    userContentPadding.set(left, top, right, bottom);
    updateContentPadding();
  }

  Rect getUserContentPadding() {
    return userContentPadding;
  }

  void updateClickable() {
    Drawable previousFgDrawable = fgDrawable;
    fgDrawable = materialCardView.isClickable() ? getClickableForeground() : strokeDrawable;
    if (previousFgDrawable != fgDrawable) {
      updateInsetForeground(fgDrawable);
    }
  }

  void setCornerRadius(float cornerRadius) {
    shapeAppearanceModel.setCornerRadius(cornerRadius);
    shapeAppearanceModelInsetByStroke.setCornerRadius(cornerRadius - strokeWidth);
    bgDrawable.invalidateSelf();
    fgDrawable.invalidateSelf();
    if (shouldAddCornerPaddingOutsideCardBackground()
        || shouldAddCornerPaddingInsideCardBackground()) {
      updateContentPadding();
    }
    if (shouldAddCornerPaddingOutsideCardBackground()) {
      updateInsets();
    }
  }

  float getCornerRadius() {
    return shapeAppearanceModel.getTopLeftCorner().getCornerSize();
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
    if (canClipToOutline()) {
      contentView.setClipToOutline(true);
      contentView.setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              temporaryBounds.set(0, 0, view.getWidth(), view.getHeight());
              drawableInsetByStroke.setBounds(temporaryBounds);
              drawableInsetByStroke.getOutline(outline);
            }
          });
    } else {
      contentView.setClipToOutline(false);
      contentView.setOutlineProvider(null);
    }
  }

  private void adjustShapeAppearanceModelInsetByStroke() {
    shapeAppearanceModelInsetByStroke
        .getTopLeftCorner()
        .setCornerSize(shapeAppearanceModel.getTopLeftCorner().getCornerSize() - strokeWidth);
    shapeAppearanceModelInsetByStroke
        .getTopRightCorner()
        .setCornerSize(shapeAppearanceModel.getTopRightCorner().getCornerSize() - strokeWidth);
    shapeAppearanceModelInsetByStroke
        .getBottomRightCorner()
        .setCornerSize(shapeAppearanceModel.getBottomRightCorner().getCornerSize() - strokeWidth);
    shapeAppearanceModelInsetByStroke
        .getBottomLeftCorner()
        .setCornerSize(shapeAppearanceModel.getBottomLeftCorner().getCornerSize() - strokeWidth);
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
      insetVertical = (int) Math.ceil(calculateVerticalBackgroundPadding());
      insetHorizontal = (int) Math.ceil(calculateHorizontalBackgroundPadding());
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

  /**
   * Calculates the amount of padding that should be added above and below the background shape.
   * This should only be called pre-lollipop or when using compat padding. This accounts for shadow
   * and corner padding when they are added outside the background.
   */
  private float calculateVerticalBackgroundPadding() {
    return materialCardView.getMaxCardElevation() * CARD_VIEW_SHADOW_MULTIPLIER
        + (shouldAddCornerPaddingOutsideCardBackground() ? calculateActualCornerPadding() : 0);
  }

  /**
   * Calculates the amount of padding that should be added to the left and right of the background
   * shape. This should only be called pre-lollipop or when using compat padding. This accounts for
   * shadow and corner padding when they are added outside the background.
   */
  private float calculateHorizontalBackgroundPadding() {
    return materialCardView.getMaxCardElevation()
        + (shouldAddCornerPaddingOutsideCardBackground() ? calculateActualCornerPadding() : 0);
  }

  private boolean canClipToOutline() {
    return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && shapeAppearanceModel.isRoundRect();
  }

  private float getParentCardViewCalculatedCornerPadding() {
    if (materialCardView.getPreventCornerOverlap()
        && (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP || materialCardView.getUseCompatPadding())) {
      return (float) ((1 - COS_45) * materialCardView.getCardViewRadius());
    }
    return 0f;
  }

  private boolean shouldAddCornerPaddingInsideCardBackground() {
    return materialCardView.getPreventCornerOverlap() && !canClipToOutline();
  }

  private boolean shouldAddCornerPaddingOutsideCardBackground() {
    return materialCardView.getPreventCornerOverlap()
        && canClipToOutline()
        && materialCardView.getUseCompatPadding();
  }

  /**
   * Calculates the amount of padding required between the card background shape and the card
   * content such that the entire content is within the bounds of the card background shape.
   *
   * <p>This should only be called when either {@link
   * #shouldAddCornerPaddingOutsideCardBackground()} or {@link
   * #shouldAddCornerPaddingInsideCardBackground()} returns true.
   */
  private float calculateActualCornerPadding() {
    return Math.max(
        Math.max(
            calculateCornerPaddingForCornerTreatment(shapeAppearanceModel.getTopLeftCorner()),
            calculateCornerPaddingForCornerTreatment(shapeAppearanceModel.getTopRightCorner())),
        Math.max(
            calculateCornerPaddingForCornerTreatment(shapeAppearanceModel.getBottomRightCorner()),
            calculateCornerPaddingForCornerTreatment(shapeAppearanceModel.getBottomLeftCorner())));
  }

  private float calculateCornerPaddingForCornerTreatment(CornerTreatment treatment) {
    if (treatment instanceof RoundedCornerTreatment) {
      return (float) ((1 - COS_45) * treatment.getCornerSize());
    } else if (treatment instanceof CutCornerTreatment) {
      return treatment.getCornerSize() / 2;
    }
    return 0;
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

  /**
   * Guarantee at least enough content padding to account for the stroke width and support
   * preventing corner overlap for shaped backgrounds.
   */
  void updateContentPadding() {
    boolean includeCornerPadding =
        shouldAddCornerPaddingInsideCardBackground()
            || shouldAddCornerPaddingOutsideCardBackground();
    // The amount with which to adjust the user provided content padding to account for stroke and
    // shape corners.
    int contentPaddingOffset =
        (int)
            (strokeWidth
                + (includeCornerPadding ? calculateActualCornerPadding() : 0)
                - getParentCardViewCalculatedCornerPadding());
    materialCardView.setContentPaddingInternal(
        userContentPadding.left + contentPaddingOffset,
        userContentPadding.top + contentPaddingOffset,
        userContentPadding.right + contentPaddingOffset,
        userContentPadding.bottom + contentPaddingOffset);
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
