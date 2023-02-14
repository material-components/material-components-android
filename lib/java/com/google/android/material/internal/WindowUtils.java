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
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A util class for window operations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class WindowUtils {

  private static final String TAG = WindowUtils.class.getSimpleName();

  private WindowUtils() {}

  @NonNull
  public static Rect getCurrentWindowBounds(@NonNull Context context) {
    WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
      return Api30Impl.getCurrentWindowBounds(windowManager);
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
      return Api17Impl.getCurrentWindowBounds(windowManager);
    } else {
      return Api14Impl.getCurrentWindowBounds(windowManager);
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.R)
  private static class Api30Impl {

    @NonNull
    static Rect getCurrentWindowBounds(@NonNull WindowManager windowManager) {
      return windowManager.getCurrentWindowMetrics().getBounds();
    }
  }

  @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
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

  private static class Api14Impl {

    @NonNull
    static Rect getCurrentWindowBounds(@NonNull WindowManager windowManager) {
      Display defaultDisplay = windowManager.getDefaultDisplay();
      Point defaultDisplaySize = getRealSizeForDisplay(defaultDisplay);

      Rect bounds = new Rect();
      if (defaultDisplaySize.x == 0 || defaultDisplaySize.y == 0) {
        defaultDisplay.getRectSize(bounds);
      } else {
        bounds.right = defaultDisplaySize.x;
        bounds.bottom = defaultDisplaySize.y;
      }

      return bounds;
    }

    private static Point getRealSizeForDisplay(Display display) {
      Point size = new Point();
      try {
        Method getRealSizeMethod = Display.class.getDeclaredMethod("getRealSize", Point.class);
        getRealSizeMethod.setAccessible(true);
        getRealSizeMethod.invoke(display, size);
      } catch (NoSuchMethodException e) {
        Log.w(TAG, e);
      } catch (IllegalAccessException e) {
        Log.w(TAG, e);
      } catch (InvocationTargetException e) {
        Log.w(TAG, e);
      }
      return size;
    }
  }
}
