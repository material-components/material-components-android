/*
 * Copyright 2017 The Android Open Source Project
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

package com.google.android.material.floatingactionbutton.transformation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.TestUtilsActions.setExpanded;
import static com.google.android.material.testutils.TestUtilsActions.waitUntilIdle;
import static org.hamcrest.Matchers.not;

import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.ExpandableTransformationActivity;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FabTransformationBehaviorTest {

  @Rule
  public final ActivityTestRule<ExpandableTransformationActivity> activityTestRule =
      new ActivityTestRule<>(ExpandableTransformationActivity.class);

  @Test
  @SmallTest
  public void testInitialSetup() {
    onView(withId(R.id.fab)).check(matches(isDisplayed()));
    onView(withId(R.id.sheet)).check(matches(not(isDisplayed())));
    onView(withId(R.id.scrim)).check(matches(not(isDisplayed())));
  }

  @Test
  @SmallTest
  public void testSetStateExpanded() {
    onView(withId(R.id.fab)).perform(setExpanded(true));
    onView(isRoot()).perform(waitUntilIdle());

    onView(withId(R.id.fab)).check(matches(not(isDisplayed())));
    onView(withId(R.id.sheet)).check(matches(isDisplayed()));
    onView(withId(R.id.scrim)).check(matches(isDisplayed()));
  }

  @Test
  @SmallTest
  public void testSetStateCollapsed() {
    onView(withId(R.id.fab)).perform(setExpanded(true));
    onView(withId(R.id.fab)).perform(setExpanded(false));
    onView(isRoot()).perform(waitUntilIdle());

    onView(withId(R.id.fab)).check(matches(isDisplayed()));
    onView(withId(R.id.sheet)).check(matches(not(isDisplayed())));
    onView(withId(R.id.scrim)).check(matches(not(isDisplayed())));
  }

  @Test
  @MediumTest
  public void testRotationInExpandedState() {
    ExpandableTransformationActivity activity = activityTestRule.getActivity();
    int oldOrientation = TestUtils.getScreenOrientation(activity);

    onView(withId(R.id.fab)).perform(setExpanded(true));
    TestUtils.switchScreenOrientation(activity);
    onView(isRoot()).perform(waitUntilIdle());

    onView(withId(R.id.fab)).check(matches(not(isDisplayed())));
    onView(withId(R.id.sheet)).check(matches(isDisplayed()));
    onView(withId(R.id.scrim)).check(matches(isDisplayed()));

    TestUtils.resetScreenOrientation(activity, oldOrientation);
  }
}
