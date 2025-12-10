/*
 * Copyright 2025 The Android Open Source Project
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
package com.google.android.material.listitem;

import com.google.android.material.R;

import static com.google.android.material.listitem.SwipeableListItem.STATE_CLOSED;
import static com.google.android.material.listitem.SwipeableListItem.STATE_DRAGGING;
import static com.google.android.material.listitem.SwipeableListItem.STATE_OPEN;
import static com.google.android.material.listitem.SwipeableListItem.STATE_SETTLING;
import static com.google.android.material.listitem.SwipeableListItem.STATE_SWIPE_PRIMARY_ACTION;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.listitem.SwipeableListItem.StableSwipeState;
import com.google.android.material.listitem.SwipeableListItem.SwipeState;

/**
 * A container layout for a List item.
 *
 * <p>This layout applies the following states according to its position in a list:
 *
 * <ul>
 *   <li>{@link android.R.attr.state_first}
 *   <li>{@link android.R.attr.state_last}
 *   <li>{@link android.R.attr.state_middle}
 *   <li>{@link android.R.attr.state_single}
 * </ul>
 *
 * <p>Children of ListItemLayout that wish to be affected by the ListItemLayout's position state
 * should duplicate the state through setting {@link android.R.attr.duplicateParentState} to true.
 *
 * <p>MaterialCardView is recommended as a ListItemLayout child, as it supports updating its shape /
 * corners based on states.
 */
public class ListItemLayout extends FrameLayout {

  private static final int[] FIRST_STATE_SET = { android.R.attr.state_first };
  private static final int[] MIDDLE_STATE_SET = { android.R.attr.state_middle };
  private static final int[] LAST_STATE_SET = { android.R.attr.state_last };
  private static final int[] SINGLE_STATE_SET = {android.R.attr.state_single};
  private static final int SETTLING_DURATION = 350;
  private static final int DEFAULT_SIGNIFICANT_VEL_THRESHOLD = 500;

  @Nullable private int[] positionState;

  @Nullable private ViewDragHelper viewDragHelper;
  @Nullable private GestureDetector gestureDetector;

  private int revealViewOffset;
  private int originalContentViewLeft;

  private View contentView;
  @Nullable private View swipeToRevealLayout;
  private boolean originalClipToPadding;

  @Nullable private AccessibilityDelegate swipeAccessibilityDelegate;

  @SwipeState private int swipeState = STATE_CLOSED;
  @StableSwipeState private int lastStableSwipeState = STATE_CLOSED;
  private final StateSettlingTracker stateSettlingTracker = new StateSettlingTracker();

  // Cubic bezier curve approximating a spring with damping = 0.6 and stiffness = 800
  private static final TimeInterpolator CUBIC_BEZIER_INTERPOLATOR =
      new PathInterpolator(0.42f, 1.67f, 0.21f, 0.9f);

  private class StateSettlingTracker {
    @StableSwipeState private int targetSwipeState;
    private boolean isContinueSettlingRunnablePosted;

    private final Runnable continueSettlingRunnable =
        () -> {
          isContinueSettlingRunnablePosted = false;
          if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
            continueSettlingToState(targetSwipeState);
          } else if (swipeState == STATE_SETTLING) {
            setSwipeStateInternal(targetSwipeState);
          }
          // In other cases, settling has been interrupted by certain UX interactions. Do nothing.
        };

