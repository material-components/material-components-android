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

import static com.google.android.material.carousel.CarouselStrategyHelper.createKeylineState;
import static com.google.android.material.carousel.CarouselStrategyHelper.maxValue;
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;
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
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md">component
 * developer guidance</a> and <a href="https://material.io/components/carousel/overview">design
 * guidelines</a>.
 */
public final class MultiBrowseCarouselStrategy extends CarouselStrategy {

  private static final int[] SMALL_COUNTS = new int[] {1};
  private static final int[] MEDIUM_COUNTS = new int[] {1, 0};

  // Current count of number of keylines. We want to refresh the strategy if there are less items
  // than this number.
  private int keylineCount = 0;

  @Override
  @NonNull
  public KeylineState onFirstChildMeasuredWithMargins(
      @NonNull Carousel carousel, @NonNull View child) {
    int carouselSize = carousel.getContainerHeight();
    if (carousel.isHorizontal()) {
      carouselSize = carousel.getContainerWidth();
    }

    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
    float measuredChildSize = child.getMeasuredHeight();

    if (carousel.isHorizontal()) {
      childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
      measuredChildSize = child.getMeasuredWidth();
    }

    float smallChildSizeMin = getSmallItemSizeMin() + childMargins;
    float smallChildSizeMax = getSmallItemSizeMax() + childMargins;
    smallChildSizeMax = max(smallChildSizeMax, smallChildSizeMin);

    float targetLargeChildSize = min(measuredChildSize + childMargins, carouselSize);
    // Ideally we would like to create a balanced arrangement where a small item is 1/3 the size of
    // the large item and medium items are sized between large and small items. Clamp the small
    // target size within our min-max range and as close to 1/3 of the target large item size as
    // possible.
    float targetSmallChildSize =
        MathUtils.clamp(
            measuredChildSize / 3F + childMargins,
            smallChildSizeMin + childMargins,
            smallChildSizeMax + childMargins);
    float targetMediumChildSize = (targetLargeChildSize + targetSmallChildSize) / 2F;

    // Create arrays representing the possible count of small, medium, and large items. These are
    // not in an asc./dec. order but are in order of priority. A small count array of { 2, 3, 1 }
    // says that ideally an arrangement with 2 small items is found, then 3 is next most desirable,
    // then finally 1.

    int[] smallCounts = SMALL_COUNTS;
    if (carouselSize <= smallChildSizeMin * 2) {
      // If the available space is too small to fit a large item and small item and a large item
      // (large items must be at least as big as a small item), allow arrangements with no small
      // items.
      smallCounts = new int[] { 0 };
    }

    int[] mediumCounts = MEDIUM_COUNTS;
    if (carousel.getCarouselAlignment() == CarouselLayoutManager.ALIGNMENT_CENTER) {
      smallCounts = doubleCounts(smallCounts);
      mediumCounts = doubleCounts(mediumCounts);
    }

    // Find the minimum space left for large items after filling the carousel with the most
    // permissible medium and small items to determine a plausible minimum large count.
    float minAvailableLargeSpace =
        carouselSize
            - (targetMediumChildSize * maxValue(mediumCounts))
            - (smallChildSizeMax * maxValue(smallCounts));
    int largeCountMin = (int) max(1, floor(minAvailableLargeSpace / targetLargeChildSize));
    int largeCountMax = (int) ceil(carouselSize / targetLargeChildSize);
    int[] largeCounts = new int[largeCountMax - largeCountMin + 1];
    for (int i = 0; i < largeCounts.length; i++) {
      largeCounts[i] = largeCountMax - i;
    }

    Arrangement arrangement = Arrangement.findLowestCostArrangement(
        carouselSize,
        targetSmallChildSize,
        smallChildSizeMin,
        smallChildSizeMax,
        smallCounts,
        targetMediumChildSize,
        mediumCounts,
        targetLargeChildSize,
        largeCounts);

    keylineCount = arrangement.getItemCount();

    boolean refreshArrangement =
        ensureArrangementFitsItemCount(arrangement, carousel.getItemCount());

    // Ensure that the arrangement is never only filled with large items unless there's no space for
    // 2 small item.
    if (arrangement.mediumCount == 0
        && arrangement.smallCount == 0
        && carouselSize > 2 * smallChildSizeMin) {
      arrangement.smallCount = 1;
      refreshArrangement = true;
    }

    if (refreshArrangement) {
      // In case counts changed after ensuring the previous arrangement fit the item
      // counts, we call `findLowestCostArrangement` again with the item counts set.
      arrangement =
          Arrangement.findLowestCostArrangement(
              carouselSize,
              targetSmallChildSize,
              smallChildSizeMin,
              smallChildSizeMax,
              new int[] {arrangement.smallCount},
              targetMediumChildSize,
              new int[] {arrangement.mediumCount},
              targetLargeChildSize,
              new int[] {arrangement.largeCount});
    }

    return createKeylineState(
        child.getContext(),
        childMargins,
        carouselSize,
        arrangement,
        carousel.getCarouselAlignment());
  }

  boolean ensureArrangementFitsItemCount(Arrangement arrangement, int carouselItemCount) {
    int keylineSurplus = arrangement.getItemCount() - carouselItemCount;
    boolean changed =
        keylineSurplus > 0 && (arrangement.smallCount > 0 || arrangement.mediumCount > 1);

    while (keylineSurplus > 0) {
      if (arrangement.smallCount > 0) {
        arrangement.smallCount -= 1;
      } else if (arrangement.mediumCount > 1) {
        // Keep at least 1 medium so the large items don't fill the entire carousel in new strategy.
        arrangement.mediumCount -= 1;
      }
      // large items don't need to be removed even if they are a surplus because large items
      // are already fully unmasked.
      keylineSurplus -= 1;
    }

    return changed;
  }

  @Override
  public boolean shouldRefreshKeylineState(@NonNull Carousel carousel, int oldItemCount) {
    return (oldItemCount < keylineCount && carousel.getItemCount() >= keylineCount)
        || (oldItemCount >= keylineCount && carousel.getItemCount() < keylineCount);
  }
}
