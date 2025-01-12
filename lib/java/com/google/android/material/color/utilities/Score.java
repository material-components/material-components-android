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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

// TODO(b/254603377): Use copybara to release material color utilities library directly to github.
/**
 * Given a large set of colors, remove colors that are unsuitable for a UI theme, and rank the rest
 * based on suitability.
 *
 * <p>Enables use of a high cluster count for image quantization, thus ensuring colors aren't
 * muddied, while curating the high cluster count to a much smaller number of appropriate choices.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class Score {
  private static final double TARGET_CHROMA = 48.; // A1 Chroma
  private static final double WEIGHT_PROPORTION = 0.7;
  private static final double WEIGHT_CHROMA_ABOVE = 0.3;
  private static final double WEIGHT_CHROMA_BELOW = 0.1;
  private static final double CUTOFF_CHROMA = 5.;
  private static final double CUTOFF_EXCITED_PROPORTION = 0.01;
  private static final int BLUE_500 = 0xff4285f4;
  private static final int MAX_COLOR_COUNT = 4;

  private Score() {}

  public static List<Integer> score(Map<Integer, Integer> colorsToPopulation) {
    // Fallback color is Google Blue.
    return score(colorsToPopulation, MAX_COLOR_COUNT, BLUE_500, true);
  }

  public static List<Integer> score(Map<Integer, Integer> colorsToPopulation, int maxColorCount) {
    return score(colorsToPopulation, maxColorCount, BLUE_500, true);
  }

  public static List<Integer> score(
      Map<Integer, Integer> colorsToPopulation, int maxColorCount, int fallbackColorArgb) {
    return score(colorsToPopulation, maxColorCount, fallbackColorArgb, true);
  }

  /**
   * Given a map with keys of colors and values of how often the color appears, rank the colors
   * based on suitability for being used for a UI theme.
   *
   * @param colorsToPopulation map with keys of colors and values of how often the color appears,
   *     usually from a source image.
   * @param maxColorCount max count of colors to be returned in the list.
   * @param fallbackColorArgb color to be returned if no other options available.
   * @param filter whether to filter out undesirable combinations.
   * @return Colors sorted by suitability for a UI theme. The most suitable color is the first item,
   *     the least suitable is the last. There will always be at least one color returned. If all
   *     the input colors were not suitable for a theme, a default fallback color will be provided,
   *     Google Blue.
   */
  public static List<Integer> score(
      Map<Integer, Integer> colorsToPopulation,
      int maxColorCount,
      int fallbackColorArgb,
      boolean filter) {

    // Get the HCT color for each Argb value, while finding the per hue count and
    // total count.
    List<Hct> colorsHct = new ArrayList<>();
    int[] huePopulation = new int[360];
    double populationSum = 0.;
    for (Map.Entry<Integer, Integer> entry : colorsToPopulation.entrySet()) {
      Hct hct = Hct.fromInt(entry.getKey());
      colorsHct.add(hct);
      int hue = (int) Math.floor(hct.getHue());
      int population = entry.getValue();
      huePopulation[hue] += population;
      populationSum += population;
    }

    // Hues with more usage in neighboring 30 degree slice get a larger number.
    double[] hueExcitedProportions = new double[360];
    for (int hue = 0; hue < 360; hue++) {
      double proportion = huePopulation[hue] / populationSum;
      for (int i = hue - 14; i < hue + 16; i++) {
        int neighborHue = MathUtils.sanitizeDegreesInt(i);
        hueExcitedProportions[neighborHue] += proportion;
      }
    }

    // Scores each HCT color based on usage and chroma, while optionally
    // filtering out values that do not have enough chroma or usage.
    List<ScoredHCT> scoredHcts = new ArrayList<>();
    for (Hct hct : colorsHct) {
      int hue = MathUtils.sanitizeDegreesInt((int) Math.round(hct.getHue()));
      double proportion = hueExcitedProportions[hue];
      if (filter && (hct.getChroma() < CUTOFF_CHROMA || proportion <= CUTOFF_EXCITED_PROPORTION)) {
        continue;
      }

      double proportionScore = proportion * 100.0 * WEIGHT_PROPORTION;
      double chromaWeight =
          hct.getChroma() < TARGET_CHROMA ? WEIGHT_CHROMA_BELOW : WEIGHT_CHROMA_ABOVE;
      double chromaScore = (hct.getChroma() - TARGET_CHROMA) * chromaWeight;
      double score = proportionScore + chromaScore;
      scoredHcts.add(new ScoredHCT(hct, score));
    }
    // Sorted so that colors with higher scores come first.
    Collections.sort(scoredHcts, new ScoredComparator());

    // Iterates through potential hue differences in degrees in order to select
    // the colors with the largest distribution of hues possible. Starting at
    // 90 degrees(maximum difference for 4 colors) then decreasing down to a
    // 15 degree minimum.
    List<Hct> chosenColors = new ArrayList<>();
    for (int differenceDegrees = 90; differenceDegrees >= 15; differenceDegrees--) {
      chosenColors.clear();
      for (ScoredHCT entry : scoredHcts) {
        Hct hct = entry.hct;
        boolean hasDuplicateHue = false;
        for (Hct chosenHct : chosenColors) {
          if (MathUtils.differenceDegrees(hct.getHue(), chosenHct.getHue()) < differenceDegrees) {
            hasDuplicateHue = true;
            break;
          }
        }
        if (!hasDuplicateHue) {
          chosenColors.add(hct);
        }
        if (chosenColors.size() >= maxColorCount) {
          break;
        }
      }
      if (chosenColors.size() >= maxColorCount) {
        break;
      }
    }
    List<Integer> colors = new ArrayList<>();
    if (chosenColors.isEmpty()) {
      colors.add(fallbackColorArgb);
      return colors;
    }
    for (Hct chosenHct : chosenColors) {
      colors.add(chosenHct.toInt());
    }
    return colors;
  }

  private static class ScoredHCT {
    public final Hct hct;
    public final double score;

    public ScoredHCT(Hct hct, double score) {
      this.hct = hct;
      this.score = score;
    }
  }

  private static class ScoredComparator implements Comparator<ScoredHCT> {
    public ScoredComparator() {}

    @Override
    public int compare(ScoredHCT entry1, ScoredHCT entry2) {
      return Double.compare(entry2.score, entry1.score);
    }
  }
}
