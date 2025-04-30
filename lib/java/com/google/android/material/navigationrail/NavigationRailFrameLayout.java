/*
 * Copyright (C) 2024 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static java.lang.Math.max;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * A {@link FrameLayout} implementation that lays out its children such that there is no vertical
 * overlap.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationRailFrameLayout extends FrameLayout {

  int paddingTop = 0;
  boolean scrollingEnabled = false;

  public NavigationRailFrameLayout(@NonNull Context context) {
    super(context);
  }

  public void setPaddingTop(int paddingTop) {
    this.paddingTop = paddingTop;
  }

  public void setScrollingEnabled(boolean scrollingEnabled) {
    this.scrollingEnabled = scrollingEnabled;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    int childCount = getChildCount();
    int totalHeaderHeight = 0;
    View menuView = getChildAt(0);
    int menuHeightSpec = heightMeasureSpec;
    int height = MeasureSpec.getSize(heightMeasureSpec);

    // If there's more than one child, the header should be first
    if (childCount > 1) {
      // Measure header
      View headerView = getChildAt(0);
      measureChild(headerView, widthMeasureSpec, heightMeasureSpec);
      LayoutParams headerLp = (LayoutParams) headerView.getLayoutParams();
      totalHeaderHeight =
          headerView.getMeasuredHeight() + headerLp.bottomMargin + headerLp.topMargin;
      int maxMenuHeight = height - totalHeaderHeight - paddingTop;

      // Measure menu
      menuView = getChildAt(1);
      // If scrolling is not enabled, we try to measure the menu such that it'll fit in the
      // remaining space
      if (!scrollingEnabled) {
        menuHeightSpec = MeasureSpec.makeMeasureSpec(maxMenuHeight, MeasureSpec.AT_MOST);
      }
    }
    LayoutParams menuLp = (LayoutParams) menuView.getLayoutParams();
    measureChild(menuView, widthMeasureSpec, menuHeightSpec);
    int totalMenuHeight = menuView.getMeasuredHeight() + menuLp.bottomMargin + menuLp.topMargin;
    int totalHeight = max(height, paddingTop + totalHeaderHeight + totalMenuHeight);

    setMeasuredDimension(getMeasuredWidth(), totalHeight);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    // We want to leave everything placed as is in the FrameLayout, but bumped down if it's
    // overlaying with something
    int childCount = getChildCount();
    int y = paddingTop;
    for (int i = 0; i < childCount; i++) {
      View child = getChildAt(i);
      FrameLayout.LayoutParams lp = (LayoutParams) child.getLayoutParams();
      y = max(y, child.getTop());
      y += lp.topMargin;
      child.layout(
          child.getLeft(),
          y,
          child.getRight(),
          y + child.getMeasuredHeight());
      y += child.getMeasuredHeight() + lp.bottomMargin;
    }
  }
}
