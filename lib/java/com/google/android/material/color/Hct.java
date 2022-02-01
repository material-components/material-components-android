/*
 * Copyright (C) 2021 The Android Open Source Project
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

/**
 * A color system built using CAM16 hue and chroma, and L* from L*a*b*.
 *
 * <p>Using L* creates a link between the color system, contrast, and thus accessibility. Contrast
 * ratio depends on relative luminance, or Y in the XYZ color space. L*, or perceptual luminance can
 * be calculated from Y.
 *
 * <p>Unlike Y, L* is linear to human perception, allowing trivial creation of accurate color tones.
 *
 * <p>Unlike contrast ratio, measuring contrast in L* is linear, and simple to calculate. A
 * difference of 40 in HCT tone guarantees a contrast ratio >= 3.0, and a difference of 50
 * guarantees a contrast ratio >= 4.5.
 */

/**
 * HCT, hue, chroma, and tone. A color system that provides a perceptually accurate color
 * measurement system that can also accurately render what colors will appear as in different
 * lighting environments.
 */
final class Hct {
  private float hue;
  private float chroma;
  private float tone;

  /**
   * Create an HCT color from hue, chroma, and tone.
   *
   * @param hue 0 <= hue < 360; invalid values are corrected.
   * @param chroma 0 <= chroma < ?; Informally, colorfulness. The color returned may be lower than
   *     the requested chroma. Chroma has a different maximum for any given hue and tone.
   * @param tone 0 <= tone <= 100; invalid values are corrected.
   * @return HCT representation of a color in default viewing conditions.
   */
  public static Hct from(float hue, float chroma, float tone) {
    return new Hct(hue, chroma, tone);
  }

  /**
   * Create an HCT color from a color.
   *
   * @param argb ARGB representation of a color.
   * @return HCT representation of a color in default viewing conditions
   */
  public static Hct fromInt(int argb) {
    Cam16 cam = Cam16.fromInt(argb);
    return new Hct(cam.getHue(), cam.getChroma(), ColorUtils.lstarFromInt(argb));
  }

  private Hct(float hue, float chroma, float tone) {
    setInternalState(gamutMap(hue, chroma, tone));
  }

  public float getHue() {
    return hue;
  }

  public float getChroma() {
    return chroma;
  }

  public float getTone() {
    return tone;
  }

  public int toInt() {
    return gamutMap(hue, chroma, tone);
  }

  /**
   * Set the hue of this color. Chroma may decrease because chroma has a different maximum for any
   * given hue and tone.
   *
   * @param newHue 0 <= newHue < 360; invalid values are corrected.
   */
  public void setHue(float newHue) {
    setInternalState(gamutMap(MathUtils.sanitizeDegrees(newHue), chroma, tone));
  }

  /**
   * Set the chroma of this color. Chroma may decrease because chroma has a different maximum for
   * any given hue and tone.
   *
   * @param newChroma 0 <= newChroma < ?
   */
  public void setChroma(float newChroma) {
    setInternalState(gamutMap(hue, newChroma, tone));
  }

  /**
   * Set the tone of this color. Chroma may decrease because chroma has a different maximum for any
   * given hue and tone.
   *
   * @param newTone 0 <= newTone <= 100; invalid valids are corrected.
   */
  public void setTone(float newTone) {
    setInternalState(gamutMap(hue, chroma, newTone));
  }

  private void setInternalState(int argb) {
    Cam16 cam = Cam16.fromInt(argb);
    float tone = ColorUtils.lstarFromInt(argb);
    hue = cam.getHue();
    chroma = cam.getChroma();
    this.tone = tone;
  }

  /**
   * When the delta between the floor & ceiling of a binary search for maximum chroma at a hue and
   * tone is less than this, the binary search terminates.
   */
  private static final float CHROMA_SEARCH_ENDPOINT = 0.4f;

  /** The maximum color distance, in CAM16-UCS, between a requested color and the color returned. */
  private static final float DE_MAX = 1.0f;

  /** The maximum difference between the requested L* and the L* returned. */
  private static final float DL_MAX = 0.2f;

