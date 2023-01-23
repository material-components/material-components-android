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

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class MathUtilsTest {

  @Test
  public void signum() {
    assertThat(MathUtils.signum(1.5)).isEqualTo(1);
    assertThat(MathUtils.signum(0)).isEqualTo(0);
    assertThat(MathUtils.signum(-3.0)).isEqualTo(-1);
  }

  @Test
  public void sanitizeDegreesInt() {
    assertThat(MathUtils.sanitizeDegreesInt(0)).isEqualTo(0);
    assertThat(MathUtils.sanitizeDegreesInt(30)).isEqualTo(30);
    assertThat(MathUtils.sanitizeDegreesInt(150)).isEqualTo(150);
    assertThat(MathUtils.sanitizeDegreesInt(360)).isEqualTo(0);
    assertThat(MathUtils.sanitizeDegreesInt(450)).isEqualTo(90);
    assertThat(MathUtils.sanitizeDegreesInt(1000000)).isEqualTo(280);
    assertThat(MathUtils.sanitizeDegreesInt(-10)).isEqualTo(350);
    assertThat(MathUtils.sanitizeDegreesInt(-90)).isEqualTo(270);
    assertThat(MathUtils.sanitizeDegreesInt(-1000000)).isEqualTo(80);
  }

  @Test
  public void sanitizeDegreesDouble() {
    assertThat(MathUtils.sanitizeDegreesDouble(0.0)).isWithin(0.001).of(0.0);
    assertThat(MathUtils.sanitizeDegreesDouble(30.0)).isWithin(0.001).of(30.0);
    assertThat(MathUtils.sanitizeDegreesDouble(150.0)).isWithin(0.001).of(150.0);
    assertThat(MathUtils.sanitizeDegreesDouble(360.0)).isWithin(0.001).of(0.0);
    assertThat(MathUtils.sanitizeDegreesDouble(450.0)).isWithin(0.001).of(90.0);
    assertThat(MathUtils.sanitizeDegreesDouble(1000000.0)).isWithin(0.001).of(280.0);
    assertThat(MathUtils.sanitizeDegreesDouble(-10.0)).isWithin(0.001).of(350.0);
    assertThat(MathUtils.sanitizeDegreesDouble(-90.0)).isWithin(0.001).of(270.0);
    assertThat(MathUtils.sanitizeDegreesDouble(-1000000.0)).isWithin(0.001).of(80.0);
    assertThat(MathUtils.sanitizeDegreesDouble(100.375)).isWithin(0.001).of(100.375);
    assertThat(MathUtils.sanitizeDegreesDouble(123456.789)).isWithin(0.001).of(336.789);
    assertThat(MathUtils.sanitizeDegreesDouble(-200.625)).isWithin(0.001).of(159.375);
    assertThat(MathUtils.sanitizeDegreesDouble(-123456.789)).isWithin(0.001).of(23.211);
  }

  @Test
  public void rotationDirection() {
    for (double from = 0.0; from < 360.0; from += 15.0) {
      for (double to = 7.5; to < 360.0; to += 15.0) {
        double expectedAnswer = originalRotationDirection(from, to);
        double actualAnswer = MathUtils.rotationDirection(from, to);
        assertThat(actualAnswer).isEqualTo(expectedAnswer);
        assertThat(Math.abs(actualAnswer)).isEqualTo(1.0);
      }
    }
  }

  // Original implementation for MathUtils.rotationDirection.
  // Included here to test equivalence with new implementation.
  private static double originalRotationDirection(double from, double to) {
    double a = to - from;
    double b = to - from + 360.0;
    double c = to - from - 360.0;
    double aAbs = Math.abs(a);
    double bAbs = Math.abs(b);
    double cAbs = Math.abs(c);
    if (aAbs <= bAbs && aAbs <= cAbs) {
      return a >= 0.0 ? 1.0 : -1.0;
    } else if (bAbs <= aAbs && bAbs <= cAbs) {
      return b >= 0.0 ? 1.0 : -1.0;
    } else {
      return c >= 0.0 ? 1.0 : -1.0;
    }
  }
}
