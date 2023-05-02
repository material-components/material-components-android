/*
 * Copyright 2022 The Android Open Source Project
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

import static com.google.android.material.carousel.CarouselStrategyHelper.createLeftAlignedKeylineState;
import static com.google.android.material.carousel.CarouselStrategyHelper.getSmallSizeMax;
import static com.google.android.material.carousel.CarouselStrategyHelper.getSmallSizeMin;
import static com.google.android.material.carousel.CarouselStrategyHelper.maxValue;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.math.MathUtils;

/**
 * A {@link CarouselStrategy} that knows how to size and fit large, medium and small items into a
 * container to create a layout for quick browsing of multiple items at once.
 *
 * <p>Note that this strategy will adjust the size of large items. In order to ensure large, medium,
 * and small items both fit perfectly into the available space and are numbered/arranged in a
 * visually pleasing and opinionated way, this strategy finds the nearest number of large items that
 * will fit into an approved arrangement that requires the least amount of size adjustment
 * necessary.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 */
public final class MultiBrowseCarouselStrategy extends CarouselStrategy {

  private static final int[] SMALL_COUNTS = new int[] {1};
  private static final int[] MEDIUM_COUNTS = new int[] {1, 0};
  private static final int[] MEDIUM_COUNTS_COMPACT = new int[] {0};

  // True if medium items should never be added and arrangements should consist of only large and
  // small items. This will often result in a greater number of large items but more variability in
  // large item size. This can be desirable when optimizing for the greatest number of fully
  // unmasked items visible at once.
  // TODO(b/274604170): Remove this option
  private final boolean forceCompactArrangement;

  public MultiBrowseCarouselStrategy() {
    this(false);
  }

  /**
   * Create a new instance of {@link MultiBrowseCarouselStrategy}.
   *
   * @param forceCompactArrangement true if items should be fit in a way that maximizes the number
   *     of large, unmasked items. false if this strategy is free to determine an opinionated
   *     balance between item sizes.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public MultiBrowseCarouselStrategy(boolean forceCompactArrangement) {
    this.forceCompactArrangement = forceCompactArrangement;
  }

  @Override
  @NonNull
  KeylineState onFirstChildMeasuredWithMargins(@NonNull Carousel carousel, @NonNull View child) {
    float availableSpace = carousel.getContainerWidth();

    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childHorizontalMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;

    float smallChildWidthMin = getSmallSizeMin(child.getContext()) + childHorizontalMargins;
    float smallChildWidthMax = getSmallSizeMax(child.getContext()) + childHorizontalMargins;

    float measuredChildWidth = child.getMeasuredWidth();
    float targetLargeChildWidth = min(measuredChildWidth + childHorizontalMargins, availableSpace);
    // Ideally we would like to create a balanced arrangement where a small item is 1/3 the size of
    // the large item and medium items are sized between large and small items. Clamp the small
    // target size within our min-max range and as close to 1/3 of the target large item size as
    // possible.
    float targetSmallChildWidth =
        MathUtils.clamp(
            measuredChildWidth / 3F + childHorizontalMargins,
            getSmallSizeMin(child.getContext()) + childHorizontalMargins,
            getSmallSizeMax(child.getContext()) + childHorizontalMargins);
    float targetMediumChildWidth = (targetLargeChildWidth + targetSmallChildWidth) / 2F;

    // Create arrays representing the possible count of small, medium, and large items. These are
    // not in an asc./dec. order but are in order of priority. A small count array of { 2, 3, 1 }
    // says that ideally an arrangement with 2 small items is found, then 3 is next most desirable,
    // then finally 1.
    int[] smallCounts = SMALL_COUNTS;
    int[] mediumCounts = forceCompactArrangement ? MEDIUM_COUNTS_COMPACT : MEDIUM_COUNTS;
    // Find the minimum space left for large items after filling the carousel with the most
    // permissible medium and small items to determine a plausible minimum large count.
    float minAvailableLargeSpace =
        availableSpace
            - (targetMediumChildWidth * maxValue(mediumCounts))
            - (smallChildWidthMax * maxValue(smallCounts));
    int largeCountMin = (int) max(1, floor(minAvailableLargeSpace / targetLargeChildWidth));
    int largeCountMax = (int) ceil(availableSpace / targetLargeChildWidth);
    int[] largeCounts = new int[largeCountMax - largeCountMin + 1];
    for (int i = 0; i < largeCounts.length; i++) {
      largeCounts[i] = largeCountMax - i;
    }

    Arrangement arrangement = Arrangement.findLowestCostArrangement(
        availableSpace,
        targetSmallChildWidth,
        smallChildWidthMin,
        smallChildWidthMax,
        smallCounts,
        targetMediumChildWidth,
        mediumCounts,
        targetLargeChildWidth,
        largeCounts);

    return createLeftAlignedKeylineState(
        child.getContext(),
        childHorizontalMargins,
        availableSpace,
        arrangement);
  }
}
