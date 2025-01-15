/*
 * Copyright (C) 2020 The Android Open Source Project
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
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.drawable.DrawableUtils;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Provides an abstract implementation of a navigation bar that can be used to implementation such
 * as <a href="https://material.io/components/bottom-navigation">Bottom Navigation</a> or <a
 * href="https://material.io/components/navigation-rail">Navigation rail</a>.
 *
 * <p>Navigation bars make it easy for users to explore and switch between top-level views in a
 * single tap.
 *
 * <p>The bar contents can be populated by specifying a menu resource file. Each menu item title,
 * icon and enabled state will be used for displaying navigation bar items. Menu items can also be
 * used for programmatically selecting which destination is currently active. It can be done using
 * {@code MenuItem#setChecked(true)}
 */
public abstract class NavigationBarView extends FrameLayout {

  /**
   * Label behaves as "labeled" when there are 3 items or less, or "selected" when there are 4 items
   * or more.
   */
  public static final int LABEL_VISIBILITY_AUTO = -1;

  /** Label is shown on the selected navigation item. */
  public static final int LABEL_VISIBILITY_SELECTED = 0;

  /** Label is shown on all navigation items. */
  public static final int LABEL_VISIBILITY_LABELED = 1;

  /** Label is not shown on any navigation items. */
  public static final int LABEL_VISIBILITY_UNLABELED = 2;

  /** The active indicator width fills up the width of its parent. */
  public static final int ACTIVE_INDICATOR_WIDTH_MATCH_PARENT = -1;

  /** The active indicator width wraps the content. */
  public static final int ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT = -2;

  /**
   * Menu Label visibility mode enum for component provide an implementation of navigation bar view.
   *
   * <p>The label visibility mode determines whether to show or hide labels in the navigation items.
   * Setting the label visibility mode to {@link NavigationBarView#LABEL_VISIBILITY_SELECTED} sets
   * the label to only show when selected, setting it to {@link
   * NavigationBarView#LABEL_VISIBILITY_LABELED} sets the label to always show, and {@link
   * NavigationBarView#LABEL_VISIBILITY_UNLABELED} sets the label to never show.
   *
   * <p>Setting the label visibility mode to {@link NavigationBarView#LABEL_VISIBILITY_AUTO} sets
   * the label to behave as "labeled" when there are 3 items or less, or "selected" when there are 4
   * items or more.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      value = {
        LABEL_VISIBILITY_AUTO,
        LABEL_VISIBILITY_SELECTED,
        LABEL_VISIBILITY_LABELED,
        LABEL_VISIBILITY_UNLABELED
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface LabelVisibility {}

  /** Icon is placed at the top of the item */
  public static final int ITEM_ICON_GRAVITY_TOP = 0;

  /** Icon is placed at the top of the item */
  public static final int ITEM_ICON_GRAVITY_START = 1;

  /**
   * Navigation Bar Item gravity enum to control where the item is in its container.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(value = {ITEM_GRAVITY_TOP_CENTER, ITEM_GRAVITY_CENTER, ITEM_GRAVITY_START_CENTER})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ItemGravity {}

  /** Item is placed at the top center of its container */
  public static final int ITEM_GRAVITY_TOP_CENTER = Gravity.TOP | Gravity.CENTER_HORIZONTAL;

  /** Item is placed at the center of its container */
  public static final int ITEM_GRAVITY_CENTER = Gravity.CENTER;

  /** Item is placed at the start center of its container */
  public static final int ITEM_GRAVITY_START_CENTER = Gravity.START | Gravity.CENTER_VERTICAL;


