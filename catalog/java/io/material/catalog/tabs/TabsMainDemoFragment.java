/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.material.catalog.tabs;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.math.MathUtils;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.badge.BadgeDrawable.BadgeGravity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.OnTabSelectedListener;
import com.google.android.material.tabs.TabLayout.Tab;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main tabs demo for the Catalog app. */
public class TabsMainDemoFragment extends DemoFragment {

  private List<TabLayout> tabLayouts;

  @Nullable
  private final int[] badgeGravityValues =
      new int[] {
        BadgeDrawable.TOP_END,
        BadgeDrawable.TOP_START,
        BadgeDrawable.BOTTOM_END,
        BadgeDrawable.BOTTOM_START
      };

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getTabsContent(), viewGroup, /* attachToRoot= */ false);

    tabLayouts = DemoUtils.findViewsWithType(view, TabLayout.class);
    MaterialButton incrementBadgeNumberButton =
        view.findViewById(R.id.increment_badge_number_button);
    incrementBadgeNumberButton.setOnClickListener(v -> incrementBadgeNumber());
    Spinner badgeGravitySpinner = view.findViewById(R.id.badge_gravity_spinner);
    ArrayAdapter<CharSequence> adapter =
        ArrayAdapter.createFromResource(
            badgeGravitySpinner.getContext(),
            R.array.cat_tabs_badge_gravity_titles,
            android.R.layout.simple_spinner_item);
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    badgeGravitySpinner.setAdapter(adapter);
    badgeGravitySpinner.setOnItemSelectedListener(
        new OnItemSelectedListener() {
          @Override
          public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            updateBadgeGravity(
                badgeGravityValues[MathUtils.clamp(position, 0, badgeGravityValues.length - 1)]);
          }

          @Override
          public void onNothingSelected(AdapterView<?> parent) {}
        });

    setupBadging();
    return view;
  }

  @LayoutRes
  protected int getTabsContent() {
    return R.layout.cat_tabs_main_content;
  }

  private void setupBadging() {
    for (TabLayout tabLayout : tabLayouts) {
      // An icon only badge will be displayed.
      BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
      badgeDrawable.setVisible(true);

      // A badge with the text "99" will be displayed.
      badgeDrawable = tabLayout.getTabAt(1).getOrCreateBadge();
      badgeDrawable.setVisible(true);
      badgeDrawable.setNumber(99);

      // A badge with the text "999+" will be displayed.
      badgeDrawable = tabLayout.getTabAt(2).getOrCreateBadge();
      badgeDrawable.setVisible(true);
      badgeDrawable.setNumber(9999);

      tabLayout.addOnTabSelectedListener(
          new OnTabSelectedListener() {
            @Override
            public void onTabSelected(Tab tab) {
              clearAndHideBadge(tab.getPosition());
            }

            @Override
            public void onTabUnselected(Tab tab) {}

            @Override
            public void onTabReselected(Tab tab) {
              clearAndHideBadge(tab.getPosition());
            }
          });
    }
  }

  private void incrementBadgeNumber() {
    for (TabLayout tabLayout : tabLayouts) {
      // Increase the badge number on the first tab position.
      // In case the first tab has been selected and the badge was hidden, call
      // BadgeDrawable#setVisible() to ensure the badge is visible.
      BadgeDrawable badgeDrawable = tabLayout.getTabAt(0).getOrCreateBadge();
      badgeDrawable.setVisible(true);
      badgeDrawable.setNumber(badgeDrawable.getNumber() + 1);
    }
  }

  private void clearAndHideBadge(int tabPosition) {
    for (TabLayout tabLayout : tabLayouts) {
      if (tabPosition == 0) {
        // Hide instead of removing the badge associated with the first menu item because the user
        // can trigger it to be displayed again.
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(tabPosition).getBadge();
        if (badgeDrawable != null) {
          badgeDrawable.setVisible(false);
          badgeDrawable.clearNumber();
        }
      } else {
        tabLayout.getTabAt(tabPosition).removeBadge();
      }
    }
  }

  private void updateBadgeGravity(@BadgeGravity int badgeGravity) {
    for (TabLayout tabLayout : tabLayouts) {
      // Update the badge gravity on all the tabs.
      for (int index = 0; index < tabLayout.getTabCount(); index++) {
        BadgeDrawable badgeDrawable = tabLayout.getTabAt(index).getBadge();
        if (badgeDrawable != null) {
          badgeDrawable.setBadgeGravity(badgeGravity);
        }
      }
    }
  }
}
