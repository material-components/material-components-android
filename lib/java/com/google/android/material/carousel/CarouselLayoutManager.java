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

import static androidx.recyclerview.widget.RecyclerView.NO_POSITION;
import static com.google.android.material.animation.AnimationUtils.lerp;
import static java.lang.Math.abs;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import androidx.recyclerview.widget.RecyclerView.Recycler;
import androidx.recyclerview.widget.RecyclerView.State;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;
import androidx.core.math.MathUtils;
import androidx.core.util.Preconditions;
import com.google.android.material.carousel.CarouselStrategy.StrategyType;
import com.google.android.material.carousel.KeylineState.Keyline;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Carousel.md">component
 * developer guidance</a> and <a href="https://material.io/components/carousel/overview">design
 * guidelines</a>.
 */
public class CarouselLayoutManager extends LayoutManager
    implements Carousel, RecyclerView.SmoothScroller.ScrollVectorProvider {

  private static final String TAG = "CarouselLayoutManager";

  @VisibleForTesting int scrollOffset;

  // Min scroll is the offset number that offsets the list to the right/bottom as much as possible.
  // In LTR layouts, this will be the scroll offset to move to the start of the container. In RTL,
  // this will move the list to the end of the container.
  @VisibleForTesting int minScroll;
  // Max scroll is the offset number that moves the list to the left/top of the list as much as
  // possible. In LTR layouts, this will move the list to the end of the container. In RTL, this
  // will move the list to the start of the container.
  @VisibleForTesting int maxScroll;

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

  // Tracks the keyline state associated with each item in the RecyclerView.
  @Nullable private Map<Integer, KeylineState> keylineStatePositionMap;

  /** Horizontal orientation for Carousel. */
  public static final int HORIZONTAL = RecyclerView.HORIZONTAL;

  /** Vertical orientation for Carousel. */
  public static final int VERTICAL = RecyclerView.VERTICAL;

  private CarouselOrientationHelper orientationHelper;

  private final OnLayoutChangeListener recyclerViewSizeChangeListener =
      (v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
        // If RV width or height values have changed, refresh the keyline state.
        if ((right - left != oldRight - oldLeft) || (bottom - top != oldBottom - oldTop)) {
          v.post(this::refreshKeylineState);
        }
      };

  /** Aligns large items to the start of the carousel. */
  public static final int ALIGNMENT_START = 0;

  /** Aligns large items to the center of the carousel. */
  public static final int ALIGNMENT_CENTER = 1;

  private int lastItemCount;

  /**
   * An estimation of the current focused position, determined by which item center is closest to
   * the first focal keyline. This is used when restoring item position after the carousel keylines
   * are re-calculated due to configuration or size changes.
   */
  private int currentEstimatedPosition = NO_POSITION;

  /**
   * Determines where to align the large items in the carousel.
   *
   * @hide
   */
  @IntDef({ALIGNMENT_START, ALIGNMENT_CENTER})
  @Retention(RetentionPolicy.SOURCE)
  @interface Alignment {}

  @Alignment private int carouselAlignment = ALIGNMENT_START;

  /**
   * An internal object used to store and run checks on a child to be potentially added to the
   * RecyclerView and laid out.
   */
  private static final class ChildCalculations {
    final View child;
    final float center;
    final float offsetCenter;
    final KeylineRange range;

    /**
     * Creates new calculations object.
     *
     * @param child The child being calculated for
     * @param center the location of the center of the {@code child} along the scrolling axis in the
     *     end-to-end model
     * @param offsetCenter the offset location of the center of the {@code child} along the
     *     scrolling axis where this child will be laid out
     * @param range the keyline range that surrounds {@code locOffset}
     */
    ChildCalculations(View child, float center, float offsetCenter, KeylineRange range) {
      this.child = child;
      this.center = center;
      this.offsetCenter = offsetCenter;
      this.range = range;
    }
  }

  public CarouselLayoutManager() {
    this(new MultiBrowseCarouselStrategy());
  }

  public CarouselLayoutManager(@NonNull CarouselStrategy strategy) {
    this(strategy, HORIZONTAL);
  }

  public CarouselLayoutManager(
      @NonNull CarouselStrategy strategy, @RecyclerView.Orientation int orientation) {
    setCarouselStrategy(strategy);
    setOrientation(orientation);
  }

  @SuppressLint("UnknownNullness") // b/240775049: Cannot annotate properly
  public CarouselLayoutManager(
      Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    setCarouselStrategy(new MultiBrowseCarouselStrategy());

    setCarouselAttributes(context, attrs);
  }

  private void setCarouselAttributes(Context context, AttributeSet attrs) {
    if (attrs != null) {
      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Carousel);
      setCarouselAlignment(a.getInt(R.styleable.Carousel_carousel_alignment, ALIGNMENT_START));
      setOrientation(
          a.getInt(
              androidx.recyclerview.R.styleable.RecyclerView_android_orientation,
              HORIZONTAL));
      a.recycle();
    }
  }

  /**
   * Recalculates the internal state of the Carousel based on the size of the items. This should be
   * called whenever the size of the items is changed.
   */
  public void notifyItemSizeChanged() {
    refreshKeylineState();
  }

  /**
   * Sets the alignment of the focal items in the carousel.
   */
  public void setCarouselAlignment(@Alignment int alignment) {
    this.carouselAlignment = alignment;
    refreshKeylineState();
  }

  @Override
  public int getCarouselAlignment() {
    return carouselAlignment;
  }

  private int getLeftOrTopPaddingForKeylineShift() {
    // TODO(b/316969331): Fix keyline shifting by decreasing carousel size when carousel is clipped
    // to padding.
    if (getClipToPadding()) {
      return 0;
    }
    if (getOrientation() == VERTICAL) {
      return getPaddingTop();
    }
    return getPaddingLeft();
  }

  private int getRightOrBottomPaddingForKeylineShift() {
    // TODO(b/316969331): Fix keyline shifting by decreasing carousel size when carousel is clipped
    // to padding.
    if (getClipToPadding()) {
      return 0;
    }
    if (getOrientation() == VERTICAL) {
      return getPaddingBottom();
    }
    return getPaddingRight();
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
    refreshKeylineState();
  }

  @Override
  public void onAttachedToWindow(RecyclerView view) {
    super.onAttachedToWindow(view);
    carouselStrategy.initialize(view.getContext());
    refreshKeylineState();
    view.addOnLayoutChangeListener(recyclerViewSizeChangeListener);
  }

  @Override
  public void onDetachedFromWindow(RecyclerView view, Recycler recycler) {
    super.onDetachedFromWindow(view, recycler);
    view.removeOnLayoutChangeListener(recyclerViewSizeChangeListener);
  }

  @Override
  public void onLayoutChildren(Recycler recycler, State state) {
    if (state.getItemCount() <= 0 || getContainerSize() <= 0f) {
      removeAndRecycleAllViews(recycler);
      currentFillStartPosition = 0;
      return;
    }

    boolean isRtl = isLayoutRtl();

    boolean isInitialLoad = keylineStateList == null;
    // If a keyline state hasn't been created or is the wrong size, use the first child as a
    // representative of how each child would like to be measured and allow the strategy to create
    // a keyline state.
    if (isInitialLoad
        || keylineStateList.getDefaultState().getCarouselSize() != getContainerSize()) {
      recalculateKeylineStateList(recycler);
    }

    // Ensure our scroll limits are initialized and valid for the data set size.
    int startScroll = calculateStartScroll(keylineStateList);
    int endScroll = calculateEndScroll(state, keylineStateList);

    // Convert the layout-direction-aware offsets into min/max absolutes. These need to be in the
    // min/max format so they can be correctly passed to KeylineStateList and used to interpolate
    // between keyline states.
    minScroll = isRtl ? endScroll : startScroll;
    maxScroll = isRtl ? startScroll : endScroll;

    if (isInitialLoad) {
      // Scroll to the start of the list on first load.
      scrollOffset = startScroll;
      keylineStatePositionMap =
          keylineStateList.getKeylineStateForPositionMap(
              getItemCount(), minScroll, maxScroll, isLayoutRtl());
      if (currentEstimatedPosition != NO_POSITION) {
        scrollOffset =
            getScrollOffsetForPosition(
                currentEstimatedPosition, getKeylineStateForPosition(currentEstimatedPosition));
      }
    }

    // Clamp the scroll offset by the new min and max by pinging the scroll by calculator
    // with a 0 delta.
    scrollOffset += calculateShouldScrollBy(0, scrollOffset, minScroll, maxScroll);

    // Ensure currentFillStartPosition is valid if the number of items in the adapter has changed.
    currentFillStartPosition = MathUtils.clamp(currentFillStartPosition, 0, state.getItemCount());

    updateCurrentKeylineStateForScrollOffset(keylineStateList);

    detachAndScrapAttachedViews(recycler);
    fill(recycler, state);
    lastItemCount = getItemCount();
  }

  @Override
  public boolean isAutoMeasureEnabled() {
    return true;
  }

  private void recalculateKeylineStateList(Recycler recycler) {
    View firstChild = recycler.getViewForPosition(0);
    measureChildWithMargins(firstChild, 0, 0);
    KeylineState keylineState = carouselStrategy.onFirstChildMeasuredWithMargins(this, firstChild);
    keylineStateList =
        KeylineStateList.from(
            this,
            isLayoutRtl() ? KeylineState.reverse(keylineState, getContainerSize()) : keylineState,
            getItemMargins(),
            getLeftOrTopPaddingForKeylineShift(),
            getRightOrBottomPaddingForKeylineShift(),
            carouselStrategy.getStrategyType());
  }

  private int getItemMargins() {
    if (getChildCount() > 0) {
      LayoutParams lp = (LayoutParams) getChildAt(0).getLayoutParams();
      if (orientationHelper.orientation == HORIZONTAL) {
        return lp.leftMargin + lp.rightMargin;
      }
      return lp.topMargin + lp.bottomMargin;
    }
    return 0;
  }

  /**
   * Recalculates the {@link KeylineState} and {@link KeylineStateList} for the current strategy.
   */
  private void refreshKeylineState() {
    keylineStateList = null;
    requestLayout();
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
    float start = calculateChildStartForFill(startPosition);
    for (int i = startPosition; i >= 0; i--) {
      float center = addEnd(start, currentKeylineState.getItemSize() / 2F);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, false);

      float offsetCenter = calculateChildOffsetCenterForLocation(center, range);
      if (isLocOffsetOutOfFillBoundsStart(offsetCenter, range)) {
        break;
      }
      start = addStart(start, currentKeylineState.getItemSize());

      // If this child's start is beyond the end of the container, don't add the child but continue
      // to loop so we can eventually get to children that are within bounds.
      if (isLocOffsetOutOfFillBoundsEnd(offsetCenter, range)) {
        continue;
      }
      View child = recycler.getViewForPosition(i);
      // Add this child to the first index of the RecyclerView.
      addAndLayoutView(
          child, /* index= */ 0, new ChildCalculations(child, center, offsetCenter, range));
    }
  }

  /**
   * Adds a child view to the RecyclerView at the given {@code childIndex}, regardless of whether or
   * not the view is in bounds.
   *
   * @param recycler current recycler that is attached to the {@link RecyclerView}
   * @param startPosition the position of the adapter whose view is to be added
   * @param childIndex the index of the RecyclerView's children that the view should be added at
   */
  private void addViewAtPosition(@NonNull Recycler recycler, int startPosition, int childIndex) {
    if (startPosition < 0 || startPosition >= getItemCount()) {
      return;
    }
    float start = calculateChildStartForFill(startPosition);
    ChildCalculations calculations = makeChildCalculations(recycler, start, startPosition);
    // Add this child to the given child index of the RecyclerView.
    addAndLayoutView(calculations.child, /* index= */ childIndex, calculations);
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
    float start = calculateChildStartForFill(startPosition);
    for (int i = startPosition; i < state.getItemCount(); i++) {
      float center = addEnd(start, currentKeylineState.getItemSize() / 2F);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, false);

      float offsetCenter = calculateChildOffsetCenterForLocation(center, range);
      if (isLocOffsetOutOfFillBoundsEnd(offsetCenter, range)) {
        break;
      }
      start = addEnd(start, currentKeylineState.getItemSize());

      // If this child's end is beyond the start of the container, don't add the child but continue
      // to loop so we can eventually get to children that are within bounds.
      if (isLocOffsetOutOfFillBoundsStart(offsetCenter, range)) {
        continue;
      }
      View child = recycler.getViewForPosition(i);
      // Add this child to the last index of the RecyclerView
      addAndLayoutView(
          child, /* index= */ -1, new ChildCalculations(child, center, offsetCenter, range));
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
        float center = getDecoratedCenterWithMargins(child);
        Log.d(
            TAG,
            "item position " + getPosition(child) + ", center:" + center + ", child index:" + i);
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
    View child = recycler.getViewForPosition(position);
    measureChildWithMargins(child, 0, 0);

    float center = addEnd(start, currentKeylineState.getItemSize() / 2F);
    KeylineRange range =
        getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, false);

    float offsetCenter = calculateChildOffsetCenterForLocation(center, range);
    return new ChildCalculations(child, center, offsetCenter, range);
  }

  /**
   * Adds a child to the RecyclerView and lays it out with its center at {@code offsetCx} on the
   * scrolling axis.
   *
   * @param child the child view to add and lay out
   * @param index the index at which to add the child to the RecyclerView. Use 0 for adding to the
   *     start of the list and -1 for adding to the end.
   * @param calculations the child calculations to be used to layout this view
   */
  private void addAndLayoutView(View child, int index, ChildCalculations calculations) {
    float halfItemSize = currentKeylineState.getItemSize() / 2F;
    addView(child, index);
    measureChildWithMargins(child, 0, 0);
    int start = (int) (calculations.offsetCenter - halfItemSize);
    int end = (int) (calculations.offsetCenter + halfItemSize);
    orientationHelper.layoutDecoratedWithMargins(child, start, end);
    updateChildMaskForLocation(child, calculations.center, calculations.range);
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
    float maskedEnd = addEnd(locOffset, maskedSize / 2F);
    return isLayoutRtl() ? maskedEnd > getContainerSize() : maskedEnd < 0;
  }

  @Override
  public boolean isHorizontal() {
    return orientationHelper.orientation == HORIZONTAL;
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
    float maskedStart = addStart(locOffset, maskedSize / 2F);
    return isLayoutRtl() ? maskedStart < 0 : maskedStart > getContainerSize();
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
    float center = outBounds.centerY();
    if (isHorizontal()) {
      center = outBounds.centerX();
    }
    float maskedSize =
        getMaskedItemSizeForLocOffset(
            center, getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, true));
    float deltaX = isHorizontal() ? (outBounds.width() - maskedSize) / 2F : 0;
    float deltaY = isHorizontal() ? 0 : (outBounds.height() - maskedSize) / 2F;

    outBounds.set(
        (int) (outBounds.left + deltaX),
        (int) (outBounds.top + deltaY),
        (int) (outBounds.right - deltaX),
        (int) (outBounds.bottom - deltaY));
  }

  private float getDecoratedCenterWithMargins(View child) {
    Rect bounds = new Rect();
    super.getDecoratedBoundsWithMargins(child, bounds);
    if (isHorizontal()) {
      return bounds.centerX();
    }
    return bounds.centerY();
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
      float center = getDecoratedCenterWithMargins(child);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, true);
      if (isLocOffsetOutOfFillBoundsStart(center, range)) {
        removeAndRecycleView(child, recycler);
      } else {
        break;
      }
    }

    // Remove items that are out of bounds at the tail of the list
    while (getChildCount() - 1 >= 0) {
      View child = getChildAt(getChildCount() - 1);
      float center = getDecoratedCenterWithMargins(child);
      KeylineRange range =
          getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, true);
      if (isLocOffsetOutOfFillBoundsEnd(center, range)) {
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
   * right is found, the right-most keyline is returned. If the orientation is vertical, the same
   * goes for top-most and bottom-most keylines respectively. This means the {@code location} is
   * outside the bounds of the outer-most keylines.
   *
   * @param location The location along the scrolling axis that should be contained by the returned
   *     keyline range. This can be either a location in the end-to-end model ({@link Keyline#loc}
   *     or in the offset model {@link Keyline#locOffset}.
   * @param isOffset true if {@code location} has been offset and should be compared against {@link
   *     Keyline#locOffset}, false if {@code location} should be compared against {@link
   *     Keyline#loc}.
   * @return A pair whose first item is the nearest {@link Keyline} before center and whose second
   *     item is the nearest {@link Keyline} after center.
   */
  private static KeylineRange getSurroundingKeylineRange(
      List<Keyline> keylines, float location, boolean isOffset) {
    int startMinDistanceIndex = -1;
    float startMinDistance = Float.MAX_VALUE;
    int startMostIndex = -1;
    float startMostX = Float.MAX_VALUE;

    int endMinDistanceIndex = -1;
    float endMinDistance = Float.MAX_VALUE;
    int endMostIndex = -1;
    float endMostX = -Float.MAX_VALUE;

    for (int i = 0; i < keylines.size(); i++) {
      Keyline keyline = keylines.get(i);
      float currentLoc = isOffset ? keyline.locOffset : keyline.loc;
      float delta = abs(currentLoc - location);

      // Find the keyline closest to the left of center with the lowest index.
      if (currentLoc <= location) {
        if (delta <= startMinDistance) {
          startMinDistance = delta;
          startMinDistanceIndex = i;
        }
      }
      // The keyline is to the right of center
      // Find the keyline closest to the right of center with the greatest index.
      if (currentLoc > location && delta <= endMinDistance) {
        endMinDistance = delta;
        endMinDistanceIndex = i;
      }
      // Find the left-most keyline
      if (currentLoc <= startMostX) {
        startMostIndex = i;
        startMostX = currentLoc;
      }
      // Find the right-most keyline
      if (currentLoc > endMostX) {
        endMostIndex = i;
        endMostX = currentLoc;
      }
    }

    // If a keyline to the left or right hasn't been found, center is outside the bounds of the
    // outer-most keylines. Use the outer-most keyline instead.
    if (startMinDistanceIndex == -1) {
      startMinDistanceIndex = startMostIndex;
    }
    if (endMinDistanceIndex == -1) {
      endMinDistanceIndex = endMostIndex;
    }

    return new KeylineRange(
        keylines.get(startMinDistanceIndex), keylines.get(endMinDistanceIndex));
  }

  private KeylineState getKeylineStartingState(KeylineStateList keylineStateList) {
    return isLayoutRtl() ? keylineStateList.getEndState() : keylineStateList.getStartState();
  }

  /**
   * Update the current keyline state by shifting it in response to any change in scroll offset.
   *
   * <p>This method should be called any time a change in the scroll offset occurs.
   */
  private void updateCurrentKeylineStateForScrollOffset(
      @NonNull KeylineStateList keylineStateList) {
    if (maxScroll <= minScroll) {
      // We don't have enough items in the list to scroll and we should use the keyline state
      // that aligns items to the start of the container.
      this.currentKeylineState = getKeylineStartingState(keylineStateList);
    } else {
      this.currentKeylineState =
          keylineStateList.getShiftedState(scrollOffset, minScroll, maxScroll);
    }
    debugItemDecoration.setKeylines(currentKeylineState.getKeylines());
  }

  /**
   * Calculates the distance children should be scrolled by for a given {@code delta}.
   *
   * @param delta the delta, resulting from a gesture or other event, that the list would like to be
   *     scrolled by
   * @param currentScroll the current scroll offset that is always between the min and max scroll
   * @param minScroll the minimum scroll offset allowed
   * @param maxScroll the maximum scroll offset allowed
   * @return an int that represents the change that should be applied to the current scroll offset,
   *     given limitations by the min and max scroll offset values
   */
  private static int calculateShouldScrollBy(
      int delta, int currentScroll, int minScroll, int maxScroll) {
    int targetScroll = currentScroll + delta;
    if (targetScroll < minScroll) {
      return minScroll - currentScroll;
    } else if (targetScroll > maxScroll) {
      return maxScroll - currentScroll;
    } else {
      return delta;
    }
  }

  /**
   * Calculates the total offset needed to scroll the first item in the list to the center of the
   * start focal keyline.
   */
  private int calculateStartScroll(@NonNull KeylineStateList stateList) {
    boolean isRtl = isLayoutRtl();
    KeylineState startState = isRtl ? stateList.getEndState() : stateList.getStartState();
    Keyline startFocalKeyline =
        isRtl ? startState.getLastFocalKeyline() : startState.getFirstFocalKeyline();
    float firstItemStart = addStart(startFocalKeyline.loc, startState.getItemSize() / 2F);
    // This value already includes any padding since startFocalKeyline.loc is already adjusted
    return (int) (getParentStart() - firstItemStart);
  }

  /**
   * Calculates the total offset needed to scroll the last item in the list to the center of the end
   * focal keyline.
   */
  private int calculateEndScroll(State state, KeylineStateList stateList) {
    boolean isRtl = isLayoutRtl();
    KeylineState endState = isRtl ? stateList.getStartState() : stateList.getEndState();
    Keyline endFocalKeyline =
        isRtl ? endState.getFirstFocalKeyline() : endState.getLastFocalKeyline();
    // Get the total distance from the first item to the last item in the end-to-end model
    float lastItemDistanceFromFirstItem =
        ((state.getItemCount() - 1) * endState.getItemSize()) * (isRtl ? -1F : 1F);

    float endFocalLocDistanceFromStart = endFocalKeyline.loc - getParentStart();

    // We want the last item in the list to only be able to scroll to the end of the list. Subtract
    // the distance to the end focal keyline and then add the distance needed to let the last
    // item hit the center of the end focal keyline.
    int endScroll =
        (int)
            (lastItemDistanceFromFirstItem
                - endFocalLocDistanceFromStart
                + (isRtl ? -1 : 1) * endFocalKeyline.maskedItemSize / 2F);
    return isRtl ? min(0, endScroll) : max(0, endScroll);
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
  private float calculateChildStartForFill(int startPosition) {
    float childScrollOffset = getParentStart() - scrollOffset;
    float positionOffset = currentKeylineState.getItemSize() * startPosition;

    return addEnd(childScrollOffset, positionOffset);
  }

  /**
   * Remaps and returns the child's offset center from the end-to-end layout model.
   *
   * @param childCenterLocation the center of the child in the end-to-end layout model
   * @param range the keyline range that the child is currently between
   * @return the location along the scroll axis where the child should be located
   */
  private float calculateChildOffsetCenterForLocation(
      float childCenterLocation, KeylineRange range) {
    float offsetCenter =
        lerp(
            range.leftOrTop.locOffset,
            range.rightOrBottom.locOffset,
            range.leftOrTop.loc,
            range.rightOrBottom.loc,
            childCenterLocation);

    // If the current center is "out of bounds", meaning it is before the first keyline or after
    // the last keyline, this item should begin scrolling at a fixed rate according to the
    // last keyline it passed (either the first or last keyline).
    // Compare reference equality here since there might be multiple keylines with the same
    // values as the first/last keyline but we want to ensure this conditional is true only when
    // we're working with the same object instance.
    if (range.rightOrBottom == currentKeylineState.getFirstKeyline()
        || range.leftOrTop == currentKeylineState.getLastKeyline()) {
      // Calculate how far past the nearest keyline (either the first or last keyline) this item
      // has scrolled in the end-to-end layout. Then use that value calculate what would be a
      // Keyline#locOffset.
      float outOfBoundOffset =
          (childCenterLocation - range.rightOrBottom.loc)
              * (1F - range.rightOrBottom.mask);
      offsetCenter += outOfBoundOffset;
    }

    return offsetCenter;
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
        range.leftOrTop.maskedItemSize,
        range.rightOrBottom.maskedItemSize,
        range.leftOrTop.locOffset,
        range.rightOrBottom.locOffset,
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
    if (!(child instanceof Maskable)) {
      return;
    }

    // Interpolate the mask value based on the location of this view between it's two
    // surrounding keylines.
    float maskProgress =
        lerp(
            range.leftOrTop.mask,
            range.rightOrBottom.mask,
            range.leftOrTop.loc,
            range.rightOrBottom.loc,
            childCenterLocation);

    float childHeight = child.getHeight();
    float childWidth = child.getWidth();
    // Translate the percentage into an actual pixel value of how much of this view should be
    // masked away.
    float maskWidth = lerp(0F, childWidth / 2F, 0F, 1F, maskProgress);
    float maskHeight = lerp(0F, childHeight / 2F, 0F, 1F, maskProgress);

    RectF maskRect = orientationHelper.getMaskRect(childHeight, childWidth, maskHeight, maskWidth);

    float offsetCenter = calculateChildOffsetCenterForLocation(childCenterLocation, range);
    float maskedTop = offsetCenter - (maskRect.height() / 2F);
    float maskedBottom = offsetCenter + (maskRect.height() / 2F);
    float maskedLeft = offsetCenter - (maskRect.width() / 2F);
    float maskedRight = offsetCenter + (maskRect.width() / 2F);

    RectF offsetMaskRect = new RectF(maskedLeft, maskedTop, maskedRight, maskedBottom);
    RectF parentBoundsRect =
        new RectF(getParentLeft(), getParentTop(), getParentRight(), getParentBottom());
    // If the carousel is a CONTAINED carousel, ensure the mask collapses against the side of the
    // container instead of bleeding and being clipped by the RecyclerView's bounds.
    // Only do this if there is only one side of the mask that is out of bounds; if
    // both sides are out of bounds on the same side, then the whole mask is out of view.
    if (carouselStrategy.getStrategyType() == StrategyType.CONTAINED) {
      orientationHelper.containMaskWithinBounds(maskRect, offsetMaskRect, parentBoundsRect);
    }

    // 'Push out' any masks that are on the parent edge by rounding up/down and adding or
    // subtracting a pixel. Otherwise, the mask on the 'edge' looks like it has a width of 1 pixel.
    orientationHelper.moveMaskOnEdgeOutsideBounds(maskRect, offsetMaskRect, parentBoundsRect);
    ((Maskable) child).setMaskRectF(maskRect);
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
        keylineStateList != null && orientationHelper.orientation == HORIZONTAL
            ? keylineStateList.getDefaultState().getItemSize()
            : lp.width;
    final float childHeightDimension =
        keylineStateList != null && orientationHelper.orientation == VERTICAL
            ? keylineStateList.getDefaultState().getItemSize()
            : lp.height;
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
            (int) childHeightDimension,
            canScrollVertically());
    child.measure(widthSpec, heightSpec);
  }

  private int getParentLeft() {
    return orientationHelper.getParentLeft();
  }

  private int getParentStart() {
    return orientationHelper.getParentStart();
  }

  private int getParentRight() {
    return orientationHelper.getParentRight();
  }

  private int getParentTop() {
    return orientationHelper.getParentTop();
  }

  private int getParentBottom() {
    return orientationHelper.getParentBottom();
  }

  @Override
  public int getContainerWidth() {
    return getWidth();
  }

  @Override
  public int getContainerHeight() {
    return getHeight();
  }

  /**
   * Returns the dimension of the container according to the carousel orientation. Eg. width if the
   * orientation is horizontal, height if vertical.
   */
  private int getContainerSize() {
    if (isHorizontal()) {
      return getContainerWidth();
    }
    return getContainerHeight();
  }

  boolean isLayoutRtl() {
    return isHorizontal() && getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
  }

  /** Moves {@code value} towards the start of the container by {@code amount}. */
  private float addStart(float value, float amount) {
    return isLayoutRtl() ? value + amount : value - amount;
  }

  /** Moves {@code value} towards the end of the container by {@code amount}. */
  private float addEnd(float value, float amount) {
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
   * <p>This will calculate the scroll offset needed to place a child at {@code position}'s center
   * at the start-most focal keyline. The returned value might be less or greater than
   * the min and max scroll offsets but this will be clamped in {@link #scrollBy(int, Recycler,
   * State)} (Recycler, State)} by {@link #calculateShouldScrollBy(int, int, int, int)}.
   *
   * @param position The position to get the scroll offset to.
   * @param keylineState The keyline state in which to calculate the scroll offset to.
   */
  private int getScrollOffsetForPosition(int position, KeylineState keylineState) {
    if (isLayoutRtl()) {
      return (int)
          ((getContainerSize() - keylineState.getLastFocalKeyline().loc)
              - (position * keylineState.getItemSize())
              - (keylineState.getItemSize() / 2F));
    } else {
      return (int)
          ((position * keylineState.getItemSize())
              - keylineState.getFirstFocalKeyline().loc
              + (keylineState.getItemSize() / 2F));
    }
  }

  private int getSmallestScrollOffsetToFocalKeyline(
      int position, @NonNull KeylineState keylineState) {
    int smallestScrollOffset = Integer.MAX_VALUE;
    for (Keyline keyline : keylineState.getFocalKeylines()) {
      float offsetWithoutKeylines = position * keylineState.getItemSize();
      float halfFocalKeylineSize = keylineState.getItemSize() / 2F;
      float offsetWithKeylines = offsetWithoutKeylines + halfFocalKeylineSize;

      int positionOffsetDistanceFromKeyline =
          isLayoutRtl()
              ? (int) ((getContainerSize() - keyline.loc) - offsetWithKeylines)
              : (int) (offsetWithKeylines - keyline.loc);
      positionOffsetDistanceFromKeyline -= scrollOffset;

      if (Math.abs(smallestScrollOffset) > Math.abs(positionOffsetDistanceFromKeyline)) {
        smallestScrollOffset = positionOffsetDistanceFromKeyline;
      }
    }
    return smallestScrollOffset;
  }

  @Nullable
  @Override
  public PointF computeScrollVectorForPosition(int targetPosition) {
    if (keylineStateList == null) {
      return null;
    }

    KeylineState keylineForScroll = getKeylineStateForPosition(targetPosition);
    int offset = getOffsetToScrollToPosition(targetPosition, keylineForScroll);
    if (isHorizontal()) {
      return new PointF(offset, 0F);
    }
    return new PointF(0F, offset);
  }

  /**
   * Gets the offset needed to scroll to a position from the current scroll offset.
   *
   * <p>This will calculate the scroll offset needed to place a child at {@code position}'s center
   * at the start-most focal keyline.
   */
  int getOffsetToScrollToPosition(int position, @NonNull KeylineState keylineState) {
    int targetScrollOffset = getScrollOffsetForPosition(position, keylineState);
    return targetScrollOffset - scrollOffset;
  }

  /**
   * Gets the offset needed to snap to a position from the current scroll offset.
   *
   * <p>This will calculate the horizontal scroll offset needed to place a child at {@code
   * position}'s center at the start-most focal keyline of the target keyline state to snap to.
   *
   * <p>Sometimes we may want to do a partial snap. Eg. When there is a fling event, the snap
   * distance is fetched before it finishes scrolling and the target keyline state is not yet
   * updated. Once the fling event finishes scrolling, the snap is triggered again with the correct
   * target keyline state. If {@code partialSnap} is true, then we want to snap to whichever is
   * smaller between {@code targetKeylineStateForSnap}, which is the closest keyline state step to
   * the current keyline state, or the KeylineState at the correct position in {@code
   * keylineStatePositionList}. Note that if there is any distance left to be snapped when the
   * fling-scroll stops, the snap helper will handle it.
   */
  int getOffsetToScrollToPositionForSnap(int position, boolean partialSnap) {
    KeylineState targetKeylineStateForSnap =
        keylineStateList.getShiftedState(scrollOffset, minScroll, maxScroll, true);
    int targetSnapOffset = getOffsetToScrollToPosition(position, targetKeylineStateForSnap);
    int positionOffset = targetSnapOffset;
    if (keylineStatePositionMap != null) {
      positionOffset = getOffsetToScrollToPosition(position, getKeylineStateForPosition(position));
    }
    if (partialSnap) {
      return Math.abs(positionOffset) < Math.abs(targetSnapOffset)
          ? positionOffset
          : targetSnapOffset;
    }
    return targetSnapOffset;
  }

  private KeylineState getKeylineStateForPosition(int position) {
    if (keylineStatePositionMap != null) {
      KeylineState keylineState = keylineStatePositionMap.get(
          MathUtils.clamp(position, 0, max(0, getItemCount() - 1)));
      if (keylineState != null) {
        return keylineState;
      }
    }
    return keylineStateList.getDefaultState();
  }

  @Override
  public void scrollToPosition(int position) {
    currentEstimatedPosition = position;
    if (keylineStateList == null) {
      return;
    }
    scrollOffset = getScrollOffsetForPosition(position, getKeylineStateForPosition(position));
    currentFillStartPosition = MathUtils.clamp(position, 0, max(0, getItemCount() - 1));
    updateCurrentKeylineStateForScrollOffset(keylineStateList);
    requestLayout();
  }

  @Override
  public void smoothScrollToPosition(RecyclerView recyclerView, State state, int position) {
    LinearSmoothScroller linearSmoothScroller =
        new LinearSmoothScroller(recyclerView.getContext()) {
          @Nullable
          @Override
          public PointF computeScrollVectorForPosition(int targetPosition) {
            return CarouselLayoutManager.this.computeScrollVectorForPosition(targetPosition);
          }

          @Override
          public int calculateDxToMakeVisible(View view, int snapPreference) {
            if (keylineStateList == null || !isHorizontal()) {
              return 0;
            }
            // Override dx calculations so the target view is brought all the way into the focal
            // range instead of just being made visible.
            return calculateScrollDeltaToMakePositionVisible(getPosition(view));
          }

          @Override
          public int calculateDyToMakeVisible(View view, int snapPreference) {
            if (keylineStateList == null || isHorizontal()) {
              return 0;
            }
            // Override dy calculations so the target view is brought all the way into the focal
            // range instead of just being made visible.
            return calculateScrollDeltaToMakePositionVisible(getPosition(view));
          }
        };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }

  int calculateScrollDeltaToMakePositionVisible(int position) {
    KeylineState scrollToKeyline = getKeylineStateForPosition(position);

    float targetScrollOffset = getScrollOffsetForPosition(position, scrollToKeyline);
    return (int) (scrollOffset - targetScrollOffset);
  }

  @Override
  public boolean canScrollHorizontally() {
    return isHorizontal();
  }

  @Override
  public int scrollHorizontallyBy(int dx, Recycler recycler, State state) {
    return canScrollHorizontally() ? scrollBy(dx, recycler, state) : 0;
  }

  @Override
  public boolean canScrollVertically() {
    return !isHorizontal();
  }

  @Override
  public int scrollVerticallyBy(int dy, Recycler recycler, State state) {
    return canScrollVertically() ? scrollBy(dy, recycler, state) : 0;
  }

  /**
   * Helper class to encapsulate information about the layout direction in relation to the focus
   * direction.
   */
  private static class LayoutDirection {
    private static final int LAYOUT_START = -1;

    private static final int LAYOUT_END = 1;

    private static final int INVALID_LAYOUT = Integer.MIN_VALUE;
  }

  /**
   * Converts a focusDirection to a layout direction.
   *
   * @param focusDirection One of {@link View#FOCUS_UP}, {@link View#FOCUS_DOWN}, {@link
   *     View#FOCUS_LEFT}, {@link View#FOCUS_RIGHT}, {@link View#FOCUS_BACKWARD}, {@link
   *     View#FOCUS_FORWARD} or 0 for not applicable
   * @return {@link LayoutDirection#LAYOUT_START} or {@link LayoutDirection#LAYOUT_END} if focus
   *     direction is applicable to current state, {@link LayoutDirection#INVALID_LAYOUT} otherwise.
   */
  private int convertFocusDirectionToLayoutDirection(int focusDirection) {
    int orientation = getOrientation();
    switch (focusDirection) {
      case View.FOCUS_BACKWARD:
        return LayoutDirection.LAYOUT_START;
      case View.FOCUS_FORWARD:
        return LayoutDirection.LAYOUT_END;
      case View.FOCUS_UP:
        return orientation == VERTICAL
            ? LayoutDirection.LAYOUT_START
            : LayoutDirection.INVALID_LAYOUT;
      case View.FOCUS_DOWN:
        return orientation == VERTICAL
            ? LayoutDirection.LAYOUT_END
            : LayoutDirection.INVALID_LAYOUT;
      case View.FOCUS_LEFT:
        if (orientation == HORIZONTAL) {
          return isLayoutRtl() ? LayoutDirection.LAYOUT_END : LayoutDirection.LAYOUT_START;
        }
        return LayoutDirection.INVALID_LAYOUT;
      case View.FOCUS_RIGHT:
        if (orientation == HORIZONTAL) {
          return isLayoutRtl() ? LayoutDirection.LAYOUT_START : LayoutDirection.LAYOUT_END;
        }
        return LayoutDirection.INVALID_LAYOUT;
      default:
        Log.d(TAG, "Unknown focus request:" + focusDirection);
        return LayoutDirection.INVALID_LAYOUT;
    }
  }

  @Nullable
  @Override
  public View onFocusSearchFailed(
      @NonNull View focused, int focusDirection, @NonNull Recycler recycler, @NonNull State state) {
    if (getChildCount() == 0) {
      return null;
    }

    final int layoutDir = convertFocusDirectionToLayoutDirection(focusDirection);
    if (layoutDir == LayoutDirection.INVALID_LAYOUT) {
      return null;
    }

    final View nextFocus;
    if (layoutDir == LayoutDirection.LAYOUT_START) {
      if (getPosition(focused) == 0) {
        return null;
      }
      int firstPosition = getPosition(getChildAt(0));
      addViewAtPosition(recycler, firstPosition - 1, 0);
      nextFocus = getChildClosestToStart();
    } else {
      if (getPosition(focused) == getItemCount() - 1) {
        return null;
      }
      int lastPosition = getPosition(getChildAt(getChildCount() - 1));
      addViewAtPosition(recycler, lastPosition + 1, -1);
      nextFocus = getChildClosestToEnd();
    }

    return nextFocus;
  }

  /**
   * Convenience method to find the child closes to start. Caller should check if it has enough
   * children.
   *
   * @return The child closest to start of the layout from user's perspective.
   */
  private View getChildClosestToStart() {
    return getChildAt(isLayoutRtl() ? getChildCount() - 1 : 0);
  }

  /**
   * Convenience method to find the child closes to end. Caller should check if it has enough
   * children.
   *
   * @return The child closest to end of the layout from user's perspective.
   */
  private View getChildClosestToEnd() {
    return getChildAt(isLayoutRtl() ? 0 : getChildCount() - 1);
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
    int delta =
        getSmallestScrollOffsetToFocalKeyline(
            getPosition(child), getKeylineStateForPosition(getPosition(child)));
      // TODO(b/266816148): Implement smoothScrollBy when immediate is false.
    if (delta == 0) {
      return false;
    }
    // Get the keyline state at the scroll offset, and scroll based on that.
    int realDelta = calculateShouldScrollBy(delta, scrollOffset, minScroll, maxScroll);
    KeylineState scrolledKeylineState =
        keylineStateList.getShiftedState(scrollOffset + realDelta, minScroll, maxScroll);

    delta = getSmallestScrollOffsetToFocalKeyline(getPosition(child), scrolledKeylineState);
    scrollBy(parent, delta);
    return true;
  }

  private void scrollBy(RecyclerView recyclerView, int delta) {
    if (isHorizontal()) {
      recyclerView.scrollBy(delta, 0);
    } else {
      recyclerView.scrollBy(0, delta);
    }
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

    if (keylineStateList == null) {
      recalculateKeylineStateList(recycler);
    }

    // If the number of items is equal or less than the number of focal items, we should not be able
    // to scroll.
    if (getItemCount() <= getKeylineStartingState(keylineStateList).getTotalVisibleFocalItems()) {
      return 0;
    }

    // Calculate how much the carousel should scroll and update the scroll offset.
    int scrolledBy = calculateShouldScrollBy(distance, scrollOffset, minScroll, maxScroll);
    scrollOffset += scrolledBy;
    updateCurrentKeylineStateForScrollOffset(keylineStateList);

    float halfItemSize = currentKeylineState.getItemSize() / 2F;
    int startPosition = getPosition(getChildAt(0));
    float start = calculateChildStartForFill(startPosition);
    Rect boundsRect = new Rect();
    float firstFocalKeylineLoc =
        isLayoutRtl()
            ? currentKeylineState.getLastFocalKeyline().locOffset
            : currentKeylineState.getFirstFocalKeyline().locOffset;
    float absDistanceToFirstFocal = Float.MAX_VALUE;
    for (int i = 0; i < getChildCount(); i++) {
      View child = getChildAt(i);
      float offsetCenter = offsetChild(child, start, halfItemSize, boundsRect);
      float distanceToFirstFocal = Math.abs(firstFocalKeylineLoc - offsetCenter);
      if (child != null && distanceToFirstFocal < absDistanceToFirstFocal) {
        absDistanceToFirstFocal = distanceToFirstFocal;
        currentEstimatedPosition = getPosition(child);
      }
      start = addEnd(start, currentKeylineState.getItemSize());
    }

    // Fill any additional space caused by scrolling with more items.
    fill(recycler, state);

    return scrolledBy;
  }

  /**
   * Offsets a child from its current location to its location when its start is placed at {@code
   * startOffset} and updates the child's mask according to its new surrounding keylines.
   *
   * @param child the child to offset
   * @param startOffset where the start of the child should be placed, in the end-to-end model,
   *     after the child has been offset
   * @param halfItemSize half of the fully unmasked item size
   * @param boundsRect a Rect to use to find the current bounds of {@code child}
   * @return the center in which the child is offset to
   */
  private float offsetChild(View child, float startOffset, float halfItemSize, Rect boundsRect) {
    float center = addEnd(startOffset, halfItemSize);
    KeylineRange range =
        getSurroundingKeylineRange(currentKeylineState.getKeylines(), center, false);
    float offsetCenter = calculateChildOffsetCenterForLocation(center, range);

    // Offset the child so its center is at offsetCenter
    super.getDecoratedBoundsWithMargins(child, boundsRect);
    updateChildMaskForLocation(child, center, range);
    orientationHelper.offsetChild(child, boundsRect, halfItemSize, offsetCenter);
    return offsetCenter;
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
    return scrollOffset;
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
    if (getChildCount() == 0 || keylineStateList == null || getItemCount() <= 1) {
      return 0;
    }
    float itemRatio =
        (keylineStateList.getDefaultState().getItemSize() / computeHorizontalScrollRange(state));
    return (int) (getWidth() * itemRatio);
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
    return maxScroll - minScroll;
  }

  @Override
  public int computeVerticalScrollOffset(@NonNull State state) {
    return scrollOffset;
  }

  @Override
  public int computeVerticalScrollExtent(@NonNull State state) {
    if (getChildCount() == 0 || keylineStateList == null || getItemCount() <= 1) {
      return 0;
    }
    float itemRatio =
        (keylineStateList.getDefaultState().getItemSize() / computeVerticalScrollRange(state));
    return (int) (getHeight() * itemRatio);
  }

  @Override
  public int computeVerticalScrollRange(@NonNull State state) {
    return maxScroll - minScroll;
  }

  /**
   * Returns the current orientation of the layout.
   *
   * @return Current orientation, either {@link #HORIZONTAL} or {@link #VERTICAL}
   * @see #setOrientation(int)
   */
  @RecyclerView.Orientation
  public int getOrientation() {
    return orientationHelper.orientation;
  }

  /**
   * Sets the orientation of the layout.
   *
   * @param orientation {@link #HORIZONTAL} or {@link #VERTICAL}
   */
  public void setOrientation(@RecyclerView.Orientation int orientation) {
    if (orientation != HORIZONTAL && orientation != VERTICAL) {
      throw new IllegalArgumentException("invalid orientation:" + orientation);
    }

    assertNotInLayoutOrScroll(null);

    if (orientationHelper == null || orientation != orientationHelper.orientation) {
      orientationHelper = CarouselOrientationHelper.createOrientationHelper(this, orientation);
      refreshKeylineState();
    }
  }

  @Override
  public void onItemsAdded(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
    super.onItemsAdded(recyclerView, positionStart, itemCount);
    updateItemCount();
  }

  @Override
  public void onItemsRemoved(@NonNull RecyclerView recyclerView, int positionStart, int itemCount) {
    super.onItemsRemoved(recyclerView, positionStart, itemCount);
    updateItemCount();
  }

  @Override
  public void onItemsChanged(@NonNull RecyclerView recyclerView) {
    super.onItemsChanged(recyclerView);
    updateItemCount();
  }

  private void updateItemCount() {
    int newItemCount = getItemCount();

    if (newItemCount == this.lastItemCount || keylineStateList == null) {
      return;
    }
    if (carouselStrategy.shouldRefreshKeylineState(this, this.lastItemCount)) {
      refreshKeylineState();
    }
    this.lastItemCount = newItemCount;
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
    final Keyline leftOrTop;
    final Keyline rightOrBottom;

    /**
     * Create a new keyline range.
     *
     * @param leftOrTop The start keyline boundary of this range.
     * @param rightOrBottom The end keyline boundary of this range.
     */
    KeylineRange(Keyline leftOrTop, Keyline rightOrBottom) {
      Preconditions.checkArgument(leftOrTop.loc <= rightOrBottom.loc);
      this.leftOrTop = leftOrTop;
      this.rightOrBottom = rightOrBottom;
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
        if (((CarouselLayoutManager) parent.getLayoutManager()).isHorizontal()) {
        c.drawLine(
            keyline.locOffset,
            ((CarouselLayoutManager) parent.getLayoutManager()).getParentTop(),
            keyline.locOffset,
            ((CarouselLayoutManager) parent.getLayoutManager()).getParentBottom(),
            linePaint);
        } else {
          c.drawLine(
              ((CarouselLayoutManager) parent.getLayoutManager()).getParentLeft(),
              keyline.locOffset,
              ((CarouselLayoutManager) parent.getLayoutManager()).getParentRight(),
              keyline.locOffset,
              linePaint);
        }
      }
    }
  }
}
