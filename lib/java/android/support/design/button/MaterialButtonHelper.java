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

package android.support.design.button;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.design.resources.MaterialResources;
import android.support.design.ripple.RippleUtils;
import android.support.v4.graphics.ColorUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
class MaterialButtonHelper {

  private static final int[] STATE_PRESSED =
      new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed};
  private static final int[] STATE_ENABLED = new int[] {android.R.attr.state_enabled};
  private static final int[] STATE_EMPTY = new int[] {};

  private final MaterialButton materialButton;

  private int insetLeft;
  private int insetRight;
  private int insetTop;
  private int insetBottom;
  private float cornerRadius;

  @Nullable private ColorStateList backgroundTint;
  @Nullable private ColorStateList strokeColor;
  @Nullable private ColorStateList rippleColor;

  @Nullable private GradientDrawable pressedBackgroundCompat;
  @Nullable private GradientDrawable enabledBackgroundCompat;
  @Nullable private GradientDrawable disabledBackgroundCompat;

  private int strokeWidth;

  public MaterialButtonHelper(MaterialButton button) {
    materialButton = button;
  }

  public void loadFromAttributes(TypedArray attributes) {
    insetLeft = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetLeft, 0);
    insetRight =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetRight, 0);
    insetTop = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetTop, 0);
    insetBottom =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetBottom, 0);
    cornerRadius = attributes.getDimensionPixelSize(R.styleable.MaterialButton_cornerRadius, 0);
    backgroundTint =
        MaterialResources.getColorStateList(
            materialButton.getContext(),
            attributes,
            R.styleable.MaterialButton_buttonBackgroundTint);
    strokeColor =
        MaterialResources.getColorStateList(
            materialButton.getContext(), attributes, R.styleable.MaterialButton_strokeColor);
    rippleColor =
        MaterialResources.getColorStateList(
            materialButton.getContext(), attributes, R.styleable.MaterialButton_rippleColor);
    strokeWidth = attributes.getDimensionPixelSize(R.styleable.MaterialButton_strokeWidth, 0);

    ViewCompat.setBackground(
        materialButton,
        VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP
            ? createBackgroundLollipop()
            : createBackgroundCompat());
  }

  /**
   * Create StateListDrawable background for KitKat (API 19) and earlier API versions
   *
   * @return Drawable representing background for this button.
   */
  private Drawable createBackgroundCompat() {
    ColorStateList backgroundColorStateList = createBackgroundColorCompat();

    pressedBackgroundCompat =
        createGradientDrawableForState(STATE_PRESSED, backgroundColorStateList);
    enabledBackgroundCompat =
        createGradientDrawableForState(STATE_ENABLED, backgroundColorStateList);
    disabledBackgroundCompat =
        createGradientDrawableForState(STATE_EMPTY, backgroundColorStateList);

    StateListDrawable stateListDrawable = new StateListDrawable();
    stateListDrawable.addState(
        STATE_PRESSED, wrapGradientDrawableWithInset(pressedBackgroundCompat));
    stateListDrawable.addState(
        STATE_ENABLED, wrapGradientDrawableWithInset(enabledBackgroundCompat));
    stateListDrawable.addState(
        STATE_EMPTY, wrapGradientDrawableWithInset(disabledBackgroundCompat));

    return stateListDrawable;
  }

  private InsetDrawable wrapGradientDrawableWithInset(GradientDrawable gradientDrawable) {
    return new InsetDrawable(gradientDrawable, insetLeft, insetTop, insetRight, insetBottom);
  }

  private GradientDrawable createGradientDrawableForState(
      int[] state, ColorStateList backgroundColor) {
    GradientDrawable bgDrawable = new GradientDrawable();
    bgDrawable.setCornerRadius(cornerRadius);
    bgDrawable.setColor(backgroundColor.getColorForState(state, 0));
    if (strokeColor != null) {
      bgDrawable.setStroke(strokeWidth, strokeColor.getColorForState(state, 0));
    }
    return bgDrawable;
  }

  private ColorStateList createBackgroundColorCompat() {
    int pressedBackgroundColor =
        ColorUtils.compositeColors(
            rippleColor != null ? rippleColor.getColorForState(STATE_PRESSED, 0) : 0,
            backgroundTint != null ? backgroundTint.getColorForState(STATE_PRESSED, 0) : 0);
    int enabledBackgroundColor = backgroundTint.getColorForState(STATE_ENABLED, 0);
    int disabledBackgroundColor = backgroundTint.getColorForState(STATE_EMPTY, 0);
    return new ColorStateList(
        new int[][] {STATE_PRESSED, STATE_ENABLED, STATE_EMPTY},
        new int[] {pressedBackgroundColor, enabledBackgroundColor, disabledBackgroundColor});
  }

  private void updateBackgroundColorCompat() {
    ColorStateList backgroundColorStateList = createBackgroundColorCompat();
    if (pressedBackgroundCompat != null) {
      pressedBackgroundCompat.setColor(backgroundColorStateList.getColorForState(STATE_PRESSED, 0));
    }
    if (enabledBackgroundCompat != null) {
      enabledBackgroundCompat.setColor(backgroundColorStateList.getColorForState(STATE_ENABLED, 0));
    }
    if (disabledBackgroundCompat != null) {
      disabledBackgroundCompat.setColor(backgroundColorStateList.getColorForState(STATE_EMPTY, 0));
    }
  }

  /**
   * Create RippleDrawable background for Lollipop (API 21) and later API versions
   *
   * @return Drawable representing background for this button.
   */
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private Drawable createBackgroundLollipop() {
    GradientDrawable bgDrawable = new GradientDrawable();
    bgDrawable.setCornerRadius(cornerRadius);
    bgDrawable.setColor(Color.BLACK);
    if (VERSION.SDK_INT == 21) {
      // GradientDrawable doesn't implement setTintList on API 21
      bgDrawable.setColorFilter(
          createTintFilter(backgroundTint, Mode.SRC, materialButton.getDrawableState()));
      // Pre-v23 there is no guarantee that a state change will invoke an invalidation,
      // so we force it ourselves
      bgDrawable.invalidateSelf();
    } else {
      DrawableCompat.setTintMode(bgDrawable, Mode.SRC);
      DrawableCompat.setTintList(bgDrawable, backgroundTint);
    }
    if (strokeColor != null) {
      bgDrawable.setStroke(strokeWidth, strokeColor);
    }
    InsetDrawable bgInsetDrawable =
        new InsetDrawable(bgDrawable, insetLeft, insetTop, insetRight, insetBottom);

    GradientDrawable maskDrawable = new GradientDrawable();
    maskDrawable.setCornerRadius(cornerRadius);
    maskDrawable.setColor(Color.WHITE);
    InsetDrawable maskInsetDrawable = new InsetDrawable(maskDrawable, 0);

    return new RippleDrawable(
        RippleUtils.convertToRippleDrawableColor(rippleColor), bgInsetDrawable, maskInsetDrawable);
  }

  @Nullable
  private PorterDuffColorFilter createTintFilter(
      ColorStateList tint, Mode tintMode, final int[] state) {
    if (tint == null || tintMode == null) {
      return null;
    }
    final int color = tint.getColorForState(state, Color.TRANSPARENT);
    return new PorterDuffColorFilter(color, tintMode);
  }

  void setSupportBackgroundTintList(ColorStateList list) {
    backgroundTint = list;
    updateBackgroundColorCompat();
  }

  ColorStateList getSupportBackgroundTintList() {
    return backgroundTint;
  }
}
