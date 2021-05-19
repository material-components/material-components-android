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

package io.material.catalog.preferences;

import io.material.catalog.R;

import android.content.Context;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;
import io.material.catalog.themeswitcher.ThemeOverlayUtils;

/**
 * Shape preference to change the corner family to rounded or cut.
 */
public class ShapePreference extends CatalogPreference {
  private static final int OPTION_ID_ROUNDED_CORNER = 1;
  private static final int OPTION_ID_CUT_CORNER = 2;

  private static final SparseIntArray OPTION_ID_TO_THEME_OVERLAY = new SparseIntArray();
  static {
    OPTION_ID_TO_THEME_OVERLAY.append(
        OPTION_ID_ROUNDED_CORNER, R.style.ThemeOverlay_Shapes_Rounded);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_CUT_CORNER, R.style.ThemeOverlay_Shapes_Cut);
  }

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_ROUNDED_CORNER,
          R.drawable.ic_rounded_corners_24px,
          R.string.shape_preference_option_rounded_corner);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          DEFAULT_OPTION,
          new Option(
              OPTION_ID_CUT_CORNER,
              R.drawable.ic_cut_corners_24px,
              R.string.shape_preference_option_cut_corner));


  public ShapePreference() {
    super(R.string.shape_preference_description);
  }

  @Override
  @NonNull
  protected ImmutableList<Option> getOptions() {
    return OPTIONS;
  }

  @Override
  @NonNull
  protected Option getDefaultOption() {
    return DEFAULT_OPTION;
  }

  @Override
  protected void apply(@NonNull Context context, @NonNull Option selectedOption) {
    ThemeOverlayUtils.setThemeOverlay(
        R.id.theme_feature_corner_family,
        OPTION_ID_TO_THEME_OVERLAY.get(selectedOption.id));
  }

  @Override
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return true;
  }
}