    private void continueSettlingToState(@StableSwipeState int targetSwipeState) {
      this.targetSwipeState = targetSwipeState;
      if (!isContinueSettlingRunnablePosted) {
        post(continueSettlingRunnable);
        isContinueSettlingRunnablePosted = true;
      }
    }
  }

  public ListItemLayout(@NonNull Context context) {
    this(context, null);
  }

  public ListItemLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.listItemLayoutStyle);
  }

  public ListItemLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.style.Widget_Material3_ListItemLayout);
  }

  public ListItemLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (positionState == null) {
      return super.onCreateDrawableState(extraSpace);
    }
    int[] drawableState = super.onCreateDrawableState(extraSpace+1);
    return mergeDrawableStates(drawableState, positionState);
  }

  /**
   * Helper method that sets the drawable state of the ListItemLayout according to its position in
   * the list. This is already called by {@link ListItemViewHolder#bind} if the ListItemLayout is
   * inside of a {@link ListItemViewHolder}.
   *
   * <p>Children of ListItemLayout that wish to be affected by this state should duplicate its
   * parent's state.
   */
  public void updateAppearance(int position, int itemCount) {
    if (position < 0 || itemCount < 0) {
      positionState = null;
    } else if (itemCount == 1) {
      positionState = SINGLE_STATE_SET;
    } else if (position == 0) {
      positionState = FIRST_STATE_SET;
    } else if (position == itemCount - 1) {
      positionState = LAST_STATE_SET;
    } else {
      positionState = MIDDLE_STATE_SET;
    }
    refreshDrawableState();
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    super.addView(child, index, params);
    if (swipeToRevealLayout != null && child instanceof RevealableListItem) {
      throw new UnsupportedOperationException(
          "Only one RevealableListItem is supported in a ListItemLayout.");
    } else if (child instanceof RevealableListItem) {
      swipeToRevealLayout = child;
      // Start the reveal view at a desired width of 0
      ((RevealableListItem) child).setRevealedWidth(0);
      // Make sure reveal view has lower elevation
      child.setElevation(getElevation() - 1);
    } else if (contentView != null && child instanceof SwipeableListItem) {
      throw new UnsupportedOperationException(
          "Only one SwipeableListItem view is allowed in a ListItemLayout.");
    } else if (child instanceof SwipeableListItem) {
      contentView = child;
    }
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);
    if (child == swipeToRevealLayout) {
      swipeToRevealLayout = null;
    } else if (child == contentView) {
      contentView = null;
    }

    if (swipeToRevealLayout == null || contentView == null) {
      viewDragHelper = null;
      gestureDetector = null;
      swipeAccessibilityDelegate = null;
      setClipToPadding(originalClipToPadding);
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ensureSwipeToRevealSetupIfNeeded()) {
      // Process the event regardless of the event type.
      viewDragHelper.processTouchEvent(ev);
      gestureDetector.onTouchEvent(ev);

      // Mark the event as handled if we are dragging
      if (viewDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING) {
        return true;
      }
    }

    // Otherwise, defer to the default behavior.
    return super.onTouchEvent(ev);
  }

  @Override
  public boolean onInterceptTouchEvent(MotionEvent ev) {
    if (ensureSwipeToRevealSetupIfNeeded()) {
      final int action = ev.getActionMasked();
      if (action == MotionEvent.ACTION_CANCEL || action == MotionEvent.ACTION_UP) {
        viewDragHelper.cancel();
        return false;
      }
      gestureDetector.onTouchEvent(ev);

      // Let ViewDragHelper decide if it should intercept.
      return viewDragHelper.shouldInterceptTouchEvent(ev);
    }

    return super.onInterceptTouchEvent(ev);
  }

  /**
   * Returns whether or not we should swipe to reveal, and ensures that the necessary swipe
   * variables are initialized if true.
   */
  private boolean ensureSwipeToRevealSetupIfNeeded() {
    if (swipeToRevealLayout == null || contentView == null) {
      return false;
    }
    if (viewDragHelper == null) {
      viewDragHelper = createViewDragHelper();
    }
    if (gestureDetector == null) {
      gestureDetector = createGestureDetector();
    }
    if (swipeAccessibilityDelegate == null) {
      swipeAccessibilityDelegate = createSwipeAccessibilityDelegate();
      contentView.setAccessibilityDelegate(swipeAccessibilityDelegate);
    }

    // If swipe to reveal is enabled, we want to disable clipping to padding so that the swipe view
    // can be swiped. If the original clip to padding value is true, we save the value so if swipe
    // is ever disabled, we can restore the original value.
    if (getClipToPadding()) {
      originalClipToPadding = getClipToPadding();
      setClipToPadding(false);
    }

    return true;
  }

  private ViewDragHelper createViewDragHelper() {
    return ViewDragHelper.create(
        this,
        new ViewDragHelper.Callback() {
          @Override
          public boolean tryCaptureView(@NonNull View child, int pointerId) {
            if (contentView instanceof SwipeableListItem
                && !((SwipeableListItem) contentView).isSwipeEnabled()) {
              return false;
            }
            if (swipeToRevealLayout != null && contentView != null) {
              viewDragHelper.captureChildView(contentView, pointerId);
            }
            return false;
          }

          @Override
          public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (!(contentView instanceof SwipeableListItem
                && swipeToRevealLayout instanceof RevealableListItem)) {
              return 0;
            }
            boolean isRtl = getLayoutDirection() == LAYOUT_DIRECTION_RTL;
            RevealableListItem revealableItem = (RevealableListItem) swipeToRevealLayout;
            SwipeableListItem swipeableItem = (SwipeableListItem) contentView;

            int maxSwipeDistance;
            if (swipeableItem.isSwipeToPrimaryActionEnabled()) {
              MarginLayoutParams contentViewLp = (MarginLayoutParams) contentView.getLayoutParams();
              maxSwipeDistance = contentView.getMeasuredWidth() + contentViewLp.getMarginEnd();
            } else {
              MarginLayoutParams revealViewLp =
                  (MarginLayoutParams) swipeToRevealLayout.getLayoutParams();
              maxSwipeDistance =
                  revealableItem.getIntrinsicWidth()
                      + revealViewLp.getMarginStart()
                      + revealViewLp.getMarginEnd();
            }

            int maxSwipeBoundary =
                originalContentViewLeft
                    + ((isRtl ? 1 : -1)
                        * (maxSwipeDistance + swipeableItem.getSwipeMaxOvershoot()));

            final int startBound = isRtl ? originalContentViewLeft : maxSwipeBoundary;
            final int endBound = isRtl ? maxSwipeBoundary : originalContentViewLeft;

            return Math.max(startBound, Math.min(left, endBound));
          }

          @Override
          public int getViewHorizontalDragRange(@NonNull View child) {
            if (contentView instanceof SwipeableListItem
                && swipeToRevealLayout instanceof RevealableListItem) {
              return ((RevealableListItem) swipeToRevealLayout).getIntrinsicWidth()
                  + ((SwipeableListItem) contentView).getSwipeMaxOvershoot();
            }
            return 0;
          }

          @Override
          public void onViewPositionChanged(
              @NonNull View changedView, int left, int top, int dx, int dy) {
            if (!(contentView instanceof SwipeableListItem
                && swipeToRevealLayout instanceof RevealableListItem)) {
              return;
            }
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            updateSwipeProgress(left);
          }

          @Override
          public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (contentView instanceof SwipeableListItem
                && swipeToRevealLayout instanceof RevealableListItem) {
              startSettling(contentView, calculateTargetSwipeState(xvel, releasedChild));
            }
          }

          private int calculateTargetSwipeState(float xvel, View swipeView) {
            if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
              xvel *= -1;
            }
            if (!((SwipeableListItem) swipeView).isSwipeToPrimaryActionEnabled()) {
              if (xvel > DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the right
                return STATE_CLOSED;
              }
              if (xvel < -DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the left
                return STATE_OPEN;
              }
              // Settle to the closest point if velocity is not significant
              return Math.abs(swipeView.getLeft() - getSwipeRevealViewRevealedOffset())
                      < Math.abs(swipeView.getLeft() - getSwipeViewClosedOffset())
                  ? STATE_OPEN
                  : STATE_CLOSED;
            }

            // Swipe to action is supported
            if (xvel > DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the right
              return lastStableSwipeState == STATE_SWIPE_PRIMARY_ACTION ? STATE_OPEN : STATE_CLOSED;
            }
            if (xvel < -DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the left
              return lastStableSwipeState == STATE_CLOSED ? STATE_OPEN : STATE_SWIPE_PRIMARY_ACTION;
            }

            // Settle to the closest point if velocity is not significant
            if (Math.abs(swipeView.getLeft() - getSwipeToActionOffset())
                < Math.abs(swipeView.getLeft() - getSwipeRevealViewRevealedOffset())) {
              return STATE_SWIPE_PRIMARY_ACTION;
            }
            if (Math.abs(swipeView.getLeft() - getSwipeRevealViewRevealedOffset())
                < Math.abs(swipeView.getLeft() - getSwipeViewClosedOffset())) {
              return STATE_OPEN;
            }
            return STATE_CLOSED;
          }

          @Override
          public void onViewDragStateChanged(int state) {
            if (state == ViewDragHelper.STATE_DRAGGING) {
              setSwipeStateInternal(STATE_DRAGGING);
            }
          }
        });
  }

  private GestureDetector createGestureDetector() {
    return new GestureDetector(
        getContext(),
        new SimpleOnGestureListener() {
          @Override
          public boolean onScroll(
              MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            if (getParent() != null) {
              // Disallow intercepting touch event on the parent so that it doesn't interfere
              // with the swipe gesture. This value gets reset to false at the end of the
              // gesture.
              getParent().requestDisallowInterceptTouchEvent(true);
            }
            return false;
          }
        });
  }

  private AccessibilityDelegate createSwipeAccessibilityDelegate() {
    return new AccessibilityDelegate() {
      @Override
      public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
        if (swipeToRevealLayout instanceof ViewGroup) {
          ViewGroup revealViewGroup = (ViewGroup) swipeToRevealLayout;
          for (int i = 0; i < revealViewGroup.getChildCount(); i++) {
            View child = revealViewGroup.getChildAt(i);
            if (shouldAddAccessibilityAction(child)) {
              infoCompat.addAction(
                  new AccessibilityActionCompat(
                      getAccessibilityActionId(child), child.getContentDescription()));
            }
          }
        }
      }

      private boolean shouldAddAccessibilityAction(View child) {
        return child.isClickable()
            && child.getContentDescription() != null
            && child.isEnabled()
            && child.getVisibility() == View.VISIBLE;
      }

      @Override
      public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (swipeToRevealLayout instanceof ViewGroup) {
          ViewGroup revealViewGroup = (ViewGroup) swipeToRevealLayout;
          for (int i = 0; i < revealViewGroup.getChildCount(); i++) {
            View child = revealViewGroup.getChildAt(i);
            if (getAccessibilityActionId(child) == action) {
              return child.performClick();
            }
          }
        }
        return super.performAccessibilityAction(host, action, args);
      }

      private int getAccessibilityActionId(View child) {
        return child.getId();
      }
    };
  }

  private int getSwipeRevealViewRevealedOffset() {
    if (swipeToRevealLayout == null) {
      return 0;
    }
    LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
    int revealViewTotalWidth =
        ((RevealableListItem) swipeToRevealLayout).getIntrinsicWidth()
            + lp.leftMargin
            + lp.rightMargin;

    return originalContentViewLeft
        + (getLayoutDirection() == LAYOUT_DIRECTION_RTL
            ? revealViewTotalWidth
            : -revealViewTotalWidth);
  }

  private int getSwipeViewClosedOffset() {
    return originalContentViewLeft;
  }

  private int getSwipeToActionOffset() {
    if (contentView == null) {
      return 0;
    }
    LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
    return originalContentViewLeft
        + (getLayoutDirection() == LAYOUT_DIRECTION_RTL ? 1 : -1)
            * (contentView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin);
  }

  private int getOffsetForSwipeState(@StableSwipeState int swipeState) {
    if (swipeToRevealLayout == null) {
      throw new IllegalArgumentException(
          "Cannot get offset for swipe without a SwipeableListItem and a RevealableListItem.");
    }
    switch (swipeState) {
      case STATE_CLOSED:
        return getSwipeViewClosedOffset();
      case STATE_OPEN:
        return getSwipeRevealViewRevealedOffset();
      case STATE_SWIPE_PRIMARY_ACTION:
        return getSwipeToActionOffset();
      default:
        throw new IllegalArgumentException("Invalid state to get swipe offset: " + swipeState);
    }
  }

  private void updateSwipeProgress(int left) {
    if (!(contentView instanceof SwipeableListItem
        && swipeToRevealLayout instanceof RevealableListItem)) {
      return;
    }
    revealViewOffset = left - originalContentViewLeft;

    LayoutParams revealViewLp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
    LayoutParams contentViewLp = (LayoutParams) contentView.getLayoutParams();

    // Desired width is how much we've displaced the content view minus any margins.
    int revealViewDesiredWidth =
        max(
            0,
            abs(originalContentViewLeft - contentView.getLeft())
                - contentViewLp.getMarginEnd() // only end margin matters here
                - revealViewLp.getMarginStart()
                - revealViewLp.getMarginEnd());

    ((RevealableListItem) swipeToRevealLayout).setRevealedWidth(revealViewDesiredWidth);
    ((SwipeableListItem) contentView).onSwipe(revealViewOffset);

    int fullSwipedOffset = getSwipeToActionOffset();
    int fadeOutThreshold = (fullSwipedOffset + getSwipeRevealViewRevealedOffset()) / 2;
    float contentViewAlpha =
        AnimationUtils.lerp(
            /* startValue= */ 1f,
            /* endValue= */ 0f,
            /* fraction= */ (float) (revealViewOffset - fadeOutThreshold)
                / (fullSwipedOffset - fadeOutThreshold));
    contentView.setAlpha(contentViewAlpha);
  }

  private void startSettling(View contentView, @StableSwipeState int targetSwipeState) {
    if (viewDragHelper == null) {
      return;
    }
    int left = getOffsetForSwipeState(targetSwipeState);
    // If we are going to the revealed state, we want to settle with a 'bounce' so we use a cubic
    // bezier interpolator. Otherwise, we are closing and we don't want a bounce.
    boolean settling =
        (targetSwipeState == STATE_OPEN)
            ? viewDragHelper.smoothSlideViewTo(
                contentView,
                left,
                contentView.getTop(),
                SETTLING_DURATION,
                (Interpolator) CUBIC_BEZIER_INTERPOLATOR)
            : viewDragHelper.smoothSlideViewTo(contentView, left, contentView.getTop());
    if (settling) {
      setSwipeStateInternal(STATE_SETTLING);
      stateSettlingTracker.continueSettlingToState(targetSwipeState);
    } else {
      setSwipeStateInternal(targetSwipeState);
    }
  }

  private void setSwipeStateInternal(@SwipeState int swipeState) {
    if (swipeState == this.swipeState) {
      return;
    }
    // If swipe to action is not supported but the swipe state to be set in
    // STATE_SWIPE_PRIMARY_ACTION, we do nothing.
    if (!(contentView instanceof SwipeableListItem)
        || (swipeState == STATE_SWIPE_PRIMARY_ACTION
            && !((SwipeableListItem) contentView).isSwipeToPrimaryActionEnabled())) {
      return;
    }
    this.swipeState = swipeState;
    if (swipeState == STATE_CLOSED
        || swipeState == STATE_OPEN
        || swipeState == STATE_SWIPE_PRIMARY_ACTION) {
      this.lastStableSwipeState = swipeState;
    }

    ((SwipeableListItem) contentView).onSwipeStateChanged(swipeState);
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   */
  public void setSwipeState(@StableSwipeState int swipeState) {
    setSwipeState(swipeState, /* animate= */ true);
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   * @param animate Whether to animate to the given swipe state.
   */
  public void setSwipeState(@StableSwipeState int swipeState, boolean animate) {
    if (swipeState != STATE_CLOSED
        && swipeState != STATE_OPEN
        && swipeState != STATE_SWIPE_PRIMARY_ACTION) {
      throw new IllegalArgumentException("Invalid swipe state: " + swipeState);
    } else if (!(contentView instanceof SwipeableListItem)
        || !(swipeToRevealLayout instanceof RevealableListItem)) {
      throw new IllegalArgumentException(
          "ListItemLayout must have a SwipeableListItem child and a RevealableListItem child to be"
              + " swiped.");
    }

    Runnable runnable =
        () -> {
          if (!animate) {
            if (viewDragHelper != null) {
              viewDragHelper.abort();
            }
            int finalLeft = getOffsetForSwipeState(swipeState);
            contentView.offsetLeftAndRight(finalLeft - contentView.getLeft());
            updateSwipeProgress(finalLeft);
            setSwipeStateInternal(swipeState);
          } else {
            startSettling(contentView, swipeState);
          }
        };
    if (isLaidOut()) {
      runnable.run();
    } else {
      // Put it into a post to ensure that all the views have been laid out.
      post(runnable);
    }
  }

  /** Returns the current {@link SwipeState} of the ListItemLayout. */
  @SwipeState
  public int getSwipeState() {
    return swipeState;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (contentView != null && swipeToRevealLayout != null && ensureSwipeToRevealSetupIfNeeded()) {
      originalContentViewLeft = contentView.getLeft();
      int originalContentViewRight = contentView.getRight();
      contentView.offsetLeftAndRight(revealViewOffset);

      // We always lay out swipeToRevealLayout such that the end is aligned to where the original
      // content view's end was. Note that if the content view had an end margin, it will
      // effectively be passed onto the reveal view.
      LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
      int swipeToRevealLeft;
      int swipeToRevealRight;
      if (getLayoutDirection() == LAYOUT_DIRECTION_RTL) {
        swipeToRevealLeft = originalContentViewLeft + lp.leftMargin;
        swipeToRevealRight = swipeToRevealLeft + swipeToRevealLayout.getMeasuredWidth();
      } else {
        swipeToRevealRight = originalContentViewRight - lp.rightMargin;
        swipeToRevealLeft = swipeToRevealRight - swipeToRevealLayout.getMeasuredWidth();
      }
      swipeToRevealLayout.layout(
          swipeToRevealLeft,
          swipeToRevealLayout.getTop(),
          swipeToRevealRight,
          swipeToRevealLayout.getBottom());
    }
  }
}
