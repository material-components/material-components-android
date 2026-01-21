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

import static android.view.Gravity.CENTER_HORIZONTAL;
import static android.view.Gravity.END;
import static android.view.Gravity.LEFT;
import static android.view.Gravity.RIGHT;
import static android.view.Gravity.START;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.ceil;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.annotation.SuppressLint;
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
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatButton;
import android.text.Layout.Alignment;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.customview.view.AbsSavedState;
import androidx.dynamicanimation.animation.FloatPropertyCompat;
import androidx.dynamicanimation.animation.SpringAnimation;
import androidx.dynamicanimation.animation.SpringForce;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.motion.MotionUtils;
import androidx.resourceinspection.annotation.Attribute;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearance;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.google.android.material.shape.StateListShapeAppearanceModel;
import com.google.android.material.shape.StateListSizeChange;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedHashSet;

/**
 * A convenience class for creating a new Material button.
 *
 * <p>This class supplies updated Material styles for the button in the constructor. The widget will
 * display the correct default Material styles without the use of the style flag.
 *
 * <p>All attributes from {@code com.google.android.material.R.styleable#MaterialButton} are
 * supported. Do not use the {@code android:background} attribute. MaterialButton manages its own
 * background drawable, and setting a new background means {@link MaterialButton} can no longer
 * guarantee that the new attributes it introduces will function properly. If the default background
 * is changed, {@link MaterialButton} cannot guarantee well-defined behavior.
 *
 * <p>For filled buttons, this class uses your theme's {@code ?attr/colorPrimary} for the background
 * tint color and {@code ?attr/colorOnPrimary} for the text color. For unfilled buttons, this class
 * uses {@code ?attr/colorPrimary} for the text color and transparent for the background tint.
 *
 * <p>Add icons to the start, center, or end of this button using the {@code app:icon}, {@code
 * app:iconPadding}, {@code app:iconTint}, {@code app:iconTintMode} and {@code app:iconGravity}
 * attributes.
 *
 * <p>If a start-aligned icon is added to this button, please use a style like one of the ".Icon"
 * styles specified in the default MaterialButton styles. The ".Icon" styles adjust padding slightly
 * to achieve a better visual balance. This style should only be used with a start-aligned icon
 * button. If your icon is end-aligned, you cannot use a ".Icon" style and must instead manually
 * adjust your padding such that the visual adjustment is mirrored.
 *
 * <p>Specify background tint using the {@code app:backgroundTint} and {@code
 * app:backgroundTintMode} attributes, which accepts either a color or a color state list.
 *
 * <p>Ripple color / press state color can be specified using the {@code app:rippleColor} attribute.
 * Ripple opacity will be determined by the Android framework when available. Otherwise, this color
 * will be overlaid on the button at a 50% opacity when button is pressed.
 *
 * <p>Set the stroke color using the {@code app:strokeColor} attribute, which accepts either a color
 * or a color state list. Stroke width can be set using the {@code app:strokeWidth} attribute.
 *
 * <p>Specify the radius of all four corners of the button using the {@code app:cornerRadius}
 * attribute.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Button.md">component
 * developer guidance</a> and <a href="https://material.io/components/buttons/overview">design
 * guidelines</a>.
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

  enum WidthChangeDirection {
    NONE,
    START,
    END,
    BOTH
  }

  private static final String LOG_TAG = "MaterialButton";

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_Button;

  @AttrRes private static final int MATERIAL_SIZE_OVERLAY_ATTR = R.attr.materialSizeOverlay;
  private static final float OPTICAL_CENTER_RATIO = 0.11f;

  private static final int UNSET = Integer.MIN_VALUE;

  @NonNull private final MaterialButtonHelper materialButtonHelper;

  @NonNull
  private final LinkedHashSet<OnCheckedChangeListener> onCheckedChangeListeners =
      new LinkedHashSet<>();

  @Nullable private OnPressedChangeListener onPressedChangeListenerInternal;
  @Nullable private Mode iconTintMode;
  @Nullable private ColorStateList iconTint;
  @Nullable private Drawable icon;
  @Nullable private Mode secondaryIconTintMode;
  @Nullable private ColorStateList secondaryIconTint;
  @Nullable private Drawable secondaryIcon;
  private boolean stopNullSecondaryIconUpdate;
  @Nullable private String accessibilityClassName;

  @Px private int iconSize;
  @Px private int iconLeft;
  @Px private int iconTop;
  @Px private int iconPadding;
  @Px private int secondaryIconLeft;
  @Px private int secondaryIconTop;

  private boolean checked = false;
  private boolean broadcasting = false;
  @IconGravity private int iconGravity;
  @IconGravity private int secondaryIconGravity;

  private int orientation = UNSET;
  private float originalWidth = UNSET;
  @Px private int originalPaddingStart = UNSET;
  @Px private int originalPaddingEnd = UNSET;

  @Nullable private LayoutParams originalLayoutParams;

  // Fields for optical center.
  private boolean opticalCenterEnabled;
  private int opticalCenterShift;
  private boolean isInHorizontalButtonGroup;

  // Fields for size morphing.
  @Px int allowedWidthDecrease = UNSET;
  @Nullable StateListSizeChange sizeChange;
  @Px int widthChangeMax;
  private WidthChangeDirection widthChangeDirection = WidthChangeDirection.BOTH;
  private float displayedWidthIncrease;
  private float displayedWidthDecrease;
  @Nullable private SpringAnimation widthIncreaseSpringAnimation;

  public MaterialButton(@NonNull Context context) {
    this(context, null /* attrs */);
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialButtonStyle);
  }

  public MaterialButton(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(
        wrap(context, attrs, defStyleAttr, DEF_STYLE_RES, new int[] {MATERIAL_SIZE_OVERLAY_ATTR}),
        attrs,
        defStyleAttr);
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

    secondaryIconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialButton_secondaryIconTintMode, -1), Mode.SRC_IN);
    secondaryIconTint =
        MaterialResources.getColorStateList(
            getContext(), attributes, R.styleable.MaterialButton_secondaryIconTint);
    secondaryIconGravity =
        attributes.getInteger(R.styleable.MaterialButton_secondaryIconGravity, ICON_GRAVITY_END);
    secondaryIcon =
        MaterialResources.getDrawable(
            getContext(), attributes, R.styleable.MaterialButton_secondaryIcon);
    // Have this flag in case there's an existing end drawable set, so that it is not nulled out.
    stopNullSecondaryIconUpdate = secondaryIcon == null;

    StateListShapeAppearanceModel stateListShapeAppearanceModel =
        StateListShapeAppearanceModel.create(
            context, attributes, R.styleable.MaterialButton_shapeAppearance);
    ShapeAppearance shapeAppearance =
        stateListShapeAppearanceModel != null
            ? stateListShapeAppearanceModel
            : ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();
    boolean opticalCenterEnabled =
        attributes.getBoolean(R.styleable.MaterialButton_opticalCenterEnabled, false);

    // Loads and sets background drawable attributes
    materialButtonHelper = new MaterialButtonHelper(this, shapeAppearance);
    materialButtonHelper.loadFromAttributes(attributes);

    // Sets the checked state after the MaterialButtonHelper is initialized.
    setCheckedInternal(attributes.getBoolean(R.styleable.MaterialButton_android_checked, false));

    if (shapeAppearance instanceof StateListShapeAppearanceModel) {
      materialButtonHelper.setCornerSpringForce(createSpringForce());
    }
    setOpticalCenterEnabled(opticalCenterEnabled);

    attributes.recycle();

    setCompoundDrawablePadding(iconPadding);
    updateIcon(/* needsIconReset= */ icon != null);
    updateSecondaryIcon(/* needsIconReset= */ secondaryIcon != null);
  }

  private void initializeSizeAnimation() {
    widthIncreaseSpringAnimation = new SpringAnimation(this, WIDTH_INCREASE);
    widthIncreaseSpringAnimation.setSpring(createSpringForce());
  }

  private SpringForce createSpringForce() {
    return MotionUtils.resolveThemeSpringForce(
        getContext(),
        R.attr.motionSpringFastSpatial,
        R.style.Motion_Material3_Spring_Standard_Fast_Spatial);
  }

  private boolean maybeRunAfterWidthAnimation(Runnable action) {
    if (widthIncreaseSpringAnimation != null && widthIncreaseSpringAnimation.isRunning()) {
      post(
          () -> {
            action.run();
            recoverOriginalLayoutParams();
            requestLayout();
          });
      return true;
    }
    return false;
  }

  @NonNull
  @SuppressLint("KotlinPropertyAccess")
  String getA11yClassName() {
    if (!TextUtils.isEmpty(accessibilityClassName)) {
      return accessibilityClassName;
    }
    // Use the platform widget classes so Talkback can recognize this as a button.
    return (isCheckable() ? CompoundButton.class : Button.class).getName();
  }

  @RestrictTo(LIBRARY_GROUP)
  public void setA11yClassName(@Nullable String className) {
    accessibilityClassName = className;
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
   * This should be accessed via {@link android.view.View#getBackgroundTintList()}
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
   * This should be accessed via {@link android.view.View#getBackgroundTintMode()}
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
    updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
    updateSecondaryIconPosition(getMeasuredWidth(), getMeasuredHeight());

    int curOrientation = getResources().getConfiguration().orientation;
    if (orientation != curOrientation) {
      orientation = curOrientation;
      originalWidth = UNSET;
    }
    if (originalWidth == UNSET) {
      originalWidth = getMeasuredWidth();
      // The width morph leverage the width of the layout params. However, it's not available if
      // layout_weight is used. We need to hardcode the width here. The original layout params will
      // be preserved for the correctness of distribution when buttons are added or removed into the
      // group programmatically.
      if (originalLayoutParams == null
          && getParent() instanceof MaterialButtonGroup
          && ((MaterialButtonGroup) getParent()).getButtonSizeChange() != null) {
        originalLayoutParams = (LayoutParams) getLayoutParams();
        LayoutParams newLayoutParams = new LayoutParams(originalLayoutParams);
        newLayoutParams.width = (int) originalWidth;
        setLayoutParams(newLayoutParams);
      }
    }

    if (allowedWidthDecrease == UNSET) {
      int localIconSizeAndPadding =
          icon == null
              ? 0
              : getIconPadding() + (iconSize == 0 ? icon.getIntrinsicWidth() : iconSize);
      allowedWidthDecrease = getMeasuredWidth() - getTextLayoutWidth() - localIconSizeAndPadding;
    }

    if (originalPaddingStart == UNSET) {
      originalPaddingStart = getPaddingStart();
    }
    if (originalPaddingEnd == UNSET) {
      originalPaddingEnd = getPaddingEnd();
    }
    isInHorizontalButtonGroup = isInHorizontalButtonGroup();
  }

  void recoverOriginalLayoutParams() {
    if (originalLayoutParams != null) {
      setLayoutParams(originalLayoutParams);
      originalLayoutParams = null;
      originalWidth = UNSET;
    }
  }

  @Override
  public void setWidth(@Px int pixels) {
    originalWidth = UNSET;
    super.setWidth(pixels);
  }

  @Override
  protected void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
    super.onTextChanged(charSequence, i, i1, i2);
    updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
    updateSecondaryIconPosition(getMeasuredWidth(), getMeasuredHeight());
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (isUsingOriginalBackground()) {
      MaterialShapeUtils.setParentAbsoluteElevation(
          this, materialButtonHelper.getMaterialShapeDrawable());
    }
  }

  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    if (isUsingOriginalBackground()) {
      materialButtonHelper.getMaterialShapeDrawable().setElevation(elevation);
    }
  }

  @Override
  public void refreshDrawableState() {
    super.refreshDrawableState();
    if (this.icon != null) {
      final int[] state = getDrawableState();
      boolean changed = icon.setState(state);

      // Force the view to draw if icon state has changed.
      if (changed) {
        invalidate();
      }
    }
  }

  @Override
  public void setText(CharSequence text, BufferType type) {
    originalWidth = UNSET;
    super.setText(text, type);
  }

  @Override
  public void setTextAppearance(Context context, int resId) {
    originalWidth = UNSET;
    super.setTextAppearance(context, resId);
  }

  @Override
  public void setTextSize(int unit, float size) {
    originalWidth = UNSET;
    super.setTextSize(unit, size);
  }

  @Override
  public void setTextAlignment(int textAlignment) {
    super.setTextAlignment(textAlignment);
    updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
    updateSecondaryIconPosition(getMeasuredWidth(), getMeasuredHeight());
  }

  /**
   * This method and {@link #getActualTextAlignment()} is modified from Android framework TextView's
   * private method getLayoutAlignment(). Please note that the logic here assumes the actual text
   * direction is the same as the layout direction, which is not always the case, especially when
   * the text mixes different languages. However, this is probably the best we can do for now,
   * unless we have a good way to detect the final text direction being used by TextView.
   */
  private Alignment getGravityTextAlignment() {
    switch (getGravity() & Gravity.RELATIVE_HORIZONTAL_GRAVITY_MASK) {
      case CENTER_HORIZONTAL:
        return Alignment.ALIGN_CENTER;
      case END:
      case RIGHT:
        return Alignment.ALIGN_OPPOSITE;
      case START:
      case LEFT:
      default:
        return Alignment.ALIGN_NORMAL;
    }
  }

  /**
   * This method and {@link #getGravityTextAlignment()} is modified from Android framework
   * TextView's private method getLayoutAlignment(). Please note that the logic here assumes the
   * actual text direction is the same as the layout direction, which is not always the case,
   * especially when the text mixes different languages. However, this is probably the best we can
   * do for now, unless we have a good way to detect the final text direction being used by
   * TextView.
   */
  private Alignment getActualTextAlignment() {
    switch (getTextAlignment()) {
      case TEXT_ALIGNMENT_GRAVITY:
        return getGravityTextAlignment();
      case TEXT_ALIGNMENT_CENTER:
        return Alignment.ALIGN_CENTER;
      case TEXT_ALIGNMENT_TEXT_END:
      case TEXT_ALIGNMENT_VIEW_END:
        return Alignment.ALIGN_OPPOSITE;
      case TEXT_ALIGNMENT_TEXT_START:
      case TEXT_ALIGNMENT_VIEW_START:
      case TEXT_ALIGNMENT_INHERIT:
      default:
        return Alignment.ALIGN_NORMAL;
    }
  }

  private void updateIconPosition(int buttonWidth, int buttonHeight) {
    if (icon == null || getLayout() == null) {
      return;
    }

    if (isIconStart() || isIconEnd()) {
      iconTop = 0;
      if (canUpdateWithoutTextAlignment(iconGravity)) {
        iconLeft = 0;
        updateIcon(/* needsIconReset= */ false);
        return;
      }

      int newIconLeft = getIconLeft(buttonWidth, iconGravity);
      if (iconLeft != newIconLeft) {
        iconLeft = newIconLeft;
        updateIcon(/* needsIconReset= */ false);
      }
    } else if (isIconTop()) {
      iconLeft = 0;
      if (iconGravity == ICON_GRAVITY_TOP) {
        iconTop = 0;
        updateIcon(/* needsIconReset= */ false);
        return;
      }

      int localIconSize = iconSize == 0 ? icon.getIntrinsicHeight() : iconSize;
      int newIconTop = getIconTop(buttonHeight, localIconSize);
      if (iconTop != newIconTop) {
        iconTop = newIconTop;
        updateIcon(/* needsIconReset= */ false);
      }
    }
  }

  private void updateSecondaryIconPosition(int buttonWidth, int buttonHeight) {
    if (secondaryIcon == null || getLayout() == null) {
      return;
    }

    if (isSecondaryIconStart() || isSecondaryIconEnd()) {
      secondaryIconTop = 0;
      if (canUpdateWithoutTextAlignment(secondaryIconGravity)) {
        secondaryIconLeft = 0;
        updateSecondaryIcon(/* needsIconReset= */ false);
        return;
      }

      int newSecondaryIconLeft = getIconLeft(buttonWidth, secondaryIconGravity);
      if (secondaryIconLeft != newSecondaryIconLeft) {
        secondaryIconLeft = newSecondaryIconLeft;
        updateSecondaryIcon(/* needsIconReset= */ false);
      }
    } else if (isSecondaryIconTop()) {
      secondaryIconLeft = 0;
      if (secondaryIconGravity == ICON_GRAVITY_TOP) {
        secondaryIconTop = 0;
        updateSecondaryIcon(/* needsIconReset= */ false);
        return;
      }

      int localSecondaryIconSize = iconSize == 0 ? secondaryIcon.getIntrinsicHeight() : iconSize;
      int newIconTop = getIconTop(buttonHeight, localSecondaryIconSize);
      if (secondaryIconTop != newIconTop) {
        secondaryIconTop = newIconTop;
        updateSecondaryIcon(/* needsIconReset= */ false);
      }
    }
  }

  private boolean canUpdateWithoutTextAlignment(@IconGravity int gravity) {
    Alignment textAlignment = getActualTextAlignment();
    return gravity == ICON_GRAVITY_START
        || gravity == ICON_GRAVITY_END
        || (gravity == ICON_GRAVITY_TEXT_START && textAlignment == Alignment.ALIGN_NORMAL)
        || (gravity == ICON_GRAVITY_TEXT_END && textAlignment == Alignment.ALIGN_OPPOSITE);
  }

  private int getIconLeft(int buttonWidth, @IconGravity int gravity) {
    int localIconSize = 0;
    if (icon != null) {
      localIconSize = iconSize == 0 ? icon.getIntrinsicWidth() : iconSize;
    }
    int localSecondaryIconSize = 0;
    if (secondaryIcon != null) {
      localSecondaryIconSize = iconSize == 0 ? secondaryIcon.getIntrinsicWidth() : iconSize;
    }
    int availableWidth =
        buttonWidth
            - getTextLayoutWidth()
            - getPaddingEnd()
            - localIconSize
            - localSecondaryIconSize
            - iconPadding
            - getPaddingStart();
    Alignment textAlignment = getActualTextAlignment();
    int iconLeft = textAlignment == Alignment.ALIGN_CENTER ? availableWidth / 2 : availableWidth;
    // Only flip the bound value if either isLayoutRTL() or iconGravity is textEnd, but not both
    if (isLayoutRTL() != (gravity == ICON_GRAVITY_TEXT_END)) {
      iconLeft = -iconLeft;
    }
    return iconLeft;
  }

  private int getIconTop(int buttonHeight, int iconSize) {
    return max(
        0, // Always put the icon on top if the content height is taller than the button.
        (buttonHeight
            - getTextHeight()
            - getPaddingTop()
            - iconSize
            - iconPadding
            - getPaddingBottom())
            / 2);
  }

  private int getTextLayoutWidth() {
    float maxWidth = 0;
    int lineCount = getLineCount();
    for (int line = 0; line < lineCount; line++) {
      maxWidth = max(maxWidth, getLayout().getLineWidth(line));
    }
    return (int) ceil(maxWidth);
  }

  private int getTextHeight() {
    if (getLineCount() > 1) {
      // If it's multi-line, return the internal text layout's height.
      return getLayout().getHeight();
    }
    Paint textPaint = getPaint();
    String buttonText = getText().toString();
    if (getTransformationMethod() != null) {
      // if text is transformed, add that transformation to to ensure correct calculation
      // of icon padding.
      buttonText = getTransformationMethod().getTransformation(buttonText, this).toString();
    }

    Rect bounds = new Rect();
    textPaint.getTextBounds(buttonText, 0, buttonText.length(), bounds);

    return min(bounds.height(), getLayout().getHeight());
  }

  private boolean isLayoutRTL() {
    return getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
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

  @Override
  public void setCompoundDrawablePadding(@Px int padding) {
    if (getCompoundDrawablePadding() != padding) {
      originalWidth = UNSET;
    }
    super.setCompoundDrawablePadding(padding);
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
  @Attribute("com.google.android.material:iconPadding")
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
      if (maybeRunAfterWidthAnimation(() -> setIconSize(iconSize))) {
        return;
      }
      originalWidth = UNSET;
      this.iconSize = iconSize;
      updateIcon(/* needsIconReset= */ true);
      updateSecondaryIcon(/* needsIconReset= */ true);
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
      if (maybeRunAfterWidthAnimation(() -> setIcon(icon))) {
        return;
      }
      originalWidth = UNSET;
      this.icon = icon;
      updateIcon(/* needsIconReset= */ true);
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
      updateIcon(/* needsIconReset= */ false);
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
      updateIcon(/* needsIconReset= */ false);
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
   * Sets the secondary icon to show for this button. By default, this icon will be shown on the end
   * of the button. A secondary icon used in addition with another icon should be generally avoided,
   * as it is meant for a pointer optimized UI such as of a laptop UI.
   *
   * @param icon Drawable to use for the secondary icon.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIcon
   * @see #setSecondaryIconResource(int)
   * @see #getSecondaryIcon()
   */
  public void setSecondaryIcon(@Nullable Drawable icon) {
    if (secondaryIcon != icon) {
      if (maybeRunAfterWidthAnimation(() -> setIcon(icon))) {
        return;
      }
      originalWidth = UNSET;
      secondaryIcon = icon;
      stopNullSecondaryIconUpdate = false;
      updateSecondaryIcon(/* needsIconReset= */ true);
      updateSecondaryIconPosition(getMeasuredWidth(), getMeasuredHeight());
    }
  }

  /**
   * Sets the secondary icon to show for this button. By default, this icon will be shown on the end
   * of the button. A secondary icon used in addition with another icon should be generally avoided,
   * as it is meant for a pointer optimized UI such as of a laptop UI.
   *
   * @param iconResourceId Drawable resource ID to use for the secondary icon.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIcon
   * @see #setSecondaryIcon(Drawable)
   * @see #getSecondaryIcon()
   */
  public void setSecondaryIconResource(@DrawableRes int iconResourceId) {
    Drawable icon = null;
    if (iconResourceId != 0) {
      icon = AppCompatResources.getDrawable(getContext(), iconResourceId);
    }
    setSecondaryIcon(icon);
  }

  /**
   * Gets the secondary icon for this button, if present.
   *
   * @return Secondary icon for this button, if present.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIcon
   * @see #setSecondaryIcon(Drawable)
   * @see #setSecondaryIconResource(int)
   */
  @Nullable
  public Drawable getSecondaryIcon() {
    return secondaryIcon;
  }

  /**
   * Sets the tint list for the secondary icon shown for this button.
   *
   * @param secondaryIconTint Tint list for the secondary icon shown for this button.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconTint
   * @see #setSecondaryIconTintResource(int)
   * @see #getSecondaryIconTint()
   */
  public void setSecondaryIconTint(@Nullable ColorStateList secondaryIconTint) {
    if (this.secondaryIconTint != secondaryIconTint) {
      this.secondaryIconTint = secondaryIconTint;
      updateSecondaryIcon(/* needsIconReset= */ false);
    }
  }

  /**
   * Sets the tint list color resource for the secondsary icon shown for this button.
   *
   * @param iconTintResourceId Tint list color resource for the secondary icon shown for this button
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconTint
   * @see #setSecondaryIconTint(ColorStateList)
   * @see #getSecondaryIconTint()
   */
  public void setSecondaryIconTintResource(@ColorRes int iconTintResourceId) {
    setSecondaryIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  /**
   * Gets the tint list for the secondary icon shown for this button.
   *
   * @return Tint list for the secondary icon shown for this button.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconTint
   * @see #setSecondaryIconTint(ColorStateList)
   * @see #setSecondaryIconTintResource(int)
   */
  public ColorStateList getSecondaryIconTint() {
    return secondaryIconTint;
  }

  /**
   * Sets the tint mode for the secondary icon shown for this button.
   *
   * @param secondaryIconTintMode Tint mode for the secondary icon shown for this button.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconTintMode
   * @see #getSecondaryIconTintMode()
   */
  public void setSecondaryIconTintMode(Mode secondaryIconTintMode) {
    if (this.secondaryIconTintMode != secondaryIconTintMode) {
      this.secondaryIconTintMode = secondaryIconTintMode;
      updateSecondaryIcon(/* needsIconReset= */ false);
    }
  }

  /**
   * Gets the tint mode for the secondary icon shown for this button.
   *
   * @return Tint mode for the secondary icon shown for this button.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconTintMode
   * @see #setSecondaryIconTintMode(Mode)
   */
  public Mode getSecondaryIconTintMode() {
    return secondaryIconTintMode;
  }

  /**
   * Updates the icon, icon tint, and icon tint mode for this button.
   *
   * @param needsIconReset Whether to force the drawable to be set
   */
  private void updateIcon(boolean needsIconReset) {
    if (icon != null) {
      icon = DrawableCompat.wrap(icon).mutate();
      icon.setTintList(iconTint);
      if (iconTintMode != null) {
        icon.setTintMode(iconTintMode);
      }

      int width = iconSize != 0 ? iconSize : icon.getIntrinsicWidth();
      int height = iconSize != 0 ? iconSize : icon.getIntrinsicHeight();
      icon.setBounds(iconLeft, iconTop, iconLeft + width, iconTop + height);
      icon.setVisible(true, needsIconReset);
    }

    // Make sure icon gravity is valid before updating it.
    validateIconGravity();
    if (icon == null && secondaryIcon != null && areIconsGravitySameAlignment()) {
      return;
    }
    Drawable[] existingDrawables = getCompoundDrawablesRelative();
    Drawable drawableStart = existingDrawables[0];
    Drawable drawableTop = existingDrawables[1];
    Drawable drawableEnd = existingDrawables[2];
    boolean hasIconChanged =
        (isIconStart() && drawableStart != icon)
            || (isIconEnd() && drawableEnd != icon)
            || (isIconTop() && drawableTop != icon);
    // Force icon update if needsIconReset otherwise updated it only if icon has changed.
    if (needsIconReset || hasIconChanged) {
      if (isIconStart()) {
        setCompoundDrawablesRelative(icon, getUpdatedIconFor(1), getUpdatedIconFor(2), null);
      } else if (isIconEnd()) {
        setCompoundDrawablesRelative(getUpdatedIconFor(0), getUpdatedIconFor(1), icon, null);
      } else if (isIconTop()) {
        setCompoundDrawablesRelative(getUpdatedIconFor(0), icon, getUpdatedIconFor(2), null);
      }
    }
  }

  private void validateIconGravity() {
    if (icon != null && secondaryIcon != null && areIconsGravitySameAlignment()) {
      throw new IllegalArgumentException(
          "iconGravity cannot have the same alignment as secondaryIconGravity");
    }
  }

  private boolean areIconsGravitySameAlignment() {
    return (isIconStart() && isSecondaryIconStart())
        || (isIconEnd() && isSecondaryIconEnd())
        || (isIconTop() && isSecondaryIconTop());
  }

  @Nullable
  private Drawable getUpdatedIconFor(int position) {
    switch (position) {
      case 0: // start
        return (secondaryIcon != null && isSecondaryIconStart()) ? secondaryIcon : null;
      case 1: // top
        return (secondaryIcon != null && isSecondaryIconTop()) ? secondaryIcon : null;
      case 2: // end
        return (secondaryIcon != null && isSecondaryIconEnd()) ? secondaryIcon : null;
      default:
        return null;
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

  private boolean isSecondaryIconStart() {
    return secondaryIconGravity == ICON_GRAVITY_START
        || secondaryIconGravity == ICON_GRAVITY_TEXT_START;
  }

  private boolean isSecondaryIconEnd() {
    return secondaryIconGravity == ICON_GRAVITY_END
        || secondaryIconGravity == ICON_GRAVITY_TEXT_END;
  }

  private boolean isSecondaryIconTop() {
    return secondaryIconGravity == ICON_GRAVITY_TOP
        || secondaryIconGravity == ICON_GRAVITY_TEXT_TOP;
  }

  private void updateSecondaryIcon(boolean needsIconReset) {
    if (secondaryIcon != null) {
      secondaryIcon = DrawableCompat.wrap(secondaryIcon).mutate();
      secondaryIcon.setTintList(secondaryIconTint);
      if (secondaryIconTintMode != null) {
        secondaryIcon.setTintMode(secondaryIconTintMode);
      }

      int width = iconSize != 0 ? iconSize : secondaryIcon.getIntrinsicWidth();
      int height = iconSize != 0 ? iconSize : secondaryIcon.getIntrinsicHeight();
      secondaryIcon.setBounds(
          secondaryIconLeft,
          secondaryIconTop,
          secondaryIconLeft + width,
          secondaryIconTop + height);
      secondaryIcon.setVisible(true, needsIconReset);
    }

    // Make sure icon gravity is valid before updating it.
    validateSecondaryIconGravity();
    if (secondaryIcon == null
        && (stopNullSecondaryIconUpdate || (icon != null && areIconsGravitySameAlignment()))) {
      return;
    }
    Drawable[] existingDrawables = getCompoundDrawablesRelative();
    Drawable drawableStart = existingDrawables[0];
    Drawable drawableTop = existingDrawables[1];
    Drawable drawableEnd = existingDrawables[2];
    boolean hasIconChanged =
        (isSecondaryIconStart() && drawableStart != secondaryIcon)
            || (isSecondaryIconEnd() && drawableEnd != secondaryIcon)
            || (isSecondaryIconTop() && drawableTop != secondaryIcon);
    // Force icon update if needsIconReset otherwise updated it only if icon has changed.
    if (needsIconReset || hasIconChanged) {
      if (isSecondaryIconStart()) {
        setCompoundDrawablesRelative(
            secondaryIcon, getUpdatedSecondaryIconFor(1), getUpdatedSecondaryIconFor(2), null);
      } else if (isSecondaryIconEnd()) {
        setCompoundDrawablesRelative(
            getUpdatedSecondaryIconFor(0), getUpdatedSecondaryIconFor(1), secondaryIcon, null);
      } else if (isSecondaryIconTop()) {
        setCompoundDrawablesRelative(
            getUpdatedSecondaryIconFor(0), secondaryIcon, getUpdatedSecondaryIconFor(2), null);
      }
    }
  }

  private void validateSecondaryIconGravity() {
    if (secondaryIcon != null && icon != null && areIconsGravitySameAlignment()) {
      throw new IllegalArgumentException(
          "secondaryIconGravity cannot have the same alignment as iconGravity");
    }
  }

  @Nullable
  private Drawable getUpdatedSecondaryIconFor(int position) {
    switch (position) {
      case 0: // start
        return (icon != null && isIconStart()) ? icon : null;
      case 1: // top
        return (icon != null && isIconEnd()) ? icon : null;
      case 2: // end
        return (icon != null && isIconEnd()) ? icon : null;
      default:
        return null;
    }
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
      validateIconGravity();
      this.iconGravity = iconGravity;
      updateIconPosition(getMeasuredWidth(), getMeasuredHeight());
    }
  }

  /**
   * Gets the secondary icon's gravity for this button.
   *
   * @return Icon gravity of the secondary icon.
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconGravity
   * @see #setIconGravity(int)
   */
  @IconGravity
  public int getSecondaryIconGravity() {
    return secondaryIconGravity;
  }

  /**
   * Sets the secondary icon's gravity for this button.
   *
   * @attr ref com.google.android.material.R.styleable#MaterialButton_secondaryIconGravity
   * @param secondaryIconGravity secondary icon gravity for this button
   * @see #getIconGravity()
   */
  public void setSecondaryIconGravity(@IconGravity int secondaryIconGravity) {
    if (this.secondaryIconGravity != secondaryIconGravity) {
      validateSecondaryIconGravity();
      this.secondaryIconGravity = secondaryIconGravity;
      updateSecondaryIconPosition(getMeasuredWidth(), getMeasuredHeight());
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
    setCheckedInternal(checked);
  }

  private void setCheckedInternal(boolean checked) {
    if (isCheckable() && this.checked != checked) {
      this.checked = checked;

      refreshDrawableState();

      // Report checked state change to the parent toggle group, if there is one
      if (getParent() instanceof MaterialButtonToggleGroup) {
        ((MaterialButtonToggleGroup) getParent()).onButtonCheckedStateChanged(this, this.checked);
      }

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
    boolean toggled = false;
    if (isEnabled() && materialButtonHelper.isToggleCheckedStateOnClick()) {
      toggle();
      toggled = true;
    }

    final boolean handled = super.performClick();

    if (toggled && !handled) {
      // View only makes a sound effect if the onClickListener was called, so for checkable button
      // we'll need to manually make one here instead.
      playSoundEffect(SoundEffectConstants.CLICK);
    }

    return handled;
  }

  /**
   * Returns whether or not clicking the button will toggle the checked state.
   *
   * @see #setToggleCheckedStateOnClick(boolean)
   * @attr ref R.styleable#toggleCheckedStateOnClick
   */
  public boolean isToggleCheckedStateOnClick() {
    return materialButtonHelper.isToggleCheckedStateOnClick();
  }

  /**
   * Sets whether or not to toggle the button checked state on click.
   *
   * @param toggleCheckedStateOnClick whether or not to toggle the checked state on click.
   * @attr ref R.styleable#toggleCheckedStateOnClick
   */
  public void setToggleCheckedStateOnClick(boolean toggleCheckedStateOnClick) {
    materialButtonHelper.setToggleCheckedStateOnClick(toggleCheckedStateOnClick);
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
   * Sets the {@link ShapeAppearanceModel} used for this {@link MaterialButton}'s original
   * drawables.
   *
   * @throws IllegalStateException if the MaterialButton's background has been overwritten.
   */
  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShapeAppearance(shapeAppearanceModel);
    } else {
      throw new IllegalStateException(
          "Attempted to set ShapeAppearanceModel on a MaterialButton which has an overwritten"
              + " background.");
    }
  }

  /**
   * Returns the {@link ShapeAppearanceModel} used for this {@link MaterialButton}'s original
   * drawables.
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
   * Sets the {@link ShapeAppearance} used for this {@link MaterialButton}'s original
   * drawables.
   *
   * @throws IllegalStateException if the MaterialButton's background has been overwritten.
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setShapeAppearance(
      @NonNull ShapeAppearance shapeAppearance) {
    if (isUsingOriginalBackground()) {
      if (materialButtonHelper.getCornerSpringForce() == null && shapeAppearance.isStateful()) {
        materialButtonHelper.setCornerSpringForce(createSpringForce());
      }
      materialButtonHelper.setShapeAppearance(shapeAppearance);
    } else {
      throw new IllegalStateException(
          "Attempted to set ShapeAppearance on a MaterialButton which has an"
              + " overwritten background.");
    }
  }

  /**
   * Returns the {@link ShapeAppearance} used for this {@link MaterialButton}'s
   * original drawables.
   *
   * <p>This {@link ShapeAppearance} can be modified to change the component's shape.
   *
   * @throws IllegalStateException if the MaterialButton's background has been overwritten.
   * @hide
   */
  @NonNull
  @RestrictTo(LIBRARY_GROUP)
  public ShapeAppearance getShapeAppearance() {
    if (isUsingOriginalBackground()) {
      return materialButtonHelper.getShapeAppearance();
    } else {
      throw new IllegalStateException(
          "Attempted to get ShapeAppearance from a MaterialButton which has an"
              + " overwritten background.");
    }
  }

  /**
   * Sets the corner spring force for this {@link MaterialButton}.
   *
   * @param springForce The new {@link SpringForce} object.
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setCornerSpringForce(@NonNull SpringForce springForce) {
    materialButtonHelper.setCornerSpringForce(springForce);
  }

  /**
   * Returns the corner spring force for this {@link MaterialButton}.
   *
   * @hide
   */
  @Nullable
  @RestrictTo(LIBRARY_GROUP)
  public SpringForce getCornerSpringForce() {
    return materialButtonHelper.getCornerSpringForce();
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
    maybeAnimateSize(/* skipAnimation= */ false);
  }

  private boolean isUsingOriginalBackground() {
    return materialButtonHelper != null && !materialButtonHelper.isBackgroundOverwritten();
  }

  void setShouldDrawSurfaceColorStroke(boolean shouldDrawSurfaceColorStroke) {
    if (isUsingOriginalBackground()) {
      materialButtonHelper.setShouldDrawSurfaceColorStroke(shouldDrawSurfaceColorStroke);
    }
  }

  private void maybeAnimateSize(boolean skipAnimation) {
    if (sizeChange == null) {
      return;
    }
    if (widthIncreaseSpringAnimation == null) {
      initializeSizeAnimation();
    }
    if (isInHorizontalButtonGroup) {
      // Animate width.
      int widthChange =
          min(
              calculateEffectiveWidthChangeMax(),
              sizeChange
                  .getSizeChangeForState(getDrawableState())
                  .widthChange
                  .getChange(getWidth()));
      widthIncreaseSpringAnimation.animateToFinalPosition(widthChange);
      if (skipAnimation) {
        widthIncreaseSpringAnimation.skipToEnd();
      }
    }
  }

  /** Returns the effective width change max based on the width change direction. */
  private int calculateEffectiveWidthChangeMax() {
    switch (widthChangeDirection) {
      case BOTH:
        return this.widthChangeMax;
      case START:
      case END:
        return this.widthChangeMax / 2;
      case NONE:
    }
    return 0;
  }

  private boolean isInHorizontalButtonGroup() {
    return getParent() instanceof MaterialButtonGroup
        && ((MaterialButtonGroup) getParent()).getOrientation() == LinearLayout.HORIZONTAL;
  }

  void setSizeChange(@NonNull StateListSizeChange sizeChange) {
    if (this.sizeChange != sizeChange) {
      this.sizeChange = sizeChange;
      maybeAnimateSize(/* skipAnimation= */ true);
    }
  }

  void setWidthChangeMax(@Px int widthChangeMax) {
    if (this.widthChangeMax != widthChangeMax) {
      this.widthChangeMax = widthChangeMax;
      maybeAnimateSize(/* skipAnimation= */ true);
    }
  }

  void setWidthChangeDirection(@NonNull WidthChangeDirection widthChangeDirection) {
    if (this.widthChangeDirection != widthChangeDirection) {
      this.widthChangeDirection = widthChangeDirection;
      maybeAnimateSize(/* skipAnimation= */ true);
    }
  }

  @Px
  int getAllowedWidthDecrease() {
    return allowedWidthDecrease;
  }

  private float getDisplayedWidthIncrease() {
    return displayedWidthIncrease;
  }

  private void setDisplayedWidthIncrease(float widthIncrease) {
    if (displayedWidthIncrease != widthIncrease) {
      displayedWidthIncrease = widthIncrease;
      updatePaddingsAndSizeForWidthAnimation();
      invalidate();
      // Report width changed to the parent group.
      if (getParent() instanceof MaterialButtonGroup) {
        ((MaterialButtonGroup) getParent())
            .onButtonWidthChanged(this, (int) displayedWidthIncrease);
      }
    }
  }

  void setDisplayedWidthDecrease(int widthDecrease) {
    displayedWidthDecrease = min(widthDecrease, allowedWidthDecrease);
    updatePaddingsAndSizeForWidthAnimation();
    invalidate();
  }

  /**
   * Sets whether to enable the optical center feature.
   *
   * @param opticalCenterEnabled whether to enable optical centering.
   * @see #isOpticalCenterEnabled()
   */
  public void setOpticalCenterEnabled(boolean opticalCenterEnabled) {
    if (this.opticalCenterEnabled != opticalCenterEnabled) {
      this.opticalCenterEnabled = opticalCenterEnabled;
      if (opticalCenterEnabled) {
        materialButtonHelper.setCornerSizeChangeListener(
            (diffX) -> {
              int opticalCenterShift = (int) (diffX * OPTICAL_CENTER_RATIO);
              if (this.opticalCenterShift != opticalCenterShift) {
                this.opticalCenterShift = opticalCenterShift;
                updatePaddingsAndSizeForWidthAnimation();
                invalidate();
              }
            });
      } else {
        materialButtonHelper.setCornerSizeChangeListener(null);
      }
      // Perform the optical center shift calculation using a post, as the calculation depends on
      // the button being fully laid out.
      post(
          () -> {
            opticalCenterShift = getOpticalCenterShift();
            updatePaddingsAndSizeForWidthAnimation();
            invalidate();
          });
    }
  }

  /**
   * Returns whether the optical center feature is enabled.
   *
   * @see #setOpticalCenterEnabled(boolean)
   */
  public boolean isOpticalCenterEnabled() {
    return opticalCenterEnabled;
  }

  private void updatePaddingsAndSizeForWidthAnimation() {
    int widthChange = (int) (displayedWidthIncrease - displayedWidthDecrease);
    int paddingStartChange = widthChange / 2 + opticalCenterShift;
    getLayoutParams().width = (int) (originalWidth + widthChange);
    setPaddingRelative(
        originalPaddingStart + paddingStartChange,
        getPaddingTop(),
        originalPaddingEnd + widthChange - paddingStartChange,
        getPaddingBottom());
  }

  private int getOpticalCenterShift() {
    if (opticalCenterEnabled && isInHorizontalButtonGroup) {
      MaterialShapeDrawable materialShapeDrawable = materialButtonHelper.getMaterialShapeDrawable();
      if (materialShapeDrawable != null) {
        return (int) (materialShapeDrawable.getCornerSizeDiffX() * OPTICAL_CENTER_RATIO);
      }
    }
    return 0;
  }

  // ******************* Properties *******************

  private static final FloatPropertyCompat<MaterialButton> WIDTH_INCREASE =
      new FloatPropertyCompat<MaterialButton>("widthIncrease") {
        @Override
        public float getValue(MaterialButton button) {
          return button.getDisplayedWidthIncrease();
        }

        @Override
        public void setValue(MaterialButton button, float value) {
          button.setDisplayedWidthIncrease(value);
        }
      };

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
