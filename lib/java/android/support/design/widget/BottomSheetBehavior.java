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

package android.support.design.widget;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.ViewDragHelper;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewParent;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;

/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as a
 * bottom sheet.
 */
public class BottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V> {

  /** Callback for monitoring events about bottom sheets. */
  public abstract static class BottomSheetCallback {

    /**
     * Called when the bottom sheet changes its state.
     *
     * @param bottomSheet The bottom sheet view.
     * @param newState The new state. This will be one of {@link #STATE_DRAGGING}, {@link
     *     #STATE_SETTLING}, {@link #STATE_EXPANDED}, {@link #STATE_COLLAPSED}, {@link
     *     #STATE_HIDDEN}, or {@link #STATE_HALF_EXPANDED}.
     */
    public abstract void onStateChanged(@NonNull View bottomSheet, @State int newState);

    /**
     * Called when the bottom sheet is being dragged.
     *
     * @param bottomSheet The bottom sheet view.
     * @param slideOffset The new offset of this bottom sheet within [-1,1] range. Offset increases
     *     as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
     *     expanded states and from -1 to 0 it is between hidden and collapsed states.
     */
    public abstract void onSlide(@NonNull View bottomSheet, float slideOffset);
  }

  /** The bottom sheet is dragging. */
  public static final int STATE_DRAGGING = 1;

  /** The bottom sheet is settling. */
  public static final int STATE_SETTLING = 2;

  /** The bottom sheet is expanded. */
  public static final int STATE_EXPANDED = 3;

  /** The bottom sheet is collapsed. */
  public static final int STATE_COLLAPSED = 4;

  /** The bottom sheet is hidden. */
  public static final int STATE_HIDDEN = 5;

