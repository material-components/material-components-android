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

  private static final Map<Integer, DynamicColor> colorResourceIdToColorValue;
  static {
    Map<Integer, DynamicColor> map = new HashMap<>();
    map.put(R.color.material_personalized_color_primary, MaterialDynamicColors.primary);
    map.put(R.color.material_personalized_color_on_primary, MaterialDynamicColors.onPrimary);
    map.put(
        R.color.material_personalized_color_primary_inverse, MaterialDynamicColors.primaryInverse);
    map.put(
        R.color.material_personalized_color_primary_container,
        MaterialDynamicColors.primaryContainer);
    map.put(
        R.color.material_personalized_color_on_primary_container,
        MaterialDynamicColors.onPrimaryContainer);
    map.put(R.color.material_personalized_color_secondary, MaterialDynamicColors.secondary);
    map.put(R.color.material_personalized_color_on_secondary, MaterialDynamicColors.onSecondary);
    map.put(
        R.color.material_personalized_color_secondary_container,
        MaterialDynamicColors.secondaryContainer);
    map.put(
        R.color.material_personalized_color_on_secondary_container,
        MaterialDynamicColors.onSecondaryContainer);
    map.put(R.color.material_personalized_color_tertiary, MaterialDynamicColors.tertiary);
    map.put(R.color.material_personalized_color_on_tertiary, MaterialDynamicColors.onTertiary);
    map.put(
        R.color.material_personalized_color_tertiary_container,
        MaterialDynamicColors.tertiaryContainer);
    map.put(
        R.color.material_personalized_color_on_tertiary_container,
        MaterialDynamicColors.onTertiaryContainer);
    map.put(R.color.material_personalized_color_background, MaterialDynamicColors.background);
    map.put(R.color.material_personalized_color_on_background, MaterialDynamicColors.onBackground);
    map.put(R.color.material_personalized_color_surface, MaterialDynamicColors.surface);
    map.put(R.color.material_personalized_color_on_surface, MaterialDynamicColors.onSurface);
    map.put(
        R.color.material_personalized_color_surface_variant, MaterialDynamicColors.surfaceVariant);
    map.put(
        R.color.material_personalized_color_on_surface_variant,
        MaterialDynamicColors.onSurfaceVariant);
    map.put(
        R.color.material_personalized_color_surface_inverse, MaterialDynamicColors.surfaceInverse);
    map.put(
        R.color.material_personalized_color_on_surface_inverse,
        MaterialDynamicColors.onSurfaceInverse);
    map.put(
        R.color.material_personalized_color_surface_bright, MaterialDynamicColors.surfaceBright);
    map.put(R.color.material_personalized_color_surface_dim, MaterialDynamicColors.surfaceDim);
    map.put(
        R.color.material_personalized_color_surface_container,
        MaterialDynamicColors.surfaceContainer);
    map.put(
        R.color.material_personalized_color_surface_container_low,
        MaterialDynamicColors.surfaceSub1);
    map.put(
        R.color.material_personalized_color_surface_container_high,
        MaterialDynamicColors.surfaceAdd1);
    map.put(
        R.color.material_personalized_color_surface_container_lowest,
        MaterialDynamicColors.surfaceSub2);
    map.put(
        R.color.material_personalized_color_surface_container_highest,
        MaterialDynamicColors.surfaceAdd2);
    map.put(R.color.material_personalized_color_outline, MaterialDynamicColors.outline);
    map.put(
        R.color.material_personalized_color_outline_variant, MaterialDynamicColors.outlineVariant);
    map.put(R.color.material_personalized_color_error, MaterialDynamicColors.error);
    map.put(R.color.material_personalized_color_on_error, MaterialDynamicColors.onError);
    map.put(
        R.color.material_personalized_color_error_container, MaterialDynamicColors.errorContainer);
    map.put(
        R.color.material_personalized_color_on_error_container,
        MaterialDynamicColors.onErrorContainer);
    map.put(
        R.color.material_personalized_color_control_activated,
        MaterialDynamicColors.controlActivated);
    map.put(
        R.color.material_personalized_color_control_normal, MaterialDynamicColors.controlNormal);
    map.put(
        R.color.material_personalized_color_control_highlight,
        MaterialDynamicColors.controlHighlight);
    map.put(
        R.color.material_personalized_color_text_primary_inverse,
        MaterialDynamicColors.textPrimaryInverse);
    map.put(
        R.color.material_personalized_color_text_secondary_and_tertiary_inverse,
        MaterialDynamicColors.textSecondaryAndTertiaryInverse);
    map.put(
        R.color.material_personalized_color_text_secondary_and_tertiary_inverse_disabled,
        MaterialDynamicColors.textSecondaryAndTertiaryInverseDisabled);
    map.put(
        R.color.material_personalized_color_text_primary_inverse_disable_only,
        MaterialDynamicColors.textPrimaryInverseDisableOnly);
    map.put(
        R.color.material_personalized_color_text_hint_foreground_inverse,
        MaterialDynamicColors.textHintInverse);
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
