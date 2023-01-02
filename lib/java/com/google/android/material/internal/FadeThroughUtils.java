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

package com.google.android.material.internal;

import androidx.annotation.FloatRange;

/** Utility methods for fade through animations. */
final class FadeThroughUtils {

  static final float THRESHOLD_ALPHA = 0.5f;

  static void calculateFadeOutAndInAlphas(
      @FloatRange(from = 0.0, to = 1.0) float progress, float[] out) {
    if (progress <= THRESHOLD_ALPHA) {
      out[0] = 1f - progress * 2f;
      out[1] = 0f;
    } else {
      out[0] = 0f;
      out[1] = progress * 2f - 1f;
    }
  }

  private FadeThroughUtils() {}
}
