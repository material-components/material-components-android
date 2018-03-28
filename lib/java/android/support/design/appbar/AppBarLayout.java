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

package android.support.design.appbar;

import android.support.design.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.annotation.RestrictTo;
import android.support.annotation.VisibleForTesting;
import android.support.design.animation.AnimationUtils;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.math.MathUtils;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.util.ObjectsCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewCompat.NestedScrollType;
import android.support.v4.view.WindowInsetsCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * AppBarLayout is a vertical {@link LinearLayout} which implements many of the features of material
 * designs app bar concept, namely scrolling gestures.
 *
 * <p>Children should provide their desired scrolling behavior through {@link
 * LayoutParams#setScrollFlags(int)} and the associated layout xml attribute: {@code
 * app:layout_scrollFlags}.
 *
 * <p>This view depends heavily on being used as a direct child within a {@link CoordinatorLayout}.
 * If you use AppBarLayout within a different {@link ViewGroup}, most of it's functionality will not
 * work.
 *
 * <p>AppBarLayout also requires a separate scrolling sibling in order to know when to scroll. The
 * binding is done through the {@link ScrollingViewBehavior} behavior class, meaning that you should
 * set your scrolling view's behavior to be an instance of {@link ScrollingViewBehavior}. A string
 * resource containing the full class name is available.
 *
 * <pre>
 * &lt;android.support.design.widget.CoordinatorLayout
 *         xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *         xmlns:app=&quot;http://schemas.android.com/apk/res-auto&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;android.support.v4.widget.NestedScrollView
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;match_parent&quot;
 *             app:layout_behavior=&quot;@string/appbar_scrolling_view_behavior&quot;&gt;
 *
 *         &lt;!-- Your scrolling content --&gt;
 *
 *     &lt;/android.support.v4.widget.NestedScrollView&gt;
 *
 *     &lt;android.support.design.appbar.AppBarLayout
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:layout_width=&quot;match_parent&quot;&gt;
 *
 *         &lt;android.support.v7.widget.Toolbar
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *         &lt;android.support.design.tabs.TabLayout
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *     &lt;/android.support.design.appbar.AppBarLayout&gt;
 *
 * &lt;/android.support.design.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/layout/structure.html#structure-app-bar">
 *     http://www.google.com/design/spec/layout/structure.html#structure-app-bar</a>
 */
@CoordinatorLayout.DefaultBehavior(AppBarLayout.Behavior.class)
public class AppBarLayout extends LinearLayout {

  static final int PENDING_ACTION_NONE = 0x0;
  static final int PENDING_ACTION_EXPANDED = 0x1;
  static final int PENDING_ACTION_COLLAPSED = 0x2;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 0x4;
  static final int PENDING_ACTION_FORCE = 0x8;

  /**
   * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
   * offset changes.
   */
  //TODO: remove this base interface after the widget migration
  public interface BaseOnOffsetChangedListener<T extends AppBarLayout> {

    /**
     * Called when the {@link AppBarLayout}'s layout offset has been changed. This allows child
     * views to implement custom behavior based on the offset (for instance pinning a view at a
     * certain y value).
     *
     * @param appBarLayout the {@link AppBarLayout} which offset has changed
     * @param verticalOffset the vertical offset for the parent {@link AppBarLayout}, in px
     */
    void onOffsetChanged(T appBarLayout, int verticalOffset);
  }

