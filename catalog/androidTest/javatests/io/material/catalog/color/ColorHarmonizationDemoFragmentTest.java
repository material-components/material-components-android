/*
 * Copyright (C) 2021 The Android Open Source Project
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

package io.material.catalog.color;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import io.material.catalog.main.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ColorHarmonizationFragment} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ColorHarmonizationDemoFragmentTest {

  @Rule
  public final ActivityScenarioRule<MainActivity> activityScenarioRule =
      new ActivityScenarioRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new ColorHarmonizationDemoFragment();

    activityScenarioRule
        .getScenario()
        .onActivity(
            activity ->
                activity
                    .getSupportFragmentManager()
                    .beginTransaction()
                    .replace(io.material.catalog.feature.R.id.container, fragment)
                    .commit());
  }

  @Test
  public void checkColorHexValueTextIsShown() {
    onView(withId(R.id.cat_color_enabled_switch)).perform(click());

    onView(withId(R.id.material_button_color_hex_value)).check(matches(isDisplayed()));
    onView(withId(R.id.material_unelevated_button_color_hex_value)).check(matches(isDisplayed()));
    onView(withId(R.id.material_text_input_color_hex_value)).check(matches(isDisplayed()));
  }
}
