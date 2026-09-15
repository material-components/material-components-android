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
package io.material.catalog.dockedtoolbar;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dockedtoolbar.DockedToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays a Docked Toolbar demo with text buttons for the Catalog app. */
public class DockedToolbarTextButtonDemoFragment extends DemoFragment {

  private DockedToolbarLayout dockedToolbar;

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getLayoutResId(), viewGroup, /* attachToRoot= */ false);
    Toolbar toolbar = view.findViewById(R.id.toolbar);
    dockedToolbar = view.findViewById(R.id.docked_toolbar);
    ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

    LinearLayout dockedToolbarChild = view.findViewById(R.id.docked_toolbar_child);

    View content = layoutInflater.inflate(getDockedToolbarContent(), dockedToolbarChild, /* attachToRoot= */ true);

    Button backButton = content.findViewById(R.id.docked_toolbar_back_button);
    Button nextButton = content.findViewById(R.id.docked_toolbar_next_button);
    setupSnackbarOnClick(backButton);
    setupSnackbarOnClick(nextButton);

    return view;
  }

  private void setupSnackbarOnClick(@NonNull View view) {
    view.setOnClickListener(
        v ->
            Snackbar.make(
                    dockedToolbar,
                    view.getContentDescription(),
                    Snackbar.LENGTH_SHORT)
                .setAnchorView(dockedToolbar)
                .show());
  }

  @LayoutRes
  protected int getLayoutResId() {
    return R.layout.cat_docked_toolbar_vibrant_fragment;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }

  @LayoutRes
  protected int getDockedToolbarContent() {
    return R.layout.cat_docked_toolbar_text_button_content;
  }
}
