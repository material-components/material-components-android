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

package android.support.design.tabs;

import android.support.design.R;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static android.support.v4.view.ViewPager.SCROLL_STATE_DRAGGING;
import static android.support.v4.view.ViewPager.SCROLL_STATE_IDLE;
import static android.support.v4.view.ViewPager.SCROLL_STATE_SETTLING;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.database.DataSetObserver;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.BoolRes;
import android.support.annotation.ColorInt;
import android.support.annotation.ColorRes;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.IntDef;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.StringRes;
import android.support.design.animation.AnimationUtils;
import android.support.design.internal.ThemeEnforcement;
import android.support.design.internal.ViewUtils;
import android.support.design.resources.MaterialResources;
import android.support.design.ripple.RippleUtils;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pools;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MarginLayoutParamsCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.PointerIconCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.TooltipCompat;
import android.text.Layout;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * TabLayout provides a horizontal layout to display tabs.
 *
 * <p>Population of the tabs to display is done through {@link Tab} instances. You create tabs via
 * {@link #newTab()}. From there you can change the tab's label or icon via {@link Tab#setText(int)}
 * and {@link Tab#setIcon(int)} respectively. To display the tab, you need to add it to the layout
 * via one of the {@link #addTab(Tab)} methods. For example:
 *
 * <pre>
 * TabLayout tabLayout = ...;
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 1"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 2"));
 * tabLayout.addTab(tabLayout.newTab().setText("Tab 3"));
 * </pre>
 *
 * You should set a listener via {@link #setOnTabSelectedListener(OnTabSelectedListener)} to be
 * notified when any tab's selection state has been changed.
 *
 * <p>You can also add items to TabLayout in your layout through the use of {@link TabItem}. An
 * example usage is like so:
 *
 * <pre>
 * &lt;android.support.design.tabs.TabLayout
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_width=&quot;match_parent&quot;&gt;
 *
 *     &lt;android.support.design.tabs.TabItem
 *             android:text=&quot;@string/tab_text&quot;/&gt;
 *
 *     &lt;android.support.design.tabs.TabItem
 *             android:icon=&quot;@drawable/ic_android&quot;/&gt;
 *
 * &lt;/android.support.design.tabs.TabLayout&gt;
 * </pre>
 *
 * <h3>ViewPager integration</h3>
 *
 * <p>If you're using a {@link android.support.v4.view.ViewPager} together with this layout, you can
 * call {@link #setupWithViewPager(ViewPager)} to link the two together. This layout will be
 * automatically populated from the {@link PagerAdapter}'s page titles.
 *
 * <p>This view also supports being used as part of a ViewPager's decor, and can be added directly
 * to the ViewPager in a layout resource file like so:
 *
 * <pre>
 * &lt;android.support.v4.view.ViewPager
 *     android:layout_width=&quot;match_parent&quot;
 *     android:layout_height=&quot;match_parent&quot;&gt;
 *
 *     &lt;android.support.design.tabs.TabLayout
 *         android:layout_width=&quot;match_parent&quot;
 *         android:layout_height=&quot;wrap_content&quot;
 *         android:layout_gravity=&quot;top&quot; /&gt;
 *
 * &lt;/android.support.v4.view.ViewPager&gt;
 * </pre>
 *
 * @see <a href="http://www.google.com/design/spec/components/tabs.html">Tabs</a>
 * @attr ref android.support.design.R.styleable#TabLayout_tabPadding
 * @attr ref android.support.design.R.styleable#TabLayout_tabPaddingStart
 * @attr ref android.support.design.R.styleable#TabLayout_tabPaddingTop
 * @attr ref android.support.design.R.styleable#TabLayout_tabPaddingEnd
 * @attr ref android.support.design.R.styleable#TabLayout_tabPaddingBottom
 * @attr ref android.support.design.R.styleable#TabLayout_tabContentStart
 * @attr ref android.support.design.R.styleable#TabLayout_tabBackground
 * @attr ref android.support.design.R.styleable#TabLayout_tabMinWidth
 * @attr ref android.support.design.R.styleable#TabLayout_tabMaxWidth
 * @attr ref android.support.design.R.styleable#TabLayout_tabTextAppearance
 */
@ViewPager.DecorView
public class TabLayout extends HorizontalScrollView {

  @Dimension(unit = Dimension.DP)
  private static final int DEFAULT_HEIGHT_WITH_TEXT_ICON = 72;

  @Dimension(unit = Dimension.DP)
  static final int DEFAULT_GAP_TEXT_ICON = 8;

  @Dimension(unit = Dimension.DP)
  private static final int DEFAULT_HEIGHT = 48;

  @Dimension(unit = Dimension.DP)
  private static final int TAB_MIN_WIDTH_MARGIN = 56;

  @Dimension(unit = Dimension.DP)
  private static final int MIN_INDICATOR_WIDTH = 24;

  @Dimension(unit = Dimension.DP)
  static final int FIXED_WRAP_GUTTER_MIN = 16;

  private static final int INVALID_WIDTH = -1;

  private static final int ANIMATION_DURATION = 300;

  private static final Pools.Pool<Tab> tabPool = new Pools.SynchronizedPool<>(16);

  /**
   * Scrollable tabs display a subset of tabs at any given moment, and can contain longer tab labels
   * and a larger number of tabs. They are best used for browsing contexts in touch interfaces when
   * users don’t need to directly compare the tab labels.
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int MODE_SCROLLABLE = 0;

  /**
   * Fixed tabs display all tabs concurrently and are best used with content that benefits from
   * quick pivots between tabs. The maximum number of tabs is limited by the view’s width. Fixed
   * tabs have equal width, based on the widest tab label.
   *
   * @see #setTabMode(int)
   * @see #getTabMode()
   */
  public static final int MODE_FIXED = 1;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {MODE_SCROLLABLE, MODE_FIXED})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Mode {}

  /**
   * Gravity used to fill the {@link TabLayout} as much as possible. This option only takes effect
   * when used with {@link #MODE_FIXED}.
   *
   * @see #setTabGravity(int)
   * @see #getTabGravity()
   */
  public static final int GRAVITY_FILL = 0;

  /**
   * Gravity used to lay out the tabs in the center of the {@link TabLayout}.
   *
   * @see #setTabGravity(int)
   * @see #getTabGravity()
   */
  public static final int GRAVITY_CENTER = 1;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
    flag = true,
    value = {GRAVITY_FILL, GRAVITY_CENTER}
  )
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabGravity {}

  /**
   * Indicator gravity used to align the tab selection indicator to the bottom of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown. This is the default value.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_BOTTOM = 0;

  /**
   * Indicator gravity used to align the tab selection indicator to the center of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_CENTER = 1;

  /**
   * Indicator gravity used to align the tab selection indicator to the top of the {@link
   * TabLayout}. This will only take effect if the indicator height is set via the custom indicator
   * drawable's intrinsic height (preferred), via the {@code tabIndicatorHeight} attribute
   * (deprecated), or via {@link #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the
   * indicator will not be shown.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_TOP = 2;

  /**
   * Indicator gravity used to stretch the tab selection indicator across the entire height and
   * width of the {@link TabLayout}. This will disregard {@code tabIndicatorHeight} and the
   * indicator drawable's intrinsic height, if set.
   *
   * @see #setSelectedTabIndicatorGravity(int)
   * @see #getTabIndicatorGravity()
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorGravity
   */
  public static final int INDICATOR_GRAVITY_STRETCH = 3;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      value = {
      INDICATOR_GRAVITY_BOTTOM,
      INDICATOR_GRAVITY_CENTER,
      INDICATOR_GRAVITY_TOP,
      INDICATOR_GRAVITY_STRETCH
    }
  )
  @Retention(RetentionPolicy.SOURCE)
  public @interface TabIndicatorGravity {}

  /** Callback interface invoked when a tab's selection state changes. */
  //TODO: Remove this base listener when the widget migration is finished.
  public interface BaseOnTabSelectedListener<T extends Tab> {
    /**
     * Called when a tab enters the selected state.
     *
     * @param tab The tab that was selected
     */
    public void onTabSelected(T tab);

    /**
     * Called when a tab exits the selected state.
     *
     * @param tab The tab that was unselected
     */
    public void onTabUnselected(T tab);

    /**
     * Called when a tab that is already selected is chosen again by the user. Some applications may
     * use this action to return to the top level of a category.
     *
     * @param tab The tab that was reselected.
     */
    public void onTabReselected(T tab);
  }

  /** Callback interface invoked when a tab's selection state changes. */
  public interface OnTabSelectedListener extends BaseOnTabSelectedListener<Tab> {

  }

  private final ArrayList<Tab> tabs = new ArrayList<>();
  private Tab selectedTab;

  private final RectF tabViewContentBounds = new RectF();

  private final SlidingTabIndicator slidingTabIndicator;

  int tabPaddingStart;
  int tabPaddingTop;
  int tabPaddingEnd;
  int tabPaddingBottom;

  int tabTextAppearance;
  ColorStateList tabTextColors;
  ColorStateList tabIconTint;
  ColorStateList tabRippleColorStateList;
  @Nullable Drawable tabSelectedIndicator;

  android.graphics.PorterDuff.Mode tabIconTintMode;
  float tabTextSize;
  float tabTextMultiLineSize;

  final int tabBackgroundResId;

  int tabMaxWidth = Integer.MAX_VALUE;
  private final int requestedTabMinWidth;
  private final int requestedTabMaxWidth;
  private final int scrollableTabMinWidth;

  private int contentInsetStart;

  int tabGravity;
  int tabIndicatorAnimationDuration;
  @TabIndicatorGravity int tabIndicatorGravity;
  @Mode int mode;
  boolean inlineLabel;
  boolean tabIndicatorFullWidth;
  boolean unboundedRipple;

  private BaseOnTabSelectedListener selectedListener;
  private final ArrayList<BaseOnTabSelectedListener> selectedListeners = new ArrayList<>();
  private BaseOnTabSelectedListener currentVpSelectedListener;

  private ValueAnimator scrollAnimator;

  ViewPager viewPager;
  private PagerAdapter pagerAdapter;
  private DataSetObserver pagerAdapterObserver;
  private TabLayoutOnPageChangeListener pageChangeListener;
  private AdapterChangeListener adapterChangeListener;
  private boolean setupViewPagerImplicitly;

  // Pool we use as a simple RecyclerBin
  private final Pools.Pool<TabView> tabViewPool = new Pools.SimplePool<>(12);

  public TabLayout(Context context) {
    this(context, null);
  }

  public TabLayout(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.tabStyle);
  }

  public TabLayout(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Disable the Scroll Bar
    setHorizontalScrollBarEnabled(false);

    // Add the TabStrip
    slidingTabIndicator = new SlidingTabIndicator(context);
    super.addView(
        slidingTabIndicator,
        0,
        new HorizontalScrollView.LayoutParams(
            LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT));

    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.TabLayout, defStyleAttr, R.style.Widget_Design_TabLayout);

    slidingTabIndicator.setSelectedIndicatorHeight(
        a.getDimensionPixelSize(R.styleable.TabLayout_tabIndicatorHeight, -1));
    slidingTabIndicator.setSelectedIndicatorColor(
        a.getColor(R.styleable.TabLayout_tabIndicatorColor, 0));
    setSelectedTabIndicator(
        MaterialResources.getDrawable(context, a, R.styleable.TabLayout_tabIndicator));
    setSelectedTabIndicatorGravity(
        a.getInt(R.styleable.TabLayout_tabIndicatorGravity, INDICATOR_GRAVITY_BOTTOM));
    setTabIndicatorFullWidth(a.getBoolean(R.styleable.TabLayout_tabIndicatorFullWidth, true));

    tabPaddingStart =
        tabPaddingTop =
            tabPaddingEnd =
                tabPaddingBottom = a.getDimensionPixelSize(R.styleable.TabLayout_tabPadding, 0);
    tabPaddingStart =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingStart, tabPaddingStart);
    tabPaddingTop = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingTop, tabPaddingTop);
    tabPaddingEnd = a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingEnd, tabPaddingEnd);
    tabPaddingBottom =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabPaddingBottom, tabPaddingBottom);

    tabTextAppearance =
        a.getResourceId(R.styleable.TabLayout_tabTextAppearance, R.style.TextAppearance_Design_Tab);

    // Text colors/sizes come from the text appearance first
    final TypedArray ta =
        context.obtainStyledAttributes(
            tabTextAppearance, android.support.v7.appcompat.R.styleable.TextAppearance);
    try {
      tabTextSize =
          ta.getDimensionPixelSize(
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textSize, 0);
      tabTextColors =
          MaterialResources.getColorStateList(
              context,
              ta,
              android.support.v7.appcompat.R.styleable.TextAppearance_android_textColor);
    } finally {
      ta.recycle();
    }

    if (a.hasValue(R.styleable.TabLayout_tabTextColor)) {
      // If we have an explicit text color set, use it instead
      tabTextColors =
          MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabTextColor);
    }

    if (a.hasValue(R.styleable.TabLayout_tabSelectedTextColor)) {
      // We have an explicit selected text color set, so we need to make merge it with the
      // current colors. This is exposed so that developers can use theme attributes to set
      // this (theme attrs in ColorStateLists are Lollipop+)
      final int selected = a.getColor(R.styleable.TabLayout_tabSelectedTextColor, 0);
      tabTextColors = createColorStateList(tabTextColors.getDefaultColor(), selected);
    }

    tabIconTint =
        MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabIconTint);
    tabIconTintMode =
        ViewUtils.parseTintMode(a.getInt(R.styleable.TabLayout_tabIconTintMode, -1), null);

    tabRippleColorStateList =
        MaterialResources.getColorStateList(context, a, R.styleable.TabLayout_tabRippleColor);

    tabIndicatorAnimationDuration =
        a.getInt(R.styleable.TabLayout_tabIndicatorAnimationDuration, ANIMATION_DURATION);

    requestedTabMinWidth =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabMinWidth, INVALID_WIDTH);
    requestedTabMaxWidth =
        a.getDimensionPixelSize(R.styleable.TabLayout_tabMaxWidth, INVALID_WIDTH);
    tabBackgroundResId = a.getResourceId(R.styleable.TabLayout_tabBackground, 0);
    contentInsetStart = a.getDimensionPixelSize(R.styleable.TabLayout_tabContentStart, 0);
    // noinspection WrongConstant
    mode = a.getInt(R.styleable.TabLayout_tabMode, MODE_FIXED);
    tabGravity = a.getInt(R.styleable.TabLayout_tabGravity, GRAVITY_FILL);
    inlineLabel = a.getBoolean(R.styleable.TabLayout_tabInlineLabel, false);
    unboundedRipple = a.getBoolean(R.styleable.TabLayout_tabUnboundedRipple, false);
    a.recycle();

    // TODO add attr for these
    final Resources res = getResources();
    tabTextMultiLineSize = res.getDimensionPixelSize(R.dimen.design_tab_text_size_2line);
    scrollableTabMinWidth = res.getDimensionPixelSize(R.dimen.design_tab_scrollable_min_width);

    // Now apply the tab mode and gravity
    applyModeAndGravity();
  }

  /**
   * Sets the tab indicator's color for the currently selected tab.
   *
   * @param color color to use for the indicator
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorColor
   */
  public void setSelectedTabIndicatorColor(@ColorInt int color) {
    slidingTabIndicator.setSelectedIndicatorColor(color);
  }

  /**
   * Sets the tab indicator's height for the currently selected tab. This method is deprecated. If
   * possible, set the intrinsic height directly on a custom indicator drawable passed to {@link
   * #setSelectedTabIndicator(Drawable)}.
   *
   * @param height height to use for the indicator in pixels
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorHeight
   */
  @Deprecated
  public void setSelectedTabIndicatorHeight(int height) {
    slidingTabIndicator.setSelectedIndicatorHeight(height);
  }

  /**
   * Set the scroll position of the tabs. This is useful for when the tabs are being displayed as
   * part of a scrolling container such as {@link android.support.v4.view.ViewPager}.
   *
   * <p>Calling this method does not update the selected tab, it is only used for drawing purposes.
   *
   * @param position current scroll position
   * @param positionOffset Value from [0, 1) indicating the offset from {@code position}.
   * @param updateSelectedText Whether to update the text's selected state.
   */
  public void setScrollPosition(int position, float positionOffset, boolean updateSelectedText) {
    setScrollPosition(position, positionOffset, updateSelectedText, true);
  }

  void setScrollPosition(
      int position,
      float positionOffset,
      boolean updateSelectedText,
      boolean updateIndicatorPosition) {
    final int roundedPosition = Math.round(position + positionOffset);
    if (roundedPosition < 0 || roundedPosition >= slidingTabIndicator.getChildCount()) {
      return;
    }

    // Set the indicator position, if enabled
    if (updateIndicatorPosition) {
      slidingTabIndicator.setIndicatorPositionFromTabPosition(position, positionOffset);
    }

    // Now update the scroll position, canceling any running animation
    if (scrollAnimator != null && scrollAnimator.isRunning()) {
      scrollAnimator.cancel();
    }
    scrollTo(calculateScrollXForTab(position, positionOffset), 0);

    // Update the 'selected state' view as we scroll, if enabled
    if (updateSelectedText) {
      setSelectedTabView(roundedPosition);
    }
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list. If this is the first
   * tab to be added it will become the selected tab.
   *
   * @param tab Tab to add
   */
  public void addTab(@NonNull Tab tab) {
    addTab(tab, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>. If this is the
   * first tab to be added it will become the selected tab.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   */
  public void addTab(@NonNull Tab tab, int position) {
    addTab(tab, position, tabs.isEmpty());
  }

  /**
   * Add a tab to this layout. The tab will be added at the end of the list.
   *
   * @param tab Tab to add
   * @param setSelected True if the added tab should become the selected tab.
   */
  public void addTab(@NonNull Tab tab, boolean setSelected) {
    addTab(tab, tabs.size(), setSelected);
  }

  /**
   * Add a tab to this layout. The tab will be inserted at <code>position</code>.
   *
   * @param tab The tab to add
   * @param position The new position of the tab
   * @param setSelected True if the added tab should become the selected tab.
   */
  public void addTab(@NonNull Tab tab, int position, boolean setSelected) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab belongs to a different TabLayout.");
    }
    configureTab(tab, position);
    addTabView(tab);

    if (setSelected) {
      tab.select();
    }
  }

  private void addTabFromItemView(@NonNull TabItem item) {
    final Tab tab = newTab();
    if (item.text != null) {
      tab.setText(item.text);
    }
    if (item.icon != null) {
      tab.setIcon(item.icon);
    }
    if (item.customLayout != 0) {
      tab.setCustomView(item.customLayout);
    }
    if (!TextUtils.isEmpty(item.getContentDescription())) {
      tab.setContentDescription(item.getContentDescription());
    }
    addTab(tab);
  }

  /**
   * @deprecated Use {@link #addOnTabSelectedListener(OnTabSelectedListener)} and {@link
   *     #removeOnTabSelectedListener(OnTabSelectedListener)}.
   */
  @Deprecated
  public void setOnTabSelectedListener(@Nullable BaseOnTabSelectedListener listener) {
    // The logic in this method emulates what we had before support for multiple
    // registered listeners.
    if (selectedListener != null) {
      removeOnTabSelectedListener(selectedListener);
    }
    // Update the deprecated field so that we can remove the passed listener the next
    // time we're called
    selectedListener = listener;
    if (listener != null) {
      addOnTabSelectedListener(listener);
    }
  }

  /**
   * Add a {@link TabLayout.OnTabSelectedListener} that will be invoked when tab selection changes.
   *
   * <p>Components that add a listener should take care to remove it when finished via {@link
   * #removeOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to add
   */
  public void addOnTabSelectedListener(@NonNull BaseOnTabSelectedListener listener) {
    if (!selectedListeners.contains(listener)) {
      selectedListeners.add(listener);
    }
  }

  /**
   * Remove the given {@link TabLayout.OnTabSelectedListener} that was previously added via {@link
   * #addOnTabSelectedListener(OnTabSelectedListener)}.
   *
   * @param listener listener to remove
   */
  public void removeOnTabSelectedListener(@NonNull BaseOnTabSelectedListener listener) {
    selectedListeners.remove(listener);
  }

  /** Remove all previously added {@link TabLayout.OnTabSelectedListener}s. */
  public void clearOnTabSelectedListeners() {
    selectedListeners.clear();
  }

  /**
   * Create and return a new {@link Tab}. You need to manually add this using {@link #addTab(Tab)}
   * or a related method.
   *
   * @return A new Tab
   * @see #addTab(Tab)
   */
  @NonNull
  public Tab newTab() {
    Tab tab = createTabFromPool();
    tab.parent = this;
    tab.view = createTabView(tab);
    return tab;
  }

  //TODO: remove this method and just create the final field after the widget migration
  protected Tab createTabFromPool() {
    Tab tab = tabPool.acquire();
    if (tab == null) {
      tab = new Tab();
    }
    return tab;
  }

  //TODO: remove this method and just create the final field after the widget migration
  protected boolean releaseFromTabPool(Tab tab) {
    return tabPool.release(tab);
  }

  /**
   * Returns the number of tabs currently registered with the action bar.
   *
   * @return Tab count
   */
  public int getTabCount() {
    return tabs.size();
  }

  /** Returns the tab at the specified index. */
  @Nullable
  public Tab getTabAt(int index) {
    return (index < 0 || index >= getTabCount()) ? null : tabs.get(index);
  }

  /**
   * Returns the position of the current selected tab.
   *
   * @return selected tab position, or {@code -1} if there isn't a selected tab.
   */
  public int getSelectedTabPosition() {
    return selectedTab != null ? selectedTab.getPosition() : -1;
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param tab The tab to remove
   */
  public void removeTab(Tab tab) {
    if (tab.parent != this) {
      throw new IllegalArgumentException("Tab does not belong to this TabLayout.");
    }

    removeTabAt(tab.getPosition());
  }

  /**
   * Remove a tab from the layout. If the removed tab was selected it will be deselected and another
   * tab will be selected if present.
   *
   * @param position Position of the tab to remove
   */
  public void removeTabAt(int position) {
    final int selectedTabPosition = selectedTab != null ? selectedTab.getPosition() : 0;
    removeTabViewAt(position);

    final Tab removedTab = tabs.remove(position);
    if (removedTab != null) {
      removedTab.reset();
      releaseFromTabPool(removedTab);
    }

    final int newTabCount = tabs.size();
    for (int i = position; i < newTabCount; i++) {
      tabs.get(i).setPosition(i);
    }

    if (selectedTabPosition == position) {
      selectTab(tabs.isEmpty() ? null : tabs.get(Math.max(0, position - 1)));
    }
  }

  /** Remove all tabs from the action bar and deselect the current tab. */
  public void removeAllTabs() {
    // Remove all the views
    for (int i = slidingTabIndicator.getChildCount() - 1; i >= 0; i--) {
      removeTabViewAt(i);
    }

    for (final Iterator<Tab> i = tabs.iterator(); i.hasNext(); ) {
      final Tab tab = i.next();
      i.remove();
      tab.reset();
      releaseFromTabPool(tab);
    }

    selectedTab = null;
  }

  /**
   * Set the behavior mode for the Tabs in this layout. The valid input options are:
   *
   * <ul>
   *   <li>{@link #MODE_FIXED}: Fixed tabs display all tabs concurrently and are best used with
   *       content that benefits from quick pivots between tabs.
   *   <li>{@link #MODE_SCROLLABLE}: Scrollable tabs display a subset of tabs at any given moment,
   *       and can contain longer tab labels and a larger number of tabs. They are best used for
   *       browsing contexts in touch interfaces when users don’t need to directly compare the tab
   *       labels. This mode is commonly used with a {@link android.support.v4.view.ViewPager}.
   * </ul>
   *
   * @param mode one of {@link #MODE_FIXED} or {@link #MODE_SCROLLABLE}.
   * @attr ref android.support.design.R.styleable#TabLayout_tabMode
   */
  public void setTabMode(@Mode int mode) {
    if (mode != this.mode) {
      this.mode = mode;
      applyModeAndGravity();
    }
  }

  /**
   * Returns the current mode used by this {@link TabLayout}.
   *
   * @see #setTabMode(int)
   */
  @Mode
  public int getTabMode() {
    return mode;
  }

  /**
   * Set the gravity to use when laying out the tabs.
   *
   * @param gravity one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
   * @attr ref android.support.design.R.styleable#TabLayout_tabGravity
   */
  public void setTabGravity(@TabGravity int gravity) {
    if (tabGravity != gravity) {
      tabGravity = gravity;
      applyModeAndGravity();
    }
  }

  /**
   * The current gravity used for laying out tabs.
   *
   * @return one of {@link #GRAVITY_CENTER} or {@link #GRAVITY_FILL}.
   */
  @TabGravity
  public int getTabGravity() {
    return tabGravity;
  }

  /**
   * Set the indicator gravity used to align the tab selection indicator in the {@link TabLayout}.
   * You must set the indicator height via the custom indicator drawable's intrinsic height
   * (preferred), via the {@code tabIndicatorHeight} attribute (deprecated), or via {@link
   * #setSelectedTabIndicatorHeight(int)} (deprecated). Otherwise, the indicator will not be shown
   * unless gravity is set to {@link #INDICATOR_GRAVITY_STRETCH}, in which case it will ignore
   * indicator height and stretch across the entire height and width of the {@link TabLayout}. This
   * defaults to {@link #INDICATOR_GRAVITY_BOTTOM} if not set.
   *
   * @param indicatorGravity one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link
   *     #INDICATOR_GRAVITY_CENTER}, {@link #INDICATOR_GRAVITY_TOP}, or {@link
   *     #INDICATOR_GRAVITY_STRETCH}
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorGravity
   */
  public void setSelectedTabIndicatorGravity(@TabIndicatorGravity int indicatorGravity) {
    if (tabIndicatorGravity != indicatorGravity) {
      tabIndicatorGravity = indicatorGravity;
      ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }
  }

  /**
   * Get the current indicator gravity used to align the tab selection indicator in the {@link
   * TabLayout}.
   *
   * @return one of {@link #INDICATOR_GRAVITY_BOTTOM}, {@link #INDICATOR_GRAVITY_CENTER}, {@link
   *     #INDICATOR_GRAVITY_TOP}, or {@link #INDICATOR_GRAVITY_STRETCH}
   */
  @TabIndicatorGravity
  public int getTabIndicatorGravity() {
    return tabIndicatorGravity;
  }

  /**
   * Enable or disable option to fit the tab selection indicator to the full width of the tab item
   * rather than to the tab item's content.
   *
   * <p>Defaults to true. If set to false and the tab item has a text label, the selection indicator
   * width will be set to the width of the text label. If the tab item has no text label, but does
   * have an icon, the selection indicator width will be set to the icon. If the tab item has
   * neither of these, or if the calculated width is less than a minimum width value, the selection
   * indicator width will be set to the minimum width value.
   *
   * @param tabIndicatorFullWidth Whether or not to fit selection indicator width to full width of
   *     the tab item
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorFullWidth
   * @see #isTabIndicatorFullWidth()
   */
  public void setTabIndicatorFullWidth(boolean tabIndicatorFullWidth) {
    this.tabIndicatorFullWidth = tabIndicatorFullWidth;
    ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
  }

  /**
   * Get whether or not selection indicator width is fit to full width of the tab item, or fit to
   * the tab item's content.
   *
   * @return whether or not selection indicator width is fit to the full width of the tab item
   * @attr ref android.support.design.R.styleable#TabLayout_tabIndicatorFullWidth
   * @see #setTabIndicatorFullWidth(boolean)
   */
  public boolean isTabIndicatorFullWidth() {
    return tabIndicatorFullWidth;
  }

  /**
   * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
   * underneath tab icons.
   *
   * @see #isInlineLabel()
   * @attr ref android.support.design.R.styleable#TabLayout_tabInlineLabel
   */
  public void setInlineLabel(boolean inline) {
    if (inlineLabel != inline) {
      inlineLabel = inline;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateOrientation();
        }
      }
      applyModeAndGravity();
    }
  }

  /**
   * Set whether tab labels will be displayed inline with tab icons, or if they will be displayed
   * underneath tab icons.
   *
   * @param inlineResourceId Resource ID for boolean inline flag
   * @see #isInlineLabel()
   * @attr ref android.support.design.R.styleable#TabLayout_tabInlineLabel
   */
  public void setInlineLabelResource(@BoolRes int inlineResourceId) {
    setInlineLabel(getResources().getBoolean(inlineResourceId));
  }

  /**
   * Returns whether tab labels will be displayed inline with tab icons, or if they will be
   * displayed underneath tab icons.
   *
   * @see #setInlineLabel(boolean)
   * @attr ref android.support.design.R.styleable#TabLayout_tabInlineLabel
   */
  public boolean isInlineLabel() {
    return inlineLabel;
  }

  /**
   * Set whether this {@link TabLayout} will have an unbounded ripple effect or if ripple will be
   * bound to the tab item size.
   *
   * <p>Defaults to false.
   *
   * @see #hasUnboundedRipple()
   * @attr ref android.support.design.R.styleable#TabLayout_tabUnboundedRipple
   */
  public void setUnboundedRipple(boolean unboundedRipple) {
    if (this.unboundedRipple != unboundedRipple) {
      this.unboundedRipple = unboundedRipple;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  /**
   * Set whether this {@link TabLayout} will have an unbounded ripple effect or if ripple will be
   * bound to the tab item size. Defaults to false.
   *
   * @param unboundedRippleResourceId Resource ID for boolean unbounded ripple value
   * @see #hasUnboundedRipple()
   * @attr ref android.support.design.R.styleable#TabLayout_tabUnboundedRipple
   */
  public void setUnboundedRippleResource(@BoolRes int unboundedRippleResourceId) {
    setUnboundedRipple(getResources().getBoolean(unboundedRippleResourceId));
  }

  /**
   * Returns whether this {@link TabLayout} has an unbounded ripple effect, or if ripple is bound to
   * the tab item size.
   *
   * @see #setUnboundedRipple(boolean)
   * @attr ref android.support.design.R.styleable#TabLayout_tabUnboundedRipple
   */
  public boolean hasUnboundedRipple() {
    return unboundedRipple;
  }

  /**
   * Sets the text colors for the different states (normal, selected) used for the tabs.
   *
   * @see #getTabTextColors()
   */
  public void setTabTextColors(@Nullable ColorStateList textColor) {
    if (tabTextColors != textColor) {
      tabTextColors = textColor;
      updateAllTabs();
    }
  }

  /** Gets the text colors for the different states (normal, selected) used for the tabs. */
  @Nullable
  public ColorStateList getTabTextColors() {
    return tabTextColors;
  }

  /**
   * Sets the text colors for the different states (normal, selected) used for the tabs.
   *
   * @attr ref android.support.design.R.styleable#TabLayout_tabTextColor
   * @attr ref android.support.design.R.styleable#TabLayout_tabSelectedTextColor
   */
  public void setTabTextColors(int normalColor, int selectedColor) {
    setTabTextColors(createColorStateList(normalColor, selectedColor));
  }

  /**
   * Sets the icon tint for the different states (normal, selected) used for the tabs.
   *
   * @see #getTabIconTint()
   */
  public void setTabIconTint(@Nullable ColorStateList iconTint) {
    if (tabIconTint != iconTint) {
      tabIconTint = iconTint;
      updateAllTabs();
    }
  }

  /**
   * Sets the icon tint resource for the different states (normal, selected) used for the tabs.
   *
   * @param iconTintResourceId A color resource to use as icon tint.
   * @see #getTabIconTint()
   */
  public void setTabIconTintResource(@ColorRes int iconTintResourceId) {
    setTabIconTint(AppCompatResources.getColorStateList(getContext(), iconTintResourceId));
  }

  /** Gets the icon tint for the different states (normal, selected) used for the tabs. */
  @Nullable
  public ColorStateList getTabIconTint() {
    return tabIconTint;
  }

  /**
   * Returns the ripple color for this TabLayout.
   *
   * @return the color (or ColorStateList) used for the ripple
   * @see #setTabRippleColor(ColorStateList)
   */
  @Nullable
  public ColorStateList getTabRippleColor() {
    return tabRippleColorStateList;
  }

  /**
   * Sets the ripple color for this TabLayout.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
   * than a ripple.
   *
   * @param color color (or ColorStateList) to use for the ripple
   * @attr ref android.support.design.R.styleable#TabLayout_tabRippleColor
   * @see #getTabRippleColor()
   */
  public void setTabRippleColor(@Nullable ColorStateList color) {
    if (tabRippleColorStateList != color) {
      tabRippleColorStateList = color;
      for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
        View child = slidingTabIndicator.getChildAt(i);
        if (child instanceof TabView) {
          ((TabView) child).updateBackgroundDrawable(getContext());
        }
      }
    }
  }

  /**
   * Sets the ripple color resource for this TabLayout.
   *
   * <p>When running on devices with KitKat or below, we draw this color as a filled overlay rather
   * than a ripple.
   *
   * @param tabRippleColorResourceId A color resource to use as ripple color.
   * @see #getTabRippleColor()
   */
  public void setTabRippleColorResource(@ColorRes int tabRippleColorResourceId) {
    setTabRippleColor(AppCompatResources.getColorStateList(getContext(), tabRippleColorResourceId));
  }

  /**
   * Returns the selection indicator drawable for this TabLayout.
   *
   * @return The drawable used as the tab selection indicator, if set.
   * @see #setSelectedTabIndicator(Drawable)
   * @see #setSelectedTabIndicator(int)
   */
  @Nullable
  public Drawable getTabSelectedIndicator() {
    return tabSelectedIndicator;
  }

  /**
   * Sets the selection indicator for this TabLayout. By default, this is a line along the bottom of
   * the tab. If {@code tabIndicatorColor} is specified via the TabLayout's style or via {@link
   * #setSelectedTabIndicatorColor(int)} the selection indicator will be tinted that color.
   * Otherwise, it will use the colors specified in the drawable.
   *
   * @param tabSelectedIndicator A drawable to use as the selected tab indicator.
   * @see #setSelectedTabIndicatorColor(int)
   * @see #setSelectedTabIndicator(int)
   */
  public void setSelectedTabIndicator(@Nullable Drawable tabSelectedIndicator) {
    if (this.tabSelectedIndicator != tabSelectedIndicator) {
      this.tabSelectedIndicator = tabSelectedIndicator;
      ViewCompat.postInvalidateOnAnimation(slidingTabIndicator);
    }
  }

  /**
   * Sets the drawable resource to use as the selection indicator for this TabLayout. By default,
   * this is a line along the bottom of the tab. If {@code tabIndicatorColor} is specified via the
   * TabLayout's style or via {@link #setSelectedTabIndicatorColor(int)} the selection indicator
   * will be tinted that color. Otherwise, it will use the colors specified in the drawable.
   *
   * @param tabSelectedIndicatorResourceId A drawable resource to use as the selected tab indicator.
   * @see #setSelectedTabIndicatorColor(int)
   * @see #setSelectedTabIndicator(Drawable)
   */
  public void setSelectedTabIndicator(@DrawableRes int tabSelectedIndicatorResourceId) {
    if (tabSelectedIndicatorResourceId != 0) {
      setSelectedTabIndicator(
          AppCompatResources.getDrawable(getContext(), tabSelectedIndicatorResourceId));
    } else {
      setSelectedTabIndicator(null);
    }
  }

  /**
   * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewPager}.
   *
   * <p>This is the same as calling {@link #setupWithViewPager(ViewPager, boolean)} with
   * auto-refresh enabled.
   *
   * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
   */
  public void setupWithViewPager(@Nullable ViewPager viewPager) {
    setupWithViewPager(viewPager, true);
  }

  /**
   * The one-stop shop for setting up this {@link TabLayout} with a {@link ViewPager}.
   *
   * <p>This method will link the given ViewPager and this TabLayout together so that changes in one
   * are automatically reflected in the other. This includes scroll state changes and clicks. The
   * tabs displayed in this layout will be populated from the ViewPager adapter's page titles.
   *
   * <p>If {@code autoRefresh} is {@code true}, any changes in the {@link PagerAdapter} will trigger
   * this layout to re-populate itself from the adapter's titles.
   *
   * <p>If the given ViewPager is non-null, it needs to already have a {@link PagerAdapter} set.
   *
   * @param viewPager the ViewPager to link to, or {@code null} to clear any previous link
   * @param autoRefresh whether this layout should refresh its contents if the given ViewPager's
   *     content changes
   */
  public void setupWithViewPager(@Nullable final ViewPager viewPager, boolean autoRefresh) {
    setupWithViewPager(viewPager, autoRefresh, false);
  }

  private void setupWithViewPager(
      @Nullable final ViewPager viewPager, boolean autoRefresh, boolean implicitSetup) {
    if (this.viewPager != null) {
      // If we've already been setup with a ViewPager, remove us from it
      if (pageChangeListener != null) {
        this.viewPager.removeOnPageChangeListener(pageChangeListener);
      }
      if (adapterChangeListener != null) {
        this.viewPager.removeOnAdapterChangeListener(adapterChangeListener);
      }
    }

    if (currentVpSelectedListener != null) {
      // If we already have a tab selected listener for the ViewPager, remove it
      removeOnTabSelectedListener(currentVpSelectedListener);
      currentVpSelectedListener = null;
    }

    if (viewPager != null) {
      this.viewPager = viewPager;

      // Add our custom OnPageChangeListener to the ViewPager
      if (pageChangeListener == null) {
        pageChangeListener = new TabLayoutOnPageChangeListener(this);
      }
      pageChangeListener.reset();
      viewPager.addOnPageChangeListener(pageChangeListener);

      // Now we'll add a tab selected listener to set ViewPager's current item
      currentVpSelectedListener = new ViewPagerOnTabSelectedListener(viewPager);
      addOnTabSelectedListener(currentVpSelectedListener);

      final PagerAdapter adapter = viewPager.getAdapter();
      if (adapter != null) {
        // Now we'll populate ourselves from the pager adapter, adding an observer if
        // autoRefresh is enabled
        setPagerAdapter(adapter, autoRefresh);
      }

      // Add a listener so that we're notified of any adapter changes
      if (adapterChangeListener == null) {
        adapterChangeListener = new AdapterChangeListener();
      }
      adapterChangeListener.setAutoRefresh(autoRefresh);
      viewPager.addOnAdapterChangeListener(adapterChangeListener);

      // Now update the scroll position to match the ViewPager's current item
      setScrollPosition(viewPager.getCurrentItem(), 0f, true);
    } else {
      // We've been given a null ViewPager so we need to clear out the internal state,
      // listeners and observers
      this.viewPager = null;
      setPagerAdapter(null, false);
    }

    setupViewPagerImplicitly = implicitSetup;
  }

  /**
   * @deprecated Use {@link #setupWithViewPager(ViewPager)} to link a TabLayout with a ViewPager
   *     together. When that method is used, the TabLayout will be automatically updated when the
   *     {@link PagerAdapter} is changed.
   */
  @Deprecated
  public void setTabsFromPagerAdapter(@Nullable final PagerAdapter adapter) {
    setPagerAdapter(adapter, false);
  }

  @Override
  public boolean shouldDelayChildPressedState() {
    // Only delay the pressed state if the tabs can scroll
    return getTabScrollRange() > 0;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    if (viewPager == null) {
      // If we don't have a ViewPager already, check if our parent is a ViewPager to
      // setup with it automatically
      final ViewParent vp = getParent();
      if (vp instanceof ViewPager) {
        // If we have a ViewPager parent and we've been added as part of its decor, let's
        // assume that we should automatically setup to display any titles
        setupWithViewPager((ViewPager) vp, true, true);
      }
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();

    if (setupViewPagerImplicitly) {
      // If we've been setup with a ViewPager implicitly, let's clear out any listeners, etc
      setupWithViewPager(null);
      setupViewPagerImplicitly = false;
    }
  }

  private int getTabScrollRange() {
    return Math.max(
        0, slidingTabIndicator.getWidth() - getWidth() - getPaddingLeft() - getPaddingRight());
  }

  void setPagerAdapter(@Nullable final PagerAdapter adapter, final boolean addObserver) {
    if (pagerAdapter != null && pagerAdapterObserver != null) {
      // If we already have a PagerAdapter, unregister our observer
      pagerAdapter.unregisterDataSetObserver(pagerAdapterObserver);
    }

    pagerAdapter = adapter;

    if (addObserver && adapter != null) {
      // Register our observer on the new adapter
      if (pagerAdapterObserver == null) {
        pagerAdapterObserver = new PagerAdapterObserver();
      }
      adapter.registerDataSetObserver(pagerAdapterObserver);
    }

    // Finally make sure we reflect the new adapter
    populateFromPagerAdapter();
  }

  void populateFromPagerAdapter() {
    removeAllTabs();

    if (pagerAdapter != null) {
      final int adapterCount = pagerAdapter.getCount();
      for (int i = 0; i < adapterCount; i++) {
        addTab(newTab().setText(pagerAdapter.getPageTitle(i)), false);
      }

      // Make sure we reflect the currently set ViewPager item
      if (viewPager != null && adapterCount > 0) {
        final int curItem = viewPager.getCurrentItem();
        if (curItem != getSelectedTabPosition() && curItem < getTabCount()) {
          selectTab(getTabAt(curItem));
        }
      }
    }
  }

  private void updateAllTabs() {
    for (int i = 0, z = tabs.size(); i < z; i++) {
      tabs.get(i).updateView();
    }
  }

  private TabView createTabView(@NonNull final Tab tab) {
    TabView tabView = tabViewPool != null ? tabViewPool.acquire() : null;
    if (tabView == null) {
      tabView = new TabView(getContext());
    }
    tabView.setTab(tab);
    tabView.setFocusable(true);
    tabView.setMinimumWidth(getTabMinWidth());
    return tabView;
  }

  private void configureTab(Tab tab, int position) {
    tab.setPosition(position);
    tabs.add(position, tab);

    final int count = tabs.size();
    for (int i = position + 1; i < count; i++) {
      tabs.get(i).setPosition(i);
    }
  }

  private void addTabView(Tab tab) {
    final TabView tabView = tab.view;
    slidingTabIndicator.addView(tabView, tab.getPosition(), createLayoutParamsForTabs());
  }

  @Override
  public void addView(View child) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    addViewInternal(child);
  }

  private void addViewInternal(final View child) {
    if (child instanceof TabItem) {
      addTabFromItemView((TabItem) child);
    } else {
      throw new IllegalArgumentException("Only TabItem instances can be added to TabLayout");
    }
  }

  private LinearLayout.LayoutParams createLayoutParamsForTabs() {
    final LinearLayout.LayoutParams lp =
        new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT);
    updateTabViewLayoutParams(lp);
    return lp;
  }

  private void updateTabViewLayoutParams(LinearLayout.LayoutParams lp) {
    if (mode == MODE_FIXED && tabGravity == GRAVITY_FILL) {
      lp.width = 0;
      lp.weight = 1;
    } else {
      lp.width = LinearLayout.LayoutParams.WRAP_CONTENT;
      lp.weight = 0;
    }
  }

  @Dimension(unit = Dimension.PX)
  int dpToPx(@Dimension(unit = Dimension.DP) int dps) {
    return Math.round(getResources().getDisplayMetrics().density * dps);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    // Draw tab background layer for each tab item
    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
      View tabView = slidingTabIndicator.getChildAt(i);
      if (tabView instanceof TabView) {
        ((TabView) tabView).drawBackground(canvas);
      }
    }

    super.onDraw(canvas);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    // If we have a MeasureSpec which allows us to decide our height, try and use the default
    // height
    final int idealHeight = dpToPx(getDefaultHeight()) + getPaddingTop() + getPaddingBottom();
    switch (MeasureSpec.getMode(heightMeasureSpec)) {
      case MeasureSpec.AT_MOST:
        heightMeasureSpec =
            MeasureSpec.makeMeasureSpec(
                Math.min(idealHeight, MeasureSpec.getSize(heightMeasureSpec)), MeasureSpec.EXACTLY);
        break;
      case MeasureSpec.UNSPECIFIED:
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(idealHeight, MeasureSpec.EXACTLY);
        break;
      default:
        break;
    }

    final int specWidth = MeasureSpec.getSize(widthMeasureSpec);
    if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.UNSPECIFIED) {
      // If we don't have an unspecified width spec, use the given size to calculate
      // the max tab width
      tabMaxWidth =
          requestedTabMaxWidth > 0
              ? requestedTabMaxWidth
              : specWidth - dpToPx(TAB_MIN_WIDTH_MARGIN);
    }

    // Now super measure itself using the (possibly) modified height spec
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);

    if (getChildCount() == 1) {
      // If we're in fixed mode then we need to make the tab strip is the same width as us
      // so we don't scroll
      final View child = getChildAt(0);
      boolean remeasure = false;

      switch (mode) {
        case MODE_SCROLLABLE:
          // We only need to resize the child if it's smaller than us. This is similar
          // to fillViewport
          remeasure = child.getMeasuredWidth() < getMeasuredWidth();
          break;
        case MODE_FIXED:
          // Resize the child so that it doesn't scroll
          remeasure = child.getMeasuredWidth() != getMeasuredWidth();
          break;
      }

      if (remeasure) {
        // Re-measure the child with a widthSpec set to be exactly our measure width
        int childHeightMeasureSpec =
            getChildMeasureSpec(
                heightMeasureSpec,
                getPaddingTop() + getPaddingBottom(),
                child.getLayoutParams().height);
        int childWidthMeasureSpec =
            MeasureSpec.makeMeasureSpec(getMeasuredWidth(), MeasureSpec.EXACTLY);
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);
      }
    }
  }

  private void removeTabViewAt(int position) {
    final TabView view = (TabView) slidingTabIndicator.getChildAt(position);
    slidingTabIndicator.removeViewAt(position);
    if (view != null) {
      view.reset();
      tabViewPool.release(view);
    }
    requestLayout();
  }

  private void animateToTab(int newPosition) {
    if (newPosition == Tab.INVALID_POSITION) {
      return;
    }

    if (getWindowToken() == null
        || !ViewCompat.isLaidOut(this)
        || slidingTabIndicator.childrenNeedLayout()) {
      // If we don't have a window token, or we haven't been laid out yet just draw the new
      // position now
      setScrollPosition(newPosition, 0f, true);
      return;
    }

    final int startScrollX = getScrollX();
    final int targetScrollX = calculateScrollXForTab(newPosition, 0);

    if (startScrollX != targetScrollX) {
      ensureScrollAnimator();

      scrollAnimator.setIntValues(startScrollX, targetScrollX);
      scrollAnimator.start();
    }

    // Now animate the indicator
    slidingTabIndicator.animateIndicatorToPosition(newPosition, tabIndicatorAnimationDuration);
  }

  private void ensureScrollAnimator() {
    if (scrollAnimator == null) {
      scrollAnimator = new ValueAnimator();
      scrollAnimator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
      scrollAnimator.setDuration(tabIndicatorAnimationDuration);
      scrollAnimator.addUpdateListener(
          new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animator) {
              scrollTo((int) animator.getAnimatedValue(), 0);
            }
          });
    }
  }

  void setScrollAnimatorListener(ValueAnimator.AnimatorListener listener) {
    ensureScrollAnimator();
    scrollAnimator.addListener(listener);
  }

  private void setSelectedTabView(int position) {
    final int tabCount = slidingTabIndicator.getChildCount();
    if (position < tabCount) {
      for (int i = 0; i < tabCount; i++) {
        final View child = slidingTabIndicator.getChildAt(i);
        child.setSelected(i == position);
        child.setActivated(i == position);
      }
    }
  }

  void selectTab(Tab tab) {
    selectTab(tab, true);
  }

  void selectTab(final Tab tab, boolean updateIndicator) {
    final Tab currentTab = selectedTab;

    if (currentTab == tab) {
      if (currentTab != null) {
        dispatchTabReselected(tab);
        animateToTab(tab.getPosition());
      }
    } else {
      final int newPosition = tab != null ? tab.getPosition() : Tab.INVALID_POSITION;
      if (updateIndicator) {
        if ((currentTab == null || currentTab.getPosition() == Tab.INVALID_POSITION)
            && newPosition != Tab.INVALID_POSITION) {
          // If we don't currently have a tab, just draw the indicator
          setScrollPosition(newPosition, 0f, true);
        } else {
          animateToTab(newPosition);
        }
        if (newPosition != Tab.INVALID_POSITION) {
          setSelectedTabView(newPosition);
        }
      }
      if (currentTab != null) {
        dispatchTabUnselected(currentTab);
      }
      selectedTab = tab;
      if (tab != null) {
        dispatchTabSelected(tab);
      }
    }
  }

  private void dispatchTabSelected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabSelected(tab);
    }
  }

  private void dispatchTabUnselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabUnselected(tab);
    }
  }

  private void dispatchTabReselected(@NonNull final Tab tab) {
    for (int i = selectedListeners.size() - 1; i >= 0; i--) {
      selectedListeners.get(i).onTabReselected(tab);
    }
  }

  private int calculateScrollXForTab(int position, float positionOffset) {
    if (mode == MODE_SCROLLABLE) {
      final View selectedChild = slidingTabIndicator.getChildAt(position);
      final View nextChild =
          position + 1 < slidingTabIndicator.getChildCount()
              ? slidingTabIndicator.getChildAt(position + 1)
              : null;
      final int selectedWidth = selectedChild != null ? selectedChild.getWidth() : 0;
      final int nextWidth = nextChild != null ? nextChild.getWidth() : 0;

      // base scroll amount: places center of tab in center of parent
      int scrollBase = selectedChild.getLeft() + (selectedWidth / 2) - (getWidth() / 2);
      // offset amount: fraction of the distance between centers of tabs
      int scrollOffset = (int) ((selectedWidth + nextWidth) * 0.5f * positionOffset);

      return (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR)
          ? scrollBase + scrollOffset
          : scrollBase - scrollOffset;
    }
    return 0;
  }

  private void applyModeAndGravity() {
    int paddingStart = 0;
    if (mode == MODE_SCROLLABLE) {
      // If we're scrollable, or fixed at start, inset using padding
      paddingStart = Math.max(0, contentInsetStart - tabPaddingStart);
    }
    ViewCompat.setPaddingRelative(slidingTabIndicator, paddingStart, 0, 0, 0);

    switch (mode) {
      case MODE_FIXED:
        slidingTabIndicator.setGravity(Gravity.CENTER_HORIZONTAL);
        break;
      case MODE_SCROLLABLE:
        slidingTabIndicator.setGravity(GravityCompat.START);
        break;
    }

    updateTabViews(true);
  }

  void updateTabViews(final boolean requestLayout) {
    for (int i = 0; i < slidingTabIndicator.getChildCount(); i++) {
      View child = slidingTabIndicator.getChildAt(i);
      child.setMinimumWidth(getTabMinWidth());
      updateTabViewLayoutParams((LinearLayout.LayoutParams) child.getLayoutParams());
      if (requestLayout) {
        child.requestLayout();
      }
    }
  }

  /** A tab in this layout. Instances can be created via {@link #newTab()}. */
  //TODO: make class final after the widget migration is finished
  public static class Tab {

    /**
     * An invalid position for a tab.
     *
     * @see #getPosition()
     */
    public static final int INVALID_POSITION = -1;

    private Object tag;
    private Drawable icon;
    private CharSequence text;
    private CharSequence contentDesc;
    private int position = INVALID_POSITION;
    private View customView;

    //TODO: make package private after the widget migration is finished
    public TabLayout parent;
    //TODO: make package private after the widget migration is finished
    public TabView view;

    //TODO: make package private constructor after the widget migration is finished
    public Tab() {
      // Private constructor
    }

    /** @return This Tab's tag object. */
    @Nullable
    public Object getTag() {
      return tag;
    }

    /**
     * Give this Tab an arbitrary object to hold for later use.
     *
     * @param tag Object to store
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setTag(@Nullable Object tag) {
      this.tag = tag;
      return this;
    }

    /**
     * Returns the custom view used for this tab.
     *
     * @see #setCustomView(View)
     * @see #setCustomView(int)
     */
    @Nullable
    public View getCustomView() {
      return customView;
    }

    /**
     * Set a custom view to be used for this tab.
     *
     * <p>If the provided view contains a {@link TextView} with an ID of {@link android.R.id#text1}
     * then that will be updated with the value given to {@link #setText(CharSequence)}. Similarly,
     * if this layout contains an {@link ImageView} with ID {@link android.R.id#icon} then it will
     * be updated with the value given to {@link #setIcon(Drawable)}.
     *
     * @param view Custom view to be used as a tab.
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setCustomView(@Nullable View view) {
      customView = view;
      updateView();
      return this;
    }

    /**
     * Set a custom view to be used for this tab.
     *
     * <p>If the inflated layout contains a {@link TextView} with an ID of {@link
     * android.R.id#text1} then that will be updated with the value given to {@link
     * #setText(CharSequence)}. Similarly, if this layout contains an {@link ImageView} with ID
     * {@link android.R.id#icon} then it will be updated with the value given to {@link
     * #setIcon(Drawable)}.
     *
     * @param resId A layout resource to inflate and use as a custom tab view
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setCustomView(@LayoutRes int resId) {
      final LayoutInflater inflater = LayoutInflater.from(view.getContext());
      return setCustomView(inflater.inflate(resId, view, false));
    }

    /**
     * Return the icon associated with this tab.
     *
     * @return The tab's icon
     */
    @Nullable
    public Drawable getIcon() {
      return icon;
    }

    /**
     * Return the current position of this tab in the action bar.
     *
     * @return Current position, or {@link #INVALID_POSITION} if this tab is not currently in the
     *     action bar.
     */
    public int getPosition() {
      return position;
    }

    void setPosition(int position) {
      this.position = position;
    }

    /**
     * Return the text of this tab.
     *
     * @return The tab's text
     */
    @Nullable
    public CharSequence getText() {
      return text;
    }

    /**
     * Set the icon displayed on this tab.
     *
     * @param icon The drawable to use as an icon
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setIcon(@Nullable Drawable icon) {
      this.icon = icon;
      updateView();
      return this;
    }

    /**
     * Set the icon displayed on this tab.
     *
     * @param resId A resource ID referring to the icon that should be displayed
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setIcon(@DrawableRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setIcon(AppCompatResources.getDrawable(parent.getContext(), resId));
    }

    /**
     * Set the text displayed on this tab. Text may be truncated if there is not room to display the
     * entire string.
     *
     * @param text The text to display
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setText(@Nullable CharSequence text) {
      this.text = text;
      updateView();
      return this;
    }

    /**
     * Set the text displayed on this tab. Text may be truncated if there is not room to display the
     * entire string.
     *
     * @param resId A resource ID referring to the text that should be displayed
     * @return The current instance for call chaining
     */
    @NonNull
    public Tab setText(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setText(parent.getResources().getText(resId));
    }

    /** Select this tab. Only valid if the tab has been added to the action bar. */
    public void select() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      parent.selectTab(this);
    }

    /** Returns true if this tab is currently selected. */
    public boolean isSelected() {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return parent.getSelectedTabPosition() == position;
    }

    /**
     * Set a description of this tab's content for use in accessibility support. If no content
     * description is provided the title will be used.
     *
     * @param resId A resource ID referring to the description text
     * @return The current instance for call chaining
     * @see #setContentDescription(CharSequence)
     * @see #getContentDescription()
     */
    @NonNull
    public Tab setContentDescription(@StringRes int resId) {
      if (parent == null) {
        throw new IllegalArgumentException("Tab not attached to a TabLayout");
      }
      return setContentDescription(parent.getResources().getText(resId));
    }

    /**
     * Set a description of this tab's content for use in accessibility support. If no content
     * description is provided the title will be used.
     *
     * @param contentDesc Description of this tab's content
     * @return The current instance for call chaining
     * @see #setContentDescription(int)
     * @see #getContentDescription()
     */
    @NonNull
    public Tab setContentDescription(@Nullable CharSequence contentDesc) {
      this.contentDesc = contentDesc;
      updateView();
      return this;
    }

    /**
     * Gets a brief description of this tab's content for use in accessibility support.
     *
     * @return Description of this tab's content
     * @see #setContentDescription(CharSequence)
     * @see #setContentDescription(int)
     */
    @Nullable
    public CharSequence getContentDescription() {
      return contentDesc;
    }

    void updateView() {
      if (view != null) {
        view.update();
      }
    }

    void reset() {
      parent = null;
      view = null;
      tag = null;
      icon = null;
      text = null;
      contentDesc = null;
      position = INVALID_POSITION;
      customView = null;
    }
  }

  class TabView extends LinearLayout {
    private Tab tab;
    private TextView textView;
    private ImageView iconView;

    private View customView;
    private TextView customTextView;
    private ImageView customIconView;
    @Nullable private Drawable baseBackgroundDrawable;

    private int defaultMaxLines = 2;

    public TabView(Context context) {
      super(context);
      updateBackgroundDrawable(context);
      ViewCompat.setPaddingRelative(
          this, tabPaddingStart, tabPaddingTop, tabPaddingEnd, tabPaddingBottom);
      setGravity(Gravity.CENTER);
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      setClickable(true);
      ViewCompat.setPointerIcon(
          this, PointerIconCompat.getSystemIcon(getContext(), PointerIconCompat.TYPE_HAND));
    }

    private void updateBackgroundDrawable(Context context) {
      if (tabBackgroundResId != 0) {
        baseBackgroundDrawable = AppCompatResources.getDrawable(context, tabBackgroundResId);
        if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
          baseBackgroundDrawable.setState(getDrawableState());
        }
      } else {
        baseBackgroundDrawable = null;
      }

      Drawable background;
      Drawable contentDrawable = new GradientDrawable();
      ((GradientDrawable) contentDrawable).setColor(Color.TRANSPARENT);

      if (tabRippleColorStateList != null) {
        GradientDrawable maskDrawable = new GradientDrawable();
        // TODO: Find a non-hacky workaround for this. Currently on certain devices/versions,
        // LayerDrawable will draw a black background underneath any layer with a non-opaque color,
        // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
        maskDrawable.setCornerRadius(0.00001F);
        maskDrawable.setColor(Color.WHITE);

        ColorStateList rippleColor =
            RippleUtils.convertToRippleDrawableColor(tabRippleColorStateList);

        // TODO: Add support to RippleUtils.compositeRippleColorStateList for different ripple color
        // for selected items vs non-selected items
        if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
          background =
              new RippleDrawable(
                  rippleColor,
                  unboundedRipple ? null : contentDrawable,
                  unboundedRipple ? null : maskDrawable);
        } else {
          Drawable rippleDrawable = DrawableCompat.wrap(maskDrawable);
          DrawableCompat.setTintList(rippleDrawable, rippleColor);
          background = new LayerDrawable(new Drawable[] {contentDrawable, rippleDrawable});
        }
      } else {
        background = contentDrawable;
      }
      ViewCompat.setBackground(this, background);
      TabLayout.this.invalidate();
    }

    /**
     * Draw the background drawable specified by tabBackground attribute onto the canvas provided.
     * This method will draw the background to the full bounds of this TabView. We provide a
     * separate method for drawing this background rather than just setting this background on the
     * TabView so that we can control when this background gets drawn. This allows us to draw the
     * tab background underneath the TabLayout selection indicator, and then draw the TabLayout
     * content (icons + labels) on top of the selection indicator.
     *
     * @param canvas canvas to draw the background on
     */
    private void drawBackground(Canvas canvas) {
      if (baseBackgroundDrawable != null) {
        baseBackgroundDrawable.setBounds(getLeft(), getTop(), getRight(), getBottom());
        baseBackgroundDrawable.draw(canvas);
      }
    }

    @Override
    protected void drawableStateChanged() {
      super.drawableStateChanged();
      boolean changed = false;
      int[] state = getDrawableState();
      if (baseBackgroundDrawable != null && baseBackgroundDrawable.isStateful()) {
        changed |= baseBackgroundDrawable.setState(state);
      }

      if (changed) {
        invalidate();
        TabLayout.this.invalidate(); // Invalidate TabLayout, which draws mBaseBackgroundDrawable
      }
    }

    @Override
    public boolean performClick() {
      final boolean handled = super.performClick();

      if (tab != null) {
        if (!handled) {
          playSoundEffect(SoundEffectConstants.CLICK);
        }
        tab.select();
        return true;
      } else {
        return handled;
      }
    }

    @Override
    public void setSelected(final boolean selected) {
      final boolean changed = isSelected() != selected;

      super.setSelected(selected);

      if (changed && selected && Build.VERSION.SDK_INT < 16) {
        // Pre-JB we need to manually send the TYPE_VIEW_SELECTED event
        sendAccessibilityEvent(AccessibilityEvent.TYPE_VIEW_SELECTED);
      }

      // Always dispatch this to the child views, regardless of whether the value has
      // changed
      if (textView != null) {
        textView.setSelected(selected);
      }
      if (iconView != null) {
        iconView.setSelected(selected);
      }
      if (customView != null) {
        customView.setSelected(selected);
      }
    }

    @Override
    public void onInitializeAccessibilityEvent(AccessibilityEvent event) {
      super.onInitializeAccessibilityEvent(event);
      // This view masquerades as an action bar tab.
      event.setClassName(ActionBar.Tab.class.getName());
    }

    @TargetApi(14)
    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
      super.onInitializeAccessibilityNodeInfo(info);
      // This view masquerades as an action bar tab.
      info.setClassName(ActionBar.Tab.class.getName());
    }

    @Override
    public void onMeasure(final int origWidthMeasureSpec, final int origHeightMeasureSpec) {
      final int specWidthSize = MeasureSpec.getSize(origWidthMeasureSpec);
      final int specWidthMode = MeasureSpec.getMode(origWidthMeasureSpec);
      final int maxWidth = getTabMaxWidth();

      final int widthMeasureSpec;
      final int heightMeasureSpec = origHeightMeasureSpec;

      if (maxWidth > 0 && (specWidthMode == MeasureSpec.UNSPECIFIED || specWidthSize > maxWidth)) {
        // If we have a max width and a given spec which is either unspecified or
        // larger than the max width, update the width spec using the same mode
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(tabMaxWidth, MeasureSpec.AT_MOST);
      } else {
        // Else, use the original width spec
        widthMeasureSpec = origWidthMeasureSpec;
      }

      // Now lets measure
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      // We need to switch the text size based on whether the text is spanning 2 lines or not
      if (textView != null) {
        float textSize = tabTextSize;
        int maxLines = defaultMaxLines;

        if (iconView != null && iconView.getVisibility() == VISIBLE) {
          // If the icon view is being displayed, we limit the text to 1 line
          maxLines = 1;
        } else if (textView != null && textView.getLineCount() > 1) {
          // Otherwise when we have text which wraps we reduce the text size
          textSize = tabTextMultiLineSize;
        }

        final float curTextSize = textView.getTextSize();
        final int curLineCount = textView.getLineCount();
        final int curMaxLines = TextViewCompat.getMaxLines(textView);

        if (textSize != curTextSize || (curMaxLines >= 0 && maxLines != curMaxLines)) {
          // We've got a new text size and/or max lines...
          boolean updateTextView = true;

          if (mode == MODE_FIXED && textSize > curTextSize && curLineCount == 1) {
            // If we're in fixed mode, going up in text size and currently have 1 line
            // then it's very easy to get into an infinite recursion.
            // To combat that we check to see if the change in text size
            // will cause a line count change. If so, abort the size change and stick
            // to the smaller size.
            final Layout layout = textView.getLayout();
            if (layout == null
                || approximateLineWidth(layout, 0, textSize)
                    > getMeasuredWidth() - getPaddingLeft() - getPaddingRight()) {
              updateTextView = false;
            }
          }

          if (updateTextView) {
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, textSize);
            textView.setMaxLines(maxLines);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
          }
        }
      }
    }

    void setTab(@Nullable final Tab tab) {
      if (tab != this.tab) {
        this.tab = tab;
        update();
      }
    }

    void reset() {
      setTab(null);
      setSelected(false);
    }

    final void update() {
      final Tab tab = this.tab;
      final View custom = tab != null ? tab.getCustomView() : null;
      if (custom != null) {
        final ViewParent customParent = custom.getParent();
        if (customParent != this) {
          if (customParent != null) {
            ((ViewGroup) customParent).removeView(custom);
          }
          addView(custom);
        }
        customView = custom;
        if (this.textView != null) {
          this.textView.setVisibility(GONE);
        }
        if (this.iconView != null) {
          this.iconView.setVisibility(GONE);
          this.iconView.setImageDrawable(null);
        }

        customTextView = custom.findViewById(android.R.id.text1);
        if (customTextView != null) {
          defaultMaxLines = TextViewCompat.getMaxLines(customTextView);
        }
        customIconView = custom.findViewById(android.R.id.icon);
      } else {
        // We do not have a custom view. Remove one if it already exists
        if (customView != null) {
          removeView(customView);
          customView = null;
        }
        customTextView = null;
        customIconView = null;
      }

      if (customView == null) {
        // If there isn't a custom view, we'll us our own in-built layouts
        if (this.iconView == null) {
          ImageView iconView =
              (ImageView)
                  LayoutInflater.from(getContext())
                      .inflate(R.layout.design_layout_tab_icon, this, false);
          addView(iconView, 0);
          this.iconView = iconView;
        }
        final Drawable icon =
            (tab != null && tab.getIcon() != null)
                ? DrawableCompat.wrap(tab.getIcon()).mutate()
                : null;
        if (icon != null) {
          DrawableCompat.setTintList(icon, tabIconTint);
          if (tabIconTintMode != null) {
            DrawableCompat.setTintMode(icon, tabIconTintMode);
          }
        }

        if (this.textView == null) {
          TextView textView =
              (TextView)
                  LayoutInflater.from(getContext())
                      .inflate(R.layout.design_layout_tab_text, this, false);
          addView(textView);
          this.textView = textView;
          defaultMaxLines = TextViewCompat.getMaxLines(this.textView);
        }
        TextViewCompat.setTextAppearance(this.textView, tabTextAppearance);
        if (tabTextColors != null) {
          this.textView.setTextColor(tabTextColors);
        }
        updateTextAndIcon(this.textView, this.iconView);
      } else {
        // Else, we'll see if there is a TextView or ImageView present and update them
        if (customTextView != null || customIconView != null) {
          updateTextAndIcon(customTextView, customIconView);
        }
      }

      // Finally update our selected state
      setSelected(tab != null && tab.isSelected());
    }

    final void updateOrientation() {
      setOrientation(inlineLabel ? HORIZONTAL : VERTICAL);
      if (customTextView != null || customIconView != null) {
        updateTextAndIcon(customTextView, customIconView);
      } else {
        updateTextAndIcon(textView, iconView);
      }
    }

    private void updateTextAndIcon(
        @Nullable final TextView textView, @Nullable final ImageView iconView) {
      final Drawable icon =
          (tab != null && tab.getIcon() != null)
              ? DrawableCompat.wrap(tab.getIcon()).mutate()
              : null;
      final CharSequence text = tab != null ? tab.getText() : null;
      final CharSequence contentDesc = tab != null ? tab.getContentDescription() : null;

      if (iconView != null) {
        if (icon != null) {
          iconView.setImageDrawable(icon);
          iconView.setVisibility(VISIBLE);
          setVisibility(VISIBLE);
        } else {
          iconView.setVisibility(GONE);
          iconView.setImageDrawable(null);
        }
        iconView.setContentDescription(contentDesc);
      }

      final boolean hasText = !TextUtils.isEmpty(text);
      if (textView != null) {
        if (hasText) {
          textView.setText(text);
          textView.setVisibility(VISIBLE);
          setVisibility(VISIBLE);
        } else {
          textView.setVisibility(GONE);
          textView.setText(null);
        }
        textView.setContentDescription(contentDesc);
      }

      if (iconView != null) {
        MarginLayoutParams lp = ((MarginLayoutParams) iconView.getLayoutParams());
        int iconMargin = 0;
        if (hasText && iconView.getVisibility() == VISIBLE) {
          // If we're showing both text and icon, add some margin bottom to the icon
          iconMargin = dpToPx(DEFAULT_GAP_TEXT_ICON);
        }
        if (inlineLabel) {
          if (iconMargin != MarginLayoutParamsCompat.getMarginEnd(lp)) {
            MarginLayoutParamsCompat.setMarginEnd(lp, iconMargin);
            lp.bottomMargin = 0;
            // Calls resolveLayoutParams(), necessary for layout direction
            iconView.setLayoutParams(lp);
            iconView.requestLayout();
          }
        } else {
          if (iconMargin != lp.bottomMargin) {
            lp.bottomMargin = iconMargin;
            MarginLayoutParamsCompat.setMarginEnd(lp, 0);
            // Calls resolveLayoutParams(), necessary for layout direction
            iconView.setLayoutParams(lp);
            iconView.requestLayout();
          }
        }
      }

      TooltipCompat.setTooltipText(this, hasText ? null : contentDesc);
    }

    /**
     * Calculates the width of the TabView's content.
     *
     * @return Width of the tab label, if present, or the width of the tab icon, if present. If tabs
     *     is in inline mode, returns the sum of both the icon and tab label widths.
     */
    private int getContentWidth() {
      boolean initialized = false;
      int left = 0;
      int right = 0;

      for (View view : new View[] {textView, iconView, customView}) {
        if (view != null && view.getVisibility() == View.VISIBLE) {
          left = initialized ? Math.min(left, view.getLeft()) : view.getLeft();
          right = initialized ? Math.max(right, view.getRight()) : view.getRight();
          initialized = true;
        }
      }

      return right - left;
    }

    public Tab getTab() {
      return tab;
    }

    /** Approximates a given lines width with the new provided text size. */
    private float approximateLineWidth(Layout layout, int line, float textSize) {
      return layout.getLineWidth(line) * (textSize / layout.getPaint().getTextSize());
    }
  }

  private class SlidingTabIndicator extends LinearLayout {
    private int selectedIndicatorHeight;
    private final Paint selectedIndicatorPaint;
    private final GradientDrawable defaultSelectionIndicator;

    int selectedPosition = -1;
    float selectionOffset;

    private int layoutDirection = -1;

    private int indicatorLeft = -1;
    private int indicatorRight = -1;

    private ValueAnimator indicatorAnimator;

    SlidingTabIndicator(Context context) {
      super(context);
      setWillNotDraw(false);
      selectedIndicatorPaint = new Paint();
      defaultSelectionIndicator = new GradientDrawable();
    }

    void setSelectedIndicatorColor(int color) {
      if (selectedIndicatorPaint.getColor() != color) {
        selectedIndicatorPaint.setColor(color);
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    void setSelectedIndicatorHeight(int height) {
      if (selectedIndicatorHeight != height) {
        selectedIndicatorHeight = height;
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    boolean childrenNeedLayout() {
      for (int i = 0, z = getChildCount(); i < z; i++) {
        final View child = getChildAt(i);
        if (child.getWidth() <= 0) {
          return true;
        }
      }
      return false;
    }

    void setIndicatorPositionFromTabPosition(int position, float positionOffset) {
      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        indicatorAnimator.cancel();
      }

      selectedPosition = position;
      selectionOffset = positionOffset;
      updateIndicatorPosition();
    }

    float getIndicatorPosition() {
      return selectedPosition + selectionOffset;
    }

    @Override
    public void onRtlPropertiesChanged(int layoutDirection) {
      super.onRtlPropertiesChanged(layoutDirection);

      // Workaround for a bug before Android M where LinearLayout did not re-layout itself when
      // layout direction changed
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        if (this.layoutDirection != layoutDirection) {
          requestLayout();
          this.layoutDirection = layoutDirection;
        }
      }
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);

      if (MeasureSpec.getMode(widthMeasureSpec) != MeasureSpec.EXACTLY) {
        // HorizontalScrollView will first measure use with UNSPECIFIED, and then with
        // EXACTLY. Ignore the first call since anything we do will be overwritten anyway
        return;
      }

      if (mode == MODE_FIXED && tabGravity == GRAVITY_CENTER) {
        final int count = getChildCount();

        // First we'll find the widest tab
        int largestTabWidth = 0;
        for (int i = 0, z = count; i < z; i++) {
          View child = getChildAt(i);
          if (child.getVisibility() == VISIBLE) {
            largestTabWidth = Math.max(largestTabWidth, child.getMeasuredWidth());
          }
        }

        if (largestTabWidth <= 0) {
          // If we don't have a largest child yet, skip until the next measure pass
          return;
        }

        final int gutter = dpToPx(FIXED_WRAP_GUTTER_MIN);
        boolean remeasure = false;

        if (largestTabWidth * count <= getMeasuredWidth() - gutter * 2) {
          // If the tabs fit within our width minus gutters, we will set all tabs to have
          // the same width
          for (int i = 0; i < count; i++) {
            final LinearLayout.LayoutParams lp = (LayoutParams) getChildAt(i).getLayoutParams();
            if (lp.width != largestTabWidth || lp.weight != 0) {
              lp.width = largestTabWidth;
              lp.weight = 0;
              remeasure = true;
            }
          }
        } else {
          // If the tabs will wrap to be larger than the width minus gutters, we need
          // to switch to GRAVITY_FILL
          tabGravity = GRAVITY_FILL;
          updateTabViews(false);
          remeasure = true;
        }

        if (remeasure) {
          // Now re-measure after our changes
          super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        }
      }
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
      super.onLayout(changed, l, t, r, b);

      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        // If we're currently running an animation, lets cancel it and start a
        // new animation with the remaining duration
        indicatorAnimator.cancel();
        final long duration = indicatorAnimator.getDuration();
        animateIndicatorToPosition(
            selectedPosition,
            Math.round((1f - indicatorAnimator.getAnimatedFraction()) * duration));
      } else {
        // If we've been layed out, update the indicator position
        updateIndicatorPosition();
      }
    }

    private void updateIndicatorPosition() {
      final View selectedTitle = getChildAt(selectedPosition);
      int left;
      int right;

      if (selectedTitle != null && selectedTitle.getWidth() > 0) {
        left = selectedTitle.getLeft();
        right = selectedTitle.getRight();

        if (!tabIndicatorFullWidth && selectedTitle instanceof TabView) {
          calculateTabViewContentBounds((TabView) selectedTitle, tabViewContentBounds);
          left = (int) tabViewContentBounds.left;
          right = (int) tabViewContentBounds.right;
        }

        if (selectionOffset > 0f && selectedPosition < getChildCount() - 1) {
          // Draw the selection partway between the tabs
          View nextTitle = getChildAt(selectedPosition + 1);
          int nextTitleLeft = nextTitle.getLeft();
          int nextTitleRight = nextTitle.getRight();

          if (!tabIndicatorFullWidth && nextTitle instanceof TabView) {
            calculateTabViewContentBounds((TabView) nextTitle, tabViewContentBounds);
            nextTitleLeft = (int) tabViewContentBounds.left;
            nextTitleRight = (int) tabViewContentBounds.right;
          }

          left = (int) (selectionOffset * nextTitleLeft + (1.0f - selectionOffset) * left);
          right = (int) (selectionOffset * nextTitleRight + (1.0f - selectionOffset) * right);
        }

      } else {
        left = right = -1;
      }

      setIndicatorPosition(left, right);
    }

    void setIndicatorPosition(int left, int right) {
      if (left != indicatorLeft || right != indicatorRight) {
        // If the indicator's left/right has changed, invalidate
        indicatorLeft = left;
        indicatorRight = right;
        ViewCompat.postInvalidateOnAnimation(this);
      }
    }

    void animateIndicatorToPosition(final int position, int duration) {
      if (indicatorAnimator != null && indicatorAnimator.isRunning()) {
        indicatorAnimator.cancel();
      }

      final View targetView = getChildAt(position);
      if (targetView == null) {
        // If we don't have a view, just update the position now and return
        updateIndicatorPosition();
        return;
      }

      int targetLeft = targetView.getLeft();
      int targetRight = targetView.getRight();

      if (!tabIndicatorFullWidth && targetView instanceof TabView) {
        calculateTabViewContentBounds((TabView) targetView, tabViewContentBounds);
        targetLeft = (int) tabViewContentBounds.left;
        targetRight = (int) tabViewContentBounds.right;
      }

      final int finalTargetLeft = targetLeft;
      final int finalTargetRight = targetRight;

      final int startLeft = indicatorLeft;
      final int startRight = indicatorRight;

      if (startLeft != finalTargetLeft || startRight != finalTargetRight) {
        ValueAnimator animator = indicatorAnimator = new ValueAnimator();
        animator.setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
        animator.setDuration(duration);
        animator.setFloatValues(0, 1);
        animator.addUpdateListener(
            new ValueAnimator.AnimatorUpdateListener() {
              @Override
              public void onAnimationUpdate(ValueAnimator animator) {
                final float fraction = animator.getAnimatedFraction();
                setIndicatorPosition(
                    AnimationUtils.lerp(startLeft, finalTargetLeft, fraction),
                    AnimationUtils.lerp(startRight, finalTargetRight, fraction));
              }
            });
        animator.addListener(
            new AnimatorListenerAdapter() {
              @Override
              public void onAnimationEnd(Animator animator) {
                selectedPosition = position;
                selectionOffset = 0f;
              }
            });
        animator.start();
      }
    }

    /**
     * Given a {@link TabView}, calculate the left and right bounds of its content.
     *
     * <p>If only text label is present, calculates the width of the text label. If only icon is
     * present, calculates the width of the icon. If both are present, the text label bounds take
     * precedence. If both are present and inline mode is enabled, the sum of the bounds of the both
     * the text label and icon are calculated. If neither are present or if the calculated
     * difference between the left and right bounds is less than 24dp, then left and right bounds
     * are adjusted such that the difference between them is equal to 24dp.
     *
     * @param tabView {@link TabView} for which to calculate left and right content bounds.
     */
    private void calculateTabViewContentBounds(TabView tabView, RectF contentBounds) {
      int tabViewContentWidth = tabView.getContentWidth();

      if (tabViewContentWidth < dpToPx(MIN_INDICATOR_WIDTH)) {
        tabViewContentWidth = dpToPx(MIN_INDICATOR_WIDTH);
      }

      int tabViewCenter = (tabView.getLeft() + tabView.getRight()) / 2;
      int contentLeftBounds = tabViewCenter - (tabViewContentWidth / 2);
      int contentRightBounds = tabViewCenter + (tabViewContentWidth / 2);

      contentBounds.set(contentLeftBounds, 0, contentRightBounds, 0);
    }

    @Override
    public void draw(Canvas canvas) {
      int indicatorHeight = 0;
      if (tabSelectedIndicator != null) {
        indicatorHeight = tabSelectedIndicator.getIntrinsicHeight();
      }
      if (selectedIndicatorHeight >= 0) {
        indicatorHeight = selectedIndicatorHeight;
      }

      int indicatorTop = 0;
      int indicatorBottom = 0;

      switch (tabIndicatorGravity) {
        case INDICATOR_GRAVITY_BOTTOM:
          indicatorTop = getHeight() - indicatorHeight;
          indicatorBottom = getHeight();
          break;
        case INDICATOR_GRAVITY_CENTER:
          indicatorTop = (getHeight() - indicatorHeight) / 2;
          indicatorBottom = (getHeight() + indicatorHeight) / 2;
          break;
        case INDICATOR_GRAVITY_TOP:
          indicatorTop = 0;
          indicatorBottom = indicatorHeight;
          break;
        case INDICATOR_GRAVITY_STRETCH:
          indicatorTop = 0;
          indicatorBottom = getHeight();
          break;
        default:
          break;
      }

      // Draw the selection indicator on top of tab item backgrounds
      if (indicatorLeft >= 0 && indicatorRight > indicatorLeft) {
        Drawable selectedIndicator;
        selectedIndicator =
            DrawableCompat.wrap(
                tabSelectedIndicator != null ? tabSelectedIndicator : defaultSelectionIndicator);
        selectedIndicator.setBounds(indicatorLeft, indicatorTop, indicatorRight, indicatorBottom);
        if (selectedIndicatorPaint != null) {
          if (VERSION.SDK_INT == VERSION_CODES.LOLLIPOP) {
            // Drawable doesn't implement setTint in API 21
            selectedIndicator.setColorFilter(
                selectedIndicatorPaint.getColor(), PorterDuff.Mode.SRC_IN);
          } else {
            DrawableCompat.setTint(selectedIndicator, selectedIndicatorPaint.getColor());
          }
        }
        selectedIndicator.draw(canvas);
      }

      // Draw the tab item contents (icon and label) on top of the background + indicator layers
      super.draw(canvas);
    }
  }

  private static ColorStateList createColorStateList(int defaultColor, int selectedColor) {
    final int[][] states = new int[2][];
    final int[] colors = new int[2];
    int i = 0;

    states[i] = SELECTED_STATE_SET;
    colors[i] = selectedColor;
    i++;

    // Default enabled state
    states[i] = EMPTY_STATE_SET;
    colors[i] = defaultColor;
    i++;

    return new ColorStateList(states, colors);
  }

  @Dimension(unit = Dimension.DP)
  private int getDefaultHeight() {
    boolean hasIconAndText = false;
    for (int i = 0, count = tabs.size(); i < count; i++) {
      Tab tab = tabs.get(i);
      if (tab != null && tab.getIcon() != null && !TextUtils.isEmpty(tab.getText())) {
        hasIconAndText = true;
        break;
      }
    }
    return (hasIconAndText && !inlineLabel) ? DEFAULT_HEIGHT_WITH_TEXT_ICON : DEFAULT_HEIGHT;
  }

  private int getTabMinWidth() {
    if (requestedTabMinWidth != INVALID_WIDTH) {
      // If we have been given a min width, use it
      return requestedTabMinWidth;
    }
    // Else, we'll use the default value
    return mode == MODE_SCROLLABLE ? scrollableTabMinWidth : 0;
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    // We don't care about the layout params of any views added to us, since we don't actually
    // add them. The only view we add is the SlidingTabStrip, which is done manually.
    // We return the default layout params so that we don't blow up if we're given a TabItem
    // without android:layout_* values.
    return generateDefaultLayoutParams();
  }

  int getTabMaxWidth() {
    return tabMaxWidth;
  }

  /**
   * A {@link ViewPager.OnPageChangeListener} class which contains the necessary calls back to the
   * provided {@link TabLayout} so that the tab position is kept in sync.
   *
   * <p>This class stores the provided TabLayout weakly, meaning that you can use {@link
   * ViewPager#addOnPageChangeListener(ViewPager.OnPageChangeListener)
   * addOnPageChangeListener(OnPageChangeListener)} without removing the listener and not cause a
   * leak.
   */
  public static class TabLayoutOnPageChangeListener implements ViewPager.OnPageChangeListener {
    private final WeakReference<TabLayout> tabLayoutRef;
    private int previousScrollState;
    private int scrollState;

    public TabLayoutOnPageChangeListener(TabLayout tabLayout) {
      tabLayoutRef = new WeakReference<>(tabLayout);
    }

    @Override
    public void onPageScrollStateChanged(final int state) {
      previousScrollState = scrollState;
      scrollState = state;
    }

    @Override
    public void onPageScrolled(
        final int position, final float positionOffset, final int positionOffsetPixels) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null) {
        // Only update the text selection if we're not settling, or we are settling after
        // being dragged
        final boolean updateText =
            scrollState != SCROLL_STATE_SETTLING || previousScrollState == SCROLL_STATE_DRAGGING;
        // Update the indicator if we're not settling after being idle. This is caused
        // from a setCurrentItem() call and will be handled by an animation from
        // onPageSelected() instead.
        final boolean updateIndicator =
            !(scrollState == SCROLL_STATE_SETTLING && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.setScrollPosition(position, positionOffset, updateText, updateIndicator);
      }
    }

    @Override
    public void onPageSelected(final int position) {
      final TabLayout tabLayout = tabLayoutRef.get();
      if (tabLayout != null
          && tabLayout.getSelectedTabPosition() != position
          && position < tabLayout.getTabCount()) {
        // Select the tab, only updating the indicator if we're not being dragged/settled
        // (since onPageScrolled will handle that).
        final boolean updateIndicator =
            scrollState == SCROLL_STATE_IDLE
                || (scrollState == SCROLL_STATE_SETTLING
                    && previousScrollState == SCROLL_STATE_IDLE);
        tabLayout.selectTab(tabLayout.getTabAt(position), updateIndicator);
      }
    }

    void reset() {
      previousScrollState = scrollState = SCROLL_STATE_IDLE;
    }
  }

  /**
   * A {@link TabLayout.OnTabSelectedListener} class which contains the necessary calls back to the
   * provided {@link ViewPager} so that the tab position is kept in sync.
   */
  public static class ViewPagerOnTabSelectedListener implements TabLayout.OnTabSelectedListener {
    private final ViewPager viewPager;

    public ViewPagerOnTabSelectedListener(ViewPager viewPager) {
      this.viewPager = viewPager;
    }

    @Override
    public void onTabSelected(TabLayout.Tab tab) {
      viewPager.setCurrentItem(tab.getPosition());
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {
      // No-op
    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {
      // No-op
    }
  }

  private class PagerAdapterObserver extends DataSetObserver {
    PagerAdapterObserver() {}

    @Override
    public void onChanged() {
      populateFromPagerAdapter();
    }

    @Override
    public void onInvalidated() {
      populateFromPagerAdapter();
    }
  }

  private class AdapterChangeListener implements ViewPager.OnAdapterChangeListener {
    private boolean autoRefresh;

    AdapterChangeListener() {}

    @Override
    public void onAdapterChanged(
        @NonNull ViewPager viewPager,
        @Nullable PagerAdapter oldAdapter,
        @Nullable PagerAdapter newAdapter) {
      if (TabLayout.this.viewPager == viewPager) {
        setPagerAdapter(newAdapter, autoRefresh);
      }
    }

    void setAutoRefresh(boolean autoRefresh) {
      this.autoRefresh = autoRefresh;
    }
  }
}
