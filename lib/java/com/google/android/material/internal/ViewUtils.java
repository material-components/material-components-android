/*
 * Copyright (C) 2015 The Android Open Source Project
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

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.PorterDuff;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.view.View;

/**
 * Utils class for custom views.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ViewUtils {
  public static PorterDuff.Mode parseTintMode(int value, PorterDuff.Mode defaultMode) {
    switch (value) {
      case 3:
        return PorterDuff.Mode.SRC_OVER;
      case 5:
        return PorterDuff.Mode.SRC_IN;
      case 9:
        return PorterDuff.Mode.SRC_ATOP;
      case 14:
        return PorterDuff.Mode.MULTIPLY;
      case 15:
        return PorterDuff.Mode.SCREEN;
      case 16:
        return PorterDuff.Mode.ADD;
      default:
        return defaultMode;
    }
  }

  public static boolean isLayoutRtl(View view) {
    return ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
  }
}
