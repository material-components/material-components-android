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

import static org.junit.Assert.assertTrue;

import android.app.Activity;
import android.support.design.test.R;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.SmallTest;

import org.junit.Test;

public class TabLayoutPoolingTest extends BaseInstrumentationTestCase<TabLayoutPoolingActivity> {

    public TabLayoutPoolingTest() {
        super(TabLayoutPoolingActivity.class);
    }

    @UiThreadTest
    @SmallTest
    @Test
    public void testUsingTabsFromOtherInstance() {
        final Activity activity = mActivityTestRule.getActivity();

        // TabLayout1 has items added via the layout, so we'll just check they're
        // there first
        final TabLayout tabLayout1 = (TabLayout) activity.findViewById(R.id.tabs_1);
        assertTrue(tabLayout1.getTabCount() > 0);

        // Now remove all tabs. TabLayout will pool the Tab instances...
        tabLayout1.removeAllTabs();

        // Now add some tabs to the second TabLayout and make sure that we don't crash
        final TabLayout tabLayout2 = (TabLayout) activity.findViewById(R.id.tabs_2);
        tabLayout2.addTab(tabLayout2.newTab());
        tabLayout2.addTab(tabLayout2.newTab());
        tabLayout2.addTab(tabLayout2.newTab());
    }

}
