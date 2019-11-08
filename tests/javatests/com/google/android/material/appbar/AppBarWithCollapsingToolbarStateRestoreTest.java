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

package com.google.android.material.appbar;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.AppBarLayoutMatchers.isCollapsed;
import static com.google.android.material.testutils.SwipeUtils.swipeUp;
import static com.google.android.material.testutils.TestUtilsMatchers.hasZ;

import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.AppBarLayoutCollapsePinActivity;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.ActivityUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AppBarWithCollapsingToolbarStateRestoreTest {
  @Rule
  public final ActivityTestRule<AppBarLayoutCollapsePinActivity> activityTestRule =
      new ActivityTestRule<>(AppBarLayoutCollapsePinActivity.class);

  @Test
  public void testRecreateAndRestoreWithSwipeInHeader() throws Throwable {
    AppBarLayoutCollapsePinActivity activity = activityTestRule.getActivity();
    AppBarLayout appBar = activity.findViewById(R.id.app_bar);

    int[] appBarLocationOnScreenXY = new int[2];
    appBar.getLocationOnScreen(appBarLocationOnScreenXY);

    // Swipe up from AppBarLayout and collapse the AppBarLayout
    onView(withId(R.id.coordinator_layout))
        .perform(
            swipeUp(
                appBarLocationOnScreenXY[0] + (appBar.getWidth() / 2),
                appBarLocationOnScreenXY[1] + (appBar.getHeight() / 2),
                appBar.getHeight()));
    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));

    ActivityUtils.recreateActivity(activityTestRule, activity);
    ActivityUtils.waitForExecution(activityTestRule);

    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));
  }

  @Test
  public void testRecreateAndRestoreWithSwipeInContent() throws Throwable {
    AppBarLayoutCollapsePinActivity activity = activityTestRule.getActivity();
    AppBarLayout appBar = activity.findViewById(R.id.app_bar);

    int[] appBarLocationOnScreenXY = new int[2];
    appBar.getLocationOnScreen(appBarLocationOnScreenXY);

    // Swipe up from scrollView and collapse the AppBarLayout
    onView(withId(R.id.coordinator_layout))
        .perform(
            swipeUp(
                appBarLocationOnScreenXY[0] + (appBar.getWidth() / 2),
                appBarLocationOnScreenXY[1] + (3 * appBar.getHeight() / 2),
                appBar.getHeight()));
    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));

    ActivityUtils.recreateActivity(activityTestRule, activity);
    ActivityUtils.waitForExecution(activityTestRule);

    onView(withId(R.id.app_bar)).check(matches(hasZ())).check(matches(isCollapsed()));
  }
}
