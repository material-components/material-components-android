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

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;

import android.content.res.ColorStateList;
import android.graphics.drawable.Drawable;
import androidx.annotation.DrawableRes;
import androidx.annotation.IdRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import android.view.LayoutInflater;
import android.view.View;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import com.google.android.material.navigation.NavigationView;
import org.hamcrest.Matcher;

public class NavigationViewActions {
  /** Sets item text appearance on the content of the navigation view. */
  public static ViewAction setItemTextAppearance(final @StyleRes int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set item text appearance";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setItemTextAppearance(resId);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets item text color on the content of the navigation view. */
  public static ViewAction setItemTextColor(final ColorStateList textColor) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set item text color";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setItemTextColor(textColor);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets item background on the content of the navigation view. */
  public static ViewAction setItemBackground(final @Nullable Drawable itemBackground) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set item background";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setItemBackground(itemBackground);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets item background on the content of the navigation view. */
  public static ViewAction setItemBackgroundResource(final @DrawableRes int resId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set item background";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setItemBackgroundResource(resId);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets item icon tint list on the content of the navigation view. */
  public static ViewAction setItemIconTintList(final @Nullable ColorStateList tint) {
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
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setItemIconTintList(tint);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Add the specified view as a header to the navigation view. */
  public static ViewAction addHeaderView(
      final @NonNull LayoutInflater inflater, final @LayoutRes int res) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Add header view";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.addHeaderView(inflater.inflate(res, null, false));

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Inflates a view from the specified layout ID and adds it as a header to the navigation view.
   */
  public static ViewAction inflateHeaderView(final @LayoutRes int res) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Inflate and add header view";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.inflateHeaderView(res);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Removes a previously added header view from the navigation view. */
  public static ViewAction removeHeaderView(final @Nullable View headerView) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Remove header view";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.removeHeaderView(headerView);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets checked item on the navigation view. */
  public static ViewAction setCheckedItem(final @IdRes int id) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayed();
      }

      @Override
      public String getDescription() {
        return "Set checked item";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.setCheckedItem(id);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets icon for the menu item of the navigation view. */
  public static ViewAction setIconForMenuItem(
      final @IdRes int menuItemId, final Drawable iconDrawable) {
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
        uiController.loopMainThreadUntilIdle();

        NavigationView navigationView = (NavigationView) view;
        navigationView.getMenu().findItem(menuItemId).setIcon(iconDrawable);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Removes the specified menu item from the navigation view.
   *
   * @param menuItemId The ID of the menu item to be removed.
   */
  public static ViewAction removeMenuItem(final @IdRes int menuItemId) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(NavigationView.class);
      }

      @Override
      public String getDescription() {
        return "Remove menu item " + menuItemId;
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();
        NavigationView navigationView = (NavigationView) view;
        navigationView.getMenu().removeItem(menuItemId);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }
}
