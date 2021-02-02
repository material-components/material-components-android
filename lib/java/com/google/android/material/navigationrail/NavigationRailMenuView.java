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

import static android.view.ViewGroup.LayoutParams.WRAP_CONTENT;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.navigationrail.NavigationRailView.DEFAULT_MENU_GRAVITY;
import static java.lang.Math.min;

import android.content.Context;
import androidx.appcompat.view.menu.MenuBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class NavigationRailMenuView extends NavigationBarMenuView {

  private final FrameLayout.LayoutParams layoutParams =
      new FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT);

  public NavigationRailMenuView(@NonNull Context context) {
    super(context);

    layoutParams.gravity = DEFAULT_MENU_GRAVITY;
    setLayoutParams(layoutParams);
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    int childHeightSpec = makeSharedHeightSpec(widthMeasureSpec, heightMeasureSpec);

    int childCount = getChildCount();
    int maxWidth = 0;
    int totalHeight = 0;
    for (int i = 0; i < childCount; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() != GONE) {
        child.measure(widthMeasureSpec, childHeightSpec);
        ViewGroup.LayoutParams params = child.getLayoutParams();
        params.width = child.getMeasuredWidth();
        params.height = child.getMeasuredHeight();
        totalHeight += params.height;
        if (params.width > maxWidth) {
          maxWidth = params.width;
        }
      }
    }

    // Set view to use a fixed width, but wrap all item heights
    setMeasuredDimension(
        View.resolveSizeAndState(
            maxWidth,
            MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY),
            /* childMeasuredState= */ 0),
        View.resolveSizeAndState(
            totalHeight,
            MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY),
            /* childMeasuredState= */ 0));
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

  private int makeSharedHeightSpec(int parentWidthSpec, int parentHeightSpec) {
    MenuBuilder menu = getMenu();
    int visibleCount = menu.getVisibleItems().size();
    int maxHeight = MeasureSpec.getSize(parentHeightSpec);
    int maxAvailable = maxHeight / (visibleCount == 0 ? 1 : visibleCount);

    return MeasureSpec.makeMeasureSpec(
        min(MeasureSpec.getSize(parentWidthSpec), maxAvailable), MeasureSpec.EXACTLY);
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
