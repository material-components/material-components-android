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
package com.google.android.material.navigation;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.google.android.material.testutils.DrawerLayoutActions.closeDrawer;
import static com.google.android.material.testutils.DrawerLayoutActions.openDrawer;
import static com.google.android.material.testutils.NavigationViewActions.addHeaderView;
import static com.google.android.material.testutils.NavigationViewActions.inflateHeaderView;
import static com.google.android.material.testutils.NavigationViewActions.removeHeaderView;
import static com.google.android.material.testutils.NavigationViewActions.removeMenuItem;
import static com.google.android.material.testutils.NavigationViewActions.setCheckedItem;
import static com.google.android.material.testutils.NavigationViewActions.setIconForMenuItem;
import static com.google.android.material.testutils.NavigationViewActions.setItemBackground;
import static com.google.android.material.testutils.NavigationViewActions.setItemBackgroundResource;
import static com.google.android.material.testutils.NavigationViewActions.setItemIconTintList;
import static com.google.android.material.testutils.NavigationViewActions.setItemTextAppearance;
import static com.google.android.material.testutils.NavigationViewActions.setItemTextColor;
import static com.google.android.material.testutils.TestUtilsActions.reinflateMenu;
import static com.google.android.material.testutils.TestUtilsActions.restoreHierarchyState;
import static com.google.android.material.testutils.TestUtilsMatchers.isActionViewOf;
import static com.google.android.material.testutils.TestUtilsMatchers.isChildOfA;
import static com.google.android.material.testutils.TestUtilsMatchers.withBackgroundFill;
import static com.google.android.material.testutils.TestUtilsMatchers.withStartDrawableFilledWith;
import static com.google.android.material.testutils.TestUtilsMatchers.withTextColor;
import static com.google.android.material.testutils.TestUtilsMatchers.withTextSize;
import static org.hamcrest.core.AllOf.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.res.Resources;
import android.os.Build;
import android.os.Parcelable;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.view.GravityCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SwitchCompat;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.IdRes;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.test.espresso.matcher.ViewMatchers.Visibility;
import androidx.test.filters.MediumTest;
import androidx.test.rule.ActivityTestRule;
import androidx.test.runner.AndroidJUnit4;
import com.google.android.material.testapp.NavigationViewActivity;
import com.google.android.material.testapp.R;
import com.google.android.material.testapp.custom.NavigationTestView;
import com.google.android.material.testutils.TestDrawable;
import java.util.HashMap;
import java.util.Map;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@MediumTest
@RunWith(AndroidJUnit4.class)
public class NavigationViewTest {
  @Rule
  public final ActivityTestRule<NavigationViewActivity> activityTestRule =
      new ActivityTestRule<>(NavigationViewActivity.class);

  private static final int[] MENU_CONTENT_ITEM_IDS = {
    R.id.destination_home,
    R.id.destination_profile,
    R.id.destination_people,
    R.id.destination_settings
  };

  private Map<Integer, String> menuStringContent;
  private DrawerLayout drawerLayout;
  private NavigationTestView navigationView;

  @Before
  public void setUp() throws Exception {
    final NavigationViewActivity activity = activityTestRule.getActivity();
    drawerLayout = activity.findViewById(R.id.drawer_layout);
    navigationView = drawerLayout.findViewById(R.id.start_drawer);

    // Close the drawer to reset the state for the next test
    onView(withId(R.id.drawer_layout)).perform(closeDrawer(GravityCompat.START));

    final Resources res = activity.getResources();
    menuStringContent = new HashMap<>(MENU_CONTENT_ITEM_IDS.length);
    menuStringContent.put(R.id.destination_home, res.getString(R.string.navigate_home));
    menuStringContent.put(R.id.destination_profile, res.getString(R.string.navigate_profile));
    menuStringContent.put(R.id.destination_people, res.getString(R.string.navigate_people));
    menuStringContent.put(R.id.destination_settings, res.getString(R.string.navigate_settings));
  }

