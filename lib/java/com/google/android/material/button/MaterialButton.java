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
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.core.widget.TextViewCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import android.util.AttributeSet;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
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
 * <p>Add icons to the start, center, or end of this button using the {@link R.attr#icon app:icon},
 * {@link R.attr#iconPadding app:iconPadding}, {@link R.attr#iconTint app:iconTint}, {@link
 * R.attr#iconTintMode app:iconTintMode} and {@link R.attr#iconGravity app:iconGravity} attributes.
 *
 * <p>If a start-aligned icon is added to this button, please use a style like one of the ".Icon"
 * styles specified in the default MaterialButton styles. The ".Icon" styles adjust padding slightly
 * to achieve a better visual balance. This style should only be used with a start-aligned icon
 * button. If your icon is end-aligned, you cannot use a ".Icon" style and must instead manually
 * adjust your padding such that the visual adjustment is mirrored.
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
public class MaterialButton extends AppCompatButton implements Checkable, Shapeable {

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

  /**
   * Gravity used to position the icon at the end of the view.
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_END = 0x3;

  /**
   * Gravity used to position the icon in the center of the view at the end of the text
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_TEXT_END = 0x4;

  /**
   * Gravity used to position the icon at the top of the view.
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_TOP = 0x10;

  /**
   * Gravity used to position the icon in the center of the view at the top of the text
   *
   * @see #setIconGravity(int)
   * @see #getIconGravity()
   */
  public static final int ICON_GRAVITY_TEXT_TOP = 0x20;

  /** Positions the icon can be set to. */
  @IntDef({
      ICON_GRAVITY_START,
      ICON_GRAVITY_TEXT_START,
      ICON_GRAVITY_END,
      ICON_GRAVITY_TEXT_END,
      ICON_GRAVITY_TOP,
      ICON_GRAVITY_TEXT_TOP
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface IconGravity {}

  private static final String LOG_TAG = "MaterialButton";

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Button;

  @NonNull private final MaterialButtonHelper materialButtonHelper;
  @NonNull private final LinkedHashSet<OnCheckedChangeListener> onCheckedChangeListeners =
      new LinkedHashSet<>();

  @Nullable private OnPressedChangeListener onPressedChangeListenerInternal;
  @Nullable private Mode iconTintMode;
  @Nullable private ColorStateList iconTint;
  @Nullable private Drawable icon;

  @Px private int iconSize;
  @Px private int iconLeft;
  @Px private int iconTop;
  @Px private int iconPadding;

  private boolean checked = false;
  private boolean broadcasting = false;
  @IconGravity private int iconGravity;

  public MaterialButton(@NonNull Context context) {
    this(context, null /* attrs */);
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
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
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();

    // Loads and sets background drawable attributes
    materialButtonHelper = new MaterialButtonHelper(this, shapeAppearanceModel);
    materialButtonHelper.loadFromAttributes(attributes);

    attributes.recycle();

    setCompoundDrawablePadding(iconPadding);
    updateIcon(/*needsIconReset=*/icon != null);
  }

  @NonNull
  private String getA11yClassName() {
    // Use the platform widget classes so Talkback can recognize this as a button.
    return (isCheckable() ? CompoundButton.class : Button.class).getName();
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    info.setClassName(getA11yClassName());
    info.setCheckable(isCheckable());
    info.setChecked(isChecked());
    info.setClickable(isClickable());
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent accessibilityEvent) {
    super.onInitializeAccessibilityEvent(accessibilityEvent);
    accessibilityEvent.setClassName(getA11yClassName());
    accessibilityEvent.setChecked(isChecked());
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.checked = checked;
    return savedState;
  }

  @Override
  public void onRestoreInstanceState(@Nullable Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    setChecked(savedState.checked);
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
  public void setBackground(@NonNull Drawable background) {
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
  public void setBackgroundDrawable(@NonNull Drawable background) {
    if (isUsingOriginalBackground()) {
      if (background != this.getBackground()) {
        Log.w(
            LOG_TAG,
            "MaterialButton manages its own background to control elevation, shape, color and"
                + " states. Consider using backgroundTint, shapeAppearance and other attributes"
                + " where available. A custom background will ignore these attributes and you"
                + " should consider handling interaction states such as pressed, focused and"
                + " disabled");
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
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    updateIconPosition(w, h);
  }

  @Override
  protected void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    super.onTextChanged(charSequence, i, i1, i2);
    updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isUsingOriginalBackground()) {
      MaterialShapeUtils.setParentAbsoluteElevation(
          this, materialButtonHelper.getMaterialShapeDrawable());
    }
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (isUsingOriginalBackground()) {
      materialButtonHelper.getMaterialShapeDrawable().setElevation(elevation);
    }
  }

  private void updateIconPosition(int buttonWidth, int buttonHeight) {
    if (icon == null || getLayout() == null) {
      return;
    }

    if (isIconStart() || isIconEnd()) {
      iconTop = 0;
      if (iconGravity == ICON_GRAVITY_START || iconGravity == ICON_GRAVITY_END) {
        iconLeft = 0;
        updateIcon(/* needsIconReset = */ false);
        return;
      }

      int localIconSize = iconSize == 0 ? icon.getIntrinsicWidth() : iconSize;
      int newIconLeft =
          (buttonWidth
              - getTextWidth()
              - ViewCompat.getPaddingEnd(this)
              - localIconSize
              - iconPadding
              - ViewCompat.getPaddingStart(this))
              / 2;

      // Only flip the bound value if either isLayoutRTL() or iconGravity is textEnd, but not both
      if (isLayoutRTL() != (iconGravity == ICON_GRAVITY_TEXT_END)) {
        newIconLeft = -newIconLeft;
      }

      if (iconLeft != newIconLeft) {
        iconLeft = newIconLeft;
        updateIcon(/* needsIconReset = */ false);
      }
    } else if (isIconTop()) {
      iconLeft = 0;
      if (iconGravity == ICON_GRAVITY_TOP) {
        iconTop = 0;
        updateIcon(/* needsIconReset = */ false);
        return;
      }

      int localIconSize = iconSize == 0 ? icon.getIntrinsicHeight() : iconSize;
      int newIconTop =
          (buttonHeight
              - getTextHeight()
              - getPaddingTop()
              - localIconSize
              - iconPadding
              - getPaddingBottom())
              / 2;

      if (iconTop != newIconTop) {
        iconTop = newIconTop;
        updateIcon(/* needsIconReset = */ false);
      }
    }
  }

  private int getTextWidth() {
    Paint textPaint = getPaint();
    String buttonText = getText().toString();
    if (getTransformationMethod() != null) {
      // if text is transformed, add that transformation to to ensure correct calculation
      // of icon padding.
      buttonText = getTransformationMethod().getTransformation(buttonText, this).toString();
    }

    return Math.min((int) textPaint.measureText(buttonText), getLayout().getEllipsizedWidth());
  }

  private int getTextHeight() {
    Paint textPaint = getPaint();
    String buttonText = getText().toString();
    if (getTransformationMethod() != null) {
      // if text is transformed, add that transformation to to ensure correct calculation
      // of icon padding.
      buttonText = getTransformationMethod().getTransformation(buttonText, this).toString();
    }

    Rect bounds = new Rect();
    textPaint.getTextBounds(buttonText, 0, buttonText.length(), bounds);

    return Math.min(bounds.height(), getLayout().getHeight());
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
      updateIcon(/* needsIconReset = */ true);
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
  public void setIcon(@Nullable Drawable icon) {
    if (this.icon != icon) {
      this.icon = icon;
      updateIcon(/* needsIconReset = */ true);
      updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
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
      updateIcon(/* needsIconReset = */ false);
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
      updateIcon(/* needsIconReset = */ false);
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

  /**
   * Updates the icon, icon tint, and icon tint mode for this button.
   * @param needsIconReset Whether to force the drawable to be set
   */
  private void updateIcon(boolean needsIconReset) {
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      DrawableCompat.setTintList(icon, iconTint);
      if (iconTintMode != null) {
        DrawableCompat.setTintMode(icon, iconTintMode);
      }

      int width = iconSize != 0 ? iconSize : icon.getIntrinsicWidth();
      int height = iconSize != 0 ? iconSize : icon.getIntrinsicHeight();
      icon.setBounds(iconLeft, iconTop, iconLeft + width, iconTop + height);
    }

    // Forced icon update
    if (needsIconReset) {
      resetIconDrawable();
      return;
    }

    // Otherwise only update if the icon or the position has changed
    Drawable[] existingDrawables = TextViewCompat.getCompoundDrawablesRelative(this);
    Drawable drawableStart = existingDrawables[0];
    Drawable drawableTop = existingDrawables[1];
    Drawable drawableEnd = existingDrawables[2];
    boolean hasIconChanged =
        (isIconStart() && drawableStart != icon)
            || (isIconEnd() && drawableEnd != icon)
            || (isIconTop() && drawableTop != icon);

    if (hasIconChanged) {
      resetIconDrawable();
    }
  }

  private void resetIconDrawable() {
    if (isIconStart()) {
      TextViewCompat.setCompoundDrawablesRelative(this, icon, null, null, null);
    } else if (isIconEnd()) {
      TextViewCompat.setCompoundDrawablesRelative(this, null, null, icon, null);
    } else if (isIconTop()) {
      TextViewCompat.setCompoundDrawablesRelative(this, null, icon, null, null);
    }
  }

  private boolean isIconStart() {
    return iconGravity == ICON_GRAVITY_START || iconGravity == ICON_GRAVITY_TEXT_START;
  }

  private boolean isIconEnd() {
    return iconGravity == ICON_GRAVITY_END || iconGravity == ICON_GRAVITY_TEXT_END;
  }

  private boolean isIconTop() {
    return iconGravity == ICON_GRAVITY_TOP || iconGravity == ICON_GRAVITY_TEXT_TOP;
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
  @Nullable
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
    if (this.iconGravity != iconGravity) {
      this.iconGravity = iconGravity;
      updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
    }
  }

  /**
   * Sets the button bottom inset
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_insetBottom
   * @see #getInsetBottom()
   */
  public void setInsetBottom(@Dimension int insetBottom) {
    materialButtonHelper.setInsetBottom(insetBottom);
  }

  /**
   * Gets the bottom inset for this button
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_insetBottom
   * @see #setInsetTop(int)
   */
  @Dimension
  public int getInsetBottom() {
    return materialButtonHelper.getInsetBottom();
  }
  /**
   * Sets the button top inset
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_insetTop
   * @see #getInsetBottom()
   */
  public void setInsetTop(@Dimension int insetTop) {
    materialButtonHelper.setInsetTop(insetTop);
  }

  /**
   * Gets the top inset for this button
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_android_insetTop
   * @see #setInsetTop(int)
   */
  @Dimension
  public int getInsetTop() {
    return materialButtonHelper.getInsetTop();
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

  /**
   * {@inheritDoc}
   *
   * @throws IllegalStateException if the MaterialButton's background has been overwritten.
   */
  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShapeAppearanceModel(shapeAppearanceModel);
    } else {
      throw new IllegalStateException(
          "Attempted to set ShapeAppearanceModel on a MaterialButton which has an overwritten"
              + " background.");
    }
  }

  /**
   * Returns the {@link ShapeAppearanceModel} used for this MaterialButton's shape definition.
   *
   * <p>This {@link ShapeAppearanceModel} can be modified to change the component's shape.
   *
   * @throws IllegalStateException if the MaterialButton's background has been overwritten.
   */
  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getShapeAppearanceModel();
    } else {
      throw new IllegalStateException(
          "Attempted to get ShapeAppearanceModel from a MaterialButton which has an overwritten"
              + " background.");
    }
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

  static class SavedState extends AbsSavedState {

    boolean checked;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      if (loader == null) {
        loader = getClass().getClassLoader();
      }
      readFromParcel(source);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(checked ? 1 : 0);
    }

    private void readFromParcel(@NonNull Parcel in) {
      checked = in.readInt() == 1;
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
