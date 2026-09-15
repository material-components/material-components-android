/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.color;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.material.catalog.feature.DemoFragment;
import java.util.Arrays;
import java.util.List;

/** A placeholder fragment that displays the main Color demo for the Catalog app. */
public final class ColorMainDemoFragment extends DemoFragment {

  private LinearLayout colorsLayoutSurfaces;
  private LinearLayout colorsLayoutContent;
  private LinearLayout colorsLayoutUtility;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_colors_fragment, viewGroup, false/* attachToRoot= */ );

    colorsLayoutSurfaces = view.findViewById(R.id.cat_colors_surfaces);
    colorsLayoutContent = view.findViewById(R.id.cat_colors_content);
    colorsLayoutUtility = view.findViewById(R.id.cat_colors_utility);

    for (ColorRow colorRow : getColorRolesSurfaces()) {
      colorRow.addTo(layoutInflater, colorsLayoutSurfaces);
    }

    for (ColorRow colorRow : getColorRolesContent()) {
      colorRow.addTo(layoutInflater, colorsLayoutContent);
    }

    for (ColorRow colorRow : getColorRolesUtility()) {
      colorRow.addTo(layoutInflater, colorsLayoutUtility);
    }

    return view;
  }

  private List<ColorRow> getColorRolesSurfaces() {
    return Arrays.asList(
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_background, android.R.attr.colorBackground),
            new ColorRoleItem(R.string.cat_color_role_on_background, com.google.android.material.R.attr.colorOnBackground)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_surface, com.google.android.material.R.attr.colorSurface),
            new ColorRoleItem(R.string.cat_color_role_on_surface, com.google.android.material.R.attr.colorOnSurface)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_surface_variant, com.google.android.material.R.attr.colorSurfaceVariant),
            new ColorRoleItem(
                R.string.cat_color_role_on_surface_variant, com.google.android.material.R.attr.colorOnSurfaceVariant)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_inverse_surface, com.google.android.material.R.attr.colorSurfaceInverse),
            new ColorRoleItem(
                R.string.cat_color_role_inverse_on_surface, com.google.android.material.R.attr.colorOnSurfaceInverse)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_surface_bright, com.google.android.material.R.attr.colorSurfaceBright),
            new ColorRoleItem(R.string.cat_color_role_surface_dim, com.google.android.material.R.attr.colorSurfaceDim)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_surface_container_low, com.google.android.material.R.attr.colorSurfaceContainerLow),
            new ColorRoleItem(
                R.string.cat_color_role_surface_container_high, com.google.android.material.R.attr.colorSurfaceContainerHigh)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_surface_container_lowest,
                com.google.android.material.R.attr.colorSurfaceContainerLowest),
            new ColorRoleItem(
                R.string.cat_color_role_surface_container_highest,
                com.google.android.material.R.attr.colorSurfaceContainerHighest)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_surface_container, com.google.android.material.R.attr.colorSurfaceContainer),
            null));
  }

  private List<ColorRow> getColorRolesContent() {
    return Arrays.asList(
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_primary, androidx.appcompat.R.attr.colorPrimary),
            new ColorRoleItem(R.string.cat_color_role_on_primary, com.google.android.material.R.attr.colorOnPrimary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_primary_container, com.google.android.material.R.attr.colorPrimaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_primary_container, com.google.android.material.R.attr.colorOnPrimaryContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_primary_fixed, com.google.android.material.R.attr.colorPrimaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_primary_fixed_dim, com.google.android.material.R.attr.colorPrimaryFixedDim)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_on_primary_fixed, com.google.android.material.R.attr.colorOnPrimaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_on_primary_fixed_variant,
                com.google.android.material.R.attr.colorOnPrimaryFixedVariant)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_inverse_primary, com.google.android.material.R.attr.colorPrimaryInverse),
            /* colorRoleItemRight= */ null),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_secondary, com.google.android.material.R.attr.colorSecondary),
            new ColorRoleItem(R.string.cat_color_role_on_secondary, com.google.android.material.R.attr.colorOnSecondary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_secondary_container, com.google.android.material.R.attr.colorSecondaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_secondary_container, com.google.android.material.R.attr.colorOnSecondaryContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_secondary_fixed, com.google.android.material.R.attr.colorSecondaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_secondary_fixed_dim, com.google.android.material.R.attr.colorSecondaryFixedDim)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_on_secondary_fixed, com.google.android.material.R.attr.colorOnSecondaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_on_secondary_fixed_variant,
                com.google.android.material.R.attr.colorOnSecondaryFixedVariant)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_tertiary, com.google.android.material.R.attr.colorTertiary),
            new ColorRoleItem(R.string.cat_color_role_on_tertiary, com.google.android.material.R.attr.colorOnTertiary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_tertiary_container, com.google.android.material.R.attr.colorTertiaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_tertiary_container, com.google.android.material.R.attr.colorOnTertiaryContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_tertiary_fixed, com.google.android.material.R.attr.colorTertiaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_tertiary_fixed_dim, com.google.android.material.R.attr.colorTertiaryFixedDim)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_on_tertiary_fixed, com.google.android.material.R.attr.colorOnTertiaryFixed),
            new ColorRoleItem(
                R.string.cat_color_role_on_tertiary_fixed_variant,
                com.google.android.material.R.attr.colorOnTertiaryFixedVariant)));
  }

  private List<ColorRow> getColorRolesUtility() {
    return Arrays.asList(
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_error, androidx.appcompat.R.attr.colorError),
            new ColorRoleItem(R.string.cat_color_role_on_error, com.google.android.material.R.attr.colorOnError)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_error_container, com.google.android.material.R.attr.colorErrorContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_error_container, com.google.android.material.R.attr.colorOnErrorContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_outline, com.google.android.material.R.attr.colorOutline),
            new ColorRoleItem(
                R.string.cat_color_role_outline_variant, com.google.android.material.R.attr.colorOutlineVariant)));
  }
}
