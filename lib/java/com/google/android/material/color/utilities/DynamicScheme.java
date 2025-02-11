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

import androidx.annotation.NonNull;
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

  @NonNull
  public Hct getHct(@NonNull DynamicColor dynamicColor) {
    return dynamicColor.getHct(this);
  }

  public int getArgb(@NonNull DynamicColor dynamicColor) {
    return dynamicColor.getArgb(this);
  }

  public int getPrimaryPaletteKeyColor() {
    return getArgb(new MaterialDynamicColors().primaryPaletteKeyColor());
  }

  public int getSecondaryPaletteKeyColor() {
    return getArgb(new MaterialDynamicColors().secondaryPaletteKeyColor());
  }

  public int getTertiaryPaletteKeyColor() {
    return getArgb(new MaterialDynamicColors().tertiaryPaletteKeyColor());
  }

  public int getNeutralPaletteKeyColor() {
    return getArgb(new MaterialDynamicColors().neutralPaletteKeyColor());
  }

  public int getNeutralVariantPaletteKeyColor() {
    return getArgb(new MaterialDynamicColors().neutralVariantPaletteKeyColor());
  }

  public int getBackground() {
    return getArgb(new MaterialDynamicColors().background());
  }

  public int getOnBackground() {
    return getArgb(new MaterialDynamicColors().onBackground());
  }

  public int getSurface() {
    return getArgb(new MaterialDynamicColors().surface());
  }

  public int getSurfaceDim() {
    return getArgb(new MaterialDynamicColors().surfaceDim());
  }

  public int getSurfaceBright() {
    return getArgb(new MaterialDynamicColors().surfaceBright());
  }

  public int getSurfaceContainerLowest() {
    return getArgb(new MaterialDynamicColors().surfaceContainerLowest());
  }

  public int getSurfaceContainerLow() {
    return getArgb(new MaterialDynamicColors().surfaceContainerLow());
  }

  public int getSurfaceContainer() {
    return getArgb(new MaterialDynamicColors().surfaceContainer());
  }

  public int getSurfaceContainerHigh() {
    return getArgb(new MaterialDynamicColors().surfaceContainerHigh());
  }

  public int getSurfaceContainerHighest() {
    return getArgb(new MaterialDynamicColors().surfaceContainerHighest());
  }

  public int getOnSurface() {
    return getArgb(new MaterialDynamicColors().onSurface());
  }

  public int getSurfaceVariant() {
    return getArgb(new MaterialDynamicColors().surfaceVariant());
  }

  public int getOnSurfaceVariant() {
    return getArgb(new MaterialDynamicColors().onSurfaceVariant());
  }

  public int getInverseSurface() {
    return getArgb(new MaterialDynamicColors().inverseSurface());
  }

  public int getInverseOnSurface() {
    return getArgb(new MaterialDynamicColors().inverseOnSurface());
  }

  public int getOutline() {
    return getArgb(new MaterialDynamicColors().outline());
  }

  public int getOutlineVariant() {
    return getArgb(new MaterialDynamicColors().outlineVariant());
  }

  public int getShadow() {
    return getArgb(new MaterialDynamicColors().shadow());
  }

  public int getScrim() {
    return getArgb(new MaterialDynamicColors().scrim());
  }

  public int getSurfaceTint() {
    return getArgb(new MaterialDynamicColors().surfaceTint());
  }

  public int getPrimary() {
    return getArgb(new MaterialDynamicColors().primary());
  }

  public int getOnPrimary() {
    return getArgb(new MaterialDynamicColors().onPrimary());
  }

  public int getPrimaryContainer() {
    return getArgb(new MaterialDynamicColors().primaryContainer());
  }

  public int getOnPrimaryContainer() {
    return getArgb(new MaterialDynamicColors().onPrimaryContainer());
  }

  public int getInversePrimary() {
    return getArgb(new MaterialDynamicColors().inversePrimary());
  }

  public int getSecondary() {
    return getArgb(new MaterialDynamicColors().secondary());
  }

  public int getOnSecondary() {
    return getArgb(new MaterialDynamicColors().onSecondary());
  }

  public int getSecondaryContainer() {
    return getArgb(new MaterialDynamicColors().secondaryContainer());
  }

  public int getOnSecondaryContainer() {
    return getArgb(new MaterialDynamicColors().onSecondaryContainer());
  }

  public int getTertiary() {
    return getArgb(new MaterialDynamicColors().tertiary());
  }

  public int getOnTertiary() {
    return getArgb(new MaterialDynamicColors().onTertiary());
  }

  public int getTertiaryContainer() {
    return getArgb(new MaterialDynamicColors().tertiaryContainer());
  }

  public int getOnTertiaryContainer() {
    return getArgb(new MaterialDynamicColors().onTertiaryContainer());
  }

  public int getError() {
    return getArgb(new MaterialDynamicColors().error());
  }

  public int getOnError() {
    return getArgb(new MaterialDynamicColors().onError());
  }

  public int getErrorContainer() {
    return getArgb(new MaterialDynamicColors().errorContainer());
  }

  public int getOnErrorContainer() {
    return getArgb(new MaterialDynamicColors().onErrorContainer());
  }

  public int getPrimaryFixed() {
    return getArgb(new MaterialDynamicColors().primaryFixed());
  }

  public int getPrimaryFixedDim() {
    return getArgb(new MaterialDynamicColors().primaryFixedDim());
  }

  public int getOnPrimaryFixed() {
    return getArgb(new MaterialDynamicColors().onPrimaryFixed());
  }

  public int getOnPrimaryFixedVariant() {
    return getArgb(new MaterialDynamicColors().onPrimaryFixedVariant());
  }

  public int getSecondaryFixed() {
    return getArgb(new MaterialDynamicColors().secondaryFixed());
  }

  public int getSecondaryFixedDim() {
    return getArgb(new MaterialDynamicColors().secondaryFixedDim());
  }

  public int getOnSecondaryFixed() {
    return getArgb(new MaterialDynamicColors().onSecondaryFixed());
  }

  public int getOnSecondaryFixedVariant() {
    return getArgb(new MaterialDynamicColors().onSecondaryFixedVariant());
  }

  public int getTertiaryFixed() {
    return getArgb(new MaterialDynamicColors().tertiaryFixed());
  }

  public int getTertiaryFixedDim() {
    return getArgb(new MaterialDynamicColors().tertiaryFixedDim());
  }

  public int getOnTertiaryFixed() {
    return getArgb(new MaterialDynamicColors().onTertiaryFixed());
  }

  public int getOnTertiaryFixedVariant() {
    return getArgb(new MaterialDynamicColors().onTertiaryFixedVariant());
  }

  public int getControlActivated() {
    return getArgb(new MaterialDynamicColors().controlActivated());
  }

  public int getControlNormal() {
    return getArgb(new MaterialDynamicColors().controlNormal());
  }

  public int getControlHighlight() {
    return getArgb(new MaterialDynamicColors().controlHighlight());
  }

  public int getTextPrimaryInverse() {
    return getArgb(new MaterialDynamicColors().textPrimaryInverse());
  }

  public int getTextSecondaryAndTertiaryInverse() {
    return getArgb(new MaterialDynamicColors().textSecondaryAndTertiaryInverse());
  }

  public int getTextPrimaryInverseDisableOnly() {
    return getArgb(new MaterialDynamicColors().textPrimaryInverseDisableOnly());
  }

  public int getTextSecondaryAndTertiaryInverseDisabled() {
    return getArgb(new MaterialDynamicColors().textSecondaryAndTertiaryInverseDisabled());
  }

  public int getTextHintInverse() {
    return getArgb(new MaterialDynamicColors().textHintInverse());
  }
}
