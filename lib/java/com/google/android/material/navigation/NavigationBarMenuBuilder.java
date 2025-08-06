/*
 * Copyright (C) 2024 The Android Open Source Project
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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPresenter;
import android.view.MenuItem;
import android.view.SubMenu;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.ArrayList;
import java.util.List;

/**
 * Wrapper class for {@link MenuBuilder} that adds methods to support submenus as a part of the
 * menu.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationBarMenuBuilder {

  private final MenuBuilder menuBuilder;
  private final List<MenuItem> items;
  private int contentItemCount = 0;
  private int visibleContentItemCount = 0;
  private int visibleMainItemCount = 0;

  NavigationBarMenuBuilder(MenuBuilder menuBuilder) {
    this.menuBuilder = menuBuilder;
    items = new ArrayList<>();
    refreshItems();
  }

  /**
   * Returns total number of items in the menu, including submenus, submenu items, and dividers. For
   * example, a Menu with items {Item, Divider, SubMenu, SubMenuItem, Divider} would have a size of
   * 5.
   */
  public int size() {
    return items.size();
  }

  /**
   * Returns number of content (non-subheader) items in the menu.
   */
  public int getContentItemCount() {
    return contentItemCount;
  }

  /**
   * Returns number of visible content (non-subheader) items in the menu.
   */
  public int getVisibleContentItemCount() {
    return visibleContentItemCount;
  }

  /**
   * Returns number of visible main items in the menu, which correspond to any content items that
   * are not under a subheader.
   */
  public int getVisibleMainContentItemCount() {
    return visibleMainItemCount;
  }

  /**
   * Returns the item at the position i.
   */
  @NonNull
  public MenuItem getItemAt(int i) {
    return items.get(i);
  }

  /**
   * Calls the underlying {@link MenuBuilder#performItemAction(MenuItem, MenuPresenter, int)}
   */
  public boolean performItemAction(
      @NonNull MenuItem item, @NonNull MenuPresenter presenter, int flags) {
    return menuBuilder.performItemAction(item, presenter, flags);
  }

  /**
   * Refresh the items to match the current state of the underlying {@link MenuBuilder}.
   */
  public void refreshItems() {
    items.clear();
    contentItemCount = 0;
    visibleContentItemCount = 0;
    visibleMainItemCount = 0;
    for (int i = 0; i < menuBuilder.size(); i++) {
      MenuItem item = menuBuilder.getItem(i);
      if (item.hasSubMenu()) {
        if (!items.isEmpty()
            && !(items.get(items.size() - 1) instanceof DividerMenuItem)
            && item.isVisible()) {
          items.add(new DividerMenuItem());
        }
        items.add(item);
        SubMenu subMenu = item.getSubMenu();
        for (int j = 0; j < subMenu.size(); j++) {
          MenuItem submenuItem = subMenu.getItem(j);
          if (!item.isVisible()) {
            submenuItem.setVisible(false);
          }
          items.add(submenuItem);
          contentItemCount++;
          if (submenuItem.isVisible()) {
            visibleContentItemCount++;
          }
        }
        items.add(new DividerMenuItem());
      } else {
        items.add(item);
        contentItemCount++;
        if (item.isVisible()) {
          visibleContentItemCount++;
          visibleMainItemCount++;
        }
      }
    }

    if (!items.isEmpty() && items.get(items.size()-1) instanceof DividerMenuItem) {
      items.remove(items.size()-1);
    }
  }
}
