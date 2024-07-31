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

package io.material.catalog.color;

import android.graphics.Color;
import androidx.annotation.ColorInt;
import androidx.core.graphics.ColorUtils;

/** Utility methods for Color. */
final class ColorDemoUtils {

  private ColorDemoUtils() {}

  static int getTextColor(@ColorInt int backgroundColor) {
    // Use the text color with the best contrast against the background color.
    if (ColorUtils.calculateContrast(Color.BLACK, backgroundColor)
        > ColorUtils.calculateContrast(Color.WHITE, backgroundColor)) {
      return Color.BLACK;
    } else {
      return Color.WHITE;
    }
  }
}
