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

/**
 * Shape corner family preference to change the corner family to rounded or cut.
 */
public class ShapeCornerFamilyPreference extends CatalogPreference {
  private static final int OPTION_ID_ROUNDED = 1;
  private static final int OPTION_ID_CUT = 2;
  private static final int OPTION_ID_DEFAULT = 3;

  private static final SparseIntArray OPTION_ID_TO_THEME_OVERLAY = new SparseIntArray();
  static {
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_ROUNDED, R.style.ThemeOverlay_Shapes_Rounded);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_CUT, R.style.ThemeOverlay_Shapes_Cut);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_DEFAULT, ThemeOverlayUtils.NO_THEME_OVERLAY);
  }

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_DEFAULT,
          0,
          R.string.shape_corner_family_preference_option_default);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          new Option(
              OPTION_ID_ROUNDED,
              R.drawable.ic_rounded_corners_24px,
              R.string.shape_corner_family_preference_option_rounded),
          new Option(
              OPTION_ID_CUT,
              R.drawable.ic_cut_corners_24px,
              R.string.shape_corner_family_preference_option_cut),
          DEFAULT_OPTION);


  public ShapeCornerFamilyPreference() {
    super(R.string.shape_corner_family_preference_description);
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
