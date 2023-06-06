/*
 * Copyright 2022 The Android Open Source Project
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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import com.google.android.material.color.ColorRoles;

/** A class to form side-by-side color palettes. */
final class ColorGrid {

  @NonNull private final MaterialColorSpec materialColorSpecAccent;
  @NonNull private final MaterialColorSpec materialColorSpecOnAccent;
  @NonNull private final MaterialColorSpec materialColorSpecAccentContainer;
  @NonNull private final MaterialColorSpec materialColorSpecOnAccentContainer;

  static ColorGrid createFromColorGridData(ColorGridData colorGridData) {
    ColorRoles colorRoles = colorGridData.getColorRoles();
    ColorRoleNames colorRoleNames = colorGridData.getColorRoleNames();
    MaterialColorSpec[] materialColorSpecs =
        new MaterialColorSpec[] {
          MaterialColorSpec.createFromColorValue(
              colorRoleNames.getAccentName(), colorRoles.getAccent()),
          MaterialColorSpec.createFromColorValue(
              colorRoleNames.getOnAccentName(), colorRoles.getOnAccent()),
          MaterialColorSpec.createFromColorValue(
              colorRoleNames.getAccentContainerName(), colorRoles.getAccentContainer()),
          MaterialColorSpec.createFromColorValue(
              colorRoleNames.getOnAccentContainerName(), colorRoles.getOnAccentContainer()),
        };
    return new ColorGrid(
        materialColorSpecs[0], materialColorSpecs[1], materialColorSpecs[2], materialColorSpecs[3]);
  }

  static ColorGrid createFromAttrResId(Context context, String[] colorNames, int[] attrResIds) {
    if (colorNames.length < 4 || colorNames.length != attrResIds.length) {
      throw new IllegalArgumentException(
          "Color names need to be at least four and correspond to attribute resource ids.");
    }
    return new ColorGrid(
        MaterialColorSpec.createFromAttrResId(context, colorNames[0], attrResIds[0]),
        MaterialColorSpec.createFromAttrResId(context, colorNames[1], attrResIds[1]),
        MaterialColorSpec.createFromAttrResId(context, colorNames[2], attrResIds[2]),
        MaterialColorSpec.createFromAttrResId(context, colorNames[3], attrResIds[3]));
  }

  private ColorGrid(
      @NonNull MaterialColorSpec materialColorSpecAccent,
      @NonNull MaterialColorSpec materialColorSpecOnAccent,
      @NonNull MaterialColorSpec materialColorSpecAccentContainer,
      @NonNull MaterialColorSpec materialColorSpecOnAccentContainer) {
    this.materialColorSpecAccent = materialColorSpecAccent;
    this.materialColorSpecOnAccent = materialColorSpecOnAccent;
    this.materialColorSpecAccentContainer = materialColorSpecAccentContainer;
    this.materialColorSpecOnAccentContainer = materialColorSpecOnAccentContainer;
  }

  View renderView(LayoutInflater layoutInflater, ViewGroup container) {
    View catalogColorsGrid =
        layoutInflater.inflate(R.layout.cat_colors_grid, container, /* attachToRoot= */ false);

    bindColorSpecItem(catalogColorsGrid, R.id.cat_color_accent, materialColorSpecAccent);
    bindColorSpecItem(catalogColorsGrid, R.id.cat_color_on_accent, materialColorSpecOnAccent);
    bindColorSpecItem(
        catalogColorsGrid, R.id.cat_color_accent_container, materialColorSpecAccentContainer);
    bindColorSpecItem(
        catalogColorsGrid, R.id.cat_color_on_accent_container, materialColorSpecOnAccentContainer);

    return catalogColorsGrid;
  }

  private static void bindColorSpecItem(
      View gridView, @IdRes int textViewId, MaterialColorSpec materialColorSpec) {
    TextView colorSpec = gridView.findViewById(textViewId);

    colorSpec.setText(materialColorSpec.getDescription());
    colorSpec.setTextColor(ColorDemoUtils.getTextColor(materialColorSpec.getColorValue()));
    colorSpec.setBackgroundColor(materialColorSpec.getColorValue());
  }
}
