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

import android.content.Context;
import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

/**
 * Wrapper class for specifying harmonization options, whether to harmonize an int array of color
 * resources, an int array of color attributes and/or harmonize with theme overlay.
 */
public class HarmonizedColorsOptions {

  @NonNull private final Context context;
  @NonNull private final int[] colorResourceIds;
  @Nullable private final HarmonizedColorAttributes colorAttributes;
  @AttrRes private final int colorAttributeToHarmonizeWith;

  /**
   * Create HarmonizedColorsOptions with Material default, with Error colors being harmonized with
   * Primary.
   */
  @NonNull
  public static HarmonizedColorsOptions createMaterialDefaults(@NonNull Context context) {
    return new HarmonizedColorsOptions.Builder(context)
        .setColorAttributes(HarmonizedColorAttributes.createMaterialDefaults())
        .build();
  }

  private HarmonizedColorsOptions(Builder builder) {
    this.context = builder.context;
    this.colorResourceIds = builder.colorResourceIds;
    this.colorAttributes = builder.colorAttributes;
    this.colorAttributeToHarmonizeWith = builder.colorAttributeToHarmonizeWith;
  }

  /** Returns the {@link Context} for harmonization. */
  @NonNull
  public Context getContext() {
    return context;
  }

  /** Returns the int array of color resource ids for harmonization. */
  @NonNull
  public int[] getColorResourceIds() {
    return colorResourceIds;
  }

  /** Returns the color attributes for harmonization. */
  @Nullable
  public HarmonizedColorAttributes getColorAttributes() {
    return colorAttributes;
  }

  /** Returns the color attribute to harmonize with for harmonization. */
  @AttrRes
  public int getColorAttributeToHarmonizeWith() {
    return colorAttributeToHarmonizeWith;
  }

  /**
   * Builder class for specifying options when harmonizing colors. When building {@code
   * ColorResourceHarmonizerOptions}, a {@link Context} is required.
   */
  public static class Builder {

    @NonNull private final Context context;
    @NonNull private int[] colorResourceIds = new int[] {};
    @Nullable private HarmonizedColorAttributes colorAttributes;
    @AttrRes private int colorAttributeToHarmonizeWith = R.attr.colorPrimary;

    public Builder(@NonNull Context context) {
      this.context = context;
    }

    /** Sets the int array of color resource ids for harmonization. */
    @NonNull
    public Builder setColorResourceIds(@NonNull int[] colorResourceIds) {
      this.colorResourceIds = colorResourceIds;
      return this;
    }

    /** Sets the harmonized color attributes for harmonization. */
    @Nullable
    public Builder setColorAttributes(@Nullable HarmonizedColorAttributes colorAttributes) {
      this.colorAttributes = colorAttributes;
      return this;
    }

    /** Sets the color attribute to harmonize with for harmonization. */
    @NonNull
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
  int getThemeOverlayResourceId() {
    return this.colorAttributes != null ? colorAttributes.getThemeOverlay() : 0;
  }
}
