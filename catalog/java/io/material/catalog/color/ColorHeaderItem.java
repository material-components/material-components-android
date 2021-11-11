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
import java.util.List;

/** A class for the headers in the color palette. */
public class ColorHeaderItem implements ColorAdapterItem {

  private static final String COLOR_600 = "600";

  @ColorRes private final int backgroundColor;
  private final String name;

  ColorHeaderItem(Context context, List<ColorItem> colors) {
    ColorItem sample = colors.get(0);
    for (ColorItem color : colors) {
      if (color.getColorResName().contains(COLOR_600)) {
        sample = color;
        break;
      }
    }
    MaterialColorSpec materialColor = MaterialColorSpec.create(context, sample.getColorRes());
    backgroundColor = materialColor.getResourceId();
    name = materialColor.getName();
  }

  @ColorRes
  int getBackgroundColorRes() {
    return backgroundColor;
  }

  /** Returns the raw name of the color. */
  @NonNull
  public String getName() {
    return name;
  }

  /** Returns the display name for the header, e.g. Blue. */
  @NonNull
  public String getDisplayName() {
    String name = getName();
    String headerColor = name.replace('_', ' ');
    return Character.toUpperCase(headerColor.charAt(0)) + headerColor.substring(1);
  }
}
