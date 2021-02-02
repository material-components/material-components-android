/*
 * Copyright (C) 2021 The Android Open Source Project
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

package com.google.android.material.navigationrail;

import com.google.android.material.R;

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.Resources;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.navigation.NavigationBarView;

/**
 * Represents a standard navigation rail view for application. It is an implementation of <a
 * href="https://material.io/components/navigation-rail">material design navigation rail.</a>.
 *
 * <p>Navigation rails make it easy for users to explore and switch between top-level views in a
 * single tap. They should be placed at the side edge of large screen devices such as tablets, when
 * an application has three to seven top-level destinations.
 *
 * <p>The bar contents can be populated by specifying a menu resource file. Each menu item title,
 * icon and enabled state will be used for displaying navigation rail bar items. Menu items can also
 * be used for programmatically selecting which destination is currently active. It can be done
 * using {@code MenuItem#setChecked(true)}
 *
 * <pre>
 * layout resource file:
 * &lt;com.google.android.material.navigationrail.NavigationRailView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schema.android.com/apk/res/res-auto"
 *     android:id="@+id/navigation"
 *     android:layout_width="wrap_content"
 *     android:layout_height="match_parent"
 *     app:menu="@menu/my_navigation_items" /&gt;
 *
 * res/menu/my_navigation_items.xml:
 * &lt;menu xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *     &lt;item android:id="@+id/action_search"
 *          android:title="@string/menu_search"
 *          android:icon="@drawable/ic_search" /&gt;
 *     &lt;item android:id="@+id/action_settings"
 *          android:title="@string/menu_settings"
 *          android:icon="@drawable/ic_add" /&gt;
 *     &lt;item android:id="@+id/action_navigation"
 *          android:title="@string/menu_navigation"
 *          android:icon="@drawable/ic_action_navigation_menu" /&gt;
 * &lt;/menu&gt;
 * </pre>
 */
public class NavigationRailView extends NavigationBarView {

  static final int DEFAULT_MENU_GRAVITY = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
  static final int MAX_ITEM_COUNT = 7;
  private static final int DEFAULT_HEADER_GRAVITY = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

  private final int topMargin;
  @Nullable private View headerView;

  public NavigationRailView(@NonNull Context context) {
    this(context, null);
  }

