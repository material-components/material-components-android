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
import static java.lang.Math.max;

import androidx.annotation.RestrictTo;

/**
 * A scheme that places the source color in Scheme.primaryContainer.
 *
 * <p>Primary Container is the source color, adjusted for color relativity. It maintains constant
 * appearance in light mode and dark mode. This adds ~5 tone in light mode, and subtracts ~5 tone in
 * dark mode.
 *
 * <p>Tertiary Container is an analogous color, specifically, the analog of a color wheel divided
 * into 6, and the precise analog is the one found by increasing hue. This is a scientifically
 * grounded equivalent to rotating hue clockwise by 60 degrees. It also maintains constant
 * appearance.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class SchemeContent extends DynamicScheme {
  public SchemeContent(Hct sourceColorHct, boolean isDark, double contrastLevel) {
    super(
        sourceColorHct,
        Variant.CONTENT,
        isDark,
        contrastLevel,
        TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), sourceColorHct.getChroma()),
        TonalPalette.fromHueAndChroma(
            sourceColorHct.getHue(),
            max(sourceColorHct.getChroma() - 32.0, sourceColorHct.getChroma() * 0.5)),
        TonalPalette.fromHct(
            DislikeAnalyzer.fixIfDisliked(
                new TemperatureCache(sourceColorHct)
                    .getAnalogousColors(/* count= */ 3, /* divisions= */ 6)
                    .get(2))),
        TonalPalette.fromHueAndChroma(sourceColorHct.getHue(), sourceColorHct.getChroma() / 8.0),
        TonalPalette.fromHueAndChroma(
            sourceColorHct.getHue(), (sourceColorHct.getChroma() / 8.0) + 4.0));
  }
}
