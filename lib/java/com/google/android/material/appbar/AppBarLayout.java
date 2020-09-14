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

import com.google.android.material.R;

import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_BACKWARD;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat.ACTION_SCROLL_FORWARD;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.animation.ValueAnimator;
import android.animation.ValueAnimator.AnimatorUpdateListener;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Build.VERSION_CODES;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.util.ObjectsCompat;
import androidx.core.view.NestedScrollingChild;
import androidx.core.view.ViewCompat;
import androidx.core.view.ViewCompat.NestedScrollType;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityViewCommand;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Interpolator;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;
import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.VisibleForTesting;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.math.MathUtils;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
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
 * If you use AppBarLayout within a different {@link ViewGroup}, most of its functionality will not
 * work.
 *
 * <p>AppBarLayout also requires a separate scrolling sibling in order to know when to scroll. The
 * binding is done through the {@link ScrollingViewBehavior} behavior class, meaning that you should
 * set your scrolling view's behavior to be an instance of {@link ScrollingViewBehavior}. A string
 * resource containing the full class name is available.
 *
 * <pre>
 * &lt;androidx.coordinatorlayout.widget.CoordinatorLayout
 *         xmlns:android=&quot;http://schemas.android.com/apk/res/android&quot;
 *         xmlns:app=&quot;http://schemas.android.com/apk/res-auto&quot;
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;androidx.core.widget.NestedScrollView
 *             android:layout_width=&quot;match_parent&quot;
 *             android:layout_height=&quot;match_parent&quot;
 *             app:layout_behavior=&quot;@string/appbar_scrolling_view_behavior&quot;&gt;
 *
 *         &lt;!-- Your scrolling content --&gt;
 *
 *     &lt;/androidx.core.widget.NestedScrollView&gt;
 *
 *     &lt;com.google.android.material.appbar.AppBarLayout
 *             android:layout_height=&quot;wrap_content&quot;
 *             android:layout_width=&quot;match_parent&quot;&gt;
 *
 *         &lt;androidx.appcompat.widget.Toolbar
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *         &lt;com.google.android.material.tabs.TabLayout
 *                 ...
 *                 app:layout_scrollFlags=&quot;scroll|enterAlways&quot;/&gt;
 *
 *     &lt;/com.google.android.material.appbar.AppBarLayout&gt;
 *
 * &lt;/androidx.coordinatorlayout.widget.CoordinatorLayout&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/layout/structure.html#structure-app-bar">
 *     http://www.google.com/design/spec/layout/structure.html#structure-app-bar</a>
 */
public class AppBarLayout extends LinearLayout implements CoordinatorLayout.AttachedBehavior {

  static final int PENDING_ACTION_NONE = 0x0;
  static final int PENDING_ACTION_EXPANDED = 0x1;
  static final int PENDING_ACTION_COLLAPSED = 1 << 1;
  static final int PENDING_ACTION_ANIMATE_ENABLED = 1 << 2;
  static final int PENDING_ACTION_FORCE = 1 << 3;