  public NavigationRailView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.navigationRailStyle);
  }

  public NavigationRailView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, R.style.Widget_MaterialComponents_NavigationRailView);
  }

  public NavigationRailView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    final Resources res = getResources();
    topMargin = res.getDimensionPixelSize(R.dimen.mtrl_navigation_rail_margin);

    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    /* Custom attributes */
    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context, attrs, R.styleable.NavigationRailView, defStyleAttr, defStyleRes);

    int headerLayoutRes = attributes.getResourceId(R.styleable.NavigationRailView_headerLayout, 0);
    if (headerLayoutRes != 0) {
      addHeaderView(headerLayoutRes);
    }

    setMenuGravity(
        attributes.getInt(R.styleable.NavigationRailView_menuGravity, DEFAULT_MENU_GRAVITY));
    attributes.recycle();
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int minWidthSpec = makeMinWidthSpec(widthMeasureSpec);
    super.onMeasure(minWidthSpec, heightMeasureSpec);

    if (isHeaderViewVisible()) {
      int maxMenuHeight = getMeasuredHeight() - headerView.getMeasuredHeight() - topMargin;
      int menuHeightSpec = MeasureSpec.makeMeasureSpec(maxMenuHeight, MeasureSpec.AT_MOST);
      measureChild(getNavigationRailMenuView(), minWidthSpec, menuHeightSpec);
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    NavigationRailMenuView menuView = getNavigationRailMenuView();
    int offsetY = 0;
    if (isHeaderViewVisible()) {
      int usedTop = headerView.getBottom() + topMargin;
      int menuTop = menuView.getTop();
      if (menuTop < usedTop) {
        offsetY = usedTop - menuTop;
      }
    } else if (menuView.isTopGravity()) {
      offsetY = topMargin;
    }

    if (offsetY > 0) {
      menuView.layout(
          menuView.getLeft(),
          menuView.getTop() + offsetY,
          menuView.getRight(),
          menuView.getBottom() + offsetY);
    }
  }

  /**
   * Adds the specified {@link View} layout resource, to appear at the top of the {@link
   * NavigationRailView}. If the view already has a header view attached to it, it will be removed
   * first.
   *
   * @param layoutRes the unique resource identifier to the layout that should be attached.
   * @see #addHeaderView(View)
   * @see #removeHeaderView()
   * @see #getHeaderView()
   */
  public void addHeaderView(@LayoutRes int layoutRes) {
    addHeaderView(LayoutInflater.from(getContext()).inflate(layoutRes, this, false));
  }

  /**
   * Adds the specified {@link View} if any, to appear at the top of the {@link NavigationRailView}.
   * If the view already has a header view attached to it, it will be removed first.
   *
   * @param headerView reference to the {@link View} that should be attached.
   * @see #addHeaderView(int)
   * @see #removeHeaderView()
   * @see #getHeaderView()
   */
  public void addHeaderView(@NonNull View headerView) {
    removeHeaderView();
    this.headerView = headerView;

    FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);
    params.gravity = DEFAULT_HEADER_GRAVITY;
    params.topMargin = topMargin;
    addView(headerView, /* index= */ 0, params);
  }

  /**
   * Returns reference to the header view if any, that is currently attached the {@link
   * NavigationRailView}.
   *
   * @see #addHeaderView(int)
   * @see #addHeaderView(View)
   * @see #removeHeaderView()
   */
  @Nullable
  public View getHeaderView() {
    return headerView;
  }

  /**
   * Removes the current header view if any, from the {@link NavigationRailView}.
   *
   * @see #addHeaderView(int)
   * @see #addHeaderView(View)
   * @see #getHeaderView()
   */
  public void removeHeaderView() {
    if (headerView != null) {
      removeView(headerView);
      headerView = null;
    }
  }

  /** Sets how destinations in the menu view will be grouped. */
  public void setMenuGravity(int gravity) {
    getNavigationRailMenuView().setMenuGravity(gravity);
  }

  /** Gets the current gravity setting for how destinations in the menu view will be grouped. */
  public int getMenuGravity() {
    return getNavigationRailMenuView().getMenuGravity();
  }

  @Override
  public int getMaxItemCount() {
    return MAX_ITEM_COUNT;
  }

  private NavigationRailMenuView getNavigationRailMenuView() {
    return (NavigationRailMenuView) getMenuView();
  }

  @Override
  @NonNull
  protected NavigationRailMenuView createNavigationBarMenuView(@NonNull Context context) {
    return new NavigationRailMenuView(context);
  }

  /**
   * Sets a listener that will be notified when a navigation rail item is selected. This listener
   * will also be notified when the currently selected item is reselected, unless an {@link
   * OnNavigationItemReselectedListener} has also been set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemReselectedListener(OnNavigationItemReselectedListener)
   */
  public void setOnNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    setOnItemSelectedListener(listener);
  }

  /**
   * Sets a listener that will be notified when the currently selected navigation rail item is
   * reselected. This does not require an {@link OnNavigationItemSelectedListener} to be set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener)
   */
  public void setOnNavigationItemReselectedListener(
      @Nullable OnNavigationItemReselectedListener listener) {
    setOnItemReselectedListener(listener);
  }

  /** Listener for handling selection events on bottom navigation items. */
  public interface OnNavigationItemSelectedListener extends OnItemSelectedListener {}

  /** Listener for handling reselection events on bottom navigation items. */
  public interface OnNavigationItemReselectedListener extends OnItemReselectedListener {}

  private int makeMinWidthSpec(int measureSpec) {
    int minWidth = getSuggestedMinimumWidth();
    if (MeasureSpec.getMode(measureSpec) != MeasureSpec.EXACTLY && minWidth > 0) {
      minWidth += getPaddingLeft() + getPaddingRight();

      return MeasureSpec.makeMeasureSpec(
          min(MeasureSpec.getSize(measureSpec), minWidth), MeasureSpec.EXACTLY);
    }

    return measureSpec;
  }

  private boolean isHeaderViewVisible() {
    return headerView != null && headerView.getVisibility() != View.GONE;
  }
}
