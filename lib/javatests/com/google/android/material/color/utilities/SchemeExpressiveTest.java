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
public final class SchemeExpressiveTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void testKeyColors() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 0.0);

    assertThat(dynamicColors.primaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff35855F);
    assertThat(dynamicColors.secondaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff8C6D8C);
    assertThat(dynamicColors.tertiaryPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff806EA1);
    assertThat(dynamicColors.neutralPaletteKeyColor().getArgb(scheme)).isSameColorAs(0xff79757F);
    assertThat(dynamicColors.neutralVariantPaletteKeyColor().getArgb(scheme))
        .isSameColorAs(0xff7A7585);
  }

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff32835d);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff146c48);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff00341f);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff99eabd);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xffa2f4c6);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff005436);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff388862);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff005234);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffffffff);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffdf7ff);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffdf7ff);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xfffdf7ff);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff51a078);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xff87d7ab);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primary().getArgb(scheme)).isSameColorAs(0xffbbffd7);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff00432a);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff005234);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.primaryContainer().getArgb(scheme)).isSameColorAs(0xff83d3a8);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff43936c);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xffa2f4c6);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.onPrimaryContainer().getArgb(scheme)).isSameColorAs(0xff000e06);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff14121a);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff14121a);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeExpressive scheme = new SchemeExpressive(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(dynamicColors.surface().getArgb(scheme)).isSameColorAs(0xff14121a);
  }
}
