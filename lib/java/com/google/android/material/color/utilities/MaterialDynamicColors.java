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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * Named colors, otherwise known as tokens, or roles, in the Material Design system.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class MaterialDynamicColors {
  private static final double CONTAINER_ACCENT_TONE_DELTA = 15.0;

  private MaterialDynamicColors() {}

  public static DynamicColor highestSurface(DynamicScheme s) {
    return s.isDark ? surfaceBright : surfaceDim;
  }

  public static final DynamicColor background =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 98.0);

  public static final DynamicColor onBackground =
      DynamicColor.fromPalette(
          (s) -> s.neutralPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> background);

  public static final DynamicColor surface =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 98.0);

  public static final DynamicColor surfaceBright =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 24.0 : 98.0);

  public static final DynamicColor surfaceDim =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 87.0);

  public static final DynamicColor surfaceSub2 =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 4.0 : 100.0);

  public static final DynamicColor surfaceSub1 =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 96.0);

  public static final DynamicColor surfaceContainer =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 12.0 : 94.0);

  public static final DynamicColor surfaceAdd1 =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 17.0 : 92.0);

  public static final DynamicColor surfaceAdd2 =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 22.0 : 90.0);

  public static final DynamicColor onSurface =
      DynamicColor.fromPalette(
          (s) -> s.neutralPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> highestSurface(s));

  public static final DynamicColor surfaceVariant =
      DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 30.0 : 90.0);

  public static final DynamicColor onSurfaceVariant =
      DynamicColor.fromPalette(
          (s) -> s.neutralVariantPalette, (s) -> s.isDark ? 80.0 : 30.0, (s) -> surfaceVariant);

  public static final DynamicColor outline =
      DynamicColor.fromPalette(
          (s) -> s.neutralVariantPalette, (s) -> 50.0, (s) -> highestSurface(s));

  public static final DynamicColor primaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.primaryPalette, (s) -> s.isDark ? 30.0 : 90.0, (s) -> highestSurface(s));

  public static final DynamicColor onPrimaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.primaryPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> primaryContainer, null);

  public static final DynamicColor primary =
      DynamicColor.fromPalette(
          (s) -> s.primaryPalette,
          (s) -> s.isDark ? 80.0 : 40.0,
          (s) -> highestSurface(s),
          (s) ->
              new ToneDeltaConstraint(
                  CONTAINER_ACCENT_TONE_DELTA,
                  primaryContainer,
                  s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));

  public static final DynamicColor onPrimary =
      DynamicColor.fromPalette(
          (s) -> s.primaryPalette, (s) -> s.isDark ? 20.0 : 100.0, (s) -> primary);

  public static final DynamicColor secondaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.secondaryPalette, (s) -> s.isDark ? 30.0 : 90.0, (s) -> highestSurface(s));

  public static final DynamicColor onSecondaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.secondaryPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> secondaryContainer);

  public static final DynamicColor secondary =
      DynamicColor.fromPalette(
          (s) -> s.secondaryPalette,
          (s) -> s.isDark ? 80.0 : 40.0,
          (s) -> highestSurface(s),
          (s) ->
              new ToneDeltaConstraint(
                  CONTAINER_ACCENT_TONE_DELTA,
                  secondaryContainer,
                  s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));

  public static final DynamicColor onSecondary =
      DynamicColor.fromPalette(
          (s) -> s.secondaryPalette, (s) -> s.isDark ? 20.0 : 100.0, (s) -> secondary);

  public static final DynamicColor tertiaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.tertiaryPalette, (s) -> s.isDark ? 30.0 : 90.0, (s) -> highestSurface(s));

  public static final DynamicColor onTertiaryContainer =
      DynamicColor.fromPalette(
          (s) -> s.tertiaryPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> tertiaryContainer);

  public static final DynamicColor tertiary =
      DynamicColor.fromPalette(
          (s) -> s.tertiaryPalette,
          (s) -> s.isDark ? 80.0 : 40.0,
          (s) -> highestSurface(s),
          (s) ->
              new ToneDeltaConstraint(
                  CONTAINER_ACCENT_TONE_DELTA,
                  tertiaryContainer,
                  s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));

  public static final DynamicColor onTertiary =
      DynamicColor.fromPalette(
          (s) -> s.tertiaryPalette, (s) -> s.isDark ? 20.0 : 100.0, (s) -> tertiary);

  public static final DynamicColor errorContainer =
      DynamicColor.fromPalette(
          (s) -> s.errorPalette, (s) -> s.isDark ? 30.0 : 90.0, (s) -> highestSurface(s));

  public static final DynamicColor onErrorContainer =
      DynamicColor.fromPalette(
          (s) -> s.errorPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> errorContainer);

  public static final DynamicColor error =
      DynamicColor.fromPalette(
          (s) -> s.errorPalette,
          (s) -> s.isDark ? 80.0 : 40.0,
          (s) -> highestSurface(s),
          (s) ->
              new ToneDeltaConstraint(
                  CONTAINER_ACCENT_TONE_DELTA,
                  errorContainer,
                  s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));

  public static final DynamicColor onError =
      DynamicColor.fromPalette((s) -> s.errorPalette, (s) -> s.isDark ? 20.0 : 100.0, (s) -> error);

  public static final DynamicColor primaryFixed =
      DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> 90.0, (s) -> highestSurface(s));

  public static final DynamicColor primaryFixedDarker =
      DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> 80.0, (s) -> highestSurface(s));

  public static final DynamicColor onPrimaryFixed =
      DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> 10.0, (s) -> primaryFixedDarker);

  public static final DynamicColor onPrimaryFixedVariant =
      DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> 30.0, (s) -> primaryFixedDarker);

  public static final DynamicColor secondaryFixed =
      DynamicColor.fromPalette((s) -> s.secondaryPalette, (s) -> 90.0, (s) -> highestSurface(s));

  public static final DynamicColor secondaryFixedDarker =
      DynamicColor.fromPalette((s) -> s.secondaryPalette, (s) -> 80.0, (s) -> highestSurface(s));

  public static final DynamicColor onSecondaryFixed =
      DynamicColor.fromPalette((s) -> s.secondaryPalette, (s) -> 10.0, (s) -> secondaryFixedDarker);

  public static final DynamicColor onSecondaryFixedVariant =
      DynamicColor.fromPalette((s) -> s.secondaryPalette, (s) -> 30.0, (s) -> secondaryFixedDarker);

  public static final DynamicColor tertiaryFixed =
      DynamicColor.fromPalette((s) -> s.tertiaryPalette, (s) -> 90.0, (s) -> highestSurface(s));

  public static final DynamicColor tertiaryFixedDarker =
      DynamicColor.fromPalette((s) -> s.tertiaryPalette, (s) -> 80.0, (s) -> highestSurface(s));

  public static final DynamicColor onTertiaryFixed =
      DynamicColor.fromPalette((s) -> s.tertiaryPalette, (s) -> 10.0, (s) -> tertiaryFixedDarker);

  public static final DynamicColor onTertiaryFixedVariant =
      DynamicColor.fromPalette((s) -> s.tertiaryPalette, (s) -> 30.0, (s) -> tertiaryFixedDarker);

  /**
   * These colors were present in Android framework before Android U, and used by MDC controls. They
   * should be avoided, if possible. It's unclear if they're used on multiple backgrounds, and if
   * they are, they can't be adjusted for contrast.* For now, they will be set with no background,
   * and those won't adjust for contrast, avoiding issues.
   *
   * <p>* For example, if the same color is on a white background _and_ black background, there's no
   * way to increase contrast with either without losing contrast with the other.
   */
  // colorControlActivated documented as colorAccent in M3.
  // colorAccent documented as colorSecondary in M3 and colorPrimary internally.
  // Android used Material's Container as Primary/Secondary/Tertiary at launch.
  // Therefore, this is a duplicated version of Primary Container.
  public static final DynamicColor controlActivated =
      DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> s.isDark ? 30.0 : 90.0, null);

  // colorControlNormal documented as textColorSecondary in M3.
  // In Material, textColorSecondary points to onSurfaceVariant in the non-disabled state,
  // which is Neutral Variant T30/80 in light/dark.
  public static final DynamicColor controlNormal =
      DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 80.0 : 30.0);

  // colorControlHighlight documented, in M3:
  // Light mode: #1f000000 dark mode: #33ffffff.
  // These are black and white with some alpha.
  // 1F hex = 31 decimal; 31 / 255 = 12% alpha.
  // 33 hex = 51 decimal; 51 / 255 = 20% alpha.
  // DynamicColors do not support alpha currently, and _may_ not need it for this use case,
  // depending on how MDC resolved alpha for the other cases.
  // Returning black in dark mode, white in light mode.
  public static final DynamicColor controlHighlight =
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

  // textColorPrimaryInverse documented, in M3, documented as N10/N90.
  public static final DynamicColor textPrimaryInverse =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);

  // textColorSecondaryInverse and textColorTertiaryInverse both documented, in M3, as NV30/NV80
  public static final DynamicColor textSecondaryAndTertiaryInverse =
      DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 30.0 : 80.0);

  // textColorPrimaryInverseDisableOnly documented, in M3, as N10/N90
  public static final DynamicColor textPrimaryInverseDisableOnly =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);

  // textColorSecondaryInverse and textColorTertiaryInverse in disabled state both documented,
  // in M3, as N10/N90
  public static final DynamicColor textSecondaryAndTertiaryInverseDisabled =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);

  // textColorHintInverse documented, in M3, as N10/N90
  public static final DynamicColor textHintInverse =
      DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);
}
