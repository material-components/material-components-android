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
import android.view.ViewGroup.LayoutParams;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonGroup;
import com.google.android.material.materialswitch.MaterialSwitch;
import io.material.catalog.feature.DemoFragment;
import io.material.catalog.feature.DemoUtils;
import java.util.List;

/** A fragment that displays a button group demo for the Catalog app. */
public class ButtonGroupDemoFragment extends DemoFragment {

  private int defaultInset;

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
    List<MaterialButtonGroup> buttonGroups =
        DemoUtils.findViewsWithType(view, MaterialButtonGroup.class);

    defaultInset = getResources().getDimensionPixelSize(R.dimen.mtrl_btn_inset);

    MaterialSwitch verticalOrientationToggle = view.findViewById(R.id.orientation_switch_toggle);
    verticalOrientationToggle.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (MaterialButtonGroup buttonGroup : buttonGroups) {
            int orientation = isChecked ? VERTICAL : HORIZONTAL;
            buttonGroup.setOrientation(orientation);
            for (int i = 0; i < buttonGroup.getChildCount(); ++i) {
              int inset = getInsetForOrientation(orientation);
              MaterialButton button = (MaterialButton) buttonGroup.getChildAt(i);
              button.setInsetBottom(inset);
              button.setInsetTop(inset);
              adjustParams(button.getLayoutParams(), orientation);
            }

            buttonGroup.requestLayout();
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
    return view;
  }

  private int getInsetForOrientation(int orientation) {
    return orientation == VERTICAL ? 0 : defaultInset;
  }

  private static void adjustParams(LayoutParams layoutParams, int orientation) {
    layoutParams.width =
        orientation == VERTICAL ? LayoutParams.MATCH_PARENT : LayoutParams.WRAP_CONTENT;
  }
}
