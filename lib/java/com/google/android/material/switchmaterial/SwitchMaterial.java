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

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import android.support.v7.widget.SwitchCompat;
import android.util.AttributeSet;

/**
 * A class that creates a Material Themed Switch.
 *
 * <p>This class uses attributes from the Material Theme to style a Switch. Excepting color changes,
 * it behaves identically to {@link SwitchCompat}. Your theme's {@code ?attr/colorSecondary}, {@code
 * ?attr/colorSurface}, and {@code ?attr/colorOnSurface} must be set. Because {@link SwitchCompat}
 * does not extend {@link android.widget.Switch}, you must explicitly declare {@link SwitchMaterial}
 * in your layout XML.
 */
public class SwitchMaterial extends SwitchCompat {

  private final int[][] enabledCheckedStates =
      new int[][] {
        new int[] {android.R.attr.state_enabled, android.R.attr.state_checked}, // [0]
        new int[] {android.R.attr.state_enabled, -android.R.attr.state_checked}, // [1]
        new int[] {-android.R.attr.state_enabled, android.R.attr.state_checked}, // [2]
        new int[] {-android.R.attr.state_enabled, -android.R.attr.state_checked} // [3]
      };

  public SwitchMaterial(Context context) {
    this(context, null);
  }

  public SwitchMaterial(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.switchStyle);
  }

  public SwitchMaterial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    if (getThumbTintList() == null || getTrackTintList() == null) {
      int colorSecondary = MaterialColors.getColor(this, R.attr.colorSecondary);
      int colorSurface = MaterialColors.getColor(this, R.attr.colorSurface);

      if (getThumbTintList() == null) {
        setColorThemedThumbTintList(colorSurface, colorSecondary);
      }
      if (getTrackTintList() == null) {
        int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
        setColorThemedTrackTintList(colorSurface, colorSecondary, colorOnSurface);
      }
    }
  }

  private void setColorThemedThumbTintList(int colorSurface, int colorSecondary) {
    int[] switchThumbColorsList = new int[enabledCheckedStates.length];
    switchThumbColorsList[0] =
        MaterialColors.layer(colorSurface, colorSecondary, MaterialColors.ALPHA_FULL);
    switchThumbColorsList[1] =
        MaterialColors.layer(colorSurface, colorSurface, MaterialColors.ALPHA_FULL);
    switchThumbColorsList[2] =
        MaterialColors.layer(colorSurface, colorSecondary, MaterialColors.ALPHA_DISABLED);
    switchThumbColorsList[3] =
        MaterialColors.layer(colorSurface, colorSurface, MaterialColors.ALPHA_FULL);
    setThumbTintList(new ColorStateList(enabledCheckedStates, switchThumbColorsList));
  }

  private void setColorThemedTrackTintList(
      int colorSurface, int colorSecondary, int colorOnSurface) {
    int[] switchTrackColorsList = new int[enabledCheckedStates.length];
    switchTrackColorsList[0] =
        MaterialColors.layer(colorSurface, colorSecondary, MaterialColors.ALPHA_MEDIUM);
    switchTrackColorsList[1] =
        MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_LOW);
    switchTrackColorsList[2] =
        MaterialColors.layer(colorSurface, colorSecondary, MaterialColors.ALPHA_DISABLED_LOW);
    switchTrackColorsList[3] =
        MaterialColors.layer(colorSurface, colorOnSurface, MaterialColors.ALPHA_DISABLED_LOW);

    setTrackTintList(new ColorStateList(enabledCheckedStates, switchTrackColorsList));
  }
}
