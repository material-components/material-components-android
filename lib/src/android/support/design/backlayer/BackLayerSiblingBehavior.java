/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.support.design.backlayer;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;

/**
 * Behavior to apply to the content view when using a BackLayerLayout.
 *
 * <p>Using this behavior requires **exactly** one sibling view of type {@link BackLayerLayout}
 * which will be used to calculate the measurements and positions for the content layer view.
 *
 * TODO: Intercept touch events when the BackLayerLayout is expanded
 */
public class BackLayerSiblingBehavior extends Behavior<View> {

  /**
   * The back layer for this content layer.
   */
  private BackLayerLayout backLayerLayout = null;

  public BackLayerSiblingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    if (dependency instanceof BackLayerLayout) {
      if (backLayerLayout != null && dependency != backLayerLayout) {
        throw new IllegalStateException(
            "There is more than one BackLayerLayout in a single CoordinatorLayout.");
      }
      backLayerLayout = (BackLayerLayout) dependency;
      return true;
    }
    return false;
  }

  @Override
  public boolean onMeasureChild(CoordinatorLayout parent, View child, int parentWidthMeasureSpec,
      int widthUsed, int parentHeightMeasureSpec, int heightUsed) {
    if (backLayerLayout == null) {
      throw new IllegalStateException(
          "There is no BackLayerLayout and a view is using BackLayerSiblingBehavior");
    }
    if (!backLayerLayout.isMeasured()) {
      throw new IllegalStateException(
          "The BackLayerLayout has not been measured.");
    }
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    CoordinatorLayout.LayoutParams childLayoutParams =
        (CoordinatorLayout.LayoutParams) child.getLayoutParams();
    if (!checkGravity(backLayerLayoutParams)) {
      throw new IllegalStateException(
          "The gravity for BackLayerLayout is not set to one of {top,bottom,left,right,start,end}");
    }

    // Use the back layer's original dimensions to measure, always, even when these have changed
    // thanks to expansion.
    int usedWidth =
        Gravity.isHorizontal(backLayerLayoutParams.gravity)
            ? ViewCompat.getMinimumWidth(backLayerLayout) : 0;
    usedWidth += parent.getPaddingLeft() + parent.getPaddingRight()
        + childLayoutParams.leftMargin + childLayoutParams.rightMargin;
    int usedHeight =
        Gravity.isVertical(backLayerLayoutParams.gravity)
            ? ViewCompat.getMinimumHeight(backLayerLayout) : 0;
    usedHeight +=  parent.getPaddingTop() + parent.getPaddingBottom()
        + childLayoutParams.topMargin + childLayoutParams.bottomMargin;

    int widthMeasureSpec =
        ViewGroup.getChildMeasureSpec(
            parentWidthMeasureSpec,
            usedWidth,
            childLayoutParams.width);
    int heightMeasureSpec =
        ViewGroup.getChildMeasureSpec(
            parentHeightMeasureSpec,
            usedHeight,
            childLayoutParams.height);

    child.measure(widthMeasureSpec, heightMeasureSpec);
    return true;
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
    parent.onLayoutChild(child, layoutDirection);
    offsetChildIfNeeded(child, layoutDirection);
    return true;
  }

  @Override
  public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
    int layoutDirection = ViewCompat.getLayoutDirection(parent);
    return offsetChildIfNeeded(child, layoutDirection);
  }

  /**
   * Reacts to changes in the boundaries of the back layer by sliding the sibling to respect the new
   * boundaries.
   * @return Returns true if the position was changed in any way.
   */
  private boolean offsetChildIfNeeded(View child, int layoutDirection) {
    int verticalOffset = 0;
    int horizontalOffset = 0;
    CoordinatorLayout.LayoutParams childLayoutParams =
        (CoordinatorLayout.LayoutParams) child.getLayoutParams();
    int absoluteGravity = Gravity.getAbsoluteGravity(
        ((CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams()).gravity,
        layoutDirection);
    switch (absoluteGravity) {
      case Gravity.TOP:
        // The top of the sibling should match the bottom of the back layer
        int childTop = child.getTop() - childLayoutParams.topMargin;
        verticalOffset = backLayerLayout.getBottom() - childTop;
        break;
      case Gravity.BOTTOM:
        // The bottom of the sibling should match the top of the back layer
        int childBottom = child.getBottom() + childLayoutParams.bottomMargin;
        verticalOffset = backLayerLayout.getTop() - childBottom;
        break;
      case Gravity.LEFT:
        // the left of the sibling should match the right of the back layer
        int childLeft = child.getLeft() - childLayoutParams.leftMargin;
        horizontalOffset = backLayerLayout.getRight() - childLeft;
        break;
      case Gravity.RIGHT:
        // The right of the sibling should match the left of the back layer.
        int childRight = child.getRight() + childLayoutParams.rightMargin;
        horizontalOffset = backLayerLayout.getLeft() - childRight;
        break;
    }
    if (horizontalOffset != 0 || verticalOffset != 0) {
      ViewCompat.offsetLeftAndRight(child, horizontalOffset);
      ViewCompat.offsetTopAndBottom(child, verticalOffset);
      return true;
    }
    return false;
  }

  /**
   * Checks that the gravity is set to one of the 4 directions.
   */
  private boolean checkGravity(CoordinatorLayout.LayoutParams layoutParams) {
    int gravity = layoutParams.gravity;
    return
        gravity == Gravity.TOP || gravity == Gravity.BOTTOM
            || gravity == Gravity.LEFT || gravity == Gravity.RIGHT
            || gravity == Gravity.START || gravity == Gravity.END
            || gravity == (Gravity.START | Gravity.LEFT)
            || gravity == (Gravity.END | Gravity.RIGHT);
  }
}
