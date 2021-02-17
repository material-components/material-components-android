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
import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class NavigationRailMenuView extends NavigationBarMenuView {

  private final FrameLayout.LayoutParams layoutParams =
      new FrameLayout.LayoutParams(MATCH_PARENT, WRAP_CONTENT);

  public NavigationRailMenuView(@NonNull Context context) {
    super(context);

    layoutParams.gravity = DEFAULT_MENU_GRAVITY;
    setLayoutParams(layoutParams);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int maxHeight = MeasureSpec.getSize(heightMeasureSpec);
    int visibleCount = getMenu().getVisibleItems().size();

    int measuredHeight;
    if (visibleCount > 1 && isShifting(getLabelVisibilityMode(), visibleCount)) {
      measuredHeight = measureShiftingChildHeights(widthMeasureSpec, maxHeight, visibleCount);
    } else {
      measuredHeight = measureSharedChildHeights(widthMeasureSpec, maxHeight, visibleCount, null);
    }

    // Set view to use parent width, but wrap all item heights
    int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
    setMeasuredDimension(
        View.resolveSizeAndState(parentWidth, widthMeasureSpec, /* childMeasuredState= */ 0),
        View.resolveSizeAndState(measuredHeight, heightMeasureSpec, /* childMeasuredState= */ 0));
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        int childHeight = child.getMeasuredHeight();
        child.layout(/* l= */ 0, used, width, childHeight + used);
        used += childHeight;
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
    return MeasureSpec.makeMeasureSpec(
        min(MeasureSpec.getSize(parentWidthSpec), maxAvailable), MeasureSpec.UNSPECIFIED);
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
        + measureSharedChildHeights(widthMeasureSpec, maxHeight, shareCount, selectedView);
  }

  private int measureSharedChildHeights(
      int widthMeasureSpec, int maxHeight, int shareCount, View selectedView) {
    int childHeightSpec = makeSharedHeightSpec(widthMeasureSpec, maxHeight, shareCount);
    if (selectedView == null) {
      childHeightSpec = makeSharedHeightSpec(widthMeasureSpec, maxHeight, shareCount);
    } else {
      // Use the same height for the unselected views, so the items do not have different heights
      // This may cause the last time to overflow and get cropped, but the developer is expected to
      // ensure that there is enough height for the rail or place it inside scroll view.
      childHeightSpec =
          MeasureSpec.makeMeasureSpec(selectedView.getMeasuredHeight(), MeasureSpec.UNSPECIFIED);
    }

    int childCount = getChildCount();
    int totalHeight = 0;
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child != selectedView) {
        totalHeight += measureChildHeight(child, widthMeasureSpec, childHeightSpec);
      }
    }

    return totalHeight;
  }

  private int measureChildHeight(View child, int widthMeasureSpec, int heightMeasureSpec) {
    if (child.getVisibility() != GONE) {
      child.measure(widthMeasureSpec, heightMeasureSpec);
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

  boolean isTopGravity() {
    return (layoutParams.gravity & Gravity.VERTICAL_GRAVITY_MASK) == Gravity.TOP;
  }
}
