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
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.LayoutRes;
import androidx.annotation.MenuRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.dockedtoolbar.DockedToolbarLayout;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main Docked Toolbar demo for the Catalog app. */
public class DockedToolbarMainDemoFragment extends DemoFragment {

  private DockedToolbarLayout dockedToolbar;

  @NonNull
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {

    View view = layoutInflater.inflate(getLayoutResId(), viewGroup, /* attachToRoot= */ false);
    Toolbar toolbar = view.findViewById(R.id.toolbar);
    dockedToolbar = view.findViewById(R.id.docked_toolbar);
    ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

    Button leftArrowButton = view.findViewById(R.id.docked_toolbar_left_arrow_button);
    Button rightArrowButton = view.findViewById(R.id.docked_toolbar_right_arrow_button);
    Button addButton = view.findViewById(R.id.docked_toolbar_add_button);
    Button tabButton = view.findViewById(R.id.docked_toolbar_tab_button);
    setupSnackbarOnClick(leftArrowButton);
    setupSnackbarOnClick(rightArrowButton);
    setupSnackbarOnClick(addButton);
    setupSnackbarOnClick(tabButton);

    Button overflowClick = view.findViewById(R.id.docked_toolbar_button_overflow_button);

    overflowClick.setOnClickListener(v -> showMenu(v, R.menu.overflow_menu));

    return view;
  }

  private void showMenu(View v, @MenuRes int menuRes) {
    PopupMenu popup = new PopupMenu(getContext(), v);
    // Inflating the Popup using xml file
    popup.getMenuInflater().inflate(menuRes, popup.getMenu());
    popup.setOnMenuItemClickListener(
        menuItem -> {
          Snackbar.make(
              dockedToolbar,
                  menuItem.getTitle(),
                  Snackbar.LENGTH_SHORT)
              .setAnchorView(dockedToolbar)
              .show();
          return true;
        });
    popup.show();
  }

  private void setupSnackbarOnClick(View view) {
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
    return R.layout.cat_docked_toolbar_fragment;
  }

  @Override
  public boolean shouldShowDefaultDemoActionBar() {
    return false;
  }
}
