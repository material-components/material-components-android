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
import static java.lang.Math.ceil;
import static java.lang.Math.floor;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
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

  // Specifies a percentage of a medium item's size by which it can be increased or decreased to
  // help fit an arrangement into the carousel's available space.
  private static final float MEDIUM_ITEM_FLEX_PERCENTAGE = .1F;

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

  private float getExtraSmallSize(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_gone_size);
  }

  private float getSmallSizeMin(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_small_item_size_min);
  }

  private float getSmallSizeMax(@NonNull Context context) {
    return context.getResources().getDimension(R.dimen.m3_carousel_small_item_size_max);
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

    Arrangement arrangement =
        findLowestCostArrangement(
            availableSpace,
            targetSmallChildWidth,
            smallChildWidthMin,
            smallChildWidthMax,
            smallCounts,
            targetMediumChildWidth,
            mediumCounts,
            targetLargeChildWidth,
            largeCounts);

    float extraSmallChildWidth = getExtraSmallSize(child.getContext()) + childHorizontalMargins;

    float start = 0F;
    float extraSmallHeadCenterX = start - (extraSmallChildWidth / 2F);

    float largeStartCenterX = start + (arrangement.largeSize / 2F);
    float largeEndCenterX =
        largeStartCenterX + (max(0, arrangement.largeCount - 1) * arrangement.largeSize);
    start = largeEndCenterX + arrangement.largeSize / 2F;

    float mediumCenterX =
        arrangement.mediumCount > 0 ? start + (arrangement.mediumSize / 2F) : largeEndCenterX;
    start = arrangement.mediumCount > 0 ? mediumCenterX + (arrangement.mediumSize / 2F) : start;

    float smallStartCenterX =
        arrangement.smallCount > 0 ? start + (arrangement.smallSize / 2F) : mediumCenterX;

    float extraSmallTailCenterX = carousel.getContainerWidth() + (extraSmallChildWidth / 2F);

    float extraSmallMask =
        getChildMaskPercentage(extraSmallChildWidth, arrangement.largeSize, childHorizontalMargins);
    float smallMask =
        getChildMaskPercentage(
            arrangement.smallSize, arrangement.largeSize, childHorizontalMargins);
    float mediumMask =
        getChildMaskPercentage(
            arrangement.mediumSize, arrangement.largeSize, childHorizontalMargins);
    float largeMask = 0F;

    KeylineState.Builder builder =
        new KeylineState.Builder(arrangement.largeSize)
            .addKeyline(extraSmallHeadCenterX, extraSmallMask, extraSmallChildWidth)
            .addKeylineRange(
                largeStartCenterX, largeMask, arrangement.largeSize, arrangement.largeCount, true);
    if (arrangement.mediumCount > 0) {
      builder.addKeyline(mediumCenterX, mediumMask, arrangement.mediumSize);
    }
    if (arrangement.smallCount > 0) {
      builder.addKeylineRange(
          smallStartCenterX, smallMask, arrangement.smallSize, arrangement.smallCount);
    }
    builder.addKeyline(extraSmallTailCenterX, extraSmallMask, extraSmallChildWidth);
    return builder.build();
  }

  /**
   * Create an arrangement for all possible permutations for {@code smallCounts}, {@code
   * mediumCounts}, and {@code largeCounts}, fit each into the available space, and return the
   * arrangement with the lowest cost.
   *
   * <p>Keep in mind that the returned arrangements do not take into account the available space
   * from the carousel. They will all occupy varying degrees of more or less space. The caller needs
   * to handle sorting the returned list, picking the most desirable arrangement, and fitting the
   * arrangement to the size of the carousel.
   *
   * @param availableSpace the space the arrangmenet needs to fit
   * @param targetSmallSize the size small items would like to be
   * @param minSmallSize the minimum size small items are allowed to be
   * @param maxSmallSize the maximum size small items are allowed to be
   * @param smallCounts an array of small item counts for a valid arrangement
   * @param targetMediumSize the size medium items would like to be
   * @param mediumCounts an array of medium item counts for a valid arrangement
   * @param targetLargeSize the size large items would like to be
   * @param largeCounts an array of large item counts for a valid arrangement
   * @return the arrangement that is considered the most desirable and has been adjusted to fit
   *     within the available space
   */
  private static Arrangement findLowestCostArrangement(
      float availableSpace,
      float targetSmallSize,
      float minSmallSize,
      float maxSmallSize,
      int[] smallCounts,
      float targetMediumSize,
      int[] mediumCounts,
      float targetLargeSize,
      int[] largeCounts) {
    Arrangement lowestCostArrangement = null;
    int priority = 1;
    for (int largeCount : largeCounts) {
      for (int mediumCount : mediumCounts) {
        for (int smallCount : smallCounts) {
          Arrangement arrangement =
              new Arrangement(
                  priority,
                  targetSmallSize,
                  minSmallSize,
                  maxSmallSize,
                  smallCount,
                  targetMediumSize,
                  mediumCount,
                  targetLargeSize,
                  largeCount,
                  availableSpace);
          if (lowestCostArrangement == null || arrangement.cost < lowestCostArrangement.cost) {
            lowestCostArrangement = arrangement;
            if (lowestCostArrangement.cost == 0F) {
              // If the new lowestCostArrangement has a cost of 0, we know it didn't have to alter
              // the large item size at all. We also know that arrangement permutations will be
              // generated in order of priority. We can exit early knowing there will not be an
              // arrangement with a better cost or priority.
              return lowestCostArrangement;
            }
          }
          priority++;
        }
      }
    }
    return lowestCostArrangement;
  }

  private static int maxValue(int[] array) {
    int largest = Integer.MIN_VALUE;
    for (int j : array) {
      if (j > largest) {
        largest = j;
      }
    }

    return largest;
  }

  /**
   * An object that holds data about a combination of large, medium, and small items, knows how to
   * alter an arrangement to fit within an available space, and can assess the arrangement's
   * desirability.
   */
  @VisibleForTesting
  static final class Arrangement {
    final int priority;
    float smallSize;
    final int smallCount;
    final int mediumCount;
    float mediumSize;
    float largeSize;
    final int largeCount;
    final float cost;

    /**
     * Creates a new arrangement by taking in a number of small, medium, and large items and the
     * size each would like to be and then fitting the sizes to work within the {@code
     * availableSpace}.
     *
     * <p>Note: The values for each item size after construction will likely differ from the target
     * values passed to the constructor since the constructor handles altering the sizes until the
     * total count is able to fit within the space see {@link #fit(float, float, float, float)} for
     * more details.
     *
     * @param priority the order in which this arrangement should be preferred against other
     *     arrangements that fit
     * @param targetSmallSize the size of a small item in this arrangement
     * @param minSmallSize the minimum size a small item is allowed to be
     * @param maxSmallSize the maximum size a small item is allowed to be
     * @param smallCount the number of small items in this arrangement
     * @param targetMediumSize the size of medium items in this arrangement
     * @param mediumCount the number of medium items in this arrangement
     * @param targetLargeSize the size of large items in this arrangement
     * @param largeCount the number of large items in this arrangement
     * @param availableSpace the space this arrangement needs to fit within
     */
    Arrangement(
        int priority,
        float targetSmallSize,
        float minSmallSize,
        float maxSmallSize,
        int smallCount,
        float targetMediumSize,
        int mediumCount,
        float targetLargeSize,
        int largeCount,
        float availableSpace) {
      this.priority = priority;
      this.smallSize = MathUtils.clamp(targetSmallSize, minSmallSize, maxSmallSize);
      this.smallCount = smallCount;
      this.mediumSize = targetMediumSize;
      this.mediumCount = mediumCount;
      this.largeSize = targetLargeSize;
      this.largeCount = largeCount;

      fit(availableSpace, minSmallSize, maxSmallSize, targetLargeSize);
      this.cost = cost(targetLargeSize);
    }

    @NonNull
    @Override
    public String toString() {
      return "Arrangement [priority="
          + priority
          + ", smallCount="
          + smallCount
          + ", smallSize="
          + smallSize
          + ", mediumCount="
          + mediumCount
          + ", mediumSize="
          + mediumSize
          + ", largeCount="
          + largeCount
          + ", largeSize="
          + largeSize
          + ", cost="
          + cost
          + "]";
    }

    /** Gets the total space taken by this arrangement. */
    private float getSpace() {
      return (largeSize * largeCount) + (mediumSize * mediumCount) + (smallSize * smallCount);
    }

    /**
     * Alters the item sizes of this arrangement until the space occupied fits within the {@code
     * availableSpace}.
     *
     * <p>This method tries to adjust the size of large items as little as possible by first
     * adjusting small items as much as possible, then adjusting medium items as much as possible,
     * and finally adjusting large items if the arrangement is still unable to fit.
     *
     * @param availableSpace the size of the carousel this arrangement needs to fit
     * @param minSmallSize the minimum size small items can be
     * @param maxSmallSize the maximum size medium items can be
     */
    private void fit(
        float availableSpace, float minSmallSize, float maxSmallSize, float targetLargeSize) {
      float delta = availableSpace - getSpace();
      // First, resize small items within their allowable min-max range to try to fit the
      // arrangement into the available space.
      if (smallCount > 0 && delta > 0) {
        // grow the small items
        smallSize += min(delta / smallCount, maxSmallSize - smallSize);
      } else if (smallCount > 0 && delta < 0) {
        // shrink the small items
        smallSize += max(delta / smallCount, minSmallSize - smallSize);
      }

      largeSize =
          calculateLargeSize(availableSpace, smallCount, smallSize, mediumCount, largeCount);
      mediumSize = (largeSize + smallSize) / 2F;

      // If the large size has been adjusted away from its target size to fit the arrangement,
      // counter this as much as possible by altering the medium item within its acceptable flex
      // range.
      if (mediumCount > 0 && largeSize != targetLargeSize) {
        float targetAdjustment = (targetLargeSize - largeSize) * largeCount;
        float availableMediumFlex = (mediumSize * MEDIUM_ITEM_FLEX_PERCENTAGE) * mediumCount;
        float distribute = min(abs(targetAdjustment), availableMediumFlex);
        if (targetAdjustment > 0F) {
          // Reduce the size of the medium item and give it back to the large items
          mediumSize -= (distribute / mediumCount);
          largeSize += (distribute / largeCount);
        } else {
          // Increase the size of the medium item and take from the large items
          mediumSize += (distribute / mediumCount);
          largeSize -= (distribute / largeCount);
        }
      }
    }

    /**
     * Calculates the large size that is able to fit within the available space given item counts,
     * the small size, and that the medium size is {@code (largeSize + smallSize) / 2}.
     *
     * <p>This method solves the following equation for largeSize:
     *
     * <p>{@code availableSpace = (largeSize * largeCount) + (((largeSize + smallSize) / 2) *
     * mediumCount) + (smallSize * smallCount)}
     *
     * @param availableSpace the total available space
     * @param smallCount the number of small items in the arrangement
     * @param smallSize the size of small items in the arrangement
     * @param mediumCount the number of medium items in the arrangement
     * @param largeCount the number of large items in the arrangement
     * @return the large item size which will fit for the available space and other item constraints
     */
    private float calculateLargeSize(
        float availableSpace, int smallCount, float smallSize, int mediumCount, int largeCount) {
      // Zero out small size if there are no small items
      smallSize = smallCount > 0 ? smallSize : 0F;
      return (availableSpace - (((float) smallCount) + ((float) mediumCount) / 2F) * smallSize)
          / (((float) largeCount) + ((float) mediumCount) / 2F);
    }

    private boolean isValid() {
      if (largeCount > 0 && smallCount > 0 && mediumCount > 0) {
        return largeSize > mediumSize && mediumSize > smallSize;
      } else if (largeCount > 0 && smallCount > 0) {
        return largeSize > smallSize;
      }

      return true;
    }

    /**
     * Calculates the cost of this arrangement to determine visual desirability and adherence to
     * inputs.
     *
     * @param targetLargeSize the size large items would like to be
     * @return a float representing the cost of this arrangement where the lower the cost the better
     */
    private float cost(float targetLargeSize) {
      if (!isValid()) {
        return Float.MAX_VALUE;
      }
      // Arrangements have a lower cost if they have a priority closer to 1 and their largeSize is
      // altered as little as possible.
      return abs(targetLargeSize - largeSize) * priority;
    }
  }
}
