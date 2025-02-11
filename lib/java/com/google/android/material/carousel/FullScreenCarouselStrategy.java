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
import static java.lang.Math.min;

import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;

/**
 * A {@link CarouselStrategy} that fits one full-width or full-height item into a container to
 * create a layout to browse one item at a time.
 *
 * <p>Note that the item masks will be based on the items encompassing the full width or full height
 * of the carousel. The carousel item dimensions should reflect this with {@code match_parent}
 * dimensions.
 *
 * <p>This class will automatically be reversed by {@link CarouselLayoutManager} if being laid out
 * right-to-left and does not need to make any account for layout direction itself.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md">component
 * developer guidance</a> and <a href="https://material.io/components/carousel/overview">design
 * guidelines</a>.
 */
public class FullScreenCarouselStrategy extends CarouselStrategy {

  @Override
  @NonNull
  public KeylineState onFirstChildMeasuredWithMargins(
      @NonNull Carousel carousel, @NonNull View child) {
    int availableSpace;
    LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
    float childMargins;
    if (carousel.isHorizontal()) {
      availableSpace = carousel.getContainerWidth();
      childMargins = childLayoutParams.leftMargin + childLayoutParams.rightMargin;
    } else {
      availableSpace = carousel.getContainerHeight();
      childMargins = childLayoutParams.topMargin + childLayoutParams.bottomMargin;
    }
    float targetChildSize = min(availableSpace + childMargins, availableSpace);
    Arrangement arrangement =
        new Arrangement(
            /* priority= */ 0,
            /* targetSmallSize= */ 0,
            /* minSmallSize= */ 0,
            /* maxSmallSize= */ 0,
            /* smallCount= */ 0,
            /* targetMediumSize= */ 0,
            /* mediumCount= */ 0,
            targetChildSize,
            /* largeCount= */ 1,
            availableSpace);
    return createLeftAlignedKeylineState(
        child.getContext(), childMargins, availableSpace, arrangement);
  }
}
