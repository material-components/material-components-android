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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.core.view.ViewCompat;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.AttrRes;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.customview.view.AbsSavedState;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.internal.ViewUtils.RelativePadding;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.ripple.RippleUtils;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
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

  private static final int MENU_PRESENTER_ID = 1;

  @NonNull private final NavigationBarMenu menu;
  @NonNull private final NavigationBarMenuView menuView;
  @NonNull private final NavigationBarPresenter presenter = new NavigationBarPresenter();
  @Nullable private ColorStateList itemRippleColor;
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
    this.menu = new NavigationBarMenu(context, this.getClass(), getMaxItemCount());

    // Create the menu view.
    menuView = createNavigationBarMenuView(context);

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

    if (attributes.hasValue(R.styleable.NavigationBarView_itemTextColor)) {
      setItemTextColor(attributes.getColorStateList(R.styleable.NavigationBarView_itemTextColor));
    }

    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      // Add a MaterialShapeDrawable as background that supports tinting in every API level.
      ViewCompat.setBackground(this, createMaterialShapeDrawableBackground(context));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_elevation)) {
      setElevation(attributes.getDimensionPixelSize(R.styleable.NavigationBarView_elevation, 0));
    }

    ColorStateList backgroundTint =
        MaterialResources.getColorStateList(
            context, attributes, R.styleable.NavigationBarView_backgroundTint);
    DrawableCompat.setTintList(getBackground().mutate(), backgroundTint);

    setLabelVisibilityMode(
        attributes.getInteger(
            R.styleable.NavigationBarView_labelVisibilityMode,
            NavigationBarView.LABEL_VISIBILITY_AUTO));

    int itemBackground = attributes.getResourceId(R.styleable.NavigationBarView_itemBackground, 0);
    if (itemBackground != 0) {
      menuView.setItemBackgroundRes(itemBackground);
    } else {
      setItemRippleColor(
          MaterialResources.getColorStateList(
              context, attributes, R.styleable.NavigationBarView_itemRippleColor));
    }

    if (attributes.hasValue(R.styleable.NavigationBarView_menu)) {
      inflateMenu(attributes.getResourceId(R.styleable.NavigationBarView_menu, 0));
    }

    attributes.recycle();

    addView(menuView);

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
            // Window insets may add additional padding, e.g., to dodge the system navigation bar
            initialPadding.bottom += insets.getSystemWindowInsetBottom();

            boolean isRtl = ViewCompat.getLayoutDirection(view) == ViewCompat.LAYOUT_DIRECTION_RTL;
            int systemWindowInsetLeft = insets.getSystemWindowInsetLeft();
            int systemWindowInsetRight = insets.getSystemWindowInsetRight();
            initialPadding.start += isRtl ? systemWindowInsetRight : systemWindowInsetLeft;
            initialPadding.end += isRtl ? systemWindowInsetLeft : systemWindowInsetRight;
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
   * @attr ref R.styleable#BottomNavigationView_elevation
   */
  @Override
  public void setElevation(float elevation) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      super.setElevation(elevation);
    }
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
  protected void setOnItemSelectedListener(@Nullable OnItemSelectedListener listener) {
    selectedListener = listener;
  }

  /**
   * Set a listener that will be notified when the currently selected navigation item is reselected.
   * This does not require an {@link OnItemSelectedListener} to be set.
   *
   * @param listener The listener to notify
   * @see #setOnItemSelectedListener(OnItemSelectedListener)
   */
  protected void setOnItemReselectedListener(@Nullable OnItemReselectedListener listener) {
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
   * <p>This will remove any ripple backgrounds created by {@link
   * #setItemRippleColor(ColorStateList)}.
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
   * background is set using {@link #setItemBackground(Drawable)}, this will return null.
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
   *     #setItemBackground(Drawable)}.
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
  protected interface OnItemSelectedListener {

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
  protected interface OnItemReselectedListener {

    /**
     * Called when the currently selected item in the navigation menu is selected again.
     *
     * @param item The selected item
     */
    void onNavigationItemReselected(@NonNull MenuItem item);
  }

  /** Returns the maximum number of items that can be shown in NavigationBarView. */
  public abstract int getMaxItemCount();

  /** Returns reference to a newly created {@link NavigationBarMenuView} */
  @NonNull
  protected abstract NavigationBarMenuView createNavigationBarMenuView(@NonNull Context context);

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  @NonNull
  protected NavigationBarPresenter getPresenter() {
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
