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
 * Shape corner size preference to change the corner size among small, medium, and large.
 */
public class ShapeCornerSizePreference extends CatalogPreference {
  private static final int OPTION_ID_SMALL = 1;
  private static final int OPTION_ID_MEDIUM = 2;
  private static final int OPTION_ID_LARGE = 3;
  private static final int OPTION_ID_DEFAULT = 4;

  private static final SparseIntArray OPTION_ID_TO_THEME_OVERLAY = new SparseIntArray();
  static {
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_SMALL, R.style.ThemeOverlay_ShapeSize_Small);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_MEDIUM, R.style.ThemeOverlay_ShapeSize_Medium);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_LARGE, R.style.ThemeOverlay_ShapeSize_Large);
    OPTION_ID_TO_THEME_OVERLAY.append(OPTION_ID_DEFAULT, ThemeOverlayUtils.NO_THEME_OVERLAY);
  }

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_DEFAULT,
          0,
          R.string.shape_corner_size_preference_option_default);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          new Option(
              OPTION_ID_SMALL,
              0,
              R.string.shape_corner_size_preference_option_small),
          new Option(
              OPTION_ID_MEDIUM,
              0,
              R.string.shape_corner_size_preference_option_medium),
          new Option(
              OPTION_ID_LARGE,
              0,
              R.string.shape_corner_size_preference_option_large),
          DEFAULT_OPTION);

  public ShapeCornerSizePreference() {
    super(R.string.shape_corner_size_preference_description);
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
        R.id.theme_feature_corner_size,
        OPTION_ID_TO_THEME_OVERLAY.get(selectedOption.id));
  }

  @Override
  protected boolean shouldRecreateActivityOnOptionChanged() {
    return true;
  }
}
