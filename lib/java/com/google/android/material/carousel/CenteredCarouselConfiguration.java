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

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * A Carousel configuration that creates a list with full sized (large) items in the center of the
 * scroll container that mask as they approach the edges of the container.
 *
 * <p>This configuration creates a Carousel that has large items centered and an extra small item at
 * the start and end of the layout container. Small items and medium items are then used to "fill
 * in" the remaining space to create a symmetrical layout.
 */
public final class CenteredCarouselConfiguration extends MultiBrowseCarouselConfiguration {

  private static final int EXTRA_SMALL_COUNT = 1;

  public CenteredCarouselConfiguration(@NonNull Carousel carousel) {
    super(carousel);
  }

  /**
   * Fills the scroll container with large, medium, small, and extra small items and create a {@link
   * KeylineState} for this arrangement.
   *
   * <p>This method uses four item sizes:
   *
   * <ul>
   *   <li>1) large - the fully unmasked, measured size of the child,
   *   <li>2) small - a 56dp item with fully rounded corners,
   *   <li>3) extra small - a barely visible sliver moving in or out of the viewable area, and
   *   <li>4) medium - a size between the large and small size used to help fit a whole number of
   *       items into a given container space.
   * </ul>
   *
   * <p>Given these four sizes, this method comes up with a symmetrical number of each that will fit
   * within the scroll container. In its maximum (most exercised) form, this arrangement will look
   * like [xs-s-m-l-m-s-xs] where `xs` is extra small, `s` is small, `m` is medium, and `l` is large
   * from the above list. In its minimum (least exercised) form, this arrangement will look like
   * [xs-l-xs].
   *
   * <p>Finding this arrangement for any given scroll container size is done by trying to fit as
   * many large items into the available space as possible, where available space is equal to the
   * scroll container minus the width of the two required extra small items at each end of the
   * container. Then large items are removed until a symmetrical combination of small and medium
   * items fit. Once these counts have been made, each items position along the scroll axis is
   * calculated and used in the construction a {@link KeylineState}.
   *
   * @param child A measured view used as a representative of how all items in the carousel would
   *     like to be sized.
   */
  @Override
  @NonNull
  public KeylineState onFirstChildMeasuredWithMargins(@NonNull View child) {
    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childHorizontalMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;

    Context context = child.getContext();
    float smallChildSize = getSmallSize(context) + childHorizontalMargins;
    float extraSmallChildSize = getExtraSmallSize(context) + childHorizontalMargins;

    float availableSpace = getTotalAvailableSpace();
    availableSpace -= (extraSmallChildSize * (EXTRA_SMALL_COUNT * 2));

    // The large child uses the width specified in its xml layout. If that width is wider than
    // the width available, it is given a new width of the entire available space.
    float largeChildWidth = min(child.getMeasuredWidth() + childHorizontalMargins, availableSpace);

    // Fill available space with large, then small, then medium items.
    int largeCount =
        getLargeCountForAvailableSpace(
            availableSpace,
            largeChildWidth,
            /* small size= */ smallChildSize * 2F,
            /* medium size= */ smallChildSize * 2F);
    availableSpace -= (largeCount * largeChildWidth);
    int smallPairCount = getSmallCountForAvailableSpace(availableSpace, smallChildSize * 2F);
    availableSpace -= ((smallPairCount * 2) * smallChildSize);
    int mediumPairCount = shouldShowMediumItem(availableSpace) ? 1 : 0;
    float mediumChildWidth = availableSpace / 2;

    // Calculate where to place the keylines at the head of the list given the scroll container size
    // and each item type's size and count.
    float extraSmallHeadCenterXKeyline =
        getCarousel().getContainerPaddingStart() + (extraSmallChildSize / 2F);
    float left = extraSmallHeadCenterXKeyline + extraSmallChildSize / 2F;

    float smallHeadStartCenterXKeyline =
        smallPairCount > 0 ? left + (smallChildSize / 2F) : extraSmallHeadCenterXKeyline;
    float smallHeadEndCenterXKeyline =
        smallHeadStartCenterXKeyline + (max(0, smallPairCount - 1) * smallChildSize);
    left = smallPairCount > 0 ? smallHeadEndCenterXKeyline + (smallChildSize / 2F) : left;

    float mediumHeadCenterXKeyline =
        mediumPairCount > 0 ? left + (mediumChildWidth / 2F) : smallHeadEndCenterXKeyline;
    left = mediumPairCount > 0 ? mediumHeadCenterXKeyline + (mediumChildWidth / 2F) : left;

    float largeStartCenterXKeyline = left + (largeChildWidth / 2F);

    // Since this configuration is symmetrical, we can invert the keylines at the head of the list
    // instead of manually calculating the tail keylines.
    float right = getCarousel().getContainerWidth() - getCarousel().getContainerPaddingEnd();
    float mediumTailCenterXKeyline = right - mediumHeadCenterXKeyline;
    float smallTailEndCenterXKeyline = right - smallHeadEndCenterXKeyline;
    float extraSmallTailCenterXKeyline = right - extraSmallHeadCenterXKeyline;

    // SET MASK PERCENTAGES
    float extraSmallMask =
        getChildMaskPercentage(extraSmallChildSize, largeChildWidth, childHorizontalMargins);
    float smallMask =
        getChildMaskPercentage(smallChildSize, largeChildWidth, childHorizontalMargins);
    float mediumMask =
        getChildMaskPercentage(mediumChildWidth, largeChildWidth, childHorizontalMargins);
    float largeMask = 0F;

    return new KeylineState.Builder(largeChildWidth)
        .addKeyline(extraSmallHeadCenterXKeyline, extraSmallMask, extraSmallChildSize)
        .addKeylineRange(smallHeadStartCenterXKeyline, smallMask, smallChildSize, smallPairCount)
        .addKeyline(mediumHeadCenterXKeyline, mediumMask, mediumChildWidth)
        .addKeylineRange(largeStartCenterXKeyline, largeMask, largeChildWidth, largeCount, true)
        .addKeyline(mediumTailCenterXKeyline, mediumMask, mediumChildWidth)
        .addKeylineRange(smallTailEndCenterXKeyline, smallMask, smallChildSize, smallPairCount)
        .addKeyline(extraSmallTailCenterXKeyline, extraSmallMask, extraSmallChildSize)
        .build();
  }
}
