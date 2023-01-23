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
public final class SchemeTest {

  @Test
  public void lightScheme_ofBlue() {
    Scheme scheme = Scheme.light(0xff0000ff);
    assertThat(scheme.getPrimary()).isSameColorAs(0xff343DFF);
  }

  @Test
  public void darkScheme_ofBlue() {
    Scheme scheme = Scheme.dark(0xff0000ff);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffBEC2FF);
  }

  @Test
  public void thirdPartyScheme_light() {
    Scheme scheme = Scheme.light(0xff6750A4);
    assertThat(scheme.getPrimary()).isSameColorAs(0xff6750A4);
    assertThat(scheme.getSecondary()).isSameColorAs(0xff625B71);
    assertThat(scheme.getTertiary()).isSameColorAs(0xff7e5260);
    assertThat(scheme.getSurface()).isSameColorAs(0xfffffbff);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xff1c1b1e);
  }

  @Test
  public void thirdPartyScheme_dark() {
    Scheme scheme = Scheme.dark(0xff6750A4);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffcfbcff);
    assertThat(scheme.getSecondary()).isSameColorAs(0xffcbc2db);
    assertThat(scheme.getTertiary()).isSameColorAs(0xffefb8c8);
    assertThat(scheme.getSurface()).isSameColorAs(0xff1c1b1e);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xffe6e1e6);
  }

  @Test
  public void lightSchemeFromHighChromaColor() {
    Scheme scheme = Scheme.light(0xfffa2bec);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffab00a2);
    assertThat(scheme.getOnPrimary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getPrimaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getOnPrimaryContainer()).isSameColorAs(0xff390035);
    assertThat(scheme.getSecondary()).isSameColorAs(0xff6e5868);
    assertThat(scheme.getOnSecondary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getSecondaryContainer()).isSameColorAs(0xfff8daee);
    assertThat(scheme.getOnSecondaryContainer()).isSameColorAs(0xff271624);
    assertThat(scheme.getTertiary()).isSameColorAs(0xff815343);
    assertThat(scheme.getOnTertiary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getTertiaryContainer()).isSameColorAs(0xffffdbd0);
    assertThat(scheme.getOnTertiaryContainer()).isSameColorAs(0xff321207);
    assertThat(scheme.getError()).isSameColorAs(0xffba1a1a);
    assertThat(scheme.getOnError()).isSameColorAs(0xffffffff);
    assertThat(scheme.getErrorContainer()).isSameColorAs(0xffffdad6);
    assertThat(scheme.getOnErrorContainer()).isSameColorAs(0xff410002);
    assertThat(scheme.getBackground()).isSameColorAs(0xfffffbff);
    assertThat(scheme.getOnBackground()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getSurface()).isSameColorAs(0xfffffbff);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getSurfaceVariant()).isSameColorAs(0xffeedee7);
    assertThat(scheme.getOnSurfaceVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getOutline()).isSameColorAs(0xff80747b);
    assertThat(scheme.getOutlineVariant()).isSameColorAs(0xffd2c2cb);
    assertThat(scheme.getShadow()).isSameColorAs(0xff000000);
    assertThat(scheme.getScrim()).isSameColorAs(0xff000000);
    assertThat(scheme.getInverseSurface()).isSameColorAs(0xff342f32);
    assertThat(scheme.getInverseOnSurface()).isSameColorAs(0xfff8eef2);
    assertThat(scheme.getInversePrimary()).isSameColorAs(0xffffabee);
  }

  @Test
  public void darkSchemeFromHighChromaColor() {
    Scheme scheme = Scheme.dark(0xfffa2bec);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffffabee);
    assertThat(scheme.getOnPrimary()).isSameColorAs(0xff5c0057);
    assertThat(scheme.getPrimaryContainer()).isSameColorAs(0xff83007b);
    assertThat(scheme.getOnPrimaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getSecondary()).isSameColorAs(0xffdbbed1);
    assertThat(scheme.getOnSecondary()).isSameColorAs(0xff3e2a39);
    assertThat(scheme.getSecondaryContainer()).isSameColorAs(0xff554050);
    assertThat(scheme.getOnSecondaryContainer()).isSameColorAs(0xfff8daee);
    assertThat(scheme.getTertiary()).isSameColorAs(0xfff5b9a5);
    assertThat(scheme.getOnTertiary()).isSameColorAs(0xff4c2619);
    assertThat(scheme.getTertiaryContainer()).isSameColorAs(0xff663c2d);
    assertThat(scheme.getOnTertiaryContainer()).isSameColorAs(0xffffdbd0);
    assertThat(scheme.getError()).isSameColorAs(0xffffb4ab);
    assertThat(scheme.getOnError()).isSameColorAs(0xff690005);
    assertThat(scheme.getErrorContainer()).isSameColorAs(0xff93000a);
    assertThat(scheme.getOnErrorContainer()).isSameColorAs(0xffffb4ab);
    assertThat(scheme.getBackground()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getOnBackground()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getSurface()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getSurfaceVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getOnSurfaceVariant()).isSameColorAs(0xffd2c2cb);
    assertThat(scheme.getOutline()).isSameColorAs(0xff9a8d95);
    assertThat(scheme.getOutlineVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getShadow()).isSameColorAs(0xff000000);
    assertThat(scheme.getScrim()).isSameColorAs(0xff000000);
    assertThat(scheme.getInverseSurface()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getInverseOnSurface()).isSameColorAs(0xff342f32);
    assertThat(scheme.getInversePrimary()).isSameColorAs(0xffab00a2);
  }

  @Test
  public void lightContentSchemeFromHighChromaColor() {
    Scheme scheme = Scheme.lightContent(0xfffa2bec);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffab00a2);
    assertThat(scheme.getOnPrimary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getPrimaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getOnPrimaryContainer()).isSameColorAs(0xff390035);
    assertThat(scheme.getSecondary()).isSameColorAs(0xff7f4e75);
    assertThat(scheme.getOnSecondary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getSecondaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getOnSecondaryContainer()).isSameColorAs(0xff330b2f);
    assertThat(scheme.getTertiary()).isSameColorAs(0xff9c4323);
    assertThat(scheme.getOnTertiary()).isSameColorAs(0xffffffff);
    assertThat(scheme.getTertiaryContainer()).isSameColorAs(0xffffdbd0);
    assertThat(scheme.getOnTertiaryContainer()).isSameColorAs(0xff390c00);
    assertThat(scheme.getError()).isSameColorAs(0xffba1a1a);
    assertThat(scheme.getOnError()).isSameColorAs(0xffffffff);
    assertThat(scheme.getErrorContainer()).isSameColorAs(0xffffdad6);
    assertThat(scheme.getOnErrorContainer()).isSameColorAs(0xff410002);
    assertThat(scheme.getBackground()).isSameColorAs(0xfffffbff);
    assertThat(scheme.getOnBackground()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getSurface()).isSameColorAs(0xfffffbff);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getSurfaceVariant()).isSameColorAs(0xffeedee7);
    assertThat(scheme.getOnSurfaceVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getOutline()).isSameColorAs(0xff80747b);
    assertThat(scheme.getOutlineVariant()).isSameColorAs(0xffd2c2cb);
    assertThat(scheme.getShadow()).isSameColorAs(0xff000000);
    assertThat(scheme.getScrim()).isSameColorAs(0xff000000);
    assertThat(scheme.getInverseSurface()).isSameColorAs(0xff342f32);
    assertThat(scheme.getInverseOnSurface()).isSameColorAs(0xfff8eef2);
    assertThat(scheme.getInversePrimary()).isSameColorAs(0xffffabee);
  }

  @Test
  public void darkContentSchemeFromHighChromaColor() {
    Scheme scheme = Scheme.darkContent(0xfffa2bec);
    assertThat(scheme.getPrimary()).isSameColorAs(0xffffabee);
    assertThat(scheme.getOnPrimary()).isSameColorAs(0xff5c0057);
    assertThat(scheme.getPrimaryContainer()).isSameColorAs(0xff83007b);
    assertThat(scheme.getOnPrimaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getSecondary()).isSameColorAs(0xfff0b4e1);
    assertThat(scheme.getOnSecondary()).isSameColorAs(0xff4b2145);
    assertThat(scheme.getSecondaryContainer()).isSameColorAs(0xff64375c);
    assertThat(scheme.getOnSecondaryContainer()).isSameColorAs(0xffffd7f3);
    assertThat(scheme.getTertiary()).isSameColorAs(0xffffb59c);
    assertThat(scheme.getOnTertiary()).isSameColorAs(0xff5c1900);
    assertThat(scheme.getTertiaryContainer()).isSameColorAs(0xff7d2c0d);
    assertThat(scheme.getOnTertiaryContainer()).isSameColorAs(0xffffdbd0);
    assertThat(scheme.getError()).isSameColorAs(0xffffb4ab);
    assertThat(scheme.getOnError()).isSameColorAs(0xff690005);
    assertThat(scheme.getErrorContainer()).isSameColorAs(0xff93000a);
    assertThat(scheme.getOnErrorContainer()).isSameColorAs(0xffffb4ab);
    assertThat(scheme.getBackground()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getOnBackground()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getSurface()).isSameColorAs(0xff1f1a1d);
    assertThat(scheme.getOnSurface()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getSurfaceVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getOnSurfaceVariant()).isSameColorAs(0xffd2c2cb);
    assertThat(scheme.getOutline()).isSameColorAs(0xff9a8d95);
    assertThat(scheme.getOutlineVariant()).isSameColorAs(0xff4e444b);
    assertThat(scheme.getShadow()).isSameColorAs(0xff000000);
    assertThat(scheme.getScrim()).isSameColorAs(0xff000000);
    assertThat(scheme.getInverseSurface()).isSameColorAs(0xffeae0e4);
    assertThat(scheme.getInverseOnSurface()).isSameColorAs(0xff342f32);
    assertThat(scheme.getInversePrimary()).isSameColorAs(0xffab00a2);
  }
}
