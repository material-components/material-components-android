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

import android.view.View;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;

/**
 * The fragment demos progress indicator drawables used as standalone drawables in other components.
 */
public class ProgressIndicatorStandaloneDemoFragment extends ProgressIndicatorDemoFragment {

  @Override
  public void initDemoContents(@NonNull View view) {
    CircularProgressIndicatorSpec spec =
        new CircularProgressIndicatorSpec(getContext(), /* attrs= */ null, 0, getSpecStyleResId());
    Chip chip = view.findViewById(R.id.cat_progress_indicator_chip);
    chip.setChipIcon(IndeterminateDrawable.createCircularDrawable(getContext(), spec));

    IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicatorDrawable =
        IndeterminateDrawable.createCircularDrawable(getContext(), spec);
    MaterialButton button = view.findViewById(R.id.cat_progress_indicator_button);
    button.setIcon(progressIndicatorDrawable);

    MaterialSwitch chipIconSwitch =
        view.findViewById(R.id.cat_progress_indicator_standalone_chip_switch);
    chipIconSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          chip.setChipIconVisible(isChecked);
          button.setIcon(isChecked ? progressIndicatorDrawable : null);
        });
  }

  @Override
  @LayoutRes
  public int getProgressIndicatorContentLayout() {
    return R.layout.cat_progress_indicator_standalone_content;
  }

  @StyleRes
  protected int getSpecStyleResId() {
    return com.google.android.material.R.style.Widget_Material3_CircularProgressIndicator_ExtraSmall;
  }
}
