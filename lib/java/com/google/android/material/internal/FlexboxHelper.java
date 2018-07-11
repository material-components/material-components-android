/*
 * Copyright 2018 The Android Open Source Project
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

package com.google.android.material.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.internal.FlexItem.FLEX_BASIS_PERCENT_DEFAULT;
import static android.support.v7.widget.RecyclerView.NO_POSITION;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Offers various calculations for Flexbox to use the common logic between the classes such as
 * {@link FlexboxLayout}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
class FlexboxHelper {

  private static final int INITIAL_CAPACITY = 10;

  private final FlexContainer flexContainer;

  /**
   * Holds the 'frozen' state of children during measure. If a view is frozen it will no longer
   * expand or shrink regardless of flex grow/flex shrink attributes.
   */
  private boolean[] childrenFrozen;

  FlexboxHelper(FlexContainer flexContainer) {
    this.flexContainer = flexContainer;
  }

  /**
   * Create an array, which indicates the reordered indices that {@link FlexItem#getOrder()}
   * attributes are taken into account. This method takes a View before that is added as the parent
   * ViewGroup's children.
   *
   * @param viewBeforeAdded the View instance before added to the array of children Views of the
   *     parent ViewGroup
   * @param indexForViewBeforeAdded the index for the View before added to the array of the parent
   *     ViewGroup
   * @param paramsForViewBeforeAdded the layout parameters for the View before added to the array of
   *     the parent ViewGroup
   * @return an array which have the reordered indices
   */
  int[] createReorderedIndices(
      View viewBeforeAdded,
      int indexForViewBeforeAdded,
      ViewGroup.LayoutParams paramsForViewBeforeAdded,
      SparseIntArray orderCache) {
    int childCount = flexContainer.getFlexItemCount();
    List<Order> orders = createOrders(childCount);
    Order orderForViewToBeAdded = new Order();
    if (viewBeforeAdded != null && paramsForViewBeforeAdded instanceof FlexItem) {
      orderForViewToBeAdded.order = ((FlexItem) paramsForViewBeforeAdded).getOrder();
    } else {
      orderForViewToBeAdded.order = FlexItem.ORDER_DEFAULT;
    }

    if (indexForViewBeforeAdded == -1 || indexForViewBeforeAdded == childCount) {
      orderForViewToBeAdded.index = childCount;
    } else if (indexForViewBeforeAdded < flexContainer.getFlexItemCount()) {
      orderForViewToBeAdded.index = indexForViewBeforeAdded;
      for (int i = indexForViewBeforeAdded; i < childCount; i++) {
        orders.get(i).index++;
      }
    } else {
      // This path is not expected since OutOfBoundException will be thrown in the ViewGroup
      // But setting the index for fail-safe
      orderForViewToBeAdded.index = childCount;
    }
    orders.add(orderForViewToBeAdded);

    return sortOrdersIntoReorderedIndices(childCount + 1, orders, orderCache);
  }

  /**
   * Create an array, which indicates the reordered indices that {@link FlexItem#getOrder()}
   * attributes are taken into account.
   *
   * @return @return an array which have the reordered indices
   */
  int[] createReorderedIndices(SparseIntArray orderCache) {
    int childCount = flexContainer.getFlexItemCount();
    List<Order> orders = createOrders(childCount);
    return sortOrdersIntoReorderedIndices(childCount, orders, orderCache);
  }

  @NonNull
  private List<Order> createOrders(int childCount) {
    List<Order> orders = new ArrayList<>(childCount);
    for (int i = 0; i < childCount; i++) {
      View child = flexContainer.getFlexItemAt(i);
      FlexItem flexItem = (FlexItem) child.getLayoutParams();
      Order order = new Order();
      order.order = flexItem.getOrder();
      order.index = i;
      orders.add(order);
    }
    return orders;
  }

  /**
   * Returns if any of the children's {@link FlexItem#getOrder()} attributes are changed from the
   * last measurement.
   *
   * @return {@code true} if changed from the last measurement, {@code false} otherwise.
   */
  boolean isOrderChangedFromLastMeasurement(SparseIntArray orderCache) {
    int childCount = flexContainer.getFlexItemCount();
    if (orderCache.size() != childCount) {
      return true;
    }
    for (int i = 0; i < childCount; i++) {
      View view = flexContainer.getFlexItemAt(i);
      if (view == null) {
        continue;
      }
      FlexItem flexItem = (FlexItem) view.getLayoutParams();
      if (flexItem.getOrder() != orderCache.get(i)) {
        return true;
      }
    }
    return false;
  }

  private int[] sortOrdersIntoReorderedIndices(
      int childCount, List<Order> orders, SparseIntArray orderCache) {
    Collections.sort(orders);
    orderCache.clear();
    int[] reorderedIndices = new int[childCount];
    int i = 0;
    for (Order order : orders) {
      reorderedIndices[i] = order.index;
      orderCache.append(order.index, order.order);
      i++;
    }
    return reorderedIndices;
  }

  /**
   * Calculate how many flex lines are needed in the flex container. This method should calculate
   * all the flex lines from the existing flex items.
   *
   * @see #calculateFlexLines(FlexLinesResult, int, int, int, int, int, List)
   */
  void calculateHorizontalFlexLines(
      FlexLinesResult result, int widthMeasureSpec, int heightMeasureSpec) {
    calculateFlexLines(
        result, widthMeasureSpec, heightMeasureSpec, Integer.MAX_VALUE, 0, NO_POSITION, null);
  }

  /**
   * Calculates how many flex lines are needed in the flex container layout by measuring each child.
   * Expanding or shrinking the flex items depending on the flex grow and flex shrink attributes are
   * done in a later procedure, so the views' measured width and measured height may be changed in a
   * later process.
   *
   * @param result an instance of {@link FlexLinesResult} that is going to contain a list of flex
   *     lines and the child state used by {@link View#setMeasuredDimension(int, int)}.
   * @param mainMeasureSpec the main axis measure spec imposed by the flex container, width for
   *     horizontal direction, height otherwise
   * @param crossMeasureSpec the cross axis measure spec imposed by the flex container, height for
   *     horizontal direction, width otherwise
   * @param needsCalcAmount the amount of pixels where flex line calculation should be stopped this
   *     is needed to avoid the expensive calculation if the calculation is needed only the small
   *     part of the entire flex container.
   * @param fromIndex the index of the child from which the calculation starts
   * @param toIndex the index of the child to which the calculation ends (until the flex line which
   *     include the which who has that index). If this and needsCalcAmount are both set, first flex
   *     lines are calculated to the index, calculate the amount of pixels as the needsCalcAmount
   *     argument in addition to that
   * @param existingLines If not null, calculated flex lines will be added to this instance
   */
  private void calculateFlexLines(
      FlexLinesResult result,
      int mainMeasureSpec,
      int crossMeasureSpec,
      int needsCalcAmount,
      int fromIndex,
      int toIndex,
      @Nullable List<FlexLine> existingLines) {

    boolean isMainHorizontal = flexContainer.isMainAxisDirectionHorizontal();

    int mainMode = View.MeasureSpec.getMode(mainMeasureSpec);
    int mainSize = View.MeasureSpec.getSize(mainMeasureSpec);

    int childState = 0;

    List<FlexLine> flexLines;
    if (existingLines == null) {
      flexLines = new ArrayList<>();
    } else {
      flexLines = existingLines;
    }

    result.flexLines = flexLines;

    boolean reachedToIndex = toIndex == NO_POSITION;

    int mainPaddingStart = getPaddingStartMain(isMainHorizontal);
    int mainPaddingEnd = getPaddingEndMain(isMainHorizontal);
    int crossPaddingStart = getPaddingStartCross(isMainHorizontal);
    int crossPaddingEnd = getPaddingEndCross(isMainHorizontal);

    int largestSizeInCross = Integer.MIN_VALUE;

    // The amount of cross size calculated in this method call.
    int sumCrossSize = 0;

    // The index of the view in the flex line.
    int indexInFlexLine = 0;

    FlexLine flexLine = new FlexLine();
    flexLine.firstIndex = fromIndex;
    flexLine.mainSize = mainPaddingStart + mainPaddingEnd;

    int childCount = flexContainer.getFlexItemCount();
    for (int i = fromIndex; i < childCount; i++) {
      View child = flexContainer.getReorderedFlexItemAt(i);

      if (child == null) {
        if (isLastFlexItem(i, childCount, flexLine)) {
          addFlexLine(flexLines, flexLine, i, sumCrossSize);
        }
        continue;
      } else if (child.getVisibility() == View.GONE) {
        flexLine.goneItemCount++;
        flexLine.itemCount++;
        if (isLastFlexItem(i, childCount, flexLine)) {
          addFlexLine(flexLines, flexLine, i, sumCrossSize);
        }
        continue;
      }

      FlexItem flexItem = (FlexItem) child.getLayoutParams();

      flexLine.indicesAlignSelfStretch.add(i);

      int childMainSize = getFlexItemSizeMain(flexItem, isMainHorizontal);

      if (flexItem.getFlexBasisPercent() != FLEX_BASIS_PERCENT_DEFAULT
          && mainMode == View.MeasureSpec.EXACTLY) {
        childMainSize = Math.round(mainSize * flexItem.getFlexBasisPercent());
        // Use the dimension from the layout if the mainMode is not
        // MeasureSpec.EXACTLY even if any fraction value is set to
        // layout_flexBasisPercent.
      }

      int childMainMeasureSpec;
      int childCrossMeasureSpec;
      if (isMainHorizontal) {
        childMainMeasureSpec =
            flexContainer.getChildWidthMeasureSpec(
                mainMeasureSpec,
                mainPaddingStart
                    + mainPaddingEnd
                    + getFlexItemMarginStartMain(flexItem, true)
                    + getFlexItemMarginEndMain(flexItem, true),
                childMainSize);
        childCrossMeasureSpec =
            flexContainer.getChildHeightMeasureSpec(
                crossMeasureSpec,
                crossPaddingStart
                    + crossPaddingEnd
                    + getFlexItemMarginStartCross(flexItem, true)
                    + getFlexItemMarginEndCross(flexItem, true)
                    + sumCrossSize,
                getFlexItemSizeCross(flexItem, true));
        child.measure(childMainMeasureSpec, childCrossMeasureSpec);
      } else {
        childCrossMeasureSpec =
            flexContainer.getChildWidthMeasureSpec(
                crossMeasureSpec,
                crossPaddingStart
                    + crossPaddingEnd
                    + getFlexItemMarginStartCross(flexItem, false)
                    + getFlexItemMarginEndCross(flexItem, false)
                    + sumCrossSize,
                getFlexItemSizeCross(flexItem, false));
        childMainMeasureSpec =
            flexContainer.getChildHeightMeasureSpec(
                mainMeasureSpec,
                mainPaddingStart
                    + mainPaddingEnd
                    + getFlexItemMarginStartMain(flexItem, false)
                    + getFlexItemMarginEndMain(flexItem, false),
                childMainSize);
        child.measure(childCrossMeasureSpec, childMainMeasureSpec);
      }
      flexContainer.updateViewCache(i, child);

      // Check the size constraint after the first measurement for the child
      // To prevent the child's width/height violate the size constraints imposed by the
      // {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()},
      // {@link FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
      // E.g. When the child's layout_width is wrap_content the measured width may be
      // less than the min width after the first measurement.
      checkSizeConstraints(child, i);

      childState = View.combineMeasuredStates(childState, child.getMeasuredState());

      if (isWrapRequired(
          child,
          mainMode,
          mainSize,
          flexLine.mainSize,
          getViewMeasuredSizeMain(child, isMainHorizontal)
              + getFlexItemMarginStartMain(flexItem, isMainHorizontal)
              + getFlexItemMarginEndMain(flexItem, isMainHorizontal),
          flexItem,
          i,
          indexInFlexLine)) {
        if (flexLine.getItemCountNotGone() > 0) {
          addFlexLine(flexLines, flexLine, i > 0 ? i - 1 : 0, sumCrossSize);
          sumCrossSize += flexLine.crossSize;
        }

        if (isMainHorizontal) {
          if (flexItem.getHeight() == ViewGroup.LayoutParams.MATCH_PARENT) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_height is set to match_parent, the height
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the height of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec =
                flexContainer.getChildHeightMeasureSpec(
                    crossMeasureSpec,
                    flexContainer.getPaddingTop()
                        + flexContainer.getPaddingBottom()
                        + flexItem.getMarginTop()
                        + flexItem.getMarginBottom()
                        + sumCrossSize,
                    flexItem.getHeight());
            child.measure(childMainMeasureSpec, childCrossMeasureSpec);
            checkSizeConstraints(child, i);
          }
        } else {
          if (flexItem.getWidth() == ViewGroup.LayoutParams.MATCH_PARENT) {
            // This case takes care of the corner case where the cross size of the
            // child is affected by the just added flex line.
            // E.g. when the child's layout_width is set to match_parent, the width
            // of that child needs to be determined taking the total cross size used
            // so far into account. In that case, the width of the child needs to be
            // measured again note that we don't need to judge if the wrapping occurs
            // because it doesn't change the size along the main axis.
            childCrossMeasureSpec =
                flexContainer.getChildWidthMeasureSpec(
                    crossMeasureSpec,
                    flexContainer.getPaddingLeft()
                        + flexContainer.getPaddingRight()
                        + flexItem.getMarginLeft()
                        + flexItem.getMarginRight()
                        + sumCrossSize,
                    flexItem.getWidth());
            child.measure(childCrossMeasureSpec, childMainMeasureSpec);
            checkSizeConstraints(child, i);
          }
        }

        flexLine = new FlexLine();
        flexLine.itemCount = 1;
        flexLine.mainSize = mainPaddingStart + mainPaddingEnd;
        flexLine.firstIndex = i;
        indexInFlexLine = 0;
        largestSizeInCross = Integer.MIN_VALUE;
      } else {
        flexLine.itemCount++;
        indexInFlexLine++;
      }
      flexLine.mainSize +=
          getViewMeasuredSizeMain(child, isMainHorizontal)
              + getFlexItemMarginStartMain(flexItem, isMainHorizontal)
              + getFlexItemMarginEndMain(flexItem, isMainHorizontal);
      flexLine.motalFlexGrow += flexItem.getFlexGrow();
      flexLine.totalFlexShrink += flexItem.getFlexShrink();

      flexContainer.onNewFlexItemAdded(child, i, indexInFlexLine, flexLine);

      largestSizeInCross =
          Math.max(
              largestSizeInCross,
              getViewMeasuredSizeCross(child, isMainHorizontal)
                  + getFlexItemMarginStartCross(flexItem, isMainHorizontal)
                  + getFlexItemMarginEndCross(flexItem, isMainHorizontal)
                  + flexContainer.getDecorationLengthCrossAxis(child));
      // Temporarily set the cross axis length as the largest child in the flexLine
      // Expand along the cross axis depending on the mAlignContent property if needed
      // later
      flexLine.crossSize = Math.max(flexLine.crossSize, largestSizeInCross);

      if (isMainHorizontal) {
        flexLine.maxBaseline =
            Math.max(flexLine.maxBaseline, child.getBaseline() + flexItem.getMarginTop());
      }

      if (isLastFlexItem(i, childCount, flexLine)) {
        addFlexLine(flexLines, flexLine, i, sumCrossSize);
        sumCrossSize += flexLine.crossSize;
      }

      if (toIndex != NO_POSITION
          && !flexLines.isEmpty()
          && flexLines.get(flexLines.size() - 1).lastIndex >= toIndex
          && i >= toIndex
          && !reachedToIndex) {
        // Calculated to include a flex line which includes the flex item having the
        // toIndex.
        // Let the sumCrossSize start from the negative value of the last flex line's
        // cross size because otherwise flex lines aren't calculated enough to fill the
        // visible area.
        sumCrossSize = -flexLine.getCrossSize();
        reachedToIndex = true;
      }
      if (sumCrossSize > needsCalcAmount && reachedToIndex) {
        // Stop the calculation if the sum of cross size calculated reached to the point
        // beyond the needsCalcAmount value to avoid unneeded calculation in a
        // RecyclerView.
        // To be precise, the decoration length may be added to the sumCrossSize,
        // but we omit adding the decoration length because even without the decorator
        // length, it's guaranteed that calculation is done at least beyond the
        // needsCalcAmount
        break;
      }
    }

    result.childState = childState;
  }

  /**
   * Returns the container's start padding in the main axis. Either start or top.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the start padding in the main axis
   */
  private int getPaddingStartMain(boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexContainer.getPaddingStart();
    }

    return flexContainer.getPaddingTop();
  }

  /**
   * Returns the container's end padding in the main axis. Either end or bottom.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the end padding in the main axis
   */
  private int getPaddingEndMain(boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexContainer.getPaddingEnd();
    }

    return flexContainer.getPaddingBottom();
  }

  /**
   * Returns the container's start padding in the cross axis. Either start or top.
   *
   * @param isMainHorizontal is the main axis horizontal.
   * @return the start padding in the cross axis
   */
  private int getPaddingStartCross(boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexContainer.getPaddingTop();
    }

    return flexContainer.getPaddingStart();
  }

  /**
   * Returns the container's end padding in the cross axis. Either end or bottom.
   *
   * @param isMainHorizontal is the main axis horizontal
   * @return the end padding in the cross axis
   */
  private int getPaddingEndCross(boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexContainer.getPaddingBottom();
    }

    return flexContainer.getPaddingEnd();
  }

  /**
   * Returns the view's measured size in the main axis. Either width or height.
   *
   * @param view the view
   * @param isMainHorizontal is the main axis horizontal
   * @return the view's measured size in the main axis
   */
  private int getViewMeasuredSizeMain(View view, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return view.getMeasuredWidth();
    }

    return view.getMeasuredHeight();
  }

  /**
   * Returns the view's measured size in the cross axis. Either width or height.
   *
   * @param view the view
   * @param isMainHorizontal is the main axis horizontal
   * @return the view's measured size in the cross axis
   */
  private int getViewMeasuredSizeCross(View view, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return view.getMeasuredHeight();
    }

    return view.getMeasuredWidth();
  }

  /**
   * Returns the flexItem's size in the main axis. Either width or height.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's size in the main axis
   */
  private int getFlexItemSizeMain(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getWidth();
    }

    return flexItem.getHeight();
  }

  /**
   * Returns the flexItem's size in the cross axis. Either width or height.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's size in the cross axis
   */
  private int getFlexItemSizeCross(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getHeight();
    }

    return flexItem.getWidth();
  }

  /**
   * Returns the flexItem's start margin in the main axis. Either start or top. For the backward
   * compatibility for API level < 17, the horizontal margin is returned using {@link
   * FlexItem#getMarginLeft} (ViewGroup.MarginLayoutParams#getMarginStart isn't available in API
   * level < 17). Thus this method needs to be used with {@link #getFlexItemMarginEndMain} not to
   * misuse the margin in RTL.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's start margin in the main axis
   */
  private int getFlexItemMarginStartMain(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getMarginLeft();
    }

    return flexItem.getMarginTop();
  }

  /**
   * Returns the flexItem's end margin in the main axis. Either end or bottom. For the backward
   * compatibility for API level < 17, the horizontal margin is returned using {@link
   * FlexItem#getMarginRight} (ViewGroup.MarginLayoutParams#getMarginEnd isn't available in API
   * level < 17). Thus this method needs to be used with {@link #getFlexItemMarginStartMain} not to
   * misuse the margin in RTL.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's end margin in the main axis
   */
  private int getFlexItemMarginEndMain(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getMarginRight();
    }

    return flexItem.getMarginBottom();
  }

  /**
   * Returns the flexItem's start margin in the cross axis. Either start or top. For the backward
   * compatibility for API level < 17, the horizontal margin is returned using {@link
   * FlexItem#getMarginLeft} (ViewGroup.MarginLayoutParams#getMarginStart isn't available in API
   * level < 17). Thus this method needs to be used with {@link #getFlexItemMarginEndCross} to not
   * to misuse the margin in RTL.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's start margin in the cross axis
   */
  private int getFlexItemMarginStartCross(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getMarginTop();
    }

    return flexItem.getMarginLeft();
  }

  /**
   * Returns the flexItem's end margin in the cross axis. Either end or bottom. For the backward
   * compatibility for API level < 17, the horizontal margin is returned using {@link
   * FlexItem#getMarginRight} (ViewGroup.MarginLayoutParams#getMarginEnd isn't available in API
   * level < 17). Thus this method needs to be used with {@link #getFlexItemMarginStartCross} to not
   * to misuse the margin in RTL.
   *
   * @param flexItem the flexItem
   * @param isMainHorizontal is the main axis horizontal
   * @return the flexItem's end margin in the cross axis
   */
  private int getFlexItemMarginEndCross(FlexItem flexItem, boolean isMainHorizontal) {
    if (isMainHorizontal) {
      return flexItem.getMarginBottom();
    }

    return flexItem.getMarginRight();
  }

  /**
   * Determine if a wrap is required (add a new flex line).
   *
   * @param view the view being judged if the wrap required
   * @param mode the width or height mode along the main axis direction
   * @param maxSize the max size along the main axis direction
   * @param currentLength the accumulated current length
   * @param childLength the length of a child view which is to be collected to the flex line
   * @param flexItem the LayoutParams for the view being determined whether a new flex line is
   *     needed
   * @return {@code true} if a wrap is required, {@code false} otherwise
   * @see FlexContainer#getFlexWrap()
   * @see FlexContainer#setFlexWrap(int)
   */
  private boolean isWrapRequired(
      View view,
      int mode,
      int maxSize,
      int currentLength,
      int childLength,
      FlexItem flexItem,
      int index,
      int indexInFlexLine) {
    if (flexContainer.getFlexWrap() == FlexWrap.NOWRAP) {
      return false;
    }
    if (flexItem.isWrapBefore()) {
      return true;
    }
    if (mode == View.MeasureSpec.UNSPECIFIED) {
      return false;
    }
    int decorationLength = flexContainer.getDecorationLengthMainAxis(view, index, indexInFlexLine);
    if (decorationLength > 0) {
      childLength += decorationLength;
    }
    return maxSize < currentLength + childLength;
  }

  private boolean isLastFlexItem(int childIndex, int childCount, FlexLine flexLine) {
    return childIndex == childCount - 1 && flexLine.getItemCountNotGone() != 0;
  }

  private void addFlexLine(
      List<FlexLine> flexLines, FlexLine flexLine, int viewIndex, int usedCrossSizeSoFar) {
    flexLine.sumCrossSizeBefore = usedCrossSizeSoFar;
    flexContainer.onNewFlexLineAdded(flexLine);
    flexLine.lastIndex = viewIndex;
    flexLines.add(flexLine);
  }

  /**
   * Checks if the view's width/height don't violate the minimum/maximum size constraints imposed by
   * the {@link FlexItem#getMinWidth()}, {@link FlexItem#getMinHeight()}, {@link
   * FlexItem#getMaxWidth()} and {@link FlexItem#getMaxHeight()} attributes.
   *
   * @param view the view to be checked
   * @param index index of the view
   */
  private void checkSizeConstraints(View view, int index) {
    boolean needsMeasure = false;
    FlexItem flexItem = (FlexItem) view.getLayoutParams();
    int childWidth = view.getMeasuredWidth();
    int childHeight = view.getMeasuredHeight();

    if (childWidth < flexItem.getMinWidth()) {
      needsMeasure = true;
      childWidth = flexItem.getMinWidth();
    } else if (childWidth > flexItem.getMaxWidth()) {
      needsMeasure = true;
      childWidth = flexItem.getMaxWidth();
    }

    if (childHeight < flexItem.getMinHeight()) {
      needsMeasure = true;
      childHeight = flexItem.getMinHeight();
    } else if (childHeight > flexItem.getMaxHeight()) {
      needsMeasure = true;
      childHeight = flexItem.getMaxHeight();
    }
    if (needsMeasure) {
      int widthSpec = View.MeasureSpec.makeMeasureSpec(childWidth, View.MeasureSpec.EXACTLY);
      int heightSpec = View.MeasureSpec.makeMeasureSpec(childHeight, View.MeasureSpec.EXACTLY);
      view.measure(widthSpec, heightSpec);
      flexContainer.updateViewCache(index, view);
    }
  }

  /** @see #determineMainSize(int, int, int) */
  void determineMainSize(int widthMeasureSpec, int heightMeasureSpec) {
    determineMainSize(widthMeasureSpec, heightMeasureSpec, 0);
  }

  /**
   * Determine the main size by expanding (shrinking if negative remaining free space is given) an
   * individual child in each flex line if any children's mFlexGrow (or mFlexShrink if remaining
   * space is negative) properties are set to non-zero.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   */
  private void determineMainSize(int widthMeasureSpec, int heightMeasureSpec, int fromIndex) {
    ensureChildrenFrozen(flexContainer.getFlexItemCount());
    if (fromIndex >= flexContainer.getFlexItemCount()) {
      return;
    }
    int mainSize;

    int widthMode = View.MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = View.MeasureSpec.getSize(widthMeasureSpec);
    if (widthMode == View.MeasureSpec.EXACTLY) {
      mainSize = widthSize;
    } else {
      mainSize = flexContainer.getLargestMainSize();
    }
    int paddingAlongMainAxis = flexContainer.getPaddingLeft() + flexContainer.getPaddingRight();

    int flexLineIndex = 0;
    List<FlexLine> flexLines = flexContainer.getFlexLinesInternal();
    for (int i = flexLineIndex, size = flexLines.size(); i < size; i++) {
      FlexLine flexLine = flexLines.get(i);
      if (flexLine.mainSize < mainSize) {
        expandFlexItems(heightMeasureSpec, flexLine, mainSize, paddingAlongMainAxis, false);
      } else {
        shrinkFlexItems(heightMeasureSpec, flexLine, mainSize, paddingAlongMainAxis, false);
      }
    }
  }

  private void ensureChildrenFrozen(int size) {
    if (childrenFrozen == null) {
      childrenFrozen = new boolean[Math.max(INITIAL_CAPACITY, size)];
    } else if (childrenFrozen.length < size) {
      int newCapacity = childrenFrozen.length * 2;
      childrenFrozen = new boolean[Math.max(newCapacity, size)];
    } else {
      Arrays.fill(childrenFrozen, false);
    }
  }

  /**
   * Expand the flex items along the main axis based on the individual mFlexGrow attribute.
   *
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Expanded main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   * @see FlexItem#getFlexGrow()
   */
  private void expandFlexItems(
      int heightMeasureSpec,
      FlexLine flexLine,
      int maxMainSize,
      int paddingAlongMainAxis,
      boolean calledRecursively) {
    if (flexLine.motalFlexGrow <= 0 || maxMainSize < flexLine.mainSize) {
      return;
    }
    int sizeBeforeExpand = flexLine.mainSize;
    boolean needsReexpand = false;
    float unitSpace = (maxMainSize - flexLine.mainSize) / flexLine.motalFlexGrow;
    flexLine.mainSize = paddingAlongMainAxis + flexLine.dividerLengthInMainSize;

    // Setting the cross size of the flex line as the temporal value since the cross size of
    // each flex item may be changed from the initial calculation
    // (in the measureHorizontal/measureVertical method) even this method is part of the main
    // size determination.
    // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
    // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
    // direction to enclose its content (in the measureHorizontal method), but
    // the width will be expanded in this method. In that case, the height needs to be measured
    // again with the expanded width.
    int largestCrossSize = 0;
    if (!calledRecursively) {
      flexLine.crossSize = Integer.MIN_VALUE;
    }
    float accumulatedRoundError = 0;
    for (int i = 0; i < flexLine.itemCount; i++) {
      int index = flexLine.firstIndex + i;
      View child = flexContainer.getReorderedFlexItemAt(index);
      if (child == null || child.getVisibility() == View.GONE) {
        continue;
      }
      FlexItem flexItem = (FlexItem) child.getLayoutParams();
      // The direction of the main axis is horizontal

      int childMeasuredWidth = child.getMeasuredWidth();
      int childMeasuredHeight = child.getMeasuredHeight();
      if (!childrenFrozen[index] && flexItem.getFlexGrow() > 0f) {
        float rawCalculatedWidth = childMeasuredWidth + unitSpace * flexItem.getFlexGrow();
        if (i == flexLine.itemCount - 1) {
          rawCalculatedWidth += accumulatedRoundError;
          accumulatedRoundError = 0;
        }
        int newWidth = Math.round(rawCalculatedWidth);
        if (newWidth > flexItem.getMaxWidth()) {
          // This means the child can't expand beyond the value of the mMaxWidth
          // attribute.
          // To adjust the flex line length to the size of maxMainSize, remaining
          // positive free space needs to be re-distributed to other flex items
          // (children views). In that case, invoke this method again with the same
          // fromIndex.
          needsReexpand = true;
          newWidth = flexItem.getMaxWidth();
          childrenFrozen[index] = true;
          flexLine.motalFlexGrow -= flexItem.getFlexGrow();
        } else {
          accumulatedRoundError += (rawCalculatedWidth - newWidth);
          if (accumulatedRoundError > 1.0) {
            newWidth += 1;
            accumulatedRoundError -= 1.0f;
          } else if (accumulatedRoundError < -1.0) {
            newWidth -= 1;
            accumulatedRoundError += 1.0f;
          }
        }
        int childHeightMeasureSpec =
            getChildHeightMeasureSpecInternal(
                heightMeasureSpec, flexItem, flexLine.sumCrossSizeBefore);
        int childWidthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
        childMeasuredWidth = child.getMeasuredWidth();
        childMeasuredHeight = child.getMeasuredHeight();
        flexContainer.updateViewCache(index, child);
      }
      largestCrossSize =
          Math.max(
              largestCrossSize,
              childMeasuredHeight
                  + flexItem.getMarginTop()
                  + flexItem.getMarginBottom()
                  + flexContainer.getDecorationLengthCrossAxis(child));
      flexLine.mainSize +=
          childMeasuredWidth + flexItem.getMarginLeft() + flexItem.getMarginRight();
      flexLine.crossSize = Math.max(flexLine.crossSize, largestCrossSize);
    }

    if (needsReexpand && sizeBeforeExpand != flexLine.mainSize) {
      // Re-invoke the method with the same flex line to distribute the positive free space
      // that wasn't fully distributed (because of maximum length constraint)
      expandFlexItems(heightMeasureSpec, flexLine, maxMainSize, paddingAlongMainAxis, true);
    }
  }

  /**
   * Shrink the flex items along the main axis based on the individual mFlexShrink attribute.
   *
   * @param heightMeasureSpec the vertical space requirements as imposed by the parent
   * @param flexLine the flex line to which flex items belong
   * @param maxMainSize the maximum main size. Shrank main size will be this size
   * @param paddingAlongMainAxis the padding value along the main axis
   * @param calledRecursively true if this method is called recursively, false otherwise
   * @see FlexItem#getFlexShrink()
   */
  private void shrinkFlexItems(
      int heightMeasureSpec,
      FlexLine flexLine,
      int maxMainSize,
      int paddingAlongMainAxis,
      boolean calledRecursively) {
    int sizeBeforeShrink = flexLine.mainSize;
    if (flexLine.totalFlexShrink <= 0 || maxMainSize > flexLine.mainSize) {
      return;
    }
    boolean needsReshrink = false;
    float unitShrink = (flexLine.mainSize - maxMainSize) / flexLine.totalFlexShrink;
    float accumulatedRoundError = 0;
    flexLine.mainSize = paddingAlongMainAxis + flexLine.dividerLengthInMainSize;

    // Setting the cross size of the flex line as the temporal value since the cross size of
    // each flex item may be changed from the initial calculation
    // (in the measureHorizontal/measureVertical method) even this method is part of the main
    // size determination.
    // E.g. If a TextView's layout_width is set to 0dp, layout_height is set to wrap_content,
    // and layout_flexGrow is set to 1, the TextView is trying to expand to the vertical
    // direction to enclose its content (in the measureHorizontal method), but
    // the width will be expanded in this method. In that case, the height needs to be measured
    // again with the expanded width.
    int largestCrossSize = 0;
    if (!calledRecursively) {
      flexLine.crossSize = Integer.MIN_VALUE;
    }
    for (int i = 0; i < flexLine.itemCount; i++) {
      int index = flexLine.firstIndex + i;
      View child = flexContainer.getReorderedFlexItemAt(index);
      if (child == null || child.getVisibility() == View.GONE) {
        continue;
      }
      FlexItem flexItem = (FlexItem) child.getLayoutParams();
      // The direction of main axis is horizontal

      int childMeasuredWidth = child.getMeasuredWidth();
      int childMeasuredHeight = child.getMeasuredHeight();
      if (!childrenFrozen[index] && flexItem.getFlexShrink() > 0f) {
        float rawCalculatedWidth = childMeasuredWidth - unitShrink * flexItem.getFlexShrink();
        if (i == flexLine.itemCount - 1) {
          rawCalculatedWidth += accumulatedRoundError;
          accumulatedRoundError = 0;
        }
        int newWidth = Math.round(rawCalculatedWidth);
        if (newWidth < flexItem.getMinWidth()) {
          // This means the child doesn't have enough space to distribute the negative
          // free space. To adjust the flex line length down to the maxMainSize,
          // remaining
          // negative free space needs to be re-distributed to other flex items
          // (children views). In that case, invoke this method again with the same
          // fromIndex.
          needsReshrink = true;
          newWidth = flexItem.getMinWidth();
          childrenFrozen[index] = true;
          flexLine.totalFlexShrink -= flexItem.getFlexShrink();
        } else {
          accumulatedRoundError += (rawCalculatedWidth - newWidth);
          if (accumulatedRoundError > 1.0) {
            newWidth += 1;
            accumulatedRoundError -= 1;
          } else if (accumulatedRoundError < -1.0) {
            newWidth -= 1;
            accumulatedRoundError += 1;
          }
        }
        int childHeightMeasureSpec =
            getChildHeightMeasureSpecInternal(
                heightMeasureSpec, flexItem, flexLine.sumCrossSizeBefore);
        int childWidthMeasureSpec =
            View.MeasureSpec.makeMeasureSpec(newWidth, View.MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);

        childMeasuredWidth = child.getMeasuredWidth();
        childMeasuredHeight = child.getMeasuredHeight();
        flexContainer.updateViewCache(index, child);
      }
      largestCrossSize =
          Math.max(
              largestCrossSize,
              childMeasuredHeight
                  + flexItem.getMarginTop()
                  + flexItem.getMarginBottom()
                  + flexContainer.getDecorationLengthCrossAxis(child));
      flexLine.mainSize +=
          childMeasuredWidth + flexItem.getMarginLeft() + flexItem.getMarginRight();
      flexLine.crossSize = Math.max(flexLine.crossSize, largestCrossSize);
    }

    if (needsReshrink && sizeBeforeShrink != flexLine.mainSize) {
      // Re-invoke the method with the same fromIndex to distribute the negative free space
      // that wasn't fully distributed (because some views length were not enough)
      shrinkFlexItems(heightMeasureSpec, flexLine, maxMainSize, paddingAlongMainAxis, true);
    }
  }

  private int getChildHeightMeasureSpecInternal(
      int heightMeasureSpec, FlexItem flexItem, int padding) {
    int childHeightMeasureSpec =
        flexContainer.getChildHeightMeasureSpec(
            heightMeasureSpec,
            flexContainer.getPaddingTop()
                + flexContainer.getPaddingBottom()
                + flexItem.getMarginTop()
                + flexItem.getMarginBottom()
                + padding,
            flexItem.getHeight());
    int childHeight = View.MeasureSpec.getSize(childHeightMeasureSpec);
    if (childHeight > flexItem.getMaxHeight()) {
      childHeightMeasureSpec =
          View.MeasureSpec.makeMeasureSpec(
              flexItem.getMaxHeight(), View.MeasureSpec.getMode(childHeightMeasureSpec));
    } else if (childHeight < flexItem.getMinHeight()) {
      childHeightMeasureSpec =
          View.MeasureSpec.makeMeasureSpec(
              flexItem.getMinHeight(), View.MeasureSpec.getMode(childHeightMeasureSpec));
    }
    return childHeightMeasureSpec;
  }

  /**
   * Determines the cross size (Calculate the length along the cross axis). Expand the cross size
   * only if the height mode is MeasureSpec.EXACTLY, otherwise use the sum of cross sizes of all
   * flex lines.
   *
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   * @param paddingAlongCrossAxis the padding value for the FlexboxLayout along the cross axis
   */
  void determineCrossSize(int heightMeasureSpec, int paddingAlongCrossAxis) {
    // The MeasureSpec mode along the cross axis
    int mode = View.MeasureSpec.getMode(heightMeasureSpec);
    // The MeasureSpec size along the cross axis
    int size = View.MeasureSpec.getSize(heightMeasureSpec);
    List<FlexLine> flexLines = flexContainer.getFlexLinesInternal();
    if (mode == View.MeasureSpec.EXACTLY) {
      int totalCrossSize = flexContainer.getSumOfCrossSize() + paddingAlongCrossAxis;
      if (flexLines.size() == 1) {
        flexLines.get(0).crossSize = size - paddingAlongCrossAxis;
        // alignContent property is valid only if the Flexbox has at least two lines
      } else if (flexLines.size() >= 2) {
        if (totalCrossSize < size) {
          float freeSpaceUnit = (size - totalCrossSize) / (float) flexLines.size();
          float accumulatedError = 0;
          for (int i = 0, flexLinesSize = flexLines.size(); i < flexLinesSize; i++) {
            FlexLine flexLine = flexLines.get(i);
            float newCrossSizeAsFloat = flexLine.crossSize + freeSpaceUnit;
            if (i == flexLines.size() - 1) {
              newCrossSizeAsFloat += accumulatedError;
              accumulatedError = 0;
            }
            int newCrossSize = Math.round(newCrossSizeAsFloat);
            accumulatedError += (newCrossSizeAsFloat - newCrossSize);
            if (accumulatedError > 1) {
              newCrossSize += 1;
              accumulatedError -= 1;
            } else if (accumulatedError < -1) {
              newCrossSize -= 1;
              accumulatedError += 1;
            }
            flexLine.crossSize = newCrossSize;
          }
        }
      }
    }
  }

  void stretchViews() {
    if (0 >= flexContainer.getFlexItemCount()) {
      return;
    }
    int flexLineIndex = 0;
    List<FlexLine> flexLines = flexContainer.getFlexLinesInternal();
    for (int i = flexLineIndex, size = flexLines.size(); i < size; i++) {
      FlexLine flexLine = flexLines.get(i);
      for (int j = 0, itemCount = flexLine.itemCount; j < itemCount; j++) {
        int viewIndex = flexLine.firstIndex + j;
        if (j >= flexContainer.getFlexItemCount()) {
          continue;
        }
        View view = flexContainer.getReorderedFlexItemAt(viewIndex);
        if (view == null || view.getVisibility() == View.GONE) {
          continue;
        }
        stretchViewVertically(view, flexLine.crossSize, viewIndex);
      }
    }
  }

  /**
   * Expand the view vertically to the size of the crossSize (considering the view margins)
   *
   * @param view the View to be stretched
   * @param crossSize the cross size
   * @param index the index of the view
   */
  private void stretchViewVertically(View view, int crossSize, int index) {
    FlexItem flexItem = (FlexItem) view.getLayoutParams();
    int newHeight =
        crossSize
            - flexItem.getMarginTop()
            - flexItem.getMarginBottom()
            - flexContainer.getDecorationLengthCrossAxis(view);
    newHeight = Math.max(newHeight, flexItem.getMinHeight());
    newHeight = Math.min(newHeight, flexItem.getMaxHeight());
    int measuredWidth = view.getMeasuredWidth();
    int childWidthSpec = View.MeasureSpec.makeMeasureSpec(measuredWidth, View.MeasureSpec.EXACTLY);

    int childHeightSpec = View.MeasureSpec.makeMeasureSpec(newHeight, View.MeasureSpec.EXACTLY);
    view.measure(childWidthSpec, childHeightSpec);

    flexContainer.updateViewCache(index, view);
  }

  /**
   * Place a single View
   *
   * @param view the View to be placed
   * @param left the left position of the View, which the View's margin is already taken into
   *     account
   * @param top the top position of the flex line where the View belongs to. The actual View's top
   *     position is shifted depending on the flexWrap and alignItems attributes
   * @param right the right position of the View, which the View's margin is already taken into
   *     account
   * @param bottom the bottom position of the flex line where the View belongs to. The actual View's
   *     bottom position is shifted depending on the flexWrap and alignItems attributes
   */
  void layoutSingleChildHorizontal(View view, int left, int top, int right, int bottom) {
    FlexItem flexItem = (FlexItem) view.getLayoutParams();
    view.layout(left, top + flexItem.getMarginTop(), right, bottom + flexItem.getMarginTop());
  }

  /**
   * A class that is used for calculating the view order which view's indices and order properties
   * from Flexbox are taken into account.
   */
  private static class Order implements Comparable<Order> {

    /** {@link View}'s index */
    int index;

    /** order property in the Flexbox */
    int order;

    @Override
    public int compareTo(@NonNull Order another) {
      if (order != another.order) {
        return order - another.order;
      }
      return index - another.index;
    }

    @Override
    public String toString() {
      return "Order{" + "order=" + order + ", index=" + index + '}';
    }
  }

  static class FlexLinesResult {

    List<FlexLine> flexLines;

    int childState;

    void reset() {
      flexLines = null;
      childState = 0;
    }
  }
}
