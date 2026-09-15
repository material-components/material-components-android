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

import static com.google.android.material.carousel.CarouselStrategyHelper.getExtraSmallSize;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * A {@link CarouselStrategy} that does not resize the original item width and fits as many as it
 * can into the container, cutting off the rest. Cut off items may be resized in order to show an
 * effect of items getting smaller at the ends.
 *
 * <p>Note that this strategy does not adjust the size of large items. Item widths are taken from
 * the {@link androidx.recyclerview.widget.RecyclerView} item width.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md">component
 * developer guidance</a> and <a href="https://material.io/components/carousel/overview">design
 * guidelines</a>.
 */
public final class UncontainedCarouselStrategy extends CarouselStrategy {

  private static final float MEDIUM_LARGE_ITEM_PERCENTAGE_THRESHOLD = 0.85F;

  @Override
  @NonNull
  public KeylineState onFirstChildMeasuredWithMargins(
      @NonNull Carousel carousel, @NonNull View child) {
    int availableSpace =
        carousel.isHorizontal() ? carousel.getContainerWidth() : carousel.getContainerHeight();

    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
    float measuredChildSize = child.getMeasuredHeight();

    if (carousel.isHorizontal()) {
      childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
      measuredChildSize = child.getMeasuredWidth();
    }

    float largeChildSize = measuredChildSize + childMargins;
    float mediumChildSize = getExtraSmallSize(child.getContext()) + childMargins;
    float xSmallChildSize = getExtraSmallSize(child.getContext()) + childMargins;

    // Calculate how much space there is remaining after squeezing in as many large items as we can.
    int largeCount = max(1, (int) Math.floor(availableSpace/largeChildSize));
    float remainingSpace = availableSpace - largeCount*largeChildSize;
    boolean isCenter = carousel.getCarouselAlignment() == CarouselLayoutManager.ALIGNMENT_CENTER;

    if (isCenter) {
      remainingSpace /= 2F;
      float smallChildSizeMin = getSmallItemSizeMin() + childMargins;
      // Ideally we would like to choose a size 3x the remaining space such that 2/3 are cut off.
      // If this is bigger than the large child size however, we limit the child size to the large
      // child size.
      mediumChildSize = min(3*remainingSpace, largeChildSize);

      // We also have a minimum child width such that the size is not too small.
      mediumChildSize = max(mediumChildSize, smallChildSizeMin);

      // Note that a center aligned keyline state will always have exactly 2 mediums with this
      // strategy; one to be cut off at the front, and one for the end.
      return createCenterAlignedKeylineState(
          availableSpace,
          childMargins,
          largeChildSize,
          largeCount,
          mediumChildSize,
          xSmallChildSize,
          remainingSpace);
    }

    int mediumCount = 0;

    if (remainingSpace > 0) {
      mediumCount = 1;
    }

    // Calculate the medium size so that it fulfils certain criteria.
    mediumChildSize = calculateMediumChildSize(mediumChildSize, largeChildSize, remainingSpace);

    return createLeftAlignedKeylineState(
        child.getContext(),
        childMargins,
        availableSpace,
        largeChildSize,
        largeCount,
        mediumChildSize,
        mediumCount,
        xSmallChildSize);
  }

  /**
   * Calculates a size of a medium child in the carousel that is not bigger than the large child
   * size, and attempts to be small enough such that there is a size disparity between the medium
   * and large sizes, but large enough to have a sufficient percentage cut off.
   */
  private float calculateMediumChildSize(
      float mediumChildSize, float largeChildSize, float remainingSpace) {
    // With the remaining space, we want to add a 'medium' item that gets sufficiently cut off
    // but is close enough to the anchor keyline such that there is a range of motion.
    // Ideally the medium child size is large enough such that a third is cut off.
    mediumChildSize = max(remainingSpace * 1.5f, mediumChildSize);
    // The size we wish to limit the medium size to.
    float largeItemThreshold = largeChildSize * MEDIUM_LARGE_ITEM_PERCENTAGE_THRESHOLD;

    // If the medium child is larger than the threshold percentage of the large child size,
    // it's too similar and won't create sufficient motion when scrolling items between the large
    // items and the medium item.
    if (mediumChildSize > largeItemThreshold) {
      // Choose whichever is bigger between the maximum threshold of the medium child size, or
      // a size such that only 20% of the space is cut off.
      mediumChildSize =
          max(largeItemThreshold, remainingSpace * 1.2F);
    }

    // Ensure that the final medium size is not larger than the large size.
    mediumChildSize = min(largeChildSize, mediumChildSize);
    return mediumChildSize;
  }

