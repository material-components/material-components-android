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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static com.google.android.material.appbar.AppBarLayout.LayoutParams.SCROLL_FLAG_NO_SCROLL;
import static org.junit.Assert.assertEquals;

import android.graphics.Rect;
import android.view.View;
import android.view.ViewGroup;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.view.ViewCompat;
import androidx.test.InstrumentationRegistry;
import androidx.test.filters.MediumTest;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.testapp.R;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class AppBarWithToolbarTest extends AppBarLayoutBaseTest {

  /**
   * Tests a AppBarLayout with fitSystemWindows = undefined, with a fitSystemWindows = true parent
   */
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

  /** Tests a AppBarLayout with fitSystemWindows = true, with a fitSystemWindows = true parent */
  @Test
  public void testScrollAppBarLayoutWithFitSystemWindows() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final int[] appbarOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);

    final int[] toolbarOnScreenXY = new int[2];
    mToolbar.getLocationOnScreen(toolbarOnScreenXY);

    final int[] coordinatorLayoutOnScreenXY = new int[2];
    mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

    final int originalAppbarTop = appbarOnScreenXY[1];
    final int originalAppbarBottom = originalAppbarTop + mAppBar.getHeight();
    final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

    final int longSwipeAmount = 3 * mAppBar.getHeight() / 2;

    final int originalToolbarTop = toolbarOnScreenXY[1];
    final int originalToolbarBottom = originalToolbarTop + mToolbar.getHeight();

    // Ensure that the Toolbar is positioned at the bottom of the AppBarLayout, and below
    // the status bar
    assertEquals(coordinatorLayoutOnScreenXY[1], originalAppbarTop);
    assertEquals(originalAppbarTop + mAppBar.getTopInset(), originalToolbarTop);
    assertEquals(originalAppbarBottom, originalToolbarBottom);

    // Perform a swipe-up gesture across the horizontal center of the screen.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout,
        centerX,
        originalAppbarBottom + 3 * longSwipeAmount / 2,
        longSwipeAmount);

    // At this point the app bar should be visually snapped below the system status bar.
    // Allow for off-by-a-pixel margin of error.
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    assertEquals(originalToolbarTop, appbarOnScreenXY[1] + mAppBar.getHeight(), 1);

    // Perform yet another swipe-down gesture across the horizontal center of the screen.
    performVerticalSwipeDownGesture(
        R.id.coordinator_layout, centerX, originalAppbarBottom, longSwipeAmount);

    // At this point the app bar should still be in its original position.
    // Allow for off-by-a-pixel margin of error.
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
    assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + mAppBar.getHeight(), 1);
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
        () -> {
          final ViewGroup scrollingContentInner =
              scrollingContent.findViewById(R.id.scrolling_content_inner);
          View child = scrollingContentInner.getChildAt(0);
          Rect rect = new Rect(0, 0, child.getWidth(), child.getHeight());
          child.requestRectangleOnScreen(rect, true);
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
        () -> {
          final ViewGroup scrollingContentInner =
              scrollingContent.findViewById(R.id.scrolling_content_inner);
          View child = scrollingContentInner.getChildAt(scrollingContentInner.getChildCount() - 1);
          Rect rect = new Rect(0, 0, child.getWidth(), child.getHeight());
          child.requestRectangleOnScreen(rect, true);
        });
    InstrumentationRegistry.getInstrumentation().waitForIdleSync();

    final int[] newScrollingXY = new int[2];
    scrollingContent.getLocationInWindow(newScrollingXY);

    // Assert that the appbar has collapsed vertically
    assertEquals(originalScrollingXY[0], newScrollingXY[0]);
    assertEquals(originalScrollingXY[1] - mAppBar.getHeight(), newScrollingXY[1]);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateAccessibilityActionsWithEnterAlwaysFlag() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final int[] appbarOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);

    final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
    final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

    final int appbarHeight = mAppBar.getHeight();
    final int longSwipeAmount = 3 * appbarHeight / 2;

    // Call onLayout so the accessibility actions are initially updated.
    activityTestRule.runOnUiThread(
        () -> {
          final CoordinatorLayout.Behavior<AppBarLayout> behavior =
              ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
          behavior.onLayoutChild(mCoordinatorLayout, mAppBar, mAppBar.getLayoutDirection());
        });

    // Very top of screen, can scroll forward to collapse but can't scroll backward.
    assertAccessibilityHasScrollForwardAction(true);
    assertAccessibilityHasScrollBackwardAction(false);
    assertAccessibilityScrollable(true);

    // Perform a swipe-up gesture across the horizontal center of the screen.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout,
        centerX,
        originalAppbarBottom + 3 * longSwipeAmount / 2,
        longSwipeAmount);

    // AppBar has been collapsed fully so we can't scroll forward. Can scroll backward since
    // the bar will always be entered/expanded on scroll.
    assertAccessibilityHasScrollForwardAction(false);
    assertAccessibilityHasScrollBackwardAction(true);
    assertAccessibilityScrollable(true);
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testUpdateAccessibilityActionWithViewsRemoved() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    // Call onLayout so the accessibility actions are initially updated.
    activityTestRule.runOnUiThread(
        () -> {
          final CoordinatorLayout.Behavior<AppBarLayout> behavior =
              ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
          behavior.onLayoutChild(mCoordinatorLayout, mAppBar, mAppBar.getLayoutDirection());
        });

    assertAccessibilityHasScrollForwardAction(true);
    assertAccessibilityHasScrollBackwardAction(false);
    assertAccessibilityScrollable(true);

    activityTestRule.runOnUiThread(
        () -> {
          mCoordinatorLayout.removeAllViews();
        });

    assertAccessibilityHasScrollForwardAction(false);
    assertAccessibilityHasScrollBackwardAction(false);
    assertAccessibilityScrollable(false);
  }

  @Test
  public void testUpdateAccessibilityActionsWithSetScrollFlags() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_scroll_fitsystemwindows,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final int[] appbarOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);
    final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
    final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;
    final int appbarHeight = mAppBar.getHeight();
    final int longSwipeAmount = 3 * appbarHeight / 2;

    AppBarLayout.LayoutParams lp = (AppBarLayout.LayoutParams) mToolbar.getLayoutParams();

    // Disable scrolling and call onLayout to update the a11y actions.
    lp.setScrollFlags(SCROLL_FLAG_NO_SCROLL);
    activityTestRule.runOnUiThread(
        () -> {
          mToolbar.setLayoutParams(lp);
          final CoordinatorLayout.Behavior<AppBarLayout> behavior =
              ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
          behavior.onLayoutChild(mCoordinatorLayout, mAppBar, mAppBar.getLayoutDirection());
        });

    assertAccessibilityHasScrollForwardAction(false);
    assertAccessibilityHasScrollBackwardAction(false);

    // Add the SCROLL_FLAG_SCROLL flag and update the a11y actions.
    lp.setScrollFlags(AppBarLayout.LayoutParams.SCROLL_FLAG_SCROLL);
    activityTestRule.runOnUiThread(
        () -> {
          mToolbar.setLayoutParams(lp);
          final CoordinatorLayout.Behavior<AppBarLayout> behavior =
              ((CoordinatorLayout.LayoutParams) mAppBar.getLayoutParams()).getBehavior();
          behavior.onLayoutChild(mCoordinatorLayout, mAppBar, mAppBar.getLayoutDirection());
        });
    // Can scroll forward to collapse, and cannot expand because it's already expanded.
    assertAccessibilityHasScrollForwardAction(true);
    assertAccessibilityHasScrollBackwardAction(false);
    assertAccessibilityScrollable(true);

    // Perform a swipe-up gesture across the horizontal center of the screen. The toolbar should be
    // collapsed.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout,
        centerX,
        originalAppbarBottom + 3 * longSwipeAmount / 2,
        longSwipeAmount);

    // Content is already collapsed, so it can't scroll forward. The pre-scroll range will be 0
    // for SCROLL_FLAG_SCROLL, so it can't scroll backward.
    assertAccessibilityHasScrollForwardAction(false);
    assertAccessibilityHasScrollBackwardAction(false);
    assertAccessibilityScrollable(false);
  }

  /** Tests the lift on scroll color of the app bar layout. */
  @Test
  public void testLiftOnScrollColor() throws Throwable {
    configureContent(
        R.layout.design_appbar_toolbar_liftonscroll_color,
        R.string.design_appbar_toolbar_scroll_tabs_pin);

    final int[] appbarOnScreenXY = new int[2];
    mAppBar.getLocationOnScreen(appbarOnScreenXY);

    final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
    final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

    final int appbarHeight = mAppBar.getHeight();
    final int longSwipeAmount = 3 * appbarHeight / 2;

    MaterialShapeDrawable backgroundDrawable = mAppBar.getMaterialShapeBackground();

    assertEquals(
        mAppBar.getResources().getColor(R.color.material_blue_grey_900),
        backgroundDrawable.getResolvedTintColor());

    // Perform a swipe-up gesture across the horizontal center of the screen.
    performVerticalSwipeUpGesture(
        R.id.coordinator_layout,
        centerX,
        originalAppbarBottom + 3 * longSwipeAmount / 2,
        longSwipeAmount);

    assertEquals(
        mAppBar.getResources().getColor(R.color.material_blue_grey_950),
        backgroundDrawable.getResolvedTintColor());
  }
}
