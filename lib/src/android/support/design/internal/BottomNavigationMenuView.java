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

package android.support.design.internal;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.design.R;
import android.support.v4.util.Pools;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

/** @hide For internal use only. */
@RestrictTo(LIBRARY_GROUP)
public class BottomNavigationMenuView extends ViewGroup implements MenuView {
  private final int mInactiveItemMaxWidth;
  private final int mInactiveItemMinWidth;
  private final int mActiveItemMaxWidth;
  private final int mActiveItemMaxHeight;
  private final int mItemWidth;
  private final int mItemHeight;
  private final OnClickListener mOnClickListener;
  private final BottomNavigationAnimationHelperBase mAnimationHelper;
  private static final Pools.Pool<BottomNavigationItemView> sItemPool =
      new Pools.SynchronizedPool<>(5);

  private boolean mShiftingMode = true;
  private boolean mTabletMode = false;

  private BottomNavigationItemView[] mButtons;
  private int mActiveButton = 0;
  private ColorStateList mItemIconTint;
  private ColorStateList mItemTextColor;
  private int mItemBackgroundRes;
  private int[] mTempChildSizes;

  private BottomNavigationPresenter mPresenter;
  private MenuBuilder mMenu;

  public BottomNavigationMenuView(Context context) {
    this(context, null);
  }

