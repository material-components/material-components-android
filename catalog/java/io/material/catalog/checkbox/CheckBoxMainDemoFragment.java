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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.checkbox.MaterialCheckBox.OnCheckedStateChangedListener;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main Checkbox demos for the Catalog app. */
public class CheckBoxMainDemoFragment extends DemoFragment {

  private boolean isUpdatingChildren = false;

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.cat_checkbox, viewGroup, false /* attachToRoot */);
    ViewGroup toggleContainer = view.findViewById(R.id.checkbox_toggle_container);
    List<CheckBox> toggledCheckBoxes = DemoUtils.findViewsWithType(toggleContainer, CheckBox.class);
    List<MaterialCheckBox> allCheckBoxes =
        DemoUtils.findViewsWithType(view, MaterialCheckBox.class);

    CheckBox checkBoxToggle = view.findViewById(R.id.checkbox_toggle);
    checkBoxToggle.setOnCheckedChangeListener(
        (CompoundButton buttonView, boolean isChecked) -> {
          for (CheckBox cb : toggledCheckBoxes) {
            cb.setEnabled(isChecked);
          }
        });

    CheckBox checkBoxToggleError = view.findViewById(R.id.checkbox_toggle_error);
    checkBoxToggleError.setOnCheckedChangeListener(
        (CompoundButton buttonView, boolean isChecked) -> {
          for (MaterialCheckBox cb : allCheckBoxes) {
            cb.setErrorShown(isChecked);
          }
        });

    CheckBox firstChild = view.findViewById(R.id.checkbox_child_1);
    firstChild.setChecked(true);
    ViewGroup indeterminateContainer = view.findViewById(R.id.checkbox_indeterminate_container);
    List<CheckBox> childrenCheckBoxes =
        DemoUtils.findViewsWithType(indeterminateContainer, CheckBox.class);
    MaterialCheckBox checkBoxParent = view.findViewById(R.id.checkbox_parent);
    OnCheckedStateChangedListener parentOnCheckedStateChangedListener =
            (checkBox, state) -> {
              boolean isChecked = checkBox.isChecked();
              if (state != MaterialCheckBox.STATE_INDETERMINATE) {
                isUpdatingChildren = true;
                for (CheckBox child : childrenCheckBoxes) {
                  child.setChecked(isChecked);
                }
                isUpdatingChildren = false;
              }
            };
    checkBoxParent.addOnCheckedStateChangedListener(parentOnCheckedStateChangedListener);

    OnCheckedStateChangedListener childOnCheckedStateChangedListener =
        (checkBox, state) -> {
          if (isUpdatingChildren) {
            return;
          }
          setParentState(checkBoxParent, childrenCheckBoxes, parentOnCheckedStateChangedListener);
        };

    for (CheckBox child : childrenCheckBoxes) {
      ((MaterialCheckBox) child)
          .addOnCheckedStateChangedListener(childOnCheckedStateChangedListener);
    }

    setParentState(checkBoxParent, childrenCheckBoxes, parentOnCheckedStateChangedListener);

    return view;
  }

  private void setParentState(
      @NonNull MaterialCheckBox checkBoxParent,
      @NonNull List<CheckBox> childrenCheckBoxes,
      @NonNull OnCheckedStateChangedListener parentOnCheckedStateChangedListener) {
    boolean allChecked = true;
    boolean noneChecked = true;
    for (CheckBox child : childrenCheckBoxes) {
      if (!child.isChecked()) {
        allChecked = false;
      } else {
        noneChecked = false;
      }
      if (!allChecked && !noneChecked) {
        break;
      }
    }
    checkBoxParent.removeOnCheckedStateChangedListener(parentOnCheckedStateChangedListener);
    if (allChecked) {
      checkBoxParent.setChecked(true);
    } else if (noneChecked) {
      checkBoxParent.setChecked(false);
    } else {
      checkBoxParent.setCheckedState(MaterialCheckBox.STATE_INDETERMINATE);
    }
    checkBoxParent.addOnCheckedStateChangedListener(parentOnCheckedStateChangedListener);
  }
}
