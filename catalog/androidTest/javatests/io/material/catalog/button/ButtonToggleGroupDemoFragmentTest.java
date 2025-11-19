/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.material.catalog.button;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.Fragment;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import com.google.android.material.button.MaterialButtonToggleGroup;
import io.material.catalog.feature.R;
import io.material.catalog.main.MainActivity;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ButtonToggleGroupDemoFragment} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ButtonToggleGroupDemoFragmentTest {

  @Rule
  public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new ButtonToggleGroupDemoFragment();
    activityTestRule
        .getActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.container, fragment)
        .commit();
  }

  @Test
  public void testSelectionRequiredToggle() {
    onView(withId(io.material.catalog.button.R.id.switch_toggle)).check(matches(isNotChecked()));

    onView(withId(io.material.catalog.button.R.id.icon_only_group))
        .check(matches(checkSelectionRequired(false)));
  }

  @Test
  public void testSelectionRequiredToggle_afterClicking() {
    onView(withId(io.material.catalog.button.R.id.switch_toggle)).perform(scrollTo()).perform(click());

    onView(withId(io.material.catalog.button.R.id.icon_only_group))
        .check(matches(checkSelectionRequired(true)));
  }

  private static TypeSafeMatcher<View> checkSelectionRequired(boolean required) {
    return new TypeSafeMatcher<View>() {

      @Override
      public void describeTo(Description description) {}

      @Override
      protected boolean matchesSafely(View view) {
        MaterialButtonToggleGroup toggleGroup = (MaterialButtonToggleGroup) view;
        return required == toggleGroup.isSelectionRequired();
      }
    };
  }
}
