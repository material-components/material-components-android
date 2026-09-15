/*
 * Copyright 2021 The Android Open Source Project
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

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_AUTO;
import static com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_LABELED;
import static com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_SELECTED;
import static com.google.android.material.navigation.NavigationBarView.LABEL_VISIBILITY_UNLABELED;

import androidx.appcompat.widget.TooltipCompat;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/** A fragment that displays controls for the bottom nav's label visibility. */
public class NavigationRailDemoControlsFragment extends NavigationRailDemoFragment {
  private static final int MENU_GRAVITY_TOP = Gravity.TOP | Gravity.CENTER_HORIZONTAL;
  private static final int MENU_GRAVITY_CENTER = Gravity.CENTER;
  private static final int MENU_GRAVITY_BOTTOM = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;

  @Override
  protected void initNavigationRailDemoControls(@NonNull View view) {
    super.initNavigationRailDemoControls(view);
    initAddRemoveHeaderViewButtons(view);
    initMenuGravityButtons(view);
    initLabelVisibilityModeButtons(view);
    initIconSlider(view);
  }

  @Override
  protected int getNavigationRailDemoControlsLayout() {
    return R.layout.cat_navigation_demo_controls;
  }

  @Override
  public int getLiftOnScrollTargetViewId() {
    return R.id.cat_navigation_rail_nested_scroll_view;
  }

  private void initAddRemoveHeaderViewButtons(View view) {
    final Button addHeaderViewButton = view.findViewById(R.id.add_header_view_button);
    final Button removeHeaderViewButton = view.findViewById(R.id.remove_header_view_button);
    addHeaderViewButton.setOnClickListener(
        v -> {
          navigationRailView.addHeaderView(R.layout.cat_navigation_rail_header_view);
          addHeaderViewButton.setVisibility(GONE);
          removeHeaderViewButton.setVisibility(VISIBLE);
          FloatingActionButton fab = navigationRailView.findViewById(R.id.cat_navigation_rail_fab);
          TooltipCompat.setTooltipText(fab, fab.getContentDescription());
        });
    removeHeaderViewButton.setOnClickListener(
        v -> {
          navigationRailView.removeHeaderView();
          addHeaderViewButton.setVisibility(VISIBLE);
          removeHeaderViewButton.setVisibility(GONE);
        });
  }

  private void initMenuGravityButtons(View view) {
    setMenuGravityClickListener(view, R.id.menu_gravity_top_button, MENU_GRAVITY_TOP);
    setMenuGravityClickListener(view, R.id.menu_gravity_center_button, MENU_GRAVITY_CENTER);
    setMenuGravityClickListener(view, R.id.menu_gravity_bottom_button, MENU_GRAVITY_BOTTOM);
  }

  private void setMenuGravityClickListener(View view, int buttonId, int gravity) {
    view.findViewById(buttonId).setOnClickListener(v -> navigationRailView.setMenuGravity(gravity));
  }

  private void initLabelVisibilityModeButtons(View view) {
    setLabelVisibilityClickListener(view, R.id.label_mode_auto_button, LABEL_VISIBILITY_AUTO);
    setLabelVisibilityClickListener(
        view, R.id.label_mode_selected_button, LABEL_VISIBILITY_SELECTED);
    setLabelVisibilityClickListener(view, R.id.label_mode_labeled_button, LABEL_VISIBILITY_LABELED);
    setLabelVisibilityClickListener(
        view, R.id.label_mode_unlabeled_button, LABEL_VISIBILITY_UNLABELED);
  }

  private void setLabelVisibilityClickListener(View view, int buttonId, int mode) {
    view.findViewById(buttonId)
        .setOnClickListener(v -> navigationRailView.setLabelVisibilityMode(mode));
  }

  private void initIconSlider(View view) {
    SeekBar iconSizeSlider = view.findViewById(R.id.icon_size_slider);
    DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
    TextView iconSizeTextView = view.findViewById(R.id.icon_size_text_view);
    String iconSizeUnit = "dp";

    iconSizeSlider.setOnSeekBarChangeListener(
        new OnSeekBarChangeListener() {
          @Override
          public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            navigationRailView.setItemIconSize(
                (int)
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP, progress, displayMetrics));
            iconSizeTextView.setText(String.valueOf(progress).concat(iconSizeUnit));
          }

          @Override
          public void onStartTrackingTouch(SeekBar seekBar) {}

          @Override
          public void onStopTrackingTouch(SeekBar seekBar) {}
        });
  }
}
