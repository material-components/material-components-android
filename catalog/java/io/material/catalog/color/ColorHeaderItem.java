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
import androidx.annotation.ColorRes;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.List;

/** A class for the headers in the color palette. */
public class ColorHeaderItem implements ColorAdapterItem {

  public static final String SYSTEM_PREFIX = "system_";
  public static final String MATERIAL_CUSTOM_PALETTE_NAME_SEARCH_WORD = "_ref_palette_dynamic_";
  public static final String MATERIAL_CUSTOM_PALETTE_TITLE_PREFIX = "Material custom ";
  private static final String COLOR_600 = "600";

  @ColorRes private final int backgroundColorRes;
  private final String description;

  ColorHeaderItem(Context context, List<ColorItem> colors) {
    ColorItem sample = colors.get(0);
    for (ColorItem color : colors) {
      if (color.getColorResName().contains(COLOR_600)) {
        sample = color;
        break;
      }
    }
    backgroundColorRes = sample.getColorRes();
    description = sample.getColorResName();
  }

  @ColorRes
  int getBackgroundColorRes() {
    return backgroundColorRes;
  }

  /** Returns the display name for the header, e.g. Blue. */
  @NonNull
  public String getDisplayName() {
    String name;
    int splitIndex = description.lastIndexOf("_");
    if (description.startsWith(SYSTEM_PREFIX)) {
      // Split the resource name into the color name and value, i.e., system_accent1_500 to
      // system_accent1 and 500.
      name = description.substring(0, splitIndex);
    } else if (description.contains(MATERIAL_CUSTOM_PALETTE_NAME_SEARCH_WORD)) {
      // Get the name of the color and value without the search word.
      splitIndex = description.lastIndexOf(MATERIAL_CUSTOM_PALETTE_NAME_SEARCH_WORD);
      String trimmedResName =
          description.substring(splitIndex + MATERIAL_CUSTOM_PALETTE_NAME_SEARCH_WORD.length());
      // Split the resource name into the color name and value, i.e., neutral92 to neutral and 92.
      List<String> parts = Arrays.asList(trimmedResName.split("(?<=\\D)(?=\\d)", -1));
      name = MATERIAL_CUSTOM_PALETTE_TITLE_PREFIX + parts.get(0);
    } else {
      // Get the name of the color and value without prefixes
      String trimmedResName = description.substring(splitIndex + 1);
      // Split the resource name into the color name and value, i.e., blue500 to blue and 500.
      List<String> parts = Arrays.asList(trimmedResName.split("(?<=\\D)(?=\\d)", -1));
      name = parts.get(0);
    }
    String headerColor = name.replace('_', ' ');
    return Character.toUpperCase(headerColor.charAt(0)) + headerColor.substring(1);
  }
}
