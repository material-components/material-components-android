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

import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayingAtLeast;

import android.view.View;
import androidx.annotation.Nullable;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import org.hamcrest.Matcher;

public class ViewPagerActions {
  /** Moves <code>ViewPager</code> to the right by one page. */
  public static ViewAction scrollRight() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "ViewPager scroll one page to the right";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        int current = viewPager.getCurrentItem();
        viewPager.setCurrentItem(current + 1, false);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Moves <code>ViewPager</code> to the left by one page. */
  public static ViewAction scrollLeft() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "ViewPager scroll one page to the left";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        int current = viewPager.getCurrentItem();
        viewPager.setCurrentItem(current - 1, false);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Moves <code>ViewPager</code> to the last page. */
  public static ViewAction scrollToLast() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "ViewPager scroll to last page";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        int size = viewPager.getAdapter().getCount();
        if (size > 0) {
          viewPager.setCurrentItem(size - 1, false);
        }

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Moves <code>ViewPager</code> to the first page. */
  public static ViewAction scrollToFirst() {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "ViewPager scroll to first page";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        int size = viewPager.getAdapter().getCount();
        if (size > 0) {
          viewPager.setCurrentItem(0, false);
        }

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Moves <code>ViewPager</code> to specific page. */
  public static ViewAction scrollToPage(final int page) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isDisplayingAtLeast(90);
      }

      @Override
      public String getDescription() {
        return "ViewPager move to a specific page";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        viewPager.setCurrentItem(page, false);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  /** Sets the specified adapter on <code>ViewPager</code>. */
  public static ViewAction setAdapter(final @Nullable PagerAdapter adapter) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(ViewPager.class);
      }

      @Override
      public String getDescription() {
        return "ViewPager set adapter";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        ViewPager viewPager = (ViewPager) view;
        viewPager.setAdapter(adapter);

        uiController.loopMainThreadUntilIdle();
      }
    };
  }
}
