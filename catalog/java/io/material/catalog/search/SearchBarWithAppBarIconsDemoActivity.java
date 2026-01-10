/*
 * Copyright 2025 The Android Open Source Project
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

package io.material.catalog.search;

import io.material.catalog.R;

import static io.material.catalog.search.SearchDemoUtils.showSnackbar;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;
import io.material.catalog.feature.DemoActivity;

/** An activity that displays a SearchBar in an AppBar with outside icons demo. */
public class SearchBarWithAppBarIconsDemoActivity extends DemoActivity {

  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_search_appbar_icons_fragment, viewGroup, false);

    SearchBar searchBar = view.findViewById(R.id.cat_search_bar);
    SearchView searchView = view.findViewById(R.id.cat_search_view);
    LinearLayout suggestionContainer = view.findViewById(R.id.cat_search_view_suggestion_container);

    MaterialSwitch menuSwitch = view.findViewById(R.id.cat_search_bar_menu_switch);
    menuSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      if (isChecked) {
        searchBar.inflateMenu(R.menu.cat_searchview_menu);
        searchBar.setOnMenuItemClickListener(
            menuItem -> {
              showSnackbar(SearchBarWithAppBarIconsDemoActivity.this, menuItem);
              return true;
            });
      } else {
        searchBar.getMenu().clear();
      }
    });
    searchView.setupWithSearchBar(searchBar);
    SearchDemoUtils.setUpSearchView(this, searchBar, searchView);
    SearchDemoUtils.setUpSuggestions(suggestionContainer, searchBar, searchView);
    SearchDemoUtils.startOnLoadAnimation(searchBar, bundle);

    return view;
  }

  @Override
  protected boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
