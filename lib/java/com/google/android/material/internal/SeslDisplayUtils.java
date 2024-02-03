/*
 * Copyright 2024 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.internal;

import android.content.Context;
import android.provider.Settings;
import android.util.Log;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

public class SeslDisplayUtils {
  private static final String TAG = "SeslDisplayUtils";

  public static boolean isPinEdgeEnabled(Context context) {
    try {
      return Settings.System.getInt(context.getContentResolver(), "panel_mode", 0) == 1;
    } catch (Exception e) {
      Log.w(TAG, "Failed get panel mode " + e.toString());
      return false;
    }
  }

  public static int getPinnedEdgeWidth(Context context) {
    try {
      return Settings.System.getInt(context.getContentResolver(), "pinned_edge_width");
    } catch (Settings.SettingNotFoundException e) {
      Log.w(TAG, "Failed get EdgeWidth " + e.toString());
      return 0;
    }
  }

  public static int getEdgeArea(Context context) {
    return Settings.System.getInt(context.getContentResolver(), "active_edge_area", 1);
  }
}
