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
import android.content.res.TypedArray;
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
 *
 * <p>You MUST NOT use a {@link ViewGroup.OnHierarchyChangedListener} on the view to which you apply
 * this behavior, as this behavior uses OnHierarchyChangedListener for internal housekeeping.
 */
public class BackLayerSiblingBehavior extends Behavior<View> {

  private static final int ANIMATION_DURATION = 225;

  /** The back layer for this content layer. */
  private BackLayerLayout backLayerLayout = null;

  private View childView = null;
  private int layoutDirection;
  private CharSequence expandedContentDescription;
  private ContentViewAccessibilityPropertiesHelper contentViewAccessibilityHelper;
  private boolean isFirstLayoutPass = true;

  public BackLayerSiblingBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    if (attrs != null) {
      TypedArray a =
          context
              .getTheme()
              .obtainStyledAttributes(attrs, R.styleable.BackLayerSiblingBehavior, 0, 0);
      try {
        expandedContentDescription =
            a.getString(R.styleable.BackLayerSiblingBehavior_behavior_expandedContentDescription);
        if (expandedContentDescription == null) {
          expandedContentDescription =
              context
                  .getResources()
                  .getString(R.string.design_backlayer_expanded_content_layer_content_description);
        }
      } finally {
        a.recycle();
      }
    }
  }

  /**
   * Sets the content description for accesibility services to be used on the content layer view.
   */
  public void setExpandedContentDescription(CharSequence expandedContentDescription) {
    this.expandedContentDescription = expandedContentDescription;
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
    // The first time onMeasureChild is called it should initialize the
    // contentViewAccessibilitHelper. This method is called before any other method that could
    // potentially need this field. Since the field is stateful we need to guarantee it is only set
    // the first time this is called.
    if (childView == null) {
      childView = child;
      contentViewAccessibilityHelper = new ContentViewAccessibilityPropertiesHelper(child);
    }
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
    int minimumWidth = ViewCompat.getMinimumWidth(backLayerLayout);
    int minimumHeight = ViewCompat.getMinimumHeight(backLayerLayout);
    if (!backLayerLayout.isExpanded()) {
      switch (absoluteGravity) {
        case Gravity.RIGHT:
        case Gravity.BOTTOM:
          childView.setX(0);
          childView.setY(0);
          break;
        case Gravity.TOP:
          childView.setX(0);
          childView.setY(minimumHeight);
          break;
        case Gravity.LEFT:
          childView.setX(minimumWidth);
          childView.setY(0);
          break;
        default:
          break;
      }
    } else if (isFirstLayoutPass) {
      // If back layer is expanded on the first layout pass this means the back layer state was
      // restored from an activity restart or a configuration change (rotation, multitasking-related
      // window size change, ...). In this case we must not animate.
      int expandedWidth = backLayerLayout.getExpandedWidth();
      int expandedHeight = backLayerLayout.getExpandedHeight();
      switch (absoluteGravity) {
        case Gravity.RIGHT:
          childView.setX(minimumWidth - expandedWidth - 1);
          childView.setY(0);
          break;
        case Gravity.BOTTOM:
          childView.setX(0);
          childView.setY(minimumHeight - expandedHeight - 1);
          break;
        case Gravity.TOP:
          childView.setX(0);
          childView.setY(expandedHeight);
          break;
        case Gravity.LEFT:
          childView.setX(expandedWidth);
          childView.setY(0);
          break;
        default:
          break;
      }
    } else {
      // This happens when the contents of the back layer change. We need to recalculate the size of
      // the expanded backlayer and animate the size change.
      backLayerLayout.measureExpanded();
      animateExpand(null);
    }
    isFirstLayoutPass = false;
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
    if (backLayerLayout.isExpanded()) {
      // onInterceptTouchEvent is called for every touch in the CoordinatorLayout. Because of this
      // we need to check that the MotionEvent's coordinates are inside of the Child View.
      if (parent.isPointInChildBounds(childView, (int) ev.getX(), (int) ev.getY())) {
        backLayerLayout.setExpanded(false);
        return true;
      }
    }
    return false;
  }

  void onBeforeExpand() {
    AnimatorListenerAdapter animatorListener =
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            backLayerLayout.onExpandAnimationDone();
          }
        };
    contentViewAccessibilityHelper.makeFocusableWithContentDescription(expandedContentDescription);
    animateExpand(animatorListener);
  }

  void onBeforeCollapse() {
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
      default:
        break;
    }
    animate(
        end,
        absoluteGravity,
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            contentViewAccessibilityHelper.restoreAccessibilityProperties();
            backLayerLayout.onCollapseAnimationDone();
          }
        });
  }

  // Private methods

  /**
   * Animates the expansion to the last calculated back layer expanded size and calls {@code
   * animationListener} when done.
   */
  private void animateExpand(AnimatorListenerAdapter animatorListener) {
    int end = 0;
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    // Calculate the end position for the content layer in the moving dimension (width for
    // start/end/left/right anchored back layers and height for top/bottom anchored back layers).
    switch (absoluteGravity) {
      case Gravity.TOP:
        end = backLayerLayout.getExpandedHeight();
        break;
      case Gravity.LEFT:
        end = backLayerLayout.getExpandedWidth();
        break;
      case Gravity.BOTTOM:
        end =
            ViewCompat.getMinimumHeight(backLayerLayout) - backLayerLayout.getExpandedHeight() - 1;
        break;
      case Gravity.RIGHT:
        end = ViewCompat.getMinimumWidth(backLayerLayout) - backLayerLayout.getExpandedWidth() - 1;
        break;
      default:
        break;
    }
    // Start the animation to slide the content layer to the desired position. We pass the absolute
    // gravity so we can animate the correct dimension.
    animate(end, absoluteGravity, animatorListener);
  }

  private void animate(int end, int absoluteGravity, AnimatorListener listener) {
    ViewPropertyAnimator animator =
        childView.animate().setListener(listener).setDuration(ANIMATION_DURATION);
    switch (absoluteGravity) {
      case Gravity.TOP:
      case Gravity.BOTTOM:
        animator.y(end);
        if (childView.getY() == end) {
          animator.setDuration(0);
        }
        break;
      case Gravity.LEFT:
      case Gravity.RIGHT:
        animator.x(end);
        if (childView.getX() == end) {
          animator.setDuration(0);
        }
        break;
      default:
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
