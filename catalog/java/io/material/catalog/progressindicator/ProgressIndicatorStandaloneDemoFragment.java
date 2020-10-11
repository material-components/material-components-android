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
import com.google.android.material.chip.Chip;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.progressindicator.CircularDrawingDelegate;
import com.google.android.material.progressindicator.CircularIndeterminateAnimatorDelegate;
import com.google.android.material.progressindicator.IndeterminateDrawable;
import com.google.android.material.progressindicator.ProgressIndicatorSpec;
import com.google.android.material.switchmaterial.SwitchMaterial;
import io.material.catalog.feature.DemoFragment;

/**
 * The fragment demos progress indicator drawables used as standalone drawables in other components.
 */
public class ProgressIndicatorStandaloneDemoFragment extends DemoFragment {

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

    ProgressIndicatorSpec progressIndicatorSpec = new ProgressIndicatorSpec();
    progressIndicatorSpec.loadFromAttributes(
        getContext(),
        /*attrs=*/null,
        /*defStyleAttr=*/0,
        /*defStyleRes=*/R.style.Widget_MaterialComponents_ProgressIndicator_Circular_Indeterminate);
    progressIndicatorSpec.circularInset = 0; // No inset.
    progressIndicatorSpec.circularRadius =
        (int) ViewUtils.dpToPx(getContext(), 10); // Circular radius is 10 dp.
    IndeterminateDrawable progressIndicatorDrawable =
        new IndeterminateDrawable(
            getContext(),
            progressIndicatorSpec,
            new CircularDrawingDelegate(progressIndicatorSpec),
            new CircularIndeterminateAnimatorDelegate(progressIndicatorSpec));

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
}
