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

import static com.google.android.material.internal.ThemeEnforcement.createThemedContext;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import androidx.annotation.VisibleForTesting;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.customview.view.AbsSavedState;
import androidx.core.view.ViewCompat;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.behavior.HideBottomViewOnScrollBehavior;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;

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

  private static final int DEF_STYLE_RES = R.style.Widget_Design_BottomNavigationView;
  private static final int MENU_PRESENTER_ID = 1;

  @NonNull private final MenuBuilder menu;
  @NonNull @VisibleForTesting final BottomNavigationMenuView menuView;
  private final BottomNavigationPresenter presenter = new BottomNavigationPresenter();
  @Nullable private ColorStateList itemRippleColor;
  private MenuInflater menuInflater;

  private OnNavigationItemSelectedListener selectedListener;
  private OnNavigationItemReselectedListener reselectedListener;

  public BottomNavigationView(@NonNull Context context) {
    this(context, null);
  }

  public BottomNavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.bottomNavigationStyle);
  }

  public BottomNavigationView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(createThemedContext(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

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
            R.style.Widget_Design_BottomNavigationView,
            R.styleable.BottomNavigationView_itemTextAppearanceInactive,
            R.styleable.BottomNavigationView_itemTextAppearanceActive);

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

    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      // Add a MaterialShapeDrawable as background that supports tinting in every API level.
      ViewCompat.setBackground(this, createMaterialShapeDrawableBackground(context));
    }

    if (a.hasValue(R.styleable.BottomNavigationView_elevation)) {
      ViewCompat.setElevation(
          this, a.getDimensionPixelSize(R.styleable.BottomNavigationView_elevation, 0));
    }

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(
            context, a, R.styleable.BottomNavigationView_backgroundTint);
    DrawableCompat.setTintList(getBackground().mutate(), backgroundTint);

    setLabelVisibilityMode(
        a.getInteger(
            R.styleable.BottomNavigationView_labelVisibilityMode,
            LabelVisibilityMode.LABEL_VISIBILITY_AUTO));
    setItemHorizontalTranslationEnabled(
        a.getBoolean(R.styleable.BottomNavigationView_itemHorizontalTranslationEnabled, true));

    int itemBackground = a.getResourceId(R.styleable.BottomNavigationView_itemBackground, 0);
    if (itemBackground != 0) {
      menuView.setItemBackgroundRes(itemBackground);
    } else {
      ColorStateList itemRippleColor =
          MaterialResources.getColorStateList(
              context, a, R.styleable.BottomNavigationView_itemRippleColor);
      setItemRippleColor(itemRippleColor);
    }

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
          public boolean onMenuItemSelected(MenuBuilder menu, @NonNull MenuItem item) {
            if (reselectedListener != null && item.getItemId() == getSelectedItemId()) {
              reselectedListener.onNavigationItemReselected(item);
              return true; // item is already selected
            }
            return selectedListener != null && !selectedListener.onNavigationItemSelected(item);
          }

          @Override
          public void onMenuModeChange(MenuBuilder menu) {}
        });

    applyWindowInsets();
  }

  private void applyWindowInsets() {
    ViewUtils.doOnApplyWindowInsets(
        this,
        new ViewUtils.OnApplyWindowInsetsListener() {
          @NonNull
          @Override
          public androidx.core.view.WindowInsetsCompat onApplyWindowInsets(
              View view,
              @NonNull androidx.core.view.WindowInsetsCompat insets,
              @NonNull RelativePadding initialPadding) {
            initialPadding.bottom += insets.getSystemWindowInsetBottom();
            initialPadding.applyToView(view);
            return insets;
          }
        });
  }

  @NonNull
  private MaterialShapeDrawable createMaterialShapeDrawableBackground(Context context) {
    MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable();
    Drawable originalBackground = getBackground();
    if (originalBackground instanceof ColorDrawable) {
      materialShapeDrawable.setFillColor(
          ColorStateList.valueOf(((ColorDrawable) originalBackground).getColor()));
    }
    materialShapeDrawable.initializeElevationOverlay(context);
    return materialShapeDrawable;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  /**
   * Sets the base elevation of this view, in pixels.
   *
   * @attr ref android.R.styleable#View_elevation
   */
  @RequiresApi(VERSION_CODES.LOLLIPOP)
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);

    MaterialShapeUtils.setElevation(this, elevation);
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
   * <p>This will remove any ripple backgrounds created by {@link setItemRippleColor()}.
   *
   * @param resId The identifier of the resource.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackgroundResource(@DrawableRes int resId) {
    menuView.setItemBackgroundRes(resId);
    itemRippleColor = null;
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
   * <p>This will remove any ripple backgrounds created by {@link setItemRippleColor()}.
   *
   * @param background The drawable for the background.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackground(@Nullable Drawable background) {
    menuView.setItemBackground(background);
    itemRippleColor = null;
  }

  /**
   * Returns the color used to create a ripple as the background drawable of the menu items. If a
   * background is set using {@link #setItemBackground()}, this will return null.
   *
   * @see #setItemBackground(Drawable)
   * @attr ref R.styleable#BottomNavigationView_itemRippleColor
   */
  @Nullable
  public ColorStateList getItemRippleColor() {
    return itemRippleColor;
  }

  /**
   * Set the background of our menu items to be a ripple with the given colors.
   *
   * @param itemRippleColor The {@link ColorStateList} for the ripple. This will create a ripple
   *     background for menu items, replacing any background previously set by {@link
   *     #setItemBackground()}.
   * @attr ref R.styleable#BottomNavigationView_itemRippleColor
   */
  public void setItemRippleColor(@Nullable ColorStateList itemRippleColor) {
    if (this.itemRippleColor == itemRippleColor) {
      // Clear the item background when setItemRippleColor(null) is called for consistency.
      if (itemRippleColor == null && menuView.getItemBackground() != null) {
        menuView.setItemBackground(null);
      }
      return;
    }

    this.itemRippleColor = itemRippleColor;
    if (itemRippleColor == null) {
      menuView.setItemBackground(null);
    } else {
      ColorStateList rippleDrawableColor =
          RippleUtils.convertToRippleDrawableColor(itemRippleColor);
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        menuView.setItemBackground(new RippleDrawable(rippleDrawableColor, null, null));
      } else {
        GradientDrawable rippleDrawable = new GradientDrawable();
        // TODO: Find a workaround for this. Currently on certain devices/versions, LayerDrawable
        // will draw a black background underneath any layer with a non-opaque color,
        // (e.g. ripple) unless we set the shape to be something that's not a perfect rectangle.
        rippleDrawable.setCornerRadius(0.00001F);
        Drawable rippleDrawableCompat = DrawableCompat.wrap(rippleDrawable);
        DrawableCompat.setTintList(rippleDrawableCompat, rippleDrawableColor);
        menuView.setItemBackground(rippleDrawableCompat);
      }
    }
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

  /**
   * Returns an instance of {@link BadgeDrawable} associated with {@code menuItemId}, null if none
   * was initialized.
   *
   * @param menuItemId Id of the menu item.
   * @return an instance of BadgeDrawable associated with {@code menuItemId} or null.
   * @see #getOrCreateBadge(int)
   */
  @Nullable
  public BadgeDrawable getBadge(int menuItemId) {
    return menuView.getBadge(menuItemId);
  }

  /**
   * Creates an instance of {@link BadgeDrawable} associated with {@code menuItemId} if none exists.
   * Initializes (if needed) and returns the associated instance of {@link BadgeDrawable} associated
   * with {@code menuItemId}.
   *
   * @param menuItemId Id of the menu item.
   * @return an instance of BadgeDrawable associated with {@code menuItemId}.
   */
  public BadgeDrawable getOrCreateBadge(int menuItemId) {
    return menuView.getOrCreateBadge(menuItemId);
  }

  /**
   * Removes the {@link BadgeDrawable} associated with {@code menuItemId}. Do nothing if none
   * exists. Consider changing the visibility of the {@link BadgeDrawable} if you only want to hide
   * it temporarily.
   *
   * @param menuItemId Id of the menu item.
   */
  public void removeBadge(int menuItemId) {
    menuView.removeBadge(menuItemId);
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
    @Nullable Bundle menuPresenterState;

    public SavedState(Parcelable superState) {
      super(superState);
    }

    public SavedState(@NonNull Parcel source, ClassLoader loader) {
      super(source, loader);
      if (loader == null) {
        loader = getClass().getClassLoader();
      }
      readFromParcel(source, loader);
    }

    @Override
    public void writeToParcel(@NonNull Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeBundle(menuPresenterState);
    }

    private void readFromParcel(@NonNull Parcel in, ClassLoader loader) {
      menuPresenterState = in.readBundle(loader);
    }

    public static final Creator<SavedState> CREATOR =
        new ClassLoaderCreator<SavedState>() {
          @NonNull
          @Override
          public SavedState createFromParcel(@NonNull Parcel in, ClassLoader loader) {
            return new SavedState(in, loader);
          }

          @Nullable
          @Override
          public SavedState createFromParcel(@NonNull Parcel in) {
            return new SavedState(in, null);
          }

          @NonNull
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}
