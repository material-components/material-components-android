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
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
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
import android.widget.TextView;

import java.util.ArrayList;

/**
 * @hide
 */
public class NavigationMenuPresenter implements MenuPresenter, AdapterView.OnItemClickListener {

    private static final String STATE_HIERARCHY = "android:menu:list";

    private NavigationMenuView mMenuView;

    private Callback mCallback;

    private MenuBuilder mMenu;

    private ArrayList<NavigationMenuItem> mItems = new ArrayList<>();

    private int mId;

    private NavigationMenuAdapter mAdapter;

    private LayoutInflater mLayoutInflater;

    private View mSpace;

    /**
     * Padding to be inserted at the top of the list to avoid the first menu item
     * from being placed underneath the status bar.
     */
    private int mPaddingTopDefault;

    @Override
    public void initForMenu(Context context, MenuBuilder menu) {
        mLayoutInflater = LayoutInflater.from(context);
        mMenu = menu;
        mPaddingTopDefault = context.getResources().getDimensionPixelOffset(
                R.dimen.drawer_padding_top_default);
    }

    @Override
    public MenuView getMenuView(ViewGroup root) {
        if (mMenuView == null) {
            mMenuView = (NavigationMenuView) mLayoutInflater.inflate(
                    R.layout.design_drawer_menu, root, false);
            if (mAdapter == null) {
                mAdapter = new NavigationMenuAdapter();
            }
            mMenuView.setAdapter(mAdapter);
            mMenuView.setOnItemClickListener(this);
        }
        return mMenuView;
    }

    @Override
    public void updateMenuView(boolean cleared) {
        if (mAdapter != null) {
            prepareMenuItems();
            mAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Flattens the visible menu items of {@link #mMenu} into {@link #mItems},
     * while inserting separators between items when necessary.
     */
    private void prepareMenuItems() {
        mItems.clear();
        int currentGroupId = 0;
        for (MenuItemImpl item : mMenu.getVisibleItems()) {
            if (item.hasSubMenu()) {
                SubMenu subMenu = item.getSubMenu();
                if (subMenu.hasVisibleItems()) {
                    mItems.add(NavigationMenuItem.SEPARATOR);
                    mItems.add(NavigationMenuItem.of(item));
                    for (int i = 0, size = subMenu.size(); i < size; i++) {
                        MenuItem subMenuItem = subMenu.getItem(i);
                        if (subMenuItem.isVisible()) {
                            mItems.add(NavigationMenuItem.of((MenuItemImpl) subMenuItem));
                        }
                    }
                }
            } else {
                int groupId = item.getGroupId();
                if (groupId != currentGroupId) {
                    mItems.add(NavigationMenuItem.SEPARATOR);
                }
                mItems.add(NavigationMenuItem.of(item));
                currentGroupId = groupId;
            }
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
        SparseArray<Parcelable> hierarchy = new SparseArray<>();
        if (mMenuView != null) {
            mMenuView.saveHierarchyState(hierarchy);
        }
        state.putSparseParcelableArray(STATE_HIERARCHY, hierarchy);
        return state;
    }

    @Override
    public void onRestoreInstanceState(Parcelable parcelable) {
        Bundle state = (Bundle) parcelable;
        SparseArray<Parcelable> hierarchy = state.getSparseParcelableArray(STATE_HIERARCHY);
        if (hierarchy != null) {
            mMenuView.restoreHierarchyState(hierarchy);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        int positionInAdapter = position - mMenuView.getHeaderViewsCount();
        if (positionInAdapter >= 0) {
            mMenu.performItemAction(mAdapter.getItem(positionInAdapter).getMenuItem(), this, 0);
        }
    }

    public View inflateHeaderView(@LayoutRes int res) {
        View view = mLayoutInflater.inflate(res, mMenuView, false);
        addHeaderView(view);
        onHeaderAdded();
        return view;
    }

    public void addHeaderView(@NonNull View view) {
        mMenuView.addHeaderView(view);
        onHeaderAdded();
    }

    private void onHeaderAdded() {
        // If we have just added the first header, we also need to insert a space
        // between the header and the menu items.
        if (mMenuView.getHeaderViewsCount() == 1) {
            mSpace = mLayoutInflater.inflate(R.layout.design_drawer_item_space, mMenuView, false);
            mMenuView.addHeaderView(mSpace);
        }
        // The padding on top should be cleared.
        mMenuView.setPadding(0, 0, 0, 0);
    }

    public void removeHeaderView(@NonNull View view) {
        if (mMenuView.removeHeaderView(view)) {
            // Remove the space if it is the only remained header
            if (mMenuView.getHeaderViewsCount() == 1) {
                mMenuView.removeHeaderView(mSpace);
                mMenuView.setPadding(0, mPaddingTopDefault, 0, 0);
            }
        }
    }

    private class NavigationMenuAdapter extends BaseAdapter {

        private static final int VIEW_TYPE_NORMAL = 0;

        private static final int VIEW_TYPE_SUBHEADER = 1;

        private static final int VIEW_TYPE_SEPARATOR = 2;

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
                        convertView = mLayoutInflater.inflate(R.layout.design_drawer_item, parent,
                                false);
                    }
                    MenuView.ItemView itemView = (MenuView.ItemView) convertView;
                    itemView.initialize(item.getMenuItem(), 0);
                    break;
                case VIEW_TYPE_SUBHEADER:
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(R.layout.design_drawer_item_subheader,
                                parent, false);
                    }
                    TextView subHeader = (TextView) convertView;
                    subHeader.setText(item.getMenuItem().getTitle());
                    break;
                case VIEW_TYPE_SEPARATOR:
                    if (convertView == null) {
                        convertView = mLayoutInflater.inflate(R.layout.design_drawer_item_separator,
                                parent, false);
                    }
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

    }

    /**
     * Wraps {@link MenuItemImpl}. This allows separators to be counted as items in list.
     */
    private static class NavigationMenuItem {

        private static final NavigationMenuItem SEPARATOR = new NavigationMenuItem(null);

        private final MenuItemImpl mMenuItem;

        private NavigationMenuItem(MenuItemImpl item) {
            mMenuItem = item;
        }

        public static NavigationMenuItem of(MenuItemImpl item) {
            return new NavigationMenuItem(item);
        }

        public boolean isSeparator() {
            return this == SEPARATOR;
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
