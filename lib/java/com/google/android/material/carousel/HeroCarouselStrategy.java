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
 * A {@link CarouselStrategy} that knows how to size and fit one large item and one small item into
 * a container to create a layout to browse one 'hero' item at a time with a preview item.
 *
 * <p>Note that this strategy resizes Carousel items to take up the full width or height of the
 * Carousel, save room for the small item.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md">component
 * developer guidance</a> and <a href="https://material.io/components/carousel/overview">design
 * guidelines</a>.
 */
public class HeroCarouselStrategy extends CarouselStrategy {

  private static final int[] SMALL_COUNTS = new int[] {1};
  private static final int[] MEDIUM_COUNTS = new int[] {0, 1};

  // Current count of number of keylines. We want to refresh the strategy if there are less items
  // than this number.
  private int keylineCount = 0;

  @Override
  @NonNull
  public KeylineState onFirstChildMeasuredWithMargins(
      @NonNull Carousel carousel, @NonNull View child) {
    int availableSpace = carousel.getContainerHeight();
    if (carousel.isHorizontal()) {
      availableSpace = carousel.getContainerWidth();
    }

    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;

    float measuredChildSize = child.getMeasuredWidth() * 2;

    if (carousel.isHorizontal()) {
      childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
      measuredChildSize = child.getMeasuredHeight() * 2;
    }

    float smallChildSizeMin = getSmallItemSizeMin() + childMargins;
    float smallChildSizeMax = getSmallItemSizeMax() + childMargins;
    // Ensure that the max size at least as big as the small size.
    smallChildSizeMax = max(smallChildSizeMax, smallChildSizeMin);

    float targetLargeChildSize = min(measuredChildSize + childMargins, availableSpace);
    // Ideally we would like to create a balanced arrangement where a small item is 1/3 the size of
    // the large item. Clamp the small target size within our min-max range and as close to 1/3 of
    // the target large item size as possible.
    float targetSmallChildSize =
        MathUtils.clamp(
            measuredChildSize / 3F + childMargins,
            smallChildSizeMin + childMargins,
            smallChildSizeMax + childMargins);
    float targetMediumChildSize = (targetLargeChildSize + targetSmallChildSize) / 2F;

    int[] smallCounts = SMALL_COUNTS;
    if (availableSpace < smallChildSizeMin * 2) {
      smallCounts = new int[] { 0 };
    }

    // Find the minimum space left for large items after filling the carousel with the most
    // permissible small items to determine a plausible minimum large count.
    float minAvailableLargeSpace = availableSpace - (smallChildSizeMax * maxValue(SMALL_COUNTS));
    int largeCountMin = (int) max(1, floor(minAvailableLargeSpace / targetLargeChildSize));
    int largeCountMax = (int) ceil(availableSpace / targetLargeChildSize);
    int[] largeCounts = new int[largeCountMax - largeCountMin + 1];
    for (int i = 0; i < largeCounts.length; i++) {
      largeCounts[i] = largeCountMin + i;
    }
    boolean isCenterAligned =
        carousel.getCarouselAlignment() == CarouselLayoutManager.ALIGNMENT_CENTER;
    Arrangement arrangement =
        Arrangement.findLowestCostArrangement(
            availableSpace,
            targetSmallChildSize,
            smallChildSizeMin,
            smallChildSizeMax,
                isCenterAligned
                ? doubleCounts(smallCounts)
                : smallCounts,
            targetMediumChildSize,
                isCenterAligned
                ? doubleCounts(MEDIUM_COUNTS)
                : MEDIUM_COUNTS,
            targetLargeChildSize,
            largeCounts);

    keylineCount = arrangement.getItemCount();

    // If there's less items than keylines, force it to be start-aligned.
    if (arrangement.getItemCount() > carousel.getItemCount()) {
      isCenterAligned = false;
      arrangement =
          Arrangement.findLowestCostArrangement(
              availableSpace,
              targetSmallChildSize,
              smallChildSizeMin,
              smallChildSizeMax,
              smallCounts,
              targetMediumChildSize,
              MEDIUM_COUNTS,
              targetLargeChildSize,
              largeCounts);
    }

    return createKeylineState(
        child.getContext(),
        childMargins,
        availableSpace,
        arrangement,
        isCenterAligned
            ? CarouselLayoutManager.ALIGNMENT_CENTER
            : CarouselLayoutManager.ALIGNMENT_START);
    }

  @Override
  public boolean shouldRefreshKeylineState(@NonNull Carousel carousel, int oldItemCount) {
    return carousel.getCarouselAlignment() == CarouselLayoutManager.ALIGNMENT_CENTER
        && ((oldItemCount < keylineCount && carousel.getItemCount() >= keylineCount)
            || (oldItemCount >= keylineCount && carousel.getItemCount() < keylineCount));
  }
}

