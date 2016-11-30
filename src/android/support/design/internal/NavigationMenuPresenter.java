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
import android.os.Parcelable;
import android.support.design.R;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuPresenter;
import android.support.v7.internal.view.menu.MenuView;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;

/**
 * @hide
 */
public class NavigationMenuPresenter implements MenuPresenter, AdapterView.OnItemClickListener {

    private NavigationMenuView mMenuView;

    private Callback mCallback;

    private MenuBuilder mMenu;

    private int mId;

    private NavigationMenuAdapter mAdapter;

    private LayoutInflater mLayoutInflater;

    @Override
    public void initForMenu(Context context, MenuBuilder menu) {
        mLayoutInflater = LayoutInflater.from(context);
        mMenu = menu;
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
        return null;
    }

    @Override
    public void onRestoreInstanceState(Parcelable state) {
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        mMenu.performItemAction(mAdapter.getItem(position), this, 0);
    }

    private class NavigationMenuAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMenu.size();
        }

        @Override
        public MenuItemImpl getItem(int position) {
            return (MenuItemImpl) mMenu.getItem(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.design_drawer_item, parent, false);
            }
            MenuView.ItemView itemView = (MenuView.ItemView) convertView;
            itemView.initialize(getItem(position), 0);
            return convertView;
        }
    }

}
