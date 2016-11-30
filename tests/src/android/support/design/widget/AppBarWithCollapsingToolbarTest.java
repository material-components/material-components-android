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

import static org.junit.Assert.assertEquals;

import android.os.Build;
import android.os.SystemClock;
import android.support.design.test.R;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.test.suitebuilder.annotation.MediumTest;
import android.widget.ImageView;

import org.junit.Test;

@MediumTest
public class AppBarWithCollapsingToolbarTest extends AppBarLayoutBaseTest {
    @Test
    public void testPinnedToolbar() {
        configureContent(R.layout.design_appbar_toolbar_collapse_pin,
                R.string.design_appbar_collapsing_toolbar_pin);

        CollapsingToolbarLayout.LayoutParams toolbarLp =
                (CollapsingToolbarLayout.LayoutParams) mToolbar.getLayoutParams();
        assertEquals(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN,
                toolbarLp.getCollapseMode());

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
        final int reallyLongSwipeAmount = 2 * appbarHeight;
        final int shortSwipeAmount = toolbarHeight;

        assertAppBarElevation(0f);

        // Perform a swipe-up gesture across the horizontal center of the screen.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + longSwipeAmount / 2,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be visually snapped below the system status bar.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + toolbarHeight + mAppBar.getTopInset(),
                appbarOnScreenXY[1] + appbarHeight, 1);

