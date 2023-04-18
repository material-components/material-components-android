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
    final DynamicColor dynamicColor = DynamicColor.fromArgb(blueArgb);

    final SchemeTonalSpot standardContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, 0.0);
    assertThat(dynamicColor.getArgb(standardContrast)).isSameColorAs(blueArgb);

    final SchemeTonalSpot minContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, -1.0);
    assertThat(dynamicColor.getArgb(minContrast)).isSameColorAs(blueArgb);

    final SchemeTonalSpot maxContrast = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, 1.0);
    assertThat(dynamicColor.getArgb(maxContrast)).isSameColorAs(blueArgb);
  }

  @Test
  public void toneDeltaConstraintNoPreference_evaluatesCorrectly() {
    final int blueArgb = 0xff0000ff;
    final int redArgb = 0xffff0000;
    final DynamicColor otherDynamicColor = DynamicColor.fromArgb(redArgb);
    final DynamicColor dynamicColor =
        DynamicColor.fromArgb(
            blueArgb,
            (s) -> 30.0,
            null,
            (s) -> new ToneDeltaConstraint(30, otherDynamicColor, TonePolarity.NO_PREFERENCE));
    final SchemeTonalSpot scheme = new SchemeTonalSpot(Hct.fromInt(blueArgb), false, 0.0);
    assertThat(dynamicColor.getArgb(scheme)).isSameColorAs(0xff0000ef);
  }

  @Test
  public void dynamicColor_withOpacity() {
    final DynamicColor dynamicColor =
        new DynamicColor(
            s -> 0.0,
            s -> 0.0,
            s -> s.isDark ? 100.0 : 0.0,
            s -> s.isDark ? 0.20 : 0.12,
            null,
            scheme ->
                DynamicColor.toneMinContrastDefault(
                    (s) -> s.isDark ? 100.0 : 0.0, null, scheme, null),
            scheme ->
                DynamicColor.toneMaxContrastDefault(
                    (s) -> s.isDark ? 100.0 : 0.0, null, scheme, null),
            null);
    final SchemeTonalSpot lightScheme = new SchemeTonalSpot(Hct.fromInt(0xff4285f4), false, 0.0);
    assertThat(dynamicColor.getArgb(lightScheme)).isSameColorAs(0x1f000000);

    final SchemeTonalSpot darkScheme = new SchemeTonalSpot(Hct.fromInt(0xff4285f4), true, 0.0);
    assertThat(dynamicColor.getArgb(darkScheme)).isSameColorAs(0x33ffffff);
  }

  @Test
  public void respectsContrast() {
    final Hct[] seedColors =
        new Hct[] {
            Hct.fromInt(0xFFFF0000),
            Hct.fromInt(0xFFFFFF00),
            Hct.fromInt(0xFF00FF00),
            Hct.fromInt(0xFF0000FF)
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
                    scheme, dynamicColors.onSurfaceVariant(), dynamicColors.surfaceVariant()));
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
        .isSameColorAs(0xFFFFE5E1);
    assertThat(
            dynamicColors
                .onSecondaryContainer()
                .getArgb(new SchemeContent(Hct.fromInt(0xFF0000FF), false, 0.5)))
        .isSameColorAs(0xFFFFFCFF);
    assertThat(
            dynamicColors
                .onTertiaryContainer()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFFFF00), true, -0.5)))
        .isSameColorAs(0xFF616600);
    assertThat(
            dynamicColors
                .inverseSurface()
                .getArgb(new SchemeContent(Hct.fromInt(0xFF0000FF), false, 0.0)))
        .isSameColorAs(0xFF2F2F3B);
    assertThat(
            dynamicColors
                .inversePrimary()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFF0000), false, -0.5)))
        .isSameColorAs(0xFFFF907F);
    assertThat(
            dynamicColors
                .outlineVariant()
                .getArgb(new SchemeContent(Hct.fromInt(0xFFFFFF00), true, 0.0)))
        .isSameColorAs(0xFF484831);
  }

  private boolean pairSatisfiesContrast(DynamicScheme scheme, DynamicColor fg, DynamicColor bg) {
    double fgTone = fg.getHct(scheme).getTone();
    double bgTone = bg.getHct(scheme).getTone();
    double minimumRequirement = scheme.contrastLevel >= 0.0 ? 4.5 : 3.0;
    return Contrast.ratioOfTones(fgTone, bgTone) >= minimumRequirement;
  }
}
