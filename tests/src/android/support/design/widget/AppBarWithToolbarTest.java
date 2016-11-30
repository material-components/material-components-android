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

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertEquals;

import android.support.design.test.R;
import android.support.v4.view.ViewCompat;
import android.test.suitebuilder.annotation.MediumTest;
import android.view.View;

import org.junit.Test;

@MediumTest
public class AppBarWithToolbarTest extends AppBarLayoutBaseTest {

    /**
     * Tests a Toolbar with fitSystemWindows = undefined, with a fitSystemWindows = true parent
     */
    @Test
    public void testScrollToolbarWithFitSystemWindowsParent() {
        configureContent(R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
                R.string.design_appbar_toolbar_scroll_tabs_pin);

        final int[] appbarOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;

        // Perform a swipe-up gesture across the horizontal center of the screen.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 3 * longSwipeAmount / 2,
                longSwipeAmount);

        // At this point the tab bar should be visually snapped below the system status bar.
        // Allow for off-by-a-pixel margin of error.
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform yet another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        // At this point the app bar should still be in its original position.
        // Allow for off-by-a-pixel margin of error.
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
    }

    /**
     * Tests a AppBarLayout + scrolling content with fitSystemWindows = undefined,
     * with a fitSystemWindows = true parent
     */
    @Test
    public void testScrollingContentPositionWithFitSystemWindowsParent() {
        configureContent(R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
                R.string.design_appbar_toolbar_scroll_tabs_pin);

        final int[] appbarOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);

        final View scrollingContent = mCoordinatorLayout.findViewById(R.id.scrolling_content);
        final int[] scrollingContentOnScreenXY = new int[2];
        scrollingContent.getLocationOnScreen(scrollingContentOnScreenXY);

        // Assert that they have the same left
        assertEquals(appbarOnScreenXY[0], scrollingContentOnScreenXY[0]);
        // ...and the same width
        assertEquals(mAppBar.getWidth(), scrollingContent.getWidth());
        // ...and are vertically stacked
        assertEquals(mAppBar.getBottom(), scrollingContent.getTop());
    }

    /**
     * Tests a AppBarLayout + scrolling content with fitSystemWindows = undefined,
     * with a fitSystemWindows = true parent, in RTL
     */
    @Test
    public void testScrollingContentPositionWithFitSystemWindowsParentInRtl() {
        configureContent(R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
                R.string.design_appbar_toolbar_scroll_tabs_pin);

        // Force RTL
        onView(withId(R.id.app_bar)).perform(setLayoutDirection(ViewCompat.LAYOUT_DIRECTION_RTL));

        final int[] appbarOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);

        final View scrollingContent = mCoordinatorLayout.findViewById(R.id.scrolling_content);
        final int[] scrollingContentOnScreenXY = new int[2];
        scrollingContent.getLocationOnScreen(scrollingContentOnScreenXY);

        // Assert that they have the same left
        assertEquals(appbarOnScreenXY[0], scrollingContentOnScreenXY[0]);
        // ...and the same width
        assertEquals(mAppBar.getWidth(), scrollingContent.getWidth());
        // ...and are vertically stacked
        assertEquals(mAppBar.getBottom(), scrollingContent.getTop());
    }

}
