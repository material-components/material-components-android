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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.design.R;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.widget.TextViewCompat;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.MenuView;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

/**
 * @hide
 */
public class NavigationMenuItemView extends TextView implements MenuView.ItemView {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};

    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

    private int mIconSize;

    private MenuItemImpl mItemData;

    private ColorStateList mTintList;

    public NavigationMenuItemView(Context context) {
        this(context, null);
    }

    public NavigationMenuItemView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NavigationMenuItemView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mIconSize = context.getResources().getDimensionPixelSize(R.dimen.navigation_icon_size);
    }

    @Override
    public void initialize(MenuItemImpl itemData, int menuType) {
        mItemData = itemData;

        setVisibility(itemData.isVisible() ? View.VISIBLE : View.GONE);

        if (mTintList == null && (itemData.isChecked() || !itemData.isEnabled())) {
            mTintList = createDefaultTintList();
            setTextColor(mTintList);
        }
        if (getBackground() == null) {
            setBackgroundDrawable(createDefaultBackground());
        }

        setCheckable(itemData.isCheckable());
        setChecked(itemData.isChecked());
        setTitle(itemData.getTitle());
        setIcon(itemData.getIcon());
        setEnabled(itemData.isEnabled());
    }

    private ColorStateList createDefaultTintList() {
        TypedValue value = new TypedValue();
        if (!getContext().getTheme()
                .resolveAttribute(android.R.attr.textColorPrimary, value, true)) {
            return null;
        }
        ColorStateList base = getResources().getColorStateList(value.resourceId);
        if (!getContext().getTheme().resolveAttribute(R.attr.colorPrimary, value, true)) {
            return null;
        }
        int colorPrimary = value.data;
        int defaultColor = base.getDefaultColor();
        return new ColorStateList(new int[][]{
                DISABLED_STATE_SET,
                CHECKED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[]{
                base.getColorForState(DISABLED_STATE_SET, defaultColor),
                colorPrimary,
                defaultColor
        });
    }

    private StateListDrawable createDefaultBackground() {
        TypedValue value = new TypedValue();
        if (getContext().getTheme()
                .resolveAttribute(R.attr.colorControlHighlight, value, true)) {
            StateListDrawable drawable = new StateListDrawable();
            drawable.addState(CHECKED_STATE_SET, new ColorDrawable(value.data));
            drawable.addState(EMPTY_STATE_SET, new ColorDrawable(Color.TRANSPARENT));
            return drawable;
        }
        return null;
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
        if (checkable && mTintList != null) {
            setTextColor(mTintList);
        }
    }

    @Override
    public void setChecked(boolean checked) {
        refreshDrawableState();
    }

    @Override
    public void setShortcut(boolean showShortcut, char shortcutKey) {
    }

    @Override
    public void setIcon(Drawable icon) {
        if (icon != null) {
            icon = DrawableCompat.wrap(icon);
            icon.setBounds(0, 0, mIconSize, mIconSize);
            icon = icon.mutate();
            if (mItemData.isChecked() || !mItemData.isEnabled()) {
                DrawableCompat.setTintList(icon, mTintList);
            } else {
                DrawableCompat.setTintList(icon, null);
            }
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

    @Override
    protected int[] onCreateDrawableState(int extraSpace) {
        if (mItemData != null && mItemData.isChecked()) {
            return mergeDrawableStates(super.onCreateDrawableState(extraSpace + 1),
                    CHECKED_STATE_SET);
        } else {
            return super.onCreateDrawableState(extraSpace);
        }
    }

    public void setTintList(ColorStateList tintList) {
        mTintList = tintList;
        if (tintList != null) {
            setTextColor(tintList);
        }
    }

}
