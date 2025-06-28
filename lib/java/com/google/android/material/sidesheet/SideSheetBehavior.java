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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import androidx.activity.BackEventCompat;
import androidx.annotation.GravityInt;
import androidx.annotation.IdRes;
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
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.motion.MaterialSideContainerBackHelper;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.ref.WeakReference;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as a
 * side sheet.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/SideSheet.md">component
 * developer guidance</a> and <a href="https://material.io/components/side-sheets/overview">design
 * guidelines</a>.
 */
public class SideSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
    implements Sheet<SideSheetCallback> {

  private static final int DEFAULT_ACCESSIBILITY_PANE_TITLE =
      R.string.side_sheet_accessibility_pane_title;

  private SheetDelegate sheetDelegate;

  static final int SIGNIFICANT_VEL_THRESHOLD = 500;

  private static final float HIDE_THRESHOLD = 0.5f;

  private static final float HIDE_FRICTION = 0.1f;

  private static final int NO_MAX_SIZE = -1;

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

  private float hideFriction = HIDE_FRICTION;

  private int childWidth;
  private int parentWidth;
  private int parentInnerEdge;
  private int innerMargin;

  @Nullable private WeakReference<V> viewRef;
  @Nullable private WeakReference<View> coplanarSiblingViewRef;
  @IdRes private int coplanarSiblingViewId = View.NO_ID;

  @Nullable private VelocityTracker velocityTracker;
  @Nullable private MaterialSideContainerBackHelper sideContainerBackHelper;

  private int initialX;

  @NonNull private final Set<SideSheetCallback> callbacks = new LinkedHashSet<>();

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
    if (a.hasValue(R.styleable.SideSheetBehavior_Layout_coplanarSiblingViewId)) {
      setCoplanarSiblingViewId(
          a.getResourceId(R.styleable.SideSheetBehavior_Layout_coplanarSiblingViewId, View.NO_ID));
    }
    createMaterialShapeDrawableIfNeeded(context);

    this.elevation = a.getDimension(R.styleable.SideSheetBehavior_Layout_android_elevation, -1);

    setDraggable(a.getBoolean(R.styleable.SideSheetBehavior_Layout_behavior_draggable, true));

    a.recycle();

    ViewConfiguration configuration = ViewConfiguration.get(context);
    maximumVelocity = configuration.getScaledMaximumFlingVelocity();
  }

  private void setSheetEdge(@NonNull V view, int layoutDirection) {
    LayoutParams params = (LayoutParams) view.getLayoutParams();
    int sheetGravity = Gravity.getAbsoluteGravity(params.gravity, layoutDirection);

    setSheetEdge(sheetGravity == Gravity.LEFT ? EDGE_LEFT : EDGE_RIGHT);
  }

  private void setSheetEdge(@SheetEdge int sheetEdge) {
    if (sheetDelegate == null || sheetDelegate.getSheetEdge() != sheetEdge) {
      if (sheetEdge == EDGE_RIGHT) {
        this.sheetDelegate = new RightSheetDelegate(this);
        if (shapeAppearanceModel != null && !hasRightMargin()) {
          ShapeAppearanceModel.Builder builder = shapeAppearanceModel.toBuilder();
          builder.setTopRightCornerSize(0).setBottomRightCornerSize(0);
          updateMaterialShapeDrawable(builder.build());
        }
        return;
      }

      if (sheetEdge == EDGE_LEFT) {
        this.sheetDelegate = new LeftSheetDelegate(this);
        if (shapeAppearanceModel != null && !hasLeftMargin()) {
          ShapeAppearanceModel.Builder builder = shapeAppearanceModel.toBuilder();
          builder.setTopLeftCornerSize(0).setBottomLeftCornerSize(0);
          updateMaterialShapeDrawable(builder.build());
        }
        return;
      }

      throw new IllegalArgumentException(
          "Invalid sheet edge position value: "
              + sheetEdge
              + ". Must be "
              + EDGE_RIGHT
              + " or "
              + EDGE_LEFT
              + ".");
    }
  }

  @GravityInt
  private int getGravityFromSheetEdge() {
    if (sheetDelegate != null) {
      return sheetDelegate.getSheetEdge() == Sheet.EDGE_RIGHT ? Gravity.RIGHT : Gravity.LEFT;
    }
    return Gravity.RIGHT;
  }

  private boolean hasRightMargin() {
    LayoutParams layoutParams = getViewLayoutParams();
    return layoutParams != null && layoutParams.rightMargin > 0;
  }

  private boolean hasLeftMargin() {
    LayoutParams layoutParams = getViewLayoutParams();
    return layoutParams != null && layoutParams.leftMargin > 0;
  }

  @Nullable
  private LayoutParams getViewLayoutParams() {
    if (viewRef != null) {
      View view = viewRef.get();
      if (view != null && view.getLayoutParams() instanceof CoordinatorLayout.LayoutParams) {
        return (LayoutParams) view.getLayoutParams();
      }
    }
    return null;
  }

  private void updateMaterialShapeDrawable(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    if (materialShapeDrawable != null) {
      materialShapeDrawable.setShapeAppearanceModel(shapeAppearanceModel);
    }
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
    sideContainerBackHelper = null;
  }

  @Override
  public void onDetachedFromLayoutParams() {
    super.onDetachedFromLayoutParams();
    // Release references so we don't run unnecessary codepaths while not attached to a view.
    viewRef = null;
    viewDragHelper = null;
    sideContainerBackHelper = null;
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
    if (parent.getFitsSystemWindows() && !child.getFitsSystemWindows()) {
      child.setFitsSystemWindows(true);
    }

    if (viewRef == null) {
      // First layout with this behavior.
      viewRef = new WeakReference<>(child);

      sideContainerBackHelper = new MaterialSideContainerBackHelper(child);

      // Only set MaterialShapeDrawable as background if shapeTheming is enabled, otherwise will
      // default to android:background declared in styles or layout.
      if (materialShapeDrawable != null) {
        child.setBackground(materialShapeDrawable);
        // Use elevation attr if set on side sheet; otherwise, use elevation of child view.
        materialShapeDrawable.setElevation(elevation == -1 ? child.getElevation() : elevation);
      } else if (backgroundTint != null) {
        ViewCompat.setBackgroundTintList(child, backgroundTint);
      }
      updateSheetVisibility(child);

      updateAccessibilityActions();
      if (child.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
        child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
      }
      ensureAccessibilityPaneTitleIsSet(child);
    }
    setSheetEdge(child, layoutDirection);

    if (viewDragHelper == null) {
      viewDragHelper = ViewDragHelper.create(parent, dragCallback);
    }

    int savedOuterEdge = sheetDelegate.getOuterEdge(child);
    // First let the parent lay it out.
    parent.onLayoutChild(child, layoutDirection);
    // Offset the sheet.
    parentWidth = parent.getWidth();
    parentInnerEdge = sheetDelegate.getParentInnerEdge(parent);
    childWidth = child.getWidth();

    MarginLayoutParams margins = (MarginLayoutParams) child.getLayoutParams();
    innerMargin = margins != null ? sheetDelegate.calculateInnerMargin(margins) : 0;

    int currentOffset = calculateCurrentOffset(savedOuterEdge, child);

    ViewCompat.offsetLeftAndRight(child, currentOffset);

    maybeAssignCoplanarSiblingViewBasedId(parent);

    for (SheetCallback callback : callbacks) {
      if (callback instanceof SideSheetCallback) {
        SideSheetCallback sideSheetCallback = (SideSheetCallback) callback;
        sideSheetCallback.onLayout(child);
      }
    }
    return true;
  }

  private void updateSheetVisibility(@NonNull View sheet) {
    // Sheet visibility is updated on state change to make TalkBack speak the accessibility pane
    // title when the sheet expands.
    int visibility = state == STATE_HIDDEN ? View.INVISIBLE : View.VISIBLE;
    if (sheet.getVisibility() != visibility) {
      sheet.setVisibility(visibility);
    }
  }

  private void ensureAccessibilityPaneTitleIsSet(View sheet) {
    // Set default accessibility pane title that TalkBack will speak when the sheet is expanded.
    if (ViewCompat.getAccessibilityPaneTitle(sheet) == null) {
      ViewCompat.setAccessibilityPaneTitle(
          sheet, sheet.getResources().getString(DEFAULT_ACCESSIBILITY_PANE_TITLE));
    }
  }

  private void maybeAssignCoplanarSiblingViewBasedId(@NonNull CoordinatorLayout parent) {
    if (coplanarSiblingViewRef == null && coplanarSiblingViewId != View.NO_ID) {
      View coplanarSiblingView = parent.findViewById(coplanarSiblingViewId);
      if (coplanarSiblingView != null) {
        this.coplanarSiblingViewRef = new WeakReference<>(coplanarSiblingView);
      }
    }
  }

  int getChildWidth() {
    return childWidth;
  }

  int getParentWidth() {
    return parentWidth;
  }

  int getParentInnerEdge() {
    return parentInnerEdge;
  }

  int getInnerMargin() {
    return innerMargin;
  }

  private int calculateCurrentOffset(int savedOuterEdge, V child) {
    int currentOffset;

    switch (state) {
      case STATE_EXPANDED:
        currentOffset = 0;
        break;
      case STATE_DRAGGING:
      case STATE_SETTLING:
        currentOffset = savedOuterEdge - sheetDelegate.getOuterEdge(child);
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
    if (!shouldInterceptTouchEvent(child)) {
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
        // Reset the ignore flag
        if (ignoreEvents) {
          ignoreEvents = false;
          return false;
        }
        break;
      case MotionEvent.ACTION_DOWN:
        initialX = (int) event.getX();
        break;
      default: // fall out
    }
    return !ignoreEvents
        && viewDragHelper != null
        && viewDragHelper.shouldInterceptTouchEvent(event);
  }

  private boolean shouldInterceptTouchEvent(@NonNull V child) {
    return (child.isShown() || ViewCompat.getAccessibilityPaneTitle(child) != null) && draggable;
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

  /**
   * Returns the sheet's offset from the inner edge when expanded. It will calculate the offset
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
   * Adds a callback to be notified of side sheet events.
   *
   * @param callback The callback to notify when side sheet events occur.
   */
  @Override
  public void addCallback(@NonNull SideSheetCallback callback) {
    callbacks.add(callback);
  }

  /**
   * Removes a previously added callback.
   *
   * @param callback The callback to remove.
   */
  @Override
  public void removeCallback(@NonNull SideSheetCallback callback) {
    callbacks.remove(callback);
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
            V child = viewRef != null ? viewRef.get() : null;
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
    return parent != null && parent.isLayoutRequested() && child.isAttachedToWindow();
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

    updateSheetVisibility(sheet);

    for (SheetCallback callback : callbacks) {
      callback.onStateChanged(sheet, state);
    }

    updateAccessibilityActions();
  }

  private void resetVelocity() {
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  boolean shouldHide(@NonNull View child, float velocity) {
    return sheetDelegate.shouldHide(child, velocity);
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
    return velocityTracker.getXVelocity();
  }

  private void startSettling(View child, @StableSheetState int state, boolean isReleasingView) {
    boolean settling = isSettling(child, state, isReleasingView);
    if (settling) {
      setStateInternal(STATE_SETTLING);
      stateSettlingTracker.continueSettlingToState(state);
    } else {
      setStateInternal(state);
    }
  }

  /**
   * Determines whether the sheet is currently settling to a target {@link StableSheetState} using
   * {@link StateSettlingTracker}.
   */
  private boolean isSettling(View child, int state, boolean isReleasingView) {
    int left = getOuterEdgeOffsetForState(state);
    ViewDragHelper viewDragHelper = getViewDragHelper();
    return viewDragHelper != null
        && (isReleasingView
            ? viewDragHelper.settleCapturedViewAt(left, child.getTop())
            : viewDragHelper.smoothSlideViewTo(child, left, child.getTop()));
  }

  int getOuterEdgeOffsetForState(@StableSheetState int state) {
    switch (state) {
      case STATE_EXPANDED:
        return getExpandedOffset();
      case STATE_HIDDEN:
        return sheetDelegate.getHiddenOffset();
      default:
        throw new IllegalArgumentException("Invalid state to get outer edge offset: " + state);
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
          return viewRef != null && viewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(
            @NonNull View changedView, int left, int top, int dx, int dy) {
          View coplanarSiblingView = getCoplanarSiblingView();
          if (coplanarSiblingView != null) {
            MarginLayoutParams layoutParams =
                (MarginLayoutParams) coplanarSiblingView.getLayoutParams();
            if (layoutParams != null) {
              sheetDelegate.updateCoplanarSiblingLayoutParams(
                  layoutParams, changedView.getLeft(), changedView.getRight());
              coplanarSiblingView.setLayoutParams(layoutParams);
            }
          }

          dispatchOnSlide(changedView, left);
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
          int targetState = calculateTargetStateOnViewReleased(releasedChild, xVelocity, yVelocity);
          startSettling(releasedChild, targetState, shouldSkipSmoothAnimation());
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return child.getTop();
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          return MathUtils.clamp(
              left,
              sheetDelegate.getMinViewPositionHorizontal(),
              sheetDelegate.getMaxViewPositionHorizontal());
        }

        @Override
        public int getViewHorizontalDragRange(@NonNull View child) {
          return childWidth + getInnerMargin();
        }
      };

  /**
   * Calculates the target {@link StableSheetState} state of the sheet after it's released from a
   * drag, using the x and y velocity of the drag to determine the state.
   */
  @StableSheetState
  private int calculateTargetStateOnViewReleased(
      @NonNull View releasedChild, float xVelocity, float yVelocity) {
    @StableSheetState int targetState;
    if (isExpandingOutwards(xVelocity)) {
      targetState = STATE_EXPANDED;

    } else if (shouldHide(releasedChild, xVelocity)) {
      // Hide if the view was either released close to the inner edge or it was a significant
      // horizontal swipe; otherwise settle to expanded state.
      if (sheetDelegate.isSwipeSignificant(xVelocity, yVelocity)
          || sheetDelegate.isReleasedCloseToInnerEdge(releasedChild)) {
        targetState = STATE_HIDDEN;
      } else {
        targetState = STATE_EXPANDED;
      }
    } else if (xVelocity == 0f || !SheetUtils.isSwipeMostlyHorizontal(xVelocity, yVelocity)) {
      // If the X velocity is 0 or the swipe was mostly vertical, indicated by the Y
      // velocity being greater than the X velocity, settle to the nearest correct state.
      int currentLeft = releasedChild.getLeft();
      if (Math.abs(currentLeft - getExpandedOffset())
          < Math.abs(currentLeft - sheetDelegate.getHiddenOffset())) {
        targetState = STATE_EXPANDED;
      } else {
        targetState = STATE_HIDDEN;
      }
    } else { // Moving inwards; collapse inwards and hide.
      targetState = STATE_HIDDEN;
    }
    return targetState;
  }

  private boolean isExpandingOutwards(float xVelocity) {
    return sheetDelegate.isExpandingOutwards(xVelocity);
  }

  private void dispatchOnSlide(@NonNull View child, int outerEdge) {
    if (!callbacks.isEmpty()) {
      float slideOffset = sheetDelegate.calculateSlideOffset(outerEdge);
      for (SheetCallback callback : callbacks) {
        callback.onSlide(child, slideOffset);
      }
    }
  }

  /**
   * Set the sibling id to use for coplanar sheet expansion. If a coplanar sibling has previously
   * been set either by this method or via {@link #setCoplanarSiblingView(View)}, that View
   * reference will be cleared in favor of this new coplanar sibling reference.
   *
   * @param coplanarSiblingViewId the id of the coplanar sibling
   */
  public void setCoplanarSiblingViewId(@IdRes int coplanarSiblingViewId) {
    this.coplanarSiblingViewId = coplanarSiblingViewId;
    // Clear any potential coplanar sibling view to make sure that we use this view id rather than
    // an existing coplanar sibling view.
    clearCoplanarSiblingView();
    // Request layout to find the view and trigger a layout pass.
    if (viewRef != null) {
      View view = viewRef.get();
      if (coplanarSiblingViewId != View.NO_ID && view.isLaidOut()) {
        view.requestLayout();
      }
    }
  }

  /**
   * Set the sibling view to use for coplanar sheet expansion. If a coplanar sibling has previously
   * been set either by this method or via {@link #setCoplanarSiblingViewId(int)}, that reference
   * will be cleared in favor of this new coplanar sibling reference.
   *
   * @param coplanarSiblingView the sibling view to squash during coplanar expansion
   */
  public void setCoplanarSiblingView(@Nullable View coplanarSiblingView) {
    this.coplanarSiblingViewId = View.NO_ID;
    if (coplanarSiblingView == null) {
      clearCoplanarSiblingView();
    } else {
      this.coplanarSiblingViewRef = new WeakReference<>(coplanarSiblingView);
      // Request layout to make the new view take effect.
      if (viewRef != null) {
        View view = viewRef.get();
        if (view.isLaidOut()) {
          view.requestLayout();
        }
      }
    }
  }

  /** Returns the sibling view that is used for coplanar sheet expansion. */
  @Nullable
  public View getCoplanarSiblingView() {
    return coplanarSiblingViewRef != null ? coplanarSiblingViewRef.get() : null;
  }

  private void clearCoplanarSiblingView() {
    if (this.coplanarSiblingViewRef != null) {
      this.coplanarSiblingViewRef.clear();
    }
    this.coplanarSiblingViewRef = null;
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

  @Override
  public void startBackProgress(@NonNull BackEventCompat backEvent) {
    if (sideContainerBackHelper == null) {
      return;
    }
    sideContainerBackHelper.startBackProgress(backEvent);
  }

  @Override
  public void updateBackProgress(@NonNull BackEventCompat backEvent) {
    if (sideContainerBackHelper == null) {
      return;
    }
    sideContainerBackHelper.updateBackProgress(backEvent, getGravityFromSheetEdge());

    updateCoplanarSiblingBackProgress();
  }

  private void updateCoplanarSiblingBackProgress() {
    if (viewRef == null || viewRef.get() == null) {
      return;
    }
    View sheet = viewRef.get();

    View coplanarSiblingView = getCoplanarSiblingView();
    if (coplanarSiblingView == null) {
      return;
    }

    MarginLayoutParams coplanarSiblingLayoutParams =
        (MarginLayoutParams) coplanarSiblingView.getLayoutParams();
    if (coplanarSiblingLayoutParams == null) {
      return;
    }

    int updatedCoplanarSiblingAdjacentMargin = (int) (childWidth * sheet.getScaleX() + innerMargin);
    sheetDelegate.updateCoplanarSiblingAdjacentMargin(
        coplanarSiblingLayoutParams, updatedCoplanarSiblingAdjacentMargin);
    coplanarSiblingView.requestLayout();
  }

  @Override
  public void handleBackInvoked() {
    if (sideContainerBackHelper == null) {
      return;
    }
    BackEventCompat backEvent = sideContainerBackHelper.onHandleBackInvoked();
    if (backEvent == null || VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      setState(STATE_HIDDEN);
      return;
    }

    sideContainerBackHelper.finishBackProgress(
        backEvent,
        getGravityFromSheetEdge(),
        new AnimatorListenerAdapter() {
          @Override
          public void onAnimationEnd(Animator animation) {
            setStateInternal(STATE_HIDDEN);
            if (viewRef != null && viewRef.get() != null) {
              viewRef.get().requestLayout();
            }
          }
        },
        getCoplanarFinishAnimatorUpdateListener());
  }

  @Nullable
  private AnimatorUpdateListener getCoplanarFinishAnimatorUpdateListener() {
    View coplanarSiblingView = getCoplanarSiblingView();
    if (coplanarSiblingView == null) {
      return null;
    }

    MarginLayoutParams coplanarSiblingLayoutParams =
        (MarginLayoutParams) coplanarSiblingView.getLayoutParams();
    if (coplanarSiblingLayoutParams == null) {
      return null;
    }

    int coplanarSiblingAdjacentMargin =
        sheetDelegate.getCoplanarSiblingAdjacentMargin(coplanarSiblingLayoutParams);

    return animation -> {
      sheetDelegate.updateCoplanarSiblingAdjacentMargin(
          coplanarSiblingLayoutParams,
          AnimationUtils.lerp(coplanarSiblingAdjacentMargin, 0, animation.getAnimatedFraction()));
      coplanarSiblingView.requestLayout();
    };
  }

  @Override
  public void cancelBackProgress() {
    if (sideContainerBackHelper == null) {
      return;
    }
    sideContainerBackHelper.cancelBackProgress();
  }

  @VisibleForTesting
  @Nullable
  MaterialSideContainerBackHelper getBackHelper() {
    return sideContainerBackHelper;
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
        viewRef.get().postOnAnimation(continueSettlingRunnable);
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
