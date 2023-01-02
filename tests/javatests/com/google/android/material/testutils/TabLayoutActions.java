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

package com.google.android.material.testutils;

import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;
import org.hamcrest.Matcher;

public class TabLayoutActions {
  /** Wires <code>TabLayout</code> to <code>ViewPager</code> content. */
  public static ViewAction setupWithViewPager(final @Nullable ViewPager viewPager) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "Setup with ViewPager content";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TabLayout tabLayout = (TabLayout) view;
        tabLayout.setupWithViewPager(viewPager);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Setup and show badge number for the specified tab of the <code>TabLayout</code>. */
  public static ViewAction showBadgeOnTab(final int tabIndex, final int badgeNumber) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "Setup tab badge number";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TabLayout tabLayout = (TabLayout) view;
        tabLayout.getTabAt(tabIndex).getOrCreateBadge().setNumber(badgeNumber);
      }
    };
  }

  /** Wires <code>TabLayout</code> to <code>ViewPager</code> content. */
  public static ViewAction setupWithViewPager(
      final @Nullable ViewPager viewPager, final boolean autoRefresh) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "Setup with ViewPager content";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TabLayout tabLayout = (TabLayout) view;
        tabLayout.setupWithViewPager(viewPager, autoRefresh);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Selects the specified tab in the <code>TabLayout</code>. */
  public static ViewAction selectTab(final int tabIndex) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "Selects tab";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TabLayout tabLayout = (TabLayout) view;
        tabLayout.getTabAt(tabIndex).select();

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets the specified tab mode in the <code>TabLayout</code>. */
  public static ViewAction setTabMode(final int tabMode) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "Sets tab mode";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        TabLayout tabLayout = (TabLayout) view;
        tabLayout.setTabMode(tabMode);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /**
   * Calls <code>setScrollPosition(position, positionOffset, true)</code> on the <code>TabLayout
   * </code>
   */
  public static ViewAction setScrollPosition(final int position, final float positionOffset) {
    return new ViewAction() {

      @Override
      public Matcher<View> getConstraints() {
        return ViewMatchers.isAssignableFrom(TabLayout.class);
      }

      @Override
      public String getDescription() {
        return "setScrollPosition(" + position + ", " + positionOffset + ", true)";
      }

      @Override
      public void perform(UiController uiController, View view) {
        TabLayout tabs = (TabLayout) view;
        tabs.setScrollPosition(position, positionOffset, true);
        uiController.loopMainThreadUntilIdle();
      }
    };
  }
}
