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

import static android.view.ViewGroup.LayoutParams.MATCH_PARENT;
import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.navigationrail.NavigationRailView.DEFAULT_MENU_GRAVITY;
import static com.google.android.material.navigationrail.NavigationRailView.NO_ITEM_MINIMUM_HEIGHT;
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class NavigationRailMenuView extends NavigationBarMenuView {

  @Px private int itemMinimumHeight = NO_ITEM_MINIMUM_HEIGHT;
  @Px private int itemSpacing = 0;
  private final FrameLayout.LayoutParams layoutParams =
      new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);

  public NavigationRailMenuView(@NonNull Context context) {
    super(context);

    layoutParams.gravity = DEFAULT_MENU_GRAVITY;
    setLayoutParams(layoutParams);
    setItemActiveIndicatorResizeable(true);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
    int visibleContentItemCount = getCurrentVisibleContentItemCount();

    int measuredHeight;
    if (visibleContentItemCount > 1
        && isShifting(getLabelVisibilityMode(), visibleContentItemCount)) {
      measuredHeight =
          measureShiftingChildHeights(widthMeasureSpec, maxHeight, visibleContentItemCount);
    } else {
      measuredHeight =
          measureSharedChildHeights(widthMeasureSpec, maxHeight, visibleContentItemCount, null);
    }

    // Set view to use parent width, but wrap all item heights
    setMeasuredDimension(
        MeasureSpec.getSize(widthMeasureSpec),
        View.resolveSizeAndState(measuredHeight, heightMeasureSpec, /* childMeasuredState= */ 0));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    int visibleCount = 0;
    int childrenHeight = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        childrenHeight += child.getMeasuredHeight();
        visibleCount += 1;
      }
    }
    int spacing =
        visibleCount <= 1
            ? 0
            : max(0, min((getMeasuredHeight() - childrenHeight) / (visibleCount - 1), itemSpacing));
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        int childHeight = child.getMeasuredHeight();
        child.layout(/* l= */ 0, used, width, childHeight + used);
        used += childHeight + spacing;
      }
    }
  }

  @Override
  @NonNull
  protected NavigationBarItemView createNavigationBarItemView(@NonNull Context context) {
    return new NavigationRailItemView(context);
  }

  private int makeSharedHeightSpec(int parentWidthSpec, int maxHeight, int shareCount) {
    int maxAvailable = maxHeight / max(1, shareCount);
    // If the navigation rail has a min item height specified, make each item that height.
    // Otherwise, use the width of the rail as the min item height.
    int minHeight =
        itemMinimumHeight != NO_ITEM_MINIMUM_HEIGHT
            ? itemMinimumHeight
            : MeasureSpec.getSize(parentWidthSpec);
    return MeasureSpec.makeMeasureSpec(min(minHeight, maxAvailable), MeasureSpec.UNSPECIFIED);
  }

  private int measureShiftingChildHeights(int widthMeasureSpec, int maxHeight, int shareCount) {
    int selectedViewHeight = 0;

    View selectedView = getChildAt(getSelectedItemPosition());
    if (selectedView != null) {
      int childHeightSpec = makeSharedHeightSpec(widthMeasureSpec, maxHeight, shareCount);
      selectedViewHeight = measureChildHeight(selectedView, widthMeasureSpec, childHeightSpec);
      maxHeight -= selectedViewHeight;
      --shareCount;
    }

    return selectedViewHeight
        + measureSharedChildHeights(
            widthMeasureSpec, maxHeight, shareCount, selectedView);
  }

  private int measureSharedChildHeights(
      int widthMeasureSpec, int maxHeight, int shareCount, View selectedView) {
    int subheaderHeightSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.UNSPECIFIED);
    int childCount = getChildCount();
    int totalHeight = 0;
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (!(child instanceof NavigationBarItemView)) {
        int subheaderHeight = measureChildHeight(child, widthMeasureSpec, subheaderHeightSpec);
        maxHeight -= subheaderHeight;
        totalHeight += subheaderHeight;
      }
    }
    maxHeight = max(maxHeight, 0);
    int childHeightSpec;
    if (selectedView == null) {
      childHeightSpec = makeSharedHeightSpec(widthMeasureSpec, maxHeight, shareCount);
    } else {
      // Use the same height for the unselected views, so the items do not have different heights
      // This may cause the last time to overflow and get cropped, but the developer is expected to
      // ensure that there is enough height for the rail or place it inside scroll view.
      childHeightSpec =
          MeasureSpec.makeMeasureSpec(selectedView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
    }

    int visibleChildCount = 0;

    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == VISIBLE) {
        visibleChildCount += 1;
      }
      // Subheaders are already measured in total height
      if (child instanceof NavigationBarItemView && child != selectedView) {
        totalHeight += measureChildHeight(child, widthMeasureSpec, childHeightSpec);
      }
    }

    return totalHeight + max(0, visibleChildCount - 1) * itemSpacing;
  }

  private int measureChildHeight(View child, int widthMeasureSpec, int heightMeasureSpec) {
    child.measure(widthMeasureSpec, heightMeasureSpec);
    if (child.getVisibility() != GONE) {
      return child.getMeasuredHeight();
    }

    return 0;
  }

  void setMenuGravity(int gravity) {
    if (layoutParams.gravity != gravity) {
      layoutParams.gravity = gravity;
      setLayoutParams(layoutParams);
    }
  }

  int getMenuGravity() {
    return layoutParams.gravity;
  }

  public void setItemMinimumHeight(@Px int minHeight) {
    if (this.itemMinimumHeight != minHeight) {
      this.itemMinimumHeight = minHeight;
      requestLayout();
    }
  }

  @Px
  public int getItemMinimumHeight() {
    return this.itemMinimumHeight;
  }

  public void setItemSpacing(@Px int spacing) {
    if (this.itemSpacing != spacing) {
      this.itemSpacing = spacing;
      requestLayout();
    }
  }

  @Px
  public int getItemSpacing() {
    return this.itemSpacing;
  }
}
