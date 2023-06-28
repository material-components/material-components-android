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
 * A loud theme, colorfulness is maximum for Primary palette, increased for others.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SchemeVibrant extends DynamicScheme {
  private static final double[] HUES = {0, 41, 61, 101, 131, 181, 251, 301, 360};
  private static final double[] SECONDARY_ROTATIONS = {18, 15, 10, 12, 15, 18, 15, 12, 12};
  private static final double[] TERTIARY_ROTATIONS = {35, 30, 20, 25, 30, 35, 30, 25, 25};

  public SchemeVibrant(Hct sourceColorHct, boolean isDark, double contrastLevel) {
    super(
        sourceColorHct,
        Variant.VIBRANT,
        isDark,
        contrastLevel,
        TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 200.0),
        TonalPalette.fromHueAndChroma(
            DynamicScheme.getRotatedHue(sourceColorHct, HUES, SECONDARY_ROTATIONS), 24.0),
        TonalPalette.fromHueAndChroma(
            DynamicScheme.getRotatedHue(sourceColorHct, HUES, TERTIARY_ROTATIONS), 32.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 10.0),
        TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), 12.0));
  }
}
