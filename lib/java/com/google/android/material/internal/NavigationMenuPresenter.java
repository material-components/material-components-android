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

package com.google.android.material.internal;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.MenuPresenter;
import androidx.appcompat.view.menu.MenuView;
import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerViewAccessibilityDelegate;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.Dimension;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.core.widget.TextViewCompat;
import java.util.ArrayList;

/** @hide */
@RestrictTo(LIBRARY_GROUP)
public class NavigationMenuPresenter implements MenuPresenter {

  public static final int NO_TEXT_APPEARANCE_SET = 0;

  private static final String STATE_HIERARCHY = "android:menu:list";
  private static final String STATE_ADAPTER = "android:menu:adapter";
  private static final String STATE_HEADER = "android:menu:header";

  private NavigationMenuView menuView;
  LinearLayout headerLayout;

  private Callback callback;
  MenuBuilder menu;
  private int id;

  NavigationMenuAdapter adapter;
  LayoutInflater layoutInflater;

  int subheaderTextAppearance = NO_TEXT_APPEARANCE_SET;
  @Nullable ColorStateList subheaderColor;
  int textAppearance = NO_TEXT_APPEARANCE_SET;
  boolean textAppearanceActiveBoldEnabled = true;
  ColorStateList textColor;
  ColorStateList iconTintList;
  Drawable itemBackground;
  RippleDrawable itemForeground;
  int itemHorizontalPadding;
  @Px int itemVerticalPadding;
  int itemIconPadding;
  int itemIconSize;
  @Px int dividerInsetStart;
  @Px int dividerInsetEnd;
  @Px int subheaderInsetStart;
  @Px int subheaderInsetEnd;
  boolean hasCustomItemIconSize;
  boolean isBehindStatusBar = true;
  private int itemMaxLines;

  /**
   * Padding to be inserted at the top of the list to avoid the first menu item from being placed
   * underneath the status bar.
   */
  private int paddingTopDefault;

  /** Padding for separators between items */
  int paddingSeparator;

  private int overScrollMode = -1;

  @Override
  public void initForMenu(@NonNull Context context, @NonNull MenuBuilder menu) {
    layoutInflater = LayoutInflater.from(context);
    this.menu = menu;
    Resources res = context.getResources();
    paddingSeparator =
        res.getDimensionPixelOffset(R.dimen.design_navigation_separator_vertical_padding);
  }

  @Override
  public MenuView getMenuView(ViewGroup root) {
    if (menuView == null) {
      menuView =
          (NavigationMenuView) layoutInflater.inflate(R.layout.design_navigation_menu, root, false);
      menuView.setAccessibilityDelegateCompat(
          new NavigationMenuViewAccessibilityDelegate(menuView));
      if (adapter == null) {
        adapter = new NavigationMenuAdapter();
        // Prevent recreating all the Views when notifyDataSetChanged() is called causing issues
        // with the a11y reader (see b/112931425)
        adapter.setHasStableIds(true);
      }
      if (overScrollMode != -1) {
        menuView.setOverScrollMode(overScrollMode);
      }
      headerLayout =
          (LinearLayout)
              layoutInflater.inflate(R.layout.design_navigation_item_header, menuView, false);
      headerLayout.setImportantForAccessibility(View.IMPORTANT_FOR_ACCESSIBILITY_NO);
      menuView.setAdapter(adapter);
    }
    return menuView;
  }

  private void updateAllTextMenuItems() {
    if (adapter != null) {
      adapter.updateAllTextMenuItems();
    }
  }

  private void updateAllSubHeaderMenuItems() {
    if (adapter != null) {
      adapter.updateAllSubHeaderMenuItems();
    }
  }

  private void updateAllDividerMenuItems() {
    if (adapter != null) {
      adapter.updateAllDividerMenuItems();
    }
  }

  @Override
  public void updateMenuView(boolean cleared) {
    if (adapter != null) {
      adapter.update();
    }
  }

  @Override
  public void setCallback(Callback cb) {
    callback = cb;
  }

  @Override
  public boolean onSubMenuSelected(SubMenuBuilder subMenu) {
    return false;
  }

  @Override
  public void onCloseMenu(MenuBuilder menu, boolean allMenusAreClosing) {
    if (callback != null) {
      callback.onCloseMenu(menu, allMenusAreClosing);
    }
  }

