/*
 * Copyright (C) 2021 The Android Open Source Project
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

import com.google.android.material.R;

import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.os.BuildCompat;

/**
 * Utility for applying dynamic colors to application/activities.
 */
public class DynamicColors {
  private static final int[] DYNAMIC_COLOR_THEME_OVERLAY_ATTRIBUTE =
      new int[] { R.attr.dynamicColorThemeOverlay };

  private static final int USE_DEFAULT_THEME_OVERLAY = 0;

  private static final Precondition ALWAYS_ALLOW = new Precondition() {
    @Override
    public boolean shouldApplyDynamicColors(@NonNull Activity activity, int theme) {
      return true;
    }
  };

  private DynamicColors() {}

  /**
   * Applies dynamic colors to all activities with the theme overlay designated by the theme
   * attribute {@link R.attr.dynamicColorThemeOverlay} by registering a
   * {@link ActivityLifecycleCallbacks} to your application.
   *
   * @see #applyToActivitiesIfAvailable(Application, int, Precondition) for more detailed info and
   *      examples.
   *
   * @param application The target application.
   */
  public static void applyToActivitiesIfAvailable(@NonNull Application application) {
    applyToActivitiesIfAvailable(application, USE_DEFAULT_THEME_OVERLAY);
  }

  /**
   * Applies dynamic colors to all activities with the given theme overlay by registering a
   * {@link ActivityLifecycleCallbacks} to your application.
   *
   * @see #applyToActivitiesIfAvailable(Application, int, Precondition) for more detailed info and
   *      examples.
   *
   * @param application The target application.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   */
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @StyleRes int theme) {
    applyToActivitiesIfAvailable(application, theme, ALWAYS_ALLOW);
  }

  /**
   * Applies dynamic colors to all activities with the theme overlay designated by the theme
   * attribute {@link R.attr.dynamicColorThemeOverlay} according to the given precondition by
   * registering a {@link ActivityLifecycleCallbacks} to your application.
   *
   * @see #applyToActivitiesIfAvailable(Application, int, Precondition) for more detailed info and
   *      examples.
   *
   * @param application The target application.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   */
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @NonNull Precondition precondition) {
    applyToActivitiesIfAvailable(application, USE_DEFAULT_THEME_OVERLAY, precondition);
  }

  /**
   * Applies dynamic colors to all activities with the given theme overlay according to the given
   * precondition by registering a {@link ActivityLifecycleCallbacks} to your application.
   *
   * A normal usage of this method should happen only once in {@link Application#onCreate()} or any
   * methods that run before any of your activities are created. For example:
   * <pre>
   * public class YourApplication extends Application {
   *   &#64;Override
   *   public void onCreate() {
   *     super.onCreate();
   *     DynamicColors.applyToActivitiesWithCallbacks(this);
   *   }
   * }
   * </pre>
   * This method will try to apply the given dynamic color theme overlay in every activity's
   * {@link ActivityLifecycleCallbacks#onActivityPreCreated(Activity, Bundle)} callback. Therefore,
   * if you are applying any other theme overlays after that, you will need to be careful about not
   * overriding the colors or you may lose the dynamic color support.
   *
   * @param application The target application.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   */
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @StyleRes int theme, @NonNull Precondition precondition) {
    application.registerActivityLifecycleCallbacks(
        new DynamicColorsActivityLifecycleCallbacks(theme, precondition));
  }

  /**
   * Applies dynamic colors to the given activity with the theme overlay designated by the theme
   * attribute {@link R.attr.dynamicColorThemeOverlay}.
   *
   * @param activity The target activity.
   */
  public static void applyIfAvailable(@NonNull Activity activity) {
    applyIfAvailable(activity, USE_DEFAULT_THEME_OVERLAY);
  }

  /**
   * Applies dynamic colors to the given activity with the given theme overlay.
   *
   * @param activity The target activity.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   */
  public static void applyIfAvailable(@NonNull Activity activity, @StyleRes int theme) {
    applyIfAvailable(activity, theme, ALWAYS_ALLOW);
  }

  /**
   * Applies dynamic colors to the given activity with the theme overlay designated by the theme
   * attribute {@link R.attr.dynamicColorThemeOverlay} according to the given precondition.
   *
   * @param activity The target activity.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   */
  public static void applyIfAvailable(
      @NonNull Activity activity, @NonNull Precondition precondition) {
    applyIfAvailable(activity, USE_DEFAULT_THEME_OVERLAY, precondition);
  }

  private static void applyIfAvailable(
      @NonNull Activity activity, @StyleRes int theme, @NonNull Precondition precondition) {
    if (!isDynamicColorAvailable()) {
      return;
    }
    if (theme == USE_DEFAULT_THEME_OVERLAY) {
      theme = getDefaultThemeOverlay(activity);
    }
    if (theme != 0 && precondition.shouldApplyDynamicColors(activity, theme)) {
      activity.setTheme(theme);
    }
  }

  /**
   * Wraps the given context with the theme overlay designated by the theme attribute
   * {@link R.attr.dynamicColorThemeOverlay}. The returned context can be used to create
   * views with dynamic color support.
   *
   * If dynamic color support or the dynamic color theme overlay is not available, the original
   * context will be returned.
   *
   * @param originalContext The original context.
   */
  @NonNull
  public static Context wrapContextIfAvailable(@NonNull Context originalContext) {
    return wrapContextIfAvailable(originalContext, USE_DEFAULT_THEME_OVERLAY);
  }

  /**
   * Wraps the given context with the given theme overlay. The returned context can be used to
   * create views with dynamic color support.
   *
   * If dynamic color support is not available, the original context will be returned.
   *
   * @param originalContext The original context.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   */
  @NonNull
  public static Context wrapContextIfAvailable(
      @NonNull Context originalContext, @StyleRes int theme) {
    if (!isDynamicColorAvailable()) {
      return originalContext;
    }
    if (theme == USE_DEFAULT_THEME_OVERLAY) {
      theme = getDefaultThemeOverlay(originalContext);
    }
    return theme == 0 ? originalContext : new ContextThemeWrapper(originalContext, theme);
  }

  /**
   * Returns {@code true} if dynamic colors are available on the current SDK level.
   */
  public static boolean isDynamicColorAvailable() {
    return BuildCompat.isAtLeastS();
  }

  private static int getDefaultThemeOverlay(@NonNull Context context) {
    TypedArray dynamicColorAttributes =
        context.obtainStyledAttributes(DYNAMIC_COLOR_THEME_OVERLAY_ATTRIBUTE);
    final int theme = dynamicColorAttributes.getResourceId(0, 0);
    dynamicColorAttributes.recycle();
    return theme;
  }

  /**
   * The interface that provides a precondition to decide if dynamic colors should be applied.
   */
  public interface Precondition {

    /**
     * Return {@code true} if dynamic colors should be applied on the given activity with the
     * given theme overlay.
     */
    boolean shouldApplyDynamicColors(@NonNull Activity activity, @StyleRes int theme);
  }

  private static class DynamicColorsActivityLifecycleCallbacks
      implements ActivityLifecycleCallbacks {
    private final int dynamicColorThemeOverlay;
    private final Precondition precondition;

    DynamicColorsActivityLifecycleCallbacks(@StyleRes int theme, @NonNull Precondition condition) {
      dynamicColorThemeOverlay = theme;
      precondition = condition;
    }

    @Override
    public void onActivityPreCreated(@NonNull Activity activity,
        @Nullable Bundle savedInstanceState) {
      applyIfAvailable(activity, dynamicColorThemeOverlay, precondition);
    }

    @Override
    public void onActivityCreated(@NonNull Activity activity,
        @Nullable Bundle savedInstanceState) {}

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
    public void onActivityDestroyed(@NonNull Activity activity) {}
  }
}
