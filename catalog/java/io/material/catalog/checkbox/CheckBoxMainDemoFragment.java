/*
 * Copyright 2018 The Android Open Source Project
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

package io.material.catalog.checkbox;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import androidx.annotation.Nullable;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main Checkbox demos for the Catalog app. */
public class CheckBoxMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_checkbox, viewGroup, false /* attachToRoot */);
    ViewGroup checkBoxDemoViewGroup = view.findViewById(R.id.main_viewGroup);
    View toggledView =
        layoutInflater.inflate(R.layout.cat_checkbox_toggled, checkBoxDemoViewGroup, false);
    checkBoxDemoViewGroup.addView(toggledView);
    List<CheckBox> toggledCheckBoxes =
        DemoUtils.<CheckBox>findViewsWithType(toggledView, CheckBox.class);

    CheckBox checkBoxToggle = view.findViewById(R.id.checkbox_toggle);
    checkBoxToggle.setOnCheckedChangeListener(
        (CompoundButton buttonView, boolean isChecked) -> {
          for (CheckBox cb : toggledCheckBoxes) {
            cb.setEnabled(isChecked);
          }
        });

    return view;
  }
}
