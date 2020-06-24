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

package io.material.catalog.topappbar;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a Toolbar Top App Bar demo for the Catalog app. */
public class TopAppBarToolbarDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(getToolbarLayoutResId(), viewGroup, /* attachToRoot= */ false);

    List<MaterialToolbar> toolbars = DemoUtils.findViewsWithType(view, MaterialToolbar.class);
    for (MaterialToolbar toolbar : toolbars) {
      initToolbar(view, toolbar);
    }

    return view;
  }

  @LayoutRes
  protected int getToolbarLayoutResId() {
    return R.layout.cat_topappbar_toolbar_fragment;
  }

  private void initToolbar(View view, MaterialToolbar toolbar) {
    toolbar.setNavigationIcon(R.drawable.ic_close_vd_theme_24px);
    toolbar.setNavigationOnClickListener(
        v -> showSnackbar(view, toolbar.getSubtitle() + " " + toolbar.getTitle()));
    toolbar.inflateMenu(R.menu.cat_topappbar_menu);
    toolbar.setOnMenuItemClickListener(
        menuItem -> {
          showSnackbar(view, menuItem.getTitle());
          return true;
        });
  }

  private void showSnackbar(View view, CharSequence text) {
    Snackbar.make(view, text, Snackbar.LENGTH_SHORT).show();
  }
}
