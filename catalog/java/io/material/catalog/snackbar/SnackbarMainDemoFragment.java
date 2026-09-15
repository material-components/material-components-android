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

package io.material.catalog.snackbar;

import io.material.catalog.R;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main Snackbar demos for the Catalog app. */
public class SnackbarMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    return layoutInflater.inflate(R.layout.cat_snackbar_main_demo_fragment, viewGroup, false);
  }

  @Override
  public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view, savedInstanceState);
    CoordinatorLayout coordinatorLayout = view.findViewById(R.id.coordinator_layout);

    // Default snackbar
    view.findViewById(R.id.default_button)
        .setOnClickListener(
            v -> {
              Snackbar.make(
                      coordinatorLayout,
                      R.string.cat_snackbar_default_message,
                      Snackbar.LENGTH_SHORT)
                  .show();
            });

    // Snackbar with single line
    view.findViewById(R.id.single_line_button)
        .setOnClickListener(
            v -> {
              Snackbar.make(
                      coordinatorLayout,
                      R.string.cat_snackbar_single_line_message,
                      Snackbar.LENGTH_SHORT)
                  .setTextMaxLines(1)
                  .show();
            });

    // Snackbar with action
    view.findViewById(R.id.with_action_button)
        .setOnClickListener(
            v -> {
              Snackbar.make(
                      coordinatorLayout,
                      R.string.cat_snackbar_with_action_message,
                      Snackbar.LENGTH_SHORT)
                  .setAction(R.string.cat_snackbar_action_title, a -> {})
                  .show();
            });

    // Snackbar with multiple lines
    view.findViewById(R.id.multi_line_button)
        .setOnClickListener(
            v -> {
              Snackbar.make(
                      coordinatorLayout,
                      R.string.cat_snackbar_multi_line_message,
                      Snackbar.LENGTH_SHORT)
                  .setAction(R.string.cat_snackbar_action_title, a -> {})
                  .setTextMaxLines(5)
                  .setCloseIconVisible(true)
                  .show();
            });

    // Snackbar with custom shape
    view.findViewById(R.id.custom_shape_button)
        .setOnClickListener(
            v -> {
              // Set a custom shape by updating the theme's snackbarStyle attribute to a
              // custom style with a custom shapeAppearance. For demonstration, this is done
              // on a single snackbar using a theme overlay but it is more typically done by
              // setting the snackbarStyle on th main theme and customizing all snackbars app-wide.
              Context c =
                  new ContextThemeWrapper(
                      v.getContext(), R.style.ThemeOverlay_Catalog_SnackbarWithCustomShape);
              Snackbar snackbar = Snackbar.make(
                      c,
                      coordinatorLayout,
                      getString(R.string.cat_snackbar_custom_shape_message),
                      Snackbar.LENGTH_SHORT)
                  .setAction("Done", a -> {})
                  .setCloseIconVisible(/* visible= */ true)
                  .setCloseIconResource(R.drawable.ic_cancel_24)
                  .setCloseIconTint(Color.GREEN);
              // Setting the close icon to visible removes the snackbar layout's end padding. To
              // customize this, get the snackbar's view and set the end padding to a value
              // that fits nicely with the custom shape and icon.
              View snackbarLayout = snackbar.getView();
              snackbarLayout.setPaddingRelative(
                  snackbarLayout.getPaddingStart(),
                  snackbarLayout.getPaddingTop(),
                  getResources().getDimensionPixelSize(
                      R.dimen.cat_snackbar_custom_shape_end_padding),
                  snackbarLayout.getPaddingBottom()
              );
              snackbar.show();
            });

    // Snackbar with close icon
    view.findViewById(R.id.with_close_button)
        .setOnClickListener(
            v -> {
              Snackbar.make(
                      coordinatorLayout,
                      R.string.cat_snackbar_with_close_message,
                      Snackbar.LENGTH_INDEFINITE)
                  .setAction(R.string.cat_snackbar_action_title, a -> {})
                  .setCloseIconVisible(true)
                  .show();
            });
  }
}
