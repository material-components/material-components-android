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

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;

/**
 * A class for specifying color attributes for harmonization, which contains an array of color
 * attributes, with the option to specify a custom theme overlay.
 */
public final class HarmonizedColorAttributes {

  private final int[] attributes;
  @StyleRes private final int themeOverlay;

  private static final int[] HARMONIZED_MATERIAL_ATTRIBUTES =
      new int[] {
        androidx.appcompat.R.attr.colorError,
        R.attr.colorOnError,
        R.attr.colorErrorContainer,
        R.attr.colorOnErrorContainer
      };

  /**
   * Create {@link HarmonizedColorAttributes} with an array of color attributes. If this is
   * called, the resources pointed by the attributes will be resolved at runtime and harmonized.
   * If you're concerned about accidentally overwriting color resources, see
   * {@link #create(int[], int)}.
   */
  @NonNull
  public static HarmonizedColorAttributes create(@NonNull @AttrRes int[] attributes) {
    return new HarmonizedColorAttributes(attributes, 0);
  }

  /**
   * Create {@link HarmonizedColorAttributes} with a theme overlay, along with an array of
   * attributes in the theme overlay.
   *
   * <p>In this method, instead of the color resource that the color attribute is pointing to in
   * the main theme/context being harmonized directly, the color resource in the theme overlay
   * context will be replaced with the harmonized color attribute instead.
   */
  @NonNull
  public static HarmonizedColorAttributes create(
      @NonNull @AttrRes int[] attributes, @StyleRes int themeOverlay) {
    return new HarmonizedColorAttributes(attributes, themeOverlay);
  }

  /**
   * Create {@link HarmonizedColorAttributes} with Material default, with Error colors being
   * harmonized.
   *
   * <p>Instead of directly overwriting the resources that `colorError`, `colorOnError`,
   * `colorErrorContainer` and `colorOnErrorContainer` points to in the main theme/context, we
   * would:
   *
   * <p>
   *   1. look up the resources values in the theme overlay `Context`.
   *   2. retrieve the harmonized resources with Primary.
   *   3. replace `@color/material_harmonized_color_error`,
   *      `@color/material_harmonized_color_on_error`, etc. with the harmonized resources.
   *
   * <p>That way the Error roles in the theme overlay would point to harmonized resources.
   */
  @NonNull
  public static HarmonizedColorAttributes createMaterialDefaults() {
    return create(HARMONIZED_MATERIAL_ATTRIBUTES, R.style.ThemeOverlay_Material3_HarmonizedColors);
  }

  private HarmonizedColorAttributes(
      @NonNull @AttrRes int[] attributes, @StyleRes int themeOverlay) {
    if (themeOverlay != 0 && attributes.length == 0) {
      throw new IllegalArgumentException(
          "Theme overlay should be used with the accompanying int[] attributes.");
    }
    this.attributes = attributes;
    this.themeOverlay = themeOverlay;
  }

  /** Returns the array of color attributes for harmonization. */
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
