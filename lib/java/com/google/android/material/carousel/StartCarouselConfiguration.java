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

import com.google.android.material.R;

import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.round;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * A Carousel configuration that creates a horizontal list with full sized items at the left of the
 * scroll container that mask as the approach the edges of the container.
 *
 * <p>Note that this configuration will adjust the size of large items. In order to ensure large,
 * medium, and small items both fit perfectly into the available space and are numbered/arranged in
 * a visually pleasing and opinionated way, this configuration finds the nearest number of large
 * items that will fit into an approved arrangement that requires the least amount of size
 * adjustment necessary.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 */
public final class StartCarouselConfiguration extends MultiBrowseCarouselConfiguration {

  // The percentage by which a medium item needs to be larger than a small item and smaller
  // than an large item. This is used to ensure a medium item is truly somewhere between the
  // small and large sizes, making for a visually balanced arrangement.
  // 0F would mean a medium item could be >= small item size and <= a large item size.
  // .25F means the medium item must be >= 125% of the small item size and <= 75% of the
  // large item size.
  private static final float MEDIUM_SIZE_PERCENTAGE_DELTA = .25F;

  // True if medium items should never be added and arrangements should consist of only large and
  // small items. This will often result in a greater number of large items but more variability in
  // large item size. This can be desirable when optimizing for the greatest number of fully
  // unmasked items visible at once.
  private final boolean forceCompactArrangement;

  public StartCarouselConfiguration(@NonNull Carousel carousel) {
    this(carousel, false);
  }

  public StartCarouselConfiguration(@NonNull Carousel carousel, boolean forceCompactArrangement) {
    super(carousel);
    this.forceCompactArrangement = forceCompactArrangement;
  }