  private KeylineState createCenterAlignedKeylineState(
      int availableSpace,
      float childMargins,
      float largeSize,
      int largeCount,
      float mediumSize,
      float xSmallSize,
      float remainingSpace) {
    xSmallSize = min(xSmallSize, largeSize);

    float extraSmallMask = getChildMaskPercentage(xSmallSize, largeSize, childMargins);
    float mediumMask = getChildMaskPercentage(mediumSize, largeSize, childMargins);
    float largeMask = 0F;

    float start = 0F;
    // Take the remaining space and show as much as you can
    float firstMediumCenterX = start + remainingSpace - mediumSize/2F;
    start = firstMediumCenterX + mediumSize / 2F;
    float extraSmallHeadCenterX = firstMediumCenterX - mediumSize / 2F - (xSmallSize / 2F);

    float largeStartCenterX = start + largeSize / 2F;
    start += largeCount * largeSize;

    KeylineState.Builder builder =
        new KeylineState.Builder(largeSize, availableSpace)
            .addAnchorKeyline(extraSmallHeadCenterX, extraSmallMask, xSmallSize)
            .addKeyline(firstMediumCenterX, mediumMask, mediumSize, false)
            .addKeylineRange(largeStartCenterX, largeMask, largeSize, largeCount, true);

    float secondMediumCenterX = start + mediumSize / 2F;
    start += mediumSize;
    builder.addKeyline(
        secondMediumCenterX, mediumMask, mediumSize, false);

    float xSmallCenterX = start + xSmallSize / 2F;
    builder.addAnchorKeyline(xSmallCenterX, extraSmallMask, xSmallSize);
    return builder.build();
  }

  private KeylineState createLeftAlignedKeylineState(
      Context context,
      float childMargins,
      int availableSpace,
      float largeSize,
      int largeCount,
      float mediumSize,
      int mediumCount,
      float xSmallSize) {
    xSmallSize = min(xSmallSize, largeSize);

    // Make the left anchor size half the cut off item size to make the motion at the left closer
    // to the right where the cut off is.
    float leftAnchorSize = max(xSmallSize, mediumSize * 0.5F);
    float leftAnchorMask = getChildMaskPercentage(leftAnchorSize, largeSize, childMargins);
    float extraSmallMask =
        getChildMaskPercentage(xSmallSize, largeSize, childMargins);
    float mediumMask =
        getChildMaskPercentage(mediumSize, largeSize, childMargins);
    float largeMask = 0F;

    float start = 0F;
    float leftAnchorCenterX = start - (leftAnchorSize / 2F);

    float largeStartCenterX = largeSize/2F;
    start += largeCount * largeSize;

    // Add xsmall keyline, and then if there is more than 1 large keyline, add
    // however many large keylines there are except for the last one that may be cut off.
    KeylineState.Builder builder =
        new KeylineState.Builder(largeSize, availableSpace)
            .addAnchorKeyline(leftAnchorCenterX, leftAnchorMask, leftAnchorSize)
            .addKeylineRange(
                largeStartCenterX,
                largeMask,
                largeSize,
                largeCount,
                /* isFocal= */ true);

    if (mediumCount > 0) {
      float mediumCenterX = start + mediumSize / 2F;
      start += mediumSize;
      builder.addKeyline(mediumCenterX, mediumMask, mediumSize, /* isFocal= */ false);
    }

    float xSmallCenterX = start + getExtraSmallSize(context) / 2F;
    builder.addAnchorKeyline(xSmallCenterX, extraSmallMask, xSmallSize);
    return builder.build();
  }

  @Override
  StrategyType getStrategyType() {
    return StrategyType.UNCONTAINED;
  }
}
