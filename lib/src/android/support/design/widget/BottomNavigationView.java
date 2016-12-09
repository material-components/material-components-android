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

package android.support.design.widget;

import android.content.Context;
import android.content.res.ColorStateList;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.R;
import android.support.design.internal.BottomNavigationMenu;
import android.support.design.internal.BottomNavigationMenuView;
import android.support.design.internal.BottomNavigationPresenter;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * <p>
 * Represents a standard bottom navigation bar for application. It is an implementation of
 * <a href="https://material.google.com/components/bottom-navigation.html">material design bottom
 * navigation</a>.
 * </p>
 *
 * <p>
 * Bottom navigation bars make it easy for users to explore and switch between top-level views in
 * a single tap. It should be used when application has three to five top-level destinations.
 * </p>
 *
 * <p>
 * The bar contents can be populated by specifying a menu resource file. Each menu item title, icon
 * and enabled state will be used for displaying bottom navigation bar items.
 * </p>
 *
 * <pre>
 * layout resource file:
 * &lt;android.support.design.widget.BottomNavigationView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:design="http://schema.android.com/apk/res/android.support.design"
 *     android:id="@+id/navigation"
 *     android:layout_width="match_parent"
 *     android:layout_height="56dp"
 *     android:layout_gravity="start"
 *     design:menu="@menu/my_navigation_items" /&gt;
 *
 * res/menu/my_navigation_items.xml:
 * &lt;menu xmlns:android="http://schemas.android.com/apk/res/android"&gt;
 *     &lt;item android:id="@+id/action_search"
 *          android:title="@string/menu_search"
 *          android:icon="@drawable/ic_search" /&gt;
 *     &lt;item android:id="@+id/action_settings"
 *          android:title="@string/menu_settings"
 *          android:icon="@drawable/ic_add" /&gt;
 *     &lt;item android:id="@+id/action_navigation"
 *          android:title="@string/menu_navigation"
 *          android:icon="@drawable/ic_action_navigation_menu" /&gt;
 * &lt;/menu&gt;
 * </pre>
 */
public class BottomNavigationView extends FrameLayout {

    private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
    private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

    private final MenuBuilder mMenu;
    private final BottomNavigationMenuView mMenuView;
    private final BottomNavigationPresenter mPresenter = new BottomNavigationPresenter();
    private MenuInflater mMenuInflater;

    private OnNavigationItemSelectedListener mListener;

    public BottomNavigationView(Context context) {
        this(context, null);
    }

    public BottomNavigationView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        ThemeUtils.checkAppCompatTheme(context);

        // Create the menu
        mMenu = new BottomNavigationMenu(context);

        mMenuView = new BottomNavigationMenuView(context);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;
        mMenuView.setLayoutParams(params);

        mPresenter.setBottomNavigationMenuView(mMenuView);
        mMenuView.setPresenter(mPresenter);
        mMenu.addMenuPresenter(mPresenter);


        // Custom attributes
        TintTypedArray a = TintTypedArray.obtainStyledAttributes(context, attrs,
                R.styleable.BottomNavigationView, defStyleAttr,
                R.style.Widget_Design_BottomNavigationView);

        if (a.hasValue(R.styleable.BottomNavigationView_itemIconTint)) {
            mMenuView.setIconTintList(
                    a.getColorStateList(R.styleable.BottomNavigationView_itemIconTint));
        } else {
            mMenuView.setIconTintList(
                    createDefaultColorStateList(android.R.attr.textColorSecondary));
        }
        if (a.hasValue(R.styleable.BottomNavigationView_itemTextColor)) {
            mMenuView.setItemTextColor(
                    a.getColorStateList(R.styleable.BottomNavigationView_itemTextColor));
        } else {
            mMenuView.setItemTextColor(
                    createDefaultColorStateList(android.R.attr.textColorSecondary));
        }

        int itemBackground = a.getResourceId(R.styleable.BottomNavigationView_itemBackground, 0);
        mMenuView.setItemBackgroundRes(itemBackground);

        if (a.hasValue(R.styleable.BottomNavigationView_menu)) {
            inflateMenu(a.getResourceId(R.styleable.BottomNavigationView_menu, 0));
        }
        a.recycle();

        addView(mMenuView, params);

