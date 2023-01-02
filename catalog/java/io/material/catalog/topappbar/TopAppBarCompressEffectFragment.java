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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ToggleButton;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.tabs.TabLayout;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;

/** A fragment that displays a scrolling Top App Bar demo for the Catalog app. */
public class TopAppBarCompressEffectFragment extends DemoFragment {

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_topappbar_compress_effect_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    AppBarLayout appBarLayout = view.findViewById(R.id.appbarlayout);
    appBarLayout.setStatusBarForeground(
        MaterialShapeDrawable.createWithElevationOverlay(requireContext()));

    TabLayout tabs = view.findViewById(R.id.tabs);
    ToggleButton showHideTabsButton = view.findViewById(R.id.show_hide_tabs_button);
    updateTabVisibility(tabs, showHideTabsButton.isChecked());
    showHideTabsButton.setOnCheckedChangeListener(
        (buttonView, isChecked) -> updateTabVisibility(tabs, isChecked));

    return view;
  }

  private static void updateTabVisibility(TabLayout tabs, boolean show) {
    tabs.setVisibility(show ? View.VISIBLE : View.GONE);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean onOptionsItemSelected(@NonNull MenuItem item) {
    return DemoUtils.showSnackbar(getActivity(), item) || super.onOptionsItemSelected(item);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
