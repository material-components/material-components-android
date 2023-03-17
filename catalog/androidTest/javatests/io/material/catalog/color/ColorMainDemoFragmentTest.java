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

  private static final int[] STRING_RESOURCE_IDS =
      new int[] {
        R.string.cat_color_role_primary,
        R.string.cat_color_role_on_primary,
        R.string.cat_color_role_primary_container,
        R.string.cat_color_role_on_primary_container,
        R.string.cat_color_role_primary_fixed,
        R.string.cat_color_role_primary_fixed_dim,
        R.string.cat_color_role_on_primary_fixed,
        R.string.cat_color_role_on_primary_fixed_variant,
        R.string.cat_color_role_inverse_primary,
        R.string.cat_color_role_secondary,
        R.string.cat_color_role_on_secondary,
        R.string.cat_color_role_secondary_container,
        R.string.cat_color_role_on_secondary_container,
        R.string.cat_color_role_secondary_fixed,
        R.string.cat_color_role_secondary_fixed_dim,
        R.string.cat_color_role_on_secondary_fixed,
        R.string.cat_color_role_on_secondary_fixed_variant,
        R.string.cat_color_role_tertiary,
        R.string.cat_color_role_on_tertiary,
        R.string.cat_color_role_tertiary_container,
        R.string.cat_color_role_on_tertiary_container,
        R.string.cat_color_role_tertiary_fixed,
        R.string.cat_color_role_tertiary_fixed_dim,
        R.string.cat_color_role_on_tertiary_fixed,
        R.string.cat_color_role_on_tertiary_fixed_variant,
        R.string.cat_color_role_error,
        R.string.cat_color_role_on_error,
        R.string.cat_color_role_error_container,
        R.string.cat_color_role_on_error_container,
        R.string.cat_color_role_outline,
        R.string.cat_color_role_outline_variant,
        R.string.cat_color_role_background,
        R.string.cat_color_role_on_background,
        R.string.cat_color_role_surface,
        R.string.cat_color_role_on_surface,
        R.string.cat_color_role_surface_variant,
        R.string.cat_color_role_on_surface_variant,
        R.string.cat_color_role_inverse_surface,
        R.string.cat_color_role_inverse_on_surface,
        R.string.cat_color_role_surface_bright,
        R.string.cat_color_role_surface_dim,
        R.string.cat_color_role_surface_container,
        R.string.cat_color_role_surface_container_low,
        R.string.cat_color_role_surface_container_high,
        R.string.cat_color_role_surface_container_lowest,
        R.string.cat_color_role_surface_container_highest,
      };

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
    for (int stringResId : STRING_RESOURCE_IDS) {
      checkIsTextDisplayed(stringResId);
    }
  }

  private void checkIsTextDisplayed(int text) {
    onView(withText(text)).perform(scrollTo()).check(matches(isDisplayed()));
  }
}
