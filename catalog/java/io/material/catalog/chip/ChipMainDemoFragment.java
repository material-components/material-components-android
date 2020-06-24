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
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.switchmaterial.SwitchMaterial;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays the main Chip demos for the Catalog app. */
public class ChipMainDemoFragment extends DemoFragment {

  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(R.layout.cat_chip_fragment, viewGroup, false /* attachToRoot */);

    ViewGroup content = view.findViewById(R.id.content);
    View.inflate(getContext(), getChipContent(), content);

    List<Chip> chips = DemoUtils.findViewsWithType(view, Chip.class);
    for (Chip chip : chips) {
      chip.setOnCloseIconClickListener(
          v -> {
            Snackbar.make(view, "Clicked close icon.", Snackbar.LENGTH_SHORT).show();
          });
      if (chip.isEnabled() && !chip.isCheckable()) {
        chip.setOnClickListener(
            v -> {
              Snackbar.make(view, "Activated chip.", Snackbar.LENGTH_SHORT)
                  .show();
            });
      }
    }
    SwitchMaterial longTextSwitch = view.findViewById(R.id.cat_chip_text_length_switch);
    longTextSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          CharSequence updatedText =
              getText(isChecked ? R.string.cat_chip_text_to_truncate : R.string.cat_chip_text);
          for (Chip chip : chips) {
            chip.setText(updatedText);
          }
        });

    SwitchMaterial enabledSwitch = view.findViewById(R.id.cat_chip_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (Chip chip : chips) {
            chip.setEnabled(isChecked);
          }
        });
    return view;
  }

  @LayoutRes
  protected int getChipContent() {
    return R.layout.cat_chip_content;
  }
}
