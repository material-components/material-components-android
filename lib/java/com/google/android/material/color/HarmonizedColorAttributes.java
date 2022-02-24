/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.color;

import com.google.android.material.R;

import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

/**
 * A class for specifying color attributes for harmonization, which would contain an int array of
 * color attributes, with the option to specify a custom theme overlay.
 */
public final class HarmonizedColorAttributes {

  private final int[] attributes;
  @StyleRes private final int themeOverlay;

  private static final int[] HARMONIZED_MATERIAL_ATTRIBUTES =
      new int[] {
        R.attr.colorError,
        R.attr.colorOnError,
        R.attr.colorErrorContainer,
        R.attr.colorOnErrorContainer
      };

  /** Create HarmonizedColorAttributes with an int array of color attributes. */
  @NonNull
  public static HarmonizedColorAttributes create(@NonNull int[] attributes) {
    return new HarmonizedColorAttributes(attributes, 0);
  }

  /**
   * Create HarmonizedColorAttributes with a theme overlay, along with an int array of attributes in
   * the theme overlay.
   */
  @NonNull
  public static HarmonizedColorAttributes create(
      @NonNull int[] attributes, @StyleRes int themeOverlay) {
    return new HarmonizedColorAttributes(attributes, themeOverlay);
  }

  /** Create HarmonizedColorAttributes with Material default, with Error colors being harmonized. */
  @NonNull
  public static HarmonizedColorAttributes createMaterialDefaults() {
    return create(HARMONIZED_MATERIAL_ATTRIBUTES, R.style.ThemeOverlay_Material3_HarmonizedColors);
  }

  private HarmonizedColorAttributes(@NonNull int[] attributes, @StyleRes int themeOverlay) {
    if (themeOverlay != 0 && attributes.length == 0) {
      throw new IllegalArgumentException(
          "Theme overlay should be used with the accompanying int[] attributes.");
    }
    this.attributes = attributes;
    this.themeOverlay = themeOverlay;
  }

  /** Returns the int array of color attributes for harmonization. */
  @NonNull
  public int[] getAttributes() {
    return attributes;
  }

  /** Returns the custom theme overlay for harmonization, default is 0 if not specified. */
  @StyleRes
  public int getThemeOverlay() {
    return themeOverlay;
  }
}
