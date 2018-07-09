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

package com.google.android.material.appbar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertEquals;

import android.graphics.Rect;
import com.google.android.material.testapp.R;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.MediumTest;
import android.support.test.runner.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class AppBarWithToolbarTest extends AppBarLayoutBaseTest {

  /** Tests a Toolbar with fitSystemWindows = undefined, with a fitSystemWindows = true parent */
  @Test
  public void testScrollToolbarWithFitSystemWindowsParent() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
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
        R.id.coordinator_layout, centerX, originalAppbarBottom, longSwipeAmount);

    // At this point the app bar should still be in its original position.
    // Allow for off-by-a-pixel margin of error.
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
    assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
  }

  /**
   * Tests a AppBarLayout + scrolling content with fitSystemWindows = undefined, with a
   * fitSystemWindows = true parent
   */
  @Test
  public void testScrollingContentPositionWithFitSystemWindowsParent() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
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
   * Tests a AppBarLayout + scrolling content with fitSystemWindows = undefined, with a
   * fitSystemWindows = true parent, in RTL
   */
  @Test
  public void testScrollingContentPositionWithFitSystemWindowsParentInRtl() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
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

  @Test
  public void testRequestRectangleWithChildThatDoesNotRequireScroll() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final View scrollingContent = mCoordinatorLayout.findViewById(R.id.scrolling_content);

    // Get the initial XY
    final int[] originalScrollingXY = new int[2];
    scrollingContent.getLocationInWindow(originalScrollingXY);

    // Now request that the first child has its full rectangle displayed
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            final ViewGroup scrollingContentInner =
                scrollingContent.findViewById(R.id.scrolling_content_inner);
            View child = scrollingContentInner.getChildAt(0);
            Rect rect = new Rect(0, 0, child.getWidth(), child.getHeight());
            child.requestRectangleOnScreen(rect, true);
          }
        });
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    final int[] newScrollingXY = new int[2];
    scrollingContent.getLocationInWindow(newScrollingXY);

    // Assert that the scrolling view has not moved
    assertEquals(originalScrollingXY[0], newScrollingXY[0]);
    assertEquals(originalScrollingXY[1], newScrollingXY[1]);
  }

  @Test
  public void testRequestRectangleWithChildThatDoesRequireScroll() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows_parent,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final View scrollingContent = mCoordinatorLayout.findViewById(R.id.scrolling_content);

    // Get the initial XY
    final int[] originalScrollingXY = new int[2];
    scrollingContent.getLocationInWindow(originalScrollingXY);

    // Now request that the first child has its full rectangle displayed
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            final ViewGroup scrollingContentInner =
                scrollingContent.findViewById(R.id.scrolling_content_inner);
            View child =
                scrollingContentInner.getChildAt(scrollingContentInner.getChildCount() - 1);
            Rect rect = new Rect(0, 0, child.getWidth(), child.getHeight());
            child.requestRectangleOnScreen(rect, true);
          }
        });
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    final int[] newScrollingXY = new int[2];
    scrollingContent.getLocationInWindow(newScrollingXY);

    // Assert that the appbar has collapsed vertically
    assertEquals(originalScrollingXY[0], newScrollingXY[0]);
    assertEquals(originalScrollingXY[1] - mAppBar.getHeight(), newScrollingXY[1]);
  }
}
