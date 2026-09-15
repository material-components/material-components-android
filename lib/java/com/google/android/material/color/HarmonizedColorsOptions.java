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

import androidx.annotation.AttrRes;
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Wrapper class for specifying harmonization options, whether to harmonize an array of color
 * resources, or a {@link HarmonizedColorAttributes}, along with the color attribute provided to
 * harmonize with.
 */
public class HarmonizedColorsOptions {

  @NonNull @ColorRes private final int[] colorResourceIds;
  @Nullable private final HarmonizedColorAttributes colorAttributes;
  @AttrRes private final int colorAttributeToHarmonizeWith;

  /**
   * Create HarmonizedColorsOptions with Material default, with Error colors being harmonized with
   * Primary.
   */
  @NonNull
  public static HarmonizedColorsOptions createMaterialDefaults() {
    return new HarmonizedColorsOptions.Builder()
        .setColorAttributes(HarmonizedColorAttributes.createMaterialDefaults())
        .build();
  }

  private HarmonizedColorsOptions(Builder builder) {
    this.colorResourceIds = builder.colorResourceIds;
    this.colorAttributes = builder.colorAttributes;
    this.colorAttributeToHarmonizeWith = builder.colorAttributeToHarmonizeWith;
  }

  /** Returns the array of color resource ids that needs to be harmonized. */
  @NonNull
  @ColorRes
  public int[] getColorResourceIds() {
    return colorResourceIds;
  }

  /** Returns the {@link HarmonizedColorAttributes} that needs to be harmonized. */
  @Nullable
  public HarmonizedColorAttributes getColorAttributes() {
    return colorAttributes;
  }

  /**
   * Returns the color attribute to harmonize color resources and {@link HarmonizedColorAttributes}
   * with.
   */
  @AttrRes
  public int getColorAttributeToHarmonizeWith() {
    return colorAttributeToHarmonizeWith;
  }

  /** Builder class for specifying options when harmonizing colors. */
  public static class Builder {

    @NonNull @ColorRes private int[] colorResourceIds = new int[] {};
    @Nullable private HarmonizedColorAttributes colorAttributes;

    @AttrRes
    private int colorAttributeToHarmonizeWith = androidx.appcompat.R.attr.colorPrimary;

    /**
     * Sets the array of color resource ids for harmonization.
     *
     * @param colorResourceIds The array of color resource ids that needs to be harmonized.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setColorResourceIds(@NonNull @ColorRes int[] colorResourceIds) {
      this.colorResourceIds = colorResourceIds;
      return this;
    }

    /**
     * Sets the harmonized color attributes for harmonization.
     *
     * <p>This method will look up the color resource the attribute points to, and harmonizing the
     * color resource directly. If you are looking to harmonize only color resources, in most cases
     * when constructing {@link HarmonizedColorsOptions},
     *
     * @see #setColorResourceIds(int[]) should be enough.
     * @param colorAttributes The {@link HarmonizedColorAttributes} that needs to be harmonized.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setColorAttributes(@Nullable HarmonizedColorAttributes colorAttributes) {
      this.colorAttributes = colorAttributes;
      return this;
    }

    /**
     * Sets the color attribute to harmonize with.
     *
     * @param colorAttributeToHarmonizeWith The color attribute provided to harmonize color
     *     resources and {@link HarmonizedColorAttributes} with.
     */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setColorAttributeToHarmonizeWith(@AttrRes int colorAttributeToHarmonizeWith) {
      this.colorAttributeToHarmonizeWith = colorAttributeToHarmonizeWith;
      return this;
    }

    @NonNull
    public HarmonizedColorsOptions build() {
      return new HarmonizedColorsOptions(this);
    }
  }

  @StyleRes
  int getThemeOverlayResourceId(@StyleRes int defaultThemeOverlay) {
    return (colorAttributes != null && colorAttributes.getThemeOverlay() != 0)
        ? colorAttributes.getThemeOverlay()
        : defaultThemeOverlay;
  }
}
