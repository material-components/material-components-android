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
import static androidx.annotation.RestrictTo.Scope.LIBRARY;
import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP_PREFIX;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.content.res.Configuration;
import android.content.ContentResolver;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.provider.Settings;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.util.Pools;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;

import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.internal.TextScale;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.HashSet;
import android.widget.TextView;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.util.SeslMisc;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.SeslMenuItem;

/**
 * <b>SESL variant</b><br><br>
 *
 * Provides a view that will be use to render a menu view inside a {@link NavigationBarView}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class NavigationBarMenuView extends ViewGroup implements MenuView {
  // Sesl
  private static final String TAG = "NavigationBarMenuView";
  static final int BADGE_TYPE_OVERFLOW = 0;
  static final int BADGE_TYPE_DOT = 1;
  static final int BADGE_TYPE_N = 2;
  private ContentResolver mContentResolver;
  MenuBuilder mDummyMenu;
  private InternalBtnInfo mInvisibleBtns = null;
  NavigationBarItemView mOverflowButton = null;
  private MenuBuilder mOverflowMenu = null;
  private ColorDrawable mSBBTextColorDrawable;
  private MenuBuilder.Callback mSelectedCallback;
  private InternalBtnInfo mVisibleBtns = null;
  private int mMaxItemCount = 0;
  @StyleRes
  private int mSeslLabelTextAppearance;
  private int mViewType = NavigationBarView.SESL_TYPE_ICON_LABEL;
  private int mViewVisibleItemCount = 0;
  private int mVisibleItemCount = 0;

  private boolean mHasGroupDivider;
  private boolean mHasOverflowMenu = false;
  protected boolean mUseItemPool = true;
  private static final long ACTIVE_ANIMATION_DURATION_MS = 0;
  // Sesl

  private static final int ITEM_POOL_SIZE = 5;
  private static final int NO_PADDING = -1;

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  @Nullable private final TransitionSet set;
  @NonNull private final OnClickListener onClickListener;
  private final Pools.Pool<NavigationBarItemView> itemPool =
      new Pools.SynchronizedPool<>(ITEM_POOL_SIZE);

  @NonNull
  private final SparseArray<OnTouchListener> onTouchListeners = new SparseArray<>(ITEM_POOL_SIZE);

  @NavigationBarView.LabelVisibility private int labelVisibilityMode;

  @Nullable private NavigationBarItemView[] buttons;
  private int selectedItemId = 0;
  private int selectedItemPosition = 0;

  @Nullable private ColorStateList itemIconTint;
  @Dimension private int itemIconSize;
  private ColorStateList itemTextColorFromUser;
  @Nullable private final ColorStateList itemTextColorDefault;
  @StyleRes private int itemTextAppearanceInactive;
  @StyleRes private int itemTextAppearanceActive;
  //private boolean itemTextAppearanceActiveBoldEnabled;
  private Drawable itemBackground;
  @Nullable private ColorStateList itemRippleColor;
  private int itemBackgroundRes;
  @NonNull private SparseArray<BadgeDrawable> badgeDrawables =
      new SparseArray<>(ITEM_POOL_SIZE);
  private int itemPaddingTop = NO_PADDING;
  private int itemPaddingBottom = NO_PADDING;
  private int itemActiveIndicatorLabelPadding = NO_PADDING;
  private boolean itemActiveIndicatorEnabled;
  private int itemActiveIndicatorWidth;
  private int itemActiveIndicatorHeight;
  private int itemActiveIndicatorMarginHorizontal;
  private ShapeAppearanceModel itemActiveIndicatorShapeAppearance;
  private boolean itemActiveIndicatorResizeable = false;
  private ColorStateList itemActiveIndicatorColor;

  private NavigationBarPresenter presenter;
  private MenuBuilder menu;

  public NavigationBarMenuView(@NonNull Context context) {
    super(context);

    itemTextColorDefault = createDefaultColorStateList(android.R.attr.textColorSecondary);

    if (this.isInEditMode()) {
      set = null;
    } else {
      set = new AutoTransition();
      set.setOrdering(TransitionSet.ORDERING_TOGETHER);
      set.setDuration(ACTIVE_ANIMATION_DURATION_MS);//sesl
      set.addTransition(new TextScale());
    }

    onClickListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            final NavigationBarItemView itemView = (NavigationBarItemView) v;
            MenuItem item = itemView.getItemData();
            if (!menu.performItemAction(item, presenter, 0)) {
              item.setChecked(true);
            }
          }
        };

    mContentResolver = context.getContentResolver();//sesl

    ViewCompat.setImportantForAccessibility(this, ViewCompat.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  @Override
  public void initialize(@NonNull MenuBuilder menu) {
    this.menu = menu;
  }

  @Override
  public int getWindowAnimations() {
    return 0;
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        AccessibilityNodeInfoCompat.CollectionInfoCompat.obtain(
            /* rowCount= */ 1,
            /* columnCount= */ menu.getVisibleItems().size(),
            /* hierarchical= */ false,
            /* selectionMode = */ AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

  /**
   * Sets the tint which is applied to the menu items' icons.
   *
   * @param tint the tint to apply
   */
  public void setIconTintList(@Nullable ColorStateList tint) {
    itemIconTint = tint;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setIconTintList(tint);
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setIconTintList(tint);//sesl
    }
  }

  /**
   * Returns the tint which is applied for the menu item labels.
   *
   * @return the ColorStateList that is used to tint menu items' icons
   */
  @Nullable
  public ColorStateList getIconTintList() {
    return itemIconTint;
  }

  /**
   * Sets the size to provide for the menu item icons.
   *
   * <p>For best image resolution, use an icon with the same size set in this method.
   *
   * @param iconSize the size to provide for the menu item icons in pixels
   */
  public void setItemIconSize(@Dimension int iconSize) {
    this.itemIconSize = iconSize;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setIconSize(iconSize);
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setIconSize(iconSize);
    }
  }

  /** Returns the size in pixels provided for the menu item icons. */
  @Dimension
  public int getItemIconSize() {
    return itemIconSize;
  }

  /**
   * Sets the text color to be used for the menu item labels.
   *
   * @param color the ColorStateList used for menu item labels
   */
  public void setItemTextColor(@Nullable ColorStateList color) {
    itemTextColorFromUser = color;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setTextColor(color);
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setTextColor(color);//sesl
      setOverflowSpanColor(0, true);//sesl
    }
  }

  /**
   * Returns the text color used for menu item labels.
   *
   * @return the ColorStateList used for menu items labels
   */
  @Nullable
  public ColorStateList getItemTextColor() {
    return itemTextColorFromUser;
  }

  /**
   * Sets the text appearance to be used for inactive menu item labels.
   *
   * @param textAppearanceRes the text appearance ID used for inactive menu item labels
   */
  public void setItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    this.itemTextAppearanceInactive = textAppearanceRes;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setTextAppearanceInactive(textAppearanceRes);
        // Set the text color if the user has set it, since itemTextColorFromUser takes precedence
        // over a color set in the text appearance.
        if (itemTextColorFromUser != null) {
          item.setTextColor(itemTextColorFromUser);
        }
      }
    }
    //Sesl
    if (mOverflowButton != null) {
      mOverflowButton.setTextAppearanceInactive(textAppearanceRes);
      if (itemTextColorFromUser != null) {
        mOverflowButton.setTextColor(itemTextColorFromUser);
      }
    }
    //sesl
  }

  /**
   * Returns the text appearance used for inactive menu item labels.
   *
   * @return the text appearance ID used for inactive menu item labels
   */
  @StyleRes
  public int getItemTextAppearanceInactive() {
    return itemTextAppearanceInactive;
  }

  /**
   * Sets the text appearance to be used for the active menu item label.
   *
   * @param textAppearanceRes the text appearance ID used for the active menu item label
   */
  public void setItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    this.itemTextAppearanceActive = textAppearanceRes;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setTextAppearanceActive(textAppearanceRes);
        // Set the text color if the user has set it, since itemTextColorFromUser takes precedence
        // over a color set in the text appearance.
        if (itemTextColorFromUser != null) {
          item.setTextColor(itemTextColorFromUser);
        }
      }
    }
    //Sesl
    if (mOverflowButton != null && itemTextColorFromUser != null) {
      mOverflowButton.setTextAppearanceActive(textAppearanceRes);
      mOverflowButton.setTextColor(itemTextColorFromUser);
    }
    //sesl
  }


