/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.chip;

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.switchmaterial.SwitchMaterial;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the ChipGroup demos for the Catalog app. */
public class ChipGroupDemoFragment extends DemoFragment {

  private SwitchMaterial singleSelectionSwitch;
  private SwitchMaterial selectionRequiredSwitch;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getChipGroupContent(), viewGroup, false /* attachToRoot */);

    singleSelectionSwitch = view.findViewById(R.id.single_selection);
    selectionRequiredSwitch = view.findViewById(R.id.selection_required);

    ChipGroup reflowGroup = view.findViewById(R.id.reflow_group);
    ChipGroup scrollGroup = view.findViewById(R.id.scroll_group);

    singleSelectionSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          reflowGroup.setSingleSelection(isChecked);
          scrollGroup.setSingleSelection(isChecked);

          initChipGroup(reflowGroup);
          initChipGroup(scrollGroup);
        });

    selectionRequiredSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          reflowGroup.setSelectionRequired(isChecked);
          scrollGroup.setSelectionRequired(isChecked);

          initChipGroup(reflowGroup);
          initChipGroup(scrollGroup);
        });

    initChipGroup(reflowGroup);
    initChipGroup(scrollGroup);

    FloatingActionButton fab = view.findViewById(R.id.cat_chip_group_refresh);
    fab.setOnClickListener(
        v -> {
          // Reload the chip group UI.
          initChipGroup(reflowGroup);
          initChipGroup(scrollGroup);
        });
    return view;
  }

  @LayoutRes
  protected int getChipGroupContent() {
    return R.layout.cat_chip_group_fragment;
  }

  @LayoutRes
  protected int getChipGroupItem(boolean singleSelection) {
    return singleSelection
        ? R.layout.cat_chip_group_item_choice
        : R.layout.cat_chip_group_item_filter;
  }

  private void initChipGroup(ChipGroup chipGroup) {
    chipGroup.removeAllViews();

    boolean singleSelection = singleSelectionSwitch.isChecked();
    String[] textArray = getResources().getStringArray(R.array.cat_chip_group_text_array);
    for (String text : textArray) {
      Chip chip =
          (Chip) getLayoutInflater().inflate(getChipGroupItem(singleSelection), chipGroup, false);
      chip.setText(text);
      chip.setCloseIconVisible(true);
      chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
      chipGroup.addView(chip);
    }
  }
}