  /**
   * Interface definition for a callback to be invoked when an {@link AppBarLayout}'s vertical
   * offset changes.
   */
  // TODO(b/76413401): remove this base interface after the widget migration
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
  // TODO(b/76413401): update this interface after the widget migration
  public interface OnOffsetChangedListener extends BaseOnOffsetChangedListener<AppBarLayout> {
    void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset);
  }

  private static final int DEF_STYLE_RES = R.style.Widget_Design_AppBarLayout;
  private static final int INVALID_SCROLL_RANGE = -1;

  private int currentOffset;
  private int totalScrollRange = INVALID_SCROLL_RANGE;
  private int downPreScrollRange = INVALID_SCROLL_RANGE;
  private int downScrollRange = INVALID_SCROLL_RANGE;

  private boolean haveChildWithInterpolator;

  private int pendingAction = PENDING_ACTION_NONE;

  @Nullable private WindowInsetsCompat lastInsets;

  private List<BaseOnOffsetChangedListener> listeners;

  private boolean liftableOverride;
  private boolean liftable;
  private boolean lifted;

  private boolean liftOnScroll;
  @IdRes private int liftOnScrollTargetViewId;
  @Nullable private WeakReference<View> liftOnScrollTargetView;
  @Nullable private ValueAnimator elevationOverlayAnimator;

  private int[] tmpStatesArray;

  @Nullable private Drawable statusBarForeground;

  public AppBarLayout(@NonNull Context context) {
    this(context, null);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.appBarLayoutStyle);
  }

  public AppBarLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();
    setOrientation(VERTICAL);

    if (Build.VERSION.SDK_INT >= 21) {
      // Use the bounds view outline provider so that we cast a shadow, even without a
      // background
      ViewUtilsLollipop.setBoundsViewOutlineProvider(this);

      // If we're running on API 21+, we should reset any state list animator from our
      // default style
      ViewUtilsLollipop.setStateListAnimatorFromAttrs(
          this, attrs, defStyleAttr, DEF_STYLE_RES);
    }

    final TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.AppBarLayout,
            defStyleAttr,
            DEF_STYLE_RES);

    ViewCompat.setBackground(this, a.getDrawable(R.styleable.AppBarLayout_android_background));

    if (getBackground() instanceof ColorDrawable) {
      ColorDrawable background = (ColorDrawable) getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      materialShapeDrawable.setFillColor(ColorStateList.valueOf(background.getColor()));
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

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

    liftOnScroll = a.getBoolean(R.styleable.AppBarLayout_liftOnScroll, false);
    liftOnScrollTargetViewId =
        a.getResourceId(R.styleable.AppBarLayout_liftOnScrollTargetViewId, View.NO_ID);

    setStatusBarForeground(a.getDrawable(R.styleable.AppBarLayout_statusBarForeground));
    a.recycle();

    ViewCompat.setOnApplyWindowInsetsListener(
        this,
        new androidx.core.view.OnApplyWindowInsetsListener() {
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
  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners == null) {
      listeners = new ArrayList<>();
    }
    if (listener != null && !listeners.contains(listener)) {
      listeners.add(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void addOnOffsetChangedListener(OnOffsetChangedListener listener) {
    addOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  /**
   * Remove the previously added {@link OnOffsetChangedListener}.
   *
   * @param listener the listener to remove.
   */
  // TODO(b/76413401): change back to removeOnOffsetChangedListener once the widget migration is
  // finished since the shim class needs to implement this method.
  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(@Nullable BaseOnOffsetChangedListener listener) {
    if (listeners != null && listener != null) {
      listeners.remove(listener);
    }
  }

  @SuppressWarnings("FunctionalInterfaceClash")
  public void removeOnOffsetChangedListener(OnOffsetChangedListener listener) {
    removeOnOffsetChangedListener((BaseOnOffsetChangedListener) listener);
  }

  /**
   * Set the drawable to use for the status bar foreground drawable. Providing null will disable the
   * scrim functionality.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param drawable the drawable to display
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForeground(@Nullable Drawable drawable) {
    if (statusBarForeground != drawable) {
      if (statusBarForeground != null) {
        statusBarForeground.setCallback(null);
      }
      statusBarForeground = drawable != null ? drawable.mutate() : null;
      if (statusBarForeground != null) {
        if (statusBarForeground.isStateful()) {
          statusBarForeground.setState(getDrawableState());
        }
        DrawableCompat.setLayoutDirection(statusBarForeground, ViewCompat.getLayoutDirection(this));
        statusBarForeground.setVisible(getVisibility() == VISIBLE, false);
        statusBarForeground.setCallback(this);
      }
      updateWillNotDraw();
      ViewCompat.postInvalidateOnAnimation(this);
    }
  }

  /**
   * Set the color to use for the status bar foreground.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param color the color to display
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForegroundColor(@ColorInt int color) {
    setStatusBarForeground(new ColorDrawable(color));
  }

  /**
   * Set the drawable to use for the status bar foreground from resources.
   *
   * <p>This scrim is only shown when we have been given a top system inset.
   *
   * @param resId drawable resource id
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   * @see #getStatusBarForeground()
   */
  public void setStatusBarForegroundResource(@DrawableRes int resId) {
    setStatusBarForeground(AppCompatResources.getDrawable(getContext(), resId));
  }

  /**
   * Returns the drawable which is used for the status bar foreground.
   *
   * @see #setStatusBarForeground(Drawable)
   * @attr ref R.styleable#AppBarLayout_statusBarForeground
   */
  @Nullable
  public Drawable getStatusBarForeground() {
    return statusBarForeground;
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    super.draw(canvas);

    // Draw the status bar foreground drawable if we have a top inset
    if (shouldDrawStatusBarForeground()) {
      int saveCount = canvas.save();
      canvas.translate(0f, -currentOffset);
      statusBarForeground.draw(canvas);
      canvas.restoreToCount(saveCount);
    }
  }

  @Override
  protected void drawableStateChanged() {
    super.drawableStateChanged();

    final int[] state = getDrawableState();

    Drawable d = statusBarForeground;
    if (d != null && d.isStateful() && d.setState(state)) {
      invalidateDrawable(d);
    }
  }

  @Override
  protected boolean verifyDrawable(@NonNull Drawable who) {
    return super.verifyDrawable(who) || who == statusBarForeground;
  }

  @Override
  public void setVisibility(int visibility) {
    super.setVisibility(visibility);

    final boolean visible = visibility == VISIBLE;
    if (statusBarForeground != null) {
      statusBarForeground.setVisible(visible, false);
    }
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    // If we're set to handle system windows but our first child is not, we need to add some
    // height to ourselves to pad the first child down below the status bar
    final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightMode != MeasureSpec.EXACTLY
        && ViewCompat.getFitsSystemWindows(this)
        && shouldOffsetFirstChild()) {
      int newHeight = getMeasuredHeight();
      switch (heightMode) {
        case MeasureSpec.AT_MOST:
          // For AT_MOST, we need to clamp our desired height with the max height
          newHeight =
              MathUtils.clamp(
                  getMeasuredHeight() + getTopInset(), 0, MeasureSpec.getSize(heightMeasureSpec));
          break;
        case MeasureSpec.UNSPECIFIED:
          // For UNSPECIFIED we can use any height so just add the top inset
          newHeight += getTopInset();
          break;
        default: // fall out
      }
      setMeasuredDimension(getMeasuredWidth(), newHeight);
    }

    invalidateScrollRanges();
  }

  @Override
  protected void onLayout(boolean changed, int l, int t, int r, int b) {
    super.onLayout(changed, l, t, r, b);

    if (ViewCompat.getFitsSystemWindows(this) && shouldOffsetFirstChild()) {
      // If we need to offset the first child, we need to offset all of them to make space
      final int topInset = getTopInset();
      for (int z = getChildCount() - 1; z >= 0; z--) {
        ViewCompat.offsetTopAndBottom(getChildAt(z), topInset);
      }
    }

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

    if (statusBarForeground != null) {
      statusBarForeground.setBounds(0, 0, getWidth(), getTopInset());
    }

    // If the user has set liftable manually, don't set liftable state automatically.
    if (!liftableOverride) {
      setLiftableState(liftOnScroll || hasCollapsibleChild());
    }
  }

  private void updateWillNotDraw() {
    setWillNotDraw(!shouldDrawStatusBarForeground());
  }

  private boolean shouldDrawStatusBarForeground() {
    return statusBarForeground != null && getTopInset() > 0;
  }

  private boolean hasCollapsibleChild() {
    for (int i = 0, z = getChildCount(); i < z; i++) {
      if (((LayoutParams) getChildAt(i).getLayoutParams()).isCollapsible()) {
        return true;
      }
    }
    return false;
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
          "AppBarLayout is always vertical and does not support horizontal orientation");
    }
    super.setOrientation(orientation);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  @NonNull
  public CoordinatorLayout.Behavior<AppBarLayout> getBehavior() {
    return new AppBarLayout.Behavior();
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
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
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
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
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_expanded
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

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    clearLiftOnScrollTargetView();
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

        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          // If this is the first child and it wants to handle system windows, we need to make
          // sure we don't scroll it past the inset
          range -= getTopInset();
        }
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
    return totalScrollRange = Math.max(0, range);
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
        int childRange = lp.topMargin + lp.bottomMargin;
        // The view has the quick return flag combination...
        if ((flags & LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED) != 0) {
          // If they're set to enter collapsed, use the minimum height
          childRange += ViewCompat.getMinimumHeight(child);
        } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
          // Only enter by the amount of the collapsed height
          childRange += childHeight - ViewCompat.getMinimumHeight(child);
        } else {
          // Else use the full height
          childRange += childHeight;
        }
        if (i == 0 && ViewCompat.getFitsSystemWindows(child)) {
          // If this is the first child and it wants to handle system windows, we need to make
          // sure we don't scroll past the inset
          childRange = Math.min(childRange, childHeight - getTopInset());
        }
        range += childRange;
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
          range -= ViewCompat.getMinimumHeight(child);
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

  void onOffsetChanged(int offset) {
    currentOffset = offset;

    if (!willNotDraw()) {
      ViewCompat.postInvalidateOnAnimation(this);
    }

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
      // Note that we can't allocate this at the class level (in declaration) since some paths in
      // super View constructor are going to call this method before that
      tmpStatesArray = new int[4];
    }
    final int[] extraStates = tmpStatesArray;
    final int[] states = super.onCreateDrawableState(extraSpace + extraStates.length);

    extraStates[0] = liftable ? R.attr.state_liftable : -R.attr.state_liftable;
    extraStates[1] = liftable && lifted ? R.attr.state_lifted : -R.attr.state_lifted;

    // Note that state_collapsible and state_collapsed are deprecated. This is to keep compatibility
    // with existing state list animators that depend on these states.
    extraStates[2] = liftable ? R.attr.state_collapsible : -R.attr.state_collapsible;
    extraStates[3] = liftable && lifted ? R.attr.state_collapsed : -R.attr.state_collapsed;

    return mergeDrawableStates(states, extraStates);
  }

  /**
   * Sets whether the {@link AppBarLayout} is liftable or not.
   *
   * @return true if the liftable state changed
   */
  public boolean setLiftable(boolean liftable) {
    this.liftableOverride = true;
    return setLiftableState(liftable);
  }

  // Internal helper method that updates liftable state without enabling the override.
  private boolean setLiftableState(boolean liftable) {
    if (this.liftable != liftable) {
      this.liftable = liftable;
      refreshDrawableState();
      return true;
    }
    return false;
  }

  /**
   * Sets whether the {@link AppBarLayout} is in a lifted state or not.
   *
   * @return true if the lifted state changed
   */
  public boolean setLifted(boolean lifted) {
    return setLiftedState(lifted);
  }

  /**
   * Returns whether the {@link AppBarLayout} is in a lifted state or not.
   */
  public boolean isLifted() {
    return lifted;
  }

  // Internal helper method that updates lifted state.
  boolean setLiftedState(boolean lifted) {
    if (this.lifted != lifted) {
      this.lifted = lifted;
      refreshDrawableState();
      if (liftOnScroll && getBackground() instanceof MaterialShapeDrawable) {
        startLiftOnScrollElevationOverlayAnimation((MaterialShapeDrawable) getBackground(), lifted);
      }
      return true;
    }
    return false;
  }

  private void startLiftOnScrollElevationOverlayAnimation(
      @NonNull final MaterialShapeDrawable background, boolean lifted) {
    float appBarElevation = getResources().getDimension(R.dimen.design_appbar_elevation);
    float fromElevation = lifted ? 0 : appBarElevation;
    float toElevation = lifted ? appBarElevation : 0;

    if (elevationOverlayAnimator != null) {
      elevationOverlayAnimator.cancel();
    }

    elevationOverlayAnimator = ValueAnimator.ofFloat(fromElevation, toElevation);
    elevationOverlayAnimator.setDuration(
        getResources().getInteger(R.integer.app_bar_elevation_anim_duration));
    elevationOverlayAnimator.setInterpolator(AnimationUtils.LINEAR_INTERPOLATOR);
    elevationOverlayAnimator.addUpdateListener(
        new AnimatorUpdateListener() {
          @Override
          public void onAnimationUpdate(@NonNull ValueAnimator valueAnimator) {
            background.setElevation((float) valueAnimator.getAnimatedValue());
          }
        });
    elevationOverlayAnimator.start();
  }

  /**
   * Sets whether the {@link AppBarLayout} lifts on scroll or not.
   *
   * <p>If set to true, the {@link AppBarLayout} will animate to the lifted, or elevated, state when
   * content is scrolled beneath it. Requires
   * `app:layout_behavior="@string/appbar_scrolling_view_behavior` to be set on the scrolling
   * sibling (e.g., `NestedScrollView`, `RecyclerView`, etc.).
   */
  public void setLiftOnScroll(boolean liftOnScroll) {
    this.liftOnScroll = liftOnScroll;
  }

  /** Returns whether the {@link AppBarLayout} lifts on scroll or not. */
  public boolean isLiftOnScroll() {
    return liftOnScroll;
  }

  /**
   * Sets the id of the view that the {@link AppBarLayout} should use to determine whether it should
   * be lifted.
   */
  public void setLiftOnScrollTargetViewId(@IdRes int liftOnScrollTargetViewId) {
    this.liftOnScrollTargetViewId = liftOnScrollTargetViewId;
    // Invalidate cached target view so it will be looked up on next scroll.
    clearLiftOnScrollTargetView();
  }

  /**
   * Returns the id of the view that the {@link AppBarLayout} should use to determine whether it
   * should be lifted.
   */
  @IdRes
  public int getLiftOnScrollTargetViewId() {
    return liftOnScrollTargetViewId;
  }

  boolean shouldLift(@Nullable View defaultScrollingView) {
    View scrollingView = findLiftOnScrollTargetView(defaultScrollingView);
    if (scrollingView == null) {
      scrollingView = defaultScrollingView;
    }
    return scrollingView != null
        && (scrollingView.canScrollVertically(-1) || scrollingView.getScrollY() > 0);
  }

  @Nullable
  private View findLiftOnScrollTargetView(@Nullable View defaultScrollingView) {
    if (liftOnScrollTargetView == null && liftOnScrollTargetViewId != View.NO_ID) {
      View targetView = null;
      if (defaultScrollingView != null) {
        targetView = defaultScrollingView.findViewById(liftOnScrollTargetViewId);
      }
      if (targetView == null && getParent() instanceof ViewGroup) {
        // Assumes the scrolling view is a child of the AppBarLayout's parent,
        // which should be true due to the CoordinatorLayout pattern.
        targetView = ((ViewGroup) getParent()).findViewById(liftOnScrollTargetViewId);
      }
      if (targetView != null) {
        liftOnScrollTargetView = new WeakReference<>(targetView);
      }
    }
    return liftOnScrollTargetView != null ? liftOnScrollTargetView.get() : null;
  }

  private void clearLiftOnScrollTargetView() {
    if (liftOnScrollTargetView != null) {
      liftOnScrollTargetView.clear();
    }
    liftOnScrollTargetView = null;
  }

  /**
   * @deprecated target elevation is now deprecated. AppBarLayout's elevation is now controlled via
   *     a {@link android.animation.StateListAnimator}. If a target elevation is set, either by this
   *     method or the {@code app:elevation} attribute, a new state list animator is created which
   *     uses the given {@code elevation} value.
   * @attr ref com.google.android.material.R.styleable#AppBarLayout_elevation
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

  /**
   * Whether the first child needs to be offset because it does not want to handle the top window
   * inset
   */
  private boolean shouldOffsetFirstChild() {
    if (getChildCount() > 0) {
      final View firstChild = getChildAt(0);
      return firstChild.getVisibility() != GONE && !ViewCompat.getFitsSystemWindows(firstChild);
    }
    return false;
  }

  WindowInsetsCompat onWindowInsetChanged(final WindowInsetsCompat insets) {
    WindowInsetsCompat newInsets = null;

    if (ViewCompat.getFitsSystemWindows(this)) {
      // If we're set to fit system windows, keep the insets
      newInsets = insets;
    }

    // If our insets have changed, keep them and trigger a layout...
    if (!ObjectsCompat.equals(lastInsets, newInsets)) {
      lastInsets = newInsets;
      updateWillNotDraw();
      requestLayout();
    }

    return insets;
  }

  /** A {@link ViewGroup.LayoutParams} implementation for {@link AppBarLayout}. */
  public static class LayoutParams extends LinearLayout.LayoutParams {

    /** @hide */
    @RestrictTo(LIBRARY_GROUP)
    @IntDef(
        flag = true,
        value = {
          SCROLL_FLAG_NO_SCROLL,
          SCROLL_FLAG_SCROLL,
          SCROLL_FLAG_EXIT_UNTIL_COLLAPSED,
          SCROLL_FLAG_ENTER_ALWAYS,
          SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED,
          SCROLL_FLAG_SNAP,
          SCROLL_FLAG_SNAP_MARGINS,
        })
    @Retention(RetentionPolicy.SOURCE)
    public @interface ScrollFlags {}
    /**
     * Disable scrolling on the view. This flag should not be combined with any of the other scroll
     * flags.
     */
    public static final int SCROLL_FLAG_NO_SCROLL = 0x0;

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
    public static final int SCROLL_FLAG_EXIT_UNTIL_COLLAPSED = 1 << 1;

    /**
     * When entering (scrolling on screen) the view will scroll on any downwards scroll event,
     * regardless of whether the scrolling view is also scrolling. This is commonly referred to as
     * the 'quick return' pattern.
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS = 1 << 2;

    /**
     * An additional flag for 'enterAlways' which modifies the returning view to only initially
     * scroll back to it's collapsed height. Once the scrolling view has reached the end of it's
     * scroll range, the remainder of this view will be scrolled into view. The collapsed height is
     * defined by the view's minimum height.
     *
     * @see ViewCompat#getMinimumHeight(View)
     * @see View#setMinimumHeight(int)
     */
    public static final int SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED = 1 << 3;

    /**
     * Upon a scroll ending, if the view is only partially visible then it will be snapped and
     * scrolled to its closest edge. For example, if the view only has its bottom 25% displayed, it
     * will be scrolled off screen completely. Conversely, if its bottom 75% is visible then it will
     * be scrolled fully into view.
     */
    public static final int SCROLL_FLAG_SNAP = 1 << 4;

    /**
     * An additional flag to be used with 'snap'. If set, the view will be snapped to its top and
     * bottom margins, as opposed to the edges of the view itself.
     */
    public static final int SCROLL_FLAG_SNAP_MARGINS = 1 << 5;

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
    public LayoutParams(@NonNull LayoutParams source) {
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
     *     #SCROLL_FLAG_SNAP_MARGINS}. Otherwise, use {@link #SCROLL_FLAG_NO_SCROLL} to disable
     *     scrolling.
     * @see #getScrollFlags()
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    public void setScrollFlags(@ScrollFlags int flags) {
      scrollFlags = flags;
    }

    /**
     * Returns the scrolling flags.
     *
     * @see #setScrollFlags(int)
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollFlags
     */
    @ScrollFlags
    public int getScrollFlags() {
      return scrollFlags;
    }

    /**
     * Set the interpolator to when scrolling the view associated with this {@link LayoutParams}.
     *
     * @param interpolator the interpolator to use, or null to use normal 1-to-1 scrolling.
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
     * @see #getScrollInterpolator()
     */
    public void setScrollInterpolator(Interpolator interpolator) {
      scrollInterpolator = interpolator;
    }

    /**
     * Returns the {@link Interpolator} being used for scrolling the view associated with this
     * {@link LayoutParams}. Null indicates 'normal' 1-to-1 scrolling.
     *
     * @attr ref com.google.android.material.R.styleable#AppBarLayout_Layout_layout_scrollInterpolator
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
  // TODO(b/76413401): remove the base class and generic type after the widget migration is done
  public static class Behavior extends BaseBehavior<AppBarLayout> {

    /** Callback to allow control over any {@link AppBarLayout} dragging. */
    public abstract static class DragCallback extends BaseBehavior.BaseDragCallback<AppBarLayout> {}

    public Behavior() {
      super();
    }

    public Behavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }
  }

  /**
   * The default {@link Behavior} for {@link AppBarLayout}. Implements the necessary nested scroll
   * handling with offsetting.
   */
  // TODO(b/76413401): remove this base class and generic type after the widget migration is done
  protected static class BaseBehavior<T extends AppBarLayout> extends HeaderBehavior<T> {
    private static final int MAX_OFFSET_ANIMATION_DURATION = 600; // ms
    private static final int INVALID_POSITION = -1;

    /** Callback to allow control over any {@link AppBarLayout} dragging. */
    // TODO(b/76413401): remove this base class and generic type after the widget migration
    public abstract static class BaseDragCallback<T extends AppBarLayout> {
      /**
       * Allows control over whether the given {@link AppBarLayout} can be dragged or not.
       *
       * <p>Dragging is defined as a direct touch on the AppBarLayout with movement. This call does
       * not affect any nested scrolling.
       *
       * @return true if we are in a position to scroll the AppBarLayout via a drag, false if not.
       */
      public abstract boolean canDrag(@NonNull T appBarLayout);
    }

    private int offsetDelta;

    @NestedScrollType private int lastStartedType;

    private ValueAnimator offsetAnimator;

    private int offsetToChildIndexOnLayout = INVALID_POSITION;
    private boolean offsetToChildIndexOnLayoutIsMinHeight;
    private float offsetToChildIndexOnLayoutPerc;

    @Nullable private WeakReference<View> lastNestedScrollingChildRef;
    private BaseDragCallback onDragCallback;

    public BaseBehavior() {}

    public BaseBehavior(Context context, AttributeSet attrs) {
      super(context, attrs);
    }

    @Override
    public boolean onStartNestedScroll(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
        @NonNull View directTargetChild,
        View target,
        int nestedScrollAxes,
        int type) {
      // Return true if we're nested scrolling vertically, and we either have lift on scroll enabled
      // or we can scroll the children.
      final boolean started =
          (nestedScrollAxes & ViewCompat.SCROLL_AXIS_VERTICAL) != 0
              && (child.isLiftOnScroll() || canScrollChildren(parent, child, directTargetChild));

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

    // Return true if there are scrollable children and the scrolling view is big enough to scroll.
    private boolean canScrollChildren(
        @NonNull CoordinatorLayout parent, @NonNull T child, @NonNull View directTargetChild) {
      return child.hasScrollableChildren()
          && parent.getHeight() - directTargetChild.getHeight() <= child.getHeight();
    }

    @Override
    public void onNestedPreScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
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
        }
      }
      if (child.isLiftOnScroll()) {
        child.setLiftedState(child.shouldLift(target));
      }
    }

    @Override
    public void onNestedScroll(
        CoordinatorLayout coordinatorLayout,
        @NonNull T child,
        View target,
        int dxConsumed,
        int dyConsumed,
        int dxUnconsumed,
        int dyUnconsumed,
        int type,
        int[] consumed) {
      if (dyUnconsumed < 0) {
        // If the scrolling view is scrolling down but not consuming, it's probably be at
        // the top of it's content
        consumed[1] =
            scroll(coordinatorLayout, child, dyUnconsumed, -child.getDownNestedScrollRange(), 0);
      }

      if (dyUnconsumed == 0) {
        // The scrolling view may scroll to the top of its content without updating the actions, so
        // update here.
        updateAccessibilityActions(coordinatorLayout, child);
      }
    }

    @Override
    public void onStopNestedScroll(
        CoordinatorLayout coordinatorLayout, @NonNull T abl, View target, int type) {
      // onStartNestedScroll for a fling will happen before onStopNestedScroll for the scroll. This
      // isn't necessarily guaranteed yet, but it should be in the future. We use this to our
      // advantage to check if a fling (ViewCompat.TYPE_NON_TOUCH) will start after the touch scroll
      // (ViewCompat.TYPE_TOUCH) ends
      if (lastStartedType == ViewCompat.TYPE_TOUCH || type == ViewCompat.TYPE_NON_TOUCH) {
        // If we haven't been flung, or a fling is ending
        snapToChildIfNeeded(coordinatorLayout, abl);
        if (abl.isLiftOnScroll()) {
          abl.setLiftedState(abl.shouldLift(target));
        }
      }

      // Keep a reference to the previous nested scrolling child
      lastNestedScrollingChildRef = new WeakReference<>(target);
    }

    /**
     * Set a callback to control any {@link AppBarLayout} dragging.
     *
     * @param callback the callback to use, or {@code null} to use the default behavior.
     */
    public void setDragCallback(@Nullable BaseDragCallback callback) {
      onDragCallback = callback;
    }

    private void animateOffsetTo(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T child,
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
              public void onAnimationUpdate(@NonNull ValueAnimator animator) {
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

    private int getChildIndexOnOffset(@NonNull T abl, final int offset) {
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

    private void snapToChildIfNeeded(CoordinatorLayout coordinatorLayout, @NonNull T abl) {
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
              coordinatorLayout, abl, MathUtils.clamp(newOffset, -abl.getTotalScrollRange(), 0), 0);
        }
      }
    }

    private static boolean checkFlag(final int flags, final int check) {
      return (flags & check) == check;
    }

    @Override
    public boolean onMeasureChild(
        @NonNull CoordinatorLayout parent,
        @NonNull T child,
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
    public boolean onLayoutChild(
        @NonNull CoordinatorLayout parent, @NonNull T abl, int layoutDirection) {
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
          MathUtils.clamp(getTopAndBottomOffset(), -abl.getTotalScrollRange(), 0));

      // Update the AppBarLayout's drawable state for any elevation changes. This is needed so that
      // the elevation is set in the first layout, so that we don't get a visual jump pre-N (due to
      // the draw dispatch skip)
      updateAppBarLayoutDrawableState(
          parent, abl, getTopAndBottomOffset(), 0 /* direction */, true /* forceJump */);

      // Make sure we dispatch the offset update
      abl.onOffsetChanged(getTopAndBottomOffset());

      updateAccessibilityActions(parent, abl);
      return handled;
    }

    private void updateAccessibilityActions(
        CoordinatorLayout coordinatorLayout, @NonNull T appBarLayout) {
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_FORWARD.getId());
      ViewCompat.removeAccessibilityAction(coordinatorLayout, ACTION_SCROLL_BACKWARD.getId());
      View scrollingView = findFirstScrollingChild(coordinatorLayout);
      // Don't add a11y actions if there is no scrolling view that the abl depends on for scrolling
      // or the abl has no scroll range.
      if (scrollingView == null || appBarLayout.getTotalScrollRange() == 0) {
        return;
      }
      // Don't add actions if the scrolling view doesn't have the behavior that will cause the abl
      // to scroll.
      CoordinatorLayout.LayoutParams lp =
          (CoordinatorLayout.LayoutParams) scrollingView.getLayoutParams();
      if (!(lp.getBehavior() instanceof ScrollingViewBehavior)) {
        return;
      }
      addAccessibilityScrollActions(coordinatorLayout, appBarLayout, scrollingView);
    }

    private void addAccessibilityScrollActions(
        final CoordinatorLayout coordinatorLayout,
        @NonNull final T appBarLayout,
        @NonNull final View scrollingView) {
      if (getTopBottomOffsetForScrollingSibling() != -appBarLayout.getTotalScrollRange()
          && scrollingView.canScrollVertically(1)) {
        // Add a collapsing action if the view can scroll up and the offset isn't the abl scroll
        // range. (This offset means the view is completely collapsed). Collapse to minimum height.
        addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_FORWARD, false);
      }
      // Don't add an expanding action if the sibling offset is 0, which would mean the abl is
      // completely expanded.
      if (getTopBottomOffsetForScrollingSibling() != 0) {
        if (scrollingView.canScrollVertically(-1)) {
          // Expanding action. If the view can scroll down, expand the app bar reflecting the logic
          // in onNestedPreScroll.
          final int dy = -appBarLayout.getDownNestedPreScrollRange();
          // Offset by non-zero.
          if (dy != 0) {
            ViewCompat.replaceAccessibilityAction(
                coordinatorLayout,
                ACTION_SCROLL_BACKWARD,
                null,
                new AccessibilityViewCommand() {
                  @Override
                  public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
                    onNestedPreScroll(
                        coordinatorLayout,
                        appBarLayout,
                        scrollingView,
                        0,
                        dy,
                        new int[] {0, 0},
                        ViewCompat.TYPE_NON_TOUCH);
                    return true;
                  }
                });
          }
        } else {
          // If the view can't scroll down, we are probably at the top of the scrolling content so
          // expand completely.
          addActionToExpand(coordinatorLayout, appBarLayout, ACTION_SCROLL_BACKWARD, true);
        }
      }
    }

    private void addActionToExpand(
        CoordinatorLayout parent,
        @NonNull final T appBarLayout,
        @NonNull AccessibilityActionCompat action,
        final boolean expand) {
      ViewCompat.replaceAccessibilityAction(
          parent,
          action,
          null,
          new AccessibilityViewCommand() {
            @Override
            public boolean perform(@NonNull View view, @Nullable CommandArguments arguments) {
              appBarLayout.setExpanded(expand);
              return true;
            }
          });
    }

    @Override
    boolean canDragView(T view) {
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
    void onFlingFinished(@NonNull CoordinatorLayout parent, @NonNull T layout) {
      // At the end of a manual fling, check to see if we need to snap to the edge-child
      snapToChildIfNeeded(parent, layout);
      if (layout.isLiftOnScroll()) {
        layout.setLiftedState(layout.shouldLift(findFirstScrollingChild(parent)));
      }
    }

    @Override
    int getMaxDragOffset(@NonNull T view) {
      return -view.getDownNestedScrollRange();
    }

    @Override
    int getScrollRangeForDragFling(@NonNull T view) {
      return view.getTotalScrollRange();
    }

    @Override
    int setHeaderTopBottomOffset(
        @NonNull CoordinatorLayout coordinatorLayout,
        @NonNull T appBarLayout,
        int newOffset,
        int minOffset,
        int maxOffset) {
      final int curOffset = getTopBottomOffsetForScrollingSibling();
      int consumed = 0;

      if (minOffset != 0 && curOffset >= minOffset && curOffset <= maxOffset) {
        // If we have some scrolling range, and we're currently within the min and max
        // offsets, calculate a new offset
        newOffset = MathUtils.clamp(newOffset, minOffset, maxOffset);
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
          appBarLayout.onOffsetChanged(getTopAndBottomOffset());

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

      updateAccessibilityActions(coordinatorLayout, appBarLayout);
      return consumed;
    }

    @VisibleForTesting
    boolean isOffsetAnimatorRunning() {
      return offsetAnimator != null && offsetAnimator.isRunning();
    }

    private int interpolateOffset(@NonNull T layout, final int offset) {
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
        @NonNull final CoordinatorLayout parent,
        @NonNull final T layout,
        final int offset,
        final int direction,
        final boolean forceJump) {
      final View child = getAppBarChildOnOffset(layout, offset);
      if (child != null) {
        final AppBarLayout.LayoutParams childLp = (LayoutParams) child.getLayoutParams();
        final int flags = childLp.getScrollFlags();
        boolean lifted = false;

        if ((flags & LayoutParams.SCROLL_FLAG_SCROLL) != 0) {
          final int minHeight = ViewCompat.getMinimumHeight(child);

          if (direction > 0
              && (flags
                      & (LayoutParams.SCROLL_FLAG_ENTER_ALWAYS
                          | LayoutParams.SCROLL_FLAG_ENTER_ALWAYS_COLLAPSED))
                  != 0) {
            // We're set to enter always collapsed so we are only collapsed when
            // being scrolled down, and in a collapsed offset
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          } else if ((flags & LayoutParams.SCROLL_FLAG_EXIT_UNTIL_COLLAPSED) != 0) {
            // We're set to exit until collapsed, so any offset which results in
            // the minimum height (or less) being shown is collapsed
            lifted = -offset >= child.getBottom() - minHeight - layout.getTopInset();
          }
        }

        if (layout.isLiftOnScroll()) {
          // Use first scrolling child as default scrolling view for updating lifted state because
          // it represents the content that would be scrolled beneath the app bar.
          lifted = layout.shouldLift(findFirstScrollingChild(parent));
        }

        final boolean changed = layout.setLiftedState(lifted);

        if (forceJump || (changed && shouldJumpElevationState(parent, layout))) {
          // If the collapsed state changed, we may need to
          // jump to the current state if we have an overlapping view
          layout.jumpDrawablesToCurrentState();
        }
      }
    }

    private boolean shouldJumpElevationState(@NonNull CoordinatorLayout parent, @NonNull T layout) {
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

    @Nullable
    private static View getAppBarChildOnOffset(
        @NonNull final AppBarLayout layout, final int offset) {
      final int absOffset = Math.abs(offset);
      for (int i = 0, z = layout.getChildCount(); i < z; i++) {
        final View child = layout.getChildAt(i);
        if (absOffset >= child.getTop() && absOffset <= child.getBottom()) {
          return child;
        }
      }
      return null;
    }

    @Nullable
    private View findFirstScrollingChild(@NonNull CoordinatorLayout parent) {
      for (int i = 0, z = parent.getChildCount(); i < z; i++) {
        final View child = parent.getChildAt(i);
        if (child instanceof NestedScrollingChild
            || child instanceof ListView
            || child instanceof ScrollView) {
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
    public Parcelable onSaveInstanceState(@NonNull CoordinatorLayout parent, @NonNull T abl) {
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
        @NonNull CoordinatorLayout parent, @NonNull T appBarLayout, Parcelable state) {
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

    /** A {@link Parcelable} implementation for {@link AppBarLayout}. */
    protected static class SavedState extends AbsSavedState {
      int firstVisibleChildIndex;
      float firstVisibleChildPercentageShown;
      boolean firstVisibleChildAtMinimumHeight;

      public SavedState(@NonNull Parcel source, ClassLoader loader) {
        super(source, loader);
        firstVisibleChildIndex = source.readInt();
        firstVisibleChildPercentageShown = source.readFloat();
        firstVisibleChildAtMinimumHeight = source.readByte() != 0;
      }

      public SavedState(Parcelable superState) {
        super(superState);
      }

      @Override
      public void writeToParcel(@NonNull Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(firstVisibleChildIndex);
        dest.writeFloat(firstVisibleChildPercentageShown);
        dest.writeByte((byte) (firstVisibleChildAtMinimumHeight ? 1 : 0));
      }

      public static final Creator<SavedState> CREATOR =
          new ClassLoaderCreator<SavedState>() {
            @NonNull
            @Override
            public SavedState createFromParcel(@NonNull Parcel source, ClassLoader loader) {
              return new SavedState(source, loader);
            }

            @Nullable
            @Override
            public SavedState createFromParcel(@NonNull Parcel source) {
              return new SavedState(source, null);
            }

            @NonNull
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
    public boolean onDependentViewChanged(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      offsetChildAsNeeded(child, dependency);
      updateLiftedStateIfNeeded(child, dependency);
      return false;
    }

    @Override
    public void onDependentViewRemoved(
        @NonNull CoordinatorLayout parent, @NonNull View child, @NonNull View dependency) {
      if (dependency instanceof AppBarLayout) {
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_FORWARD.getId());
        ViewCompat.removeAccessibilityAction(parent, ACTION_SCROLL_BACKWARD.getId());
      }
    }

    @Override
    public boolean onRequestChildRectangleOnScreen(
        @NonNull CoordinatorLayout parent,
        @NonNull View child,
        @NonNull Rect rectangle,
        boolean immediate) {
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

    private void offsetChildAsNeeded(@NonNull View child, @NonNull View dependency) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) dependency.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        // Offset the child, pinning it to the bottom the header-dependency, maintaining
        // any vertical gap and overlap
        final BaseBehavior ablBehavior = (BaseBehavior) behavior;
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

    private static int getAppBarLayoutOffset(@NonNull AppBarLayout abl) {
      final CoordinatorLayout.Behavior behavior =
          ((CoordinatorLayout.LayoutParams) abl.getLayoutParams()).getBehavior();
      if (behavior instanceof BaseBehavior) {
        return ((BaseBehavior) behavior).getTopBottomOffsetForScrollingSibling();
      }
      return 0;
    }

    @Nullable
    @Override
    AppBarLayout findFirstDependency(@NonNull List<View> views) {
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

    private void updateLiftedStateIfNeeded(View child, View dependency) {
      if (dependency instanceof AppBarLayout) {
        AppBarLayout appBarLayout = (AppBarLayout) dependency;
        if (appBarLayout.isLiftOnScroll()) {
          appBarLayout.setLiftedState(appBarLayout.shouldLift(child));
        }
      }
    }
  }
}
