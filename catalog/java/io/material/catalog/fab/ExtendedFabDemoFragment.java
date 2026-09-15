/*
 * Copyright 2019 The Android Open Source Project
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** An Extended FAB demo fragment. */
public class ExtendedFabDemoFragment extends DemoFragment {

  private boolean fabsShown = true;
  private boolean fabsExpanded = true;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View root =
        layoutInflater.inflate(
            R.layout.cat_extended_fab_fragment, viewGroup, false /* attachToRoot */);

    ViewCompat.setOnApplyWindowInsetsListener(
        root,
        (v, insets) -> {
          Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
          v.setPadding(0, 0, 0, navBarInsets.bottom);
          return insets;
        });

    ViewGroup content = root.findViewById(R.id.content);
    View.inflate(getContext(), getExtendedFabContent(), content);

    List<ExtendedFloatingActionButton> extendedFabs =
        DemoUtils.findViewsWithType(root, ExtendedFloatingActionButton.class);
    for (ExtendedFloatingActionButton extendedFab : extendedFabs) {
      extendedFab.setOnClickListener(
          v -> {
            Snackbar.make(v, R.string.cat_extended_fab_clicked, Snackbar.LENGTH_SHORT)
                .show();
          });
    }

    Button showHideFabs = root.findViewById(R.id.show_hide_fabs);
    showHideFabs.setOnClickListener(
        v -> {
          for (ExtendedFloatingActionButton extendedFab : extendedFabs) {
            if (fabsShown) {
              extendedFab.hide();
              showHideFabs.setText(R.string.show_fabs_label);
            } else {
              extendedFab.show();
              showHideFabs.setText(R.string.hide_fabs_label);
            }
          }
          fabsShown = !fabsShown;
        });

    Button collapseExpandFabs = root.findViewById(R.id.collapse_expand_fabs);
    collapseExpandFabs.setOnClickListener(
        v -> {
          for (ExtendedFloatingActionButton extendedFab : extendedFabs) {
            if (fabsExpanded) {
              extendedFab.shrink();
              collapseExpandFabs.setText(R.string.extend_fabs_label);
            } else {
              extendedFab.extend();
              collapseExpandFabs.setText(R.string.shrink_fabs_label);
            }
          }
          fabsExpanded = !fabsExpanded;
        });

    return root;
  }

  @LayoutRes
  protected int getExtendedFabContent() {
    return R.layout.m3_extended_fabs;
  }
}
