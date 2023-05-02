/*
 * Copyright 2023 The Android Open Source Project
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
package com.google.android.material.carousel;

import com.google.android.material.R;

import static com.google.android.material.carousel.CarouselStrategy.getChildMaskPercentage;
import static java.lang.Math.max;

import android.content.Context;
import androidx.annotation.NonNull;

/**
 * A helper class with utility methods for {@link CarouselStrategy} implementations.
 */
final class CarouselStrategyHelper {

  private CarouselStrategyHelper() {}

  static float getExtraSmallSize(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_gone_size);
  }

  static float getSmallSizeMin(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
  }

  static float getSmallSizeMax(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_small_item_size_max);
  }

  /**
   * Gets the {@link KeylineState} associated with the given parameters.
   *
   * @param context The context used to load resources.
   * @param childHorizontalMargins The child margins to use when calculating mask percentage.
   * @param availableSpace the space that the {@link KeylineState} needs to fit.
   * @param arrangement the {@link Arrangement} to translate into a {@link KeylineState}.
   * @return the {@link KeylineState} associated with the arrangement with the lowest cost
   * according to the item count array priorities and how close it is to the target sizes.
   */
  static KeylineState createLeftAlignedKeylineState(
      @NonNull Context context,
      float childHorizontalMargins,
      float availableSpace,
      @NonNull Arrangement arrangement) {

    float extraSmallChildWidth = getExtraSmallSize(context) + childHorizontalMargins;

    float start = 0F;
    float extraSmallHeadCenterX = start - (extraSmallChildWidth / 2F);

    float largeStartCenterX = start + (arrangement.largeSize / 2F);
    float largeEndCenterX =
        largeStartCenterX + (max(0, arrangement.largeCount - 1) * arrangement.largeSize);
    start = largeEndCenterX + arrangement.largeSize / 2F;

    float mediumCenterX =
        arrangement.mediumCount > 0 ? start + (arrangement.mediumSize / 2F) : largeEndCenterX;
    start = arrangement.mediumCount > 0 ? mediumCenterX + (arrangement.mediumSize / 2F) : start;

    float smallStartCenterX =
        arrangement.smallCount > 0 ? start + (arrangement.smallSize / 2F) : mediumCenterX;

    float extraSmallTailCenterX = availableSpace + (extraSmallChildWidth / 2F);

    float extraSmallMask =
        getChildMaskPercentage(extraSmallChildWidth, arrangement.largeSize, childHorizontalMargins);
    float smallMask =
        getChildMaskPercentage(
            arrangement.smallSize, arrangement.largeSize, childHorizontalMargins);
    float mediumMask =
        getChildMaskPercentage(
            arrangement.mediumSize, arrangement.largeSize, childHorizontalMargins);
    float largeMask = 0F;

    KeylineState.Builder builder =
        new KeylineState.Builder(arrangement.largeSize)
            .addKeyline(extraSmallHeadCenterX, extraSmallMask, extraSmallChildWidth)
            .addKeylineRange(
                largeStartCenterX, largeMask, arrangement.largeSize, arrangement.largeCount, true);
    if (arrangement.mediumCount > 0) {
      builder.addKeyline(mediumCenterX, mediumMask, arrangement.mediumSize);
    }
    if (arrangement.smallCount > 0) {
      builder.addKeylineRange(
          smallStartCenterX, smallMask, arrangement.smallSize, arrangement.smallCount);
    }
    builder.addKeyline(extraSmallTailCenterX, extraSmallMask, extraSmallChildWidth);
    return builder.build();
  }

  static int maxValue(int[] array) {
    int largest = Integer.MIN_VALUE;
    for (int j : array) {
      if (j > largest) {
        largest = j;
      }
    }

    return largest;
  }
}
