/*
 * Copyright 2019 The Android Open Source Project
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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import android.graphics.RectF;

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
interface FitModeEvaluator {

  /** Calculate the current start and end view sizes and scales depending on the fit mode. */
  FitModeResult evaluate(
      float progress,
      float scaleStartFraction,
      float scaleEndFraction,
      float startWidth,
      float startHeight,
      float endWidth,
      float endHeight);

  /**
   * Determine whether the start or end view should be masked. For example, if fitting to width and
   * the end view is proportionally taller than the start view, then this method should return false
   * so that the end view is masked to create the reveal effect.
   */
  boolean shouldMaskStartBounds(FitModeResult fitModeResult);

  /** Update the mask bounds to create the reveal effect. */
  void applyMask(RectF maskBounds, float maskMultiplier, FitModeResult fitModeResult);
}
