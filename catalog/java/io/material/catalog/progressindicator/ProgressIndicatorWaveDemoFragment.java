/*
 * Copyright 2024 The Android Open Source Project
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

import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;

/**
 * This is the fragment to demo wave effects in {@link LinearProgressIndicator} and {@link
 * CircularProgressIndicator}.
 */
public class ProgressIndicatorWaveDemoFragment extends ProgressIndicatorDemoFragment {

  @NonNull private LinearProgressIndicator linearIndicator;
  @NonNull private CircularProgressIndicator circularIndicator;

  @Override
  public void initDemoContents(@NonNull View view) {
    linearIndicator = view.findViewById(R.id.linear_indicator);
    circularIndicator = view.findViewById(R.id.circular_indicator);
  }

  @Override
  public void initDemoControls(@NonNull View view) {
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

    Slider amplitudeSlider = view.findViewById(R.id.amplitude_slider);
    amplitudeSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newAmplitude = (int) (value * pixelsInDp);
          if (linearIndicator.getAmplitude() != newAmplitude) {
            linearIndicator.setAmplitude(newAmplitude);
          }
          if (circularIndicator.getAmplitude() != newAmplitude) {
            circularIndicator.setAmplitude((int) (value * pixelsInDp));
          }
        });
    Slider waveLengthSlider = view.findViewById(R.id.wavelength_slider);
    waveLengthSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newWaveLength = (int) (value * pixelsInDp);
          if (linearIndicator.getWavelength() != newWaveLength) {
            linearIndicator.setWavelength(newWaveLength);
          }
          if (circularIndicator.getWavelength() != newWaveLength) {
            circularIndicator.setWavelength(newWaveLength);
          }
        });
    Slider speedSlider = view.findViewById(R.id.speed_slider);
    speedSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          int newSpeed = (int) (value * pixelsInDp);
          if (linearIndicator.getSpeed() != newSpeed) {
            linearIndicator.setSpeed(newSpeed);
          }
          if (circularIndicator.getSpeed() != newSpeed) {
            circularIndicator.setSpeed(newSpeed);
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

  @Override
  @LayoutRes
  public int getProgressIndicatorContentLayout() {
    return R.layout.cat_progress_indicator_main_content;
  }

  @Override
  @LayoutRes
  public int getProgressIndicatorDemoControlLayout() {
    return R.layout.cat_progress_indicator_wave_controls;
  }
}
