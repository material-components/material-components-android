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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class SchemeContentTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff080CFF);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff656DD3);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff81009F);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff767684);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff757589);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff5660ff);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xFF0001C3);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xFF000181);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd5d6ff);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFF2D36FF);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFF0000E3);
  }

  @Test
  public void lightTheme_minContrast_tertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, -1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xfffac9ff);
  }

  @Test
  public void lightTheme_standardContrast_tertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 0.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xFF9221AF);
  }

  @Test
  public void lightTheme_maxContrast_tertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xFF73008E);
  }

  @Test
  public void lightTheme_minContrast_objectionableTertiaryContainerLightens() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF850096), false, -1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xffffccd7);
  }

  @Test
  public void lightTheme_standardContrast_objectionableTertiaryContainerLightens() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF850096), false, 0.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xFFAC1B57);
  }

  @Test
  public void lightTheme_maxContrast_objectionableTertiaryContainerDarkens() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF850096), false, 1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xFF870040);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff333dff);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, -1);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFFFBF8FF);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFFFBF8FF);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFFFBF8FF);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff7c84ff);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xFFBEC2FF);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xfffdf9ff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff0001c9);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFF0000E6);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFFC4C6FF);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff949bff);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffd7d8ff);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffe577ff);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xfffbccff);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFF12121D);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFF12121D);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeContent scheme = new SchemeContent(Hct.fromInt(0xFF0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xFF12121D);
  }
}
