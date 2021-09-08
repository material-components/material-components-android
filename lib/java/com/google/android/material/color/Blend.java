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

/** Functions for blending in HCT and CAM16. */
final class Blend {
  private static final float HARMONIZE_BLEND_AMOUNT = 0.25f;

  private Blend() {}

  /**
   * Blend the design color's HCT hue towards the key color's HCT hue, in a way that leaves the
   * original color recognizable and recognizably shifted towards the key color.
   *
   * @param designColor ARGB representation of an arbitrary color.
   * @param keyColor ARGB representation of the main theme color.
   * @return The design color with a hue shifted towards the system's color, a slightly
   *     warmer/cooler variant of the design color's hue.
   */
  public static int harmonize(int designColor, int keyColor) {
    return blendHctHue(designColor, keyColor, HARMONIZE_BLEND_AMOUNT);
  }

  /**
   * Blends hue from one color into another. The chroma and tone of the original color are
   * maintained.
   *
   * @param from ARGB representation of color
   * @param to ARGB representation of color
   * @param amount how much blending to perform; 0.0 >= and <= 1.0
   * @return from, with a hue blended towards to. Chroma and tone are constant.
   */
  public static int blendHctHue(int from, int to, float amount) {
    int ucs = blendCam16Ucs(from, to, amount);
    Cam16 ucsCam = Cam16.fromInt(ucs);
    Cam16 fromCam = Cam16.fromInt(from);
    return Hct.from(ucsCam.getHue(), fromCam.getChroma(), ColorUtils.lstarFromInt(from)).toInt();
  }

  /**
   * Blend in CAM16-UCS space.
   *
   * @param from ARGB representation of color
   * @param to ARGB representation of color
   * @param amount how much blending to perform; 0.0 >= and <= 1.0
   * @return from, blended towards to. Hue, chroma, and tone will change.
   */
  public static int blendCam16Ucs(int from, int to, float amount) {
    Cam16 fromCam = Cam16.fromInt(from);
    Cam16 toCam = Cam16.fromInt(to);

    float aJ = fromCam.getJStar();
    float aA = fromCam.getAStar();
    float aB = fromCam.getBStar();

    float bJ = toCam.getJStar();
    float bA = toCam.getAStar();
    float bB = toCam.getBStar();

    float j = aJ + (bJ - aJ) * amount;
    float a = aA + (bA - aA) * amount;
    float b = aB + (bB - aB) * amount;

    Cam16 blended = Cam16.fromUcs(j, a, b);
    return blended.getInt();
  }
}
