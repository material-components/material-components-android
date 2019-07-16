/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.ripple;

import static com.google.common.truth.Truth.assertThat;

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

/** Unit tests for {@link RippleUtils#shouldDrawRippleCompat}. */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class RippleUtilsShouldDrawRippleCompatTest {

  @ParameterizedRobolectricTestRunner.Parameter(0)
  public String stateDescription;

  @ParameterizedRobolectricTestRunner.Parameter(1)
  public int[] stateSet;

  @ParameterizedRobolectricTestRunner.Parameter(2)
  public boolean shouldDrawRippleCompat;

  /**
   * Parameterized test data for this class. Returns a {@link List} of {@link Object} arrays that
   * include:
   * <li>Description of the states set (used for the test name since int[] toString is not very
   *     descriptive)
   * <li>The int[] of states to pass to shouldDrawRippleCompat
   * <li>The expected value of shouldDrawRippleCompat
   */
  @ParameterizedRobolectricTestRunner.Parameters(name = "States: {0} Result: {2}")
  public static List<Object[]> getTestData() {
    Object[][] data = {
      {"disabled", new int[] {}, false},
      {"disabled, pressed", new int[] {android.R.attr.state_pressed}, false},
      {"enabled", new int[] {android.R.attr.state_enabled}, false},
      {
        "enabled, pressed",
        new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed},
        true
      },
      {
        "enabled, focused",
        new int[] {android.R.attr.state_enabled, android.R.attr.state_focused},
        true
      },
      {
        "enabled, pressed, focused",
        new int[] {
          android.R.attr.state_enabled, android.R.attr.state_pressed, android.R.attr.state_focused
        },
        true
      },
      {
        "enabled, hovered",
        new int[] {android.R.attr.state_enabled, android.R.attr.state_hovered},
        true
      },
    };
    return Arrays.asList(data);
  }

  /** Tests shouldDrawRippleCompat for the sets of states defined in {@link #getTestData}. */
  @Test
  public void testShouldDrawRippleCompat() {
    assertThat(RippleUtils.shouldDrawRippleCompat(stateSet)).isEqualTo(shouldDrawRippleCompat);
  }
}
