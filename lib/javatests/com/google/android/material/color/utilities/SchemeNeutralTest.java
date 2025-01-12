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
public final class SchemeNeutralTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff767685);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777680);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff75758B);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff787678);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff787678);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff737383);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff5d5d6c);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff2b2b38);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd9d7e9);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffe2e1f3);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff484856);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff797888);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff454654);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffcf8fa);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffcf8fa);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffcf8fa);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff908f9f);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffc6c5d6);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xfff0eeff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff393947);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff454654);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffc2c1d2);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff838393);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffe2e1f3);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff090a16);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff828299);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffe1e0f9);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff080a1b);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131315);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131315);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeNeutral scheme = new SchemeNeutral(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131315);
  }
}
