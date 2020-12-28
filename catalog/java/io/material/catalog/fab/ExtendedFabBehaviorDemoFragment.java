/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.fab;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/**
 * An Extended FAB motion demo fragment which demonstrate the FAB behavior when positioned over a
 * scrollable component.
 */
public class ExtendedFabBehaviorDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View root =
        layoutInflater.inflate(getExtendedFabContent(), viewGroup, false /* attachToRoot */);

    Toolbar toolbar = root.findViewById(R.id.toolbar);
    AppCompatActivity activity = (AppCompatActivity) getActivity();
    activity.setSupportActionBar(toolbar);

    List<ExtendedFloatingActionButton> extendedFabs =
        DemoUtils.findViewsWithType(root, ExtendedFloatingActionButton.class);
    for (ExtendedFloatingActionButton extendedFab : extendedFabs) {
      extendedFab.setOnClickListener(
          v ->
              Snackbar.make(
                      v, R.string.cat_extended_fab_clicked, Snackbar.LENGTH_SHORT)
                  .show());
    }

    return root;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @LayoutRes
  protected int getExtendedFabContent() {
    return R.layout.cat_extended_fab_behavior_fragment;
  }
}
