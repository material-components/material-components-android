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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;

import android.content.Context;
import android.view.Menu;
import android.view.MenuItem;
import androidx.test.core.app.ApplicationProvider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/** Tests for {@link BottomNavigationView}. */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public final class BottomNavigationViewTest {

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test
  public void testSetSelectedItemId_itemIsSelected() {
    BottomNavigationView bottomNavigation = new BottomNavigationView(context);
    Menu menu = bottomNavigation.getMenu();
    menu.add(Menu.NONE, 123, Menu.NONE, "first item");
    MenuItem selectedItem = menu.add(Menu.NONE, 456, Menu.NONE, "second item");
    menu.add(Menu.NONE, 789, Menu.NONE, "third item");

    bottomNavigation.setSelectedItemId(selectedItem.getItemId());

    assertThat(bottomNavigation.getSelectedItemId()).isEqualTo(selectedItem.getItemId());
  }

  @Test
  public void testAddItem_atSelectedIndex_selectedItemDoesNotChange() {
    int selectedItemOrder = Menu.CATEGORY_CONTAINER + 2;

    // Create a BottomNavigationView with a selected item.
    BottomNavigationView bottomNavigation = new BottomNavigationView(context);
    Menu menu = bottomNavigation.getMenu();
    MenuItem selectedItem = menu.add(Menu.NONE, 123, selectedItemOrder, "selected item");
    bottomNavigation.setSelectedItemId(selectedItem.getItemId());

    // Add another item, with a lower order than the selected item, so that its index is the
    // selected item's old index.
    menu.add(Menu.NONE, 456, selectedItemOrder - 1, "other item");

    // Assert that the selected item is still selected.
    assertThat(bottomNavigation.getSelectedItemId()).isEqualTo(selectedItem.getItemId());
  }

  @Test
  public void testRemoveBadgeEmptyMenu() {
    BottomNavigationView bottomNavigation = new BottomNavigationView(context);
    Menu menu = bottomNavigation.getMenu();
    assertThat(menu.size()).isEqualTo(0);
    bottomNavigation.removeBadge(123);
    // No exception expected.
  }
}
