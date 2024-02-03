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
import static com.google.android.material.navigation.NavigationBarView.SESL_TYPE_LABEL_ONLY;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import android.content.Context;
import android.content.res.Resources;
import androidx.appcompat.view.menu.MenuBuilder;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.core.view.ViewCompat;
import com.google.android.material.navigation.NavigationBarItemView;
import com.google.android.material.navigation.NavigationBarMenuView;
import java.util.ArrayList;
import java.util.List;

/**
 * <b>SESL Variant</b><br><br>
 *
 * @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationMenuView extends NavigationBarMenuView {
  private final int inactiveItemMaxWidth;
  private final int inactiveItemMinWidth;
  private int activeItemMaxWidth;
  private final int activeItemMinWidth;

  private boolean itemHorizontalTranslationEnabled;
  private final List<Integer> tempChildWidths = new ArrayList<>();

  //Sesl
  private int itemHeight;
  private boolean mHasIcon;
  private float mWidthPercent;
  //sesl

  public BottomNavigationMenuView(@NonNull Context context) {
    super(context);

    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    setLayoutParams(params);

    final Resources res = getResources();
    inactiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_item_max_width/*sesl*/);
    inactiveItemMinWidth =
        res.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_item_min_width/*sesl*/);
    TypedValue outValue = new TypedValue();
    res.getValue(R.dimen.sesl_bottom_navigation_width_proportion, outValue, true);
    mWidthPercent = outValue.getFloat();
    activeItemMaxWidth =
        /*sesl*/(int) (getResources().getDisplayMetrics().widthPixels * mWidthPercent);
    activeItemMinWidth =
        res.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_active_item_min_width);//sesl
    itemHeight = res.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_mode_height);//sesl
    mUseItemPool = false;//sesl
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final MenuBuilder menu = getMenu();
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    // Use visible item count to calculate widths
    final int visibleCount = menu.getVisibleItems().size();
    // Use total item counts to measure children
    final int totalCount = getChildCount();
    tempChildWidths.clear();
    //Sesl
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    mWidthPercent = width / displayMetrics.density < 590.0F ? 1.0F : 0.75F;
    activeItemMaxWidth = (int)(displayMetrics.widthPixels * mWidthPercent);
    final int maxWidth = (int)(width * mWidthPercent);
    mHasIcon = getViewType() != SESL_TYPE_LABEL_ONLY;
    final int parentHeight = getResources().getDimensionPixelSize(
        mHasIcon ? R.dimen.sesl_bottom_navigation_icon_mode_height : R.dimen.sesl_bottom_navigation_text_mode_height
    );
    itemHeight = parentHeight;
    //sesl

    final int heightSpec = MeasureSpec.makeMeasureSpec(parentHeight, MeasureSpec.EXACTLY);

    if (isShifting(getLabelVisibilityMode(), totalCount/*sesl*/)
        && isItemHorizontalTranslationEnabled()) {
      final View activeChild = getChildAt(getSelectedItemPosition());
      int activeItemWidth = activeItemMinWidth;
      if (activeChild.getVisibility() != View.GONE) {
        // Do an AT_MOST measure pass on the active child to get its desired width, and resize the
        // active child view based on that width
        activeChild.measure(
            MeasureSpec.makeMeasureSpec(activeItemMaxWidth, MeasureSpec.AT_MOST), heightSpec);
        activeItemWidth = Math.max(activeItemWidth, activeChild.getMeasuredWidth());
      }
      final int inactiveCount = totalCount/*sesl*/ - (activeChild.getVisibility() != View.GONE ? 1 : 0);
      final int activeMaxAvailable = maxWidth/*sesl*/ - inactiveCount * inactiveItemMinWidth;
      final int activeWidth =
          Math.min(activeMaxAvailable, Math.min(activeItemWidth, activeItemMaxWidth));
      final int inactiveMaxAvailable =
          (maxWidth/*sesl*/ - activeWidth) / (inactiveCount == 0 ? 1 : inactiveCount);
      final int inactiveWidth = Math.min(inactiveMaxAvailable, inactiveItemMaxWidth);
      int extra = maxWidth/*sesl*/ - activeWidth - inactiveWidth * inactiveCount;

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
      final int maxAvailable = maxWidth/*sesl*/ / (visibleCount == 0 ? 1 : visibleCount);
      final int childWidth = (visibleCount == 2) ? maxAvailable/*sesl*/: Math.min(maxAvailable, activeItemMaxWidth);
      int extra = maxWidth/*sesl*/ - childWidth * visibleCount;
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

    int totalWidth = 0;
    for (int i = 0; i < totalCount; i++) {
      final View child = getChildAt(i);
      if (child == null/*sesl*/ || child.getVisibility() == GONE) {
        continue;
      }
      child.measure(
          MeasureSpec.makeMeasureSpec(tempChildWidths.get(i), MeasureSpec.EXACTLY), heightSpec);
      ViewGroup.LayoutParams params = child.getLayoutParams();
      params.width = child.getMeasuredWidth();
      totalWidth += child.getMeasuredWidth();
    }
    setMeasuredDimension(
        View.resolveSizeAndState(totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), 0),
        View.resolveSizeAndState(parentHeight, heightSpec, 0)
    );//sesl
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    final int count = getChildCount();
    final int width = right - left;
    final int height = bottom - top;
    //Sesl
    final int padding;
    if (!mHasIcon) {
      padding = 0;
    } else if (getViewVisibleItemCount() == 5) {
      padding = getResources()
              .getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_mode_min_padding_horizontal);
    } else {
      padding = getResources()
              .getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_mode_padding_horizontal);
    }
    //sesl
    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
        child.layout(width - used - child.getMeasuredWidth() + padding/*sesl*/, 0, width - used - padding, height);
      } else {
        child.layout(used + padding/*sesl*/, 0, child.getMeasuredWidth() + used - padding, height);
      }
      used += child.getMeasuredWidth();
    }
    updateBadgeIfNeeded();//sesl
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
