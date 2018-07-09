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

package io.material.catalog.bottomappbar;

import io.material.catalog.R;

import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import com.google.android.material.bottomappbar.BottomAppBar;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import android.support.design.widget.CoordinatorLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ToggleButton;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.OnBackPressedHandler;
import io.material.catalog.themeswitcher.ThemeSwitcherHelper;

/** A fragment that displays the main Bottom App Bar demos for the Catalog app. */
public class BottomAppBarMainDemoFragment extends DemoFragment implements OnBackPressedHandler {

  protected BottomAppBar bar;
  protected CoordinatorLayout coordinatorLayout;

  @Nullable private ThemeSwitcherHelper themeSwitcherHelper;
  private BottomSheetBehavior<View> bottomDrawerBehavior;

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);

    // The theme switcher helper is used in an adhoc way with the toolbar since the BottomAppBar is
    // set as the action bar.
    themeSwitcherHelper = new ThemeSwitcherHelper(getFragmentManager());
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.demo_primary, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem menuItem) {
    Snackbar.make(getView(), menuItem.getTitle(), Snackbar.LENGTH_SHORT).show();
    return true;
  }

  @LayoutRes
  public int getBottomAppBarContent() {
    return R.layout.cat_bottomappbar_fragment;
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getBottomAppBarContent(), viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    toolbar.setTitle(getDefaultDemoTitle());
    themeSwitcherHelper.onCreateOptionsMenu(toolbar.getMenu(), getActivity().getMenuInflater());
    toolbar.setOnMenuItemClickListener(themeSwitcherHelper::onOptionsItemSelected);
    toolbar.setNavigationOnClickListener(
        v -> {
          getActivity().onBackPressed();
        });

    coordinatorLayout = view.findViewById(R.id.coordinator_layout);
    bar = view.findViewById(R.id.bar);
    ((AppCompatActivity) getActivity()).setSupportActionBar(bar);

    setUpBottomDrawer(view);

    FloatingActionButton fab = view.findViewById(R.id.fab);
    fab.setOnClickListener(
        v -> Snackbar.make(getView(), fab.getContentDescription(), Snackbar.LENGTH_SHORT).show());
    NavigationView navigationView = view.findViewById(R.id.navigation_view);
    navigationView.setNavigationItemSelectedListener(
        item -> {
          Snackbar.make(getView(), item.getTitle(), Snackbar.LENGTH_SHORT).show();
          return false;
        });

    Button centerButton = view.findViewById(R.id.center);
    Button endButton = view.findViewById(R.id.end);
    ToggleButton attachToggle = view.findViewById(R.id.attach_toggle);
    attachToggle.setChecked(fab.getVisibility() == View.VISIBLE);
    centerButton.setOnClickListener(
        v -> bar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_CENTER));
    endButton.setOnClickListener(v -> bar.setFabAlignmentMode(BottomAppBar.FAB_ALIGNMENT_MODE_END));
    attachToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          if (isChecked) {
            fab.show();
          } else {
            fab.hide();
          }
        });

    return view;
  }

  @Override
  public boolean onBackPressed() {
    if (bottomDrawerBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
      bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
      return true;
    }
    return false;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  protected void setUpBottomDrawer(View view) {
    View bottomDrawer = coordinatorLayout.findViewById(R.id.bottom_drawer);
    bottomDrawerBehavior = BottomSheetBehavior.from(bottomDrawer);
    bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

    bar.setNavigationOnClickListener(
        v -> bottomDrawerBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED));
    bar.setNavigationIcon(R.drawable.ic_drawer_menu_24px);
    bar.replaceMenu(R.menu.demo_primary);
  }
}
