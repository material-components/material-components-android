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

package io.material.catalog.adaptive;

import io.material.catalog.R;

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.fragment.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ReactiveGuide;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import io.material.catalog.feature.DemoActivity;

/** An Activity which hosts the Adaptive list view demo flow. */
public class AdaptiveListViewDemoActivity extends DemoActivity {

  private DrawerLayout drawerLayout;
  private NavigationView modalNavDrawer;
  private View detailViewContainer;
  private ReactiveGuide guideline;
  private BottomNavigationView bottomNav;
  private FloatingActionButton fab;
  private NavigationRailView navRail;
  private NavigationView navDrawer;
  private ExtendedFloatingActionButton navFab;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_list_view_activity, viewGroup, false);
    drawerLayout = view.findViewById(R.id.drawer_layout);
    modalNavDrawer = view.findViewById(R.id.modal_nav_drawer);
    detailViewContainer = view.findViewById(R.id.list_view_detail_fragment_container);
    guideline = view.findViewById(R.id.guideline);
    bottomNav = view.findViewById(R.id.bottom_nav);
    fab = view.findViewById(R.id.fab);
    navRail = view.findViewById(R.id.nav_rail);
    navDrawer = view.findViewById(R.id.nav_drawer);
    navFab = view.findViewById(R.id.nav_fab);
    return view;
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Configuration configuration = getResources().getConfiguration();
    FragmentManager fragmentManager = getSupportFragmentManager();
    int listViewFragmentId = R.id.list_view_fragment_container;
    int detailViewFragmentId = R.id.list_view_detail_fragment_container;
    AdaptiveListViewDemoFragment listViewFragment = new AdaptiveListViewDemoFragment();
    AdaptiveListViewDetailDemoFragment detailViewFragment =
        new AdaptiveListViewDetailDemoFragment();

    // Update navigation views according to screen width size.
    int screenWidth = configuration.screenWidthDp;
    AdaptiveUtils.updateNavigationViewLayout(
        screenWidth,
        drawerLayout,
        modalNavDrawer,
        fab,
        bottomNav,
        navRail,
        navDrawer,
        navFab);

    // Clear backstack to prevent unexpected behaviors when pressing back button.
    int backStrackEntryCount = fragmentManager.getBackStackEntryCount();
    for (int entry = 0; entry < backStrackEntryCount; entry++) {
      fragmentManager.popBackStack();
    }
    // Update layout according to orientation.
    if (configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
      guideline.setGuidelineEnd(0);
      detailViewContainer.setVisibility(View.GONE);
      listViewFragment.setDetailViewContainerId(listViewFragmentId);
      fragmentManager.beginTransaction().replace(listViewFragmentId, listViewFragment).commit();
    } else {
      guideline.setGuidelinePercent(0.5f);
      listViewFragment.setDetailViewContainerId(detailViewFragmentId);
      fragmentManager
          .beginTransaction()
          .replace(listViewFragmentId, listViewFragment)
          .replace(detailViewFragmentId, detailViewFragment)
          .commit();
    }
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
