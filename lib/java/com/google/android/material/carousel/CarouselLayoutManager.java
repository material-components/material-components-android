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

import static com.google.android.material.animation.AnimationUtils.lerp;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import com.google.android.material.carousel.KeylineState.Keyline;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A {@link LayoutManager} that can mask and offset items along the scrolling axis, creating a
 * unique list optimized for a stylized viewing experience.
 *
 * <p>{@link CarouselLayoutManager} requires all children to use {@link MaskableFrameLayout} as
 * their root ViewGroup.
 *
 * <p>Note that when Carousel measures and lays out items, the first item in the adapter will be
 * measured and it's desired size will be used to determine an appropriate size for all items in the
 * carousel.
 */
public class CarouselLayoutManager extends LayoutManager implements Carousel {

  private static final String TAG = "CarouselLayoutManager";

  private int horizontalScrollOffset;

  // Min scroll is the offset number that offsets the list to the right/bottom as much as possible.
  // In LTR layouts, this will be the scroll offset to move to the start of the container. In RTL,
  // this will move the list to the end of the container.
  private int minHorizontalScroll;
  // Max scroll is the offset number that moves the list to the left/top of the list as much as
  // possible. In LTR layouts, this will move the list to the end of the container. In RTL, this
  // will move the list to the start of the container.
  private int maxHorizontalScroll;

  private boolean isDebuggingEnabled = false;
  private final DebugItemDecoration debugItemDecoration = new DebugItemDecoration();
  @NonNull private CarouselStrategy carouselStrategy;
  @Nullable private KeylineStateList keylineStateList;
  // A KeylineState shifted for any current scroll offset.
  @Nullable private KeylineState currentKeylineState;

  // Tracks the last position to be at child index 0 after the most recent call to #fill. This helps
  // optimize fill loops by starting the fill from an adapter position that will need the least
  // number of loop iterations to fill the RecyclerView.
  private int currentFillStartPosition = 0;

  /**
   * An internal object used to store and run checks on a child to be potentially added to the
   * RecyclerView and laid out.
   */
  private static final class ChildCalculations {
    View child;
    float locOffset;
    KeylineRange range;

    /**
     * Creates new calculations object.
     *
     * @param child The child being calculated for
     * @param locOffset the offset location along the scrolling axis where this child will be laid
     *     out
     * @param range the keyline range that surrounds {@code locOffset}
     */
    ChildCalculations(View child, float locOffset, KeylineRange range) {
      this.child = child;
      this.locOffset = locOffset;
      this.range = range;
    }
  }

  public CarouselLayoutManager() {
    setCarouselStrategy(new MultiBrowseCarouselStrategy());
  }

  @Override
  public LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(
        ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
  }

  /**
   * Sets the {@link CarouselStrategy} used by this layout manager to mask and offset child views as
   * they move along the scrolling axis.
   */
  public void setCarouselStrategy(@NonNull CarouselStrategy carouselStrategy) {
    this.carouselStrategy = carouselStrategy;
    this.keylineStateList = null;
    requestLayout();
  }

  @Override
  public void onLayoutChildren(Recycler recycler, State state) {
    if (state.getItemCount() <= 0) {
      removeAndRecycleAllViews(recycler);
      currentFillStartPosition = 0;
      return;
    }

    boolean isRtl = isLayoutRtl();

    // If a keyline state hasn't been created, use the first child as a representative of how each
    // child would like to be measured and allow the strategy to create a keyline state.
    boolean isInitialLoad = keylineStateList == null;
    if (isInitialLoad) {
      View firstChild = recycler.getViewForPosition(0);
      measureChildWithMargins(firstChild, 0, 0);
      KeylineState keylineState =
          carouselStrategy.onFirstChildMeasuredWithMargins(this, firstChild);
      keylineStateList =
          KeylineStateList.from(this, isRtl ? KeylineState.reverse(keylineState) : keylineState);
    }

    // Ensure our scroll limits are initialized and valid for the data set size.
    int startHorizontalScroll = calculateStartHorizontalScroll(keylineStateList);
    int endHorizontalScroll = calculateEndHorizontalScroll(state, keylineStateList);
    // Convert the layout-direction-aware offsets into min/max absolutes. These need to be in the
    // min/max format so they can be correctly passed to KeylineStateList and used to interpolate
    // between keyline states.
    minHorizontalScroll = isRtl ? endHorizontalScroll : startHorizontalScroll;
    maxHorizontalScroll = isRtl ? startHorizontalScroll : endHorizontalScroll;

    if (isInitialLoad) {
      // Scroll to the start of the list on first load.
      horizontalScrollOffset = startHorizontalScroll;
    } else {
      // Clamp the horizontal scroll offset by the new min and max by pinging the scroll by
      // calculator with a 0 delta.
      horizontalScrollOffset +=
          calculateShouldHorizontallyScrollBy(
              0, horizontalScrollOffset, minHorizontalScroll, maxHorizontalScroll);
    }

    // Ensure currentFillStartPosition is valid if the number of items in the adapter has changed.
    currentFillStartPosition = MathUtils.clamp(currentFillStartPosition, 0, state.getItemCount());

    updateCurrentKeylineStateForScrollOffset();

    detachAndScrapAttachedViews(recycler);
    fill(recycler, state);
  }

