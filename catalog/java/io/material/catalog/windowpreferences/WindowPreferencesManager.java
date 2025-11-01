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

package io.material.catalog.windowpreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Window;
import android.view.WindowInsets;
import androidx.activity.ComponentActivity;
import androidx.activity.EdgeToEdge;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsControllerCompat;

/** Helper that saves the current window preferences for the Catalog. */
public class WindowPreferencesManager {

  private static final String PREFERENCES_NAME = "window_preferences";
  private static final String KEY_EDGE_TO_EDGE_ENABLED = "edge_to_edge_enabled";

  private final Context context;
  private final OnApplyWindowInsetsListener listener;

  public WindowPreferencesManager(Context context) {
    this.context = context;
    this.listener =
        (v, insets) -> {
          int leftInset = insets.getStableInsetLeft();
          int rightInset = insets.getStableInsetRight();
          if (VERSION.SDK_INT >= VERSION_CODES.R) {
            leftInset = insets.getInsets(WindowInsets.Type.systemBars()).left;
            rightInset = insets.getInsets(WindowInsets.Type.systemBars()).right;
          }

          v.setPadding(leftInset, 0, rightInset, 0);
          return insets;
        };
  }

  @SuppressWarnings("ApplySharedPref")
  public void toggleEdgeToEdgeEnabled() {
    getSharedPreferences()
        .edit()
        .putBoolean(KEY_EDGE_TO_EDGE_ENABLED, !isEdgeToEdgeEnabled())
        .commit();
  }

  public boolean isEdgeToEdgeEnabled() {
    return getSharedPreferences()
        .getBoolean(KEY_EDGE_TO_EDGE_ENABLED, VERSION.SDK_INT >= VERSION_CODES.Q);
  }

  public void applyEdgeToEdgePreference(Window window) {
    boolean dark = (window.getContext().getResources().getConfiguration().uiMode &
        Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES;
    WindowInsetsControllerCompat windowInsetsControllerCompat =
        new WindowInsetsControllerCompat(window, window.getDecorView());
    if (isEdgeToEdgeEnabled()) {
      if (window.getContext() instanceof ComponentActivity) {
        EdgeToEdge.enable((ComponentActivity) window.getContext());
      } else {
        window.setNavigationBarColor(0);
        if (!dark) {
          windowInsetsControllerCompat.setAppearanceLightNavigationBars(true);
        }
      }
    } else {
      if (window.getContext() instanceof ComponentActivity) {
        windowInsetsControllerCompat.setAppearanceLightStatusBars(dark);
      }
    }
    ViewCompat.setOnApplyWindowInsetsListener(
        window.getDecorView(), isEdgeToEdgeEnabled() ? listener : null);
  }

  private SharedPreferences getSharedPreferences() {
    return context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE);
  }
}
