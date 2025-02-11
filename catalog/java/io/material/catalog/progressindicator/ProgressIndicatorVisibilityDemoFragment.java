/*
 * Copyright 2023 The Android Open Source Project
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
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.slider.Slider;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * This is the fragment to demo different visibility change behaviors of {@link
 * LinearProgressIndicator} and {@link CircularProgressIndicator}.
 */
public class ProgressIndicatorVisibilityDemoFragment extends ProgressIndicatorDemoFragment {

  public static final int SHOW_NONE = 0;
  public static final int SHOW_OUTWARD = 1;
  public static final int SHOW_INWARD = 2;
  public static final int HIDE_NONE = 0;
  public static final int HIDE_OUTWARD = 1;
  public static final int HIDE_INWARD = 2;
  public static final int HIDE_ESCAPE = 3;

  private static final Map<String, Integer> showBehaviorCodes = new HashMap<>();
  private static final Map<String, Integer> hideBehaviorCodes = new HashMap<>();

  static {
    showBehaviorCodes.put("none", SHOW_NONE);
    showBehaviorCodes.put("outward", SHOW_OUTWARD);
    showBehaviorCodes.put("inward", SHOW_INWARD);
    hideBehaviorCodes.put("none", HIDE_NONE);
    hideBehaviorCodes.put("outward", HIDE_OUTWARD);
    hideBehaviorCodes.put("inward", HIDE_INWARD);
    hideBehaviorCodes.put("escape", HIDE_ESCAPE);
  }

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

    AutoCompleteTextView showBehaviorInput = view.findViewById(R.id.showBehaviorDropdown);
    showBehaviorInput.setOnItemClickListener(
        (parent, view12, position, id) -> {
          String selected = (String) showBehaviorInput.getAdapter().getItem(position);
          int showBehaviorCode = showBehaviorCodes.get(selected.toLowerCase(Locale.US));
          linearIndicator.setShowAnimationBehavior(showBehaviorCode);
          circularIndicator.setShowAnimationBehavior(showBehaviorCode);
        });
    AutoCompleteTextView hideBehaviorInput = view.findViewById(R.id.hideBehaviorDropdown);
    hideBehaviorInput.setOnItemClickListener(
        (parent, view1, position, id) -> {
          String selected = (String) hideBehaviorInput.getAdapter().getItem(position);
          int hideBehaviorCode = hideBehaviorCodes.get(selected.toLowerCase(Locale.US));
          linearIndicator.setHideAnimationBehavior(hideBehaviorCode);
          circularIndicator.setHideAnimationBehavior(hideBehaviorCode);
        });

    Button showButton = view.findViewById(R.id.showButton);
    showButton.setOnClickListener(
        v -> {
          if (linearIndicator.getVisibility() != View.VISIBLE) {
            linearIndicator.show();
          }
          if (circularIndicator.getVisibility() != View.VISIBLE) {
            circularIndicator.show();
          }
        });
    Button hideButton = view.findViewById(R.id.hideButton);
    hideButton.setOnClickListener(
        v -> {
          if (linearIndicator.getVisibility() == View.VISIBLE) {
            linearIndicator.hide();
          }
          if (circularIndicator.getVisibility() == View.VISIBLE) {
            circularIndicator.hide();
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
    return R.layout.cat_progress_indicator_visibility_controls;
  }
}
