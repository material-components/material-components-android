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
public final class SchemeFruitSaladTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff0393c3);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff3A7E9E);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff6E72AC);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff777682);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff75758B);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff007ea7);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff006688);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff003042);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffaae0ff);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffC2E8FF);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff004f6b);
  }

  @Test
  public void lightTheme_minContrast_tertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xffd5d6ff);
  }

  @Test
  public void lightTheme_standardContrast_tertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xffE0E0FF);
  }

  @Test
  public void lightTheme_maxContrast_tertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.tertiaryContainer().getArgb(scheme)).isSameColorAs(0xff40447b);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff0083ae);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff004d67);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffbf8ff);
  }

  @Test
  public void lightTheme_standardContrast_secondary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.secondary().getArgb(scheme)).isSameColorAs(0xff196584);
  }

  @Test
  public void lightTheme_standardContrast_secondaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.secondaryContainer().getArgb(scheme)).isSameColorAs(0xffc2e8ff);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff1e9bcb);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xFF76D1FF);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xFFe0f3ff);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff003f56);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFF004D67);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xFF68ceff);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff008ebc);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffC2E8FF);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xFF000d15);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xff7b7fbb);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xffe0e0ff);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onTertiaryContainer().getArgb(scheme)).isSameColorAs(0xFF00003c);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff12131c);
  }

  @Test
  public void darkTheme_standardContrast_secondary() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.secondary().getArgb(scheme)).isSameColorAs(0xff8ecff2);
  }

  @Test
  public void darkTheme_standardContrast_secondaryContainer() {
    SchemeFruitSalad scheme = new SchemeFruitSalad(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.secondaryContainer().getArgb(scheme)).isSameColorAs(0xff004d67);
  }
}
