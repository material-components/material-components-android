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
import android.view.Menu;

import androidx.annotation.NonNull;
import androidx.test.core.app.ApplicationProvider;

import com.google.android.material.test.R;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Tests for {@link BottomNavigationMenuView}.
 */
@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public final class BottomNavigationMenuViewTest {
  private static class BottomNavigationViewWithSixMaxItems extends BottomNavigationView {
    public BottomNavigationViewWithSixMaxItems(@NonNull Context context) {
      super(context);

      Menu menu = getMenu();
      menu.add(Menu.NONE, 1, Menu.NONE, "first item");
      menu.add(Menu.NONE, 2, Menu.NONE, "second item");
      menu.add(Menu.NONE, 3, Menu.NONE, "third item");
      menu.add(Menu.NONE, 4, Menu.NONE, "fourth item");
      menu.add(Menu.NONE, 5, Menu.NONE, "fifth item");
      menu.add(Menu.NONE, 6, Menu.NONE, "sixth item");
    }

    @Override
    public int getMaxItemCount() {
      return 6;
    }
  }

  private final Context context = ApplicationProvider.getApplicationContext();

  @Before
  public void themeApplicationContext() {
    context.setTheme(R.style.Theme_MaterialComponents_Light);
  }

  @Test(expected = Test.None.class /* no exception expected */)
  public void testOnMeasure_withAnyNumberOfChildren_canMeasure() {
    BottomNavigationView bottomNavigation = new BottomNavigationViewWithSixMaxItems(context);
    BottomNavigationMenuView menuView = (BottomNavigationMenuView) bottomNavigation.getMenuView();
    menuView.measure(0, 0);
  }
}

