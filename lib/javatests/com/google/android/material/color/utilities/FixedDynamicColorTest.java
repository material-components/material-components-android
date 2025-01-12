/*
 * Copyright (C) 2023 The Android Open Source Project
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
public final class FixedDynamicColorTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void fixedColorsInTonalSpot() {
    final DynamicScheme scheme = new SchemeTonalSpot(Hct.fromInt(0xFFFF0000), true, 0.0);

    assertThat(dynamicColors.primaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(90.0);
    assertThat(dynamicColors.primaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(80.0);
    assertThat(dynamicColors.onPrimaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(10.0);
    assertThat(dynamicColors.onPrimaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(30.0);
    assertThat(dynamicColors.secondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(90.0);
    assertThat(dynamicColors.secondaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(80.0);
    assertThat(dynamicColors.onSecondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(10.0);
    assertThat(dynamicColors.onSecondaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(30.0);
    assertThat(dynamicColors.tertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(90.0);
    assertThat(dynamicColors.tertiaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(80.0);
    assertThat(dynamicColors.onTertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(10.0);
    assertThat(dynamicColors.onTertiaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(30.0);
  }

  @Test
  public void fixedArgbColorsInTonalSpot() {
    final DynamicScheme scheme = new SchemeTonalSpot(Hct.fromInt(0xFFFF0000), true, 0.0);

    assertThat(scheme.getPrimaryFixed()).isEqualTo(0xFFFFDAD4);
    assertThat(scheme.getPrimaryFixedDim()).isEqualTo(0xFFFFB4A8);
    assertThat(scheme.getOnPrimaryFixed()).isEqualTo(0xFF3A0905);
    assertThat(scheme.getOnPrimaryFixedVariant()).isEqualTo(0xFF73342A);
    assertThat(scheme.getSecondaryFixed()).isEqualTo(0xFFFFDAD4);
    assertThat(scheme.getSecondaryFixedDim()).isEqualTo(0xFFE7BDB6);
    assertThat(scheme.getOnSecondaryFixed()).isEqualTo(0xFF2C1512);
    assertThat(scheme.getOnSecondaryFixedVariant()).isEqualTo(0xFF5D3F3B);
    assertThat(scheme.getTertiaryFixed()).isEqualTo(0xFFFBDFA6);
    assertThat(scheme.getTertiaryFixedDim()).isEqualTo(0xFFDEC48C);
    assertThat(scheme.getOnTertiaryFixed()).isEqualTo(0xFF251A00);
    assertThat(scheme.getOnTertiaryFixedVariant()).isEqualTo(0xFF564419);
  }

  @Test
  public void fixedColorsInLightMonochrome() {
    final DynamicScheme scheme = new SchemeMonochrome(Hct.fromInt(0xFFFF0000), false, 0.0);

    assertThat(dynamicColors.primaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(40.0);
    assertThat(dynamicColors.primaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(30.0);
    assertThat(dynamicColors.onPrimaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(100.0);
    assertThat(dynamicColors.onPrimaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(90.0);
    assertThat(dynamicColors.secondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(80.0);
    assertThat(dynamicColors.secondaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(70.0);
    assertThat(dynamicColors.onSecondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(10.0);
    assertThat(dynamicColors.onSecondaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(25.0);
    assertThat(dynamicColors.tertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(40.0);
    assertThat(dynamicColors.tertiaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(30.0);
    assertThat(dynamicColors.onTertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(100.0);
    assertThat(dynamicColors.onTertiaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(90.0);
  }

  @Test
  public void fixedColorsInDarkMonochrome() {
    final DynamicScheme scheme = new SchemeMonochrome(Hct.fromInt(0xFFFF0000), true, 0.0);

    assertThat(dynamicColors.primaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(40.0);
    assertThat(dynamicColors.primaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(30.0);
    assertThat(dynamicColors.onPrimaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(100.0);
    assertThat(dynamicColors.onPrimaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(90.0);
    assertThat(dynamicColors.secondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(80.0);
    assertThat(dynamicColors.secondaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(70.0);
    assertThat(dynamicColors.onSecondaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(10.0);
    assertThat(dynamicColors.onSecondaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(25.0);
    assertThat(dynamicColors.tertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(40.0);
    assertThat(dynamicColors.tertiaryFixedDim().getHct(scheme).getTone()).isWithin(1.0).of(30.0);
    assertThat(dynamicColors.onTertiaryFixed().getHct(scheme).getTone()).isWithin(1.0).of(100.0);
    assertThat(dynamicColors.onTertiaryFixedVariant().getHct(scheme).getTone())
        .isWithin(1.0)
        .of(90.0);
  }
}
