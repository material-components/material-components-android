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
import static java.lang.Math.max;
import static java.lang.Math.min;

import androidx.annotation.RestrictTo;
import java.util.HashMap;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * A color that adjusts itself based on UI state, represented by DynamicScheme.
 *
 * <p>This color automatically adjusts to accommodate a desired contrast level, or other adjustments
 * such as differing in light mode versus dark mode, or what the theme is, or what the color that
 * produced the theme is, etc.
 *
 * <p>Colors without backgrounds do not change tone when contrast changes. Colors with backgrounds
 * become closer to their background as contrast lowers, and further when contrast increases.
 *
 * <p>Prefer the static constructors. They provide a much more simple interface, such as requiring
 * just a hexcode, or just a hexcode and a background.
 *
 * <p>Ultimately, each component necessary for calculating a color, adjusting it for a desired
 * contrast level, and ensuring it has a certain lightness/tone difference from another color, is
 * provided by a function that takes a DynamicScheme and returns a value. This ensures ultimate
 * flexibility, any desired behavior of a color for any design system, but it usually unnecessary.
 * See the default constructor for more information.
 *
 * @hide
 */
// Prevent lint for Function.apply not being available on Android before API level 14 (4.0.1).
// "AndroidJdkLibsChecker" for Function, "NewApi" for Function.apply().
// A java_library Bazel rule with an Android constraint cannot skip these warnings without this
// annotation; another solution would be to create an android_library rule and supply
// AndroidManifest with an SDK set higher than 14.
@SuppressWarnings({"AndroidJdkLibsChecker", "NewApi"})
@RestrictTo(LIBRARY_GROUP)
public final class DynamicColor {
  public final Function<DynamicScheme, Double> hue;
  public final Function<DynamicScheme, Double> chroma;
  public final Function<DynamicScheme, Double> tone;
  public final Function<DynamicScheme, Double> opacity;

  public final Function<DynamicScheme, DynamicColor> background;
  public final Function<DynamicScheme, Double> toneMinContrast;
  public final Function<DynamicScheme, Double> toneMaxContrast;
  public final Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint;

  private final HashMap<DynamicScheme, Hct> hctCache = new HashMap<>();

  /**
   * The base constructor for DynamicColor.
   *
   * <p>Functional arguments allow overriding without risks that come with subclasses. _Strongly_
   * prefer using one of the static convenience constructors. This class is arguably too flexible to
   * ensure it can support any scenario.
   *
   * <p>For example, the default behavior of adjust tone at max contrast to be at a 7.0 ratio with
   * its background is principled and matches a11y guidance. That does not mean it's the desired
   * approach for _every_ design system, and every color pairing, always, in every case.
   *
   * @param hue given DynamicScheme, return the hue in HCT of the output color.
   * @param chroma given DynamicScheme, return chroma in HCT of the output color.
   * @param tone given DynamicScheme, return tone in HCT of the output color.
   * @param background given DynamicScheme, return the DynamicColor that is the background of this
   *     DynamicColor. When this is provided, automated adjustments to lower and raise contrast are
   *     made.
   * @param toneMinContrast given DynamicScheme, return tone in HCT/L* in L*a*b* this color should
   *     be at minimum contrast. See toneMinContrastDefault for the default behavior, and strongly
   *     consider using it unless you have strong opinions on a11y. The static constructors use it.
   * @param toneMaxContrast given DynamicScheme, return tone in HCT/L* in L*a*b* this color should
   *     be at maximum contrast. See toneMaxContrastDefault for the default behavior, and strongly
   *     consider using it unless you have strong opinions on a11y. The static constructors use it.
   * @param toneDeltaConstraint given DynamicScheme, return a ToneDeltaConstraint instance that
   *     describes a requirement that this DynamicColor must always have some difference in tone/L*
   *     from another DynamicColor.<br>
   *     Unlikely to be useful unless a design system has some distortions where colors that don't
   *     have a background/foreground relationship must have _some_ difference in tone, yet, not
   *     enough difference to create meaningful contrast.
   */
  public DynamicColor(
      Function<DynamicScheme, Double> hue,
      Function<DynamicScheme, Double> chroma,
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, Double> opacity,
      Function<DynamicScheme, DynamicColor> background,
      Function<DynamicScheme, Double> toneMinContrast,
      Function<DynamicScheme, Double> toneMaxContrast,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint) {
    this.hue = hue;
    this.chroma = chroma;
    this.tone = tone;
    this.opacity = opacity;
    this.background = background;
    this.toneMinContrast = toneMinContrast;
    this.toneMaxContrast = toneMaxContrast;
    this.toneDeltaConstraint = toneDeltaConstraint;
  }

