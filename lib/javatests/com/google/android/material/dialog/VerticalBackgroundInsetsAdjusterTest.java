/*
 * Copyright 2026 The Android Open Source Project
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
package com.google.android.material.dialog;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Tests for {@link VerticalBackgroundInsetsAdjuster}. */
@RunWith(RobolectricTestRunner.class)
public class VerticalBackgroundInsetsAdjusterTest {

  @Test
  public void calculateVerticalInsets_enoughSpace_keepsConfiguredInsets() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(80, 80, 500))
        .asList()
        .containsExactly(80, 80)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_exactlyEnoughSpace_keepsConfiguredInsets() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(80, 80, 160))
        .asList()
        .containsExactly(80, 80)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_notEnoughSpace_shrinksInsetsToSpareHeight() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(80, 80, 101))
        .asList()
        .containsExactly(50, 51)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_asymmetricInsets_keepsRatio() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(30, 90, 60))
        .asList()
        .containsExactly(15, 45)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_noSpareHeight_removesInsets() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(80, 80, 0))
        .asList()
        .containsExactly(0, 0)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_contentTallerThanWindow_removesInsets() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(80, 80, -40))
        .asList()
        .containsExactly(0, 0)
        .inOrder();
  }

  @Test
  public void calculateVerticalInsets_noConfiguredInsets_staysZero() {
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(0, 0, -40))
        .asList()
        .containsExactly(0, 0)
        .inOrder();
    assertThat(VerticalBackgroundInsetsAdjuster.calculateVerticalInsets(0, 0, 100))
        .asList()
        .containsExactly(0, 0)
        .inOrder();
  }
}
