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
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public final class DynamicColorTest {

  private final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();

  @Test
  public void fromArgbNoBackground_doesntChangeForContrast() {
    final int blueArgb = 0xff0000ff;
    final DynamicColor dynamicColor = DynamicColor.fromArgb("blue", blueArgb);

    final SchemeTonalSpot standardContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, 0.0);
    assertThat(dynamicColor.getArgb(standardContrast)).isSameColorAs(blueArgb);

    final SchemeTonalSpot minContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, -1.0);
    assertThat(dynamicColor.getArgb(minContrast)).isSameColorAs(blueArgb);

    final SchemeTonalSpot maxContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, 1.0);
    assertThat(dynamicColor.getArgb(maxContrast)).isSameColorAs(blueArgb);
  }

  @Test
  public void dynamicColor_withOpacity() {
    final DynamicColor dynamicColor =
        new DynamicColor(
            /* name= */ "control",
            /* palette= */ (s) -> s.primaryPalette,
            /* tone= */ (s) -> s.isDark ? 100.0 : 0.0,
            /* isBackground= */ false,
            /* background= */ null,
            /* secondBackground= */ null,
            /* contrastCurve= */ null,
            /* toneDeltaPair= */ null,
            /* opacity= */ s -> s.isDark ? 0.20 : 0.12);

    final SchemeTonalSpot lightScheme = new SchemeTonalSpot(Hct.fromInt(0xff4285f4), false, 0.0);
    assertThat(dynamicColor.getArgb(lightScheme)).isSameColorAs(0x1f000000);

    final SchemeTonalSpot darkScheme = new SchemeTonalSpot(Hct.fromInt(0xff4285f4), true, 0.0);
    assertThat(dynamicColor.getArgb(darkScheme)).isSameColorAs(0x33ffffff);
  }

  @Test
  public void respectsContrast() {
    final Hct[] seedColors =
        new Hct[] {
          Hct.fromInt(0xffff0000),
          Hct.fromInt(0xffffff00),
          Hct.fromInt(0xff00ff00),
          Hct.fromInt(0xff0000ff)
        };

    final double[] contrastLevels = {-1.0, -0.5, 0.0, 0.5, 1.0};

    for (Hct seedColor : seedColors) {
      for (double contrastLevel : contrastLevels) {
        for (boolean isDark : new boolean[] {false, true}) {
          final DynamicScheme[] schemes =
              new DynamicScheme[] {
                new SchemeContent(seedColor, isDark, contrastLevel),
                new SchemeMonochrome(seedColor, isDark, contrastLevel),
                new SchemeTonalSpot(seedColor, isDark, contrastLevel),
                new SchemeFidelity(seedColor, isDark, contrastLevel)
              };
          for (final DynamicScheme scheme : schemes) {
            assertTrue(
                pairSatisfiesContrast(scheme, dynamicColors.onPrimary(), dynamicColors.primary()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onPrimaryContainer(), dynamicColors.primaryContainer()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onSecondary(), dynamicColors.secondary()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme,
                    dynamicColors.onSecondaryContainer(),
                    dynamicColors.secondaryContainer()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onTertiary(), dynamicColors.tertiary()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme,
                    dynamicColors.onTertiaryContainer(),
                    dynamicColors.tertiaryContainer()));
            assertTrue(
                pairSatisfiesContrast(scheme, dynamicColors.onError(), dynamicColors.error()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onErrorContainer(), dynamicColors.errorContainer()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onBackground(), dynamicColors.background()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onSurfaceVariant(), dynamicColors.surfaceBright()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.onSurfaceVariant(), dynamicColors.surfaceDim()));
            assertTrue(
                pairSatisfiesContrast(
                    scheme, dynamicColors.inverseOnSurface(), dynamicColors.inverseSurface()));
          }
        }
      }
    }
  }

  @Test
  public void valuesAreCorrect() {
    // Checks that the values of certain dynamic colors match Dart results.
    assertThat(
            dynamicColors
                .onPrimaryContainer()
                .getArgb(new SchemeFidelity(Hct.fromInt(0xFFFF0000), false, 0.5)))
        .isSameColorAs(0xFFFFFFFF);
    assertThat(
            dynamicColors
                .onSecondaryContainer()
                .getArgb(new SchemeContent(Hct.fromInt(0xFF0000FF), false, 0.5)))
        .isSameColorAs(0xFFFFFFFF);
    assertThat(
            dynamicColors
                .onTertiaryContainer()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFFFF00), true, -0.5)))
        .isSameColorAs(0xFF959b1a);
    assertThat(
            dynamicColors
                .inverseSurface()
                .getArgb(new SchemeContent(Hct.fromInt(0xFF0000FF), false, 0.0)))
        .isSameColorAs(0xFF2F2F3B);
    assertThat(
            dynamicColors
                .inversePrimary()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFF0000), false, -0.5)))
        .isSameColorAs(0xffff422f);
    assertThat(
            dynamicColors
                .outlineVariant()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFFFF00), true, 0.0)))
        .isSameColorAs(0xFF484831);
  }

  @Test
  public void fidelityValuesAreCorrect() {
    final MaterialDynamicColors fidelityColors = new MaterialDynamicColors(true);

    assertThat(
            dynamicColors
                .onPrimaryContainer()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xffff0000), false, 0.5)))
        .isSameColorAs(0xffffffff);
    assertThat(
            fidelityColors
                .onPrimaryContainer()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xffff0000), false, 0.5)))
        .isSameColorAs(0xffffffff);

    assertThat(
            dynamicColors
                .onSecondaryContainer()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.5)))
        .isSameColorAs(0xffffffff);
    assertThat(
            fidelityColors
                .onSecondaryContainer()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xff0000ff), false, 0.5)))
        .isSameColorAs(0xffffffff);

    assertThat(
            dynamicColors
                .onTertiaryContainer()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xffffff00), true, 0.5)))
        .isSameColorAs(0xff000000);
    assertThat(
            fidelityColors
                .onTertiaryContainer()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xffffff00), true, 0.5)))
        .isSameColorAs(0xff394e1d);

    assertThat(
            dynamicColors
                .inverseSurface()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0)))
        .isSameColorAs(0xff303036);
    assertThat(
            fidelityColors
                .inverseSurface()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xff0000ff), false, 0.0)))
        .isSameColorAs(0xff303036);

    assertThat(
            dynamicColors
                .inversePrimary()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xffff0000), false, 0.5)))
        .isSameColorAs(0xffffb4a8);
    assertThat(
            fidelityColors
                .inversePrimary()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xffff0000), false, 0.5)))
        .isSameColorAs(0xffffb4a8);

    assertThat(
            dynamicColors
                .outlineVariant()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xffffff00), true, 0.0)))
        .isSameColorAs(0xff444937);
    assertThat(
            fidelityColors
                .outlineVariant()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xffffff00), true, 0.0)))
        .isSameColorAs(0xff444937);

    assertThat(
            dynamicColors
                .secondaryContainer()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff554050);
    assertThat(
            fidelityColors
                .secondaryContainer()
                .getArgb(new SchemeTonalSpot(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff554050);

    assertThat(
            dynamicColors
                .secondaryContainer()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff603b4f);
    assertThat(
            fidelityColors
                .secondaryContainer()
                .getArgb(new SchemeVibrant(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff603b4f);

    assertThat(
            dynamicColors
                .secondaryContainer()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff663b38);
    assertThat(
            fidelityColors
                .secondaryContainer()
                .getArgb(new SchemeExpressive(Hct.fromInt(0xfffa2bec), true, 0.0)))
        .isSameColorAs(0xff693d3a);
  }

  private boolean pairSatisfiesContrast(DynamicScheme scheme, DynamicColor fg, DynamicColor bg) {
    double fgTone = fg.getHct(scheme).getTone();
    double bgTone = bg.getHct(scheme).getTone();
    double minimumRequirement = scheme.contrastLevel >= 0.0 ? 4.5 : 3.0;
    return Contrast.ratioOfTones(fgTone, bgTone) >= minimumRequirement;
  }
}
