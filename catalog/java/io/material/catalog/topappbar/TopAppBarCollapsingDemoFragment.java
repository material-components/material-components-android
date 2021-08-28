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

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.widget.NestedScrollView;

import com.google.android.material.appbar.CollapsingToolbarLayout;

import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a collapsing Top App Bar demo for the Catalog app. */
public class TopAppBarCollapsingDemoFragment extends DemoFragment {

  @Override
  public void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getCollapsingToolbarLayoutResId(), viewGroup, false);

    Toolbar toolbar = view.findViewById(R.id.toolbar);
    CollapsingToolbarLayout collapsingToolbarLayout = view.findViewById(R.id.collapsingtoolbarlayout);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    collapsingToolbarLayout.setCollapsedTitleGravity(Gravity.CENTER);
    collapsingToolbarLayout.setTitle("ToolbarLayout");
    collapsingToolbarLayout.setExpandedTitleColor(Color.WHITE);
    collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
    final NestedScrollView scrollView = view.findViewById(R.id.scrollView);
    collapsingToolbarLayout.startLayoutAnimation();
    activity.setSupportActionBar(toolbar);
    scrollView.setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
      @SuppressLint("NewApi")
      @Override
      public WindowInsets onApplyWindowInsets(View v, WindowInsets insets) {
        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) scrollView.getLayoutParams();
        params.bottomMargin = insets.getInsets(WindowInsets.Type.ime()).bottom;
        scrollView.setLayoutParams(params);
        return insets;
      }
    });
    return view;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    menuInflater.inflate(R.menu.cat_text, menu);
    super.onCreateOptionsMenu(menu, menuInflater);
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @LayoutRes
  protected int getCollapsingToolbarLayoutResId() {
    return R.layout.cat_topappbar_collapsing_fragment;
  }
}
