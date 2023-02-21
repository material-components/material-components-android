/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.color;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources.Theme;
import android.view.View;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;

// TODO(b/269781013): move this class to internal folder, which involves resolving cyclic dependency
//           between color and internal folders
/**
 * Utility methods for theme.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class ThemeUtils {

  private ThemeUtils() {}

  public static void applyThemeOverlay(@NonNull Context context, @StyleRes int theme) {
    // Use applyStyle() instead of setTheme() due to Force Dark issue.
    context.getTheme().applyStyle(theme, /* force= */ true);

    // Make sure the theme overlay is applied to the Window decorView similar to Activity#setTheme,
    // to ensure that it will be applied to things like ContextMenu using the DecorContext.
    if (context instanceof Activity) {
      Theme windowDecorViewTheme = getWindowDecorViewTheme((Activity) context);
      if (windowDecorViewTheme != null) {
        windowDecorViewTheme.applyStyle(theme, /* force= */ true);
      }
    }
  }

  @Nullable
  private static Theme getWindowDecorViewTheme(@NonNull Activity activity) {
    Window window = activity.getWindow();
    if (window != null) {
      // Use peekDecorView() instead of getDecorView() to avoid locking the Window.
      View decorView = window.peekDecorView();
      if (decorView != null) {
        Context context = decorView.getContext();
        if (context != null) {
          return context.getTheme();
        }
      }
    }
    return null;
  }
}
