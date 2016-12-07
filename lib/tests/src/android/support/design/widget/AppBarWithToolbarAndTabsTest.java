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

import android.os.SystemClock;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.support.design.test.R;
import android.support.design.testutils.Cheeses;
import android.test.suitebuilder.annotation.LargeTest;
import android.test.suitebuilder.annotation.MediumTest;
import org.junit.Test;

import static android.support.design.testutils.TestUtilsActions.addTabs;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

@MediumTest
public class AppBarWithToolbarAndTabsTest extends AppBarLayoutBaseTest {
    private TabLayout mTabLayout;

    @Override
    protected void configureContent(@LayoutRes int layoutResId, @StringRes int titleResId) {
        super.configureContent(layoutResId, titleResId);

        mTabLayout = (TabLayout) mAppBar.findViewById(R.id.tabs);
        String[] tabTitles = new String[5];
        System.arraycopy(Cheeses.sCheeseStrings, 0, tabTitles, 0, 5);
        onView(withId(R.id.tabs)).perform(addTabs(tabTitles));
    }

    @Test
    public void testScrollingToolbarAndScrollingTabs() {
        configureContent(R.layout.design_appbar_toolbar_scroll_tabs_scroll,
                R.string.design_appbar_toolbar_scroll_tabs_scroll);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int tabsHeight = mTabLayout.getHeight();
        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;
        final int shortSwipeAmount = toolbarHeight;

        // Perform a swipe-up gesture across the horizontal center of the screen, starting from
        // just below the AppBarLayout
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 20,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should not be visually "present" on the screen, with its bottom
        // edge aligned with the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform another swipe-up gesture
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be off the screen. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a long swipe-down gesture across the horizontal center of the screen.
        // Note that the swipe down is a bit longer than the swipe up to fully bring down
        // the scrolled-away toolbar and tab layout
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount * 3 / 2);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be visually snapped below the system status bar as it
        // in scrolling mode and we've swiped down. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

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
        assertAppBarElevation(mDefaultElevationValue);

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
        assertAppBarElevation(mDefaultElevationValue);
    }

    @Test
    public void testScrollingToolbarAndPinnedTabs() {
        configureContent(R.layout.design_appbar_toolbar_scroll_tabs_pinned,
                R.string.design_appbar_toolbar_scroll_tabs_pin);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int tabsHeight = mTabLayout.getHeight();
        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;
        final int shortSwipeAmount = toolbarHeight;

        // Perform a swipe-up gesture across the horizontal center of the screen, starting from
        // just below the AppBarLayout
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 20,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the tab bar should be visually snapped below the system status bar.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform another swipe-up gesture
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the tab bar should still be visually snapped below the system status bar
        // as it is in the pinned mode. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a short swipe-down gesture across the horizontal center of the screen.
        // Note that the swipe down is a bit longer than the swipe up to fully bring down
        // the scrolled-away toolbar and tab layout
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom - shortSwipeAmount,
                3 * shortSwipeAmount / 2);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position as it
        // in scrolling mode and we've swiped down. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

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
        assertAppBarElevation(mDefaultElevationValue);

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
        assertAppBarElevation(mDefaultElevationValue);
    }

    @LargeTest
    @Test
    public void testSnappingToolbarAndSnappingTabs() {
        configureContent(R.layout.design_appbar_toolbar_scroll_tabs_scroll_snap,
                R.string.design_appbar_toolbar_scroll_tabs_scroll_snap);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int tabsHeight = mTabLayout.getHeight();
        final int appbarHeight = mAppBar.getHeight();

        // Since AppBarLayout doesn't expose a way to track snap animations, the three possible
        // options are
        // a) track how vertical offsets and invalidation is propagated through the
        // view hierarchy and wait until there are no more events of that kind
        // b) run a dummy Espresso action that waits until the main thread is idle
        // c) sleep for a hardcoded period of time to "wait" until the snap animation is done
        // In this test method we go with option b)

        // Perform a swipe-up gesture across the horizontal center of the screen. The amount
        // of swipe is 25% of the toolbar height and we expect the snap behavior to "move"
        // the app bar back to its original position.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                toolbarHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position as it
        // in snapping mode and we haven't swiped "enough". Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a slightly longer swipe-up gesture, this time by 75% of the toolbar height.
        // We expect the snap behavior to move the app bar to snap the tab layout below the
        // system status bar.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                3 * toolbarHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should "snap" the toolbar away and align the tab layout below
        // the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a short swipe-up gesture, this time by 25% of the tab layout height. We expect
        // snap behavior to move the app bar back to snap the tab layout below the system status
        // bar.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                tabsHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should "snap" back to align the tab layout below
        // the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a longer swipe-up gesture, this time by 75% of the tab layout height. We expect
        // snap behavior to move the app bar fully away from the screen.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                3 * tabsHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should not be visually "present" on the screen, with its bottom
        // edge aligned with the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a short swipe-down gesture by 25% of the tab layout height. We expect
        // snap behavior to move the app bar back fully away from the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                tabsHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still not be visually "present" on the screen, with
        // its bottom edge aligned with the system status bar. Allow for off-by-a-pixel margin
        // of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a longer swipe-up gesture, this time by 75% of the tab layout height. We expect
        // snap behavior to move the app bar to snap the tab layout below the system status
        // bar.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                3 * tabsHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should "snap" the toolbar away and align the tab layout below
        // the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a short swipe-down gesture by 25% of the toolbar height. We expect
        // snap behavior to align the tab layout below the system status bar
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                toolbarHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still align the tab layout below
        // the system status bar. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + tabsHeight, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform a longer swipe-up gesture, this time by 75% of the toolbar height. We expect
        // snap behavior to move the app bar back to its original place (fully visible).
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + toolbarHeight,
                3 * tabsHeight / 4);

        // Wait for the snap animation to be done
        waitForSnapAnimationToFinish();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);
    }

    private void waitForSnapAnimationToFinish() {
        final AppBarLayout.Behavior behavior = (AppBarLayout.Behavior)
                ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
        while (behavior.isOffsetAnimatorRunning()) {
            SystemClock.sleep(16);
        }
    }
}
