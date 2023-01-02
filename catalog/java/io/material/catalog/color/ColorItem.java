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
import androidx.annotation.NonNull;

/** A class for the items in the color palette. */
public class ColorItem implements ColorAdapterItem {

  private final MaterialColorSpec colorSpec;
  @ColorRes private final int colorRes;

  ColorItem(Context context, @ColorRes int colorRes) {
    this.colorRes = colorRes;
    colorSpec = MaterialColorSpec.createFromResource(context, colorRes);
  }

  /** Returns the resource ID of the color. */
  @NonNull
  @ColorRes
  public int getColorRes() {
    return colorRes;
  }

  /** Returns the resource name of the color, e.g. system_accent1_100. */
  @NonNull
  public String getColorResName() {
    return colorSpec.getDescription();
  }

  /** Returns the int value of the color. */
  @ColorInt
  int getColorValue() {
    return colorSpec.getColorValue();
  }
}
