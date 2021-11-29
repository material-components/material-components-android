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
import static com.google.android.material.theme.overlay.MaterialThemeOverlay.wrap;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.SupportMenuInflater;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.widget.TintTypedArray;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import androidx.annotation.DimenRes;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.customview.view.AbsSavedState;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.internal.ContextUtils;
import com.google.android.material.internal.NavigationMenu;
import com.google.android.material.internal.NavigationMenuPresenter;
import com.google.android.material.internal.ScrimInsetsFrameLayout;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.MaterialShapeUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;

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

  private static final int DEF_STYLE_RES = R.style.Widget_Design_NavigationView;
  private static final int PRESENTER_NAVIGATION_VIEW_ID = 1;

  @NonNull private final NavigationMenu menu;
  private final NavigationMenuPresenter presenter = new NavigationMenuPresenter();

  OnNavigationItemSelectedListener listener;
  private final int maxWidth;

  private final int[] tmpLocation = new int[2];

  private MenuInflater menuInflater;
  private OnGlobalLayoutListener onGlobalLayoutListener;
  private boolean topInsetScrimEnabled = true;
  private boolean bottomInsetScrimEnabled = true;

  private int layoutGravity = Gravity.NO_GRAVITY;
  @Px private int drawerLayoutCornerSize = 0;

  @Nullable private Path shapeClipPath;
  private final RectF shapeClipBounds = new RectF();

  public NavigationView(@NonNull Context context) {
    this(context, null);
  }

  public NavigationView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.navigationViewStyle);
  }

  public NavigationView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(wrap(context, attrs, defStyleAttr, DEF_STYLE_RES), attrs, defStyleAttr);
    // Ensure we are using the correctly themed context rather than the context that was passed in.
    context = getContext();

    // Create the menu
    this.menu = new NavigationMenu(context);

    // Custom attributes
    TintTypedArray a =
        ThemeEnforcement.obtainTintedStyledAttributes(
            context,
            attrs,
            R.styleable.NavigationView,
            defStyleAttr,
            DEF_STYLE_RES);

    if (a.hasValue(R.styleable.NavigationView_android_background)) {
      ViewCompat.setBackground(this, a.getDrawable(R.styleable.NavigationView_android_background));
    }

    // Get the drawer layout corner size and layout gravity to be used to shape the exposed corners
    // of this view when placed inside a drawer layout.
    drawerLayoutCornerSize =
        a.getDimensionPixelSize(
            R.styleable.NavigationView_drawerLayoutCornerSize, 0);
    layoutGravity = a.getInt(R.styleable.NavigationView_android_layout_gravity, Gravity.NO_GRAVITY);

    // Set the background to a MaterialShapeDrawable if it hasn't been set or if it can be converted
    // to a MaterialShapeDrawable.
    if (getBackground() == null || getBackground() instanceof ColorDrawable) {
      ShapeAppearanceModel shapeAppearanceModel =
          ShapeAppearanceModel.builder(context, attrs, defStyleAttr, DEF_STYLE_RES).build();
      Drawable orig = getBackground();
      MaterialShapeDrawable materialShapeDrawable = new MaterialShapeDrawable(shapeAppearanceModel);
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

    ColorStateList subheaderColor = null;
    if (a.hasValue(R.styleable.NavigationView_subheaderColor)) {
      subheaderColor = a.getColorStateList(R.styleable.NavigationView_subheaderColor);
    }

    int subheaderTextAppearance = NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET;
    if (a.hasValue(R.styleable.NavigationView_subheaderTextAppearance)) {
      subheaderTextAppearance =
          a.getResourceId(R.styleable.NavigationView_subheaderTextAppearance, 0);
    }

    if (subheaderTextAppearance == NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET
            && subheaderColor == null) {
      // If there isn't a text appearance set, we'll use a default text color
      subheaderColor = createDefaultColorStateList(android.R.attr.textColorSecondary);
    }

    final ColorStateList itemIconTint;
    if (a.hasValue(R.styleable.NavigationView_itemIconTint)) {
      itemIconTint = a.getColorStateList(R.styleable.NavigationView_itemIconTint);
    } else {
      itemIconTint = createDefaultColorStateList(android.R.attr.textColorSecondary);
    }

    int textAppearance = NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET;
    if (a.hasValue(R.styleable.NavigationView_itemTextAppearance)) {
      textAppearance = a.getResourceId(R.styleable.NavigationView_itemTextAppearance, 0);
    }

    if (a.hasValue(R.styleable.NavigationView_itemIconSize)) {
      setItemIconSize(a.getDimensionPixelSize(R.styleable.NavigationView_itemIconSize, 0));
    }

    ColorStateList itemTextColor = null;
    if (a.hasValue(R.styleable.NavigationView_itemTextColor)) {
      itemTextColor = a.getColorStateList(R.styleable.NavigationView_itemTextColor);
    }

    if (textAppearance == NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET && itemTextColor == null) {
      // If there isn't a text appearance set, we'll use a default text color
      itemTextColor = createDefaultColorStateList(android.R.attr.textColorPrimary);
    }

    Drawable itemBackground = a.getDrawable(R.styleable.NavigationView_itemBackground);
    // Set a shaped itemBackground if itemBackground hasn't been set and there is a shape
    // appearance.
    if (itemBackground == null && hasShapeAppearance(a)) {
      itemBackground = createDefaultItemBackground(a);
    }

    if (a.hasValue(R.styleable.NavigationView_itemHorizontalPadding)) {
      final int itemHorizontalPadding =
          a.getDimensionPixelSize(R.styleable.NavigationView_itemHorizontalPadding, 0);
      setItemHorizontalPadding(itemHorizontalPadding);
    }

    if (a.hasValue(R.styleable.NavigationView_itemVerticalPadding)) {
      final int itemVerticalPadding =
          a.getDimensionPixelSize(R.styleable.NavigationView_itemVerticalPadding, 0);
      setItemVerticalPadding(itemVerticalPadding);
    }

    final int dividerInsetStart =
        a.getDimensionPixelSize(R.styleable.NavigationView_dividerInsetStart, 0);
    setDividerInsetStart(dividerInsetStart);

    final int dividerInsetEnd =
        a.getDimensionPixelSize(R.styleable.NavigationView_dividerInsetEnd, 0);
    setDividerInsetEnd(dividerInsetEnd);

    final int subheaderInsetStart =
        a.getDimensionPixelSize(R.styleable.NavigationView_subheaderInsetStart, 0);
    setSubheaderInsetStart(subheaderInsetStart);

    final int subheaderInsetEnd =
        a.getDimensionPixelSize(R.styleable.NavigationView_subheaderInsetEnd, 0);
    setSubheaderInsetEnd(subheaderInsetEnd);

    setTopInsetScrimEnabled(
        a.getBoolean(R.styleable.NavigationView_topInsetScrimEnabled, topInsetScrimEnabled));

    setBottomInsetScrimEnabled(
        a.getBoolean(R.styleable.NavigationView_bottomInsetScrimEnabled, bottomInsetScrimEnabled)
    );

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
    if (subheaderTextAppearance != NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET) {
      presenter.setSubheaderTextAppearance(subheaderTextAppearance);
    }
    presenter.setSubheaderColor(subheaderColor);
    presenter.setItemIconTintList(itemIconTint);
    presenter.setOverScrollMode(getOverScrollMode());
    if (textAppearance != NavigationMenuPresenter.NO_TEXT_APPEARANCE_SET) {
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

    setupInsetScrimsListener();
  }

  @Override
  public void setOverScrollMode(int overScrollMode) {
    super.setOverScrollMode(overScrollMode);
    if (presenter != null) {
      presenter.setOverScrollMode(overScrollMode);
    }
  }

  /**
   * Determine whether this view is placed inside a drawer layout and should have its exposed
   * corners shaped according to the <code>app:drawerLayoutCornerSize</code> attribute.
   *
   * @attr ref com.google.android.material.R.styleable#NavigationView_drawerLayoutCornerSize
   */
  private void maybeUpdateCornerSizeForDrawerLayout(@Px int width, @Px int height) {
    if (getParent() instanceof DrawerLayout
        && drawerLayoutCornerSize > 0
        && getBackground() instanceof MaterialShapeDrawable) {
      // Get the absolute gravity of this view and set the top and bottom exposed corner sizes.
      MaterialShapeDrawable background = (MaterialShapeDrawable) getBackground();
      ShapeAppearanceModel.Builder builder =
          background.getShapeAppearanceModel().toBuilder();
      int absGravity =
          GravityCompat.getAbsoluteGravity(layoutGravity, ViewCompat.getLayoutDirection(this));
      if (absGravity == Gravity.LEFT) {
        // Exposed edge is on the right
        builder.setTopRightCornerSize(drawerLayoutCornerSize);
        builder.setBottomRightCornerSize(drawerLayoutCornerSize);
      } else {
        // Exposed edge is on the left
        builder.setTopLeftCornerSize(drawerLayoutCornerSize);
        builder.setBottomLeftCornerSize(drawerLayoutCornerSize);
      }
      background.setShapeAppearanceModel(builder.build());

      if (shapeClipPath == null) {
        shapeClipPath = new Path();
      }
      shapeClipPath.reset();
      shapeClipBounds.set(0, 0, width, height);
      ShapeAppearancePathProvider.getInstance()
          .calculatePath(
              background.getShapeAppearanceModel(),
              background.getInterpolation(),
              shapeClipBounds,
              shapeClipPath);
      invalidate();
    } else {
      shapeClipPath = null;
      shapeClipBounds.setEmpty();
    }
  }

  private boolean hasShapeAppearance(@NonNull TintTypedArray a) {
    return a.hasValue(R.styleable.NavigationView_itemShapeAppearance)
        || a.hasValue(R.styleable.NavigationView_itemShapeAppearanceOverlay);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    MaterialShapeUtils.setParentAbsoluteElevation(this);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    maybeUpdateCornerSizeForDrawerLayout(w, h);
  }

  @Override
  public void setElevation(float elevation) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      super.setElevation(elevation);
    }
    MaterialShapeUtils.setElevation(this, elevation);
  }

  /**
   * Creates a {@link MaterialShapeDrawable} to use as the {@code itemBackground} and wraps it in an
   * {@link InsetDrawable} for margins.
   *
   * @param a The TintTypedArray containing the resolved NavigationView style attributes.
   */
  @NonNull
  private final Drawable createDefaultItemBackground(@NonNull TintTypedArray a) {
    int shapeAppearanceResId = a.getResourceId(R.styleable.NavigationView_itemShapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.NavigationView_itemShapeAppearanceOverlay, 0);
    MaterialShapeDrawable materialShapeDrawable =
        new MaterialShapeDrawable(
            ShapeAppearanceModel.builder(
                    getContext(), shapeAppearanceResId, shapeAppearanceOverlayResId)
                .build());
    materialShapeDrawable.setFillColor(
        MaterialResources.getColorStateList(
            getContext(), a, R.styleable.NavigationView_itemShapeFillColor));

    int insetLeft = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetStart, 0);
    int insetTop = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetTop, 0);
    int insetRight = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetEnd, 0);
    int insetBottom = a.getDimensionPixelSize(R.styleable.NavigationView_itemShapeInsetBottom, 0);
    return new InsetDrawable(materialShapeDrawable, insetLeft, insetTop, insetRight, insetBottom);
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

  @Override
  protected void dispatchDraw(@NonNull Canvas canvas) {
    if (shapeClipPath == null) {
      super.dispatchDraw(canvas);
      return;
    }

    int save = canvas.save();
    canvas.clipPath(shapeClipPath);
    super.dispatchDraw(canvas);
    canvas.restoreToCount(save);
  }

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @Override
  protected void onInsetsChanged(@NonNull WindowInsetsCompat insets) {
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
  @NonNull
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
   * Set the background of our menu items to the given resource. This overrides the default
   * background set to items and it's styling.
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
   * Returns the vertical (top and bottom) padding in pixels applied to menu items.
   *
   * @see #setItemVerticalPadding(int)
   * @attr ref R.styleable#NavigationView_itemVerticalPadding
   */
  @Px
  public int getItemVerticalPadding() {
    return presenter.getItemVerticalPadding();
  }

  /**
   * Set the vertical (top and bottom) padding in pixels of menu items.
   *
   * @param padding The vertical padding in pixels.
   * @attr ref R.styleable#NavigationView_itemVerticalPadding
   */
  public void setItemVerticalPadding(@Px int padding) {
    presenter.setItemVerticalPadding(padding);
  }

  /**
   * Set the vertical (top and bottom) padding of menu items.
   *
   * @param paddingResource Dimension resource to use for the vertical padding.
   * @attr ref R.styleable#NavigationView_itemVerticalPadding
   */
  public void setItemVerticalPaddingResource(@DimenRes int paddingResource) {
    presenter.setItemVerticalPadding(getResources().getDimensionPixelSize(paddingResource));
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

  /**
   * Whether or not the NavigationView will draw a scrim behind the window's top inset.
   */
  public boolean isTopInsetScrimEnabled() {
    return this.topInsetScrimEnabled;
  }

  /**
   * Set whether or not the NavigationView should draw a scrim behind the window's top
   * inset (typically the status bar).
   *
   * @param enabled true when the NavigationView should draw a scrim.
   */
  public void setTopInsetScrimEnabled(boolean enabled) {
    this.topInsetScrimEnabled = enabled;
  }

  /**
   * Whether or not the NavigationView will draw a scrim behind the window's bottom inset.
   */
  public boolean isBottomInsetScrimEnabled() {
    return this.bottomInsetScrimEnabled;
  }

  /**
   * Set whether or not the NavigationView should draw a scrim behind the window's bottom
   * inset (typically the navigation bar)
   *
   * @param enabled true when the NavigationView should draw a scrim.
   */
  public void setBottomInsetScrimEnabled(boolean enabled) {
    this.bottomInsetScrimEnabled = enabled;
  }

  /**
   * Get the distance between the start edge of the NavigationView and the start of a menu divider.
   */
  @Px
  public int getDividerInsetStart() {
    return presenter.getDividerInsetStart();
  }

  /**
   * Set the distance between the start edge of the NavigationView and the start of a menu divider.
   */
  public void setDividerInsetStart(@Px int dividerInsetStart) {
    presenter.setDividerInsetStart(dividerInsetStart);
  }

  /**
   * Get the distance between the end of a divider and the end of the NavigationView.
   */
  @Px
  public int getDividerInsetEnd() {
    return presenter.getDividerInsetEnd();
  }

  /**
   * Set the distance between the end of a divider and the end of the NavigationView.
   */
  public void setDividerInsetEnd(@Px int dividerInsetEnd) {
    presenter.setDividerInsetEnd(dividerInsetEnd);
  }

  /**
   * Get the distance between the start of the NavigationView and the start of a menu subheader.
   */
  @Px
  public int getSubheaderInsetStart() {
    return presenter.getSubheaderInsetStart();
  }

  /**
   * Set the distance between the start of the NavigationView and the start of a menu subheader.
   */
  public void setSubheaderInsetStart(@Px int subheaderInsetStart) {
    presenter.setSubheaderInsetStart(subheaderInsetStart);
  }

  /**
   * Get the distance between the end of a menu subheader and the end of the NavigationView.
   */
  @Px
  public int getSubheaderInsetEnd() {
    return presenter.getSubheaderInsetEnd();
  }

  /**
   * Set the distance between the end of a menu subheader and the end of the NavigationView.
   */
  public void setSubheaderInsetEnd(@Px int subheaderInsetEnd) {
    presenter.setSubheaderInsetStart(subheaderInsetEnd);
  }

  private MenuInflater getMenuInflater() {
    if (menuInflater == null) {
      menuInflater = new SupportMenuInflater(getContext());
    }
    return menuInflater;
  }

  @Nullable
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

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    if (Build.VERSION.SDK_INT < 16) {
      getViewTreeObserver().removeGlobalOnLayoutListener(onGlobalLayoutListener);
    } else {
      getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
    }
  }

  /**
   * Add a listener to wait for layout changes so we can determine the location on screen. Based on
   * the location we'll try to be smart about showing the scrim at under the status bar and under
   * the system nav only when we should.
   */
  private void setupInsetScrimsListener() {
    onGlobalLayoutListener =
        new OnGlobalLayoutListener() {
          @Override
          public void onGlobalLayout() {
            getLocationOnScreen(tmpLocation);
            boolean isBehindStatusBar = tmpLocation[1] == 0;
            presenter.setBehindStatusBar(isBehindStatusBar);
            setDrawTopInsetForeground(isBehindStatusBar && isTopInsetScrimEnabled());

            Activity activity = ContextUtils.getActivity(getContext());
            if (activity != null && VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
              boolean isBehindSystemNav =
                  activity.findViewById(android.R.id.content).getHeight() == getHeight();
              boolean hasNonZeroAlpha =
                  Color.alpha(activity.getWindow().getNavigationBarColor()) != 0;

              setDrawBottomInsetForeground(
                  isBehindSystemNav && hasNonZeroAlpha && isBottomInsetScrimEnabled());
            }
          }
        };

    getViewTreeObserver()
        .addOnGlobalLayoutListener(
            onGlobalLayoutListener);
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
    @Nullable public Bundle menuState;

    public SavedState(@NonNull Parcel in, @Nullable ClassLoader loader) {
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
