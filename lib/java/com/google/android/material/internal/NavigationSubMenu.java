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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.appcompat.view.menu.SubMenuBuilder;
import androidx.annotation.RestrictTo;

/**
 * This is a {@link SubMenuBuilder} that it notifies the parent {@link NavigationMenu} of its menu
 * updates.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class NavigationSubMenu extends SubMenuBuilder {

  public NavigationSubMenu(Context context, NavigationMenu menu, MenuItemImpl item) {
    super(context, menu, item);
  }

  @Override
  public void onItemsChanged(boolean structureChanged) {
    super.onItemsChanged(structureChanged);
    ((MenuBuilder) getParentMenu()).onItemsChanged(structureChanged);
  }
}
