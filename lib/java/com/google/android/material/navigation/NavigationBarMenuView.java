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
import static java.lang.Math.min;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuView;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.util.Pools;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.transition.AutoTransition;
import androidx.transition.TransitionManager;
import androidx.transition.TransitionSet;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.internal.TextScale;
import com.google.android.material.motion.MotionUtils;
import com.google.android.material.navigation.NavigationBarView.ItemIconGravity;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.HashSet;

/**
 * Provides a view that will be use to render a menu view inside a {@link NavigationBarView}.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public abstract class NavigationBarMenuView extends ViewGroup implements MenuView {
  private static final int NO_PADDING = -1;

  private static final int[] CHECKED_STATE_SET = {android.R.attr.state_checked};
  private static final int[] DISABLED_STATE_SET = {-android.R.attr.state_enabled};

  @Nullable private final TransitionSet set;
  @NonNull private final OnClickListener onClickListener;
  @Nullable private Pools.Pool<NavigationBarItemView> itemPool;

  @NonNull
  private final SparseArray<OnTouchListener> onTouchListeners = new SparseArray<>();

  @NavigationBarView.LabelVisibility private int labelVisibilityMode;

  @ItemIconGravity private int itemIconGravity;

  @Nullable private NavigationBarMenuItemView[] buttons;

  private static final int NO_SELECTED_ITEM = -1;
  private int selectedItemId = NO_SELECTED_ITEM;
  private int selectedItemPosition = NO_SELECTED_ITEM;

  @Nullable private ColorStateList itemIconTint;
  @Dimension private int itemIconSize;
  private ColorStateList itemTextColorFromUser;
  @Nullable private final ColorStateList itemTextColorDefault;
  @StyleRes private int itemTextAppearanceInactive;
  @StyleRes private int itemTextAppearanceActive;
  @StyleRes private int horizontalItemTextAppearanceInactive;
  @StyleRes private int horizontalItemTextAppearanceActive;
  private boolean itemTextAppearanceActiveBoldEnabled;
  private Drawable itemBackground;
  @Nullable private ColorStateList itemRippleColor;
  private int itemBackgroundRes;
  @NonNull private final SparseArray<BadgeDrawable> badgeDrawables =
      new SparseArray<>();
  private int itemPaddingTop = NO_PADDING;
  private int itemPaddingBottom = NO_PADDING;
  private int itemActiveIndicatorLabelPadding = NO_PADDING;
  private int iconLabelHorizontalSpacing = NO_PADDING;
  private boolean itemActiveIndicatorEnabled;
  private int itemActiveIndicatorWidth;
  private int itemActiveIndicatorHeight;
  private int itemActiveIndicatorExpandedWidth;

  private int itemActiveIndicatorExpandedHeight;

  private int itemActiveIndicatorMarginHorizontal;
  private int itemActiveIndicatorExpandedMarginHorizontal;
  private int itemGravity = NavigationBarView.ITEM_GRAVITY_TOP_CENTER;
  private ShapeAppearanceModel itemActiveIndicatorShapeAppearance;
  private boolean itemActiveIndicatorResizeable = false;
  private ColorStateList itemActiveIndicatorColor;

  private NavigationBarPresenter presenter;
  private NavigationBarMenuBuilder menu;
  private boolean measurePaddingFromLabelBaseline;
  private boolean scaleLabelWithFont;
  private int labelMaxLines = 1;

  private int itemPoolSize = 0;
  private boolean expanded;
  private MenuItem checkedItem = null;

  private static final int DEFAULT_COLLAPSED_MAX_COUNT = 7;
  private int collapsedMaxItemCount = DEFAULT_COLLAPSED_MAX_COUNT;
  private boolean dividersEnabled = false;
  private final Rect itemActiveIndicatorExpandedPadding = new Rect();

  public NavigationBarMenuView(@NonNull Context context) {
    super(context);

    itemTextColorDefault = createDefaultColorStateList(android.R.attr.textColorSecondary);

    if (this.isInEditMode()) {
      set = null;
    } else {
      set = new AutoTransition();
      set.setOrdering(TransitionSet.ORDERING_TOGETHER);
      set.excludeTarget(TextView.class, true);
      set.setDuration(
          MotionUtils.resolveThemeDuration(
              getContext(),
              R.attr.motionDurationMedium4,
              getResources().getInteger(R.integer.material_motion_duration_long_1)));
      set.setInterpolator(
          MotionUtils.resolveThemeInterpolator(
              getContext(),
              R.attr.motionEasingStandard,
              AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR));
      set.addTransition(new TextScale());
    }

    onClickListener =
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            final NavigationBarItemView itemView = (NavigationBarItemView) v;
            MenuItem item = itemView.getItemData();
            boolean result = menu.performItemAction(item, presenter, 0);
            if (item != null && item.isCheckable() && (!result || item.isChecked())) {
              // If the item action was not invoked successfully (ie if there's no listener) or if
              // the item was checked through the action, we should update the checked item.
              setCheckedItem(item);
            }
          }
        };

    setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_YES);
  }

  /**
   * Set the checked item in the menu view.
   *
   * @param checkedItem the item to set checked
   */
  public void setCheckedItem(@NonNull MenuItem checkedItem) {
    if (this.checkedItem == checkedItem || !checkedItem.isCheckable()) {
      return;
    }
    // Unset the previous checked item
    if (this.checkedItem != null && this.checkedItem.isChecked()) {
      this.checkedItem.setChecked(false);
    }
    checkedItem.setChecked(true);
    this.checkedItem = checkedItem;
  }

  /** Set the current expanded state. */
  public void setExpanded(boolean expanded) {
    this.expanded = expanded;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        item.setExpanded(expanded);
      }
    }
  }

  /** Returns the current expanded state. */
  public boolean isExpanded() {
    return expanded;
  }

  @Override
  public void initialize(@NonNull MenuBuilder menu) {
    this.menu = new NavigationBarMenuBuilder(menu);
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
        CollectionInfoCompat.obtain(
            /* rowCount= */ 1,
            /* columnCount= */ getCurrentVisibleContentItemCount(),
            /* hierarchical= */ false,
            /* selectionMode= */ CollectionInfoCompat.SELECTION_MODE_SINGLE));
  }

  /**
   * Sets the tint which is applied to the menu items' icons.
   *
   * @param tint the tint to apply
   */
  public void setIconTintList(@Nullable ColorStateList tint) {
    itemIconTint = tint;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setIconTintList(tint);
        }
      }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setIconSize(iconSize);
        }
      }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setTextColor(color);
        }
      }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setTextAppearanceInactive(textAppearanceRes);
        }
      }
    }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setTextAppearanceActive(textAppearanceRes);
        }
      }
    }
  }

  /**
   * Sets whether the active menu item label is bold.
   *
   * @param isBold whether the active menu item label is bold
   */
  public void setItemTextAppearanceActiveBoldEnabled(boolean isBold) {
    this.itemTextAppearanceActiveBoldEnabled = isBold;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setTextAppearanceActiveBoldEnabled(isBold);
        }
      }
    }
  }

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
   * Sets the text appearance to be used for inactive menu item labels when they are in the
   * horizontal item layout (when the start icon value is {@link
   * ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @param textAppearanceRes the text appearance ID used for inactive menu item labels
   */
  public void setHorizontalItemTextAppearanceInactive(@StyleRes int textAppearanceRes) {
    this.horizontalItemTextAppearanceInactive = textAppearanceRes;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setHorizontalTextAppearanceInactive(textAppearanceRes);
        }
      }
    }
  }

  /**
   * Returns the text appearance used for inactive menu item labels when they are in the horizontal
   * item layout (when the start icon value is {@link ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @return the text appearance ID used for inactive menu item labels
   */
  @StyleRes
  public int getHorizontalItemTextAppearanceInactive() {
    return horizontalItemTextAppearanceInactive;
  }

  /**
   * Sets the text appearance to be used for the active menu item label when they are in the
   * horizontal item layout (when the start icon value is {@link
   * ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @param textAppearanceRes the text appearance ID used for the active menu item label
   */
  public void setHorizontalItemTextAppearanceActive(@StyleRes int textAppearanceRes) {
    this.horizontalItemTextAppearanceActive = textAppearanceRes;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setHorizontalTextAppearanceActive(textAppearanceRes);
        }
      }
    }
  }

  /**
   * Returns the text appearance used for the active menu item label when they are in the horizontal
   * item layout (when the start icon value is {@link ItemIconGravity#ITEM_ICON_GRAVITY_START}).
   *
   * @return the text appearance ID used for the active menu item label
   */
  @StyleRes
  public int getHorizontalItemTextAppearanceActive() {
    return horizontalItemTextAppearanceActive;
  }

  /**
   * Sets the resource ID to be used for item backgrounds.
   *
   * @param background the resource ID of the background
   */
  public void setItemBackgroundRes(int background) {
    itemBackgroundRes = background;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemBackground(background);
        }
      }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemPaddingTop(paddingTop);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemPaddingBottom(itemPaddingBottom);
        }
      }
    }
  }

  public void setMeasurePaddingFromLabelBaseline(boolean measurePaddingFromLabelBaseline) {
    this.measurePaddingFromLabelBaseline = measurePaddingFromLabelBaseline;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setMeasureBottomPaddingFromLabelBaseline(measurePaddingFromLabelBaseline);
        }
      }
    }
  }

  public void setLabelFontScalingEnabled(boolean scaleLabelWithFont) {
    this.scaleLabelWithFont = scaleLabelWithFont;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setLabelFontScalingEnabled(scaleLabelWithFont);
        }
      }
    }
  }

  public boolean getScaleLabelTextWithFont() {
    return scaleLabelWithFont;
  }

  public void setLabelMaxLines(int labelMaxLines) {
    this.labelMaxLines = labelMaxLines;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setLabelMaxLines(labelMaxLines);
        }
      }
    }
  }

  public int getLabelMaxLines() {
    return labelMaxLines;
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setActiveIndicatorLabelPadding(activeIndicatorLabelPadding);
        }
      }
    }
  }

  /**
   * Get the horizontal distance between the item's icon and the label which
   * is shown when the item is in the {@link NavigationBarView#ITEM_ICON_GRAVITY_START}
   * configuration.
   */
  @Px
  public int getIconLabelHorizontalSpacing() {
    return iconLabelHorizontalSpacing;
  }

  /**
   * Set the horizontal distance between the icon and the label which is shown when the item is in
   * the {@link NavigationBarView#ITEM_ICON_GRAVITY_START} configuration.
   */
  public void setIconLabelHorizontalSpacing(
      @Px int iconLabelHorizontalSpacing) {
    this.iconLabelHorizontalSpacing = iconLabelHorizontalSpacing;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setIconLabelHorizontalSpacing(iconLabelHorizontalSpacing);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorEnabled(enabled);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorWidth(width);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorHeight(height);
        }
      }
    }
  }

  /**
   * Sets the navigation items' layout gravity.
   *
   * @param itemGravity the layout {@link android.view.Gravity} of the item
   * @see #getItemGravity()
   */
  public void setItemGravity(int itemGravity) {
    this.itemGravity = itemGravity;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemGravity(itemGravity);
        }
      }
    }
  }

  /**
   * Returns the navigation items' layout gravity.
   *
   * @see #setItemGravity(int)
   */
  public int getItemGravity() {
    return itemGravity;
  }

  /**
   * Get the width of the selected item's active indicator when expanded.
   *
   * @return The width, in pixels, of the active indicator when expanded.
   */
  @Px
  public int getItemActiveIndicatorExpandedWidth() {
    return itemActiveIndicatorExpandedWidth;
  }

  /**
   * Set the width to be used for the selected item's active indicator when it is expanded, ie. set
   * to an item gravity of {@link ItemIconGravity#ITEM_ICON_GRAVITY_START}.
   *
   * @param width The width, in pixels, of the menu item's expanded active indicator. The width may
   * also be set as {@link NavigationBarView#ACTIVE_INDICATOR_WIDTH_WRAP_CONTENT} or
   * {@link NavigationBarView#ACTIVE_INDICATOR_WIDTH_MATCH_PARENT}.
   */
  public void setItemActiveIndicatorExpandedWidth(@Px int width) {
    this.itemActiveIndicatorExpandedWidth = width;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorExpandedWidth(width);
        }
      }
    }
  }

  /**
   * Get the height of the selected item's active indicator when expanded.
   *
   * @return The height, in pixels, of the active indicator when expanded.
   */
  @Px
  public int getItemActiveIndicatorExpandedHeight() {
    return itemActiveIndicatorExpandedHeight;
  }

  /**
   * Set the height to be used for the selected item's active indicator when set to an item gravity
   * of {@link ItemIconGravity#ITEM_ICON_GRAVITY_START}.
   *
   * @param height The height, in pixels, of the active indicator.
   */
  public void setItemActiveIndicatorExpandedHeight(@Px int height) {
    this.itemActiveIndicatorExpandedHeight = height;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorExpandedHeight(height);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorMarginHorizontal(marginHorizontal);
        }
      }
    }
  }

  /**
   * Get the margin that will be maintained at the start and end of the expanded active indicator
   * away from the edges of its parent container.
   *
   * @return The horizontal margin, in pixels.
   */
  @Px
  public int getItemActiveIndicatorExpandedMarginHorizontal() {
    return itemActiveIndicatorExpandedMarginHorizontal;
  }

  /**
   * Set the horizontal margin that will be maintained at the start and end of the expanded active
   * indicator, making sure the indicator remains the given distance from the edge of its parent
   * container.
   *
   * @param marginHorizontal The horizontal margin, in pixels.
   */
  public void setItemActiveIndicatorExpandedMarginHorizontal(@Px int marginHorizontal) {
    itemActiveIndicatorExpandedMarginHorizontal = marginHorizontal;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setActiveIndicatorExpandedMarginHorizontal(marginHorizontal);
        }
      }
    }
  }

  /**
   * Set the padding of the expanded active indicator wrapping the content.
   *
   * @param paddingLeft The left padding, in pixels.
   * @param paddingTop The top padding, in pixels.
   * @param paddingRight The right padding, in pixels.
   * @param paddingBottom The bottom padding, in pixels.
   */
  public void setItemActiveIndicatorExpandedPadding(int paddingLeft, int paddingTop,
      int paddingRight, int paddingBottom) {
    itemActiveIndicatorExpandedPadding.left = paddingLeft;
    itemActiveIndicatorExpandedPadding.top = paddingTop;
    itemActiveIndicatorExpandedPadding.right = paddingRight;
    itemActiveIndicatorExpandedPadding.bottom = paddingBottom;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setActiveIndicatorExpandedPadding(itemActiveIndicatorExpandedPadding);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setActiveIndicatorDrawable(createItemActiveIndicatorDrawable());
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setActiveIndicatorResizeable(resizeable);
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item)
              .setActiveIndicatorDrawable(createItemActiveIndicatorDrawable());
        }
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemBackground(background);
        }
      }
    }
  }

  /**
   * Sets the color of the item's ripple.
   *
   * This will only be used if there is not a custom background set on the item.
   *
   * @param itemRippleColor the color of the ripple
   */
  public void setItemRippleColor(@Nullable ColorStateList itemRippleColor) {
    this.itemRippleColor = itemRippleColor;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemRippleColor(itemRippleColor);
        }
      }
    }
  }

  /**
   * Returns the color to be used for the items ripple.
   *
   * @return the color for the items ripple
   */
  @Nullable
  public ColorStateList getItemRippleColor() {
    return itemRippleColor;
  }

  /**
   * Returns the drawable for the background of the menu items.
   *
   * @return the drawable for the background
   */
  @Nullable
  public Drawable getItemBackground() {
    if (buttons != null && buttons.length > 0) {
      // Find the first instance of NavigationBarItemView
      for (NavigationBarMenuItemView button : buttons) {
        if (button instanceof NavigationBarItemView) {
          // Return button background instead of itemBackground if possible, so that the correct
          // drawable is returned if the background is set via #setItemBackgroundRes.
          return ((NavigationBarItemView) button).getBackground();
        }
      }
    }
    return itemBackground;
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
   * Sets the navigation items' icon gravity.
   *
   * @param itemIconGravity the placement of the icon in the nav item one of {@link
   *     NavigationBarView#ITEM_ICON_GRAVITY_TOP}, or {@link
   *     NavigationBarView#ITEM_ICON_GRAVITY_START}
   * @see #getItemIconGravity()
   */
  public void setItemIconGravity(@ItemIconGravity int itemIconGravity) {
    this.itemIconGravity = itemIconGravity;
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).setItemIconGravity(itemIconGravity);
        }
      }
    }
  }

  /**
   * Returns the current item icon gravity.
   *
   * @see #setItemIconGravity(int)
   */
  @ItemIconGravity
  public int getItemIconGravity() {
    return itemIconGravity;
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
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView
            && item.getItemData() != null
            && item.getItemData().getItemId() == menuItemId) {
          ((NavigationBarItemView) item).setOnTouchListener(onTouchListener);
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

  public void setPresenter(@NonNull NavigationBarPresenter presenter) {
    this.presenter = presenter;
  }

  private void releaseItemPool() {
    if (buttons != null && itemPool != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          itemPool.release((NavigationBarItemView) item);
          ((NavigationBarItemView) item).clear();
        }
      }
    }
  }

  private NavigationBarItemView createMenuItem(
      int index, MenuItemImpl item, boolean shifting, boolean hideWhenCollapsed) {
    presenter.setUpdateSuspended(true);
    item.setCheckable(true);
    presenter.setUpdateSuspended(false);
    NavigationBarItemView child = getNewItem();
    child.setShifting(shifting);
    child.setLabelMaxLines(labelMaxLines);
    child.setIconTintList(itemIconTint);
    child.setIconSize(itemIconSize);
    // Set the text color the default, then look for another text color in order of precedence.
    child.setTextColor(itemTextColorDefault);
    child.setTextAppearanceInactive(itemTextAppearanceInactive);
    child.setTextAppearanceActive(itemTextAppearanceActive);
    child.setHorizontalTextAppearanceInactive(horizontalItemTextAppearanceInactive);
    child.setHorizontalTextAppearanceActive(horizontalItemTextAppearanceActive);
    child.setTextAppearanceActiveBoldEnabled(itemTextAppearanceActiveBoldEnabled);
    child.setTextColor(itemTextColorFromUser);
    if (itemPaddingTop != NO_PADDING) {
      child.setItemPaddingTop(itemPaddingTop);
    }
    if (itemPaddingBottom != NO_PADDING) {
      child.setItemPaddingBottom(itemPaddingBottom);
    }
    child.setMeasureBottomPaddingFromLabelBaseline(measurePaddingFromLabelBaseline);
    child.setLabelFontScalingEnabled(scaleLabelWithFont);
    if (itemActiveIndicatorLabelPadding != NO_PADDING) {
      child.setActiveIndicatorLabelPadding(itemActiveIndicatorLabelPadding);
    }
    if (iconLabelHorizontalSpacing != NO_PADDING) {
      child.setIconLabelHorizontalSpacing(iconLabelHorizontalSpacing);
    }
    child.setActiveIndicatorWidth(itemActiveIndicatorWidth);
    child.setActiveIndicatorHeight(itemActiveIndicatorHeight);
    child.setActiveIndicatorExpandedWidth(itemActiveIndicatorExpandedWidth);
    child.setActiveIndicatorExpandedHeight(itemActiveIndicatorExpandedHeight);
    child.setActiveIndicatorMarginHorizontal(itemActiveIndicatorMarginHorizontal);
    child.setItemGravity(itemGravity);
    child.setActiveIndicatorExpandedPadding(itemActiveIndicatorExpandedPadding);
    child.setActiveIndicatorExpandedMarginHorizontal(itemActiveIndicatorExpandedMarginHorizontal);
    child.setActiveIndicatorDrawable(createItemActiveIndicatorDrawable());
    child.setActiveIndicatorResizeable(itemActiveIndicatorResizeable);
    child.setActiveIndicatorEnabled(itemActiveIndicatorEnabled);
    if (itemBackground != null) {
      child.setItemBackground(itemBackground);
    } else {
      child.setItemBackground(itemBackgroundRes);
    }
    child.setItemRippleColor(itemRippleColor);
    child.setLabelVisibilityMode(labelVisibilityMode);
    child.setItemIconGravity(itemIconGravity);
    child.setOnlyShowWhenExpanded(hideWhenCollapsed);
    child.setExpanded(expanded);
    child.initialize(item, 0);
    child.setItemPosition(index);
    int itemId = item.getItemId();
    child.setOnTouchListener(onTouchListeners.get(itemId));
    child.setOnClickListener(onClickListener);
    if (selectedItemId != Menu.NONE && itemId == selectedItemId) {
      selectedItemPosition = index;
    }
    setBadgeIfNeeded(child);
    return child;
  }

  @SuppressLint("ClickableViewAccessibility")
  public void buildMenuView() {
    removeAllViews();
    releaseItemPool();

    presenter.setUpdateSuspended(true);
    menu.refreshItems();
    presenter.setUpdateSuspended(false);

    int contentItemCount = menu.getContentItemCount();
    if (contentItemCount == 0) {
      selectedItemId = 0;
      selectedItemPosition = 0;
      buttons = null;
      itemPool = null;
      return;
    }

    if (itemPool == null || itemPoolSize != contentItemCount) {
      itemPoolSize = contentItemCount;
      itemPool = new Pools.SynchronizedPool<>(contentItemCount);
    }
    removeUnusedBadges();

    int menuSize = menu.size();
    buttons = new NavigationBarMenuItemView[menuSize];
    int collapsedItemsSoFar = 0;
    int nextSubheaderItemCount = 0;
    boolean shifting =
        isShifting(labelVisibilityMode, getCurrentVisibleContentItemCount());
    for (int i = 0; i < menuSize; i++) {
      MenuItem menuItem = menu.getItemAt(i);
      NavigationBarMenuItemView child;
      if (menuItem instanceof DividerMenuItem) {
        // Add a divider
        child = new NavigationBarDividerView(getContext());
        child.setOnlyShowWhenExpanded(true);
        ((NavigationBarDividerView) child).setDividersEnabled(dividersEnabled);
      } else if (menuItem.hasSubMenu()) {
        if (nextSubheaderItemCount > 0) {
          // We do not support submenus inside submenus. If there is still subheader items to be
          // instantiated, we should not have another submenu.
          throw new IllegalArgumentException(
              "Only one layer of submenu is supported; a submenu "
                  + "inside a submenu is not supported by the Navigation Bar.");
        }
        // Add subheader item
        child = new NavigationBarSubheaderView(getContext());
        ((NavigationBarSubheaderView) child).
            setTextAppearance(horizontalItemTextAppearanceActive != 0
                ? horizontalItemTextAppearanceActive : itemTextAppearanceActive);
        ((NavigationBarSubheaderView) child).setTextColor(itemTextColorFromUser);
        child.setOnlyShowWhenExpanded(true);
        child.initialize((MenuItemImpl) menuItem, 0);
        nextSubheaderItemCount = menuItem.getSubMenu().size();
      } else if (nextSubheaderItemCount > 0) { // Add submenu items
        child =
            createMenuItem(i, (MenuItemImpl) menuItem, shifting, /* hideWhenCollapsed= */ true);
        nextSubheaderItemCount--;
      } else {
        child =
            createMenuItem(
                i, (MenuItemImpl) menuItem, shifting, collapsedItemsSoFar >= collapsedMaxItemCount);
        collapsedItemsSoFar++;
      }
      if (!(menuItem instanceof DividerMenuItem)
          && menuItem.isCheckable()
          && selectedItemPosition == NO_SELECTED_ITEM) {
        selectedItemPosition = i;
      }
      buttons[i] = child;
      addView((View) child);
    }
    selectedItemPosition = min(menuSize - 1, selectedItemPosition);
    setCheckedItem(buttons[selectedItemPosition].getItemData());
  }

  private boolean isMenuStructureSame() {
    if (buttons == null || menu == null || menu.size() != buttons.length) {
      return false;
    }
    for (int i = 0; i < buttons.length; i++) {
      // If the menu item is a divider but the existing item is not a divider, return false
      if (menu.getItemAt(i) instanceof DividerMenuItem
          && !(buttons[i] instanceof NavigationBarDividerView)) {
        return false;
      }
      boolean incorrectSubheaderType =
          menu.getItemAt(i).hasSubMenu() && !(buttons[i] instanceof NavigationBarSubheaderView);
      boolean incorrectItemType =
          !menu.getItemAt(i).hasSubMenu() && !(buttons[i] instanceof NavigationBarItemView);
      if (!(menu.getItemAt(i) instanceof DividerMenuItem)
          && (incorrectSubheaderType || incorrectItemType)) {
          return false;
      }
    }
    return true;
  }

  public void updateMenuView() {
    if (menu == null || buttons == null) {
      return;
    }
    presenter.setUpdateSuspended(true);
    menu.refreshItems();
    presenter.setUpdateSuspended(false);

    if (!isMenuStructureSame()) {
      buildMenuView();
      return;
    }

    int previousSelectedId = selectedItemId;
    int menuSize = menu.size();

    for (int i = 0; i < menuSize; i++) {
      MenuItem item = menu.getItemAt(i);
      if (item.isChecked()) {
        setCheckedItem(item);
        selectedItemId = item.getItemId();
        selectedItemPosition = i;
      }
    }
    if (previousSelectedId != selectedItemId && set != null) {
      // Note: this has to be called before NavigationBarItemView#initialize().
      TransitionManager.beginDelayedTransition(this, set);
    }

    boolean shifting =
        isShifting(labelVisibilityMode, getCurrentVisibleContentItemCount());
    for (int i = 0; i < menuSize; i++) {
      presenter.setUpdateSuspended(true);
      buttons[i].setExpanded(expanded);
      if (buttons[i] instanceof NavigationBarItemView) {
        NavigationBarItemView itemView = (NavigationBarItemView) buttons[i];
        itemView.setLabelVisibilityMode(labelVisibilityMode);
        itemView.setItemIconGravity(itemIconGravity);
        itemView.setItemGravity(itemGravity);
        itemView.setShifting(shifting);
      }
      if (menu.getItemAt(i) instanceof MenuItemImpl) {
        buttons[i].initialize((MenuItemImpl) menu.getItemAt(i), 0);
      }
      presenter.setUpdateSuspended(false);
    }
  }

  private NavigationBarItemView getNewItem() {
    NavigationBarItemView item = itemPool != null ? itemPool.acquire() : null;
    if (item == null) {
      item = createNavigationBarItemView(getContext());
    }
    return item;
  }

  public void setSubmenuDividersEnabled(boolean dividersEnabled) {
    if (this.dividersEnabled == dividersEnabled) {
      return;
    }
    this.dividersEnabled = dividersEnabled;
    if (buttons != null) {
      for (NavigationBarMenuItemView itemView : buttons) {
        if (itemView instanceof NavigationBarDividerView) {
          ((NavigationBarDividerView) itemView).setDividersEnabled(dividersEnabled);
        }
      }
    }
  }

  public void setCollapsedMaxItemCount(int collapsedMaxCount) {
    this.collapsedMaxItemCount = collapsedMaxCount;
  }

  private int getCollapsedVisibleItemCount() {
    return min(collapsedMaxItemCount, menu.getVisibleMainContentItemCount());
  }

  public int getCurrentVisibleContentItemCount() {
    return expanded ? menu.getVisibleContentItemCount() : getCollapsedVisibleItemCount();
  }

  public int getSelectedItemId() {
    return selectedItemId;
  }

  protected boolean isShifting(
      @NavigationBarView.LabelVisibility int labelVisibilityMode, int childCount) {
    return labelVisibilityMode == NavigationBarView.LABEL_VISIBILITY_AUTO
        ? childCount > 3
        : labelVisibilityMode == NavigationBarView.LABEL_VISIBILITY_SELECTED;
  }

  void tryRestoreSelectedItemId(int itemId) {
    final int size = menu.size();
    for (int i = 0; i < size; i++) {
      MenuItem item = menu.getItemAt(i);
      if (itemId == item.getItemId()) {
        selectedItemId = itemId;
        selectedItemPosition = i;
        setCheckedItem(item);
        break;
      }
    }
  }

  SparseArray<BadgeDrawable> getBadgeDrawables() {
    return badgeDrawables;
  }

  void restoreBadgeDrawables(SparseArray<BadgeDrawable> badgeDrawables) {
    for (int i = 0; i < badgeDrawables.size(); i++) {
      int key = badgeDrawables.keyAt(i);
      if (this.badgeDrawables.indexOfKey(key) < 0) {
        // badge doesn't exist yet, restore it
        this.badgeDrawables.append(key, badgeDrawables.get(key));
      }
    }
    if (buttons != null) {
      for (NavigationBarMenuItemView itemView : buttons) {
        if (itemView instanceof NavigationBarItemView) {
          BadgeDrawable badge = this.badgeDrawables.get(((NavigationBarItemView) itemView).getId());
          if (badge != null) {
            ((NavigationBarItemView) itemView).setBadge(badge);
          }
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
    NavigationBarItemView itemView = findItemView(menuItemId);
    if (itemView != null) {
      itemView.removeBadge();
    }
    badgeDrawables.put(menuItemId, null);
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
      activeKeys.add(menu.getItemAt(i).getItemId());
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
      for (NavigationBarMenuItemView itemView : buttons) {
        if (itemView instanceof NavigationBarItemView) {
          if (((NavigationBarItemView) itemView).getId() == menuItemId) {
            return (NavigationBarItemView) itemView;
          }
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
  protected NavigationBarMenuBuilder getMenu() {
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

  public void updateActiveIndicator(int availableWidth) {
    if (buttons != null) {
      for (NavigationBarMenuItemView item : buttons) {
        if (item instanceof NavigationBarItemView) {
          ((NavigationBarItemView) item).updateActiveIndicatorLayoutParams(availableWidth);
        }
      }
    }
  }
}