  /**
   * The minimum color distance, in CAM16-UCS, between a requested color and an 'exact' match. This
   * allows the binary search during gamut mapping to terminate much earlier when the error is
   * infinitesimal.
   */
  private static final float DE_MAX_ERROR = 0.000000001f;

  /**
   * When the delta between the floor & ceiling of a binary search for J, lightness in CAM16, is
   * less than this, the binary search terminates.
   */
  private static final float LIGHTNESS_SEARCH_ENDPOINT = 0.01f;

  /**
   * @param hue a number, in degrees, representing ex. red, orange, yellow, etc. Ranges from 0 <=
   *     hue < 360.
   * @param chroma Informally, colorfulness. Ranges from 0 to roughly 150. Like all perceptually
   *     accurate color systems, chroma has a different maximum for any given hue and tone, so the
   *     color returned may be lower than the requested chroma.
   * @param tone Lightness. Ranges from 0 to 100.
   * @return ARGB representation of a color in default viewing conditions
   */
  private static int gamutMap(float hue, float chroma, float tone) {
    return gamutMapInViewingConditions(hue, chroma, tone, ViewingConditions.DEFAULT);
  }

  /**
   * @param hue CAM16 hue.
   * @param chroma CAM16 chroma.
   * @param tone L*a*b* lightness.
   * @param viewingConditions Information about the environment where the color was observed.
   */
  static int gamutMapInViewingConditions(
      float hue, float chroma, float tone, ViewingConditions viewingConditions) {

    if (chroma < 1.0 || Math.round(tone) <= 0.0 || Math.round(tone) >= 100.0) {
      return ColorUtils.intFromLstar(tone);
    }

    hue = MathUtils.sanitizeDegrees(hue);

    float high = chroma;
    float mid = chroma;
    float low = 0.0f;
    boolean isFirstLoop = true;

    Cam16 answer = null;
    while (Math.abs(low - high) >= CHROMA_SEARCH_ENDPOINT) {
      Cam16 possibleAnswer = findCamByJ(hue, mid, tone);

      if (isFirstLoop) {
        if (possibleAnswer != null) {
          return possibleAnswer.viewed(viewingConditions);
        } else {
          isFirstLoop = false;
          mid = low + (high - low) / 2.0f;
          continue;
        }
      }

      if (possibleAnswer == null) {
        high = mid;
      } else {
        answer = possibleAnswer;
        low = mid;
      }

      mid = low + (high - low) / 2.0f;
    }

    if (answer == null) {
      return ColorUtils.intFromLstar(tone);
    }

    return answer.viewed(viewingConditions);
  }

  /**
   * @param hue CAM16 hue
   * @param chroma CAM16 chroma
   * @param tone L*a*b* lightness
   * @return CAM16 instance within error tolerance of the provided dimensions, or null.
   */
  private static Cam16 findCamByJ(float hue, float chroma, float tone) {
    float low = 0.0f;
    float high = 100.0f;
    float mid = 0.0f;
    float bestdL = 1000.0f;
    float bestdE = 1000.0f;

    Cam16 bestCam = null;
    while (Math.abs(low - high) > LIGHTNESS_SEARCH_ENDPOINT) {
      mid = low + (high - low) / 2;
      Cam16 camBeforeClip = Cam16.fromJch(mid, chroma, hue);
      int clipped = camBeforeClip.getInt();
      float clippedLstar = ColorUtils.lstarFromInt(clipped);
      float dL = Math.abs(tone - clippedLstar);

      if (dL < DL_MAX) {
        Cam16 camClipped = Cam16.fromInt(clipped);
        float dE =
            camClipped.distance(Cam16.fromJch(camClipped.getJ(), camClipped.getChroma(), hue));
        if (dE <= DE_MAX && dE <= bestdE) {
          bestdL = dL;
          bestdE = dE;
          bestCam = camClipped;
        }
      }

      if (bestdL == 0 && bestdE < DE_MAX_ERROR) {
        break;
      }

      if (clippedLstar < tone) {
        low = mid;
      } else {
        high = mid;
      }
    }

    return bestCam;
  }
}
