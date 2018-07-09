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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DimenRes;
import android.support.annotation.Dimension;
import android.support.annotation.DrawableRes;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.internal.ThemeEnforcement;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.AbsSavedState;
import android.support.v4.view.ViewCompat;
import android.support.v7.view.SupportMenuInflater;
import android.support.v7.view.menu.MenuBuilder;
import android.support.v7.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Represents a standard bottom navigation bar for application. It is an implementation of <a
 * href="https://material.google.com/components/bottom-navigation.html">material design bottom
 * navigation</a>.
 *
 * <p>Bottom navigation bars make it easy for users to explore and switch between top-level views in
 * a single tap. They should be used when an application has three to five top-level destinations.
 *
 * <p>The bar can disappear on scroll, based on {@link
 * com.google.android.material.behavior.HideBottomViewOnScrollBehavior}, when it is placed within a
 * {@link CoordinatorLayout} and one of the children within the {@link CoordinatorLayout} is
 * scrolled. This behavior is only set if the {@code layout_behavior} property is set to {@link
 * HideBottomViewOnScrollBehavior}.
 *
 * <p>The bar contents can be populated by specifying a menu resource file. Each menu item title,
 * icon and enabled state will be used for displaying bottom navigation bar items. Menu items can
 * also be used for programmatically selecting which destination is currently active. It can be done
 * using {@code MenuItem#setChecked(true)}
 *
 * <pre>
 * layout resource file:
 * &lt;com.google.android.material.bottomnavigation.BottomNavigationView
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schema.android.com/apk/res/res-auto"
 *     android:id="@+id/navigation"
 *     android:layout_width="match_parent"
 *     android:layout_height="56dp"
 *     android:layout_gravity="start"
 *     app:menu="@menu/my_navigation_items" /&gt;
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

  private static final int MENU_PRESENTER_ID = 1;

  private final MenuBuilder menu;
  private final BottomNavigationMenuView menuView;
  private final BottomNavigationPresenter presenter = new BottomNavigationPresenter();
  private MenuInflater menuInflater;

  private OnNavigationItemSelectedListener selectedListener;
  private OnNavigationItemReselectedListener reselectedListener;

  public BottomNavigationView(Context context) {
    this(context, null);
  }

  public BottomNavigationView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.bottomNavigationStyle);
  }

  public BottomNavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Create the menu
    this.menu = new BottomNavigationMenu(context);

    menuView = new BottomNavigationMenuView(context);
    FrameLayout.LayoutParams params =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
    params.gravity = Gravity.CENTER;
    menuView.setLayoutParams(params);

    presenter.setBottomNavigationMenuView(menuView);
    presenter.setId(MENU_PRESENTER_ID);
    menuView.setPresenter(presenter);
    this.menu.addMenuPresenter(presenter);
    presenter.initForMenu(getContext(), this.menu);

    // Custom attributes
    TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.BottomNavigationView,
            defStyleAttr,
            R.style.Widget_Design_BottomNavigationView);

    if (a.hasValue(R.styleable.BottomNavigationView_itemIconTint)) {
      menuView.setIconTintList(a.getColorStateList(R.styleable.BottomNavigationView_itemIconTint));
    } else {
      menuView.setIconTintList(
          menuView.createDefaultColorStateList(android.R.attr.textColorSecondary));
    }

    setItemIconSize(
        a.getDimensionPixelSize(
            R.styleable.BottomNavigationView_itemIconSize,
            getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_icon_size)));
    if (a.hasValue(R.styleable.BottomNavigationView_itemTextAppearanceInactive)) {
      setItemTextAppearanceInactive(
          a.getResourceId(R.styleable.BottomNavigationView_itemTextAppearanceInactive, 0));
    }
    if (a.hasValue(R.styleable.BottomNavigationView_itemTextAppearanceActive)) {
      setItemTextAppearanceActive(
          a.getResourceId(R.styleable.BottomNavigationView_itemTextAppearanceActive, 0));
    }

    if (a.hasValue(R.styleable.BottomNavigationView_itemTextColor)) {
      setItemTextColor(a.getColorStateList(R.styleable.BottomNavigationView_itemTextColor));
    }

    if (a.hasValue(R.styleable.BottomNavigationView_elevation)) {
      ViewCompat.setElevation(
          this, a.getDimensionPixelSize(R.styleable.BottomNavigationView_elevation, 0));
    }

    setLabelVisibilityMode(
        a.getInteger(
            R.styleable.BottomNavigationView_labelVisibilityMode,
            LabelVisibilityMode.LABEL_VISIBILITY_AUTO));
    setItemHorizontalTranslationEnabled(
        a.getBoolean(R.styleable.BottomNavigationView_itemHorizontalTranslationEnabled, true));

    int itemBackground = a.getResourceId(R.styleable.BottomNavigationView_itemBackground, 0);
    menuView.setItemBackgroundRes(itemBackground);

    if (a.hasValue(R.styleable.BottomNavigationView_menu)) {
      inflateMenu(a.getResourceId(R.styleable.BottomNavigationView_menu, 0));
    }
    a.recycle();

    addView(menuView, params);
    if (Build.VERSION.SDK_INT < 21) {
      addCompatibilityTopDivider(context);
    }

    this.menu.setCallback(
        new MenuBuilder.Callback() {
          @Override
          public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            if (reselectedListener != null && item.getItemId() == getSelectedItemId()) {
              reselectedListener.onNavigationItemReselected(item);
              return true; // item is already selected
            }
            return selectedListener != null && !selectedListener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });
  }

  /**
   * Set a listener that will be notified when a bottom navigation item is selected. This listener
   * will also be notified when the currently selected item is reselected, unless an {@link
   * OnNavigationItemReselectedListener} has also been set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemReselectedListener(OnNavigationItemReselectedListener)
   */
  public void setOnNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    selectedListener = listener;
  }

  /**
   * Set a listener that will be notified when the currently selected bottom navigation item is
   * reselected. This does not require an {@link OnNavigationItemSelectedListener} to be set.
   *
   * @param listener The listener to notify
   * @see #setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener)
   */
  public void setOnNavigationItemReselectedListener(
      @Nullable OnNavigationItemReselectedListener listener) {
    reselectedListener = listener;
  }

  /** Returns the {@link Menu} instance associated with this bottom navigation bar. */
  @NonNull
  public Menu getMenu() {
    return menu;
  }

  /**
   * Inflate a menu resource into this navigation view.
   *
   * <p>Existing items in the menu will not be modified or removed.
   *
   * @param resId ID of a menu resource to inflate
   */
  public void inflateMenu(int resId) {
    presenter.setUpdateSuspended(true);
    getMenuInflater().inflate(resId, menu);
    presenter.setUpdateSuspended(false);
    presenter.updateMenuView(true);
  }

  /** @return The maximum number of items that can be shown in BottomNavigationView. */
  public int getMaxItemCount() {
    return BottomNavigationMenu.MAX_ITEM_COUNT;
  }

  /**
   * Returns the tint which is applied to our menu items' icons.
   *
   * @see #setItemIconTintList(ColorStateList)
   * @attr ref R.styleable#BottomNavigationView_itemIconTint
   */
  @Nullable
  public ColorStateList getItemIconTintList() {
    return menuView.getIconTintList();
  }

  /**
   * Set the tint which is applied to our menu items' icons.
   *
   * @param tint the tint to apply.
   * @attr ref R.styleable#BottomNavigationView_itemIconTint
   */
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    menuView.setIconTintList(tint);
  }

  /**
   * Set the size to provide for the menu item icons.
   *
   * <p>For best image resolution, use an icon with the same size set in this method.
   *
   * @param iconSize the size in pixels to provide for the menu item icons
   * @attr ref R.styleable#BottomNavigationView_itemIconSize
   */
  public void setItemIconSize(@Dimension int iconSize) {
    menuView.setItemIconSize(iconSize);
  }

  /**
   * Set the size to provide for the menu item icons using a resource ID.
   *
   * <p>For best image resolution, use an icon with the same size set in this method.
   *
   * @param iconSizeRes the resource ID for the size to provide for the menu item icons
   * @attr ref R.styleable#BottomNavigationView_itemIconSize
   */
  public void setItemIconSizeRes(@DimenRes int iconSizeRes) {
    setItemIconSize(getResources().getDimensionPixelSize(iconSizeRes));
  }

  /**
   * Returns the size provided for the menu item icons in pixels.
   *
   * @see #setItemIconSize(int)
   * @attr ref R.styleable#BottomNavigationView_itemIconSize
   */
  @Dimension
  public int getItemIconSize() {
    return menuView.getItemIconSize();
  }

  /**
   * Returns colors used for the different states (normal, selected, focused, etc.) of the menu item
   * text.
   *
   * @see #setItemTextColor(ColorStateList)
   * @return the ColorStateList of colors used for the different states of the menu items text.
   * @attr ref R.styleable#BottomNavigationView_itemTextColor
   */
  @Nullable
  public ColorStateList getItemTextColor() {
    return menuView.getItemTextColor();
  }

  /**
   * Set the colors to use for the different states (normal, selected, focused, etc.) of the menu
   * item text.
   *
   * @see #getItemTextColor()
   * @attr ref R.styleable#BottomNavigationView_itemTextColor
   */
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    menuView.setItemTextColor(textColor);
  }

  /**
   * Returns the background resource of the menu items.
   *
   * @see #setItemBackgroundResource(int)
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   * @deprecated Use {@link #getItemBackground()} instead.
   */
  @Deprecated
  @DrawableRes
  public int getItemBackgroundResource() {
    return menuView.getItemBackgroundRes();
  }

  /**
   * Set the background of our menu items to the given resource.
   *
   * @param resId The identifier of the resource.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackgroundResource(@DrawableRes int resId) {
    menuView.setItemBackgroundRes(resId);
  }

  /**
   * Returns the background drawable of the menu items.
   *
   * @see #setItemBackground(Drawable)
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  @Nullable
  public Drawable getItemBackground() {
    return menuView.getItemBackground();
  }

  /**
   * Set the background of our menu items to the given drawable.
   *
   * @param background The drawable for the background.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackground(@Nullable Drawable background) {
    menuView.setItemBackground(background);
  }

  /**
   * Returns the currently selected menu item ID, or zero if there is no menu.
   *
   * @see #setSelectedItemId(int)
   */
  @IdRes
  public int getSelectedItemId() {
    return menuView.getSelectedItemId();
  }

  /**
   * Set the selected menu item ID. This behaves the same as tapping on an item.
   *
   * @param itemId The menu item ID. If no item has this ID, the current selection is unchanged.
   * @see #getSelectedItemId()
   */
  public void setSelectedItemId(@IdRes int itemId) {
    MenuItem item = menu.findItem(itemId);
    if (item != null) {
      if (!menu.performItemAction(item, presenter, 0)) {
        item.setChecked(true);
      }
    }
  }

  /**
   * Sets the navigation items' label visibility mode.
   *
   * <p>The label is either always shown, never shown, or only shown when activated. Also supports
   * "auto" mode, which uses the item count to determine whether to show or hide the label.
   *
   * @attr ref com.google.android.material.R.styleable#BottomNavigationView_labelVisibilityMode
   * @param labelVisibilityMode mode which decides whether or not the label should be shown. Can be
   *     one of {@link LabelVisibilityMode#LABEL_VISIBILITY_AUTO}, {@link
   *     LabelVisibilityMode#LABEL_VISIBILITY_SELECTED}, {@link
   *     LabelVisibilityMode#LABEL_VISIBILITY_LABELED}, or {@link
   *     LabelVisibilityMode#LABEL_VISIBILITY_UNLABELED}
   * @see #getLabelVisibilityMode()
   */
  public void setLabelVisibilityMode(@LabelVisibilityMode int labelVisibilityMode) {
    if (menuView.getLabelVisibilityMode() != labelVisibilityMode) {
      menuView.setLabelVisibilityMode(labelVisibilityMode);
      presenter.updateMenuView(false);
    }
  }

  /**
   * Returns the current label visibility mode used by this {@link BottomNavigationView}.
   *
   * @attr ref com.google.android.material.R.styleable#BottomNavigationView_labelVisibilityMode
   * @see #setLabelVisibilityMode(int)
   */
  @LabelVisibilityMode
  public int getLabelVisibilityMode() {
    return menuView.getLabelVisibilityMode();
  }

  /**
   * Sets the text appearance to be used for inactive menu item labels.
   *
   * @param textAppearanceRes the text appearance ID used for inactive menu item labels
   */
  public void setItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    menuView.setItemTextAppearanceInactive(textAppearanceRes);
  }

  /**
   * Returns the text appearance used for inactive menu item labels.
   *
   * @return the text appearance ID used for inactive menu item labels
   */
  @StyleRes
  public int getItemTextAppearanceInactive() {
    return menuView.getItemTextAppearanceInactive();
  }

  /**
   * Sets the text appearance to be used for the menu item labels.
   *
   * @param textAppearanceRes the text appearance ID used for menu item labels
   */
  public void setItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    menuView.setItemTextAppearanceActive(textAppearanceRes);
  }

  /**
   * Returns the text appearance used for the active menu item label.
   *
   * @return the text appearance ID used for the active menu item label
   */
  @StyleRes
  public int getItemTextAppearanceActive() {
    return menuView.getItemTextAppearanceActive();
  }

  /**
   * Sets whether the menu items horizontally translate on selection when the combined item widths
   * fill up the screen.
   *
   * @param itemHorizontalTranslationEnabled whether the items horizontally translate on selection
   * @see #isItemHorizontalTranslationEnabled()
   */
  public void setItemHorizontalTranslationEnabled(boolean itemHorizontalTranslationEnabled) {
    if (menuView.isItemHorizontalTranslationEnabled() != itemHorizontalTranslationEnabled) {
      menuView.setItemHorizontalTranslationEnabled(itemHorizontalTranslationEnabled);
      presenter.updateMenuView(false);
    }
  }

  /**
   * Returns whether the items horizontally translate on selection when the item widths fill up the
   * screen.
   *
   * @return whether the menu items horizontally translate on selection
   * @see #setItemHorizontalTranslationEnabled(boolean)
   */
  public boolean isItemHorizontalTranslationEnabled() {
    return menuView.isItemHorizontalTranslationEnabled();
  }

  /** Listener for handling selection events on bottom navigation items. */
  public interface OnNavigationItemSelectedListener {

    /**
     * Called when an item in the bottom navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     *     selected. Consider setting non-selectable items as disabled preemptively to make them
     *     appear non-interactive.
     */
    boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  /** Listener for handling reselection events on bottom navigation items. */
  public interface OnNavigationItemReselectedListener {

    /**
     * Called when the currently selected item in the bottom navigation menu is selected again.
     *
     * @param item The selected item
     */
    void onNavigationItemReselected(@NonNull MenuItem item);
  }

  private void addCompatibilityTopDivider(Context context) {
    View divider = new View(context);
    divider.setBackgroundColor(
        ContextCompat.getColor(context, R.color.design_bottom_navigation_shadow_color));
    FrameLayout.LayoutParams dividerParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            getResources().getDimensionPixelSize(R.dimen.design_bottom_navigation_shadow_height));
    divider.setLayoutParams(dividerParams);
    addView(divider);
  }

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.menuPresenterState = new Bundle();
    menu.savePresenterStates(savedState.menuPresenterState);
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    if (!(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }
    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    menu.restorePresenterStates(savedState.menuPresenterState);
  }

  static class SavedState extends AbsSavedState {
    Bundle menuPresenterState;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(Parcel source, ClassLoader loader) {
      super(source, loader);
      readFromParcel(source, loader);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeBundle(menuPresenterState);
    }

    private void readFromParcel(Parcel in, ClassLoader loader) {
      menuPresenterState = in.readBundle(loader);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in, null);
          }

          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
