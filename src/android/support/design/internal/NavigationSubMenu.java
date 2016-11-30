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

package android.support.design.internal;

import android.content.Context;
import android.support.v7.internal.view.menu.MenuBuilder;
import android.support.v7.internal.view.menu.MenuItemImpl;
import android.support.v7.internal.view.menu.SubMenuBuilder;
import android.view.MenuItem;

/**
 * This is a {@link SubMenuBuilder} that it notifies the parent {@link NavigationMenu} of its menu
 * updates.
 *
 * @hide
 */
public class NavigationSubMenu extends SubMenuBuilder {

    public NavigationSubMenu(Context context, NavigationMenu menu, MenuItemImpl item) {
        super(context, menu, item);
    }

    @Override
    public MenuItem add(CharSequence title) {
        MenuItem item = super.add(title);
        notifyParent();
        return item;
    }

    @Override
    public MenuItem add(int titleRes) {
        MenuItem item = super.add(titleRes);
        notifyParent();
        return item;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
        MenuItem item = super.add(groupId, itemId, order, title);
        notifyParent();
        return item;
    }

    @Override
    public MenuItem add(int groupId, int itemId, int order, int titleRes) {
        MenuItem item = super.add(groupId, itemId, order, titleRes);
        notifyParent();
        return item;
    }

    private void notifyParent() {
        ((MenuBuilder) getParentMenu()).onItemsChanged(true);
    }

}
