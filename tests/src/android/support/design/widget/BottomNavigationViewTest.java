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

import static android.support.design.testutils.BottomNavigationViewActions.setIconForMenuItem;
import static android.support.design.testutils.BottomNavigationViewActions.setItemIconTintList;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.design.test.R;
import android.support.design.testutils.TestDrawable;
import android.support.design.testutils.TestUtilsMatchers;
import android.support.v4.content.res.ResourcesCompat;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.Menu;
import android.view.MenuItem;

import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

public class BottomNavigationViewTest
        extends BaseInstrumentationTestCase<BottomNavigationViewActivity> {
    private static final int[] MENU_CONTENT_ITEM_IDS = { R.id.destination_home,
            R.id.destination_profile, R.id.destination_people };
    private Map<Integer, String> mMenuStringContent;

    private BottomNavigationView mBottomNavigation;

    public BottomNavigationViewTest() {
        super(BottomNavigationViewActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        final BottomNavigationViewActivity activity = mActivityTestRule.getActivity();
        mBottomNavigation = (BottomNavigationView) activity.findViewById(R.id.bottom_navigation);

        final Resources res = activity.getResources();
        mMenuStringContent = new HashMap<>(MENU_CONTENT_ITEM_IDS.length);
        mMenuStringContent.put(R.id.destination_home, res.getString(R.string.navigate_home));
        mMenuStringContent.put(R.id.destination_profile, res.getString(R.string.navigate_profile));
        mMenuStringContent.put(R.id.destination_people, res.getString(R.string.navigate_people));
    }

    @Test
    @SmallTest
    public void testBasics() {
        // Check the contents of the Menu object
        final Menu menu = mBottomNavigation.getMenu();
        assertNotNull("Menu should not be null", menu);
        assertEquals("Should have matching number of items", MENU_CONTENT_ITEM_IDS.length,
                menu.size());
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            final MenuItem currItem = menu.getItem(i);
            assertEquals("ID for Item #" + i, MENU_CONTENT_ITEM_IDS[i], currItem.getItemId());
        }

    }

    @Test
    @SmallTest
    public void testNavigationSelectionListener() {
        BottomNavigationView.OnNavigationItemSelectedListener mockedListener =
                mock(BottomNavigationView.OnNavigationItemSelectedListener.class);
        mBottomNavigation.setOnNavigationItemSelectedListener(mockedListener);

        // Make the listener return true to allow selecting the item.
        when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(true);
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)), isDisplayed())).perform(click());
        // Verify our listener has been notified of the click
        verify(mockedListener, times(1)).onNavigationItemSelected(
                mBottomNavigation.getMenu().findItem(R.id.destination_profile));
        // Verify the item is now selected
        assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

        // Make the listener return false to disallow selecting the item.
        when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(false);
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.bottom_navigation)), isDisplayed())).perform(click());
        // Verify our listener has been notified of the click
        verify(mockedListener, times(1)).onNavigationItemSelected(
                mBottomNavigation.getMenu().findItem(R.id.destination_people));
        // Verify the previous item is still selected
        assertFalse(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
        assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

        // Set null listener to test that the next click is not going to notify the
        // previously set listener and will allow selecting items.
        mBottomNavigation.setOnNavigationItemSelectedListener(null);

        // Click one of our items
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.bottom_navigation)), isDisplayed())).perform(click());
        // And that our previous listener has not been notified of the click
        verifyNoMoreInteractions(mockedListener);
        // Verify the correct item is now selected.
        assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_home).isChecked());
    }

    @Test
    @SmallTest
    public void testIconTinting() {
        final Resources res = mActivityTestRule.getActivity().getResources();
        @ColorInt final int redFill = ResourcesCompat.getColor(res, R.color.test_red, null);
        @ColorInt final int greenFill = ResourcesCompat.getColor(res, R.color.test_green, null);
        @ColorInt final int blueFill = ResourcesCompat.getColor(res, R.color.test_blue, null);
        final int iconSize = res.getDimensionPixelSize(R.dimen.drawable_small_size);
        onView(withId(R.id.bottom_navigation)).perform(setIconForMenuItem(R.id.destination_home,
                new TestDrawable(redFill, iconSize, iconSize)));
        onView(withId(R.id.bottom_navigation)).perform(setIconForMenuItem(R.id.destination_profile,
                new TestDrawable(greenFill, iconSize, iconSize)));
        onView(withId(R.id.bottom_navigation)).perform(setIconForMenuItem(R.id.destination_people,
                new TestDrawable(blueFill, iconSize, iconSize)));

        @ColorInt final int defaultTintColor = ResourcesCompat.getColor(res,
                R.color.emerald_translucent, null);

        // We're allowing a margin of error in checking the color of the items' icons.
        // This is due to the translucent color being used in the icon tinting
        // and off-by-one discrepancies of SRC_IN when it's compositing
        // translucent color. Note that all the checks below are written for the current
        // logic on BottomNavigationView that uses the default SRC_IN tint mode - effectively
        // replacing all non-transparent pixels in the destination (original icon) with
        // our translucent tint color.
        final int allowedComponentVariance = 1;

        // Note that here we're tying ourselves to the implementation details of the internal
        // structure of the BottomNavigationView. Specifically, we're checking the drawable the
        // ImageView with id R.id.icon. If the internal implementation of BottomNavigationView
        // changes, the second Matcher in the lookups below will need to be tweaked.
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home)))).check(
                matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile)))).check(
                matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people)))).check(
                matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));

        @ColorInt final int newTintColor = ResourcesCompat.getColor(res,
                R.color.red_translucent, null);
        onView(withId(R.id.bottom_navigation)).perform(setItemIconTintList(
                ResourcesCompat.getColorStateList(res, R.color.color_state_list_red_translucent,
                        null)));
        // Check that all menu items with icons now have icons tinted with the newly set color
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home)))).check(
                matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile)))).check(
                matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people)))).check(
                matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));

        // And now remove all icon tinting
        onView(withId(R.id.bottom_navigation)).perform(setItemIconTintList(null));
        // And verify that all menu items with icons now have the original colors for their icons.
        // Note that since there is no tinting at this point, we don't allow any color variance
        // in these checks.
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home)))).check(
                matches(TestUtilsMatchers.drawable(redFill, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile)))).check(
                matches(TestUtilsMatchers.drawable(greenFill, allowedComponentVariance)));
        onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people)))).check(
                matches(TestUtilsMatchers.drawable(blueFill, allowedComponentVariance)));
    }
}
