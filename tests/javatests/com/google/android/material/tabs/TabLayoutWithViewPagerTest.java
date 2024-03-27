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
package com.google.android.material.tabs;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withParent;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.material.testutils.TabLayoutActions.setupWithViewPager;
import static com.google.android.material.testutils.TabLayoutActions.showBadgeOnTab;
import static com.google.android.material.testutils.ViewPagerActions.setAdapter;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

import android.content.res.Resources;
import android.graphics.Color;
import android.util.Pair;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import androidx.annotation.DimenRes;
import androidx.annotation.LayoutRes;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.filters.LargeTest;
import androidx.test.filters.MediumTest;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.TabLayoutWithViewPagerActivity;
import com.google.android.material.testutils.TabLayoutActions;
import com.google.android.material.testutils.TestUtilsActions;
import com.google.android.material.testutils.TestUtilsMatchers;
import com.google.android.material.testutils.ViewPagerActions;
import java.util.ArrayList;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class TabLayoutWithViewPagerTest {

  @Rule
  public final ActivityTestRule<TabLayoutWithViewPagerActivity> activityTestRule =
      new ActivityTestRule<>(TabLayoutWithViewPagerActivity.class);

  private TabLayout tabLayout;
  private ViewPager viewPager;
  private ColorPagerAdapter defaultPagerAdapter;

  static class BasePagerAdapter<Q> extends PagerAdapter {
    ArrayList<Pair<String, Q>> entries = new ArrayList<>();

    public void add(String title, Q content) {
      entries.add(new Pair<>(title, content));
    }

    public void remove(int index) {
      entries.remove(index);
    }

    @Override
    public int getCount() {
      return entries.size();
    }

    void configureInstantiatedItem(View view, int position) {
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
        default:
          throw new IllegalArgumentException("Invalid position: " + position);
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
      return entries.get(position).first;
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
      view.setBackgroundColor(entries.get(position).second);
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
      view.setText(entries.get(position).second);
      configureInstantiatedItem(view, position);

      // Unlike ListView adapters, the ViewPager adapter is responsible
      // for adding the view to the container.
      container.addView(view);

      return new ViewHolder(view, position);
    }
  }

  private static <Q> ViewAction addItemToPager(final String title, final Q content) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(ViewPager.class);
      }

      @Override
      public String getDescription() {
        return "Add item and notify on content change";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        final ViewPager viewPager = (ViewPager) view;
        @SuppressWarnings("unchecked") // no way to avoid this cast
        final BasePagerAdapter<Q> viewPagerAdapter = (BasePagerAdapter<Q>) viewPager.getAdapter();
        viewPagerAdapter.add(title, content);
        viewPagerAdapter.notifyDataSetChanged();

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  private static <Q> ViewAction addItemsToPager(final String[] title, final Q[] content) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(ViewPager.class);
      }

      @Override
      public String getDescription() {
        return "Add items and notify on content change";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        final ViewPager viewPager = (ViewPager) view;
        @SuppressWarnings("unchecked") // no way to avoid this cast
        final BasePagerAdapter<Q> viewPagerAdapter = (BasePagerAdapter<Q>) viewPager.getAdapter();
        int itemCount = title.length;
        for (int i = 0; i < itemCount; i++) {
          viewPagerAdapter.add(title[i], content[i]);
        }
        viewPagerAdapter.notifyDataSetChanged();

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  private static <Q> ViewAction removeItemAtIndexFromPager(final int index) {
    return new ViewAction() {
      @Override
      public Matcher<View> getConstraints() {
        return isAssignableFrom(ViewPager.class);
      }

      @Override
      public String getDescription() {
        return "Remove item at specified index and notify on content change";
      }

      @Override
      public void perform(UiController uiController, View view) {
        uiController.loopMainThreadUntilIdle();

        final ViewPager viewPager = (ViewPager) view;
        @SuppressWarnings("unchecked") // no way to avoid this cast
        final BasePagerAdapter<Q> viewPagerAdapter = (BasePagerAdapter<Q>) viewPager.getAdapter();
        viewPagerAdapter.remove(index);
        viewPagerAdapter.notifyDataSetChanged();

        uiController.loopMainThreadUntilIdle();
      }
    };
  }

  @Before
  public void setUp() throws Exception {
    final TabLayoutWithViewPagerActivity activity = activityTestRule.getActivity();
    activity.setTheme(R.style.Theme_MaterialComponents_Light);

    tabLayout = activity.findViewById(R.id.tabs);
    viewPager = activity.findViewById(R.id.tabs_viewpager);

    defaultPagerAdapter = new ColorPagerAdapter();
    defaultPagerAdapter.add("Red", Color.RED);
    defaultPagerAdapter.add("Green", Color.GREEN);
    defaultPagerAdapter.add("Blue", Color.BLUE);

    // Configure view pager
    onView(withId(R.id.tabs_viewpager))
        .perform(setAdapter(defaultPagerAdapter), ViewPagerActions.scrollToPage(0));
  }

  private void setupTabLayoutWithViewPager() {
    // And wire the tab layout to it
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager));
  }

  /**
   * Verifies that selecting pages in <code>ViewPager</code> also updates the tab selection in the
   * wired <code>TabLayout</code>
   */
  private void verifyViewPagerSelection() {
    int itemCount = viewPager.getAdapter().getCount();

    onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollToPage(0));
    assertEquals("Selected page", 0, viewPager.getCurrentItem());
    assertEquals("Selected tab", 0, tabLayout.getSelectedTabPosition());

    // Scroll tabs to the right
    for (int i = 0; i < (itemCount - 1); i++) {
      // Scroll one tab to the right
      onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollRight());
      final int expectedCurrentTabIndex = i + 1;
      assertEquals("Scroll right #" + i, expectedCurrentTabIndex, viewPager.getCurrentItem());
      assertEquals(
          "Selected tab after scrolling right #" + i,
          expectedCurrentTabIndex,
          tabLayout.getSelectedTabPosition());
    }

    // Scroll tabs to the left
    for (int i = 0; i < (itemCount - 1); i++) {
      // Scroll one tab to the left
      onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollLeft());
      final int expectedCurrentTabIndex = itemCount - i - 2;
      assertEquals("Scroll left #" + i, expectedCurrentTabIndex, viewPager.getCurrentItem());
      assertEquals(
          "Selected tab after scrolling left #" + i,
          expectedCurrentTabIndex,
          tabLayout.getSelectedTabPosition());
    }
  }

  /**
   * Verifies that selecting pages in <code>ViewPager</code> also updates the tab selection in the
   * wired <code>TabLayout</code>
   */
  private void verifyTabLayoutSelection() {
    int itemCount = tabLayout.getTabCount();

    onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollToPage(0));
    assertEquals("Selected tab", 0, tabLayout.getSelectedTabPosition());
    assertEquals("Selected page", 0, viewPager.getCurrentItem());

    // Select tabs "going" to the right. Note that the first loop iteration tests the
    // scenario of "selecting" the first tab when it's already selected.
    for (int i = 0; i < itemCount; i++) {
      onView(withId(R.id.tabs)).perform(TabLayoutActions.selectTab(i));
      assertEquals("Selected tab after selecting #" + i, i, tabLayout.getSelectedTabPosition());
      assertEquals("Select tab #" + i, i, viewPager.getCurrentItem());
    }

    // Select tabs "going" to the left. Note that the first loop iteration tests the
    // scenario of "selecting" the last tab when it's already selected.
    for (int i = itemCount - 1; i >= 0; i--) {
      onView(withId(R.id.tabs)).perform(TabLayoutActions.selectTab(i));
      assertEquals("Scroll left #" + i, i, viewPager.getCurrentItem());
      assertEquals(
          "Selected tab after scrolling left #" + i, i, tabLayout.getSelectedTabPosition());
    }
  }

  @Test
  @SmallTest
  public void testBasics() {
    setupTabLayoutWithViewPager();

    final int itemCount = viewPager.getAdapter().getCount();

    assertEquals("Matching item count", itemCount, tabLayout.getTabCount());

    for (int i = 0; i < itemCount; i++) {
      assertEquals(
          "Tab #" + i, viewPager.getAdapter().getPageTitle(i), tabLayout.getTabAt(i).getText());
    }

    assertEquals("Selected tab", viewPager.getCurrentItem(), tabLayout.getSelectedTabPosition());

    verifyViewPagerSelection();
  }

    @Test
  @SmallTest
  public void testBadge() {
    setupTabLayoutWithViewPager();
    onView(withId(R.id.tabs)).perform(showBadgeOnTab(tabLayout.getTabCount() - 1, 1));
    final int itemCount = viewPager.getAdapter().getCount();
    assertEquals("Matching item count", itemCount, tabLayout.getTabCount());
    assertEquals(
        "Matching badge number",
        1,
        tabLayout.getTabAt(tabLayout.getTabCount() - 1).getBadge().getNumber());
  }

  @Test
  @SmallTest
  public void testBadgeWithAdapterContentChange() {
    setupTabLayoutWithViewPager();
    onView(withId(R.id.tabs)).perform(showBadgeOnTab(tabLayout.getTabCount() - 1, 1));
    try {
      final int itemCount = viewPager.getAdapter().getCount();
      // Remove the last entry from our adapter
      onView(withId(R.id.tabs_viewpager)).perform(removeItemAtIndexFromPager(itemCount - 1));
    } catch (NullPointerException e) {
      // App should not crash.
      throw new AssertionError(
          "Removing a tab from the view pager should not throw, but it did!", e);
    }
    assertNull("No badge is displayed", tabLayout.getTabAt(tabLayout.getTabCount() - 1).getBadge());
  }

  @Test
  @SmallTest
  public void testInteraction() {
    setupTabLayoutWithViewPager();

    assertEquals("Default selected page", 0, viewPager.getCurrentItem());
    assertEquals("Default selected tab", 0, tabLayout.getSelectedTabPosition());

    verifyTabLayoutSelection();
  }

  @Test
  @SmallTest
  public void testAdapterContentChange() {
    setupTabLayoutWithViewPager();

    // Verify that we have the expected initial adapter
    PagerAdapter initialAdapter = viewPager.getAdapter();
    assertEquals("Initial adapter class", ColorPagerAdapter.class, initialAdapter.getClass());
    assertEquals("Initial adapter page count", 3, initialAdapter.getCount());

    // Add two more entries to our adapter
    onView(withId(R.id.tabs_viewpager))
        .perform(
            addItemsToPager(
                new String[] {"Yellow", "Magenta"}, new Integer[] {Color.YELLOW, Color.MAGENTA}));

    // We have more comprehensive test coverage for changing the ViewPager adapter in v4/tests.
    // Here we are focused on testing the continuous integration of TabLayout with the new
    // content of ViewPager

    final int newItemCount = defaultPagerAdapter.getCount();
    assertEquals("Matching item count", newItemCount, tabLayout.getTabCount());

    for (int i = 0; i < newItemCount; i++) {
      assertEquals(
          "Tab #" + i, viewPager.getAdapter().getPageTitle(i), tabLayout.getTabAt(i).getText());
    }

    verifyViewPagerSelection();
    verifyTabLayoutSelection();
  }

  @Test
  @SmallTest
  public void testAdapterContentChangeWithAutoRefreshDisabled() {
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager, false));

    // Verify that we have the expected initial adapter
    PagerAdapter initialAdapter = viewPager.getAdapter();
    assertEquals("Initial adapter class", ColorPagerAdapter.class, initialAdapter.getClass());
    assertEquals("Initial adapter page count", 3, initialAdapter.getCount());

    // Add two more entries to our adapter
    onView(withId(R.id.tabs_viewpager))
        .perform(
            addItemsToPager(
                new String[] {"Yellow", "Magenta"}, new Integer[] {Color.YELLOW, Color.MAGENTA}));

    // Assert that the TabLayout did not update and add the new items
    final int newItemCount = defaultPagerAdapter.getCount();
    assertNotEquals("Matching item count", newItemCount, tabLayout.getTabCount());
  }

  @Test
  @SmallTest
  public void testBasicAutoRefreshDisabled() {
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager, false));

    // Check that the TabLayout has the same number of items are the adapter
    PagerAdapter initialAdapter = viewPager.getAdapter();
    assertEquals("Initial adapter page count", initialAdapter.getCount(), tabLayout.getTabCount());

    // Add two more entries to our adapter
    defaultPagerAdapter.add("Yellow", Color.YELLOW);
    defaultPagerAdapter.add("Magenta", Color.MAGENTA);
    final int newItemCount = defaultPagerAdapter.getCount();

    // Assert that the TabLayout did not update and add the new items
    assertNotEquals("Matching item count", newItemCount, tabLayout.getTabCount());

    // Now setup again to update the tabs
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager, false));

    // Assert that the TabLayout updated and added the new items
    assertEquals("Matching item count", newItemCount, tabLayout.getTabCount());
  }

  @Test
  @SmallTest
  public void testAdapterChange() {
    setupTabLayoutWithViewPager();

    // Verify that we have the expected initial adapter
    PagerAdapter initialAdapter = viewPager.getAdapter();
    assertEquals("Initial adapter class", ColorPagerAdapter.class, initialAdapter.getClass());
    assertEquals("Initial adapter page count", 3, initialAdapter.getCount());

    // Create a new adapter
    TextPagerAdapter newAdapter = new TextPagerAdapter();
    final int newItemCount = 6;
    for (int i = 0; i < newItemCount; i++) {
      newAdapter.add("Title " + i, "Body " + i);
    }
    // And set it on the ViewPager
    onView(withId(R.id.tabs_viewpager))
        .perform(setAdapter(newAdapter), ViewPagerActions.scrollToPage(0));

    // As TabLayout doesn't track adapter changes, we need to re-wire the new adapter
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager));

    // We have more comprehensive test coverage for changing the ViewPager adapter in v4/tests.
    // Here we are focused on testing the integration of TabLayout with the new
    // content of ViewPager

    assertEquals("Matching item count", newItemCount, tabLayout.getTabCount());

    for (int i = 0; i < newItemCount; i++) {
      assertEquals(
          "Tab #" + i, viewPager.getAdapter().getPageTitle(i), tabLayout.getTabAt(i).getText());
    }

    verifyViewPagerSelection();
    verifyTabLayoutSelection();
  }

  @Test
  @MediumTest
  public void testFixedTabMode() {
    // Create a new adapter (with no content)
    final TextPagerAdapter newAdapter = new TextPagerAdapter();
    // And set it on the ViewPager
    onView(withId(R.id.tabs_viewpager)).perform(setAdapter(newAdapter));
    // As TabLayout doesn't track adapter changes, we need to re-wire the new adapter
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager));

    // Set fixed mode on the TabLayout
    onView(withId(R.id.tabs)).perform(TabLayoutActions.setTabMode(TabLayout.MODE_FIXED));
    assertEquals("Fixed tab mode", TabLayout.MODE_FIXED, tabLayout.getTabMode());

    // Add a bunch of tabs and verify that all of them are visible on the screen
    for (int i = 0; i < 8; i++) {
      onView(withId(R.id.tabs_viewpager)).perform(addItemToPager("Title " + i, "Body " + i));

      int expectedTabCount = i + 1;
      assertEquals("Tab count after adding #" + i, expectedTabCount, tabLayout.getTabCount());
      assertEquals(
          "Page count after adding #" + i, expectedTabCount, viewPager.getAdapter().getCount());

      verifyViewPagerSelection();
      verifyTabLayoutSelection();

      // Check that all tabs are fully visible (the content may or may not be elided)
      for (int j = 0; j < expectedTabCount; j++) {
        onView(allOf(isDescendantOfA(withId(R.id.tabs)), withText("Title " + j)))
            .check(matches(isCompletelyDisplayed()));
      }
    }
  }

  /**
   * Helper method to verify support for min and max tab width on TabLayout in scrollable mode. It
   * replaces the TabLayout based on the passed layout resource ID and then adds a bunch of tab
   * titles to the wired ViewPager with progressively longer texts. After each tab is added this
   * method then checks that all tab views respect the minimum and maximum tab width set on
   * TabLayout.
   *
   * @param tabLayoutResId Layout resource for the TabLayout to be wired to the ViewPager.
   * @param tabMinWidthResId If non zero, points to the dimension resource to use for tab min width
   *     check.
   * @param tabMaxWidthResId If non zero, points to the dimension resource to use for tab max width
   *     check.
   */
  private void verifyMinMaxTabWidth(
      @LayoutRes int tabLayoutResId,
      @DimenRes int tabMinWidthResId,
      @DimenRes int tabMaxWidthResId) {
    setupTabLayoutWithViewPager();

    assertEquals("Scrollable tab mode", TabLayout.MODE_SCROLLABLE, tabLayout.getTabMode());

    final Resources res = activityTestRule.getActivity().getResources();
    final int minTabWidth =
        (tabMinWidthResId == 0) ? -1 : res.getDimensionPixelSize(tabMinWidthResId);
    final int maxTabWidth =
        (tabMaxWidthResId == 0) ? -1 : res.getDimensionPixelSize(tabMaxWidthResId);

    // Create a new adapter (with no content)
    final TextPagerAdapter newAdapter = new TextPagerAdapter();
    // And set it on the ViewPager
    onView(withId(R.id.tabs_viewpager)).perform(setAdapter(newAdapter));

    // Replace the default TabLayout with the passed one
    onView(withId(R.id.container)).perform(TestUtilsActions.replaceTabLayout(tabLayoutResId));

    // Now that we have a new TabLayout, wire it to the new content of our ViewPager
    onView(withId(R.id.tabs)).perform(setupWithViewPager(viewPager));

    // Since TabLayout doesn't expose a getter for fetching the configured max tab width,
    // start adding a variety of tabs with progressively longer tab titles and test that
    // no tab is wider than the configured max width. Before we start that test,
    // verify that we're in the scrollable mode so that each tab title gets as much width
    // as needed to display its text.
    assertEquals("Scrollable tab mode", TabLayout.MODE_SCROLLABLE, tabLayout.getTabMode());

    final StringBuilder tabTitleBuilder = new StringBuilder();
    for (int i = 0; i < 40; i++) {
      final char titleComponent = (char) ('A' + i);
      for (int j = 0; j <= (i + 1); j++) {
        tabTitleBuilder.append(titleComponent);
      }
      final String tabTitle = tabTitleBuilder.toString();
      onView(withId(R.id.tabs_viewpager)).perform(addItemToPager(tabTitle, "Body " + i));

      int expectedTabCount = i + 1;
      // Check that all tabs are at least as wide as min width *and* at most as wide as max
      // width specified in the XML for the newly loaded TabLayout
      for (int j = 0; j < expectedTabCount; j++) {
        // Find the view that is our tab title. It should be:
        // 1. Descendant of our TabLayout
        // 2. But not a direct child of the horizontal scroller
        // 3. With just-added title text
        // These conditions make sure that we're selecting the "top-level" tab view
        // instead of the inner (and narrower) TextView
        Matcher<View> tabMatcher =
            allOf(
                isDescendantOfA(withId(R.id.tabs)),
                not(withParent(isAssignableFrom(HorizontalScrollView.class))),
                hasDescendant(withText(tabTitle)));
        if (minTabWidth >= 0) {
          onView(tabMatcher).check(matches(TestUtilsMatchers.isNotNarrowerThan(minTabWidth)));
        }
        if (maxTabWidth >= 0) {
          onView(tabMatcher).check(matches(TestUtilsMatchers.isNotWiderThan(maxTabWidth)));
        }
      }

      // Reset the title builder for the next tab
      tabTitleBuilder.setLength(0);
      tabTitleBuilder.trimToSize();
    }
  }

  @Test
  @LargeTest
  public void testMinTabWidth() {
    verifyMinMaxTabWidth(R.layout.tab_layout_bound_min, R.dimen.tab_width_limit_medium, 0);
  }

  @Test
  @LargeTest
  public void testMaxTabWidth() {
    verifyMinMaxTabWidth(R.layout.tab_layout_bound_max, 0, R.dimen.tab_width_limit_medium);
  }

  @Test
  @LargeTest
  public void testMinMaxTabWidth() {
    verifyMinMaxTabWidth(
        R.layout.tab_layout_bound_minmax,
        R.dimen.tab_width_limit_small,
        R.dimen.tab_width_limit_large);
  }

  @Test
  @SmallTest
  public void testSetupAfterViewPagerScrolled() {
    // Scroll to the last item
    final int selected = viewPager.getAdapter().getCount() - 1;
    onView(withId(R.id.tabs_viewpager)).perform(ViewPagerActions.scrollToPage(selected));

    // Now setup the TabLayout with the ViewPager
    setupTabLayoutWithViewPager();

    assertEquals("Selected page", selected, viewPager.getCurrentItem());
    assertEquals("Selected tab", selected, tabLayout.getSelectedTabPosition());
  }

  @Test
  @SmallTest
  public void testEmptyAdapter() {
    ColorPagerAdapter adapter = new ColorPagerAdapter();
    onView(withId(R.id.tabs_viewpager)).perform(setAdapter(adapter));
  }
}
