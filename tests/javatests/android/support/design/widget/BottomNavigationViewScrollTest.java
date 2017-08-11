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

package android.support.design.widget;

import static android.support.design.testutils.SwipeUtils.swipeDown;
import static android.support.design.testutils.SwipeUtils.swipeUp;
import static android.support.design.testutils.TestUtilsActions.setText;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.support.annotation.CallSuper;
import android.support.annotation.IdRes;
import android.support.annotation.LayoutRes;
import android.support.design.testapp.R;
import android.support.design.testutils.Shakespeare;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import android.text.TextUtils;
import android.widget.TextView;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class BottomNavigationViewScrollTest extends BaseDynamicCoordinatorLayoutTest {

  private BottomNavigationView bottomNavigationView;

  protected static void performVerticalSwipeUpGesture(
      @IdRes int containerId, final int swipeX, final int swipeStartY, final int swipeAmountY) {
    onView(withId(containerId)).perform(swipeUp(swipeX, swipeStartY, swipeAmountY));
  }

  protected static void performVerticalSwipeDownGesture(
      @IdRes int containerId, final int swipeX, final int swipeStartY, final int swipeAmountY) {
    onView(withId(containerId)).perform(swipeDown(swipeX, swipeStartY, swipeAmountY));
  }

  @CallSuper
  protected void configureContent(@LayoutRes final int layoutResId) throws Throwable {
    onView(withId(R.id.coordinator_stub)).perform(inflateViewStub(layoutResId));

    bottomNavigationView = mCoordinatorLayout.findViewById(R.id.bottom_navigation);

    TextView dialogue = mCoordinatorLayout.findViewById(R.id.textview_dialogue);
    if (dialogue != null) {
      onView(withId(R.id.textview_dialogue))
          .perform(setText(TextUtils.concat(Shakespeare.DIALOGUE)));
    }
  }

  @Test
  public void testScrollingBottomNavigationView() throws Throwable {
    configureContent(R.layout.design_bottom_navigation_view);

    final int[] bottomNavOnScreenXY = new int[2];
    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);

    final int originalBottomNavTop = bottomNavOnScreenXY[1];
    final int centerX = bottomNavOnScreenXY[0] + bottomNavigationView.getWidth() / 2;

    final int bottomNavHeight = bottomNavigationView.getHeight();
    final int longSwipeAmount = 3 * bottomNavHeight / 2;

    // Perform a swipe-up gesture across the horizontal center of the screen.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout, centerX, originalBottomNavTop - longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // At this point the bottom nav should have disappeared off the bottom of the screen.
    assertEquals(originalBottomNavTop + bottomNavHeight, bottomNavOnScreenXY[1], 1);

    // Perform another swipe-up gesture
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout, centerX, originalBottomNavTop - longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // Bottom nav should still be disappeared off the bottom of the screen.
    assertEquals(originalBottomNavTop + bottomNavHeight, bottomNavOnScreenXY[1], 1);

    // Perform a swipe-down gesture across the horizontal center of the screen.
    performVerticalSwipeDownGesture(
        R.id.coordinator_layout, centerX, longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // Bottom nav should reappear from the bottom of the screen
    assertEquals(originalBottomNavTop, bottomNavOnScreenXY[1], 1);
  }

  @Test
  public void testNonHidingBottomNavigationView() throws Throwable {
    configureContent(R.layout.design_bottom_navigation_nonhiding_view);

    final int[] bottomNavOnScreenXY = new int[2];
    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);

    final int originalBottomNavTop = bottomNavOnScreenXY[1];
    final int centerX = bottomNavOnScreenXY[0] + bottomNavigationView.getWidth() / 2;

    final int bottomNavHeight = bottomNavigationView.getHeight();
    final int longSwipeAmount = 3 * bottomNavHeight / 2;

    // Perform a swipe-up gesture across the horizontal center of the screen.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout, centerX, originalBottomNavTop - longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // Bottom nav should not have moved.
    assertEquals(originalBottomNavTop, bottomNavOnScreenXY[1], 1);

    // Perform another swipe-up gesture
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout, centerX, originalBottomNavTop - longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // Bottom nav should not have moved.
    assertEquals(originalBottomNavTop, bottomNavOnScreenXY[1], 1);

    // Perform a swipe-down gesture across the horizontal center of the screen.
    performVerticalSwipeDownGesture(
        R.id.coordinator_layout, centerX, longSwipeAmount, longSwipeAmount);

    bottomNavigationView.getLocationOnScreen(bottomNavOnScreenXY);
    // Bottom nav should not have moved.
    assertEquals(originalBottomNavTop, bottomNavOnScreenXY[1], 1);
  }
}
