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

import android.content.Context;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.StringRes;
import androidx.core.content.ContextCompat;

/**
 * Represents a color value as defined in the Material Spec.
 */
final class MaterialColorSpec {

  private final String description;
  @ColorInt private final int colorValue;

  MaterialColorSpec(String description, int colorValue) {
    this.description = description;
    this.colorValue = colorValue;
  }

  String getDescription() {
    return description;
  }

  @ColorInt
  int getColorValue() {
    return colorValue;
  }

  static MaterialColorSpec createFromResource(Context context, @ColorRes int colorRes) {
    return new MaterialColorSpec(
        context.getResources().getResourceEntryName(colorRes),
        ContextCompat.getColor(context, colorRes));
  }

  static MaterialColorSpec createFromColorValue(
      Context context, @StringRes int colorNameResId, @ColorInt int colorValue) {
    return new MaterialColorSpec(context.getString(colorNameResId), colorValue);
  }
}
