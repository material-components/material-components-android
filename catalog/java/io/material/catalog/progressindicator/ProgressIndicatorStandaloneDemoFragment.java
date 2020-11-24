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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;
import com.google.android.material.chip.Chip;
import com.google.android.material.progressindicator.CircularProgressIndicatorSpec;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.switchmaterial.SwitchMaterial;
import io.material.catalog.feature.DemoFragment;

/**
 * The fragment demos progress indicator drawables used as standalone drawables in other components.
 */
public class ProgressIndicatorStandaloneDemoFragment extends DemoFragment {

  @SuppressWarnings("RestrictTo")
  @Override
  @NonNull
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_progress_indicator_standalone_fragment,
            viewGroup,
            /*attachToRoot=*/ false);

    CircularProgressIndicatorSpec spec =
        new CircularProgressIndicatorSpec(getContext(), /*attrs=*/ null, 0, getSpecStyleResId());
    IndeterminateDrawable<CircularProgressIndicatorSpec> progressIndicatorDrawable =
        IndeterminateDrawable.createCircularDrawable(getContext(), spec);

    Chip chip = view.findViewById(R.id.cat_progress_indicator_chip);
    chip.setChipIcon(progressIndicatorDrawable);

    SwitchMaterial chipIconSwitch =
        view.findViewById(R.id.cat_progress_indicator_standalone_chip_switch);
    chipIconSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          chip.setChipIconVisible(isChecked);
        });
    return view;
  }

  @StyleRes
  protected int getSpecStyleResId() {
    return R.style.Widget_MaterialComponents_CircularProgressIndicator_ExtraSmall;
  }
}
