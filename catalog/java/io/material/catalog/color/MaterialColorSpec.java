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
import java.util.Arrays;
import java.util.List;

/**
 * Represents a color value as defined in the Material Spec. These are all defined by color name +
 * color value. ie. blue500.
 */
public final class MaterialColorSpec {
  public static final String SYSTEM_PREFIX = "system_";
  static final int TEXT_COLOR_SWITCH_VALUE = 400;

  @ColorRes private final int resourceId;
  private final String resourceName;
  private final String name;
  private final int value;

  MaterialColorSpec(int resourceId, String resourceName, String name, int value) {
    this.resourceId = resourceId;
    this.resourceName = resourceName;
    this.name = name;
    this.value = value;
  }

  @ColorRes
  int getResourceId() {
    return resourceId;
  }

  String getResourceName() {
    return resourceName;
  }

  String getName() {
    return name;
  }

  int getValue() {
    return value;
  }

  static MaterialColorSpec create(Context context, @ColorRes int colorRes) {
    String resName = context.getResources().getResourceEntryName(colorRes);
    String name;
    String value;
    if (resName.startsWith(SYSTEM_PREFIX)) {
      // Split the resource name into the color name and value, ie. system_accent1_500 to
      // system_accent1 and 500.
      int splitIndex = resName.lastIndexOf("_");
      name = resName.substring(0, splitIndex);
      value = resName.substring(splitIndex + 1);
    } else {
      // Get the name of the color an value without prefixes
      // String trimmedResName = resName;
      int splitIndex = resName.lastIndexOf("_");
      String trimmedResName = resName.substring(splitIndex + 1);
      // Split the resource name into the color name and value, ie. blue500 to blue and 500.
      List<String> parts = Arrays.asList(trimmedResName.split("(?<=\\D)(?=\\d)", -1));
      name = parts.get(0);
      value = parts.get(1);
    }
    return new MaterialColorSpec(colorRes, resName, name, Integer.parseInt(value));
  }
}
