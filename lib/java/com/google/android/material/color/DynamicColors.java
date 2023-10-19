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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Application;
import android.app.Application.ActivityLifecycleCallbacks;
import android.app.UiModeManager;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import androidx.annotation.ChecksSdkIntAtLeast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import androidx.core.os.BuildCompat;
import com.google.android.material.color.utilities.Hct;
import com.google.android.material.color.utilities.SchemeContent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/** Utility for applying dynamic colors to application/activities. */
public class DynamicColors {
  private static final int[] DYNAMIC_COLOR_THEME_OVERLAY_ATTRIBUTE =
      new int[] {R.attr.dynamicColorThemeOverlay};

  private static final DeviceSupportCondition DEFAULT_DEVICE_SUPPORT_CONDITION =
      new DeviceSupportCondition() {
        @Override
        public boolean isSupported() {
          return true;
        }
      };

  @SuppressLint("PrivateApi")
  private static final DeviceSupportCondition SAMSUNG_DEVICE_SUPPORT_CONDITION =
      new DeviceSupportCondition() {
        private Long version;

        @Override
        public boolean isSupported() {
          if (version == null) {
            try {
              Method method = Build.class.getDeclaredMethod("getLong", String.class);
              method.setAccessible(true);
              version = (long) method.invoke(null, "ro.build.version.oneui");
            } catch (Exception e) {
              version = -1L;
            }
          }
          return version >= 40100L;
        }
      };

  private static final Map<String, DeviceSupportCondition> DYNAMIC_COLOR_SUPPORTED_MANUFACTURERS;

