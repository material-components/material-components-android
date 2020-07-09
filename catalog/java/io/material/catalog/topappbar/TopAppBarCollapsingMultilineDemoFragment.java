/*
 * Copyright 2020 The Android Open Source Project
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a collapsing Top App Bar demo for the Catalog app. */
public class TopAppBarCollapsingMultilineDemoFragment extends DemoFragment {

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_topappbar_collapsing_multiline_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu_maxlines, menu);

    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public void onPrepareOptionsMenu(@NonNull Menu menu) {
    super.onPrepareOptionsMenu(menu);

    CollapsingToolbarLayout collapsingToolbarLayout = requireView().findViewById(R.id.collapsingtoolbarlayout);
    switch (collapsingToolbarLayout.getMaxLines()) {
      case 1:
        menu.findItem(R.id.maxLines1).setChecked(true);
        break;
      case 2:
        menu.findItem(R.id.maxLines2).setChecked(true);
        break;
      case 3:
        menu.findItem(R.id.maxLines3).setChecked(true);
        break;
    }
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    CollapsingToolbarLayout collapsingToolbarLayout = requireView().findViewById(R.id.collapsingtoolbarlayout);
    switch (item.getItemId()) {
      case R.id.maxLines1:
        collapsingToolbarLayout.setMaxLines(1);
        return true;
      case R.id.maxLines2:
        collapsingToolbarLayout.setMaxLines(2);
        return true;
      case R.id.maxLines3:
        collapsingToolbarLayout.setMaxLines(3);
        return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