        // Perform another swipe-up gesture
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                appbarOnScreenXY[1] + appbarHeight + 5,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be visually snapped below the system status bar
        // as it is in the pinned mode. Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop + toolbarHeight + mAppBar.getTopInset(),
                appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

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
        assertEquals(originalAppbarTop + toolbarHeight + mAppBar.getTopInset(),
                appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                reallyLongSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1], 1);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(0f);

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
        assertAppBarElevation(0f);
    }

    @Test
    public void testScrollingToolbar() {
        configureContent(R.layout.design_appbar_toolbar_collapse_scroll,
                R.string.design_appbar_collapsing_toolbar_scroll);

        CollapsingToolbarLayout.LayoutParams toolbarLp =
                (CollapsingToolbarLayout.LayoutParams) mToolbar.getLayoutParams();
        assertEquals(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN,
                toolbarLp.getCollapseMode());

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int topInset = mAppBar.getTopInset();

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;
        final int reallyLongSwipeAmount = 2 * appbarHeight;
        final int shortSwipeAmount = toolbarHeight;

        assertAppBarElevation(0f);

        // Perform a swipe-up gesture across the horizontal center of the screen, starting from
        // just below the AppBarLayout
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + 20,
                longSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should not be visually "present" on the screen, with its bottom
        // edge aligned with the bottom of system status bar. If we're running on a device which
        // supports a translucent status bar, we need to take the status bar height into account.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight - topInset, 1);
        assertAppBarElevation(0f);

        // Perform another swipe-up gesture
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                shortSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should still be off the screen. Allow for off-by-a-pixel
        // margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1] + appbarHeight - topInset, 1);
        assertAppBarElevation(0f);

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
        assertEquals(originalAppbarTop + toolbarHeight + topInset,
                appbarOnScreenXY[1] + appbarHeight, 1);
        assertAppBarElevation(mDefaultElevationValue);

        // Perform another swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                reallyLongSwipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        // At this point the app bar should be in its original position.
        // Allow for off-by-a-pixel margin of error.
        assertEquals(originalAppbarTop, appbarOnScreenXY[1]);
        assertEquals(originalAppbarBottom, appbarOnScreenXY[1] + appbarHeight);
        assertAppBarElevation(0f);

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
        assertAppBarElevation(0f);
    }

    @Test
    public void testPinnedToolbarAndAnchoredFab() throws Throwable {
        configureContent(R.layout.design_appbar_toolbar_collapse_pin_with_fab,
                R.string.design_appbar_collapsing_toolbar_pin_fab);

        CollapsingToolbarLayout.LayoutParams toolbarLp =
                (CollapsingToolbarLayout.LayoutParams) mToolbar.getLayoutParams();
        assertEquals(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN,
                toolbarLp.getCollapseMode());

        final FloatingActionButton fab =
                (FloatingActionButton) mCoordinatorLayout.findViewById(R.id.fab);

        final int[] appbarOnScreenXY = new int[2];
        final int[] coordinatorLayoutOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mCoordinatorLayout.getLocationOnScreen(coordinatorLayoutOnScreenXY);

        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int appbarHeight = mAppBar.getHeight();
        final int longSwipeAmount = 3 * appbarHeight / 2;

        // Perform a swipe-up gesture across the horizontal center of the screen.
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + longSwipeAmount / 2,
                longSwipeAmount);

        // Since we the visibility change listener path is only exposed via direct calls to
        // FloatingActionButton.show and not the internal path that FAB's behavior is using,
        // this test needs to be tied to the internal implementation details of running animation
        // that scales the FAB to 0/0 scales and interpolates its alpha to 0. Since that animation
        // starts running partway through our swipe gesture and may complete a bit later then
        // the swipe gesture, sleep for a bit to catch the "final" state of the FAB.
        SystemClock.sleep(200);

        // At this point the FAB should be scaled to 0/0 and set at alpha 0. Since the relevant
        // getter methods are only available on v11+, wrap the asserts with build version check.
        if (Build.VERSION.SDK_INT >= 11) {
            assertEquals(0.0f, fab.getScaleX(), 0.0f);
            assertEquals(0.0f, fab.getScaleY(), 0.0f);
            assertEquals(0.0f, fab.getAlpha(), 0.0f);
        }

        // Perform a swipe-down gesture across the horizontal center of the screen.
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                longSwipeAmount);

        // Same as for swipe-up gesture - sleep for a bit to catch the "final" visible state of
        // the FAB.
        SystemClock.sleep(200);

        // At this point the FAB should be scaled back to its original size and be at full opacity.
        if (Build.VERSION.SDK_INT >= 11) {
            assertEquals(1.0f, fab.getScaleX(), 0.0f);
            assertEquals(1.0f, fab.getScaleY(), 0.0f);
            assertEquals(1.0f, fab.getAlpha(), 0.0f);
        }
    }

    @Test
    public void testPinnedToolbarAndParallaxImage() {
        configureContent(R.layout.design_appbar_toolbar_collapse_with_image,
                R.string.design_appbar_collapsing_toolbar_with_image);

        final ImageView parallaxImageView =
                (ImageView) mCoordinatorLayout.findViewById(R.id.app_bar_image);

        CollapsingToolbarLayout.LayoutParams parallaxImageViewLp =
                (CollapsingToolbarLayout.LayoutParams) parallaxImageView.getLayoutParams();
        assertEquals(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PARALLAX,
                parallaxImageViewLp.getCollapseMode());

        final float parallaxMultiplier = parallaxImageViewLp.getParallaxMultiplier();

        final int[] appbarOnScreenXY = new int[2];
        final int[] parallaxImageOnScreenXY = new int[2];
        final int appbarHeight = mAppBar.getHeight();
        final int toolbarHeight = mToolbar.getHeight();
        final int parallaxImageHeight = parallaxImageView.getHeight();

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        parallaxImageView.getLocationOnScreen(parallaxImageOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = appbarOnScreenXY[1] + mAppBar.getHeight();
        final int originalParallaxImageTop = parallaxImageOnScreenXY[1];
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        // Test that at the beginning our image is top-aligned with the app bar
        assertEquals(appbarOnScreenXY[1], parallaxImageOnScreenXY[1]);

        // Swipe up by the toolbar's height
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                toolbarHeight);

        // Test that the top edge of the image (in the screen coordinates) has "moved" by half
        // the amount that the top edge of the app bar (in the screen coordinates) has.
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        parallaxImageView.getLocationOnScreen(parallaxImageOnScreenXY);
        assertEquals(parallaxMultiplier * (appbarOnScreenXY[1] - originalAppbarTop),
                parallaxImageOnScreenXY[1] - originalParallaxImageTop, 1);

        // Swipe up by another toolbar's height
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                toolbarHeight);

        // Test that the top edge of the image (in the screen coordinates) has "moved" by half
        // the amount that the top edge of the app bar (in the screen coordinates) has.
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        parallaxImageView.getLocationOnScreen(parallaxImageOnScreenXY);
        assertEquals(parallaxMultiplier * (appbarOnScreenXY[1] - originalAppbarTop),
                parallaxImageOnScreenXY[1] - originalParallaxImageTop, 1);

        // Swipe down by a different value (150% of the toolbar's height) to test parallax going the
        // other way
        performVerticalSwipeDownGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom,
                3 * toolbarHeight / 2);

        // Test that the top edge of the image (in the screen coordinates) has "moved" by half
        // the amount that the top edge of the app bar (in the screen coordinates) has.
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        parallaxImageView.getLocationOnScreen(parallaxImageOnScreenXY);
        assertEquals(parallaxMultiplier * (appbarOnScreenXY[1] - originalAppbarTop),
                parallaxImageOnScreenXY[1] - originalParallaxImageTop, 1);
    }

    @Test
    public void testAddViewWithDefaultLayoutParams() {
        configureContent(R.layout.design_appbar_toolbar_collapse_pin,
                R.string.design_appbar_collapsing_toolbar_pin);

        InstrumentationRegistry.getInstrumentation().runOnMainSync(new Runnable() {
            @Override
            public void run() {
                ImageView view = new ImageView(mCollapsingToolbar.getContext());
                mCollapsingToolbar.addView(view);
            }
        });

    }

    /**
     * This test only runs on API 11+ since FrameLayout (which CollapsingToolbarLayout
     * inherits from) has an issue with measuring children with margins when run on earlier
     * versions of the platform.
     */
    @Test
    @SdkSuppress(minSdkVersion = 11)
    public void testPinnedToolbarWithMargins() {
        configureContent(R.layout.design_appbar_toolbar_collapse_pin_margins,
                R.string.design_appbar_collapsing_toolbar_pin_margins);

        CollapsingToolbarLayout.LayoutParams toolbarLp =
                (CollapsingToolbarLayout.LayoutParams) mToolbar.getLayoutParams();
        assertEquals(CollapsingToolbarLayout.LayoutParams.COLLAPSE_MODE_PIN,
                toolbarLp.getCollapseMode());

        final int[] appbarOnScreenXY = new int[2];
        final int[] toolbarOnScreenXY = new int[2];
        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mToolbar.getLocationOnScreen(toolbarOnScreenXY);

        final int originalAppbarTop = appbarOnScreenXY[1];
        final int originalAppbarBottom = originalAppbarTop + mAppBar.getHeight();
        final int centerX = appbarOnScreenXY[0] + mAppBar.getWidth() / 2;

        final int toolbarHeight = mToolbar.getHeight();
        final int toolbarVerticalMargins = toolbarLp.topMargin + toolbarLp.bottomMargin;
        final int appbarHeight = mAppBar.getHeight();

        // Perform a swipe-up gesture across the horizontal center of the screen.
        int swipeAmount = appbarHeight - toolbarHeight - toolbarVerticalMargins;
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + (3 * swipeAmount / 2),
                swipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mToolbar.getLocationOnScreen(toolbarOnScreenXY);
        // At this point the toolbar should be visually pinned to the bottom of the appbar layout,
        // observing it's margins and top inset
        // The toolbar should still be visually pinned to the bottom of the appbar layout
        assertEquals(originalAppbarTop + mAppBar.getTopInset(),
                toolbarOnScreenXY[1] - toolbarLp.topMargin, 1);

        // Swipe up again, this time just 50% of the margin size
        swipeAmount = toolbarVerticalMargins / 2;
        performVerticalSwipeUpGesture(
                R.id.coordinator_layout,
                centerX,
                originalAppbarBottom + (3 * swipeAmount / 2),
                swipeAmount);

        mAppBar.getLocationOnScreen(appbarOnScreenXY);
        mToolbar.getLocationOnScreen(toolbarOnScreenXY);

        // The toolbar should still be visually pinned to the bottom of the appbar layout
        assertEquals(appbarOnScreenXY[1] + appbarHeight,
                toolbarOnScreenXY[1] + toolbarHeight + toolbarLp.bottomMargin, 1);
    }
}
