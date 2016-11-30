/*
 * Copyright (C) 2015 The Android Open Source Project
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
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.design.R;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuPresenter;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @hide
 */
public class NavigationMenuPresenter implements MenuPresenter, AdapterView.OnItemClickListener {

    private static final String STATE_HIERARCHY = "android:menu:list";
    private static final String STATE_ADAPTER = "android:menu:adapter";

    private NavigationMenuView mMenuView;
    private LinearLayout mHeader;

    private Callback mCallback;
    private MenuBuilder mMenu;
    private int mId;

    private NavigationMenuAdapter mAdapter;
    private LayoutInflater mLayoutInflater;

    private int mTextAppearance;
    private boolean mTextAppearanceSet;
    private ColorStateList mTextColor;
    private ColorStateList mIconTintList;
    private Drawable mItemBackground;

    /**
     * Padding to be inserted at the top of the list to avoid the first menu item
     * from being placed underneath the status bar.
     */
    private int mPaddingTopDefault;

    /**
     * Padding for separators between items
     */
    private int mPaddingSeparator;

    @Override
    public void initForMenu(Context context, MenuBuilder menu) {
        mLayoutInflater = LayoutInflater.from(context);
        mMenu = menu;
        Resources res = context.getResources();
        mPaddingTopDefault = res.getDimensionPixelOffset(
                R.dimen.design_navigation_padding_top_default);
        mPaddingSeparator = res.getDimensionPixelOffset(
                R.dimen.design_navigation_separator_vertical_padding);
    }

    @Override
    public MenuView getMenuView(ViewGroup root) {
        if (mMenuView == null) {
            mMenuView = (NavigationMenuView) mLayoutInflater.inflate(
                    R.layout.design_navigation_menu, root, false);
            if (mAdapter == null) {
                mAdapter = new NavigationMenuAdapter();
            }
            mHeader = (LinearLayout) mLayoutInflater.inflate(R.layout.design_navigation_item_header,
                    mMenuView, false);
            mMenuView.addHeaderView(mHeader, null, false);
            mMenuView.setAdapter(mAdapter);
            mMenuView.setOnItemClickListener(this);
        }
        return mMenuView;
    }

    @Override
    public void updateMenuView(boolean cleared) {
        if (mAdapter != null) {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void setCallback(Callback cb) {
        mCallback = cb;
    }

    @Override
    public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
        return false;
    }

    @Override
    public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
        if (mCallback != null) {
            mCallback.onCloseMenu(menu, allMenusAreClosing);
        }
    }

    @Override
    public boolean flagActionItems() {
        return false;
    }