  /**
   * Navigation Bar Item icon gravity enum to control which item configuration to display.
   *
   * <p>There are 2 item configurations. {@link NavigationBarView#ITEM_ICON_GRAVITY_START} shows the
   * icon at the start of the item in a horizontal configuration, and {@link
   * NavigationBarView#ITEM_ICON_GRAVITY_TOP} shows the icon at the top of the item in a vertical
   * configuration.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef(
      value = {
        ITEM_ICON_GRAVITY_TOP,
        ITEM_ICON_GRAVITY_START,
      })
  @Retention(RetentionPolicy.SOURCE)
  public @interface ItemIconGravity {}

  private static final int MENU_PRESENTER_ID = 1;

  @NonNull private final NavigationBarMenu menu;
  @NonNull private final NavigationBarMenuView menuView;
  @NonNull private final NavigationBarPresenter presenter = new NavigationBarPresenter();
  private MenuInflater menuInflater;

  private OnItemSelectedListener selectedListener;
  private OnItemReselectedListener reselectedListener;

  public NavigationBarView(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes) {
    super(wrap(context, attrs, defStyleAttr, defStyleRes), attrs, defStyleAttr);

    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    /* Custom attributes */
    TintTypedArray attributes =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.NavigationBarView,
            defStyleAttr,
            defStyleRes,
            R.styleable.NavigationBarView_itemTextAppearanceInactive,
            R.styleable.NavigationBarView_itemTextAppearanceActive);

    // Create the menu.
    this.menu =
        new NavigationBarMenu(context, this.getClass(), getMaxItemCount(), isSubMenuSupported());

    // Create the menu view.
    menuView = createNavigationBarMenuView(context);
    menuView.setMinimumHeight(getSuggestedMinimumHeight());
    menuView.setCollapsedMaxItemCount(getCollapsedMaxItemCount());

    presenter.setMenuView(menuView);
    presenter.setId(MENU_PRESENTER_ID);
    menuView.setPresenter(presenter);
    this.menu.addMenuPresenter(presenter);
    presenter.initForMenu(getContext(), this.menu);

    if (attributes.hasValue(R.styleable.NavigationBarView_itemIconTint)) {
      menuView.setIconTintList(
          attributes.getColorStateList(R.styleable.NavigationBarView_itemIconTint));
    } else {
      menuView.setIconTintList(
          menuView.createDefaultColorStateList(android.R.attr.textColorSecondary));
    }

    setItemIconSize(
        attributes.getDimensionPixelSize(
            R.styleable.NavigationBarView_itemIconSize,
            getResources()
                .getDimensionPixelSize(R.dimen.mtrl_navigation_bar_item_default_icon_size)));

    if (attributes.hasValue(R.styleable.NavigationBarView_itemTextAppearanceInactive)) {
      setItemTextAppearanceInactive(
          attributes.getResourceId(R.styleable.NavigationBarView_itemTextAppearanceInactive, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_itemTextAppearanceActive)) {
      setItemTextAppearanceActive(
          attributes.getResourceId(R.styleable.NavigationBarView_itemTextAppearanceActive, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_horizontalItemTextAppearanceInactive)) {
      setHorizontalItemTextAppearanceInactive(
          attributes.getResourceId(R.styleable.NavigationBarView_horizontalItemTextAppearanceInactive, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_horizontalItemTextAppearanceActive)) {
      setHorizontalItemTextAppearanceActive(
          attributes.getResourceId(R.styleable.NavigationBarView_horizontalItemTextAppearanceActive, 0));
    }

    boolean isBold =
        attributes.getBoolean(R.styleable.NavigationBarView_itemTextAppearanceActiveBoldEnabled, true);
    setItemTextAppearanceActiveBoldEnabled(isBold);

    if (attributes.hasValue(R.styleable.NavigationBarView_itemTextColor)) {
      setItemTextColor(attributes.getColorStateList(R.styleable.NavigationBarView_itemTextColor));
    }

    // Add a MaterialShapeDrawable as background that supports tinting in every API level.
    Drawable background = getBackground();
    ColorStateList backgroundColorStateList = DrawableUtils.getColorStateListOrNull(background);

    if (background == null || backgroundColorStateList != null) {
      ShapeAppearanceModel shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, attrs, defStyleAttr, defStyleRes).build();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
      if (backgroundColorStateList != null) {
        // Setting fill color with a transparent CSL will disable the tint list.
        materialShapeDrawable.setFillColor(backgroundColorStateList);
      }
      materialShapeDrawable.initializeElevationOverlay(context);
      setBackground(materialShapeDrawable);
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_itemPaddingTop)) {
      setItemPaddingTop(
          attributes.getDimensionPixelSize(R.styleable.NavigationBarView_itemPaddingTop, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_itemPaddingBottom)) {
      setItemPaddingBottom(
          attributes.getDimensionPixelSize(R.styleable.NavigationBarView_itemPaddingBottom, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_activeIndicatorLabelPadding)) {
      setActiveIndicatorLabelPadding(
          attributes.getDimensionPixelSize(R.styleable.NavigationBarView_activeIndicatorLabelPadding, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_iconLabelHorizontalSpacing)) {
      setIconLabelHorizontalSpacing(
          attributes.getDimensionPixelSize(R.styleable.NavigationBarView_iconLabelHorizontalSpacing, 0));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_elevation)) {
      setElevation(attributes.getDimensionPixelSize(R.styleable.NavigationBarView_elevation, 0));
    }

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(
            context, attributes, R.styleable.NavigationBarView_backgroundTint);
    getBackground().mutate().setTintList(backgroundTint);

    setLabelVisibilityMode(
        attributes.getInteger(
            R.styleable.NavigationBarView_labelVisibilityMode,
            NavigationBarView.LABEL_VISIBILITY_AUTO));
    setItemIconGravity(
        attributes.getInteger(
            R.styleable.NavigationBarView_itemIconGravity,
            NavigationBarView.ITEM_ICON_GRAVITY_TOP));
    setItemGravity(
        attributes.getInteger(
            R.styleable.NavigationBarView_itemGravity, NavigationBarView.ITEM_GRAVITY_TOP_CENTER));

    int itemBackground = attributes.getResourceId(R.styleable.NavigationBarView_itemBackground, 0);
    if (itemBackground != 0) {
      menuView.setItemBackgroundRes(itemBackground);
    } else {
      setItemRippleColor(
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.NavigationBarView_itemRippleColor));
    }

    setMeasureBottomPaddingFromLabelBaseline(attributes.getBoolean(
        R.styleable.NavigationBarView_measureBottomPaddingFromLabelBaseline, true));

    setLabelFontScalingEnabled(
        attributes.getBoolean(R.styleable.NavigationBarView_labelFontScalingEnabled, false));

    setLabelMaxLines(
        attributes.getInteger(R.styleable.NavigationBarView_labelMaxLines, 1));

    int activeIndicatorStyleResId =
        attributes.getResourceId(R.styleable.NavigationBarView_itemActiveIndicatorStyle, 0);

    if (activeIndicatorStyleResId != 0) {
      setItemActiveIndicatorEnabled(true);

      @SuppressLint("CustomViewStyleable")
      TypedArray activeIndicatorAttributes =
          context.obtainStyledAttributes(
              activeIndicatorStyleResId, R.styleable.NavigationBarActiveIndicator);

      int itemActiveIndicatorWidth =
          activeIndicatorAttributes.getDimensionPixelSize(
              R.styleable.NavigationBarActiveIndicator_android_width, 0);
      setItemActiveIndicatorWidth(itemActiveIndicatorWidth);

      int itemActiveIndicatorHeight =
          activeIndicatorAttributes.getDimensionPixelSize(
              R.styleable.NavigationBarActiveIndicator_android_height, 0);
      setItemActiveIndicatorHeight(itemActiveIndicatorHeight);

      int itemActiveIndicatorMarginHorizontal =
          activeIndicatorAttributes.getDimensionPixelOffset(
              R.styleable.NavigationBarActiveIndicator_marginHorizontal, 0);
      setItemActiveIndicatorMarginHorizontal(itemActiveIndicatorMarginHorizontal);

      int itemActiveIndicatorExpandedWidth = ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT;
      String expandedWidthString =
          activeIndicatorAttributes.getString(
              R.styleable.NavigationBarActiveIndicator_expandedWidth);
      if (expandedWidthString != null) {
        if (String.valueOf(ACTIVE_INDICATOR_WIDTH_MATCH_PARENT).equals(expandedWidthString)) {
          itemActiveIndicatorExpandedWidth = ACTIVE_INDICATOR_WIDTH_MATCH_PARENT;
        } else if (String.valueOf(ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT).equals(expandedWidthString)) {
          itemActiveIndicatorExpandedWidth = ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT;
        } else {
          itemActiveIndicatorExpandedWidth = activeIndicatorAttributes.getDimensionPixelSize(
              R.styleable.NavigationBarActiveIndicator_expandedWidth,
              ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT);
        }
      }

      setItemActiveIndicatorExpandedWidth(itemActiveIndicatorExpandedWidth);

      int itemActiveIndicatorExpandedHeight =
          activeIndicatorAttributes.getDimensionPixelSize(
              R.styleable.NavigationBarActiveIndicator_expandedHeight,
              itemActiveIndicatorWidth);
      setItemActiveIndicatorExpandedHeight(itemActiveIndicatorExpandedHeight);

      int itemActiveIndicatorExpandedMarginHorizontal =
          activeIndicatorAttributes.getDimensionPixelOffset(
              R.styleable.NavigationBarActiveIndicator_expandedMarginHorizontal,
              itemActiveIndicatorMarginHorizontal);
      setItemActiveIndicatorExpandedMarginHorizontal(itemActiveIndicatorExpandedMarginHorizontal);

      int activeIndicatorExpandedDefaultStartEndPadding = getResources()
          .getDimensionPixelSize(R.dimen.m3_navigation_item_leading_trailing_space);
      int activeIndicatorExpandedStartPadding =
          activeIndicatorAttributes.getDimensionPixelOffset(
          R.styleable.NavigationBarActiveIndicator_expandedActiveIndicatorPaddingStart,
              activeIndicatorExpandedDefaultStartEndPadding);
      int activeIndicatorExpandedEndPadding =
          activeIndicatorAttributes.getDimensionPixelOffset(
              R.styleable.NavigationBarActiveIndicator_expandedActiveIndicatorPaddingEnd,
              activeIndicatorExpandedDefaultStartEndPadding);

      setItemActiveIndicatorExpandedPadding(
          getLayoutDirection() == LAYOUT_DIRECTION_RTL
              ? activeIndicatorExpandedEndPadding : activeIndicatorExpandedStartPadding,
          activeIndicatorAttributes.getDimensionPixelOffset(
              R.styleable.NavigationBarActiveIndicator_expandedActiveIndicatorPaddingTop,
              0),
          getLayoutDirection() == LAYOUT_DIRECTION_RTL
              ? activeIndicatorExpandedStartPadding : activeIndicatorExpandedEndPadding,
          activeIndicatorAttributes.getDimensionPixelOffset(
              R.styleable.NavigationBarActiveIndicator_expandedActiveIndicatorPaddingBottom,
              0));

      ColorStateList itemActiveIndicatorColor =
          MaterialResources.getColorStateList(
              context,
              activeIndicatorAttributes,
              R.styleable.NavigationBarActiveIndicator_android_color);
      setItemActiveIndicatorColor(itemActiveIndicatorColor);

      int shapeAppearanceResId =
          activeIndicatorAttributes.getResourceId(
              R.styleable.NavigationBarActiveIndicator_shapeAppearance, 0);
      ShapeAppearanceModel itemActiveIndicatorShapeAppearance =
          ShapeAppearanceModel.builder(context, shapeAppearanceResId, 0).build();
      setItemActiveIndicatorShapeAppearance(itemActiveIndicatorShapeAppearance);

      activeIndicatorAttributes.recycle();
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_menu)) {
      inflateMenu(attributes.getResourceId(R.styleable.NavigationBarView_menu, 0));
    }

    attributes.recycle();

    if (!shouldAddMenuView()) {
      addView(menuView);
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
  }

  /**
   * Whether or not to add the menu view; if true, the menu view is added to the NavigationBarView
   * in the constructor. Otherwise, the menu view should be added to the NavigationBarView as a
   * descendant view somewhere else.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean shouldAddMenuView() {
    return false;
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();

    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  /**
   * Sets the base elevation of this view, in pixels.
   *
   * @attr ref R.styleable#BottomNavigationView_elevation
   */
  @Override
  public void setElevation(float elevation) {
    super.setElevation(elevation);
    MaterialShapeUtils.setElevation(this, elevation);
  }

  /**
   * Set a listener that will be notified when a navigation item is selected. This listener will
   * also be notified when the currently selected item is reselected, unless an {@link
   * OnItemReselectedListener} has also been set.
   *
   * @param listener The listener to notify
   * @see #setOnItemReselectedListener(OnItemReselectedListener)
   */
  public void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
    selectedListener = listener;
  }

  /**
   * Set a listener that will be notified when the currently selected navigation item is reselected.
   * This does not require an {@link OnItemSelectedListener} to be set.
   *
   * @param listener The listener to notify
   * @see #setOnItemSelectedListener(OnItemSelectedListener)
   */
  public void setOnItemReselectedListener(@Nullable OnItemReselectedListener listener) {
    reselectedListener = listener;
  }

  /** Returns the {@link Menu} instance associated with this navigation bar. */
  @NonNull
  public Menu getMenu() {
    return menu;
  }

  /**
   * Returns the {@link MenuView} instance associated with this navigation bar.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public MenuView getMenuView() {
    return menuView;
  }

  /**
   * Returns the {@link android.view.ViewGroup} associated with the navigation bar menu.
   */
  @NonNull
  public ViewGroup getMenuViewGroup() {
    return menuView;
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
   * <p>This will remove any ripple backgrounds created by {@link
   * #setItemRippleColor(ColorStateList)}.
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
   * <p>This will remove any ripple backgrounds created by {@link
   * #setItemRippleColor(ColorStateList)}.
   *
   * @param background The drawable for the background.
   * @attr ref R.styleable#BottomNavigationView_itemBackground
   */
  public void setItemBackground(@Nullable Drawable background) {
    menuView.setItemBackground(background);
  }

  /**
   * Returns the color used to create a ripple as the background drawable of the menu items. If a
   * background is set using {@link #setItemBackground(Drawable)}, this will return null.
   *
   * @see #setItemBackground(Drawable)
   * @attr ref R.styleable#BottomNavigationView_itemRippleColor
   */
  @Nullable
  public ColorStateList getItemRippleColor() {
    return menuView.getItemRippleColor();
  }

  /**
   * Set the background of our menu items to be a ripple with the given colors.
   *
   * @param itemRippleColor The {@link ColorStateList} for the ripple. This will create a ripple
   *     background for menu items, replacing any background previously set by {@link
   *     #setItemBackground(Drawable)}.
   * @attr ref R.styleable#BottomNavigationView_itemRippleColor
   */
  public void setItemRippleColor(@Nullable ColorStateList itemRippleColor) {
    menuView.setItemRippleColor(itemRippleColor);
  }

  /**
   * Get the distance from the top of an item's icon/active indicator to the top of the navigation
   * bar item.
   */
  @Px
  public int getItemPaddingTop() {
    return menuView.getItemPaddingTop();
  }

  /**
   * Set the distance from the top of an items icon/active indicator to the top of the navigation
   * bar item.
   */
  public void setItemPaddingTop(@Px int paddingTop) {
    menuView.setItemPaddingTop(paddingTop);
  }

  /**
   * Get the distance from the bottom of an item's label to the bottom of the navigation bar item.
   */
  @Px
  public int getItemPaddingBottom() {
    return menuView.getItemPaddingBottom();
  }

  /**
   * Set the distance from the bottom of an item's label to the bottom of the navigation bar item.
   */
  public void setItemPaddingBottom(@Px int paddingBottom) {
    menuView.setItemPaddingBottom(paddingBottom);
  }

  private void setMeasureBottomPaddingFromLabelBaseline(boolean measurePaddingFromBaseline) {
    menuView.setMeasurePaddingFromLabelBaseline(measurePaddingFromBaseline);
  }

  /**
   * Sets whether or not the label text should scale with the system font size.
   */
  public void setLabelFontScalingEnabled(boolean labelFontScalingEnabled) {
    menuView.setLabelFontScalingEnabled(labelFontScalingEnabled);
  }

  /**
   * Returns whether or not the label text should scale with the system font size.
   */
  public boolean getScaleLabelTextWithFont() {
    return menuView.getScaleLabelTextWithFont();
  }

  /**
   * Set the max lines limit for the label text.
   */
  public void setLabelMaxLines(int labelMaxLines) {
    menuView.setLabelMaxLines(labelMaxLines);
  }

  /**
   * Returns the max lines limit for the label text.
   */
  public int getLabelMaxLines(int labelMaxLines) {
    return menuView.getLabelMaxLines();
  }

  /**
   * Set the distance between the active indicator container and the item's label.
   */
  public void setActiveIndicatorLabelPadding(@Px int activeIndicatorLabelPadding) {
    menuView.setActiveIndicatorLabelPadding(activeIndicatorLabelPadding);
  }

  /**
   * Get the distance between the active indicator container and the item's label.
   */
  @Px
  public int getActiveIndicatorLabelPadding() {
    return menuView.getActiveIndicatorLabelPadding();
  }

  /**
   * Set the horizontal distance between the icon and the item's label when the item is in the
   * {@link NavigationBarView#ITEM_ICON_GRAVITY_START} configuration.
   */
  public void setIconLabelHorizontalSpacing(@Px int iconLabelSpacing) {
    menuView.setIconLabelHorizontalSpacing(iconLabelSpacing);
  }

  /**
   * Get the horizontal distance between the icon and the item's label when the item is in the
   * {@link NavigationBarView#ITEM_ICON_GRAVITY_START} configuration.
   */
  @Px
  public int getIconLabelHorizontalSpacing() {
    return menuView.getIconLabelHorizontalSpacing();
  }


  /**
   * Get whether or not a selected item should show an active indicator.
   *
   * @return true if an active indicator will be shown when an item is selected.
   */
  public boolean isItemActiveIndicatorEnabled() {
    return menuView.getItemActiveIndicatorEnabled();
  }

  /**
   * Set whether a selected item should show an active indicator.
   *
   * @param enabled true if a selected item should show an active indicator.
   */
  public void setItemActiveIndicatorEnabled(boolean enabled) {
    menuView.setItemActiveIndicatorEnabled(enabled);
  }

  /**
   * Get the width of an item's active indicator.
   *
   * @return The width, in pixels, of a menu item's active indicator.
   */
  @Px
  public int getItemActiveIndicatorWidth() {
    return menuView.getItemActiveIndicatorWidth();
  }

  /**
   * Set the width of an item's active indicator.
   *
   * @param width The width, in pixels, of the menu item's active indicator.
   */
  public void setItemActiveIndicatorWidth(@Px int width) {
    menuView.setItemActiveIndicatorWidth(width);
  }

  /**
   * Get the width of an item's active indicator.
   *
   * @return The width, in pixels, of a menu item's active indicator.
   */
  @Px
  public int getItemActiveIndicatorHeight() {
    return menuView.getItemActiveIndicatorHeight();
  }

  /**
   * Set the height of an item's active indicator.
   *
   * @param height The height, in pixels, of the menu item's active indicator.
   */
  public void setItemActiveIndicatorHeight(@Px int height) {
    menuView.setItemActiveIndicatorHeight(height);
  }

  /**
   * Get the margin that will be maintained at the start and end of the active indicator away from
   * the edges of its parent container.
   *
   * @return The horizontal margin, in pixels.
   */
  @Px
  public int getItemActiveIndicatorMarginHorizontal() {
    return menuView.getItemActiveIndicatorMarginHorizontal();
  }

  /**
   * Set the horizontal margin that will be maintained at the start and end of the active indicator,
   * making sure the indicator remains the given distance from the edge of its parent container.
   *
   * @param horizontalMargin The horizontal margin, in pixels.
   */
  public void setItemActiveIndicatorMarginHorizontal(@Px int horizontalMargin) {
    menuView.setItemActiveIndicatorMarginHorizontal(horizontalMargin);
  }

  /**
   * Sets the navigation items' layout gravity.
   *
   * @param itemGravity the layout {@link android.view.Gravity} of the item
   * @see #getItemGravity()
   */
  public void setItemGravity(@ItemGravity int itemGravity) {
    if (menuView.getItemGravity() != itemGravity) {
      menuView.setItemGravity(itemGravity);
      presenter.updateMenuView(false);
    }
  }

  /**
   * Returns the navigation items' layout gravity.
   *
   * @see #setItemGravity(int)
   */
  @ItemGravity
  public int getItemGravity() {
    return menuView.getItemGravity();
  }

  /**
   * Get the width of an item's active indicator when it is expanded to wrap the item content, ie.
   * when it is in the {@link ItemIconGravity#ITEM_ICON_GRAVITY_START} configuration.
   *
   * @return The width, in pixels, of a menu item's active indicator.
   */
  @Px
  public int getItemActiveIndicatorExpandedWidth() {
    return menuView.getItemActiveIndicatorExpandedWidth();
  }

  /**
   * Set the width of an item's active indicator when it is expanded to wrap the item content, ie.
   * when it is in the {@link ItemIconGravity#ITEM_ICON_GRAVITY_START} configuration.
   *
   * @param width The width, in pixels, of the menu item's expanded active indicator. The width may
   *     also be set as {@link #ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT} or {@link
   *     #ACTIVE_INDICATOR_WIDTH_MATCH_PARENT}.
   */
  public void setItemActiveIndicatorExpandedWidth(@Px int width) {
    menuView.setItemActiveIndicatorExpandedWidth(width);
  }

  /**
   * Get the height of an item's active indicator when it is expanded to wrap the item content, ie.
   * when it is in the {@link ItemIconGravity#ITEM_ICON_GRAVITY_START} configuration.
   *
   * @return The height, in pixels, of a menu item's expanded active indicator.
   */
  @Px
  public int getItemActiveIndicatorExpandedHeight() {
    return menuView.getItemActiveIndicatorExpandedHeight();
  }

  /**
   * Set the height of an item's active indicator when it is expanded to wrap the item content, ie.
   * when it is in the {@link ItemIconGravity#ITEM_ICON_GRAVITY_START} configuration.
   *
   * @param height The height, in pixels, of the menu item's active indicator.
   */
  public void setItemActiveIndicatorExpandedHeight(@Px int height) {
    menuView.setItemActiveIndicatorExpandedHeight(height);
  }

  /**
   * Get the margin that will be maintained at the start and end of the expanded active indicator
   * away from the edges of its parent container.
   *
   * @return The horizontal margin, in pixels.
   */
  @Px
  public int getItemActiveIndicatorExpandedMarginHorizontal() {
    return menuView.getItemActiveIndicatorExpandedMarginHorizontal();
  }

  /**
   * Set the horizontal margin that will be maintained at the start and end of the expanded active
   * indicator, making sure the indicator maintains the given distance from the edge of its parent
   * container.
   *
   * @param horizontalMargin The horizontal margin, in pixels.
   */
  public void setItemActiveIndicatorExpandedMarginHorizontal(@Px int horizontalMargin) {
    menuView.setItemActiveIndicatorExpandedMarginHorizontal(horizontalMargin);
  }

  /**
   * Set the padding of the expanded active indicator wrapping the content.
   *
   * @param paddingLeft The left padding, in pixels.
   * @param paddingTop The top padding, in pixels.
   * @param paddingRight The right padding, in pixels.
   * @param paddingBottom The bottom padding, in pixels.
   */
  public void setItemActiveIndicatorExpandedPadding(
      @Px int paddingLeft, @Px int paddingTop, @Px int paddingRight, @Px int paddingBottom) {
    menuView.setItemActiveIndicatorExpandedPadding(paddingLeft, paddingTop, paddingRight, paddingBottom);
  }

  /**
   * Get the {@link ShapeAppearanceModel} of the active indicator drawable.
   *
   * @return The {@link ShapeAppearanceModel} of the active indicator drawable.
   */
  @Nullable
  public ShapeAppearanceModel getItemActiveIndicatorShapeAppearance() {
    return menuView.getItemActiveIndicatorShapeAppearance();
  }

  /**
   * Set the {@link ShapeAppearanceModel} of the active indicator drawable.
   *
   * @param shapeAppearance The {@link ShapeAppearanceModel} of the active indicator drawable.
   */
  public void setItemActiveIndicatorShapeAppearance(
      @Nullable ShapeAppearanceModel shapeAppearance) {
    menuView.setItemActiveIndicatorShapeAppearance(shapeAppearance);
  }

  /**
   * Get the color of the active indicator drawable.
   *
   * @return A {@link ColorStateList} used as the color of the active indicator.
   */
  @Nullable
  public ColorStateList getItemActiveIndicatorColor() {
    return menuView.getItemActiveIndicatorColor();
  }

  /**
   * Set the {@link ColorStateList} of the active indicator drawable.
   *
   * @param csl The {@link ColorStateList} used as the color of the active indicator.
   */
  public void setItemActiveIndicatorColor(@Nullable ColorStateList csl) {
    menuView.setItemActiveIndicatorColor(csl);
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
      boolean result = menu.performItemAction(item, presenter, 0);
      // If the item action was not invoked successfully (ie if there's no listener) or if
      // the item was checked through the action, we should update the checked item.
      if (item.isCheckable() && (!result || item.isChecked())) {
        menuView.setCheckedItem(item);
      }
    }
  }

  /**
   * Sets the navigation items' label visibility mode.
   *
   * <p>The label is either always shown, never shown, or only shown when activated. Also supports
   * "auto" mode, which uses the item count to determine whether to show or hide the label.
   *
   * @attr ref com.google.android.material.R.styleable#NavigationBarView_labelVisibilityMode
   * @param labelVisibilityMode mode which decides whether or not the label should be shown. Can be
   *     one of {@link NavigationBarView#LABEL_VISIBILITY_AUTO}, {@link
   *     NavigationBarView#LABEL_VISIBILITY_SELECTED}, {@link
   *     NavigationBarView#LABEL_VISIBILITY_LABELED}, or {@link
   *     NavigationBarView#LABEL_VISIBILITY_UNLABELED}
   * @see #getLabelVisibilityMode()
   */
  public void setLabelVisibilityMode(@LabelVisibility int labelVisibilityMode) {
    if (menuView.getLabelVisibilityMode() != labelVisibilityMode) {
      menuView.setLabelVisibilityMode(labelVisibilityMode);
      presenter.updateMenuView(false);
    }
  }

  /**
   * Returns the current label visibility mode used by this {@link NavigationBarView}.
   *
   * @attr ref com.google.android.material.R.styleable#BottomNavigationView_labelVisibilityMode
   * @see #setLabelVisibilityMode(int)
   */
  @NavigationBarView.LabelVisibility
  public int getLabelVisibilityMode() {
    return menuView.getLabelVisibilityMode();
  }

  /**
   * Sets the navigation items' icon gravity.
   *
   * @param itemIconGravity the placement of the icon in the nav item one of {@link
   *     NavigationBarView#ITEM_ICON_GRAVITY_TOP}, or {@link
   *     NavigationBarView#ITEM_ICON_GRAVITY_START}
   * @see #getItemIconGravity()
   */
  public void setItemIconGravity(@ItemIconGravity int itemIconGravity) {
    if (menuView.getItemIconGravity() != itemIconGravity) {
      menuView.setItemIconGravity(itemIconGravity);
      presenter.updateMenuView(false);
    }
  }

  /**
   * Returns the current item icon gravity.
   *
   * @see #setItemIconGravity(int)
   */
  public int getItemIconGravity() {
    return menuView.getItemIconGravity();
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
   * Sets the text appearance to be used for inactive menu item labels when they are in the
   * horizontal item layout (when the start icon value is {@link
   * ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @param textAppearanceRes the text appearance ID used for inactive menu item labels
   */
  public void setHorizontalItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    menuView.setHorizontalItemTextAppearanceInactive(textAppearanceRes);
  }

  /**
   * Returns the text appearance used for inactive menu item labels when they are in the
   * horizontal item layout (when the start icon value is {@link
   * ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @return the text appearance ID used for inactive menu item labels
   */
  @StyleRes
  public int getHorizontalItemTextAppearanceInactive() {
    return menuView.getHorizontalItemTextAppearanceInactive();
  }

  /**
   * Sets the text appearance to be used for the menu item labels when they are in the horizontal
   * item layout (when the start icon value is {@link ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @param textAppearanceRes the text appearance ID used for menu item labels
   */
  public void setHorizontalItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    menuView.setHorizontalItemTextAppearanceActive(textAppearanceRes);
  }

  /**
   * Returns the text appearance used for the active menu item label when they are in the
   * horizontal item layout (when the start icon value is {@link
   * ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @return the text appearance ID used for the active menu item label
   */
  @StyleRes
  public int getHorizontalItemTextAppearanceActive() {
    return menuView.getHorizontalItemTextAppearanceActive();
  }

  /**
   * Sets whether the active menu item labels are bold.
   *
   * @param isBold whether the active menu item labels are bold
   */
  public void setItemTextAppearanceActiveBoldEnabled(boolean isBold) {
    menuView.setItemTextAppearanceActiveBoldEnabled(isBold);
  }

  /**
   * Sets an {@link android.view.View.OnTouchListener} for the item view associated with the
   * provided {@code menuItemId}.
   */
  public void setItemOnTouchListener(int menuItemId, @Nullable OnTouchListener onTouchListener) {
    menuView.setItemOnTouchListener(menuItemId, onTouchListener);
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
  @NonNull
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

  /** Listener for handling selection events on navigation items. */
  public interface OnItemSelectedListener {

    /**
     * Called when an item in the navigation menu is selected.
     *
     * @param item The selected item
     * @return true to display the item as the selected item and false if the item should not be
     *     selected. Consider setting non-selectable items as disabled preemptively to make them
     *     appear non-interactive.
     */
    boolean onNavigationItemSelected(@NonNull MenuItem item);
  }

  /** Listener for handling reselection events on navigation items. */
  public interface OnItemReselectedListener {

    /**
     * Called when the currently selected item in the navigation menu is selected again.
     *
     * @param item The selected item
     */
    void onNavigationItemReselected(@NonNull MenuItem item);
  }

  /** Returns the maximum number of items that can be shown in NavigationBarView. */
  public abstract int getMaxItemCount();

  /** Returns whether or not submenus are supported. */
  protected boolean isSubMenuSupported() {
    return false;
  }

  // TODO: b/361189184 - Make public once expanded state is public
  /**
   * Returns the maximum number of items that can be shown in the collapsed state in
   * NavigationBarView.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public int getCollapsedMaxItemCount() {
    return getMaxItemCount();
  }

  /**
   * Returns reference to a newly created {@link NavigationBarMenuView}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  protected abstract NavigationBarMenuView createNavigationBarMenuView(@NonNull Context context);

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  /**
   * Returns reference to the {@link NavigationBarPresenter}
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public NavigationBarPresenter getPresenter() {
    return presenter;
  }

  @Override
  @NonNull
  protected Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();
    SavedState savedState = new SavedState(superState);
    savedState.menuPresenterState = new Bundle();
    menu.savePresenterStates(savedState.menuPresenterState);
    return savedState;
  }

  @Override
  protected void onRestoreInstanceState(@Nullable Parcelable state) {
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
