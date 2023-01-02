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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.Nullable;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.slider.BasicLabelFormatter;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;
import java.util.Arrays;
import java.util.List;

/**
 * Fragment to display a few basic uses of the {@link Slider} widget in discrete mode for the
 * Catalog app.
 */
public class SliderDiscreteDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater inflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        inflater.inflate(R.layout.cat_slider_demo_discrete, viewGroup, false /* attachToRoot */);

    Slider withLabelFormatter = view.findViewById(R.id.slider_4);
    withLabelFormatter.setLabelFormatter(new BasicLabelFormatter());
    List<Slider> sliders = Arrays.asList(
        view.findViewById(R.id.slider_1),
        view.findViewById(R.id.slider_2),
        view.findViewById(R.id.slider_3),
        withLabelFormatter,
        view.findViewById(R.id.slider_5),
        view.findViewById(R.id.slider_6)
    );
    MaterialSwitch switchButton = view.findViewById(R.id.switch_button);

    switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
      for (Slider slider : sliders) {
        slider.setEnabled(isChecked);
      }
    });

    return view;
  }
}
