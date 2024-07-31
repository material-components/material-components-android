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

package com.google.android.material.color.utilities;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.annotation.RestrictTo;

/**
 * Check and/or fix universally disliked colors.
 *
 * <p>Color science studies of color preference indicate universal distaste for dark yellow-greens,
 * and also show this is correlated to distaste for biological waste and rotting food.
 *
 * <p>See Palmer and Schloss, 2010 or Schloss and Palmer's Chapter 21 in Handbook of Color
 * Psychology (2015).
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class DislikeAnalyzer {

  private DislikeAnalyzer() {
    throw new UnsupportedOperationException();
  }

  /**
   * Returns true if color is disliked.
   *
   * <p>Disliked is defined as a dark yellow-green that is not neutral.
   */
  public static boolean isDisliked(Hct hct) {
    final boolean huePasses = Math.round(hct.getHue()) >= 90.0 && Math.round(hct.getHue()) <= 111.0;
    final boolean chromaPasses = Math.round(hct.getChroma()) > 16.0;
    final boolean tonePasses = Math.round(hct.getTone()) < 65.0;

    return huePasses && chromaPasses && tonePasses;
  }

  /** If color is disliked, lighten it to make it likable. */
  public static Hct fixIfDisliked(Hct hct) {
    if (isDisliked(hct)) {
      return Hct.from(hct.getHue(), hct.getChroma(), 70.0);
    }

    return hct;
  }
}