  @Test
  public void testBasics() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // Check the contents of the Menu object
    final Menu menu = navigationView.getMenu();
    assertNotNull("Menu should not be null", menu);
    assertEquals(
        "Should have matching number of items", MENU_CONTENT_ITEM_IDS.length + 1, menu.size());
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      final MenuItem currItem = menu.getItem(i);
      assertEquals("ID for Item #" + i, MENU_CONTENT_ITEM_IDS[i], currItem.getItemId());
    }

    // Check that we have the expected menu items in our NavigationView
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      onView(
              allOf(
                  withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                  isDescendantOfA(withId(R.id.start_drawer))))
          .check(matches(isDisplayed()));
    }
  }

  @Test
  public void testWillNotDraw() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    if (Build.VERSION.SDK_INT >= 21) {
      if (navigationView.hasSystemWindowInsets()) {
        assertFalse(navigationView.willNotDraw());
      } else {
        assertTrue(navigationView.willNotDraw());
      }
    } else {
      assertTrue(navigationView.willNotDraw());
    }
  }

  @Test
  public void testTextAppearance() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    final Resources res = activityTestRule.getActivity().getResources();
    final int defaultTextSize = res.getDimensionPixelSize(R.dimen.text_medium_size);

    // Check the default style of the menu items in our NavigationView
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      onView(
              allOf(
                  withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                  isDescendantOfA(withId(R.id.start_drawer))))
          .check(matches(withTextSize(defaultTextSize)));
    }

    // Set a new text appearance on our NavigationView
    onView(withId(R.id.start_drawer)).perform(setItemTextAppearance(R.style.TextSmallStyle));

    // And check that all the menu items have the new style
    final int newTextSize = res.getDimensionPixelSize(R.dimen.text_small_size);
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      onView(
              allOf(
                  withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                  isDescendantOfA(withId(R.id.start_drawer))))
          .check(matches(withTextSize(newTextSize)));
    }
  }

  @Test
  public void testTextColor() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    final Resources res = activityTestRule.getActivity().getResources();
    final @ColorInt int defaultTextColor =
        ResourcesCompat.getColor(res, R.color.emerald_text, null);

    // Check the default text color of the menu items in our NavigationView
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      onView(
              allOf(
                  withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                  isDescendantOfA(withId(R.id.start_drawer))))
          .check(matches(withTextColor(defaultTextColor)));
    }

    // Set a new text color on our NavigationView
    onView(withId(R.id.start_drawer))
        .perform(
            setItemTextColor(
                ResourcesCompat.getColorStateList(res, R.color.color_state_list_lilac, null)));

    // And check that all the menu items have the new color
    final @ColorInt int newTextColor = ResourcesCompat.getColor(res, R.color.lilac_default, null);
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      onView(
              allOf(
                  withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
                  isDescendantOfA(withId(R.id.start_drawer))))
          .check(matches(withTextColor(newTextColor)));
    }
  }

  @Test
  public void testBackground() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    final Resources res = activityTestRule.getActivity().getResources();
    final @ColorInt int defaultFillColor =
        ResourcesCompat.getColor(res, R.color.sand_default, null);

    // Check the default fill color of the menu items in our NavigationView
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      // Note that here we're tying ourselves to the implementation details of the
      // internal structure of the NavigationView. Specifically, we're looking at the
      // direct child of RecyclerView which is expected to have the background set
      // on it. If the internal implementation of NavigationView changes, the second
      // Matcher below will need to be tweaked.
      Matcher<View> menuItemMatcher =
          allOf(
              hasDescendant(withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
              isChildOfA(isAssignableFrom(RecyclerView.class)),
              isDescendantOfA(withId(R.id.start_drawer)));

      onView(menuItemMatcher).check(matches(withBackgroundFill(defaultFillColor)));
    }

    // Set a new background (flat fill color) on our NavigationView
    onView(withId(R.id.start_drawer))
        .perform(setItemBackgroundResource(R.drawable.test_background_blue));

    // And check that all the menu items have the new fill
    final @ColorInt int newFillColorBlue = ResourcesCompat.getColor(res, R.color.test_blue, null);
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      Matcher<View> menuItemMatcher =
          allOf(
              hasDescendant(withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
              isChildOfA(isAssignableFrom(RecyclerView.class)),
              isDescendantOfA(withId(R.id.start_drawer)));

      onView(menuItemMatcher).check(matches(withBackgroundFill(newFillColorBlue)));
    }

    // Set another new background on our NavigationView
    onView(withId(R.id.start_drawer))
        .perform(
            setItemBackground(
                ResourcesCompat.getDrawable(res, R.drawable.test_background_green, null)));

    // And check that all the menu items have the new fill
    final @ColorInt int newFillColorGreen = ResourcesCompat.getColor(res, R.color.test_green, null);
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      Matcher<View> menuItemMatcher =
          allOf(
              hasDescendant(withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
              isChildOfA(isAssignableFrom(RecyclerView.class)),
              isDescendantOfA(withId(R.id.start_drawer)));

      onView(menuItemMatcher).check(matches(withBackgroundFill(newFillColorGreen)));
    }
  }

  @Test
  public void testIconTinting() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    final Resources res = activityTestRule.getActivity().getResources();
    final @ColorInt int redFill = ResourcesCompat.getColor(res, R.color.test_red, null);
    final @ColorInt int greenFill = ResourcesCompat.getColor(res, R.color.test_green, null);
    final @ColorInt int blueFill = ResourcesCompat.getColor(res, R.color.test_blue, null);
    final int iconSize = res.getDimensionPixelSize(R.dimen.drawable_small_size);
    onView(withId(R.id.start_drawer))
        .perform(
            setIconForMenuItem(
                R.id.destination_home, new TestDrawable(redFill, iconSize, iconSize)));
    onView(withId(R.id.start_drawer))
        .perform(
            setIconForMenuItem(
                R.id.destination_profile, new TestDrawable(greenFill, iconSize, iconSize)));
    onView(withId(R.id.start_drawer))
        .perform(
            setIconForMenuItem(
                R.id.destination_people, new TestDrawable(blueFill, iconSize, iconSize)));

    final @ColorInt int defaultTintColor =
        ResourcesCompat.getColor(res, R.color.emerald_translucent, null);

    // We're allowing a margin of error in checking the color of the items' icons.
    // This is due to the translucent color being used in the icon tinting
    // and off-by-one discrepancies of SRC_IN when it's compositing
    // translucent color. Note that all the checks below are written for the current
    // logic on NavigationView that uses the default SRC_IN tint mode - effectively
    // replacing all non-transparent pixels in the destination (original icon) with
    // our translucent tint color.
    final int allowedComponentVariance = 1;

    // Note that here we're tying ourselves to the implementation details of the
    // internal structure of the NavigationView. Specifically, we're checking the
    // start drawable of the text view with the specific text. If the internal
    // implementation of NavigationView changes, the second Matcher in the lookups
    // below will need to be tweaked.
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(defaultTintColor, allowedComponentVariance)));

    final @ColorInt int newTintColor = ResourcesCompat.getColor(res, R.color.red_translucent, null);

    onView(withId(R.id.start_drawer))
        .perform(
            setItemIconTintList(
                ResourcesCompat.getColorStateList(
                    res, R.color.color_state_list_red_translucent, null)));
    // Check that all menu items with icons now have icons tinted with the newly set color
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(newTintColor, allowedComponentVariance)));

    // And now remove all icon tinting
    onView(withId(R.id.start_drawer)).perform(setItemIconTintList(null));
    // And verify that all menu items with icons now have the original colors for their icons.
    // Note that since there is no tinting at this point, we don't allow any color variance
    // in these checks.
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_home)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(redFill, 0)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(greenFill, 0)));
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .check(matches(withStartDrawableFilledWith(blueFill, 0)));
  }

  /**
   * Gets the list of header IDs (which can be empty) and verifies that the actual header content of
   * our navigation view matches the expected header content.
   */
  private void verifyHeaders(@IdRes int... expectedHeaderIds) {
    final int expectedHeaderCount = (expectedHeaderIds != null) ? expectedHeaderIds.length : 0;
    final int actualHeaderCount = navigationView.getHeaderCount();
    assertEquals("Header count", expectedHeaderCount, actualHeaderCount);

    if (expectedHeaderCount > 0) {
      for (int i = 0; i < expectedHeaderCount; i++) {
        final View currentHeader = navigationView.getHeaderView(i);
        assertEquals("Header at #" + i, expectedHeaderIds[i], currentHeader.getId());
      }
    }
  }

  @Test
  public void testHeaders() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // We should have no headers at the start
    verifyHeaders();

    // Inflate one header and check that it's there in the navigation view
    onView(withId(R.id.start_drawer))
        .perform(inflateHeaderView(R.layout.design_navigation_view_header1));
    verifyHeaders(R.id.header1);

    final LayoutInflater inflater = LayoutInflater.from(activityTestRule.getActivity());

    // Add one more header and check that it's there in the navigation view
    onView(withId(R.id.start_drawer))
        .perform(addHeaderView(inflater, R.layout.design_navigation_view_header2));
    verifyHeaders(R.id.header1, R.id.header2);

    final View header1 = navigationView.findViewById(R.id.header1);
    // Remove the first header and check that we still have the second header
    onView(withId(R.id.start_drawer)).perform(removeHeaderView(header1));
    verifyHeaders(R.id.header2);

    // Add one more header and check that we now have two headers
    onView(withId(R.id.start_drawer))
        .perform(inflateHeaderView(R.layout.design_navigation_view_header3));
    verifyHeaders(R.id.header2, R.id.header3);

    // Add another "copy" of the header from the just-added layout and check that we now
    // have three headers
    onView(withId(R.id.start_drawer))
        .perform(addHeaderView(inflater, R.layout.design_navigation_view_header3));
    verifyHeaders(R.id.header2, R.id.header3, R.id.header3);
  }

  @Test
  public void testHeaderState() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // Inflate a header with a toggle switch and check that it's there in the navigation view
    onView(withId(R.id.start_drawer))
        .perform(inflateHeaderView(R.layout.design_navigation_view_header_switch));
    verifyHeaders(R.id.header_frame);

    onView(withId(R.id.header_toggle))
        .check(matches(isNotChecked()))
        .perform(click())
        .check(matches(isChecked()));

    // Save the current state
    SparseArray<Parcelable> container = new SparseArray<>();
    navigationView.saveHierarchyState(container);

    // Remove the header
    final View header = navigationView.findViewById(R.id.header_frame);
    onView(withId(R.id.start_drawer)).perform(removeHeaderView(header));
    verifyHeaders();

    // Inflate the header again
    onView(withId(R.id.start_drawer))
        .perform(inflateHeaderView(R.layout.design_navigation_view_header_switch));
    verifyHeaders(R.id.header_frame);

    // Restore the saved state
    onView(withId(R.id.start_drawer)).perform(restoreHierarchyState(container));

    // Confirm that the state was restored
    onView(withId(R.id.header_toggle)).check(matches(isChecked()));
  }

  @Test
  public void testActionViewState() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    final Menu menu = navigationView.getMenu();
    onView(isActionViewOf(menu, R.id.destination_people))
        .check(matches(isNotChecked())) // Not checked by default
        .perform(click()) // Check it
        .check(matches(isChecked()));

    // Remove the other action view to simulate the case where it is not yet inflated
    onView(isActionViewOf(menu, R.id.destination_custom)).check(matches(isDisplayed()));
    onView(withId(R.id.start_drawer)).perform(removeMenuItem(R.id.destination_custom));

    // Save the current state
    SparseArray<Parcelable> container = new SparseArray<>();
    navigationView.saveHierarchyState(container);

    // Restore the saved state
    onView(withId(R.id.start_drawer))
        .perform(reinflateMenu(R.menu.navigation_view_content))
        .perform(restoreHierarchyState(container));

    // Checked state should be restored
    onView(isActionViewOf(menu, R.id.destination_people)).check(matches(isChecked()));
  }

  @Test
  public void testNavigationSelectionListener() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // Click one of our items
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .perform(click());
    // Check that the drawer is still open
    assertTrue("Drawer is still open after click", drawerLayout.isDrawerOpen(GravityCompat.START));

    // Register a listener
    NavigationView.OnNavigationItemSelectedListener mockedListener =
        mock(NavigationView.OnNavigationItemSelectedListener.class);
    navigationView.setNavigationItemSelectedListener(mockedListener);

    // Click one of our items
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_profile)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .perform(click());
    // Check that the drawer is still open
    assertTrue("Drawer is still open after click", drawerLayout.isDrawerOpen(GravityCompat.START));
    // And that our listener has been notified of the click
    verify(mockedListener, times(1))
        .onNavigationItemSelected(navigationView.getMenu().findItem(R.id.destination_profile));

    // Set null listener to test that the next click is not going to notify the
    // previously set listener
    navigationView.setNavigationItemSelectedListener(null);

    // Click one of our items
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_settings)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .perform(click());
    // Check that the drawer is still open
    assertTrue("Drawer is still open after click", drawerLayout.isDrawerOpen(GravityCompat.START));
    // And that our previous listener has not been notified of the click
    verifyNoMoreInteractions(mockedListener);
  }

  private void verifyCheckedAppearance(
      @IdRes int checkedItemId,
      @ColorInt int uncheckedItemForeground,
      @ColorInt int checkedItemForeground,
      @ColorInt int uncheckedItemBackground,
      @ColorInt int checkedItemBackground) {
    for (int i = 0; i < MENU_CONTENT_ITEM_IDS.length; i++) {
      final boolean expectedToBeChecked = (MENU_CONTENT_ITEM_IDS[i] == checkedItemId);
      final @ColorInt int expectedItemForeground =
          expectedToBeChecked ? checkedItemForeground : uncheckedItemForeground;
      final @ColorInt int expectedItemBackground =
          expectedToBeChecked ? checkedItemBackground : uncheckedItemBackground;

      // For the background fill check we need to select a view that has its background
      // set by the current implementation (see disclaimer in testBackground)
      Matcher<View> menuItemMatcher =
          allOf(
              hasDescendant(withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i]))),
              isChildOfA(isAssignableFrom(RecyclerView.class)),
              isDescendantOfA(withId(R.id.start_drawer)));
      onView(menuItemMatcher).check(matches(withBackgroundFill(expectedItemBackground)));

      // And for the foreground color check we need to select a view with the text content
      Matcher<View> menuItemTextMatcher =
          allOf(
              withText(menuStringContent.get(MENU_CONTENT_ITEM_IDS[i])),
              isDescendantOfA(withId(R.id.start_drawer)));
      onView(menuItemTextMatcher).check(matches(withTextColor(expectedItemForeground)));
    }
  }

  @Test
  public void testCheckedAppearance() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // Reconfigure our navigation view to use foreground (text) and background visuals
    // with explicitly different colors for the checked state
    final Resources res = activityTestRule.getActivity().getResources();
    onView(withId(R.id.start_drawer))
        .perform(
            setItemTextColor(
                ResourcesCompat.getColorStateList(res, R.color.color_state_list_sand, null)));
    onView(withId(R.id.start_drawer))
        .perform(setItemBackgroundResource(R.drawable.test_drawable_state_list));

    final @ColorInt int uncheckedItemForeground =
        ResourcesCompat.getColor(res, R.color.sand_default, null);
    final @ColorInt int checkedItemForeground =
        ResourcesCompat.getColor(res, R.color.sand_checked, null);
    final @ColorInt int uncheckedItemBackground =
        ResourcesCompat.getColor(res, R.color.test_green, null);
    final @ColorInt int checkedItemBackground =
        ResourcesCompat.getColor(res, R.color.test_blue, null);

    // Verify that all items are rendered with unchecked visuals
    verifyCheckedAppearance(
        0,
        uncheckedItemForeground,
        checkedItemForeground,
        uncheckedItemBackground,
        checkedItemBackground);

    // Mark one of the items as checked
    onView(withId(R.id.start_drawer)).perform(setCheckedItem(R.id.destination_profile));
    // And verify that it's now rendered with checked visuals
    verifyCheckedAppearance(
        R.id.destination_profile,
        uncheckedItemForeground,
        checkedItemForeground,
        uncheckedItemBackground,
        checkedItemBackground);

    // Register a navigation listener that "marks" the selected item
    navigationView.setNavigationItemSelectedListener(item -> true);

    // Click one of our items
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_people)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .perform(click());
    // and verify that it's now checked
    verifyCheckedAppearance(
        R.id.destination_people,
        uncheckedItemForeground,
        checkedItemForeground,
        uncheckedItemBackground,
        checkedItemBackground);

    // Register a navigation listener that doesn't "mark" the selected item
    navigationView.setNavigationItemSelectedListener(item -> false);

    // Click another items
    onView(
            allOf(
                withText(menuStringContent.get(R.id.destination_settings)),
                isDescendantOfA(withId(R.id.start_drawer))))
        .perform(click());
    // and verify that the checked state remains on the previously clicked item
    // since the current navigation listener returns false from its callback
    // implementation
    verifyCheckedAppearance(
        R.id.destination_people,
        uncheckedItemForeground,
        checkedItemForeground,
        uncheckedItemBackground,
        checkedItemBackground);
  }

  @Test
  public void testActionLayout() {
    // Open our drawer
    onView(withId(R.id.drawer_layout)).perform(openDrawer(GravityCompat.START));

    // There are four conditions to "find" the menu item with action layout (switch):
    // 1. Is in the NavigationView
    // 2. Is direct child of a class that extends RecyclerView
    // 3. Has a child with "people" text
    // 4. Has fully displayed child that extends SwitchCompat
    // Note that condition 2 makes a certain assumption about the internal implementation
    // details of the NavigationMenu, while conditions 3 and 4 aim to be as generic as
    // possible and to not rely on the internal details of the current layout implementation
    // of an individual menu item in NavigationMenu.
    Matcher<View> menuItemMatcher =
        allOf(
            isDescendantOfA(withId(R.id.start_drawer)),
            isChildOfA(isAssignableFrom(RecyclerView.class)),
            hasDescendant(withText(menuStringContent.get(R.id.destination_people))),
            hasDescendant(allOf(isAssignableFrom(SwitchCompat.class), isCompletelyDisplayed())));

    // While we don't need to perform any action on our row, the invocation of perform()
    // makes our matcher actually run. If for some reason NavigationView fails to inflate and
    // display our SwitchCompat action layout, the next line will fail in the matcher pass.
    onView(menuItemMatcher).perform(click());

    // Check that the full custom view is displayed without title and icon.
    final Resources res = activityTestRule.getActivity().getResources();
    Matcher<View> customItemMatcher =
        allOf(
            isDescendantOfA(withId(R.id.start_drawer)),
            isChildOfA(isAssignableFrom(RecyclerView.class)),
            hasDescendant(withText(res.getString(R.string.navigate_custom))),
            hasDescendant(
                allOf(isAssignableFrom(TextView.class), withEffectiveVisibility(Visibility.GONE))));
    onView(customItemMatcher).perform(click());
  }
}
