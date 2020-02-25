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

import io.material.catalog.R;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the ChipGroup Max row demos for the Catalog app. */
public class ChipGroupMaxRowsDemoFragment extends DemoFragment {

  private SwitchMaterial singleSelectionSwitch;
  private SwitchMaterial overflowEnabledSwitch;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getChipGroupContent(), viewGroup, false /* attachToRoot */);

    overflowEnabledSwitch = view.findViewById(R.id.overflow_enabled);
    ChipGroup maxRowflowGroup = view.findViewById(R.id.maxrow_group);

    overflowEnabledSwitch.setOnCheckedChangeListener(
      (buttonView, isChecked) -> {
        maxRowflowGroup.setSingleSelection(isChecked);
        maxRowflowGroup.setOverflowChipEnabled(isChecked);

        initChipGroup(maxRowflowGroup);
      });

    initChipGroup(maxRowflowGroup);

    FloatingActionButton fab = view.findViewById(R.id.cat_chip_group_refresh);
    fab.setOnClickListener(
        v -> {
          // Reload the chip group UI.
            initChipGroup(maxRowflowGroup);
        });
    return view;
  }

  @LayoutRes
  protected int getChipGroupContent() {
    return R.layout.cat_chip_group_max_rows_fragment;
  }

  @LayoutRes
  protected int getChipGroupItem() {
    return R.layout.cat_chip_group_item_entry;
  }

  private void initChipGroup(ChipGroup chipGroup) {
    chipGroup.removeAllViews();

    String[] textArray = getResources().getStringArray(R.array.cat_chip_group_text_array);
    for (String text : textArray) {
        Chip chip = (Chip) getLayoutInflater().inflate(getChipGroupItem(), chipGroup, false);
        chip.setText(text);
        chipGroup.addView(chip);
    }
  }
}
