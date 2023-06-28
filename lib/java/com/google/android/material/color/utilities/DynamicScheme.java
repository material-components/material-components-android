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

package com.google.android.material.color.utilities;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * Provides important settings for creating colors dynamically, and 6 color palettes. Requires: 1. A
 * color. (source color) 2. A theme. (Variant) 3. Whether or not its dark mode. 4. Contrast level.
 * (-1 to 1, currently contrast ratio 3.0 and 7.0)
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class DynamicScheme {
  public final int sourceColorArgb;
  public final Hct sourceColorHct;
  public final Variant variant;
  public final boolean isDark;
  public final double contrastLevel;

  public final TonalPalette primaryPalette;
  public final TonalPalette secondaryPalette;
  public final TonalPalette tertiaryPalette;
  public final TonalPalette neutralPalette;
  public final TonalPalette neutralVariantPalette;
  public final TonalPalette errorPalette;

  public DynamicScheme(
      Hct sourceColorHct,
      Variant variant,
      boolean isDark,
      double contrastLevel,
      TonalPalette primaryPalette,
      TonalPalette secondaryPalette,
      TonalPalette tertiaryPalette,
      TonalPalette neutralPalette,
      TonalPalette neutralVariantPalette) {
    this.sourceColorArgb = sourceColorHct.toInt();
    this.sourceColorHct = sourceColorHct;
    this.variant = variant;
    this.isDark = isDark;
    this.contrastLevel = contrastLevel;

    this.primaryPalette = primaryPalette;
    this.secondaryPalette = secondaryPalette;
    this.tertiaryPalette = tertiaryPalette;
    this.neutralPalette = neutralPalette;
    this.neutralVariantPalette = neutralVariantPalette;
    this.errorPalette = TonalPalette.fromHueAndChroma(25.0, 84.0);
  }

  /**
   * Given a set of hues and set of hue rotations, locate which hues the source color's hue is
   * between, apply the rotation at the same index as the first hue in the range, and return the
   * rotated hue.
   *
   * @param sourceColorHct The color whose hue should be rotated.
   * @param hues A set of hues.
   * @param rotations A set of hue rotations.
   * @return Color's hue with a rotation applied.
   */
  public static double getRotatedHue(Hct sourceColorHct, double[] hues, double[] rotations) {
    final double sourceHue = sourceColorHct.getHue();
    if (rotations.length == 1) {
      return MathUtils.sanitizeDegreesDouble(sourceHue + rotations[0]);
    }
    final int size = hues.length;
    for (int i = 0; i <= (size - 2); i++) {
      final double thisHue = hues[i];
      final double nextHue = hues[i + 1];
      if (thisHue < sourceHue && sourceHue < nextHue) {
        return MathUtils.sanitizeDegreesDouble(sourceHue + rotations[i]);
      }
    }
    // If this statement executes, something is wrong, there should have been a rotation
    // found using the arrays.
    return sourceHue;
  }
}
