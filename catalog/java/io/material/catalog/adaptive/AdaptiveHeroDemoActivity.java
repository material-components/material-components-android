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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.navigationrail.NavigationRailView;
import io.material.catalog.feature.DemoActivity;

/** An Activity which hosts the Adaptive hero demo flow. */
public class AdaptiveHeroDemoActivity extends DemoActivity {

  private DrawerLayout drawerLayout;
  private NavigationView modalNavDrawer;
  private BottomNavigationView bottomNav;
  private NavigationRailView navRail;
  private NavigationView navDrawer;
  private ExtendedFloatingActionButton navFab;

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_adaptive_hero_activity, viewGroup, false);
    drawerLayout = view.findViewById(R.id.drawer_layout);
    modalNavDrawer = view.findViewById(R.id.modal_nav_drawer);
    bottomNav = view.findViewById(R.id.bottom_nav);
    navRail = view.findViewById(R.id.nav_rail);
    navDrawer = view.findViewById(R.id.nav_drawer);
    navFab = view.findViewById(R.id.nav_fab);
    return view;
  }

  @Override
  protected void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Configuration configuration = getResources().getConfiguration();

    // Update navigation views according to screen width size.
    int screenWidth = configuration.screenWidthDp;
    AdaptiveUtils.updateNavigationViewLayout(
        screenWidth,
        drawerLayout,
        modalNavDrawer,
        /* fab= */ null,
        bottomNav,
        navRail,
        navDrawer,
        navFab);

    getSupportFragmentManager()
        .beginTransaction()
        .replace(R.id.fragment_container, new AdaptiveHeroDemoFragment())
        .commit();
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
