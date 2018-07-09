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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a scrolling Top App Bar demo for the Catalog app. */
public class TopAppBarScrollingDemoFragment extends DemoFragment {

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_topappbar_scrolling_fragment, viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_topappbar_menu, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
