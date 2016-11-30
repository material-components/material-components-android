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

        ColorPagerAdapter adapter = new ColorPagerAdapter();
        adapter.add("Red", Color.RED);
        adapter.add("Green", Color.GREEN);
        adapter.add("Blue", Color.BLUE);

        // Configure view pager
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.setAdapter(adapter),
                ViewPagerActions.scrollToPage(0));

        // And wire the tab layout to it
        onView(withId(R.id.tabs)).perform(TabLayoutActions.setupWithViewPager(mViewPager));
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

        // Scroll one tab to the right
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollRight());
        assertEquals("Scroll right", 1, mViewPager.getCurrentItem());
        assertEquals("Selected tab after scrolling", 1, mTabLayout.getSelectedTabPosition());

        // Scroll one more tab to the right
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollRight());
        assertEquals("Scroll right", 2, mViewPager.getCurrentItem());
        assertEquals("Selected tab after scrolling", 2, mTabLayout.getSelectedTabPosition());

        // Scroll one tab to the left
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollLeft());
        assertEquals("Scroll left", 1, mViewPager.getCurrentItem());
        assertEquals("Selected tab after scrolling", 1, mTabLayout.getSelectedTabPosition());

        // Scroll one more tab to the left
        onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollLeft());
        assertEquals("Scroll left", 0, mViewPager.getCurrentItem());
        assertEquals("Selected tab after scrolling", 0, mTabLayout.getSelectedTabPosition());
    }
}
