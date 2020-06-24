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
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatCheckBox;
import android.util.AttributeSet;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;

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
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, // [0]
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, // [1]
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, // [2]
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} // [3]
      };
  @Nullable private ColorStateList materialThemeColorsTintList;
  private boolean useMaterialThemeColors;

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

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && CompoundButtonCompat.getButtonTintList(this) == null) {
      setUseMaterialThemeColors(true);
    }
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

  private ColorStateList getMaterialThemeColorsTintList() {
    if (materialThemeColorsTintList == null) {
      int[] checkBoxColorsList = new int[ENABLED_CHECKED_STATES.length];
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);

      checkBoxColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      checkBoxColorsList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_MEDIUM);
      checkBoxColorsList[2] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);
      checkBoxColorsList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED);

      materialThemeColorsTintList = new ColorStateList(ENABLED_CHECKED_STATES, checkBoxColorsList);
    }
    return materialThemeColorsTintList;
  }
}
