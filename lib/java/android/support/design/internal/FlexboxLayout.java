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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.util.SparseIntArray;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.List;

/**
 * A layout that arranges its children in a way its attributes can be specified like the CSS
 * Flexible Box Layout Module. This class extends the {@link ViewGroup} like other layout classes
 * such as {@link LinearLayout} or {@link RelativeLayout}, the attributes can be specified from a
 * layout XML or from code.
 *
 * <p>The supported attributes that you can use are:
 *
 * <ul>
 *   <li>{@code flexDirection}
 *   <li>{@code flexWrap}
 *   <li>{@code justifyContent}
 *   <li>{@code alignItems}
 *   <li>{@code alignContent}
 *   <li>{@code showDivider}
 *   <li>{@code showDividerHorizontal}
 *   <li>{@code showDividerVertical}
 *   <li>{@code dividerDrawable}
 *   <li>{@code dividerDrawableHorizontal}
 *   <li>{@code dividerDrawableVertical}
 * </ul>
 *
 * for the FlexboxLayout.
 *
 * <p>And for the children of the FlexboxLayout, you can use:
 *
 * <ul>
 *   <li>{@code layout_order}
 *   <li>{@code layout_flexGrow}
 *   <li>{@code layout_flexShrink}
 *   <li>{@code layout_flexBasisPercent}
 *   <li>{@code layout_alignSelf}
 *   <li>{@code layout_minWidth}
 *   <li>{@code layout_minHeight}
 *   <li>{@code layout_maxWidth}
 *   <li>{@code layout_maxHeight}
 *   <li>{@code layout_wrapBefore}
 * </ul>
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class FlexboxLayout extends ViewGroup implements FlexContainer {

  /**
   * The current value of the {@link FlexWrap}, the default value is {@link FlexWrap#NOWRAP}.
   *
   * @see FlexWrap
   */
  private int flexWrap;

  /**
   * The int definition to be used as the arguments for the {@link #setShowDivider(int)}, {@link
   * #setShowDividerHorizontal(int)} or {@link #setShowDividerVertical(int)}. One or more of the
   * values (such as {@link #SHOW_DIVIDER_BEGINNING} | {@link #SHOW_DIVIDER_MIDDLE}) can be passed
   * to those set methods.
   */
  @IntDef(
    flag = true,
    value = {SHOW_DIVIDER_NONE, SHOW_DIVIDER_BEGINNING, SHOW_DIVIDER_MIDDLE, SHOW_DIVIDER_END}
  )
  @Retention(RetentionPolicy.SOURCE)
  @SuppressWarnings("WeakerAccess")
  public @interface DividerMode {}

  /** Constant to show no dividers */
  public static final int SHOW_DIVIDER_NONE = 0;

  /** Constant to show a divider at the beginning of the flex lines (or flex items). */
  public static final int SHOW_DIVIDER_BEGINNING = 1;

  /** Constant to show dividers between flex lines or flex items. */
  public static final int SHOW_DIVIDER_MIDDLE = 1 << 1;

  /** Constant to show a divider at the end of the flex lines or flex items. */
  public static final int SHOW_DIVIDER_END = 1 << 2;

  /** The drawable to be drawn for the horizontal dividers. */
  @Nullable private Drawable dividerDrawableHorizontal;

  /** The drawable to be drawn for the vertical dividers. */
  @Nullable private Drawable dividerDrawableVertical;

  /**
   * Indicates the divider mode for the {@link #dividerDrawableHorizontal}. The value needs to be
   * the combination of the value of {@link #SHOW_DIVIDER_NONE}, {@link #SHOW_DIVIDER_BEGINNING},
   * {@link #SHOW_DIVIDER_MIDDLE} and {@link #SHOW_DIVIDER_END}
   */
  private int showDividerHorizontal;

  /**
   * Indicates the divider mode for the {@link #dividerDrawableVertical}. The value needs to be the
   * combination of the value of {@link #SHOW_DIVIDER_NONE}, {@link #SHOW_DIVIDER_BEGINNING}, {@link
   * #SHOW_DIVIDER_MIDDLE} and {@link #SHOW_DIVIDER_END}
   */
  private int showDividerVertical;

  /** The height of the {@link #dividerDrawableHorizontal}. */
  private int dividerHorizontalHeight;

  /** The width of the {@link #dividerDrawableVertical}. */
  private int dividerVerticalWidth;

  /**
   * Holds reordered indices, which {@link FlexItem#getOrder()} parameters are taken into account
   */
  private int[] reorderedIndices;

  /**
   * Caches the {@link FlexItem#getOrder()} attributes for children views. Key: the index of the
   * view reordered indices using the {@link FlexItem#getOrder()} isn't taken into account) Value:
   * the value for the order attribute
   */
  private SparseIntArray orderCache;

  private FlexboxHelper flexboxHelper = new FlexboxHelper(this);

  private List<FlexLine> flexLines = new ArrayList<>();

  /**
   * Used for receiving the calculation of the flex results to avoid creating a new instance every
   * time flex lines are calculated.
   */
  private FlexboxHelper.FlexLinesResult flexLinesResult = new FlexboxHelper.FlexLinesResult();

  public FlexboxLayout(Context context) {
    this(context, null);
  }

  public FlexboxLayout(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public FlexboxLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (orderCache == null) {
      orderCache = new SparseIntArray(getChildCount());
    }
    if (flexboxHelper.isOrderChangedFromLastMeasurement(orderCache)) {
      reorderedIndices = flexboxHelper.createReorderedIndices(orderCache);
    }

    // TODO: Only calculate the children views which are affected from the last measure.
    measureHorizontal(widthMeasureSpec, heightMeasureSpec);
  }

  @Override
  public int getFlexItemCount() {
    return getChildCount();
  }

  @Override
  public View getFlexItemAt(int index) {
    return getChildAt(index);
  }

  /**
   * Returns a View, which is reordered by taking {@link LayoutParams#order} parameters into
   * account.
   *
   * @param index the index of the view
   * @return the reordered view, which {@link LayoutParams@order} is taken into account. If the
   *     index is negative or out of bounds of the number of contained views, returns {@code null}.
   */
  public View getReorderedChildAt(int index) {
    if (index < 0 || index >= reorderedIndices.length) {
      return null;
    }
    return getChildAt(reorderedIndices[index]);
  }

  @Override
  public View getReorderedFlexItemAt(int index) {
    return getReorderedChildAt(index);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    if (orderCache == null) {
      orderCache = new SparseIntArray(getChildCount());
    }
    // Create an array for the reordered indices before the View is added in the parent
    // ViewGroup since otherwise reordered indices won't be in effect before the
    // FlexboxLayout's onMeasure is called.
    // Because requestLayout is requested in the super.addView method.
    reorderedIndices = flexboxHelper.createReorderedIndices(child, index, params, orderCache);
    super.addView(child, index, params);
  }

  /**
   * Sub method for {@link #onMeasure(int, int)}, when the main axis direction is horizontal (either
   * left to right or right to left).
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   * @see #onMeasure(int, int)
   * @see #setFlexWrap(int)
   */
  private void measureHorizontal(int widthMeasureSpec, int heightMeasureSpec) {
    flexLines.clear();

    flexLinesResult.reset();
    flexboxHelper.calculateHorizontalFlexLines(
        flexLinesResult, widthMeasureSpec, heightMeasureSpec);
    flexLines = flexLinesResult.flexLines;

    flexboxHelper.determineMainSize(widthMeasureSpec, heightMeasureSpec);

    flexboxHelper.determineCrossSize(heightMeasureSpec, getPaddingTop() + getPaddingBottom());
    // Now cross size for each flex line is determined.
    // Expand the views if alignItems (or mAlignSelf in each child view) is set to stretch
    flexboxHelper.stretchViews();
    setMeasuredDimensionForFlex(widthMeasureSpec, heightMeasureSpec, flexLinesResult.childState);
  }

  /**
   * Set this FlexboxLayouts' width and height depending on the calculated size of main axis and
   * cross axis.
   *
   * @param widthMeasureSpec horizontal space requirements as imposed by the parent
   * @param heightMeasureSpec vertical space requirements as imposed by the parent
   * @param childState the child state of the View
   */
  private void setMeasuredDimensionForFlex(
      int widthMeasureSpec, int heightMeasureSpec, int childState) {
    int widthMode = MeasureSpec.getMode(widthMeasureSpec);
    int widthSize = MeasureSpec.getSize(widthMeasureSpec);
    int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    int heightSize = MeasureSpec.getSize(heightMeasureSpec);
    int calculatedMaxHeight = getSumOfCrossSize() + getPaddingTop() + getPaddingBottom();
    int calculatedMaxWidth = getLargestMainSize();

    int widthSizeAndState;
    switch (widthMode) {
      case MeasureSpec.EXACTLY:
        if (widthSize < calculatedMaxWidth) {
          childState = View.combineMeasuredStates(childState, View.MEASURED_STATE_TOO_SMALL);
        }
        widthSizeAndState = View.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
        break;
      case MeasureSpec.AT_MOST:
        {
          if (widthSize < calculatedMaxWidth) {
            childState = View.combineMeasuredStates(childState, View.MEASURED_STATE_TOO_SMALL);
          } else {
            widthSize = calculatedMaxWidth;
          }
          widthSizeAndState = View.resolveSizeAndState(widthSize, widthMeasureSpec, childState);
          break;
        }
      case MeasureSpec.UNSPECIFIED:
        {
          widthSizeAndState =
              View.resolveSizeAndState(calculatedMaxWidth, widthMeasureSpec, childState);
          break;
        }
      default:
        throw new IllegalStateException("Unknown width mode is set: " + widthMode);
    }
    int heightSizeAndState;
    switch (heightMode) {
      case MeasureSpec.EXACTLY:
        if (heightSize < calculatedMaxHeight) {
          childState =
              View.combineMeasuredStates(
                  childState, View.MEASURED_STATE_TOO_SMALL >> View.MEASURED_HEIGHT_STATE_SHIFT);
        }
        heightSizeAndState = View.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
        break;
      case MeasureSpec.AT_MOST:
        {
          if (heightSize < calculatedMaxHeight) {
            childState =
                View.combineMeasuredStates(
                    childState, View.MEASURED_STATE_TOO_SMALL >> View.MEASURED_HEIGHT_STATE_SHIFT);
          } else {
            heightSize = calculatedMaxHeight;
          }
          heightSizeAndState = View.resolveSizeAndState(heightSize, heightMeasureSpec, childState);
          break;
        }
      case MeasureSpec.UNSPECIFIED:
        {
          heightSizeAndState =
              View.resolveSizeAndState(calculatedMaxHeight, heightMeasureSpec, childState);
          break;
        }
      default:
        throw new IllegalStateException("Unknown height mode is set: " + heightMode);
    }
    setMeasuredDimension(widthSizeAndState, heightSizeAndState);
  }

  @Override
  public int getLargestMainSize() {
    int largestSize = Integer.MIN_VALUE;
    for (FlexLine flexLine : flexLines) {
      largestSize = Math.max(largestSize, flexLine.mainSize);
    }
    return largestSize;
  }

  @Override
  public int getSumOfCrossSize() {
    int sum = 0;
    for (int i = 0, size = flexLines.size(); i < size; i++) {
      FlexLine flexLine = flexLines.get(i);

      // Judge if the beginning or middle dividers are required
      if (hasDividerBeforeFlexLine(i)) {
        if (isMainAxisDirectionHorizontal()) {
          sum += dividerHorizontalHeight;
        } else {
          sum += dividerVerticalWidth;
        }
      }

      // Judge if the end divider is required
      if (hasEndDividerAfterFlexLine(i)) {
        if (isMainAxisDirectionHorizontal()) {
          sum += dividerHorizontalHeight;
        } else {
          sum += dividerVerticalWidth;
        }
      }
      sum += flexLine.crossSize;
    }
    return sum;
  }

  @Override
  public boolean isMainAxisDirectionHorizontal() {
    return true;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    int layoutDirection = ViewCompat.getLayoutDirection(this);
    boolean isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
    layoutHorizontal(isRtl, left, right);
  }

  /**
   * Sub method for {@link #onLayout(boolean, int, int, int, int)}
   *
   * @param isRtl {@code true} if the horizontal layout direction is right to left, {@code false}
   *     otherwise.
   * @param left the left position of this View
   * @param right the right position of this View
   * @see #getFlexWrap()
   * @see #setFlexWrap(int)
   */
  private void layoutHorizontal(boolean isRtl, int left, int right) {
    int paddingLeft = getPaddingLeft();
    int paddingRight = getPaddingRight();
    float childLeft;

    int width = right - left;
    // childTop is used to align the vertical position of the children views.
    int childTop = getPaddingTop();

    // Used only for RTL layout
    float childRight;
    for (int i = 0, size = flexLines.size(); i < size; i++) {
      FlexLine flexLine = flexLines.get(i);
      if (hasDividerBeforeFlexLine(i)) {
        childTop += dividerHorizontalHeight;
      }
      float spaceBetweenItem = 0f;
      childLeft = paddingLeft;
      childRight = width - paddingRight;
      spaceBetweenItem = Math.max(spaceBetweenItem, 0);

      for (int j = 0; j < flexLine.itemCount; j++) {
        int index = flexLine.firstIndex + j;
        View child = getReorderedChildAt(index);
        if (child == null || child.getVisibility() == View.GONE) {
          continue;
        }
        LayoutParams lp = ((LayoutParams) child.getLayoutParams());
        childLeft += lp.leftMargin;
        childRight -= lp.rightMargin;
        int beforeDividerLength = 0;
        int endDividerLength = 0;
        if (hasDividerBeforeChildAtAlongMainAxis(index, j)) {
          beforeDividerLength = dividerVerticalWidth;
          childLeft += beforeDividerLength;
          childRight -= beforeDividerLength;
        }
        if (j == flexLine.itemCount - 1 && (showDividerVertical & SHOW_DIVIDER_END) > 0) {
          endDividerLength = dividerVerticalWidth;
        }

        if (isRtl) {
          flexboxHelper.layoutSingleChildHorizontal(
              child,
              Math.round(childRight) - child.getMeasuredWidth(),
              childTop,
              Math.round(childRight),
              childTop + child.getMeasuredHeight());
        } else {
          flexboxHelper.layoutSingleChildHorizontal(
              child,
              Math.round(childLeft),
              childTop,
              Math.round(childLeft) + child.getMeasuredWidth(),
              childTop + child.getMeasuredHeight());
        }
        childLeft += child.getMeasuredWidth() + spaceBetweenItem + lp.rightMargin;
        childRight -= child.getMeasuredWidth() + spaceBetweenItem + lp.leftMargin;

        if (isRtl) {
          flexLine.updatePositionFromView(
              child, /*leftDecoration*/
              endDividerLength,
              0,
              /*rightDecoration*/ beforeDividerLength,
              0);
        } else {
          flexLine.updatePositionFromView(
              child, /*leftDecoration*/
              beforeDividerLength,
              0,
              /*rightDecoration*/ endDividerLength,
              0);
        }
      }
      childTop += flexLine.crossSize;
    }
  }

  @Override
  protected void onDraw(Canvas canvas) {
    if (dividerDrawableVertical == null && dividerDrawableHorizontal == null) {
      return;
    }
    if (showDividerHorizontal == SHOW_DIVIDER_NONE && showDividerVertical == SHOW_DIVIDER_NONE) {
      return;
    }

    int layoutDirection = ViewCompat.getLayoutDirection(this);
    boolean isRtl = layoutDirection == ViewCompat.LAYOUT_DIRECTION_RTL;
    drawDividersHorizontal(canvas, isRtl);
  }

  /**
   * Sub method for {@link #onDraw(Canvas)}
   *
   * @param canvas the canvas on which the background will be drawn
   * @param isRtl {@code true} when the horizontal layout direction is right to left, {@code false}
   *     otherwise
   */
  private void drawDividersHorizontal(Canvas canvas, boolean isRtl) {
    int paddingLeft = getPaddingLeft();
    int paddingRight = getPaddingRight();
    int horizontalDividerLength = Math.max(0, getWidth() - paddingRight - paddingLeft);
    for (int i = 0, size = flexLines.size(); i < size; i++) {
      FlexLine flexLine = flexLines.get(i);
      for (int j = 0; j < flexLine.itemCount; j++) {
        int viewIndex = flexLine.firstIndex + j;
        View view = getReorderedChildAt(viewIndex);
        if (view == null || view.getVisibility() == View.GONE) {
          continue;
        }
        LayoutParams lp = (LayoutParams) view.getLayoutParams();

        // Judge if the beginning or middle divider is needed
        if (hasDividerBeforeChildAtAlongMainAxis(viewIndex, j)) {
          int dividerLeft;
          if (isRtl) {
            dividerLeft = view.getRight() + lp.rightMargin;
          } else {
            dividerLeft = view.getLeft() - lp.leftMargin - dividerVerticalWidth;
          }

          drawVerticalDivider(canvas, dividerLeft, flexLine.top, flexLine.crossSize);
        }

        // Judge if the end divider is needed
        if (j == flexLine.itemCount - 1) {
          if ((showDividerVertical & SHOW_DIVIDER_END) > 0) {
            int dividerLeft;
            if (isRtl) {
              dividerLeft = view.getLeft() - lp.leftMargin - dividerVerticalWidth;
            } else {
              dividerLeft = view.getRight() + lp.rightMargin;
            }

            drawVerticalDivider(canvas, dividerLeft, flexLine.top, flexLine.crossSize);
          }
        }
      }

      // Judge if the beginning or middle dividers are needed before the flex line
      if (hasDividerBeforeFlexLine(i)) {
        int horizontalDividerTop = flexLine.top - dividerHorizontalHeight;
        drawHorizontalDivider(canvas, paddingLeft, horizontalDividerTop, horizontalDividerLength);
      }
      // Judge if the end divider is needed before the flex line
      if (hasEndDividerAfterFlexLine(i)) {
        if ((showDividerHorizontal & SHOW_DIVIDER_END) > 0) {
          int horizontalDividerTop = flexLine.bottom;
          drawHorizontalDivider(canvas, paddingLeft, horizontalDividerTop, horizontalDividerLength);
        }
      }
    }
  }

  private void drawVerticalDivider(Canvas canvas, int left, int top, int length) {
    if (dividerDrawableVertical == null) {
      return;
    }
    dividerDrawableVertical.setBounds(left, top, left + dividerVerticalWidth, top + length);
    dividerDrawableVertical.draw(canvas);
  }

  private void drawHorizontalDivider(Canvas canvas, int left, int top, int length) {
    if (dividerDrawableHorizontal == null) {
      return;
    }
    dividerDrawableHorizontal.setBounds(left, top, left + length, top + dividerHorizontalHeight);
    dividerDrawableHorizontal.draw(canvas);
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof FlexboxLayout.LayoutParams;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new FlexboxLayout.LayoutParams(getContext(), attrs);
  }

  @Override
  protected ViewGroup.LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    if (lp instanceof FlexboxLayout.LayoutParams) {
      return new FlexboxLayout.LayoutParams((FlexboxLayout.LayoutParams) lp);
    } else if (lp instanceof MarginLayoutParams) {
      return new FlexboxLayout.LayoutParams((MarginLayoutParams) lp);
    }
    return new LayoutParams(lp);
  }

  @FlexWrap
  @Override
  public int getFlexWrap() {
    return flexWrap;
  }

  @Override
  public void setFlexWrap(@FlexWrap int flexWrap) {
    if (this.flexWrap != flexWrap) {
      this.flexWrap = flexWrap;
      requestLayout();
    }
  }

  /**
   * @return the flex lines composing this flex container. This method returns a copy of the
   *     original list excluding a dummy flex line (flex line that doesn't have any flex items in it
   *     but used for the alignment along the cross axis). Thus any changes of the returned list are
   *     not reflected to the original list.
   */
  @Override
  public List<FlexLine> getFlexLines() {
    List<FlexLine> result = new ArrayList<>(flexLines.size());
    for (FlexLine flexLine : flexLines) {
      if (flexLine.getItemCountNotGone() == 0) {
        continue;
      }
      result.add(flexLine);
    }
    return result;
  }

  @Override
  public int getDecorationLengthMainAxis(View view, int index, int indexInFlexLine) {
    int decorationLength = 0;
    if (isMainAxisDirectionHorizontal()) {
      if (hasDividerBeforeChildAtAlongMainAxis(index, indexInFlexLine)) {
        decorationLength += dividerVerticalWidth;
      }
      if ((showDividerVertical & SHOW_DIVIDER_END) > 0) {
        decorationLength += dividerVerticalWidth;
      }
    } else {
      if (hasDividerBeforeChildAtAlongMainAxis(index, indexInFlexLine)) {
        decorationLength += dividerHorizontalHeight;
      }
      if ((showDividerHorizontal & SHOW_DIVIDER_END) > 0) {
        decorationLength += dividerHorizontalHeight;
      }
    }
    return decorationLength;
  }

  @Override
  public int getDecorationLengthCrossAxis(View view) {
    // Decoration along the cross axis for an individual view is not supported in the
    // FlexboxLayout.
    return 0;
  }

  @Override
  public void onNewFlexLineAdded(FlexLine flexLine) {
    // The size of the end divider isn't added until the flexLine is added to the flex container
    // take the divider width (or height) into account when adding the flex line.
    if (isMainAxisDirectionHorizontal()) {
      if ((showDividerVertical & SHOW_DIVIDER_END) > 0) {
        flexLine.mainSize += dividerVerticalWidth;
        flexLine.dividerLengthInMainSize += dividerVerticalWidth;
      }
    } else {
      if ((showDividerHorizontal & SHOW_DIVIDER_END) > 0) {
        flexLine.mainSize += dividerHorizontalHeight;
        flexLine.dividerLengthInMainSize += dividerHorizontalHeight;
      }
    }
  }

  @Override
  public int getChildWidthMeasureSpec(int widthSpec, int padding, int childDimension) {
    return getChildMeasureSpec(widthSpec, padding, childDimension);
  }

  @Override
  public int getChildHeightMeasureSpec(int heightSpec, int padding, int childDimension) {
    return getChildMeasureSpec(heightSpec, padding, childDimension);
  }

  @Override
  public void onNewFlexItemAdded(View view, int index, int indexInFlexLine, FlexLine flexLine) {
    // Check if the beginning or middle divider is required for the flex item
    if (hasDividerBeforeChildAtAlongMainAxis(index, indexInFlexLine)) {
      if (isMainAxisDirectionHorizontal()) {
        flexLine.mainSize += dividerVerticalWidth;
        flexLine.dividerLengthInMainSize += dividerVerticalWidth;
      } else {
        flexLine.mainSize += dividerHorizontalHeight;
        flexLine.dividerLengthInMainSize += dividerHorizontalHeight;
      }
    }
  }

  @Override
  public void setFlexLines(List<FlexLine> flexLines) {
    this.flexLines = flexLines;
  }

  @Override
  public List<FlexLine> getFlexLinesInternal() {
    return flexLines;
  }

  @Override
  public void updateViewCache(int position, View view) {
    // No op
  }

  /**
   * @return the horizontal divider drawable that will divide each item.
   * @see #setDividerDrawable(Drawable)
   * @see #setDividerDrawableHorizontal(Drawable)
   */
  @Nullable
  @SuppressWarnings("UnusedDeclaration")
  public Drawable getDividerDrawableHorizontal() {
    return dividerDrawableHorizontal;
  }

  /**
   * @return the vertical divider drawable that will divide each item.
   * @see #setDividerDrawable(Drawable)
   * @see #setDividerDrawableVertical(Drawable)
   */
  @Nullable
  @SuppressWarnings("UnusedDeclaration")
  public Drawable getDividerDrawableVertical() {
    return dividerDrawableVertical;
  }

  /**
   * Set a drawable to be used as a divider between items. The drawable is used for both horizontal
   * and vertical dividers.
   *
   * @param divider Drawable that will divide each item for both horizontally and vertically.
   * @see #setShowDivider(int)
   */
  public void setDividerDrawable(Drawable divider) {
    setDividerDrawableHorizontal(divider);
    setDividerDrawableVertical(divider);
  }

  /**
   * Set a drawable to be used as a horizontal divider between items.
   *
   * @param divider Drawable that will divide each item.
   * @see #setDividerDrawable(Drawable)
   * @see #setShowDivider(int)
   * @see #setShowDividerHorizontal(int)
   */
  public void setDividerDrawableHorizontal(@Nullable Drawable divider) {
    if (divider == dividerDrawableHorizontal) {
      return;
    }
    dividerDrawableHorizontal = divider;
    if (divider != null) {
      dividerHorizontalHeight = divider.getIntrinsicHeight();
    } else {
      dividerHorizontalHeight = 0;
    }
    setWillNotDrawFlag();
    requestLayout();
  }

  /**
   * Set a drawable to be used as a vertical divider between items.
   *
   * @param divider Drawable that will divide each item.
   * @see #setDividerDrawable(Drawable)
   * @see #setShowDivider(int)
   * @see #setShowDividerVertical(int)
   */
  public void setDividerDrawableVertical(@Nullable Drawable divider) {
    if (divider == dividerDrawableVertical) {
      return;
    }
    dividerDrawableVertical = divider;
    if (divider != null) {
      dividerVerticalWidth = divider.getIntrinsicWidth();
    } else {
      dividerVerticalWidth = 0;
    }
    setWillNotDrawFlag();
    requestLayout();
  }

  /**
   * Set how dividers should be shown between items in this layout. This method sets the divider
   * mode for both horizontally and vertically.
   *
   * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING}, {@link
   *     #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END}, or {@link #SHOW_DIVIDER_NONE} to show
   *     no dividers.
   * @see #setShowDividerVertical(int)
   * @see #setShowDividerHorizontal(int)
   */
  public void setShowDivider(@DividerMode int dividerMode) {
    setShowDividerVertical(dividerMode);
    setShowDividerHorizontal(dividerMode);
  }

  /**
   * Set how vertical dividers should be shown between items in this layout
   *
   * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING}, {@link
   *     #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END}, or {@link #SHOW_DIVIDER_NONE} to show
   *     no dividers.
   * @see #setShowDivider(int)
   */
  public void setShowDividerVertical(@DividerMode int dividerMode) {
    if (dividerMode != showDividerVertical) {
      showDividerVertical = dividerMode;
      requestLayout();
    }
  }

  /**
   * Set how horizontal dividers should be shown between items in this layout.
   *
   * @param dividerMode One or more of {@link #SHOW_DIVIDER_BEGINNING}, {@link
   *     #SHOW_DIVIDER_MIDDLE}, or {@link #SHOW_DIVIDER_END}, or {@link #SHOW_DIVIDER_NONE} to show
   *     no dividers.
   * @see #setShowDivider(int)
   */
  public void setShowDividerHorizontal(@DividerMode int dividerMode) {
    if (dividerMode != showDividerHorizontal) {
      showDividerHorizontal = dividerMode;
      requestLayout();
    }
  }

  private void setWillNotDrawFlag() {
    if (dividerDrawableHorizontal == null && dividerDrawableVertical == null) {
      setWillNotDraw(true);
    } else {
      setWillNotDraw(false);
    }
  }

  /**
   * Check if a divider is needed before the view whose indices are passed as arguments.
   *
   * @param index the absolute index of the view to be judged
   * @param indexInFlexLine the relative index in the flex line where the view belongs
   * @return {@code true} if a divider is needed, {@code false} otherwise
   */
  private boolean hasDividerBeforeChildAtAlongMainAxis(int index, int indexInFlexLine) {
    if (allViewsAreGoneBefore(index, indexInFlexLine)) {
      if (isMainAxisDirectionHorizontal()) {
        return (showDividerVertical & SHOW_DIVIDER_BEGINNING) != 0;
      } else {
        return (showDividerHorizontal & SHOW_DIVIDER_BEGINNING) != 0;
      }
    } else {
      if (isMainAxisDirectionHorizontal()) {
        return (showDividerVertical & SHOW_DIVIDER_MIDDLE) != 0;
      } else {
        return (showDividerHorizontal & SHOW_DIVIDER_MIDDLE) != 0;
      }
    }
  }

  private boolean allViewsAreGoneBefore(int index, int indexInFlexLine) {
    for (int i = 1; i <= indexInFlexLine; i++) {
      View view = getReorderedChildAt(index - i);
      if (view != null && view.getVisibility() != View.GONE) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if a divider is needed before the flex line whose index is passed as an argument.
   *
   * @param flexLineIndex the index of the flex line to be checked
   * @return {@code true} if a divider is needed, {@code false} otherwise
   */
  private boolean hasDividerBeforeFlexLine(int flexLineIndex) {
    if (flexLineIndex < 0 || flexLineIndex >= flexLines.size()) {
      return false;
    }
    if (allFlexLinesAreDummyBefore(flexLineIndex)) {
      if (isMainAxisDirectionHorizontal()) {
        return (showDividerHorizontal & SHOW_DIVIDER_BEGINNING) != 0;
      } else {
        return (showDividerVertical & SHOW_DIVIDER_BEGINNING) != 0;
      }
    } else {
      if (isMainAxisDirectionHorizontal()) {
        return (showDividerHorizontal & SHOW_DIVIDER_MIDDLE) != 0;
      } else {
        return (showDividerVertical & SHOW_DIVIDER_MIDDLE) != 0;
      }
    }
  }

  private boolean allFlexLinesAreDummyBefore(int flexLineIndex) {
    for (int i = 0; i < flexLineIndex; i++) {
      if (flexLines.get(i).getItemCountNotGone() > 0) {
        return false;
      }
    }
    return true;
  }

  /**
   * Check if a end divider is needed after the flex line whose index is passed as an argument.
   *
   * @param flexLineIndex the index of the flex line to be checked
   * @return {@code true} if a divider is needed, {@code false} otherwise
   */
  private boolean hasEndDividerAfterFlexLine(int flexLineIndex) {
    if (flexLineIndex < 0 || flexLineIndex >= flexLines.size()) {
      return false;
    }

    for (int i = flexLineIndex + 1; i < flexLines.size(); i++) {
      if (flexLines.get(i).getItemCountNotGone() > 0) {
        return false;
      }
    }
    if (isMainAxisDirectionHorizontal()) {
      return (showDividerHorizontal & SHOW_DIVIDER_END) != 0;
    } else {
      return (showDividerVertical & SHOW_DIVIDER_END) != 0;
    }
  }

  /**
   * Per child parameters for children views of the {@link FlexboxLayout}.
   *
   * <p>Note that some parent fields (which are not primitive nor a class implements {@link
   * Parcelable}) are not included as the stored/restored fields after this class is
   * serialized/de-serialized as an {@link Parcelable}.
   */
  public static class LayoutParams extends ViewGroup.MarginLayoutParams implements FlexItem {

    /** @see FlexItem#getOrder() */
    private int order = FlexItem.ORDER_DEFAULT;

    /** @see FlexItem#getFlexGrow() */
    private float flexGrow = FlexItem.FLEX_GROW_DEFAULT;

    /** @see FlexItem#getFlexShrink() */
    private float flexShrink = FlexItem.FLEX_SHRINK_DEFAULT;

    /** @see FlexItem#getFlexBasisPercent() */
    private float flexBasisPercent = FlexItem.FLEX_BASIS_PERCENT_DEFAULT;

    /** @see FlexItem#getMinWidth() */
    private int minWidth;

    /** @see FlexItem#getMinHeight() */
    private int minHeight;

    /** @see FlexItem#getMaxWidth() */
    private int maxWidth = MAX_SIZE;

    /** @see FlexItem#getMaxHeight() */
    private int maxHeight = MAX_SIZE;

    /** @see FlexItem#isWrapBefore() */
    private boolean wrapBefore;

    public LayoutParams(Context context, AttributeSet attrs) {
      super(context, attrs);

      TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FlexboxLayout_Layout);
      order = a.getInt(R.styleable.FlexboxLayout_Layout_layout_order, ORDER_DEFAULT);
      flexGrow = a.getFloat(R.styleable.FlexboxLayout_Layout_layout_flexGrow, FLEX_GROW_DEFAULT);
      flexShrink =
          a.getFloat(R.styleable.FlexboxLayout_Layout_layout_flexShrink, FLEX_SHRINK_DEFAULT);
      flexBasisPercent =
          a.getFraction(
              R.styleable.FlexboxLayout_Layout_layout_flexBasisPercent,
              1,
              1,
              FLEX_BASIS_PERCENT_DEFAULT);
      minWidth = a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_minWidth, 0);
      minHeight = a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_minHeight, 0);
      maxWidth =
          a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_maxWidth, MAX_SIZE);
      maxHeight =
          a.getDimensionPixelSize(R.styleable.FlexboxLayout_Layout_layout_maxHeight, MAX_SIZE);
      wrapBefore = a.getBoolean(R.styleable.FlexboxLayout_Layout_layout_wrapBefore, false);
      a.recycle();
    }

    public LayoutParams(LayoutParams source) {
      super(source);

      order = source.order;
      flexGrow = source.flexGrow;
      flexShrink = source.flexShrink;
      flexBasisPercent = source.flexBasisPercent;
      minWidth = source.minWidth;
      minHeight = source.minHeight;
      maxWidth = source.maxWidth;
      maxHeight = source.maxHeight;
      wrapBefore = source.wrapBefore;
    }

    public LayoutParams(ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(int width, int height) {
      super(new ViewGroup.LayoutParams(width, height));
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    protected LayoutParams(Parcel in) {
      // Passing a resolved value to resolve a lint warning
      // height and width are set in this method anyway.
      super(0, 0);
      this.order = in.readInt();
      this.flexGrow = in.readFloat();
      this.flexShrink = in.readFloat();
      this.flexBasisPercent = in.readFloat();
      this.minWidth = in.readInt();
      this.minHeight = in.readInt();
      this.maxWidth = in.readInt();
      this.maxHeight = in.readInt();
      this.wrapBefore = in.readByte() != 0;
      this.bottomMargin = in.readInt();
      this.leftMargin = in.readInt();
      this.rightMargin = in.readInt();
      this.topMargin = in.readInt();
      this.height = in.readInt();
      this.width = in.readInt();
    }

    @Override
    public int getWidth() {
      return width;
    }

    @Override
    public void setWidth(int width) {
      this.width = width;
    }

    @Override
    public int getHeight() {
      return height;
    }

    @Override
    public void setHeight(int height) {
      this.height = height;
    }

    @Override
    public int getOrder() {
      return order;
    }

    @Override
    public void setOrder(int order) {
      this.order = order;
    }

    @Override
    public float getFlexGrow() {
      return flexGrow;
    }

    @Override
    public void setFlexGrow(float flexGrow) {
      this.flexGrow = flexGrow;
    }

    @Override
    public float getFlexShrink() {
      return flexShrink;
    }

    @Override
    public void setFlexShrink(float flexShrink) {
      this.flexShrink = flexShrink;
    }

    @Override
    public int getMinWidth() {
      return minWidth;
    }

    @Override
    public void setMinWidth(int minWidth) {
      this.minWidth = minWidth;
    }

    @Override
    public int getMinHeight() {
      return minHeight;
    }

    @Override
    public void setMinHeight(int minHeight) {
      this.minHeight = minHeight;
    }

    @Override
    public int getMaxWidth() {
      return maxWidth;
    }

    @Override
    public void setMaxWidth(int maxWidth) {
      this.maxWidth = maxWidth;
    }

    @Override
    public int getMaxHeight() {
      return maxHeight;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
      this.maxHeight = maxHeight;
    }

    @Override
    public boolean isWrapBefore() {
      return wrapBefore;
    }

    @Override
    public void setWrapBefore(boolean wrapBefore) {
      this.wrapBefore = wrapBefore;
    }

    @Override
    public float getFlexBasisPercent() {
      return flexBasisPercent;
    }

    @Override
    public void setFlexBasisPercent(float flexBasisPercent) {
      this.flexBasisPercent = flexBasisPercent;
    }

    @Override
    public int getMarginLeft() {
      return leftMargin;
    }

    @Override
    public int getMarginTop() {
      return topMargin;
    }

    @Override
    public int getMarginRight() {
      return rightMargin;
    }

    @Override
    public int getMarginBottom() {
      return bottomMargin;
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(this.order);
      dest.writeFloat(this.flexGrow);
      dest.writeFloat(this.flexShrink);
      dest.writeFloat(this.flexBasisPercent);
      dest.writeInt(this.minWidth);
      dest.writeInt(this.minHeight);
      dest.writeInt(this.maxWidth);
      dest.writeInt(this.maxHeight);
      dest.writeByte(this.wrapBefore ? (byte) 1 : (byte) 0);
      dest.writeInt(this.bottomMargin);
      dest.writeInt(this.leftMargin);
      dest.writeInt(this.rightMargin);
      dest.writeInt(this.topMargin);
      dest.writeInt(this.height);
      dest.writeInt(this.width);
    }

    public static final Parcelable.Creator<LayoutParams> CREATOR =
        new Parcelable.Creator<LayoutParams>() {
          @Override
          public LayoutParams createFromParcel(Parcel source) {
            return new LayoutParams(source);
          }

          @Override
          public LayoutParams[] newArray(int size) {
            return new LayoutParams[size];
          }
        };
  }
}