  /**
   * Adds and places children into the {@link RecyclerView}, handling child layout and recycling
   * according to this class' {@link CarouselStrategy}.
   *
   * <p>This method is responsible for making sure views are added when additional space is created
   * due to an initial layout or a scroll event. All offsetting due to scroll events is done by
   * {@link #scrollBy(int, Recycler, State)}.
   *
   * <p>This layout manager tracks item location using two "models". The first is an end-to-end
   * model that keeps track of items as if they were laid out one after the other and fully unmasked
   * (the same way they would be laid out in a traditional list). This model is primarily useful for
   * tracking scroll minimums, maximums, and offsets. The second model is an offset model which is
   * the location of an item after it's position has been interpolated from {@link Keyline#loc}
   * (it's end-to-end location) to {@link Keyline#locOffset}. This is the model in which children
   * are actually laid out and drawn.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param state state passed by the {@link RecyclerView} with useful information like item count
   *     and focal state
   */
  private void fill(Recycler recycler, State state) {

    removeAndRecycleOutOfBoundsViews(recycler);

    if (getChildCount() == 0) {
      // First layout or the data set has changed. Re-layout all views by filling from start to end.
      addViewsStart(recycler, currentFillStartPosition - 1);
      addViewsEnd(recycler, state, currentFillStartPosition);
    } else {
      // Fill the container where there is now empty space after scrolling.
      int firstPosition = getPosition(getChildAt(0));
      int lastPosition = getPosition(getChildAt(getChildCount() - 1));
      addViewsStart(recycler, firstPosition - 1);
      addViewsEnd(recycler, state, lastPosition + 1);
    }

    validateChildOrderIfDebugging();
  }

  @Override
  public void onLayoutCompleted(State state) {
    super.onLayoutCompleted(state);
    if (getChildCount() == 0) {
      currentFillStartPosition = 0;
    } else {
      currentFillStartPosition = getPosition(getChildAt(0));
    }

    validateChildOrderIfDebugging();
  }

  /**
   * Adds views to the RecyclerView, moving towards the start of the carousel container, until
   * potentially new items are no longer in bounds or the beginning of the adapter list is reached.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param startPosition the adapter position from which to start adding views
   */
  private void addViewsStart(Recycler recycler, int startPosition) {
    int start = calculateChildStartForFill(startPosition);
    for (int i = startPosition; i >= 0; i--) {
      ChildCalculations calculations = makeChildCalculations(recycler, start, i);
      if (isLocOffsetOutOfFillBoundsStart(calculations.locOffset, calculations.range)) {
        break;
      }
      start = addStart(start, (int) currentKeylineState.getItemSize());

      // If this child's start is beyond the end of the container, don't add the child but continue
      // to loop so we can eventually get to children that are within bounds.
      if (isLocOffsetOutOfFillBoundsEnd(calculations.locOffset, calculations.range)) {
        continue;
      }
      // Add this child to the first index of the RecyclerView.
      addAndLayoutView(calculations.child, /* index= */ 0, calculations.locOffset);
    }
  }

  /**
   * Adds views to the RecyclerView, moving towards the end of the carousel container, until
   * potentially new items are no longer in bounds or the end of the adapter list is reached.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param state state passed by the {@link RecyclerView} used here to determine item count
   * @param startPosition the adapter position from which to start adding views
   */
  private void addViewsEnd(Recycler recycler, State state, int startPosition) {
    int start = calculateChildStartForFill(startPosition);
    for (int i = startPosition; i < state.getItemCount(); i++) {
      ChildCalculations calculations = makeChildCalculations(recycler, start, i);
      if (isLocOffsetOutOfFillBoundsEnd(calculations.locOffset, calculations.range)) {
        break;
      }
      start = addEnd(start, (int) currentKeylineState.getItemSize());

      // If this child's end is beyond the start of the container, don't add the child but continue
      // to loop so we can eventually get to children that are within bounds.
      if (isLocOffsetOutOfFillBoundsStart(calculations.locOffset, calculations.range)) {
        continue;
      }
      // Add this child to the last index of the RecyclerView
      addAndLayoutView(calculations.child, /* index= */ -1, calculations.locOffset);
    }
  }

  /** Used for debugging. Logs the internal representation of children to default logger. */
  private void logChildrenIfDebugging() {
    if (!isDebuggingEnabled) {
      return;
    }

    if (Log.isLoggable(TAG, Log.DEBUG)) {
      Log.d(TAG, "internal representation of views on the screen");
      for (int i = 0; i < getChildCount(); i++) {
        View child = getChildAt(i);
        float centerX = getDecoratedCenterXWithMargins(child);
        Log.d(
            TAG,
            "item position " + getPosition(child) + ", center:" + centerX + ", child index:" + i);
      }
      Log.d(TAG, "==============");
    }
  }

