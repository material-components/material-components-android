/*
 * Copyright 2018 The Android Open Source Project
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
package com.google.android.material.bottomnavigation;

import android.content.Context;

import androidx.appcompat.view.menu.MenuItemImpl;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.material.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Mockito.when;

/** Tests for {@link BottomNavigationItemView}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
@Config(sdk = O)
public final class BottomNavigationItemViewTest {

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
    BottomNavigationItemView itemView = new BottomNavigationItemView(context);

    itemView.initialize(menuItem, MENU_TYPE);

    assertThat(itemView.getTooltipText()).isEqualTo(tooltip);
  }

  @Test
  public void testMissingTooltip_itemFallsBackToTitle() {
    String title = "menu item title";
    MenuItemImpl menuItem = createMenuItemImpl(title, null);
    BottomNavigationItemView itemView = new BottomNavigationItemView(context);

    itemView.initialize(menuItem, MENU_TYPE);

    assertThat(itemView.getTooltipText()).isEqualTo(title);
  }

  @Test
  public void testSetTitle_updatesTooltip() {
    MenuItemImpl menuItem = createMenuItemImpl("menu item title", null);
    BottomNavigationItemView itemView = new BottomNavigationItemView(context);
    itemView.initialize(menuItem, MENU_TYPE);

    CharSequence updatedTitle = "menu item title updated";
    itemView.setTitle(updatedTitle);

    assertThat(itemView.getTooltipText()).isEqualTo(updatedTitle);
  }

  private MenuItemImpl createMenuItemImpl(CharSequence title, CharSequence tooltip) {
    MenuItemImpl menuItem = Mockito.mock(MenuItemImpl.class);
    when(menuItem.getTooltipText()).thenReturn(tooltip);
    when(menuItem.getTitle()).thenReturn(title);
    return menuItem;
  }
}
