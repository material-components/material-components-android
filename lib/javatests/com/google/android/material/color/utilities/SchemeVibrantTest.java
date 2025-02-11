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
public final class SchemeVibrantTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff080CFF);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff7B7296);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff886C9D);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777682);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff767685);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff5660ff);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff343dff);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff00019f);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd5d6ff);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff0000f6);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff5e68ff);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff0000ef);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff7c84ff);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffbec2ff);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xfff0eeff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff0001c9);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff0000ef);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffbabdff);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff6b75ff);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff00003d);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff9679ab);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xfff2daff);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff16002a);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeVibrant scheme = new SchemeVibrant(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }
}
