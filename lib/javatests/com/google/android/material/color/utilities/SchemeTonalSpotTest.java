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
public final class SchemeTonalSpotTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff6E72AC);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff75758B);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff936B84);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff77767d);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff777680);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff6c70aa);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff555992);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff22265c);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffd5d6ff);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff40447b);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff7175b0);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff3e4278);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_minContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xff5f5e65);
  }

  @Test
  public void lightTheme_standardContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xff1b1b21);
  }

  @Test
  public void lightTheme_maxContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void lightTheme_minContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xfffffbff);
  }

  @Test
  public void lightTheme_standardContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_maxContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xfffffbff);
  }

  @Test
  public void lightTheme_standardContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_maxContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xfffffbff);
  }

  @Test
  public void lightTheme_standardContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_maxContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff888cc8);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffbec2ff);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xfff0eeff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff31356b);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff3E4278);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffbabefd);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff7b7fbb);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff00003c);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffa17891);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffffd8ee);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff1b0315);
  }

  @Test
  public void darkTheme_minContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xff27283b);
  }

  @Test
  public void darkTheme_standardContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xff2e2f42);
  }

  @Test
  public void darkTheme_maxContrast_onSecondary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onSecondary().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xff3e1f34);
  }

  @Test
  public void darkTheme_standardContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xff46263b);
  }

  @Test
  public void darkTheme_maxContrast_onTertiary() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiary().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xff5c0003);
  }

  @Test
  public void darkTheme_standardContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xff690005);
  }

  @Test
  public void darkTheme_maxContrast_onError() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onError().getArgb(scheme)).isSameColorAs(0xff000000);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131318);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131318);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff131318);
  }

  @Test
  public void darkTheme_minContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xffa4a2a9);
  }

  @Test
  public void darkTheme_standardContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xffe4e1e9);
  }

  @Test
  public void darkTheme_maxContrast_onSurface() {
    SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onSurface().getArgb(scheme)).isSameColorAs(0xffffffff);
  }
}
