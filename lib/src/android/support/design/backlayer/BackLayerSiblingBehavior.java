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

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.CoordinatorLayout.Behavior;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewPropertyAnimator;

/**
 * Behavior to apply to the content view when using a BackLayerLayout.
 *
 * <p>Using this behavior requires **exactly** one sibling view of type {@link BackLayerLayout}
 * which will be used to calculate the measurements and positions for the content layer view.
 */
public class BackLayerSiblingBehavior extends Behavior<View> {

  private static final int ANIMATION_DURATION = 225;

  /** The back layer for this content layer. */
  private BackLayerLayout backLayerLayout = null;

  private View childView = null;
  private boolean previousChildViewFocusability = false;
  private int layoutDirection;

  public BackLayerSiblingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  //  Implementation of Behavior Methods

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    if (dependency instanceof BackLayerLayout) {
      if (backLayerLayout == null) {
        backLayerLayout = (BackLayerLayout) dependency;
        backLayerLayout.setSibling(this);
      } else if (dependency != backLayerLayout) {
        throw new IllegalStateException(
            "There is more than one BackLayerLayout in a single CoordinatorLayout.");
      }
      return true;
    }
    return false;
  }

  @Override
  public boolean onMeasureChild(
      CoordinatorLayout parent,
      View child,
      int parentWidthMeasureSpec,
      int widthUsed,
      int parentHeightMeasureSpec,
      int heightUsed) {
    childView = child;
    if (backLayerLayout == null) {
      throw new IllegalStateException(
          "There is no BackLayerLayout and a view is using BackLayerSiblingBehavior");
    }
    if (!backLayerLayout.hasMeasuredCollapsedSize()) {
      throw new IllegalStateException("The BackLayerLayout has not been measured.");
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
            ? ViewCompat.getMinimumWidth(backLayerLayout)
            : 0;
    usedWidth +=
        parent.getPaddingLeft()
            + parent.getPaddingRight()
            + childLayoutParams.leftMargin
            + childLayoutParams.rightMargin;
    int usedHeight =
        Gravity.isVertical(backLayerLayoutParams.gravity)
            ? ViewCompat.getMinimumHeight(backLayerLayout)
            : 0;
    usedHeight +=
        parent.getPaddingTop()
            + parent.getPaddingBottom()
            + childLayoutParams.topMargin
            + childLayoutParams.bottomMargin;

    int widthMeasureSpec =
        ViewGroup.getChildMeasureSpec(parentWidthMeasureSpec, usedWidth, childLayoutParams.width);
    int heightMeasureSpec =
        ViewGroup.getChildMeasureSpec(
            parentHeightMeasureSpec, usedHeight, childLayoutParams.height);

    child.measure(widthMeasureSpec, heightMeasureSpec);
    return true;
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, View child, int layoutDirection) {
    this.layoutDirection = layoutDirection;
    parent.onLayoutChild(child, layoutDirection);
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    switch (absoluteGravity) {
      case Gravity.RIGHT:
      case Gravity.BOTTOM:
        childView.setX(0);
        childView.setY(0);
        break;
      case Gravity.TOP:
        childView.setX(0);
        childView.setY(ViewCompat.getMinimumHeight(backLayerLayout));
        break;
      case Gravity.LEFT:
        childView.setX(ViewCompat.getMinimumWidth(backLayerLayout));
        childView.setY(0);
        break;
    }
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
    if (backLayerLayout.isExpanded()) {
      backLayerLayout.collapse();
      return true;
    }
    return false;
  }

  public void onBeforeExpand() {
    previousChildViewFocusability = childView.isFocusable();
    childView.setFocusable(true);
    int end = 0;
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    switch (absoluteGravity) {
      case Gravity.TOP:
        end = backLayerLayout.getExpandedHeight();
        break;
      case Gravity.LEFT:
        end = backLayerLayout.getExpandedWidth();
        break;
      case Gravity.BOTTOM:
        end = ViewCompat.getMinimumHeight(backLayerLayout) - backLayerLayout.getExpandedHeight();
        break;
      case Gravity.RIGHT:
        end = ViewCompat.getMinimumWidth(backLayerLayout) - backLayerLayout.getExpandedWidth();
        break;
    }
    animate(
        end,
        absoluteGravity,
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            backLayerLayout.onExpandAnimationDone();
          }
        });
  }

  public void onBeforeCollapse() {
    previousChildViewFocusability = childView.isFocusable();
    childView.setFocusable(true);
    int end = 0;
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    switch (absoluteGravity) {
      case Gravity.TOP:
        end = ViewCompat.getMinimumHeight(backLayerLayout);
        break;
      case Gravity.LEFT:
        end = ViewCompat.getMinimumWidth(backLayerLayout);
        break;
      case Gravity.BOTTOM:
      case Gravity.RIGHT:
        end = 0;
        break;
    }
    animate(
        end,
        absoluteGravity,
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            childView.setFocusable(previousChildViewFocusability);
            backLayerLayout.onCollapseAnimationDone();
          }
        });
  }

  // Private methods

  private void animate(int end, int absoluteGravity, AnimatorListener listener) {
    ViewPropertyAnimator animator =
        childView.animate().setListener(listener).setDuration(ANIMATION_DURATION);
    switch (absoluteGravity) {
      case Gravity.TOP:
      case Gravity.BOTTOM:
        animator.y(end);
        break;
      case Gravity.LEFT:
      case Gravity.RIGHT:
        animator.x(end);
        break;
    }
    animator.start();
  }

  /** Checks that the gravity is set to one of the 4 directions. */
  private boolean checkGravity(CoordinatorLayout.LayoutParams layoutParams) {
    int gravity = layoutParams.gravity;
    return gravity == Gravity.TOP
        || gravity == Gravity.BOTTOM
        || gravity == Gravity.LEFT
        || gravity == Gravity.RIGHT
        || gravity == Gravity.START
        || gravity == Gravity.END
        || gravity == (Gravity.START | Gravity.LEFT)
        || gravity == (Gravity.END | Gravity.RIGHT);
  }
}
