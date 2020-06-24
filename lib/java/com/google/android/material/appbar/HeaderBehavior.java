/*
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.appbar;

import android.content.Context;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.OverScroller;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.Behavior;
import androidx.core.math.MathUtils;

/**
 * The {@link Behavior} for a view that sits vertically above scrolling a view. See {@link
 * HeaderScrollingViewBehavior}.
 */
abstract class HeaderBehavior<V extends View> extends ViewOffsetBehavior<V> {

  private static final int INVALID_POINTER = -1;

  @Nullable private Runnable flingRunnable;
  OverScroller scroller;

  private boolean isBeingDragged;
  private int activePointerId = INVALID_POINTER;
  private int lastMotionY;
  private int touchSlop = -1;
  @Nullable private VelocityTracker velocityTracker;

  public HeaderBehavior() {}

  public HeaderBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    if (touchSlop < 0) {
      touchSlop = ViewConfiguration.get(parent.getContext()).getScaledTouchSlop();
    }

    // Shortcut since we're being dragged
    if (ev.getActionMasked() == MotionEvent.ACTION_MOVE && isBeingDragged) {
      if (activePointerId == INVALID_POINTER) {
        // If we don't have a valid id, the touch down wasn't on content.
        return false;
      }
      int pointerIndex = ev.findPointerIndex(activePointerId);
      if (pointerIndex == -1) {
        return false;
      }

      int y = (int) ev.getY(pointerIndex);
      int yDiff = Math.abs(y - lastMotionY);
      if (yDiff > touchSlop) {
        lastMotionY = y;
        return true;
      }
    }

    if (ev.getActionMasked() == MotionEvent.ACTION_DOWN) {
      activePointerId = INVALID_POINTER;

      int x = (int) ev.getX();
      int y = (int) ev.getY();
      isBeingDragged = canDragView(child) && parent.isPointInChildBounds(child, x, y);
      if (isBeingDragged) {
        lastMotionY = y;
        activePointerId = ev.getPointerId(0);
        ensureVelocityTracker();

        // There is an animation in progress. Stop it and catch the view.
        if (scroller != null && !scroller.isFinished()) {
          scroller.abortAnimation();
          return true;
        }
      }
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(ev);
    }

    return false;
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent ev) {
    boolean consumeUp = false;
    switch (ev.getActionMasked()) {
      case MotionEvent.ACTION_MOVE:
        final int activePointerIndex = ev.findPointerIndex(activePointerId);
        if (activePointerIndex == -1) {
          return false;
        }

        final int y = (int) ev.getY(activePointerIndex);
        int dy = lastMotionY - y;
        lastMotionY = y;
        // We're being dragged so scroll the ABL
        scroll(parent, child, dy, getMaxDragOffset(child), 0);
        break;
      case MotionEvent.ACTION_POINTER_UP:
        int newIndex = ev.getActionIndex() == 0 ? 1 : 0;
        activePointerId = ev.getPointerId(newIndex);
        lastMotionY = (int) (ev.getY(newIndex) + 0.5f);
        break;
      case MotionEvent.ACTION_UP:
        if (velocityTracker != null) {
          consumeUp = true;
          velocityTracker.addMovement(ev);
          velocityTracker.computeCurrentVelocity(1000);
          float yvel = velocityTracker.getYVelocity(activePointerId);
          fling(parent, child, -getScrollRangeForDragFling(child), 0, yvel);
        }

        // $FALLTHROUGH
      case MotionEvent.ACTION_CANCEL:
        isBeingDragged = false;
        activePointerId = INVALID_POINTER;
        if (velocityTracker != null) {
          velocityTracker.recycle();
          velocityTracker = null;
        }
        break;
    }

    if (velocityTracker != null) {
      velocityTracker.addMovement(ev);
    }

    return isBeingDragged || consumeUp;
  }

  int setHeaderTopBottomOffset(CoordinatorLayout parent, V header, int newOffset) {
    return setHeaderTopBottomOffset(
        parent, header, newOffset, Integer.MIN_VALUE, Integer.MAX_VALUE);
  }

  int setHeaderTopBottomOffset(
      CoordinatorLayout parent, V header, int newOffset, int minOffset, int maxOffset) {
    final int curOffset = getTopAndBottomOffset();
    int consumed = 0;

    if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
      // If we have some scrolling range, and we're currently within the min and max
      // offsets, calculate a new offset
      newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);

      if (curOffset != newOffset) {
        setTopAndBottomOffset(newOffset);
        // Update how much dy we have consumed
        consumed = curOffset - newOffset;
      }
    }

    return consumed;
  }

  int getTopBottomOffsetForScrollingSibling() {
    return getTopAndBottomOffset();
  }

  final int scroll(
      CoordinatorLayout coordinatorLayout, V header, int dy, int minOffset, int maxOffset) {
    return setHeaderTopBottomOffset(
        coordinatorLayout,
        header,
        getTopBottomOffsetForScrollingSibling() - dy,
        minOffset,
        maxOffset);
  }

  final boolean fling(
      CoordinatorLayout coordinatorLayout,
      @NonNull V layout,
      int minOffset,
      int maxOffset,
      float velocityY) {
    if (flingRunnable != null) {
      layout.removeCallbacks(flingRunnable);
      flingRunnable = null;
    }

    if (scroller == null) {
      scroller = new OverScroller(layout.getContext());
    }

    scroller.fling(
        0,
        getTopAndBottomOffset(), // curr
        0,
        Math.round(velocityY), // velocity.
        0,
        0, // x
        minOffset,
        maxOffset); // y

    if (scroller.computeScrollOffset()) {
      flingRunnable = new FlingRunnable(coordinatorLayout, layout);
      ViewCompat.postOnAnimation(layout, flingRunnable);
      return true;
    } else {
      onFlingFinished(coordinatorLayout, layout);
      return false;
    }
  }

  /**
   * Called when a fling has finished, or the fling was initiated but there wasn't enough velocity
   * to start it.
   */
  void onFlingFinished(CoordinatorLayout parent, V layout) {
    // no-op
  }

  /** Return true if the view can be dragged. */
  boolean canDragView(V view) {
    return false;
  }

  /** Returns the maximum px offset when {@code view} is being dragged. */
  int getMaxDragOffset(@NonNull V view) {
    return -view.getHeight();
  }

  int getScrollRangeForDragFling(@NonNull V view) {
    return view.getHeight();
  }

  private void ensureVelocityTracker() {
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
  }

  private class FlingRunnable implements Runnable {
    private final CoordinatorLayout parent;
    private final V layout;

    FlingRunnable(CoordinatorLayout parent, V layout) {
      this.parent = parent;
      this.layout = layout;
    }

    @Override
    public void run() {
      if (layout != null && scroller != null) {
        if (scroller.computeScrollOffset()) {
          setHeaderTopBottomOffset(parent, layout, scroller.getCurrY());
          // Post ourselves so that we run on the next animation
          ViewCompat.postOnAnimation(layout, this);
        } else {
          onFlingFinished(parent, layout);
        }
      }
    }
  }
}