  /**
   * Used for debugging. Validates that child views are laid out in correct order. This is important
   * because rest of the algorithm relies on this constraint.
   *
   * <p>Child 0 should be closest to adapter position 0 and last child should be closest to the last
   * adapter position.
   */
  private void validateChildOrderIfDebugging() {
    if (!isDebuggingEnabled || getChildCount() < 1) {
      return;
    }

    for (int i = 0; i < getChildCount() - 1; i++) {
      int currPos = getPosition(getChildAt(i));
      int nextPos = getPosition(getChildAt(i + 1));
      if (currPos > nextPos) {
        logChildrenIfDebugging();
        throw new IllegalStateException(
            "Detected invalid child order. Child at index ["
                + i
                + "] had adapter position ["
                + currPos
                + "] and child at index ["
                + (i + 1)
                + "] had adapter position ["
                + nextPos
                + "].");
      }
    }
  }

  /**
   * Calculates position and mask for a view at at adapter {@code position} and returns an object
   * with the calculated values.
   *
   * <p>The returned object is used to run any checks/validations around whether or not this child
   * should be added to the RecyclerView given its calculated location.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param start the start location of this items view in the end-to-end layout model
   * @param position the adapter position of the item to add
   * @return a {@link ChildCalculations} object
   */
  private ChildCalculations makeChildCalculations(Recycler recycler, float start, int position) {
    float halfItemSize = currentKeylineState.getItemSize() / 2F;
    View child = recycler.getViewForPosition(position);
    measureChildWithMargins(child, 0, 0);

    int centerX = addEnd((int) start, (int) halfItemSize);
    KeylineRange range =
        getSurroundingKeylineRange(currentKeylineState.getKeylines(), centerX, false);

    float offsetCx = calculateChildOffsetCenterForLocation(child, centerX, range);
    updateChildMaskForLocation(child, centerX, range);

    return new ChildCalculations(child, offsetCx, range);
  }

  /**
   * Adds a child to the RecyclerView and lays it out with its center at {@code offsetCx} on the
   * scrolling axis.
   *
   * @param child the child view to add and lay out
   * @param index the index at which to add the child to the RecyclerView. Use 0 for adding to the
   *     start of the list and -1 for adding to the end.
   * @param offsetCx where the center of the masked child should be placed along the scrolling axis
   */
  private void addAndLayoutView(View child, int index, float offsetCx) {
    float halfItemSize = currentKeylineState.getItemSize() / 2F;
    addView(child, index);
    layoutDecoratedWithMargins(
        child,
        /* left= */ (int) (offsetCx - halfItemSize),
        /* top= */ getParentTop(),
        /* right= */ (int) (offsetCx + halfItemSize),
        /* bottom= */ getParentBottom());
  }

  /**
   * Returns true if a view rendered at {@code locOffset} will be completely out of bounds (its end
   * comes before the start of the container) when masked according to the {@code KeylineRange}.
   *
   * <p>Use this method to determine whether or not a child whose center is at {@code locOffset}
   * should be added to the RecyclerView.
   *
   * @param locOffset the center of the view to be checked along the scroll axis
   * @param range the keyline range surrounding {@code locOffset}
   * @return true if the end of a masked view, whose center is at {@code locOffset}, will come
   *     before the start of the container.
   */
  private boolean isLocOffsetOutOfFillBoundsStart(float locOffset, KeylineRange range) {
    float maskedSize = getMaskedItemSizeForLocOffset(locOffset, range);
    int maskedEnd = addEnd((int) locOffset, (int) (maskedSize / 2F));
    return isLayoutRtl() ? maskedEnd > getContainerWidth() : maskedEnd < 0;
  }

  /**
   * Returns true if a view rendered at {@code locOffset} will be completely out of bounds (its
   * start comes after the end of the container) when masked according to the {@code KeylineRange}.
   *
   * <p>Use this method to determine whether or not a child whose center is at {@code locOffset}
   * should be added to the RecyclerView.
   *
   * @param locOffset the center of the view to be checked along the scroll axis
   * @param range the keyline range surrounding {@code locOffset}
   * @return true if the start of a masked view, whose center is at {@code locOffset}, will come
   *     after the start of the container.
   */
  private boolean isLocOffsetOutOfFillBoundsEnd(float locOffset, KeylineRange range) {
    float maskedSize = getMaskedItemSizeForLocOffset(locOffset, range);
    int maskedStart = addStart((int) locOffset, (int) (maskedSize / 2F));
    return isLayoutRtl() ? maskedStart < 0 : maskedStart > getContainerWidth();
  }

  /**
   * Returns the masked, decorated bounds with margins for {@code view}.
   *
   * <p>Note that this differs from the super method which returns the fully unmasked bounds of
   * {@code view}.
   *
   * <p>Getting the masked, decorated bounds is useful for item decorations and other associated
   * classes which need the actual visual bounds of an item in the RecyclerView. If the full,
   * unmasked bounds is needed, see {@link RecyclerView#getDecoratedBoundsWithMargins(View, Rect)}.
   *
   * @param view the view element to check
   * @param outBounds a rect that will receive the bounds of the element including its maks,
   *     decoration, and margins.
   */
  @Override
  public void getDecoratedBoundsWithMargins(@NonNull View view, @NonNull Rect outBounds) {
    super.getDecoratedBoundsWithMargins(view, outBounds);
    float centerX = outBounds.centerX();
    float maskedSize =
        getMaskedItemSizeForLocOffset(
            centerX, getSurroundingKeylineRange(currentKeylineState.getKeylines(), centerX, true));
    float delta = (outBounds.width() - maskedSize) / 2F;
    outBounds.set(
        (int) (outBounds.left + delta),
        outBounds.top,
        (int) (outBounds.right - delta),
        outBounds.bottom);
  }

