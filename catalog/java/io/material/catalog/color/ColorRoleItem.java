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

import androidx.annotation.AttrRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

/** A class for an item in the color scheme. (e.g. colorPrimary, colorSecondary, etc.) */
final class ColorRoleItem {

  @StringRes private final int colorRoleStringResId;
  @AttrRes private final int colorRoleAttrResId;

  ColorRoleItem(@StringRes int colorRoleStringResId, @AttrRes int colorRoleAttrResId) {
    this.colorRoleStringResId = colorRoleStringResId;
    this.colorRoleAttrResId = colorRoleAttrResId;
  }

  /** Returns the attr resource id of the color. */
  @NonNull
  @AttrRes
  public int getColorRoleAttrResId() {
    return colorRoleAttrResId;
  }

  /** Returns the string resource id of the color. */
  @NonNull
  @StringRes
  public int getColorRoleStringResId() {
    return colorRoleStringResId;
  }
}
