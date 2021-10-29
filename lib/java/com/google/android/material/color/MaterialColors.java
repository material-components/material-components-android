/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.material.color;

import com.google.android.material.R;

import static android.graphics.Color.TRANSPARENT;

import android.content.Context;
import android.graphics.Color;
import androidx.core.graphics.ColorUtils;
import android.util.TypedValue;
import android.view.View;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import com.google.android.material.resources.MaterialAttributes;

/**
 * A utility class for common color variants used in Material themes.
 */
public class MaterialColors {

  public static final float ALPHA_FULL = 1.00F;
  public static final float ALPHA_MEDIUM = 0.54F;
  public static final float ALPHA_DISABLED = 0.38F;
  public static final float ALPHA_LOW = 0.32F;
  public static final float ALPHA_DISABLED_LOW = 0.12F;

  // TODO(b/199495444): token integration for color roles luminance values.
  // Tone means degrees of lightness, in the range of 0 (inclusive) to 100 (inclusive).
  private static final int TONE_ACCENT_LIGHT = 40;

  private static final int TONE_ON_ACCENT_LIGHT = 100;
  private static final int TONE_ACCENT_CONTAINER_LIGHT = 90;
  private static final int TONE_ON_ACCENT_CONTAINER_LIGHT = 10;
  private static final int TONE_ACCENT_DARK = 70;
  private static final int TONE_ON_ACCENT_DARK = 10;
  private static final int TONE_ACCENT_CONTAINER_DARK = 20;
  private static final int TONE_ON_ACCENT_CONTAINER_DARK = 80;

  private MaterialColors() {
    // Private constructor to prevent unwanted construction.
  }

  /**
   * Returns the color int for the provided theme color attribute, using the {@link Context} of the
   * provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(@NonNull View view, @AttrRes int colorAttributeResId) {
    return MaterialAttributes.resolveOrThrow(view, colorAttributeResId);
  }

  /**
   * Returns the color int for the provided theme color attribute.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(
      Context context, @AttrRes int colorAttributeResId, String errorMessageComponent) {
    return MaterialAttributes.resolveOrThrow(context, colorAttributeResId, errorMessageComponent);
  }

  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme, using the {@code view}'s {@link Context}.
   */
  @ColorInt
  public static int getColor(
      @NonNull View view, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    return getColor(view.getContext(), colorAttributeResId, defaultValue);
  }

  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(
      @NonNull Context context, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    TypedValue typedValue = MaterialAttributes.resolve(context, colorAttributeResId);
    if (typedValue != null) {
      return typedValue.data;
    } else {
      return defaultValue;
    }
  }

  /**
   * Convenience method that calculates {@link MaterialColors#layer(View, int, int, float)} without
   * an {@code overlayAlpha} value by passing in {@code 1f} for the alpha value.
   */
  @ColorInt
  public static int layer(
      @NonNull View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId) {
    return layer(view, backgroundColorAttributeResId, overlayColorAttributeResId, 1f);
  }

  /**
   * Convenience method that wraps {@link MaterialColors#layer(int, int, float)} for layering colors
   * from theme attributes.
   */
  @ColorInt
  public static int layer(
      @NonNull View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int backgroundColor = getColor(view, backgroundColorAttributeResId);
    int overlayColor = getColor(view, overlayColorAttributeResId);
    return layer(backgroundColor, overlayColor, overlayAlpha);
  }

  /**
   * Calculates a color that represents the layering of the {@code overlayColor} (with {@code
   * overlayAlpha} applied) on top of the {@code backgroundColor}.
   */
  @ColorInt
  public static int layer(
      @ColorInt int backgroundColor,
      @ColorInt int overlayColor,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int computedAlpha = Math.round(Color.alpha(overlayColor) * overlayAlpha);
    int computedOverlayColor = ColorUtils.setAlphaComponent(overlayColor, computedAlpha);
    return layer(backgroundColor, computedOverlayColor);
  }

  /**
   * Calculates a color that represents the layering of the {@code overlayColor} on top of the
   * {@code backgroundColor}.
   */
  @ColorInt
  public static int layer(@ColorInt int backgroundColor, @ColorInt int overlayColor) {
    return ColorUtils.compositeColors(overlayColor, backgroundColor);
  }

  /**
   * Calculates a new color by multiplying an additional alpha int value to the alpha channel of a
   * color in integer type.
   *
   * @param originalARGB The original color.
   * @param alpha The additional alpha [0-255].
   * @return The blended color.
   */
  @ColorInt
  public static int compositeARGBWithAlpha(
      @ColorInt int originalARGB, @IntRange(from = 0, to = 255) int alpha) {
    alpha = Color.alpha(originalARGB) * alpha / 255;
    return ColorUtils.setAlphaComponent(originalARGB, alpha);
  }

  /** Determines if a color should be considered light or dark. */
  public static boolean isColorLight(@ColorInt int color) {
    return color != TRANSPARENT && ColorUtils.calculateLuminance(color) > 0.5;
  }

  /**
   * Returns the color int of the given color harmonized with the context theme's colorPrimary.
   *
   * @param context The target context.
   * @param colorToHarmonize The color to harmonize.
   */
  @ColorInt
  public static int harmonizeWithPrimary(@NonNull Context context, @ColorInt int colorToHarmonize) {
    return harmonize(
        colorToHarmonize,
        getColor(context, R.attr.colorPrimary, MaterialColors.class.getCanonicalName()));
  }

  /**
   * A convenience function to harmonize any two colors provided, returns the color int of the
   * harmonized color, or the original design color value if color harmonization is not available.
   *
   * @param colorToHarmonize The color to harmonize.
   * @param colorToHarmonizeWith The primary color selected for harmonization.
   */
  @ColorInt
  public static int harmonize(@ColorInt int colorToHarmonize, @ColorInt int colorToHarmonizeWith) {
    return Blend.harmonize(colorToHarmonize, colorToHarmonizeWith);
  }

  /**
   * Returns the {@link ColorRoles} object generated from the provided input color.
   *
   * @param context The target context.
   * @param color The input color provided for generating its associated four color roles.
   */
  @NonNull
  public static ColorRoles getColorRoles(@NonNull Context context, @ColorInt int color) {
    return getColorRoles(
        color,
        MaterialAttributes.resolveBoolean(context, R.attr.isLightTheme, /* defaultValue= */ true));
  }

  /**
   * Returns the {@link ColorRoles} object generated from the provided input color.
   *
   * @param color The input color provided for generating its associated four color roles.
   * @param isLightTheme Whether the input is light themed or not, true if light theme is enabled.
   */
  @NonNull
  public static ColorRoles getColorRoles(@ColorInt int color, boolean isLightTheme) {
    return isLightTheme
        ? new ColorRoles(
            getColorRole(color, TONE_ACCENT_LIGHT),
            getColorRole(color, TONE_ON_ACCENT_LIGHT),
            getColorRole(color, TONE_ACCENT_CONTAINER_LIGHT),
            getColorRole(color, TONE_ON_ACCENT_CONTAINER_LIGHT))
        : new ColorRoles(
            getColorRole(color, TONE_ACCENT_DARK),
            getColorRole(color, TONE_ON_ACCENT_DARK),
            getColorRole(color, TONE_ACCENT_CONTAINER_DARK),
            getColorRole(color, TONE_ON_ACCENT_CONTAINER_DARK));
  }

  @ColorInt
  private static int getColorRole(@ColorInt int color, @IntRange(from = 0, to = 100) int tone) {
    Hct hctColor = Hct.fromInt(color);
    hctColor.setTone(tone);
    return hctColor.toInt();
  }
}
