/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.tabs;

import static com.google.android.material.animation.AnimationUtils.lerp;

import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.Dimension;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.tabs.TabLayout.SlidingTabIndicator;
import com.google.android.material.tabs.TabLayout.TabView;

/**
 * A class used to manipulate the {@link SlidingTabIndicator}'s indicator {@link Drawable} at any
 * point at or between tabs.
 *
 * <p>By default, this class will size the indicator according to {@link
 * TabLayout#isTabIndicatorFullWidth()} and linearly move the indicator between tabs.
 *
 * <p>Subclasses can override {@link #setIndicatorBoundsForTab(TabLayout, View, Drawable)} and
 * {@link #setIndicatorBoundsForOffset(TabLayout, View, View, float, Drawable)} (TabLayout, View,
 * View, float, Drawable)} to define how the indicator should be drawn for a single tab or at any
 * point between two tabs.
 *
 * <p>Additionally, subclasses can use the provided helpers {@link
 * #calculateIndicatorWidthForTab(TabLayout, View)} and {@link
 * #calculateTabViewContentBounds(TabView, int)} to capture the bounds of the tab or tab's content.
 */
class TabIndicatorInterpolator {

  @Dimension(unit = Dimension.DP)
  private static final int MIN_INDICATOR_WIDTH = 24;

  /**
   * A helper method that calculates the bounds of a {@link TabView}'s content.
   *
   * <p>For width, if only text label is present, calculates the width of the text label. If only
   * icon is present, calculates the width of the icon. If both are present, the text label bounds
   * take precedence. If both are present and inline mode is enabled, the sum of the bounds of the
   * both the text label and icon are calculated. If neither are present or if the calculated
   * difference between the left and right bounds is less than 24dp, then left and right bounds are
   * adjusted such that the difference between them is equal to 24dp.
   *
   * <p>For height, this method calculates the combined height of the icon (if present) and label
   * (if present).
   *
   * @param tabView {@link TabView} for which to calculate left and right content bounds.
   * @param minWidth the min width between the returned RectF's left and right bounds. Useful if
   *     enforcing a min width of the indicator.
   */
  static RectF calculateTabViewContentBounds(
      @NonNull TabView tabView, @Dimension(unit = Dimension.DP) int minWidth) {
    int tabViewContentWidth = tabView.getContentWidth();
    int tabViewContentHeight = tabView.getContentHeight();
    int minWidthPx = (int) ViewUtils.dpToPx(tabView.getContext(), minWidth);

    if (tabViewContentWidth < minWidthPx) {
      tabViewContentWidth = minWidthPx;
    }

    int tabViewCenterX = (tabView.getLeft() + tabView.getRight()) / 2;
    int tabViewCenterY = (tabView.getTop() + tabView.getBottom()) / 2;
    int contentLeftBounds = tabViewCenterX - (tabViewContentWidth / 2);
    int contentTopBounds = tabViewCenterY - (tabViewContentHeight / 2);
    int contentRightBounds = tabViewCenterX + (tabViewContentWidth / 2);
    int contentBottomBounds = tabViewCenterY + (tabViewCenterX / 2);

    return new RectF(contentLeftBounds, contentTopBounds, contentRightBounds, contentBottomBounds);
  }

  /**
   * A helper method to calculate the left and right bounds of an indicator when {@code tab} is
   * selected.
   *
   * <p>This method accounts for {@link TabLayout#isTabIndicatorFullWidth()}'s value. If true, the
   * returned left and right bounds will span the full width of {@code tab}. If false, the returned
   * bounds will span the width of the {@code tab}'s content.
   *
   * @param tabLayout The tab's parent {@link TabLayout}
   * @param tab The view of the tab under which the indicator will be positioned
   * @return A {@link RectF} containing the left and right bounds that the indicator should span
   *     when {@code tab} is selected.
   */
  static RectF calculateIndicatorWidthForTab(TabLayout tabLayout, @Nullable View tab) {
    if (tab == null) {
      return new RectF();
    }

    // If the indicator should fit to the tab's content, calculate the content's widtd
    if (!tabLayout.isTabIndicatorFullWidth() && tab instanceof TabView) {
      return calculateTabViewContentBounds((TabView) tab, MIN_INDICATOR_WIDTH);
    }

    // Return the entire width of the tab
    return new RectF(tab.getLeft(), tab.getTop(), tab.getRight(), tab.getBottom());
  }

  /**
   * Called whenever {@code indicator} should be drawn to show the given {@code tab} as selected.
   *
   * <p>This method should update the bounds of indicator to be correctly positioned to indicate
   * {@code tab} as selected.
   *
   * @param tabLayout The {@link TabLayout} parent of the tab and indicator being drawn.
   * @param tab The tab that should be marked as selected
   * @param indicator The drawable to be drawn to indicate the selected tab. Update the drawable's
   *     bounds, color, etc to mark the given tab as selected.
   */
  void setIndicatorBoundsForTab(TabLayout tabLayout, View tab, @NonNull Drawable indicator) {
    RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, tab);
    indicator.setBounds(
        (int) startIndicator.left,
        indicator.getBounds().top,
        (int) startIndicator.right,
        indicator.getBounds().bottom);
  }

  /**
   * Called whenever the {@code indicator} should be drawn between two destinations and the {@link
   * Drawable}'s bounds should be changed. When {@code offset} is 0.0, the tab {@code indicator}
   * should indicate that the {@code startTitle} tab is selected. When {@code offset} is 1.0, the
   * tab {@code indicator} should indicate that the {@code endTitle} tab is selected. When offset is
   * between 0.0 and 1.0, the {@code indicator} is moving between the startTitle and endTitle and
   * the indicator should reflect this movement.
   *
   * <p>By default, this class will move the indicator linearly between tab destinations.
   *
   * @param tabLayout The TabLayout parent of the indicator being drawn.
   * @param startTitle The title that should be indicated as selected when offset is 0.0.
   * @param endTitle The title that should be indicated as selected when offset is 1.0.
   * @param offset The fraction between startTitle and endTitle where the indicator is for a given
   *     frame
   * @param indicator The drawable to be drawn to indicate the selected tab. Update the drawable's
   *     bounds, color, etc as {@code offset} changes to show the indicator in the correct position.
   */
  void setIndicatorBoundsForOffset(
      TabLayout tabLayout,
      View startTitle,
      View endTitle,
      @FloatRange(from = 0.0, to = 1.0) float offset,
      @NonNull Drawable indicator) {
    RectF startIndicator = calculateIndicatorWidthForTab(tabLayout, startTitle);
    // Linearly interpolate the indicator's position, using it's left and right bounds, between the
    // two destinations.
    RectF endIndicator = calculateIndicatorWidthForTab(tabLayout, endTitle);
    indicator.setBounds(
        lerp((int) startIndicator.left, (int) endIndicator.left, offset),
        indicator.getBounds().top,
        lerp((int) startIndicator.right, (int) endIndicator.right, offset),
        indicator.getBounds().bottom);
  }
}