  @Override
  public boolean flagActionItems() {
    return false;
  }

  @Override
  public boolean expandItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  @Override
  public boolean collapseItemActionView(MenuBuilder menu, MenuItemImpl item) {
    return false;
  }

  @Override
  public int getId() {
    return id;
  }

  public void setId(int id) {
    this.id = id;
  }

  @NonNull
  @Override
  public Parcelable onSaveInstanceState() {
    final Bundle state = new Bundle();
    if (menuView != null) {
      SparseArray<Parcelable> hierarchy = new SparseArray<>();
      menuView.saveHierarchyState(hierarchy);
      state.putSparseParcelableArray(STATE_HIERARCHY, hierarchy);
    }
    if (adapter != null) {
      state.putBundle(STATE_ADAPTER, adapter.createInstanceState());
    }
    if (headerLayout != null) {
      SparseArray<Parcelable> header = new SparseArray<>();
      headerLayout.saveHierarchyState(header);
      state.putSparseParcelableArray(STATE_HEADER, header);
    }
    return state;
  }

  @Override
  public void onRestoreInstanceState(final Parcelable parcelable) {
    if (parcelable instanceof Bundle) {
      Bundle state = (Bundle) parcelable;
      SparseArray<Parcelable> hierarchy = state.getSparseParcelableArray(STATE_HIERARCHY);
      if (hierarchy != null) {
        menuView.restoreHierarchyState(hierarchy);
      }
      Bundle adapterState = state.getBundle(STATE_ADAPTER);
      if (adapterState != null) {
        adapter.restoreInstanceState(adapterState);
      }
      SparseArray<Parcelable> header = state.getSparseParcelableArray(STATE_HEADER);
      if (header != null) {
        headerLayout.restoreHierarchyState(header);
      }
    }
  }

  public void setCheckedItem(@NonNull MenuItemImpl item) {
    adapter.setCheckedItem(item);
  }

  @Nullable
  public MenuItemImpl getCheckedItem() {
    return adapter.getCheckedItem();
  }

  public View inflateHeaderView(@LayoutRes int res) {
    View view = layoutInflater.inflate(res, headerLayout, false);
    addHeaderView(view);
    return view;
  }

  public void addHeaderView(@NonNull View view) {
    headerLayout.addView(view);
    // The padding on top should be cleared.
    menuView.setPadding(0, 0, 0, menuView.getPaddingBottom());
  }

  public void removeHeaderView(@NonNull View view) {
    headerLayout.removeView(view);
    if (!hasHeader()) {
      menuView.setPadding(0, paddingTopDefault, 0, menuView.getPaddingBottom());
    }
  }

  public int getHeaderCount() {
    return headerLayout.getChildCount();
  }

  private boolean hasHeader() {
    return getHeaderCount() > 0;
  }

  public View getHeaderView(int index) {
    return headerLayout.getChildAt(index);
  }

  public void setSubheaderColor(@Nullable ColorStateList subheaderColor) {
    this.subheaderColor = subheaderColor;
    updateAllSubHeaderMenuItems();
  }

  public void setSubheaderTextAppearance(@StyleRes int resId) {
    subheaderTextAppearance = resId;
    updateAllSubHeaderMenuItems();
  }

  @Nullable
  public ColorStateList getItemTintList() {
    return iconTintList;
  }

  public void setItemIconTintList(@Nullable ColorStateList tint) {
    iconTintList = tint;
    updateAllTextMenuItems();
  }

  @Nullable
  public ColorStateList getItemTextColor() {
    return textColor;
  }

  public void setItemTextColor(@Nullable ColorStateList textColor) {
    this.textColor = textColor;
    updateAllTextMenuItems();
  }

  public void setItemTextAppearance(@StyleRes int resId) {
    textAppearance = resId;
    updateAllTextMenuItems();
  }

  public void setItemTextAppearanceActiveBoldEnabled(boolean isBold) {
    textAppearanceActiveBoldEnabled = isBold;
    updateAllTextMenuItems();
  }

  @Nullable
  public Drawable getItemBackground() {
    return itemBackground;
  }

  public void setItemBackground(@Nullable Drawable itemBackground) {
    this.itemBackground = itemBackground;
    updateAllTextMenuItems();
  }

