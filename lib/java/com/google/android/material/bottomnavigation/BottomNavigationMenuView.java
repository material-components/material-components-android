/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.bottomnavigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;
import com.google.android.material.navigation.NavigationBarView;
import java.util.ArrayList;
import java.util.List;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationMenuView extends NavigationBarMenuView {
  private final int inactiveItemMaxWidth;
  private final int inactiveItemMinWidth;
  private final int activeItemMaxWidth;
  private final int activeItemMinWidth;

  private boolean itemHorizontalTranslationEnabled;
  private final List<Integer> tempChildWidths = new ArrayList<>();

  public BottomNavigationMenuView(@NonNull Context context) {
    super(context);

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    setLayoutParams(params);

    final Resources res = getResources();
    inactiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width);
    inactiveItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width);
    activeItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);
    activeItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_min_width);

  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    // Use visible item count to calculate widths
    final int visibleCount = getCurrentVisibleContentItemCount();
    // Use total item counts to measure children
    final int totalCount = getChildCount();
    tempChildWidths.clear();

    int totalWidth = 0;
    int maxHeight = 0;

    int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
    final int heightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.AT_MOST);

    if (getItemIconGravity() == NavigationBarView.ITEM_ICON_GRAVITY_TOP) {
      if (isShifting(getLabelVisibilityMode(), visibleCount)
          && isItemHorizontalTranslationEnabled()) {
        final View activeChild = getChildAt(getSelectedItemPosition());
        int activeItemWidth = activeItemMinWidth;
        if (activeChild.getVisibility() != View.GONE) {
          // Do an AT_MOST measure pass on the active child to get its desired width, and resize the
          // active child view based on that width
          activeChild.measure(
              MeasureSpec.makeMeasureSpec(activeItemMaxWidth, MeasureSpec.AT_MOST), heightSpec);
          activeItemWidth = max(activeItemWidth, activeChild.getMeasuredWidth());
        }
        final int inactiveCount = visibleCount - (activeChild.getVisibility() != View.GONE ? 1 : 0);
        final int activeMaxAvailable = width - inactiveCount * inactiveItemMinWidth;
        final int activeWidth = min(activeMaxAvailable, min(activeItemWidth, activeItemMaxWidth));
        final int inactiveMaxAvailable =
            (width - activeWidth) / (inactiveCount == 0 ? 1 : inactiveCount);
        final int inactiveWidth = min(inactiveMaxAvailable, inactiveItemMaxWidth);
        int extra = width - activeWidth - inactiveWidth * inactiveCount;

        for (int i = 0; i < totalCount; i++) {
          int tempChildWidth = 0;
          if (getChildAt(i).getVisibility() != View.GONE) {
            tempChildWidth = (i == getSelectedItemPosition()) ? activeWidth : inactiveWidth;
            // Account for integer division which sometimes leaves some extra pixel spaces.
            // e.g. If the nav was 10px wide, and 3 children were measured to be 3px-3px-3px, there
            // would be a 1px gap somewhere, which this fills in.
            if (extra > 0) {
              tempChildWidth++;
              extra--;
            }
          }
          tempChildWidths.add(tempChildWidth);
        }
      } else {
        final int maxAvailable = width / (visibleCount == 0 ? 1 : visibleCount);
        final int childWidth = min(maxAvailable, activeItemMaxWidth);
        int extra = width - childWidth * visibleCount;
        for (int i = 0; i < totalCount; i++) {
          int tempChildWidth = 0;
          if (getChildAt(i).getVisibility() != View.GONE) {
            tempChildWidth = childWidth;
            if (extra > 0) {
              tempChildWidth++;
              extra--;
            }
          }
          tempChildWidths.add(tempChildWidth);
        }
      }

      for (int i = 0; i < totalCount; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() == GONE) {
          continue;
        }
        child.measure(
            MeasureSpec.makeMeasureSpec(tempChildWidths.get(i), MeasureSpec.EXACTLY), heightSpec);
        ViewGroup.LayoutParams params = child.getLayoutParams();
        params.width = child.getMeasuredWidth();
        totalWidth += child.getMeasuredWidth();
        maxHeight = max(maxHeight, child.getMeasuredHeight());
      }
    } else { // icon gravity is start
      int childCount = visibleCount == 0 ? 1 : visibleCount;
      // Calculate the min nav item width based on the item count and bar width according to
      // these rules:
      // 3 items: the items should occupy 60% of the bar's width
      // 4 items: the items should occupy 70% of the bar's width
      // 5 items: the items should occupy 80% of the bar's width
      // 6+ items: the items should occupy 90% of the bar's width
      int minChildWidth = Math.round((min((childCount + 3) / 10f, 0.9f) * width) / childCount);
      int maxChildWidth = Math.round((float) width / childCount);
      for (int i = 0; i < totalCount; i++) {
        View child = getChildAt(i);
        if (child.getVisibility() != View.GONE) {
          child.measure(
              MeasureSpec.makeMeasureSpec(maxChildWidth, MeasureSpec.AT_MOST), heightSpec);
          if (child.getMeasuredWidth() < minChildWidth) {
            child.measure(
                MeasureSpec.makeMeasureSpec(minChildWidth, MeasureSpec.EXACTLY), heightSpec);
          }
          totalWidth += child.getMeasuredWidth();
          maxHeight = max(maxHeight, child.getMeasuredHeight());
          }
        }
    }

    setMeasuredDimension(totalWidth, max(maxHeight, getSuggestedMinimumHeight()));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    final int height = bottom - top;
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      if (getLayoutDirection() == View.LAYOUT_DIRECTION_RTL) {
        child.layout(width - used - child.getMeasuredWidth(), 0, width - used, height);
      } else {
        child.layout(used, 0, child.getMeasuredWidth() + used, height);
      }
      used += child.getMeasuredWidth();
    }
  }

  /**
   * Sets whether the menu items horizontally translate on selection when the combined item widths
   * fill the screen.
   *
   * @param itemHorizontalTranslationEnabled whether the menu items horizontally translate on
   *     selection
   * @see #isItemHorizontalTranslationEnabled()
   */
  public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
    this.itemHorizontalTranslationEnabled = itemHorizontalTranslationEnabled;
  }

  /**
   * Returns whether the menu items horizontally translate on selection when the combined item
   * widths fill the screen.
   *
   * @return whether the menu items horizontally translate on selection
   * @see #setItemHorizontalTranslationEnabled(boolean)
   */
  public boolean isItemHorizontalTranslationEnabled() {
    return itemHorizontalTranslationEnabled;
  }

  @Override
  @NonNull
  protected NavigationBarItemView createNavigationBarItemView(@NonNull Context context) {
    return new BottomNavigationItemView(context);
  }
}
