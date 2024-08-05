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
    final Hct keyColor = new KeyColor(hue, chroma).create();
    return new TonalPalette(hue, chroma, keyColor);
  }

  private TonalPalette(double hue, double chroma, Hct keyColor) {
    cache = new HashMap<>();
    this.hue = hue;
    this.chroma = chroma;
    this.keyColor = keyColor;
  }

  /**
   * Create an ARGB color with HCT hue and chroma of this Tones instance, and the provided HCT tone.
   *
   * @param tone HCT tone, measured from 0 to 100.
   * @return ARGB representation of a color with that tone.
   */
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

  /** Key color is a color that represents the hue and chroma of a tonal palette. */
  private static final class KeyColor {
    private final double hue;
    private final double requestedChroma;

    // Cache that maps tone to max chroma to avoid duplicated HCT calculation.
    private final Map<Integer, Double> chromaCache = new HashMap<>();
    private static final double MAX_CHROMA_VALUE = 200.0;

    /** Key color is a color that represents the hue and chroma of a tonal palette */
    public KeyColor(double hue, double requestedChroma) {
      this.hue = hue;
      this.requestedChroma = requestedChroma;
    }

    /**
     * Creates a key color from a [hue] and a [chroma]. The key color is the first tone, starting
     * from T50, matching the given hue and chroma.
     *
     * @return Key color [Hct]
     */
    public Hct create() {
      // Pivot around T50 because T50 has the most chroma available, on
      // average. Thus it is most likely to have a direct answer.
      final int pivotTone = 50;
      final int toneStepSize = 1;
      // Epsilon to accept values slightly higher than the requested chroma.
      final double epsilon = 0.01;

      // Binary search to find the tone that can provide a chroma that is closest
      // to the requested chroma.
      int lowerTone = 0;
      int upperTone = 100;
      while (lowerTone < upperTone) {
        final int midTone = (lowerTone + upperTone) / 2;
        boolean isAscending = maxChroma(midTone) < maxChroma(midTone + toneStepSize);
        boolean sufficientChroma = maxChroma(midTone) >= requestedChroma - epsilon;

        if (sufficientChroma) {
          // Either range [lowerTone, midTone] or [midTone, upperTone] has
          // the answer, so search in the range that is closer the pivot tone.
          if (Math.abs(lowerTone - pivotTone) < Math.abs(upperTone - pivotTone)) {
            upperTone = midTone;
          } else {
            if (lowerTone == midTone) {
              return Hct.from(this.hue, this.requestedChroma, lowerTone);
            }
            lowerTone = midTone;
          }
        } else {
          // As there is no sufficient chroma in the midTone, follow the direction to the chroma
          // peak.
          if (isAscending) {
            lowerTone = midTone + toneStepSize;
          } else {
            // Keep midTone for potential chroma peak.
            upperTone = midTone;
          }
        }
      }

      return Hct.from(this.hue, this.requestedChroma, lowerTone);
    }

    // Find the maximum chroma for a given tone
    private double maxChroma(int tone) {
      if (chromaCache.get(tone) == null) {
        Double newChroma = Hct.from(hue, MAX_CHROMA_VALUE, tone).getChroma();
        if (newChroma != null) {
          chromaCache.put(tone, newChroma);
        }
      }
      return chromaCache.get(tone);
    }
  }
}
