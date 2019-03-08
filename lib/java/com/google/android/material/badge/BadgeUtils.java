/*
 * Copyright (C) 2019 The Android Open Source Project
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

package com.google.android.material.badge;

import android.graphics.Rect;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * Utility class for {@link BadgeDrawable}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY)
public class BadgeUtils {

  /**
   * Maximum number of characters a badge supports displaying by default. It could be changed using
   * BadgeDrawable#setMaxBadgeCount.
   */
  public static final int DEFAULT_MAX_BADGE_CHARACTER_COUNT = 4;

  /** Value of -1 denotes an icon only badge. */
  public static final int ICON_ONLY_BADGE_NUMBER = -1;

  /** Maximum value of number that can be displayed in a circular badge. */
  static final int MAX_CIRCULAR_BADGE_NUMBER_COUNT = 99;

  /**
   * If the badge number exceeds the maximum allowed number, append this suffix to the max badge
   * number and display is as the badge text instead.
   */
  static final String DEFAULT_EXCEED_MAX_BADGE_NUMBER_SUFFIX = "+";

  private BadgeUtils() {
    // Private constructor to prevent unwanted construction.
  }

  /**
   * Updates a badge's bounds using its center coordinate, {@code halfWidth} and {@code halfHeight}.
   *
   * @param rect Holds rectangular coordinates of the badge's bounds.
   * @param centerX A badge's center x coordinate.
   * @param centerY A badge's center y coordinate.
   * @param halfWidth Half of a badge's width.
   * @param halfHeight Half of a badge's height.
   */
  public static void updateBadgeBounds(
      Rect rect, float centerX, float centerY, float halfWidth, float halfHeight) {
    rect.set(
        (int) (centerX - halfWidth),
        (int) (centerY - halfHeight),
        (int) (centerX + halfWidth),
        (int) (centerY + halfHeight));
  }
}
