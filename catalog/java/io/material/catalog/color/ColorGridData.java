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

import android.content.Context;
import androidx.annotation.ArrayRes;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import com.google.android.material.color.ColorRoles;
import com.google.android.material.color.MaterialColors;

/** A class that provides data for {@link ColorGrid}. */
final class ColorGridData {

  private final ColorRoles colorRoles;
  private final ColorRoleNames colorRoleNames;

  static ColorGridData createFromColorResId(
      Context context, @ColorRes int colorResourceId, @ArrayRes int colorNameIds) {
    return createFromColorValue(
        context, context.getResources().getColor(colorResourceId), colorNameIds);
  }

  static ColorGridData createFromColorValue(
      Context context, @ColorInt int seedColorValue, @ArrayRes int colorNameIds) {
    String[] colorNames = context.getResources().getStringArray(colorNameIds);
    return new ColorGridData(
        MaterialColors.getColorRoles(context, seedColorValue),
        new ColorRoleNames(colorNames[0], colorNames[1], colorNames[2], colorNames[3]));
  }

  private ColorGridData(ColorRoles colorRoles, ColorRoleNames colorRoleNames) {
    this.colorRoles = colorRoles;
    this.colorRoleNames = colorRoleNames;
  }

  ColorRoles getColorRoles() {
    return colorRoles;
  }

  ColorRoleNames getColorRoleNames() {
    return colorRoleNames;
  }
}
