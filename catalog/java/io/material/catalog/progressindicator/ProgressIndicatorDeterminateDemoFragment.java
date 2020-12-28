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

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/**
 * This is the fragment to dome in details of different determinate progress indicators.
 *
 * <p>This demo includes multiple examples of determinate {@link LinearProgressIndicator} and {@link
 * CircularProgressIndicator} and the ability to:
 *
 * <ul>
 *   <li>Update the indicator with a specified progress.
 *   <li>Hide the indicator
 *   <li>Show the indicator
 * </ul>
 */
public class ProgressIndicatorDeterminateDemoFragment extends DemoFragment {

  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_determinate_fragment,
            viewGroup,
            false /* attachToRoot */);

    initialize(view);

    return view;
  }

  public void initialize(@NonNull View view) {
    List<LinearProgressIndicator> linearProgressIndicatorList =
        DemoUtils.findViewsWithType(view, LinearProgressIndicator.class);
    List<CircularProgressIndicator> circularProgressIndicatorList =
        DemoUtils.findViewsWithType(view, CircularProgressIndicator.class);

    Slider slider = view.findViewById(R.id.slider);
    Button showButton = view.findViewById(R.id.show_button);
    Button hideButton = view.findViewById(R.id.hide_button);

    slider.addOnChangeListener(
        (sliderObj, value, fromUser) -> {
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            indicator.setProgressCompat((int) value, true);
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            indicator.setProgressCompat((int) value, true);
          }
        });
    showButton.setOnClickListener(
        v -> {
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            indicator.show();
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            indicator.show();
          }
        });
    hideButton.setOnClickListener(
        v -> {
          for (LinearProgressIndicator indicator : linearProgressIndicatorList) {
            indicator.hide();
          }
          for (CircularProgressIndicator indicator : circularProgressIndicatorList) {
            indicator.hide();
          }
        });
  }
}
