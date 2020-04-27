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

@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
class FitModeResult {
  final float startScale;
  final float endScale;
  final float currentStartWidth;
  final float currentStartHeight;
  final float currentEndWidth;
  final float currentEndHeight;

  FitModeResult(
      float startScale,
      float endScale,
      float currentStartWidth,
      float currentStartHeight,
      float currentEndWidth,
      float currentEndHeight) {
    this.startScale = startScale;
    this.endScale = endScale;
    this.currentStartWidth = currentStartWidth;
    this.currentStartHeight = currentStartHeight;
    this.currentEndWidth = currentEndWidth;
    this.currentEndHeight = currentEndHeight;
  }
}
