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
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.Nullable;
import android.support.design.transformation.ExpandableBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

/**
 * Behavior to apply to the content view when using a BackLayerLayout.
 *
 * <p>Using this behavior requires **exactly** one sibling view of type {@link BackLayerLayout}
 * which will be used to calculate the measurements and positions for the content layer view.
 *
 * <p>You MUST NOT use a {@link ViewGroup.OnHierarchyChangeListener} on the view to which you apply
 * this behavior, as this behavior uses OnHierarchyChangedListener for internal housekeeping.
 */
public class BackLayerSiblingBehavior extends ExpandableBehavior {

  private static final int ANIMATION_DURATION = 225;

  private int layoutDirection;
  private CharSequence expandedContentDescription;
  private ContentViewAccessibilityPropertiesHelper contentViewAccessibilityHelper;
  @Nullable private Animator currentAnimator;

  public BackLayerSiblingBehavior() {}

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
   * Sets the content description for accessibility services to be used on the content layer view.
   */
  public void setExpandedContentDescription(CharSequence expandedContentDescription) {
    this.expandedContentDescription = expandedContentDescription;
  }

  //  Implementation of Behavior Methods

  @Override
  public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
    return dependency instanceof BackLayerLayout;
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
    if (contentViewAccessibilityHelper == null) {
      contentViewAccessibilityHelper = new ContentViewAccessibilityPropertiesHelper(child);
    }

    BackLayerLayout backLayerLayout = (BackLayerLayout) findExpandableWidget(parent, child);
    if (backLayerLayout == null) {
      throw new IllegalStateException(
          "There is no BackLayerLayout and a view is using BackLayerSiblingBehavior");
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
    // Super contains support for detecting first layout after configuration change.
    super.onLayoutChild(parent, child, layoutDirection);

    BackLayerLayout backLayerLayout = (BackLayerLayout) findExpandableWidget(parent, child);
    if (backLayerLayout == null) {
      throw new IllegalStateException(
          "There is no BackLayerLayout and a view is using BackLayerSiblingBehavior");
    }

    this.layoutDirection = layoutDirection;
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    int collapsedWidth = ViewCompat.getMinimumWidth(backLayerLayout);
    int collapsedHeight = ViewCompat.getMinimumHeight(backLayerLayout);

    // Do actual layout using measured dimensions from #onMeasureChild().
    parent.onLayoutChild(child, layoutDirection);

    // Adjust position - note that this places the child correctly in its collapsed state.
    switch (absoluteGravity) {
      case Gravity.TOP:
        int top = collapsedHeight;
        child.offsetTopAndBottom(top - child.getTop());
        break;
      case Gravity.LEFT:
        int left = collapsedWidth;
        child.offsetLeftAndRight(left - child.getLeft());
        break;
      case Gravity.BOTTOM:
        int bottom = parent.getHeight() - collapsedHeight;
        child.offsetTopAndBottom(bottom - child.getBottom());
        break;
      case Gravity.RIGHT:
        int right = parent.getWidth() - collapsedWidth;
        child.offsetLeftAndRight(right - child.getRight());
        break;
      default: // do nothing
    }

    if (backLayerLayout.isExpanded()) {
      // If we went through layout due to BackLayerLayout's children changing size, we need to
      // translate to the new expanded position.
      animateTranslation(backLayerLayout, child, true, null);
    }
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(CoordinatorLayout parent, View child, MotionEvent ev) {
    BackLayerLayout backLayerLayout = (BackLayerLayout) findExpandableWidget(parent, child);
    if (backLayerLayout == null) {
      throw new IllegalStateException(
          "There is no BackLayerLayout and a view is using BackLayerSiblingBehavior");
    }

    if (backLayerLayout.isExpanded()) {
      // onInterceptTouchEvent is called for every touch in the CoordinatorLayout. Because of this
      // we need to check that the MotionEvent's coordinates are inside of the Child View.
      if (parent.isPointInChildBounds(child, (int) ev.getX(), (int) ev.getY())) {
        backLayerLayout.setExpanded(false);
        return true;
      }
    }
    return false;
  }

  @Override
  protected boolean onExpandedStateChange(
      View dependency, View child, final boolean expanded, boolean animated) {
    // Translate the content layer to the desired position.
    final BackLayerLayout backLayerLayout = (BackLayerLayout) dependency;
    animateTranslation(
        backLayerLayout,
        child,
        animated,
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationStart(Animator animation) {
            if (expanded) {
              contentViewAccessibilityHelper.makeFocusableWithContentDescription(
                  expandedContentDescription);
              backLayerLayout.onExpandAnimationStart();
            } else {
              backLayerLayout.onCollapseAnimationStart();
            }
          }

          @Override
          public void onAnimationEnd(Animator animation) {
            if (expanded) {
              backLayerLayout.onExpandAnimationEnd();
            } else {
              contentViewAccessibilityHelper.restoreAccessibilityProperties();
              backLayerLayout.onCollapseAnimationEnd();
            }
          }
        });
    return true;
  }

  // Private methods

  private void animateTranslation(
      BackLayerLayout backLayerLayout,
      View child,
      boolean animated,
      @Nullable AnimatorListener listener) {
    if (currentAnimator != null) {
      currentAnimator.cancel();
    }

    if (backLayerLayout.isExpanded()) {
      currentAnimator = createExpandAnimation(backLayerLayout, child);
    } else {
      currentAnimator = createCollapseAnimation(backLayerLayout, child);
    }

    if (listener != null) {
      currentAnimator.addListener(listener);
    }

    currentAnimator.start();
    if (!animated) {
      // Synchronously end the animation, jumping to the end state.
      currentAnimator.end();
    }
  }

  private Animator createExpandAnimation(BackLayerLayout backLayerLayout, View child) {
    int expandedWidth = backLayerLayout.calculateExpandedWidth();
    int expandedHeight = backLayerLayout.calculateExpandedHeight();
    int collapsedWidth = ViewCompat.getMinimumWidth(backLayerLayout);
    int collapsedHeight = ViewCompat.getMinimumHeight(backLayerLayout);

    int deltaX = expandedWidth - collapsedWidth;
    int deltaY = expandedHeight - collapsedHeight;

    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    Animator animator;
    switch (absoluteGravity) {
      case Gravity.TOP:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, deltaY);
        break;
      case Gravity.LEFT:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, deltaX);
        break;
      case Gravity.BOTTOM:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, -deltaY);
        break;
      case Gravity.RIGHT:
      default:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, -deltaX);
        break;
    }
    animator.setDuration(ANIMATION_DURATION);
    return animator;
  }

  private Animator createCollapseAnimation(BackLayerLayout backLayerLayout, View child) {
    CoordinatorLayout.LayoutParams backLayerLayoutParams =
        (CoordinatorLayout.LayoutParams) backLayerLayout.getLayoutParams();
    int absoluteGravity =
        Gravity.getAbsoluteGravity(backLayerLayoutParams.gravity, layoutDirection);
    Animator animator;
    switch (absoluteGravity) {
      case Gravity.TOP:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, 0f);
        break;
      case Gravity.LEFT:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, 0f);
        break;
      case Gravity.BOTTOM:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_Y, 0f);
        break;
      case Gravity.RIGHT:
      default:
        animator = ObjectAnimator.ofFloat(child, View.TRANSLATION_X, 0f);
        break;
    }
    animator.setDuration(ANIMATION_DURATION);
    return animator;
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
