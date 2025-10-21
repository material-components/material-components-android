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
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;
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
  // The overshoot that the user can swipe the reveal view by before it settles
  // back to the closest stable swipe state.
  private final int swipeMaxOvershoot;

  @Nullable private int[] positionState;

  @Nullable private ViewDragHelper viewDragHelper;
  @Nullable private GestureDetector gestureDetector;

  private int revealViewOffset;
  private int originalContentViewLeft;

  private View contentView;
  @Nullable private View swipeToRevealLayout;
  private boolean originalClipToPadding;

  private int swipeState = STATE_CLOSED;
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
    this(context, attrs, defStyleAttr, R.attr.listItemLayoutStyle);
  }

  public ListItemLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);
    context = getContext();
    swipeMaxOvershoot = getResources().getDimensionPixelSize(R.dimen.m3_list_max_swipe_overshoot);
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
    if (swipeToRevealLayout != null && child instanceof ListItemRevealLayout) {
      throw new UnsupportedOperationException(
          "Only one ListItemRevealLayout is supported in a ListItemLayout.");
    } else if (child instanceof RevealableListItem) {
      swipeToRevealLayout = child;
      originalClipToPadding = getClipToPadding();
      setClipToPadding(false);
      // Start the reveal view at a desired width of 0
      ((RevealableListItem) child).setRevealedWidth(0);
      // Make sure reveal view has lower elevation
      child.setElevation(getElevation() - 1);
    }
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);
    if (child == swipeToRevealLayout) {
      viewDragHelper = null;
      gestureDetector = null;
      swipeToRevealLayout = null;
      setClipToPadding(originalClipToPadding);
    }
  }

  private void ensureContentViewIfRevealLayoutExists() {
    if (contentView != null || swipeToRevealLayout == null) {
      return;
    }

    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      if (getChildAt(i) instanceof SwipeableListItem) {
        if (contentView != null) {
          throw new UnsupportedOperationException(
              "Only one SwipeableListItem view is allowed in a ListItemLayout.");
        }
        contentView = getChildAt(i);
        originalContentViewLeft = contentView.getLeft();
      }
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ensureSwipeToRevealSetupIfNeeded()) {
      // TODO - b/447218120: Check that at least one child is a ListItemRevealLayout and the other
      //  is List content.
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
    if (swipeToRevealLayout == null) {
      return false;
    }
    if (viewDragHelper == null || gestureDetector == null) {
      viewDragHelper =
          ViewDragHelper.create(
              this,
              new ViewDragHelper.Callback() {
                @Override
                public boolean tryCaptureView(@NonNull View child, int pointerId) {
                  ensureContentViewIfRevealLayoutExists();
                  if (swipeToRevealLayout != null && contentView != null) {
                    viewDragHelper.captureChildView(contentView, pointerId);
                  }
                  return false;
                }

                @Override
                public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
                  // TODO:b/443153708 - Support RTL
                  LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
                  return max(
                      min(left, originalContentViewLeft),
                      originalContentViewLeft
                          - ((RevealableListItem) swipeToRevealLayout).getIntrinsicWidth()
                          - lp.leftMargin
                          - lp.rightMargin
                          - swipeMaxOvershoot);
                }

                @Override
                public int getViewHorizontalDragRange(@NonNull View child) {
                  return ((RevealableListItem) swipeToRevealLayout).getIntrinsicWidth()
                      + swipeMaxOvershoot;
                }

                @Override
                public void onViewPositionChanged(
                    @NonNull View changedView, int left, int top, int dx, int dy) {
                  super.onViewPositionChanged(changedView, left, top, dx, dy);
                  // TODO:b/443153708 - Support RTL
                  revealViewOffset = left - originalContentViewLeft;

                  LayoutParams revealViewLp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
                  LayoutParams contentViewLp = (LayoutParams) contentView.getLayoutParams();

                  // Desired width is how much we've displaced the content view minus any margins.
                  int revealViewDesiredWidth =
                      max(
                          0,
                          originalContentViewLeft
                              - contentView.getLeft()
                              - contentViewLp.rightMargin // only end margin matters here
                              - revealViewLp.leftMargin
                              - revealViewLp.rightMargin);
                  ((RevealableListItem) swipeToRevealLayout)
                      .setRevealedWidth(revealViewDesiredWidth);
                }

                @Override
                public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
                  startSettling(contentView, calculateTargetSwipeState(xvel, releasedChild));
                }

                private int calculateTargetSwipeState(float xvel, View swipeView) {
                  if (xvel > DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the right
                    return STATE_CLOSED;
                  }
                  if (xvel < -DEFAULT_SIGNIFICANT_VEL_THRESHOLD) { // A fast fling to the left
                    return STATE_OPEN;
                  }
                  if (Math.abs(swipeView.getLeft() - getSwipeRevealViewRevealedOffset())
                      < Math.abs(swipeView.getLeft() - getSwipeViewClosedOffset())) {
                    // Settle to the closest point if velocity is not significant
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

      gestureDetector =
          new GestureDetector(
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

    ensureContentViewIfRevealLayoutExists();

    return true;
  }

  private int getSwipeRevealViewRevealedOffset() {
    if (swipeToRevealLayout == null) {
      return 0;
    }
    LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
    return originalContentViewLeft
        - ((RevealableListItem) swipeToRevealLayout).getIntrinsicWidth()
        - lp.leftMargin
        - lp.rightMargin;
  }

  private int getSwipeViewClosedOffset() {
    return originalContentViewLeft;
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
      default:
        throw new IllegalArgumentException("Invalid state to get swipe offset: " + swipeState);
    }
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
    this.swipeState = swipeState;
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    if (contentView != null && swipeToRevealLayout != null) {
      originalContentViewLeft = contentView.getLeft();
      int originalContentViewRight = contentView.getRight();
      contentView.offsetLeftAndRight(revealViewOffset);
      // We always lay out swipeToRevealLayout such that the right is aligned to where the original
      // content view's right was. Note that if the content view had a right margin, it will
      // effectively be passed onto the reveal view.
      LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
      // TODO:b/443153708 - Support RTL
      swipeToRevealLayout.layout(
          originalContentViewRight - lp.rightMargin - swipeToRevealLayout.getMeasuredWidth(),
          swipeToRevealLayout.getTop(),
          originalContentViewRight - lp.rightMargin,
          swipeToRevealLayout.getBottom());
    }
  }
}
