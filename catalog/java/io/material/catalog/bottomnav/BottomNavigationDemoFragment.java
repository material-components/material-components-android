/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.bottomnav;

import io.material.catalog.R;

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
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeDrawable.BadgeGravity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationBarView.OnItemSelectedListener;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A base class that provides a demo screen structure for a single bottom nav demo. */
public abstract class BottomNavigationDemoFragment extends DemoFragment {

  private static final int MAX_BOTTOM_NAV_CHILDREN = 5;

  @Nullable
  private final int[] badgeGravityValues =
      new int[] {
        BadgeDrawable.TOP_END,
        BadgeDrawable.TOP_START,
      };

  private int numVisibleChildren = 3;
  protected List<BottomNavigationView> bottomNavigationViews;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_bottom_nav_fragment, viewGroup, false /* attachToRoot */);
    initBottomNavs(layoutInflater, view);
    initBottomNavDemoControls(view);

    OnItemSelectedListener navigationItemListener =
        item -> {
          handleAllBottomNavSelections(item.getItemId());

          TextView page1Text = view.findViewById(R.id.page_1);
          TextView page2Text = view.findViewById(R.id.page_2);
          TextView page3Text = view.findViewById(R.id.page_3);
          TextView page4Text = view.findViewById(R.id.page_4);
          TextView page5Text = view.findViewById(R.id.page_5);

          int itemId = item.getItemId();
          page1Text.setVisibility(itemId == R.id.action_page_1 ? View.VISIBLE : View.GONE);
          page2Text.setVisibility(itemId == R.id.action_page_2 ? View.VISIBLE : View.GONE);
          page3Text.setVisibility(itemId == R.id.action_page_3 ? View.VISIBLE : View.GONE);
          page4Text.setVisibility(itemId == R.id.action_page_4 ? View.VISIBLE : View.GONE);
          page5Text.setVisibility(itemId == R.id.action_page_5 ? View.VISIBLE : View.GONE);

          clearAndHideBadge(item.getItemId());
          return false;
        };
    setBottomNavListeners(navigationItemListener);
    if (bundle == null) {
      setupBadging();
    }
    return view;
  }

  private void setupBadging() {
    for (BottomNavigationView bn : bottomNavigationViews) {
      int menuItemId = bn.getMenu().getItem(0).getItemId();
      // An icon only badge will be displayed.
      BadgeDrawable badge = bn.getOrCreateBadge(menuItemId);
      badge.setVisible(true);

      menuItemId = bn.getMenu().getItem(1).getItemId();
      // A badge with the text "99" will be displayed.
      badge = bn.getOrCreateBadge(menuItemId);
      badge.setVisible(true);
      badge.setNumber(99);

      menuItemId = bn.getMenu().getItem(2).getItemId();
      // A badge with the text "999+" will be displayed.
      badge = bn.getOrCreateBadge(menuItemId);
      badge.setVisible(true);
      badge.setNumber(9999);
    }
  }

  private void updateBadgeNumber(int delta) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      // Increase the badge number on the first menu item.
      MenuItem menuItem = bn.getMenu().getItem(0);
      int menuItemId = menuItem.getItemId();
      BadgeDrawable badgeDrawable = bn.getOrCreateBadge(menuItemId);
      // In case the first menu item has been selected and the badge was hidden, call
      // BadgeDrawable#setVisible() to ensure the badge is visible.
      badgeDrawable.setVisible(true);
      badgeDrawable.setNumber(badgeDrawable.getNumber() + delta);
    }
  }

  private void updateBadgeGravity(@BadgeGravity int badgeGravity) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      for (int i = 0; i < MAX_BOTTOM_NAV_CHILDREN; i++) {
        // Update the badge gravity on all the menu items.
        MenuItem menuItem = bn.getMenu().getItem(i);
        int menuItemId = menuItem.getItemId();
        BadgeDrawable badgeDrawable = bn.getBadge(menuItemId);
        if (badgeDrawable != null) {
          badgeDrawable.setBadgeGravity(badgeGravity);
        }
      }
    }
  }

  private void clearAndHideBadge(int menuItemId) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      MenuItem menuItem = bn.getMenu().getItem(0);
      if (menuItem.getItemId() == menuItemId) {
        // Hide instead of removing the badge associated with the first menu item because the user
        // can trigger it to be displayed again.
        BadgeDrawable badgeDrawable = bn.getBadge(menuItemId);
        if (badgeDrawable != null) {
          badgeDrawable.setVisible(false);
          badgeDrawable.clearNumber();
        }
      } else {
        // Remove the badge associated with this menu item because cannot be displayed again.
        bn.removeBadge(menuItemId);
      }
    }
  }

  private void handleAllBottomNavSelections(int itemId) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      handleBottomNavItemSelections(bn, itemId);
    }
  }

  private void handleBottomNavItemSelections(BottomNavigationView bn, int itemId) {
    bn.getMenu().findItem(itemId).setChecked(true);
  }

  protected void initBottomNavDemoControls(View view) {
    initAddNavItemButton(view.findViewById(R.id.add_button));
    initRemoveNavItemButton(view.findViewById(R.id.remove_button));
    initAddIncreaseBadgeNumberButton(view.findViewById(R.id.increment_badge_number_button));

    Spinner badgeGravitySpinner = view.findViewById(R.id.badge_gravity_spinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            badgeGravitySpinner.getContext(),
            R.array.cat_bottom_nav_badge_gravity_titles,
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
        (buttonView, isChecked) -> {
          for (BottomNavigationView bn : bottomNavigationViews) {
            bn.setItemTextAppearanceActiveBoldEnabled(isChecked);
          }
        });
  }

  private void initAddIncreaseBadgeNumberButton(Button incrementBadgeNumberButton) {
    incrementBadgeNumberButton.setOnClickListener(v -> updateBadgeNumber(1));
  }

  private void initAddNavItemButton(Button addNavItemButton) {
    addNavItemButton.setOnClickListener(
        v -> {
          if (numVisibleChildren < MAX_BOTTOM_NAV_CHILDREN) {
            addNavItemsToBottomNavs();
            numVisibleChildren++;
          }
        });
  }

  private void initRemoveNavItemButton(Button removeNavItemButton) {
    removeNavItemButton.setOnClickListener(
        v -> {
          if (numVisibleChildren > 0) {
            numVisibleChildren--;
            removeNavItemsFromBottomNavs();
          }
        });
  }

  private void setBottomNavListeners(OnItemSelectedListener listener) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      bn.setOnItemSelectedListener(listener);
    }
  }

  private void removeNavItemsFromBottomNavs() {
    adjustAllBottomNavItemsVisibilities(false);
  }

  private void addNavItemsToBottomNavs() {
    adjustAllBottomNavItemsVisibilities(true);
  }

  private void adjustAllBottomNavItemsVisibilities(boolean visibility) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      adjustBottomNavItemsVisibility(bn, visibility);
    }
  }

  private void adjustBottomNavItemsVisibility(BottomNavigationView bn, boolean visibility) {
    bn.getMenu().getItem(numVisibleChildren).setVisible(visibility);
  }

  private void initBottomNavs(LayoutInflater layoutInflater, View view) {
    inflateBottomNavs(layoutInflater, view.findViewById(R.id.bottom_navs));
    inflateBottomNavDemoControls(layoutInflater, view.findViewById(R.id.demo_controls));
    addBottomNavsToList(view);
  }

  private void inflateBottomNavDemoControls(LayoutInflater layoutInflater, ViewGroup content) {
    @LayoutRes int demoControls = getBottomNavDemoControlsLayout();
    if (demoControls != 0) {
      content.addView(layoutInflater.inflate(getBottomNavDemoControlsLayout(), content, false));
    }
  }

  private void inflateBottomNavs(LayoutInflater layoutInflater, ViewGroup content) {
    content.addView(layoutInflater.inflate(getBottomNavsContent(), content, false));
  }

  private void addBottomNavsToList(View view) {
    bottomNavigationViews = DemoUtils.findViewsWithType(view, BottomNavigationView.class);
  }

  @LayoutRes
  protected int getBottomNavsContent() {
    return R.layout.cat_bottom_navs;
  }

  @LayoutRes
  protected int getBottomNavDemoControlsLayout() {
    return 0;
  }
}
