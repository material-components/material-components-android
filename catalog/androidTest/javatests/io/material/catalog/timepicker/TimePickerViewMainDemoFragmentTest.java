/*
 * Copyright (C) 2020 The Android Open Source Project
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

package io.material.catalog.timepicker;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import io.material.catalog.main.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link TimePickerMainDemoFragment} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class TimePickerViewMainDemoFragmentTest {

  @Rule
  public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new TimePickerMainDemoFragment();
    activityTestRule
        .getActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(io.material.catalog.feature.R.id.container, fragment)
        .commit();
  }

  @Test
  public void checkClockFaceIsShown() {
    // launch picker
    onView(withId(R.id.timepicker_button)).perform(click());

    onView(withId(com.google.android.material.timepicker.R.id.material_timepicker_view))
        .check(matches(isDisplayed()));
  }
}
