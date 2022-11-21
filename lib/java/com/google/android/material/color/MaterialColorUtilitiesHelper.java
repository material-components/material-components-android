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

import androidx.annotation.NonNull;
import com.google.android.material.color.utilities.Scheme;
import java.util.HashMap;
import java.util.Map;

/** Helper methods for communication with the Material Color Utilities library. */
final class MaterialColorUtilitiesHelper {

  private MaterialColorUtilitiesHelper() {}

  @NonNull
  static Map<Integer, Integer> createColorResourcesIdsToColorValues(@NonNull Scheme colorScheme) {
    HashMap<Integer, Integer> map = new HashMap<>();

    map.put(R.color.material_personalized_color_primary, colorScheme.getPrimary());
    map.put(R.color.material_personalized_color_on_primary, colorScheme.getOnPrimary());
    map.put(R.color.material_personalized_color_primary_inverse, colorScheme.getInversePrimary());
    map.put(
        R.color.material_personalized_color_primary_container, colorScheme.getPrimaryContainer());
    map.put(
        R.color.material_personalized_color_on_primary_container,
        colorScheme.getOnPrimaryContainer());
    map.put(R.color.material_personalized_color_secondary, colorScheme.getSecondary());
    map.put(R.color.material_personalized_color_on_secondary, colorScheme.getOnSecondary());
    map.put(
        R.color.material_personalized_color_secondary_container,
        colorScheme.getSecondaryContainer());
    map.put(
        R.color.material_personalized_color_on_secondary_container,
        colorScheme.getOnSecondaryContainer());
    map.put(R.color.material_personalized_color_tertiary, colorScheme.getTertiary());
    map.put(R.color.material_personalized_color_on_tertiary, colorScheme.getOnTertiary());
    map.put(
        R.color.material_personalized_color_tertiary_container, colorScheme.getTertiaryContainer());
    map.put(
        R.color.material_personalized_color_on_tertiary_container,
        colorScheme.getOnTertiaryContainer());
    map.put(R.color.material_personalized_color_background, colorScheme.getBackground());
    map.put(R.color.material_personalized_color_on_background, colorScheme.getOnBackground());
    map.put(R.color.material_personalized_color_surface, colorScheme.getSurface());
    map.put(R.color.material_personalized_color_on_surface, colorScheme.getOnSurface());
    map.put(R.color.material_personalized_color_surface_variant, colorScheme.getSurfaceVariant());
    map.put(
        R.color.material_personalized_color_on_surface_variant, colorScheme.getOnSurfaceVariant());
    map.put(R.color.material_personalized_color_surface_inverse, colorScheme.getInverseSurface());
    map.put(
        R.color.material_personalized_color_on_surface_inverse, colorScheme.getInverseOnSurface());
    map.put(R.color.material_personalized_color_surface_outline, colorScheme.getOutline());
    map.put(R.color.material_personalized_color_error, colorScheme.getError());
    map.put(R.color.material_personalized_color_on_error, colorScheme.getOnError());
    map.put(R.color.material_personalized_color_error_container, colorScheme.getErrorContainer());
    map.put(
        R.color.material_personalized_color_on_error_container, colorScheme.getOnErrorContainer());

    // TODO(b/254612063): Add default framework attributes when the material color utilities library
    // provides the color values for non-Material roles.
    return map;
  }
}
