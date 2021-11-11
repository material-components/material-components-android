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

import java.util.Arrays;

/**
 * Utility methods for color science constants and color space conversions that aren't HCT or CAM16.
 */
final class ColorUtils {
  private ColorUtils() {}

  private static final float[] WHITE_POINT_D65 = {95.047f, 100.0f, 108.883f};

  /** Standard white point; white on a sunny day. */
  public static final float[] whitePointD65() {
    return Arrays.copyOf(WHITE_POINT_D65, 3);
  }

  /**
   * The red channel of the color, from 0 to 255.
   *
   * @param argb ARGB representation of a color.
   */
  public static int redFromInt(int argb) {
    return (argb & 0x00ff0000) >> 16;
  }

  /**
   * The green channel of the color, from 0 to 255.
   *
   * @param argb ARGB representation of a color.
   */
  public static int greenFromInt(int argb) {
    return (argb & 0x0000ff00) >> 8;
  }

  /**
   * The blue channel of the color, from 0 to 255.
   *
   * @param argb ARGB representation of a color.
   */
  public static int blueFromInt(int argb) {
    return (argb & 0x000000ff);
  }

  /**
   * L*, from L*a*b*, coordinate of the color.
   *
   * @param argb ARGB representation of a color.
   */
  public static float lstarFromInt(int argb) {
    return (float) labFromInt(argb)[0];
  }

  /**
   * Hex string representing color, ex. #ff0000 for red.
   *
   * @param argb ARGB representation of a color.
   */
  public static String hexFromInt(int argb) {
    int red = redFromInt(argb);
    int blue = blueFromInt(argb);
    int green = greenFromInt(argb);
    return String.format("#%02x%02x%02x", red, green, blue);
  }

  /**
   * Color's coordinates in the XYZ color space.
   *
   * @param argb ARGB representation of a color.
   */
  // The RGB => XYZ conversion matrix elements are derived scientific constants. While the values
  // may differ at runtime due to floating point imprecision, keeping the values the same, and
  // accurate, across implementations takes precedence.
  @SuppressWarnings("FloatingPointLiteralPrecision")
  public static float[] xyzFromInt(int argb) {
    final float r = linearized(redFromInt(argb) / 255f) * 100f;
    final float g = linearized(greenFromInt(argb) / 255f) * 100f;
    final float b = linearized(blueFromInt(argb) / 255f) * 100f;
    final float x = 0.41233894f * r + 0.35762064f * g + 0.18051042f * b;
    final float y = 0.2126f * r + 0.7152f * g + 0.0722f * b;
    final float z = 0.01932141f * r + 0.11916382f * g + 0.95034478f * b;
    return new float[] {x, y, z};
  }

  /**
   * Create an ARGB color from R, G, and B coordinates. Coordinates are expected to be >= 0 and <=
   * 255.
   */
  public static int intFromRgb(int r, int g, int b) {
    return (((255 << 24) | ((r & 0x0ff) << 16) | ((g & 0x0ff) << 8) | (b & 0x0ff)) >>> 0);
  }

  /**
   * Color's coordinates in the L*a*b* color space.
   *
   * @param argb ARGB representation of a color.
   */
  public static double[] labFromInt(int argb) {
    final double e = 216.0 / 24389.0;
    final double kappa = 24389.0 / 27.0;

    final float[] xyz = xyzFromInt(argb);
    final double yNormalized = xyz[1] / WHITE_POINT_D65[1];
    double fy;
    if (yNormalized > e) {
      fy = Math.cbrt(yNormalized);
    } else {
      fy = (kappa * yNormalized + 16) / 116;
    }

    final double xNormalized = xyz[0] / WHITE_POINT_D65[0];
    double fx;
    if (xNormalized > e) {
      fx = Math.cbrt(xNormalized);
    } else {
      fx = (kappa * xNormalized + 16) / 116;
    }

    final double zNormalized = xyz[2] / WHITE_POINT_D65[2];
    double fz;
    if (zNormalized > e) {
      fz = Math.cbrt(zNormalized);
    } else {
      fz = (kappa * zNormalized + 16) / 116;
    }

    final double l = 116.0 * fy - 16;
    final double a = 500.0 * (fx - fy);
    final double b = 200.0 * (fy - fz);
    return new double[] {l, a, b};
  }

