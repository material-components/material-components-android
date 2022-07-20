/*
 * Copyright (C) 2018 The Android Open Source Project
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

package com.google.android.material.checkbox;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.drawable.AnimatedStateListDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.TintTypedArray;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.CompoundButtonCompat;
import androidx.core.widget.TintableCompoundButton;
import androidx.vectordrawable.graphics.drawable.Animatable2Compat.AnimationCallback;
import androidx.vectordrawable.graphics.drawable.AnimatedVectorDrawableCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import java.util.LinkedHashSet;

/**
 * A class that creates a Material Themed CheckBox.
 *
 * <p>This class uses attributes from the Material Theme to style a CheckBox. It behaves similarly
 * to {@link AppCompatCheckBox}, but with color changes and the support of an error state.
 *
 * <p>The checkbox is composed of an {@code app:buttonCompat} button drawable (the squared icon) and
 * an {@code app:buttonIcon} icon drawable (the checkmark icon) layered on top of it. Their colors
 * can be customized via {@code app:buttonTint} and {@code app:buttonIconTint} respectively.
 *
 * <p>If setting a custom {@code app:buttonCompat}, make sure to also set {@code app:buttonIcon} if
 * an icon is desired. The checkbox does not support having a custom {@code app:buttonCompat} and
 * preserving the default {@code app:buttonIcon} checkmark at the same time.
 *
 */
public class MaterialCheckBox extends AppCompatCheckBox {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CompoundButton_CheckBox;