  public void setItemForeground(@Nullable RippleDrawable itemForeground) {
    this.itemForeground = itemForeground;
    updateAllTextMenuItems();
  }

  public int getItemHorizontalPadding() {
    return itemHorizontalPadding;
  }

  public void setItemHorizontalPadding(int itemHorizontalPadding) {
    this.itemHorizontalPadding = itemHorizontalPadding;
    updateAllTextMenuItems();
  }

  @Px
  public int getItemVerticalPadding() {
    return itemVerticalPadding;
  }

  public void setItemVerticalPadding(@Px int itemVerticalPadding) {
    this.itemVerticalPadding = itemVerticalPadding;
    updateAllTextMenuItems();
  }

  @Px
  public int getDividerInsetStart() {
    return this.dividerInsetStart;
  }

  public void setDividerInsetStart(@Px int dividerInsetStart) {
    this.dividerInsetStart = dividerInsetStart;
    updateAllDividerMenuItems();
  }

  @Px
  public int getDividerInsetEnd() {
    return this.dividerInsetEnd;
  }

  public void setDividerInsetEnd(@Px int dividerInsetEnd) {
    this.dividerInsetEnd = dividerInsetEnd;
    updateAllDividerMenuItems();
  }

  @Px
  public int getSubheaderInsetStart() {
    return this.subheaderInsetStart;
  }

  public void setSubheaderInsetStart(@Px int subheaderInsetStart) {
    this.subheaderInsetStart = subheaderInsetStart;
    updateAllSubHeaderMenuItems();
  }

  @Px
  public int getSubheaderInsetEnd() {
    return this.subheaderInsetEnd;
  }

  public void setSubheaderInsetEnd(@Px int subheaderInsetEnd) {
    this.subheaderInsetEnd = subheaderInsetEnd;
    updateAllSubHeaderMenuItems();
  }

  public int getItemIconPadding() {
    return itemIconPadding;
  }

  public void setItemIconPadding(int itemIconPadding) {
    this.itemIconPadding = itemIconPadding;
    updateAllTextMenuItems();
  }

  public void setItemMaxLines(int itemMaxLines) {
    this.itemMaxLines = itemMaxLines;
    updateAllTextMenuItems();
  }

  public int getItemMaxLines() {
    return itemMaxLines;
  }

  public void setItemIconSize(@Dimension int itemIconSize) {
    if (this.itemIconSize != itemIconSize) {
      this.itemIconSize = itemIconSize;
      hasCustomItemIconSize = true;
      updateAllTextMenuItems();
    }
  }

  public void setUpdateSuspended(boolean updateSuspended) {
    if (adapter != null) {
      adapter.setUpdateSuspended(updateSuspended);
    }
  }

  /** Updates the top padding depending on if this view is drawn behind the status bar. */
  public void setBehindStatusBar(boolean behindStatusBar) {
    if (isBehindStatusBar != behindStatusBar) {
      isBehindStatusBar = behindStatusBar;
      updateTopPadding();
    }
  }

  /** True if the NavigationView will be drawn behind the status bar */
  public boolean isBehindStatusBar() {
    return isBehindStatusBar;
  }

  private void updateTopPadding() {
    int topPadding = 0;
    // Set padding if there's no header and we are drawing behind the status bar.
    if (!hasHeader() && isBehindStatusBar) {
      topPadding = paddingTopDefault;
    }

    menuView.setPadding(0, topPadding, 0, menuView.getPaddingBottom());
  }

  public void dispatchApplyWindowInsets(@NonNull WindowInsetsCompat insets) {
    int top = insets.getSystemWindowInsetTop();
    if (paddingTopDefault != top) {
      paddingTopDefault = top;
      // Apply the padding to the top of the view if it has changed.
      updateTopPadding();
    }

    // Always apply the bottom padding.
    menuView.setPadding(0, menuView.getPaddingTop(), 0, insets.getSystemWindowInsetBottom());
    ViewCompat.dispatchApplyWindowInsets(headerLayout, insets);
  }

  public void setOverScrollMode(int overScrollMode) {
    this.overScrollMode = overScrollMode;
    if (menuView != null) {
      menuView.setOverScrollMode(overScrollMode);
    }
  }

