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

package com.google.android.material.color;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.UiModeManager;
import android.app.UiModeManager.ContrastChangeListener;
import android.content.Context;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Utility for applying contrast colors to application/activities.
 *
 * <p>Please note that if you are already using dynamic colors, contrast will be applied
 * automatically on Android U+. This is only needed if you have a branded or custom theme and want
 * to support contrast.
 */
public class ColorContrast {

  private static final float MEDIUM_CONTRAST_THRESHOLD = 1 / 3f;
  private static final float HIGH_CONTRAST_THRESHOLD = 2 / 3f;

  private ColorContrast() {}

  /**
   * Applies contrast to all activities by registering a {@link ActivityLifecycleCallbacks} to your
   * application.
   *
   * <p>A normal usage of this method should happen only once in {@link Application#onCreate()} or
   * any methods that run before any of your activities are created. For example:
   *
   * <pre>
   * public class YourApplication extends Application {
   *   &#64;Override
   *   public void onCreate() {
   *     super.onCreate();
   *     ColorContrast.applyToActivitiesIfAvailable(this);
   *   }
   * }
   * </pre>
   *
   * <p>This method will try to apply a theme overlay in every activity's {@link
   * ActivityLifecycleCallbacks#onActivityPreCreated(Activity, Bundle)} callback.
   *
   * @param application The target application.
   * @param colorContrastOptions The color contrast options object that specifies the theme overlay
   *     resource IDs for medium and high contrast mode.
   */
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @NonNull ColorContrastOptions colorContrastOptions) {
    if (!isContrastAvailable()) {
      return;
    }

    application.registerActivityLifecycleCallbacks(
        new ColorContrastActivityLifecycleCallbacks(colorContrastOptions));
  }

  /**
   * Applies contrast to the given activity.
   *
   * <p>Note that this method does not guarantee the consistency of contrast throughout the app. If
   * you want contrast to be updated automatically when a different contrast level is selected in
   * the system, please use #applyToActivitiesIfAvailable(Application, ColorContrastOptions).
   *
   * @param activity The target activity.
   * @param colorContrastOptions The color contrast options object that specifies the theme overlay
   *     resource IDs for medium and high contrast mode.
   */
  public static void applyToActivityIfAvailable(
      @NonNull Activity activity, @NonNull ColorContrastOptions colorContrastOptions) {
    if (!isContrastAvailable()) {
      return;
    }

    int themeOverlayResourcesId = getContrastThemeOverlayResourceId(activity, colorContrastOptions);
    if (themeOverlayResourcesId != 0) {
      ThemeUtils.applyThemeOverlay(activity, themeOverlayResourcesId);
    }
  }

  /**
   * Wraps the given context with the theme overlay where color resources are updated. The returned
   * context can be used to create views with contrast support.
   *
   * <p>Note that this method does not guarantee the consistency of contrast throughout the app. If
   * you want contrast to be updated automatically when a different contrast level is selected in
   * the system, please use #applyToActivitiesIfAvailable(Application, ColorContrastOptions).
   *
   * @param context The target context.
   * @param colorContrastOptions The color contrast options object that specifies the theme overlay
   *     resource IDs for medium and high contrast mode.
   */
  @NonNull
  public static Context wrapContextIfAvailable(
      @NonNull Context context, @NonNull ColorContrastOptions colorContrastOptions) {
    if (!isContrastAvailable()) {
      return context;
    }

    int themeOverlayResourcesId = getContrastThemeOverlayResourceId(context, colorContrastOptions);
    if (themeOverlayResourcesId == 0) {
      return context;
    }
    return new ContextThemeWrapper(context, themeOverlayResourcesId);
  }

  /** Returns {@code true} if contrast control is available on the current SDK level. */
  @ChecksSdkIntAtLeast(api = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public static boolean isContrastAvailable() {
    return VERSION.SDK_INT >= VERSION_CODES.UPSIDE_DOWN_CAKE;
  }

  private static int getContrastThemeOverlayResourceId(
      Context context, ColorContrastOptions colorContrastOptions) {
    UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
    if (!isContrastAvailable() || uiModeManager == null) {
      return 0;
    }

    float currentContrast = uiModeManager.getContrast();
    int mediumContrastThemeOverlay = colorContrastOptions.getMediumContrastThemeOverlay();
    int highContrastThemeOverlay = colorContrastOptions.getHighContrastThemeOverlay();
    if (currentContrast >= HIGH_CONTRAST_THRESHOLD) {
      // Falls back to mediumContrastThemeOverlay if highContrastThemeOverlay is not set in
      // ColorContrastOptions. If mediumContrastThemeOverlay is not set, default 0 will be returned.
      return highContrastThemeOverlay == 0 ? mediumContrastThemeOverlay : highContrastThemeOverlay;
    } else if (currentContrast >= MEDIUM_CONTRAST_THRESHOLD) {
      // Falls back to highContrastThemeOverlay if mediumContrastThemeOverlay is not set in
      // ColorContrastOptions. If highContrastThemeOverlay is not set, default 0 will be returned.
      return mediumContrastThemeOverlay == 0
          ? highContrastThemeOverlay
          : mediumContrastThemeOverlay;
    }
    return 0;
  }

  @RequiresApi(VERSION_CODES.UPSIDE_DOWN_CAKE)
  private static class ColorContrastActivityLifecycleCallbacks
      implements ActivityLifecycleCallbacks {

    private final Set<Activity> activitiesInStack = new LinkedHashSet<>();
    private final ColorContrastOptions colorContrastOptions;

    @Nullable private ContrastChangeListener contrastChangeListener;

    ColorContrastActivityLifecycleCallbacks(ColorContrastOptions colorContrastOptions) {
      this.colorContrastOptions = colorContrastOptions;
    }

    @Override
    public void onActivityPreCreated(
        @NonNull Activity activity, @Nullable Bundle savedInstanceState) {
      UiModeManager uiModeManager =
          (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
      if (uiModeManager != null && activitiesInStack.isEmpty() && contrastChangeListener == null) {
        contrastChangeListener =
            new ContrastChangeListener() {
              @Override
              public void onContrastChanged(float contrastLevel) {
                for (Activity activityInStack : activitiesInStack) {
                  activityInStack.recreate();
                }
              }
            };
        // Register UiContrastChangeListener on the application level.
        uiModeManager.addContrastChangeListener(
            ContextCompat.getMainExecutor(activity.getApplicationContext()),
            contrastChangeListener);
      }

      activitiesInStack.add(activity);
      if (uiModeManager != null) {
        applyToActivityIfAvailable(activity, colorContrastOptions);
      }
    }

    @Override
    public void onActivityCreated(
        @NonNull Activity activity, @Nullable Bundle savedInstanceState) {}

    @Override
    public void onActivityStarted(@NonNull Activity activity) {}

    @Override
    public void onActivityResumed(@NonNull Activity activity) {}

    @Override
    public void onActivityPaused(@NonNull Activity activity) {}

    @Override
    public void onActivityStopped(@NonNull Activity activity) {}

    @Override
    public void onActivitySaveInstanceState(@NonNull Activity activity, @NonNull Bundle outState) {}

    @Override
    public void onActivityDestroyed(@NonNull Activity activity) {
      // Always remove the activity from the stack to avoid memory leak.
      activitiesInStack.remove(activity);

      UiModeManager uiModeManager =
          (UiModeManager) activity.getSystemService(Context.UI_MODE_SERVICE);
      if (uiModeManager != null && contrastChangeListener != null && activitiesInStack.isEmpty()) {
        uiModeManager.removeContrastChangeListener(contrastChangeListener);
        contrastChangeListener = null;
      }
    }
  }
}
