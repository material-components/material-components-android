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
import java.util.HashMap;
import java.util.Map;

// TODO(b/254603377): Use copybara to release material color utilities library directly to github.
/**
 * A convenience class for retrieving colors that are constant in hue and chroma, but vary in tone.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class TonalPalette {
  Map<Integer, Integer> cache;
  Hct keyColor;
  double hue;
  double chroma;

  /**
   * Create tones using the HCT hue and chroma from a color.
   *
   * @param argb ARGB representation of a color
   * @return Tones matching that color's hue and chroma.
   */
  public static TonalPalette fromInt(int argb) {
    return fromHct(Hct.fromInt(argb));
  }

  /**
   * Create tones using a HCT color.
   *
   * @param hct HCT representation of a color.
   * @return Tones matching that color's hue and chroma.
   */
  public static TonalPalette fromHct(Hct hct) {
    return new TonalPalette(hct.getHue(), hct.getChroma(), hct);
  }

  /**
   * Create tones from a defined HCT hue and chroma.
   *
   * @param hue HCT hue
   * @param chroma HCT chroma
   * @return Tones matching hue and chroma.
   */
  public static TonalPalette fromHueAndChroma(double hue, double chroma) {
    return new TonalPalette(hue, chroma, createKeyColor(hue, chroma));
  }

  private TonalPalette(double hue, double chroma, Hct keyColor) {
    cache = new HashMap<>();
    this.hue = hue;
    this.chroma = chroma;
    this.keyColor = keyColor;
  }

  /** The key color is the first tone, starting from T50, matching the given hue and chroma. */
  private static Hct createKeyColor(double hue, double chroma) {
    double startTone = 50.0;
    Hct smallestDeltaHct = Hct.from(hue, chroma, startTone);
    double smallestDelta = Math.abs(smallestDeltaHct.getChroma() - chroma);
    // Starting from T50, check T+/-delta to see if they match the requested
    // chroma.
    //
    // Starts from T50 because T50 has the most chroma available, on
    // average. Thus it is most likely to have a direct answer and minimize
    // iteration.
    for (double delta = 1.0; delta < 50.0; delta += 1.0) {
      // Termination condition rounding instead of minimizing delta to avoid
      // case where requested chroma is 16.51, and the closest chroma is 16.49.
      // Error is minimized, but when rounded and displayed, requested chroma
      // is 17, key color's chroma is 16.
      if (Math.round(chroma) == Math.round(smallestDeltaHct.getChroma())) {
        return smallestDeltaHct;
      }

      final Hct hctAdd = Hct.from(hue, chroma, startTone + delta);
      final double hctAddDelta = Math.abs(hctAdd.getChroma() - chroma);
      if (hctAddDelta < smallestDelta) {
        smallestDelta = hctAddDelta;
        smallestDeltaHct = hctAdd;
      }

      final Hct hctSubtract = Hct.from(hue, chroma, startTone - delta);
      final double hctSubtractDelta = Math.abs(hctSubtract.getChroma() - chroma);
      if (hctSubtractDelta < smallestDelta) {
        smallestDelta = hctSubtractDelta;
        smallestDeltaHct = hctSubtract;
      }
    }

    return smallestDeltaHct;
  }

  /**
   * Create an ARGB color with HCT hue and chroma of this Tones instance, and the provided HCT tone.
   *
   * @param tone HCT tone, measured from 0 to 100.
   * @return ARGB representation of a color with that tone.
   */
  // AndroidJdkLibsChecker is higher priority than ComputeIfAbsentUseValue (b/119581923)
  @SuppressWarnings("ComputeIfAbsentUseValue")
  public int tone(int tone) {
    Integer color = cache.get(tone);
    if (color == null) {
      color = Hct.from(this.hue, this.chroma, tone).toInt();
      cache.put(tone, color);
    }
    return color;
  }

  /** Given a tone, use hue and chroma of palette to create a color, and return it as HCT. */
  public Hct getHct(double tone) {
    return Hct.from(this.hue, this.chroma, tone);
  }

  /** The chroma of the Tonal Palette, in HCT. Ranges from 0 to ~130 (for sRGB gamut). */
  public double getChroma() {
    return this.chroma;
  }

  /** The hue of the Tonal Palette, in HCT. Ranges from 0 to 360. */
  public double getHue() {
    return this.hue;
  }

  /** The key color is the first tone, starting from T50, that matches the palette's chroma. */
  public Hct getKeyColor() {
    return this.keyColor;
  }
}
