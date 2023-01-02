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

import androidx.annotation.ArrayRes;
import androidx.annotation.ColorRes;
import androidx.annotation.IdRes;

/** A class that provides data for a row in the Color Harmonization demo grid. */
final class ColorHarmonizationGridRowData {

  @IdRes private final int leftLayoutId;
  @IdRes private final int rightLayoutId;
  @ColorRes private final int colorResId;
  @ArrayRes private final int colorNameIds;
  private final int[] colorAttributeResIds;

  ColorHarmonizationGridRowData(
      @IdRes int leftLayoutId,
      @IdRes int rightLayoutId,
      @ColorRes int colorResId,
      @ArrayRes int colorNameIds) {
    this.leftLayoutId = leftLayoutId;
    this.rightLayoutId = rightLayoutId;
    this.colorResId = colorResId;
    this.colorAttributeResIds = new int[] {};
    this.colorNameIds = colorNameIds;
  }

  ColorHarmonizationGridRowData(
      @IdRes int leftLayoutId,
      @IdRes int rightLayoutId,
      int[] colorAttributeResIds,
      @ArrayRes int colorNameIds) {
    this.leftLayoutId = leftLayoutId;
    this.rightLayoutId = rightLayoutId;
    this.colorResId = 0;
    this.colorAttributeResIds = colorAttributeResIds;
    this.colorNameIds = colorNameIds;
  }

  @IdRes
  int getLeftLayoutId() {
    return leftLayoutId;
  }

  @IdRes
  int getRightLayoutId() {
    return rightLayoutId;
  }

  @ColorRes
  int getColorResId() {
    return colorResId;
  }

  @ArrayRes
  int getColorNameIds() {
    return colorNameIds;
  }

  int[] getColorAttributeResIds() {
    return colorAttributeResIds;
  }
}
