/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.color;

import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;

/** A class that provides data for {@link HarmonizableButton}. */
final class HarmonizableButtonData {

  @IdRes private final int buttonId;
  @ColorRes private final int colorResId;
  private final boolean isLightButton;

  HarmonizableButtonData(@IdRes int buttonId, @ColorRes int colorResId, boolean isLightButton) {
    this.buttonId = buttonId;
    this.colorResId = colorResId;
    this.isLightButton = isLightButton;
  }

  @IdRes
  int getButtonId() {
    return buttonId;
  }

  @ColorRes
  int getColorResId() {
    return colorResId;
  }

  boolean isLightButton() {
    return isLightButton;
  }
}
