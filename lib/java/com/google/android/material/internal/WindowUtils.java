/*
 * Copyright (C) 2023 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Build;
import android.view.Display;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;

/**
 * A util class for window operations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class WindowUtils {

  private WindowUtils() {}

  @NonNull
  public static Rect getCurrentWindowBounds(@NonNull Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Api30Impl.getCurrentWindowBounds(windowManager);
    } else {
      return Api17Impl.getCurrentWindowBounds(windowManager);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private static class Api30Impl {

    @NonNull
    static Rect getCurrentWindowBounds(@NonNull WindowManager windowManager) {
      return windowManager.getCurrentWindowMetrics().getBounds();
    }
  }

  private static class Api17Impl {

    @NonNull
    static Rect getCurrentWindowBounds(@NonNull WindowManager windowManager) {
      Display defaultDisplay = windowManager.getDefaultDisplay();

      Point defaultDisplaySize = new Point();
      defaultDisplay.getRealSize(defaultDisplaySize);

      Rect bounds = new Rect();
      bounds.right = defaultDisplaySize.x;
      bounds.bottom = defaultDisplaySize.y;

      return bounds;
    }
  }
}
