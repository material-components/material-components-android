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
import static com.google.android.material.carousel.CarouselStrategyHelper.getSmallSizeMin;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/**
 * A {@link CarouselStrategy} that does not resize the original item width and fits as many as it
 * can into the container, cutting off the rest. Cut off items may be resized in order to show an
 * effect of items getting smaller at the ends.
 *
 * Note that this strategy does not adjust the size of large items. Item widths are taken
 * from the {@link androidx.recyclerview.widget.RecyclerView} item width.
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

  @RestrictTo(Scope.LIBRARY_GROUP)
  public UncontainedCarouselStrategy() {
  }

  @Override
  @NonNull
  KeylineState onFirstChildMeasuredWithMargins(@NonNull Carousel carousel, @NonNull View child) {
    float availableSpace =
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
    int largeCount = (int) Math.floor(availableSpace/largeChildSize);
    float remainingSpace = availableSpace - largeCount*largeChildSize;
    int mediumCount = 0;
    boolean isCenter = carousel.getCarouselAlignment() == CarouselLayoutManager.ALIGNMENT_CENTER;

    if (isCenter) {
      remainingSpace /= 2F;
      float smallChildSizeMin = getSmallSizeMin(child.getContext()) + childMargins;
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

    // If the keyline location for the next large size would be within the remaining space,
    // then we can place a large child there as the last non-anchor keyline because visually
    // keylines will become smaller as it goes past the large keyline location.
    // Otherwise, we want to add a medium item instead so that visually there will still
    // be the effect of the item getting smaller closer to the end.
    if (remainingSpace > largeChildSize/2F) {
      largeCount += 1;
    } else {
      mediumCount = 1;
      // We want the medium size to be large enough to be at least 1/3 of the way cut
      // off.
      mediumChildSize = max(remainingSpace + remainingSpace/2F, mediumChildSize);
    }

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

  private KeylineState createCenterAlignedKeylineState(
      float availableSpace,
      float childMargins,
      float largeSize,
      int largeCount,
      float mediumSize,
      float xSmallSize,
      float remainingSpace) {

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
      float availableSpace,
      float largeSize,
      int largeCount,
      float mediumSize,
      int mediumCount,
      float xSmallSize) {

    float extraSmallMask =
        getChildMaskPercentage(xSmallSize, largeSize, childMargins);
    float mediumMask =
        getChildMaskPercentage(mediumSize, largeSize, childMargins);
    float largeMask = 0F;

    float start = 0F;
    float extraSmallHeadCenterX = start - (xSmallSize / 2F);

    float largeStartCenterX = largeSize/2F;
    // We exclude one large item from the large count so we can calculate the correct
    // cutoff value for the last large item if it is getting cut off.
    int excludedLargeCount = max(largeCount - 1, 1);
    start += excludedLargeCount * largeSize;
    // Where to start laying the last possibly-cutoff large item.
    float lastEndStart = start + largeSize / 2F;

    // Add xsmall keyline, and then if there is more than 1 large keyline, add
    // however many large keylines there are except for the last one that may be cut off.
    KeylineState.Builder builder =
        new KeylineState.Builder(largeSize, availableSpace)
            .addAnchorKeyline(extraSmallHeadCenterX, extraSmallMask, xSmallSize)
            .addKeylineRange(
                largeStartCenterX,
                largeMask,
                largeSize,
                excludedLargeCount,
                /* isFocal= */ true);

    // If we have more than 1 large item, then here we include the last large item that is
    // possibly getting cut off.
    if (largeCount > 1) {
      start += largeSize;
      builder.addKeyline(
          lastEndStart,
          largeMask,
          largeSize,
          /* isFocal= */ true);
    }

    if (mediumCount > 0) {
      float mediumCenterX = start + mediumSize / 2F;
      start += mediumSize;
      builder.addKeyline(
          mediumCenterX,
          mediumMask,
          mediumSize,
          /* isFocal= */ false);
    }

    float xSmallCenterX = start + getExtraSmallSize(context) / 2F;
    builder.addAnchorKeyline(xSmallCenterX, extraSmallMask, xSmallSize);
    return builder.build();
  }

  @Override
  boolean isContained() {
    return false;
  }
}
