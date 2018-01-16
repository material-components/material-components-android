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

import android.animation.AnimatorInflater;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StyleRes;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.resources.MaterialResources;
import android.support.design.widget.ViewUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.view.Gravity;

/**
 * A convenience class for creating a new Material button.
 *
 * <p>This class supplies updated Material styles for the button in the constructor. The widget will
 * display the correct default Material styles without the use of the style flag.
 *
 * <p>For filled Material buttons, this class uses your theme's {@code ?attr/colorAccent} for the
 * background fill color and white for the text color. For unfilled buttons, this class uses {@code
 * ?attr/colorAccent} for the text color and transparent for the background.
 *
 * <p>Add icons to the start of this Material Button using the {@code icon}, {@code iconPadding},
 * {@code iconTint} and {@code iconTintMode} attributes.
 *
 * <p>Specify background tint using the {@code buttonBackgroundTint} attribute, which accepts either
 * a color or a color state list.
 *
 * <p>Ripple color / press state color can be specified using the {@code rippleColor} attribute.
 * Ripple opacity will be determined by the Android framework when available. Otherwise, this color
 * will be overlaid on the button at a 50% opacity when button is pressed.
 *
 * <p>Set the stroke color using the {@code strokeColor} attribute, which accepts either a color or
 * a color state list. Stroke width can be set using the {@code strokeWidth} attribute.
 *
 * <p>Specify the radius of all four corners of the button using the {@code cornerRadius} attribute.
 */
public class MaterialButton extends AppCompatButton {

  private final MaterialButtonHelper materialButtonHelper;

  public MaterialButton(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialButton(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    ThemeEnforcement.checkAppCompatTheme(context);

    TypedArray attributes =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.MaterialButton,
            defStyleAttr,
            R.style.Widget_Design_Button_MaterialButton);
    int minWidth = attributes.getDimensionPixelSize(R.styleable.MaterialButton_android_minWidth, 0);
    int minHeight =
        attributes.getDimensionPixelSize(R.styleable.MaterialButton_android_minHeight, 0);

    int padding = attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_padding, 0);
    int paddingLeft =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_paddingLeft, padding);
    int paddingRight =
        attributes.getDimensionPixelOffset(
            R.styleable.MaterialButton_android_paddingRight, padding);
    int paddingTop =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_paddingTop, padding);
    int paddingBottom =
        attributes.getDimensionPixelOffset(
            R.styleable.MaterialButton_android_paddingBottom, padding);
    int insetLeft =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetLeft, 0);
    int insetRight =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetRight, 0);
    int insetTop =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetTop, 0);
    int insetBottom =
        attributes.getDimensionPixelOffset(R.styleable.MaterialButton_android_insetBottom, 0);

    int additionalPaddingLeftForIcon =
        attributes.getDimensionPixelOffset(
            R.styleable.MaterialButton_additionalPaddingLeftForIcon, 0);
    int additionalPaddingRightForIcon =
        attributes.getDimensionPixelOffset(
            R.styleable.MaterialButton_additionalPaddingRightForIcon, 0);

    @StyleRes
    int textAppearanceStyleRes =
        attributes.getResourceId(R.styleable.MaterialButton_android_textAppearance, 0);
    int animatorResId =
        attributes.getResourceId(R.styleable.MaterialButton_android_stateListAnimator, 0);
    boolean isFocusable = attributes.getBoolean(R.styleable.MaterialButton_android_focusable, true);
    boolean isClickable = attributes.getBoolean(R.styleable.MaterialButton_android_clickable, true);
    int iconPadding = attributes.getDimensionPixelSize(R.styleable.MaterialButton_iconPadding, 0);
    int gravity = attributes.getInt(R.styleable.MaterialButton_android_gravity, Gravity.CENTER);
    Mode iconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialButton_iconTintMode, -1), null);

    // Workaround to support VectorDrawables on pre-Lollipop, as there is no compat implementation
    // for icon within AppCompatButton
    ColorStateList iconTint =
        MaterialResources.getColorStateList(
            getContext(), attributes, R.styleable.MaterialButton_iconTint);
    Drawable icon =
        MaterialResources.getDrawable(getContext(), attributes, R.styleable.MaterialButton_icon);

    // Loads and sets background drawable attributes
    materialButtonHelper = new MaterialButtonHelper(this);
    materialButtonHelper.loadFromAttributes(attributes);

    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP && animatorResId != 0) {
      setStateListAnimator(AnimatorInflater.loadStateListAnimator(context, animatorResId));
    }

    TextViewCompat.setTextAppearance(this, textAppearanceStyleRes);
    setMinWidth(minWidth);
    setMinHeight(minHeight);
    setFocusable(isFocusable);
    setClickable(isClickable);
    setCompoundDrawablePadding(iconPadding);
    setGravity(gravity);

    // TODO: Add attributes for elevation/translationZ

    // setPadding() sets padding on button including inset, so we have to add inset and padding
    // attributes to get the button's visible padding to look correct.
    ViewCompat.setPaddingRelative(
        this,
        paddingLeft + (icon != null ? additionalPaddingLeftForIcon : 0) + insetLeft,
        paddingTop + insetTop,
        paddingRight + (icon != null ? additionalPaddingRightForIcon : 0) + insetRight,
        paddingBottom + insetBottom);

    if (icon != null) {
      icon = icon.mutate();
      DrawableCompat.setTintList(icon, iconTint);
      if (iconTintMode != null) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }
    }

    TextViewCompat.setCompoundDrawablesRelativeWithIntrinsicBounds(this, icon, null, null, null);

    attributes.recycle();
  }

  /**
   * This should be accessed via {@link
   * android.support.v4.view.ViewCompat#setBackgroundTintList(android.view.View, ColorStateList)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
    if (materialButtonHelper != null) {
      materialButtonHelper.setSupportBackgroundTintList(tint);
    }
  }

  /**
   * This should be accessed via {@link
   * android.support.v4.view.ViewCompat#getBackgroundTintList(android.view.View)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  @Nullable
  public ColorStateList getSupportBackgroundTintList() {
    return materialButtonHelper != null
        ? materialButtonHelper.getSupportBackgroundTintList()
        : null;
  }

  /**
   * This should be accessed via {@link
   * android.support.v4.view.ViewCompat#setBackgroundTintMode(android.view.View, PorterDuff.Mode)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    // We currently do not support background tint mode for MaterialButton
  }

  /**
   * This should be accessed via {@link
   * android.support.v4.view.ViewCompat#getBackgroundTintMode(android.view.View)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  @Nullable
  public PorterDuff.Mode getSupportBackgroundTintMode() {
    // We currently do not support background tint mode for MaterialButton
    return null;
  }
}
