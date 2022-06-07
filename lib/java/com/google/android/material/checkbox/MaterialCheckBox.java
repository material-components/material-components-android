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
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.widget.CompoundButtonCompat;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialResources;
import java.util.LinkedHashSet;

/**
 * A class that creates a Material Themed CheckBox.
 *
 * <p>This class uses attributes from the Material Theme to style a CheckBox. Excepting color
 * changes, it behaves identically to {@link AppCompatCheckBox}. Your theme's {@code
 * ?attr/colorControlActivated}, {@code ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must
 * be set.
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

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialCheckBox, defStyleAttr, DEF_STYLE_RES);

    // If buttonTint is specified, read it using MaterialResources to allow themeable attributes in
    // all API levels.
    if (attributes.hasValue(R.styleable.MaterialCheckBox_buttonTint)) {
      CompoundButtonCompat.setButtonTintList(
          this,
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.MaterialCheckBox_buttonTint));
    }

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialCheckBox_useMaterialThemeColors, false);
    centerIfNoTextEnabled =
        attributes.getBoolean(R.styleable.MaterialCheckBox_centerIfNoTextEnabled, true);
    errorShown = attributes.getBoolean(R.styleable.MaterialCheckBox_errorShown, false);
    errorAccessibilityLabel =
        attributes.getText(R.styleable.MaterialCheckBox_errorAccessibilityLabel);

    attributes.recycle();
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

    if (useMaterialThemeColors && CompoundButtonCompat.getButtonTintList(this) == null) {
      setUseMaterialThemeColors(true);
    }
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    final int[] drawableStates = super.onCreateDrawableState(extraSpace + 1);

    if (isErrorShown()) {
      mergeDrawableStates(drawableStates, ERROR_STATE_SET);
    }

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

  /**
   * Forces the {@link MaterialCheckBox} to use colors from a Material Theme. Overrides any
   * specified ButtonTintList. If set to false, sets the tints to null. Use {@link
   * MaterialCheckBox#setSupportButtonTintList} to change button tints.
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
