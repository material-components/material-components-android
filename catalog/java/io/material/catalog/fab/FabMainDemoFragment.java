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

package io.material.catalog.fab;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main FAB demos for the Catalog app. */
public class FabMainDemoFragment extends DemoFragment {

  private boolean fabsShown = true;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_fab_fragment, viewGroup, false /* attachToRoot */);

    ViewCompat.setOnApplyWindowInsetsListener(
        view,
        (v, insets) -> {
          Insets navBarInsets = insets.getInsets(WindowInsetsCompat.Type.navigationBars());
          v.setPadding(0, 0, 0, navBarInsets.bottom);
          return insets;
        });

    ViewGroup content = view.findViewById(R.id.content);
    View.inflate(getContext(), getFabsContent(), content);
    View.inflate(getContext(), getThemeFabLayoutResId(), content);

    List<FloatingActionButton> fabs = DemoUtils.findViewsWithType(view, FloatingActionButton.class);

    for (FloatingActionButton fab : fabs) {
      fab.setOnClickListener(
          v -> {
            Snackbar.make(v, R.string.cat_fab_clicked, Snackbar.LENGTH_SHORT).show();
          });
    }

    Button showHideFabs = view.findViewById(R.id.show_hide_fabs);
    showHideFabs.setOnClickListener(
        v -> {
          for (FloatingActionButton fab : fabs) {
            if (fabsShown) {
              fab.hide();
              showHideFabs.setText(R.string.show_fabs_label);
            } else {
              fab.show();
              showHideFabs.setText(R.string.hide_fabs_label);
            }
          }
          fabsShown = !fabsShown;
        });

    Button spinFabs = view.findViewById(R.id.rotate_fabs);
    spinFabs.setOnClickListener(
        v -> {
          if (!fabsShown) {
            return;
          }

          for (FloatingActionButton fab : fabs) {
            fab.setRotation(0);
            fab.animate()
                .rotation(360)
                .withLayer()
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
          }
        });

    return view;
  }

  @LayoutRes
  protected int getFabsContent() {
    return R.layout.m3_fabs;
  }

  @LayoutRes
  protected int getThemeFabLayoutResId() {
    return R.layout.m3_theme_fab;
  }
}
