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

package io.material.catalog.bottomnav;

import io.material.catalog.R;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.bottomnavigation.LabelVisibilityMode;

/** A fragment that displays controls for the bottom nav's label visibility. */
public class BottomNavigationLabelVisibilityDemoFragment extends BottomNavigationDemoFragment {
  @Override
  protected void initBottomNavDemoControls(View view) {
    super.initBottomNavDemoControls(view);
    initLabelVisibilityModeButtons(view);
    initIconSlider(view);
  }

  @Override
  protected int getBottomNavDemoControlsLayout() {
    return R.layout.cat_bottom_navs_label_visibility_controls;
  }

  private void setAllBottomNavsLabelVisibilityMode(@LabelVisibilityMode int labelVisibilityMode) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      setBottomNavsLabelVisibilityMode(bn, labelVisibilityMode);
    }
  }

  private void setBottomNavsLabelVisibilityMode(
      BottomNavigationView bn, @LabelVisibilityMode int labelVisibilityMode) {
    bn.setLabelVisibilityMode(labelVisibilityMode);
  }

  private void setAllBottomNavsIconSize(int size) {
    for (BottomNavigationView bn : bottomNavigationViews) {
      bn.setItemIconSize(size);
    }
  }

  private void initLabelVisibilityModeButtons(View view) {
    initLabelVisibilityModeButton(
        view.findViewById(R.id.label_mode_auto_button), LabelVisibilityMode.LABEL_VISIBILITY_AUTO);
    initLabelVisibilityModeButton(
        view.findViewById(R.id.label_mode_selected_button),
        LabelVisibilityMode.LABEL_VISIBILITY_SELECTED);
    initLabelVisibilityModeButton(
        view.findViewById(R.id.label_mode_labeled_button),
        LabelVisibilityMode.LABEL_VISIBILITY_LABELED);
    initLabelVisibilityModeButton(
        view.findViewById(R.id.label_mode_unlabeled_button),
        LabelVisibilityMode.LABEL_VISIBILITY_UNLABELED);
  }

  private void initLabelVisibilityModeButton(
      Button labelVisibilityModeButton, @LabelVisibilityMode int labelVisibilityMode) {
    labelVisibilityModeButton.setOnClickListener(
        v -> setAllBottomNavsLabelVisibilityMode(labelVisibilityMode));
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
            setAllBottomNavsIconSize(
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
