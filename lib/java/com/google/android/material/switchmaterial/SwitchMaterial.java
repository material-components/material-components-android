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

package com.google.android.material.switchmaterial;

import com.google.android.material.R;

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.elevation.ElevationOverlayProvider;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;

/**
 * A class that creates a Material Themed Switch.
 *
 * <p>This class uses attributes from the Material Theme to style a Switch. Excepting color changes,
 * it behaves identically to {@link SwitchCompat}. Your theme's {@code ?attr/colorControlActivated},
 * {@code ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must be set. Because {@link
 * SwitchCompat} does not extend {@link android.widget.Switch}, you must explicitly declare {@link
 * SwitchMaterial} in your layout XML.
 */
public class SwitchMaterial extends SwitchCompat {

  private static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_CompoundButton_Switch;
  private static final int[][] ENABLED_CHECKED_STATES =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, // [0]
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, // [1]
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, // [2]
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} // [3]
      };

  @NonNull private final ElevationOverlayProvider elevationOverlayProvider;

  @Nullable private ColorStateList materialThemeColorsThumbTintList;
  @Nullable private ColorStateList materialThemeColorsTrackTintList;
  private boolean useMaterialThemeColors;

  public SwitchMaterial(@NonNull Context context) {
    this(context, null);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, androidx.appcompat.R.attr.switchStyle);
  }

  public SwitchMaterial(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    elevationOverlayProvider = new ElevationOverlayProvider(context);

    TypedArray attributes =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.SwitchMaterial, defStyleAttr, DEF_STYLE_RES);

    useMaterialThemeColors =
        attributes.getBoolean(R.styleable.SwitchMaterial_useMaterialThemeColors, false);

    attributes.recycle();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (useMaterialThemeColors && getThumbTintList() == null) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
    }
    if (useMaterialThemeColors && getTrackTintList() == null) {
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    }
  }

  /**
   * Forces the {@link SwitchMaterial} to use colors from a Material Theme. Overrides any specified
   * tint list for the track and thumb. If set to false, sets the tints to null. Use {@link
   * SwitchCompat#setTrackTintList(ColorStateList)} and {@link
   * SwitchCompat#setThumbTintList(ColorStateList)} to change tints.
   */
  public void setUseMaterialThemeColors(boolean useMaterialThemeColors) {
    this.useMaterialThemeColors = useMaterialThemeColors;
    if (useMaterialThemeColors) {
      setThumbTintList(getMaterialThemeColorsThumbTintList());
      setTrackTintList(getMaterialThemeColorsTrackTintList());
    } else {
      setThumbTintList(null);
      setTrackTintList(null);
    }
  }

  /** Returns true if this {@link SwitchMaterial} defaults to colors from a Material Theme. */
  public boolean isUseMaterialThemeColors() {
    return useMaterialThemeColors;
  }

  private ColorStateList getMaterialThemeColorsThumbTintList() {
    if (materialThemeColorsThumbTintList == null) {
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorControlActivated =
          MaterialColors.getColor(this, androidx.appcompat.R.attr.colorControlActivated);
      float thumbElevation = getResources().getDimension(R.dimen.mtrl_switch_thumb_elevation);
      if (elevationOverlayProvider.isThemeElevationOverlayEnabled()) {
        thumbElevation += ViewUtils.getParentAbsoluteElevation(this);
      }
      int colorThumbOff =
          elevationOverlayProvider.compositeOverlayIfNeeded(colorSurface, thumbElevation);

      int[] switchThumbColorsList = new int[ENABLED_CHECKED_STATES.length];
      switchThumbColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_FULL);
      switchThumbColorsList[1] = colorThumbOff;
      switchThumbColorsList[2] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED);
      switchThumbColorsList[3] = colorThumbOff;
      materialThemeColorsThumbTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, switchThumbColorsList);
    }
    return materialThemeColorsThumbTintList;
  }

  private ColorStateList getMaterialThemeColorsTrackTintList() {
    if (materialThemeColorsTrackTintList == null) {
      int[] switchTrackColorsList = new int[ENABLED_CHECKED_STATES.length];
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);
      int colorControlActivated =
          MaterialColors.getColor(this, androidx.appcompat.R.attr.colorControlActivated);
      int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
      switchTrackColorsList[0] =
          MaterialColors.layer(colorSurface, colorControlActivated, MaterialColors.ALPHA_MEDIUM);
      switchTrackColorsList[1] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_LOW);
      switchTrackColorsList[2] =
          MaterialColors.layer(
              colorSurface, colorControlActivated, MaterialColors.ALPHA_DISABLED_LOW);
      switchTrackColorsList[3] =
          MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED_LOW);
      materialThemeColorsTrackTintList =
          new ColorStateList(ENABLED_CHECKED_STATES, switchTrackColorsList);
    }
    return materialThemeColorsTrackTintList;
  }
}