//  /**
//   * Sets whether the active menu item label is bold.
//   *
//   * @param isBold whether the active menu item label is bold
//   */
//  public void setItemTextAppearanceActiveBoldEnabled(boolean isBold) {
//    this.itemTextAppearanceActiveBoldEnabled = isBold;
//    if (buttons != null) {
//      for (NavigationBarItemView item : buttons) {
//        item.setTextAppearanceActiveBoldEnabled(isBold);
//      }
//    }
//  }

  /**
   * Returns the text appearance used for the active menu item label.
   *
   * @return the text appearance ID used for the active menu item label
   */
  @StyleRes
  public int getItemTextAppearanceActive() {
    return itemTextAppearanceActive;
  }

  /**
   * Sets the resource ID to be used for item backgrounds.
   *
   * @param background the resource ID of the background
   */
  public void setItemBackgroundRes(int background) {
    itemBackgroundRes = background;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) break;//sesl
        item.setItemBackground(background);
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setItemBackground(background);//sesl
    }
  }


  /**
   * Get the distance from the top of an item's icon/active indicator to the top of the navigation
   * bar item.
   */
  @Px
  public int getItemPaddingTop() {
    return itemPaddingTop;
  }

  /**
   * Set the distance from the top of an items icon/active indicator to the top of the navigation
   * bar item.
   */
  public void setItemPaddingTop(@Px int paddingTop) {
    itemPaddingTop = paddingTop;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setItemPaddingTop(paddingTop);
      }
    }
  }

  /**
   * Get the distance from the bottom of an item's label to the bottom of the navigation bar item.
   */
  @Px
  public int getItemPaddingBottom() {
    return itemPaddingBottom;
  }

  /**
   * Set the distance from the bottom of an item's label to the bottom of the navigation bar item.
   */
  public void setItemPaddingBottom(@Px int paddingBottom) {
    itemPaddingBottom = paddingBottom;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setItemPaddingBottom(paddingBottom);
      }
    }
  }

  /**
   * Get the distance between the item's active indicator container and the label.
   */
  @Px
  public int getActiveIndicatorLabelPadding() {
    return itemActiveIndicatorLabelPadding;
  }

  /**
   * Set the distance between the active indicator container and the item's label.
   */
  public void setActiveIndicatorLabelPadding(@Px int activeIndicatorLabelPadding) {
    itemActiveIndicatorLabelPadding = activeIndicatorLabelPadding;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorLabelPadding(activeIndicatorLabelPadding);
      }
    }
  }

  /**
   * Returns whether or not an active indicator is enabled for the navigation bar.
   *
   * @return true if the active indicator is enabled.
   */
  public boolean getItemActiveIndicatorEnabled() {
    return itemActiveIndicatorEnabled;
  }

  /**
   * Set whether or not an active indicator is enabled for the navigation bar.
   *
   * @param enabled true if an active indicator should be shown.
   */
  public void setItemActiveIndicatorEnabled(boolean enabled) {
    this.itemActiveIndicatorEnabled = enabled;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorEnabled(enabled);
      }
    }
  }

  /**
   * Get the width of the selected item's active indicator.
   *
   * @return The width, in pixels, of the active indicator.
   */
  @Px
  public int getItemActiveIndicatorWidth() {
    return itemActiveIndicatorWidth;
  }

  /**
   * Set the width to be used for the selected item's active indicator.
   *
   * @param width The width, in pixels, of the active indicator.
   */
  public void setItemActiveIndicatorWidth(@Px int width) {
    this.itemActiveIndicatorWidth = width;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorWidth(width);
      }
    }
  }

  /**
   * Get the height of the selected item's active indicator.
   *
   * @return The height, in pixels, of the active indicator.
   */
  @Px
  public int getItemActiveIndicatorHeight() {
    return itemActiveIndicatorHeight;
  }

  /**
   * Set the height to be used for the selected item's active indicator.
   *
   * @param height The height, in pixels, of the active indicator.
   */
  public void setItemActiveIndicatorHeight(@Px int height) {
    this.itemActiveIndicatorHeight = height;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorHeight(height);
      }
    }
  }

  /**
   * Get the margin that will be maintained at the start and end of the active indicator away from
   * the edges of its parent container.
   *
   * @return The horizontal margin, in pixels.
   */
  @Px
  public int getItemActiveIndicatorMarginHorizontal() {
    return itemActiveIndicatorMarginHorizontal;
  }

  /**
   * Set the horizontal margin that will be maintained at the start and end of the active indicator,
   * making sure the indicator remains the given distance from the edge of its parent container.
   *
   * @param marginHorizontal The horizontal margin, in pixels.
   */
  public void setItemActiveIndicatorMarginHorizontal(@Px int marginHorizontal) {
    itemActiveIndicatorMarginHorizontal = marginHorizontal;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorMarginHorizontal(marginHorizontal);
      }
    }
  }

  /**
   * Get the {@link ShapeAppearanceModel} of the active indicator drawable.
   *
   * @return The {@link ShapeAppearanceModel} of the active indicator drawable.
   */
  @Nullable
  public ShapeAppearanceModel getItemActiveIndicatorShapeAppearance() {
    return itemActiveIndicatorShapeAppearance;
  }

  /**
   * Set the {@link ShapeAppearanceModel} of the active indicator drawable.
   *
   * @param shapeAppearance The {@link ShapeAppearanceModel} of the active indicator drawable.
   */
  public void setItemActiveIndicatorShapeAppearance(
      @Nullable ShapeAppearanceModel shapeAppearance) {
    this.itemActiveIndicatorShapeAppearance = shapeAppearance;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorDrawable(createItemActiveIndicatorDrawable());
      }
    }
  }

  /**
   * Get whether the active indicator can be resized.
   */
  protected boolean isItemActiveIndicatorResizeable() {
    return this.itemActiveIndicatorResizeable;
  }

  /**
   * Set whether the active indicator can be resized. If true, the indicator will automatically
   * change size in response to label visibility modes.
   */
  protected void setItemActiveIndicatorResizeable(boolean resizeable) {
    this.itemActiveIndicatorResizeable = resizeable;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorResizeable(resizeable);
      }
    }
  }

  /**
   * Get the color of the active indicator drawable.
   *
   * @return A {@link ColorStateList} used as the color of the active indicator.
   */
  @Nullable
  public ColorStateList getItemActiveIndicatorColor() {
    return itemActiveIndicatorColor;
  }

  /**
   * Set the {@link ColorStateList} of the active indicator drawable.
   *
   * @param csl The {@link ColorStateList} used as the color of the active indicator.
   */
  public void setItemActiveIndicatorColor(@Nullable ColorStateList csl) {
    this.itemActiveIndicatorColor = csl;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        item.setActiveIndicatorDrawable(createItemActiveIndicatorDrawable());
      }
    }
  }

  /**
   * Create a drawable using the {@code itemActiveIndicatorShapeAppearance} and {@code
   * itemActiveIndicatorColor} to be used as an item's active indicator.
   *
   * <p>This method is called once per menu item so each item has a unique drawable instance which
   * can be manipulated/animated independently.
   *
   * @return A drawable to be used as a menu item's active indicator.
   */
  @Nullable
  private Drawable createItemActiveIndicatorDrawable() {
    if (itemActiveIndicatorShapeAppearance != null && itemActiveIndicatorColor != null) {
      MaterialShapeDrawable drawable =
          new MaterialShapeDrawable(itemActiveIndicatorShapeAppearance);
      drawable.setFillColor(itemActiveIndicatorColor);
      return drawable;
    }

    return null;
  }

  /**
   * Returns the resource ID for the background of the menu items.
   *
   * @return the resource ID for the background
   * @deprecated Use {@link #getItemBackground()} instead.
   */
  @Deprecated
  public int getItemBackgroundRes() {
    return itemBackgroundRes;
  }

  /**
   * Sets the drawable to be used for item backgrounds.
   *
   * @param background the drawable of the background
   */
  public void setItemBackground(@Nullable Drawable background) {
    itemBackground = background;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) {
          break;
        }
        item.setItemBackground(background);
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setItemBackground(background);
    }
  }

  /**
   * Returns the drawable for the background of the menu items.
   *
   * @return the drawable for the background
   */
  @Nullable
  public Drawable getItemBackground() {
    if (buttons != null && buttons.length > 0) {
      // Return button background instead of itemBackground if possible, so that the correct
      // drawable is returned if the background is set via #setItemBackgroundRes.
      return buttons[0].getBackground();
    } else {
      return itemBackground;
    }
  }

  /**
   * Sets the navigation items' label visibility mode.
   *
   * <p>The label is either always shown, never shown, or only shown when activated. Also supports
   * "auto" mode, which uses the item count to determine whether to show or hide the label.
   *
   * @param labelVisibilityMode mode which decides whether or not the label should be shown. Can be
   *     one of {@link NavigationBarView#LABEL_VISIBILITY_AUTO}, {@link
   *     NavigationBarView#LABEL_VISIBILITY_SELECTED}, {@link
   *     NavigationBarView#LABEL_VISIBILITY_LABELED}, or {@link
   *     NavigationBarView#LABEL_VISIBILITY_UNLABELED}
   * @see #getLabelVisibilityMode()
   */
  public void setLabelVisibilityMode(@NavigationBarView.LabelVisibility int labelVisibilityMode) {
    this.labelVisibilityMode = labelVisibilityMode;
  }

  /**
   * Returns the current label visibility mode.
   *
   * @see #setLabelVisibilityMode(int)
   */
  public int getLabelVisibilityMode() {
    return labelVisibilityMode;
  }

  /**
   * Sets an {@link android.view.View.OnTouchListener} for the item view associated with the
   * provided {@code menuItemId}.
   */
  @SuppressLint("ClickableViewAccessibility")
  public void setItemOnTouchListener(int menuItemId, @Nullable OnTouchListener onTouchListener) {
    if (onTouchListener == null) {
      onTouchListeners.remove(menuItemId);
    } else {
      onTouchListeners.put(menuItemId, onTouchListener);
    }
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) return;//sesl
        if (item.getItemData().getItemId() == menuItemId) {
          item.setOnTouchListener(onTouchListener);
        }
      }
    }
  }

  @Nullable
  public ColorStateList createDefaultColorStateList(int baseColorThemeAttr) {
    final TypedValue value = new TypedValue();
    if (!getContext().getTheme().resolveAttribute(baseColorThemeAttr, value, true)) {
      return null;
    }
    ColorStateList baseColor = AppCompatResources.getColorStateList(getContext(), value.resourceId);
    if (!getContext()
        .getTheme()
        .resolveAttribute(R.attr.colorPrimary, value, true)) {
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

  public void setPresenter(@NonNull NavigationBarPresenter presenter) {
    this.presenter = presenter;
  }

  @SuppressLint("ClickableViewAccessibility")
  public void buildMenuView() {
    this.removeAllViews();
    TransitionManager.beginDelayedTransition(this, this.set);
    NavigationBarItemView[] var1 = this.buttons;
    byte var2 = 0;
    int var3;
    int var4;
    if (var1 != null && this.mUseItemPool) {
      var3 = var1.length;

      for(var4 = 0; var4 < var3; ++var4) {
        NavigationBarItemView var5 = var1[var4];
        if (var5 != null) {
          this.itemPool.release(var5);
          var5.removeBadge();
          this.seslRemoveBadge(var5.getId());
        }
      }
    }

    if (this.mOverflowButton != null) {
      this.seslRemoveBadge(R.id.bottom_overflow);
    }

    int var6 = this.menu.size();
    if (var6 == 0) {
      this.selectedItemId = 0;
      this.selectedItemPosition = 0;
      this.buttons = null;
      this.mVisibleItemCount = 0;
      this.mOverflowButton = null;
      this.mOverflowMenu = null;
      this.mVisibleBtns = null;
      this.mInvisibleBtns = null;
    } else {
      this.removeUnusedBadges();
      boolean var7 = this.isShifting(this.labelVisibilityMode, this.menu.getVisibleItems().size());
      this.buttons = new NavigationBarItemView[this.menu.size()];
      this.mVisibleBtns = new InternalBtnInfo(var6);
      this.mInvisibleBtns = new InternalBtnInfo(var6);
      this.mOverflowMenu = new MenuBuilder(this.getContext());
      this.mVisibleBtns.cnt = 0;
      this.mInvisibleBtns.cnt = 0;
      byte var8 = 0;
      var3 = var8;
      int var9 = var8;

      int var10;
      int[] var11;
      com.google.android.material.navigation.NavigationBarMenuView.InternalBtnInfo var14;
      for(var4 = var8; var4 < var6; var3 = var10) {
        this.presenter.setUpdateSuspended(true);
        this.menu.getItem(var4).setCheckable(true);
        this.presenter.setUpdateSuspended(false);
        int var16;
        if (((MenuItemImpl)this.menu.getItem(var4)).requiresOverflow()) {
          var11 = this.mInvisibleBtns.originPos;
          var14 = this.mInvisibleBtns;
          var16 = var14.cnt++;
          var11[var16] = var4;
          var16 = var9;
          var10 = var3;
          if (!this.menu.getItem(var4).isVisible()) {
            var16 = var9 + 1;
            var10 = var3;
          }
        } else {
          var11 = this.mVisibleBtns.originPos;
          var14 = this.mVisibleBtns;
          var16 = var14.cnt++;
          var11[var16] = var4;
          var16 = var9;
          var10 = var3;
          if (this.menu.getItem(var4).isVisible()) {
            var10 = var3 + 1;
            var16 = var9;
          }
        }

        ++var4;
        var9 = var16;
      }

      byte var18;
      if (this.mInvisibleBtns.cnt - var9 > 0) {
        var18 = 1;
      } else {
        var18 = 0;
      }

      this.mHasOverflowMenu = var18 == 1;
      var3 += var18;
      var4 = this.mMaxItemCount;
      if (var3 > var4) {
        var3 -= var4 - 1;
        var4 = var3;
        if (var18 != 0) {
          var4 = var3 - 1;
        }

        var9 = this.mVisibleBtns.cnt - 1;
        var3 = var4;

        for(var4 = var9; var4 >= 0; --var4) {
          if (!this.menu.getItem(this.mVisibleBtns.originPos[var4]).isVisible()) {
            var11 = this.mInvisibleBtns.originPos;
            var14 = this.mInvisibleBtns;
            var9 = var14.cnt++;
            var11[var9] = this.mVisibleBtns.originPos[var4];
            var14 = this.mVisibleBtns;
            --var14.cnt;
          } else {
            var11 = this.mInvisibleBtns.originPos;
            var14 = this.mInvisibleBtns;
            var9 = var14.cnt++;
            var11[var9] = this.mVisibleBtns.originPos[var4];
            var14 = this.mVisibleBtns;
            --var14.cnt;
            var9 = var3 - 1;
            var3 = var9;
            if (var9 == 0) {
              break;
            }
          }
        }
      }

      this.mVisibleItemCount = 0;
      this.mViewVisibleItemCount = 0;

      for(var4 = 0; var4 < this.mVisibleBtns.cnt; ++var4) {
        this.buildInternalMenu(var7, this.mVisibleBtns.originPos[var4]);
      }

      NavigationBarItemView[] var17;
      if (this.mInvisibleBtns.cnt > 0) {
        var9 = 0;

        for(var4 = var9; var9 < this.mInvisibleBtns.cnt; var4 = var3) {
          MenuItemImpl var12 = (MenuItemImpl)this.menu.getItem(this.mInvisibleBtns.originPos[var9]);
          var3 = var4;
          if (var12 != null) {
            CharSequence var15;
            if (var12.getTitle() == null) {
              var15 = var12.getContentDescription();
            } else {
              var15 = var12.getTitle();
            }

            this.mOverflowMenu.add(var12.getGroupId(), var12.getItemId(), var12.getOrder(), var15).setVisible(var12.isVisible()).setEnabled(var12.isEnabled());
            this.mOverflowMenu.setGroupDividerEnabled(this.mHasGroupDivider);
            var12.setBadgeText(var12.getBadgeText());
            var3 = var4;
            if (!var12.isVisible()) {
              var3 = var4 + 1;
            }
          }

          ++var9;
        }

        if (this.mInvisibleBtns.cnt - var4 > 0) {
          this.mOverflowButton = this.ensureOverflowButton(var7);
          var17 = this.buttons;
          var4 = this.mVisibleBtns.cnt;
          NavigationBarItemView var13 = this.mOverflowButton;
          var17[var4] = var13;
          ++this.mVisibleItemCount;
          ++this.mViewVisibleItemCount;
          var13.setVisibility(View.VISIBLE);
        }
      }

      var4 = var2;
      if (this.mViewVisibleItemCount > this.mMaxItemCount) {
        Log.i(TAG, "Maximum number of visible items supported by BottomNavigationView is " + this.mMaxItemCount + ". Current visible count is " + this.mViewVisibleItemCount);
        var4 = this.mMaxItemCount;
        this.mVisibleItemCount = var4;
        this.mViewVisibleItemCount = var4;
        var4 = var2;
      }

      while(true) {
        var17 = this.buttons;
        if (var4 >= var17.length) {
          var4 = Math.min(this.mMaxItemCount - 1, this.selectedItemPosition);
          this.selectedItemPosition = var4;
          this.menu.getItem(var4).setChecked(true);
          return;
        }

        this.setShowButtonShape(var17[var4]);
        ++var4;
      }
    }
  }

  //sesl
  public void updateMenuView() {
    MenuBuilder var1 = this.menu;
    if (var1 != null && this.buttons != null && this.mVisibleBtns != null && this.mInvisibleBtns != null) {
      int var2 = var1.size();
      this.hideOverflowMenu();
      if (var2 != this.mVisibleBtns.cnt + this.mInvisibleBtns.cnt) {
        this.buildMenuView();
        return;
      }

      int var3 = this.selectedItemId;

      for(var2 = 0; var2 < this.mVisibleBtns.cnt; ++var2) {
        MenuItem var8 = this.menu.getItem(this.mVisibleBtns.originPos[var2]);
        if (var8.isChecked()) {
          this.selectedItemId = var8.getItemId();
          this.selectedItemPosition = var2;
        }

        if (var8 instanceof SeslMenuItem) {
          SeslMenuItem var4 = (SeslMenuItem)var8;
          this.seslRemoveBadge(var8.getItemId());
          if (var4.getBadgeText() != null) {
            this.seslAddBadge(var4.getBadgeText(), var8.getItemId());
          }
        }
      }

      if (var3 != this.selectedItemId) {
        TransitionManager.beginDelayedTransition(this, this.set);
      }

      boolean var5 = this.isShifting(this.labelVisibilityMode, this.menu.getVisibleItems().size());

      for(var2 = 0; var2 < this.mVisibleBtns.cnt; ++var2) {
        this.presenter.setUpdateSuspended(true);
        this.buttons[var2].setLabelVisibilityMode(this.labelVisibilityMode);
        this.buttons[var2].setShifting(var5);
        this.buttons[var2].initialize((MenuItemImpl)this.menu.getItem(this.mVisibleBtns.originPos[var2]), 0);
        this.presenter.setUpdateSuspended(false);
      }

      var3 = 0;

      int var6;
      for(var2 = var3; var3 < this.mInvisibleBtns.cnt; var2 = var6) {
        MenuItem var10 = this.menu.getItem(this.mInvisibleBtns.originPos[var3]);
        var6 = var2;
        if (var10 instanceof SeslMenuItem) {
          MenuBuilder var7 = this.mOverflowMenu;
          var6 = var2;
          if (var7 != null) {
            SeslMenuItem var9 = (SeslMenuItem)var10;
            MenuItem var12 = var7.findItem(var10.getItemId());
            if (var12 instanceof SeslMenuItem) {
              var12.setTitle(var10.getTitle());
              ((SeslMenuItem)var12).setBadgeText(var9.getBadgeText());
            }

            byte var11;
            if (var9.getBadgeText() != null) {
              var11 = 1;
            } else {
              var11 = 0;
            }

            var6 = var2 | var11;
          }
        }

        ++var3;
      }

      if (var2 != 0) {
        this.seslAddBadge(this.getContext().getResources().getString(R.string.sesl_material_overflow_badge_text_n), R.id.bottom_overflow);
      } else {
        this.seslRemoveBadge(R.id.bottom_overflow);
      }
    }
  }

  private NavigationBarItemView getNewItem() {
    NavigationBarItemView item = itemPool.acquire();
    if (item == null) {
      item = createNavigationBarItemView(getContext());
    }
    return item;
  }

  public int getSelectedItemId() {
    return selectedItemId;
  }

  protected boolean isShifting(
      @NavigationBarView.LabelVisibility int labelVisibilityMode, int childCount) {
    return labelVisibilityMode == NavigationBarView.LABEL_VISIBILITY_SELECTED;//sesl
  }

  void tryRestoreSelectedItemId(int itemId) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      MenuItem item = menu.getItem(i);
      if (itemId == item.getItemId()) {
        selectedItemId = itemId;
        selectedItemPosition = i;
        item.setChecked(true);
        break;
      }
    }
  }

  SparseArray<BadgeDrawable> getBadgeDrawables() {
    return badgeDrawables;
  }

  //sesl
  void setBadgeDrawables(SparseArray<BadgeDrawable> badgeDrawables) {
    this.badgeDrawables = badgeDrawables;
    if (buttons != null) {
      for (NavigationBarItemView itemView : buttons) {
        if (itemView != null) {
          itemView.setBadge(badgeDrawables.get(itemView.getId()));
        } else {
          return;
        }
      }
    }
  }

  @Nullable
  public BadgeDrawable getBadge(int menuItemId) {
    return badgeDrawables.get(menuItemId);
  }

  /**
   * Creates an instance of {@link BadgeDrawable} if none exists. Initializes (if needed) and
   * returns the associated instance of {@link BadgeDrawable}.
   *
   * @param menuItemId Id of the menu item.
   * @return an instance of BadgeDrawable associated with {@code menuItemId}.
   */
  BadgeDrawable getOrCreateBadge(int menuItemId) {
    validateMenuItemId(menuItemId);
    BadgeDrawable badgeDrawable = badgeDrawables.get(menuItemId);
    // Create an instance of BadgeDrawable if none were already initialized for this menu item.
    if (badgeDrawable == null) {
      badgeDrawable = BadgeDrawable.create(getContext());
      badgeDrawables.put(menuItemId, badgeDrawable);
    }
    NavigationBarItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      itemView.setBadge(badgeDrawable);
    }
    return badgeDrawable;
  }

  void removeBadge(int menuItemId) {
    validateMenuItemId(menuItemId);
    BadgeDrawable badgeDrawable = badgeDrawables.get(menuItemId);//sesl
    NavigationBarItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      itemView.removeBadge();
    }
    if (badgeDrawable != null) {//sesl
      badgeDrawables.remove(menuItemId);//sesl
    }
  }

  private void setBadgeIfNeeded(@NonNull NavigationBarItemView child) {
    int childId = child.getId();
    if (!isValidId(childId)) {
      // Child doesn't have a valid id, do not set any BadgeDrawable on the view.
      return;
    }

    BadgeDrawable badgeDrawable = badgeDrawables.get(childId);
    if (badgeDrawable != null) {
      child.setBadge(badgeDrawable);
    }
  }

  private void removeUnusedBadges() {
    HashSet<Integer> activeKeys = new HashSet<>();
    // Remove keys from badgeDrawables that don't have a corresponding value in the menu.
    for (int i = 0; i < menu.size(); i++) {
      activeKeys.add(menu.getItem(i).getItemId());
    }

    for (int i = 0; i < badgeDrawables.size(); i++) {
      int key = badgeDrawables.keyAt(i);
      if (!activeKeys.contains(key)) {
        badgeDrawables.delete(key);
      }
    }
  }

  @Nullable
  public NavigationBarItemView findItemView(int menuItemId) {
    validateMenuItemId(menuItemId);
    if (buttons != null) {
      for (NavigationBarItemView itemView : buttons) {
        if (itemView == null) return null;//sesl
        if (itemView.getId() == menuItemId) {
          return itemView;
        }
      }
    }
    return null;
  }

  /** Returns reference to newly created {@link NavigationBarItemView}. */
  @NonNull
  protected abstract NavigationBarItemView createNavigationBarItemView(@NonNull Context context);

  protected int getSelectedItemPosition() {
    return selectedItemPosition;
  }

  @Nullable
  protected MenuBuilder getMenu() {
    return menu;
  }

  private boolean isValidId(int viewId) {
    return viewId != View.NO_ID;
  }

  private void validateMenuItemId(int viewId) {
    if (!isValidId(viewId)) {
      throw new IllegalArgumentException(viewId + " is not a valid view id");
    }
  }


  protected void setOverflowSelectedCallback(MenuBuilder.Callback callback) {
    mSelectedCallback = callback;
  }

  boolean hasOverflowButton() {
    return mHasOverflowMenu;
  }

  MenuBuilder getOverflowMenu() {
    return mOverflowMenu;
  }

  void showOverflowMenu() {
    if (hasOverflowButton() && presenter != null) {
      presenter.showOverflowMenu(mOverflowMenu);
    }
  }

  void hideOverflowMenu() {
    if (hasOverflowButton() && presenter != null) {
      if (presenter.isOverflowMenuShowing()) {
        presenter.hideOverflowMenu();
      }
    }
  }

  void setMaxItemCount(int maxItemCount) {
    mMaxItemCount = maxItemCount;
  }

  private void setShowButtonShape(NavigationBarItemView itemView) {
    if (itemView != null) {
      ColorStateList itemTextColor = getItemTextColor();
      if (isShowButtonShapesEnabled()) {
        if (Build.VERSION.SDK_INT > 26) {
          final int color;
          if (mSBBTextColorDrawable != null) {
            color = mSBBTextColorDrawable.getColor();
          } else {
            color = getResources()
                .getColor(SeslMisc.isLightTheme(getContext()) ?
                    R.color.sesl_bottom_navigation_background_light
                    : R.color.sesl_bottom_navigation_background_dark, null);
          }
          itemView.setShowButtonShape(color, itemTextColor);

          if (mOverflowButton != null) {
            MenuItemImpl item = itemView.getItemData();
            if (item != null && mDummyMenu != null) {
              if (item.getItemId() == mDummyMenu.getItem(0).getItemId()) {
                setOverflowSpanColor(color, false);
              }
            }
          }
        } else {
          itemView.setShowButtonShape(0, itemTextColor);
        }
      }
    }
  }

  private NavigationBarItemView getNewItem(final int viewType) {
    NavigationBarItemView item = itemPool.acquire();
    if (item == null) {
      item = new NavigationBarItemView(getContext(), viewType) {
        @Override
        protected int getItemLayoutResId() {
          switch (viewType) {
            case NavigationBarView.SESL_TYPE_ICON_LABEL:
              return R.layout.sesl_bottom_navigation_item;
            case NavigationBarView.SESL_TYPE_ICON_ONLY:
              return R.layout.sesl_bottom_navigation_item;
            case NavigationBarView.SESL_TYPE_LABEL_ONLY:
              return R.layout.sesl_bottom_navigation_item_text;
            default:
              return R.layout.sesl_bottom_navigation_item;
          }
        }
      };
    }
    return item;
  }


  private void buildInternalMenu(boolean shifting, int index) {
    if (buttons != null) {
      NavigationBarItemView child = getNewItem(getViewType());
      buttons[mVisibleItemCount] = child;
      child.setVisibility(menu.getItem(index).isVisible() ? View.VISIBLE : View.GONE);
      child.setIconTintList(itemIconTint);
      child.setIconSize(itemIconSize);
      child.setTextColor(itemTextColorDefault);
      child.seslSetLabelTextAppearance(mSeslLabelTextAppearance);
      child.setTextAppearanceInactive(itemTextAppearanceInactive);
      child.setTextAppearanceActive(itemTextAppearanceActive);
      child.setTextColor(itemTextColorFromUser);
      if (itemBackground != null) {
        child.setItemBackground(itemBackground);
      } else {
        child.setItemBackground(itemBackgroundRes);
      }
      child.setShifting(shifting);
      child.setLabelVisibilityMode(labelVisibilityMode);
      child.initialize((MenuItemImpl) menu.getItem(index), 0);
      child.setItemPosition(mVisibleItemCount);
      child.setOnClickListener(onClickListener);

      if (selectedItemId != 0 && menu.getItem(index).getItemId() == selectedItemId) {
        selectedItemPosition = mVisibleItemCount;
      }

      MenuItemImpl item = (MenuItemImpl) menu.getItem(index);
      String badgeText = item.getBadgeText();
      if (badgeText != null) {
        seslAddBadge(badgeText, item.getItemId());
      } else {
        seslRemoveBadge(item.getItemId());
      }
      setBadgeIfNeeded(child);

      if (child.getParent() instanceof ViewGroup) {
        ((ViewGroup) child.getParent()).removeView(child);
      }
      addView(child);

      mVisibleItemCount++;
      if (child.getVisibility() == View.VISIBLE) {
        mViewVisibleItemCount++;
      }
    }
  }

  private NavigationBarItemView ensureOverflowButton(boolean shifting) {
    mHasOverflowMenu = true;
    mDummyMenu = new MenuBuilder(getContext());

    MenuInflater inflater = new MenuInflater(getContext());
    inflater.inflate(R.menu.nv_dummy_overflow_menu_icon, mDummyMenu);

    if (mDummyMenu.getItem(0) instanceof MenuItemImpl) {
      MenuItemImpl item = (MenuItemImpl) mDummyMenu.getItem(0);
      if (getViewType() == NavigationBarView.SESL_TYPE_ICON_LABEL) {
        item.setTooltipText(null);
      } else {
        item.setTooltipText(getResources().getString(R.string.sesl_more_item_label));
      }
    }

    NavigationBarItemView child = getNewItem(getViewType());
    child.setIconTintList(itemIconTint);
    child.setIconSize(itemIconSize);
    child.setTextColor(itemTextColorDefault);
    child.seslSetLabelTextAppearance(mSeslLabelTextAppearance);
    child.setTextAppearanceInactive(itemTextAppearanceInactive);
    child.setTextAppearanceActive(itemTextAppearanceActive);
    child.setTextColor(itemTextColorFromUser);
    if (itemBackground != null) {
      child.setItemBackground(itemBackground);
    } else {
      child.setItemBackground(itemBackgroundRes);
    }
    child.setShifting(shifting);
    child.setLabelVisibilityMode(labelVisibilityMode);
    child.initialize((MenuItemImpl) mDummyMenu.getItem(0), 0);
    child.setBadgeType(BADGE_TYPE_OVERFLOW);
    child.setItemPosition(mVisibleItemCount);
    child.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        mOverflowMenu.setCallback(mSelectedCallback);
        presenter.showOverflowMenu(mOverflowMenu);
      }
    });
    child.setContentDescription(getResources()
        .getString(R.string.sesl_action_menu_overflow_description));
    if (getViewType() == NavigationBarView.SESL_TYPE_LABEL_ONLY) {
      initOverflowSpan(child);
    }
    if (child.getParent() instanceof ViewGroup) {
      ((ViewGroup) child.getParent()).removeView(child);
    }
    addView(child);
    return child;
  }



  protected void updateBadgeIfNeeded() {
    if (buttons != null) {
      for (NavigationBarItemView itemView : buttons) {
        if (itemView != null) {
          updateBadge(itemView);
        } else {
          return;
        }
      }
    }
  }

  // TODO rework this method
  // kang
  private void updateBadge(NavigationBarItemView var1) {
    /* var1 = itemView */
    if (var1 != null) {
      TextView var2 = (TextView)var1.findViewById(R.id.notifications_badge);
      if (var2 != null) {
        Resources var3 = this.getResources();
        int var4 = var1.getBadgeType();
        int var5 = var3.getDimensionPixelOffset(R.dimen.sesl_bottom_navigation_dot_badge_size);
        int var6;
        if (this.mVisibleItemCount == this.mMaxItemCount) {
          var6 = var3.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_mode_min_padding_horizontal);
        } else {
          var6 = var3.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_mode_padding_horizontal);
        }

        int var7 = var3.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_N_badge_top_margin);
        int var8 = var3.getDimensionPixelSize(R.dimen.sesl_bottom_navigation_N_badge_start_margin);
        TextView var9 = var1.getLabel();
        int var10;
        if (var9 == null) {
          var10 = 1;
        } else {
          var10 = var9.getWidth();
        }

        int var11;
        if (var9 == null) {
          var11 = 1;
        } else {
          var11 = var9.getHeight();
        }

        int var13;
        int var14;
        if (var4 == 1) {
          ViewCompat.setBackground(var2, var3.getDrawable(R.drawable.sesl_dot_badge));
          var13 = var5;
          var14 = var5;
        } else {
          ViewCompat.setBackground(var2, var3.getDrawable(R.drawable.sesl_tab_n_badge));
          var2.measure(0, 0);
          var14 = var2.getMeasuredWidth();
          var13 = var2.getMeasuredHeight();
        }

        int var12;
        if (this.getViewType() != 3) {
          if (var4 == 1) {
            var6 = this.getItemIconSize() / 2;
            var12 = var5;
            var5 = var6;
          } else {
            var6 = var2.getMeasuredWidth() / 2 - var6;
            var12 = var5 / 2;
            var5 = var6;
          }
        } else if (var4 == 1) {
          var5 = (var10 + var2.getMeasuredWidth()) / 2;
          var12 = (var1.getHeight() - var11) / 2;
        } else if (var4 == 0) {
          var5 = (var10 - var2.getMeasuredWidth() - var8) / 2;
          var12 = (var1.getHeight() - var11) / 2 - var7;
        } else {
          var10 = (var10 + var2.getMeasuredWidth()) / 2;
          var6 = (var1.getHeight() - var11) / 2 - var7;
          var5 = var10;
          var12 = var6;
          if (var1.getWidth() / 2 + var10 + var2.getMeasuredWidth() / 2 > var1.getWidth()) {
            var5 = var10 + (var1.getWidth() - (var1.getWidth() / 2 + var10 + var2.getMeasuredWidth() / 2));
            var12 = var6;
          }
        }

        MarginLayoutParams var15 = (MarginLayoutParams) var2.getLayoutParams();
        var6 = var15.width;
        var11 = var15.leftMargin;
        if (var6 != var14 || var11 != var5) {
          var15.width = var14;
          var15.height = var13;
          var15.topMargin = var12;
          var15.setMarginStart(var5);
          var2.setLayoutParams(var15);
        }

      }
    }
  }
  // kang

  void seslAddBadge(String text, int menuItemId) {
    TextView badgeTextView;

    NavigationBarItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      View badgeContainer = itemView.findViewById(R.id.notifications_badge_container);
      if (badgeContainer != null) {
        badgeTextView = badgeContainer.findViewById(R.id.notifications_badge);
      } else {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        badgeContainer = inflater.inflate(R.layout.sesl_navigation_bar_badge_layout,
            this, false);
        badgeTextView = badgeContainer.findViewById(R.id.notifications_badge);
        itemView.addView(badgeContainer);
      }

      if (!isNumericValue(text)) {
        itemView.setBadgeNumberless(false);
      } else if (Integer.parseInt(text) > 999) {
        itemView.setBadgeNumberless(true);
        text = "999+";
      } else {
        itemView.setBadgeNumberless(false);
      }
    } else {
      badgeTextView = null;
    }

    if (badgeTextView != null) {
      badgeTextView.setText(text);
    }

    updateBadge(itemView);
  }

  void seslRemoveBadge(int menuItemId) {
    NavigationBarItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      View badgeContainer = itemView.findViewById(R.id.notifications_badge_container);
      if (badgeContainer != null) {
        itemView.removeView(badgeContainer);
      }
    }
  }

  private boolean isNumericValue(String value) {
    if (value == null) {
      return false;
    }
    try {
      Integer.parseInt(value);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    if (getViewType() != NavigationBarView.SESL_TYPE_LABEL_ONLY) {
      setItemIconSize(getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size));
      if (buttons != null) {
        for (NavigationBarItemView itemView : buttons) {
          if (itemView == null) {
            break;
          }
          itemView.updateLabelGroupTopMargin(
              getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size));
        }
      }
    }
    hideOverflowMenu();
  }

  class InternalBtnInfo {
    int cnt = 0;
    int[] originPos;

    InternalBtnInfo(int size) {
      originPos = new int[size];
    }
  }

  @RestrictTo(LIBRARY)
  public int getVisibleItemCount() {
    return mVisibleItemCount;
  }

  @RestrictTo(LIBRARY)
  public int getViewVisibleItemCount() {
    return mViewVisibleItemCount;
  }

  private boolean isShowButtonShapesEnabled() {
    return Settings.System.getInt(mContentResolver, "show_button_background", 0) == 1;
  }

  @RestrictTo(LIBRARY_GROUP_PREFIX)
  public void setBackgroundColorDrawable(ColorDrawable d) {
    mSBBTextColorDrawable = d;
  }

  @RestrictTo(LIBRARY_GROUP_PREFIX)
  public ColorDrawable getBackgroundColorDrawable() {
    return mSBBTextColorDrawable;
  }

  void setViewType(int viewType) {
    mViewType = viewType;
  }

  @RestrictTo(LIBRARY)
  public int getViewType() {
    return mViewType;
  }

  private void setOverflowSpanColor(int color, boolean fromUser) {
    if (mOverflowButton != null) {
      SpannableStringBuilder labelImageSpan = mOverflowButton.getLabelImageSpan();
      if (labelImageSpan != null) {
        Drawable d = getContext().getDrawable(R.drawable.sesl_ic_menu_overflow_dark);

        ImageSpan[] spans = labelImageSpan
            .getSpans(0, labelImageSpan.length(), ImageSpan.class);
        if (spans != null) {
          for (ImageSpan span : spans) {
            labelImageSpan.removeSpan(span);
          }
        }

        ImageSpan imageSpan = new ImageSpan(d);
        d.setState(
            new int[]{
                android.R.attr.state_enabled,
                -android.R.attr.state_enabled
            }
        );
        if (fromUser) {
          d.setTintList(itemTextColorFromUser);
        } else {
          d.setTint(color);
        }
        d.setBounds(0, 0,
            getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size),
            getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size));
        labelImageSpan.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

        mOverflowButton.setLabelImageSpan(labelImageSpan);
      }
    }
  }

  private void initOverflowSpan(NavigationBarItemView itemView) {
    Drawable d = getContext().getDrawable(R.drawable.sesl_ic_menu_overflow_dark);

    SpannableStringBuilder span = new SpannableStringBuilder(" ");
    ImageSpan imageSpan = new ImageSpan(d);
    d.setState(
        new int[]{
            android.R.attr.state_enabled,
            -android.R.attr.state_enabled
        }
    );
    d.setTintList(itemTextColorFromUser);
    d.setBounds(0, 0,
        getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size),
        getResources().getDimensionPixelSize(R.dimen.sesl_bottom_navigation_icon_size));
    span.setSpan(imageSpan, 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);

    itemView.setLabelImageSpan(span);
  }

  public void seslSetLabelTextAppearance(@StyleRes int textAppearanceRes) {
    mSeslLabelTextAppearance = textAppearanceRes;
    if (buttons != null) {
      for (NavigationBarItemView item : buttons) {
        if (item == null) {
          break;
        }
        item.setTextAppearanceInactive(textAppearanceRes);
        if (itemTextColorFromUser != null) {
          item.setTextColor(itemTextColorFromUser);
        }
      }
    }
    if (mOverflowButton != null) {
      mOverflowButton.setTextAppearanceInactive(textAppearanceRes);
      if (itemTextColorFromUser != null) {
        mOverflowButton.setTextColor(itemTextColorFromUser);
      }
    }
  }

  @StyleRes
  public int seslGetLabelTextAppearance() {
    return mSeslLabelTextAppearance;
  }

  void setGroupDividerEnabled(boolean enabled) {
    mHasGroupDivider = enabled;
    if (mOverflowMenu != null) {
      mOverflowMenu.setGroupDividerEnabled(enabled);
    } else {
      updateMenuView();
    }
  }

}
