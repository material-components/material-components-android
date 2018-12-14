/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.themeswitcher;

import io.material.catalog.R;

import androidx.annotation.ArrayRes;

/** A helper class that facilitates overriding of theme switcher resources in the Catalog app. */
public class ThemeSwitcherResourceProvider {

  @ArrayRes
  public int getPrimaryColors() {
    return R.array.mtrl_primary_palettes;
  }

  @ArrayRes
  public int getSecondaryColors() {
    return R.array.mtrl_secondary_palettes;
  }

  @ArrayRes
  public int getPrimaryColorsContentDescription() {
    return R.array.mtrl_palettes_content_description;
  }

  @ArrayRes
  public int getSecondaryColorsContentDescription() {
    return R.array.mtrl_palettes_content_description;
  }
}