  private abstract static class ViewHolder extends RecyclerView.ViewHolder {

    public ViewHolder(View itemView) {
      super(itemView);
    }
  }

  private static class NormalViewHolder extends ViewHolder {

    public NormalViewHolder(
        @NonNull LayoutInflater inflater, ViewGroup parent, View.OnClickListener listener) {
      super(inflater.inflate(R.layout.design_navigation_item, parent, false));
      itemView.setOnClickListener(listener);
    }
  }

  private static class SubheaderViewHolder extends ViewHolder {

    public SubheaderViewHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
      super(inflater.inflate(R.layout.design_navigation_item_subheader, parent, false));
    }
  }

  private static class SeparatorViewHolder extends ViewHolder {

    public SeparatorViewHolder(@NonNull LayoutInflater inflater, ViewGroup parent) {
      super(inflater.inflate(R.layout.design_navigation_item_separator, parent, false));
    }
  }

  private static class HeaderViewHolder extends ViewHolder {

    public HeaderViewHolder(View itemView) {
      super(itemView);
    }
  }

  /**
   * Handles click events for the menu items. The items has to be {@link NavigationMenuItemView}.
   */
  final View.OnClickListener onClickListener =
      new View.OnClickListener() {

        @Override
        public void onClick(View view) {
          NavigationMenuItemView itemView = (NavigationMenuItemView) view;
          setUpdateSuspended(true);
          MenuItemImpl item = itemView.getItemData();
          boolean result = menu.performItemAction(item, NavigationMenuPresenter.this, 0);
          boolean checkStateChanged = false;
          if (item != null && item.isCheckable() && result) {
            adapter.setCheckedItem(item);
            checkStateChanged = true;
          }
          setUpdateSuspended(false);
          if (checkStateChanged) {
            updateMenuView(false);
          }
        }
      };

  private class NavigationMenuAdapter extends RecyclerView.Adapter<ViewHolder> {

    private static final String STATE_CHECKED_ITEM = "android:menu:checked";

    private static final String STATE_ACTION_VIEWS = "android:menu:action_views";
    private static final int VIEW_TYPE_NORMAL = 0;
    private static final int VIEW_TYPE_SUBHEADER = 1;
    private static final int VIEW_TYPE_SEPARATOR = 2;
    private static final int VIEW_TYPE_HEADER = 3;

    private final ArrayList<NavigationMenuItem> items = new ArrayList<>();
    private MenuItemImpl checkedItem;
    private boolean updateSuspended;

    NavigationMenuAdapter() {
      prepareMenuItems();
    }

    @Override
    public long getItemId(int position) {
      return position;
    }

    @Override
    public int getItemCount() {
      return items.size();
    }

    @Override
    public int getItemViewType(int position) {
      NavigationMenuItem item = items.get(position);
      if (item instanceof NavigationMenuSeparatorItem) {
        return VIEW_TYPE_SEPARATOR;
      } else if (item instanceof NavigationMenuHeaderItem) {
        return VIEW_TYPE_HEADER;
      } else if (item instanceof NavigationMenuTextItem) {
        NavigationMenuTextItem textItem = (NavigationMenuTextItem) item;
        if (textItem.getMenuItem().hasSubMenu()) {
          return VIEW_TYPE_SUBHEADER;
        } else {
          return VIEW_TYPE_NORMAL;
        }
      }
      throw new RuntimeException("Unknown item type.");
    }

    @Nullable
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      switch (viewType) {
        case VIEW_TYPE_NORMAL:
          return new NormalViewHolder(layoutInflater, parent, onClickListener);
        case VIEW_TYPE_SUBHEADER:
          return new SubheaderViewHolder(layoutInflater, parent);
        case VIEW_TYPE_SEPARATOR:
          return new SeparatorViewHolder(layoutInflater, parent);
        case VIEW_TYPE_HEADER:
          return new HeaderViewHolder(headerLayout);
      }
      return null;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
      switch (getItemViewType(position)) {
        case VIEW_TYPE_NORMAL:
          {
            NavigationMenuItemView itemView = (NavigationMenuItemView) holder.itemView;
            itemView.setIconTintList(iconTintList);
            itemView.setTextAppearance(textAppearance);
            if (textColor != null) {
              itemView.setTextColor(textColor);
            }
            itemView.setBackground(
                itemBackground != null ? itemBackground.getConstantState().newDrawable() : null);
            if (itemForeground != null) {
              itemView.setForeground(itemForeground.getConstantState().newDrawable());
            }
            NavigationMenuTextItem item = (NavigationMenuTextItem) items.get(position);
            itemView.setNeedsEmptyIcon(item.needsEmptyIcon);
            itemView.setPadding(
                itemHorizontalPadding,
                itemVerticalPadding,
                itemHorizontalPadding,
                itemVerticalPadding);
            itemView.setIconPadding(itemIconPadding);
            if (hasCustomItemIconSize) {
              itemView.setIconSize(itemIconSize);
            }
            itemView.setMaxLines(itemMaxLines);
            itemView.initialize(item.getMenuItem(), /* isBold= */ textAppearanceActiveBoldEnabled);
            setAccessibilityDelegate(itemView, position, false);
            break;
          }
        case VIEW_TYPE_SUBHEADER:
          {
            TextView subHeader = (TextView) holder.itemView;
            NavigationMenuTextItem item = (NavigationMenuTextItem) items.get(position);
            subHeader.setText(item.getMenuItem().getTitle());
            TextViewCompat.setTextAppearance(subHeader, subheaderTextAppearance);
            subHeader.setPaddingRelative(
                subheaderInsetStart,
                subHeader.getPaddingTop(),
                subheaderInsetEnd,
                subHeader.getPaddingBottom()
            );
            if (subheaderColor != null) {
              subHeader.setTextColor(subheaderColor);
            }
            setAccessibilityDelegate(subHeader, position, true);
            break;
          }
        case VIEW_TYPE_SEPARATOR:
          {
            NavigationMenuSeparatorItem item = (NavigationMenuSeparatorItem) items.get(position);
            holder.itemView.setPaddingRelative(
                dividerInsetStart,
                item.getPaddingTop(),
                dividerInsetEnd,
                item.getPaddingBottom());
            break;
          }
      }
    }

    private void setAccessibilityDelegate(View view, int position, boolean isHeader) {
      ViewCompat.setAccessibilityDelegate(
          view,
          new AccessibilityDelegateCompat() {
            @Override
            public void onInitializeAccessibilityNodeInfo(
                @NonNull View host, @NonNull AccessibilityNodeInfoCompat info) {
              super.onInitializeAccessibilityNodeInfo(host, info);
              info.setCollectionItemInfo(
                  CollectionItemInfoCompat.obtain(
                      /* rowIndex= */ adjustItemPositionForA11yDelegate(position),
                      /* rowSpan= */ 1,
                      /* columnIndex= */ 1,
                      /* columnSpan= */ 1,
                      /* heading= */ isHeader,
                      /* selected= */ host.isSelected()));
            }
          });
    }

    /** Adjusts position based on the presence of separators and header. */
    private int adjustItemPositionForA11yDelegate(int position) {
      int adjustedPosition = position;
      for (int i = 0; i < position; i++) {
        if (adapter.getItemViewType(i) == VIEW_TYPE_SEPARATOR
            || adapter.getItemViewType(i) == VIEW_TYPE_HEADER) {
          adjustedPosition--;
        }
      }
      return adjustedPosition;
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
      if (holder instanceof NormalViewHolder) {
        ((NavigationMenuItemView) holder.itemView).recycle();
      }
    }

    public void update() {
      int prevItemSize = items.size();
      prepareMenuItems();
      notifyDataSetChanged();
      // If there were no structural changes, update the items due to the adapter having stable ids.
      if (prevItemSize == items.size()) {
        notifyItemRangeChanged(0, items.size());
      }
    }

    /**
     * Flattens the visible menu items of {@link #menu} into {@link #items}, while inserting
     * separators between items when necessary.
     */
    private void prepareMenuItems() {
      if (updateSuspended) {
        return;
      }
      updateSuspended = true;
      items.clear();
      items.add(new NavigationMenuHeaderItem());

      int currentGroupId = -1;
      int currentGroupStart = 0;
      boolean currentGroupHasIcon = false;
      for (int i = 0, totalSize = menu.getVisibleItems().size(); i < totalSize; i++) {
        MenuItemImpl item = menu.getVisibleItems().get(i);
        if (item.isChecked()) {
          setCheckedItem(item);
        }
        if (item.isCheckable()) {
          item.setExclusiveCheckable(false);
        }
        if (item.hasSubMenu()) {
          SubMenu subMenu = item.getSubMenu();
          if (subMenu.hasVisibleItems()) {
            if (i != 0) {
              items.add(new NavigationMenuSeparatorItem(paddingSeparator, 0));
            }
            items.add(new NavigationMenuTextItem(item));
            boolean subMenuHasIcon = false;
            int subMenuStart = items.size();
            for (int j = 0, size = subMenu.size(); j < size; j++) {
              MenuItemImpl subMenuItem = (MenuItemImpl) subMenu.getItem(j);
              if (subMenuItem.isVisible()) {
                if (!subMenuHasIcon && subMenuItem.getIcon() != null) {
                  subMenuHasIcon = true;
                }
                if (subMenuItem.isCheckable()) {
                  subMenuItem.setExclusiveCheckable(false);
                }
                if (subMenuItem.isChecked()) {
                  setCheckedItem(subMenuItem);
                }
                items.add(new NavigationMenuTextItem(subMenuItem));
              }
            }
            if (subMenuHasIcon) {
              appendTransparentIconIfMissing(subMenuStart, items.size());
            }
          }
        } else {
          int groupId = item.getGroupId();
          if (groupId != currentGroupId) { // first item in group
            currentGroupStart = items.size();
            currentGroupHasIcon = item.getIcon() != null;
            if (i != 0) {
              currentGroupStart++;
              items.add(new NavigationMenuSeparatorItem(paddingSeparator, paddingSeparator));
            }
          } else if (!currentGroupHasIcon && item.getIcon() != null) {
            currentGroupHasIcon = true;
            appendTransparentIconIfMissing(currentGroupStart, items.size());
          }
          NavigationMenuTextItem textItem = new NavigationMenuTextItem(item);
          textItem.needsEmptyIcon = currentGroupHasIcon;
          items.add(textItem);
          currentGroupId = groupId;
        }
      }
      updateSuspended = false;
    }

    private void appendTransparentIconIfMissing(int startIndex, int endIndex) {
      for (int i = startIndex; i < endIndex; i++) {
        NavigationMenuTextItem textItem = (NavigationMenuTextItem) items.get(i);
        textItem.needsEmptyIcon = true;
      }
    }

    public void setCheckedItem(@NonNull MenuItemImpl checkedItem) {
      if (this.checkedItem == checkedItem || !checkedItem.isCheckable()) {
        return;
      }
      if (this.checkedItem != null) {
        this.checkedItem.setChecked(false);
      }
      this.checkedItem = checkedItem;
      checkedItem.setChecked(true);
    }

    public MenuItemImpl getCheckedItem() {
      return checkedItem;
    }

    @NonNull
    public Bundle createInstanceState() {
      Bundle state = new Bundle();
      if (checkedItem != null) {
        state.putInt(STATE_CHECKED_ITEM, checkedItem.getItemId());
      }
      // Store the states of the action views.
      SparseArray<ParcelableSparseArray> actionViewStates = new SparseArray<>();
      for (int i = 0, size = items.size(); i < size; i++) {
        NavigationMenuItem navigationMenuItem = items.get(i);
        if (navigationMenuItem instanceof NavigationMenuTextItem) {
          MenuItemImpl item = ((NavigationMenuTextItem) navigationMenuItem).getMenuItem();
          View actionView = item != null ? item.getActionView() : null;
          if (actionView != null) {
            ParcelableSparseArray container = new ParcelableSparseArray();
            actionView.saveHierarchyState(container);
            actionViewStates.put(item.getItemId(), container);
          }
        }
      }
      state.putSparseParcelableArray(STATE_ACTION_VIEWS, actionViewStates);
      return state;
    }

    public void restoreInstanceState(@NonNull Bundle state) {
      int checkedItem = state.getInt(STATE_CHECKED_ITEM, 0);
      if (checkedItem != 0) {
        updateSuspended = true;
        for (int i = 0, size = items.size(); i < size; i++) {
          NavigationMenuItem item = items.get(i);
          if (item instanceof NavigationMenuTextItem) {
            MenuItemImpl menuItem = ((NavigationMenuTextItem) item).getMenuItem();
            if (menuItem != null && menuItem.getItemId() == checkedItem) {
              setCheckedItem(menuItem);
              break;
            }
          }
        }
        updateSuspended = false;
        prepareMenuItems();
      }
      // Restore the states of the action views.
      SparseArray<ParcelableSparseArray> actionViewStates =
          state.getSparseParcelableArray(STATE_ACTION_VIEWS);
      if (actionViewStates != null) {
        for (int i = 0, size = items.size(); i < size; i++) {
          NavigationMenuItem navigationMenuItem = items.get(i);
          if (!(navigationMenuItem instanceof NavigationMenuTextItem)) {
            continue;
          }
          MenuItemImpl item = ((NavigationMenuTextItem) navigationMenuItem).getMenuItem();
          if (item == null) {
            continue;
          }
          View actionView = item.getActionView();
          if (actionView == null) {
            continue;
          }
          ParcelableSparseArray container = actionViewStates.get(item.getItemId());
          if (container == null) {
            continue;
          }
          actionView.restoreHierarchyState(container);
        }
      }
    }

    public void setUpdateSuspended(boolean updateSuspended) {
      this.updateSuspended = updateSuspended;
    }

    /** Returns the number of rows that will be used for accessibility. */
    int getRowCount() {
      int itemCount = 0;
      for (int i = 0; i < adapter.getItemCount(); i++) {
        int type = adapter.getItemViewType(i);
        if (type == VIEW_TYPE_NORMAL || type == VIEW_TYPE_SUBHEADER) {
          itemCount++;
        }
      }
      return itemCount;
    }

    private void updateAllTextMenuItems() {
      for (int i = 0; i < items.size(); i++) {
        if (items.get(i) instanceof NavigationMenuTextItem
            && getItemViewType(i) == VIEW_TYPE_NORMAL) {
          notifyItemChanged(i);
        }
      }
    }

    private void updateAllSubHeaderMenuItems() {
      for (int i = 0; i < items.size(); i++) {
        if (items.get(i) instanceof NavigationMenuTextItem
            && getItemViewType(i) == VIEW_TYPE_SUBHEADER) {
          notifyItemChanged(i);
        }
      }
    }

    private void updateAllDividerMenuItems() {
      for (int i = 0; i < items.size(); i++) {
        if (items.get(i) instanceof NavigationMenuSeparatorItem) {
          notifyItemChanged(i);
        }
      }
    }
  }

  /** Unified data model for all sorts of navigation menu items. */
  private interface NavigationMenuItem {}

  /** Normal or subheader items. */
  private static class NavigationMenuTextItem implements NavigationMenuItem {

    private final MenuItemImpl menuItem;

    boolean needsEmptyIcon;

    NavigationMenuTextItem(MenuItemImpl item) {
      menuItem = item;
    }

    public MenuItemImpl getMenuItem() {
      return menuItem;
    }
  }

  /** Separator items. */
  private static class NavigationMenuSeparatorItem implements NavigationMenuItem {

    private final int paddingTop;

    private final int paddingBottom;

    public NavigationMenuSeparatorItem(int paddingTop, int paddingBottom) {
      this.paddingTop = paddingTop;
      this.paddingBottom = paddingBottom;
    }

    public int getPaddingTop() {
      return paddingTop;
    }

    public int getPaddingBottom() {
      return paddingBottom;
    }
  }

  /** Header (not subheader) items. */
  private static class NavigationMenuHeaderItem implements NavigationMenuItem {
    NavigationMenuHeaderItem() {}
    // The actual content is hold by NavigationMenuPresenter#mHeaderLayout.
  }

  private class NavigationMenuViewAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {

    NavigationMenuViewAccessibilityDelegate(@NonNull RecyclerView recyclerView) {
      super(recyclerView);
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(
        View host, @NonNull AccessibilityNodeInfoCompat info) {
      super.onInitializeAccessibilityNodeInfo(host, info);
      info.setCollectionInfo(
          CollectionInfoCompat.obtain(
              adapter.getRowCount(), /* columnCount= */ 1, /* hierarchical= */ false));
    }
  }
}
