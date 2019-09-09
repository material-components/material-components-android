/*
 * Copyright (C) 2017 The Android Open Source Project
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

import android.view.View;
import androidx.test.espresso.matcher.BoundedMatcher;
import com.google.android.material.appbar.AppBarLayout;
import org.hamcrest.Description;
import org.hamcrest.Matcher;

/** Espresso matches for {@link AppBarLayout}. */
public final class AppBarLayoutMatchers {

  private AppBarLayoutMatchers() {}

  /** Returns a matcher that matches AppBarLayouts which are collapsed. */
  public static Matcher<View> isCollapsed() {
    return new BoundedMatcher<View, AppBarLayout>(AppBarLayout.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("AppBarLayout is collapsed");
      }

      @Override
      protected boolean matchesSafely(AppBarLayout item) {
        return item.getBottom() == (item.getHeight() - item.getTotalScrollRange());
      }
    };
  }
}
