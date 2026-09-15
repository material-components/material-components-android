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
import static java.lang.Math.min;

import android.content.Context;
import androidx.annotation.NonNull;
import com.google.android.material.carousel.CarouselLayoutManager.Alignment;

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

  static KeylineState createKeylineState(
      @NonNull Context context,
      float childMargins,
      int availableSpace,
      @NonNull Arrangement arrangement,
      @Alignment int alignment) {
    if (alignment == CarouselLayoutManager.ALIGNMENT_CENTER) {
      return createCenterAlignedKeylineState(context, childMargins, availableSpace, arrangement);
    }
    return createLeftAlignedKeylineState(context, childMargins, availableSpace, arrangement);
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
      int availableSpace,
      @NonNull Arrangement arrangement) {

    float extraSmallChildWidth =
        min(getExtraSmallSize(context) + childHorizontalMargins, arrangement.largeSize);

    float start = 0F;
    float extraSmallHeadCenterX = start - (extraSmallChildWidth / 2F);

    float largeStartCenterX = addStart(start, arrangement.largeSize, arrangement.largeCount);
    float largeEndCenterX =
        addEnd(largeStartCenterX, arrangement.largeSize, arrangement.largeCount);
    start =
        updateCurPosition(start, largeEndCenterX, arrangement.largeSize, arrangement.largeCount);

    float mediumCenterX = addStart(start, arrangement.mediumSize, arrangement.mediumCount);
    start =
        updateCurPosition(start, mediumCenterX, arrangement.mediumSize, arrangement.mediumCount);

    float smallStartCenterX = addStart(start, arrangement.smallSize, arrangement.smallCount);

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
        new KeylineState.Builder(arrangement.largeSize, availableSpace)
            .addAnchorKeyline(extraSmallHeadCenterX, extraSmallMask, extraSmallChildWidth)
            .addKeylineRange(
                largeStartCenterX, largeMask, arrangement.largeSize, arrangement.largeCount, true);
    if (arrangement.mediumCount > 0) {
      builder.addKeyline(mediumCenterX, mediumMask, arrangement.mediumSize);
    }
    if (arrangement.smallCount > 0) {
      builder.addKeylineRange(
          smallStartCenterX, smallMask, arrangement.smallSize, arrangement.smallCount);
    }
    builder.addAnchorKeyline(extraSmallTailCenterX, extraSmallMask, extraSmallChildWidth);
    return builder.build();
  }

  /**
   * Gets the {@link KeylineState} associated with the given parameters, with the focal item
   * in the center.
   *
   * @param context The context used to load resources.
   * @param childHorizontalMargins The child margins to use when calculating mask percentage.
   * @param availableSpace the space that the {@link KeylineState} needs to fit.
   * @param arrangement the {@link Arrangement} to translate into a {@link KeylineState}.
   * @return the {@link KeylineState} associated with the arrangement with the lowest cost
   * according to the item count array priorities and how close it is to the target sizes.
   */
  static KeylineState createCenterAlignedKeylineState(
      @NonNull Context context,
      float childHorizontalMargins,
      int availableSpace,
      @NonNull Arrangement arrangement) {

    float extraSmallChildWidth =
        min(getExtraSmallSize(context) + childHorizontalMargins, arrangement.largeSize);

    float start = 0F;
    float extraSmallHeadCenterX = start - (extraSmallChildWidth / 2F);

    // Half of the small items
    float halfSmallStartCenterX = addStart(start, arrangement.smallSize, arrangement.smallCount);
    float halfSmallEndCenterX =
        addEnd(
            halfSmallStartCenterX,
            arrangement.smallSize,
            (int) Math.floor((float) arrangement.smallCount / 2F));
    start =
        updateCurPosition(
            start, halfSmallEndCenterX, arrangement.smallSize, arrangement.smallCount);

    // Half of the medium items
    float halfMediumStartCenterX = addStart(start, arrangement.mediumSize, arrangement.mediumCount);
    float halfMediumEndCenterX =
        addEnd(
            halfMediumStartCenterX,
            arrangement.mediumSize,
            (int) Math.floor((float) arrangement.mediumCount / 2F));
    start =
        updateCurPosition(
            start, halfMediumEndCenterX, arrangement.mediumSize, arrangement.mediumCount);

    float largeStartCenterX = addStart(start, arrangement.largeSize, arrangement.largeCount);
    float largeEndCenterX =
        addEnd(largeStartCenterX, arrangement.largeSize, arrangement.largeCount);
    start =
        updateCurPosition(start, largeEndCenterX, arrangement.largeSize, arrangement.largeCount);

    float secondHalfMediumStartCenterX =
        addStart(start, arrangement.mediumSize, arrangement.mediumCount);
    float secondHalfMediumEndCenterX =
        addEnd(
            secondHalfMediumStartCenterX,
            arrangement.mediumSize,
            (int) Math.ceil((float) arrangement.mediumCount / 2F));
    start =
        updateCurPosition(
            start, secondHalfMediumEndCenterX, arrangement.mediumSize, arrangement.mediumCount);

    float secondHalfSmallStartCenterX =
        addStart(start, arrangement.smallSize, arrangement.smallCount);

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
        new KeylineState.Builder(arrangement.largeSize, availableSpace)
            .addAnchorKeyline(extraSmallHeadCenterX, extraSmallMask, extraSmallChildWidth);
    if (arrangement.smallCount > 0) {
      builder.addKeylineRange(
          halfSmallStartCenterX,
          smallMask,
          arrangement.smallSize,
          (int) Math.floor(arrangement.smallCount / 2F));
    }
    if (arrangement.mediumCount > 0) {
      builder.addKeylineRange(
          halfMediumStartCenterX,
          mediumMask,
          arrangement.mediumSize,
          (int) Math.floor(arrangement.mediumCount / 2F));
    }

    builder.addKeylineRange(
        largeStartCenterX, largeMask, arrangement.largeSize, arrangement.largeCount, true);

    if (arrangement.mediumCount > 0) {
      builder.addKeylineRange(
          secondHalfMediumStartCenterX,
          mediumMask,
          arrangement.mediumSize,
          (int) Math.ceil(arrangement.mediumCount / 2F));
    }

    if (arrangement.smallCount > 0) {
      builder.addKeylineRange(
          secondHalfSmallStartCenterX,
          smallMask,
          arrangement.smallSize,
          (int) Math.ceil(arrangement.smallCount / 2F));
    }

    builder.addAnchorKeyline(extraSmallTailCenterX, extraSmallMask, extraSmallChildWidth);
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

  /**
   * Helper method to calculate the keyline position of the start of the item block according to the
   * start position, item size, and item count.
   */
  static float addStart(float start, float itemSize, int count) {
    if (count > 0) {
      return start + itemSize / 2F;
    }
    return start;
  }

  /**
   * Helper method to calculate the keyline position of the end of the item block according to the
   * start position, item size, and item count.
   */
  static float addEnd(float startKeylinePos, float itemSize, int count) {
    return startKeylinePos + (max(0, count - 1) * itemSize);
  }

  /**
   * Helper method to update the current position of the keyline calculations after the last keyline
   * in the item range.
   */
  static float updateCurPosition(
      float curPosition, float lastEndKeyline, float itemSize, int count) {
    if (count > 0) {
      return lastEndKeyline + itemSize / 2F;
    }
    return curPosition;
  }
}
