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
import static com.google.android.material.card.MaterialCardView.CHECKED_ICON_GRAVITY_TOP_END;

import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.Gravity;
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
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.card.MaterialCardView.CheckedIconGravity;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RoundedCornerTreatment;
import com.google.android.material.shape.ShapeAppearance;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.StateListShapeAppearanceModel;

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

  private static final int CHECKED_ICON_LAYER_INDEX = 2;

  private static final int NOT_SET = -1;

  // We need to create a dummy drawable to avoid LayerDrawable crashes on API 28-.
  private static final Drawable CHECKED_ICON_NONE =
      VERSION.SDK_INT <= VERSION_CODES.P ? new ColorDrawable() : null;

  @NonNull private final MaterialCardView materialCardView;
  @NonNull private final Rect userContentPadding = new Rect();

  // Will always wrapped in an InsetDrawable
  @NonNull private final MaterialShapeDrawable bgDrawable;

  // Will always wrapped in an InsetDrawable
  @NonNull private final MaterialShapeDrawable foregroundContentDrawable;
  private float cardCornerRadius = NOT_SET;

  @Dimension private int checkedIconMargin;
  @Dimension private int checkedIconSize;
  @CheckedIconGravity private int checkedIconGravity;
  @Dimension private int strokeWidth;

  // If card is clickable, this is the clickableForegroundDrawable otherwise it draws the stroke.
  @Nullable private Drawable fgDrawable;
  @Nullable private Drawable checkedIcon;
  @Nullable private ColorStateList rippleColor;
  @Nullable private ColorStateList checkedIconTint;
  @NonNull private ShapeAppearance shapeAppearanceModel;
  @Nullable private ColorStateList strokeColor;
  @Nullable private Drawable rippleDrawable;
  @Nullable private LayerDrawable clickableForegroundDrawable;
  @Nullable private MaterialShapeDrawable foregroundShapeDrawable;

  private boolean isBackgroundOverwritten = false;
  private boolean checkable;

  @Nullable private ValueAnimator iconAnimator;
  private final TimeInterpolator iconFadeAnimInterpolator;
  private final int iconFadeInAnimDuration;
  private final int iconFadeOutAnimDuration;
  private float checkedAnimationProgress = 0F;

  public static final int DEFAULT_FADE_ANIM_DURATION = 300;

  public MaterialCardViewHelper(
      @NonNull MaterialCardView card,
      AttributeSet attrs,
      int defStyleAttr,
      @StyleRes int defStyleRes) {
    materialCardView = card;

    TypedArray cardViewAttributes =
        card.getContext()
            .obtainStyledAttributes(
                attrs,
                androidx.cardview.R.styleable.CardView,
                defStyleAttr,
                androidx.cardview.R.style.CardView);
    bgDrawable = new MaterialShapeDrawable(card.getContext(), attrs, defStyleAttr, defStyleRes);
    bgDrawable.initializeElevationOverlay(card.getContext());
    bgDrawable.setShadowColor(Color.DKGRAY);
    ShapeAppearanceModel.Builder shapeAppearanceModelBuilder =
        bgDrawable.getShapeAppearanceModel().toBuilder();

    if (cardViewAttributes.hasValue(androidx.cardview.R.styleable.CardView_cardCornerRadius)) {
      // If cardCornerRadius is set, remember it so we can let it override the shape appearance
      cardCornerRadius = cardViewAttributes.getDimension(
          androidx.cardview.R.styleable.CardView_cardCornerRadius, 0);
      shapeAppearanceModelBuilder.setAllCornerSizes(cardCornerRadius);
    }

    foregroundContentDrawable = new MaterialShapeDrawable();
    setShapeAppearance(shapeAppearanceModelBuilder.build());

    iconFadeAnimInterpolator =
        MotionUtils.resolveThemeInterpolator(
            materialCardView.getContext(),
            R.attr.motionEasingLinearInterpolator,
            AnimationUtils.LINEAR_INTERPOLATOR);
    iconFadeInAnimDuration =
        MotionUtils.resolveThemeDuration(
            materialCardView.getContext(), R.attr.motionDurationShort2, DEFAULT_FADE_ANIM_DURATION);
    iconFadeOutAnimDuration =
        MotionUtils.resolveThemeDuration(
            materialCardView.getContext(), R.attr.motionDurationShort1, DEFAULT_FADE_ANIM_DURATION);

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
    checkedIconGravity =
        attributes.getInteger(
            R.styleable.MaterialCardView_checkedIconGravity, CHECKED_ICON_GRAVITY_TOP_END);

    rippleColor =
        MaterialResources.getColorStateList(
            materialCardView.getContext(), attributes, R.styleable.MaterialCardView_rippleColor);
    if (rippleColor == null) {
      rippleColor =
          ColorStateList.valueOf(
              MaterialColors.getColor(
                  materialCardView, androidx.appcompat.R.attr.colorControlHighlight));
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
        shouldUseClickableForeground() ? getClickableForeground() : foregroundContentDrawable;
    materialCardView.setForeground(insetDrawable(fgDrawable));

    // Card corner radius overrides the shape appearance in precedence.
    if (cardCornerRadius == NOT_SET) {
      StateListShapeAppearanceModel stateListShapeAppearanceModel =
          StateListShapeAppearanceModel.create(
              materialCardView.getContext(),
              attributes,
              R.styleable.MaterialCardView_shapeAppearance);
      if (stateListShapeAppearanceModel != null) {
        SpringForce springForce = createSpringForce(materialCardView.getContext());
        bgDrawable.setCornerSpringForce(springForce);
        foregroundContentDrawable.setCornerSpringForce(springForce);
        if (foregroundShapeDrawable != null) {
          foregroundShapeDrawable.setCornerSpringForce(springForce);
        }
        setShapeAppearance(stateListShapeAppearanceModel);
      }
    }
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
        shouldUseClickableForeground() ? getClickableForeground() : foregroundContentDrawable;
    if (previousFgDrawable != fgDrawable) {
      updateInsetForeground(fgDrawable);
    }
  }

  public void animateCheckedIcon(boolean checked) {
    float targetCheckedProgress = checked ? 1F : 0F;
    float delta = checked ? 1F - checkedAnimationProgress : checkedAnimationProgress;
    if (iconAnimator != null) {
      iconAnimator.cancel();
      iconAnimator = null;
    }
    iconAnimator = ValueAnimator.ofFloat(checkedAnimationProgress, targetCheckedProgress);
    iconAnimator.addUpdateListener(
        animation -> {
          float progress = (float) animation.getAnimatedValue();
          int alpha = (int) (255F * progress);
          checkedIcon.setAlpha(alpha);
          checkedAnimationProgress = progress;
        });
    iconAnimator.setInterpolator(iconFadeAnimInterpolator);
    // Cut the total duration if this animation is starting after interrupting an in-progress
    // animation.
    iconAnimator.setDuration(
        checked
            ? (long) (iconFadeInAnimDuration * delta)
            : (long) (iconFadeOutAnimDuration * delta));
    iconAnimator.start();
  }

  void setCornerRadius(float cornerRadius) {
    cardCornerRadius = cornerRadius;
    setShapeAppearance(
        shapeAppearanceModel.getDefaultShape().withCornerSize(cornerRadius));
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
      checkedIcon.setTintList(checkedIconTint);
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
    if (checkedIcon != null) {
      this.checkedIcon = DrawableCompat.wrap(checkedIcon).mutate();
      this.checkedIcon.setTintList(checkedIconTint);
      setChecked(materialCardView.isChecked());
    } else {
      this.checkedIcon = CHECKED_ICON_NONE;
    }

    if (clickableForegroundDrawable != null) {
      clickableForegroundDrawable.setDrawableByLayerId(
          R.id.mtrl_card_checked_layer_id, this.checkedIcon);
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

  void recalculateCheckedIconPosition(int measuredWidth, int measuredHeight) {
    if (clickableForegroundDrawable != null) {
      int verticalPaddingAdjustment = 0;
      int horizontalPaddingAdjustment = 0;
      if (materialCardView.getUseCompatPadding()) {
        verticalPaddingAdjustment = (int) Math.ceil(2f * calculateVerticalBackgroundPadding());
        horizontalPaddingAdjustment = (int) Math.ceil(2f * calculateHorizontalBackgroundPadding());
      }

      int left =
          isCheckedIconEnd()
              ? measuredWidth - checkedIconMargin - checkedIconSize - horizontalPaddingAdjustment
              : checkedIconMargin;
      int bottom =
          isCheckedIconBottom()
              ? checkedIconMargin
              : measuredHeight - checkedIconMargin - checkedIconSize - verticalPaddingAdjustment;

      int right =
          isCheckedIconEnd()
              ? checkedIconMargin
              : measuredWidth - checkedIconMargin - checkedIconSize - horizontalPaddingAdjustment;
      int top =
          isCheckedIconBottom()
              ? measuredHeight - checkedIconMargin - checkedIconSize - verticalPaddingAdjustment
              : checkedIconMargin;

      if (materialCardView.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
        // swap left and right
        int tmp = right;
        right = left;
        left = tmp;
      }

      clickableForegroundDrawable.setLayerInset(CHECKED_ICON_LAYER_INDEX, left, top, right, bottom);
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

  void setShapeAppearance(@NonNull ShapeAppearance shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    bgDrawable.setShapeAppearance(shapeAppearanceModel);
    foregroundContentDrawable.setShapeAppearance(shapeAppearanceModel);
    if (foregroundShapeDrawable != null) {
      foregroundShapeDrawable.setShapeAppearance(shapeAppearanceModel);
    }
    bgDrawable.setShadowBitmapDrawingEnable(!bgDrawable.isRoundRect());
  }

  @NonNull
  ShapeAppearance getShapeAppearance() {
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
    if (materialCardView.getUseCompatPadding()) {
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
    return bgDrawable.isRoundRect();
  }

  private float getParentCardViewCalculatedCornerPadding() {
    if (materialCardView.getPreventCornerOverlap() && materialCardView.getUseCompatPadding()) {
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

  private float getMaxCornerPadding(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
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

  /**
   * Calculates the amount of padding required between the card background shape and the card
   * content such that the entire content is within the bounds of the card background shape.
   *
   * <p>This should only be called when either {@link
   * #shouldAddCornerPaddingOutsideCardBackground()} or {@link
   * #shouldAddCornerPaddingInsideCardBackground()} returns true.
   */
  private float calculateActualCornerPadding() {
    float maxCornerPadding = 0;
    ShapeAppearanceModel[] shapeAppearanceModels =
        shapeAppearanceModel.getShapeAppearanceModels();
    for (ShapeAppearanceModel shapeAppearanceModel : shapeAppearanceModels) {
      if (shapeAppearanceModel != null) {
        maxCornerPadding = Math.max(maxCornerPadding, getMaxCornerPadding(shapeAppearanceModel));
      }
    }
    return maxCornerPadding;
  }

  private float calculateCornerPaddingForCornerTreatment(CornerTreatment treatment, float size) {
    if (treatment instanceof RoundedCornerTreatment) {
      return (float) ((1 - COS_45) * size);
    } else if (treatment instanceof CutCornerTreatment) {
      return size / 2;
    }
    return 0;
  }

  private boolean shouldUseClickableForeground() {
    if (materialCardView.isClickable()) {
      return true;
    }
    View view = materialCardView;
    while (view.isDuplicateParentStateEnabled() && view.getParent() instanceof View) {
      view = (View) view.getParent();
    }
    return view.isClickable();
  }

  @NonNull
  private Drawable getClickableForeground() {
    if (rippleDrawable == null) {
      rippleDrawable = createForegroundRippleDrawable();
    }

    if (clickableForegroundDrawable == null) {
      clickableForegroundDrawable =
          new LayerDrawable(
              new Drawable[] {rippleDrawable, foregroundContentDrawable, checkedIcon});
      clickableForegroundDrawable.setId(CHECKED_ICON_LAYER_INDEX, R.id.mtrl_card_checked_layer_id);
    }

    return clickableForegroundDrawable;
  }

  @NonNull
  private Drawable createForegroundRippleDrawable() {
    foregroundShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    return new RippleDrawable(rippleColor, null, foregroundShapeDrawable);
  }

  private void updateRippleColor() {
    if (rippleDrawable != null) {
      ((RippleDrawable) rippleDrawable).setColor(rippleColor);
    }
  }

  public void setChecked(boolean checked) {
    setChecked(checked, /* animate= */ false);
  }

  public void setChecked(boolean checked, boolean animate) {
    if (checkedIcon != null) {
      if (animate) {
        animateCheckedIcon(checked);
      } else {
        checkedIcon.setAlpha(checked ? 255 : 0);
        checkedAnimationProgress = checked ? 1F : 0F;
      }
    }
  }

  @CheckedIconGravity
  int getCheckedIconGravity() {
    return checkedIconGravity;
  }

  void setCheckedIconGravity(@CheckedIconGravity int checkedIconGravity) {
    this.checkedIconGravity = checkedIconGravity;
    recalculateCheckedIconPosition(
        materialCardView.getMeasuredWidth(), materialCardView.getMeasuredHeight());
  }

  private boolean isCheckedIconEnd() {
    return (checkedIconGravity & Gravity.END) == Gravity.END;
  }

  private boolean isCheckedIconBottom() {
    return (checkedIconGravity & Gravity.BOTTOM) == Gravity.BOTTOM;
  }

  @NonNull
  private SpringForce createSpringForce(@NonNull Context context) {
    return MotionUtils.resolveThemeSpringForce(
        context,
        R.attr.motionSpringFastSpatial,
        R.style.Motion_Material3_Spring_Standard_Fast_Spatial);
  }
}
