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

import static java.lang.Math.floor;
import static java.lang.Math.max;

import android.content.Context;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

/**
 * A class that knows how to size and fit large, medium, small, and extra small items into a
 * container to create a layout for quick browsing of multiple items at once.
 *
 * <p>Large items are items that are fully unmasked and in focus. Large items use the full size set
 * by the client for items in the Carousel. Medium items are items that can vary in size and are
 * used used to fill extra space in the container when a whole number of large and small items
 * cannot fit. Small items are masked slices that appear and disappear at the edges of the container
 * before unmasking into large items.
 */
public abstract class MultiBrowseCarouselConfiguration extends CarouselConfiguration {

  MultiBrowseCarouselConfiguration(@NonNull Carousel carousel) {
    super(carousel);
  }

  protected float getSmallSize(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_small_item_size);
  }

  protected float getExtraSmallSize(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_extra_small_item_size);
  }

  /**
   * Gets the total available space to be filled with items inside the carousel.
   *
   * <p>This is the width of the carousel minus any paddings.
   */
  protected float getTotalAvailableSpace() {
    return getCarousel().getContainerWidth()
        - (getCarousel().getContainerPaddingStart() + getCarousel().getContainerPaddingEnd());
  }

  /**
   * Fits the maximum number of large items into {@code availableSpace} that creates a pleasing
   * visual layout.
   *
   * <p>Valid combinations that fill {@code availableSpace} include a single large item or any
   * number of large items plus medium and small items. The arrangement is determined by the size of
   * {@code largeChildSize} and the way it divides into {@code availableSpace}.
   *
   * @param availableSpace The amount of space available to be occupied by large items. The total
   *     width taken up by large items must not be larger than this available space.
   * @param largeChildSize The size of a single large item.
   * @param smallChildSize The size needed for a small item
   * @param mediumChildSize The size needed for a medium item
   * @return The number of expanded items for this configuration.
   */
  protected static int getLargeCountForAvailableSpace(
      float availableSpace, float largeChildSize, float smallChildSize, float mediumChildSize) {
    if (largeChildSize == availableSpace) {
      // There is space for exactly 1 large item. Fill the entire available space.
      return 1;
    }
    // Determine the number of large items that can fit within the given space.
    float largeFill = availableSpace / largeChildSize;
    float largeRemainder = availableSpace % largeChildSize;
    if (largeRemainder == smallChildSize
        || largeRemainder >= (smallChildSize + mediumChildSize)
        || largeFill < 2) {
      // The remaining space after flooring the largeFill is enough to fit exactly one
      // small item OR one small item plus one medium item.
      return (int) floor(largeFill);
    } else {
      // Remove the overflowed large item plus an additional item to make room for small or
      // medium items.
      // TODO(b/239845088): Create strategy for resizing items for a pleasing layout.
      return (int) floor(largeFill - 1F);
    }
  }

  /**
   * Fits the maximum number of small items into {@code availableSpace} that creates a pleasing
   * visual layout.
   *
   * @param availableSpace The amount of space available to be occupied by small items.
   * @param smallChildSize The size of a single small item.
   * @return The number of small items for this configuration.
   */
  protected static int getSmallCountForAvailableSpace(float availableSpace, float smallChildSize) {
    if (availableSpace < smallChildSize) {
      return 0;
    }
    // Determine the number of small items that can fit within the remaining available space.
    float smallFill = availableSpace / smallChildSize;
    float smallRemainder = availableSpace % smallChildSize;
    if (smallRemainder == 0F) {
      // There is exactly enough space for a whole number of small items
      return (int) smallFill;
    } else {
      // There is a mix of small and other items. Remove the remainder and return the fill
      // count.
      return (int) max(0, floor(smallFill - 1F));
    }
  }

  /**
   * Gets whether a medium item should be used for this configuration.
   *
   * <p>Only one medium item should ever be used on either size of a focal range.
   *
   * @param availableSpace The amount of space available to be occupied by medium items.
   * @return true if a medium item should be used for this configuration
   */
  protected static boolean shouldShowMediumItem(float availableSpace) {
    return availableSpace > 0;
  }

  @FloatRange(from = 0F, to = 1F)
  protected static float getChildMaskPercentage(
      float maskedSize, float unmaskedSize, float childMargins) {
    return 1F - ((maskedSize - childMargins) / (unmaskedSize - childMargins));
  }
}
