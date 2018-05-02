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
import android.support.annotation.StyleRes;
import java.util.Arrays;

/** Utils for theme overlays. */
public abstract class ThemeOverlayUtils {

  @StyleRes private static int[] themeOverlays = new int[0];

  public static void setThemeOverlays(Activity activity, @StyleRes int... themeOverlays) {
    if (!Arrays.equals(ThemeOverlayUtils.themeOverlays, themeOverlays)) {
      ThemeOverlayUtils.themeOverlays = themeOverlays;
      activity.recreate();
    }
  }

  public static void clearThemeOverlays(Activity activity) {
    setThemeOverlays(activity);
  }

  @StyleRes
  public static int[] getThemeOverlays() {
    return themeOverlays;
  }

  public static void applyThemeOverlays(Activity activity) {
    for (int themeOverlay : themeOverlays) {
      activity.setTheme(themeOverlay);
    }
  }
}
