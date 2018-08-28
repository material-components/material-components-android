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
import com.google.android.material.internal.ThemeEnforcement;
import android.support.v4.graphics.ColorUtils;
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

  private final int[][] enabledStates =
      new int[][] {
        new int[] {android.R.attr.state_enabled}, // [0]
        new int[] {-android.R.attr.state_enabled}, // [1]
      };

  public SwitchMaterial(Context context) {
    this(context, null);
  }

  public SwitchMaterial(Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.switchStyle);
  }

  public SwitchMaterial(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    int colorSecondary = ThemeEnforcement.resolveAttributeOrThrow(this, R.attr.colorSecondary).data;
    int colorSurface = ThemeEnforcement.resolveAttributeOrThrow(this, R.attr.colorSurface).data;
    int colorOnSurface = ThemeEnforcement.resolveAttributeOrThrow(this, R.attr.colorOnSurface).data;
    int colorText = this.getCurrentTextColor();

    int[] switchThumbColorsList = new int[enabledCheckedStates.length];
    switchThumbColorsList[0] = colorSecondary;
    switchThumbColorsList[1] = colorSurface;
    switchThumbColorsList[2] = ColorUtils.blendARGB(colorSurface, colorSecondary, 0.38F);
    switchThumbColorsList[3] = colorSurface;

    int[] switchTrackColorsList = new int[enabledCheckedStates.length];
    switchTrackColorsList[0] = ColorUtils.blendARGB(colorSurface, colorSecondary, 0.54F);
    switchTrackColorsList[1] = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.32F);
    switchTrackColorsList[2] = ColorUtils.blendARGB(colorSurface, colorSecondary, 0.12F);
    switchTrackColorsList[3] = ColorUtils.blendARGB(colorSurface, colorOnSurface, 0.12F);

    int[] switchTextColorList = new int[enabledStates.length];
    switchTextColorList[0] = colorText;
    switchTextColorList[1] = ColorUtils.blendARGB(colorSurface, colorText, 0.38F);

    setThumbTintList(new ColorStateList(enabledCheckedStates, switchThumbColorsList));
    setTrackTintList(new ColorStateList(enabledCheckedStates, switchTrackColorsList));
    setTextColor(new ColorStateList(enabledStates, switchTextColorList));
  }
}
