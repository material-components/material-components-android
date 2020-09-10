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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.cardview.widget.CardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearanceModel;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialCardViewHelper {

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

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

  private static final int CHECKED_ICON_LAYER_INDEX = 2;

  @NonNull private final MaterialCardView materialCardView;
  @NonNull private final Rect userContentPadding = new Rect();

  // Will always wrapped in an InsetDrawable
  @NonNull private final MaterialShapeDrawable bgDrawable;

  // Will always wrapped in an InsetDrawable
  @NonNull private final MaterialShapeDrawable foregroundContentDrawable;

  @Dimension private int checkedIconMargin;
  @Dimension private int checkedIconSize;
  @Dimension private int strokeWidth;

  // If card is clickable, this is the clickableForegroundDrawable otherwise it draws the stroke.
  @Nullable private Drawable fgDrawable;
  @Nullable private Drawable checkedIcon;
  @Nullable private ColorStateList rippleColor;
  @Nullable private ColorStateList checkedIconTint;
  @Nullable private ShapeAppearanceModel shapeAppearanceModel;
  @Nullable private ColorStateList strokeColor;
  @Nullable private Drawable rippleDrawable;
  @Nullable private LayerDrawable clickableForegroundDrawable;
  @Nullable private MaterialShapeDrawable compatRippleDrawable;
  @Nullable private MaterialShapeDrawable foregroundShapeDrawable;

  private boolean isBackgroundOverwritten = false;
  private boolean checkable;

  public MaterialCardViewHelper(
      @NonNull MaterialCardView card,
      AttributeSet attrs,
      int defStyleAttr,
      @StyleRes int defStyleRes) {
    materialCardView = card;
    bgDrawable = new MaterialShapeDrawable(card.getContext(), attrs, defStyleAttr, defStyleRes);
    bgDrawable.initializeElevationOverlay(card.getContext());
    bgDrawable.setShadowColor(Color.DKGRAY);
    ShapeAppearanceModel.Builder shapeAppearanceModelBuilder =
        bgDrawable.getShapeAppearanceModel().toBuilder();

    TypedArray cardViewAttributes =
        card.getContext()
            .obtainStyledAttributes(attrs, R.styleable.CardView, defStyleAttr, R.style.CardView);
    if (cardViewAttributes.hasValue(R.styleable.CardView_cardCornerRadius)) {
      // If cardCornerRadius is set, let it override the shape appearance.
      shapeAppearanceModelBuilder.setAllCornerSizes(
          cardViewAttributes.getDimension(R.styleable.CardView_cardCornerRadius, 0));
    }

    foregroundContentDrawable = new MaterialShapeDrawable();
    setShapeAppearanceModel(shapeAppearanceModelBuilder.build());

    cardViewAttributes.recycle();
  }

  void loadFromAttributes(@NonNull TypedArray attributes) {
    strokeColor = MaterialResources.getColorStateList(
        materialCardView.getContext(),
        attributes,
        R.styleable.MaterialCardView_strokeColor);
    if (strokeColor == null) {
      strokeColor = ColorStateList.valueOf(DEFAULT_STROKE_VALUE);
    }

    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialCardView_strokeWidth, 0);
    checkable = attributes.getBoolean(R.styleable.MaterialCardView_android_checkable, false);
    materialCardView.setLongClickable(checkable);
    checkedIconTint = MaterialResources.getColorStateList(
        materialCardView.getContext(), attributes, R.styleable.MaterialCardView_checkedIconTint);
    setCheckedIcon(
        MaterialResources.getDrawable(
            materialCardView.getContext(), attributes, R.styleable.MaterialCardView_checkedIcon));
    setCheckedIconSize(
        attributes.getDimensionPixelSize(R.styleable.MaterialCardView_checkedIconSize, 0));
    setCheckedIconMargin(
        attributes.getDimensionPixelSize(R.styleable.MaterialCardView_checkedIconMargin, 0));

    rippleColor =
        MaterialResources.getColorStateList(
            materialCardView.getContext(), attributes, R.styleable.MaterialCardView_rippleColor);
    if (rippleColor == null) {
      rippleColor =
          ColorStateList.valueOf(
              MaterialColors.getColor(materialCardView, R.attr.colorControlHighlight));
    }

    ColorStateList foregroundColor =
        MaterialResources.getColorStateList(
            materialCardView.getContext(),
            attributes,
            R.styleable.MaterialCardView_cardForegroundColor);

    setCardForegroundColor(foregroundColor);

    updateRippleColor();
    updateElevation();
    updateStroke();

    materialCardView.setBackgroundInternal(insetDrawable(bgDrawable));
    fgDrawable =
        materialCardView.isClickable() ? getClickableForeground() : foregroundContentDrawable;
    materialCardView.setForeground(insetDrawable(fgDrawable));
  }

  boolean isBackgroundOverwritten() {
    return isBackgroundOverwritten;
  }

  void setBackgroundOverwritten(boolean isBackgroundOverwritten) {
    this.isBackgroundOverwritten = isBackgroundOverwritten;
  }

  void setStrokeColor(ColorStateList strokeColor) {
    if (this.strokeColor == strokeColor) {
      return;
    }

    this.strokeColor = strokeColor;
    updateStroke();
  }

  @ColorInt
  int getStrokeColor() {
    return strokeColor == null ? DEFAULT_STROKE_VALUE : strokeColor.getDefaultColor();
  }

  @Nullable
  ColorStateList getStrokeColorStateList() {
    return strokeColor;
  }

  void setStrokeWidth(@Dimension int strokeWidth) {
    if (strokeWidth == this.strokeWidth) {
      return;
    }
    this.strokeWidth = strokeWidth;
    updateStroke();
  }

  @Dimension
  int getStrokeWidth() {
    return strokeWidth;
  }

  @NonNull
  MaterialShapeDrawable getBackground() {
    return bgDrawable;
  }

  void setCardBackgroundColor(ColorStateList color) {
    bgDrawable.setFillColor(color);
  }

  ColorStateList getCardBackgroundColor() {
    return bgDrawable.getFillColor();
  }

  void setCardForegroundColor(@Nullable ColorStateList foregroundColor) {
    foregroundContentDrawable.setFillColor(
        foregroundColor == null ? ColorStateList.valueOf(Color.TRANSPARENT) : foregroundColor);
  }

  ColorStateList getCardForegroundColor() {
    return foregroundContentDrawable.getFillColor();
  }

  void setUserContentPadding(int left, int top, int right, int bottom) {
    userContentPadding.set(left, top, right, bottom);
    updateContentPadding();
  }

  @NonNull
  Rect getUserContentPadding() {
    return userContentPadding;
  }

  void updateClickable() {
    Drawable previousFgDrawable = fgDrawable;
    fgDrawable =
        materialCardView.isClickable() ? getClickableForeground() : foregroundContentDrawable;
    if (previousFgDrawable != fgDrawable) {
      updateInsetForeground(fgDrawable);
    }
  }

  void setCornerRadius(float cornerRadius) {
    setShapeAppearanceModel(shapeAppearanceModel.withCornerSize(cornerRadius));
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
    return bgDrawable.getTopLeftCornerResolvedSize();
  }

  void setProgress(@FloatRange(from = 0f, to = 1f) float progress) {
    bgDrawable.setInterpolation(progress);
    if (foregroundContentDrawable != null) {
      foregroundContentDrawable.setInterpolation(progress);
    }

    if (foregroundShapeDrawable != null) {
      foregroundShapeDrawable.setInterpolation(progress);
    }
  }

  @FloatRange(from = 0f, to = 1f)
  float getProgress() {
    return bgDrawable.getInterpolation();
  }

  void updateElevation() {
    bgDrawable.setElevation(materialCardView.getCardElevation());
  }

  void updateInsets() {
    // No way to update the inset amounts for an InsetDrawable, so recreate insets as needed.
    if (!isBackgroundOverwritten()) {
      materialCardView.setBackgroundInternal(insetDrawable(bgDrawable));
    }
    materialCardView.setForeground(insetDrawable(fgDrawable));
  }

  void updateStroke() {
    foregroundContentDrawable.setStroke(strokeWidth, strokeColor);
  }

  /**
   * Apply content padding to the intermediate contentLayout. Padding includes the user-specified
   * content padding as well as any padding ot prevent corner overlap. The padding is applied to the
   * intermediate contentLayout so that the bounds of the contentLayout match the bounds of the
   * stroke (or card bounds if there is no stroke). This ensures that clipping is applied properly
   * to the inside of the stroke, not around the content.
   */
  void updateContentPadding() {
    boolean includeCornerPadding =
        shouldAddCornerPaddingInsideCardBackground()
            || shouldAddCornerPaddingOutsideCardBackground();
    // The amount with which to adjust the user provided content padding to account for stroke and
    // shape corners.
    int contentPaddingOffset =
        (int)
            ((includeCornerPadding ? calculateActualCornerPadding() : 0)
                - getParentCardViewCalculatedCornerPadding());

    materialCardView.setAncestorContentPadding(
        userContentPadding.left + contentPaddingOffset,
        userContentPadding.top + contentPaddingOffset,
        userContentPadding.right + contentPaddingOffset,
        userContentPadding.bottom + contentPaddingOffset);
  }

  void setCheckable(boolean checkable) {
    this.checkable = checkable;
  }

  boolean isCheckable() {
    return checkable;
  }

  void setRippleColor(@Nullable ColorStateList rippleColor) {
    this.rippleColor = rippleColor;
    updateRippleColor();
  }

  void setCheckedIconTint(@Nullable ColorStateList checkedIconTint) {
    this.checkedIconTint = checkedIconTint;
    if (checkedIcon != null) {
      DrawableCompat.setTintList(checkedIcon, checkedIconTint);
    }
  }

  @Nullable
  ColorStateList getCheckedIconTint() {
    return checkedIconTint;
  }

  @Nullable
  ColorStateList getRippleColor() {
    return rippleColor;
  }

  @Nullable
  Drawable getCheckedIcon() {
    return checkedIcon;
  }

  void setCheckedIcon(@Nullable Drawable checkedIcon) {
    this.checkedIcon = checkedIcon;
    if (checkedIcon != null) {
      this.checkedIcon = DrawableCompat.wrap(checkedIcon.mutate());
      DrawableCompat.setTintList(this.checkedIcon, checkedIconTint);
    }

    if (clickableForegroundDrawable != null) {
      Drawable checkedLayer = createCheckedIconLayer();
      clickableForegroundDrawable.setDrawableByLayerId(
          R.id.mtrl_card_checked_layer_id, checkedLayer);
    }
  }

  @Dimension
  int getCheckedIconSize() {
    return checkedIconSize;
  }

  void setCheckedIconSize(@Dimension int checkedIconSize) {
    this.checkedIconSize = checkedIconSize;
  }

  @Dimension
  int getCheckedIconMargin() {
    return checkedIconMargin;
  }

  void setCheckedIconMargin(@Dimension int checkedIconMargin) {
    this.checkedIconMargin = checkedIconMargin;
  }

  void onMeasure(int measuredWidth, int measuredHeight) {
    if (clickableForegroundDrawable != null) {
      int left = measuredWidth - checkedIconMargin - checkedIconSize;
      int bottom = measuredHeight - checkedIconMargin - checkedIconSize;
      boolean isPreLollipop = VERSION.SDK_INT < VERSION_CODES.LOLLIPOP;
      if (isPreLollipop || materialCardView.getUseCompatPadding()) {
        bottom -= (int) Math.ceil(2f * calculateVerticalBackgroundPadding());
        left -= (int) Math.ceil(2f * calculateHorizontalBackgroundPadding());
      }

      int right = checkedIconMargin;
      if (ViewCompat.getLayoutDirection(materialCardView) == ViewCompat.LAYOUT_DIRECTION_RTL) {
        // swap left and right
        int tmp = right;
        right = left;
        left = tmp;
      }

      clickableForegroundDrawable.setLayerInset(
          CHECKED_ICON_LAYER_INDEX, left, checkedIconMargin /* top */, right, bottom);
    }
  }

  @RequiresApi(api = VERSION_CODES.M)
  void forceRippleRedraw() {
    if (rippleDrawable != null) {
      Rect bounds = rippleDrawable.getBounds();
      // Change the bounds slightly to force the layer to change color, then change the layer again.
      // In API 28 the color for the Ripple is snapshot at the beginning of the animation,
      // it doesn't update when the drawable changes to android:state_checked.
      int bottom = bounds.bottom;
      rippleDrawable.setBounds(bounds.left, bounds.top, bounds.right, bottom - 1);
      rippleDrawable.setBounds(bounds.left, bounds.top, bounds.right, bottom);
    }
  }

  void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    bgDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    bgDrawable.setShadowBitmapDrawingEnable(!bgDrawable.isRoundRect());
    if (foregroundContentDrawable != null) {
      foregroundContentDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }

    if (foregroundShapeDrawable != null) {
      foregroundShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }

    if (compatRippleDrawable != null) {
      compatRippleDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }
  }

  ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
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
  @NonNull
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

      /** Don't force the card to be as big as this drawable */
      @Override
      public int getMinimumWidth() {
        return -1;
      }

      /** Don't force the card to be as big as this drawable */
      @Override
      public int getMinimumHeight() {
        return -1;
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
    return VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && bgDrawable.isRoundRect();
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
            calculateCornerPaddingForCornerTreatment(
                shapeAppearanceModel.getTopLeftCorner(), bgDrawable.getTopLeftCornerResolvedSize()),
            calculateCornerPaddingForCornerTreatment(
                shapeAppearanceModel.getTopRightCorner(),
                bgDrawable.getTopRightCornerResolvedSize())),
        Math.max(
            calculateCornerPaddingForCornerTreatment(
                shapeAppearanceModel.getBottomRightCorner(),
                bgDrawable.getBottomRightCornerResolvedSize()),
            calculateCornerPaddingForCornerTreatment(
                shapeAppearanceModel.getBottomLeftCorner(),
                bgDrawable.getBottomLeftCornerResolvedSize())));
  }

  private float calculateCornerPaddingForCornerTreatment(CornerTreatment treatment, float size) {
    if (treatment instanceof RoundedCornerTreatment) {
      return (float) ((1 - COS_45) * size);
    } else if (treatment instanceof CutCornerTreatment) {
      return size / 2;
    }
    return 0;
  }

  @NonNull
  private Drawable getClickableForeground() {
    if (rippleDrawable == null) {
      rippleDrawable = createForegroundRippleDrawable();
    }

    if (clickableForegroundDrawable == null) {
      Drawable checkedLayer = createCheckedIconLayer();
      clickableForegroundDrawable =
          new LayerDrawable(
              new Drawable[] {rippleDrawable, foregroundContentDrawable, checkedLayer});
      clickableForegroundDrawable.setId(CHECKED_ICON_LAYER_INDEX, R.id.mtrl_card_checked_layer_id);
    }

    return clickableForegroundDrawable;
  }

  @NonNull
  private Drawable createForegroundRippleDrawable() {
    if (RippleUtils.USE_FRAMEWORK_RIPPLE) {
      foregroundShapeDrawable = createForegroundShapeDrawable();
      //noinspection NewApi
      return new RippleDrawable(rippleColor, null, foregroundShapeDrawable);
    }

    return createCompatRippleDrawable();
  }

  @NonNull
  private Drawable createCompatRippleDrawable() {
    StateListDrawable rippleDrawable = new StateListDrawable();
    compatRippleDrawable = createForegroundShapeDrawable();
    compatRippleDrawable.setFillColor(rippleColor);
    rippleDrawable.addState(new int[] {android.R.attr.state_pressed}, compatRippleDrawable);
    return rippleDrawable;
  }

  private void updateRippleColor() {
    //noinspection NewApi
    if (RippleUtils.USE_FRAMEWORK_RIPPLE && rippleDrawable != null) {
      ((RippleDrawable) rippleDrawable).setColor(rippleColor);
    } else if (compatRippleDrawable != null) {
      compatRippleDrawable.setFillColor(rippleColor);
    }
  }

  @NonNull
  private Drawable createCheckedIconLayer() {
    StateListDrawable checkedLayer = new StateListDrawable();
    if (checkedIcon != null) {
      checkedLayer.addState(CHECKED_STATE_SET, checkedIcon);
    }
    return checkedLayer;
  }

  @NonNull
  private MaterialShapeDrawable createForegroundShapeDrawable() {
    return new MaterialShapeDrawable(shapeAppearanceModel);
  }
}