  /** ARGB representation of color in the L*a*b* color space. */
  public static int intFromLab(double l, double a, double b) {
    final double e = 216.0 / 24389.0;
    final double kappa = 24389.0 / 27.0;
    final double ke = 8.0;

    final double fy = (l + 16.0) / 116.0;
    final double fx = (a / 500.0) + fy;
    final double fz = fy - (b / 200.0);
    final double fx3 = fx * fx * fx;
    final double xNormalized = (fx3 > e) ? fx3 : (116.0 * fx - 16.0) / kappa;
    final double yNormalized = (l > ke) ? fy * fy * fy : (l / kappa);
    final double fz3 = fz * fz * fz;
    final double zNormalized = (fz3 > e) ? fz3 : (116.0 * fz - 16.0) / kappa;
    final double x = xNormalized * WHITE_POINT_D65[0];
    final double y = yNormalized * WHITE_POINT_D65[1];
    final double z = zNormalized * WHITE_POINT_D65[2];
    return intFromXyzComponents((float) x, (float) y, (float) z);
  }

  /** ARGB representation of color in the XYZ color space. */
  public static int intFromXyzComponents(float x, float y, float z) {
    x = x / 100f;
    y = y / 100f;
    z = z / 100f;

    float rL = x * 3.2406f + y * -1.5372f + z * -0.4986f;
    float gL = x * -0.9689f + y * 1.8758f + z * 0.0415f;
    float bL = x * 0.0557f + y * -0.204f + z * 1.057f;

    float r = ColorUtils.delinearized(rL);
    float g = ColorUtils.delinearized(gL);
    float b = ColorUtils.delinearized(bL);

    int rInt = (Math.max(Math.min(255, Math.round(r * 255)), 0));
    int gInt = (Math.max(Math.min(255, Math.round(g * 255)), 0));
    int bInt = (Math.max(Math.min(255, Math.round(b * 255)), 0));
    return intFromRgb(rInt, gInt, bInt);
  }

  /** ARGB representation of color in the XYZ color space. */
  public static int intFromXyz(float[] xyz) {
    return intFromXyzComponents(xyz[0], xyz[1], xyz[2]);
  }

  /**
   * ARGB representation of grayscale (0 chroma) color with lightness matching L*
   *
   * @param lstar L* in L*a*b*
   */
  public static int intFromLstar(float lstar) {
    float fy = (lstar + 16.0f) / 116.0f;
    float fz = fy;
    float fx = fy;

    float kappa = 24389f / 27f;
    float epsilon = 216f / 24389f;
    boolean cubeExceedEpsilon = (fy * fy * fy) > epsilon;
    boolean lExceedsEpsilonKappa = (lstar > 8.0f);
    float y = lExceedsEpsilonKappa ? fy * fy * fy : lstar / kappa;
    float x = cubeExceedEpsilon ? fx * fx * fx : (116f * fx - 16f) / kappa;
    float z = cubeExceedEpsilon ? fz * fz * fz : (116f * fx - 16f) / kappa;
    float[] xyz =
        new float[] {
          x * ColorUtils.WHITE_POINT_D65[0],
          y * ColorUtils.WHITE_POINT_D65[1],
          z * ColorUtils.WHITE_POINT_D65[2],
        };
    return intFromXyz(xyz);
  }

  /**
   * L* in L*a*b* and Y in XYZ measure the same quantity, luminance. L* measures perceptual
   * luminance, a linear scale. Y in XYZ measures relative luminance, a logarithmic scale.
   *
   * @param lstar L* in L*a*b*
   * @return Y in XYZ
   */
  public static float yFromLstar(float lstar) {
    float ke = 8.0f;
    if (lstar > ke) {
      return (float) Math.pow(((lstar + 16.0) / 116.0), 3) * 100f;
    } else {
      return lstar / (24389f / 27f) * 100f;
    }
  }

  /**
   * Convert a normalized RGB channel to a normalized linear RGB channel.
   *
   * @param rgb 0.0 <= rgb <= 1.0, represents R/G/B channel
   */
  public static float linearized(float rgb) {
    if (rgb <= 0.04045f) {
      return (rgb / 12.92f);
    } else {
      return (float) Math.pow(((rgb + 0.055f) / 1.055f), 2.4f);
    }
  }

  /**
   * Convert a normalized linear RGB channel to a normalized RGB channel.
   *
   * @param rgb 0.0 <= rgb <= 1.0, represents linear R/G/B channel
   */
  public static float delinearized(float rgb) {
    if (rgb <= 0.0031308f) {
      return (rgb * 12.92f);
    } else {
      return ((1.055f * (float) Math.pow(rgb, 1.0f / 2.4f)) - 0.055f);
    }
  }
}
