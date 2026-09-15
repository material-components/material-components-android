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
import androidx.appcompat.widget.PopupMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the ChipGroup demos for the Catalog app. */
public class ChipGroupDemoFragment extends DemoFragment {

  private MaterialSwitch singleSelectionSwitch;

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getChipGroupContent(), viewGroup, false /* attachToRoot */);

    singleSelectionSwitch = view.findViewById(R.id.single_selection);
    MaterialSwitch selectionRequiredSwitch = view.findViewById(R.id.selection_required);

    ChipGroup reflowGroup = view.findViewById(R.id.reflow_group);
    ChipGroup scrollGroup = view.findViewById(R.id.scroll_group);

    singleSelectionSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          reflowGroup.setSingleSelection(isChecked);
          scrollGroup.setSingleSelection(isChecked);

          initChipGroup(reflowGroup, false);
          initChipGroup(scrollGroup, true);
        });

    selectionRequiredSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          reflowGroup.setSelectionRequired(isChecked);
          scrollGroup.setSelectionRequired(isChecked);

          initChipGroup(reflowGroup, false);
          initChipGroup(scrollGroup, true);
        });

    initChipGroup(reflowGroup, false);
    initChipGroup(scrollGroup, true);

    FloatingActionButton fab = view.findViewById(R.id.cat_chip_group_refresh);
    fab.setOnClickListener(
        v -> {
          // Reload the chip group UI.
          reflowGroup.setSingleLine(true);
          initChipGroup(reflowGroup, false);
          initChipGroup(scrollGroup, true);
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

  private void initChipGroup(ChipGroup chipGroup, boolean showMenu) {
    chipGroup.removeAllViews();

    Chip viewAllChip = new Chip(chipGroup.getContext());
    viewAllChip.setText(viewAllChip.getResources().getString(R.string.cat_chip_text_all));
    viewAllChip.setChipIconResource(R.drawable.ic_drawer_menu_open_24px);
    viewAllChip.setChipIconTint(viewAllChip.getTextColors());
    viewAllChip.setChipIconVisible(true);
    viewAllChip.setCheckable(!showMenu);
    chipGroup.addView(viewAllChip);

    PopupMenu menu;
    if (showMenu) {
      menu = new PopupMenu(viewAllChip.getContext(), viewAllChip);
      viewAllChip.setOnClickListener(v -> menu.show());
    } else {
      menu = null;
      viewAllChip.setOnClickListener(
          v -> {
            chipGroup.setSingleLine(!chipGroup.isSingleLine());
            viewAllChip.setChecked(!chipGroup.isSingleLine());
            chipGroup.requestLayout();
          });
    }

    boolean singleSelection = singleSelectionSwitch.isChecked();
    String[] textArray = getResources().getStringArray(R.array.cat_chip_group_text_array);
    for (int i = 0; i < textArray.length; i++) {
      Chip chip =
          (Chip) getLayoutInflater().inflate(getChipGroupItem(singleSelection), chipGroup, false);
      chip.setId(i);
      chip.setText(textArray[i]);
      chipGroup.addView(chip);

      if (menu != null) {
        menu.getMenu().add(Menu.NONE, i, i, textArray[i]);
        menu.getMenu().setGroupCheckable(Menu.NONE, true, singleSelection);
        menu.setOnMenuItemClickListener(
            menuItem -> {
              Chip targetChip = (Chip) chipGroup.getChildAt(menuItem.getOrder() + 1);
              targetChip.setChecked(!targetChip.isChecked());
              menuItem.setChecked(targetChip.isChecked());
              return true;
            });
        chip.setCloseIconVisible(false);
        chip.setOnCheckedChangeListener(
            (buttonView, isChecked) -> menu.getMenu().getItem(chip.getId()).setChecked(isChecked));
      } else {
        chip.setCloseIconVisible(true);
        chip.setOnCloseIconClickListener(v -> chipGroup.removeView(chip));
      }
    }
  }
}
