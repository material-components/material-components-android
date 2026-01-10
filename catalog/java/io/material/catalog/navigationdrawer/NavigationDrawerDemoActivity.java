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

import android.content.res.Configuration;
import android.os.Bundle;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.navigation.NavigationView;
import io.material.catalog.feature.DemoActivity;

/** A fragment that displays the main Navigation Drawer demo for the Catalog app. */
public class NavigationDrawerDemoActivity extends DemoActivity {

  private final OnBackPressedCallback drawerOnBackPressedCallback =
      new OnBackPressedCallback(/* enabled= */ false) {
        @Override
        public void handleOnBackPressed() {
          drawerLayout.closeDrawers();
        }
      };

  private DrawerLayout drawerLayout;
  private ActionBarDrawerToggle toggle;
  private MaterialSwitch autoCloseSwitch;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_navigationdrawer, viewGroup, false /* attachToRoot */);

    getOnBackPressedDispatcher().addCallback(this, drawerOnBackPressedCallback);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);

    drawerLayout = view.findViewById(R.id.drawer);
    toggle =
        new ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.cat_navigationdrawer_button_show_content_description,
            R.string.cat_navigationdrawer_button_hide_content_description) {
          @Override
          public void onDrawerOpened(View drawerView) {
            super.onDrawerOpened(drawerView);
            drawerOnBackPressedCallback.setEnabled(true);
          }

          @Override
          public void onDrawerClosed(View drawerView) {
            super.onDrawerClosed(drawerView);
            drawerOnBackPressedCallback.setEnabled(false);
          }
        };
    drawerLayout.addDrawerListener(toggle);

    NavigationView navigationViewStart = view.findViewById(R.id.navigation_view_start);
    initNavigationView(navigationViewStart);

    NavigationView navigationViewEnd = view.findViewById(R.id.navigation_view_end);
    initNavigationView(navigationViewEnd);

    view.findViewById(R.id.show_end_drawer_gravity)
        .setOnClickListener(v -> drawerLayout.openDrawer(navigationViewEnd));

    MaterialSwitch boldTextSwitch = view.findViewById(R.id.bold_text_switch);
    boldTextSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          navigationViewStart.setItemTextAppearanceActiveBoldEnabled(isChecked);
          navigationViewEnd.setItemTextAppearanceActiveBoldEnabled(isChecked);
        });
    autoCloseSwitch = view.findViewById(R.id.auto_close_switch);

    drawerLayout.post(
        () -> {
          if (drawerLayout.isDrawerOpen(GravityCompat.START)
              || drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerOnBackPressedCallback.setEnabled(true);
          }
        });

    return view;
  }

  private void initNavigationView(NavigationView navigationView) {
    navigationView.setCheckedItem(R.id.search_item);
    navigationView.setNavigationItemSelectedListener(
        menuItem -> {
          navigationView.setCheckedItem(menuItem);
          if (autoCloseSwitch.isChecked()) {
            drawerLayout.closeDrawer(navigationView);
          }
          return true;
        });
  }

  @Override
  public boolean dispatchKeyEvent(KeyEvent keyEvent) {
    if (keyEvent.getKeyCode() == KeyEvent.KEYCODE_ESCAPE
        && (drawerLayout.isDrawerOpen(GravityCompat.START)
            || drawerLayout.isDrawerOpen(GravityCompat.END))) {
      drawerLayout.closeDrawers();
      return true;
    }
    return super.dispatchKeyEvent(keyEvent);
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @Override
  protected void onPostCreate(@Nullable Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    toggle.syncState();
  }

  @Override
  public void onConfigurationChanged(@NonNull Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    toggle.onConfigurationChanged(newConfig);
  }
}
