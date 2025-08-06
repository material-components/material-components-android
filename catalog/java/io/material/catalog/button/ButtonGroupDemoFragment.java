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

import static android.widget.LinearLayout.HORIZONTAL;
import static android.widget.LinearLayout.VERTICAL;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a button group demo for the Catalog app. */
public class ButtonGroupDemoFragment extends DemoFragment {

  /** Create a Demo View with different types of {@link MaterialButtonGroup}. */
  @Nullable
  @Override
  public View onCreateDemoView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_buttons_group_fragment, viewGroup, /* attachToRoot= */ false);

    ViewGroup content = view.findViewById(R.id.button_group_content);
    layoutInflater.inflate(getIconOnlyButtonGroupContent(), content, /* attachToRoot= */ true);
    layoutInflater.inflate(getLabelOnlyButtonGroupContent(), content, /* attachToRoot= */ true);
    layoutInflater.inflate(getMixedButtonGroupContent(), content, /* attachToRoot= */ true);

    List<MaterialButton> buttons = DemoUtils.findViewsWithType(view, MaterialButton.class);
    List<MaterialButtonGroup> buttonGroups =
        DemoUtils.findViewsWithType(view, MaterialButtonGroup.class);

    MaterialSwitch verticalOrientationToggle = view.findViewById(R.id.orientation_switch_toggle);
    verticalOrientationToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonGroup buttonGroup : buttonGroups) {
            int orientation = isChecked ? VERTICAL : HORIZONTAL;
            buttonGroup.setOrientation(orientation);
            buttonGroup.requestLayout();
          }
        });
    MaterialSwitch groupToggleableToggle = view.findViewById(R.id.switch_toggle);
    groupToggleableToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButton button : buttons) {
            if (button.getTag() != MaterialButtonGroup.OVERFLOW_BUTTON_TAG) {
              button.setCheckable(isChecked);
              button.refreshDrawableState();
            }
          }
        });
    MaterialSwitch groupEnabledToggle = view.findViewById(R.id.switch_enable);
    groupEnabledToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonGroup buttonGroup : buttonGroups) {
            // Enable the button group if enable toggle is checked.
            buttonGroup.setEnabled(isChecked);
          }
        });
    MaterialSwitch opticalCenterSwitch = view.findViewById(R.id.switch_optical_center);
    opticalCenterSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButton button : buttons) {
            button.setOpticalCenterEnabled(isChecked);
          }
        });
    for (MaterialButton button : buttons) {
      if (button.getTag() != MaterialButtonGroup.OVERFLOW_BUTTON_TAG) {
        MaterialButtonGroup buttonGroup = (MaterialButtonGroup) button.getParent();
        button.setOnClickListener(
            v -> {
              String snackbarText = button.getText() + "";
              if (snackbarText.isEmpty()) {
                snackbarText = button.getContentDescription() + "";
              }
              snackbarText += " button clicked.";
              Snackbar.make(buttonGroup, snackbarText, Snackbar.LENGTH_LONG).show();
            });
      }
    }
    return view;
  }

  @LayoutRes
  protected int getIconOnlyButtonGroupContent() {
    return R.layout.cat_button_group_content_icon_only;
  }

  @LayoutRes
  protected int getLabelOnlyButtonGroupContent() {
    return R.layout.cat_button_group_content_label_only;
  }

  @LayoutRes
  protected int getMixedButtonGroupContent() {
    return R.layout.cat_button_group_content_mixed;
  }
}
