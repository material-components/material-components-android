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

package com.google.android.material.button;

import com.google.android.material.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.DimenRes;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.Px;
import android.support.annotation.RestrictTo;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A convenience class for creating a new Material button.
 *
 * <p>This class supplies updated Material styles for the button in the constructor. The widget will
 * display the correct default Material styles without the use of the style flag.
 *
 * <p>All attributes from {@link com.google.android.material.button.R.styleable#MaterialButton} are
 * supported. Do not use the {@code android:background} attribute. MaterialButton manages its own
 * background drawable, and setting a new background means {@link MaterialButton} can no longer
 * guarantee that the new attributes it introduces will function properly. If the default background
 * is changed, {@link MaterialButton} cannot guarantee well-defined behavior.
 *
 * <p>For filled buttons, this class uses your theme's {@code ?attr/colorAccent} for the background
 * tint color and white for the text color. For unfilled buttons, this class uses {@code
 * ?attr/colorAccent} for the text color and transparent for the background tint.
 *
 * <p>Add icons to the start or center of this button of this button using the {@link R.attr#icon
 * app:icon}, {@link R.attr#iconPadding app:iconPadding}, {@link R.attr#iconTint app:iconTint},
 * {@link R.attr#iconTintMode app:iconTintMode} and {@link R.attr#iconGravity app:iconGravity}
 * attributes.
 *
 * <p>Specify background tint using the {@link R.attr#backgroundTint app:backgroundTint} and {@link
 * R.attr#backgroundTintMode app:backgroundTintMode} attributes, which accepts either a color or a
 * color state list.
 *
 * <p>Ripple color / press state color can be specified using the {@link R.attr#rippleColor
 * app:rippleColor} attribute. Ripple opacity will be determined by the Android framework when
 * available. Otherwise, this color will be overlaid on the button at a 50% opacity when button is
 * pressed.
 *
 * <p>Set the stroke color using the {@link R.attr#strokeColor app:strokeColor} attribute, which
 * accepts either a color or a color state list. Stroke width can be set using the {@link
 * R.attr#strokeWidth app:strokeWidth} attribute.
 *
 * <p>Specify the radius of all four corners of the button using the {@link R.attr#cornerRadius
 * app:cornerRadius} attribute.
 */
public class MaterialButton extends AppCompatButton {

  /**
   * Gravity used to position the icon at the start of the view.
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_START = 0x1;

  /**
   * Gravity used to position the icon in the center of the view at the start of the text
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_TEXT_START = 0x2;


  /** Positions the icon can be set to. */
  @IntDef({ICON_GRAVITY_START, ICON_GRAVITY_TEXT_START})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IconGravity {}

  private static final String LOG_TAG = "MaterialButton";

  @Nullable private final MaterialButtonHelper materialButtonHelper;

  @Px private int iconPadding;
  private Mode iconTintMode;
  private ColorStateList iconTint;
  private Drawable icon;
  @Px private int iconSize;
  @Px private int iconLeft;

  @IconGravity private int iconGravity;

  public MaterialButton(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialButton(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.MaterialButton,
            defStyleAttr,
            R.style.Widget_MaterialComponents_Button);

    iconPadding = attributes.getDimensionPixelSize(R.styleable.MaterialButton_iconPadding, 0);
    iconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialButton_iconTintMode, -1), Mode.SRC_IN);

    iconTint =
        MaterialResources.getColorStateList(
            getContext(), attributes, R.styleable.MaterialButton_iconTint);
    icon = MaterialResources.getDrawable(getContext(), attributes, R.styleable.MaterialButton_icon);
    iconGravity = attributes.getInteger(R.styleable.MaterialButton_iconGravity, ICON_GRAVITY_START);

    iconSize = attributes.getDimensionPixelSize(R.styleable.MaterialButton_iconSize, 0);

    // Loads and sets background drawable attributes
    materialButtonHelper = new MaterialButtonHelper(this);
    materialButtonHelper.loadFromAttributes(attributes);

    attributes.recycle();

    setCompoundDrawablePadding(iconPadding);
    updateIcon();
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    // Manually draw stroke on top of background for Kit Kat (API 19) and earlier versions
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP && isUsingOriginalBackground()) {
      materialButtonHelper.drawStroke(canvas);
    }
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
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintList(tint);
    } else if (materialButtonHelper != null) {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tinting
      super.setSupportBackgroundTintList(tint);
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
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getSupportBackgroundTintList();
    } else {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tinting
      // return null;
      return super.getSupportBackgroundTintList();
    }
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
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintMode(tintMode);
    } else if (materialButtonHelper != null) {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tint Mode
      super.setSupportBackgroundTintMode(tintMode);
    }
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
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getSupportBackgroundTintMode();
    } else {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tint mode
      return super.getSupportBackgroundTintMode();
    }
  }

  @Override
  public void setBackgroundTintList(@Nullable ColorStateList tintList) {
    setSupportBackgroundTintList(tintList);
  }

  @Nullable
  @Override
  public ColorStateList getBackgroundTintList() {
    return getSupportBackgroundTintList();
  }

  @Override
  public void setBackgroundTintMode(@Nullable Mode tintMode) {
    setSupportBackgroundTintMode(tintMode);
  }

  @Nullable
  @Override
  public Mode getBackgroundTintMode() {
    return getSupportBackgroundTintMode();
  }

  @Override
  public void setBackgroundColor(@ColorInt int color) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setBackgroundColor(color);
    } else {
      // If default MaterialButton background has been overwritten, we will let View handle
      // setting the background color.
      super.setBackgroundColor(color);
    }
  }

  @Override
  public void setBackground(Drawable background) {
    setBackgroundDrawable(background);
  }

  @Override
  public void setBackgroundResource(@DrawableRes int backgroundResourceId) {
    Drawable background = null;
    if (backgroundResourceId != 0) {
      background = AppCompatResources.getDrawable(getContext(), backgroundResourceId);
    }
    setBackgroundDrawable(background);
  }

  @Override
  public void setBackgroundDrawable(Drawable background) {
    if (isUsingOriginalBackground()) {
      if (background != this.getBackground()) {
        Log.i(LOG_TAG, "Setting a custom background is not supported.");
        materialButtonHelper.setBackgroundOverwritten();
        super.setBackgroundDrawable(background);
      } else {
        // ViewCompat.setBackgroundTintList() and setBackgroundTintMode() call setBackground() on
        // the view in API 21, since background state doesn't automatically update in API 21. We
        // capture this case here, and update our background without replacing it or re-tinting it.
        getBackground().setState(background.getState());
      }
    } else {
      super.setBackgroundDrawable(background);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    // Workaround for API 21 ripple bug (possibly internal in GradientDrawable)
    if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP && materialButtonHelper != null) {
      materialButtonHelper.updateMaskBounds(bottom - top, right - left);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    if (icon == null || iconGravity != ICON_GRAVITY_TEXT_START) {
      return;
    }

    Paint textPaint = getPaint();
    int textWidth = (int) textPaint.measureText(getText().toString());
    int localIconSize = iconSize == 0 ? icon.getIntrinsicWidth() : iconSize;
    int newIconLeft =
        (getMeasuredWidth()
                - textWidth
                - ViewCompat.getPaddingEnd(this)
                - localIconSize
                - iconPadding
                - ViewCompat.getPaddingStart(this))
            / 2;

    if (isLayoutRTL()) {
      newIconLeft = -newIconLeft;
    }

    if (iconLeft != newIconLeft) {
      iconLeft = newIconLeft;
      updateIcon();
    }
  }

  private boolean isLayoutRTL() {
    return ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }


  /**
   * Update the button's background without changing the background state in {@link
   * MaterialButtonHelper}. This should be used when we initially set the background drawable
   * created by {@link MaterialButtonHelper}.
   *
   * @param background Background to set on this button
   */
  void setInternalBackground(Drawable background) {
    super.setBackgroundDrawable(background);
  }

  /**
   * Sets the padding between the button icon and the button text, if icon is present.
   *
   * @param iconPadding Padding between the button icon and the button text, if icon is present.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconPadding
   * @see #getIconPadding()
   */
  public void setIconPadding(@Px int iconPadding) {
    if (this.iconPadding != iconPadding) {
      this.iconPadding = iconPadding;
      setCompoundDrawablePadding(iconPadding);
    }
  }

  /**
   * Gets the padding between the button icon and the button text, if icon is present.
   *
   * @return Padding between the button icon and the button text, if icon is present.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconPadding
   * @see #setIconPadding(int)
   */
  @Px
  public int getIconPadding() {
    return iconPadding;
  }

  /**
   * Sets the width and height of the icon. Use 0 to use source Drawable size.
   *
   * @param iconSize new dimension for width and height of the icon in pixels.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconSize
   * @see #getIconSize()
   */
  public void setIconSize(@Px int iconSize) {
    if (iconSize < 0) {
      throw new IllegalArgumentException("iconSize cannot be less than 0");
    }

    if (this.iconSize != iconSize) {
      this.iconSize = iconSize;
      updateIcon();
    }
  }

  /**
   * Returns the size of the icon if it was set.
   *
   * @return Returns the size of the icon if it was set in pixels, 0 otherwise.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconSize
   * @see #setIconSize(int)
   */
  @Px
  public int getIconSize() {
    return iconSize;
  }

  /**
   * Sets the icon to show for this button. By default, this icon will be shown on the left side of
   * the button.
   *
   * @param icon Drawable to use for the button's icon.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_icon
   * @see #setIconResource(int)
   * @see #getIcon()
   */
  public void setIcon(Drawable icon) {
    if (this.icon != icon) {
      this.icon = icon;
      updateIcon();
    }
  }
  /**
   * Sets the icon drawable resource to show for this button. By default, this icon will be shown on
   * the left side of the button.
   *
   * @param iconResourceId Drawable resource ID to use for the button's icon.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_icon
   * @see #setIcon(Drawable)
   * @see #getIcon()
   */
  public void setIconResource(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setIcon(icon);
  }

  /**
   * Gets the icon shown for this button, if present.
   *
   * @return Icon shown for this button, if present.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_icon
   * @see #setIcon(Drawable)
   * @see #setIconResource(int)
   */
  public Drawable getIcon() {
    return icon;
  }

  /**
   * Sets the tint list for the icon shown for this button.
   *
   * @param iconTint Tint list for the icon shown for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconTint
   * @see #setIconTintResource(int)
   * @see #getIconTint()
   */
  public void setIconTint(@Nullable ColorStateList iconTint) {
    if (this.iconTint != iconTint) {
      this.iconTint = iconTint;
      updateIcon();
    }
  }

  /**
   * Sets the tint list color resource for the icon shown for this button.
   *
   * @param iconTintResourceId Tint list color resource for the icon shown for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconTint
   * @see #setIconTint(ColorStateList)
   * @see #getIconTint()
   */
  public void setIconTintResource(@ColorRes int iconTintResourceId) {
    setIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  /**
   * Gets the tint list for the icon shown for this button.
   *
   * @return Tint list for the icon shown for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconTint
   * @see #setIconTint(ColorStateList)
   * @see #setIconTintResource(int)
   */
  public ColorStateList getIconTint() {
    return iconTint;
  }

  /**
   * Sets the tint mode for the icon shown for this button.
   *
   * @param iconTintMode Tint mode for the icon shown for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconTintMode
   * @see #getIconTintMode()
   */
  public void setIconTintMode(Mode iconTintMode) {
    if (this.iconTintMode != iconTintMode) {
      this.iconTintMode = iconTintMode;
      updateIcon();
    }
  }

  /**
   * Gets the tint mode for the icon shown for this button.
   *
   * @return Tint mode for the icon shown for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconTintMode
   * @see #setIconTintMode(Mode)
   */
  public Mode getIconTintMode() {
    return iconTintMode;
  }

  /** Updates the icon, icon tint, and icon tint mode for this button. */
  private void updateIcon() {
    if (icon != null) {
      icon = icon.mutate();
      DrawableCompat.setTintList(icon, iconTint);
      if (iconTintMode != null) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }

      int width = iconSize != 0 ? iconSize : icon.getIntrinsicWidth();
      int height = iconSize != 0 ? iconSize : icon.getIntrinsicHeight();
      icon.setBounds(iconLeft, 0, iconLeft + width, height);
    }

    TextViewCompat.setCompoundDrawablesRelative(this, icon, null, null, null);
  }

  /**
   * Sets the ripple color for this button.
   *
   * @param rippleColor Color to use for the ripple.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_rippleColor
   * @see #setRippleColorResource(int)
   * @see #getRippleColor()
   */
  public void setRippleColor(@Nullable ColorStateList rippleColor) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setRippleColor(rippleColor);
    }
  }

  /**
   * Sets the ripple color resource for this button.
   *
   * @param rippleColorResourceId Color resource to use for the ripple.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_rippleColor
   * @see #setRippleColor(ColorStateList)
   * @see #getRippleColor()
   */
  public void setRippleColorResource(@ColorRes int rippleColorResourceId) {
    if (isUsingOriginalBackground()) {
      setRippleColor(AppCompatResources.getColorStateList(getContext(), rippleColorResourceId));
    }
  }

  /**
   * Gets the ripple color for this button.
   *
   * @return The color used for the ripple.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_rippleColor
   * @see #setRippleColor(ColorStateList)
   * @see #setRippleColorResource(int)
   */
  public ColorStateList getRippleColor() {
    return isUsingOriginalBackground() ? materialButtonHelper.getRippleColor() : null;
  }

  /**
   * Sets the stroke color for this button. Both stroke color and stroke width must be set for a
   * stroke to be drawn.
   *
   * @param strokeColor Color to use for the stroke.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeColor
   * @see #setStrokeColorResource(int)
   * @see #getStrokeColor()
   */
  public void setStrokeColor(@Nullable ColorStateList strokeColor) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setStrokeColor(strokeColor);
    }
  }

  /**
   * Sets the stroke color resource for this button. Both stroke color and stroke width must be set
   * for a stroke to be drawn.
   *
   * @param strokeColorResourceId Color resource to use for the stroke.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeColor
   * @see #setStrokeColor(ColorStateList)
   * @see #getStrokeColor()
   */
  public void setStrokeColorResource(@ColorRes int strokeColorResourceId) {
    if (isUsingOriginalBackground()) {
      setStrokeColor(AppCompatResources.getColorStateList(getContext(), strokeColorResourceId));
    }
  }

  /**
   * Gets the stroke color for this button.
   *
   * @return The color used for the stroke.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeColor
   * @see #setStrokeColor(ColorStateList)
   * @see #setStrokeColorResource(int)
   */
  public ColorStateList getStrokeColor() {
    return isUsingOriginalBackground() ? materialButtonHelper.getStrokeColor() : null;
  }

  /**
   * Sets the stroke width for this button. Both stroke color and stroke width must be set for a
   * stroke to be drawn.
   *
   * @param strokeWidth Stroke width for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeWidth
   * @see #setStrokeWidthResource(int)
   * @see #getStrokeWidth()
   */
  public void setStrokeWidth(@Px int strokeWidth) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setStrokeWidth(strokeWidth);
    }
  }

  /**
   * Sets the stroke width dimension resource for this button. Both stroke color and stroke width
   * must be set for a stroke to be drawn.
   *
   * @param strokeWidthResourceId Stroke width dimension resource for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeWidth
   * @see #setStrokeWidth(int)
   * @see #getStrokeWidth()
   */
  public void setStrokeWidthResource(@DimenRes int strokeWidthResourceId) {
    if (isUsingOriginalBackground()) {
      setStrokeWidth(getResources().getDimensionPixelSize(strokeWidthResourceId));
    }
  }

  /**
   * Gets the stroke width for this button.
   *
   * @return Stroke width for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_strokeWidth
   * @see #setStrokeWidth(int)
   * @see #setStrokeWidthResource(int)
   */
  @Px
  public int getStrokeWidth() {
    return isUsingOriginalBackground() ? materialButtonHelper.getStrokeWidth() : 0;
  }

  /**
   * Sets the corner radius for this button.
   *
   * @param cornerRadius Corner radius for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_cornerRadius
   * @see #setCornerRadiusResource(int)
   * @see #getCornerRadius()
   */
  public void setCornerRadius(@Px int cornerRadius) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setCornerRadius(cornerRadius);
    }
  }

  /**
   * Sets the corner radius dimension resource for this button.
   *
   * @param cornerRadiusResourceId Corner radius dimension resource for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_cornerRadius
   * @see #setCornerRadius(int)
   * @see #getCornerRadius()
   */
  public void setCornerRadiusResource(@DimenRes int cornerRadiusResourceId) {
    if (isUsingOriginalBackground()) {
      setCornerRadius(getResources().getDimensionPixelSize(cornerRadiusResourceId));
    }
  }

  /**
   * Gets the corner radius for this button.
   *
   * @return Corner radius for this button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_cornerRadius
   * @see #setCornerRadius(int)
   * @see #setCornerRadiusResource(int)
   */
  @Px
  public int getCornerRadius() {
    return isUsingOriginalBackground() ? materialButtonHelper.getCornerRadius() : 0;
  }

  /**
   * Gets the icon gravity for this button
   *
   * @return Icon gravity of the button.
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconGravity
   * @see #setIconGravity(int)
   */
  @IconGravity
  public int getIconGravity() {
    return iconGravity;
  }

  /**
   * Sets the icon gravity for this button
   *
   * @attr ref com.google.android.material.button.R.styleable#MaterialButton_iconGravity
   * @param iconGravity icon gravity for this button
   * @see #getIconGravity()
   */
  public void setIconGravity(@IconGravity int iconGravity) {
    this.iconGravity = iconGravity;
  }

  private boolean isUsingOriginalBackground() {
    return materialButtonHelper != null && !materialButtonHelper.isBackgroundOverwritten();
  }
}
