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
import android.widget.Button;
import android.widget.RadioButton;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a scrollable tabs demo for the Catalog app. */
public class TabsScrollableDemoFragment extends DemoFragment {

  private static final String KEY_TABS = "TABS";
  private static final String KEY_TAB_GRAVITY = "TAB_GRAVITY";

  private int numTabs = 0;
  private String[] tabTitles;
  private TabLayout scrollableTabLayout;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    // return layoutInflater.inflate(getTabsContent(), viewGroup, false /* attachToRoot */);
    View view =
        layoutInflater.inflate(
            R.layout.cat_tabs_scrollable_fragment, viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    View tabsContent = layoutInflater.inflate(getTabsContent(), content, false /* attachToRoot */);
    content.addView(tabsContent, 0);

    scrollableTabLayout = tabsContent.findViewById(R.id.scrollable_tab_layout);

    RadioButton tabGravityStartButton = view.findViewById(R.id.tabs_gravity_start_button);
    tabGravityStartButton.setOnClickListener(
        v -> scrollableTabLayout.setTabGravity(TabLayout.GRAVITY_START));

    RadioButton tabGravityCenterButton = view.findViewById(R.id.tabs_gravity_center_button);
    tabGravityCenterButton.setOnClickListener(
        v -> scrollableTabLayout.setTabGravity(TabLayout.GRAVITY_CENTER));

    if (bundle != null) {
      scrollableTabLayout.removeAllTabs();
      scrollableTabLayout.setTabGravity(bundle.getInt(KEY_TAB_GRAVITY));
      // Restore saved tabs
      String[] tabLabels = bundle.getStringArray(KEY_TABS);

      for (String label : tabLabels) {
        scrollableTabLayout.addTab(scrollableTabLayout.newTab().setText(label));
      }
    }

    numTabs = scrollableTabLayout.getTabCount();

    tabTitles = getContext().getResources().getStringArray(R.array.cat_tabs_titles);

    Button addButton = view.findViewById(R.id.add_tab_button);
    addButton.setOnClickListener(
        v -> {
          scrollableTabLayout.addTab(
              scrollableTabLayout.newTab().setText(tabTitles[numTabs % tabTitles.length]));
          numTabs++;
        });

    Button removeButton = view.findViewById(R.id.remove_tab_button);
    removeButton.setOnClickListener(
        v -> {
          Tab tab = scrollableTabLayout.getTabAt(scrollableTabLayout.getTabCount() - 1);
          if (tab != null) {
            scrollableTabLayout.removeTab(tab);
          }
          numTabs = Math.max(0, numTabs - 1);
        });
    return view;
  }

  @LayoutRes
  protected int getTabsContent() {
    return R.layout.cat_tabs_scrollable_content;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    String[] tabLabels = new String[scrollableTabLayout.getTabCount()];
    for (int i = 0; i < scrollableTabLayout.getTabCount(); i++) {
      tabLabels[i] = scrollableTabLayout.getTabAt(i).getText().toString();
    }
    bundle.putStringArray(KEY_TABS, tabLabels);
    bundle.putInt(KEY_TAB_GRAVITY, scrollableTabLayout.getTabGravity());
  }
}
