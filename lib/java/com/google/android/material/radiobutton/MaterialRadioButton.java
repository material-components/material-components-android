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

package com.google.android.material.radiobutton;

import com.google.android.material.R;

import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v7.widget.AppCompatRadioButton;
import android.util.AttributeSet;

/**
 * A class that creates a Material Themed RadioButton.
 *
 * <p>This class uses attributes from the Material Theme to style a RadioButton. Excepting color
 * changes, it behaves identically to {@link AppCompatRadioButton}. Your theme's {@code
 * ?attr/colorSecondary}, {@code ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must be set.
 */
public class MaterialRadioButton extends AppCompatRadioButton {

  private static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CompoundButton_RadioButton;
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, // [0]
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, // [1]
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, // [2]
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} // [3]
      };
  @Nullable private ColorStateList materialThemeColorsTintList;

  public MaterialRadioButton(Context context) {
    this(context, null);
  }

  public MaterialRadioButton(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.radioButtonStyle);
  }

  public MaterialRadioButton(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialRadioButton, defStyleAttr, DEF_STYLE_RES);

    boolean useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialRadioButton_useMaterialThemeColors, false);

    attributes.recycle();

    if (useMaterialThemeColors && CompoundButtonCompat.getButtonTintList(this) == null) {
      setUseMaterialThemeColors(true);
    }
  }

  /**
   * Forces the {@link MaterialRadioButton} to use colors from a Material Theme. Overrides any
   * specified ButtonTintList. If set to false, sets the tints to null. Use {@link
   * MaterialRadioButton#setSupportButtonTintList} to change button tints.
   */
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    if (useMaterialThemeColors) {
      CompoundButtonCompat.setButtonTintList(this, getMaterialThemeColorsTintList());
    } else {
      CompoundButtonCompat.setButtonTintList(this, null);
    }
  }

  /**
   * Returns true if the colors of this {@link MaterialRadioButton} are from a Material Theme.
   *
   * @return True if the colors of this {@link MaterialRadioButton} are from a Material Theme.
   */
  public boolean isUseMaterialThemeColors() {
    return materialThemeColorsTintList != null
        && materialThemeColorsTintList.equals(CompoundButtonCompat.getButtonTintList(this));
  }

  private ColorStateList getMaterialThemeColorsTintList() {
    if (materialThemeColorsTintList == null) {
      int colorSecondary = MaterialColors.getColor(this, R.attr.colorSecondary);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);

      int[] radioButtonColorList = new int[ENABLED_CHECKED_STATES.length];
      radioButtonColorList[0] =
          MaterialColors.layer(colorSurface, colorSecondary, MaterialColors.ALPHA_FULL);
      radioButtonColorList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
      radioButtonColorList[2] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      radioButtonColorList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      materialThemeColorsTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, radioButtonColorList);
    }
    return materialThemeColorsTintList;
  }
}
