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

package com.google.android.material.bottomsheet;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.util.SparseIntArray;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.RoundedCorner;
import android.view.VelocityTracker;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.ViewParent;
import android.view.WindowInsets;
import android.view.accessibility.AccessibilityEvent;
import androidx.activity.BackEventCompat;
import androidx.annotation.FloatRange;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.StringRes;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout.LayoutParams;
import androidx.core.graphics.Insets;
import androidx.core.math.MathUtils;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsCompat.Side;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.core.view.insets.GradientProtection;
import androidx.core.view.insets.Protection;
import androidx.customview.view.AbsSavedState;
import androidx.customview.widget.ViewDragHelper;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.motion.MaterialBackHandler;
import com.google.android.material.motion.MaterialBottomContainerBackHelper;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An interaction behavior plugin for a child view of {@link CoordinatorLayout} to make it work as a
 * bottom sheet.
 *
 * <p>To send useful accessibility events, set a title on bottom sheets that are windows or are
 * window-like. For BottomSheetDialog use {@link BottomSheetDialog#setTitle(int)}, and for
 * BottomSheetDialogFragment use {@link ViewCompat#setAccessibilityPaneTitle(View, CharSequence)}.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/BottomSheet.md">component
 * developer guidance</a> and <a
 * href="https://material.io/components/bottom-sheets/overview">design guidelines</a>.
 */
public class BottomSheetBehavior<V extends View> extends CoordinatorLayout.Behavior<V>
    implements MaterialBackHandler {

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

    void onLayout(@NonNull View bottomSheet) {}
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

  /** The bottom sheet is half-expanded (used when fitToContents is false). */
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
   * Stable states that can be set by the {@link #setState(int)} method. These includes all the
   * possible states a bottom sheet can be in when it's settled.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({STATE_EXPANDED, STATE_COLLAPSED, STATE_HIDDEN, STATE_HALF_EXPANDED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface StableState {}

  /**
   * Peek at the 16:9 ratio keyline of its parent.
   *
   * <p>This can be used as a parameter for {@link #setPeekHeight(int)}. {@link #getPeekHeight()}
   * will return this when the value is set.
   */
  public static final int PEEK_HEIGHT_AUTO = -1;

  /** This flag will preserve the peekHeight int value on configuration change. */
  public static final int SAVE_PEEK_HEIGHT = 0x1;

  /** This flag will preserve the fitToContents boolean value on configuration change. */
  public static final int SAVE_FIT_TO_CONTENTS = 1 << 1;

  /** This flag will preserve the hideable boolean value on configuration change. */
  public static final int SAVE_HIDEABLE = 1 << 2;

  /** This flag will preserve the skipCollapsed boolean value on configuration change. */
  public static final int SAVE_SKIP_COLLAPSED = 1 << 3;

  /** This flag will preserve all aforementioned values on configuration change. */
  public static final int SAVE_ALL = -1;

  /**
   * This flag will not preserve the aforementioned values set at runtime if the view is destroyed
   * and recreated. The only value preserved will be the positional state, e.g. collapsed, hidden,
   * expanded, etc. This is the default behavior.
   */
  public static final int SAVE_NONE = 0;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      flag = true,
      value = {
        SAVE_PEEK_HEIGHT,
        SAVE_FIT_TO_CONTENTS,
        SAVE_HIDEABLE,
        SAVE_SKIP_COLLAPSED,
        SAVE_ALL,
        SAVE_NONE,
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface SaveFlags {}

  private static final String TAG = "BottomSheetBehavior";

  @SaveFlags private int saveFlags = SAVE_NONE;

  @VisibleForTesting static final int DEFAULT_SIGNIFICANT_VEL_THRESHOLD = 500;

  private static final float HIDE_THRESHOLD = 0.5f;

  private static final float HIDE_FRICTION = 0.1f;

  private static final int CORNER_ANIMATION_DURATION = 500;

  private static final int NO_MAX_SIZE = -1;

  @VisibleForTesting
  static final int VIEW_INDEX_BOTTOM_SHEET = 0;

  private static final int INVALID_POSITION = -1;

  @VisibleForTesting
  static final int VIEW_INDEX_ACCESSIBILITY_DELEGATE_VIEW = 1;

  private boolean fitToContents = true;

  private boolean updateImportantForAccessibilityOnSiblings = false;

  private float maximumVelocity;

  private int significantVelocityThreshold;
  private boolean multipleScrollingChildrenSupported;

  /** Peek height set by the user. */
  private int peekHeight;

  /** Whether or not to use automatic peek height. */
  private boolean peekHeightAuto;

  /** Minimum peek height permitted. */
  private int peekHeightMin;

  /** Peek height gesture inset buffer to ensure enough swipeable space. */
  private int peekHeightGestureInsetBuffer;

  private MaterialShapeDrawable materialShapeDrawable;

  @Nullable private ColorStateList backgroundTint;

  private int maxWidth = NO_MAX_SIZE;

  private int maxHeight = NO_MAX_SIZE;

  private int gestureInsetBottom;
  private boolean gestureInsetBottomIgnored;
  private boolean paddingBottomSystemWindowInsets;
  private boolean paddingLeftSystemWindowInsets;
  private boolean paddingRightSystemWindowInsets;
  private boolean paddingTopSystemWindowInsets;
  private boolean marginLeftSystemWindowInsets;
  private boolean marginRightSystemWindowInsets;
  private boolean marginTopSystemWindowInsets;

  private int insetBottom;
  private int insetTop;

  private boolean shouldRemoveExpandedCorners;

  /** Default Shape Appearance to be used in bottomsheet */
  private ShapeAppearanceModel shapeAppearanceModelDefault;

  private boolean expandedCornersRemoved;

  private final StateSettlingTracker stateSettlingTracker = new StateSettlingTracker();

  @Nullable private ValueAnimator interpolatorAnimator;

  private static final int DEF_STYLE_RES = R.style.Widget_Design_BottomSheet_Modal;

  int expandedOffset;

  int fitToContentsOffset;

  int halfExpandedOffset;

  float halfExpandedRatio = 0.5f;

  int collapsedOffset;

  float elevation = -1;

  boolean hideable;

  private boolean skipCollapsed;

  private boolean draggable = true;

  private boolean draggableOnNestedScroll = true;
  private boolean draggableOnNestedScrollLastDragIgnored;

  @State int state = STATE_COLLAPSED;

  @State int lastStableState = STATE_COLLAPSED;

  @Nullable ViewDragHelper viewDragHelper;

  private boolean ignoreEvents;

  private int lastNestedScrollDy;

  private boolean nestedScrolled;

  private float hideFriction = HIDE_FRICTION;

  private int childHeight;
  int parentWidth;
  int parentHeight;

  @Nullable WeakReference<V> viewRef;
  @Nullable WeakReference<View> accessibilityDelegateViewRef;
  @Nullable WeakReference<View> dragHandleViewRef;

  @VisibleForTesting @NonNull
  final List<WeakReference<View>> nestedScrollingChildrenRef = new ArrayList<>();

  @NonNull private final ArrayList<BottomSheetCallback> callbacks = new ArrayList<>();

  @Nullable private VelocityTracker velocityTracker;
  @Nullable MaterialBottomContainerBackHelper bottomContainerBackHelper;

  int activePointerId;

  private int initialY = INVALID_POSITION;

  private WeakReference<View> currentTouchedScrollChildRef;

  boolean touchingScrollingChild;

  @Nullable private Map<View, Integer> importantForAccessibilityMap;

  @VisibleForTesting
  final SparseIntArray expandHalfwayActionIds = new SparseIntArray();
  @VisibleForTesting
  final SparseIntArray expandActionIds = new SparseIntArray();
  @VisibleForTesting
  final SparseIntArray collapseActionIds = new SparseIntArray();

  public BottomSheetBehavior() {}

  public BottomSheetBehavior(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);

    peekHeightGestureInsetBuffer =
        context.getResources().getDimensionPixelSize(R.dimen.mtrl_min_touch_target_size);

    TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BottomSheetBehavior_Layout);
    if (a.hasValue(R.styleable.BottomSheetBehavior_Layout_backgroundTint)) {
      this.backgroundTint = MaterialResources.getColorStateList(
          context, a, R.styleable.BottomSheetBehavior_Layout_backgroundTint);
    }
    if (a.hasValue(R.styleable.BottomSheetBehavior_Layout_shapeAppearance)) {
      this.shapeAppearanceModelDefault =
          ShapeAppearanceModel.builder(context, attrs, R.attr.bottomSheetStyle, DEF_STYLE_RES)
              .build();
    }
    createMaterialShapeDrawableIfNeeded(context);
    createShapeValueAnimator();

    this.elevation = a.getDimension(R.styleable.BottomSheetBehavior_Layout_android_elevation, -1);

    if (a.hasValue(R.styleable.BottomSheetBehavior_Layout_android_maxWidth)) {
      setMaxWidth(
          a.getDimensionPixelSize(
              R.styleable.BottomSheetBehavior_Layout_android_maxWidth, NO_MAX_SIZE));
    }

    if (a.hasValue(R.styleable.BottomSheetBehavior_Layout_android_maxHeight)) {
      setMaxHeight(
          a.getDimensionPixelSize(
              R.styleable.BottomSheetBehavior_Layout_android_maxHeight, NO_MAX_SIZE));
    }

    TypedValue value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight);
    if (value != null && value.data == PEEK_HEIGHT_AUTO) {
      setPeekHeight(value.data);
    } else {
      setPeekHeight(
          a.getDimensionPixelSize(
              R.styleable.BottomSheetBehavior_Layout_behavior_peekHeight, PEEK_HEIGHT_AUTO));
    }
    setHideable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_hideable, false));
    setGestureInsetBottomIgnored(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_gestureInsetBottomIgnored, false));
    setFitToContents(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_fitToContents, true));
    setSkipCollapsed(
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_skipCollapsed, false));
    setDraggable(a.getBoolean(R.styleable.BottomSheetBehavior_Layout_behavior_draggable, true));
    setDraggableOnNestedScroll(
        a.getBoolean(
            R.styleable.BottomSheetBehavior_Layout_behavior_draggableOnNestedScroll, true));
    setSaveFlags(a.getInt(R.styleable.BottomSheetBehavior_Layout_behavior_saveFlags, SAVE_NONE));
    setHalfExpandedRatio(
        a.getFloat(R.styleable.BottomSheetBehavior_Layout_behavior_halfExpandedRatio, 0.5f));

    value = a.peekValue(R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset);
    if (value != null && value.type == TypedValue.TYPE_FIRST_INT) {
      setExpandedOffset(value.data);
    } else {
      setExpandedOffset(
          a.getDimensionPixelOffset(
              R.styleable.BottomSheetBehavior_Layout_behavior_expandedOffset, 0));
    }

    setSignificantVelocityThreshold(
        a.getInt(
            R.styleable.BottomSheetBehavior_Layout_behavior_significantVelocityThreshold,
            DEFAULT_SIGNIFICANT_VEL_THRESHOLD));

    multipleScrollingChildrenSupported =
        a.getBoolean(
            R.styleable.BottomSheetBehavior_Layout_behavior_multipleScrollingChildrenSupported,
            false);

    // Reading out if we are handling padding, so we can apply it to the content.
    paddingBottomSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_paddingBottomSystemWindowInsets, false);
    paddingLeftSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_paddingLeftSystemWindowInsets, false);
    paddingRightSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_paddingRightSystemWindowInsets, false);
    // Setting this to false will prevent the bottomsheet from going below the status bar. Since
    // this is a breaking change from the old behavior the default is true.
    paddingTopSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_paddingTopSystemWindowInsets, true);
    marginLeftSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_marginLeftSystemWindowInsets, false);
    marginRightSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_marginRightSystemWindowInsets, false);
    marginTopSystemWindowInsets =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_marginTopSystemWindowInsets, false);
    shouldRemoveExpandedCorners =
        a.getBoolean(R.styleable.BottomSheetBehavior_Layout_shouldRemoveExpandedCorners, true);

    a.recycle();
    ViewConfiguration configuration = ViewConfiguration.get(context);
    maximumVelocity = configuration.getScaledMaximumFlingVelocity();
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
    super.onRestoreInstanceState(parent, child, ss.getSuperState());
    // Restore Optional State values designated by saveFlags
    restoreOptionalState(ss);
    // Intermediate states are restored as collapsed state
    if (ss.state == STATE_DRAGGING || ss.state == STATE_SETTLING) {
      this.state = STATE_COLLAPSED;
      this.lastStableState = this.state;
    } else {
      this.state = ss.state;
      this.lastStableState = this.state;
    }
  }

  @Override
  public void onAttachedToLayoutParams(@NonNull LayoutParams layoutParams) {
    super.onAttachedToLayoutParams(layoutParams);
    // These may already be null, but just be safe, explicitly assign them. This lets us know the
    // first time we layout with this behavior by checking (viewRef == null).
    viewRef = null;
    viewDragHelper = null;
    bottomContainerBackHelper = null;
  }

  @Override
  public void onDetachedFromLayoutParams() {
    super.onDetachedFromLayoutParams();
    // Release references so we don't run unnecessary codepaths while not attached to a view.
    viewRef = null;
    viewDragHelper = null;
    bottomContainerBackHelper = null;
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
            maxWidth,
            lp.width);
    int childHeightMeasureSpec =
        getChildMeasureSpec(
            parentHeightMeasureSpec,
            parent.getPaddingTop()
                + parent.getPaddingBottom()
                + lp.topMargin
                + lp.bottomMargin
                + heightUsed,
            maxHeight,
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
      peekHeightMin =
          parent.getResources().getDimensionPixelSize(R.dimen.design_bottom_sheet_peek_height_min);
      setWindowInsetsListener(child);
      ViewCompat.setWindowInsetsAnimationCallback(child, new InsetsAnimationCallback(child));
      viewRef = new WeakReference<>(child);
      bottomContainerBackHelper = new MaterialBottomContainerBackHelper(child);
      // Only set MaterialShapeDrawable as background if shapeTheming is enabled, otherwise will
      // default to android:background declared in styles or layout.
      if (materialShapeDrawable != null) {
        child.setBackground(materialShapeDrawable);
        // Use elevation attr if set on bottomsheet; otherwise, use elevation of child view.
        materialShapeDrawable.setElevation(elevation == -1 ? child.getElevation() : elevation);
      } else if (backgroundTint != null) {
        ViewCompat.setBackgroundTintList(child, backgroundTint);
      }
      updateAccessibilityActions();
      if (child.getImportantForAccessibility() == View.IMPORTANT_FOR_ACCESSIBILITY_AUTO) {
        child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
      }
    }
    if (viewDragHelper == null) {
      viewDragHelper = ViewDragHelper.create(parent, dragCallback);
    }

    int savedTop = child.getTop();
    // First let the parent lay it out
    parent.onLayoutChild(child, layoutDirection);
    // Offset the bottom sheet
    parentWidth = parent.getWidth();
    parentHeight = parent.getHeight();
    childHeight = child.getHeight();
    if (parentHeight - childHeight < insetTop) {
      if (paddingTopSystemWindowInsets) {
        // If the bottomsheet would land in the middle of the status bar when fully expanded add
        // extra space to make sure it goes all the way up or up to max height if it is specified.
        childHeight = (maxHeight == NO_MAX_SIZE) ? parentHeight : min(parentHeight, maxHeight);
      } else {
        // If we don't want the bottomsheet to go under the status bar we cap its height
        int insetHeight = parentHeight - insetTop;
        childHeight = (maxHeight == NO_MAX_SIZE) ? insetHeight : min(insetHeight, maxHeight);
      }
    }
    fitToContentsOffset = max(0, parentHeight - childHeight);
    calculateHalfExpandedOffset();
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
    updateDrawableForTargetState(state, /* animate= */ false);

    nestedScrollingChildrenRef.clear();
    if (multipleScrollingChildrenSupported) {
      populateScrollingChildren(child);
    } else {
      nestedScrollingChildrenRef.add(new WeakReference<>(findScrollingChild(child)));
    }

    for (int i = 0; i < callbacks.size(); i++) {
      callbacks.get(i).onLayout(child);
    }
    return true;
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
        currentTouchedScrollChildRef = null;
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
        currentTouchedScrollChildRef =
            new WeakReference<>(
                findScrollingChildContainingCoordinates(
                    parent, (int) event.getX(), (int) event.getY()));
        // Only intercept nested scrolling events here if the view not being moved by the
        // ViewDragHelper.
        if (state != STATE_SETTLING) {
          if (currentTouchedScrollChildRef.get() != null) {
            activePointerId = event.getPointerId(event.getActionIndex());
            if (!isTouchingDragHandle(parent, initialX, initialY)) {
              touchingScrollingChild = true;
            }
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

    // We have to handle cases that the ViewDragHelper does not capture the bottom sheet because
    // it is not the top most view of its parent. This is not necessary when the touch event is
    // happening over the scrolling content as nested scrolling logic handles that case.
    return action == MotionEvent.ACTION_MOVE
        && hasScrollingChild()
        && !ignoreEvents
        && state != STATE_DRAGGING
        && !isTouchingScrollingChild(parent, event)
        && viewDragHelper != null
        && initialY != INVALID_POSITION
        && Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop();
  }

  private boolean hasScrollingChild() {
  for (WeakReference<View> ref : nestedScrollingChildrenRef) {
      if (ref.get() != null) {
        return true;
      }
    }
    return false;
  }

  @Nullable
  private View getSingleScrollingChildOrNull() {
    return !nestedScrollingChildrenRef.isEmpty() ? nestedScrollingChildrenRef.get(0).get() : null;
  }

  private boolean isTouchingScrollingChild(CoordinatorLayout parent, MotionEvent event) {
    if (multipleScrollingChildrenSupported) {
      return currentTouchedScrollChildRef != null && currentTouchedScrollChildRef.get() != null;
    }
    View scroll = getSingleScrollingChildOrNull();
    return scroll != null
        && parent.isPointInChildBounds(scroll, (int) event.getX(), (int) event.getY());
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
      reset();
    }
    if (velocityTracker == null) {
      velocityTracker = VelocityTracker.obtain();
    }
    velocityTracker.addMovement(event);
    // The ViewDragHelper tries to capture only the top-most View. We have to explicitly tell it
    // to capture the bottom sheet in case it is not captured and the touch slop is passed.
    if (shouldHandleDraggingWithHelper() && action == MotionEvent.ACTION_MOVE && !ignoreEvents) {
      if (Math.abs(initialY - event.getY()) > viewDragHelper.getTouchSlop()) {
        viewDragHelper.captureChildView(child, event.getPointerId(event.getActionIndex()));
      }
    }
    return !ignoreEvents;
  }

  @Override
  public boolean onStartNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View directTargetChild,
      @NonNull View target,
      int axes,
      int type) {
    lastNestedScrollDy = 0;
    nestedScrolled = false;
    return (axes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0;
  }

  private boolean isViewScrollingChild(View v) {
    for (WeakReference<View> ref : nestedScrollingChildrenRef) {
      if (ref.get() == v) {
        return true;
      }
    }
    return false;
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
    boolean isTargetScrollingChild = isViewScrollingChild(target);
    if (isNestedScrollingCheckEnabled() && !isTargetScrollingChild) {
      return;
    }
    int currentTop = child.getTop();
    int newTop = currentTop - dy;
    if (dy > 0) { // Upward swipe
      if (!nestedScrolled
          && !draggableOnNestedScroll
          && isTargetScrollingChild
          && target.canScrollVertically(1)) {
        // Prevent dragging if draggableOnNestedScroll=false and we can scroll the scrolling child.
        draggableOnNestedScrollLastDragIgnored = true;
        return;
      }
      if (newTop < getExpandedOffset()) {
        consumed[1] = currentTop - getExpandedOffset();
        ViewCompat.offsetTopAndBottom(child, -consumed[1]);
        setStateInternal(STATE_EXPANDED);
      } else {
        if (!draggable) {
          // Prevent dragging
          return;
        }

        consumed[1] = dy;
        ViewCompat.offsetTopAndBottom(child, -dy);
        setStateInternal(STATE_DRAGGING);
      }
    } else if (dy < 0) { // Downward swipe
      boolean canScrollUp = target.canScrollVertically(-1);
      if (!nestedScrolled && !draggableOnNestedScroll && isTargetScrollingChild && canScrollUp) {
        // Prevent dragging if draggableOnNestedScroll=false and we can scroll the scrolling child.
        draggableOnNestedScrollLastDragIgnored = true;
        return;
      }
      if (!canScrollUp) {
        if (newTop <= collapsedOffset || canBeHiddenByDragging()) {
          if (!draggable) {
            // Prevent dragging
            return;
          }

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
    draggableOnNestedScrollLastDragIgnored = false;
  }

  @Override
  public void onStopNestedScroll(
      @NonNull CoordinatorLayout coordinatorLayout,
      @NonNull V child,
      @NonNull View target,
      int type) {
    if (child.getTop() == getExpandedOffset()) {
      setStateInternal(STATE_EXPANDED);
      return;
    }
    if (isNestedScrollingCheckEnabled() && (!isViewScrollingChild(target) || !nestedScrolled)) {
      return;
    }
    @StableState int targetState;
    if (lastNestedScrollDy > 0) {
      if (fitToContents) {
        targetState = STATE_EXPANDED;
      } else {
        int currentTop = child.getTop();
        if (currentTop > halfExpandedOffset) {
          targetState = STATE_HALF_EXPANDED;
        } else {
          targetState = STATE_EXPANDED;
        }
      }
    } else if (hideable && shouldHide(child, getYVelocity())) {
      targetState = STATE_HIDDEN;
    } else if (lastNestedScrollDy == 0) {
      int currentTop = child.getTop();
      if (fitToContents) {
        if (Math.abs(currentTop - fitToContentsOffset) < Math.abs(currentTop - collapsedOffset)) {
          targetState = STATE_EXPANDED;
        } else {
          targetState = STATE_COLLAPSED;
        }
      } else {
        if (currentTop < halfExpandedOffset) {
          if (currentTop < Math.abs(currentTop - collapsedOffset)) {
            targetState = STATE_EXPANDED;
          } else {
            if (shouldSkipHalfExpandedStateWhenDragging()) {
              targetState = STATE_COLLAPSED;
            } else {
              targetState = STATE_HALF_EXPANDED;
            }
          }
        } else {
          if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
            targetState = STATE_HALF_EXPANDED;
          } else {
            targetState = STATE_COLLAPSED;
          }
        }
      }
    } else {
      if (fitToContents) {
        targetState = STATE_COLLAPSED;
      } else {
        // Settle to nearest height.
        int currentTop = child.getTop();
        if (Math.abs(currentTop - halfExpandedOffset) < Math.abs(currentTop - collapsedOffset)) {
          targetState = STATE_HALF_EXPANDED;
        } else {
          targetState = STATE_COLLAPSED;
        }
      }
    }
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

    if (isNestedScrollingCheckEnabled() && hasScrollingChild()) {
      return isViewScrollingChild(target)
          && ((state != STATE_EXPANDED && !draggableOnNestedScrollLastDragIgnored)
              || super.onNestedPreFling(coordinatorLayout, child, target, velocityX, velocityY));
    } else {
      return false;
    }
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

    updateDrawableForTargetState(state, /* animate= */ true);
    updateAccessibilityActions();
  }

  /**
   * Sets the maximum width of the bottom sheet. The layout will be at most this dimension wide.
   * This method should be called before {@link BottomSheetDialog#show()} in order for the width to
   * be adjusted as expected.
   *
   * @param maxWidth The maximum width in pixels to be set
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_android_maxWidth
   * @see #getMaxWidth()
   */
  public void setMaxWidth(@Px int maxWidth) {
    this.maxWidth = maxWidth;
  }

  /**
   * Returns the bottom sheet's maximum width, or -1 if no maximum width is set.
   *
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_android_maxWidth
   * @see #setMaxWidth(int)
   */
  @Px
  public int getMaxWidth() {
    return maxWidth;
  }

  /**
   * Sets the maximum height of the bottom sheet. This method should be called before {@link
   * BottomSheetDialog#show()} in order for the height to be adjusted as expected.
   *
   * @param maxHeight The maximum height in pixels to be set
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_android_maxHeight
   * @see #getMaxHeight()
   */
  public void setMaxHeight(@Px int maxHeight) {
    this.maxHeight = maxHeight;
  }

  /**
   * Returns the bottom sheet's maximum height, or -1 if no maximum height is set.
   *
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_android_maxHeight
   * @see #setMaxHeight(int)
   */
  @Px
  public int getMaxHeight() {
    return maxHeight;
  }

  /**
   * Sets the height of the bottom sheet when it is collapsed.
   *
   * @param peekHeight The height of the collapsed bottom sheet in pixels, or {@link
   *     #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically at 16:9 ratio keyline.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
   */
  public void setPeekHeight(int peekHeight) {
    setPeekHeight(peekHeight, false);
  }

  /**
   * Sets the height of the bottom sheet when it is collapsed while optionally animating between the
   * old height and the new height.
   *
   * @param peekHeight The height of the collapsed bottom sheet in pixels, or {@link
   *     #PEEK_HEIGHT_AUTO} to configure the sheet to peek automatically at 16:9 ratio keyline.
   * @param animate Whether to animate between the old height and the new height.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
   */
  public final void setPeekHeight(int peekHeight, boolean animate) {
    boolean layout = false;
    if (peekHeight == PEEK_HEIGHT_AUTO) {
      if (!peekHeightAuto) {
        peekHeightAuto = true;
        layout = true;
      }
    } else if (peekHeightAuto || this.peekHeight != peekHeight) {
      peekHeightAuto = false;
      this.peekHeight = max(0, peekHeight);
      layout = true;
    }
    // If sheet is already laid out, recalculate the collapsed offset based on new setting.
    // Otherwise, let onLayoutChild handle this later.
    if (layout) {
      updatePeekHeight(animate);
    }
  }

  private void updatePeekHeight(boolean animate) {
    if (viewRef != null) {
      calculateCollapsedOffset();
      if (state == STATE_COLLAPSED) {
        V view = viewRef.get();
        if (view != null) {
          if (animate) {
            setState(STATE_COLLAPSED);
          } else {
            view.requestLayout();
          }
        }
      }
    }
  }

  /**
   * Gets the height of the bottom sheet when it is collapsed.
   *
   * @return The height of the collapsed bottom sheet in pixels, or {@link #PEEK_HEIGHT_AUTO} if the
   *     sheet is configured to peek automatically at 16:9 ratio keyline
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_peekHeight
   */
  public int getPeekHeight() {
    return peekHeightAuto ? PEEK_HEIGHT_AUTO : peekHeight;
  }

  /**
   * Determines the height of the BottomSheet in the {@link #STATE_HALF_EXPANDED} state. The
   * material guidelines recommended a value of 0.5, which results in the sheet filling half of the
   * parent. The height of the BottomSheet will be smaller as this ratio is decreased and taller as
   * it is increased. The default value is 0.5.
   *
   * @param ratio a float between 0 and 1, representing the {@link #STATE_HALF_EXPANDED} ratio.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_halfExpandedRatio
   */
  public void setHalfExpandedRatio(
      @FloatRange(from = 0.0f, to = 1.0f, fromInclusive = false, toInclusive = false) float ratio) {

    if ((ratio <= 0) || (ratio >= 1)) {
      throw new IllegalArgumentException("ratio must be a float value between 0 and 1");
    }
    this.halfExpandedRatio = ratio;
    // If sheet is already laid out, recalculate the half expanded offset based on new setting.
    // Otherwise, let onLayoutChild handle this later.
    if (viewRef != null) {
      calculateHalfExpandedOffset();
    }
  }

  /**
   * Gets the ratio for the height of the BottomSheet in the {@link #STATE_HALF_EXPANDED} state.
   *
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_halfExpandedRatio
   */
  @FloatRange(from = 0.0f, to = 1.0f)
  public float getHalfExpandedRatio() {
    return halfExpandedRatio;
  }

  /**
   * Determines the top offset of the BottomSheet in the {@link #STATE_EXPANDED} state when
   * fitsToContent is false. The default value is 0, which results in the sheet matching the
   * parent's top.
   *
   * @param offset an integer value greater than equal to 0, representing the {@link
   *     #STATE_EXPANDED} offset. Value must not exceed the offset in the half expanded state.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_expandedOffset
   */
  public void setExpandedOffset(int offset) {
    if (offset < 0) {
      throw new IllegalArgumentException("offset must be greater than or equal to 0");
    }
    this.expandedOffset = offset;
    updateDrawableForTargetState(state, /* animate= */ true);
  }

  /**
   * Returns the current expanded offset. If {@code fitToContents} is true, it will automatically
   * pick the offset depending on the height of the content.
   *
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_expandedOffset
   */
  public int getExpandedOffset() {
    return fitToContents
        ? fitToContentsOffset
        : Math.max(expandedOffset, paddingTopSystemWindowInsets ? 0 : insetTop);
  }

  /**
   * Calculates the current offset of the bottom sheet.
   *
   * This method should be called when the child view is laid out.
   *
   * @return The offset of this bottom sheet within [-1,1] range. Offset increases
   * as this bottom sheet is moving upward. From 0 to 1 the sheet is between collapsed and
   * expanded states and from -1 to 0 it is between hidden and collapsed states. Returns
   * -1 if the bottom sheet is not laid out (therefore it's hidden).
   */
  public float calculateSlideOffset() {
    if (viewRef == null || viewRef.get() == null) {
      return -1;
    }

    return calculateSlideOffsetWithTop(viewRef.get().getTop());
  }

  /**
   * Sets whether this bottom sheet can hide.
   *
   * @param hideable {@code true} to make this bottom sheet hideable.
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
   */
  public void setHideable(boolean hideable) {
    if (this.hideable != hideable) {
      this.hideable = hideable;
      if (!hideable && state == STATE_HIDDEN) {
        // Lift up to collapsed state
        setState(STATE_COLLAPSED);
      }
      updateAccessibilityActions();
    }
  }

  /**
   * Gets whether this bottom sheet can hide when it is swiped down.
   *
   * @return {@code true} if this bottom sheet can hide.
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_hideable
   */
  public boolean isHideable() {
    return hideable;
  }

  /**
   * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
   * is expanded once. Setting this to true has no effect unless the sheet is hideable.
   *
   * @param skipCollapsed True if the bottom sheet should skip the collapsed state.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
   */
  public void setSkipCollapsed(boolean skipCollapsed) {
    this.skipCollapsed = skipCollapsed;
  }

  /**
   * Sets whether this bottom sheet should skip the collapsed state when it is being hidden after it
   * is expanded once.
   *
   * @return Whether the bottom sheet should skip the collapsed state.
   * @attr ref
   *     com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_skipCollapsed
   */
  public boolean getSkipCollapsed() {
    return skipCollapsed;
  }

  /**
   * Sets whether this bottom sheet can be collapsed/expanded by dragging. Note: When disabling
   * dragging, an app will require to implement a custom way to expand/collapse the bottom sheet
   *
   * @param draggable {@code false} to prevent dragging the sheet to collapse and expand
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_draggable
   */
  public void setDraggable(boolean draggable) {
    this.draggable = draggable;
  }

  public boolean isDraggable() {
    return draggable;
  }

  /**
   * Sets whether this bottom sheet can be collapsed/expanded by dragging on the nested scrolling
   * child view. Default is true.
   *
   * @param draggableOnNestedScroll {@code false} to prevent dragging the nested scrolling child
   * view to collapse and expand the sheet
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_draggableOnNestedScroll
   */
  public void setDraggableOnNestedScroll(boolean draggableOnNestedScroll) {
    this.draggableOnNestedScroll = draggableOnNestedScroll;
  }

  public boolean isDraggableOnNestedScroll() {
    return draggableOnNestedScroll;
  }

  /**
   * Sets the velocity threshold considered significant enough to trigger a slide
   * to the next stable state.
   *
   * @param significantVelocityThreshold The velocity threshold that warrants a vertical swipe.
   * @see #getSignificantVelocityThreshold()
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_significantVelocityThreshold
   */
  public void setSignificantVelocityThreshold(int significantVelocityThreshold) {
    this.significantVelocityThreshold = significantVelocityThreshold;
  }

  /**
   * Returns the significant velocity threshold.
   *
   * @see #setSignificantVelocityThreshold(int)
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_significantVelocityThreshold
   */
  public int getSignificantVelocityThreshold() {
    return this.significantVelocityThreshold;
  }

  /**
   * Sets save flags to be preserved in bottomsheet on configuration change.
   *
   * @param flags bitwise int of {@link #SAVE_PEEK_HEIGHT}, {@link #SAVE_FIT_TO_CONTENTS}, {@link
   *     #SAVE_HIDEABLE}, {@link #SAVE_SKIP_COLLAPSED}, {@link #SAVE_ALL} and {@link #SAVE_NONE}.
   * @see #getSaveFlags()
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_saveFlags
   */
  public void setSaveFlags(@SaveFlags int flags) {
    this.saveFlags = flags;
  }
  /**
   * Returns the save flags.
   *
   * @see #setSaveFlags(int)
   * @attr ref com.google.android.material.R.styleable#BottomSheetBehavior_Layout_behavior_saveFlags
   */
  @SaveFlags
  public int getSaveFlags() {
    return this.saveFlags;
  }

  /**
   * Sets the friction coefficient to hide the bottom sheet, or set it to the next closest
   * expanded state.
   *
   * @param hideFriction The friction coefficient that determines the swipe velocity needed to
   *  hide or set the bottom sheet to the closest expanded state.
   */
  public void setHideFriction(float hideFriction) {
    this.hideFriction = hideFriction;
  }

  /**
   * Gets the friction coefficient to hide the bottom sheet, or set it to the next closest
   * expanded state.
   *
   * @return The friction coefficient that determines the swipe velocity needed to hide or set the
   *  bottom sheet to the closest expanded state.
   */
  public float getHideFriction() {
    return this.hideFriction;
  }

  /**
   * Sets a callback to be notified of bottom sheet events.
   *
   * @param callback The callback to notify when bottom sheet events occur.
   * @deprecated use {@link #addBottomSheetCallback(BottomSheetCallback)} and {@link
   *     #removeBottomSheetCallback(BottomSheetCallback)} instead
   */
  @Deprecated
  public void setBottomSheetCallback(BottomSheetCallback callback) {
    Log.w(
        TAG,
        "BottomSheetBehavior now supports multiple callbacks. `setBottomSheetCallback()` removes"
            + " all existing callbacks, including ones set internally by library authors, which"
            + " may result in unintended behavior. This may change in the future. Please use"
            + " `addBottomSheetCallback()` and `removeBottomSheetCallback()` instead to set your"
            + " own callbacks.");
    callbacks.clear();
    if (callback != null) {
      callbacks.add(callback);
    }
  }

  /**
   * Adds a callback to be notified of bottom sheet events.
   *
   * @param callback The callback to notify when bottom sheet events occur.
   */
  public void addBottomSheetCallback(@NonNull BottomSheetCallback callback) {
    if (!callbacks.contains(callback)) {
      callbacks.add(callback);
    }
  }

  /**
   * Removes a previously added callback.
   *
   * @param callback The callback to remove.
   */
  public void removeBottomSheetCallback(@NonNull BottomSheetCallback callback) {
    callbacks.remove(callback);
  }

  /**
   * Sets the state of the bottom sheet. The bottom sheet will transition to that state with
   * animation.
   *
   * @param state One of {@link #STATE_COLLAPSED}, {@link #STATE_EXPANDED}, {@link #STATE_HIDDEN},
   *     or {@link #STATE_HALF_EXPANDED}.
   */
  public void setState(@StableState int state) {
    if (state == STATE_DRAGGING || state == STATE_SETTLING) {
      throw new IllegalArgumentException(
          "STATE_"
              + (state == STATE_DRAGGING ? "DRAGGING" : "SETTLING")
              + " should not be set externally.");
    }
    if (!hideable && state == STATE_HIDDEN) {
      Log.w(TAG, "Cannot set state: " + state);
      return;
    }
    final int finalState;
    if (state == STATE_HALF_EXPANDED
        && fitToContents
        && getTopOffsetForState(state) <= fitToContentsOffset) {
      // Skip to the expanded state if we would scroll past the height of the contents.
      finalState = STATE_EXPANDED;
    } else {
      finalState = state;
    }
    if (viewRef == null || viewRef.get() == null) {
      // The view is not laid out yet; modify mState and let onLayoutChild handle it later
      setStateInternal(state);
    } else {
      final V child = viewRef.get();
      runAfterLayout(
          child,
          new Runnable() {
            @Override
            public void run() {
              startSettling(child, finalState, false);
            }
          });
    }
  }

  private void runAfterLayout(V child, Runnable runnable) {
    if (isLayouting(child)) {
      child.post(runnable);
    } else {
      runnable.run();
    }
  }

  private boolean isLayouting(V child) {
    ViewParent parent = child.getParent();
    return parent != null && parent.isLayoutRequested() && child.isAttachedToWindow();
  }

  /**
   * Sets whether this bottom sheet should adjust it's position based on the system gesture area on
   * Android Q and above.
   *
   * <p>Note: the bottom sheet will only adjust it's position if it would be unable to be scrolled
   * upwards because the peekHeight is less than the gesture inset margins,(because that would cause
   * a gesture conflict), gesture navigation is enabled, and this {@code ignoreGestureInsetBottom}
   * flag is false.
   */
  public void setGestureInsetBottomIgnored(boolean gestureInsetBottomIgnored) {
    this.gestureInsetBottomIgnored = gestureInsetBottomIgnored;
  }

  /**
   * Returns whether this bottom sheet should adjust it's position based on the system gesture area.
   */
  public boolean isGestureInsetBottomIgnored() {
    return gestureInsetBottomIgnored;
  }

  /**
   * Sets whether the bottom sheet should remove its corners when it reaches the expanded state.
   *
   * <p>If false, the bottom sheet will only remove its corners if it is expanded and reaches the
   * top of the screen.
   */
  public void setShouldRemoveExpandedCorners(boolean shouldRemoveExpandedCorners) {
    if (this.shouldRemoveExpandedCorners != shouldRemoveExpandedCorners) {
      this.shouldRemoveExpandedCorners = shouldRemoveExpandedCorners;
      updateDrawableForTargetState(getState(), /* animate= */ true);
    }
  }

  /**
   * Returns whether the bottom sheet will remove its corners when it reaches the expanded state.
   */
  public boolean isShouldRemoveExpandedCorners() {
    return shouldRemoveExpandedCorners;
  }

  /**
   * Gets the current state of the bottom sheet.
   *
   * @return One of {@link #STATE_EXPANDED}, {@link #STATE_HALF_EXPANDED}, {@link #STATE_COLLAPSED},
   *     {@link #STATE_DRAGGING}, or {@link #STATE_SETTLING}.
   */
  @State
  public int getState() {
    return state;
  }

  void setStateInternal(@State int state) {
    if (this.state == state) {
      return;
    }
    this.state = state;
    if (state == STATE_COLLAPSED
        || state == STATE_EXPANDED
        || state == STATE_HALF_EXPANDED
        || (hideable && state == STATE_HIDDEN)) {
      this.lastStableState = state;
    }

    if (viewRef == null) {
      return;
    }

    View bottomSheet = viewRef.get();
    if (bottomSheet == null) {
      return;
    }

    if (state == STATE_EXPANDED) {
      updateImportantForAccessibility(true);
    } else if (state == STATE_HALF_EXPANDED || state == STATE_HIDDEN || state == STATE_COLLAPSED) {
      updateImportantForAccessibility(false);
    }

    updateDrawableForTargetState(state, /* animate= */ true);
    for (int i = 0; i < callbacks.size(); i++) {
      callbacks.get(i).onStateChanged(bottomSheet, state);
    }
    updateAccessibilityActions();
  }

  private void updateDrawableForTargetState(@State int state, boolean animate) {
    if (state == STATE_SETTLING) {
      // Special case: we want to know which state we're settling to, so wait for another call.
      return;
    }

    boolean removeCorners = isExpandedAndShouldRemoveCorners();
    if (expandedCornersRemoved == removeCorners || materialShapeDrawable == null) {
      return;
    }
    expandedCornersRemoved = removeCorners;
    if (animate && interpolatorAnimator != null) {
      if (interpolatorAnimator.isRunning()) {
        interpolatorAnimator.reverse();
      } else {
        float from = materialShapeDrawable.getInterpolation();
        float to = removeCorners ? calculateInterpolationWithCornersRemoved() : 1f;
        interpolatorAnimator.setFloatValues(from, to);
        interpolatorAnimator.start();
      }
    } else {
      if (interpolatorAnimator != null && interpolatorAnimator.isRunning()) {
        interpolatorAnimator.cancel();
      }
      materialShapeDrawable.setInterpolation(
          expandedCornersRemoved ? calculateInterpolationWithCornersRemoved() : 1f);
    }
  }

  private float calculateInterpolationWithCornersRemoved() {
    if (materialShapeDrawable != null
        && viewRef != null
        && viewRef.get() != null
        && VERSION.SDK_INT >= VERSION_CODES.S) {
      V view = viewRef.get();
      // Only use device corner radius if sheet is touching top of screen.
      if (isAtTopOfScreen()) {
        final WindowInsets insets = view.getRootWindowInsets();
        if (insets != null) {
          float topLeftInterpolation =
              calculateCornerInterpolation(
                  materialShapeDrawable.getTopLeftCornerResolvedSize(),
                  insets.getRoundedCorner(RoundedCorner.POSITION_TOP_LEFT));
          float topRightInterpolation =
              calculateCornerInterpolation(
                  materialShapeDrawable.getTopRightCornerResolvedSize(),
                  insets.getRoundedCorner(RoundedCorner.POSITION_TOP_RIGHT));
          return Math.max(topLeftInterpolation, topRightInterpolation);
        }
      }
    }
    return 0;
  }

  @RequiresApi(VERSION_CODES.S)
  private float calculateCornerInterpolation(
      float materialShapeDrawableCornerSize, @Nullable RoundedCorner deviceRoundedCorner) {
    if (deviceRoundedCorner != null) {
      float deviceCornerRadius = deviceRoundedCorner.getRadius();
      if (deviceCornerRadius > 0 && materialShapeDrawableCornerSize > 0) {
        return deviceCornerRadius / materialShapeDrawableCornerSize;
      }
    }
    return 0;
  }

  /**
   * Returns the scrolling child that contains the given coordinates, if any. If no scrolling child
   * contains those coordinates, returns null.
   */
  @Nullable
  private View findScrollingChildContainingCoordinates(
      @NonNull CoordinatorLayout parent, int xCoordinate, int yCoordinate) {
    if (nestedScrollingChildrenRef.isEmpty()) {
      return null;
    }

    // Check to see if point is in any scrolling child.
    for (WeakReference<View> ref : nestedScrollingChildrenRef) {
      View scrollingChild = ref.get();
      if (scrollingChild != null
          && parent.isPointInChildBounds(scrollingChild, xCoordinate, yCoordinate)) {
        return scrollingChild;
      }
    }

    return null;
  }

  private boolean isTouchingDragHandle(
      @NonNull CoordinatorLayout parent, int xCoordinate, int yCoordinate) {
    View dragHandleView = dragHandleViewRef != null ? dragHandleViewRef.get() : null;
    return dragHandleView != null
        && parent.isPointInChildBounds(dragHandleView, xCoordinate, yCoordinate);
  }

  private boolean isAtTopOfScreen() {
    if (viewRef == null || viewRef.get() == null) {
      return false;
    }
    int[] location = new int[2];
    viewRef.get().getLocationOnScreen(location);
    return location[1] == 0;
  }

  private boolean isExpandedAndShouldRemoveCorners() {
    // Only remove corners when it's full screen.
    return state == STATE_EXPANDED && (shouldRemoveExpandedCorners || isAtTopOfScreen());
  }

  private int calculatePeekHeight() {
    if (peekHeightAuto) {
      int desiredHeight = max(peekHeightMin, parentHeight - parentWidth * 9 / 16);
      return min(desiredHeight, childHeight) + insetBottom;
    }
    // Only make sure the peek height is above the gesture insets if we're not applying system
    // insets.
    if (!gestureInsetBottomIgnored && !paddingBottomSystemWindowInsets && gestureInsetBottom > 0) {
      return max(peekHeight, gestureInsetBottom + peekHeightGestureInsetBuffer);
    }
    return peekHeight + insetBottom;
  }

  private void calculateCollapsedOffset() {
    int peek = calculatePeekHeight();

    if (fitToContents) {
      collapsedOffset = max(parentHeight - peek, fitToContentsOffset);
    } else {
      collapsedOffset = parentHeight - peek;
    }
  }

  private void calculateHalfExpandedOffset() {
    this.halfExpandedOffset = (int) (parentHeight * (1 - halfExpandedRatio));
  }

  private float calculateSlideOffsetWithTop(int top) {
      return
          (top > collapsedOffset || collapsedOffset == getExpandedOffset())
              ? (float) (collapsedOffset - top) / (parentHeight - collapsedOffset)
              : (float) (collapsedOffset - top) / (collapsedOffset - getExpandedOffset());
  }

  private void reset() {
    activePointerId = ViewDragHelper.INVALID_POINTER;
    initialY = INVALID_POSITION;
    currentTouchedScrollChildRef = null;
    if (velocityTracker != null) {
      velocityTracker.recycle();
      velocityTracker = null;
    }
  }

  private void restoreOptionalState(@NonNull SavedState ss) {
    if (this.saveFlags == SAVE_NONE) {
      return;
    }
    if (this.saveFlags == SAVE_ALL || (this.saveFlags & SAVE_PEEK_HEIGHT) == SAVE_PEEK_HEIGHT) {
      this.peekHeight = ss.peekHeight;
    }
    if (this.saveFlags == SAVE_ALL
        || (this.saveFlags & SAVE_FIT_TO_CONTENTS) == SAVE_FIT_TO_CONTENTS) {
      this.fitToContents = ss.fitToContents;
    }
    if (this.saveFlags == SAVE_ALL || (this.saveFlags & SAVE_HIDEABLE) == SAVE_HIDEABLE) {
      this.hideable = ss.hideable;
    }
    if (this.saveFlags == SAVE_ALL
        || (this.saveFlags & SAVE_SKIP_COLLAPSED) == SAVE_SKIP_COLLAPSED) {
      this.skipCollapsed = ss.skipCollapsed;
    }
  }

  boolean shouldHide(@NonNull View child, float yvel) {
    if (skipCollapsed) {
      return true;
    }
    if (!isHideableWhenDragging()) {
      return false;
    }
    if (child.getTop() < collapsedOffset) {
      // It should not hide, but collapse.
      return false;
    }
    int peek = calculatePeekHeight();
    final float newTop = child.getTop() + yvel * hideFriction;
    return Math.abs(newTop - collapsedOffset) / (float) peek > HIDE_THRESHOLD;
  }

  @Override
  public void startBackProgress(@NonNull BackEventCompat backEvent) {
    if (bottomContainerBackHelper == null) {
      return;
    }
    bottomContainerBackHelper.startBackProgress(backEvent);
  }

  @Override
  public void updateBackProgress(@NonNull BackEventCompat backEvent) {
    if (bottomContainerBackHelper == null) {
      return;
    }
    bottomContainerBackHelper.updateBackProgress(backEvent);
  }

  @Override
  public void handleBackInvoked() {
    if (bottomContainerBackHelper == null) {
      return;
    }
    BackEventCompat backEvent = bottomContainerBackHelper.onHandleBackInvoked();
    if (backEvent == null || VERSION.SDK_INT < VERSION_CODES.UPSIDE_DOWN_CAKE) {
      // If using traditional button system nav or if pre-U, just hide or collapse the bottom sheet.
      setState(hideable ? STATE_HIDDEN : STATE_COLLAPSED);
      return;
    }
    if (hideable) {
      bottomContainerBackHelper.finishBackProgressNotPersistent(
          backEvent,
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
              // Hide immediately following the built-in predictive back slide down animation.
              setStateInternal(STATE_HIDDEN);
              if (viewRef != null && viewRef.get() != null) {
                viewRef.get().requestLayout();
              }
            }
          });
    } else {
      bottomContainerBackHelper.finishBackProgressPersistent(
          backEvent, /* animatorListener= */ null);
      setState(STATE_COLLAPSED);
    }
  }

  @Override
  public void cancelBackProgress() {
    if (bottomContainerBackHelper == null) {
      return;
    }
    bottomContainerBackHelper.cancelBackProgress();
  }

  @VisibleForTesting
  @Nullable
  MaterialBottomContainerBackHelper getBackHelper() {
    return bottomContainerBackHelper;
  }

  @Nullable
  @VisibleForTesting
  View findScrollingChild(View view) {
    if (view.getVisibility() != View.VISIBLE) {
      return null;
    }
    if (view.isNestedScrollingEnabled()) {
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

  @VisibleForTesting
  void populateScrollingChildren(View view) {
    if (view.getVisibility() != View.VISIBLE) {
      return;
    }
    if (view.isNestedScrollingEnabled()) {
      nestedScrollingChildrenRef.add(new WeakReference<>(view));
      return;
    }
    if (view instanceof ViewGroup) {
      ViewGroup group = (ViewGroup) view;
      for (int i = 0; i < group.getChildCount(); i++) {
        populateScrollingChildren(group.getChildAt(i));
      }
    }
  }

  private boolean shouldHandleDraggingWithHelper() {
    // If it's not draggable, do not forward events to viewDragHelper; however, if it's already
    // dragging, let it finish.
    return viewDragHelper != null && (draggable || state == STATE_DRAGGING);
  }

  private void createMaterialShapeDrawableIfNeeded(@NonNull Context context) {
    if (shapeAppearanceModelDefault == null) {
      return;
    }

    this.materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModelDefault);
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

  MaterialShapeDrawable getMaterialShapeDrawable() {
    return materialShapeDrawable;
  }

  private void createShapeValueAnimator() {
    interpolatorAnimator = ValueAnimator.ofFloat(calculateInterpolationWithCornersRemoved(), 1f);
    interpolatorAnimator.setDuration(CORNER_ANIMATION_DURATION);
    interpolatorAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator animation) {
            float value = (float) animation.getAnimatedValue();
            if (materialShapeDrawable != null) {
              materialShapeDrawable.setInterpolation(value);
            }
          }
        });
  }

  private void setWindowInsetsListener(@NonNull View child) {
    // Ensure the peek height is at least as large as the bottom gesture inset size so that
    // the sheet can always be dragged, but only when the inset is required by the system.
    final boolean shouldHandleGestureInsets =
        VERSION.SDK_INT >= VERSION_CODES.Q && !isGestureInsetBottomIgnored() && !peekHeightAuto;

    // If were not handling insets at all, don't apply the listener.
    if (!paddingBottomSystemWindowInsets
        && !paddingLeftSystemWindowInsets
        && !paddingRightSystemWindowInsets
        && !marginLeftSystemWindowInsets
        && !marginRightSystemWindowInsets
        && !marginTopSystemWindowInsets
        && !shouldHandleGestureInsets) {
      return;
    }
    ViewUtils.doOnApplyWindowInsets(
        child,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @Override
          @SuppressWarnings("deprecation") // getSystemWindowInsetBottom is used for adjustResize.
          public WindowInsetsCompat onApplyWindowInsets(
              View view, WindowInsetsCompat insets, RelativePadding initialPadding) {
            Insets systemBarInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets mandatoryGestureInsets =
                insets.getInsets(WindowInsetsCompat.Type.mandatorySystemGestures());

            insetTop = systemBarInsets.top;

            boolean isRtl = ViewUtils.isLayoutRtl(view);

            int bottomPadding = view.getPaddingBottom();
            int leftPadding = view.getPaddingLeft();
            int rightPadding = view.getPaddingRight();

            if (paddingBottomSystemWindowInsets) {
              // Intentionally uses getSystemWindowInsetBottom to apply padding properly when
              // adjustResize is used as the windowSoftInputMode.
              insetBottom = insets.getSystemWindowInsetBottom();
              bottomPadding = initialPadding.bottom + insetBottom;
            }

            if (paddingLeftSystemWindowInsets) {
              leftPadding = isRtl ? initialPadding.end : initialPadding.start;
              leftPadding += systemBarInsets.left;
            }

            if (paddingRightSystemWindowInsets) {
              rightPadding = isRtl ? initialPadding.start : initialPadding.end;
              rightPadding += systemBarInsets.right;
            }

            MarginLayoutParams mlp = (MarginLayoutParams) view.getLayoutParams();
            boolean marginUpdated = false;

            if (marginLeftSystemWindowInsets && mlp.leftMargin != systemBarInsets.left) {
              mlp.leftMargin = systemBarInsets.left;
              marginUpdated = true;
            }

            if (marginRightSystemWindowInsets && mlp.rightMargin != systemBarInsets.right) {
              mlp.rightMargin = systemBarInsets.right;
              marginUpdated = true;
            }

            if (marginTopSystemWindowInsets && mlp.topMargin != systemBarInsets.top) {
              mlp.topMargin = systemBarInsets.top;
              marginUpdated = true;
            }

            if (marginUpdated) {
              view.setLayoutParams(mlp);
            }
            view.setPadding(leftPadding, view.getPaddingTop(), rightPadding, bottomPadding);

            if (shouldHandleGestureInsets) {
              gestureInsetBottom = mandatoryGestureInsets.bottom;
            }

            // Don't update the peek height to be above the navigation bar or gestures if these
            // flags are off. It means the client is already handling it.
            if (paddingBottomSystemWindowInsets || shouldHandleGestureInsets) {
              updatePeekHeight(/* animate= */ false);
            }
            return insets;
          }
        });
  }

  private float getYVelocity() {
    if (velocityTracker == null) {
      return 0;
    }
    velocityTracker.computeCurrentVelocity(1000, maximumVelocity);
    return velocityTracker.getYVelocity(activePointerId);
  }

  private void startSettling(View child, @StableState int state, boolean isReleasingView) {
    int top = getTopOffsetForState(state);
    boolean settling =
        viewDragHelper != null
            && (isReleasingView
                ? viewDragHelper.settleCapturedViewAt(child.getLeft(), top)
                : viewDragHelper.smoothSlideViewTo(child, child.getLeft(), top));
    if (settling) {
      setStateInternal(STATE_SETTLING);
      // STATE_SETTLING won't animate the material shape, so do that here with the target state.
      updateDrawableForTargetState(state, /* animate= */ true);
      stateSettlingTracker.continueSettlingToState(state);
    } else {
      setStateInternal(state);
    }
  }

  private int getTopOffsetForState(@StableState int state) {
    switch (state) {
      case STATE_COLLAPSED:
        return collapsedOffset;
      case STATE_EXPANDED:
        return getExpandedOffset();
      case STATE_HALF_EXPANDED:
        return halfExpandedOffset;
      case STATE_HIDDEN:
        return parentHeight;
      default:
        // Fall through
    }
    throw new IllegalArgumentException("Invalid state to get top offset: " + state);
  }

  private final ViewDragHelper.Callback dragCallback =
      new ViewDragHelper.Callback() {

        private long viewCapturedMillis;

        @Override
        public boolean tryCaptureView(@NonNull View child, int pointerId) {
          if (state == STATE_DRAGGING) {
            return false;
          }
          if (touchingScrollingChild) {
            return false;
          }
          if (state == STATE_EXPANDED && activePointerId == pointerId) {
            View scroll;
            if (multipleScrollingChildrenSupported) {
              scroll =
                  currentTouchedScrollChildRef != null ? currentTouchedScrollChildRef.get() : null;
            } else {
              scroll = getSingleScrollingChildOrNull();
            }
            if (scroll != null && scroll.canScrollVertically(-1)) {
              // Let the content scroll up
              return false;
            }
          }
          viewCapturedMillis = SystemClock.uptimeMillis();
          return viewRef != null && viewRef.get() == child;
        }

        @Override
        public void onViewPositionChanged(
            @NonNull View changedView, int left, int top, int dx, int dy) {
          dispatchOnSlide(top);
        }

        @Override
        public void onViewDragStateChanged(@State int state) {
          if (state == ViewDragHelper.STATE_DRAGGING && draggable) {
            setStateInternal(STATE_DRAGGING);
          }
        }

        private boolean releasedLow(@NonNull View child) {
          // Needs to be at least half way to the bottom.
          return child.getTop() > (parentHeight + getExpandedOffset()) / 2;
        }

        @Override
        public void onViewReleased(@NonNull View releasedChild, float xvel, float yvel) {
          @State int targetState;
          if (yvel < 0) { // Moving up
            if (fitToContents) {
              targetState = STATE_EXPANDED;
            } else {
              int currentTop = releasedChild.getTop();
              long dragDurationMillis = SystemClock.uptimeMillis() - viewCapturedMillis;

              if (shouldSkipHalfExpandedStateWhenDragging()) {
                float yPositionPercentage = currentTop * 100f / parentHeight;

                if (shouldExpandOnUpwardDrag(dragDurationMillis, yPositionPercentage)) {
                  targetState = STATE_EXPANDED;
                } else {
                  targetState = STATE_COLLAPSED;
                }
              } else {
                if (currentTop > halfExpandedOffset) {
                  targetState = STATE_HALF_EXPANDED;
                } else {
                  targetState = STATE_EXPANDED;
                }
              }
            }
          } else if (hideable && shouldHide(releasedChild, yvel)) {
            // Hide if the view was either released low or it was a significant vertical swipe
            // otherwise settle to closest expanded state.
            if ((Math.abs(xvel) < Math.abs(yvel) && yvel > significantVelocityThreshold)
                || releasedLow(releasedChild)) {
              targetState = STATE_HIDDEN;
            } else if (fitToContents) {
              targetState = STATE_EXPANDED;
            } else if (Math.abs(releasedChild.getTop() - getExpandedOffset())
                < Math.abs(releasedChild.getTop() - halfExpandedOffset)) {
              targetState = STATE_EXPANDED;
            } else {
              targetState = STATE_HALF_EXPANDED;
            }
          } else if (yvel == 0.f || Math.abs(xvel) > Math.abs(yvel)) {
            // If the Y velocity is 0 or the swipe was mostly horizontal indicated by the X velocity
            // being greater than the Y velocity, settle to the nearest correct height.
            int currentTop = releasedChild.getTop();
            if (fitToContents) {
              if (Math.abs(currentTop - fitToContentsOffset)
                  < Math.abs(currentTop - collapsedOffset)) {
                targetState = STATE_EXPANDED;
              } else {
                targetState = STATE_COLLAPSED;
              }
            } else {
              if (currentTop < halfExpandedOffset) {
                if (currentTop < Math.abs(currentTop - collapsedOffset)) {
                  targetState = STATE_EXPANDED;
                } else {
                  if (shouldSkipHalfExpandedStateWhenDragging()) {
                    targetState = STATE_COLLAPSED;
                  } else {
                    targetState = STATE_HALF_EXPANDED;
                  }
                }
              } else {
                if (Math.abs(currentTop - halfExpandedOffset)
                    < Math.abs(currentTop - collapsedOffset)) {
                  if (shouldSkipHalfExpandedStateWhenDragging()) {
                    targetState = STATE_COLLAPSED;
                  } else {
                    targetState = STATE_HALF_EXPANDED;
                  }
                } else {
                  targetState = STATE_COLLAPSED;
                }
              }
            }
          } else { // Moving Down
            if (fitToContents) {
              targetState = STATE_COLLAPSED;
            } else {
              // Settle to the nearest correct height.
              int currentTop = releasedChild.getTop();
              if (Math.abs(currentTop - halfExpandedOffset)
                  < Math.abs(currentTop - collapsedOffset)) {
                if (shouldSkipHalfExpandedStateWhenDragging()) {
                  targetState = STATE_COLLAPSED;
                } else {
                  targetState = STATE_HALF_EXPANDED;
                }
              } else {
                targetState = STATE_COLLAPSED;
              }
            }
          }
          startSettling(releasedChild, targetState, shouldSkipSmoothAnimation());
        }

        @Override
        public int clampViewPositionVertical(@NonNull View child, int top, int dy) {
          return MathUtils.clamp(top, getExpandedOffset(), getViewVerticalDragRange(child));
        }

        @Override
        public int clampViewPositionHorizontal(@NonNull View child, int left, int dx) {
          return child.getLeft();
        }

        @Override
        public int getViewVerticalDragRange(@NonNull View child) {
          if (canBeHiddenByDragging()) {
            return parentHeight;
          } else {
            return collapsedOffset;
          }
        }
      };

  void dispatchOnSlide(int top) {
    View bottomSheet = viewRef.get();
    if (bottomSheet != null && !callbacks.isEmpty()) {
      float slideOffset = calculateSlideOffsetWithTop(top);
      for (int i = 0; i < callbacks.size(); i++) {
        callbacks.get(i).onSlide(bottomSheet, slideOffset);
      }
    }
  }

  @VisibleForTesting
  int getPeekHeightMin() {
    return peekHeightMin;
  }

  /**
   * Disables the shaped corner {@link ShapeAppearanceModel} interpolation transition animations.
   * Will have no effect unless the sheet utilizes a {@link MaterialShapeDrawable} with set shape
   * theming properties. Only For use in UI testing.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @VisibleForTesting
  public void disableShapeAnimations() {
    // Sets the shape value animator to null, prevents animations from occurring during testing.
    interpolatorAnimator = null;
  }

  /**
   * Checks weather a nested scroll should be enabled. If {@code false} all nested scrolls will be
   * consumed by the bottomSheet.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isNestedScrollingCheckEnabled() {
    return true;
  }

  /**
   * Checks weather half expended state should be skipped when drag is ended. If {@code true}, the
   * bottomSheet will go to the next closest state.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean shouldSkipHalfExpandedStateWhenDragging() {
    return false;
  }

  /**
   * Checks whether an animation should be smooth after the bottomSheet is released after dragging.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean shouldSkipSmoothAnimation() {
    return true;
  }

  /**
   * Checks whether hiding gestures should be enabled while {@code isHideable} is set to true.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isHideableWhenDragging() {
    return true;
  }

  private boolean canBeHiddenByDragging() {
    return isHideable() && isHideableWhenDragging();
  }

  /**
   * Checks whether the bottom sheet should be expanded after it has been released after dragging.
   *
   * @param dragDurationMillis how long the bottom sheet was dragged.
   * @param yPositionPercentage position of the bottom sheet when released after dragging. Lower
   *     values mean that view was released closer to the top of the screen.
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean shouldExpandOnUpwardDrag(
      long dragDurationMillis, @FloatRange(from = 0.0f, to = 100.0f) float yPositionPercentage) {
    return false;
  }

  /**
   * Sets whether this bottom sheet can hide when it is swiped down.
   *
   * @param hideable {@code true} to make this bottom sheet hideable.
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void setHideableInternal(boolean hideable) {
    this.hideable = hideable;
  }

  /**
   * Gets the last stable state of the bottom sheet.
   *
   * @return One of {@link #STATE_EXPANDED}, {@link #STATE_HALF_EXPANDED}, {@link #STATE_COLLAPSED},
   *     {@link #STATE_HIDDEN}.
   * @hide
   */
  @State
  @RestrictTo(LIBRARY_GROUP)
  public int getLastStableState() {
    return lastStableState;
  }

  private class StateSettlingTracker {
    @State private int targetState;
    private boolean isContinueSettlingRunnablePosted;

    private final Runnable continueSettlingRunnable =
        new Runnable() {
          @Override
          public void run() {
            isContinueSettlingRunnablePosted = false;
            if (viewDragHelper != null && viewDragHelper.continueSettling(true)) {
              continueSettlingToState(targetState);
            } else if (state == STATE_SETTLING) {
              setStateInternal(targetState);
            }
            // In other cases, settling has been interrupted by certain UX interactions. Do nothing.
          }
        };

    void continueSettlingToState(@State int targetState) {
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
    @State final int state;
    int peekHeight;
    boolean fitToContents;
    boolean hideable;
    boolean skipCollapsed;

    public SavedState(@NonNull Parcel source) {
      this(source, null);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      //noinspection ResourceType
      state = source.readInt();
      peekHeight = source.readInt();
      fitToContents = source.readInt() == 1;
      hideable = source.readInt() == 1;
      skipCollapsed = source.readInt() == 1;
    }

    public SavedState(Parcelable superState, @NonNull BottomSheetBehavior<?> behavior) {
      super(superState);
      this.state = behavior.state;
      this.peekHeight = behavior.peekHeight;
      this.fitToContents = behavior.fitToContents;
      this.hideable = behavior.hideable;
      this.skipCollapsed = behavior.skipCollapsed;
    }

    /**
     * This constructor does not respect flags: {@link BottomSheetBehavior#SAVE_PEEK_HEIGHT}, {@link
     * BottomSheetBehavior#SAVE_FIT_TO_CONTENTS}, {@link BottomSheetBehavior#SAVE_HIDEABLE}, {@link
     * BottomSheetBehavior#SAVE_SKIP_COLLAPSED}. It is as if {@link BottomSheetBehavior#SAVE_NONE}
     * were set.
     *
     * @deprecated Use {@link #SavedState(Parcelable, BottomSheetBehavior)} instead.
     */
    @Deprecated
    public SavedState(Parcelable superstate, @State int state) {
      super(superstate);
      this.state = state;
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeInt(state);
      out.writeInt(peekHeight);
      out.writeInt(fitToContents ? 1 : 0);
      out.writeInt(hideable ? 1 : 0);
      out.writeInt(skipCollapsed ? 1 : 0);
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
   * A utility function to get the {@link BottomSheetBehavior} associated with the {@code view}.
   *
   * @param view The {@link View} with {@link BottomSheetBehavior}.
   * @return The {@link BottomSheetBehavior} associated with the {@code view}.
   */
  @NonNull
  @SuppressWarnings("unchecked")
  public static <V extends View> BottomSheetBehavior<V> from(@NonNull V view) {
    ViewGroup.LayoutParams params = view.getLayoutParams();
    if (!(params instanceof CoordinatorLayout.LayoutParams)) {
      throw new IllegalArgumentException("The view is not a child of CoordinatorLayout");
    }
    CoordinatorLayout.Behavior<?> behavior =
        ((CoordinatorLayout.LayoutParams) params).getBehavior();
    if (!(behavior instanceof BottomSheetBehavior)) {
      throw new IllegalArgumentException("The view is not associated with BottomSheetBehavior");
    }
    return (BottomSheetBehavior<V>) behavior;
  }

  /**
   * Sets whether the BottomSheet should update the accessibility status of its {@link
   * CoordinatorLayout} siblings when expanded.
   *
   * <p>Set this to true if the expanded state of the sheet blocks access to siblings (e.g., when
   * the sheet expands over the full screen).
   */
  public void setUpdateImportantForAccessibilityOnSiblings(
      boolean updateImportantForAccessibilityOnSiblings) {
    this.updateImportantForAccessibilityOnSiblings = updateImportantForAccessibilityOnSiblings;
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
    if (expanded) {
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
        importantForAccessibilityMap.put(child, child.getImportantForAccessibility());
        if (updateImportantForAccessibilityOnSiblings) {
          child.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO_HIDE_DESCENDANTS);
        }
      } else {
        if (updateImportantForAccessibilityOnSiblings
            && importantForAccessibilityMap != null
            && importantForAccessibilityMap.containsKey(child)) {
          // Restores the original important for accessibility value of the child view.
          child.setImportantForAccessibility(importantForAccessibilityMap.get(child));
        }
      }
    }

    if (!expanded) {
      importantForAccessibilityMap = null;
    } else if (updateImportantForAccessibilityOnSiblings) {
      // If the siblings of the bottom sheet have been set to not important for a11y, move the focus
      // to the bottom sheet when expanded.
      viewRef.get().sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_FOCUSED);
    }
  }

  void setDragHandleView(@Nullable BottomSheetDragHandleView dragHandleView) {
    dragHandleViewRef = dragHandleView != null ? new WeakReference<>(dragHandleView) : null;
  }

  void setAccessibilityDelegateView(@Nullable View accessibilityDelegateView) {
    if (accessibilityDelegateView == null && accessibilityDelegateViewRef != null) {
      clearAccessibilityAction(
          accessibilityDelegateViewRef.get(), VIEW_INDEX_ACCESSIBILITY_DELEGATE_VIEW);
      accessibilityDelegateViewRef = null;
      return;
    }
    accessibilityDelegateViewRef = new WeakReference<>(accessibilityDelegateView);
    updateAccessibilityActions(accessibilityDelegateView, VIEW_INDEX_ACCESSIBILITY_DELEGATE_VIEW);
  }

  private void updateAccessibilityActions() {
    if (viewRef != null) {
      updateAccessibilityActions(viewRef.get(), VIEW_INDEX_BOTTOM_SHEET);
    }
    if (accessibilityDelegateViewRef != null) {
      updateAccessibilityActions(
          accessibilityDelegateViewRef.get(), VIEW_INDEX_ACCESSIBILITY_DELEGATE_VIEW);
    }
  }

  private void updateAccessibilityActions(View view, int viewIndex) {
    if (view == null) {
      return;
    }
    clearAccessibilityAction(view, viewIndex);

    if (!fitToContents && state != STATE_HALF_EXPANDED) {
      expandHalfwayActionIds.put(
          viewIndex,
          addAccessibilityActionForState(
              view, R.string.bottomsheet_action_expand_halfway, STATE_HALF_EXPANDED));
    }

    if ((hideable && isHideableWhenDragging()) && state != STATE_HIDDEN) {
      replaceAccessibilityActionForState(
          view, AccessibilityActionCompat.ACTION_DISMISS, STATE_HIDDEN);
    }

    switch (state) {
      case STATE_EXPANDED:
        {
          collapseActionIds.put(
              viewIndex,
              addAccessibilityActionForState(
                  view, R.string.bottomsheet_action_collapse, STATE_COLLAPSED));
          break;
        }
      case STATE_HALF_EXPANDED:
        {
          collapseActionIds.put(
              viewIndex,
              addAccessibilityActionForState(
                  view, R.string.bottomsheet_action_collapse, STATE_COLLAPSED));
          expandActionIds.put(
              viewIndex,
              addAccessibilityActionForState(
                  view, R.string.bottomsheet_action_expand, STATE_EXPANDED));
          break;
        }
      case STATE_COLLAPSED:
        {
          expandActionIds.put(
              viewIndex,
              addAccessibilityActionForState(
                  view, R.string.bottomsheet_action_expand, STATE_EXPANDED));
          break;
        }
      case STATE_HIDDEN:
      case STATE_DRAGGING:
      case STATE_SETTLING:
        // Accessibility actions are not applicable, do nothing
    }
  }

  private void clearAccessibilityAction(View view, int viewIndex) {
    if (view == null) {
      return;
    }
    ViewCompat.removeAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_DISMISS);
    ViewCompat.removeAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_COLLAPSE);
    ViewCompat.removeAccessibilityAction(view, AccessibilityNodeInfoCompat.ACTION_EXPAND);

    int expandActionId = expandActionIds.get(viewIndex, View.NO_ID);
    if (expandActionId != View.NO_ID) {
      ViewCompat.removeAccessibilityAction(view, expandActionId);
      expandActionIds.delete(viewIndex);
    }

    int expandHalfwayActionId = expandHalfwayActionIds.get(viewIndex, View.NO_ID);
    if (expandHalfwayActionId != View.NO_ID) {
      ViewCompat.removeAccessibilityAction(view, expandHalfwayActionId);
      expandHalfwayActionIds.delete(viewIndex);
    }

    int collapseActionId = collapseActionIds.get(viewIndex, View.NO_ID);
    if (collapseActionId != View.NO_ID) {
      ViewCompat.removeAccessibilityAction(view, collapseActionId);
      collapseActionIds.delete(viewIndex);
    }
  }

  private void replaceAccessibilityActionForState(
      View child, AccessibilityActionCompat action, @State int state) {
    ViewCompat.replaceAccessibilityAction(
        child, action, null, createAccessibilityViewCommandForState(state));
  }

  private int addAccessibilityActionForState(
      View child, @StringRes int stringResId, @State int state) {
    return ViewCompat.addAccessibilityAction(
        child,
        child.getResources().getString(stringResId),
        createAccessibilityViewCommandForState(state));
  }

  private AccessibilityViewCommand createAccessibilityViewCommandForState(@State final int state) {
    return new AccessibilityViewCommand() {
      @Override
      public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
        setState(state);
        return true;
      }
    };
  }

  /**
   * Returns a default {@link GradientProtection} for use with BottomSheets.
   *
   * @throws IllegalArgumentException if {@code R.attr.colorSurfaceContainerLow} and {@code
   *     R.attr.colorSurface} are not set in the current theme.
   */
  @NonNull
  public static Protection getDefaultBottomGradientProtection(@NonNull Context context) {
    Integer color = MaterialColors.getColorOrNull(context, R.attr.colorSurfaceContainerLow);
    if (color == null) {
      color =
          MaterialColors.getColor(
              context, R.attr.colorSurface, BottomSheetBehavior.class.getSimpleName());
    }
    return new GradientProtection(Side.BOTTOM, color);
  }
}
