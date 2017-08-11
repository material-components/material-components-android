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

import static android.support.design.testutils.BottomNavigationViewActions.setIconForMenuItem;
import static android.support.design.testutils.BottomNavigationViewActions.setItemIconTintList;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.design.testapp.BottomNavigationViewActivity;
import android.support.design.testapp.R;
import android.support.design.testutils.TestDrawable;
import android.support.design.testutils.TestUtilsMatchers;
import android.support.test.annotation.UiThreadTest;
import android.support.test.filters.LargeTest;
import android.support.test.filters.SdkSuppress;
import android.support.test.filters.SmallTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.PointerIcon;
import android.view.View;
import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class BottomNavigationViewTest {
  @Rule
  public final ActivityTestRule<BottomNavigationViewActivity> activityTestRule =
      new ActivityTestRule<>(BottomNavigationViewActivity.class);

  private static final int[] MENU_CONTENT_ITEM_IDS = {
    R.id.destination_home, R.id.destination_profile, R.id.destination_people
  };
  private Map<Integer, String> mMenuStringContent;

  private BottomNavigationView mBottomNavigation;

  @Before
  public void setUp() throws Exception {
    final BottomNavigationViewActivity activity = activityTestRule.getActivity();
    mBottomNavigation = activity.findViewById(R.id.bottom_navigation);

    final Resources res = activity.getResources();
    mMenuStringContent = new HashMap<>(MENU_CONTENT_ITEM_IDS.length);
    mMenuStringContent.put(R.id.destination_home, res.getString(R.string.navigate_home));
    mMenuStringContent.put(R.id.destination_profile, res.getString(R.string.navigate_profile));
    mMenuStringContent.put(R.id.destination_people, res.getString(R.string.navigate_people));
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testAddItemsWithoutMenuInflation() {
    BottomNavigationView navigation = new BottomNavigationView(activityTestRule.getActivity());
    activityTestRule.getActivity().setContentView(navigation);
    navigation.getMenu().add("Item1");
    navigation.getMenu().add("Item2");
    assertEquals(2, navigation.getMenu().size());
    navigation.getMenu().removeItem(0);
    navigation.getMenu().removeItem(0);
    assertEquals(0, navigation.getMenu().size());
  }

  @Test
  @SmallTest
  public void testBasics() {
    // Check the contents of the Menu object
    final Menu menu = mBottomNavigation.getMenu();
    assertNotNull("Menu should not be null", menu);
    assertEquals("Should have matching number of items", MENU_CONTENT_ITEM_IDS.length, menu.size());
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      final MenuItem currItem = menu.getItem(i);
      assertEquals("ID for Item #" + i, MENU_CONTENT_ITEM_IDS[i], currItem.getItemId());
    }
  }

  @Test
  @LargeTest
  public void testNavigationSelectionListener() {
    BottomNavigationView.OnNavigationItemSelectedListener mockedListener =
        mock(BottomNavigationView.OnNavigationItemSelectedListener.class);
    mBottomNavigation.setOnNavigationItemSelectedListener(mockedListener);

    // Make the listener return true to allow selecting the item.
    when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(true);
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify our listener has been notified of the click
    verify(mockedListener, times(1))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_profile));
    // Verify the item is now selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Select the same item again
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify our listener has been notified of the click
    verify(mockedListener, times(2))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_profile));
    // Verify the item is still selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Make the listener return false to disallow selecting the item.
    when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(false);
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify our listener has been notified of the click
    verify(mockedListener, times(1))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_people));
    // Verify the previous item is still selected
    assertFalse(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Set null listener to test that the next click is not going to notify the
    // previously set listener and will allow selecting items.
    mBottomNavigation.setOnNavigationItemSelectedListener(null);

    // Click one of our items
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify that our previous listener has not been notified of the click
    verifyNoMoreInteractions(mockedListener);
    // Verify the correct item is now selected.
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_home).isChecked());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testSetSelectedItemId() {
    BottomNavigationView.OnNavigationItemSelectedListener mockedListener =
        mock(BottomNavigationView.OnNavigationItemSelectedListener.class);
    mBottomNavigation.setOnNavigationItemSelectedListener(mockedListener);

    // Make the listener return true to allow selecting the item.
    when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(true);
    // Programmatically select an item
    mBottomNavigation.setSelectedItemId(R.id.destination_profile);
    // Verify our listener has been notified of the click
    verify(mockedListener, times(1))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_profile));
    // Verify the item is now selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Select the same item
    mBottomNavigation.setSelectedItemId(R.id.destination_profile);
    // Verify our listener has been notified of the click
    verify(mockedListener, times(2))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_profile));
    // Verify the item is still selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Make the listener return false to disallow selecting the item.
    when(mockedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(false);
    // Programmatically select an item
    mBottomNavigation.setSelectedItemId(R.id.destination_people);
    // Verify our listener has been notified of the click
    verify(mockedListener, times(1))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_people));
    // Verify the previous item is still selected
    assertFalse(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());

    // Set null listener to test that the next click is not going to notify the
    // previously set listener and will allow selecting items.
    mBottomNavigation.setOnNavigationItemSelectedListener(null);

    // Select one of our items
    mBottomNavigation.setSelectedItemId(R.id.destination_home);
    // Verify that our previous listener has not been notified of the click
    verifyNoMoreInteractions(mockedListener);
    // Verify the correct item is now selected.
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_home).isChecked());
  }

  @Test
  @SmallTest
  public void testNavigationReselectionListener() {
    // Add an OnNavigationItemReselectedListener
    BottomNavigationView.OnNavigationItemReselectedListener reselectedListener =
        mock(BottomNavigationView.OnNavigationItemReselectedListener.class);
    mBottomNavigation.setOnNavigationItemReselectedListener(reselectedListener);

    // Select an item
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify the item is now selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());
    // Verify the listener was not called
    verify(reselectedListener, never()).onNavigationItemReselected(any(MenuItem.class));

    // Select the same item again
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify the item is still selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());
    // Verify the listener was called
    verify(reselectedListener, times(1))
        .onNavigationItemReselected(mBottomNavigation.getMenu().findItem(R.id.destination_profile));

    // Add an OnNavigationItemSelectedListener
    BottomNavigationView.OnNavigationItemSelectedListener selectedListener =
        mock(BottomNavigationView.OnNavigationItemSelectedListener.class);
    mBottomNavigation.setOnNavigationItemSelectedListener(selectedListener);
    // Make the listener return true to allow selecting the item.
    when(selectedListener.onNavigationItemSelected(any(MenuItem.class))).thenReturn(true);

    // Select another item
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify the item is now selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
    // Verify the correct listeners were called
    verify(selectedListener, times(1))
        .onNavigationItemSelected(mBottomNavigation.getMenu().findItem(R.id.destination_people));
    verify(reselectedListener, never())
        .onNavigationItemReselected(mBottomNavigation.getMenu().findItem(R.id.destination_people));

    // Select the same item again
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify the item is still selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
    // Verify the correct listeners were called
    verifyNoMoreInteractions(selectedListener);
    verify(reselectedListener, times(1))
        .onNavigationItemReselected(mBottomNavigation.getMenu().findItem(R.id.destination_people));

    // Remove the OnNavigationItemReselectedListener
    mBottomNavigation.setOnNavigationItemReselectedListener(null);

    // Select the same item again
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    // Verify the item is still selected
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_people).isChecked());
    // Verify the reselectedListener was not called
    verifyNoMoreInteractions(reselectedListener);
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testSelectedItemIdWithEmptyMenu() {
    // First item initially selected
    assertEquals(R.id.destination_home, mBottomNavigation.getSelectedItemId());

    // Remove all the items
    for (int id : mMenuStringContent.keySet()) {
      mBottomNavigation.getMenu().removeItem(id);
    }
    // Verify selected ID is zero
    assertEquals(0, mBottomNavigation.getSelectedItemId());

    // Add an item
    mBottomNavigation.getMenu().add(0, R.id.destination_home, 0, R.string.navigate_home);
    // Verify item is selected
    assertEquals(R.id.destination_home, mBottomNavigation.getSelectedItemId());

    // Try selecting an invalid ID
    mBottomNavigation.setSelectedItemId(R.id.destination_people);
    // Verify the view has not changed
    assertEquals(R.id.destination_home, mBottomNavigation.getSelectedItemId());
  }

  @Test
  @SmallTest
  public void testIconTinting() {
    final Resources res = activityTestRule.getActivity().getResources();
    @ColorInt final int redFill = ResourcesCompat.getColor(res, R.color.test_red, null);
    @ColorInt final int greenFill = ResourcesCompat.getColor(res, R.color.test_green, null);
    @ColorInt final int blueFill = ResourcesCompat.getColor(res, R.color.test_blue, null);
    final int iconSize = res.getDimensionPixelSize(R.dimen.drawable_small_size);
    onView(withId(R.id.bottom_navigation))
        .perform(
            setIconForMenuItem(
                R.id.destination_home, new TestDrawable(redFill, iconSize, iconSize)));
    onView(withId(R.id.bottom_navigation))
        .perform(
            setIconForMenuItem(
                R.id.destination_profile, new TestDrawable(greenFill, iconSize, iconSize)));
    onView(withId(R.id.bottom_navigation))
        .perform(
            setIconForMenuItem(
                R.id.destination_people, new TestDrawable(blueFill, iconSize, iconSize)));

    @ColorInt
    final int defaultTintColor = ResourcesCompat.getColor(res, R.color.emerald_translucent, null);

    // We're allowing a margin of error in checking the color of the items' icons.
    // This is due to the translucent color being used in the icon tinting
    // and off-by-one discrepancies of SRC_IN when it's compositing
    // translucent color. Note that all the checks below are written for the current
    // logic on BottomNavigationView that uses the default SRC_IN tint mode - effectively
    // replacing all non-transparent pixels in the destination (original icon) with
    // our translucent tint color.
    final int allowedComponentVariance = 1;

    // Note that here we're tying ourselves to the implementation details of the internal
    // structure of the BottomNavigationView. Specifically, we're checking the drawable the
    // ImageView with id R.id.icon. If the internal implementation of BottomNavigationView
    // changes, the second Matcher in the lookups below will need to be tweaked.
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home))))
        .check(matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile))))
        .check(matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people))))
        .check(matches(TestUtilsMatchers.drawable(defaultTintColor, allowedComponentVariance)));

    @ColorInt final int newTintColor = ResourcesCompat.getColor(res, R.color.red_translucent, null);
    onView(withId(R.id.bottom_navigation))
        .perform(
            setItemIconTintList(
                ResourcesCompat.getColorStateList(
                    res, R.color.color_state_list_red_translucent, null)));
    // Check that all menu items with icons now have icons tinted with the newly set color
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home))))
        .check(matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile))))
        .check(matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people))))
        .check(matches(TestUtilsMatchers.drawable(newTintColor, allowedComponentVariance)));

    // And now remove all icon tinting
    onView(withId(R.id.bottom_navigation)).perform(setItemIconTintList(null));
    // And verify that all menu items with icons now have the original colors for their icons.
    // Note that since there is no tinting at this point, we don't allow any color variance
    // in these checks.
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_home))))
        .check(matches(TestUtilsMatchers.drawable(redFill, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_profile))))
        .check(matches(TestUtilsMatchers.drawable(greenFill, allowedComponentVariance)));
    onView(allOf(withId(R.id.icon), isDescendantOfA(withId(R.id.destination_people))))
        .check(matches(TestUtilsMatchers.drawable(blueFill, allowedComponentVariance)));
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testItemChecking() throws Throwable {
    final Menu menu = mBottomNavigation.getMenu();
    assertTrue(menu.getItem(0).isChecked());
    checkAndVerifyExclusiveItem(menu, R.id.destination_home);
    checkAndVerifyExclusiveItem(menu, R.id.destination_profile);
    checkAndVerifyExclusiveItem(menu, R.id.destination_people);
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testForcedShiftingItemChecking() throws Throwable {
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_ON);
    final Menu menu = mBottomNavigation.getMenu();
    assertTrue(menu.getItem(0).isChecked());
    checkAndVerifyExclusiveItem(menu, R.id.destination_home);
    checkAndVerifyExclusiveItem(menu, R.id.destination_profile);
    checkAndVerifyExclusiveItem(menu, R.id.destination_people);
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testAutoShiftingItemChecking() throws Throwable {
    mBottomNavigation.getMenu().clear();
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_AUTO);
    mBottomNavigation.inflateMenu(R.menu.bottom_navigation_view_shifting_content);
    final Menu menu = mBottomNavigation.getMenu();
    assertTrue(menu.getItem(0).isChecked());
    checkAndVerifyExclusiveItem(menu, R.id.destination_home);
    checkAndVerifyExclusiveItem(menu, R.id.destination_profile);
    checkAndVerifyExclusiveItem(menu, R.id.destination_people);
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testClearingMenu() throws Throwable {
    mBottomNavigation.getMenu().clear();
    assertEquals(0, mBottomNavigation.getMenu().size());
    mBottomNavigation.inflateMenu(R.menu.bottom_navigation_view_content);
    assertEquals(3, mBottomNavigation.getMenu().size());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testClearingForcedShiftingMenu() throws Throwable {
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_ON);
    mBottomNavigation.getMenu().clear();
    assertEquals(0, mBottomNavigation.getMenu().size());
    mBottomNavigation.inflateMenu(R.menu.bottom_navigation_view_content);
    assertEquals(3, mBottomNavigation.getMenu().size());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testClearingAutoShiftingMenu() throws Throwable {
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_AUTO);
    mBottomNavigation.getMenu().clear();
    assertEquals(0, mBottomNavigation.getMenu().size());
    mBottomNavigation.inflateMenu(R.menu.bottom_navigation_view_shifting_content);
    assertEquals(4, mBottomNavigation.getMenu().size());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testSettingMenuItemVisibility() throws Throwable {
    final MenuItem homeMenuItem = mBottomNavigation.getMenu().findItem(R.id.destination_home);
    assertTrue(homeMenuItem.isVisible());
    homeMenuItem.setVisible(false);
    assertFalse(homeMenuItem.isVisible());

    mBottomNavigation.getMenu().clear();
    mBottomNavigation.inflateMenu(R.menu.bottom_navigation_view_with_invisible_button_content);
    assertEquals(3, mBottomNavigation.getMenu().size());

    final MenuItem destinationMenuItem =
        mBottomNavigation.getMenu().findItem(R.id.destination_profile);
    assertFalse(destinationMenuItem.isVisible());
    destinationMenuItem.setVisible(true);
    assertTrue(destinationMenuItem.isVisible());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testSettingForcedShiftingMenuItemVisibility() throws Throwable {
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_ON);
    final MenuItem homeMenuItem = mBottomNavigation.getMenu().findItem(R.id.destination_home);
    assertTrue(homeMenuItem.isVisible());
    homeMenuItem.setVisible(false);
    assertFalse(homeMenuItem.isVisible());
  }

  @UiThreadTest
  @Test
  @SmallTest
  public void testSettingAutoShiftingMenuItemVisibility() throws Throwable {
    mBottomNavigation.getMenu().clear();
    mBottomNavigation.setShiftingMode(BottomNavigationView.SHIFTING_MODE_AUTO);
    mBottomNavigation.inflateMenu(
        R.menu.bottom_navigation_view_shifting_with_invisible_button_content);
    assertEquals(4, mBottomNavigation.getMenu().size());

    final MenuItem destinationMenuItem =
        mBottomNavigation.getMenu().findItem(R.id.destination_profile);
    assertFalse(destinationMenuItem.isVisible());
    destinationMenuItem.setVisible(true);
    assertTrue(destinationMenuItem.isVisible());
  }

  @Test
  @SmallTest
  public void testSavedState() throws Throwable {
    // Select an item other than the first
    onView(
            allOf(
                withText(mMenuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.bottom_navigation)),
                isDisplayed()))
        .perform(click());
    assertTrue(mBottomNavigation.getMenu().findItem(R.id.destination_profile).isChecked());
    // Save the state
    final Parcelable state = mBottomNavigation.onSaveInstanceState();

    // Restore the state into a fresh BottomNavigationView
    activityTestRule.runOnUiThread(
        new Runnable() {
          @Override
          public void run() {
            BottomNavigationView testView =
                new BottomNavigationView(activityTestRule.getActivity());
            testView.inflateMenu(R.menu.bottom_navigation_view_content);
            testView.onRestoreInstanceState(state);
            assertTrue(testView.getMenu().findItem(R.id.destination_profile).isChecked());
          }
        });
  }

  @UiThreadTest
  @Test
  @SmallTest
  @SdkSuppress(minSdkVersion = Build.VERSION_CODES.N)
  @TargetApi(Build.VERSION_CODES.N)
  public void testPointerIcon() throws Throwable {
    final Activity activity = activityTestRule.getActivity();
    final PointerIcon expectedIcon = PointerIcon.getSystemIcon(activity, PointerIcon.TYPE_HAND);
    final MotionEvent event = MotionEvent.obtain(0, 0, MotionEvent.ACTION_HOVER_MOVE, 0, 0, 0);
    final Menu menu = mBottomNavigation.getMenu();
    for (int i = 0; i < menu.size(); i++) {
      final MenuItem item = menu.getItem(i);
      assertTrue(item.isEnabled());
      final View itemView = activity.findViewById(item.getItemId());
      assertEquals(expectedIcon, itemView.onResolvePointerIcon(event, 0));
      item.setEnabled(false);
      assertEquals(null, itemView.onResolvePointerIcon(event, 0));
      item.setEnabled(true);
      assertEquals(expectedIcon, itemView.onResolvePointerIcon(event, 0));
    }
  }

  private void checkAndVerifyExclusiveItem(final Menu menu, final int id) throws Throwable {
    menu.findItem(id).setChecked(true);
    for (int i = 0; i < menu.size(); i++) {
      final MenuItem item = menu.getItem(i);
      if (item.getItemId() == id) {
        assertTrue(item.isChecked());
      } else {
        assertFalse(item.isChecked());
      }
    }
  }
}