  public BottomNavigationMenuView(Context context, AttributeSet attrs) {
    super(context, attrs);
    final Resources res = getResources();
    mInactiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_max_width);
    mInactiveItemMinWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_item_min_width);
    mActiveItemMaxWidth =
        res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);
    mActiveItemMaxHeight =
            res.getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_height);
    mItemHeight = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_height);
    mItemWidth = res.getDimensionPixelSize(R.dimen.design_bottom_navigation_width);

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
      mAnimationHelper = new BottomNavigationAnimationHelperIcs();
    } else {
      mAnimationHelper = new BottomNavigationAnimationHelperBase();
    }

    mOnClickListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            final BottomNavigationItemView itemView = (BottomNavigationItemView) v;
            final int itemPosition = itemView.getItemPosition();
            if (!mMenu.performItemAction(itemView.getItemData(), mPresenter, 0)) {
              activateNewButton(itemPosition);
            }
          }
        };

    mTempChildSizes = new int[BottomNavigationMenu.MAX_ITEM_COUNT];
  }

  @Override
  public void initialize(MenuBuilder menu) {
    mMenu = menu;
  }

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int count = getChildCount();

    final int heightSpec = MeasureSpec.makeMeasureSpec(mItemHeight, MeasureSpec.EXACTLY);

    if (mShiftingMode) {
      final int inactiveCount = count - 1;
      final int activeMaxAvailable = width - inactiveCount * mInactiveItemMinWidth;
      final int activeWidth = Math.min(activeMaxAvailable, mActiveItemMaxWidth);
      final int inactiveMaxAvailable = (width - activeWidth) / inactiveCount;
      final int inactiveWidth = Math.min(inactiveMaxAvailable, mInactiveItemMaxWidth);
      int extra = width - activeWidth - inactiveWidth * inactiveCount;
      for (int i = 0; i < count; i++) {
        mTempChildSizes[i] = (i == mActiveButton) ? activeWidth : inactiveWidth;
        if (extra > 0) {
          mTempChildSizes[i]++;
          extra--;
        }
      }
    } else if (mTabletMode) {
      measureTabletMode(heightMeasureSpec);
      return;
    } else {
      final int maxAvailable = width / (count == 0 ? 1 : count);
      final int childWidth = Math.min(maxAvailable, mActiveItemMaxWidth);
      int extra = width - childWidth * count;
      for (int i = 0; i < count; i++) {
        mTempChildSizes[i] = childWidth;
        if (extra > 0) {
          mTempChildSizes[i]++;
          extra--;
        }
      }
    }

    int totalWidth = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.measure(
          MeasureSpec.makeMeasureSpec(mTempChildSizes[i], MeasureSpec.EXACTLY), heightSpec);
      ViewGroup.LayoutParams params = child.getLayoutParams();
      params.width = child.getMeasuredWidth();
      totalWidth += child.getMeasuredWidth();
    }
    setMeasuredDimension(
        ViewCompat.resolveSizeAndState(
            totalWidth, MeasureSpec.makeMeasureSpec(totalWidth, MeasureSpec.EXACTLY), 0),
        ViewCompat.resolveSizeAndState(mItemHeight, heightSpec, 0));
  }

  private void measureTabletMode(int heightMeasureSpec) {
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    final int count = getChildCount();

    final int widthSpec = MeasureSpec.makeMeasureSpec(mItemWidth, MeasureSpec.EXACTLY);

    final int maxAvailable = height / (count == 0 ? 1 : count);
    final int childHeight = Math.min(maxAvailable, mActiveItemMaxHeight);
    int extra = height - childHeight * count;
    for (int i = 0; i < count; i++) {
      mTempChildSizes[i] = childHeight;
      if (extra > 0) {
        mTempChildSizes[i]++;
        extra--;
      }
    }

    int totalHeight = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.measure(
              widthSpec,
              MeasureSpec.makeMeasureSpec(mTempChildSizes[i], MeasureSpec.EXACTLY));
      ViewGroup.LayoutParams params = child.getLayoutParams();
      params.height = child.getMeasuredHeight();
      totalHeight += child.getMeasuredHeight();
    }

    setMeasuredDimension(
            ViewCompat.resolveSizeAndState(mItemWidth, widthSpec, 0),
            ViewCompat.resolveSizeAndState(
                    totalHeight, MeasureSpec.makeMeasureSpec(totalHeight, MeasureSpec.EXACTLY), 0)
            );
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    if (mTabletMode) {
      layoutTabletMode(left, right);
    } else {
      final int count = getChildCount();
      final int width = right - left;
      final int height = bottom - top;
      int used = 0;
      for (int i = 0; i < count; i++) {
        final View child = getChildAt(i);
        if (child.getVisibility() == GONE) {
          continue;
        }
        if (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL) {
          child.layout(width - used - child.getMeasuredWidth(), 0, width - used, height);
        } else {
          child.layout(used, 0, child.getMeasuredWidth() + used, height);
        }
        used += child.getMeasuredWidth();
      }
    }
  }

  private void layoutTabletMode(int left, int right) {
    final int count = getChildCount();
    final int width = right - left;

    int used = 0;
    for (int i = 0; i < count; i++) {
      final View child = getChildAt(i);
      if (child.getVisibility() == GONE) {
        continue;
      }
      child.layout(0, used, width, child.getMeasuredHeight() + used);
      used += child.getMeasuredHeight();
    }
  }

  @Override
  public int getWindowAnimations() {
    return 0;
  }

  /**
   * Sets the tint which is applied to the menu items' icons.
   *
   * @param tint the tint to apply
   */
  public void setIconTintList(ColorStateList tint) {
    mItemIconTint = tint;
    if (mButtons == null) return;
    for (BottomNavigationItemView item : mButtons) {
      item.setIconTintList(tint);
    }
  }

  /**
   * Returns the tint which is applied to menu items' icons.
   *
   * @return the ColorStateList that is used to tint menu items' icons
   */
  @Nullable
  public ColorStateList getIconTintList() {
    return mItemIconTint;
  }

  /**
   * Sets the text color to be used on menu items.
   *
   * @param color the ColorStateList used for menu items' text.
   */
  public void setItemTextColor(ColorStateList color) {
    mItemTextColor = color;
    if (mButtons == null) return;
    for (BottomNavigationItemView item : mButtons) {
      item.setTextColor(color);
    }
  }

  /**
   * Returns the text color used on menu items.
   *
   * @return the ColorStateList used for menu items' text
   */
  public ColorStateList getItemTextColor() {
    return mItemTextColor;
  }

  /**
   * Sets the resource ID to be used for item background.
   *
   * @param background the resource ID of the background
   */
  public void setItemBackgroundRes(int background) {
    mItemBackgroundRes = background;
    if (mButtons == null) return;
    for (BottomNavigationItemView item : mButtons) {
      item.setItemBackground(background);
    }
  }

  /**
   * Returns the resource ID for the background of the menu items.
   *
   * @return the resource ID for the background
   */
  public int getItemBackgroundRes() {
    return mItemBackgroundRes;
  }

  public void setPresenter(BottomNavigationPresenter presenter) {
    mPresenter = presenter;
  }

  /**
   * Sets the tablet mode flag to layout children differently
   * @param enabled table mode flag value
   */
  public void setTabletMode(boolean enabled) {
    this.mTabletMode = enabled;
  }

  public void buildMenuView() {
    if (mButtons != null) {
      for (BottomNavigationItemView item : mButtons) {
        sItemPool.release(item);
      }
    }
    removeAllViews();
    if (mMenu.size() == 0) {
      mButtons = null;
      return;
    }
    mButtons = new BottomNavigationItemView[mMenu.size()];
    mShiftingMode = mMenu.size() > 3;
    for (int i = 0; i < mMenu.size(); i++) {
      mPresenter.setUpdateSuspended(true);
      mMenu.getItem(i).setCheckable(true);
      mPresenter.setUpdateSuspended(false);
      BottomNavigationItemView child = getNewItem();
      mButtons[i] = child;
      child.setIconTintList(mItemIconTint);
      child.setTextColor(mItemTextColor);
      child.setItemBackground(mItemBackgroundRes);
      child.setShiftingMode(mShiftingMode);
      child.setTabletMode(mTabletMode);
      child.initialize((MenuItemImpl) mMenu.getItem(i), 0);
      child.setItemPosition(i);
      child.setOnClickListener(mOnClickListener);
      addView(child);
    }
    mActiveButton = Math.min(mMenu.size() - 1, mActiveButton);
    mMenu.getItem(mActiveButton).setChecked(true);
  }

  public void updateMenuView() {
    final int menuSize = mMenu.size();
    if (menuSize != mButtons.length) {
      // The size has changed. Rebuild menu view from scratch.
      buildMenuView();
      return;
    }
    for (int i = 0; i < menuSize; i++) {
      mPresenter.setUpdateSuspended(true);
      if (mMenu.getItem(i).isChecked()) {
        mActiveButton = i;
      }
      mButtons[i].initialize((MenuItemImpl) mMenu.getItem(i), 0);
      mPresenter.setUpdateSuspended(false);
    }
  }

  private void activateNewButton(int newButton) {
    if (mActiveButton == newButton) return;

    mAnimationHelper.beginDelayedTransition(this);

    mMenu.getItem(newButton).setChecked(true);

    mActiveButton = newButton;
  }

  private BottomNavigationItemView getNewItem() {
    BottomNavigationItemView item = sItemPool.acquire();
    if (item == null) {
      item = new BottomNavigationItemView(getContext());
    }
    return item;
  }
}
