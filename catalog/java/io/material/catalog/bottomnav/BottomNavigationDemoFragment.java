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
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.BottomNavigationView.OnNavigationItemSelectedListener;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A base class that provides a demo screen structure for a single bottom nav demo. */
public abstract class BottomNavigationDemoFragment extends DemoFragment {

  private static final int MAX_BOTTOM_NAV_CHILDREN = 5;
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

    OnNavigationItemSelectedListener navigationItemListener =
        new OnNavigationItemSelectedListener() {
          @Override
          public boolean onNavigationItemSelected(@NonNull MenuItem item) {
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
            return false;
          }
        };
    setBottomNavListeners(navigationItemListener);
    return view;
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
  }

  private void initAddNavItemButton(Button addNavItemButton) {
    addNavItemButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (numVisibleChildren < MAX_BOTTOM_NAV_CHILDREN) {
              addNavItemsToBottomNavs();
              numVisibleChildren++;
            }
          }
        });
  }

  private void initRemoveNavItemButton(Button removeNavItemButton) {
    removeNavItemButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (numVisibleChildren > 0) {
              numVisibleChildren--;
              removeNavItemsFromBottomNavs();
            }
          }
        });
  }

  private void setBottomNavListeners(OnNavigationItemSelectedListener listener) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      bn.setOnNavigationItemSelectedListener(listener);
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
    inflateBottomNavs(layoutInflater, view.findViewById(R.id.content));
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
