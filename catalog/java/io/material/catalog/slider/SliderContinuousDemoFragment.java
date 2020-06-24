/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.slider;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/**
 * Fragment to display a few basic uses of the {@link Slider} widget in continuous mode for the
 * Catalog app.
 */
public class SliderContinuousDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = inflater.inflate(R.layout.cat_slider_demo_continuous, viewGroup, false);

    setUpSlider(view, R.id.switch_button_1, R.id.slider_1, R.id.value_1, "%.0f");
    setUpSlider(view, R.id.switch_button_2, R.id.slider_2, R.id.value_2, "%.0f");
    setUpSlider(view, R.id.switch_button_3, R.id.slider_3, R.id.value_3, "%.2f");

    return view;
  }

  private void setUpSlider(
      View view,
      @IdRes int switchId,
      @IdRes int sliderId,
      @IdRes int valueId,
      final String valueFormat) {
    final TextView sliderValue = view.findViewById(valueId);
    final Slider slider = view.findViewById(sliderId);
    slider.addOnChangeListener(
        (slider1, value, fromUser) -> sliderValue.setText(String.format(valueFormat, value)));
    slider.setValue(slider.getValueFrom());
    SwitchCompat switchButton = view.findViewById(switchId);
    switchButton.setOnCheckedChangeListener(
        (buttonView, isChecked) -> slider.setEnabled(isChecked));
    switchButton.setChecked(true);
  }
}
