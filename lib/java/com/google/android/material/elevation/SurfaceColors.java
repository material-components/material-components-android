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

package com.google.android.material.elevation;

import com.google.android.material.R;

import android.content.Context;
import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import com.google.android.material.color.MaterialColors;

/**
 * Provides a convenient way to get color values of tonal variations of {@code R.attr.colorSurface}.
 */
public enum SurfaceColors {
  SURFACE_0(R.dimen.m3_sys_elevation_level0),
  SURFACE_1(R.dimen.m3_sys_elevation_level1),
  SURFACE_2(R.dimen.m3_sys_elevation_level2),
  SURFACE_3(R.dimen.m3_sys_elevation_level3),
  SURFACE_4(R.dimen.m3_sys_elevation_level4),
  SURFACE_5(R.dimen.m3_sys_elevation_level5);

  private final int elevationResId;

  SurfaceColors(@DimenRes int elevationResId) {
    this.elevationResId = elevationResId;
  }

  /**
   * Returns the tonal surface color value in RGB.
   */
  @ColorInt
  public int getColor(@NonNull Context context) {
    return getColorForElevation(
        context, context.getResources().getDimension(elevationResId));
  }

  /**
   * Returns the corresponding surface color in RGB with the given elevation.
   */
  @ColorInt
  public static int getColorForElevation(@NonNull Context context, @Dimension float elevation) {
    return new ElevationOverlayProvider(context).compositeOverlay(
        MaterialColors.getColor(context, R.attr.colorSurface, Color.TRANSPARENT), elevation);
  }
}
