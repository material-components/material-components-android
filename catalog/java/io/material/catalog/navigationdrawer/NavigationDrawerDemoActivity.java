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

package io.material.catalog.navigationdrawer;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.navigation.NavigationView;
import io.material.catalog.feature.DemoActivity;

/** A fragment that displays the main Navigation Drawer demo for the Catalog app. */
public class NavigationDrawerDemoActivity extends DemoActivity {

  private DrawerLayout drawerLayout;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_navigationdrawer, viewGroup, false /* attachToRoot */);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    drawerLayout = view.findViewById(R.id.drawer);
    drawerLayout.addDrawerListener(
        new ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.cat_navigationdrawer_button_show_content_description,
            R.string.cat_navigationdrawer_button_hide_content_description));

    NavigationView navigationView = view.findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          navigationView.setCheckedItem(menuItem);
          drawerLayout.closeDrawer(Gravity.START);
          return true;
        });

    return view;
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem menuItem) {
    if (menuItem.getItemId() == android.R.id.home) {
      drawerLayout.openDrawer(Gravity.START);
      return true;
    }

    return super.onOptionsItemSelected(menuItem);
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
