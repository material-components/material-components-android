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

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.material.testutils.TabLayoutActions.selectTab;
import static com.google.android.material.testutils.TabLayoutActions.setScrollPosition;
import static com.google.android.material.testutils.TabLayoutActions.setTabMode;
import static com.google.android.material.testutils.TestUtilsActions.setLayoutDirection;
import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.os.Build;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import androidx.test.annotation.UiThreadTest;
import androidx.test.espresso.Espresso;
import androidx.test.espresso.IdlingRegistry;
import androidx.test.espresso.IdlingResource;
import androidx.test.filters.SdkSuppress;
import androidx.test.filters.SmallTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.tabs.TabLayout.Tab;
import com.google.android.material.tabs.TabLayout.TabView;
import com.google.android.material.testapp.R;
import com.google.android.material.testutils.AccessibilityUtils;
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
    assertEquals(
        activityTestRule.getActivity().getString(R.string.tab_layout_text),
        tab.getText().toString());
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
  public void testTabWithIdIsFound() throws Throwable {
    AppCompatActivity activity = activityTestRule.getActivity();
    int id = ViewCompat.generateViewId();
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            activity.setContentView(R.layout.design_tabs);
            TabLayout tabs = activity.findViewById(R.id.tabs);
            TabLayout.Tab tab = tabs.newTab().setId(id).setText("test text");
            tabs.addTab(tab);
          }
        });

    Espresso.onIdle();

    onView(withId(id)).check(matches(hasDescendant(withText(containsString("test text")))));
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

  @Test
  public void testModeAuto() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> activityTestRule.getActivity().setContentView(R.layout.design_tabs_fixed_width));
    final TabLayout tabs = activityTestRule.getActivity().findViewById(R.id.tabs);

    final TabLayoutScrollIdlingResource idler = new TabLayoutScrollIdlingResource(tabs);
    IdlingRegistry.getInstance().register(idler);

    onView(withId(R.id.tabs)).perform(setTabMode(TabLayout.MODE_AUTO));

    // Make sure tabs are scrolled all the way to the start
    onView(withId(R.id.tabs)).perform(selectTab(0));

    onView(withId(R.id.tabs))
        .check(
            (view, notFoundException) -> {
              if (!(view instanceof TabLayout)) {
                throw notFoundException;
              }

              TabLayout tabs1 = (TabLayout) view;

              assertEquals(TabLayout.MODE_AUTO, tabs1.getTabMode());
              int tabWidth = 0;
              for (int i = 0; i < tabs1.getTabCount(); i++) {
                Tab tab = tabs1.getTabAt(i);
                tabWidth += tab.view.getMeasuredWidth();
              }

              // In MODE_AUTO, the total width of tabs can exceed the width of the parent
              // TabLayout
              assertTrue(tabWidth > tabs1.getMeasuredWidth());
            });

    // Make sure tabs are scrolled all the way to the end
    onView(withId(R.id.tabs))
        .perform(selectTab(7))
        .check(
            (view, notFoundException) -> {
              if (!(view instanceof TabLayout)) {
                throw notFoundException;
              }

              assertTrue(view.getScrollX() > view.getMeasuredWidth());
            });

    IdlingRegistry.getInstance().unregister(idler);
  }

  @Test
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.M)
  public void initializesAccessibilityNodeInfo() {
    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());
    final TabLayout tabs = (TabLayout) inflater.inflate(R.layout.design_tabs, null);

    final TabLayout.Tab tab1 = tabs.newTab();
    tabs.addTab(tab1);
    final TabLayout.Tab tab2 = tabs.newTab();
    tabs.addTab(tab2, true);

    tabs.getTabAt(0).setCustomView(R.layout.design_tab_item_custom);
    tabs.getTabAt(1).setCustomView(R.layout.design_tab_item_custom);

    AccessibilityNodeInfoCompat groupInfoCompat = AccessibilityNodeInfoCompat.obtain();
    ViewCompat.onInitializeAccessibilityNodeInfo(tabs, groupInfoCompat);

    CollectionInfoCompat collectionInfo = groupInfoCompat.getCollectionInfo();
    assertEquals(2, collectionInfo.getColumnCount());
    assertEquals(1, collectionInfo.getRowCount());

    TabView secondChild = tabs.getTabAt(1).view;
    secondChild.setSelected(true);
    AccessibilityNodeInfoCompat tabInfoCompat = AccessibilityNodeInfoCompat.obtain();
    ViewCompat.onInitializeAccessibilityNodeInfo(secondChild, tabInfoCompat);

    // A tab that is currently selected won't be clickable
    assertFalse(tabInfoCompat.isClickable());
    assertFalse(
        AccessibilityUtils.hasAction(tabInfoCompat, AccessibilityActionCompat.ACTION_CLICK));

    CollectionItemInfoCompat itemInfo = tabInfoCompat.getCollectionItemInfo();
    assertEquals(1, itemInfo.getColumnIndex());
    assertEquals(0, itemInfo.getRowIndex());
    assertTrue(itemInfo.isSelected());
  }

  private void testSetScrollPosition(final boolean isLtr) throws Throwable {
    activityTestRule.runOnUiThread(
        () -> activityTestRule.getActivity().setContentView(R.layout.design_tabs_fixed_width));
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
              (view, notFoundException) -> {
                if (view == null) {
                  throw notFoundException;
                }
                // Verify increasing or decreasing scroll X values
                int sx = view.getScrollX();
                assertTrue(isLtr ? sx > lastScrollX.get() : sx < lastScrollX.get());
                lastScrollX.set(sx);
              });
    }

    Espresso.unregisterIdlingResources(idler);
  }

  /**
   * Tests that the indicator animation still functions as intended when modifying the animator's
   * update listener, instead of removing/recreating the animator itself.
   */
  @Test
  public void testIndicatorAnimator_worksAfterReplacingUpdateListener() throws Throwable {
    activityTestRule.runOnUiThread(
        () -> activityTestRule.getActivity().setContentView(R.layout.design_tabs_items));
    final TabLayout tabs = activityTestRule.getActivity().findViewById(R.id.tabs);

    onView(withId(R.id.tabs)).perform(setTabMode(TabLayout.MODE_FIXED));

    final TabLayoutScrollIdlingResource idler = new TabLayoutScrollIdlingResource(tabs);
    IdlingRegistry.getInstance().register(idler);

    // We need to click a tab once to set up the indicator animation (so that it's not still null).
    onView(withId(R.id.tabs)).perform(selectTab(1));

    // Select new tab. This action should modify the listener on the animator.
    onView(withId(R.id.tabs)).perform(selectTab(2));

    onView(withId(R.id.tabs))
        .check(
            (view, notFoundException) -> {
              if (view == null) {
                throw notFoundException;
              }

              TabLayout tabs1 = (TabLayout) view;

              int tabTwoLeft = tabs1.getTabAt(/* index= */ 2).view.getLeft();
              int tabTwoRight = tabs1.getTabAt(/* index= */ 2).view.getRight();

              assertEquals(tabs1.tabSelectedIndicator.getBounds().left, tabTwoLeft);
              assertEquals(tabs1.tabSelectedIndicator.getBounds().right, tabTwoRight);
            });

    IdlingRegistry.getInstance().unregister(idler);
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
