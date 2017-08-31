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
package android.support.design.math;

/** A class that contains utility methods related to numbers. */
public final class MathUtils {

  /** Default epsilon value for fuzzy float comparisons. */
  public static final float DEFAULT_EPSILON = 0.0001f;

  private static final float DEG_TO_RAD = 3.1415926f / 180.0f;
  private static final float RAD_TO_DEG = 180.0f / 3.1415926f;

  private MathUtils() {}

  public static float abs(float v) {
    return v > 0 ? v : -v;
  }

  public static int constrain(int amount, int low, int high) {
    return amount < low ? low : (amount > high ? high : amount);
  }

  public static long constrain(long amount, long low, long high) {
    return amount < low ? low : (amount > high ? high : amount);
  }

  public static float constrain(float amount, float low, float high) {
    return amount < low ? low : (amount > high ? high : amount);
  }

  public static int nearest(int amount, int low, int high) {
    return abs(amount - low) < abs(amount - high) ? low : high;
  }

  public static float nearest(float amount, float low, float high) {
    return abs(amount - low) < abs(amount - high) ? low : high;
  }

  public static float log(float a) {
    return (float) Math.log(a);
  }

  public static float exp(float a) {
    return (float) Math.exp(a);
  }

  public static float pow(float a, float b) {
    return (float) Math.pow(a, b);
  }

  public static float max(float a, float b) {
    return a > b ? a : b;
  }

  public static float max(int a, int b) {
    return a > b ? a : b;
  }

  public static float max(float a, float b, float c) {
    return a > b ? (a > c ? a : c) : (b > c ? b : c);
  }

  public static float max(int a, int b, int c) {
    return a > b ? (a > c ? a : c) : (b > c ? b : c);
  }

  public static float max(float a, float b, float c, float d) {
    return a > b && a > c && a > d ? a : b > c && b > d ? b : c > d ? c : d;
  }

  public static float max(int a, int b, int c, int d) {
    return a > b && a > c && a > d ? a : b > c && b > d ? b : c > d ? c : d;
  }

  public static float min(float a, float b) {
    return a < b ? a : b;
  }

  public static float min(int a, int b) {
    return a < b ? a : b;
  }

  public static float min(float a, float b, float c) {
    return a < b ? (a < c ? a : c) : (b < c ? b : c);
  }

  public static float min(int a, int b, int c) {
    return a < b ? (a < c ? a : c) : (b < c ? b : c);
  }

  public static float dist(float x1, float y1, float x2, float y2) {
    final float x = (x2 - x1);
    final float y = (y2 - y1);
    return (float) Math.hypot(x, y);
  }

  public static float dist(float x1, float y1, float z1, float x2, float y2, float z2) {
    final float x = (x2 - x1);
    final float y = (y2 - y1);
    final float z = (z2 - z1);
    return (float) Math.sqrt(x * x + y * y + z * z);
  }

  public static float mag(float a, float b) {
    return (float) Math.hypot(a, b);
  }

  public static float mag(float a, float b, float c) {
    return (float) Math.sqrt(a * a + b * b + c * c);
  }

  public static float sq(float v) {
    return v * v;
  }

  public static float sqrt(float value) {
    return (float) Math.sqrt(value);
  }

  public static float dot(float v1x, float v1y, float v2x, float v2y) {
    return v1x * v2x + v1y * v2y;
  }

  public static float cross(float v1x, float v1y, float v2x, float v2y) {
    return v1x * v2y - v1y * v2x;
  }

  public static float radians(float degrees) {
    return degrees * DEG_TO_RAD;
  }

  public static float degrees(float radians) {
    return radians * RAD_TO_DEG;
  }

  public static float acos(float value) {
    return (float) Math.acos(value);
  }

  public static float asin(float value) {
    return (float) Math.asin(value);
  }

  public static float atan(float value) {
    return (float) Math.atan(value);
  }

  public static float atan2(float a, float b) {
    return (float) Math.atan2(a, b);
  }

  public static float tan(float angle) {
    return (float) Math.tan(angle);
  }

  public static float lerp(float start, float stop, float amount) {
    return (1 - amount) * start + amount * stop;
  }

  public static float norm(float start, float stop, float value) {
    return (value - start) / (stop - start);
  }

  public static float map(
      float minStart, float minStop, float maxStart, float maxStop, float value) {
    return maxStart + (maxStart - maxStop) * ((value - minStart) / (minStop - minStart));
  }

  /**
   * Fuzzy less than or equal to for floats.
   *
   * <p>Returns true if {@code a} is less than or equal to {@code b}, allowing for {@code epsilon}
   * error due to limitations in floating point accuracy.
   *
   * <p>Does not handle overflow, underflow, infinity, or NaN.
   */
  public static boolean leq(float a, float b, float epsilon) {
    return a <= b + epsilon;
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
   * Fuzzy equal to for floats.
   *
   * <p>Returns true if {@code a} is equal to {@code b}, allowing for {@code epsilon} error due to
   * limitations in floating point accuracy.
   *
   * <p>Does not handle overflow, underflow, infinity, or NaN.
   */
  public static boolean eq(float a, float b, float epsilon) {
    return abs(a - b) <= epsilon;
  }

  /**
   * Fuzzy not equal to for floats.
   *
   * <p>Returns false if {@code a} is equal to {@code b}, allowing for {@code epsilon} error due to
   * limitations in floating point accuracy.
   *
   * <p>Does not handle overflow, underflow, infinity, or NaN.
   */
  public static boolean neq(float a, float b, float epsilon) {
    return abs(a - b) > epsilon;
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
}