    @Override
    public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override
    public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
        return false;
    }

    @Override
    public int getId() {
        return mId;
    }

    public void setId(int id) {
        mId = id;
    }

    @Override
    public Parcelable onSaveInstanceState() {
        Bundle state = new Bundle();
        if (mMenuView != null) {
            SparseArray<Parcelable> hierarchy = new SparseArray<>();
            mMenuView.saveHierarchyState(hierarchy);
            state.putSparseParcelableArray(STATE_HIERARCHY, hierarchy);
        }
        if (mAdapter != null) {
            state.putBundle(STATE_ADAPTER, mAdapter.createInstanceState());
        }
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        Bundle state = (Bundle) parcelable;
        SparseArray<Parcelable> hierarchy = state.getSparseParcelableArray(STATE_HIERARCHY);
        if (hierarchy != null) {
            mMenuView.restoreHierarchyState(hierarchy);
        }
        Bundle adapterState = state.getBundle(STATE_ADAPTER);
        if (adapterState != null) {
            mAdapter.restoreInstanceState(adapterState);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int positionInAdapter = position - mMenuView.getHeaderViewsCount();
        if (positionInAdapter >= 0) {
            setUpdateSuspended(true);
            MenuItemImpl item = mAdapter.getItem(positionInAdapter).getMenuItem();
            if (item != null && item.isCheckable()) {
                mAdapter.setCheckedItem(item);
            }
            mMenu.performItemAction(item, this, 0);
            setUpdateSuspended(false);
            updateMenuView(false);
        }
    }

    public void setCheckedItem(MenuItemImpl item) {
        mAdapter.setCheckedItem(item);
    }

    public View inflateHeaderView(@LayoutRes int res) {
        View view = mLayoutInflater.inflate(res, mHeader, false);
        addHeaderView(view);
        return view;
    }

    public void addHeaderView(@NonNull View view) {
        mHeader.addView(view);
        // The padding on top should be cleared.
        mMenuView.setPadding(0, 0, 0, mMenuView.getPaddingBottom());
    }

    public void removeHeaderView(@NonNull View view) {
        mHeader.removeView(view);
        if (mHeader.getChildCount() == 0) {
            mMenuView.setPadding(0, mPaddingTopDefault, 0, mMenuView.getPaddingBottom());
        }
    }

    @Nullable
    public ColorStateList getItemTintList() {
        return mIconTintList;
    }

    public void setItemIconTintList(@Nullable ColorStateList tint) {
        mIconTintList = tint;
        updateMenuView(false);
    }

    @Nullable
    public ColorStateList getItemTextColor() {
        return mTextColor;
    }

    public void setItemTextColor(@Nullable ColorStateList textColor) {
        mTextColor = textColor;
        updateMenuView(false);
    }

    public void setItemTextAppearance(@StyleRes int resId) {
        mTextAppearance = resId;
        mTextAppearanceSet = true;
        updateMenuView(false);
    }

    public Drawable getItemBackground() {
        return mItemBackground;
    }

    public void setItemBackground(Drawable itemBackground) {
        mItemBackground = itemBackground;
    }

    public void setUpdateSuspended(boolean updateSuspended) {
        if (mAdapter != null) {
            mAdapter.setUpdateSuspended(updateSuspended);
        }
    }

    private class NavigationMenuAdapter extends BaseAdapter {

        private static final String STATE_CHECKED_ITEM = "android:menu:checked";

        private static final int VIEW_TYPE_NORMAL = 0;
        private static final int VIEW_TYPE_SUBHEADER = 1;
        private static final int VIEW_TYPE_SEPARATOR = 2;

        private final ArrayList<NavigationMenuItem> mItems = new ArrayList<>();
        private MenuItemImpl mCheckedItem;
        private ColorDrawable mTransparentIcon;
        private boolean mUpdateSuspended;

        NavigationMenuAdapter() {
            prepareMenuItems();
        }

        @Override
        public int getCount() {
            return mItems.size();
        }

        @Override
        public NavigationMenuItem getItem(int position) {
            return mItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getViewTypeCount() {
            return 3;
        }

        @Override
        public int getItemViewType(int position) {
            NavigationMenuItem item = getItem(position);
            if (item.isSeparator()) {
                return VIEW_TYPE_SEPARATOR;
            } else if (item.getMenuItem().hasSubMenu()) {
                return VIEW_TYPE_SUBHEADER;
            } else {
                return VIEW_TYPE_NORMAL;
            }
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            NavigationMenuItem item = getItem(position);
            int viewType = getItemViewType(position);
            switch (viewType) {
                case VIEW_TYPE_NORMAL:
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(R.layout.design_navigation_item,
                                parent, false);
                    }
                    NavigationMenuItemView itemView = (NavigationMenuItemView) convertView;
                    itemView.setIconTintList(mIconTintList);
                    if (mTextAppearanceSet) {
                        itemView.setTextAppearance(itemView.getContext(), mTextAppearance);
                    }
                    if (mTextColor != null) {
                        itemView.setTextColor(mTextColor);
                    }
                    itemView.setBackgroundDrawable(mItemBackground != null ?
                            mItemBackground.getConstantState().newDrawable() : null);
                    itemView.initialize(item.getMenuItem(), 0);
                    break;
                case VIEW_TYPE_SUBHEADER:
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(
                                R.layout.design_navigation_item_subheader, parent, false);
                    }
                    TextView subHeader = (TextView) convertView;
                    subHeader.setText(item.getMenuItem().getTitle());
                    break;
                case VIEW_TYPE_SEPARATOR:
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(
                                R.layout.design_navigation_item_separator, parent, false);
                    }
                    convertView.setPadding(0, item.getPaddingTop(), 0,
                            item.getPaddingBottom());
                    break;
            }
            return convertView;
        }

        @Override
        public boolean areAllItemsEnabled() {
            return false;
        }

        @Override
        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        @Override
        public void notifyDataSetChanged() {
            prepareMenuItems();
            super.notifyDataSetChanged();
        }

        /**
         * Flattens the visible menu items of {@link #mMenu} into {@link #mItems},
         * while inserting separators between items when necessary.
         */
        private void prepareMenuItems() {
            if (mUpdateSuspended) {
                return;
            }
            mUpdateSuspended = true;
            mItems.clear();
            int currentGroupId = -1;
            int currentGroupStart = 0;
            boolean currentGroupHasIcon = false;
            for (int i = 0, totalSize = mMenu.getVisibleItems().size(); i < totalSize; i++) {
                MenuItemImpl item = mMenu.getVisibleItems().get(i);
                if (item.isChecked()) {
                    setCheckedItem(item);
                }
                if (item.isCheckable()) {
                    item.setExclusiveCheckable(false);
                }
                if (item.hasSubMenu()) {
                    SubMenu subMenu = item.getSubMenu();
                    if (subMenu.hasVisibleItems()) {
                        if (i != 0) {
                            mItems.add(NavigationMenuItem.separator(mPaddingSeparator, 0));
                        }
                        mItems.add(NavigationMenuItem.of(item));
                        boolean subMenuHasIcon = false;
                        int subMenuStart = mItems.size();
                        for (int j = 0, size = subMenu.size(); j < size; j++) {
                            MenuItemImpl subMenuItem = (MenuItemImpl) subMenu.getItem(j);
                            if (subMenuItem.isVisible()) {
                                if (!subMenuHasIcon && subMenuItem.getIcon() != null) {
                                    subMenuHasIcon = true;
                                }
                                if (subMenuItem.isCheckable()) {
                                    subMenuItem.setExclusiveCheckable(false);
                                }
                                if (item.isChecked()) {
                                    setCheckedItem(item);
                                }
                                mItems.add(NavigationMenuItem.of(subMenuItem));
                            }
                        }
                        if (subMenuHasIcon) {
                            appendTransparentIconIfMissing(subMenuStart, mItems.size());
                        }
                    }
                } else {
                    int groupId = item.getGroupId();
                    if (groupId != currentGroupId) { // first item in group
                        currentGroupStart = mItems.size();
                        currentGroupHasIcon = item.getIcon() != null;
                        if (i != 0) {
                            currentGroupStart++;
                            mItems.add(NavigationMenuItem.separator(
                                    mPaddingSeparator, mPaddingSeparator));
                        }
                    } else if (!currentGroupHasIcon && item.getIcon() != null) {
                        currentGroupHasIcon = true;
                        appendTransparentIconIfMissing(currentGroupStart, mItems.size());
                    }
                    if (currentGroupHasIcon && item.getIcon() == null) {
                        item.setIcon(android.R.color.transparent);
                    }
                    mItems.add(NavigationMenuItem.of(item));
                    currentGroupId = groupId;
                }
            }
            mUpdateSuspended = false;
        }

        private void appendTransparentIconIfMissing(int startIndex, int endIndex) {
            for (int i = startIndex; i < endIndex; i++) {
                MenuItem item = mItems.get(i).getMenuItem();
                if (item.getIcon() == null) {
                    if (mTransparentIcon == null) {
                        mTransparentIcon = new ColorDrawable(android.R.color.transparent);
                    }
                    item.setIcon(mTransparentIcon);
                }
            }
        }

        public void setCheckedItem(MenuItemImpl checkedItem) {
            if (mCheckedItem == checkedItem || !checkedItem.isCheckable()) {
                return;
            }
            if (mCheckedItem != null) {
                mCheckedItem.setChecked(false);
            }
            mCheckedItem = checkedItem;
            checkedItem.setChecked(true);
        }

        public Bundle createInstanceState() {
            Bundle state = new Bundle();
            if (mCheckedItem != null) {
                state.putInt(STATE_CHECKED_ITEM, mCheckedItem.getItemId());
            }
            return state;
        }

        public void restoreInstanceState(Bundle state) {
            int checkedItem = state.getInt(STATE_CHECKED_ITEM, 0);
            if (checkedItem != 0) {
                mUpdateSuspended = true;
                for (NavigationMenuItem item : mItems) {
                    MenuItemImpl menuItem = item.getMenuItem();
                    if (menuItem !=  null && menuItem.getItemId() == checkedItem) {
                        setCheckedItem(menuItem);
                        break;
                    }
                }
                mUpdateSuspended = false;
                prepareMenuItems();
            }
        }

        public void setUpdateSuspended(boolean updateSuspended) {
            mUpdateSuspended = updateSuspended;
        }

    }

    /**
     * Wraps {@link MenuItemImpl}. This allows separators to be counted as items in list.
     */
    private static class NavigationMenuItem {

        /** The item; null for separators */
        private final MenuItemImpl mMenuItem;

        /** Padding top; used only for separators */
        private final int mPaddingTop;

        /** Padding bottom; used only for separators */
        private final int mPaddingBottom;

        private NavigationMenuItem(MenuItemImpl item, int paddingTop, int paddingBottom) {
            mMenuItem = item;
            mPaddingTop = paddingTop;
            mPaddingBottom = paddingBottom;
        }

        public static NavigationMenuItem of(MenuItemImpl item) {
            return new NavigationMenuItem(item, 0, 0);
        }

        public static NavigationMenuItem separator(int paddingTop, int paddingBottom) {
            return new NavigationMenuItem(null, paddingTop, paddingBottom);
        }

        public boolean isSeparator() {
            return mMenuItem == null;
        }

        public int getPaddingTop() {
            return mPaddingTop;
        }

        public int getPaddingBottom() {
            return mPaddingBottom;
        }

        public MenuItemImpl getMenuItem() {
            return mMenuItem;
        }

        public boolean isEnabled() {
            // Separators and subheaders never respond to click
            return mMenuItem != null && !mMenuItem.hasSubMenu() && mMenuItem.isEnabled();
        }

    }

}
