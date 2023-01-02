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
import android.widget.Button;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.slider.RangeSlider;
import com.google.android.material.slider.Slider;
import com.google.android.material.slider.Slider.OnSliderTouchListener;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main Slider demo for the Catalog app. */
public class SliderMainDemoFragment extends DemoFragment {

  private final OnSliderTouchListener touchListener =
      new OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(Slider slider) {
          showSnackbar(slider, R.string.cat_slider_start_touch_description);
        }

        @Override
        public void onStopTrackingTouch(Slider slider) {
          showSnackbar(slider, R.string.cat_slider_stop_touch_description);
        }
      };

  private final RangeSlider.OnSliderTouchListener rangeSliderTouchListener =
      new RangeSlider.OnSliderTouchListener() {
        @Override
        public void onStartTrackingTouch(RangeSlider slider) {
          showSnackbar(slider, R.string.cat_slider_start_touch_description);
        }

        @Override
        public void onStopTrackingTouch(RangeSlider slider) {
          showSnackbar(slider, R.string.cat_slider_stop_touch_description);
        }
      };

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_slider_fragment, viewGroup, false /* attachToRoot */);

    Slider slider = view.findViewById(R.id.slider);
    slider.addOnSliderTouchListener(touchListener);

    RangeSlider rangeSlider = view.findViewById(R.id.range_slider);
    rangeSlider.addOnSliderTouchListener(rangeSliderTouchListener);

    Button button = view.findViewById(R.id.button);
    button.setOnClickListener(v -> slider.setValue(slider.getValueTo()));

    return view;
  }

  private static void showSnackbar(View view, @StringRes int messageRes) {
    Snackbar.make(view, messageRes, Snackbar.LENGTH_SHORT).show();
  }
}
