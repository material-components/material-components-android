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
import android.graphics.drawable.Drawable;
import android.support.design.R;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

/**
 * @hide
 */
public class NavigationMenuItemView extends TextView implements MenuView.ItemView {

    private MenuItemImpl mItemData;

    private int mIconSize;

    public NavigationMenuItemView(Context context) {
        this(context, null);
    }

    public NavigationMenuItemView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.drawer_icon_size);
    }

    @Override
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;

        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);

        setTitle(itemData.getTitle());
        setCheckable(itemData.isCheckable());
        setIcon(itemData.getIcon());
        setEnabled(itemData.isEnabled());
    }

    @Override
    public MenuItemImpl getItemData() {
        return mItemData;
    }

    @Override
    public void setTitle(CharSequence title) {
        setText(title);
    }

    @Override
    public void setCheckable(boolean checkable) {
    }

    @Override
    public void setChecked(boolean checked) {
    }

    @Override
    public void setShortcut(boolean showShortcut, char shortcutKey) {
    }

    @Override
    public void setIcon(Drawable icon) {
        if (icon != null) {
            icon.setBounds(0, 0, mIconSize, mIconSize);
        }
        TextViewCompat.setCompoundDrawablesRelative(this, icon, null, null, null);
    }

    @Override
    public boolean prefersCondensedTitle() {
        return false;
    }

    @Override
    public boolean showsIcon() {
        return true;
    }

}
