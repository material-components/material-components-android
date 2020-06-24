/*
 * Copyright 2017 The Android Open Source Project
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

import android.app.Activity;
import android.util.SparseIntArray;
import androidx.annotation.IdRes;
import androidx.annotation.StyleRes;

/** Utils for theme themeOverlays. */
public class ThemeOverlayUtils {

  private ThemeOverlayUtils() { }

  private static final SparseIntArray themeOverlays = new SparseIntArray();

  public static void setThemeOverlay(@IdRes int id, @StyleRes int themeOverlay) {
    themeOverlays.put(id, themeOverlay);
  }

  public static void clearThemeOverlays(Activity activity) {
    themeOverlays.clear();
    activity.recreate();
  }

  public static int getThemeOverlay(@IdRes int id) {
    return themeOverlays.get(id);
  }

  public static void applyThemeOverlays(Activity activity) {
    for (int i = 0; i < themeOverlays.size(); ++i) {
      activity.setTheme(themeOverlays.valueAt(i));
    }
  }
}
