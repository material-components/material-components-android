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

package com.google.android.material.navigation;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.internal.NavigationMenuPresenter;
import com.google.android.material.internal.ScrimInsetsFrameLayout;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.shape.MaterialShapeDrawable;
import androidx.core.content.ContextCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

/**
 * Represents a standard navigation menu for application. The menu contents can be populated by a
 * menu resource file.
 *
 * <p>NavigationView is typically placed inside a {@link androidx.drawerlayout.widget.DrawerLayout}.
 *
 * <pre>
 * &lt;androidx.drawerlayout.widget.DrawerLayout
 *     xmlns:android="http://schemas.android.com/apk/res/android"
 *     xmlns:app="http://schemas.android.com/apk/res-auto"
 *     android:id="@+id/drawer_layout"
 *     android:layout_width="match_parent"
 *     android:layout_height="match_parent"
 *     android:fitsSystemWindows="true"&gt;
 *
 *     &lt;!-- Your contents --&gt;
 *
 *     &lt;com.google.android.material.navigation.NavigationView
 *         android:id="@+id/navigation"
 *         android:layout_width="wrap_content"
 *         android:layout_height="match_parent"
 *         android:layout_gravity="start"
 *         app:menu="@menu/my_navigation_items" /&gt;
 * &lt;/androidx.drawerlayout.widget.DrawerLayout&gt;
 * </pre>
 */
public class NavigationView extends ScrimInsetsFrameLayout {

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  private static final int PRESENTER_NAVIGATION_VIEW_ID = 1;

  private final NavigationMenu menu;
  private final NavigationMenuPresenter presenter = new NavigationMenuPresenter();

  OnNavigationItemSelectedListener listener;
  private final int maxWidth;

  private MenuInflater menuInflater;

  public NavigationView(Context context) {
    this(context, null);
  }

  public NavigationView(Context context, AttributeSet attrs) {
    this(context, attrs, R.attr.navigationViewStyle);
  }

  public NavigationView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Create the menu
    this.menu = new NavigationMenu(context);

