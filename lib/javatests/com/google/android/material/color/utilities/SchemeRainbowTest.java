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

import static com.google.android.material.color.utilities.ArgbSubject.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SchemeRainbowTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff696FC4);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff75758B);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff936B84);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777777);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff777777);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff676DC1);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff5056A9);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff1b2074);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd5d6ff);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffE0E0FF);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff3a4092);
  }

  @Test
  public void lightTheme_minContrast_tertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xfffbcbe7);
  }

  @Test
  public void lightTheme_standardContrast_tertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xffffd8ee);
  }

  @Test
  public void lightTheme_maxContrast_tertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xff613e55);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff6c72c7);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff383e8f);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_standardContrast_secondary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.secondary().getArgb(scheme)).isSameColorAs(0xff5c5d72);
  }

  @Test
  public void lightTheme_standardContrast_secondaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.secondaryContainer().getArgb(scheme)).isSameColorAs(0xffe1e0f9);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff8389e0);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffbec2ff);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xfff0eeff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff2a3082);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff383E8F);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffbabdff);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff767cd2);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff00003d);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffa17891);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffffd8ee);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff1b0315);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_standardContrast_secondary() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.secondary().getArgb(scheme)).isSameColorAs(0xffc5c4dd);
  }

  @Test
  public void darkTheme_standardContrast_secondaryContainer() {
    SchemeRainbow scheme = new SchemeRainbow(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.secondaryContainer().getArgb(scheme)).isSameColorAs(0xff444559);
  }
}