  /**
   * Create a DynamicColor from a hex code.
   *
   * <p>Result has no background; thus no support for increasing/decreasing contrast for a11y.
   */
  public static DynamicColor fromArgb(int argb) {
    final Hct hct = Hct.fromInt(argb);
    final TonalPalette palette = TonalPalette.fromInt(argb);
    return DynamicColor.fromPalette((s) -> palette, (s) -> hct.getTone());
  }

  /**
   * Create a DynamicColor from just a hex code.
   *
   * <p>Result has no background; thus cannot support increasing/decreasing contrast for a11y.
   *
   * @param argb A hex code.
   * @param tone Function that provides a tone given DynamicScheme. Useful for adjusting for dark
   *     vs. light mode.
   */
  public static DynamicColor fromArgb(int argb, Function<DynamicScheme, Double> tone) {
    return DynamicColor.fromPalette((s) -> TonalPalette.fromInt(argb), tone);
  }

  /**
   * Create a DynamicColor.
   *
   * <p>If you don't understand HCT fully, or your design team doesn't, but wants support for
   * automated contrast adjustment, this method is _extremely_ useful: you can take a standard
   * design system expressed as hex codes, create DynamicColors corresponding to each color, and
   * then wire up backgrounds.
   *
   * <p>If the design system uses the same hex code on multiple backgrounds, define that in multiple
   * DynamicColors so that the background is accurate for each one. If you define a DynamicColor
   * with one background, and actually use it on another, DynamicColor can't guarantee contrast. For
   * example, if you use a color on both black and white, increasing the contrast on one necessarily
   * decreases contrast of the other.
   *
   * @param argb A hex code.
   * @param tone Function that provides a tone given DynamicScheme. (useful for dark vs. light mode)
   * @param background Function that provides background DynamicColor given DynamicScheme. Useful
   *     for contrast, given a background, colors can adjust to increase/decrease contrast.
   */
  public static DynamicColor fromArgb(
      int argb,
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background) {
    return DynamicColor.fromPalette((s) -> TonalPalette.fromInt(argb), tone, background);
  }

  /**
   * Create a DynamicColor from:
   *
   * @param argb A hex code.
   * @param tone Function that provides a tone given DynamicScheme. (useful for dark vs. light mode)
   * @param background Function that provides background DynamicColor given DynamicScheme. Useful
   *     for contrast, given a background, colors can adjust to increase/decrease contrast.
   * @param toneDeltaConstraint Function that provides a ToneDeltaConstraint given DynamicScheme.
   *     Useful for ensuring lightness difference between colors that don't _require_ contrast or
   *     have a formal background/foreground relationship.
   */
  public static DynamicColor fromArgb(
      int argb,
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint) {
    return DynamicColor.fromPalette(
        (s) -> TonalPalette.fromInt(argb), tone, background, toneDeltaConstraint);
  }

  /**
   * Create a DynamicColor.
   *
   * @param palette Function that provides a TonalPalette given DynamicScheme. A TonalPalette is
   *     defined by a hue and chroma, so this replaces the need to specify hue/chroma. By providing
   *     a tonal palette, when contrast adjustments are made, intended chroma can be preserved. For
   *     example, at T/L* 90, there is a significant limit to the amount of chroma. There is no
   *     colorful red, a red that light is pink. By preserving the _intended_ chroma if lightness
   *     lowers for contrast adjustments, the intended chroma is restored.
   * @param tone Function that provides a tone given DynamicScheme. (useful for dark vs. light mode)
   */
  public static DynamicColor fromPalette(
      Function<DynamicScheme, TonalPalette> palette, Function<DynamicScheme, Double> tone) {
    return fromPalette(palette, tone, null, null);
  }

