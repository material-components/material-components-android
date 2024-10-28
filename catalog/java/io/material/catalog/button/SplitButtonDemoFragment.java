/*
 * Copyright 2024 The Android Open Source Project
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

package io.material.catalog.button;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialSplitButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a split button demo for the Catalog app. */
public class SplitButtonDemoFragment extends DemoFragment {

  /** Create a Demo View with different types of {@link MaterialSplitButton} */
  @Nullable
  @Override
  public View onCreateDemoView(
      @Nullable LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(getSplitButtonContent(), viewGroup, /* attachToRoot= */ false);
    List<MaterialSplitButton> splitButtons =
        DemoUtils.findViewsWithType(view, MaterialSplitButton.class);
    MaterialSwitch enabledToggle = view.findViewById(R.id.switch_enable);
    enabledToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialSplitButton splitButton : splitButtons) {
            // Enable the SplitButton if enable toggle is checked.
            splitButton.setEnabled(isChecked);
          }
        });
    return view;
  }

  @LayoutRes
  protected int getSplitButtonContent() {
    return R.layout.cat_split_button_fragment;
  }
}
