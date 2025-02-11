/*
 * Copyright 2019 The Android Open Source Project
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

package io.material.catalog.slider;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import androidx.annotation.Nullable;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;

/**
 * A fragment that displays a list of {@link Slider} inside a ScrollView to showcase how touch
 * events are handled.
 */
public class SliderScrollContainerDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_slider_demo_scroll, viewGroup, false /* attachToRoot */);
    LinearLayout sliderContainer = view.findViewById(R.id.sliderContainer);
    for (int i = 0; i < 50; i++) {
      Slider slider = new Slider(layoutInflater.getContext());
      slider.setValueTo(11f);
      sliderContainer.addView(
          slider,
          ViewGroup.LayoutParams.MATCH_PARENT,
          ViewGroup.LayoutParams.WRAP_CONTENT);
    }
    return view;
  }
}