  /**
   * Create a DynamicColor.
   *
   * @param palette Function that provides a TonalPalette given DynamicScheme. A TonalPalette is
   *     defined by a hue and chroma, so this replaces the need to specify hue/chroma. By providing
   *     a tonal palette, when contrast adjustments are made, intended chroma can be preserved. For
   *     example, at T/L* 90, there is a significant limit to the amount of chroma. There is no
   *     colorful red, a red that light is pink. By preserving the _intended_ chroma if lightness
   *     lowers for contrast adjustments, the intended chroma is restored.
   * @param tone Function that provides a tone given DynamicScheme. (useful for dark vs. light mode)
   * @param background Function that provides background DynamicColor given DynamicScheme. Useful
   *     for contrast, given a background, colors can adjust to increase/decrease contrast.
   */
  public static DynamicColor fromPalette(
      Function<DynamicScheme, TonalPalette> palette,
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background) {
    return fromPalette(palette, tone, background, null);
  }

  /**
   * Create a DynamicColor.
   *
   * @param palette Function that provides a TonalPalette given DynamicScheme. A TonalPalette is
   *     defined by a hue and chroma, so this replaces the need to specify hue/chroma. By providing
   *     a tonal palette, when contrast adjustments are made, intended chroma can be preserved. For
   *     example, at T/L* 90, there is a significant limit to the amount of chroma. There is no
   *     colorful red, a red that light is pink. By preserving the _intended_ chroma if lightness
   *     lowers for contrast adjustments, the intended chroma is restored.
   * @param tone Function that provides a tone given DynamicScheme. (useful for dark vs. light mode)
   * @param background Function that provides background DynamicColor given DynamicScheme. Useful
   *     for contrast, given a background, colors can adjust to increase/decrease contrast.
   * @param toneDeltaConstraint Function that provides a ToneDeltaConstraint given DynamicScheme.
   *     Useful for ensuring lightness difference between colors that don't _require_ contrast or
   *     have a formal background/foreground relationship.
   */
  public static DynamicColor fromPalette(
      Function<DynamicScheme, TonalPalette> palette,
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint) {
    return new DynamicColor(
        scheme -> palette.apply(scheme).getHue(),
        scheme -> palette.apply(scheme).getChroma(),
        tone,
        null,
        background,
        scheme -> toneMinContrastDefault(tone, background, scheme, toneDeltaConstraint),
        scheme -> toneMaxContrastDefault(tone, background, scheme, toneDeltaConstraint),
        toneDeltaConstraint);
  }

  public int getArgb(DynamicScheme scheme) {
    final int argb = getHct(scheme).toInt();
    if (opacity == null) {
      return argb;
    }
    final double percentage = opacity.apply(scheme);
    final int alpha = MathUtils.clampInt(0, 255, (int) Math.round(percentage * 255));
    return (argb & 0x00ffffff) | (alpha << 24);
  }

  public Hct getHct(DynamicScheme scheme) {
    final Hct cachedAnswer = hctCache.get(scheme);
    if (cachedAnswer != null) {
      return cachedAnswer;
    }
    // This is crucial for aesthetics: we aren't simply the taking the standard color
    // and changing its tone for contrast. Rather, we find the tone for contrast, then
    // use the specified chroma from the palette to construct a new color.
    //
    // For example, this enables colors with standard tone of T90, which has limited chroma, to
    // "recover" intended chroma as contrast increases.
    final Hct answer = Hct.from(hue.apply(scheme), chroma.apply(scheme), getTone(scheme));
    // NOMUTANTS--trivial test with onerous dependency injection requirement.
    if (hctCache.size() > 4) {
      hctCache.clear();
    }
    // NOMUTANTS--trivial test with onerous dependency injection requirement.
    hctCache.put(scheme, answer);
    return answer;
  }

