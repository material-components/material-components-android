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

import android.content.res.Resources;
import android.support.annotation.ColorInt;
import android.support.design.test.R;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.RecyclerView;
import android.test.suitebuilder.annotation.SmallTest;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static android.support.design.testutils.DrawerLayoutActions.closeDrawer;
import static android.support.design.testutils.DrawerLayoutActions.openDrawer;
import static android.support.design.testutils.NavigationViewActions.*;
import static android.support.design.testutils.TestUtilsMatchers.*;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

public class NavigationViewTest
        extends BaseInstrumentationTestCase<NavigationViewActivity> {
    private static final int[] MENU_CONTENT_ITEM_IDS = { R.id.destination_home,
            R.id.destination_profile, R.id.destination_people, R.id.destination_settings };
    private Map<Integer, String> mMenuStringContent;

    private DrawerLayout mDrawerLayout;

    private NavigationView mNavigationView;

    public NavigationViewTest() {
        super(NavigationViewActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        final NavigationViewActivity activity = mActivityTestRule.getActivity();
        mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) mDrawerLayout.findViewById(R.id.start_drawer);

        // Close the drawer to reset the state for the next test
        onView(withId(R.id.drawer_layout)).perform(closeDrawer(GravityCompat.START));

        final Resources res = activity.getResources();
        mMenuStringContent = new HashMap<>(MENU_CONTENT_ITEM_IDS.length);
        mMenuStringContent.put(R.id.destination_home, res.getString(R.string.navigate_home));
        mMenuStringContent.put(R.id.destination_profile, res.getString(R.string.navigate_profile));
        mMenuStringContent.put(R.id.destination_people, res.getString(R.string.navigate_people));
        mMenuStringContent.put(R.id.destination_settings,
                res.getString(R.string.navigate_settings));
    }

    @Test
    @SmallTest
    public void testBasics() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        // Check that we have the expected menu items in our NavigationView
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            onView(allOf(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                    isDescendantOfA(withId(R.id.start_drawer)))).check(matches(isDisplayed()));
        }
    }

    @Test
    @SmallTest
    public void testTextAppearance() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        final Resources res = mActivityTestRule.getActivity().getResources();
        final int defaultTextSize = res.getDimensionPixelSize(R.dimen.text_medium_size);

        // Check the default style of the menu items in our NavigationView
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            onView(allOf(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                    isDescendantOfA(withId(R.id.start_drawer)))).check(
                    matches(withTextSize(defaultTextSize)));
        }

        // Set a new text appearance on our NavigationView
        onView(withId(R.id.start_drawer)).perform(setItemTextAppearance(R.style.TextSmallStyle));

        // And check that all the menu items have the new style
        final int newTextSize = res.getDimensionPixelSize(R.dimen.text_small_size);
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            onView(allOf(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                    isDescendantOfA(withId(R.id.start_drawer)))).check(
                    matches(withTextSize(newTextSize)));
        }
    }

    @Test
    @SmallTest
    public void testTextColor() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        final Resources res = mActivityTestRule.getActivity().getResources();
        final @ColorInt int defaultTextColor = ResourcesCompat.getColor(res,
                R.color.emerald_text, null);

        // Check the default text color of the menu items in our NavigationView
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            onView(allOf(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                    isDescendantOfA(withId(R.id.start_drawer)))).check(
                    matches(withTextColor(defaultTextColor)));
        }

        // Set a new text color on our NavigationView
        onView(withId(R.id.start_drawer)).perform(setItemTextColor(
                ResourcesCompat.getColorStateList(res, R.color.color_state_list_lilac, null)));

        // And check that all the menu items have the new color
        final @ColorInt int newTextColor = ResourcesCompat.getColor(res,
                R.color.lilac_default, null);
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            onView(allOf(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                    isDescendantOfA(withId(R.id.start_drawer)))).check(
                    matches(withTextColor(newTextColor)));
        }
    }

    @Test
    @SmallTest
    public void testBackground() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        final Resources res = mActivityTestRule.getActivity().getResources();
        final @ColorInt int defaultFillColor = ResourcesCompat.getColor(res,
                R.color.sand_default, null);

        // Check the default fill color of the menu items in our NavigationView
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            // Note that here we're tying ourselves to the implementation details of the
            // internal structure of the NavigationView. Specifically, we're looking at the
            // direct child of RecyclerView which is expected to have the background set
            // on it. If the internal implementation of NavigationView changes, the second
            // Matcher below will need to be tweaked.
            Matcher menuItemMatcher = allOf(
                    hasDescendant(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
                    isChildOfA(isAssignableFrom(RecyclerView.class)),
                    isDescendantOfA(withId(R.id.start_drawer)));

            onView(menuItemMatcher).check(matches(withBackgroundFill(defaultFillColor)));
        }

        // Set a new background (flat fill color) on our NavigationView
        onView(withId(R.id.start_drawer)).perform(setItemBackgroundResource(
                R.drawable.test_background_blue));

        // And check that all the menu items have the new fill
        final @ColorInt int newFillColorBlue = ResourcesCompat.getColor(res,
                R.color.test_blue, null);
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            Matcher menuItemMatcher = allOf(
                    hasDescendant(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
                    isChildOfA(isAssignableFrom(RecyclerView.class)),
                    isDescendantOfA(withId(R.id.start_drawer)));

            onView(menuItemMatcher).check(matches(withBackgroundFill(newFillColorBlue)));
        }

        // Set another new background on our NavigationView
        onView(withId(R.id.start_drawer)).perform(setItemBackground(
                ResourcesCompat.getDrawable(res, R.drawable.test_background_green, null)));

        // And check that all the menu items have the new fill
        final @ColorInt int newFillColorGreen = ResourcesCompat.getColor(res,
                R.color.test_green, null);
        for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
            Matcher menuItemMatcher = allOf(
                    hasDescendant(withText(mMenuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
                    isChildOfA(isAssignableFrom(RecyclerView.class)),
                    isDescendantOfA(withId(R.id.start_drawer)));

            onView(menuItemMatcher).check(matches(withBackgroundFill(newFillColorGreen)));
        }
    }

    @Test
    @SmallTest
    public void testIconTinting() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        final Resources res = mActivityTestRule.getActivity().getResources();
        final @ColorInt int defaultTintColor = ResourcesCompat.getColor(res,
                R.color.emerald_translucent, null);

        // We're allowing a margin of error in checking the color of the items' icons.
        // This is due to the translucent color being used in the icon tinting
        // and off-by-one discrepancies of SRC_IN when it's compositing
        // translucent color. Note that all the checks below are written for the current
        // logic on NavigationView that uses the default SRC_IN tint mode - effectively
        // replacing all non-transparent pixels in the destination (original icon) with
        // our translucent tint color.
        final int allowedComponentVariance = 1;

        // Note that here we're tying ourselves to the implementation details of the
        // internal structure of the NavigationView. Specifically, we're checking the
        // start drawable of the text view with the specific text. If the internal
        // implementation of NavigationView changes, the second Matcher in the lookups
        // below will need to be tweaked.
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));

        final @ColorInt int newTintColor = ResourcesCompat.getColor(res,
                R.color.red_translucent, null);

        onView(withId(R.id.start_drawer)).perform(setItemIconTintList(
                ResourcesCompat.getColorStateList(res, R.color.color_state_list_red_translucent,
                        null)));
        // Check that all menu items with icons now have icons tinted with the newly set color
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));

        // And now remove all icon tinting
        onView(withId(R.id.start_drawer)).perform(setItemIconTintList(null));
        // And verify that all menu items with icons now have the original colors for their icons.
        // Note that since there is no tinting at this point, we don't allow any color variance
        // in these checks.
        final @ColorInt int redIconColor = ResourcesCompat.getColor(res, R.color.test_red, null);
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(redIconColor, 0)));
        final @ColorInt int greenIconColor = ResourcesCompat.getColor(res, R.color.test_green,
                null);
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(greenIconColor, 0)));
        final @ColorInt int blueIconColor = ResourcesCompat.getColor(res, R.color.test_blue, null);
        onView(allOf(withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(
                    withStartDrawableFilledWith(blueIconColor, 0)));
    }
}
