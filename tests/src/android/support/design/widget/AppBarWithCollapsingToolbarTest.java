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

package android.support.design.widget;

import android.support.annotation.IdRes;
import android.support.design.test.R;
import android.support.test.espresso.action.CoordinatesProvider;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

@MediumTest
public class AppBarWithCollapsingToolbarTest extends AppBarLayoutBaseTest {
    private static void performVerticalUpGesture(@IdRes int containerId, final int swipeX,
            final int swipeStartY, final int swipeAmountY) {
        onView(withId(containerId)).perform(new GeneralSwipeAction(
                Swipe.SLOW,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY };
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY - swipeAmountY };
                    }
                }, Press.FINGER));
    }

    private static void performVerticalSwipeDownGesture(@IdRes int containerId, final int swipeX,
            final int swipeStartY, final int swipeAmountY) {
        onView(withId(containerId)).perform(new GeneralSwipeAction(
                Swipe.SLOW,
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY };
                    }
                },
                new CoordinatesProvider() {
                    @Override
                    public float[] calculateCoordinates(View view) {
                        return new float[] { swipeX, swipeStartY + swipeAmountY };
                    }
                }, Press.FINGER));
    }

    @Test
    public void testPinnedToolbar() {
        configureContent(R.layout.design_appbar_toolbar_collapse_pin,
                R.string.design_appbar_collapsing_toolbar_pin);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;
        final int shortSwipeAmount = toolbarHeight;

        // Perform a swipe-up gesture across the horizontal center of the screen. Start
        // below the app bar and end the gesture "inside" the app bar. This way we are testing
        // the nested scrolling that crosses the boundary between the NestedScrollView that
        // shows our texts and the AppBarLayout.
        performVerticalUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 3 * longSwipeAmount / 2,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be visually snapped below the system status bar.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + toolbarHeight, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform another swipe-up gesture
        performVerticalUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be visually snapped below the system status bar
        // as it is in the pinned mode. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + toolbarHeight, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform a short swipe-down gesture across the horizontal center of the screen.
        // Note that the swipe down is a bit longer than the swipe up to check that the app bar
        // is not starting to expand too early.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom - shortSwipeAmount,
                3 * shortSwipeAmount / 2);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be visually snapped below the system status bar
        // as it is in the pinned mode and we haven't fully swiped down the content below the
        // app bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + toolbarHeight, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform yet another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
    }

    @Test
    public void testScrollingToolbar() {
        configureContent(R.layout.design_appbar_toolbar_collapse_scroll,
                R.string.design_appbar_collapsing_toolbar_scroll);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;
        final int shortSwipeAmount = toolbarHeight;

        // Perform a swipe-up gesture across the horizontal center of the screen. Start
        // below the app bar and end the gesture "inside" the app bar. This way we are testing
        // the nested scrolling that crosses the boundary between the NestedScrollView that
        // shows our texts and the AppBarLayout.
        performVerticalUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 3 * longSwipeAmount / 2,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should not be visually "present" on the screen, with its bottom
        // edge aligned with the system status bar.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform another swipe-up gesture
        performVerticalUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be off the screen. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform a short swipe-down gesture across the horizontal center of the screen.
        // Note that the swipe down is a bit longer than the swipe up to fully bring down
        // the scrolled-away toolbar
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                3 * shortSwipeAmount / 2);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be visually snapped below the system status bar as it
        // in scrolling mode and we've swiped down, but not fully. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop + toolbarHeight, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1]);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight);

        // Perform yet another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
    }
}
