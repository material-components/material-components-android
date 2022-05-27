/*
 * Copyright 2020 The Android Open Source Project
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

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

/** Tests for {@link NavigationBarItemView}. */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = O)
public final class NavigationBarItemViewTest {

  private static final int MENU_TYPE = 0;

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test
  public void testSetTooltip_itemHasTooltip() {
    String tooltip = "menu item tooltip";
    MenuItemImpl menuItem = createMenuItemImpl("menu item title", tooltip);
    NavigationBarItemView itemView = new NavigationBarItemTestView(context);

    itemView.initialize(menuItem, MENU_TYPE);

    assertThat(itemView.getTooltipText().toString()).isEqualTo(tooltip);
  }

  @Test
  public void testMissingTooltip_itemFallsBackToTitle() {
    String title = "menu item title";
    MenuItemImpl menuItem = createMenuItemImpl(title, null);
    NavigationBarItemView itemView = new NavigationBarItemTestView(context);

    itemView.initialize(menuItem, MENU_TYPE);

    assertThat(itemView.getTooltipText().toString()).isEqualTo(title);
  }

  @Test
  public void testSetTitle_updatesTooltip() {
    MenuItemImpl menuItem = createMenuItemImpl("menu item title", null);
    NavigationBarItemView itemView = new NavigationBarItemTestView(context);
    itemView.initialize(menuItem, MENU_TYPE);

    String updatedTitle = "menu item title updated";
    itemView.setTitle(updatedTitle);

    assertThat(itemView.getTooltipText().toString()).isEqualTo(updatedTitle);
  }

  private MenuItemImpl createMenuItemImpl(CharSequence title, CharSequence tooltip) {
    MenuBuilder builder = new MenuBuilder(context);
    builder.add(title);
    MenuItemImpl menuItem = (MenuItemImpl) builder.getItem(0);
    menuItem.setTooltipText(tooltip);
    return menuItem;
  }

  private static class NavigationBarItemTestView extends NavigationBarItemView {
    public NavigationBarItemTestView(@NonNull Context context) {
      super(context);
    }

    @Override
    @LayoutRes
    protected int getItemLayoutResId() {
      return R.layout.test_navigation_bar_item_layout;
    }
  }
}
