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
 * In traditional color spaces, a color can be identified solely by the observer's measurement of
 * the color. Color appearance models such as CAM16 also use information about the environment where
 * the color was observed, known as the viewing conditions.
 *
 * <p>For example, white under the traditional assumption of a midday sun white point is accurately
 * measured as a slightly chromatic blue by CAM16. (roughly, hue 203, chroma 3, lightness 100)
 *
 * <p>This class caches intermediate values of the CAM16 conversion process that depend only on
 * viewing conditions, enabling speed ups.
 */
final class ViewingConditions {
  /** sRGB-like viewing conditions. */
  public static final ViewingConditions DEFAULT =
      ViewingConditions.make(
          ColorUtils.whitePointD65(),
          (float) (200.0f / Math.PI * ColorUtils.yFromLstar(50.0f) / 100.f),
          50.0f,
          2.0f,
          false);

  private final float aw;
  private final float nbb;
  private final float ncb;
  private final float c;
  private final float nc;
  private final float n;
  private final float[] rgbD;
  private final float fl;
  private final float flRoot;
  private final float z;

  public float getAw() {
    return aw;
  }

  public float getN() {
    return n;
  }

  public float getNbb() {
    return nbb;
  }

  float getNcb() {
    return ncb;
  }

  float getC() {
    return c;
  }

  float getNc() {
    return nc;
  }

  public float[] getRgbD() {
    return rgbD;
  }

  float getFl() {
    return fl;
  }

  public float getFlRoot() {
    return flRoot;
  }

  float getZ() {
    return z;
  }

  /**
   * Create ViewingConditions from a simple, physically relevant, set of parameters.
   *
   * @param whitePoint White point, measured in the XYZ color space. default = D65, or sunny day
   *     afternoon
   * @param adaptingLuminance The luminance of the adapting field. Informally, how bright it is in
   *     the room where the color is viewed. Can be calculated from lux by multiplying lux by
   *     0.0586. default = 11.72, or 200 lux.
   * @param backgroundLstar The lightness of the area surrounding the color. measured by L* in
   *     L*a*b*. default = 50.0
   * @param surround A general description of the lighting surrounding the color. 0 is pitch dark,
   *     like watching a movie in a theater. 1.0 is a dimly light room, like watching TV at home at
   *     night. 2.0 means there is no difference between the lighting on the color and around it.
   *     default = 2.0
   * @param discountingIlluminant Whether the eye accounts for the tint of the ambient lighting,
   *     such as knowing an apple is still red in green light. default = false, the eye does not
   *     perform this process on self-luminous objects like displays.
   */
  static ViewingConditions make(
      float[] whitePoint,
      float adaptingLuminance,
      float backgroundLstar,
      float surround,
      boolean discountingIlluminant) {
    // Transform white point XYZ to 'cone'/'rgb' responses
    float[][] matrix = Cam16.XYZ_TO_CAM16RGB;
    float[] xyz = whitePoint;
    float rW = (xyz[0] * matrix[0][0]) + (xyz[1] * matrix[0][1]) + (xyz[2] * matrix[0][2]);
    float gW = (xyz[0] * matrix[1][0]) + (xyz[1] * matrix[1][1]) + (xyz[2] * matrix[1][2]);
    float bW = (xyz[0] * matrix[2][0]) + (xyz[1] * matrix[2][1]) + (xyz[2] * matrix[2][2]);
    float f = 0.8f + (surround / 10.0f);
    float c =
        (f >= 0.9)
            ? MathUtils.lerp(0.59f, 0.69f, ((f - 0.9f) * 10.0f))
            : MathUtils.lerp(0.525f, 0.59f, ((f - 0.8f) * 10.0f));
    float d =
        discountingIlluminant
            ? 1.0f
            : f * (1.0f - ((1.0f / 3.6f) * (float) Math.exp((-adaptingLuminance - 42.0f) / 92.0f)));
    d = (d > 1.0) ? 1.0f : (d < 0.0) ? 0.0f : d;
    float nc = f;
    float[] rgbD =
        new float[] {
          d * (100.0f / rW) + 1.0f - d, d * (100.0f / gW) + 1.0f - d, d * (100.0f / bW) + 1.0f - d
        };
    float k = 1.0f / (5.0f * adaptingLuminance + 1.0f);
    float k4 = k * k * k * k;
    float k4F = 1.0f - k4;
    float fl =
        (k4 * adaptingLuminance) + (0.1f * k4F * k4F * (float) Math.cbrt(5.0 * adaptingLuminance));
    float n = ColorUtils.yFromLstar(backgroundLstar) / whitePoint[1];
    float z = 1.48f + (float) Math.sqrt(n);
    float nbb = 0.725f / (float) Math.pow(n, 0.2);
    float ncb = nbb;
    float[] rgbAFactors =
        new float[] {
          (float) Math.pow(fl * rgbD[0] * rW / 100.0, 0.42),
          (float) Math.pow(fl * rgbD[1] * gW / 100.0, 0.42),
          (float) Math.pow(fl * rgbD[2] * bW / 100.0, 0.42)
        };

    float[] rgbA =
        new float[] {
          (400.0f * rgbAFactors[0]) / (rgbAFactors[0] + 27.13f),
          (400.0f * rgbAFactors[1]) / (rgbAFactors[1] + 27.13f),
          (400.0f * rgbAFactors[2]) / (rgbAFactors[2] + 27.13f)
        };

    float aw = ((2.0f * rgbA[0]) + rgbA[1] + (0.05f * rgbA[2])) * nbb;
    return new ViewingConditions(n, aw, nbb, ncb, c, nc, rgbD, fl, (float) Math.pow(fl, 0.25), z);
  }

  /**
   * Parameters are intermediate values of the CAM16 conversion process. Their names are shorthand
   * for technical color science terminology, this class would not benefit from documenting them
   * individually. A brief overview is available in the CAM16 specification, and a complete overview
   * requires a color science textbook, such as Fairchild's Color Appearance Models.
   */
  private ViewingConditions(
      float n,
      float aw,
      float nbb,
      float ncb,
      float c,
      float nc,
      float[] rgbD,
      float fl,
      float flRoot,
      float z) {
    this.n = n;
    this.aw = aw;
    this.nbb = nbb;
    this.ncb = ncb;
    this.c = c;
    this.nc = nc;
    this.rgbD = rgbD;
    this.fl = fl;
    this.flRoot = flRoot;
    this.z = z;
  }
}
