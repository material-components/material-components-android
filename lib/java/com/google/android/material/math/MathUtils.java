/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.math;

import androidx.annotation.NonNull;

/** A class that contains utility methods related to numbers. */
public final class MathUtils {

  /** Default epsilon value for fuzzy float comparisons. */
  public static final float DEFAULT_EPSILON = 0.0001f;

  private MathUtils() {}

  /** Returns the distance between two points. */
  public static float dist(float x1, float y1, float x2, float y2) {
    final float x = (x2 - x1);
    final float y = (y2 - y1);
    return (float) Math.hypot(x, y);
  }

  /**
   * Returns the linear interpolation of {@code amount} between {@code start} and {@code stop}.
   */
  public static float lerp(float start, float stop, float amount) {
    return (1 - amount) * start + amount * stop;
  }

  /**
   * Fuzzy greater than or equal to for floats.
   *
   * <p>Returns true if {@code a} is greater than or equal to {@code b}, allowing for {@code
   * epsilon} error due to limitations in floating point accuracy.
   *
   * <p>Does not handle overflow, underflow, infinity, or NaN.
   */
  public static boolean geq(float a, float b, float epsilon) {
    return a + epsilon >= b;
  }

  /**
   * Returns the furthest distance from the point defined by pointX and pointY to the four corners
   * of the rectangle defined by rectLeft, rectTop, rectRight, and rectBottom.
   *
   * <p>The caller should ensure that the point and rectangle share the same coordinate space.
   */
  public static float distanceToFurthestCorner(
      float pointX,
      float pointY,
      float rectLeft,
      float rectTop,
      float rectRight,
      float rectBottom) {
    return max(
        dist(pointX, pointY, rectLeft, rectTop),
        dist(pointX, pointY, rectRight, rectTop),
        dist(pointX, pointY, rectRight, rectBottom),
        dist(pointX, pointY, rectLeft, rectBottom));
  }

  /** Returns the maximum of the input values. */
  private static float max(float a, float b, float c, float d) {
    return a > b && a > c && a > d ? a : b > c && b > d ? b : c > d ? c : d;
  }

  /**
   * This returns as similar as {@link Math#floorMod(int, int)}. Instead it works for float type
   * values. And the re-implementation makes it back compatible to API<24.
   */
  public static float floorMod(float x, int y) {
    int r = (int) (x / y);
    // if the signs are different and modulo not zero, round down
    if (Math.signum(x) * y < 0 && (r * y != x)) {
      r--;
    }
    return x - r * y;
  }

  /**
   * This is same as {@link Math#floorMod(int, int)}. Re-implementation makes it back compatible to
   * API<24.
   */
  public static int floorMod(int x, int y) {
    int r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return x - r * y;
  }

  /**
   * Returns whether the array contains all same elements.
   *
   * @param array Input array of floats.
   */
  public static boolean areAllElementsEqual(@NonNull float[] array) {
    if (array.length <= 1) {
      return true;
    }
    float sample = array[0];
    for (int i = 1; i < array.length; i++) {
      if (array[i] != sample) {
        return false;
      }
    }
    return true;
  }
}
