/*
 * Copyright 2019 The Android Open Source Project
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
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayout.Tab;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a scrollable tabs demo for the Catalog app. */
public class TabsAutoDemoFragment extends DemoFragment {

  private int numTabs = 0;
  private String[] tabTitles;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_tabs_auto_fragment, viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    View tabsContent = layoutInflater.inflate(getTabsContent(), content, false /* attachToRoot */);
    content.addView(tabsContent, 0);

    TabLayout autoScrollableTabLayout = tabsContent.findViewById(R.id.auto_tab_layout);
    numTabs = autoScrollableTabLayout.getChildCount();

    tabTitles = getContext().getResources().getStringArray(R.array.cat_tabs_titles);

    Button addButton = view.findViewById(R.id.add_tab_button);
    addButton.setOnClickListener(
        v -> {
          autoScrollableTabLayout.addTab(
              autoScrollableTabLayout.newTab().setText(tabTitles[numTabs % tabTitles.length]));
          numTabs += 1;
        });

    Button removeButton = view.findViewById(R.id.remove_tab_button);
    removeButton.setOnClickListener(
        v -> {
          Tab tab = autoScrollableTabLayout.getTabAt(autoScrollableTabLayout.getTabCount() - 1);
          if (tab != null) {
            autoScrollableTabLayout.removeTab(tab);
          }
          numTabs = Math.max(0, numTabs - 1);
        });

    return view;
  }

  @LayoutRes
  protected int getTabsContent() {
    return R.layout.cat_tabs_auto_content;
  }
}