  /** Returns the tone in HCT, ranging from 0 to 100, of the resolved color given scheme. */
  public double getTone(DynamicScheme scheme) {
    double answer = tone.apply(scheme);

    final boolean decreasingContrast = scheme.contrastLevel < 0.0;
    if (scheme.contrastLevel != 0.0) {
      final double startTone = tone.apply(scheme);
      final double endTone =
          decreasingContrast ? toneMinContrast.apply(scheme) : toneMaxContrast.apply(scheme);
      final double delta = (endTone - startTone) * Math.abs(scheme.contrastLevel);
      answer = delta + startTone;
    }

    final DynamicColor bgDynamicColor = background == null ? null : background.apply(scheme);
    double minRatio = Contrast.RATIO_MIN;
    double maxRatio = Contrast.RATIO_MAX;
    if (bgDynamicColor != null) {
      final boolean bgHasBg =
          bgDynamicColor.background != null && bgDynamicColor.background.apply(scheme) != null;
      final double standardRatio =
          Contrast.ratioOfTones(tone.apply(scheme), bgDynamicColor.tone.apply(scheme));
      if (decreasingContrast) {
        final double minContrastRatio =
            Contrast.ratioOfTones(
                toneMinContrast.apply(scheme), bgDynamicColor.toneMinContrast.apply(scheme));
        minRatio = bgHasBg ? minContrastRatio : 1.0;
        maxRatio = standardRatio;
      } else {
        final double maxContrastRatio =
            Contrast.ratioOfTones(
                toneMaxContrast.apply(scheme), bgDynamicColor.toneMaxContrast.apply(scheme));
        minRatio = bgHasBg ? min(maxContrastRatio, standardRatio) : 1.0;
        maxRatio = bgHasBg ? max(maxContrastRatio, standardRatio) : 21.0;
      }
    }

    final double finalMinRatio = minRatio;
    final double finalMaxRatio = maxRatio;
    final double finalAnswer = answer;
    answer =
        calculateDynamicTone(
            scheme,
            this.tone,
            (dynamicColor) -> dynamicColor.getTone(scheme),
            (a, b) -> finalAnswer,
            (s) -> bgDynamicColor,
            toneDeltaConstraint,
            (s) -> finalMinRatio,
            (s) -> finalMaxRatio);

    return answer;
  }

  /**
   * The default algorithm for calculating the tone of a color at minimum contrast.<br>
   * If the original contrast ratio was >= 7.0, reach contrast 4.5.<br>
   * If the original contrast ratio was >= 3.0, reach contrast 3.0.<br>
   * If the original contrast ratio was < 3.0, reach that ratio.
   */
  public static double toneMinContrastDefault(
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background,
      DynamicScheme scheme,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint) {
    return DynamicColor.calculateDynamicTone(
        scheme,
        tone,
        c -> c.toneMinContrast.apply(scheme),
        (stdRatio, bgTone) -> {
          double answer = tone.apply(scheme);
          if (stdRatio >= Contrast.RATIO_70) {
            answer = contrastingTone(bgTone, Contrast.RATIO_45);
          } else if (stdRatio >= Contrast.RATIO_30) {
            answer = contrastingTone(bgTone, Contrast.RATIO_30);
          } else {
            final boolean backgroundHasBackground =
                background != null
                    && background.apply(scheme) != null
                    && background.apply(scheme).background != null
                    && background.apply(scheme).background.apply(scheme) != null;
            if (backgroundHasBackground) {
              answer = contrastingTone(bgTone, stdRatio);
            }
          }
          return answer;
        },
        background,
        toneDeltaConstraint,
        null,
        standardRatio -> standardRatio);
  }

  /**
   * The default algorithm for calculating the tone of a color at maximum contrast.<br>
   * If the color's background has a background, reach contrast 7.0.<br>
   * If it doesn't, maintain the original contrast ratio.<br>
   *
   * <p>This ensures text on surfaces maintains its original, often detrimentally excessive,
   * contrast ratio. But, text on buttons can soften to not have excessive contrast.
   *
   * <p>Historically, digital design uses pure whites and black for text and surfaces. It's too much
   * of a jump at this point in history to introduce a dynamic contrast system _and_ insist that
   * text always had excessive contrast and should reach 7.0, it would deterimentally affect desire
   * to understand and use dynamic contrast.
   */
  public static double toneMaxContrastDefault(
      Function<DynamicScheme, Double> tone,
      Function<DynamicScheme, DynamicColor> background,
      DynamicScheme scheme,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint) {
    return DynamicColor.calculateDynamicTone(
        scheme,
        tone,
        c -> c.toneMaxContrast.apply(scheme),
        (stdRatio, bgTone) -> {
          final boolean backgroundHasBackground =
              background != null
                  && background.apply(scheme) != null
                  && background.apply(scheme).background != null
                  && background.apply(scheme).background.apply(scheme) != null;
          if (backgroundHasBackground) {
            return contrastingTone(bgTone, Contrast.RATIO_70);
          } else {
            return contrastingTone(bgTone, max(Contrast.RATIO_70, stdRatio));
          }
        },
        background,
        toneDeltaConstraint,
        null,
        null);
  }

