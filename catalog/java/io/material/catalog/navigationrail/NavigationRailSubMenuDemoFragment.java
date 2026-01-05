/*
 * Copyright 2024 The Android Open Source Project
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

package io.material.catalog.navigationrail;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.widget.TooltipCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton;
import com.google.android.material.navigationrail.NavigationRailView;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A base class that provides a demo screen structure for a single navigation rail demo. */
public class NavigationRailSubMenuDemoFragment extends DemoFragment {

  @Nullable NavigationRailView navigationRailView;

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_navigation_rail_submenus_fragment, viewGroup, /* attachToRoot= */ false);
    navigationRailView = view.findViewById(R.id.cat_navigation_rail);
    // Add extended floating action button
    navigationRailView.addHeaderView(R.layout.cat_navigation_rail_efab_header_view);
    FrameLayout.LayoutParams lp =
        (LayoutParams) navigationRailView.getHeaderView().getLayoutParams();
    lp.gravity = Gravity.START;
    navigationRailView.getHeaderView().
        findViewById(R.id.cat_navigation_rail_efab_container)
        .setPadding(
            navigationRailView.getItemActiveIndicatorExpandedMarginHorizontal(),
            0,
            navigationRailView.getItemActiveIndicatorExpandedMarginHorizontal(),
            0);

    ExtendedFloatingActionButton efab =
        navigationRailView.getHeaderView().findViewById(R.id.cat_navigation_rail_efab);
    efab.setAnimationEnabled(false);
    efab.setExtended(false);
    efab.setOnClickListener(v ->
      Snackbar.make(v, R.string.cat_navigation_rail_efab_message, Snackbar.LENGTH_SHORT)
          .show());
    TooltipCompat.setTooltipText(efab, efab.getContentDescription());

    ImageView button =
        navigationRailView.getHeaderView().findViewById(R.id.cat_navigation_rail_expand_button);
    String expandButtonContentDescription =
        getResources().getString(R.string.cat_navigation_rail_expand_button_description);
    button.setContentDescription(expandButtonContentDescription);
    TooltipCompat.setTooltipText(button, expandButtonContentDescription);
    button.setOnClickListener(
        v -> {
          if (efab.isExtended()) {
            efab.shrink();
            navigationRailView.collapse();
            button.setContentDescription(getResources().getString(R.string.cat_navigation_rail_expand_button_description));
            button.setImageResource(R.drawable.ic_drawer_menu_24px);
          } else {
            efab.extend();
            navigationRailView.expand();
            button.setContentDescription(getResources().getString(R.string.cat_navigation_rail_collapse_button_description));
            button.setImageResource(R.drawable.ic_drawer_menu_open_24px);
          }
          TooltipCompat.setTooltipText(button, button.getContentDescription());
        });
    return view;
  }
}
