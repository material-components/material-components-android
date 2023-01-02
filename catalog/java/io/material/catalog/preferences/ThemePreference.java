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
import androidx.appcompat.app.AppCompatDelegate;
import android.util.SparseIntArray;
import androidx.annotation.NonNull;
import com.google.common.collect.ImmutableList;

/**
 * Night mode preference to enforce light/dark theme or use system default.
 */
public class ThemePreference extends CatalogPreference {
  private static final int OPTION_ID_LIGHT = 1;
  private static final int OPTION_ID_DARK = 2;
  private static final int OPTION_ID_SYSTEM_DEFAULT = 3;

  private static final SparseIntArray OPTION_ID_TO_NIGHT_MODE = new SparseIntArray();
  static {
    OPTION_ID_TO_NIGHT_MODE.append(OPTION_ID_LIGHT, AppCompatDelegate.MODE_NIGHT_NO);
    OPTION_ID_TO_NIGHT_MODE.append(OPTION_ID_DARK, AppCompatDelegate.MODE_NIGHT_YES);
    OPTION_ID_TO_NIGHT_MODE.append(
        OPTION_ID_SYSTEM_DEFAULT, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  }

  private static final Option DEFAULT_OPTION =
      new Option(
          OPTION_ID_SYSTEM_DEFAULT,
          R.drawable.ic_theme_default_24px,
          R.string.theme_preference_option_system_default);

  private static final ImmutableList<Option> OPTIONS =
      ImmutableList.of(
          new Option(
              OPTION_ID_LIGHT,
              R.drawable.ic_theme_light_24px,
              R.string.theme_preference_option_light),
          new Option(
              OPTION_ID_DARK,
              R.drawable.ic_theme_dark_24px,
              R.string.theme_preference_option_dark),
          DEFAULT_OPTION);

  public ThemePreference() {
    super(R.string.theme_preference_description);
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
    AppCompatDelegate.setDefaultNightMode(
        OPTION_ID_TO_NIGHT_MODE.get(selectedOption.id));
  }
}
