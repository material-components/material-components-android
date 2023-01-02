/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.android.material.resources;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.graphics.fonts.FontStyle;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.math.MathUtils;

/**
 * Utility class that helps interact with Typeface objects.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class TypefaceUtils {

  private TypefaceUtils() {}

  /** Clones a typeface with additional boldness (weight). */
  @Nullable
  public static Typeface maybeCopyWithFontWeightAdjustment(
      @NonNull Context context, @NonNull Typeface typeface) {
    return maybeCopyWithFontWeightAdjustment(context.getResources().getConfiguration(), typeface);
  }

  /** Clones a typeface with additional boldness (weight). */
  @Nullable
  public static Typeface maybeCopyWithFontWeightAdjustment(
      @NonNull Configuration configuration, @NonNull Typeface typeface) {
    if (VERSION.SDK_INT >= VERSION_CODES.S
        && configuration.fontWeightAdjustment != Configuration.FONT_WEIGHT_ADJUSTMENT_UNDEFINED
        && configuration.fontWeightAdjustment != 0
        && typeface != null) {
      int adjustedWeight =
          MathUtils.clamp(
              typeface.getWeight() + configuration.fontWeightAdjustment,
              FontStyle.FONT_WEIGHT_MIN,
              FontStyle.FONT_WEIGHT_MAX);
      return Typeface.create(typeface, adjustedWeight, typeface.isItalic());
    }
    return null;
  }
}
