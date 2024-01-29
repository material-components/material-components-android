/*
 * Copyright 2020 The Android Open Source Project
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
package io.material.catalog.progressindicator;

import io.material.catalog.R;

import static com.google.android.material.progressindicator.CircularProgressIndicator.INDICATOR_DIRECTION_CLOCKWISE;
import static com.google.android.material.progressindicator.CircularProgressIndicator.INDICATOR_DIRECTION_COUNTERCLOCKWISE;
import static com.google.android.material.progressindicator.LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT;
import static com.google.android.material.progressindicator.LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/**
 * This is the fragment to demo simple use cases of {@link LinearProgressIndicator} and {@link
 * CircularProgressIndicator}.
 */
public class ProgressIndicatorMainDemoFragment extends DemoFragment {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {

    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_main_fragment, viewGroup, false /* attachToRoot */);

    initialize(view);

    return view;
  }

  public void initialize(@NonNull View view) {
    LinearProgressIndicator linearIndicator = view.findViewById(R.id.linear_indicator);
    CircularProgressIndicator circularIndicator = view.findViewById(R.id.circular_indicator);
    Slider progressSlider = view.findViewById(R.id.progress_slider);
    MaterialSwitch determinateSwitch = view.findViewById(R.id.determinate_mode_switch);

    progressSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          if (!linearIndicator.isIndeterminate()) {
            linearIndicator.setProgressCompat((int) value, true);
          }
          if (!circularIndicator.isIndeterminate()) {
            circularIndicator.setProgressCompat((int) value, true);
          }
        });
    determinateSwitch.setOnCheckedChangeListener(
        (v, isChecked) -> {
          if (isChecked) {
            float progress = progressSlider.getValue();
            linearIndicator.setProgressCompat((int) progress, true);
            circularIndicator.setProgressCompat((int) progress, true);
          } else {
            linearIndicator.setProgressCompat(0, false);
            circularIndicator.setProgressCompat(0, false);
            linearIndicator.setIndeterminate(true);
            circularIndicator.setIndeterminate(true);
          }
        });

    float pixelsInDp = view.getResources().getDisplayMetrics().density;

    Slider thicknessSlider = view.findViewById(R.id.thicknessSlider);
    thicknessSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newThickness = (int) (value * pixelsInDp);
          if (linearIndicator.getTrackThickness() != newThickness) {
            linearIndicator.setTrackThickness(newThickness);
          }
          if (circularIndicator.getTrackThickness() != newThickness) {
            circularIndicator.setTrackThickness(newThickness);
          }
        });

    Slider cornerSlider = view.findViewById(R.id.cornerSlider);
    cornerSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newCornerRadius = (int) (value * pixelsInDp);
          if (linearIndicator.getTrackCornerRadius() != newCornerRadius) {
            linearIndicator.setTrackCornerRadius(newCornerRadius);
          }
          if (circularIndicator.getTrackCornerRadius() != newCornerRadius) {
            circularIndicator.setTrackCornerRadius(newCornerRadius);
          }
        });

    Slider gapSlider = view.findViewById(R.id.gapSlider);
    gapSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newGapSize = (int) (value * pixelsInDp);
          if (linearIndicator.getIndicatorTrackGapSize() != newGapSize) {
            linearIndicator.setIndicatorTrackGapSize(newGapSize);
          }
          if (circularIndicator.getIndicatorTrackGapSize() != newGapSize) {
            circularIndicator.setIndicatorTrackGapSize(newGapSize);
          }
        });

    MaterialSwitch reverseSwitch = view.findViewById(R.id.reverseSwitch);
    reverseSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          linearIndicator.setIndicatorDirection(
              isChecked ? INDICATOR_DIRECTION_RIGHT_TO_LEFT : INDICATOR_DIRECTION_LEFT_TO_RIGHT);
          circularIndicator.setIndicatorDirection(
              isChecked ? INDICATOR_DIRECTION_COUNTERCLOCKWISE : INDICATOR_DIRECTION_CLOCKWISE);
        });

    Slider linearStopIndicatorSlider = view.findViewById(R.id.linearStopIndicatorSizeSlider);
    linearStopIndicatorSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newStopIndicatorSize = (int) (value * pixelsInDp);
          if (linearIndicator.getTrackStopIndicatorSize() != newStopIndicatorSize) {
            linearIndicator.setTrackStopIndicatorSize(newStopIndicatorSize);
          }
        });

    Slider circularSizeSlider = view.findViewById(R.id.circularSizeSlider);
    circularSizeSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newCornerRadius = (int) (value * pixelsInDp);
          if (circularIndicator.getIndicatorSize() != newCornerRadius) {
            circularIndicator.setIndicatorSize(newCornerRadius);
          }
        });
  }
}
