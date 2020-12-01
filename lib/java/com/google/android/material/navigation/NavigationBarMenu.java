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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import android.view.MenuItem;
import android.view.SubMenu;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Provides a {@link MenuBuilder} that can be used to build a menu that can be placed inside
 * navigation bar view such as the bottom navigation menu or the navigation rail view. This
 * implementation of the menu builder prevents the addition of submenus to the primary destinations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class NavigationBarMenu extends MenuBuilder {

  @NonNull private final Class<?> viewClass;
  private final int maxItemCount;

  public NavigationBarMenu(
      @NonNull Context context, @NonNull Class<?> viewClass, int maxItemCount) {
    super(context);
    this.viewClass = viewClass;
    this.maxItemCount = maxItemCount;
  }

  /** Returns the maximum number of items that can be shown in NavigationBarMenu. */
  public int getMaxItemCount() {
    return maxItemCount;
  }

  @NonNull
  @Override
  public SubMenu addSubMenu(int group, int id, int categoryOrder, @NonNull CharSequence title) {
    throw new UnsupportedOperationException(
        viewClass.getSimpleName() + " does not support submenus");
  }

  @Override
  @NonNull
  protected MenuItem addInternal(
      int group, int id, int categoryOrder, @NonNull CharSequence title) {
    if (size() + 1 > maxItemCount) {
      String viewClassName = viewClass.getSimpleName();
      throw new IllegalArgumentException(
          "Maximum number of items supported by "
              + viewClassName
              + " is "
              + maxItemCount
              + ". Limit can be checked with "
              + viewClassName
              + "#getMaxItemCount()");
    }
    stopDispatchingItemsChanged();
    final MenuItem item = super.addInternal(group, id, categoryOrder, title);
    if (item instanceof MenuItemImpl) {
      ((MenuItemImpl) item).setExclusiveCheckable(true);
    }
    startDispatchingItemsChanged();
    return item;
  }
}