  /**
   * Core method for calculating a tone for under dynamic contrast.
   *
   * <p>It enforces important properties:<br>
   * #1. Desired contrast ratio is reached.<br>
   * As contrast increases from standard to max, the tones involved should always be at least the
   * standard ratio. For example, if a button is T90, and button text is T0, and the button is T0 at
   * max contrast, the button text cannot simply linearly interpolate from T0 to T100, or at some
   * point they'll both be at the same tone.
   *
   * <p>#2. Enable light foregrounds on midtones.<br>
   * The eye prefers light foregrounds on T50 to T60, possibly up to T70, but, contrast ratio 4.5
   * can't be reached with T100 unless the foreground is T50. Contrast ratio 4.5 is crucial, it
   * represents 'readable text', i.e. text smaller than ~40 dp / 1/4". So, if a tone is between T50
   * and T60, it is proactively changed to T49 to enable light foregrounds.
   *
   * <p>#3. Ensure tone delta with another color.<br>
   * In design systems, there may be colors that don't have a pure background/foreground
   * relationship, but, do require different tones for visual differentiation. ToneDeltaConstraint
   * models this requirement, and DynamicColor enforces it.
   */
  public static double calculateDynamicTone(
      DynamicScheme scheme,
      Function<DynamicScheme, Double> toneStandard,
      Function<DynamicColor, Double> toneToJudge,
      BiFunction<Double, Double, Double> desiredTone,
      Function<DynamicScheme, DynamicColor> background,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint,
      Function<Double, Double> minRatio,
      Function<Double, Double> maxRatio) {
    // Start with the tone with no adjustment for contrast.
    // If there is no background, don't perform any adjustment, return immediately.
    final double toneStd = toneStandard.apply(scheme);
    double answer = toneStd;
    final DynamicColor bgDynamic = background == null ? null : background.apply(scheme);
    if (bgDynamic == null) {
      return answer;
    }
    final double bgToneStd = bgDynamic.tone.apply(scheme);
    final double stdRatio = Contrast.ratioOfTones(toneStd, bgToneStd);

    // If there is a background, determine its tone after contrast adjustment.
    // Then, calculate the foreground tone that ensures the caller's desired contrast ratio is met.
    final double bgTone = toneToJudge.apply(bgDynamic);
    final double myDesiredTone = desiredTone.apply(stdRatio, bgTone);
    final double currentRatio = Contrast.ratioOfTones(bgTone, myDesiredTone);
    final double minRatioRealized =
        minRatio == null
            ? Contrast.RATIO_MIN
            : minRatio.apply(stdRatio) == null ? Contrast.RATIO_MIN : minRatio.apply(stdRatio);
    final double maxRatioRealized =
        maxRatio == null
            ? Contrast.RATIO_MAX
            : maxRatio.apply(stdRatio) == null ? Contrast.RATIO_MAX : maxRatio.apply(stdRatio);
    final double desiredRatio =
        MathUtils.clampDouble(minRatioRealized, maxRatioRealized, currentRatio);
    if (desiredRatio == currentRatio) {
      answer = myDesiredTone;
    } else {
      answer = DynamicColor.contrastingTone(bgTone, desiredRatio);
    }

    // If the background has no background,  adjust the foreground tone to ensure that
    // it is dark enough to have a light foreground.
    if (bgDynamic.background == null || bgDynamic.background.apply(scheme) == null) {
      answer = DynamicColor.enableLightForeground(answer);
    }

    // If the caller has specified a constraint where it must have a certain  tone distance from
    // another color, enforce that constraint.
    answer = ensureToneDelta(answer, toneStd, scheme, toneDeltaConstraint, toneToJudge);

    return answer;
  }

