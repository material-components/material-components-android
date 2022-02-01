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
            new ColorRoleItem(R.string.cat_color_role_on_background, R.attr.colorOnBackground)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_surface, R.attr.colorSurface),
            new ColorRoleItem(R.string.cat_color_role_on_surface, R.attr.colorOnSurface)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_surface_variant, R.attr.colorSurfaceVariant),
            new ColorRoleItem(
                R.string.cat_color_role_on_surface_variant, R.attr.colorOnSurfaceVariant)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_inverse_surface, R.attr.colorSurfaceInverse),
            new ColorRoleItem(
                R.string.cat_color_role_inverse_on_surface, R.attr.colorOnSurfaceInverse)));
  }

  private List<ColorRow> getColorRolesContent() {
    return Arrays.asList(
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_primary, R.attr.colorPrimary),
            new ColorRoleItem(R.string.cat_color_role_on_primary, R.attr.colorOnPrimary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_primary_container, R.attr.colorPrimaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_primary_container, R.attr.colorOnPrimaryContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_inverse_primary, R.attr.colorPrimaryInverse),
            /* colorRoleItemRight= */ null),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_secondary, R.attr.colorSecondary),
            new ColorRoleItem(R.string.cat_color_role_on_secondary, R.attr.colorOnSecondary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_secondary_container, R.attr.colorSecondaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_secondary_container, R.attr.colorOnSecondaryContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_tertiary, R.attr.colorTertiary),
            new ColorRoleItem(R.string.cat_color_role_on_tertiary, R.attr.colorOnTertiary)),
        new ColorRow(
            new ColorRoleItem(
                R.string.cat_color_role_tertiary_container, R.attr.colorTertiaryContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_tertiary_container, R.attr.colorOnTertiaryContainer)));
  }

  private List<ColorRow> getColorRolesUtility() {
    return Arrays.asList(
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_error, R.attr.colorError),
            new ColorRoleItem(R.string.cat_color_role_on_error, R.attr.colorOnError)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_error_container, R.attr.colorErrorContainer),
            new ColorRoleItem(
                R.string.cat_color_role_on_error_container, R.attr.colorOnErrorContainer)),
        new ColorRow(
            new ColorRoleItem(R.string.cat_color_role_outline, R.attr.colorOutline),
            /* colorRoleItemRight= */ null));
  }
}
