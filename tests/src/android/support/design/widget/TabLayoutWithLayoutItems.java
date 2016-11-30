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

import org.junit.Test;

import android.support.design.test.R;
import android.support.v7.app.AppCompatActivity;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.LayoutInflater;

public class TabLayoutWithLayoutItems extends BaseInstrumentationTestCase<AppCompatActivity> {

    public TabLayoutWithLayoutItems() {
        super(AppCompatActivity.class);
    }

    @Test
    @SmallTest
    public void testInflateTabLayoutWithTabItems() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LayoutInflater inflater = LayoutInflater.from(getActivity());
                final TabLayout tabLayout = (TabLayout) inflater.inflate(
                        R.layout.design_tabs_items, null);

                assertEquals(3, tabLayout.getTabCount());

                // Tab 0 has text, but no icon or custom view
                TabLayout.Tab tab = tabLayout.getTabAt(0);
                assertEquals(getActivity().getString(R.string.tab_layout_text), tab.getText());
                assertNull(tab.getIcon());
                assertNull(tab.getCustomView());

                // Tab 1 has an icon, but no text or custom view
                tab = tabLayout.getTabAt(1);
                assertNull(tab.getText());
                assertNotNull(tab.getIcon());
                assertNull(tab.getCustomView());

                // Tab 2 has a custom view, but no text or icon
                tab = tabLayout.getTabAt(2);
                assertNull(tab.getText());
                assertNull(tab.getIcon());
                assertNotNull(tab.getCustomView());
                assertEquals(R.id.my_custom_tab, tab.getCustomView().getId());
            }
        });
    }

    @Test(expected = IllegalArgumentException.class)
    @SmallTest
    public void testInflateTabLayoutWithNonTabItem() throws Throwable {
        runTestOnUiThread(new Runnable() {
            @Override
            public void run() {
                final LayoutInflater inflater = LayoutInflater.from(getActivity());
                inflater.inflate(R.layout.design_tabs_with_non_tabitems, null);
            }
        });
    }
}