  static double ensureToneDelta(
      double tone,
      double toneStandard,
      DynamicScheme scheme,
      Function<DynamicScheme, ToneDeltaConstraint> toneDeltaConstraint,
      Function<DynamicColor, Double> toneToDistanceFrom) {
    final ToneDeltaConstraint constraint =
        (toneDeltaConstraint == null ? null : toneDeltaConstraint.apply(scheme));
    if (constraint == null) {
      return tone;
    }

    final double requiredDelta = constraint.delta;
    final double keepAwayTone = toneToDistanceFrom.apply(constraint.keepAway);
    final double delta = Math.abs(tone - keepAwayTone);
    if (delta >= requiredDelta) {
      return tone;
    }
    switch (constraint.keepAwayPolarity) {
      case DARKER:
        return MathUtils.clampDouble(0, 100, keepAwayTone + requiredDelta);
      case LIGHTER:
        return MathUtils.clampDouble(0, 100, keepAwayTone - requiredDelta);
      case NO_PREFERENCE:
        final double keepAwayToneStandard = constraint.keepAway.tone.apply(scheme);
        final boolean preferLighten = toneStandard > keepAwayToneStandard;
        final double alterAmount = Math.abs(delta - requiredDelta);
        final boolean lighten = preferLighten ? (tone + alterAmount <= 100.0) : tone < alterAmount;
        return lighten ? tone + alterAmount : tone - alterAmount;
    }
    return tone;
  }

  /**
   * Given a background tone, find a foreground tone, while ensuring they reach a contrast ratio
   * that is as close to ratio as possible.
   */
  public static double contrastingTone(double bgTone, double ratio) {
    final double lighterTone = Contrast.lighterUnsafe(bgTone, ratio);
    final double darkerTone = Contrast.darkerUnsafe(bgTone, ratio);
    final double lighterRatio = Contrast.ratioOfTones(lighterTone, bgTone);
    final double darkerRatio = Contrast.ratioOfTones(darkerTone, bgTone);
    final boolean preferLighter = tonePrefersLightForeground(bgTone);

    if (preferLighter) {
      // "Neglible difference" handles an edge case where the initial contrast ratio is high
      // (ex. 13.0), and the ratio passed to the function is that high ratio, and both the lighter
      // and darker ratio fails to pass that ratio.
      //
      // This was observed with Tonal Spot's On Primary Container turning black momentarily between
      // high and max contrast in light mode. PC's standard tone was T90, OPC's was T10, it was
      // light mode, and the contrast level was 0.6568521221032331.
      final boolean negligibleDifference =
          Math.abs(lighterRatio - darkerRatio) < 0.1 && lighterRatio < ratio && darkerRatio < ratio;
      if (lighterRatio >= ratio || lighterRatio >= darkerRatio || negligibleDifference) {
        return lighterTone;
      } else {
        return darkerTone;
      }
    } else {
      return darkerRatio >= ratio || darkerRatio >= lighterRatio ? darkerTone : lighterTone;
    }
  }

  /**
   * Adjust a tone down such that white has 4.5 contrast, if the tone is reasonably close to
   * supporting it.
   */
  public static double enableLightForeground(double tone) {
    if (tonePrefersLightForeground(tone) && !toneAllowsLightForeground(tone)) {
      return 49.0;
    }
    return tone;
  }

  /**
   * People prefer white foregrounds on ~T60-70. Observed over time, and also by Andrew Somers
   * during research for APCA.
   *
   * <p>T60 used as to create the smallest discontinuity possible when skipping down to T49 in order
   * to ensure light foregrounds.
   *
   * <p>Since `tertiaryContainer` in dark monochrome scheme requires a tone of 60, it should not be
   * adjusted. Therefore, 60 is excluded here.
   */
  public static boolean tonePrefersLightForeground(double tone) {
    return Math.round(tone) < 60;
  }

  /** Tones less than ~T50 always permit white at 4.5 contrast. */
  public static boolean toneAllowsLightForeground(double tone) {
    return Math.round(tone) <= 49;
  }
}
