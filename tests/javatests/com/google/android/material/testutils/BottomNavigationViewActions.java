/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.testutils;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import android.view.View;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;
import org.hamcrest.Matcher;

public class BottomNavigationViewActions {
  /** Sets item icon tint list on the content of the bottom navigation view. */
  public static ViewAction setItemIconTintList(@Nullable final ColorStateList tint) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set item icon tint list";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.setItemIconTintList(tint);
      }
    };
  }

  /** Sets icon for the menu item of the navigation view. */
  public static ViewAction setIconForMenuItem(
      @IdRes final int menuItemId, final Drawable iconDrawable) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set menu item icon";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.getMenu().findItem(menuItemId).setIcon(iconDrawable);
      }
    };
  }

  /** Sets and show badge number for the menu item of the navigation view. */
  public static ViewAction showBadgeNumberForMenuItem(
      @IdRes final int menuItemId, final int badgeNumber) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set menu item badge number";
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.getOrCreateBadge(menuItemId).setNumber(badgeNumber);
      }
    };
  }

  /** Add a navigation item to the bottom navigation view. */
  public static ViewAction addMenuItem(final String title) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Add item with title" + title;
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.getMenu().add(title);
      }
    };
  }

  /** Set the bottom navigation view's label visibility mode. */
  public static ViewAction setLabelVisibilityMode(@LabelVisibilityMode final int mode) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set the bottom navigation's label visibility mode to " + mode;
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.setLabelVisibilityMode(mode);
      }
    };
  }

  /** Set the bottom navigation view's icon size. */
  public static ViewAction setIconSize(final int size) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set the bottom navigation's icon size to " + size;
      }

      @Override
      public void perform(UiController uiController, View view) {
        BottomNavigationView navigationView = (BottomNavigationView) view;
        navigationView.setItemIconSize(size);
      }
    };
  }
}
