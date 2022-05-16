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
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ColorHarmonizationDemoActivity} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ColorHarmonizationDemoActivityTest {

  @Rule
  public final ActivityScenarioRule<ColorHarmonizationDemoActivity> activityScenarioRule =
      new ActivityScenarioRule<>(ColorHarmonizationDemoActivity.class);

  @Test
  public void checkButtonsAreShown() {
    onView(withId(R.id.red_button_dark)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.red_button_light)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.yellow_button_dark)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.yellow_button_light)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.green_button_dark)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.green_button_light)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.blue_button_dark)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.blue_button_light)).perform(scrollTo()).check(matches(isDisplayed()));
  }

  @Test
  public void checkColorPalettesAreShown() {
    onView(withId(R.id.cat_colors_error)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_harmonized_error))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_yellow)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_harmonized_yellow))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_green)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_harmonized_green))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_blue)).perform(scrollTo()).check(matches(isDisplayed()));
    onView(withId(R.id.cat_colors_harmonized_blue))
        .perform(scrollTo())
        .check(matches(isDisplayed()));
  }
}
