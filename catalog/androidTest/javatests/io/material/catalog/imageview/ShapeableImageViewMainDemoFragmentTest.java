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

package io.material.catalog.imageview;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import androidx.fragment.app.Fragment;
import android.view.View;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.shape.CornerTreatment;
import com.google.android.material.shape.CutCornerTreatment;
import com.google.android.material.shape.RoundedCornerTreatment;
import io.material.catalog.main.MainActivity;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/** Tests for {@link ShapeableImageViewMainDemoFragment} */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class ShapeableImageViewMainDemoFragmentTest {

  @Rule
  public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new ShapeableImageViewMainDemoFragment();
    activityTestRule
        .getActivity()
        .getSupportFragmentManager()
        .beginTransaction()
        .replace(io.material.catalog.feature.R.id.container, fragment)
        .commit();
  }

  @Test
  public void checkImageViewIsShown() {
    onView(withId(io.material.catalog.imageview.R.id.image_view)).check(matches(isDisplayed()));
  }

  @Test
  public void checkShapeChange() {
    onView(withId(io.material.catalog.imageview.R.id.image_view))
        .check(matches(checkShape(RoundedCornerTreatment.class)));

    // Selects cut corner.
    onView(withId(io.material.catalog.imageview.R.id.button_diamond)).perform(click());

    onView(withId(io.material.catalog.imageview.R.id.image_view))
        .check(matches(checkShape(CutCornerTreatment.class)));
  }

  private static TypeSafeMatcher<View> checkShape(Class<? extends CornerTreatment> clazz) {

    return new TypeSafeMatcher<View>() {

      @Override
      public void describeTo(Description description) {}

      @Override
      protected boolean matchesSafely(View view) {
        ShapeableImageView imageView = (ShapeableImageView) view;
        return imageView.getShapeAppearanceModel().getBottomLeftCorner().getClass().equals(clazz);
      }
    };
  }
}
