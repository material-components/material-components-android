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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.progressindicator.ProgressIndicator;
import com.google.android.material.slider.Slider;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** The fragment demos determinate types of progress indicator. */
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
    List<ProgressIndicator> indicatorList =
        DemoUtils.findViewsWithType(view, ProgressIndicator.class);

    Slider slider = view.findViewById(R.id.slider);
    Button showButton = view.findViewById(R.id.show_button);
    Button hideButton = view.findViewById(R.id.hide_button);

    slider.addOnChangeListener(
        (sliderObj, value, fromUser) -> {
          for (ProgressIndicator progressIndicator : indicatorList) {
            progressIndicator.setProgressCompat((int) value, true);
          }
        });
    showButton.setOnClickListener(
        v -> {
          for (ProgressIndicator progressIndicator : indicatorList) {
            progressIndicator.show();
          }
        });
    hideButton.setOnClickListener(
        v -> {
          for (ProgressIndicator progressIndicator : indicatorList) {
            progressIndicator.hide();
          }
        });
  }
}
