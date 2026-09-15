/*
 * Copyright 2019 The Android Open Source Project
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
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.shape.RelativeCornerSize;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a button toggle group demo for the Catalog app. */
public class ButtonToggleGroupDemoFragment extends DemoFragment {

  private int defaultInset;

  /**
   * Create a Demo View with different types of {@link MaterialButtonToggleGroup} and a switch to
   * toggle {@link MaterialButtonToggleGroup#setSelectionRequired(boolean)}
   */
  @Nullable
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(getButtonToggleGroupContent(), viewGroup, /* attachToRoot= */ false);
    MaterialSwitch requireSelectionToggle = view.findViewById(R.id.switch_toggle);
    defaultInset = getResources().getDimensionPixelSize(com.google.android.material.R.dimen.mtrl_btn_inset);
    List<MaterialButton> buttons = DemoUtils.findViewsWithType(view, MaterialButton.class);
    List<MaterialButtonToggleGroup> toggleGroups =
        DemoUtils.findViewsWithType(view, MaterialButtonToggleGroup.class);
    requireSelectionToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
            toggleGroup.setSelectionRequired(isChecked);
          }
        });

    MaterialSwitch verticalOrientationToggle = view.findViewById(R.id.orientation_switch_toggle);
    verticalOrientationToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
            int orientation = isChecked ? VERTICAL : HORIZONTAL;
            toggleGroup.setOrientation(orientation);
            for (int i = 0; i < toggleGroup.getChildCount(); ++i) {
              int inset = getInsetForOrientation(orientation);
              MaterialButton button = (MaterialButton) toggleGroup.getChildAt(i);
              button.setInsetBottom(inset);
              button.setInsetTop(inset);
              adjustParams(button.getLayoutParams(), orientation);
            }

            toggleGroup.requestLayout();
          }
        });

    MaterialSwitch groupEnabledToggle = view.findViewById(R.id.switch_enable);
    groupEnabledToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
            // Enable the button group if enable toggle is checked.
            toggleGroup.setEnabled(isChecked);
          }
        });

    for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
      toggleGroup.addOnButtonCheckedListener(
          (group, checkedId, isChecked) -> {
            String message = "button" + (isChecked ? " checked" : " unchecked");
            Snackbar.make(view, message, Snackbar.LENGTH_SHORT).show();
          });
    }

    Slider innerCornerSizeSlider = view.findViewById(R.id.innerCornerSizeSlider);
    innerCornerSizeSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
            toggleGroup.setInnerCornerSize(new RelativeCornerSize(value / 100f));
          }
        });

    Slider spacingSlider = view.findViewById(R.id.spacingSlider);
    spacingSlider.addOnChangeListener(
        (slider, value, fromUser) -> {
          float pixelsInDp = view.getResources().getDisplayMetrics().density;
          for (MaterialButtonToggleGroup toggleGroup : toggleGroups) {
            toggleGroup.setSpacing((int) (value * pixelsInDp));
          }
        });
    MaterialSwitch opticalCenterSwitch = view.findViewById(R.id.switch_optical_center);
    opticalCenterSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButton button : buttons) {
            button.setOpticalCenterEnabled(isChecked);
          }
        });
    return view;
  }

  private int getInsetForOrientation(int orientation) {
    return orientation == VERTICAL ? 0 : defaultInset;
  }

  private static void adjustParams(LayoutParams layoutParams, int orientation) {
    layoutParams.width =
        orientation == VERTICAL ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
  }

  @LayoutRes
  protected int getButtonToggleGroupContent() {
    return R.layout.cat_buttons_toggle_group_fragment;
  }
}
