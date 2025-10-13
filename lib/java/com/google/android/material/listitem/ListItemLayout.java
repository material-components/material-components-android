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

import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.content.Context;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.customview.widget.ViewDragHelper;
import java.lang.ref.WeakReference;

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

  @Nullable private int[] positionState;

  @Nullable private ViewDragHelper viewDragHelper;
  @Nullable private GestureDetector gestureDetector;
  private WeakReference<ListItemRevealLayout> swipeToRevealLayoutRef;

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
    if (swipeToRevealLayoutRef != null
        && swipeToRevealLayoutRef.get() != null
        && child instanceof ListItemRevealLayout) {
      throw new UnsupportedOperationException(
          "Only one ListItemRevealLayout is supported in a ListItemLayout.");
    } else if (child instanceof ListItemRevealLayout) {
      swipeToRevealLayoutRef = new WeakReference<>((ListItemRevealLayout) child);
    }
  }

  @Override
  public void onViewRemoved(View child) {
    super.onViewRemoved(child);
    if (child instanceof ListItemRevealLayout) {
      viewDragHelper = null;
      gestureDetector = null;
      swipeToRevealLayoutRef = null;
    }
  }

  @Override
  public boolean onTouchEvent(MotionEvent ev) {
    if (ensureSwipeToRevealSetupIfNeeded() && viewDragHelper != null && gestureDetector != null) {
      // TODO - b/447218120: Check that at least one child is a ListItemRevealLayout and the other
      //  is List content.
      // Process the event
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
    if (swipeToRevealLayoutRef == null || swipeToRevealLayoutRef.get() == null) {
      return false;
    }
    if (viewDragHelper == null || gestureDetector == null) {
      viewDragHelper =
          ViewDragHelper.create(
              this,
              new ViewDragHelper.Callback() {
                @Override
                public boolean tryCaptureView(@NonNull View child, int pointerId) {
                  return false;
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
    return true;
  }
}
