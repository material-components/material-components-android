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

package com.google.android.material.internal;

import static android.graphics.Color.TRANSPARENT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.color.MaterialColors.isColorLight;

import android.content.Context;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.Window;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.ColorUtils;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import com.google.android.material.color.MaterialColors;

/**
 * A util class that helps apply edge-to-edge mode to activity/dialog windows.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class EdgeToEdgeUtils {
  private static final int EDGE_TO_EDGE_BAR_ALPHA = 128;

  private EdgeToEdgeUtils() {}

  /**
   * Applies or removes edge-to-edge mode to the provided {@link Window}. When edge-to-edge mode is
   * applied, the activities, or the non-floating dialogs, that host the provided window will be
   * drawn over the system bar area by default and the system bar colors will be adjusted according
   * to the background color you provide.
   */
  public static void applyEdgeToEdge(@NonNull Window window, boolean edgeToEdgeEnabled) {
    applyEdgeToEdge(window, edgeToEdgeEnabled, null, null);
  }

  /**
   * Applies or removes edge-to-edge mode to the provided {@link Window}. When edge-to-edge mode is
   * applied, the activities, or the non-floating dialogs, that host the provided window will be
   * drawn over the system bar area by default and the system bar colors will be adjusted according
   * to the background color you provide.
   *
   * @param statusBarOverlapBackgroundColor The reference background color to decide the text/icon
   *        colors on status bars. {@code null} to use the default color from
   *        {@code ?android:attr/colorBackground}.
   * @param navigationBarOverlapBackgroundColor The reference background color to decide the icon
   *        colors on navigation bars.{@code null} to use the default color from
   *        {@code ?android:attr/colorBackground}.
   */
  public static void applyEdgeToEdge(
      @NonNull Window window,
      boolean edgeToEdgeEnabled,
      @Nullable @ColorInt Integer statusBarOverlapBackgroundColor,
      @Nullable @ColorInt Integer navigationBarOverlapBackgroundColor) {
    // If the overlapping background color is unknown or TRANSPARENT, use the default one.
    boolean useDefaultBackgroundColorForStatusBar =
        statusBarOverlapBackgroundColor == null || statusBarOverlapBackgroundColor == 0;
    boolean useDefaultBackgroundColorForNavigationBar =
        navigationBarOverlapBackgroundColor == null || navigationBarOverlapBackgroundColor == 0;
    if (useDefaultBackgroundColorForStatusBar || useDefaultBackgroundColorForNavigationBar) {
      int defaultBackgroundColor =
          MaterialColors.getColor(window.getContext(), android.R.attr.colorBackground, Color.BLACK);
      if (useDefaultBackgroundColorForStatusBar) {
        statusBarOverlapBackgroundColor = defaultBackgroundColor;
      }
      if (useDefaultBackgroundColorForNavigationBar) {
        navigationBarOverlapBackgroundColor = defaultBackgroundColor;
      }
    }

    WindowCompat.setDecorFitsSystemWindows(window, !edgeToEdgeEnabled);

    int statusBarColor = getStatusBarColor(window.getContext(), edgeToEdgeEnabled);
    int navigationBarColor = getNavigationBarColor(window.getContext(), edgeToEdgeEnabled);

    setStatusBarColor(window, statusBarColor);
    setNavigationBarColor(window, navigationBarColor);

    setLightStatusBar(
        window,
        isUsingLightSystemBar(statusBarColor, isColorLight(statusBarOverlapBackgroundColor)));
    setLightNavigationBar(
        window,
        isUsingLightSystemBar(
            navigationBarColor, isColorLight(navigationBarOverlapBackgroundColor)));
  }

  /**
   * Changes the foreground color of the status bars to light or dark so that the items on the bar
   * can be read clearly.
   *
   * @param window Window that hosts the status bars
   * @param isLight {@code true} to make the foreground color light
   */
  public static void setLightStatusBar(@NonNull Window window, boolean isLight) {
    WindowInsetsControllerCompat insetsController =
        WindowCompat.getInsetsController(window, window.getDecorView());
    insetsController.setAppearanceLightStatusBars(isLight);
  }

  /**
   * Changes the foreground color of the navigation bars to light or dark so that the items on the
   * bar can be read clearly.
   *
   * @param window Window that hosts the status bars
   * @param isLight {@code true} to make the foreground color light.
   */
  public static void setLightNavigationBar(@NonNull Window window, boolean isLight) {
    WindowInsetsControllerCompat insetsController =
        WindowCompat.getInsetsController(window, window.getDecorView());
    insetsController.setAppearanceLightNavigationBars(isLight);
  }

  private static int getStatusBarColor(Context context, boolean isEdgeToEdgeEnabled) {
    if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.M) {
      // Light status bars are only supported on M+. So we need to use a translucent black status
      // bar instead to ensure the text/icon contrast of it.
      int opaqueStatusBarColor =
          MaterialColors.getColor(context, android.R.attr.statusBarColor, Color.BLACK);
      return ColorUtils.setAlphaComponent(opaqueStatusBarColor, EDGE_TO_EDGE_BAR_ALPHA);
    }
    if (isEdgeToEdgeEnabled) {
      return TRANSPARENT;
    }
    return MaterialColors.getColor(context, android.R.attr.statusBarColor, Color.BLACK);
  }

  public static void setStatusBarColor(@NonNull Window window, @ColorInt int color) {
    if (VERSION.SDK_INT < VERSION_CODES.VANILLA_ICE_CREAM) {
      window.setStatusBarColor(color);
    }
  }

  private static int getNavigationBarColor(Context context, boolean isEdgeToEdgeEnabled) {
    // Light navigation bars are only supported on O_MR1+. So we need to use a translucent black
    // navigation bar instead to ensure the text/icon contrast of it.
    if (isEdgeToEdgeEnabled && VERSION.SDK_INT < VERSION_CODES.O_MR1) {
      int opaqueNavBarColor =
          MaterialColors.getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
      return ColorUtils.setAlphaComponent(opaqueNavBarColor, EDGE_TO_EDGE_BAR_ALPHA);
    }
    if (isEdgeToEdgeEnabled) {
      return TRANSPARENT;
    }
    return MaterialColors.getColor(context, android.R.attr.navigationBarColor, Color.BLACK);
  }

  public static int getNavigationBarColor(@NonNull Window window) {
    if (VERSION.SDK_INT < VERSION_CODES.VANILLA_ICE_CREAM) {
      return window.getNavigationBarColor();
    }
    return Color.TRANSPARENT;
  }

  public static void setNavigationBarColor(@NonNull Window window, @ColorInt int color) {
    if (VERSION.SDK_INT < VERSION_CODES.VANILLA_ICE_CREAM) {
      window.setNavigationBarColor(color);
    }
  }

  private static boolean isUsingLightSystemBar(int systemBarColor, boolean isLightBackground) {
    return isColorLight(systemBarColor) || (systemBarColor == TRANSPARENT && isLightBackground);
  }
}
