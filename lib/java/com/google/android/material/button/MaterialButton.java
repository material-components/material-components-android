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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Checkable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/**
 * A convenience class for creating a new Material button.
 *
 * <p>This class supplies updated Material styles for the button in the constructor. The widget will
 * display the correct default Material styles without the use of the style flag.
 *
 * <p>All attributes from {@link com.google.android.material.R.styleable#MaterialButton} are
 * supported. Do not use the {@code android:background} attribute. MaterialButton manages its own
 * background drawable, and setting a new background means {@link MaterialButton} can no longer
 * guarantee that the new attributes it introduces will function properly. If the default background
 * is changed, {@link MaterialButton} cannot guarantee well-defined behavior.
 *
 * <p>For filled buttons, this class uses your theme's {@code ?attr/colorPrimary} for the background
 * tint color and {@code ?attr/colorOnPrimary} for the text color. For unfilled buttons, this class
 * uses {@code ?attr/colorPrimary} for the text color and transparent for the background tint.
 *
 * <p>Add icons to the start or center of this button using the {@link R.attr#icon app:icon}, {@link
 * R.attr#iconPadding app:iconPadding}, {@link R.attr#iconTint app:iconTint}, {@link
 * R.attr#iconTintMode app:iconTintMode} and {@link R.attr#iconGravity app:iconGravity} attributes.
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
public class MaterialButton extends AppCompatButton implements Checkable {

  /** Interface definition for a callback to be invoked when the button checked state changes. */
  public interface OnCheckedChangeListener {
    /**
     * Called when the checked state of a MaterialButton has changed.
     *
     * @param button The MaterialButton whose state has changed.
     * @param isChecked The new checked state of MaterialButton.
     */
    void onCheckedChanged(MaterialButton button, boolean isChecked);
  }

  /** Interface to listen for press state changes on this button. Internal use only. */
  interface OnPressedChangeListener {
    void onPressedChanged(MaterialButton button, boolean isPressed);
  }

  private static final int[] CHECKABLE_STATE_SET = {android.R.attr.state_checkable};
  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

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

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Button;

  @Nullable private final MaterialButtonHelper materialButtonHelper;

  @Px private int iconPadding;
  private Mode iconTintMode;
  private ColorStateList iconTint;
  private Drawable icon;
  @Px private int iconSize;
  @Px private int iconLeft;
  private boolean checked = false;
  private boolean broadcasting = false;

  private final LinkedHashSet<OnCheckedChangeListener> onCheckedChangeListeners =
      new LinkedHashSet<>();
  @Nullable private OnPressedChangeListener onPressedChangeListenerInternal;

  @IconGravity private int iconGravity;

  public MaterialButton(Context context) {
    this(context, null /* attrs */);
  }

  public MaterialButton(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialButton, defStyleAttr, DEF_STYLE_RES);

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
    ShapeAppearanceModel shapeAppearanceModel =
        new ShapeAppearanceModel(context, attrs, defStyleAttr, DEF_STYLE_RES);

    // Loads and sets background drawable attributes
    materialButtonHelper = new MaterialButtonHelper(this, shapeAppearanceModel);
    materialButtonHelper.loadFromAttributes(attributes);

    attributes.recycle();

    setCompoundDrawablePadding(iconPadding);
    updateIcon();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(MaterialButton.class.getName());
    info.setCheckable(isCheckable());
    info.setChecked(isChecked());
    info.setClickable(isClickable());
  }

  @Override
  public void onInitializeAccessibilityEvent(AccessibilityEvent accessibilityEvent) {
    super.onInitializeAccessibilityEvent(accessibilityEvent);
    accessibilityEvent.setClassName(MaterialButton.class.getName());
    accessibilityEvent.setChecked(isChecked());
  }

  /**
   * This should be accessed via {@link
   * androidx.core.view.ViewCompat#setBackgroundTintList(android.view.View, ColorStateList)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintList(@Nullable ColorStateList tint) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintList(tint);
    } else {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tinting
      super.setSupportBackgroundTintList(tint);
    }
  }

  /**
   * This should be accessed via {@link
   * androidx.core.view.ViewCompat#getBackgroundTintList(android.view.View)}
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
   * androidx.core.view.ViewCompat#setBackgroundTintMode(android.view.View, PorterDuff.Mode)}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  public void setSupportBackgroundTintMode(@Nullable PorterDuff.Mode tintMode) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setSupportBackgroundTintMode(tintMode);
    } else {
      // If default MaterialButton background has been overwritten, we will let AppCompatButton
      // handle the tint Mode
      super.setSupportBackgroundTintMode(tintMode);
    }
  }

  /**
   * This should be accessed via {@link
   * androidx.core.view.ViewCompat#getBackgroundTintMode(android.view.View)}
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
    updateIconPosition();
  }

  @Override
  protected void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    super.onTextChanged(charSequence, i, i1, i2);
    updateIconPosition();
  }

  private void updateIconPosition() {
    if (icon == null || iconGravity != ICON_GRAVITY_TEXT_START || getLayout() == null) {
      return;
    }

    Paint textPaint = getPaint();
    String buttonText = getText().toString();
    if (getTransformationMethod() != null) {
      // if text is transformed, add that transformation to to ensure correct calculation
      // of icon padding.
      buttonText = getTransformationMethod().getTransformation(buttonText, this).toString();
    }

    int textWidth = Math.min((int) textPaint.measureText(buttonText), getLayout().getWidth());

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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconPadding
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconPadding
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconSize
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconSize
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_icon
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_icon
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_icon
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconTint
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconTint
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconTint
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconTintMode
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconTintMode
   * @see #setIconTintMode(Mode)
   */
  public Mode getIconTintMode() {
    return iconTintMode;
  }

  /** Updates the icon, icon tint, and icon tint mode for this button. */
  private void updateIcon() {
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_rippleColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_rippleColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_rippleColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeColor
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeWidth
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeWidth
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_strokeWidth
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_cornerRadius
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_cornerRadius
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_cornerRadius
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
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconGravity
   * @see #setIconGravity(int)
   */
  @IconGravity
  public int getIconGravity() {
    return iconGravity;
  }

  /**
   * Sets the icon gravity for this button
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_iconGravity
   * @param iconGravity icon gravity for this button
   * @see #getIconGravity()
   */
  public void setIconGravity(@IconGravity int iconGravity) {
    this.iconGravity = iconGravity;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableState = super.onCreateDrawableState(extraSpace + 2);

    if (isCheckable()) {
      mergeDrawableStates(drawableState, CHECKABLE_STATE_SET);
    }

    if (isChecked()) {
      mergeDrawableStates(drawableState, CHECKED_STATE_SET);
    }

    return drawableState;
  }

  /**
   * Add a listener that will be invoked when the checked state of this MaterialButton changes. See
   * {@link OnCheckedChangeListener}.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnCheckedChangeListener(OnCheckedChangeListener)}.
   *
   * @param listener listener to add
   */
  public void addOnCheckedChangeListener(@NonNull OnCheckedChangeListener listener) {
    onCheckedChangeListeners.add(listener);
  }

  /**
   * Remove a listener that was previously added via {@link
   * #addOnCheckedChangeListener(OnCheckedChangeListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnCheckedChangeListener(@NonNull OnCheckedChangeListener listener) {
    onCheckedChangeListeners.remove(listener);
  }

  /** Remove all previously added {@link OnCheckedChangeListener}s. */
  public void clearOnCheckedChangeListeners() {
    onCheckedChangeListeners.clear();
  }

  @Override
  public void setChecked(boolean checked) {
    if (isCheckable() && isEnabled() && this.checked != checked) {
      this.checked = checked;
      refreshDrawableState();

      // Avoid infinite recursions if setChecked() is called from a listener
      if (broadcasting) {
        return;
      }

      broadcasting = true;
      for (OnCheckedChangeListener listener : onCheckedChangeListeners) {
        listener.onCheckedChanged(this, this.checked);
      }
      broadcasting = false;
    }
  }

  @Override
  public boolean isChecked() {
    return checked;
  }

  @Override
  public void toggle() {
    setChecked(!checked);
  }

  @Override
  public boolean performClick() {
    toggle();

    return super.performClick();
  }

  /**
   * Returns whether this MaterialButton is checkable.
   *
   * @see #setCheckable(boolean)
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_checkable
   */
  public boolean isCheckable() {
    return materialButtonHelper != null && materialButtonHelper.isCheckable();
  }

  /**
   * Sets whether this MaterialButton is checkable.
   *
   * @param checkable Whether this button is checkable.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_checkable
   */
  public void setCheckable(boolean checkable) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setCheckable(checkable);
    }
  }

  void setShapeAppearanceModel(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShapeAppearanceModel(shapeAppearanceModel);
    }
  }

  @Nullable
  ShapeAppearanceModel getShapeAppearanceModel() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getShapeAppearanceModel();
    }

    return null;
  }

  /**
   * Register a callback to be invoked when the pressed state of this button changes. This callback
   * is used for internal purpose only.
   */
  void setOnPressedChangeListenerInternal(@Nullable OnPressedChangeListener listener) {
    onPressedChangeListenerInternal = listener;
  }

  @Override
  public void setPressed(boolean pressed) {
    if (onPressedChangeListenerInternal != null) {
      onPressedChangeListenerInternal.onPressedChanged(this, pressed);
    }
    super.setPressed(pressed);
  }

  private boolean isUsingOriginalBackground() {
    return materialButtonHelper != null && !materialButtonHelper.isBackgroundOverwritten();
  }

  void setShouldDrawSurfaceColorStroke(boolean shouldDrawSurfaceColorStroke) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShouldDrawSurfaceColorStroke(shouldDrawSurfaceColorStroke);
    }
  }
}
