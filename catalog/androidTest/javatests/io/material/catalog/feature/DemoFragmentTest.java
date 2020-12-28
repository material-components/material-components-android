/*
 * Copyright (C) 2019 The Android Open Source Project
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

package io.material.catalog.feature;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.swipeDown;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.startsWith;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import io.material.catalog.main.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DemoFragment}
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class DemoFragmentTest {

  /** Empty demo fragment to test basic DemoFragment functionality */
  public static class SubjectForTest extends DemoFragment {

    @Override
    public View onCreateDemoView(LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup,
        @Nullable Bundle bundle) {
      return new LinearLayout(getContext());
    }
  }

  @Rule
  public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new SubjectForTest();
    activityTestRule.getActivity()
        .getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, fragment).commit();
  }

  @Test
  public void showMemoryWidget_whenSwipeDownOnToolbar() {
    onView(withId(R.id.toolbar)).perform(swipeDown());

    onView(withId(R.id.memorymonitor_widget))
        .check(matches(isDisplayed()))
        .check(matches(withText(startsWith("used:"))));
  }
}
