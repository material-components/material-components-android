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
import androidx.core.math.MathUtils;

/**
 * A {@link CarouselStrategy} that knows how to size and fit one large item and one small item into
 * a container to create a layout to browse one 'hero' item at a time with a preview item.
 *
 * <p>Note that this strategy resizes Carousel items to take up the full width of the Carousel, save
 * room for the small item.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 */
public class HeroCarouselStrategy extends CarouselStrategy {

  private static final int[] SMALL_COUNTS = new int[] {1};
  private static final int[] MEDIUM_COUNTS = new int[] {0};

  @Override
  @NonNull
  KeylineState onFirstChildMeasuredWithMargins(@NonNull Carousel carousel, @NonNull View child) {
    float availableSpace = carousel.getContainerWidth();

    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childHorizontalMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;

    float smallChildWidthMin = getSmallSizeMin(child.getContext()) + childHorizontalMargins;
    float smallChildWidthMax = getSmallSizeMax(child.getContext()) + childHorizontalMargins;

    float measuredChildWidth = availableSpace;
    float targetLargeChildWidth = min(measuredChildWidth + childHorizontalMargins, availableSpace);
    // Ideally we would like to create a balanced arrangement where a small item is 1/3 the size of
    // the large item. Clamp the small target size within our min-max range and as close to 1/3 of
    // the target large item size as possible.
    float targetSmallChildWidth =
        MathUtils.clamp(
            measuredChildWidth / 3F + childHorizontalMargins,
            getSmallSizeMin(child.getContext()) + childHorizontalMargins,
            getSmallSizeMax(child.getContext()) + childHorizontalMargins);
    float targetMediumChildWidth = (targetLargeChildWidth + targetSmallChildWidth) / 2F;

    // Find the minimum space left for large items after filling the carousel with the most
    // permissible small items to determine a plausible minimum large count.
    float minAvailableLargeSpace =
        availableSpace
            - (smallChildWidthMax * maxValue(SMALL_COUNTS));
    int largeCountMin = (int) max(1, floor(minAvailableLargeSpace / targetLargeChildWidth));
    int largeCountMax = (int) ceil(availableSpace / targetLargeChildWidth);
    int[] largeCounts = new int[largeCountMax - largeCountMin + 1];
    for (int i = 0; i < largeCounts.length; i++) {
      largeCounts[i] = largeCountMin + i;
    }
    Arrangement arrangement = Arrangement.findLowestCostArrangement(
        availableSpace,
        targetSmallChildWidth,
        smallChildWidthMin,
        smallChildWidthMax,
        SMALL_COUNTS,
        targetMediumChildWidth,
        MEDIUM_COUNTS,
        targetLargeChildWidth,
        largeCounts);
    return createLeftAlignedKeylineState(
        child.getContext(), childHorizontalMargins, availableSpace, arrangement);
  }
}
