/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.sidesheet;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as a
 * side sheet.
 */
public final class SideSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
    implements Sheet {

  private SheetDelegate sheetDelegate;

  static final int SIGNIFICANT_VEL_THRESHOLD = 500;

  private static final float HIDE_THRESHOLD = 0.5f;

  private static final float HIDE_FRICTION = 0.1f;

  private static final int NO_MAX_SIZE = -1;

  private static final boolean UPDATE_IMPORTANT_FOR_ACCESSIBILITY_ON_SIBLINGS = false;

  private float maximumVelocity;

  @Nullable private MaterialShapeDrawable materialShapeDrawable;

  @Nullable private ColorStateList backgroundTint;

  /** Shape Appearance to be used in sheet. */
  private ShapeAppearanceModel shapeAppearanceModel;

  private final StateSettlingTracker stateSettlingTracker = new StateSettlingTracker();

  private static final int DEF_STYLE_RES = R.style.Widget_Material3_SideSheet;

  private float elevation;

  private boolean draggable = true;

  @SheetState private int state = STATE_HIDDEN;

  @StableSheetState private int lastStableState = STATE_HIDDEN;

  @Nullable private ViewDragHelper viewDragHelper;

  private boolean ignoreEvents;

  private int lastNestedScrollDx;

  private boolean nestedScrolled;

  private float hideFriction = HIDE_FRICTION;

  private int childWidth;
  private int parentWidth;

  @Nullable private WeakReference<V> viewRef;

  @Nullable private WeakReference<View> nestedScrollingChildRef;

  @Nullable private VelocityTracker velocityTracker;

  private int activePointerId;

  private int initialX;
  private int initialY;

  private boolean touchingScrollingChild;

  @Nullable private Map<View, Integer> importantForAccessibilityMap;

  public SideSheetBehavior() {}

  public SideSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SideSheetBehavior_Layout);
    if (a.hasValue(R.styleable.SideSheetBehavior_Layout_backgroundTint)) {
      this.backgroundTint =
          MaterialResources.getColorStateList(
              context, a, R.styleable.SideSheetBehavior_Layout_backgroundTint);
    }
    if (a.hasValue(R.styleable.SideSheetBehavior_Layout_shapeAppearance)) {
      this.shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, attrs, 0, DEF_STYLE_RES).build();
    }
    createMaterialShapeDrawableIfNeeded(context);

    this.elevation = a.getDimension(R.styleable.SideSheetBehavior_Layout_android_elevation, -1);

    setDraggable(a.getBoolean(R.styleable.SideSheetBehavior_Layout_behavior_draggable, true));

    a.recycle();

    setSheetEdge(getDefaultSheetEdge());
    ViewConfiguration configuration = ViewConfiguration.get(context);
    maximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  private void setSheetEdge(@SheetEdge int sheetEdge) {
    if (sheetDelegate == null || sheetDelegate.getSheetEdge() != sheetEdge) {

      if (sheetEdge == RIGHT) {
        this.sheetDelegate = new RightSheetDelegate(this);
        return;
      }

      throw new IllegalArgumentException(
          "Invalid sheet edge position value: " + sheetEdge + ". Must be " + RIGHT);
    }
  }

  @SheetEdge
  private int getDefaultSheetEdge() {
    return RIGHT;
  }

  /**
   * Expand the sheet by setting the side sheet state to {@link Sheet#STATE_EXPANDED}. This is a
   * convenience method for {@link #setState(int)}.
   */
  public void expand() {
    setState(STATE_EXPANDED);
  }

  /**
   * Hide the sheet by setting the sheet state to {@link Sheet#STATE_HIDDEN}. This is a convenience
   * method for {@link #setState(int)}.
   */
  public void hide() {
    setState(STATE_HIDDEN);
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull V child) {
    return new SavedState(super.onSaveInstanceState(parent, child), this);
  }

  @Override
  public void onRestoreInstanceState(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull Parcelable state) {
    SavedState ss = (SavedState) state;
    if (ss.getSuperState() != null) {
      super.onRestoreInstanceState(parent, child, ss.getSuperState());
    }
    // Intermediate states are restored as hidden state.
    this.state = ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING ? STATE_HIDDEN : ss.state;
    this.lastStableState = this.state;
  }

  @Override
  public void onAttachedToLayoutParams(@NonNull LayoutParams layoutParams) {
    super.onAttachedToLayoutParams(layoutParams);
    // These may already be null, but just be safe, explicitly assign them. This lets us know the
    // first time we layout with this behavior by checking (viewRef == null).
    viewRef = null;
    viewDragHelper = null;
  }

  @Override
  public void onDetachedFromLayoutParams() {
    super.onDetachedFromLayoutParams();
    // Release references so we don't run unnecessary codepaths while not attached to a view.
    viewRef = null;
    viewDragHelper = null;
  }

  @Override
  public boolean onMeasureChild(
      @NonNull CoordinatorLayout parent,
      @NonNull V child,
      int parentWidthMeasureSpec,
      int widthUsed,
      int parentHeightMeasureSpec,
      int heightUsed) {
    MarginLayoutParams lp = (MarginLayoutParams) child.getLayoutParams();
    int childWidthMeasureSpec =
        getChildMeasureSpec(
            parentWidthMeasureSpec,
            parent.getPaddingLeft()
                + parent.getPaddingRight()
                + lp.leftMargin
                + lp.rightMargin
                + widthUsed,
            NO_MAX_SIZE,
            lp.width);
    int childHeightMeasureSpec =
        getChildMeasureSpec(
            parentHeightMeasureSpec,
            parent.getPaddingTop()
                + parent.getPaddingBottom()
                + lp.topMargin
                + lp.bottomMargin
                + heightUsed,
            NO_MAX_SIZE,
            lp.height);
    child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
    return true; // Child was measured
  }

  private int getChildMeasureSpec(
      int parentMeasureSpec, int padding, int maxSize, int childDimension) {
    int result = ViewGroup.getChildMeasureSpec(parentMeasureSpec, padding, childDimension);
    if (maxSize == NO_MAX_SIZE) {
      return result;
    } else {
      int mode = MeasureSpec.getMode(result);
      int size = MeasureSpec.getSize(result);
      switch (mode) {
        case MeasureSpec.EXACTLY:
          return MeasureSpec.makeMeasureSpec(min(size, maxSize), MeasureSpec.EXACTLY);
        case MeasureSpec.AT_MOST:
        case MeasureSpec.UNSPECIFIED:
        default:
          return MeasureSpec.makeMeasureSpec(
              size == 0 ? maxSize : min(size, maxSize), MeasureSpec.AT_MOST);
      }
    }
  }

  @Override
  public boolean onLayoutChild(
      @NonNull CoordinatorLayout parent, @NonNull final V child, int layoutDirection) {
    if (ViewCompat.getFitsSystemWindows(parent) && !ViewCompat.getFitsSystemWindows(child)) {
      child.setFitsSystemWindows(true);
    }

    if (viewRef == null) {
      // First layout with this behavior.
      viewRef = new WeakReference<>(child);
      // Only set MaterialShapeDrawable as background if shapeTheming is enabled, otherwise will
      // default to android:background declared in styles or layout.
      if (materialShapeDrawable != null) {
        ViewCompat.setBackground(child, materialShapeDrawable);
        // Use elevation attr if set on side sheet; otherwise, use elevation of child view.
        materialShapeDrawable.setElevation(
            elevation == -1 ? ViewCompat.getElevation(child) : elevation);
      } else if (backgroundTint != null) {
        ViewCompat.setBackgroundTintList(child, backgroundTint);
      }
      updateAccessibilityActions();
      if (ViewCompat.getImportantForAccessibility(child)
          == ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
        ViewCompat.setImportantForAccessibility(child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
      }
    }
    if (viewDragHelper == null) {
      viewDragHelper = ViewDragHelper.create(parent, dragCallback);
    }

    int savedOutwardEdge = sheetDelegate.getOutwardEdge(child);
    // First let the parent lay it out.
    parent.onLayoutChild(child, layoutDirection);
    // Offset the sheet.
    parentWidth = parent.getWidth();
    childWidth = child.getWidth();

    int currentOffset = calculateCurrentOffset(savedOutwardEdge, child);

    ViewCompat.offsetLeftAndRight(child, currentOffset);

    nestedScrollingChildRef = new WeakReference<>(findScrollingChild(child));
    return true;
  }

  int getChildWidth() {
    return childWidth;
  }

  int getParentWidth() {
    return parentWidth;
  }

  private int calculateCurrentOffset(int savedOutwardEdge, V child) {
    int currentOffset;

    switch (state) {
      case STATE_EXPANDED:
        currentOffset = savedOutwardEdge;
        break;
      case STATE_DRAGGING:
      case STATE_SETTLING:
        currentOffset = savedOutwardEdge - sheetDelegate.getOutwardEdge(child);
        break;
      case STATE_HIDDEN:
        currentOffset = sheetDelegate.getHiddenOffset();
        break;
      default:
        throw new IllegalStateException("Unexpected value: " + state);
    }
    return currentOffset;
  }

  @Override
  public boolean onInterceptTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown() || !draggable) {
      ignoreEvents = true;
      return false;
    }
    int action = event.getActionMasked();
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      resetVelocity();
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
        initialX = (int) event.getX();
        initialY = (int) event.getY();
        // Only intercept nested scrolling events here if the view is not being moved by the
        // ViewDragHelper.
        if (state != STATE_SETTLING) {
          View nestedScrollChild =
              nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
          if (nestedScrollChild != null
              && parent.isPointInChildBounds(nestedScrollChild, initialX, initialY)) {
            activePointerId = event.getPointerId(event.getActionIndex());
            touchingScrollingChild = true;
          }
        }
        ignoreEvents =
            activePointerId == MotionEvent.INVALID_POINTER_ID
                && !parent.isPointInChildBounds(child, initialX, initialY);
        break;
      default: // fall out
    }
    if (!ignoreEvents
        && viewDragHelper != null
        && viewDragHelper.shouldInterceptTouchEvent(event)) {
      return true;
    }
    // We have to handle cases where the ViewDragHelper does not capture the sheet because
    // it is not the top most view of its parent. This is not necessary when the touch event is
    // happening over the scrolling content as nested scrolling logic handles that case.
    View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
    return action == MotionEvent.ACTION_MOVE
        && scroll != null
        && !ignoreEvents
        && state != STATE_DRAGGING
        && !parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY())
        && viewDragHelper != null
        && calculateDragDistance(initialY, event.getY()) > viewDragHelper.getTouchSlop();
  }

  int getSignificantVelocityThreshold() {
    return SIGNIFICANT_VEL_THRESHOLD;
  }

  @Override
  public boolean onTouchEvent(
      @NonNull CoordinatorLayout parent, @NonNull V child, @NonNull MotionEvent event) {
    if (!child.isShown()) {
      return false;
    }
    int action = event.getActionMasked();
    if (state == STATE_DRAGGING && action == MotionEvent.ACTION_DOWN) {
      return true;
    }
    if (shouldHandleDraggingWithHelper()) {
      viewDragHelper.processTouchEvent(event);
    }
    // Record the velocity
    if (action == MotionEvent.ACTION_DOWN) {
      resetVelocity();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
    // to capture the sheet in case it is not captured and the touch slop is passed.
    if (shouldHandleDraggingWithHelper() && action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
      if (isDraggedFarEnough(event)) {
        viewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !ignoreEvents;
  }

  private boolean isDraggedFarEnough(@NonNull MotionEvent event) {
    if (!shouldHandleDraggingWithHelper()) {
      return false;
    }
    float distanceDragged = calculateDragDistance(initialX, event.getX());
    return distanceDragged > viewDragHelper.getTouchSlop();
  }

  private float calculateDragDistance(float initialPoint, float currentPoint) {
    return Math.abs(initialPoint - currentPoint);
  }

  @Override
  public boolean onStartNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View directTargetChild,
      @NonNull View target,
      int axes,
      int type) {
    lastNestedScrollDx = 0;
    nestedScrolled = false;
    return (axes & ViewCompat.SCROLL_AXIS_HORIZONTAL) != 0;
  }

  @Override
  public void onNestedPreScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dx,
      int dy,
      @NonNull int[] consumed,
      int type) {
    if (type == ViewCompat.TYPE_NON_TOUCH) {
      // Ignore fling here. The ViewDragHelper handles it.
      return;
    }
    View scrollingChild = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
    if (isNestedScrollingCheckEnabled() && target != scrollingChild) {
      return;
    }
    sheetDelegate.setTargetStateOnNestedPreScroll(
        coordinatorLayout, child, target, dx, dy, consumed, type);
    lastNestedScrollDx = dx;
    nestedScrolled = true;
  }

  @Override
  public void onStopNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int type) {
    if (sheetDelegate.hasReachedExpandedOffset(child)) {
      setStateInternal(STATE_EXPANDED);
      return;
    }
    if (isNestedScrollingCheckEnabled()
        && (nestedScrollingChildRef == null
            || target != nestedScrollingChildRef.get()
            || !nestedScrolled)) {
      return;
    }
    @StableSheetState int targetState = sheetDelegate.calculateTargetStateOnStopNestedScroll(child);
    startSettling(child, targetState, false);
    nestedScrolled = false;
  }

  @Override
  public void onNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int dxConsumed,
      int dyConsumed,
      int dxUnconsumed,
      int dyUnconsumed,
      int type,
      @NonNull int[] consumed) {
    // Overridden to prevent the default consumption of the entire scroll distance.
  }

  @Override
  public boolean onNestedPreFling(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      float velocityX,
      float velocityY) {

    if (nestedScrollingChildRef == null || !isNestedScrollingCheckEnabled()) {
      return false;
    }

    return target == nestedScrollingChildRef.get()
        && (state != STATE_EXPANDED
            || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
  }

  int getLastNestedScrollDx() {
    return lastNestedScrollDx;
  }

  /**
   * Returns the sheet's offset from the origin edge when expanded. It will calculate the offset
   * based on the width of the content.
   */
  public int getExpandedOffset() {
    return sheetDelegate.getExpandedOffset();
  }

  /**
   * Sets whether this sheet is can be hidden/expanded by dragging. Note: When disabling dragging,
   * an app will require to implement a custom way to expand/collapse the sheet
   *
   * @param draggable {@code false} to prevent dragging the sheet to collapse and expand
   * @attr ref com.google.android.material.R.styleable#SideSheetBehavior_Layout_behavior_draggable
   */
  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  public boolean isDraggable() {
    return draggable;
  }

  /**
   * Sets the friction coefficient to hide the sheet, or set it to the next closest expanded state.
   *
   * @param hideFriction The friction coefficient that determines the swipe velocity needed to hide
   *     or set the sheet to the closest expanded state.
   */
  public void setHideFriction(float hideFriction) {
    this.hideFriction = hideFriction;
  }

  /**
   * Gets the friction coefficient to hide the sheet, or set it to the next closest expanded state.
   *
   * @return The friction coefficient that determines the swipe velocity needed to hide or set the
   *     sheet to the closest expanded state.
   */
  public float getHideFriction() {
    return this.hideFriction;
  }

  float getHideThreshold() {
    return HIDE_THRESHOLD;
  }

  /**
   * Sets the state of the sheet. The sheet will transition to that state with animation.
   *
   * @param state One of {@link #STATE_EXPANDED} or {@link #STATE_HIDDEN}.
   */
  @Override
  public void setState(@StableSheetState int state) {
    if (state == STATE_DRAGGING || state == STATE_SETTLING) {
      throw new IllegalArgumentException(
          "STATE_"
              + (state == STATE_DRAGGING ? "DRAGGING" : "SETTLING")
              + " should not be set externally.");
    }
    final int finalState = state;
    if (viewRef == null || viewRef.get() == null) {
      // The view is not laid out yet; modify state and let onLayoutChild handle it later
      setStateInternal(state);
    } else {
      runAfterLayout(
          viewRef.get(),
          () -> {
            V child = viewRef.get();
            if (child != null) {
              startSettling(child, finalState, false);
            }
          });
    }
  }

  private void runAfterLayout(@NonNull V child, Runnable runnable) {
    if (isLayingOut(child)) {
      child.post(runnable);
    } else {
      runnable.run();
    }
  }

  private boolean isLayingOut(@NonNull V child) {
    ViewParent parent = child.getParent();
    return parent != null && parent.isLayoutRequested() && ViewCompat.isAttachedToWindow(child);
  }

  /**
   * Gets the current state of the sheet.
   *
   * @return One of {@link #STATE_EXPANDED}, {@link #STATE_DRAGGING}, {@link #STATE_SETTLING}, or
   *     {@link #STATE_HIDDEN}.
   */
  @SheetState
  @Override
  public int getState() {
    return state;
  }

  void setStateInternal(@SheetState int state) {
    if (this.state == state) {
      return;
    }
    this.state = state;
    if (state == STATE_EXPANDED || (state == STATE_HIDDEN)) {
      this.lastStableState = state;
    }

    if (viewRef == null) {
      return;
    }

    View sheet = viewRef.get();
    if (sheet == null) {
      return;
    }

    if (state == STATE_EXPANDED) {
      updateImportantForAccessibility(true);
    } else if (state == STATE_HIDDEN) {
      updateImportantForAccessibility(false);
    }

    updateAccessibilityActions();
  }

  private void resetVelocity() {
    activePointerId = ViewDragHelper.INVALID_POINTER;
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  boolean shouldHide(@NonNull View child, float velocity) {
    return sheetDelegate.shouldHide(child, velocity);
  }

  @Nullable
  @VisibleForTesting
  View findScrollingChild(View view) {
    if (view.getVisibility() != View.VISIBLE) {
      return null;
    }
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

  private boolean shouldHandleDraggingWithHelper() {
    // If it's not draggable, do not forward events to viewDragHelper; however, if it's already
    // dragging, let it finish.
    return viewDragHelper != null && (draggable || state == STATE_DRAGGING);
  }

  private void createMaterialShapeDrawableIfNeeded(@NonNull Context context) {
    if (shapeAppearanceModel == null) {
      return;
    }

    this.materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
    this.materialShapeDrawable.initializeElevationOverlay(context);

    if (backgroundTint != null) {
      materialShapeDrawable.setFillColor(backgroundTint);
    } else {
      // If the tint isn't set, use the theme default background color.
      TypedValue defaultColor = new TypedValue();
      context.getTheme().resolveAttribute(android.R.attr.colorBackground, defaultColor, true);
      materialShapeDrawable.setTint(defaultColor.data);
    }
  }

  float getXVelocity() {
    if (velocityTracker == null) {
      return 0;
    }
    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
    return velocityTracker.getXVelocity(activePointerId);
  }

  private void startSettling(View child, @StableSheetState int state, boolean isReleasingView) {
    boolean settling = sheetDelegate.isSettling(child, state, isReleasingView);
    if (settling) {
      setStateInternal(STATE_SETTLING);
      stateSettlingTracker.continueSettlingToState(state);
    } else {
      setStateInternal(state);
    }
  }

  int getOutwardEdgeOffsetForState(@StableSheetState int state) {
    switch (state) {
      case STATE_EXPANDED:
        return getExpandedOffset();
      case STATE_HIDDEN:
        return sheetDelegate.getHiddenOffset();
      default:
        throw new IllegalArgumentException("Invalid state to get outward edge offset: " + state);
    }
  }

  @Nullable
  ViewDragHelper getViewDragHelper() {
    return viewDragHelper;
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
          if (state == STATE_DRAGGING) {
            return false;
          }
          if (touchingScrollingChild) {
            return false;
          }
          if (state == STATE_EXPANDED && activePointerId == pointerId) {
            View scroll = nestedScrollingChildRef != null ? nestedScrollingChildRef.get() : null;
            if (scroll != null && scroll.canScrollVertically(-1)) {
              // Let the content scroll.
              return false;
            }
          }
          return viewRef != null && viewRef.get() == child;
        }

        @Override
        public void onViewDragStateChanged(@SheetState int state) {
          if (state == ViewDragHelper.STATE_DRAGGING && draggable) {
            setStateInternal(STATE_DRAGGING);
          }
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xVelocity, float yVelocity) {
          @StableSheetState
          int targetState =
              sheetDelegate.calculateTargetStateOnViewReleased(releasedChild, xVelocity, yVelocity);
          startSettling(releasedChild, targetState, shouldSkipSmoothAnimation());
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          return MathUtils.clamp(left, getExpandedOffset(), parentWidth);
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
          return parentWidth;
        }
      };

  /**
   * Checks whether a nested scroll should be enabled. If {@code false} all nested scrolls will be
   * consumed by the side sheet.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isNestedScrollingCheckEnabled() {
    return true;
  }

  /**
   * Checks whether an animation should be smooth after the side sheet is released after dragging.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean shouldSkipSmoothAnimation() {
    return true;
  }

  /**
   * Gets the last stable state of the sheet.
   *
   * @return Either {@link #STATE_EXPANDED} or {@link #STATE_HIDDEN}.
   * @hide
   */
  @SheetState
  @RestrictTo(LIBRARY_GROUP)
  public int getLastStableState() {
    return lastStableState;
  }

  class StateSettlingTracker {
    @StableSheetState private int targetState;
    private boolean isContinueSettlingRunnablePosted;

    private final Runnable continueSettlingRunnable =
        () -> {
          isContinueSettlingRunnablePosted = false;
          if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
            continueSettlingToState(targetState);
          } else if (state == STATE_SETTLING) {
            setStateInternal(targetState);
          }
          // In other cases, settling has been interrupted by certain UX interactions. Do nothing.
        };

    void continueSettlingToState(@StableSheetState int targetState) {
      if (viewRef == null || viewRef.get() == null) {
        return;
      }
      this.targetState = targetState;
      if (!isContinueSettlingRunnablePosted) {
        ViewCompat.postOnAnimation(viewRef.get(), continueSettlingRunnable);
        isContinueSettlingRunnablePosted = true;
      }
    }
  }

  /** State persisted across instances */
  protected static class SavedState extends AbsSavedState {
    @SheetState final int state;

    public SavedState(@NonNull Parcel source) {
      this(source, null);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      //noinspection ResourceType
      state = source.readInt();
    }

    public SavedState(Parcelable superState, @NonNull SideSheetBehavior<?> behavior) {
      super(superState);
      this.state = behavior.state;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(state);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }

  /**
   * A utility function to get the {@link SideSheetBehavior} associated with the {@code view}.
   *
   * @param view The {@link View} with {@link SideSheetBehavior}.
   * @return The {@link SideSheetBehavior} associated with the {@code view}.
   */
  @NonNull
  @SuppressWarnings("unchecked")
  public static <V extends View> SideSheetBehavior<V> from(@NonNull V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior = ((LayoutParams) params).getBehavior();
    if (!(behavior instanceof SideSheetBehavior)) {
      throw new IllegalArgumentException("The view is not associated with SideSheetBehavior");
    }
    return (SideSheetBehavior<V>) behavior;
  }

  private void updateImportantForAccessibility(boolean expanded) {
    if (viewRef == null) {
      return;
    }

    ViewParent viewParent = viewRef.get().getParent();
    if (!(viewParent instanceof CoordinatorLayout)) {
      return;
    }

    CoordinatorLayout parent = (CoordinatorLayout) viewParent;
    final int childCount = parent.getChildCount();
    if ((VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) && expanded) {
      if (importantForAccessibilityMap == null) {
        importantForAccessibilityMap = new HashMap<>(childCount);
      } else {
        // The important for accessibility values of the child views have been saved already.
        return;
      }
    }

    for (int i = 0; i < childCount; i++) {
      final View child = parent.getChildAt(i);
      if (child == viewRef.get()) {
        continue;
      }
      if (expanded) {
        // Saves the important for accessibility value of the child view.
        if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
          importantForAccessibilityMap.put(child, child.getImportantForAccessibility());
        }
        if (UPDATE_IMPORTANT_FOR_ACCESSIBILITY_ON_SIBLINGS) {
          ViewCompat.setImportantForAccessibility(
              child, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
          // If the siblings of the sheet have been set to not important for a11y, move the focus
          // to the sheet when expanded.
          viewRef.get().sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
        }
      } else {
        if (UPDATE_IMPORTANT_FOR_ACCESSIBILITY_ON_SIBLINGS
            && importantForAccessibilityMap != null
            && importantForAccessibilityMap.containsKey(child)) {
          // Restores the original important for accessibility value of the child view.
          ViewCompat.setImportantForAccessibility(child, importantForAccessibilityMap.get(child));
        }
        importantForAccessibilityMap = null;
      }
    }
  }

  private void updateAccessibilityActions() {
    if (viewRef == null) {
      return;
    }
    V child = viewRef.get();
    if (child == null) {
      return;
    }
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_EXPAND);
    ViewCompat.removeAccessibilityAction(child, AccessibilityNodeInfoCompat.ACTION_DISMISS);

    if (state != STATE_HIDDEN) {
      replaceAccessibilityActionForState(
          child, AccessibilityActionCompat.ACTION_DISMISS, STATE_HIDDEN);
    }
    if (state != STATE_EXPANDED) {
      replaceAccessibilityActionForState(
          child, AccessibilityActionCompat.ACTION_EXPAND, STATE_EXPANDED);
    }
  }

  private void replaceAccessibilityActionForState(
      V child, AccessibilityActionCompat action, @SheetState int state) {
    ViewCompat.replaceAccessibilityAction(
        child, action, null, createAccessibilityViewCommandForState(state));
  }

  private AccessibilityViewCommand createAccessibilityViewCommandForState(
      @SheetState final int state) {
    return (view, arguments) -> {
      setState(state);
      return true;
    };
  }
}
