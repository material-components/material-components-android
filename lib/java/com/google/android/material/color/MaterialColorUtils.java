/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.color;

import android.content.Context;
import android.view.View;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import com.google.android.material.R;

/** Utility methods for {@link MaterialColors}. */
public class MaterialColorUtils {

  private MaterialColorUtils() {}
  private static final int COLOR_PRIMARY_ATTR = R.attr.colorPrimary;
  private static final int COLOR_PRIMARY_VARIANT_ATTR = R.attr.colorPrimaryVariant;
  private static final int COLOR_ON_PRIMARY_ATTR = R.attr.colorOnPrimary;
  private static final int COLOR_SECONDARY_ATTR = R.attr.colorSecondary;
  private static final int COLOR_SECONDARY_VARIANT_ATTR = R.attr.colorSecondaryVariant;
  private static final int COLOR_ON_SECONDARY_ATTR = R.attr.colorOnSecondary;
  private static final int COLOR_SURFACE_ATTR = R.attr.colorSurface;
  private static final int COLOR_ON_SURFACE_ATTR = R.attr.colorOnSurface;
  private static final int COLOR_BACKGROUND_ATTR = android.R.attr.colorBackground;
  private static final int COLOR_ON_BACKGROUND_ATTR = R.attr.colorOnBackground;
  private static final int COLOR_ERROR_ATTR = R.attr.colorError;
  private static final int COLOR_ON_ERROR_ATTR = R.attr.colorOnError;


  /**
   * Returns the primary color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorPrimary(Context context) {
    return MaterialColors.getColor(context, COLOR_PRIMARY_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the primary color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorPrimary(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_PRIMARY_ATTR);
  }

  /**
   * Returns the primary variant color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorPrimaryVariant(Context context) {
    return MaterialColors.getColor(context, COLOR_PRIMARY_VARIANT_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the primary variant color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorPrimaryVariant(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_PRIMARY_VARIANT_ATTR);
  }

  /**
   * Returns the onPrimary color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnPrimary(Context context) {
    return MaterialColors.getColor(context, COLOR_ON_PRIMARY_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the OnPrimary color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnPrimary(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ON_PRIMARY_ATTR);
  }

  /**
   * Returns the secondary color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSecondary(Context context) {
    return MaterialColors.getColor(context, COLOR_SECONDARY_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the secondary color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSecondary(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_SECONDARY_ATTR);
  }

  /**
   * Returns the secondary variant color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSecondaryVariant(Context context) {
    return MaterialColors.getColor(context, COLOR_SECONDARY_VARIANT_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the secondary variant color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSecondaryVariant(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_SECONDARY_VARIANT_ATTR);
  }

  /**
   * Returns the OnSecondary color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnSecondary(Context context) {
    return MaterialColors.getColor(context, COLOR_ON_SECONDARY_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the onSecondary color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnSecondary(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ON_SECONDARY_ATTR);
  }

  /**
   * Returns the surface color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSurface(Context context) {
    return MaterialColors.getColor(context, COLOR_SURFACE_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the surface color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorSurface(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_SURFACE_ATTR);
  }

  /**
   * Returns the onSurface color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnSurface(Context context) {
    return MaterialColors.getColor(context, COLOR_ON_SURFACE_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the onSurface color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnSurface(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ON_SURFACE_ATTR);
  }

  /**
   * Returns the colorBackground int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorBackground(Context context) {
    return MaterialColors.getColor(context, COLOR_BACKGROUND_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the colorBackground int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorBackground(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_BACKGROUND_ATTR);
  }

  /**
   * Returns the colorOnBackground int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnBackground(Context context) {
    return MaterialColors.getColor(context, COLOR_ON_BACKGROUND_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the colorOnBackground int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnBackground(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ON_BACKGROUND_ATTR);
  }

  /**
   * Returns the error color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorError(Context context) {
    return MaterialColors.getColor(context, COLOR_ERROR_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the error color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorError(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ERROR_ATTR);
  }

  /**
   * Returns the onError color int
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnError(Context context) {
    return MaterialColors.getColor(context, COLOR_ON_ERROR_ATTR, MaterialColorUtils.class.getSimpleName());
  }

  /**
   * Returns the onError color int, using the {@link Context} of the provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int colorOnError(@NonNull View view) {
    return MaterialColors.getColor(view, COLOR_ON_ERROR_ATTR);
  }
}
