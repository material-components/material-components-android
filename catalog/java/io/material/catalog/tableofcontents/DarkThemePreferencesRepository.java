/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.tableofcontents;

import android.content.Context;
import android.content.SharedPreferences;

class DarkThemePreferencesRepository {

  private static final String PREFERENCES_NAME = "dark_theme_preferences";
  private static final String KEY_DARK_THEME_ENABLED = "dark_theme_enabled";

  private final Context context;

  DarkThemePreferencesRepository(Context context) {
    this.context = context;
  }

  boolean isDarkThemeEnabled() {
    return getSharedPreferences().getBoolean(KEY_DARK_THEME_ENABLED, false);
  }

  void saveDarkThemeEnabled(boolean enabled) {
    getSharedPreferences().edit().putBoolean(KEY_DARK_THEME_ENABLED, enabled).commit();
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }
}
