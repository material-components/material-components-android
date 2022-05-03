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

import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.AttrRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.color.MaterialColors;

/**
 * A class for the items in a color row. A ColorRow consists of two {@link ColorRoleItem} objects.
 * The left colorRoleItem represents the Container Color and right colorRoleItem represents the
 * Content Color.
 */
final class ColorRow {

  @NonNull private final ColorRoleItem colorRoleItemLeft;
  @Nullable private final ColorRoleItem colorRoleItemRight;

  private View catColorsSchemeRow;

  ColorRow(@NonNull ColorRoleItem colorRoleItemLeft, @Nullable ColorRoleItem colorRoleItemRight) {
    this.colorRoleItemLeft = colorRoleItemLeft;
    this.colorRoleItemRight = colorRoleItemRight;
  }

  void addTo(@NonNull LayoutInflater layoutInflater, LinearLayout layout) {
    catColorsSchemeRow =
        layoutInflater.inflate(R.layout.cat_colors_scheme_row, layout, /* attachToRoot= */ false);

    bindColorRoleItem(
        catColorsSchemeRow,
        R.id.cat_color_role_left,
        colorRoleItemLeft.getColorRoleStringResId(),
        colorRoleItemLeft.getColorRoleAttrResId());
    if (colorRoleItemRight != null) {
      bindColorRoleItem(
          catColorsSchemeRow,
          R.id.cat_color_role_right,
          colorRoleItemRight.getColorRoleStringResId(),
          colorRoleItemRight.getColorRoleAttrResId());
    }

    layout.addView(catColorsSchemeRow);
  }

  private void bindColorRoleItem(
      View view,
      @IdRes int textViewId,
      @StringRes int colorRoleTextResID,
      @AttrRes int colorAttrResId) {
    TextView colorRole = view.findViewById(textViewId);

    colorRole.setText(colorRoleTextResID);
    colorRole.setTextColor(
        ColorDemoUtils.getTextColor(MaterialColors.getColor(catColorsSchemeRow, colorAttrResId)));
    colorRole.setBackgroundColor(MaterialColors.getColor(catColorsSchemeRow, colorAttrResId));
  }
}
