/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.graphics.Color;
import android.support.design.testutils.TabLayoutActions;
import android.support.design.testutils.ViewPagerActions;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.design.test.R;
import android.test.suitebuilder.annotation.SmallTest;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import org.junit.Test;

import static android.support.test.espresso.matcher.ViewMatchers.withId;

import java.util.ArrayList;

import static android.support.test.espresso.Espresso.onView;

public class TabLayoutWithViewPagerTest
        extends BaseInstrumentationTestCase<TabLayoutWithViewPagerActivity> {
    private TabLayout mTabLayout;

    private ViewPager mViewPager;

    private ColorPagerAdapter mDefaultPagerAdapter;

    protected static class BasePagerAdapter<Q> extends PagerAdapter {
        protected ArrayList<Pair<String, Q>> mEntries = new ArrayList<>();

        public void add(String title, Q content) {
            mEntries.add(new Pair(title, content));
        }

        @Override
        public int getCount() {
            return mEntries.size();
        }

        protected void configureInstantiatedItem(View view, int position) {
            switch (position) {
                case 0:
                    view.setId(R.id.page_0);
                    break;
                case 1:
                    view.setId(R.id.page_1);
                    break;
                case 2:
                    view.setId(R.id.page_2);
                    break;
                case 3:
                    view.setId(R.id.page_3);
                    break;
                case 4:
                    view.setId(R.id.page_4);
                    break;
                case 5:
                    view.setId(R.id.page_5);
                    break;
                case 6:
                    view.setId(R.id.page_6);
                    break;
                case 7:
                    view.setId(R.id.page_7);
                    break;
                case 8:
                    view.setId(R.id.page_8);
                    break;
                case 9:
                    view.setId(R.id.page_9);
                    break;
            }
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            // The adapter is also responsible for removing the view.
            container.removeView(((ViewHolder) object).view);
        }

        @Override
        public int getItemPosition(Object object) {
            return ((ViewHolder) object).position;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return ((ViewHolder) object).view == view;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mEntries.get(position).first;
        }

        protected static class ViewHolder {
            final View view;
            final int position;

            public ViewHolder(View view, int position) {
                this.view = view;
                this.position = position;
            }
        }
    }

    protected static class ColorPagerAdapter extends BasePagerAdapter<Integer> {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final View view = new View(container.getContext());
            view.setBackgroundColor(mEntries.get(position).second);
            configureInstantiatedItem(view, position);

            // Unlike ListView adapters, the ViewPager adapter is responsible
            // for adding the view to the container.
            container.addView(view);

            return new ViewHolder(view, position);
        }
    }

    protected static class TextPagerAdapter extends BasePagerAdapter<String> {
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final TextView view = new TextView(container.getContext());
            view.setText(mEntries.get(position).second);
            configureInstantiatedItem(view, position);

            // Unlike ListView adapters, the ViewPager adapter is responsible
            // for adding the view to the container.
            container.addView(view);

            return new ViewHolder(view, position);
        }
    }

    public TabLayoutWithViewPagerTest() {
        super(TabLayoutWithViewPagerActivity.class);
    }

    public void setUp() throws Exception {
        super.setUp();

        final TabLayoutWithViewPagerActivity activity = getActivity();
        mTabLayout = (TabLayout) activity.findViewById(R.id.tabs);
        mViewPager = (ViewPager) activity.findViewById(R.id.tabs_viewpager);

        mDefaultPagerAdapter = new ColorPagerAdapter();
        mDefaultPagerAdapter.add("Red", Color.RED);
        mDefaultPagerAdapter.add("Green", Color.GREEN);
        mDefaultPagerAdapter.add("Blue", Color.BLUE);

        // Configure view pager
        onView(withId(R.id.tabs_viewpager)).perform(
                ViewPagerActions.setAdapter(mDefaultPagerAdapter),
                ViewPagerActions.scrollToPage(0));

        // And wire the tab layout to it
        onView(withId(R.id.tabs)).perform(TabLayoutActions.setupWithViewPager(mViewPager));
    }

    /**
     * Verifies that selecting pages in <code>ViewPager</code> also updates the tab selection
     * in the wired <code>TabLayout</code>
     */
    private void verifyViewPagerSelection() {
        int itemCount = mViewPager.getAdapter().getCount();

        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollToPage(0));
        assertEquals("Selected page", 0, mViewPager.getCurrentItem());
        assertEquals("Selected tab", 0, mTabLayout.getSelectedTabPosition());

        // Scroll tabs to the right
        for (int i = 0; i < (itemCount - 1); i++) {
            // Scroll one tab to the right
            onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollRight());
            final int expectedCurrentTabIndex = i + 1;
            assertEquals("Scroll right #" + i, expectedCurrentTabIndex,
                    mViewPager.getCurrentItem());
            assertEquals("Selected tab after scrolling right #" + i, expectedCurrentTabIndex,
                    mTabLayout.getSelectedTabPosition());
        }

        // Scroll tabs to the left
        for (int i = 0; i < (itemCount - 1); i++) {
            // Scroll one tab to the left
            onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollLeft());
            final int expectedCurrentTabIndex = itemCount - i - 2;
            assertEquals("Scroll left #" + i, expectedCurrentTabIndex, mViewPager.getCurrentItem());
            assertEquals("Selected tab after scrolling left #" + i, expectedCurrentTabIndex,
                    mTabLayout.getSelectedTabPosition());
        }
    }

    /**
     * Verifies that selecting pages in <code>ViewPager</code> also updates the tab selection
     * in the wired <code>TabLayout</code>
     */
    private void verifyTabLayoutSelection() {
        int itemCount = mTabLayout.getTabCount();

        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollToPage(0));
        assertEquals("Selected tab", 0, mTabLayout.getSelectedTabPosition());
        assertEquals("Selected page", 0, mViewPager.getCurrentItem());

        // Select tabs "going" to the right. Note that the first loop iteration tests the
        // scenario of "selecting" the first tab when it's already selected.
        for (int i = 0; i < itemCount; i++) {
            onView(withId(R.id.tabs)).perform(TabLayoutActions.selectTab(i));
            assertEquals("Selected tab after selecting #" + i, i,
                    mTabLayout.getSelectedTabPosition());
            assertEquals("Select tab #" + i, i, mViewPager.getCurrentItem());
        }

        // Select tabs "going" to the left. Note that the first loop iteration tests the
        // scenario of "selecting" the last tab when it's already selected.
        for (int i = itemCount - 1; i >= 0; i--) {
            onView(withId(R.id.tabs)).perform(TabLayoutActions.selectTab(i));
            assertEquals("Scroll left #" + i, i, mViewPager.getCurrentItem());
            assertEquals("Selected tab after scrolling left #" + i, i,
                    mTabLayout.getSelectedTabPosition());
        }
    }

    @Test
    @SmallTest
    public void testBasics() {
        final int itemCount = mViewPager.getAdapter().getCount();

        assertEquals("Matching item count", itemCount, mTabLayout.getTabCount());

        for (int i = 0; i < itemCount; i++) {
            assertEquals("Tab #" +i, mViewPager.getAdapter().getPageTitle(i),
                    mTabLayout.getTabAt(i).getText());
        }

        assertEquals("Selected tab", mViewPager.getCurrentItem(),
                mTabLayout.getSelectedTabPosition());

        verifyViewPagerSelection();
    }

    @Test
    @SmallTest
    public void testInteraction() {
        assertEquals("Default selected page", 0, mViewPager.getCurrentItem());
        assertEquals("Default selected tab", 0, mTabLayout.getSelectedTabPosition());

        verifyTabLayoutSelection();
    }

    @Test
    @SmallTest
    public void testAdapterContentChange() {
        // Verify that we have the expected initial adapter
        PagerAdapter initialAdapter = mViewPager.getAdapter();
        assertEquals("Initial adapter class", ColorPagerAdapter.class, initialAdapter.getClass());
        assertEquals("Initial adapter page count", 3, initialAdapter.getCount());

        // Add two more entries to our adapter
        final int newItemCount = 5;
        mDefaultPagerAdapter.add("Yellow", Color.YELLOW);
        mDefaultPagerAdapter.add("Magenta", Color.MAGENTA);
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.notifyAdapterContentChange());

        // We have more comprehensive test coverage for changing the ViewPager adapter in v4/tests.
        // Here we are focused on testing the continuous integration of TabLayout with the new
        // content of ViewPager

        assertEquals("Matching item count", newItemCount, mTabLayout.getTabCount());

        for (int i = 0; i < newItemCount; i++) {
            assertEquals("Tab #" +i, mViewPager.getAdapter().getPageTitle(i),
                    mTabLayout.getTabAt(i).getText());
        }

        verifyViewPagerSelection();
        verifyTabLayoutSelection();
    }

    @Test
    @SmallTest
    public void testAdapterChange() {
        // Verify that we have the expected initial adapter
        PagerAdapter initialAdapter = mViewPager.getAdapter();
        assertEquals("Initial adapter class", ColorPagerAdapter.class, initialAdapter.getClass());
        assertEquals("Initial adapter page count", 3, initialAdapter.getCount());

        // Create a new adapter
        TextPagerAdapter newAdapter = new TextPagerAdapter();
        final int newItemCount = 6;
        for (int i = 0; i < newItemCount; i++) {
            newAdapter.add("Title " + i, "Body " + i);
        }
        // And set it on the ViewPager
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.setAdapter(newAdapter),
                ViewPagerActions.scrollToPage(0));

        // As TabLayout doesn't track adapter changes, we need to re-wire the new adapter
        onView(withId(R.id.tabs)).perform(TabLayoutActions.setupWithViewPager(mViewPager));

        // We have more comprehensive test coverage for changing the ViewPager adapter in v4/tests.
        // Here we are focused on testing the integration of TabLayout with the new
        // content of ViewPager

        assertEquals("Matching item count", newItemCount, mTabLayout.getTabCount());

        for (int i = 0; i < newItemCount; i++) {
            assertEquals("Tab #" +i, mViewPager.getAdapter().getPageTitle(i),
                    mTabLayout.getTabAt(i).getText());
        }

        verifyViewPagerSelection();
        verifyTabLayoutSelection();
    }
}
