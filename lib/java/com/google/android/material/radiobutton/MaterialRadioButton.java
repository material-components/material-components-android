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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.core.widget.CompoundButtonCompat;
import androidx.appcompat.widget.AppCompatRadioButton;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;

/**
 * A class that creates a Material Themed RadioButton.
 *
 * <p>This class uses attributes from the Material Theme to style a RadioButton. Excepting color
 * changes, it behaves identically to {@link AppCompatRadioButton}. Your theme's {@code
 * ?attr/colorControlActivated}, {@code ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must
 * be set.
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
  private boolean useMaterialThemeColors;

  public MaterialRadioButton(@NonNull Context context) {
    this(context, null);
  }

  public MaterialRadioButton(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.radioButtonStyle);
  }

  public MaterialRadioButton(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.MaterialRadioButton, defStyleAttr, DEF_STYLE_RES);

    // If buttonTint is specified, read it using MaterialResources to allow themeable attributes in
    // all API levels.
    if (attributes.hasValue(R.styleable.MaterialRadioButton_buttonTint)) {
      CompoundButtonCompat.setButtonTintList(
          this,
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.MaterialRadioButton_buttonTint));
    }

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.MaterialRadioButton_useMaterialThemeColors, false);

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
   * Forces the {@link MaterialRadioButton} to use colors from a Material Theme. Overrides any
   * specified ButtonTintList. If set to false, sets the tints to null. Use {@link
   * MaterialRadioButton#setSupportButtonTintList} to change button tints.
   */
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      CompoundButtonCompat.setButtonTintList(this, getMaterialThemeColorsTintList());
    } else {
      CompoundButtonCompat.setButtonTintList(this, null);
    }
  }

  /** Returns true if this {@link MaterialRadioButton} defaults to colors from a Material Theme. */
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  private ColorStateList getMaterialThemeColorsTintList() {
    if (materialThemeColorsTintList == null) {
      int colorControlActivated = MaterialColors.getColor(this, R.attr.colorControlActivated);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);

      int[] radioButtonColorList = new int[ENABLED_CHECKED_STATES.length];
      radioButtonColorList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
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
