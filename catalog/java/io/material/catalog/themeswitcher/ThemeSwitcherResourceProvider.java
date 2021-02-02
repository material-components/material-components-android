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
import androidx.annotation.AttrRes;
import androidx.annotation.StringRes;
import androidx.annotation.StyleableRes;

/** A helper class that facilitates overriding of theme switcher resources in the Catalog app. */
public class ThemeSwitcherResourceProvider {

  @StyleableRes
  private static final int[] PRIMARY_THEME_OVERLAY_ATTRS = {
      R.attr.colorPrimary, R.attr.colorPrimaryDark
  };

  @StyleableRes private static final int[] SECONDARY_THEME_OVERLAY_ATTRS = {R.attr.colorSecondary};

  @StyleableRes
  public int[] getPrimaryThemeOverlayAttrs() {
    return PRIMARY_THEME_OVERLAY_ATTRS;
  };

  @StyleableRes
  public int[] getSecondaryThemeOverlayAttrs() {
    return SECONDARY_THEME_OVERLAY_ATTRS;
  }

  @AttrRes
  public int getPrimaryColor() {
    return R.attr.colorPrimary;
  }

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

  @StringRes
  public int getPrimaryColorsGroupDescription() {
    return R.string.mtrl_primary_color_description;
  }

  @StringRes
  public int getSecondaryColorsGroupDescription() {
    return R.string.mtrl_secondary_color_description;
  }

  public int getShapes() {
    return R.array.mtrl_shape_overlays;
  }

  public int getShapesContentDescription() {
    return R.array.mtrl_shapes_content_description;
  }

  @StringRes
  public int getShapesGroupDescription() {
    return R.string.mtrl_shape_corner_family;
  }

  public int getShapeSizes() {
    return R.array.mtrl_shape_size_overlays;
  }

  public int getShapeSizesContentDescription() {
    return R.array.mtrl_shape_size_content_description;
  }

  @StringRes
  public int getShapeSizesGroupDescription() {
    return R.string.mtrl_shape_corner_size;
  }
}