  private static final int[] ERROR_STATE_SET = {R.attr.state_error};
  private static final int[][] CHECKBOX_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, R.attr.state_error}, // [0]
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, // [1]
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, // [2]
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, // [3]
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} // [4]
      };
  @NonNull private final LinkedHashSet<OnErrorChangedListener> onErrorChangedListeners =
      new LinkedHashSet<>();
  @Nullable private ColorStateList materialThemeColorsTintList;
  private boolean useMaterialThemeColors;
  private boolean centerIfNoTextEnabled;
  private boolean errorShown;
  private CharSequence errorAccessibilityLabel;

  @Nullable private Drawable buttonDrawable;
  @Nullable private Drawable buttonIconDrawable;
  private boolean usingDefaultButtonDrawable;

  @Nullable ColorStateList buttonTintList;
  @Nullable ColorStateList buttonIconTintList;
  @NonNull private PorterDuff.Mode buttonIconTintMode;

  private int[] currentStateChecked;

  @Nullable
  private final AnimatedVectorDrawableCompat transitionToUnchecked =
      AnimatedVectorDrawableCompat.create(
          getContext(), R.drawable.mtrl_checkbox_button_checked_unchecked);
  private final AnimationCallback transitionToUncheckedCallback =
      new AnimationCallback() {
        @Override
        public void onAnimationStart(Drawable drawable) {
          super.onAnimationStart(drawable);
          if (buttonTintList != null) {
            // Have the color remain on the checked state while the animation is happening.
            DrawableCompat.setTint(
                drawable,
                buttonTintList.getColorForState(
                    currentStateChecked, buttonTintList.getDefaultColor()));
          }
        }

        @Override
        public void onAnimationEnd(Drawable drawable) {
          super.onAnimationEnd(drawable);
          if (buttonTintList != null) {
            DrawableCompat.setTintList(
                drawable,
                buttonTintList);
          }
        }
      };

  /**
   * Callback interface invoked when the checkbox error state changes.
   */
  public interface OnErrorChangedListener {

    /**
     * Called when the error state of a checkbox changes.
     *
     * @param checkBox the {@link MaterialCheckBox}
     * @param errorShown whether the checkbox is on error
     */
    void onErrorChanged(@NonNull MaterialCheckBox checkBox, boolean errorShown);
  }

  public MaterialCheckBox(Context context) {
    this(context, null);
  }

  public MaterialCheckBox(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.checkboxStyle);
  }

  public MaterialCheckBox(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    buttonDrawable = CompoundButtonCompat.getButtonDrawable(this);
    buttonTintList = getSuperButtonTintList();
    // Always use our custom tinting logic.
    ((TintableCompoundButton) this).setSupportButtonTintList(null);

    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.MaterialCheckBox, defStyleAttr, DEF_STYLE_RES);

    buttonIconDrawable = attributes.getDrawable(R.styleable.MaterialCheckBox_buttonIcon);
    // If there's not a custom drawable set, we set our own.
    if (buttonDrawable == null) {
      buttonDrawable = AppCompatResources.getDrawable(context, R.drawable.mtrl_checkbox_button);
      usingDefaultButtonDrawable = true;
      if (buttonIconDrawable == null) {
        buttonIconDrawable =
            AppCompatResources.getDrawable(context, R.drawable.mtrl_checkbox_button_icon);
      }
    }
    buttonIconTintList =
        MaterialResources.getColorStateList(
            context, attributes, R.styleable.MaterialCheckBox_buttonIconTint);
    buttonIconTintMode =
        ViewUtils.parseTintMode(
            attributes.getInt(R.styleable.MaterialCheckBox_buttonIconTintMode, -1), Mode.SRC_IN);
    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialCheckBox_useMaterialThemeColors, false);
    centerIfNoTextEnabled =
        attributes.getBoolean(R.styleable.MaterialCheckBox_centerIfNoTextEnabled, true);
    errorShown = attributes.getBoolean(R.styleable.MaterialCheckBox_errorShown, false);
    errorAccessibilityLabel =
        attributes.getText(R.styleable.MaterialCheckBox_errorAccessibilityLabel);

    attributes.recycle();

    refreshButtonDrawable();

    // This is needed due to a KitKat bug where the drawable states don't get updated correctly
    // in time.
    if (VERSION.SDK_INT < VERSION_CODES.LOLLIPOP) {
      post(
          () -> {
            if (buttonIconDrawable != null) {
              buttonIconDrawable.invalidateSelf();
            }
          });
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Horizontally center the button drawable and ripple when there's no text.
    if (centerIfNoTextEnabled && TextUtils.isEmpty(getText())) {
      Drawable drawable = CompoundButtonCompat.getButtonDrawable(this);
      if (drawable != null) {
        int direction = ViewUtils.isLayoutRtl(this) ? -1 : 1;
        int dx = (getWidth() - drawable.getIntrinsicWidth()) / 2 * direction;

        int saveCount = canvas.save();
        canvas.translate(dx, 0);
        super.onDraw(canvas);
        canvas.restoreToCount(saveCount);

        if (getBackground() != null) {
          Rect bounds = drawable.getBounds();
          DrawableCompat.setHotspotBounds(
              getBackground(), bounds.left + dx, bounds.top, bounds.right + dx, bounds.bottom);
        }

        return;
      }
    }

    super.onDraw(canvas);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && buttonTintList == null && buttonIconTintList == null) {
      setUseMaterialThemeColors(true);
    }
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableStates = super.onCreateDrawableState(extraSpace + 1);

    if (isErrorShown()) {
      mergeDrawableStates(drawableStates, ERROR_STATE_SET);
    }

    currentStateChecked = DrawableUtils.getCheckedState(drawableStates);

    return drawableStates;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@Nullable AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    if (info == null) {
      return;
    }

    if (isErrorShown()) {
      info.setText(info.getText() + ", " + errorAccessibilityLabel);
    }
  }

  /**
   * Sets whether the checkbox should be on error state. If true, the error color will be applied to
   * the checkbox.
   *
   * @param errorShown whether the checkbox should be on error state.
   * @see #isErrorShown()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_errorShown
   */
  public void setErrorShown(boolean errorShown) {
    if (this.errorShown == errorShown) {
      return;
    }
    this.errorShown = errorShown;
    refreshDrawableState();
    jumpDrawablesToCurrentState();
    for (OnErrorChangedListener listener : onErrorChangedListeners) {
      listener.onErrorChanged(this, this.errorShown);
    }
  }

  /**
   * Returns whether the checkbox is on error state.
   *
   * @see #setErrorShown(boolean)
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_errorShown
   */
  public boolean isErrorShown() {
    return errorShown;
  }

  /**
   * Sets the accessibility label to be used for the error state announcement by screen readers.
   *
   * @param resId resource ID of the error announcement text
   * @see #setErrorShown(boolean)
   * @see #getErrorAccessibilityLabel()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_errorAccessibilityLabel
   */
  public void setErrorAccessibilityLabelResource(@StringRes int resId) {
    setErrorAccessibilityLabel(resId != 0 ? getResources().getText(resId) : null);
  }

  /**
   * Sets the accessibility label to be used for the error state announcement by screen readers.
   *
   * @param errorAccessibilityLabel the error announcement
   * @see #setErrorShown(boolean)
   * @see #getErrorAccessibilityLabel()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_errorAccessibilityLabel
   */
  public void setErrorAccessibilityLabel(@Nullable CharSequence errorAccessibilityLabel) {
    this.errorAccessibilityLabel = errorAccessibilityLabel;
  }

  /**
   * Returns the accessibility label used for the error state announcement.
   *
   * @see #setErrorAccessibilityLabel(CharSequence)
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_errorAccessibilityLabel
   */
  @Nullable
  public CharSequence getErrorAccessibilityLabel() {
    return errorAccessibilityLabel;
  }

  /**
   * Adds a {@link OnErrorChangedListener} that will be invoked when the checkbox error state
   * changes.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnErrorChangedListener(OnErrorChangedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnErrorChangedListener(@NonNull OnErrorChangedListener listener) {
    onErrorChangedListeners.add(listener);
  }

  /**
   * Remove a listener that was previously added via {@link
   * #addOnErrorChangedListener(OnErrorChangedListener)}
   *
   * @param listener listener to remove
   */
  public void removeOnErrorChangedListener(@NonNull OnErrorChangedListener listener) {
    onErrorChangedListeners.remove(listener);
  }

  /** Remove all previously added {@link OnErrorChangedListener}s. */
  public void clearOnErrorChangedListeners() {
    onErrorChangedListeners.clear();
  }

  @Override
  public void setButtonDrawable(@DrawableRes int resId) {
    setButtonDrawable(AppCompatResources.getDrawable(getContext(), resId));
  }

  @Override
  public void setButtonDrawable(@Nullable Drawable drawable) {
    buttonDrawable = drawable;
    usingDefaultButtonDrawable = false;
    refreshButtonDrawable();
  }

  @Override
  @Nullable
  public Drawable getButtonDrawable() {
    return buttonDrawable;
  }

  @Override
  public void setButtonTintList(@Nullable ColorStateList tintList) {
    if (buttonTintList == tintList) {
      return;
    }
    buttonTintList = tintList;
    refreshButtonDrawable();
  }

  @Nullable
  @Override
  public ColorStateList getButtonTintList() {
    return buttonTintList;
  }

  @Override
  public void setButtonTintMode(@Nullable Mode tintMode) {
    ((TintableCompoundButton) this).setSupportButtonTintMode(tintMode);
    refreshButtonDrawable();
  }

  /**
   * Sets the button icon drawable of the checkbox.
   *
   * <p>The icon will be layered above the button drawable set by {@link
   * #setButtonDrawable(Drawable)}.
   *
   * @param resId resource id of the drawable to set, or 0 to clear and remove the icon
   * @see #getButtonIconDrawable()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIcon
   */
  public void setButtonIconDrawableResource(@DrawableRes int resId) {
    setButtonIconDrawable(AppCompatResources.getDrawable(getContext(), resId));
  }

  /**
   * Sets the button icon drawable of the checkbox.
   *
   * <p/>The icon will be layered above the button drawable set by {@link
   * #setButtonDrawable(Drawable)}.
   *
   * @param drawable the icon drawable to be set
   * @see #getButtonIconDrawable()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIcon
   */
  public void setButtonIconDrawable(@Nullable Drawable drawable) {
    buttonIconDrawable = drawable;
    refreshButtonDrawable();
  }

  /**
   * Returns the button icon drawable, or null if none.
   *
   * <p/> This method expects that the icon will be the second layer of a two-layer drawable.
   *
   * @see #setButtonIconDrawable(Drawable)
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIcon
   */
  @Nullable
  public Drawable getButtonIconDrawable() {
    return buttonIconDrawable;
  }

  /**
   * Sets the checkbox button icon's tint list, if an icon is present.
   *
   * <p/> This method expects that the icon will be the second layer of a two-layer drawable.
   *
   * @param tintList the tint to set on the button icon
   * @see #getButtonIconTintList()
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIconTint
   */
  public void setButtonIconTintList(@Nullable ColorStateList tintList) {
    if (buttonIconTintList == tintList) {
      return;
    }
    buttonIconTintList = tintList;
    refreshButtonDrawable();
  }

  /**
   * Returns the checkbox button icon's tint list.
   *
   * @see #setButtonIconTintList(ColorStateList)
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIconTint
   */
  @Nullable
  public ColorStateList getButtonIconTintList() {
    return buttonIconTintList;
  }

  /**
   * Specifies the blending mode used to apply the tint specified by
   * {@link #setButtonIconTintList(ColorStateList)}} to the button icon drawable. The default mode
   * is {@link PorterDuff.Mode#SRC_IN}.
   *
   * @see #getButtonIconTintMode()
   * @param tintMode the blending mode used to apply the tint
   * @attr ref com.google.android.material.R.styleable#MaterialCheckBox_buttonIconTintMode
   */
  public void setButtonIconTintMode(@NonNull PorterDuff.Mode tintMode) {
    if (buttonIconTintMode == tintMode) {
      return;
    }
    buttonIconTintMode = tintMode;
    refreshButtonDrawable();
  }

  /**
   * Returns the blending mode used to apply the tint to the button icon drawable.
   *
   * @see #setButtonIconTintMode(Mode)
   * @attr ref com.google.android.material.R.styleable#MaterialSwitch_buttonIconTintMode
   */
  @NonNull
  public PorterDuff.Mode getButtonIconTintMode() {
    return buttonIconTintMode;
  }

  /**
   * Forces the {@link MaterialCheckBox} to use colors from a Material Theme. Overrides any
   * specified ButtonTintList. If set to false, sets the tints to null.
   */
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      CompoundButtonCompat.setButtonTintList(this, getMaterialThemeColorsTintList());
    } else {
      CompoundButtonCompat.setButtonTintList(this, null);
    }
  }

  /** Returns true if this {@link MaterialCheckBox} defaults to colors from a Material Theme. */
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  /**
   * Sets whether this {@link MaterialCheckBox} should center the checkbox icon when there is no
   * text. Default is true.
   */
  public void setCenterIfNoTextEnabled(boolean centerIfNoTextEnabled) {
    this.centerIfNoTextEnabled = centerIfNoTextEnabled;
  }

  /**
   * Returns true if this {@link MaterialCheckBox} will center the checkbox icon when there is no
   * text.
   */
  public boolean isCenterIfNoTextEnabled() {
    return centerIfNoTextEnabled;
  }

  private void refreshButtonDrawable() {
    buttonDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            buttonDrawable, buttonTintList, CompoundButtonCompat.getButtonTintMode(this));
    buttonIconDrawable =
        DrawableUtils.createTintableDrawableIfNeeded(
            buttonIconDrawable, buttonIconTintList, buttonIconTintMode);

    setUpDefaultButtonDrawableAnimationIfNeeded();
    updateButtonTints();

    super.setButtonDrawable(
        DrawableUtils.compositeTwoLayeredDrawable(buttonDrawable, buttonIconDrawable));

    refreshDrawableState();
  }

  @Override
  public void jumpDrawablesToCurrentState() {
    super.jumpDrawablesToCurrentState();
    if (buttonIconDrawable != null) {
      buttonIconDrawable.jumpToCurrentState();
    }
  }

  /**
   * Set the transition animation from checked to unchecked programmatically so that we can control
   * the color change between states.
   */
  private void setUpDefaultButtonDrawableAnimationIfNeeded() {
    if (!usingDefaultButtonDrawable) {
      return;
    }

    if (transitionToUnchecked != null) {
      transitionToUnchecked.unregisterAnimationCallback(transitionToUncheckedCallback);
      transitionToUnchecked.registerAnimationCallback(transitionToUncheckedCallback);
    }

    // Due to a framework bug where AnimatedStateListDrawableCompat doesn't support constant state
    // in lower APIs while LayerDrawable assumes it does, causing a crash, we can only have the
    // color change animation in N+.
    if (VERSION.SDK_INT >= VERSION_CODES.N
        && buttonDrawable instanceof AnimatedStateListDrawable
        && transitionToUnchecked != null) {
      ((AnimatedStateListDrawable) buttonDrawable)
          .addTransition(
              R.id.checked, R.id.unchecked, transitionToUnchecked, /* reversible= */ false);
    }
  }

  private void updateButtonTints() {
    if (buttonDrawable != null && buttonTintList != null) {
      DrawableCompat.setTintList(buttonDrawable, buttonTintList);
    }

    if (buttonIconDrawable != null && buttonIconTintList != null) {
      DrawableCompat.setTintList(buttonIconDrawable, buttonIconTintList);
    }
  }

  @Nullable
  private ColorStateList getSuperButtonTintList() {
    if (buttonTintList != null) {
      return buttonTintList;
    }
    if (VERSION.SDK_INT >= 21 && super.getButtonTintList() != null) {
      return super.getButtonTintList();
    }
    return ((TintableCompoundButton) this).getSupportButtonTintList();
  }

  private ColorStateList getMaterialThemeColorsTintList() {
    if (materialThemeColorsTintList == null) {
      int[] checkBoxColorsList = new int[CHECKBOX_STATES.length];
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorError = MaterialColors.getColor(this, R.attr.colorError);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);

      checkBoxColorsList[0] =
          MaterialColors.layer(colorSurface, colorError, MaterialColors.ALPHA_FULL);
      checkBoxColorsList[1] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      checkBoxColorsList[2] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
      checkBoxColorsList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      checkBoxColorsList[4] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);

      materialThemeColorsTintList = new ColorStateList(CHECKBOX_STATES, checkBoxColorsList);
    }
    return materialThemeColorsTintList;
  }
}