  /** The bottom sheet is half-expanded (used when mFitToContents is false). */
  public static final int STATE_HALF_EXPANDED = 6;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({
    STATE_EXPANDED,
    STATE_COLLAPSED,
    STATE_DRAGGING,
    STATE_SETTLING,
    STATE_HIDDEN,
    STATE_HALF_EXPANDED
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface State {}

  /**
   * Peek at the 16:9 ratio keyline of its parent.
   *
   * <p>This can be used as a parameter for {@link #setPeekHeight(int)}. {@link #getPeekHeight()}
   * will return this when the value is set.
   */
  public static final int PEEK_HEIGHT_AUTO = -1;

  private static final float HIDE_THRESHOLD = 0.5f;

  private static final float HIDE_FRICTION = 0.1f;

  private boolean fitToContents = true;

  private float maximumVelocity;

  /** Peek height set by the user. */
  private int peekHeight;

  /** Whether or not to use automatic peek height. */
  private boolean peekHeightAuto;

  /** Minimum peek height permitted. */
  private int peekHeightMin;

  /** The last peek height calculated in onLayoutChild. */
  private int lastPeekHeight;

  int fitToContentsOffset;

  int halfExpandedOffset;

  int collapsedOffset;

  boolean hideable;

  private boolean skipCollapsed;

  @State int state = STATE_COLLAPSED;

  ViewDragHelper viewDragHelper;

  private boolean ignoreEvents;

  private int lastNestedScrollDy;

  private boolean nestedScrolled;

  int parentHeight;

  WeakReference<V> viewRef;

  WeakReference<View> nestedScrollingChildRef;

  private BottomSheetCallback callback;

  private VelocityTracker velocityTracker;

  int activePointerId;

  private int initialY;

  boolean touchingScrollingChild;

  /** Default constructor for instantiating BottomSheetBehaviors. */
  public BottomSheetBehavior() {}

  /**
   * Default constructor for inflating BottomSheetBehaviors from layout.
   *
   * @param context The {@link Context}.
   * @param attrs The {@link AttributeSet}.
   */
  public BottomSheetBehavior(Context context, AttributeSet attrs) {
    super(context, attrs);
    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout);
    TypedValue value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight);
    if (value != null && value.data == PEEK_HEIGHT_AUTO) {
      setPeekHeight(value.data);
    } else {
      setPeekHeight(
          a.getDimensionPixelSize(
              R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO));
    }
    setHideable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
    setFitToContents(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true));
    setSkipCollapsed(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false));
    a.recycle();
    ViewConfiguration configuration = ViewConfiguration.get(context);
    maximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  @Override
  public Parcelable onSaveInstanceState(CoordinatorLayout parent, V child) {
    return new SavedState(super.onSaveInstanceState(parent, child), state);
  }

  @Override
  public void onRestoreInstanceState(CoordinatorLayout parent, V child, Parcelable state) {
    SavedState ss = (SavedState) state;
    super.onRestoreInstanceState(parent, child, ss.getSuperState());
    // Intermediate states are restored as collapsed state
    if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
      this.state = STATE_COLLAPSED;
    } else {
      this.state = ss.state;
    }
  }

  @Override
  public boolean onLayoutChild(CoordinatorLayout parent, V child, int layoutDirection) {
    if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
      ViewCompat.setFitsSystemWindows(child, true);
    }
    int savedTop = child.getTop();
    // First let the parent lay it out
    parent.onLayoutChild(child, layoutDirection);
    // Offset the bottom sheet
    parentHeight = parent.getHeight();
    if (peekHeightAuto) {
      if (peekHeightMin == 0) {
        peekHeightMin =
            parent
                .getResources()
                .getDimensionPixelSize(R.dimen.design_bottom_sheet_peek_height_min);
      }
      lastPeekHeight = Math.max(peekHeightMin, parentHeight - parent.getWidth() * 9 / 16);
    } else {
      lastPeekHeight = peekHeight;
    }
    fitToContentsOffset = Math.max(0, parentHeight - child.getHeight());
    halfExpandedOffset = parentHeight / 2;
    calculateCollapsedOffset();

    if (state == STATE_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, getExpandedOffset());
    } else if (state == STATE_HALF_EXPANDED) {
      ViewCompat.offsetTopAndBottom(child, halfExpandedOffset);
    } else if (hideable && state == STATE_HIDDEN) {
      ViewCompat.offsetTopAndBottom(child, parentHeight);
    } else if (state == STATE_COLLAPSED) {
      ViewCompat.offsetTopAndBottom(child, collapsedOffset);
    } else if (state == STATE_DRAGGING || state == STATE_SETTLING) {
      ViewCompat.offsetTopAndBottom(child, savedTop - child.getTop());
    }
    if (viewDragHelper == null) {
      viewDragHelper = ViewDragHelper.create(parent, dragCallback);
    }
    viewRef = new WeakReference<>(child);
    nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
    return true;
  }

  @Override
  public boolean onInterceptTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
    if (!child.isShown()) {
      ignoreEvents = true;
      return false;
    }
    int action = event.getActionMasked();
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    switch (action) {
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_CANCEL:
        touchingScrollingChild = false;
        activePointerId = MotionEvent.INVALID_POINTER_ID;
        // Reset the ignore flag
        if (ignoreEvents) {
          ignoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        int initialX = (int) event.getX();
        initialY = (int) event.getY();
        View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
        if (scroll != null && parent.isPointInChildBounds(scroll, initialX, initialY)) {
          activePointerId = event.getPointerId(event.getActionIndex());
          touchingScrollingChild = true;
        }
        ignoreEvents =
            activePointerId == MotionEvent.INVALID_POINTER_ID
                && !parent.isPointInChildBounds(child, initialX, initialY);
        break;
      default: // fall out
    }
    if (!ignoreEvents && viewDragHelper.shouldInterceptTouchEvent(event)) {
      return true;
    }
    // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
    // it is not the top most view of its parent. This is not necessary when the touch event is
    // happening over the scrolling content as nested scrolling logic handles that case.
    View scroll = nestedScrollingChildRef.get();
    return action == MotionEvent.ACTION_MOVE
        && scroll != null
        && !ignoreEvents
        && state != STATE_DRAGGING
        && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY())
        && Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop();
  }

  @Override
  public boolean onTouchEvent(CoordinatorLayout parent, V child, MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }
    int action = event.getActionMasked();
    if (state == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
      return true;
    }
    if (viewDragHelper != null) {
      viewDragHelper.processTouchEvent(event);
    }
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
    // to capture the bottom sheet in case it is not captured and the touch slop is passed.
    if (action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
      if (Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop()) {
        viewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !ignoreEvents;
  }

  @Override
  public boolean onStartNestedScroll(
      CoordinatorLayout coordinatorLayout,
      V child,
      View directTargetChild,
      View target,
      int nestedScrollAxes) {
    lastNestedScrollDy = 0;
    nestedScrolled = false;
    return (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  @Override
  public void onNestedPreScroll(
      CoordinatorLayout coordinatorLayout, V child, View target, int dx, int dy, int[] consumed) {
    View scrollingChild = nestedScrollingChildRef.get();
    if (target != scrollingChild) {
      return;
    }
    int currentTop = child.getTop();
    int newTop = currentTop - dy;
    if (dy > 0) { // Upward
      if (newTop < getExpandedOffset()) {
        consumed[1] = currentTop - getExpandedOffset();
        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
        setStateInternal(STATE_EXPANDED);
      } else {
        consumed[1] = dy;
        ViewCompat.offsetTopAndBottom(child, -dy);
        setStateInternal(STATE_DRAGGING);
      }
    } else if (dy < 0) { // Downward
      if (!target.canScrollVertically(-1)) {
        if (newTop <= collapsedOffset || hideable) {
          consumed[1] = dy;
          ViewCompat.offsetTopAndBottom(child, -dy);
          setStateInternal(STATE_DRAGGING);
        } else {
          consumed[1] = currentTop - collapsedOffset;
          ViewCompat.offsetTopAndBottom(child, -consumed[1]);
          setStateInternal(STATE_COLLAPSED);
        }
      }
    }
    dispatchOnSlide(child.getTop());
    lastNestedScrollDy = dy;
    nestedScrolled = true;
  }

  @Override
  public void onStopNestedScroll(CoordinatorLayout coordinatorLayout, V child, View target) {
    if (child.getTop() == getExpandedOffset()) {
      setStateInternal(STATE_EXPANDED);
      return;
    }
    if (target != nestedScrollingChildRef.get() || !nestedScrolled) {
      return;
    }
    int top;
    int targetState;
    if (lastNestedScrollDy > 0) {
      top = getExpandedOffset();
      targetState = STATE_EXPANDED;
    } else if (hideable && shouldHide(child, getYVelocity())) {
      top = parentHeight;
      targetState = STATE_HIDDEN;
    } else if (lastNestedScrollDy == 0) {
      int currentTop = child.getTop();
      if (fitToContents) {
        if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
          top = fitToContentsOffset;
          targetState = STATE_EXPANDED;
        } else {
          top = collapsedOffset;
          targetState = STATE_COLLAPSED;
        }
      } else {
        if (currentTop < halfExpandedOffset) {
          if (currentTop < Math.abs(currentTop - collapsedOffset)) {
            top = 0;
            targetState = STATE_EXPANDED;
          } else {
            top = halfExpandedOffset;
            targetState = STATE_HALF_EXPANDED;
          }
        } else {
          if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
            top = halfExpandedOffset;
            targetState = STATE_HALF_EXPANDED;
          } else {
            top = collapsedOffset;
            targetState = STATE_COLLAPSED;
          }
        }
      }
    } else {
      top = collapsedOffset;
      targetState = STATE_COLLAPSED;
    }
    if (viewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      setStateInternal(STATE_SETTLING);
      ViewCompat.postOnAnimation(child, new SettleRunnable(child, targetState));
    } else {
      setStateInternal(targetState);
    }
    nestedScrolled = false;
  }

  @Override
  public boolean onNestedPreFling(
      CoordinatorLayout coordinatorLayout, V child, View target, float velocityX, float velocityY) {
    return target == nestedScrollingChildRef.get()
        && (state != STATE_EXPANDED
            || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
  }

  /**
   * @return whether the height of the expanded sheet is determined by the height of its contents,
   *     or if it is expanded in two stages (half the height of the parent container, full height of
   *     parent container).
   */
  public boolean isFitToContents() {
    return fitToContents;
  }

  /**
   * Sets whether the height of the expanded sheet is determined by the height of its contents, or
   * if it is expanded in two stages (half the height of the parent container, full height of parent
   * container). Default value is true.
   *
   * @param fitToContents whether or not to fit the expanded sheet to its contents.
   */
  public void setFitToContents(boolean fitToContents) {
    if (this.fitToContents == fitToContents) {
      return;
    }
    this.fitToContents = fitToContents;

    // If sheet is already laid out, recalculate the collapsed offset based on new setting.
    // Otherwise, let onLayoutChild handle this later.
    if (viewRef != null) {
      calculateCollapsedOffset();
    }
    // Fix incorrect expanded settings depending on whether or not we are fitting sheet to contents.
    setStateInternal((this.fitToContents && state == STATE_HALF_EXPANDED) ? STATE_EXPANDED : state);
  }

  /**
   * Sets the height of the bottom sheet when it is collapsed.
   *
   * @param peekHeight The height of the collapsed bottom sheet in pixels, or {@link
   *     #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically at 16:9 ratio keyline.
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
   */
  public final void setPeekHeight(int peekHeight) {
    boolean layout = false;
    if (peekHeight == PEEK_HEIGHT_AUTO) {
      if (!peekHeightAuto) {
        peekHeightAuto = true;
        layout = true;
      }
    } else if (peekHeightAuto || this.peekHeight != peekHeight) {
      peekHeightAuto = false;
      this.peekHeight = Math.max(0, peekHeight);
      collapsedOffset = parentHeight - peekHeight;
      layout = true;
    }
    if (layout && state == STATE_COLLAPSED && viewRef != null) {
      V view = viewRef.get();
      if (view != null) {
        view.requestLayout();
      }
    }
  }

  /**
   * Gets the height of the bottom sheet when it is collapsed.
   *
   * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_HEIGHT_AUTO} if the
   *     sheet is configured to peek automatically at 16:9 ratio keyline
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
   */
  public final int getPeekHeight() {
    return peekHeightAuto ? PEEK_HEIGHT_AUTO : peekHeight;
  }

  /**
   * Sets whether this bottom sheet can hide when it is swiped down.
   *
   * @param hideable {@code true} to make this bottom sheet hideable.
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
   */
  public void setHideable(boolean hideable) {
    this.hideable = hideable;
  }

  /**
   * Gets whether this bottom sheet can hide when it is swiped down.
   *
   * @return {@code true} if this bottom sheet can hide.
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
   */
  public boolean isHideable() {
    return hideable;
  }

  /**
   * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
   * is expanded once. Setting this to true has no effect unless the sheet is hideable.
   *
   * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
   */
  public void setSkipCollapsed(boolean skipCollapsed) {
    this.skipCollapsed = skipCollapsed;
  }

  /**
   * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
   * is expanded once.
   *
   * @return Whether the bottom sheet should skip the collapsed state.
   * @attr ref android.support.design.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
   */
  public boolean getSkipCollapsed() {
    return skipCollapsed;
  }

  /**
   * Sets a callback to be notified of bottom sheet events.
   *
   * @param callback The callback to notify when bottom sheet events occur.
   */
  public void setBottomSheetCallback(BottomSheetCallback callback) {
    this.callback = callback;
  }

  /**
   * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
   * animation.
   *
   * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, {@link #STATE_HIDDEN},
   *     or {@link #STATE_HALF_EXPANDED}.
   */
  public final void setState(@State int state) {
    if (state == this.state) {
      return;
    }
    if (viewRef == null) {
      // The view is not laid out yet; modify mState and let onLayoutChild handle it later
      if (state == STATE_COLLAPSED
          || state == STATE_EXPANDED
          || state == STATE_HALF_EXPANDED
          || (hideable && state == STATE_HIDDEN)) {
        this.state = state;
      }
      return;
    }
    final V child = viewRef.get();
    if (child == null) {
      return;
    }
    // Start the animation; wait until a pending layout if there is one.
    ViewParent parent = child.getParent();
    if (parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child)) {
      final int finalState = state;
      child.post(
          new Runnable() {
            @Override
            public void run() {
              startSettlingAnimation(child, finalState);
            }
          });
    } else {
      startSettlingAnimation(child, state);
    }
  }

  /**
   * Gets the current state of the bottom sheet.
   *
   * @return One of {@link #STATE_EXPANDED}, {@link #STATE_HALF_EXPANDED}, {@link #STATE_COLLAPSED},
   *     {@link #STATE_DRAGGING}, {@link #STATE_SETTLING}, or {@link #STATE_HALF_EXPANDED}.
   */
  @State
  public final int getState() {
    return state;
  }

  void setStateInternal(@State int state) {
    if (this.state == state) {
      return;
    }
    this.state = state;
    View bottomSheet = viewRef.get();
    if (bottomSheet != null && callback != null) {
      callback.onStateChanged(bottomSheet, state);
    }
  }

  private void calculateCollapsedOffset() {
    if (fitToContents) {
      collapsedOffset = Math.max(parentHeight - lastPeekHeight, fitToContentsOffset);
    } else {
      collapsedOffset = parentHeight - lastPeekHeight;
    }
  }

  private void reset() {
    activePointerId = ViewDragHelper.INVALID_POINTER;
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  boolean shouldHide(View child, float yvel) {
    if (skipCollapsed) {
      return true;
    }
    if (child.getTop() < collapsedOffset) {
      // It should not hide, but collapse.
      return false;
    }
    final float newTop = child.getTop() + yvel * HIDE_FRICTION;
    return Math.abs(newTop - collapsedOffset) / (float) peekHeight > HIDE_THRESHOLD;
  }

  @VisibleForTesting
  View findScrollingChild(View view) {
    if (ViewCompat.isNestedScrollingEnabled(view)) {
      return view;
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0, count = group.getChildCount(); i < count; i++) {
        View scrollingChild = findScrollingChild(group.getChildAt(i));
        if (scrollingChild != null) {
          return scrollingChild;
        }
      }
    }
    return null;
  }

  private float getYVelocity() {
    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
    return velocityTracker.getYVelocity(activePointerId);
  }

  private int getExpandedOffset() {
    return fitToContents ? fitToContentsOffset : 0;
  }

  void startSettlingAnimation(View child, int state) {
    int top;
    if (state == STATE_COLLAPSED) {
      top = collapsedOffset;
    } else if (state == STATE_HALF_EXPANDED) {
      top = halfExpandedOffset;
    } else if (state == STATE_EXPANDED) {
      top = getExpandedOffset();
    } else if (hideable && state == STATE_HIDDEN) {
      top = parentHeight;
    } else {
      throw new IllegalArgumentException("Illegal state argument: " + state);
    }
    if (viewDragHelper.smoothSlideViewTo(child, child.getLeft(), top)) {
      setStateInternal(STATE_SETTLING);
      ViewCompat.postOnAnimation(child, new SettleRunnable(child, state));
    } else {
      setStateInternal(state);
    }
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(View child, int pointerId) {
          if (state == STATE_DRAGGING) {
            return false;
          }
          if (touchingScrollingChild) {
            return false;
          }
          if (state == STATE_EXPANDED && activePointerId == pointerId) {
            View scroll = nestedScrollingChildRef.get();
            if (scroll != null && scroll.canScrollVertically(-1)) {
              // Let the content scroll up
              return false;
            }
          }
          return viewRef != null && viewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(View changedView, int left, int top, int dx, int dy) {
          dispatchOnSlide(top);
        }

        @Override
        public void onViewDragStateChanged(int state) {
          if (state == ViewDragHelper.STATE_DRAGGING) {
            setStateInternal(STATE_DRAGGING);
          }
        }

        @Override
        public void onViewReleased(View releasedChild, float xvel, float yvel) {
          int top;
          @State int targetState;
          if (yvel < 0) { // Moving up
            if (fitToContents) {
              top = fitToContentsOffset;
              targetState = STATE_EXPANDED;
            } else {
              int currentTop = releasedChild.getTop();
              if (currentTop > halfExpandedOffset) {
                top = halfExpandedOffset;
                targetState = STATE_HALF_EXPANDED;
              } else {
                top = 0;
                targetState = STATE_EXPANDED;
              }
            }
          } else if (hideable && shouldHide(releasedChild, yvel)) {
            top = parentHeight;
            targetState = STATE_HIDDEN;
          } else if (yvel == 0.f) {
            int currentTop = releasedChild.getTop();
            if (fitToContents) {
              if (Math.abs(currentTop - fitToContentsOffset)
                  < Math.abs(currentTop - collapsedOffset)) {
                top = fitToContentsOffset;
                targetState = STATE_EXPANDED;
              } else {
                top = collapsedOffset;
                targetState = STATE_COLLAPSED;
              }
            } else {
              if (currentTop < halfExpandedOffset) {
                if (currentTop < Math.abs(currentTop - collapsedOffset)) {
                  top = 0;
                  targetState = STATE_EXPANDED;
                } else {
                  top = halfExpandedOffset;
                  targetState = STATE_HALF_EXPANDED;
                }
              } else {
                if (Math.abs(currentTop - halfExpandedOffset)
                    < Math.abs(currentTop - collapsedOffset)) {
                  top = halfExpandedOffset;
                  targetState = STATE_HALF_EXPANDED;
                } else {
                  top = collapsedOffset;
                  targetState = STATE_COLLAPSED;
                }
              }
            }
          } else {
            top = collapsedOffset;
            targetState = STATE_COLLAPSED;
          }
          if (viewDragHelper.settleCapturedViewAt(releasedChild.getLeft(), top)) {
            setStateInternal(STATE_SETTLING);
            ViewCompat.postOnAnimation(
                releasedChild, new SettleRunnable(releasedChild, targetState));
          } else {
            setStateInternal(targetState);
          }
        }

        @Override
        public int clampViewPositionVertical(View child, int top, int dy) {
          return MathUtils.constrain(
              top, getExpandedOffset(), hideable ? parentHeight : collapsedOffset);
        }

        @Override
        public int clampViewPositionHorizontal(View child, int left, int dx) {
          return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(View child) {
          if (hideable) {
            return parentHeight;
          } else {
            return collapsedOffset;
          }
        }
      };

  void dispatchOnSlide(int top) {
    View bottomSheet = viewRef.get();
    if (bottomSheet != null && callback != null) {
      if (top > collapsedOffset) {
        callback.onSlide(
            bottomSheet, (float) (collapsedOffset - top) / (parentHeight - collapsedOffset));
      } else {
        callback.onSlide(
            bottomSheet, (float) (collapsedOffset - top) / (collapsedOffset - getExpandedOffset()));
      }
    }
  }

  @VisibleForTesting
  int getPeekHeightMin() {
    return peekHeightMin;
  }

  private class SettleRunnable implements Runnable {

    private final View view;

    @State private final int targetState;

    SettleRunnable(View view, @State int targetState) {
      this.view = view;
      this.targetState = targetState;
    }

    @Override
    public void run() {
      if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
        ViewCompat.postOnAnimation(view, this);
      } else {
        setStateInternal(targetState);
      }
    }
  }

  protected static class SavedState extends AbsSavedState {
    @State final int state;

    public SavedState(Parcel source) {
      this(source, null);
    }

    public SavedState(Parcel source, ClassLoader loader) {
      super(source, loader);
      //noinspection ResourceType
      state = source.readInt();
    }

    public SavedState(Parcelable superState, @State int state) {
      super(superState);
      this.state = state;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(state);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in, null);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  /**
   * A utility function to get the {@link BottomSheetBehavior} associated with the {@code view}.
   *
   * @param view The {@link View} with {@link BottomSheetBehavior}.
   * @return The {@link BottomSheetBehavior} associated with the {@code view}.
   */
  @SuppressWarnings("unchecked")
  public static <V extends View> BottomSheetBehavior<V> from(V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior behavior = ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof BottomSheetBehavior)) {
      throw new IllegalArgumentException("The view is not associated with BottomSheetBehavior");
    }
    return (BottomSheetBehavior<V>) behavior;
  }
}