  @Override
  protected float getExtraSmallSize(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_gone_size);
  }

  @Override
  @NonNull
  protected KeylineState onFirstChildMeasuredWithMargins(@NonNull View child) {
    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childHorizontalMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;

    float smallChildWidth = getSmallSize(child.getContext()) + childHorizontalMargins;
    float extraSmallChildWidth = getExtraSmallSize(child.getContext()) + childHorizontalMargins;

    float availableSpace = getCarousel().getContainerWidth();

    // The minimum viable arrangement is 1 large and 1 small child. A single large item size
    // cannot be greater than the available space minus a small child width.
    float maxLargeChildSpace = availableSpace - smallChildWidth;
    float largeChildWidth = child.getMeasuredWidth() + childHorizontalMargins;

    int largeCount;
    int mediumCount;
    int smallCount;
    float mediumChildWidth;

    if (maxLargeChildSpace <= smallChildWidth) {
      // There is not enough space to show a small and a large item. Remove the small item and
      // default to showing a single, fullscreen item.
      largeCount = 1;
      largeChildWidth = availableSpace;
      mediumCount = 0;
      mediumChildWidth = 0;
      smallCount = 0;
    } else if (largeChildWidth >= maxLargeChildSpace) {
      // There is only enough space to show 1 large, and 1 small item.
      largeCount = 1;
      largeChildWidth = maxLargeChildSpace;
      mediumCount = 0;
      mediumChildWidth = 0F;
      smallCount = 1;
    } else {
      // There is enough space for some combination of large items, an optional medium item,
      // and a small item. Find the arrangement where large items need to be adjusted in
      // size by the least amount.
      float mediumChildMinWidth =
          smallChildWidth + (smallChildWidth * MEDIUM_SIZE_PERCENTAGE_DELTA);
      // TODO: Ensure this is always <= expanded size even after expanded size is adjusted.
      float mediumChildMaxWidth =
          largeChildWidth - (largeChildWidth * MEDIUM_SIZE_PERCENTAGE_DELTA);

      float largeRangeMin = availableSpace - (mediumChildMaxWidth + smallChildWidth);
      float largeRangeMax = availableSpace - (mediumChildMinWidth + smallChildWidth);

      // The standard arrangement is `x` large, 1 medium, and 1 small item where `x` is the
      // maximum number of large items that can fit within the available space.
      float standardLargeRangeCenter = (largeRangeMin + largeRangeMax) / 2;
      float standardLargeQuotient = standardLargeRangeCenter / largeChildWidth;
      int standardLargeCount = round(standardLargeQuotient);
      float standardLargeChildWidth = largeChildWidth;
      // If the largeChildWidth * count falls outside of the large min-max range, the width of
      // large children for the standard arrangement needs to be adjusted. Make the smallest
      // adjustment possible to bring the number of large children back to fit within the
      // available space.
      if (largeChildWidth * standardLargeCount < largeRangeMin) {
        standardLargeChildWidth = largeRangeMin / standardLargeCount;
      } else if (largeChildWidth * standardLargeCount > largeRangeMax) {
        standardLargeChildWidth = largeRangeMax / standardLargeCount;
      }

      // The compact arrangement is `x` large, and 1 small item where `x` is the maximum
      // number of large items that can fit within the available space.
      float compactLargeQuotient = (availableSpace - smallChildWidth) / largeChildWidth;
      int compactLargeCount = round(compactLargeQuotient);
      // Adjust the largeChildWidth so largeChildWidth * largeCount fits perfectly within
      // the available space.
      float compactLargeChildWidth = (availableSpace - smallChildWidth) / compactLargeCount;

      // Use the arrangement type which requires the large item size to be adjusted the least,
      // retaining the developer specified item size as much as possible.
      if (abs(largeChildWidth - standardLargeChildWidth)
              <= abs(largeChildWidth - compactLargeChildWidth)
          && !forceCompactArrangement) {
        largeCount = standardLargeCount;
        largeChildWidth = standardLargeChildWidth;
        mediumCount = 1;
        mediumChildWidth = availableSpace - (largeChildWidth * largeCount) - smallChildWidth;
        smallCount = 1;
      } else {
        largeCount = compactLargeCount;
        largeChildWidth = compactLargeChildWidth;
        mediumCount = 0;
        mediumChildWidth = 0;
        smallCount = 1;
      }
    }

    float start = 0F;
    float extraSmallHeadCenterX = start - (extraSmallChildWidth / 2F);

    float largeStartCenterX = start + (largeChildWidth / 2F);
    float largeEndCenterX = largeStartCenterX + (max(0, largeCount - 1) * largeChildWidth);
    start = largeEndCenterX + largeChildWidth / 2F;

    float mediumCenterX = mediumCount > 0 ? start + (mediumChildWidth / 2F) : largeEndCenterX;
    start = mediumCount > 0 ? mediumCenterX + (mediumChildWidth / 2F) : start;

    float smallStartCenterX = smallCount > 0 ? start + (smallChildWidth / 2F) : mediumCenterX;

    float extraSmallTailCenterX = getCarousel().getContainerWidth() + (extraSmallChildWidth / 2F);

    float extraSmallMask =
        getChildMaskPercentage(extraSmallChildWidth, largeChildWidth, childHorizontalMargins);
    float smallMask =
        getChildMaskPercentage(smallChildWidth, largeChildWidth, childHorizontalMargins);
    float mediumMask =
        getChildMaskPercentage(mediumChildWidth, largeChildWidth, childHorizontalMargins);
    float largeMask = 0F;

    return new KeylineState.Builder(largeChildWidth)
        .addKeyline(extraSmallHeadCenterX, extraSmallMask, extraSmallChildWidth)
        .addKeylineRange(largeStartCenterX, largeMask, largeChildWidth, largeCount, true)
        .addKeyline(mediumCenterX, mediumMask, mediumChildWidth)
        .addKeylineRange(smallStartCenterX, smallMask, smallChildWidth, smallCount)
        .addKeyline(extraSmallTailCenterX, extraSmallMask, extraSmallChildWidth)
        .build();
  }
}
