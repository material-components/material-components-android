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

package io.material.catalog.card;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.os.SystemClock;
import androidx.fragment.app.Fragment;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import io.material.catalog.R;
import io.material.catalog.main.MainActivity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link CardSwipeDismissFragment}
 */
@MediumTest
@RunWith(AndroidJUnit4.class)
public class CardSwipeDismissFragmentTest {

  @Rule
  public final ActivityTestRule<MainActivity> activityTestRule =
      new ActivityTestRule<>(MainActivity.class);

  @Before
  public void setUpAndLaunchFragment() {
    Fragment fragment = new CardSwipeDismissFragment();
    activityTestRule.getActivity()
        .getSupportFragmentManager().beginTransaction()
        .replace(R.id.container, fragment).commit();
  }

  @Test
  public void testFragmentSwipeActions() {
    onView(withId(R.id.card_content_layout)).check(matches(ViewMatchers.withAlpha(1)));

    // The dismiss action will set the alpha to 0
    // Swipe away view and check it's not longer displayed
    onView(withId(R.id.card_content_layout)).perform(swipeRight())
        .check(matches(ViewMatchers.withAlpha(0)));
  }


  @Test
  public void testSnackbarBehavior_afterSwipingCard() {
    // Test snackbar is displayed after swipe and undo action makes the card be visible.
    onView(withId(R.id.card_content_layout)).perform(swipeRight());
    // Sleep until espresso can recognize the Snackbar
    SystemClock.sleep(300);
    
    onView(withText(R.string.cat_card_dismissed)).check(matches(isDisplayed()));
    onView(withText(R.string.cat_card_undo)).perform(click());

    onView(withId(R.id.card_content_layout)).check(matches(ViewMatchers.withAlpha(1)));
  }
}