  static {
    Map<String, DeviceSupportCondition> deviceMap = new HashMap<>();
    deviceMap.put("fcnt", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("google", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("hmd global", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("infinix", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("infinix mobility limited", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("itel", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("kyocera", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("lenovo", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("lge", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("meizu", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("motorola", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("nothing", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("oneplus", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("oppo", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("realme", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("robolectric", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("samsung", SAMSUNG_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("sharp", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("shift", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("sony", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("tcl", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("tecno", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("tecno mobile limited", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("vivo", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("wingtech", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("xiaomi", DEFAULT_DEVICE_SUPPORT_CONDITION);
    DYNAMIC_COLOR_SUPPORTED_MANUFACTURERS = Collections.unmodifiableMap(deviceMap);
  }

  private static final Map<String, DeviceSupportCondition> DYNAMIC_COLOR_SUPPORTED_BRANDS;

  static {
    Map<String, DeviceSupportCondition> deviceMap = new HashMap<>();
    deviceMap.put("asus", DEFAULT_DEVICE_SUPPORT_CONDITION);
    deviceMap.put("jio", DEFAULT_DEVICE_SUPPORT_CONDITION);
    DYNAMIC_COLOR_SUPPORTED_BRANDS = Collections.unmodifiableMap(deviceMap);
  }

  private static final int USE_DEFAULT_THEME_OVERLAY = 0;
  private static final String TAG = DynamicColors.class.getSimpleName();

  private DynamicColors() {}

  /**
   * Applies dynamic colors to all activities with the theme overlay designated by the theme
   * attribute {@code dynamicColorThemeOverlay} by registering a {@link ActivityLifecycleCallbacks}
   * to your application.
   *
   * @see #applyToActivitiesIfAvailable(Application, DynamicColorsOptions) for more detailed info
   *     and examples.
   * @param application The target application.
   */
  public static void applyToActivitiesIfAvailable(@NonNull Application application) {
    applyToActivitiesIfAvailable(application, new DynamicColorsOptions.Builder().build());
  }

  /**
   * Applies dynamic colors to all activities with the given theme overlay by registering a {@link
   * ActivityLifecycleCallbacks} to your application.
   *
   * @param application The target application.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   * @deprecated Use {@link #applyToActivitiesIfAvailable(Application, DynamicColorsOptions)}
   *     instead.
   */
  @Deprecated
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @StyleRes int theme) {
    applyToActivitiesIfAvailable(
        application, new DynamicColorsOptions.Builder().setThemeOverlay(theme).build());
  }

  /**
   * Applies dynamic colors to all activities with the theme overlay designated by the theme
   * attribute {@code dynamicColorThemeOverlay} according to the given precondition by registering a
   * {@link ActivityLifecycleCallbacks} to your application.
   *
   * @param application The target application.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   * @deprecated Use {@link #applyToActivitiesIfAvailable(Application, DynamicColorsOptions)}
   *     instead.
   */
  @Deprecated
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @NonNull Precondition precondition) {
    applyToActivitiesIfAvailable(
        application, new DynamicColorsOptions.Builder().setPrecondition(precondition).build());
  }

  /**
   * Applies dynamic colors to all activities with the given theme overlay according to the given
   * precondition by registering a {@link ActivityLifecycleCallbacks} to your application.
   *
   * @param application The target application.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   * @deprecated Use {@link #applyToActivitiesIfAvailable(Application, DynamicColorsOptions)}
   *     instead.
   */
  @Deprecated
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @StyleRes int theme, @NonNull Precondition precondition) {
    applyToActivitiesIfAvailable(
        application,
        new DynamicColorsOptions.Builder()
            .setThemeOverlay(theme)
            .setPrecondition(precondition)
            .build());
  }

  /**
   * Applies dynamic colors to all activities based on the provided {@link DynamicColorsOptions}, by
   * registering a {@link ActivityLifecycleCallbacks} to your application.
   *
   * <p>A normal usage of this method should happen only once in {@link Application#onCreate()} or
   * any methods that run before any of your activities are created. For example:
   *
   * <pre>
   * public class YourApplication extends Application {
   *   &#64;Override
   *   public void onCreate() {
   *     super.onCreate();
   *     DynamicColors.applyToActivitiesWithCallbacks(this);
   *   }
   * }
   * </pre>
   *
   * This method will try to apply the given dynamic color theme overlay in every activity's {@link
   * ActivityLifecycleCallbacks#onActivityPreCreated(Activity, Bundle)} callback. Therefore, if you
   * are applying any other theme overlays after that, you will need to be careful about not
   * overriding the colors or you may lose the dynamic color support.
   *
   * @param application The target application.
   * @param dynamicColorsOptions The dynamic colors options object that specifies the theme resource
   *     ID, precondition to decide if dynamic colors should be applied and the callback function
   *     for after dynamic colors have been applied.
   */
  public static void applyToActivitiesIfAvailable(
      @NonNull Application application, @NonNull DynamicColorsOptions dynamicColorsOptions) {
    application.registerActivityLifecycleCallbacks(
        new DynamicColorsActivityLifecycleCallbacks(dynamicColorsOptions));
  }

  /**
   * Applies dynamic colors to the given activity with the theme overlay designated by the theme
   * attribute {@code dynamicColorThemeOverlay}.
   *
   * @param activity The target activity.
   * @deprecated Use {@link #applyToActivityIfAvailable(Activity)} instead.
   */
  @Deprecated
  public static void applyIfAvailable(@NonNull Activity activity) {
    applyToActivityIfAvailable(activity);
  }

  /**
   * Applies dynamic colors to the given activity with the given theme overlay.
   *
   * @param activity The target activity.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   * @deprecated Use {@link #applyToActivityIfAvailable(Activity, DynamicColorsOptions)} instead.
   */
  @Deprecated
  public static void applyIfAvailable(@NonNull Activity activity, @StyleRes int theme) {
    applyToActivityIfAvailable(
        activity, new DynamicColorsOptions.Builder().setThemeOverlay(theme).build());
  }

  /**
   * Applies dynamic colors to the given activity with the theme overlay designated by the theme
   * attribute {@code dynamicColorThemeOverlay} according to the given precondition.
   *
   * @param activity The target activity.
   * @param precondition The precondition to decide if dynamic colors should be applied.
   * @deprecated Use {@link #applyToActivityIfAvailable(Activity, DynamicColorsOptions)} instead.
   */
  @Deprecated
  public static void applyIfAvailable(
      @NonNull Activity activity, @NonNull Precondition precondition) {
    applyToActivityIfAvailable(
        activity, new DynamicColorsOptions.Builder().setPrecondition(precondition).build());
  }

  /**
   * Applies dynamic colors to the given activity.
   *
   * @param activity The target activity.
   * @see #applyToActivityIfAvailable(Activity, DynamicColorsOptions)
   */
  public static void applyToActivityIfAvailable(@NonNull Activity activity) {
    applyToActivityIfAvailable(activity, new DynamicColorsOptions.Builder().build());
  }

  /**
   * Applies dynamic colors to the given activity with {@link DynamicColorsOptions} provided.
   *
   * @param activity The target activity.
   * @param dynamicColorsOptions The dynamic colors options object that specifies the theme resource
   *     ID, precondition to decide if dynamic colors should be applied and the callback function
   *     for after dynamic colors have been applied.
   */
  public static void applyToActivityIfAvailable(
      @NonNull Activity activity, @NonNull DynamicColorsOptions dynamicColorsOptions) {
    if (!isDynamicColorAvailable()) {
      return;
    }
    // Set default theme overlay as 0, as it's not used in content-based dynamic colors.
    int theme = 0;
    // Only retrieves the theme overlay if we're applying just dynamic colors.
    if (dynamicColorsOptions.getContentBasedSeedColor() == null) {
      theme =
          dynamicColorsOptions.getThemeOverlay() == USE_DEFAULT_THEME_OVERLAY
              ? getDefaultThemeOverlay(activity, DYNAMIC_COLOR_THEME_OVERLAY_ATTRIBUTE)
              : dynamicColorsOptions.getThemeOverlay();
    }

    if (dynamicColorsOptions.getPrecondition().shouldApplyDynamicColors(activity, theme)) {
      // Applies content-based dynamic colors if content-based source is provided.
      if (dynamicColorsOptions.getContentBasedSeedColor() != null) {
        SchemeContent scheme =
            new SchemeContent(
                Hct.fromInt(dynamicColorsOptions.getContentBasedSeedColor()),
                !MaterialColors.isLightTheme(activity),
                getSystemContrast(activity));
        ColorResourcesOverride resourcesOverride = ColorResourcesOverride.getInstance();
        if (resourcesOverride == null) {
          return;
        } else {
          if (!resourcesOverride.applyIfPossible(
              activity,
              MaterialColorUtilitiesHelper.createColorResourcesIdsToColorValues(scheme))) {
            return;
          }
        }
      } else {
        ThemeUtils.applyThemeOverlay(activity, theme);
      }
      // Applies client's callback after content-based dynamic colors or just dynamic colors has
      // been applied.
      dynamicColorsOptions.getOnAppliedCallback().onApplied(activity);
    }
  }

  /**
   * Wraps the given context with the theme overlay designated by the theme attribute {@code
   * dynamicColorThemeOverlay}. The returned context can be used to create views with dynamic color
   * support.
   *
   * <p>If dynamic color support or the dynamic color theme overlay is not available, the original
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
   * <p>If dynamic color support is not available, the original context will be returned.
   *
   * @param originalContext The original context.
   * @param theme The resource ID of the theme overlay that provides dynamic color definition.
   */
  @NonNull
  public static Context wrapContextIfAvailable(
      @NonNull Context originalContext, @StyleRes int theme) {
    return wrapContextIfAvailable(
        originalContext, new DynamicColorsOptions.Builder().setThemeOverlay(theme).build());
  }

  /**
   * Wraps the given context with the given theme overlay provided in {@link DynamicColorsOptions}.
   * The returned context can be used to create views with dynamic color support.
   *
   * <p>If dynamic color support is not available, the original context will be returned.
   *
   * @param originalContext The original context.
   * @param dynamicColorsOptions The dynamic colors options object that specifies the theme resource
   *     ID, seed color for content-based dynamic colors.
   */
  @NonNull
  public static Context wrapContextIfAvailable(
      @NonNull Context originalContext, @NonNull DynamicColorsOptions dynamicColorsOptions) {
    if (!isDynamicColorAvailable()) {
      return originalContext;
    }
    int theme = dynamicColorsOptions.getThemeOverlay();
    if (theme == USE_DEFAULT_THEME_OVERLAY) {
      theme = getDefaultThemeOverlay(originalContext, DYNAMIC_COLOR_THEME_OVERLAY_ATTRIBUTE);
    }

    if (theme == 0) {
      return originalContext;
    }

    if (dynamicColorsOptions.getContentBasedSeedColor() != null) {
      SchemeContent scheme =
          new SchemeContent(
              Hct.fromInt(dynamicColorsOptions.getContentBasedSeedColor()),
              !MaterialColors.isLightTheme(originalContext),
              getSystemContrast(originalContext));
      ColorResourcesOverride resourcesOverride = ColorResourcesOverride.getInstance();
      if (resourcesOverride != null) {
        return resourcesOverride.wrapContextIfPossible(
            originalContext,
            MaterialColorUtilitiesHelper.createColorResourcesIdsToColorValues(scheme));
      }
    }
    return new ContextThemeWrapper(originalContext, theme);
  }

  /** Returns {@code true} if dynamic colors are available on the current SDK level. */
  @SuppressLint("DefaultLocale")
  @ChecksSdkIntAtLeast(api = VERSION_CODES.S)
  public static boolean isDynamicColorAvailable() {
    if (VERSION.SDK_INT < VERSION_CODES.S) {
      return false;
    }
    if (BuildCompat.isAtLeastT()) {
      return true;
    }
    DeviceSupportCondition deviceSupportCondition =
        DYNAMIC_COLOR_SUPPORTED_MANUFACTURERS.get(Build.MANUFACTURER.toLowerCase(Locale.ROOT));
    if (deviceSupportCondition == null) {
      deviceSupportCondition =
          DYNAMIC_COLOR_SUPPORTED_BRANDS.get(Build.BRAND.toLowerCase(Locale.ROOT));
    }
    return deviceSupportCondition != null && deviceSupportCondition.isSupported();
  }

  private static int getDefaultThemeOverlay(@NonNull Context context, int[] themeOverlayAttribute) {
    TypedArray dynamicColorAttributes = context.obtainStyledAttributes(themeOverlayAttribute);
    final int theme = dynamicColorAttributes.getResourceId(0, 0);
    dynamicColorAttributes.recycle();
    return theme;
  }

  /** The interface that provides a precondition to decide if dynamic colors should be applied. */
  public interface Precondition {

    /**
     * Return {@code true} if dynamic colors should be applied on the given activity with the given
     * theme overlay.
     */
    boolean shouldApplyDynamicColors(@NonNull Activity activity, @StyleRes int theme);
  }

  /** The interface that provides a callback method after dynamic colors have been applied. */
  public interface OnAppliedCallback {

    /** The callback method after dynamic colors have been applied. */
    void onApplied(@NonNull Activity activity);
  }

  private static class DynamicColorsActivityLifecycleCallbacks
      implements ActivityLifecycleCallbacks {
    private final DynamicColorsOptions dynamicColorsOptions;

    DynamicColorsActivityLifecycleCallbacks(@NonNull DynamicColorsOptions options) {
      this.dynamicColorsOptions = options;
    }

    @Override
    public void onActivityPreCreated(
        @NonNull Activity activity, @Nullable Bundle savedInstanceState) {
      applyToActivityIfAvailable(activity, dynamicColorsOptions);
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
    public void onActivityDestroyed(@NonNull Activity activity) {}
  }

  private interface DeviceSupportCondition {
    boolean isSupported();
  }

  private static float getSystemContrast(Context context) {
    UiModeManager uiModeManager = (UiModeManager) context.getSystemService(Context.UI_MODE_SERVICE);
    return (uiModeManager == null || VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE)
        ? 0
        : uiModeManager.getContrast();
  }
}
