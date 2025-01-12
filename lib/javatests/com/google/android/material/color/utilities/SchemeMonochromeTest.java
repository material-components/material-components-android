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

import static com.google.android.material.color.utilities.ArgbSubject.assertThat;
import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SchemeMonochromeTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777777);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777777);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777777);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777777);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff777777);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff747474);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd9d9d9);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff3b3b3b);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff3b3b3b);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff7a7a7a);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff919191);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff3a3a3a);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd4d4d4);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd4d4d4);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff848484);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff848484);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_monochromeSpec() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getHct(scheme).getTone()).isWithin(1).of(100);
    assertThat(dynamicColors.onPrimary().getHct(scheme).getTone()).isWithin(1).of(10);
    assertThat(dynamicColors.primaryContainer().getHct(scheme).getTone()).isWithin(1).of(85);
    assertThat(dynamicColors.onPrimaryContainer().getHct(scheme).getTone()).isWithin(1).of(0);
    assertThat(dynamicColors.secondary().getHct(scheme).getTone()).isWithin(1).of(80);
    assertThat(dynamicColors.onSecondary().getHct(scheme).getTone()).isWithin(1).of(10);
    assertThat(dynamicColors.secondaryContainer().getHct(scheme).getTone()).isWithin(1).of(30);
    assertThat(dynamicColors.onSecondaryContainer().getHct(scheme).getTone()).isWithin(1).of(90);
    assertThat(dynamicColors.tertiary().getHct(scheme).getTone()).isWithin(1).of(90);
    assertThat(dynamicColors.onTertiary().getHct(scheme).getTone()).isWithin(1).of(10);
    assertThat(dynamicColors.tertiaryContainer().getHct(scheme).getTone()).isWithin(1).of(60);
    assertThat(dynamicColors.onTertiaryContainer().getHct(scheme).getTone()).isWithin(1).of(0);
  }

  @Test
  public void lightTheme_monochromeSpec() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getHct(scheme).getTone()).isWithin(1).of(0);
    assertThat(dynamicColors.onPrimary().getHct(scheme).getTone()).isWithin(1).of(90);
    assertThat(dynamicColors.primaryContainer().getHct(scheme).getTone()).isWithin(1).of(25);
    assertThat(dynamicColors.onPrimaryContainer().getHct(scheme).getTone()).isWithin(1).of(100);
    assertThat(dynamicColors.secondary().getHct(scheme).getTone()).isWithin(1).of(40);
    assertThat(dynamicColors.onSecondary().getHct(scheme).getTone()).isWithin(1).of(100);
    assertThat(dynamicColors.secondaryContainer().getHct(scheme).getTone()).isWithin(1).of(85);
    assertThat(dynamicColors.onSecondaryContainer().getHct(scheme).getTone()).isWithin(1).of(10);
    assertThat(dynamicColors.tertiary().getHct(scheme).getTone()).isWithin(1).of(25);
    assertThat(dynamicColors.onTertiary().getHct(scheme).getTone()).isWithin(1).of(90);
    assertThat(dynamicColors.tertiaryContainer().getHct(scheme).getTone()).isWithin(1).of(49);
    assertThat(dynamicColors.onTertiaryContainer().getHct(scheme).getTone()).isWithin(1).of(100);
  }
}
