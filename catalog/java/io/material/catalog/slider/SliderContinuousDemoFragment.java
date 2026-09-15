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

import static java.lang.Math.max;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.material.materialswitch.MaterialSwitch;
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

    Slider sliderOne = view.findViewById(R.id.slider_1);
    Slider sliderTwo = view.findViewById(R.id.slider_2);
    Slider sliderThree = view.findViewById(R.id.slider_3);

    MaterialSwitch switchButton = view.findViewById(R.id.switch_button);

    switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
      sliderOne.setEnabled(isChecked);
      sliderTwo.setEnabled(isChecked);
      sliderThree.setEnabled(isChecked);
    });

    setUpSlider(sliderOne, view.findViewById(R.id.value_1), "%.0f");
    setUpSlider(sliderTwo, view.findViewById(R.id.value_2), "%.0f");
    setUpSlider(sliderThree, view.findViewById(R.id.value_3), "%.2f");

    view.findViewById(R.id.add_tick).setOnClickListener(v -> {
      sliderOne.setContinuousModeTickCount(sliderOne.getContinuousModeTickCount() + 1);
      sliderTwo.setContinuousModeTickCount(sliderTwo.getContinuousModeTickCount() + 1);
      sliderThree.setContinuousModeTickCount(sliderThree.getContinuousModeTickCount() + 1);
    });

    view.findViewById(R.id.remove_tick).setOnClickListener(v -> {
      sliderOne.setContinuousModeTickCount(max(sliderOne.getContinuousModeTickCount() - 1, 0));
      sliderTwo.setContinuousModeTickCount(max(sliderTwo.getContinuousModeTickCount() - 1, 0));
      sliderThree.setContinuousModeTickCount(max(sliderThree.getContinuousModeTickCount() - 1, 0));
    });

    return view;
  }

  private void setUpSlider(Slider slider, TextView textView, final String valueFormat) {
    slider.addOnChangeListener((slider1, value, fromUser) -> textView
        .setText(String.format(valueFormat, value)));
    slider.setValue(slider.getValueFrom());
  }
}
