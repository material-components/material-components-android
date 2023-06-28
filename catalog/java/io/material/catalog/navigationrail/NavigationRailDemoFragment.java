/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.navigationrail;

import io.material.catalog.R;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeDrawable.BadgeGravity;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener;
import com.google.android.material.navigationrail.NavigationRailView;
import io.material.catalog.feature.DemoFragment;

/** A base class that provides a demo screen structure for a single navigation rail demo. */
public class NavigationRailDemoFragment extends DemoFragment {

  private static final int MAX_NAVIGATION_RAIL_CHILDREN = 7;

  @Nullable
  private final int[] badgeGravityValues =
      new int[] {
        BadgeDrawable.TOP_END,
        BadgeDrawable.TOP_START,
      };

  private int numVisibleChildren = 3;
  @Nullable NavigationRailView navigationRailView;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_navigation_rail_fragment, viewGroup, false /* attachToRoot */);
    initNavigationRail(getContext(), view);
    initNavigationRailDemoControls(view);

    OnItemSelectedListener navigationItemListener =
        item -> {
          handleAllNavigationRailSelections(item.getItemId());

          TextView page1Text = view.findViewById(R.id.page_1);
          TextView page2Text = view.findViewById(R.id.page_2);
          TextView page3Text = view.findViewById(R.id.page_3);
          TextView page4Text = view.findViewById(R.id.page_4);
          TextView page5Text = view.findViewById(R.id.page_5);
          TextView page6Text = view.findViewById(R.id.page_6);
          TextView page7Text = view.findViewById(R.id.page_7);

          int itemId = item.getItemId();
          page1Text.setVisibility(itemId == R.id.action_page_1 ? View.VISIBLE : View.GONE);
          page2Text.setVisibility(itemId == R.id.action_page_2 ? View.VISIBLE : View.GONE);
          page3Text.setVisibility(itemId == R.id.action_page_3 ? View.VISIBLE : View.GONE);
          page4Text.setVisibility(itemId == R.id.action_page_4 ? View.VISIBLE : View.GONE);
          page5Text.setVisibility(itemId == R.id.action_page_5 ? View.VISIBLE : View.GONE);
          page6Text.setVisibility(itemId == R.id.action_page_6 ? View.VISIBLE : View.GONE);
          page7Text.setVisibility(itemId == R.id.action_page_7 ? View.VISIBLE : View.GONE);

          clearAndHideBadge(item.getItemId());
          return false;
        };
    setNavigationRailListeners(navigationItemListener);
    if (bundle == null) {
      setupBadging();
    }
    return view;
  }

  private void setupBadging() {
    int menuItemId = navigationRailView.getMenu().getItem(0).getItemId();
    // An icon only badge will be displayed.
    BadgeDrawable badge = navigationRailView.getOrCreateBadge(menuItemId);
    badge.setVisible(true);

    menuItemId = navigationRailView.getMenu().getItem(1).getItemId();
    // A badge with the text "99" will be displayed.
    badge = navigationRailView.getOrCreateBadge(menuItemId);
    badge.setVisible(true);
    badge.setNumber(99);

    menuItemId = navigationRailView.getMenu().getItem(2).getItemId();
    // A badge with the text "999+" will be displayed.
    badge = navigationRailView.getOrCreateBadge(menuItemId);
    badge.setVisible(true);
    badge.setNumber(9999);
  }

  private void updateBadgeNumber(int delta) {
    // Increase the badge number on the first menu item.
    MenuItem menuItem = navigationRailView.getMenu().getItem(0);
    int menuItemId = menuItem.getItemId();
    BadgeDrawable badgeDrawable = navigationRailView.getOrCreateBadge(menuItemId);
    // In case the first menu item has been selected and the badge was hidden, call
    // BadgeDrawable#setVisible() to ensure the badge is visible.
    badgeDrawable.setVisible(true);
    badgeDrawable.setNumber(badgeDrawable.getNumber() + delta);
  }

  private void updateBadgeGravity(@BadgeGravity int badgeGravity) {
    for (int i = 0; i < navigationRailView.getMenu().size(); i++) {
      // Update the badge gravity on all the menu items.
      MenuItem menuItem = navigationRailView.getMenu().getItem(i);
      int menuItemId = menuItem.getItemId();
      BadgeDrawable badgeDrawable = navigationRailView.getBadge(menuItemId);
      if (badgeDrawable != null) {
        badgeDrawable.setBadgeGravity(badgeGravity);
      }
    }
  }

  private void clearAndHideBadge(int menuItemId) {
    MenuItem menuItem = navigationRailView.getMenu().getItem(0);
    if (menuItem.getItemId() == menuItemId) {
      // Hide instead of removing the badge associated with the first menu item because the user
      // can trigger it to be displayed again.
      BadgeDrawable badgeDrawable = navigationRailView.getBadge(menuItemId);
      if (badgeDrawable != null) {
        badgeDrawable.setVisible(false);
        badgeDrawable.clearNumber();
      }
    } else {
      // Remove the badge associated with this menu item because cannot be displayed again.
      navigationRailView.removeBadge(menuItemId);
    }
  }

  private void handleAllNavigationRailSelections(int itemId) {
    handleNavigationRailItemSelections(navigationRailView, itemId);
  }

  private static void handleNavigationRailItemSelections(NavigationRailView view, int itemId) {
    view.getMenu().findItem(itemId).setChecked(true);
  }

  protected void initNavigationRailDemoControls(View view) {
    initAddNavItemButton(view.findViewById(R.id.add_button));
    initRemoveNavItemButton(view.findViewById(R.id.remove_button));
    initAddIncreaseBadgeNumberButton(view.findViewById(R.id.increment_badge_number_button));

    Spinner badgeGravitySpinner = view.findViewById(R.id.badge_gravity_spinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            badgeGravitySpinner.getContext(),
            R.array.cat_navigation_rail_badge_gravity_titles,
            android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    badgeGravitySpinner.setAdapter(adapter);

    badgeGravitySpinner.setOnItemSelectedListener(
        new AdapterView.OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateBadgeGravity(
                badgeGravityValues[MathUtils.clamp(position, 0, badgeGravityValues.length - 1)]);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });

    MaterialSwitch materialSwitch = view.findViewById(R.id.bold_text_switch);
    materialSwitch.setChecked(true);
    materialSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) ->
            navigationRailView.setItemTextAppearanceActiveBoldEnabled(isChecked));
  }

  private void initAddIncreaseBadgeNumberButton(Button incrementBadgeNumberButton) {
    incrementBadgeNumberButton.setOnClickListener(v -> updateBadgeNumber(1));
  }

  private void initAddNavItemButton(Button addNavItemButton) {
    addNavItemButton.setOnClickListener(
        v -> {
          if (numVisibleChildren < MAX_NAVIGATION_RAIL_CHILDREN) {
            addNavItemsToNavigationRails();
            numVisibleChildren++;
          }
        });
  }

  private void initRemoveNavItemButton(Button removeNavItemButton) {
    removeNavItemButton.setOnClickListener(
        v -> {
          if (numVisibleChildren > 0) {
            numVisibleChildren--;
            removeNavItemsFromNavigationRails();
          }
        });
  }

  private void setNavigationRailListeners(OnItemSelectedListener listener) {
    navigationRailView.setOnItemSelectedListener(listener);
  }

  private void removeNavItemsFromNavigationRails() {
    navigationRailView.getMenu().getItem(numVisibleChildren).setVisible(false);
  }

  private void addNavItemsToNavigationRails() {
    navigationRailView.getMenu().getItem(numVisibleChildren).setVisible(true);
  }

  private void initNavigationRail(@NonNull Context context, @NonNull View view) {
    navigationRailView = view.findViewById(R.id.cat_navigation_rail);

    @LayoutRes int demoControls = getNavigationRailDemoControlsLayout();
    if (demoControls != 0) {
      ViewGroup controlsView = view.findViewById(R.id.demo_controls);
      View.inflate(context, getNavigationRailDemoControlsLayout(), controlsView);
    }
  }

  /**
   * Provides the layout resource id for a view that can be control the navigation rail view.
   * Default is 0, which means there is no controls view.
   */
  @LayoutRes
  protected int getNavigationRailDemoControlsLayout() {
    return 0;
  }
}
