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
public final class SchemeMonochromeTest {

  @Test
  public void lightTheme_minContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xff747474);
  }

  @Test
  public void lightTheme_standardContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xff5e5e5e);
  }

  @Test
  public void lightTheme_maxContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xff222222);
  }

  @Test
  public void lightTheme_minContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xffe2e2e2);
  }

  @Test
  public void lightTheme_standardContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xffe2e2e2);
  }

  @Test
  public void lightTheme_maxContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xff434343);
  }

  @Test
  public void lightTheme_minContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xff646464);
  }

  @Test
  public void lightTheme_standardContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xff1b1b1b);
  }

  @Test
  public void lightTheme_maxContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xffdadada);
  }

  @Test
  public void lightTheme_minContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, -1.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_standardContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 0.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void lightTheme_maxContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), false, 1.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xfff9f9f9);
  }

  @Test
  public void darkTheme_minContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xff747474);
  }

  @Test
  public void darkTheme_standardContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xffc6c6c6);
  }

  @Test
  public void darkTheme_maxContrast_primary() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(MaterialDynamicColors.primary.getArgb(scheme)).isSameColorAs(0xfff5f5f5);
  }

  @Test
  public void darkTheme_minContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xff474747);
  }

  @Test
  public void darkTheme_standardContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xff474747);
  }

  @Test
  public void darkTheme_maxContrast_primaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(MaterialDynamicColors.primaryContainer.getArgb(scheme)).isSameColorAs(0xffcbcbcb);
  }

  @Test
  public void darkTheme_minContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xffb5b5b5);
  }

  @Test
  public void darkTheme_standardContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xffe2e2e2);
  }

  @Test
  public void darkTheme_maxContrast_onPrimaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(MaterialDynamicColors.onPrimaryContainer.getArgb(scheme)).isSameColorAs(0xff393939);
  }

  @Test
  public void darkTheme_minContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(MaterialDynamicColors.onTertiaryContainer.getArgb(scheme)).isSameColorAs(0xffb5b5b5);
  }

  @Test
  public void darkTheme_standardContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(MaterialDynamicColors.onTertiaryContainer.getArgb(scheme)).isSameColorAs(0xffe2e2e2);
  }

  @Test
  public void darkTheme_maxContrast_onTertiaryContainer() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(MaterialDynamicColors.onTertiaryContainer.getArgb(scheme)).isSameColorAs(0xff393939);
  }

  @Test
  public void darkTheme_minContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, -1.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_standardContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 0.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xff131313);
  }

  @Test
  public void darkTheme_maxContrast_surface() {
    SchemeMonochrome scheme = new SchemeMonochrome(Hct.fromInt(0xff0000ff), true, 1.0);
    assertThat(MaterialDynamicColors.surface.getArgb(scheme)).isSameColorAs(0xff131313);
  }
}
