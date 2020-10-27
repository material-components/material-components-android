/*
 * Copyright 2020 The Android Open Source Project
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

package io.material.catalog.transition;

import io.material.catalog.R;

import android.util.SparseIntArray;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.transition.MaterialSharedAxis;

/** A helper class that sets up and manages shared axis demo controls. */
public class SharedAxisHelper {

  private static final SparseIntArray BUTTON_AXIS_MAP = new SparseIntArray();

  static {
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_x, MaterialSharedAxis.X);
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_y, MaterialSharedAxis.Y);
    BUTTON_AXIS_MAP.append(R.id.radio_button_axis_z, MaterialSharedAxis.Z);
  }

  private final Button backButton;
  private final Button nextButton;
  private final RadioGroup directionRadioGroup;

  SharedAxisHelper(@NonNull ViewGroup controlsLayout) {
    backButton = controlsLayout.findViewById(R.id.back_button);
    nextButton = controlsLayout.findViewById(R.id.next_button);
    directionRadioGroup = controlsLayout.findViewById(R.id.radio_button_group_direction);
  }

  public void setNextButtonOnClickListener(@Nullable OnClickListener onClickListener) {
    nextButton.setOnClickListener(onClickListener);
  }

  public void setBackButtonOnClickListener(@Nullable OnClickListener onClickListener) {
    backButton.setOnClickListener(onClickListener);
  }

  public void updateButtonsEnabled(boolean startScreenShowing) {
    backButton.setEnabled(!startScreenShowing);
    nextButton.setEnabled(startScreenShowing);
  }

  public int getSelectedAxis() {
    return BUTTON_AXIS_MAP.get(directionRadioGroup.getCheckedRadioButtonId());
  }

  public void setSelectedAxis(int axis) {
    int index = BUTTON_AXIS_MAP.indexOfValue(axis);
    int id = BUTTON_AXIS_MAP.keyAt(index);
    directionRadioGroup.check(id);
  }

  public void setAxisButtonGroupEnabled(boolean enabled) {
    for (int i = 0; i < directionRadioGroup.getChildCount(); i++) {
      directionRadioGroup.getChildAt(i).setEnabled(enabled);
    }
  }
}