        mMenu.setCallback(new MenuBuilder.Callback() {
            @Override
            public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
                return mListener != null && !mListener.onNavigationItemSelected(item);
            }

            @Override
            public void onMenuModeChange(MenuBuilder menu) {}
        });
    }

    /**
     * Set a listener that will be notified when a bottom navigation item is selected.
     *
     * @param listener The listener to notify
     */
    public void setOnNavigationItemSelectedListener(
            @Nullable OnNavigationItemSelectedListener listener) {
        mListener = listener;
    }

    /**
     * Returns the {@link Menu} instance associated with this bottom navigation bar.
     */
    @NonNull
    public Menu getMenu() {
        return mMenu;
    }

    /**
     * Inflate a menu resource into this navigation view.
     *
     * <p>Existing items in the menu will not be modified or removed.</p>
     *
     * @param resId ID of a menu resource to inflate
     */
    public void inflateMenu(int resId) {
        mPresenter.setUpdateSuspended(true);
        getMenuInflater().inflate(resId, mMenu);
        mPresenter.initForMenu(getContext(), mMenu);
        mPresenter.setUpdateSuspended(false);
        mPresenter.updateMenuView(true);
    }

    /**
     * @return The maximum number of items that can be shown in BottomNavigationView.
     */
    public int getMaxItemCount() {
        return BottomNavigationMenu.MAX_ITEM_COUNT;
    }

    /**
     * Returns the tint which is applied to our menu items' icons.
     *
     * @see #setItemIconTintList(ColorStateList)
     *
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    @Nullable
    public ColorStateList getItemIconTintList() {
        return mMenuView.getIconTintList();
    }

    /**
     * Set the tint which is applied to our menu items' icons.
     *
     * @param tint the tint to apply.
     *
     * @attr ref R.styleable#BottomNavigationView_itemIconTint
     */
    public void setItemIconTintList(@Nullable ColorStateList tint) {
        mMenuView.setIconTintList(tint);
    }

    /**
     * Returns the text color used on menu items.
     *
     * @see #setItemTextColor(ColorStateList)
     *
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    @Nullable
    public ColorStateList getItemTextColor() {
        return mMenuView.getItemTextColor();
    }

    /**
     * Set the text color to be used on menu items.
     *
     * @see #getItemTextColor()
     *
     * @attr ref R.styleable#BottomNavigationView_itemTextColor
     */
    public void setItemTextColor(@Nullable ColorStateList textColor) {
        mMenuView.setItemTextColor(textColor);
    }

    /**
     * Returns the background resource of the menu items.
     *
     * @see #setItemBackgroundResource(int)
     *
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    @DrawableRes
    public int getItemBackgroundResource() {
        return mMenuView.getItemBackgroundRes();
    }

    /**
     * Set the background of our menu items to the given resource.
     *
     * @param resId The identifier of the resource.
     *
     * @attr ref R.styleable#BottomNavigationView_itemBackground
     */
    public void setItemBackgroundResource(@DrawableRes int resId) {
        mMenuView.setItemBackgroundRes(resId);
    }

    /**
     * Listener for handling events on bottom navigation items.
     */
    public interface OnNavigationItemSelectedListener {

        /**
         * Called when an item in the bottom navigation menu is selected.
         *
         * @param item The selected item
         *
         * @return true to display the item as the selected item and false if the item should not
         *         be selected. Consider setting non-selectable items as disabled preemptively to
         *         make them appear non-interactive.
         */
        boolean onNavigationItemSelected(@NonNull MenuItem item);
    }

    private MenuInflater getMenuInflater() {
        if (mMenuInflater == null) {
            mMenuInflater = new SupportMenuInflater(getContext());
        }
        return mMenuInflater;
    }

    private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
        final TypedValue value = new TypedValue();
        if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
            return null;
        }
        ColorStateList baseColor = AppCompatResources.getColorStateList(
                getContext(), value.resourceId);
        if (!getContext().getTheme().resolveAttribute(
                android.support.v7.appcompat.R.attr.colorPrimary, value, true)) {
            return null;
        }
        int colorPrimary = value.data;
        int defaultColor = baseColor.getDefaultColor();
        return new ColorStateList(new int[][]{
                DISABLED_STATE_SET,
                CHECKED_STATE_SET,
                EMPTY_STATE_SET
        }, new int[]{
                baseColor.getColorForState(DISABLED_STATE_SET, defaultColor),
                colorPrimary,
                defaultColor
        });
    }
}
