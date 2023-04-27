/*
 * Copyright (C) 2023 The Android Open Source Project
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
import com.google.errorprone.annotations.CanIgnoreReturnValue;

/**
 * Wrapper class for specifying color contrast options when applying contrast to branded and custom
 * themes. Clients have the options to provide theme overlay resource ids for medium and high
 * contrast mode.
 *
 * <p>An example of the provided theme overlay resource ids could be one of the following:
 *
 * <ul>
 *   <li>contrast in light mode: R.style.ThemeOverlay_XxxContrast_Light
 *   <li>contrast in dark mode: R.style.ThemeOverlay_XxxContrast_Dark
 *   <li>contrast in both light and dark mode: R.style.ThemeOverlay_XxxContrast_DayNight
 * </ul>
 */
public class ColorContrastOptions {

  @StyleRes private final int mediumContrastThemeOverlayResourceId;

  @StyleRes private final int highContrastThemeOverlayResourceId;

  private ColorContrastOptions(Builder builder) {
    this.mediumContrastThemeOverlayResourceId = builder.mediumContrastThemeOverlayResourceId;
    this.highContrastThemeOverlayResourceId = builder.highContrastThemeOverlayResourceId;
  }

  /** Returns the resource id of the medium contrast theme overlay. */
  @StyleRes
  public int getMediumContrastThemeOverlay() {
    return mediumContrastThemeOverlayResourceId;
  }

  /** Returns the resource id of the high contrast theme overlay. */
  @StyleRes
  public int getHighContrastThemeOverlay() {
    return highContrastThemeOverlayResourceId;
  }

  /** Builder class for specifying options when applying contrast. */
  public static class Builder {

    @StyleRes private int mediumContrastThemeOverlayResourceId;

    @StyleRes private int highContrastThemeOverlayResourceId;

    /** Sets the resource id of the medium contrast theme overlay. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setMediumContrastThemeOverlay(
        @StyleRes int mediumContrastThemeOverlayResourceId) {
      this.mediumContrastThemeOverlayResourceId = mediumContrastThemeOverlayResourceId;
      return this;
    }

    /** Sets the resource id of the high contrast theme overlay. */
    @NonNull
    @CanIgnoreReturnValue
    public Builder setHighContrastThemeOverlay(@StyleRes int highContrastThemeOverlayResourceId) {
      this.highContrastThemeOverlayResourceId = highContrastThemeOverlayResourceId;
      return this;
    }

    @NonNull
    public ColorContrastOptions build() {
      return new ColorContrastOptions(this);
    }
  }
}
