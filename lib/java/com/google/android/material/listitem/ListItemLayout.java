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

import static com.google.android.material.listitem.ListItemUtils.isRightAligned;
import static com.google.android.material.listitem.RevealableListItem.PRIMARY_ACTION_SWIPE_DIRECT;
import static com.google.android.material.listitem.RevealableListItem.PRIMARY_ACTION_SWIPE_DISABLED;
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
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.listitem.RevealableListItem.RevealGravity;
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
 *
 * <p>ListItemLayout supports swipe-to-reveal via a single {@link SwipeableListItem} and up to 2
 * {@link RevealableListItem}s. The {@link SwipeableListItem} and {@link RevealableListItem}s must
 * be direct children of the ListItemLayout. {@link RevealableListItem}s can be set to the start or
 * end of the {@link SwipeableListItem} via android:layout_gravity. There cannot be more than 1
 * {@link RevealableListItem} with the same layout_gravity.
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
  @Nullable private View swipeToRevealLayoutLeft;
  @Nullable private View swipeToRevealLayoutRight;
  @Nullable private RevealableListItem activeSwipeToRevealLayout;
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
    @RevealGravity private int targetRevealGravity;
    private boolean isContinueSettlingRunnablePosted;

    private final Runnable continueSettlingRunnable =
        () -> {
          isContinueSettlingRunnablePosted = false;
          if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
            continueSettlingToState(targetSwipeState, targetRevealGravity);
          } else if (swipeState == STATE_SETTLING) {
            setSwipeStateInternal(targetSwipeState, targetRevealGravity);
          }
          // In other cases, settling has been interrupted by certain UX interactions. Do nothing.
        };

    private void continueSettlingToState(
        @StableSwipeState int targetSwipeState, @RevealGravity int targetRevealGravity) {
      this.targetSwipeState = targetSwipeState;
      this.targetRevealGravity = targetRevealGravity;
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
    if (child instanceof RevealableListItem) {
      if (isRightAligned(child)) {
        if (swipeToRevealLayoutRight != null) {
          throw new UnsupportedOperationException(
              "Only one RevealableListItem with end gravity is supported.");
        }
        swipeToRevealLayoutRight = child;
      } else {
        if (swipeToRevealLayoutLeft != null) {
          throw new UnsupportedOperationException(
              "Only one RevealableListItem with start gravity is supported.");
        }
        swipeToRevealLayoutLeft = child;
      }
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
    if (child == swipeToRevealLayoutLeft) {
      swipeToRevealLayoutLeft = null;
    } else if (child == swipeToRevealLayoutRight) {
      swipeToRevealLayoutRight = null;
    } else if (contentView == child) {
      contentView = null;
    }

    if (!swipeToRevealLayoutExists() || contentView == null) {
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
    if (!swipeToRevealLayoutExists() || contentView == null) {
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
            if (swipeToRevealLayoutExists() && contentView != null) {
              viewDragHelper.captureChildView(contentView, pointerId);
            }
            return false;
          }

          @Override
          public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
            if (!(contentView instanceof SwipeableListItem && swipeToRevealLayoutExists())) {
              return 0;
            }
            SwipeableListItem swipeableItem = (SwipeableListItem) contentView;
            int minClamp = originalContentViewLeft;
            int maxClamp = originalContentViewLeft;

            if (swipeToRevealLayoutRight instanceof RevealableListItem) {
              int maxDistance =
                  calculateMaxSwipeDistance((RevealableListItem) swipeToRevealLayoutRight);
              minClamp =
                  originalContentViewLeft - (maxDistance + swipeableItem.getSwipeMaxOvershoot());
            }

            if (swipeToRevealLayoutLeft instanceof RevealableListItem) {
              int maxDistance =
                  calculateMaxSwipeDistance((RevealableListItem) swipeToRevealLayoutLeft);
              maxClamp =
                  originalContentViewLeft + (maxDistance + swipeableItem.getSwipeMaxOvershoot());
            }

            return Math.max(minClamp, Math.min(left, maxClamp));
          }

          /** Calculates the maximum required distance for a revealable layout. */
          private int calculateMaxSwipeDistance(@NonNull RevealableListItem revealView) {
            MarginLayoutParams revealViewLp =
                (MarginLayoutParams) ((View) revealView).getLayoutParams();

            if (revealView.getPrimaryActionSwipeMode() != PRIMARY_ACTION_SWIPE_DISABLED) {
              MarginLayoutParams contentViewLp = (MarginLayoutParams) contentView.getLayoutParams();
              int margin =
                  isRightAligned((View) revealView)
                      ? contentViewLp.leftMargin
                      : contentViewLp.rightMargin;
              return contentView.getMeasuredWidth() + margin;
            } else {
              return revealView.getIntrinsicWidth()
                  + revealViewLp.getMarginStart()
                  + revealViewLp.getMarginEnd();
            }
          }

          @Override
          public int getViewHorizontalDragRange(@NonNull View child) {
            int range = 0;
            if (contentView instanceof SwipeableListItem) {
              SwipeableListItem item = (SwipeableListItem) contentView;
              if (swipeToRevealLayoutLeft instanceof RevealableListItem) {
                range +=
                    ((RevealableListItem) swipeToRevealLayoutLeft).getIntrinsicWidth()
                        + item.getSwipeMaxOvershoot();
              }
              if (swipeToRevealLayoutRight instanceof RevealableListItem) {
                range +=
                    ((RevealableListItem) swipeToRevealLayoutRight).getIntrinsicWidth()
                        + item.getSwipeMaxOvershoot();
              }
            }
            return range;
          }

          @Override
          public void onViewPositionChanged(
              @NonNull View changedView, int left, int top, int dx, int dy) {
            if (viewDragHelper == null
                || !(contentView instanceof SwipeableListItem && swipeToRevealLayoutExists())) {
              return;
            }
            super.onViewPositionChanged(changedView, left, top, dx, dy);
            updateSwipeProgress(left);
            if (viewDragHelper.getViewDragState() == ViewDragHelper.STATE_DRAGGING
                && activeSwipeToRevealLayout != null) {
              setSwipeStateInternal(
                  STATE_DRAGGING, getAbsoluteRevealGravity((View) activeSwipeToRevealLayout));
            }
          }

          @Override
          public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
            if (contentView instanceof SwipeableListItem && swipeToRevealLayoutExists()) {
              int currentLeft = releasedChild.getLeft();
              // No settling necessary
              if (currentLeft == originalContentViewLeft) {
                return;
              }
              boolean isRevealingLeft = currentLeft > originalContentViewLeft;
              @RevealGravity int absoluteGravity = isRevealingLeft ? Gravity.LEFT : Gravity.RIGHT;
              RevealableListItem revealLayout =
                  absoluteGravity == Gravity.LEFT
                      ? (RevealableListItem) swipeToRevealLayoutLeft
                      : (RevealableListItem) swipeToRevealLayoutRight;
              if (revealLayout == null) {
                // This shouldn't be the case if we are able to reveal in the given direction
                // according to the clamp values.
                return;
              }
              @StableSwipeState
              int targetSwipeState =
                  calculateTargetSwipeState(absoluteGravity, revealLayout, xvel, currentLeft);
              startSettling(contentView, targetSwipeState, absoluteGravity);
            }
          }

          private int calculateTargetSwipeState(
              @RevealGravity int absoluteGravity,
              @NonNull RevealableListItem revealLayout,
              float xvel,
              int swipeViewLeft) {
            if (!swipeToRevealLayoutExistsForGravity(absoluteGravity)) {
              return STATE_CLOSED;
            }
            float effectiveXvel = absoluteGravity == Gravity.LEFT ? xvel : -xvel;
            return calculateTargetSwipeStateForRevealLayout(
                swipeViewLeft,
                effectiveXvel,
                revealLayout,
                getSwipeRevealViewRevealedOffset(absoluteGravity),
                getSwipeToActionOffset(absoluteGravity));
          }

          /**
           * Calculates the target swipe state (open, primary action, or closed) based on current
           * position, velocity, and reveal gravity.
           */
          private int calculateTargetSwipeStateForRevealLayout(
              int currentLeft,
              float effectiveXvel,
              @NonNull RevealableListItem swipeToRevealLayout,
              int revealedOffset,
              int primaryActionOffset) {
            boolean primaryActionEnabled =
                swipeToRevealLayout.getPrimaryActionSwipeMode() != PRIMARY_ACTION_SWIPE_DISABLED;
            boolean swipeDirectlyToPrimaryAction =
                swipeToRevealLayout.getPrimaryActionSwipeMode() == PRIMARY_ACTION_SWIPE_DIRECT;

            // Fast fling to open
            if (effectiveXvel > DEFAULT_SIGNIFICANT_VEL_THRESHOLD) {
              return !primaryActionEnabled
                      || (lastStableSwipeState == STATE_CLOSED && !swipeDirectlyToPrimaryAction)
                  ? STATE_OPEN
                  : STATE_SWIPE_PRIMARY_ACTION;
            }

            // Fast fling to close
            if (effectiveXvel < -DEFAULT_SIGNIFICANT_VEL_THRESHOLD) {
              return !swipeDirectlyToPrimaryAction
                      && lastStableSwipeState == STATE_SWIPE_PRIMARY_ACTION
                  ? STATE_OPEN
                  : STATE_CLOSED;
            }

            // If closer to the primary action offset than the regular revealed offset, go
            // primary.
            if (primaryActionEnabled
                && abs(currentLeft - primaryActionOffset) < abs(currentLeft - revealedOffset)) {
              return STATE_SWIPE_PRIMARY_ACTION;
            }

            // The target offset for this check is either primary action offset (if skipping
            // open revealed state) or the revealed offset.
            int targetOpenOffset =
                primaryActionEnabled && swipeDirectlyToPrimaryAction
                    ? primaryActionOffset
                    : revealedOffset;

            // If closer to the target open offset than the closed offset, go to target open
            // state.
            if (abs(currentLeft - targetOpenOffset)
                < abs(currentLeft - getSwipeViewClosedOffset())) {
              return primaryActionEnabled && swipeDirectlyToPrimaryAction
                  ? STATE_SWIPE_PRIMARY_ACTION
                  : STATE_OPEN;
            }

            return STATE_CLOSED;
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
      private void addSwipeAccessibilityActions(
          @Nullable View revealLayout, AccessibilityNodeInfoCompat infoCompat) {
        if (revealLayout instanceof ViewGroup) {
          ViewGroup revealViewGroup = (ViewGroup) revealLayout;
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

      private boolean performRevealViewAction(@Nullable View revealLayout, int action) {
        if (revealLayout instanceof ViewGroup) {
          ViewGroup revealViewGroup = (ViewGroup) revealLayout;
          for (int i = 0; i < revealViewGroup.getChildCount(); i++) {
            View child = revealViewGroup.getChildAt(i);
            if (getAccessibilityActionId(child) == action) {
              return child.performClick();
            }
          }
        }
        return false;
      }

      private boolean shouldAddAccessibilityAction(View child) {
        return child.isClickable()
            && child.getContentDescription() != null
            && child.isEnabled()
            && child.getVisibility() == View.VISIBLE;
      }

      @Override
      public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
        super.onInitializeAccessibilityNodeInfo(host, info);
        AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
        addSwipeAccessibilityActions(swipeToRevealLayoutLeft, infoCompat);
        addSwipeAccessibilityActions(swipeToRevealLayoutRight, infoCompat);
      }

      @Override
      public boolean performAccessibilityAction(View host, int action, Bundle args) {
        if (performRevealViewAction(swipeToRevealLayoutLeft, action)) {
          return true;
        }
        if (performRevealViewAction(swipeToRevealLayoutRight, action)) {
          return true;
        }
        return super.performAccessibilityAction(host, action, args);
      }

      private int getAccessibilityActionId(View child) {
        return child.getId();
      }
    };
  }

  private int getSwipeRevealViewRevealedOffset(@RevealGravity int gravity) {
    View revealLayout =
        isRevealGravityLeft(gravity) ? swipeToRevealLayoutLeft : swipeToRevealLayoutRight;
    if (revealLayout == null) {
      return 0;
    }
    LayoutParams lp = (LayoutParams) revealLayout.getLayoutParams();
    int revealViewTotalWidth =
        ((RevealableListItem) revealLayout).getIntrinsicWidth() + lp.leftMargin + lp.rightMargin;
    int direction = isRevealGravityLeft(gravity) ? 1 : -1;
    return originalContentViewLeft + direction * revealViewTotalWidth;
  }

  private int getSwipeViewClosedOffset() {
    return originalContentViewLeft;
  }

  private int getSwipeToActionOffset(@RevealGravity int revealedGravity) {
    if (contentView == null) {
      return 0;
    }
    LayoutParams lp = (LayoutParams) contentView.getLayoutParams();
    int width = contentView.getMeasuredWidth() + lp.leftMargin + lp.rightMargin;
    int direction = isRevealGravityLeft(revealedGravity) ? 1 : -1;
    return originalContentViewLeft + direction * width;
  }

  private boolean isRevealGravityLeft(@RevealGravity int gravity) {
    return getAbsoluteHorizontalGravity(gravity) == Gravity.LEFT;
  }

  /** Normalizes gravity to an absolute horizontal value (Gravity.LEFT or Gravity.RIGHT). */
  @RevealGravity
  private int getAbsoluteHorizontalGravity(@RevealGravity int gravity) {
    int horizontalGravity =
        GravityCompat.getAbsoluteGravity(gravity, getLayoutDirection())
            & Gravity.HORIZONTAL_GRAVITY_MASK;
    if (horizontalGravity == Gravity.LEFT) {
      return Gravity.LEFT;
    }
    if (horizontalGravity == Gravity.RIGHT) {
      return Gravity.RIGHT;
    }
    // If absolute gravity is not LEFT or RIGHT, we default to Gravity.END values (LEFT in RTL and
    // RIGHT in LTR).
    return getLayoutDirection() == LAYOUT_DIRECTION_RTL ? Gravity.LEFT : Gravity.RIGHT;
  }

  private boolean swipeToRevealLayoutExistsForGravity(@RevealGravity int gravity) {
    // Make sure the reveal layouts are associated with the correct gravities, in case there are any
    // swaps.
    maybeSwapRevealLayoutsForGravity();

    if (isRevealGravityLeft(gravity)) {
      return swipeToRevealLayoutLeft instanceof RevealableListItem;
    } else {
      return swipeToRevealLayoutRight instanceof RevealableListItem;
    }
  }

  private int getOffsetForSwipeState(
      @StableSwipeState int swipeState, @RevealGravity int revealGravity) {
    if (!swipeToRevealLayoutExistsForGravity(revealGravity)) {
      throw new IllegalArgumentException("No RevealableListItem with gravity " + revealGravity);
    }
    switch (swipeState) {
      case STATE_CLOSED:
        return getSwipeViewClosedOffset();
      case STATE_OPEN:
        return getSwipeRevealViewRevealedOffset(revealGravity);
      case STATE_SWIPE_PRIMARY_ACTION:
        return getSwipeToActionOffset(revealGravity);
      default:
        throw new IllegalArgumentException("Invalid state to get swipe offset: " + swipeState);
    }
  }

  private void updateSwipeProgress(int left) {
    if (!(contentView instanceof SwipeableListItem && swipeToRevealLayoutExists())) {
      return;
    }
    revealViewOffset = left - originalContentViewLeft;
    boolean revealingLeft = revealViewOffset > 0;
    boolean revealingRight = revealViewOffset < 0;

    // Update the activeSwipeToRevealLayout
    if (revealingLeft && swipeToRevealLayoutLeft instanceof RevealableListItem) {
      activeSwipeToRevealLayout = (RevealableListItem) swipeToRevealLayoutLeft;
    } else if (revealingRight && swipeToRevealLayoutRight instanceof RevealableListItem) {
      activeSwipeToRevealLayout = (RevealableListItem) swipeToRevealLayoutRight;
    }

    LayoutParams contentViewLp = (LayoutParams) contentView.getLayoutParams();

    if (swipeToRevealLayoutLeft instanceof RevealableListItem) {
      LayoutParams revealViewLp = (LayoutParams) swipeToRevealLayoutLeft.getLayoutParams();
      int revealViewDesiredWidth =
          max(
              0,
              abs(originalContentViewLeft - contentView.getLeft())
                  - contentViewLp.leftMargin // only left margin matters here for left reveal layout
                  - revealViewLp.getMarginStart()
                  - revealViewLp.getMarginEnd());

      revealViewDesiredWidth =
          revealingLeft ? revealViewDesiredWidth : 0; // If not revealing left, width is 0.

      ((RevealableListItem) swipeToRevealLayoutLeft).setRevealedWidth(revealViewDesiredWidth);
    }

    if (swipeToRevealLayoutRight instanceof RevealableListItem) {
      LayoutParams revealViewLp = (LayoutParams) swipeToRevealLayoutRight.getLayoutParams();
      // Desired width is how much we've displaced the content view minus any margins.
      int revealViewDesiredWidth =
          max(
              0,
              abs(originalContentViewLeft - contentView.getLeft())
                  - contentViewLp.rightMargin // only right margin matters here
                  - revealViewLp.getMarginStart()
                  - revealViewLp.getMarginEnd());

      revealViewDesiredWidth =
          revealingRight ? revealViewDesiredWidth : 0; // If not revealing right, width is 0.

      ((RevealableListItem) swipeToRevealLayoutRight).setRevealedWidth(revealViewDesiredWidth);
    }

    ((SwipeableListItem) contentView).onSwipe(revealViewOffset);

    if (revealingRight && swipeToRevealLayoutRight instanceof RevealableListItem) {
      updateAlphaFade(
          getSwipeToActionOffset(Gravity.RIGHT), getSwipeRevealViewRevealedOffset(Gravity.RIGHT));
    } else if (revealingLeft && swipeToRevealLayoutLeft instanceof RevealableListItem) {
      updateAlphaFade(
          getSwipeToActionOffset(Gravity.LEFT), getSwipeRevealViewRevealedOffset(Gravity.LEFT));
    } else {
      // Otherwise we're in the closed state.
      contentView.setAlpha(1f);
    }
  }

  private void updateAlphaFade(int fullSwipedOffset, int revealedOffset) {
    int fadeOutThreshold =
        (revealedOffset == fullSwipedOffset)
            ? (fullSwipedOffset + getSwipeViewClosedOffset()) / 2
            : (fullSwipedOffset + revealedOffset) / 2;

    float contentViewAlpha =
        AnimationUtils.lerp(
            /* startValue= */ 1f,
            /* endValue= */ 0f,
            /* fraction= */ (float) (revealViewOffset - fadeOutThreshold)
                / (fullSwipedOffset - fadeOutThreshold));
    contentView.setAlpha(contentViewAlpha);
  }

  private void startSettling(
      View contentView, @StableSwipeState int targetSwipeState, @RevealGravity int revealGravity) {
    if (viewDragHelper == null) {
      return;
    }
    int left = getOffsetForSwipeState(targetSwipeState, revealGravity);
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
      setSwipeStateInternal(STATE_SETTLING, revealGravity);
      stateSettlingTracker.continueSettlingToState(targetSwipeState, revealGravity);
    } else {
      setSwipeStateInternal(targetSwipeState, revealGravity);
    }
  }

  private void setSwipeStateInternal(
      @SwipeState int swipeState, @RevealGravity int revealLayoutGravity) {
    revealLayoutGravity = getAbsoluteHorizontalGravity(revealLayoutGravity);
    if (swipeState == this.swipeState
        && (activeSwipeToRevealLayout == null
            || getAbsoluteRevealGravity((View) activeSwipeToRevealLayout) == revealLayoutGravity)) {
      return;
    }
    // If the swipe state is not closed and there doesn't exist a swipe state for the given
    // alignment, this is a no-op.
    if (swipeState != STATE_CLOSED && !swipeToRevealLayoutExistsForGravity(revealLayoutGravity)) {
      return;
    }

    // If swipe to action is not supported but the swipe state to be set is
    // STATE_SWIPE_PRIMARY_ACTION, we do nothing.
    if (swipeState == STATE_SWIPE_PRIMARY_ACTION
        && (activeSwipeToRevealLayout == null
            || activeSwipeToRevealLayout.getPrimaryActionSwipeMode()
                == PRIMARY_ACTION_SWIPE_DISABLED)) {
      return;
    }
    // Ensure that the active swipe to reveal layout is set accordingly to the revealLayoutGravity.
    activeSwipeToRevealLayout =
        isRevealGravityLeft(revealLayoutGravity)
            ? (RevealableListItem) swipeToRevealLayoutLeft
            : (RevealableListItem) swipeToRevealLayoutRight;
    this.swipeState = swipeState;
    if (swipeState != STATE_DRAGGING && swipeState != STATE_SETTLING) {
      this.lastStableSwipeState = swipeState;
    }

    // For the callback, we want to pass the original gravity of the given reveal layout
    int originalGravity = revealLayoutGravity;
    if (activeSwipeToRevealLayout != null) {
      originalGravity =
          ((LayoutParams) ((View) activeSwipeToRevealLayout).getLayoutParams()).gravity;
    }
    ((SwipeableListItem) contentView)
        .onSwipeStateChanged(
            swipeState,
            castToView(activeSwipeToRevealLayout),
            originalGravity == -1 ? Gravity.END : originalGravity);
  }

  @SuppressWarnings("unchecked")
  private <T extends View & RevealableListItem> T castToView(
      RevealableListItem revealableListItem) {
    return (T) revealableListItem;
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   * @param revealView The {@link RevealableListItem} view to affect when setting the swipe state.
   */
  public <T extends View & RevealableListItem> void setSwipeState(
      @StableSwipeState int swipeState, @NonNull T revealView) {
    setSwipeState(swipeState, revealView, /* animate= */ true);
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   * @param revealView The {@link RevealableListItem} view to reveal when swiping.
   * @param animate Whether to animate to the given swipe state.
   */
  public <T extends View & RevealableListItem> void setSwipeState(
      @StableSwipeState int swipeState, @NonNull T revealView, boolean animate) {
    if (revealView != swipeToRevealLayoutLeft && revealView != swipeToRevealLayoutRight) {
      throw new IllegalArgumentException("revealView must be a child of ListItemLayout.");
    }
    setSwipeState(swipeState, ((LayoutParams) revealView.getLayoutParams()).gravity, animate);
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   * @param revealGravity The gravity of the {@link RevealableListItem} to affect when setting the
   *     swipe state.
   */
  public void setSwipeState(@StableSwipeState int swipeState, @RevealGravity int revealGravity) {
    setSwipeState(swipeState, revealGravity, /* animate= */ true);
  }

  /**
   * Sets the state to swipe the {@link SwipeableListItem} child to.
   *
   * @param swipeState The state to swipe to. This must be one of {@link
   *     SwipeableListItem#STATE_CLOSED}, {@link SwipeableListItem#STATE_OPEN}, or {@link
   *     SwipeableListItem#STATE_SWIPE_PRIMARY_ACTION}
   * @param revealGravity The gravity of the {@link RevealableListItem} to reveal when swiping.
   * @param animate Whether to animate to the given swipe state.
   */
  public void setSwipeState(
      @StableSwipeState int swipeState, @RevealGravity int revealGravity, boolean animate) {
    if (swipeState != STATE_CLOSED
        && swipeState != STATE_OPEN
        && swipeState != STATE_SWIPE_PRIMARY_ACTION) {
      throw new IllegalArgumentException("Invalid swipe state: " + swipeState);
    } else if (!(contentView instanceof SwipeableListItem) || !swipeToRevealLayoutExists()) {
      throw new IllegalArgumentException(
          "ListItemLayout must have a SwipeableListItem child and a RevealableListItem child to be"
              + " swiped.");
    } else if (swipeState != STATE_CLOSED && !swipeToRevealLayoutExistsForGravity(revealGravity)) {
      throw new IllegalArgumentException(
          "No RevealableListItem is defined for the given gravity: " + revealGravity);
    }

    Runnable runnable =
        () -> {
          if (!animate) {
            if (viewDragHelper != null) {
              viewDragHelper.abort();
            }
            int finalLeft = getOffsetForSwipeState(swipeState, revealGravity);
            contentView.offsetLeftAndRight(finalLeft - contentView.getLeft());
            updateSwipeProgress(finalLeft);
            setSwipeStateInternal(swipeState, revealGravity);
          } else {
            startSettling(contentView, swipeState, revealGravity);
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

  private boolean swipeToRevealLayoutExists() {
    return swipeToRevealLayoutLeft instanceof RevealableListItem
        || swipeToRevealLayoutRight instanceof RevealableListItem;
  }

  private void maybeSwapRevealLayoutsForGravity() {
    boolean leftIsMisaligned =
        swipeToRevealLayoutLeft != null && isRightAligned(swipeToRevealLayoutLeft);
    boolean rightIsMisaligned =
        swipeToRevealLayoutRight != null && !isRightAligned(swipeToRevealLayoutRight);

    if (leftIsMisaligned && rightIsMisaligned) {
      View temp = swipeToRevealLayoutLeft;
      swipeToRevealLayoutLeft = swipeToRevealLayoutRight;
      swipeToRevealLayoutRight = temp;
      // If we swap, the offset should flip to keep the same active reveal layout.
      revealViewOffset *= -1;
    } else if (leftIsMisaligned) {
      // If there is already an end gravity swipeToRevealLayout, we cannot replace it.
      if (swipeToRevealLayoutRight != null) {
        throw new IllegalStateException(
            "Cannot have more than one RevealableListItem with the same absolute gravity.");
      }
      swipeToRevealLayoutRight = swipeToRevealLayoutLeft;
      swipeToRevealLayoutLeft = null;
      revealViewOffset *= -1;
    } else if (rightIsMisaligned) {
      // If there is already an start gravity swipeToRevealLayout, we cannot replace it.
      if (swipeToRevealLayoutLeft != null) {
        throw new IllegalStateException(
            "Cannot have more than one RevealableListItem with the same absolute gravity.");
      }
      swipeToRevealLayoutLeft = swipeToRevealLayoutRight;
      swipeToRevealLayoutRight = null;
      revealViewOffset *= -1;
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    maybeSwapRevealLayoutsForGravity();

    if (contentView != null && swipeToRevealLayoutExists() && ensureSwipeToRevealSetupIfNeeded()) {
      originalContentViewLeft = contentView.getLeft();
      int originalContentViewRight = contentView.getRight();
      contentView.offsetLeftAndRight(revealViewOffset);

      if (swipeToRevealLayoutLeft != null) {
        layoutRevealView(
            swipeToRevealLayoutLeft, originalContentViewLeft, originalContentViewRight);
      }

      if (swipeToRevealLayoutRight != null) {
        layoutRevealView(
            swipeToRevealLayoutRight, originalContentViewLeft, originalContentViewRight);
      }
    }
  }

  private void layoutRevealView(
      @NonNull View swipeToRevealLayout, int contentLeft, int contentRight) {
    // We always lay out the end swipeToRevealLayout such that the end is aligned to where the
    // original content view's end was. Note that if the content view had an end margin, it will
    // effectively be passed onto the reveal view. Opposite for the start swipeToRevealLayout.
    LayoutParams lp = (LayoutParams) swipeToRevealLayout.getLayoutParams();
    int swipeToRevealLeft;
    int swipeToRevealRight;

    if (isRightAligned(swipeToRevealLayout)) {
      // Reveal layout is aligned to the right edge of the content view.
      swipeToRevealRight = contentRight - lp.rightMargin;
      swipeToRevealLeft = swipeToRevealRight - swipeToRevealLayout.getMeasuredWidth();
    } else {
      // Reveal layout is aligned to the left edge of the content view.
      swipeToRevealLeft = contentLeft + lp.leftMargin;
      swipeToRevealRight = swipeToRevealLeft + swipeToRevealLayout.getMeasuredWidth();
    }

    swipeToRevealLayout.layout(
        swipeToRevealLeft,
        swipeToRevealLayout.getTop(),
        swipeToRevealRight,
        swipeToRevealLayout.getBottom());
  }

  @RevealGravity
  private int getAbsoluteRevealGravity(@NonNull View revealView) {
    return isRightAligned(revealView) ? Gravity.RIGHT : Gravity.LEFT;
  }
}
