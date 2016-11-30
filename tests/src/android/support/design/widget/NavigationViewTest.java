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
import android.support.design.test.R;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.test.suitebuilder.annotation.SmallTest;
import org.junit.Before;
import org.junit.Test;

import static android.support.design.testutils.DrawerLayoutActions.closeDrawer;
import static android.support.design.testutils.DrawerLayoutActions.openDrawer;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.*;
import static org.hamcrest.core.AllOf.allOf;

public class NavigationViewTest
        extends BaseInstrumentationTestCase<NavigationViewActivity> {
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
    }

    @Test
    @SmallTest
    public void testBasics() {
        // Open our drawer
        onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

        final Resources res = mActivityTestRule.getActivity().getResources();

        // Check that we have the expected menu items in our NavigationView
        onView(allOf(withText(res.getString(R.string.navigate_home)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(isDisplayed()));
        onView(allOf(withText(res.getString(R.string.navigate_profile)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(isDisplayed()));
        onView(allOf(withText(res.getString(R.string.navigate_people)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(isDisplayed()));
        onView(allOf(withText(res.getString(R.string.navigate_settings)),
                isDescendantOfA(withId(R.id.start_drawer)))).check(matches(isDisplayed()));
    }
}
