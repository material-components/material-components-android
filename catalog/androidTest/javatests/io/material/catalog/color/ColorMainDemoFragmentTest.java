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
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.fragment.app.Fragment;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import io.material.catalog.main.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ColorMainDemoFragment} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public final class ColorMainDemoFragmentTest {

  @Rule
  public final ActivityScenarioRule<MainActivity> activityScenarioRule =
      new ActivityScenarioRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new ColorMainDemoFragment();

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
  public void checkColorRowTextValueIsShown() {
    onView(withText(R.string.cat_color_role_primary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_primary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_primary_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_primary_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_inverse_primary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_secondary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_secondary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_secondary_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_secondary_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_tertiary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_tertiary)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_tertiary_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_tertiary_container)).check(matches(isDisplayed()));

    onView(withText(R.string.cat_color_role_error)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_error)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_error_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_error_container)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_outline)).check(matches(isDisplayed()));

    onView(withText(R.string.cat_color_role_background)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_background)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_surface)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_surface)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_surface_variant)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_on_surface_variant)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_inverse_surface)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_color_role_inverse_on_surface)).check(matches(isDisplayed()));
  }
}
