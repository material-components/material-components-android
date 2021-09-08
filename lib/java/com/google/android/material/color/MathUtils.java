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

import static java.lang.Math.max;
import static java.lang.Math.min;

/** Utility methods for mathematical operations. */
final class MathUtils {
  private MathUtils() {}
  /** Ensure min <= input <= max */
  static float clamp(float min, float max, float input) {
    return min(max(input, min), max);
  }

  /** Linearly interpolate from start to stop, by amount (0.0 <= amount <= 1.0) */
  public static float lerp(float start, float stop, float amount) {
    return (1.0f - amount) * start + amount * stop;
  }

  /** Determine the shortest angle between two angles, measured in degrees. */
  public static float differenceDegrees(float a, float b) {
    return 180f - Math.abs(Math.abs(a - b) - 180f);
  }

  /** Ensure 0 <= degrees < 360 */
  public static float sanitizeDegrees(float degrees) {
    if (degrees < 0f) {
      return (degrees % 360.0f) + 360.f;
    } else if (degrees >= 360.0f) {
      return degrees % 360.0f;
    } else {
      return degrees;
    }
  }

  /** Ensure 0 <= degrees < 360 */
  public static int sanitizeDegrees(int degrees) {
    if (degrees < 0) {
      return (degrees % 360) + 360;
    } else if (degrees >= 360) {
      return degrees % 360;
    } else {
      return degrees;
    }
  }

  /** Convert radians to degrees. */
  static float toDegrees(float radians) {
    return radians * 180.0f / (float) Math.PI;
  }

  /** Convert degrees to radians. */
  static float toRadians(float degrees) {
    return degrees / 180.0f * (float) Math.PI;
  }
}
