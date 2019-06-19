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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Unit tests for {@link RippleUtils}. */
@RunWith(RobolectricTestRunner.class)
public class RippleUtilsTest {

  @Test
  public void testShouldDrawRippleCompat_disabled_returnsFalse() {
    assertThat(RippleUtils.shouldDrawRippleCompat(new int[] {})).isFalse();
  }

  @Test
  public void testShouldDrawRippleCompat_disabledAndPressed_returnsFalse() {
    assertThat(RippleUtils.shouldDrawRippleCompat(new int[] {android.R.attr.state_pressed}))
        .isFalse();
  }

  @Test
  public void testShouldDrawRippleCompat_enabled_returnsFalse() {
    assertThat(RippleUtils.shouldDrawRippleCompat(new int[] {android.R.attr.state_enabled}))
        .isFalse();
  }

  @Test
  public void testShouldDrawRippleCompat_enabledAndPressed_returnsTrue() {
    assertThat(
            RippleUtils.shouldDrawRippleCompat(
                new int[] {android.R.attr.state_enabled, android.R.attr.state_pressed}))
        .isTrue();
  }

  @Test
  public void testShouldDrawRippleCompat_enabledAndFocused_returnsTrue() {
    assertThat(
            RippleUtils.shouldDrawRippleCompat(
                new int[] {android.R.attr.state_enabled, android.R.attr.state_focused}))
        .isTrue();
  }

  @Test
  public void testShouldDrawRippleCompat_enabledPressedAndFocused_returnsTrue() {
    assertThat(
            RippleUtils.shouldDrawRippleCompat(
                new int[] {
                  android.R.attr.state_enabled,
                  android.R.attr.state_pressed,
                  android.R.attr.state_focused
                }))
        .isTrue();
  }

  @Test
  public void testShouldDrawRippleCompat_enabledAndHovered_returnsTrue() {
    assertThat(
            RippleUtils.shouldDrawRippleCompat(
                new int[] {android.R.attr.state_enabled, android.R.attr.state_hovered}))
        .isTrue();
  }
}
