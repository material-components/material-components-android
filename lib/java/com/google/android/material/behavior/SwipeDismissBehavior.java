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

package com.google.android.material.behavior;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.widget.ViewDragHelper;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * An interaction behavior plugin for child views of {@link CoordinatorLayout} to provide support
 * for the 'swipe-to-dismiss' gesture.
 */
public class SwipeDismissBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  /** A view is not currently being dragged or animating as a result of a fling/snap. */
  public static final int STATE_IDLE = ViewDragHelper.STATE_IDLE;

  /**
   * A view is currently being dragged. The position is currently changing as a result of user input
   * or simulated user input.
   */
  public static final int STATE_DRAGGING = ViewDragHelper.STATE_DRAGGING;

  /**
   * A view is currently settling into place as a result of a fling or predefined non-interactive
   * motion.
   */
  public static final int STATE_SETTLING = ViewDragHelper.STATE_SETTLING;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({SWIPE_DIRECTION_START_TO_END, SWIPE_DIRECTION_END_TO_START, SWIPE_DIRECTION_ANY})
  @Retention(RetentionPolicy.SOURCE)
  private @interface SwipeDirection {}

  /**
   * Swipe direction that only allows swiping in the direction of start-to-end. That is
   * left-to-right in LTR, or right-to-left in RTL.
   */
  public static final int SWIPE_DIRECTION_START_TO_END = 0;

  /**
   * Swipe direction that only allows swiping in the direction of end-to-start. That is
   * right-to-left in LTR or left-to-right in RTL.
   */
  public static final int SWIPE_DIRECTION_END_TO_START = 1;

  /** Swipe direction which allows swiping in either direction. */
  public static final int SWIPE_DIRECTION_ANY = 2;

  private static final float DEFAULT_DRAG_DISMISS_THRESHOLD = 0.5f;
  private static final float DEFAULT_ALPHA_START_DISTANCE = 0f;
  private static final float DEFAULT_ALPHA_END_DISTANCE = DEFAULT_DRAG_DISMISS_THRESHOLD;

  ViewDragHelper viewDragHelper;
  OnDismissListener listener;
  private boolean interceptingEvents;
  private boolean requestingDisallowInterceptTouchEvent;

  private float sensitivity = 0f;
  private boolean sensitivitySet;

  int swipeDirection = SWIPE_DIRECTION_ANY;
  float dragDismissThreshold = DEFAULT_DRAG_DISMISS_THRESHOLD;
  float alphaStartSwipeDistance = DEFAULT_ALPHA_START_DISTANCE;
  float alphaEndSwipeDistance = DEFAULT_ALPHA_END_DISTANCE;

  /** Callback interface used to notify the application that the view has been dismissed. */
  public interface OnDismissListener {
    /** Called when {@code view} has been dismissed via swiping. */
    public void onDismiss(View view);

    /**
     * Called when the drag state has changed.
     *
     * @param state the new state. One of {@link #STATE_IDLE}, {@link #STATE_DRAGGING} or {@link
     *     #STATE_SETTLING}.
     */
    public void onDragStateChanged(int state);
  }

  /**
   * Set the listener to be used when a dismiss event occurs.
   *
   * @param listener the listener to use.
   */
  public void setListener(@Nullable OnDismissListener listener) {
    this.listener = listener;
  }

  @VisibleForTesting
  @Nullable
  public OnDismissListener getListener() {
    return listener;
  }

  /**
   * Sets the swipe direction for this behavior.
   *
   * @param direction one of the {@link #SWIPE_DIRECTION_START_TO_END}, {@link
   *     #SWIPE_DIRECTION_END_TO_START} or {@link #SWIPE_DIRECTION_ANY}
   */
  public void setSwipeDirection(@SwipeDirection int direction) {
    swipeDirection = direction;
  }

  /**
   * Set the threshold for telling if a view has been dragged enough to be dismissed.
   *
   * @param distance a ratio of a view's width, values are clamped to 0 >= x <= 1f;
   */
  public void setDragDismissDistance(float distance) {
    dragDismissThreshold = clamp(0f, distance, 1f);
  }

  /**
   * The minimum swipe distance before the view's alpha is modified.
   *
   * @param fraction the distance as a fraction of the view's width.
   */
  public void setStartAlphaSwipeDistance(float fraction) {
    alphaStartSwipeDistance = clamp(0f, fraction, 1f);
  }

  /**
   * The maximum swipe distance for the view's alpha is modified.
   *
   * @param fraction the distance as a fraction of the view's width.
   */
  public void setEndAlphaSwipeDistance(float fraction) {
    alphaEndSwipeDistance = clamp(0f, fraction, 1f);
  }

  /**
   * Set the sensitivity used for detecting the start of a swipe. This only takes effect if no touch
   * handling has occurred yet.
   *
   * @param sensitivity Multiplier for how sensitive we should be about detecting the start of a
   *     drag. Larger values are more sensitive. 1.0f is normal.
   */
  public void setSensitivity(float sensitivity) {
    this.sensitivity = sensitivity;
    sensitivitySet = true;
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull V child, int layoutDirection) {
    boolean handled = super.onLayoutChild(parent, child, layoutDirection);
    if (child.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
      child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
      updateAccessibilityActions(child);
    }
    return handled;
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    boolean dispatchEventToHelper = interceptingEvents;

    switch (event.getActionMasked()) {
      case MotionEvent.ACTION_DOWN:
        interceptingEvents =
            parent.isPointInChildBounds(child, (int) event.getX(), (int) event.getY());
        dispatchEventToHelper = interceptingEvents;
        break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        // Reset the ignore flag for next times
        interceptingEvents = false;
        break;
    }

    if (dispatchEventToHelper) {
      ensureViewDragHelper(parent);
      return !requestingDisallowInterceptTouchEvent
          && viewDragHelper.shouldInterceptTouchEvent(event);
    }
    return false;
  }

  @Override
  public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
    if (viewDragHelper != null) {
      // Don't process CANCEL sent for requesting disallow intercept touch event.
      if (!requestingDisallowInterceptTouchEvent
          || event.getActionMasked() != MotionEvent.ACTION_CANCEL) {
        viewDragHelper.processTouchEvent(event);
      }
      return true;
    }
    return false;
  }

  /**
   * Called when the user's input indicates that they want to swipe the given view.
   *
   * @param view View the user is attempting to swipe
   * @return true if the view can be dismissed via swiping, false otherwise
   */
  public boolean canSwipeDismissView(@NonNull View view) {
    return true;
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {
        private static final int INVALID_POINTER_ID = -1;

        private int originalCapturedViewLeft;
        private int activePointerId = INVALID_POINTER_ID;

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
          // Only capture if we don't already have an active pointer id
          return (activePointerId == INVALID_POINTER_ID || activePointerId == pointerId)
              && canSwipeDismissView(child);
        }

        @Override
        public void onViewCaptured(@NonNull View capturedChild, int activePointerId) {
          this.activePointerId = activePointerId;
          originalCapturedViewLeft = capturedChild.getLeft();

          // The view has been captured, and thus a drag is about to start so stop any parents
          // intercepting
          final ViewParent parent = capturedChild.getParent();
          if (parent != null) {
            // The requestDisallowInterceptTouchEvent() will send a CANCEL event to all children's
            // onInterceptTouchEvent(). This breaks the ongoing dragging event sequence. We
            // temporarily ignore all CANCEL event while requesting.
            requestingDisallowInterceptTouchEvent = true;
            parent.requestDisallowInterceptTouchEvent(true);
            requestingDisallowInterceptTouchEvent = false;
          }
        }

        @Override
        public void onViewDragStateChanged(int state) {
          if (listener != null) {
            listener.onDragStateChanged(state);
          }
        }

        @Override
        public void onViewReleased(@NonNull View child, float xVelocity, float yVelocity) {
          // Reset the active pointer ID
          activePointerId = INVALID_POINTER_ID;

          final int childWidth = child.getWidth();
          int targetLeft;
          boolean dismiss = false;

          if (shouldDismiss(child, xVelocity)) {
            targetLeft =
                xVelocity < 0f || child.getLeft() < originalCapturedViewLeft
                    ? originalCapturedViewLeft - childWidth
                    : originalCapturedViewLeft + childWidth;
            dismiss = true;
          } else {
            // Else, reset back to the original left
            targetLeft = originalCapturedViewLeft;
          }

          if (viewDragHelper.settleCapturedViewAt(targetLeft, child.getTop())) {
            child.postOnAnimation(new SettleRunnable(child, dismiss));
          } else if (dismiss && listener != null) {
            listener.onDismiss(child);
          }
        }

        private boolean shouldDismiss(@NonNull View child, float xVelocity) {
          if (xVelocity != 0f) {
            final boolean isRtl = child.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;

            if (swipeDirection == SWIPE_DIRECTION_ANY) {
              // We don't care about the direction so return true
              return true;
            } else if (swipeDirection == SWIPE_DIRECTION_START_TO_END) {
              // We only allow start-to-end swiping, so the fling needs to be in the
              // correct direction
              return isRtl ? xVelocity < 0f : xVelocity > 0f;
            } else if (swipeDirection == SWIPE_DIRECTION_END_TO_START) {
              // We only allow end-to-start swiping, so the fling needs to be in the
              // correct direction
              return isRtl ? xVelocity > 0f : xVelocity < 0f;
            }
          } else {
            final int distance = child.getLeft() - originalCapturedViewLeft;
            final int thresholdDistance = Math.round(child.getWidth() * dragDismissThreshold);
            return Math.abs(distance) >= thresholdDistance;
          }

          return false;
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
          return child.getWidth();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          final boolean isRtl = child.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
          int min;
          int max;

          if (swipeDirection == SWIPE_DIRECTION_START_TO_END) {
            if (isRtl) {
              min = originalCapturedViewLeft - child.getWidth();
              max = originalCapturedViewLeft;
            } else {
              min = originalCapturedViewLeft;
              max = originalCapturedViewLeft + child.getWidth();
            }
          } else if (swipeDirection == SWIPE_DIRECTION_END_TO_START) {
            if (isRtl) {
              min = originalCapturedViewLeft;
              max = originalCapturedViewLeft + child.getWidth();
            } else {
              min = originalCapturedViewLeft - child.getWidth();
              max = originalCapturedViewLeft;
            }
          } else {
            min = originalCapturedViewLeft - child.getWidth();
            max = originalCapturedViewLeft + child.getWidth();
          }

          return clamp(min, left, max);
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return child.getTop();
        }

        @Override
        public void onViewPositionChanged(@NonNull View child, int left, int top, int dx, int dy) {
          final float startAlphaDistance =
              child.getWidth() * alphaStartSwipeDistance;
          final float endAlphaDistance =
              child.getWidth() * alphaEndSwipeDistance;
          final float currentDistance = Math.abs(left - originalCapturedViewLeft);

          if (currentDistance <= startAlphaDistance) {
            child.setAlpha(1f);
          } else if (currentDistance >= endAlphaDistance) {
            child.setAlpha(0f);
          } else {
            // We're between the start and end distances
            final float distance = fraction(startAlphaDistance, endAlphaDistance, currentDistance);
            child.setAlpha(clamp(0f, 1f - distance, 1f));
          }
        }
      };

  private void ensureViewDragHelper(ViewGroup parent) {
    if (viewDragHelper == null) {
      viewDragHelper =
          sensitivitySet
              ? ViewDragHelper.create(parent, sensitivity, dragCallback)
              : ViewDragHelper.create(parent, dragCallback);
    }
  }

  private class SettleRunnable implements Runnable {
    private final View view;
    private final boolean dismiss;

    SettleRunnable(View view, boolean dismiss) {
      this.view = view;
      this.dismiss = dismiss;
    }

    @Override
    public void run() {
      if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
        view.postOnAnimation(this);
      } else {
        if (dismiss && listener != null) {
          listener.onDismiss(view);
        }
      }
    }
  }

  private void updateAccessibilityActions(View child) {
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_DISMISS);
    if (canSwipeDismissView(child)) {
      ViewCompat.replaceAccessibilityAction(
          child,
          AccessibilityActionCompat.ACTION_DISMISS,
          null,
          new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
              if (canSwipeDismissView(view)) {
                final boolean isRtl = view.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
                boolean dismissToLeft =
                    (swipeDirection == SWIPE_DIRECTION_START_TO_END && isRtl)
                        || (swipeDirection == SWIPE_DIRECTION_END_TO_START && !isRtl);
                int offset = dismissToLeft ? -view.getWidth() : view.getWidth();
                ViewCompat.offsetLeftAndRight(view, offset);
                view.setAlpha(0f);
                if (listener != null) {
                  listener.onDismiss(view);
                }
                return true;
              }
              return false;
            }
          });
    }
  }

  static float clamp(float min, float value, float max) {
    return Math.min(Math.max(min, value), max);
  }

  static int clamp(int min, int value, int max) {
    return Math.min(Math.max(min, value), max);
  }

  /**
   * Retrieve the current drag state of this behavior. This will return one of {@link #STATE_IDLE},
   * {@link #STATE_DRAGGING} or {@link #STATE_SETTLING}.
   *
   * @return The current drag state
   */
  public int getDragState() {
    return viewDragHelper != null ? viewDragHelper.getViewDragState() : STATE_IDLE;
  }

  /** The fraction that {@code value} is between {@code startValue} and {@code endValue}. */
  static float fraction(float startValue, float endValue, float value) {
    return (value - startValue) / (endValue - startValue);
  }
}
