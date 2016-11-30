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

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.Nullable;
import android.support.design.R;
import android.support.v4.util.Pools;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.view.menu.MenuItemImpl;
import android.support.v7.view.menu.MenuView;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

/**
 * @hide
 */
public class BottomNavigationMenuView extends LinearLayout implements MenuView {
    private final int mInactiveItemMaxWidth;
    private final int mActiveItemMaxWidth;
    private final OnClickListener mOnClickListener;
    private static final Pools.Pool<BottomNavigationItemView> sItemPool =
            new Pools.SynchronizedPool<>(5);

    private BottomNavigationItemView[] mButtons;
    private int mActiveButton = 0;
    private ColorStateList mItemIconTint;
    private ColorStateList mItemTextColor;
    private int mItemBackgroundRes;

    private BottomNavigationPresenter mPresenter;
    private MenuBuilder mMenu;

    public BottomNavigationMenuView(Context context) {
        this(context, null);
    }

    public BottomNavigationMenuView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setGravity(Gravity.CENTER);
        setOrientation(HORIZONTAL);

        mInactiveItemMaxWidth = getResources().getDimensionPixelSize(
                R.dimen.design_bottom_navigation_item_max_width);
        mActiveItemMaxWidth = getResources()
                .getDimensionPixelSize(R.dimen.design_bottom_navigation_active_item_max_width);

        mOnClickListener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                final BottomNavigationItemView itemView = (BottomNavigationItemView) v;
                final int itemPosition = itemView.getItemPosition();
                activateNewButton(itemPosition);
                mMenu.performItemAction(itemView.getItemData(), mPresenter, 0);
            }
        };
    }

    @Override
    public void initialize(MenuBuilder menu) {
        mMenu = menu;
        if (mMenu == null) return;
        if (mMenu.size() > mActiveButton) {
            mMenu.getItem(mActiveButton).setChecked(true);
        }
    }

    @Override
    public int getWindowAnimations() {
        return 0;
    }

    public void setIconTintList(ColorStateList color) {
        mItemIconTint = color;
        if (mButtons == null) return;
        for (BottomNavigationItemView item : mButtons) {
            item.setIconTintList(color);
        }
    }

    @Nullable
    public ColorStateList getIconTintList() {
        return mItemIconTint;
    }

    public void setItemTextColor(ColorStateList color) {
        mItemTextColor = color;
        if (mButtons == null) return;
        for (BottomNavigationItemView item : mButtons) {
            item.setTextColor(color);
        }
    }

    public ColorStateList getItemTextColor() {
        return mItemTextColor;
    }

    public void setItemBackgroundRes(int background) {
        mItemBackgroundRes = background;
        if (mButtons == null) return;
        for (BottomNavigationItemView item : mButtons) {
            item.setItemBackground(background);
        }
    }

    public int getItemBackgroundRes() {
        return mItemBackgroundRes;
    }

    public void setPresenter(BottomNavigationPresenter presenter) {
        mPresenter = presenter;
    }

    public void buildMenuView() {
        if (mButtons != null) {
            for (BottomNavigationItemView item : mButtons) {
                sItemPool.release(item);
            }
        }
        removeAllViews();
        mButtons = new BottomNavigationItemView[mMenu.size()];
        for (int i = 0; i < mMenu.size(); i++) {
            mPresenter.setUpdateSuspended(true);
            mMenu.getItem(i).setCheckable(true);
            mPresenter.setUpdateSuspended(false);
            BottomNavigationItemView child = getNewItem();
            mButtons[i] = child;
            child.setIconTintList(mItemIconTint);
            child.setTextColor(mItemTextColor);
            child.setItemBackground(mItemBackgroundRes);
            child.initialize((MenuItemImpl) mMenu.getItem(i), 0);
            child.setItemPosition(i);
            child.setOnClickListener(mOnClickListener);
            addView(child);
        }
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
            mButtons[i].initialize((MenuItemImpl) mMenu.getItem(i), 0);
            mPresenter.setUpdateSuspended(false);
        }
    }

    private void activateNewButton(int newButton) {
        if (mActiveButton == newButton) return;

        mPresenter.setUpdateSuspended(true);
        mButtons[mActiveButton].setChecked(false);
        mButtons[newButton].setChecked(true);
        mPresenter.setUpdateSuspended(false);

        mActiveButton = newButton;
    }

    public boolean updateOnSizeChange(int width) {
        if (getChildCount() == 0) {
            return false;
        }
        int available = width / getChildCount();
        int itemWidth = Math.min(available, mActiveItemMaxWidth);

        boolean changed = false;

        for (int i = 0; i < mButtons.length; i++) {
            ViewGroup.LayoutParams params = mButtons[i].getLayoutParams();
            if (params.width == itemWidth) {
                continue;
            }
            changed = true;
            params.width = itemWidth;
            params.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mButtons[i].setLayoutParams(params);
        }
        return changed;
    }

    private BottomNavigationItemView getNewItem() {
        BottomNavigationItemView item = sItemPool.acquire();
        if (item == null) {
            item = new BottomNavigationItemView(getContext());
        }
        return item;
    }
}