  /**
   * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
   * offset changes.
   */
  //TODO: update this interface after the widget migration
  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
    void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
  }

  private static final int INVALID_SCROLL_RANGE = -1;

  private int totalScrollRange = INVALID_SCROLL_RANGE;
  private int downPreScrollRange = INVALID_SCROLL_RANGE;
  private int downScrollRange = INVALID_SCROLL_RANGE;

  private boolean haveChildWithInterpolator;

  private int pendingAction = PENDING_ACTION_NONE;

  private WindowInsetsCompat lastInsets;

  private List<BaseOnOffsetChangedListener> listeners;

  private boolean collapsible;
  private boolean collapsed;

  private int[] tmpStatesArray;

  public AppBarLayout(Context context) {
    this(context, null);
  }

  public AppBarLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    setOrientation(VERTICAL);

    if (Build.VERSION.SDK_INT >= 21) {
      // Use the bounds view outline provider so that we cast a shadow, even without a
      // background
      ViewUtilsLollipop.setBoundsViewOutlineProvider(this);

      // If we're running on API 21+, we should reset any state list animator from our
      // default style
      ViewUtilsLollipop.setStateListAnimatorFromAttrs(
          this, attrs, 0, R.style.Widget_Design_AppBarLayout);
    }

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.AppBarLayout, 0, R.style.Widget_Design_AppBarLayout);
    ViewCompat.setBackground(this, a.getDrawable(R.styleable.AppBarLayout_android_background));
    if (a.hasValue(R.styleable.AppBarLayout_expanded)) {
      setExpanded(
          a.getBoolean(R.styleable.AppBarLayout_expanded, false),
          false, /* animate */
          false /* force */);
    }
    if (Build.VERSION.SDK_INT >= 21 && a.hasValue(R.styleable.AppBarLayout_elevation)) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(
          this, a.getDimensionPixelSize(R.styleable.AppBarLayout_elevation, 0));
    }
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      // In O+, we have these values set in the style. Since there is no defStyleAttr for
      // AppBarLayout at the AppCompat level, check for these attributes here.
      if (a.hasValue(R.styleable.AppBarLayout_android_keyboardNavigationCluster)) {
        this.setKeyboardNavigationCluster(
            a.getBoolean(R.styleable.AppBarLayout_android_keyboardNavigationCluster, false));
      }
      if (a.hasValue(R.styleable.AppBarLayout_android_touchscreenBlocksFocus)) {
        this.setTouchscreenBlocksFocus(
            a.getBoolean(R.styleable.AppBarLayout_android_touchscreenBlocksFocus, false));
      }
    }
    a.recycle();

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new android.support.v4.view.OnApplyWindowInsetsListener() {
          @Override
          public WindowInsetsCompat onApplyWindowInsets(View v, WindowInsetsCompat insets) {
            return onWindowInsetChanged(insets);
          }
        });
  }

  /**
   * Add a listener that will be called when the offset of this {@link AppBarLayout} changes.
   *
   * @param listener The listener that will be called when the offset changes.]
   * @see #removeOnOffsetChangedListener(OnOffsetChangedListener)
   */
  public void addOnOffsetChangedListener(BaseOnOffsetChangedListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  /**
   * Remove the previously added {@link OnOffsetChangedListener}.
   *
   * @param listener the listener to remove.
   */
  // TODO: change back to removeOnOffsetChangedListener once the widget migration is
  // finished since the shim class needs to implement this method.
  public void removeOnOffsetChangedListener(BaseOnOffsetChangedListener listener) {
    if (listeners != null && listener != null) {
      listeners.remove(listener);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    invalidateScrollRanges();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);
    invalidateScrollRanges();

    haveChildWithInterpolator = false;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams childLp = (LayoutParams) child.getLayoutParams();
      final Interpolator interpolator = childLp.getScrollInterpolator();

      if (interpolator != null) {
        haveChildWithInterpolator = true;
        break;
      }
    }

    updateCollapsible();
  }

  private void updateCollapsible() {
    boolean haveCollapsibleChild = false;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
        haveCollapsibleChild = true;
        break;
      }
    }
    setCollapsibleState(haveCollapsibleChild);
  }

  private void invalidateScrollRanges() {
    // Invalidate the scroll ranges
    totalScrollRange = INVALID_SCROLL_RANGE;
    downPreScrollRange = INVALID_SCROLL_RANGE;
    downScrollRange = INVALID_SCROLL_RANGE;
  }

  @Override
  public void setOrientation(int orientation) {
    if (orientation != VERTICAL) {
      throw new IllegalArgumentException(
          "AppBarLayout is always vertical and does" + " not support horizontal orientation");
    }
    super.setOrientation(orientation);
  }

  /**
   * Sets whether this {@link AppBarLayout} is expanded or not, animating if it has already been
   * laid out.
   *
   * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
   * child of a {@link CoordinatorLayout}.
   *
   * @param expanded true if the layout should be fully expanded, false if it should be fully
   *     collapsed
   * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
   */
  public void setExpanded(boolean expanded) {
    setExpanded(expanded, ViewCompat.isLaidOut(this));
  }

  /**
   * Sets whether this {@link AppBarLayout} is expanded or not.
   *
   * <p>As with {@link AppBarLayout}'s scrolling, this method relies on this layout being a direct
   * child of a {@link CoordinatorLayout}.
   *
   * @param expanded true if the layout should be fully expanded, false if it should be fully
   *     collapsed
   * @param animate Whether to animate to the new state
   * @attr ref android.support.design.R.styleable#AppBarLayout_expanded
   */
  public void setExpanded(boolean expanded, boolean animate) {
    setExpanded(expanded, animate, true);
  }

  private void setExpanded(boolean expanded, boolean animate, boolean force) {
    pendingAction =
        (expanded ? PENDING_ACTION_EXPANDED : PENDING_ACTION_COLLAPSED)
            | (animate ? PENDING_ACTION_ANIMATE_ENABLED : 0)
            | (force ? PENDING_ACTION_FORCE : 0);
    requestLayout();
  }

  @Override
  protected boolean checkLayoutParams(ViewGroup.LayoutParams p) {
    return p instanceof LayoutParams;
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams p) {
    if (Build.VERSION.SDK_INT >= 19 && p instanceof LinearLayout.LayoutParams) {
      return new LayoutParams((LinearLayout.LayoutParams) p);
    } else if (p instanceof MarginLayoutParams) {
      return new LayoutParams((MarginLayoutParams) p);
    }
    return new LayoutParams(p);
  }

  boolean hasChildWithInterpolator() {
    return haveChildWithInterpolator;
  }

  /**
   * Returns the scroll range of all children.
   *
   * @return the scroll range in px
   */
  public final int getTotalScrollRange() {
    if (totalScrollRange != INVALID_SCROLL_RANGE) {
      return totalScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        // We're set to scroll so add the child's height
        range += childHeight + lp.topMargin + lp.bottomMargin;

        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // For a collapsing scroll, we to take the collapsed height into account.
          // We also break straight away since later views can't scroll beneath
          // us
          range -= ViewCompat.getMinimumHeight(child);
          break;
        }
      } else {
        // As soon as a view doesn't have the scroll flag, we end the range calculation.
        // This is because views below can not scroll under a fixed view.
        break;
      }
    }
    return totalScrollRange = Math.max(0, range - getTopInset());
  }

  boolean hasScrollableChildren() {
    return getTotalScrollRange() != 0;
  }

  /** Return the scroll range when scrolling up from a nested pre-scroll. */
  int getUpNestedPreScrollRange() {
    return getTotalScrollRange();
  }

  /** Return the scroll range when scrolling down from a nested pre-scroll. */
  int getDownNestedPreScrollRange() {
    if (downPreScrollRange != INVALID_SCROLL_RANGE) {
      // If we already have a valid value, return it
      return downPreScrollRange;
    }

    int range = 0;
    for (int i = getChildCount() - 1; i >= 0; i--) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      final int childHeight = child.getMeasuredHeight();
      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.FLAG_QUICK_RETURN) == LayoutParams.FLAG_QUICK_RETURN) {
        // First take the margin into account
        range += lp.topMargin + lp.bottomMargin;
        // The view has the quick return flag combination...
        if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
          // If they're set to enter collapsed, use the minimum height
          range += ViewCompat.getMinimumHeight(child);
        } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // Only enter by the amount of the collapsed height
          range += childHeight - ViewCompat.getMinimumHeight(child);
        } else {
          // Else use the full height (minus the top inset)
          range += childHeight - getTopInset();
        }
      } else if (range > 0) {
        // If we've hit an non-quick return scrollable view, and we've already hit a
        // quick return view, return now
        break;
      }
    }
    return downPreScrollRange = Math.max(0, range);
  }

  /** Return the scroll range when scrolling down from a nested scroll. */
  int getDownNestedScrollRange() {
    if (downScrollRange != INVALID_SCROLL_RANGE) {
      // If we already have a valid value, return it
      return downScrollRange;
    }

    int range = 0;
    for (int i = 0, z = getChildCount(); i < z; i++) {
      final View child = getChildAt(i);
      final LayoutParams lp = (LayoutParams) child.getLayoutParams();
      int childHeight = child.getMeasuredHeight();
      childHeight += lp.topMargin + lp.bottomMargin;

      final int flags = lp.scrollFlags;

      if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
        // We're set to scroll so add the child's height
        range += childHeight;

        if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // For a collapsing exit scroll, we to take the collapsed height into account.
          // We also break the range straight away since later views can't scroll
          // beneath us
          range -= ViewCompat.getMinimumHeight(child) + getTopInset();
          break;
        }
      } else {
        // As soon as a view doesn't have the scroll flag, we end the range calculation.
        // This is because views below can not scroll under a fixed view.
        break;
      }
    }
    return downScrollRange = Math.max(0, range);
  }

  void dispatchOffsetUpdates(int offset) {
    // Iterate backwards through the list so that most recently added listeners
    // get the first chance to decide
    if (listeners != null) {
      for (int i = 0, z = listeners.size(); i < z; i++) {
        final BaseOnOffsetChangedListener listener = listeners.get(i);
        if (listener != null) {
          listener.onOffsetChanged(this, offset);
        }
      }
    }
  }

  public final int getMinimumHeightForVisibleOverlappingContent() {
    final int topInset = getTopInset();
    final int minHeight = ViewCompat.getMinimumHeight(this);
    if (minHeight != 0) {
      // If this layout has a min height, use it (doubled)
      return (minHeight * 2) + topInset;
    }

    // Otherwise, we'll use twice the min height of our last child
    final int childCount = getChildCount();
    final int lastChildMinHeight =
        childCount >= 1 ? ViewCompat.getMinimumHeight(getChildAt(childCount - 1)) : 0;
    if (lastChildMinHeight != 0) {
      return (lastChildMinHeight * 2) + topInset;
    }

    // If we reach here then we don't have a min height explicitly set. Instead we'll take a
    // guess at 1/3 of our height being visible
    return getHeight() / 3;
  }

  @Override
  protected int[] onCreateDrawableState(int extraSpace) {
    if (tmpStatesArray == null) {
      // Note that we can't allocate this at the class level (in declaration) since
      // some paths in super View constructor are going to call this method before
      // that
      tmpStatesArray = new int[2];
    }
    final int[] extraStates = tmpStatesArray;
    final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

    extraStates[0] = collapsible ? R.attr.state_collapsible : -R.attr.state_collapsible;
    extraStates[1] = collapsible && collapsed ? R.attr.state_collapsed : -R.attr.state_collapsed;

    return mergeDrawableStates(states, extraStates);
  }

  /**
   * Sets whether the AppBarLayout has collapsible children or not.
   *
   * @return true if the collapsible state changed
   */
  private boolean setCollapsibleState(boolean collapsible) {
    if (this.collapsible != collapsible) {
      this.collapsible = collapsible;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  /**
   * Sets whether the AppBarLayout is in a collapsed state or not.
   *
   * @return true if the collapsed state changed
   */
  boolean setCollapsedState(boolean collapsed) {
    if (this.collapsed != collapsed) {
      this.collapsed = collapsed;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  /**
   * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
   *     a {@link android.animation.StateListAnimator}. If a target elevation is set, either by this
   *     method or the {@code app:elevation} attribute, a new state list animator is created which
   *     uses the given {@code elevation} value.
   * @attr ref android.support.design.R.styleable#AppBarLayout_elevation
   */
  @Deprecated
  public void setTargetElevation(float elevation) {
    if (Build.VERSION.SDK_INT >= 21) {
      ViewUtilsLollipop.setDefaultAppBarLayoutStateListAnimator(this, elevation);
    }
  }

  /**
   * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
   *     a {@link android.animation.StateListAnimator}. This method now always returns 0.
   */
  @Deprecated
  public float getTargetElevation() {
    return 0;
  }

  int getPendingAction() {
    return pendingAction;
  }

  void resetPendingAction() {
    pendingAction = PENDING_ACTION_NONE;
  }

  @VisibleForTesting
  final int getTopInset() {
    return lastInsets != null ? lastInsets.getSystemWindowInsetTop() : 0;
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;
    }

    // If our insets have changed, keep them and invalidate the scroll ranges...
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      invalidateScrollRanges();
    }

    return insets;
  }

  /** TODO: Add a real comment here after the widget migration */
  public static class LayoutParams extends LinearLayout.LayoutParams {

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
      flag = true,
      value = {
        SCROLL_FLAG_SCROLL,
        SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
        SCROLL_FLAG_ENTER_ALWAYS,
        SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
        SCROLL_FLAG_SNAP,
        SCROLL_FLAG_SNAP_MARGINS,
      }
    )
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {}

    /**
     * The view will be scroll in direct relation to scroll events. This flag needs to be set for
     * any of the other flags to take effect. If any sibling views before this one do not have this
     * flag, then this value has no effect.
     */
    public static final int SCROLL_FLAG_SCROLL = 0x1;

    /**
     * When exiting (scrolling off screen) the view will be scrolled until it is 'collapsed'. The
     * collapsed height is defined by the view's minimum height.
     *
     * @see ViewCompat#getMinimumHeight(View)
     * @see View#setMinimumHeight(int)
     */
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 0x2;

    /**
     * When entering (scrolling on screen) the view will scroll on any downwards scroll event,
     * regardless of whether the scrolling view is also scrolling. This is commonly referred to as
     * the 'quick return' pattern.
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 0x4;

    /**
     * An additional flag for 'enterAlways' which modifies the returning view to only initially
     * scroll back to it's collapsed height. Once the scrolling view has reached the end of it's
     * scroll range, the remainder of this view will be scrolled into view. The collapsed height is
     * defined by the view's minimum height.
     *
     * @see ViewCompat#getMinimumHeight(View)
     * @see View#setMinimumHeight(int)
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 0x8;

    /**
     * Upon a scroll ending, if the view is only partially visible then it will be snapped and
     * scrolled to it's closest edge. For example, if the view only has it's bottom 25% displayed,
     * it will be scrolled off screen completely. Conversely, if it's bottom 75% is visible then it
     * will be scrolled fully into view.
     */
    public static final int SCROLL_FLAG_SNAP = 0x10;

    /**
     * An additional flag to be used with 'snap'. If set, the view will be snapped to its top and
     * bottom margins, as opposed to the edges of the view itself.
     */
    public static final int SCROLL_FLAG_SNAP_MARGINS = 0x20;

    /** Internal flags which allows quick checking features */
    static final int FLAG_QUICK_RETURN = SCROLL_FLAG_SCROLL | SCROLL_FLAG_ENTER_ALWAYS;

    static final int FLAG_SNAP = SCROLL_FLAG_SCROLL | SCROLL_FLAG_SNAP;
    static final int COLLAPSIBLE_FLAGS =
        SCROLL_FLAG_EXIT_UNTIL_COLLAPSED | SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED;

    int scrollFlags = SCROLL_FLAG_SCROLL;
    Interpolator scrollInterpolator;

    public LayoutParams(Context c, AttributeSet attrs) {
      super(c, attrs);
      TypedArray a = c.obtainStyledAttributes(attrs, R.styleable.AppBarLayout_Layout);
      scrollFlags = a.getInt(R.styleable.AppBarLayout_Layout_layout_scrollFlags, 0);
      if (a.hasValue(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator)) {
        int resId = a.getResourceId(R.styleable.AppBarLayout_Layout_layout_scrollInterpolator, 0);
        scrollInterpolator = android.view.animation.AnimationUtils.loadInterpolator(c, resId);
      }
      a.recycle();
    }

    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, float weight) {
      super(width, height, weight);
    }

    public LayoutParams(ViewGroup.LayoutParams p) {
      super(p);
    }

    public LayoutParams(MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LinearLayout.LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
    }

    @RequiresApi(19)
    public LayoutParams(LayoutParams source) {
      // The copy constructor called here only exists on API 19+.
      super(source);
      scrollFlags = source.scrollFlags;
      scrollInterpolator = source.scrollInterpolator;
    }

    /**
     * Set the scrolling flags.
     *
     * @param flags bitwise int of {@link #SCROLL_FLAG_SCROLL}, {@link
     *     #SCROLL_FLAG_EXIT_UNTIL_COLLAPSED}, {@link #SCROLL_FLAG_ENTER_ALWAYS}, {@link
     *     #SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED}, {@link #SCROLL_FLAG_SNAP}, and {@link
     *     #SCROLL_FLAG_SNAP_MARGINS}.
     * @see #getScrollFlags()
     * @attr ref android.support.design.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    public void setScrollFlags(@ScrollFlags int flags) {
      scrollFlags = flags;
    }

    /**
     * Returns the scrolling flags.
     *
     * @see #setScrollFlags(int)
     * @attr ref android.support.design.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    @ScrollFlags
    public int getScrollFlags() {
      return scrollFlags;
    }

    /**
     * Set the interpolator to when scrolling the view associated with this {@link LayoutParams}.
     *
     * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
     * @attr ref android.support.design.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
     * @see #getScrollInterpolator()
     */
    public void setScrollInterpolator(Interpolator interpolator) {
      scrollInterpolator = interpolator;
    }

    /**
     * Returns the {@link Interpolator} being used for scrolling the view associated with this
     * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
     *
     * @attr ref android.support.design.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
     * @see #setScrollInterpolator(Interpolator)
     */
    public Interpolator getScrollInterpolator() {
      return scrollInterpolator;
    }

    /** Returns true if the scroll flags are compatible for 'collapsing' */
    boolean isCollapsible() {
      return (scrollFlags & SCROLL_FLAG_SCROLL) == SCROLL_FLAG_SCROLL
          && (scrollFlags & COLLAPSIBLE_FLAGS) != 0;
    }
  }

  /**
   * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested scroll
   * handling with offsetting.
   */
  //TODO: remove this generic type after the widget migration is done
  public static class Behavior<T extends AppBarLayout> extends HeaderBehavior<T> {
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600; // ms
    private static final int INVALID_POSITION = -1;

    /** Callback to allow control over any {@link AppBarLayout} dragging. */
    public abstract static class DragCallback {
      /**
       * Allows control over whether the given {@link AppBarLayout} can be dragged or not.
       *
       * <p>Dragging is defined as a direct touch on the AppBarLayout with movement. This call does
       * not affect any nested scrolling.
       *
       * @return true if we are in a position to scroll the AppBarLayout via a drag, false if not.
       */
      public abstract boolean canDrag(@NonNull AppBarLayout appBarLayout);
    }

    private int offsetDelta;

    @NestedScrollType private int lastStartedType;

    private ValueAnimator offsetAnimator;

    private int offsetToChildIndexOnLayout = INVALID_POSITION;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;

    private WeakReference<View> lastNestedScrollingChildRef;
    private DragCallback onDragCallback;

    public Behavior() {}

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(
        CoordinatorLayout parent,
        AppBarLayout child,
        View directTargetChild,
        View target,
        int nestedScrollAxes,
        int type) {
      // Return true if we're nested scrolling vertically, and we have scrollable children
      // and the scrolling view is big enough to scroll
      final boolean started =
          (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
              && child.hasScrollableChildren()
              && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();

      if (started && offsetAnimator != null) {
        // Cancel any offset animation
        offsetAnimator.cancel();
      }

      // A new nested scroll has started so clear out the previous ref
      lastNestedScrollingChildRef = null;

      // Track the last started type so we know if a fling is about to happen once scrolling ends
      lastStartedType = type;

      return started;
    }

    @Override
    public void onNestedPreScroll(
        CoordinatorLayout coordinatorLayout,
        T child,
        View target,
        int dx,
        int dy,
        int[] consumed,
        int type) {
      if (dy != 0) {
        int min;
        int max;
        if (dy < 0) {
          // We're scrolling down
          min = -child.getTotalScrollRange();
          max = min + child.getDownNestedPreScrollRange();
        } else {
          // We're scrolling up
          min = -child.getUpNestedPreScrollRange();
          max = 0;
        }
        if (min != max) {
          consumed[1] = scroll(coordinatorLayout, child, dy, min, max);
          stopNestedScrollIfNeeded(dy, child, target, type);
        }
      }
    }

    @Override
    public void onNestedScroll(
        CoordinatorLayout coordinatorLayout,
        T child,
        View target,
        int dxConsumed,
        int dyConsumed,
        int dxUnconsumed,
        int dyUnconsumed,
        int type) {
      if (dyUnconsumed < 0) {
        // If the scrolling view is scrolling down but not consuming, it's probably be at
        // the top of it's content
        scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
        stopNestedScrollIfNeeded(dyUnconsumed, child, target, type);
      }
    }

    private void stopNestedScrollIfNeeded(int dy, T child, View target, int type) {
      if (type == ViewCompat.TYPE_NON_TOUCH) {
        final int curOffset = getTopBottomOffsetForScrollingSibling();
        if ((dy < 0 && curOffset == 0)
            || (dy > 0 && curOffset == -child.getDownNestedScrollRange())) {
          ViewCompat.stopNestedScroll(target, ViewCompat.TYPE_NON_TOUCH);
        }
      }
    }

    @Override
    public void onStopNestedScroll(
        CoordinatorLayout coordinatorLayout, T abl, View target, int type) {
      // onStartNestedScroll for a fling will happen before onStopNestedScroll for the scroll. This
      // isn't necessarily guaranteed yet, but it should be in the future. We use this to our
      // advantage to check if a fling (ViewCompat.TYPE_NON_TOUCH) will start after the touch scroll
      // (ViewCompat.TYPE_TOUCH) ends
      if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        // If we haven't been flung, or a fling is ending
        snapToChildIfNeeded(coordinatorLayout, abl);
      }

      // Keep a reference to the previous nested scrolling child
      lastNestedScrollingChildRef = new WeakReference<>(target);
    }

    /**
     * Set a callback to control any {@link AppBarLayout} dragging.
     *
     * @param callback the callback to use, or {@code null} to use the default behavior.
     */
    // TODO: change back to setDragCallback once the widget migration is done
    // This needs to be changed since some callers pass null, and the overriding wrapper class
    // declares its own version of this method.
    public void setDragCallbackInternal(@Nullable DragCallback callback) {
      onDragCallback = callback;
    }

    private void animateOffsetTo(
        final CoordinatorLayout coordinatorLayout,
        final T child,
        final int offset,
        float velocity) {
      final int distance = Math.abs(getTopBottomOffsetForScrollingSibling() - offset);

      final int duration;
      velocity = Math.abs(velocity);
      if (velocity > 0) {
        duration = 3 * Math.round(1000 * (distance / velocity));
      } else {
        final float distanceRatio = (float) distance / child.getHeight();
        duration = (int) ((distanceRatio + 1) * 150);
      }

      animateOffsetWithDuration(coordinatorLayout, child, offset, duration);
    }

    private void animateOffsetWithDuration(
        final CoordinatorLayout coordinatorLayout,
        final T child,
        final int offset,
        final int duration) {
      final int currentOffset = getTopBottomOffsetForScrollingSibling();
      if (currentOffset == offset) {
        if (offsetAnimator != null && offsetAnimator.isRunning()) {
          offsetAnimator.cancel();
        }
        return;
      }

      if (offsetAnimator == null) {
        offsetAnimator = new ValueAnimator();
        offsetAnimator.setInterpolator(AnimationUtils.DECELERATE_INTERPOLATOR);
        offsetAnimator.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
              @Override
              public void onAnimationUpdate(ValueAnimator animator) {
                setHeaderTopBottomOffset(
                    coordinatorLayout, child, (int) animator.getAnimatedValue());
              }
            });
      } else {
        offsetAnimator.cancel();
      }

      offsetAnimator.setDuration(Math.min(duration, MAX_OFFSET_ANIMATION_DURATION));
      offsetAnimator.setIntValues(currentOffset, offset);
      offsetAnimator.start();
    }

    private int getChildIndexOnOffset(AppBarLayout abl, final int offset) {
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        int top = child.getTop();
        int bottom = child.getBottom();

        final LayoutParams lp = (LayoutParams) child.getLayoutParams();
        if (checkFlag(lp.getScrollFlags(), LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
          // Update top and bottom to include margins
          top -= lp.topMargin;
          bottom += lp.bottomMargin;
        }

        if (top <= -offset && bottom >= -offset) {
          return i;
        }
      }
      return -1;
    }

    private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, T abl) {
      final int offset = getTopBottomOffsetForScrollingSibling();
      final int offsetChildIndex = getChildIndexOnOffset(abl, offset);
      if (offsetChildIndex >= 0) {
        final View offsetChild = abl.getChildAt(offsetChildIndex);
        final LayoutParams lp = (LayoutParams) offsetChild.getLayoutParams();
        final int flags = lp.getScrollFlags();

        if ((flags & LayoutParams.FLAG_SNAP) == LayoutParams.FLAG_SNAP) {
          // We're set the snap, so animate the offset to the nearest edge
          int snapTop = -offsetChild.getTop();
          int snapBottom = -offsetChild.getBottom();

          if (offsetChildIndex == abl.getChildCount() - 1) {
            // If this is the last child, we need to take the top inset into account
            snapBottom += abl.getTopInset();
          }

          if (checkFlag(flags, LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED)) {
            // If the view is set only exit until it is collapsed, we'll abide by that
            snapBottom += ViewCompat.getMinimumHeight(offsetChild);
          } else if (checkFlag(
              flags, LayoutParams.FLAG_QUICK_RETURN | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS)) {
            // If it's set to always enter collapsed, it actually has two states. We
            // select the state and then snap within the state
            final int seam = snapBottom + ViewCompat.getMinimumHeight(offsetChild);
            if (offset < seam) {
              snapTop = seam;
            } else {
              snapBottom = seam;
            }
          }

          if (checkFlag(flags, LayoutParams.SCROLL_FLAG_SNAP_MARGINS)) {
            // Update snap destinations to include margins
            snapTop += lp.topMargin;
            snapBottom -= lp.bottomMargin;
          }

          final int newOffset = offset < (snapBottom + snapTop) / 2 ? snapBottom : snapTop;
          animateOffsetTo(
              coordinatorLayout,
              abl,
              MathUtils.constrain(newOffset, -abl.getTotalScrollRange(), 0),
              0);
        }
      }
    }

    private static boolean checkFlag(final int flags, final int check) {
      return (flags & check) == check;
    }

    @Override
    public boolean onMeasureChild(
        CoordinatorLayout parent,
        T child,
        int parentWidthMeasureSpec,
        int widthUsed,
        int parentHeightMeasureSpec,
        int heightUsed) {
      final CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) child.getLayoutParams();
      if (lp.height == CoordinatorLayout.LayoutParams.WRAP_CONTENT) {
        // If the view is set to wrap on it's height, CoordinatorLayout by default will
        // cap the view at the CoL's height. Since the AppBarLayout can scroll, this isn't
        // what we actually want, so we measure it ourselves with an unspecified spec to
        // allow the child to be larger than it's parent
        parent.onMeasureChild(
            child,
            parentWidthMeasureSpec,
            widthUsed,
            MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED),
            heightUsed);
        return true;
      }

      // Let the parent handle it as normal
      return super.onMeasureChild(
          parent, child, parentWidthMeasureSpec, widthUsed, parentHeightMeasureSpec, heightUsed);
    }

    @Override
    public boolean onLayoutChild(CoordinatorLayout parent, T abl, int layoutDirection) {
      boolean handled = super.onLayoutChild(parent, abl, layoutDirection);

      // The priority for actions here is (first which is true wins):
      // 1. forced pending actions
      // 2. offsets for restorations
      // 3. non-forced pending actions
      final int pendingAction = abl.getPendingAction();
      if (offsetToChildIndexOnLayout >= 0 && (pendingAction & PENDING_ACTION_FORCE) == 0) {
        View child = abl.getChildAt(offsetToChildIndexOnLayout);
        int offset = -child.getBottom();
        if (offsetToChildIndexOnLayoutIsMinHeight) {
          offset += ViewCompat.getMinimumHeight(child) + abl.getTopInset();
        } else {
          offset += Math.round(child.getHeight() * offsetToChildIndexOnLayoutPerc);
        }
        setHeaderTopBottomOffset(parent, abl, offset);
      } else if (pendingAction != PENDING_ACTION_NONE) {
        final boolean animate = (pendingAction & PENDING_ACTION_ANIMATE_ENABLED) != 0;
        if ((pendingAction & PENDING_ACTION_COLLAPSED) != 0) {
          final int offset = -abl.getUpNestedPreScrollRange();
          if (animate) {
            animateOffsetTo(parent, abl, offset, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, offset);
          }
        } else if ((pendingAction & PENDING_ACTION_EXPANDED) != 0) {
          if (animate) {
            animateOffsetTo(parent, abl, 0, 0);
          } else {
            setHeaderTopBottomOffset(parent, abl, 0);
          }
        }
      }

      // Finally reset any pending states
      abl.resetPendingAction();
      offsetToChildIndexOnLayout = INVALID_POSITION;

      // We may have changed size, so let's constrain the top and bottom offset correctly,
      // just in case we're out of the bounds
      setTopAndBottomOffset(
          MathUtils.constrain(getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));

      // Update the AppBarLayout's drawable state for any elevation changes. This is needed so that
      // the elevation is set in the first layout, so that we don't get a visual jump pre-N (due to
      // the draw dispatch skip)
      updateAppBarLayoutDrawableState(
          parent, abl, getTopAndBottomOffset(), 0 /* direction */, true /* forceJump */);

      // Make sure we dispatch the offset update
      abl.dispatchOffsetUpdates(getTopAndBottomOffset());

      return handled;
    }

    @Override
    boolean canDragView(AppBarLayout view) {
      if (onDragCallback != null) {
        // If there is a drag callback set, it's in control
        return onDragCallback.canDrag(view);
      }

      // Else we'll use the default behaviour of seeing if it can scroll down
      if (lastNestedScrollingChildRef != null) {
        // If we have a reference to a scrolling view, check it
        final View scrollingView = lastNestedScrollingChildRef.get();
        return scrollingView != null
            && scrollingView.isShown()
            && !scrollingView.canScrollVertically(-1);
      } else {
        // Otherwise we assume that the scrolling view hasn't been scrolled and can drag.
        return true;
      }
    }

    @Override
    void onFlingFinished(CoordinatorLayout parent, T layout) {
      // At the end of a manual fling, check to see if we need to snap to the edge-child
      snapToChildIfNeeded(parent, layout);
    }

    @Override
    int getMaxDragOffset(AppBarLayout view) {
      return -view.getDownNestedScrollRange();
    }

    @Override
    int getScrollRangeForDragFling(AppBarLayout view) {
      return view.getTotalScrollRange();
    }

    @Override
    int setHeaderTopBottomOffset(
        CoordinatorLayout coordinatorLayout,
        AppBarLayout appBarLayout,
        int newOffset,
        int minOffset,
        int maxOffset) {
      final int curOffset = getTopBottomOffsetForScrollingSibling();
      int consumed = 0;

      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        // If we have some scrolling range, and we're currently within the min and max
        // offsets, calculate a new offset
        newOffset = MathUtils.constrain(newOffset, minOffset, maxOffset);
        if (curOffset != newOffset) {
          final int interpolatedOffset =
              appBarLayout.hasChildWithInterpolator()
                  ? interpolateOffset(appBarLayout, newOffset)
                  : newOffset;

          final boolean offsetChanged = setTopAndBottomOffset(interpolatedOffset);

          // Update how much dy we have consumed
          consumed = curOffset - newOffset;
          // Update the stored sibling offset
          offsetDelta = newOffset - interpolatedOffset;

          if (!offsetChanged && appBarLayout.hasChildWithInterpolator()) {
            // If the offset hasn't changed and we're using an interpolated scroll
            // then we need to keep any dependent views updated. CoL will do this for
            // us when we move, but we need to do it manually when we don't (as an
            // interpolated scroll may finish early).
            coordinatorLayout.dispatchDependentViewsChanged(appBarLayout);
          }

          // Dispatch the updates to any listeners
          appBarLayout.dispatchOffsetUpdates(getTopAndBottomOffset());

          // Update the AppBarLayout's drawable state (for any elevation changes)
          updateAppBarLayoutDrawableState(
              coordinatorLayout,
              appBarLayout,
              newOffset,
              newOffset < curOffset ? -1 : 1,
              false /* forceJump */);
        }
      } else {
        // Reset the offset delta
        offsetDelta = 0;
      }

      return consumed;
    }

    @VisibleForTesting
    boolean isOffsetAnimatorRunning() {
      return offsetAnimator != null && offsetAnimator.isRunning();
    }

    private int interpolateOffset(AppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);

      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final Interpolator interpolator = childLp.getScrollInterpolator();

        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          if (interpolator != null) {
            int childScrollableHeight = 0;
            final int flags = childLp.getScrollFlags();
            if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
              // We're set to scroll so add the child's height plus margin
              childScrollableHeight += child.getHeight() + childLp.topMargin + childLp.bottomMargin;

              if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
                // For a collapsing scroll, we to take the collapsed height
                // into account.
                childScrollableHeight -= ViewCompat.getMinimumHeight(child);
              }
            }

            if (ViewCompat.getFitsSystemWindows(child)) {
              childScrollableHeight -= layout.getTopInset();
            }

            if (childScrollableHeight > 0) {
              final int offsetForView = absOffset - child.getTop();
              final int interpolatedDiff =
                  Math.round(
                      childScrollableHeight
                          * interpolator.getInterpolation(
                              offsetForView / (float) childScrollableHeight));

              return Integer.signum(offset) * (child.getTop() + interpolatedDiff);
            }
          }

          // If we get to here then the view on the offset isn't suitable for interpolated
          // scrolling. So break out of the loop
          break;
        }
      }

      return offset;
    }

    private void updateAppBarLayoutDrawableState(
        final CoordinatorLayout parent,
        final AppBarLayout layout,
        final int offset,
        final int direction,
        final boolean forceJump) {
      final View child = getAppBarChildOnOffset(layout, offset);
      if (child != null) {
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final int flags = childLp.getScrollFlags();
        boolean collapsed = false;

        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
          final int minHeight = ViewCompat.getMinimumHeight(child);

          if (direction > 0
              && (flags
                      & (LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                          | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED))
                  != 0) {
            // We're set to enter always collapsed so we are only collapsed when
            // being scrolled down, and in a collapsed offset
            collapsed = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
            // We're set to exit until collapsed, so any offset which results in
            // the minimum height (or less) being shown is collapsed
            collapsed = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          }
        }

        final boolean changed = layout.setCollapsedState(collapsed);

        if (Build.VERSION.SDK_INT >= 11
            && (forceJump || (changed && shouldJumpElevationState(parent, layout)))) {
          // If the collapsed state changed, we may need to
          // jump to the current state if we have an overlapping view
          layout.jumpDrawablesToCurrentState();
        }
      }
    }

    private boolean shouldJumpElevationState(CoordinatorLayout parent, AppBarLayout layout) {
      // We should jump the elevated state if we have a dependent scrolling view which has
      // an overlapping top (i.e. overlaps us)
      final List<View> dependencies = parent.getDependents(layout);
      for (int i = 0, size = dependencies.size(); i < size; i++) {
        final View dependency = dependencies.get(i);
        final CoordinatorLayout.LayoutParams lp =
            (CoordinatorLayout.LayoutParams) dependency.getLayoutParams();
        final CoordinatorLayout.Behavior behavior = lp.getBehavior();

        if (behavior instanceof ScrollingViewBehavior) {
          return ((ScrollingViewBehavior) behavior).getOverlayTop() != 0;
        }
      }
      return false;
    }

    private static View getAppBarChildOnOffset(final AppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);
      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          return child;
        }
      }
      return null;
    }

    @Override
    int getTopBottomOffsetForScrollingSibling() {
      return getTopAndBottomOffset() + offsetDelta;
    }

    @Override
    public Parcelable onSaveInstanceState(CoordinatorLayout parent, T abl) {
      final Parcelable superState = super.onSaveInstanceState(parent, abl);
      final int offset = getTopAndBottomOffset();

      // Try and find the first visible child...
      for (int i = 0, count = abl.getChildCount(); i < count; i++) {
        View child = abl.getChildAt(i);
        final int visBottom = child.getBottom() + offset;

        if (child.getTop() + offset <= 0 && visBottom >= 0) {
          final SavedState ss = new SavedState(superState);
          ss.firstVisibleChildIndex = i;
          ss.firstVisibleChildAtMinimumHeight =
              visBottom == (ViewCompat.getMinimumHeight(child) + abl.getTopInset());
          ss.firstVisibleChildPercentageShown = visBottom / (float) child.getHeight();
          return ss;
        }
      }

      // Else we'll just return the super state
      return superState;
    }

    @Override
    public void onRestoreInstanceState(
        CoordinatorLayout parent, T appBarLayout, Parcelable state) {
      if (state instanceof SavedState) {
        final SavedState ss = (SavedState) state;
        super.onRestoreInstanceState(parent, appBarLayout, ss.getSuperState());
        offsetToChildIndexOnLayout = ss.firstVisibleChildIndex;
        offsetToChildIndexOnLayoutPerc = ss.firstVisibleChildPercentageShown;
        offsetToChildIndexOnLayoutIsMinHeight = ss.firstVisibleChildAtMinimumHeight;
      } else {
        super.onRestoreInstanceState(parent, appBarLayout, state);
        offsetToChildIndexOnLayout = INVALID_POSITION;
      }
    }

    /** TODO: Add a real comment here after the widget migration */
    protected static class SavedState extends AbsSavedState {
      int firstVisibleChildIndex;
      float firstVisibleChildPercentageShown;
      boolean firstVisibleChildAtMinimumHeight;

      public SavedState(Parcel source, ClassLoader loader) {
        super(source, loader);
        firstVisibleChildIndex = source.readInt();
        firstVisibleChildPercentageShown = source.readFloat();
        firstVisibleChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      @Override
      public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(firstVisibleChildIndex);
        dest.writeFloat(firstVisibleChildPercentageShown);
        dest.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
      }

      public static final Creator<SavedState> CREATOR =
          new ClassLoaderCreator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel source, ClassLoader loader) {
              return new SavedState(source, loader);
            }

            @Override
            public SavedState createFromParcel(Parcel source) {
              return new SavedState(source, null);
            }

            @Override
            public SavedState[] newArray(int size) {
              return new SavedState[size];
            }
          };
    }
  }

  /**
   * Behavior which should be used by {@link View}s which can scroll vertically and support nested
   * scrolling to automatically scroll any {@link AppBarLayout} siblings.
   */
  public static class ScrollingViewBehavior extends HeaderScrollingViewBehavior {

    public ScrollingViewBehavior() {}

    public ScrollingViewBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);

      final TypedArray a =
          context.obtainStyledAttributes(attrs, R.styleable.ScrollingViewBehavior_Layout);
      setOverlayTop(
          a.getDimensionPixelSize(R.styleable.ScrollingViewBehavior_Layout_behavior_overlapTop, 0));
      a.recycle();
    }

    @Override
    public boolean layoutDependsOn(CoordinatorLayout parent, View child, View dependency) {
      // We depend on any AppBarLayouts
      return dependency instanceof AppBarLayout;
    }

    @Override
    public boolean onDependentViewChanged(CoordinatorLayout parent, View child, View dependency) {
      offsetChildAsNeeded(child, dependency);
      return false;
    }

    @Override
    public boolean onRequestChildRectangleOnScreen(
        CoordinatorLayout parent, View child, Rect rectangle, boolean immediate) {
      final AppBarLayout header = findFirstDependency(parent.getDependencies(child));
      if (header != null) {
        // Offset the rect by the child's left/top
        rectangle.offset(child.getLeft(), child.getTop());

        final Rect parentRect = tempRect1;
        parentRect.set(0, 0, parent.getWidth(), parent.getHeight());

        if (!parentRect.contains(rectangle)) {
          // If the rectangle can not be fully seen the visible bounds, collapse
          // the AppBarLayout
          header.setExpanded(false, !immediate);
          return true;
        }
      }
      return false;
    }

    private void offsetChildAsNeeded(View child, View dependency) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
      if (behavior instanceof Behavior) {
        // Offset the child, pinning it to the bottom the header-dependency, maintaining
        // any vertical gap and overlap
        final Behavior ablBehavior = (Behavior) behavior;
        ViewCompat.offsetTopAndBottom(
            child,
            (dependency.getBottom() - child.getTop())
                + ablBehavior.offsetDelta
                + getVerticalLayoutGap()
                - getOverlapPixelsForOffset(dependency));
      }
    }

    @Override
    float getOverlapRatioForOffset(final View header) {
      if (header instanceof AppBarLayout) {
        final AppBarLayout abl = (AppBarLayout) header;
        final int totalScrollRange = abl.getTotalScrollRange();
        final int preScrollDown = abl.getDownNestedPreScrollRange();
        final int offset = getAppBarLayoutOffset(abl);

        if (preScrollDown != 0 && (totalScrollRange + offset) <= preScrollDown) {
          // If we're in a pre-scroll down. Don't use the offset at all.
          return 0;
        } else {
          final int availScrollRange = totalScrollRange - preScrollDown;
          if (availScrollRange != 0) {
            // Else we'll use a interpolated ratio of the overlap, depending on offset
            return 1f + (offset / (float) availScrollRange);
          }
        }
      }
      return 0f;
    }

    private static int getAppBarLayoutOffset(AppBarLayout abl) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
      if (behavior instanceof Behavior) {
        return ((Behavior) behavior).getTopBottomOffsetForScrollingSibling();
      }
      return 0;
    }

    @Override
    AppBarLayout findFirstDependency(List<View> views) {
      for (int i = 0, z = views.size(); i < z; i++) {
        View view = views.get(i);
        if (view instanceof AppBarLayout) {
          return (AppBarLayout) view;
        }
      }
      return null;
    }

    @Override
    int getScrollRange(View v) {
      if (v instanceof AppBarLayout) {
        return ((AppBarLayout) v).getTotalScrollRange();
      } else {
        return super.getScrollRange(v);
      }
    }
  }
}
