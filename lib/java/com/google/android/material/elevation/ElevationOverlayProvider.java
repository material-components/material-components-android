/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.elevation;

import com.google.android.material.R;

import android.content.Context;
import android.graphics.Color;
import androidx.core.graphics.ColorUtils;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.resources.MaterialAttributes;

/** Utility for calculating elevation overlay alpha values and colors. */
public class ElevationOverlayProvider {

  private static final float FORMULA_MULTIPLIER = 4.5f;
  private static final float FORMULA_OFFSET = 2f;

  private final boolean elevationOverlayEnabled;
  private final int elevationOverlayColor;
  private final int colorSurface;
  private final float displayDensity;

  public ElevationOverlayProvider(@NonNull Context context) {
    this.elevationOverlayEnabled =
        MaterialAttributes.resolveBoolean(context, R.attr.elevationOverlayEnabled, false);
    this.elevationOverlayColor =
        MaterialColors.getColor(context, R.attr.elevationOverlayColor, Color.TRANSPARENT);
    this.colorSurface = MaterialColors.getColor(context, R.attr.colorSurface, Color.TRANSPARENT);
    this.displayDensity = context.getResources().getDisplayMetrics().density;
  }

  /**
   * See {@link #compositeOverlayWithThemeSurfaceColorIfNeeded(float)}.
   *
   * <p>The absolute elevation of the parent of the provided {@code overlayView} will also be
   * factored in when determining the overlay color.
   */
  @ColorInt
  public int compositeOverlayWithThemeSurfaceColorIfNeeded(
      float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlayWithThemeSurfaceColorIfNeeded(elevation);
  }

  /**
   * Blends the calculated elevation overlay color (@see #compositeOverlayIfNeeded(int, float)) with
   * the current theme's color int value for {@code R.attr.colorSurface} if needed.
   */
  @ColorInt
  public int compositeOverlayWithThemeSurfaceColorIfNeeded(float elevation) {
    return compositeOverlayIfNeeded(colorSurface, elevation);
  }

  /**
   * See {@link #compositeOverlayIfNeeded(int, float)}.
   *
   * <p>The absolute elevation of the parent of the provided {@code overlayView} will also be
   * factored in when determining the overlay color.
   */
  @ColorInt
  public int compositeOverlayIfNeeded(
      @ColorInt int backgroundColor, float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlayIfNeeded(backgroundColor, elevation);
  }

  /**
   * Blends the calculated elevation overlay color (@see #compositeOverlay(int, float)) with the
   * {@code backgroundColor}, only if the current theme's {@code R.attr.elevationOverlayEnabled} is
   * true and the {@code backgroundColor} matches the theme's surface color ({@code
   * R.attr.colorSurface}); otherwise returns the {@code backgroundColor}.
   */
  @ColorInt
  public int compositeOverlayIfNeeded(@ColorInt int backgroundColor, float elevation) {
    if (elevationOverlayEnabled && isThemeSurfaceColor(backgroundColor)) {
      return compositeOverlay(backgroundColor, elevation);
    } else {
      return backgroundColor;
    }
  }

  /** See {@link #compositeOverlay(int, float)}. */
  @ColorInt
  public int compositeOverlay(
      @ColorInt int backgroundColor, float elevation, @NonNull View overlayView) {
    elevation += getParentAbsoluteElevation(overlayView);
    return compositeOverlay(backgroundColor, elevation);
  }

  /**
   * Blends the calculated elevation overlay color with the provided {@code backgroundColor}.
   *
   * <p>An alpha level is applied to the theme's {@code R.attr.elevationOverlayColor} by using a
   * formula that is based on the provided {@code elevation} value.
   */
  @ColorInt
  public int compositeOverlay(@ColorInt int backgroundColor, float elevation) {
    float overlayAlphaFraction = calculateOverlayAlphaFraction(elevation);
    int backgroundAlpha = Color.alpha(backgroundColor);
    int backgroundColorOpaque = ColorUtils.setAlphaComponent(backgroundColor, 255);
    int overlayColorOpaque =
        MaterialColors.layer(backgroundColorOpaque, elevationOverlayColor, overlayAlphaFraction);
    return ColorUtils.setAlphaComponent(overlayColorOpaque, backgroundAlpha);
  }

  /**
   * Calculates the alpha value, between 0 and 255, that should be used with the elevation overlay
   * color, based on the provided {@code elevation} value.
   */
  public int calculateOverlayAlpha(float elevation) {
    return Math.round(calculateOverlayAlphaFraction(elevation) * 255);
  }

  /**
   * Calculates the alpha fraction, between 0 and 1, that should be used with the elevation overlay
   * color, based on the provided {@code elevation} value.
   */
  public float calculateOverlayAlphaFraction(float elevation) {
    if (displayDensity <= 0 || elevation <= 0) {
      return 0;
    }
    float elevationDp = elevation / displayDensity;
    float alphaFraction =
        (FORMULA_MULTIPLIER * (float) Math.log1p(elevationDp) + FORMULA_OFFSET) / 100;
    return Math.min(alphaFraction, 1);
  }

  /** Returns the current theme's boolean value for {@code R.attr.elevationOverlayEnabled}. */
  public boolean isThemeElevationOverlayEnabled() {
    return elevationOverlayEnabled;
  }

  /** Returns the current theme's color int value for {@code R.attr.elevationOverlayColor}. */
  @ColorInt
  public int getThemeElevationOverlayColor() {
    return elevationOverlayColor;
  }

  /** Returns the current theme's color int value for {@code R.attr.colorSurface}. */
  @ColorInt
  public int getThemeSurfaceColor() {
    return colorSurface;
  }

  /**
   * Returns the absolute elevation of the parent of the provided {@code overlayView}, or in other
   * words, the sum of the elevations of all ancestors of the {@code overlayView}.
   */
  public float getParentAbsoluteElevation(@NonNull View overlayView) {
    return ViewUtils.getParentAbsoluteElevation(overlayView);
  }

  private boolean isThemeSurfaceColor(@ColorInt int color) {
    return ColorUtils.setAlphaComponent(color, 255) == colorSurface;
  }
}
