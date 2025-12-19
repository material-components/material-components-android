/*
 * Copyright 2025 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.listitem;

import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.core.view.GravityCompat;

/** Package-private utilities for list item. */
class ListItemUtils {

  private ListItemUtils() {}

  /**
   * Checks if the given view resolves to {@link Gravity#RIGHT} based on its {@link
   * ListItemLayout.LayoutParams#gravity} and its layout direction. If gravity is unspecified (-1)
   * in {@link ListItemLayout.LayoutParams}, or if the view's LayoutParams are not an instance of
   * {@link ListItemLayout.LayoutParams}, gravity defaults to {@link Gravity#END}.
   *
   * @param view The view to check alignment for.
   * @return true if gravity resolves to RIGHT, false otherwise.
   */
  static boolean isRightAligned(@NonNull View view) {
    int gravity = Gravity.END; // Default gravity if LayoutParams are missing or not ListItemLayout.
    if (view.getLayoutParams() instanceof ListItemLayout.LayoutParams) {
      FrameLayout.LayoutParams lp = (ListItemLayout.LayoutParams) view.getLayoutParams();
      if (lp.gravity != -1) {
        gravity = lp.gravity;
      }
    }

    int absoluteGravity =
        GravityCompat.getAbsoluteGravity(gravity, view.getLayoutDirection())
            & Gravity.HORIZONTAL_GRAVITY_MASK;
    return absoluteGravity == Gravity.RIGHT;
  }
}
