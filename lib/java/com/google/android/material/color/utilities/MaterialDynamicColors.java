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

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Named colors, otherwise known as tokens, or roles, in the Material Design system.
 *
 * <p> Prevent lint for Function.apply not being available on Android before API level 14 (4.0.1).
 * "AndroidJdkLibsChecker" for Function, "NewApi" for Function.apply().
 * A java_library Bazel rule with an Android constraint cannot skip these warnings without this
 * annotation; another solution would be to create an android_library rule and supply
 * AndroidManifest with an SDK set higher than 14.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
@SuppressWarnings({"AndroidJdkLibsChecker", "NewApi"})
public final class MaterialDynamicColors {
  private static final double CONTAINER_ACCENT_TONE_DELTA = 15.0;

  public MaterialDynamicColors() {}

  @NonNull
  public DynamicColor highestSurface(@NonNull DynamicScheme s) {
    return s.isDark ? surfaceBright() : surfaceDim();
  }

  @NonNull
  public DynamicColor background() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 98.0);
  }

  @NonNull
  public DynamicColor onBackground() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> background());
  }

  @NonNull
  public DynamicColor surface() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 98.0);
  }

  @NonNull
  public DynamicColor inverseSurface() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 90.0 : 20.0);
  }

  @NonNull
  public DynamicColor surfaceBright() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 24.0 : 98.0);
  }

  @NonNull
  public DynamicColor surfaceDim() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 6.0 : 87.0);
  }

  @NonNull
  public DynamicColor surfaceContainerLowest() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 4.0 : 100.0);
  }

  @NonNull
  public DynamicColor surfaceContainerLow() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 96.0);
  }

  @NonNull
  public DynamicColor surfaceContainer() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 12.0 : 94.0);
  }

  @NonNull
  public DynamicColor surfaceContainerHigh() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 17.0 : 92.0);
  }

  @NonNull
  public DynamicColor surfaceContainerHighest() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 22.0 : 90.0);
  }

  @NonNull
  public DynamicColor onSurface() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralPalette, (s) -> s.isDark ? 90.0 : 10.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor inverseOnSurface() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralPalette, (s) -> s.isDark ? 20.0 : 95.0, (s) -> inverseSurface());
  }

  @NonNull
  public DynamicColor surfaceVariant() {
    return DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 30.0 : 90.0);
  }

  @NonNull
  public DynamicColor onSurfaceVariant() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralVariantPalette, (s) -> s.isDark ? 80.0 : 30.0, (s) -> surfaceVariant());
  }

  @NonNull
  public DynamicColor outline() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralVariantPalette, (s) -> 50.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor outlineVariant() {
    return DynamicColor.fromPalette(
        (s) -> s.neutralVariantPalette, (s) -> s.isDark ? 30.0 : 80.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor primaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isFidelity(s)) {
            return performAlbers(s.sourceColorHct, s);
          }
          if (isMonochrome(s)) {
            return s.isDark ? 85.0 : 25.0;
          }
          return s.isDark ? 30.0 : 90.0;
        },
        this::highestSurface);
  }

  @NonNull
  public DynamicColor onPrimaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isFidelity(s)) {
            return DynamicColor.contrastingTone(primaryContainer().tone.apply(s), 4.5);
          }
          if (isMonochrome(s)) {
            return s.isDark ? 0.0 : 100.0;
          }
          return s.isDark ? 90.0 : 10.0;
        },
        (s) -> primaryContainer(),
        null);
  }

  @NonNull
  public DynamicColor primary() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 100.0 : 0.0;
          }
          return s.isDark ? 80.0 : 40.0;
        },
        this::highestSurface,
        (s) ->
            new ToneDeltaConstraint(
                CONTAINER_ACCENT_TONE_DELTA,
                primaryContainer(),
                s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));
  }

  @NonNull
  public DynamicColor inversePrimary() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette, (s) -> s.isDark ? 40.0 : 80.0, (s) -> inverseSurface());
  }

  @NonNull
  public DynamicColor onPrimary() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 10.0 : 90.0;
          }
          return s.isDark ? 20.0 : 100.0;
        },
        (s) -> primary());
  }

  @NonNull
  public DynamicColor secondaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 30.0 : 85.0;
          }
          final double initialTone = s.isDark ? 30.0 : 90.0;
          if (!isFidelity(s)) {
            return initialTone;
          }
          double answer =
              findDesiredChromaByTone(
                  s.secondaryPalette.getHue(),
                  s.secondaryPalette.getChroma(),
                  initialTone,
                  !s.isDark);
          answer = performAlbers(s.secondaryPalette.getHct(answer), s);
          return answer;
        },
        this::highestSurface);
  }

  @NonNull
  public DynamicColor onSecondaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette,
        (s) -> {
          if (!isFidelity(s)) {
            return s.isDark ? 90.0 : 10.0;
          }
          return DynamicColor.contrastingTone(secondaryContainer().tone.apply(s), 4.5);
        },
        (s) -> secondaryContainer());
  }

  @NonNull
  public DynamicColor secondary() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette,
        (s) -> s.isDark ? 80.0 : 40.0,
        this::highestSurface,
        (s) ->
            new ToneDeltaConstraint(
                CONTAINER_ACCENT_TONE_DELTA,
                secondaryContainer(),
                s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));
  }

  @NonNull
  public DynamicColor onSecondary() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 10.0 : 100.0;
          }
          return s.isDark ? 20.0 : 100.0;
        },
        (s) -> secondary());
  }

  @NonNull
  public DynamicColor tertiaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 60.0 : 49.0;
          }
          if (!isFidelity(s)) {
            return s.isDark ? 30.0 : 90.0;
          }
          final double albersTone =
              performAlbers(s.tertiaryPalette.getHct(s.sourceColorHct.getTone()), s);
          final Hct proposedHct = s.tertiaryPalette.getHct(albersTone);
          return DislikeAnalyzer.fixIfDisliked(proposedHct).getTone();
        },
        this::highestSurface);
  }

  @NonNull
  public DynamicColor onTertiaryContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 0.0 : 100.0;
          }
          if (!isFidelity(s)) {
            return s.isDark ? 90.0 : 10.0;
          }
          return DynamicColor.contrastingTone(tertiaryContainer().tone.apply(s), 4.5);
        },
        (s) -> tertiaryContainer());
  }

  @NonNull
  public DynamicColor tertiary() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 90.0 : 25.0;
          }
          return s.isDark ? 80.0 : 40.0;
        },
        this::highestSurface,
        (s) ->
            new ToneDeltaConstraint(
                CONTAINER_ACCENT_TONE_DELTA,
                tertiaryContainer(),
                s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));
  }

  @NonNull
  public DynamicColor onTertiary() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 10.0 : 90.0;
          }
          return s.isDark ? 20.0 : 100.0;
        },
        (s) -> tertiary());
  }

  @NonNull
  public DynamicColor errorContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.errorPalette, (s) -> s.isDark ? 30.0 : 90.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor onErrorContainer() {
    return DynamicColor.fromPalette(
        (s) -> s.errorPalette, (s) -> s.isDark ? 90.0 : 10.0, (s) -> errorContainer());
  }

  @NonNull
  public DynamicColor error() {
    return DynamicColor.fromPalette(
        (s) -> s.errorPalette,
        (s) -> s.isDark ? 80.0 : 40.0,
        this::highestSurface,
        (s) ->
            new ToneDeltaConstraint(
                CONTAINER_ACCENT_TONE_DELTA,
                errorContainer(),
                s.isDark ? TonePolarity.DARKER : TonePolarity.LIGHTER));
  }

  @NonNull
  public DynamicColor onError() {
    return DynamicColor.fromPalette(
        (s) -> s.errorPalette, (s) -> s.isDark ? 20.0 : 100.0, (s) -> error());
  }

  @NonNull
  public DynamicColor primaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 100.0 : 10.0;
          }
          return 90.0;
        },
        this::highestSurface);
  }

  @NonNull
  public DynamicColor primaryFixedDim() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 90.0 : 20.0;
          }
          return 80.0;
        },
        this::highestSurface);
  }

  @NonNull
  public DynamicColor onPrimaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 10.0 : 90.0;
          }
          return 10.0;
        },
        (s) -> primaryFixedDim());
  }

  @NonNull
  public DynamicColor onPrimaryFixedVariant() {
    return DynamicColor.fromPalette(
        (s) -> s.primaryPalette,
        (s) -> {
          if (isMonochrome(s)) {
            return s.isDark ? 30.0 : 70.0;
          }
          return 30.0;
        },
        (s) -> primaryFixedDim());
  }

  @NonNull
  public DynamicColor secondaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette, (s) -> isMonochrome(s) ? 80.0 : 90.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor secondaryFixedDim() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette, (s) -> isMonochrome(s) ? 70.0 : 80.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor onSecondaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette, (s) -> 10.0, (s) -> secondaryFixedDim());
  }

  @NonNull
  public DynamicColor onSecondaryFixedVariant() {
    return DynamicColor.fromPalette(
        (s) -> s.secondaryPalette,
        (s) -> isMonochrome(s) ? 25.0 : 30.0,
        (s) -> secondaryFixedDim());
  }

  @NonNull
  public DynamicColor tertiaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette, (s) -> isMonochrome(s) ? 40.0 : 90.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor tertiaryFixedDim() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette, (s) -> isMonochrome(s) ? 30.0 : 80.0, this::highestSurface);
  }

  @NonNull
  public DynamicColor onTertiaryFixed() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette, (s) -> isMonochrome(s) ? 90.0 : 10.0, (s) -> tertiaryFixedDim());
  }

  @NonNull
  public DynamicColor onTertiaryFixedVariant() {
    return DynamicColor.fromPalette(
        (s) -> s.tertiaryPalette, (s) -> isMonochrome(s) ? 70.0 : 30.0, (s) -> tertiaryFixedDim());
  }

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
  @NonNull
  public DynamicColor controlActivated() {
    return DynamicColor.fromPalette((s) -> s.primaryPalette, (s) -> s.isDark ? 30.0 : 90.0, null);
  }

  // colorControlNormal documented as textColorSecondary in M3.
  // In Material, textColorSecondary points to onSurfaceVariant in the non-disabled state,
  // which is Neutral Variant T30/80 in light/dark.
  @NonNull
  public DynamicColor controlNormal() {
    return DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 80.0 : 30.0);
  }

  // colorControlHighlight documented, in M3:
  // Light mode: #1f000000 dark mode: #33ffffff.
  // These are black and white with some alpha.
  // 1F hex = 31 decimal; 31 / 255 = 12% alpha.
  // 33 hex = 51 decimal; 51 / 255 = 20% alpha.
  // DynamicColors do not support alpha currently, and _may_ not need it for this use case,
  // depending on how MDC resolved alpha for the other cases.
  // Returning black in dark mode, white in light mode.
  @NonNull
  public DynamicColor controlHighlight() {
    return new DynamicColor(
        s -> 0.0,
        s -> 0.0,
        s -> s.isDark ? 100.0 : 0.0,
        s -> s.isDark ? 0.20 : 0.12,
        null,
        scheme ->
            DynamicColor.toneMinContrastDefault((s) -> s.isDark ? 100.0 : 0.0, null, scheme, null),
        scheme ->
            DynamicColor.toneMaxContrastDefault((s) -> s.isDark ? 100.0 : 0.0, null, scheme, null),
        null);
  }

  // textColorPrimaryInverse documented, in M3, documented as N10/N90.
  @NonNull
  public DynamicColor textPrimaryInverse() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);
  }

  // textColorSecondaryInverse and textColorTertiaryInverse both documented, in M3, as NV30/NV80
  @NonNull
  public DynamicColor textSecondaryAndTertiaryInverse() {
    return DynamicColor.fromPalette((s) -> s.neutralVariantPalette, (s) -> s.isDark ? 30.0 : 80.0);
  }

  // textColorPrimaryInverseDisableOnly documented, in M3, as N10/N90
  @NonNull
  public DynamicColor textPrimaryInverseDisableOnly() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);
  }

  // textColorSecondaryInverse and textColorTertiaryInverse in disabled state both documented,
  // in M3, as N10/N90
  @NonNull
  public DynamicColor textSecondaryAndTertiaryInverseDisabled() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);
  }

  // textColorHintInverse documented, in M3, as N10/N90
  @NonNull
  public DynamicColor textHintInverse() {
    return DynamicColor.fromPalette((s) -> s.neutralPalette, (s) -> s.isDark ? 10.0 : 90.0);
  }

  private static ViewingConditions viewingConditionsForAlbers(DynamicScheme scheme) {
    return ViewingConditions.defaultWithBackgroundLstar(scheme.isDark ? 30.0 : 80.0);
  }

  private static boolean isFidelity(DynamicScheme scheme) {
    return scheme.variant == Variant.FIDELITY || scheme.variant == Variant.CONTENT;
  }

  private static boolean isMonochrome(DynamicScheme scheme) {
    return scheme.variant == Variant.MONOCHROME;
  }

  static double findDesiredChromaByTone(
      double hue, double chroma, double tone, boolean byDecreasingTone) {
    double answer = tone;

    Hct closestToChroma = Hct.from(hue, chroma, tone);
    if (closestToChroma.getChroma() < chroma) {
      double chromaPeak = closestToChroma.getChroma();
      while (closestToChroma.getChroma() < chroma) {
        answer += byDecreasingTone ? -1.0 : 1.0;
        Hct potentialSolution = Hct.from(hue, chroma, answer);
        if (chromaPeak > potentialSolution.getChroma()) {
          break;
        }
        if (Math.abs(potentialSolution.getChroma() - chroma) < 0.4) {
          break;
        }

        double potentialDelta = Math.abs(potentialSolution.getChroma() - chroma);
        double currentDelta = Math.abs(closestToChroma.getChroma() - chroma);
        if (potentialDelta < currentDelta) {
          closestToChroma = potentialSolution;
        }
        chromaPeak = Math.max(chromaPeak, potentialSolution.getChroma());
      }
    }

    return answer;
  }

  static double performAlbers(Hct prealbers, DynamicScheme scheme) {
    final Hct albersd = prealbers.inViewingConditions(viewingConditionsForAlbers(scheme));
    if (DynamicColor.tonePrefersLightForeground(prealbers.getTone())
        && !DynamicColor.toneAllowsLightForeground(albersd.getTone())) {
      return DynamicColor.enableLightForeground(prealbers.getTone());
    } else {
      return DynamicColor.enableLightForeground(albersd.getTone());
    }
  }
}
