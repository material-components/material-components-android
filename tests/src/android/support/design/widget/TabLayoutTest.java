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
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.support.design.test.R;
import android.support.test.annotation.UiThreadTest;
import android.support.v7.app.AppCompatActivity;
import android.test.suitebuilder.annotation.SmallTest;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;

import org.junit.Test;

@SmallTest
public class TabLayoutTest extends BaseInstrumentationTestCase<AppCompatActivity> {
    public TabLayoutTest() {
        super(AppCompatActivity.class);
    }

    @Test
    @UiThreadTest
    public void testInflateTabLayoutWithTabItems() {
        final LayoutInflater inflater = LayoutInflater.from(mActivityTestRule.getActivity());
        final TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.design_tabs_items, null);

        assertEquals(3, tabLayout.getTabCount());

        // Tab 0 has text, but no icon or custom view
        TabLayout.Tab tab = tabLayout.getTabAt(0);
        assertEquals(mActivityTestRule.getActivity().getString(R.string.tab_layout_text),
                tab.getText());
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

    @Test
    @UiThreadTest
    public void testInflateTabLayoutWithNonTabItem() throws Throwable {
        try {
            final LayoutInflater inflater =
                    LayoutInflater.from(mActivityTestRule.getActivity());
            inflater.inflate(R.layout.design_tabs_with_non_tabitems, null);
        } catch (Throwable throwable) {
            assertTrue(throwable instanceof InflateException
                    || throwable instanceof IllegalArgumentException);
        }
    }

    @Test
    @UiThreadTest
    public void testTabWithCustomLayoutSelection1() {
        final TabLayout.OnTabSelectedListener mockListener =
                mock(TabLayout.OnTabSelectedListener.class);
        final LayoutInflater inflater = LayoutInflater.from(mActivityTestRule.getActivity());

        final TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.design_tabs, null);
        tabLayout.addOnTabSelectedListener(mockListener);
        final TabLayout.Tab tab = tabLayout.newTab();
        tab.setCustomView(R.layout.design_tab_item_custom);
        tabLayout.addTab(tab);
        verify(mockListener, times(1)).onTabSelected(eq(tab));
        verify(mockListener, times(0)).onTabUnselected(any(TabLayout.Tab.class));

        assertNotNull("Tab has custom view", tab.getCustomView());
        assertEquals("First tab is selected", 0, tabLayout.getSelectedTabPosition());
        assertTabCustomViewSelected(tabLayout);
    }

    @Test
    @UiThreadTest
    public void testTabWithCustomLayoutSelection2() {
        final TabLayout.OnTabSelectedListener mockListener =
                mock(TabLayout.OnTabSelectedListener.class);
        final LayoutInflater inflater = LayoutInflater.from(mActivityTestRule.getActivity());

        final TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.design_tabs, null);
        tabLayout.addOnTabSelectedListener(mockListener);
        final TabLayout.Tab tab = tabLayout.newTab();
        tabLayout.addTab(tab);
        verify(mockListener, times(1)).onTabSelected(eq(tab));
        verify(mockListener, times(0)).onTabUnselected(any(TabLayout.Tab.class));
        tab.setCustomView(R.layout.design_tab_item_custom);

        assertNotNull("Tab has custom view", tab.getCustomView());
        assertEquals("First tab is selected", 0, tabLayout.getSelectedTabPosition());
        assertTabCustomViewSelected(tabLayout);
    }

    @Test
    @UiThreadTest
    public void testMultipleTabsWithCustomLayoutSelection1() {
        final TabLayout.OnTabSelectedListener mockListener =
                mock(TabLayout.OnTabSelectedListener.class);
        final LayoutInflater inflater = LayoutInflater.from(mActivityTestRule.getActivity());
        final TabLayout tabs = (TabLayout) inflater.inflate(R.layout.design_tabs, null);
        tabs.addOnTabSelectedListener(mockListener);

        final TabLayout.Tab tab1 = tabs.newTab().setCustomView(R.layout.design_tab_item_custom);
        tabs.addTab(tab1);
        verify(mockListener, times(1)).onTabSelected(eq(tab1));
        verify(mockListener, times(0)).onTabUnselected(any(TabLayout.Tab.class));
        final TabLayout.Tab tab2 = tabs.newTab().setCustomView(R.layout.design_tab_item_custom);
        tabs.addTab(tab2, true);
        verify(mockListener, times(1)).onTabSelected(eq(tab2));
        verify(mockListener, times(1)).onTabUnselected(eq(tab1));
        final TabLayout.Tab tab3 = tabs.newTab().setCustomView(R.layout.design_tab_item_custom);
        tabs.addTab(tab3);
        verifyNoMoreInteractions(mockListener);

        assertEquals("Second tab is selected", 1, tabs.getSelectedTabPosition());
        assertTabCustomViewSelected(tabs);
    }

    @Test
    @UiThreadTest
    public void testMultipleTabsWithCustomLayoutSelection2() {
        final TabLayout.OnTabSelectedListener mockListener =
                mock(TabLayout.OnTabSelectedListener.class);
        final LayoutInflater inflater = LayoutInflater.from(mActivityTestRule.getActivity());
        final TabLayout tabs = (TabLayout) inflater.inflate(R.layout.design_tabs, null);
        tabs.addOnTabSelectedListener(mockListener);

        final TabLayout.Tab tab1 = tabs.newTab();
        tabs.addTab(tab1);
        verify(mockListener, times(1)).onTabSelected(eq(tab1));
        verify(mockListener, times(0)).onTabUnselected(any(TabLayout.Tab.class));
        final TabLayout.Tab tab2 = tabs.newTab();
        tabs.addTab(tab2, true);
        verify(mockListener, times(1)).onTabSelected(eq(tab2));
        verify(mockListener, times(1)).onTabUnselected(eq(tab1));
        final TabLayout.Tab tab3 = tabs.newTab();
        tabs.addTab(tab3);
        verifyNoMoreInteractions(mockListener);

        tabs.getTabAt(0).setCustomView(R.layout.design_tab_item_custom);
        tabs.getTabAt(1).setCustomView(R.layout.design_tab_item_custom);
        tabs.getTabAt(2).setCustomView(R.layout.design_tab_item_custom);

        assertEquals("Second tab is selected", 1, tabs.getSelectedTabPosition());
        assertTabCustomViewSelected(tabs);
    }

    private static void assertTabCustomViewSelected(final TabLayout tabLayout) {
        for (int i = 0, count = tabLayout.getTabCount(); i < count; i++) {
            final TabLayout.Tab tab = tabLayout.getTabAt(i);
            final View tabCustomView = tab.getCustomView();
            if (tabCustomView != null) {
                assertEquals(tab.isSelected(), tabCustomView.isSelected());
            }
        }
    }
}
