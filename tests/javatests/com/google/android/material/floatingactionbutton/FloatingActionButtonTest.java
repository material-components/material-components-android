/*
 * Copyright (C) 2016 The Android Open Source Project
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

package com.google.android.material.floatingactionbutton;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.testutils.DesignViewActions.setVisibility;
import static com.google.android.material.testutils.FloatingActionButtonActions.hideThenShow;
import static com.google.android.material.testutils.FloatingActionButtonActions.setBackgroundTintColor;
import static com.google.android.material.testutils.FloatingActionButtonActions.setBackgroundTintList;
import static com.google.android.material.testutils.FloatingActionButtonActions.setCompatElevation;
import static com.google.android.material.testutils.FloatingActionButtonActions.setCustomSize;
import static com.google.android.material.testutils.FloatingActionButtonActions.setImageResource;
import static com.google.android.material.testutils.FloatingActionButtonActions.setLayoutGravity;
import static com.google.android.material.testutils.FloatingActionButtonActions.setSize;
import static com.google.android.material.testutils.FloatingActionButtonActions.showThenHide;
import static com.google.android.material.testutils.TestUtilsActions.setClickable;
import static com.google.android.material.testutils.TestUtilsActions.setEnabled;
import static com.google.android.material.testutils.TestUtilsActions.setExpanded;
import static com.google.android.material.testutils.TestUtilsActions.setPressed;
import static com.google.android.material.testutils.TestUtilsActions.setSelected;
import static com.google.android.material.testutils.TestUtilsActions.waitFor;
import static com.google.android.material.testutils.TestUtilsMatchers.hasTranslationZ;
import static com.google.android.material.testutils.TestUtilsMatchers.isExpanded;
import static com.google.android.material.testutils.TestUtilsMatchers.isPressed;
import static com.google.android.material.testutils.TestUtilsMatchers.withFabBackgroundFill;
import static com.google.android.material.testutils.TestUtilsMatchers.withFabContentAreaOnMargins;
import static com.google.android.material.testutils.TestUtilsMatchers.withFabContentHeight;
import static com.google.android.material.testutils.TestUtilsMatchers.withFabCustomSize;
import static org.hamcrest.Matchers.not;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Rect;
import android.view.Gravity;
import android.view.View;
import androidx.core.content.ContextCompat;
import androidx.test.espresso.action.GeneralSwipeAction;
import androidx.test.espresso.action.Press;
import androidx.test.espresso.action.Swipe;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.FloatingActionButtonActivity;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.ActivityUtils;
import com.google.android.material.testutils.TestUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class FloatingActionButtonTest {

  @Rule
  public final ActivityTestRule<FloatingActionButtonActivity> activityTestRule =
      new ActivityTestRule<>(FloatingActionButtonActivity.class);

  @Test
  @SmallTest
  public void testDefaultBackgroundTint() {
    final int colorAccent =
        TestUtils.getThemeAttrColor(activityTestRule.getActivity(), R.attr.colorAccent);
    onView(withId(R.id.fab_standard)).check(matches(withFabBackgroundFill(colorAccent)));
  }

  @Test
  @SmallTest
  public void testSetTintOnDefaultBackgroundTint() {
    onView(withId(R.id.fab_standard))
        .perform(setBackgroundTintColor(Color.GREEN))
        .check(matches(withFabBackgroundFill(Color.GREEN)));
  }

  @Test
  @SmallTest
  public void testDeclaredBackgroundTint() {
    onView(withId(R.id.fab_tint)).check(matches(withFabBackgroundFill(Color.MAGENTA)));
  }

  @Test
  @SmallTest
  public void testSetTintOnDeclaredBackgroundTint() {
    onView(withId(R.id.fab_tint))
        .perform(setBackgroundTintColor(Color.GREEN))
        .check(matches(withFabBackgroundFill(Color.GREEN)));
  }

  @Test
  @SmallTest
  public void testSetStatefulTintAcrossStateChanges() {
    final Activity activity = activityTestRule.getActivity();

    final ColorStateList tint = ContextCompat.getColorStateList(activity, R.color.fab_tint);
    final int normal = ContextCompat.getColor(activity, R.color.sand_default);
    final int notSelected = ContextCompat.getColor(activity, R.color.sand_disabled);

    // First set the background tint list to the ColorStateList
    onView(withId(R.id.fab_standard)).perform(setBackgroundTintList(tint));

    // Assert that the background is tinted correctly across state changes
    onView(withId(R.id.fab_standard))
        .perform(setSelected(true))
        .check(matches(withFabBackgroundFill(normal)))
        .perform(setSelected(false))
        .check(matches(withFabBackgroundFill(notSelected)))
        .perform(setSelected(true))
        .check(matches(withFabBackgroundFill(normal)));
  }

  @Test
  @SmallTest
  public void testDeclaredStatefulTintAcrossStateChanges() {
    final Activity activity = activityTestRule.getActivity();
    final int normal = ContextCompat.getColor(activity, R.color.sand_default);
    final int disabled = ContextCompat.getColor(activity, R.color.sand_disabled);

    // Assert that the background is tinted correctly across state changes
    onView(withId(R.id.fab_state_tint))
        .perform(setSelected(true))
        .check(matches(withFabBackgroundFill(normal)))
        .perform(setSelected(false))
        .check(matches(withFabBackgroundFill(disabled)));
  }

  @Test
  @SmallTest
  public void setVectorDrawableSrc() {
    onView(withId(R.id.fab_standard)).perform(setImageResource(R.drawable.vector_icon));
  }

  @Test
  @SmallTest
  public void testSetMiniSize() {
    final int miniSize =
        activityTestRule
            .getActivity()
            .getResources()
            .getDimensionPixelSize(R.dimen.fab_mini_height);

    onView(withId(R.id.fab_standard))
        .perform(setSize(FloatingActionButton.SIZE_MINI))
        .check(matches(withFabContentHeight(miniSize)));
  }

  @Test
  @SmallTest
  public void testSetSizeToggle() {
    final int miniSize =
        activityTestRule
            .getActivity()
            .getResources()
            .getDimensionPixelSize(R.dimen.fab_mini_height);
    final int normalSize =
        activityTestRule
            .getActivity()
            .getResources()
            .getDimensionPixelSize(R.dimen.fab_normal_height);

    onView(withId(R.id.fab_standard))
        .perform(setSize(FloatingActionButton.SIZE_MINI))
        .check(matches(withFabContentHeight(miniSize)));

    onView(withId(R.id.fab_standard))
        .perform(setSize(FloatingActionButton.SIZE_NORMAL))
        .check(matches(withFabContentHeight(normalSize)));
  }

  @Test
  @SmallTest
  public void testOffset() {
    onView(withId(R.id.fab_standard))
        .perform(setLayoutGravity(Gravity.LEFT | Gravity.TOP))
        .check(matches(withFabContentAreaOnMargins(Gravity.LEFT | Gravity.TOP)));

    onView(withId(R.id.fab_standard))
        .perform(setLayoutGravity(Gravity.RIGHT | Gravity.BOTTOM))
        .check(matches(withFabContentAreaOnMargins(Gravity.RIGHT | Gravity.BOTTOM)));
  }

  @Test
  @SmallTest
  public void testHideShow() {
    onView(withId(R.id.fab_standard))
        .perform(setVisibility(View.VISIBLE))
        .perform(hideThenShow())
        .check(matches(isDisplayed()));
  }

  @Test
  @MediumTest
  public void testShowHide() {
    onView(withId(R.id.fab_standard))
        .perform(setVisibility(View.GONE))
        .perform(showThenHide())
        .check(matches(not(isDisplayed())));
  }

  @Test
  @LargeTest
  public void testClickableTouchAndDragOffView() {
    onView(withId(R.id.fab_standard))
        .perform(setClickable(true))
        .perform(
            new GeneralSwipeAction(
                Swipe.SLOW,
                view -> {
                  // Create coordinators that in the center of the FAB's content area
                  final FloatingActionButton fab = (FloatingActionButton) view;

                  final int[] xy = new int[2];
                  fab.getLocationOnScreen(xy);
                  final Rect rect = new Rect();
                  fab.getContentRect(rect);

                  return new float[] {xy[0] + rect.centerX(), xy[1] + rect.centerY()};
                },
                view -> {
                  // Create coordinators that in the center horizontally, but well
                  // below the view vertically (by 50% of the height)
                  final int[] xy = new int[2];
                  view.getLocationOnScreen(xy);

                  return new float[] {
                    xy[0] + (view.getWidth() / 2f), xy[1] + (view.getHeight() * 1.5f)
                  };
                },
                Press.FINGER))
        .check(matches(not(isPressed())));
  }

  @Test
  @MediumTest
  public void testOnClickListener() {
      final View.OnClickListener listener = mock(View.OnClickListener.class);
      final View view = activityTestRule.getActivity().findViewById(R.id.fab_standard);
      view.setOnClickListener(listener);

      // Click on the fab
      onView(withId(R.id.fab_standard)).perform(click());

      // And verify that the listener was invoked once
      verify(listener, times(1)).onClick(view);
  }

  @Test
  @SmallTest
  public void testSetCompatElevation() {
    onView(withId(R.id.fab_standard)).perform(setEnabled(false)).perform(setCompatElevation(0));
  }

  @Test
  @MediumTest
  public void testElevationOnPressed() {
    onView(withId(R.id.fab_standard)).perform(setPressed(true));
    // It's unclear why the 21+ state list animator is not affected by loopMainThreadUntilIdle().
    onView(isRoot()).perform(waitFor(1000));
    onView(withId(R.id.fab_standard)).check(matches(hasTranslationZ()));
  }

  @Test
  @MediumTest
  public void testInstanceState() throws Throwable {
    FloatingActionButtonActivity activity = activityTestRule.getActivity();

    onView(withId(R.id.fab_standard)).perform(setExpanded(false));
    ActivityUtils.recreateActivity(activityTestRule, activity);
    ActivityUtils.waitForExecution(activityTestRule);
    onView(withId(R.id.fab_standard)).check(matches(not(isExpanded())));

    onView(withId(R.id.fab_standard)).perform(setExpanded(true));
    ActivityUtils.recreateActivity(activityTestRule, activity);
    ActivityUtils.waitForExecution(activityTestRule);
    onView(withId(R.id.fab_standard)).check(matches(isExpanded()));
  }

  @Test
  @SmallTest
  public void testSetCustomSize() {
    onView(withId(R.id.fab_standard))
        .perform(setCustomSize(10))
        .check(matches(withFabCustomSize(10)));

    onView(withId(R.id.fab_standard))
        .perform(setCustomSize(20))
        .check(matches(withFabCustomSize(20)));
  }
}