    // Custom attributes
    TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.NavigationView,
            defStyleAttr,
            R.style.Widget_Design_NavigationView);

    if (a.hasValue(R.styleable.NavigationView_android_background)) {
      ViewCompat.setBackground(this, a.getDrawable(R.styleable.NavigationView_android_background));
    }

    // Set the background to a MaterialShapeDrawable if it hasn't been set or if it can be converted
    // to a MaterialShapeDrawable.
    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      Drawable orig = getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
      if (orig instanceof ColorDrawable) {
        materialShapeDrawable.setFillColor(
            ColorStateList.valueOf(((ColorDrawable) orig).getColor()));
      }
      materialShapeDrawable.initializeElevationOverlay(context);
      ViewCompat.setBackground(this, materialShapeDrawable);
    }

    if (a.hasValue(R.styleable.NavigationView_elevation)) {
      setElevation(a.getDimensionPixelSize(R.styleable.NavigationView_elevation, 0));
    }
    setFitsSystemWindows(a.getBoolean(R.styleable.NavigationView_android_fitsSystemWindows, false));

    maxWidth = a.getDimensionPixelSize(R.styleable.NavigationView_android_maxWidth, 0);

    final ColorStateList itemIconTint;
    if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
      itemIconTint = a.getColorStateList(R.styleable.NavigationView_itemIconTint);
    } else {
      itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
    }

    boolean textAppearanceSet = false;
    int textAppearance = 0;
    if (a.hasValue(R.styleable.NavigationView_itemTextAppearance)) {
      textAppearance = a.getResourceId(R.styleable.NavigationView_itemTextAppearance, 0);
      textAppearanceSet = true;
    }

    if (a.hasValue(R.styleable.NavigationView_itemIconSize)) {
      setItemIconSize(a.getDimensionPixelSize(R.styleable.NavigationView_itemIconSize, 0));
    }

    ColorStateList itemTextColor = null;
    if (a.hasValue(R.styleable.NavigationView_itemTextColor)) {
      itemTextColor = a.getColorStateList(R.styleable.NavigationView_itemTextColor);
    }

    if (!textAppearanceSet && itemTextColor == null) {
      // If there isn't a text appearance set, we'll use a default text color
      itemTextColor = createDefaultColorStateList(android.R.attr.textColorPrimary);
    }

    final Drawable itemBackground = a.getDrawable(R.styleable.NavigationView_itemBackground);

    if (a.hasValue(R.styleable.NavigationView_itemHorizontalPadding)) {
      final int itemHorizontalPadding =
          a.getDimensionPixelSize(R.styleable.NavigationView_itemHorizontalPadding, 0);
      presenter.setItemHorizontalPadding(itemHorizontalPadding);
    }
    final int itemIconPadding =
        a.getDimensionPixelSize(R.styleable.NavigationView_itemIconPadding, 0);

    setItemMaxLines(a.getInt(R.styleable.NavigationView_itemMaxLines, 1));

    this.menu.setCallback(
        new MenuBuilder.Callback() {
          @Override
          public boolean onMenuItemSelected(MenuBuilder menu, MenuItem item) {
            return listener != null && listener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });
    presenter.setId(PRESENTER_NAVIGATION_VIEW_ID);
    presenter.initForMenu(context, this.menu);
    presenter.setItemIconTintList(itemIconTint);
    if (textAppearanceSet) {
      presenter.setItemTextAppearance(textAppearance);
    }
    presenter.setItemTextColor(itemTextColor);
    presenter.setItemBackground(itemBackground);
    presenter.setItemIconPadding(itemIconPadding);
    this.menu.addMenuPresenter(presenter);
    addView((View) presenter.getMenuView(this));

    if (a.hasValue(R.styleable.NavigationView_menu)) {
      inflateMenu(a.getResourceId(R.styleable.NavigationView_menu, 0));
    }

    if (a.hasValue(R.styleable.NavigationView_headerLayout)) {
      inflateHeaderView(a.getResourceId(R.styleable.NavigationView_headerLayout, 0));
    }

    a.recycle();
  }

  @Override
  public void setElevation(float elevation) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      super.setElevation(elevation);
    }
    Drawable background = getBackground();
    if (background instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) background).setElevation(elevation);
    }
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState state = new SavedState(superState);
    state.menuState = new Bundle();
    menu.savePresenterStates(state.menuState);
    return state;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable savedState) {
    if (!(savedState instanceof SavedState)) {
      super.onRestoreInstanceState(savedState);
      return;
    }
    SavedState state = (SavedState) savedState;
    super.onRestoreInstanceState(state.getSuperState());
    menu.restorePresenterStates(state.menuState);
  }

  /**
   * Set a listener that will be notified when a menu item is selected.
   *
   * @param listener The listener to notify
   */
  public void setNavigationItemSelectedListener(
      @Nullable OnNavigationItemSelectedListener listener) {
    this.listener = listener;
  }

  @Override
  protected void onMeasure(int widthSpec, int heightSpec) {
    switch (MeasureSpec.getMode(widthSpec)) {
      case MeasureSpec.EXACTLY:
        // Nothing to do
        break;
      case MeasureSpec.AT_MOST:
        widthSpec =
            MeasureSpec.makeMeasureSpec(
                Math.min(MeasureSpec.getSize(widthSpec), maxWidth), MeasureSpec.EXACTLY);
        break;
      case MeasureSpec.UNSPECIFIED:
        widthSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.EXACTLY);
        break;
    }
    // Let super sort out the height
    super.onMeasure(widthSpec, heightSpec);
  }

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  protected void onInsetsChanged(WindowInsetsCompat insets) {
    presenter.dispatchApplyWindowInsets(insets);
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
    presenter.updateMenuView(false);
  }

  /** Returns the {@link Menu} instance associated with this navigation view. */
  public Menu getMenu() {
    return menu;
  }

  /**
   * Inflates a View and add it as a header of the navigation menu.
   *
   * @param res The layout resource ID.
   * @return a newly inflated View.
   */
  public View inflateHeaderView(@LayoutRes int res) {
    return presenter.inflateHeaderView(res);
  }

  /**
   * Adds a View as a header of the navigation menu.
   *
   * @param view The view to be added as a header of the navigation menu.
   */
  public void addHeaderView(@NonNull View view) {
    presenter.addHeaderView(view);
  }

  /**
   * Removes a previously-added header view.
   *
   * @param view The view to remove
   */
  public void removeHeaderView(@NonNull View view) {
    presenter.removeHeaderView(view);
  }

  /**
   * Gets the number of headers in this NavigationView.
   *
   * @return A positive integer representing the number of headers.
   */
  public int getHeaderCount() {
    return presenter.getHeaderCount();
  }

  /**
   * Gets the header view at the specified position.
   *
   * @param index The position at which to get the view from.
   * @return The header view the specified position or null if the position does not exist in this
   *     NavigationView.
   */
  public View getHeaderView(int index) {
    return presenter.getHeaderView(index);
  }

  /**
   * Returns the tint which is applied to our menu items' icons.
   *
   * @see #setItemIconTintList(ColorStateList)
   * @attr ref R.styleable#NavigationView_itemIconTint
   */
  @Nullable
  public ColorStateList getItemIconTintList() {
    return presenter.getItemTintList();
  }

  /**
   * Set the tint which is applied to our menu items' icons.
   *
   * @param tint the tint to apply.
   * @attr ref R.styleable#NavigationView_itemIconTint
   */
  public void setItemIconTintList(@Nullable ColorStateList tint) {
    presenter.setItemIconTintList(tint);
  }

  /**
   * Returns the tint which is applied to our menu items' icons.
   *
   * @see #setItemTextColor(ColorStateList)
   * @attr ref R.styleable#NavigationView_itemTextColor
   */
  @Nullable
  public ColorStateList getItemTextColor() {
    return presenter.getItemTextColor();
  }

  /**
   * Set the text color to be used on our menu items.
   *
   * @see #getItemTextColor()
   * @attr ref R.styleable#NavigationView_itemTextColor
   */
  public void setItemTextColor(@Nullable ColorStateList textColor) {
    presenter.setItemTextColor(textColor);
  }

  /**
   * Returns the background drawable for our menu items.
   *
   * @see #setItemBackgroundResource(int)
   * @attr ref R.styleable#NavigationView_itemBackground
   */
  @Nullable
  public Drawable getItemBackground() {
    return presenter.getItemBackground();
  }

  /**
   * Set the background of our menu items to the given resource.
   *
   * @param resId The identifier of the resource.
   * @attr ref R.styleable#NavigationView_itemBackground
   */
  public void setItemBackgroundResource(@DrawableRes int resId) {
    setItemBackground(ContextCompat.getDrawable(getContext(), resId));
  }

  /**
   * Set the background of our menu items to a given resource. The resource should refer to a
   * Drawable object or null to use the default background set on this navigation menu.
   *
   * @attr ref R.styleable#NavigationView_itemBackground
   */
  public void setItemBackground(@Nullable Drawable itemBackground) {
    presenter.setItemBackground(itemBackground);
  }

  /**
   * Returns the horizontal (left and right) padding in pixels applied to menu items.
   *
   * @see #setItemHorizontalPadding(int)
   * @attr ref R.styleable#NavigationView_itemHorizontalPadding
   */
  @Dimension
  public int getItemHorizontalPadding() {
    return presenter.getItemHorizontalPadding();
  }

  /**
   * Set the horizontal (left and right) padding in pixels of menu items.
   *
   * @param padding The horizontal padding in pixels.
   * @attr ref R.styleable#NavigationView_itemHorizontalPadding
   */
  public void setItemHorizontalPadding(@Dimension int padding) {
    presenter.setItemHorizontalPadding(padding);
  }

  /**
   * Set the horizontal (left and right) padding of menu items.
   *
   * @param paddingResource Dimension resource to use for the horizontal padding.
   * @attr ref R.styleable#NavigationView_itemHorizontalPadding
   */
  public void setItemHorizontalPaddingResource(@DimenRes int paddingResource) {
    presenter.setItemHorizontalPadding(getResources().getDimensionPixelSize(paddingResource));
  }

  /**
   * Returns the padding in pixels between the icon (if present) and the text of menu items.
   *
   * @see #setItemIconPadding(int)
   * @attr ref R.styleable#NavigationView_itemIconPadding
   */
  @Dimension
  public int getItemIconPadding() {
    return presenter.getItemIconPadding();
  }

  /**
   * Set the padding in pixels between the icon (if present) and the text of menu items.
   *
   * @param padding The padding in pixels.
   * @attr ref R.styleable#NavigationView_itemIconPadding
   */
  public void setItemIconPadding(@Dimension int padding) {
    presenter.setItemIconPadding(padding);
  }

  /**
   * Set the padding between the icon (if present) and the text of menu items.
   *
   * @param paddingResource Dimension resource to use for the icon padding.
   * @attr ref R.styleable#NavigationView_itemIconPadding
   */
  public void setItemIconPaddingResource(int paddingResource) {
    presenter.setItemIconPadding(getResources().getDimensionPixelSize(paddingResource));
  }

  /**
   * Sets the currently checked item in this navigation menu.
   *
   * @param id The item ID of the currently checked item.
   */
  public void setCheckedItem(@IdRes int id) {
    MenuItem item = menu.findItem(id);
    if (item != null) {
      presenter.setCheckedItem((MenuItemImpl) item);
    }
  }

  /**
   * Sets the currently checked item in this navigation menu.
   *
   * @param checkedItem The checked item from the menu available from {@link #getMenu()}.
   */
  public void setCheckedItem(@NonNull MenuItem checkedItem) {
    MenuItem item = menu.findItem(checkedItem.getItemId());
    if (item != null) {
      presenter.setCheckedItem((MenuItemImpl) item);
    } else {
      throw new IllegalArgumentException(
          "Called setCheckedItem(MenuItem) with an item that is not in the current menu.");
    }
  }

  /** Returns the currently checked item in this navigation menu. */
  @Nullable
  public MenuItem getCheckedItem() {
    return presenter.getCheckedItem();
  }

  /**
   * Set the text appearance of the menu items to a given resource.
   *
   * @attr ref R.styleable#NavigationView_itemTextAppearance
   */
  public void setItemTextAppearance(@StyleRes int resId) {
    presenter.setItemTextAppearance(resId);
  }

  /**
   * Sets the size to be used for the menu item icons in pixels. If no icons are set, calling this
   * method will do nothing.
   *
   * @attr ref R.styleable#NavigationView_itemIconSize
   */
  public void setItemIconSize(@Dimension int iconSize) {
    presenter.setItemIconSize(iconSize);
  }

  /**
   * Sets the android:maxLines attribute of the text view in the menu item.
   *
   * @attr ref R.styleable#NavigationView_itemMaxLines
   */
  public void setItemMaxLines(int itemMaxLines) {
    presenter.setItemMaxLines(itemMaxLines);
  }

  /**
   * Gets the android:maxLines attribute of the text view in the menu item.
   *
   * @attr ref R.styleable#NavigationView_itemMaxLines
   */
  public int getItemMaxLines() {
    return presenter.getItemMaxLines();
  }

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  private ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(androidx.appcompat.R.attr.colorPrimary, value, true)) {
      return null;
    }
    int colorPrimary = value.data;
    int defaultColor = baseColor.getDefaultColor();
    return new ColorStateList(
        new int[][] {DISABLED_STATE_SET, CHECKED_STATE_SET, EMPTY_STATE_SET},
        new int[] {
          baseColor.getColorForState(DISABLED_STATE_SET, defaultColor), colorPrimary, defaultColor
        });
  }

  /** Listener for handling events on navigation items. */
  public interface OnNavigationItemSelectedListener {

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item
     */
    public boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  /**
   * User interface state that is stored by NavigationView for implementing onSaveInstanceState().
   */
  public static class SavedState extends AbsSavedState {
    public Bundle menuState;

    public SavedState(Parcel in, ClassLoader loader) {
      super(in, loader);
      menuState = in.readBundle(loader);
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeBundle(menuState);
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
