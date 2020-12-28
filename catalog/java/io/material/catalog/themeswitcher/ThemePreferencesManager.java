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

package io.material.catalog.themeswitcher;

import io.material.catalog.R;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.PopupMenu;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.util.SparseIntArray;
import android.view.MenuItem;
import android.view.View;
import androidx.annotation.IdRes;
import com.google.android.material.color.MaterialColors;

/** Helper that saves the current theme choice for the Catalog, which maps to a night mode. */
public class ThemePreferencesManager {

  private static final String PREFERENCES_NAME = "night_mode_preferences";
  private static final String KEY_NIGHT_MODE = "night_mode";
  private static final SparseIntArray THEME_NIGHT_MODE_MAP = new SparseIntArray();

  static {
    THEME_NIGHT_MODE_MAP.append(R.id.theme_light, AppCompatDelegate.MODE_NIGHT_NO);
    THEME_NIGHT_MODE_MAP.append(R.id.theme_dark, AppCompatDelegate.MODE_NIGHT_YES);
    THEME_NIGHT_MODE_MAP.append(R.id.theme_default, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  }

  private final Context context;
  private final ThemeSwitcherResourceProvider resourceProvider;

  public ThemePreferencesManager(Context context, ThemeSwitcherResourceProvider resourceProvider) {
    this.context = context;
    this.resourceProvider = resourceProvider;
  }

  @SuppressWarnings("RestrictTo")
  public void showChooseThemePopup(View anchor) {
    PopupMenu popupMenu = new PopupMenu(context, anchor);
    popupMenu.inflate(R.menu.mtrl_choose_theme_menu);
    if (popupMenu.getMenu() instanceof MenuBuilder) {
      MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();

      menuBuilder.setOptionalIconsVisible(true);

      ColorStateList defaultColor =
          AppCompatResources.getColorStateList(
              context, R.color.material_on_surface_emphasis_medium);
      int selectedColor = MaterialColors.getColor(anchor, resourceProvider.getPrimaryColor());
      int currentThemeId = getCurrentThemeId();
      for (int i = 0; i < menuBuilder.size(); i++) {
        MenuItem item = menuBuilder.getItem(i);
        if (item.getItemId() == currentThemeId) {
          DrawableCompat.setTint(item.getIcon(), selectedColor);

          SpannableString s = new SpannableString(item.getTitle());
          s.setSpan(new ForegroundColorSpan(selectedColor), 0, s.length(), 0);
          item.setTitle(s);
        } else {
          DrawableCompat.setTintList(item.getIcon(), defaultColor);
        }
      }
    }
    popupMenu.setOnMenuItemClickListener(
        item -> {
          saveAndApplyTheme(item.getItemId());
          return false;
        });
    popupMenu.show();
  }

  public void saveAndApplyTheme(@IdRes int id) {
    int nightMode = convertToNightMode(id);
    saveNightMode(nightMode);
    AppCompatDelegate.setDefaultNightMode(nightMode);
  }

  public void applyTheme() {
    AppCompatDelegate.setDefaultNightMode(getNightMode());
  }

  @IdRes
  public int getCurrentThemeId() {
    return convertToThemeId(getNightMode());
  }

  public int[] getThemeIds() {
    int[] themeIds = new int[THEME_NIGHT_MODE_MAP.size()];
    for (int i = 0; i < THEME_NIGHT_MODE_MAP.size(); i++) {
      themeIds[i] = THEME_NIGHT_MODE_MAP.keyAt(i);
    }
    return themeIds;
  }

  private int getNightMode() {
    return getSharedPreferences()
        .getInt(KEY_NIGHT_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  }

  private void saveNightMode(int nightMode) {
    getSharedPreferences().edit().putInt(KEY_NIGHT_MODE, nightMode).commit();
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }

  private int convertToNightMode(@IdRes int id) {
    return THEME_NIGHT_MODE_MAP.get(id, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
  }

  @IdRes
  private int convertToThemeId(int nightMode) {
    return THEME_NIGHT_MODE_MAP.keyAt(THEME_NIGHT_MODE_MAP.indexOfValue(nightMode));
  }
}
