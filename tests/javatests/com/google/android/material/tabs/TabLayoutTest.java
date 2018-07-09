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

package com.google.android.material.tabs;

import static com.google.android.material.testutils.TabLayoutActions.selectTab;
import static com.google.android.material.testutils.TabLayoutActions.setScrollPosition;
import static com.google.android.material.testutils.TestUtilsActions.setLayoutDirection;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
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

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import com.google.android.material.testapp.R;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import android.support.test.annotation.UiThreadTest;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.espresso.NoMatchingViewException;
import android.support.test.espresso.ViewAssertion;
import android.support.test.filters.SdkSuppress;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@SmallTest
@RunWith(AndroidJUnit4.class)
public class TabLayoutTest {

  @Rule
  public final ActivityTestRule<AppCompatActivity> activityTestRule =
      new ActivityTestRule<>(AppCompatActivity.class);

  @Test
  @UiThreadTest
  public void testInflateTabLayoutWithTabItems() {
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
    final TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.design_tabs_items, null);

    assertEquals(3, tabLayout.getTabCount());

    // Tab 0 has text, but no icon or custom view
    TabLayout.Tab tab = tabLayout.getTabAt(0);
    assertEquals(activityTestRule.getActivity().getString(R.string.tab_layout_text), tab.getText());
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
      final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
      inflater.inflate(R.layout.design_tabs_with_non_tabitems, null);
    } catch (Throwable throwable) {
      assertTrue(
          throwable instanceof InflateException || throwable instanceof IllegalArgumentException);
    }
  }

  @Test
  @UiThreadTest
  public void testTabWithCustomLayoutSelection1() {
    final TabLayout.OnTabSelectedListener mockListener =
        mock(TabLayout.OnTabSelectedListener.class);
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());

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
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());

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
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
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
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
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

  @Test
  @UiThreadTest
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  @TargetApi(Build.VERSION_CODES.N)
  public void testPointerIcon() {
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
    final TabLayout tabLayout = (TabLayout) inflater.inflate(R.layout.design_tabs_items, null);
    final PointerIcon expectedIcon =
        PointerIcon.getSystemIcon(activityTestRule.getActivity(), PointerIcon.TYPE_HAND);

    final int tabCount = tabLayout.getTabCount();
    assertEquals(3, tabCount);

    final MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_HOVER_MOVE, 0, 0, 0);
    for (int i = 0; i < tabCount; i++) {
      assertEquals(expectedIcon, tabLayout.getTabAt(i).view.onResolvePointerIcon(event, 0));
    }
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

  @Test
  public void setScrollPositionLtr() throws Throwable {
    testSetScrollPosition(true);
  }

  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.JELLY_BEAN_MR1)
  @Test
  public void setScrollPositionRtl() throws Throwable {
    testSetScrollPosition(false);
  }

  private void testSetScrollPosition(final boolean isLtr) throws Throwable {
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            activityTestRule.getActivity().setContentView(R.layout.design_tabs_fixed_width);
          }
        });
    final TabLayout tabs = activityTestRule.getActivity().findViewById(R.id.tabs);
    assertEquals(TabLayout.MODE_SCROLLABLE, tabs.getTabMode());

    final TabLayoutScrollIdlingResource idler = new TabLayoutScrollIdlingResource(tabs);
    Espresso.registerIdlingResources(idler);

    // We're going to call setScrollPosition() incrementally, as if scrolling between one tab
    // and the next. Use the middle tab for best results. The positionOffsets should be in the
    // range [0, 1), so the final call will wrap to 0 but use the next tab's position.
    final int middleTab = tabs.getTabCount() / 2;
    final int[] positions = {middleTab, middleTab, middleTab, middleTab, middleTab + 1};
    final float[] positionOffsets = {0f, .25f, .5f, .75f, 0f};

    // Set layout direction
    onView(withId(R.id.tabs))
        .perform(
            setLayoutDirection(
                isLtr ? ViewCompat.LAYOUT_DIRECTION_LTR : ViewCompat.LAYOUT_DIRECTION_RTL));
    // Make sure it's scrolled all the way to the start
    onView(withId(R.id.tabs)).perform(selectTab(0));

    // Perform a series of setScrollPosition() calls
    final AtomicInteger lastScrollX = new AtomicInteger(tabs.getScrollX());
    for (int i = 0; i < positions.length; i++) {
      onView(withId(R.id.tabs))
          .perform(setScrollPosition(positions[i], positionOffsets[i]))
          .check(
              new ViewAssertion() {
                @Override
                public void check(View view, NoMatchingViewException notFoundException) {
                  if (view == null) {
                    throw notFoundException;
                  }
                  // Verify increasing or decreasing scroll X values
                  int sx = view.getScrollX();
                  assertTrue(isLtr ? sx > lastScrollX.get() : sx < lastScrollX.get());
                  lastScrollX.set(sx);
                }
              });
    }

    Espresso.unregisterIdlingResources(idler);
  }

  static class TabLayoutScrollIdlingResource implements IdlingResource {

    private boolean isIdle = true;
    private ResourceCallback callback;

    TabLayoutScrollIdlingResource(final TabLayout tabLayout) {
      tabLayout.setScrollAnimatorListener(
          new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animator) {
              setIdle(false);
            }

            @Override
            public void onAnimationEnd(Animator animator) {
              setIdle(true);
            }
          });
    }

    @Override
    public String getName() {
      return "TabLayoutScrollIdlingResource";
    }

    @Override
    public boolean isIdleNow() {
      return isIdle;
    }

    @Override
    public void registerIdleTransitionCallback(ResourceCallback callback) {
      this.callback = callback;
    }

    private void setIdle(boolean idle) {
      boolean wasIdle = isIdle;
      isIdle = idle;
      if (isIdle && !wasIdle && callback != null) {
        callback.onTransitionToIdle();
      }
    }
  }
}
