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

package com.google.android.material.color;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.color.utilities.DynamicColor;
import com.google.android.material.color.utilities.DynamicScheme;
import com.google.android.material.color.utilities.MaterialDynamicColors;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Helper methods for communication with the Material Color Utilities library.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class MaterialColorUtilitiesHelper {

  private MaterialColorUtilitiesHelper() {}

  private static final MaterialDynamicColors dynamicColors = new MaterialDynamicColors();
  private static final Map<Integer, DynamicColor> colorResourceIdToColorValue;
  static {
    Map<Integer, DynamicColor> map = new HashMap<>();
    map.put(R.color.material_personalized_color_primary, dynamicColors.primary());
    map.put(R.color.material_personalized_color_on_primary, dynamicColors.onPrimary());
    map.put(R.color.material_personalized_color_primary_inverse, dynamicColors.inversePrimary());
    map.put(
        R.color.material_personalized_color_primary_container, dynamicColors.primaryContainer());
    map.put(
        R.color.material_personalized_color_on_primary_container,
        dynamicColors.onPrimaryContainer());
    map.put(R.color.material_personalized_color_secondary, dynamicColors.secondary());
    map.put(R.color.material_personalized_color_on_secondary, dynamicColors.onSecondary());
    map.put(
        R.color.material_personalized_color_secondary_container,
        dynamicColors.secondaryContainer());
    map.put(
        R.color.material_personalized_color_on_secondary_container,
        dynamicColors.onSecondaryContainer());
    map.put(R.color.material_personalized_color_tertiary, dynamicColors.tertiary());
    map.put(R.color.material_personalized_color_on_tertiary, dynamicColors.onTertiary());
    map.put(
        R.color.material_personalized_color_tertiary_container, dynamicColors.tertiaryContainer());
    map.put(
        R.color.material_personalized_color_on_tertiary_container,
        dynamicColors.onTertiaryContainer());
    map.put(R.color.material_personalized_color_background, dynamicColors.background());
    map.put(R.color.material_personalized_color_on_background, dynamicColors.onBackground());
    map.put(R.color.material_personalized_color_surface, dynamicColors.surface());
    map.put(R.color.material_personalized_color_on_surface, dynamicColors.onSurface());
    map.put(R.color.material_personalized_color_surface_variant, dynamicColors.surfaceVariant());
    map.put(
        R.color.material_personalized_color_on_surface_variant, dynamicColors.onSurfaceVariant());
    map.put(R.color.material_personalized_color_surface_inverse, dynamicColors.inverseSurface());
    map.put(
        R.color.material_personalized_color_on_surface_inverse, dynamicColors.inverseOnSurface());
    map.put(R.color.material_personalized_color_surface_bright, dynamicColors.surfaceBright());
    map.put(R.color.material_personalized_color_surface_dim, dynamicColors.surfaceDim());
    map.put(
        R.color.material_personalized_color_surface_container, dynamicColors.surfaceContainer());
    map.put(
        R.color.material_personalized_color_surface_container_low,
        dynamicColors.surfaceContainerLow());
    map.put(
        R.color.material_personalized_color_surface_container_high,
        dynamicColors.surfaceContainerHigh());
    map.put(
        R.color.material_personalized_color_surface_container_lowest,
        dynamicColors.surfaceContainerLowest());
    map.put(
        R.color.material_personalized_color_surface_container_highest,
        dynamicColors.surfaceContainerHighest());
    map.put(R.color.material_personalized_color_outline, dynamicColors.outline());
    map.put(R.color.material_personalized_color_outline_variant, dynamicColors.outlineVariant());
    map.put(R.color.material_personalized_color_error, dynamicColors.error());
    map.put(R.color.material_personalized_color_on_error, dynamicColors.onError());
    map.put(R.color.material_personalized_color_error_container, dynamicColors.errorContainer());
    map.put(
        R.color.material_personalized_color_on_error_container, dynamicColors.onErrorContainer());
    map.put(
        R.color.material_personalized_color_control_activated, dynamicColors.controlActivated());
    map.put(R.color.material_personalized_color_control_normal, dynamicColors.controlNormal());
    map.put(
        R.color.material_personalized_color_control_highlight, dynamicColors.controlHighlight());
    map.put(
        R.color.material_personalized_color_text_primary_inverse,
        dynamicColors.textPrimaryInverse());
    map.put(
        R.color.material_personalized_color_text_secondary_and_tertiary_inverse,
        dynamicColors.textSecondaryAndTertiaryInverse());
    map.put(
        R.color.material_personalized_color_text_secondary_and_tertiary_inverse_disabled,
        dynamicColors.textSecondaryAndTertiaryInverseDisabled());
    map.put(
        R.color.material_personalized_color_text_primary_inverse_disable_only,
        dynamicColors.textPrimaryInverseDisableOnly());
    map.put(
        R.color.material_personalized_color_text_hint_foreground_inverse,
        dynamicColors.textHintInverse());
    colorResourceIdToColorValue = Collections.unmodifiableMap(map);
  }

  @NonNull
  public static Map<Integer, Integer> createColorResourcesIdsToColorValues(
      @NonNull DynamicScheme colorScheme) {
    HashMap<Integer, Integer> map = new HashMap<>();
    for (Map.Entry<Integer, DynamicColor> entry : colorResourceIdToColorValue.entrySet()) {
      map.put(entry.getKey(), entry.getValue().getArgb(colorScheme));
    }
    return Collections.unmodifiableMap(map);
  }
}