  private float getDecoratedCenterXWithMargins(View child) {
    Rect bounds = new Rect();
    super.getDecoratedBoundsWithMargins(child, bounds);
    return bounds.centerX();
  }

  /**
   * Remove and recycle any views outside of the bounds of this carousel.
   *
   * <p>This method uses two loops, one starting from the head of the list and one from the tail.
   * This tries to check as few items as necessary before finding the first head or tail child that
   * is in bounds.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   */
  private void removeAndRecycleOutOfBoundsViews(Recycler recycler) {
    // Remove items that are out of bounds at the head of the list
    while (getChildCount() > 0) {
      View child = getChildAt(0);
      float centerX = getDecoratedCenterXWithMargins(child);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), centerX, true);
      if (isLocOffsetOutOfFillBoundsStart(centerX, range)) {
        removeAndRecycleView(child, recycler);
      } else {
        break;
      }
    }

    // Remove items that are out of bounds at the tail of the list
    while (getChildCount() - 1 >= 0) {
      View child = getChildAt(getChildCount() - 1);
      float centerX = getDecoratedCenterXWithMargins(child);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), centerX, true);
      if (isLocOffsetOutOfFillBoundsEnd(centerX, range)) {
        removeAndRecycleView(child, recycler);
      } else {
        break;
      }
    }
  }

  /**
   * Finds the keylines located immediately before and after {@code location}, forming a keyline
   * range that {@code location} is currently within.
   *
   * <p>When looking before {@code location}, the nearest keyline with the lowest index is found.
   * When looking after {@code location}, the nearest keyline with the highest index is found. This
   * avoids conflicts if two keylines share the same location and allows keylines to be pinned
   * together.
   *
   * <p>If no keyline is found for the left, the left-most keyline is returned. If no keyline to the
   * right is found, the right-most keyline is returned. This means the {@code location} is outside
   * the bounds of the outer-most keylines.
   *
   * @param location The location along the scrolling axis that should be contained by the returned
   *     keyline range. This can be either a location in the end-to-end model ({@link Keyline#loc}
   *     or in the offset model {@link Keyline#locOffset}.
   * @param isOffset true if {@code location} has been offset and should be compared against {@link
   *     Keyline#locOffset}, false if {@code location} should be compared against {@link
   *     Keyline#loc}.
   * @return A pair whose first item is the nearest {@link Keyline} before centerX and whose second
   *     item is the nearest {@link Keyline} after centerX.
   */
  private static KeylineRange getSurroundingKeylineRange(
      List<Keyline> keylines, float location, boolean isOffset) {
    int leftMinDistanceIndex = -1;
    float leftMinDistance = Float.MAX_VALUE;
    int leftMostIndex = -1;
    float leftMostX = Float.MAX_VALUE;

    int rightMinDistanceIndex = -1;
    float rightMinDistance = Float.MAX_VALUE;
    int rightMostIndex = -1;
    float rightMostX = -Float.MAX_VALUE;

    for (int i = 0; i < keylines.size(); i++) {
      Keyline keyline = keylines.get(i);
      float currentLoc = isOffset ? keyline.locOffset : keyline.loc;
      float delta = abs(currentLoc - location);

      // Find the keyline closest to the left of centerX with the lowest index.
      if (currentLoc <= location) {
        if (delta <= leftMinDistance) {
          leftMinDistance = delta;
          leftMinDistanceIndex = i;
        }
      }
      // The keyline is to the right of centerX
      // Find the keyline closest to the right of centerX with the greatest index.
      if (currentLoc > location && delta <= rightMinDistance) {
        rightMinDistance = delta;
        rightMinDistanceIndex = i;
      }
      // Find the left-most keyline
      if (currentLoc <= leftMostX) {
        leftMostIndex = i;
        leftMostX = currentLoc;
      }
      // Find the right-most keyline
      if (currentLoc > rightMostX) {
        rightMostIndex = i;
        rightMostX = currentLoc;
      }
    }

    // If a keyline to the left or right hasn't been found, centerX is outside the bounds of the
    // outer-most keylines. Use the outer-most keyline instead.
    if (leftMinDistanceIndex == -1) {
      leftMinDistanceIndex = leftMostIndex;
    }
    if (rightMinDistanceIndex == -1) {
      rightMinDistanceIndex = rightMostIndex;
    }

    return new KeylineRange(
        keylines.get(leftMinDistanceIndex), keylines.get(rightMinDistanceIndex));
  }

  /**
   * Update the current keyline state by shifting it in response to any change in scroll offset.
   *
   * <p>This method should be called any time a change in the scroll offset occurs.
   */
  private void updateCurrentKeylineStateForScrollOffset() {
    if (maxHorizontalScroll <= minHorizontalScroll) {
      // We don't have enough items in the list to scroll and we should use the keyline state
      // that aligns items to the start of the container.
      this.currentKeylineState =
          isLayoutRtl() ? keylineStateList.getRightState() : keylineStateList.getLeftState();
    } else {
      this.currentKeylineState =
          keylineStateList.getShiftedState(
              horizontalScrollOffset, minHorizontalScroll, maxHorizontalScroll);
    }
    debugItemDecoration.setKeylines(currentKeylineState.getKeylines());
  }

  /**
   * Calculates the horizontal distance children should be scrolled by for a given {@code dx}.
   *
   * @param dx the delta, resulting from a gesture or other event, that the list would like to be
   *     scrolled by
   * @param currentHorizontalScroll the current horizontal scroll offset that is always between the
   *     min and max horizontal scroll
   * @param minHorizontalScroll the minimum scroll offset allowed
   * @param maxHorizontalScroll the maximum scroll offset allowed
   * @return an int that represents the change that should be applied to the current scroll offset,
   *     given limitations by the min and max scroll offset values
   */
  private static int calculateShouldHorizontallyScrollBy(
      int dx, int currentHorizontalScroll, int minHorizontalScroll, int maxHorizontalScroll) {
    int targetHorizontalScroll = currentHorizontalScroll + dx;
    if (targetHorizontalScroll < minHorizontalScroll) {
      return minHorizontalScroll - currentHorizontalScroll;
    } else if (targetHorizontalScroll > maxHorizontalScroll) {
      return maxHorizontalScroll - currentHorizontalScroll;
    } else {
      return dx;
    }
  }

  /**
   * Calculates the total offset needed to scroll the first item in the list to the center of the
   * start focal keyline.
   */
  private int calculateStartHorizontalScroll(KeylineStateList stateList) {
    boolean isRtl = isLayoutRtl();
    KeylineState startState = isRtl ? stateList.getRightState() : stateList.getLeftState();
    Keyline startFocalKeyline =
        isRtl ? startState.getLastFocalKeyline() : startState.getFirstFocalKeyline();
    float firstItemDistanceFromStart = getPaddingStart() * (isRtl ? 1 : -1);
    int firstItemStart =
        addStart((int) startFocalKeyline.loc, (int) (startState.getItemSize() / 2F));
    return (int) (firstItemDistanceFromStart + getParentStart() - firstItemStart);
  }

  /**
   * Calculates the total offset needed to scroll the last item in the list to the center of the end
   * focal keyline.
   */
  private int calculateEndHorizontalScroll(State state, KeylineStateList stateList) {
    boolean isRtl = isLayoutRtl();
    KeylineState endState = isRtl ? stateList.getLeftState() : stateList.getRightState();
    Keyline endFocalKeyline =
        isRtl ? endState.getFirstFocalKeyline() : endState.getLastFocalKeyline();
    // Get the total distance from the first item to the last item in the end-to-end model
    float lastItemDistanceFromFirstItem =
        (((state.getItemCount() - 1) * endState.getItemSize()) + getPaddingEnd())
            * (isRtl ? -1F : 1F);
    // We want the last item in the list to only be able to scroll to the end of the list. Subtract
    // the distance to the end focal keyline and then add the distance needed to let the last
    // item hit the center of the end focal keyline.
    float endFocalLocDistanceFromStart = endFocalKeyline.loc - getParentStart();
    float endFocalLocDistanceFromEnd = getParentEnd() - endFocalKeyline.loc;
    if (abs(endFocalLocDistanceFromStart) > abs(lastItemDistanceFromFirstItem)) {
      // The last item comes before the last focal keyline which means all items should be within
      // the focal range and there is nowhere to scroll.
      return 0;
    }

    return (int)
        (lastItemDistanceFromFirstItem - endFocalLocDistanceFromStart + endFocalLocDistanceFromEnd);
  }

  /**
   * Calculates the start of the child view item at {@code startPosition} in the end-to-end layout
   * model.
   *
   * <p>This is used to calculate an initial point along the scroll axis from which to start looping
   * over adapter items and calculating where children should be placed.
   *
   * @param startPosition the adapter position of the item whose start position will be calculated
   * @return the start location of the view at {@code startPosition} along the scroll axis
   */
  private int calculateChildStartForFill(int startPosition) {
    float scrollOffset = getParentStart() - horizontalScrollOffset;
    float positionOffset = currentKeylineState.getItemSize() * startPosition;

    return addEnd((int) scrollOffset, (int) positionOffset);
  }

  /**
   * Remaps and returns the child's offset center from the end-to-end layout model.
   *
   * @param child the child to calculate the offset for
   * @param childCenterLocation the center of the child in the end-to-end layout model
   * @param range the keyline range that the child is currently between
   * @return the location along the scroll axis where the child should be located
   */
  private float calculateChildOffsetCenterForLocation(
      View child, float childCenterLocation, KeylineRange range) {
    float offsetCx =
        lerp(
            range.left.locOffset,
            range.right.locOffset,
            range.left.loc,
            range.right.loc,
            childCenterLocation);

    // If the current centerX is "out of bounds", meaning it is before the first keyline or after
    // the last keyline, this item should begin scrolling at a fixed rate according to the
    // last keyline it passed (either the first or last keyline).
    // Compare reference equality here since there might be multiple keylines with the same
    // values as the first/last keyline but we want to ensure this conditional is true only when
    // we're working with the same object instance.
    if (range.right == currentKeylineState.getFirstKeyline()
        || range.left == currentKeylineState.getLastKeyline()) {
      // Calculate how far past the nearest keyline (either the first or last keyline) this item
      // has scrolled in the end-to-end layout. Then use that value calculate what would be a
      // Keyline#locOffset.
      LayoutParams lp = (LayoutParams) child.getLayoutParams();
      float horizontalMarginMask =
          (lp.rightMargin + lp.leftMargin) / currentKeylineState.getItemSize();
      float outOfBoundOffset =
          (childCenterLocation - range.right.loc) * (1F - range.right.mask + horizontalMarginMask);
      offsetCx += outOfBoundOffset;
    }

    return offsetCx;
  }

  /**
   * Gets the masked size of a child when its center is at {@code locOffset} and is between the
   * given {@code range}.
   *
   * @param locOffset the offset location along the scrolling axis that should be within the keyline
   *     {@code range}
   * @param range the keyline range that surrounds {@code locOffset}
   * @return the masked size of a child when its center is at {@code locOffset} and is between the
   *     given {@code range}
   */
  private float getMaskedItemSizeForLocOffset(float locOffset, KeylineRange range) {
    return lerp(
        range.left.maskedItemSize,
        range.right.maskedItemSize,
        range.left.locOffset,
        range.right.locOffset,
        locOffset);
  }

  /**
   * Calculates and sets the child's mask according to its current location.
   *
   * @param child the child to mask
   * @param childCenterLocation the center of the child in the end-to-end layout model
   * @param range the keyline range that the child is currently between
   */
  private void updateChildMaskForLocation(
      View child, float childCenterLocation, KeylineRange range) {
    if (child instanceof Maskable) {
      // Interpolate the mask value based on the location of this view between it's two
      // surrounding keylines.
      float maskProgress =
          lerp(
              range.left.mask,
              range.right.mask,
              range.left.loc,
              range.right.loc,
              childCenterLocation);
      ((Maskable) child).setMaskXPercentage(maskProgress);
    }
  }

  @Override
  public void measureChildWithMargins(@NonNull View child, int widthUsed, int heightUsed) {
    if (!(child instanceof Maskable)) {
      throw new IllegalStateException(
          "All children of a RecyclerView using CarouselLayoutManager must use MaskableFrameLayout"
              + " as their root ViewGroup.");
    }

    LayoutParams lp = (LayoutParams) child.getLayoutParams();

    Rect insets = new Rect();
    calculateItemDecorationsForChild(child, insets);
    widthUsed += insets.left + insets.right;
    heightUsed += insets.top + insets.bottom;

    // If the strategy's keyline set is available, use the item size from the keyline set.
    // Otherwise, measure the item to what it would like to be so the strategy will be given an
    // opportunity to use this desired size in making it's sizing decision.
    final float childWidthDimension =
        keylineStateList != null ? keylineStateList.getDefaultState().getItemSize() : lp.width;
    final int widthSpec =
        getChildMeasureSpec(
            getWidth(),
            getWidthMode(),
            getPaddingLeft() + getPaddingRight() + lp.leftMargin + lp.rightMargin + widthUsed,
            (int) childWidthDimension,
            canScrollHorizontally());

    final int heightSpec =
        getChildMeasureSpec(
            getHeight(),
            getHeightMode(),
            getPaddingTop() + getPaddingBottom() + lp.topMargin + lp.bottomMargin + heightUsed,
            lp.height,
            canScrollVertically());
    child.measure(widthSpec, heightSpec);
  }

  private int getParentStart() {
    return isLayoutRtl() ? getWidth() : 0;
  }

  private int getParentEnd() {
    return isLayoutRtl() ? 0 : getWidth();
  }

  private int getParentTop() {
    return getPaddingTop();
  }

  private int getParentBottom() {
    return getHeight() - getPaddingBottom();
  }

  @Override
  public int getContainerWidth() {
    return getWidth();
  }

  private boolean isLayoutRtl() {
    return getLayoutDirection() == ViewCompat.LAYOUT_DIRECTION_RTL;
  }

  /** Moves {@code value} towards the start of the container by {@code amount}. */
  private int addStart(int value, int amount) {
    return isLayoutRtl() ? value + amount : value - amount;
  }

  /** Moves {@code value} towards the end of the container by {@code amount}. */
  private int addEnd(int value, int amount) {
    return isLayoutRtl() ? value - amount : value + amount;
  }

  @Override
  public void onInitializeAccessibilityEvent(@NonNull AccessibilityEvent event) {
    super.onInitializeAccessibilityEvent(event);
    if (getChildCount() > 0) {
      event.setFromIndex(getPosition(getChildAt(0)));
      event.setToIndex(getPosition(getChildAt(getChildCount() - 1)));
    }
  }

  /**
   * Gets the scroll offset for a position in the adapter.
   *
   * <p>This will calculate the horizontal scroll offset needed to place a child at {@code
   * position}'s center at the start-most focal keyline. The returned value might be less or greater
   * than the min and max scroll offsets but this will be clamped in {@link #scrollBy(int, Recycler,
   * State)} (Recycler, State)} by {@link #calculateShouldHorizontallyScrollBy(int, int, int, int)}.
   */
  private int getScrollOffsetForPosition(KeylineState keylineState, int position) {
    if (isLayoutRtl()) {
      return (int)
          ((getContainerWidth() - keylineState.getLastFocalKeyline().loc)
              - (position * keylineState.getItemSize())
              - (keylineState.getItemSize() / 2F));
    } else {
      return (int)
          ((position * keylineState.getItemSize())
              - keylineState.getFirstFocalKeyline().loc
              + (keylineState.getItemSize() / 2F));
    }
  }

  @Override
  public void scrollToPosition(int position) {
    if (keylineStateList == null) {
      return;
    }
    horizontalScrollOffset =
        getScrollOffsetForPosition(keylineStateList.getDefaultState(), position);
    currentFillStartPosition = MathUtils.clamp(position, 0, max(0, getItemCount() - 1));
    updateCurrentKeylineStateForScrollOffset();
    requestLayout();
  }

  @Override
  public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
    LinearSmoothScroller linearSmoothScroller =
        new LinearSmoothScroller(recyclerView.getContext()) {
          @Nullable
          @Override
          public PointF computeScrollVectorForPosition(int targetPosition) {
            if (keylineStateList == null) {
              return null;
            }

            float targetScrollOffset =
                getScrollOffsetForPosition(keylineStateList.getDefaultState(), targetPosition);
            return new PointF(targetScrollOffset - horizontalScrollOffset, 0F);
          }

          @Override
          public int calculateDxToMakeVisible(View view, int snapPreference) {
            // Override dx calculations so the target view is brought all the way into the focal
            // range instead of just being made visible.
            float targetScrollOffset =
                getScrollOffsetForPosition(keylineStateList.getDefaultState(), getPosition(view));
            return (int) (horizontalScrollOffset - targetScrollOffset);
          }
        };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }

  @Override
  public boolean canScrollHorizontally() {
    return true;
  }

  @Override
  public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
    return canScrollHorizontally() ? scrollBy(dx, recycler, state) : 0;
  }

  @Override
  public boolean requestChildRectangleOnScreen(
      @NonNull RecyclerView parent,
      @NonNull View child,
      @NonNull Rect rect,
      boolean immediate,
      boolean focusedChildVisible) {
    if (keylineStateList == null) {
      return false;
    }

    int offsetForChild =
        getScrollOffsetForPosition(keylineStateList.getDefaultState(), getPosition(child));
    int dx = offsetForChild - horizontalScrollOffset;
    if (!focusedChildVisible) {
      if (dx != 0) {
        // TODO(b/266816148): Implement smoothScrollBy when immediate is false.
        parent.scrollBy(dx, 0);
        return true;
      }
    }
    return false;
  }

  /**
   * Offset child items, respecting min and max scroll offsets, and fill additional space with new
   * items.
   *
   * @param distance the total scroll delta requested
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param state state passed by the {@link RecyclerView} with useful information like item count
   *     and focal state*
   * @return the actually delta scrolled by the list. This will differ from {@code distance} if the
   *     start or end of the list has been reached.
   */
  private int scrollBy(int distance, Recycler recycler, State state) {
    if (getChildCount() == 0 || distance == 0) {
      return 0;
    }

    // Calculate how much the carousel should scroll and update the horizontal scroll offset.
    int scrolledBy =
        calculateShouldHorizontallyScrollBy(
            distance, horizontalScrollOffset, minHorizontalScroll, maxHorizontalScroll);
    horizontalScrollOffset += scrolledBy;
    updateCurrentKeylineStateForScrollOffset();

    float halfItemSize = currentKeylineState.getItemSize() / 2F;
    int startPosition = getPosition(getChildAt(0));
    int start = calculateChildStartForFill(startPosition);
    Rect boundsRect = new Rect();
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      offsetChildLeftAndRight(child, start, halfItemSize, boundsRect);
      start = addEnd(start, (int) currentKeylineState.getItemSize());
    }

    // Fill any additional space caused by scrolling with more items.
    fill(recycler, state);

    return scrolledBy;
  }

  /**
   * Offsets a child horizontally from its current location to its location when its start is placed
   * at {@code startOffset} and updates the child's mask according to its new surrounding keylines.
   *
   * @param child the child to offset
   * @param startOffset where the start of the child should be placed, in the end-to-end model,
   *     after the child has been offset
   * @param halfItemSize half of the fully unmasked item size
   * @param boundsRect a Rect to use to find the current bounds of {@code child}
   */
  private void offsetChildLeftAndRight(
      View child, float startOffset, float halfItemSize, Rect boundsRect) {
    int centerX = addEnd((int) startOffset, (int) halfItemSize);
    KeylineRange range =
        getSurroundingKeylineRange(currentKeylineState.getKeylines(), centerX, false);

    float offsetCx = calculateChildOffsetCenterForLocation(child, centerX, range);
    updateChildMaskForLocation(child, centerX, range);

    // Offset the child so its center is at offsetCx
    super.getDecoratedBoundsWithMargins(child, boundsRect);
    float actualCx = boundsRect.left + halfItemSize;
    child.offsetLeftAndRight((int) (offsetCx - actualCx));
  }

  /**
   * Calculate the offset of the horizontal scrollbar thumb within the horizontal range. This is the
   * position of the thumb within the scrollbar track.
   *
   * <p>This is also used for accessibility when scrolling to give auditory feedback about the
   * current scroll position within the total range.
   *
   * <p>This method can return an arbitrary unit as long as the unit is shared across {@link
   * #computeHorizontalScrollExtent(State)} and {@link #computeHorizontalScrollRange(State)}.
   */
  @Override
  public int computeHorizontalScrollOffset(@NonNull State state) {
    return horizontalScrollOffset;
  }

  /**
   * Compute the extent of the horizontal scrollbar thumb. This is the size of the thumb inside the
   * scrollbar track.
   *
   * <p>This method can return an arbitrary unit as long as the unit is shared across {@link
   * #computeHorizontalScrollExtent(State)} and {@link #computeHorizontalScrollOffset(State)}.
   */
  @Override
  public int computeHorizontalScrollExtent(@NonNull State state) {
    return (int) keylineStateList.getDefaultState().getItemSize();
  }

  /**
   * Compute the horizontal range represented by the horizontal scroll bars. This is the total
   * length of the scrollbar track within the range.
   *
   * <p>This method can return an arbitrary unit as long as the unit is shared across {@link
   * #computeHorizontalScrollExtent(State)} and {@link #computeHorizontalScrollOffset(State)}.
   */
  @Override
  public int computeHorizontalScrollRange(@NonNull State state) {
    return maxHorizontalScroll - minHorizontalScroll;
  }

  /**
   * Enables features to help debug keylines and other internal layout manager logic.
   *
   * <p>This will draw lines on top of the RecyclerView that show where keylines are placed for the
   * current {@link CarouselStrategy}. Enabling debugging will also throw an exception when an
   * invalid child order is detected (child index and adapter position are incorrectly ordered). See
   * {@link #validateChildOrderIfDebugging()} ()} ()} for more details.
   *
   * @param recyclerView The {@link RecyclerView} this layout manager is attached to.
   * @param enabled Whether to draw debug lines and throw on state errors.
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void setDebuggingEnabled(@NonNull RecyclerView recyclerView, boolean enabled) {
    this.isDebuggingEnabled = enabled;
    recyclerView.removeItemDecoration(debugItemDecoration);
    if (enabled) {
      recyclerView.addItemDecoration(debugItemDecoration);
    }
    recyclerView.invalidateItemDecorations();
  }

  /** A class that represents a pair of keylines which create a range along the scrolling axis. */
  private static class KeylineRange {
    final Keyline left;
    final Keyline right;

    /**
     * Create a new keyline range.
     *
     * @param left The left keyline boundary of this range.
     * @param right The right keyline boundary of this range.
     */
    KeylineRange(Keyline left, Keyline right) {
      Preconditions.checkArgument(left.loc <= right.loc);
      this.left = left;
      this.right = right;
    }
  }

  /**
   * A {@link RecyclerView.ItemDecoration} that draws keylines and other information to help debug
   * strategies.
   */
  private static class DebugItemDecoration extends RecyclerView.ItemDecoration {

    private final Paint linePaint = new Paint();
    private List<Keyline> keylines = Collections.unmodifiableList(new ArrayList<>());

    DebugItemDecoration() {
      linePaint.setStrokeWidth(5F);
      linePaint.setColor(Color.MAGENTA);
    }

    /** Updates the keylines that should be drawn over the children in the RecyclerView. */
    void setKeylines(List<Keyline> keylines) {
      this.keylines = Collections.unmodifiableList(keylines);
    }

    @Override
    public void onDrawOver(@NonNull Canvas c, @NonNull RecyclerView parent, @NonNull State state) {
      super.onDrawOver(c, parent, state);
      linePaint.setStrokeWidth(
          parent.getResources().getDimension(R.dimen.m3_carousel_debug_keyline_width));
      for (Keyline keyline : keylines) {
        linePaint.setColor(ColorUtils.blendARGB(Color.MAGENTA, Color.BLUE, keyline.mask));
        c.drawLine(
            keyline.locOffset,
            ((CarouselLayoutManager) parent.getLayoutManager()).getParentTop(),
            keyline.locOffset,
            ((CarouselLayoutManager) parent.getLayoutManager()).getParentBottom(),
            linePaint);
      }
    }
  }
}
