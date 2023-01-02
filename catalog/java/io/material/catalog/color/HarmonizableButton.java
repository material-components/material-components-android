/*
 * Copyright 2022 The Android Open Source Project
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

package io.material.catalog.color;

import android.view.View;
import androidx.annotation.ColorInt;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.ColorRoles;
import com.google.android.material.color.MaterialColors;

/** A class for {@link MaterialButton} that can be harmonized. */
final class HarmonizableButton {

  private final MaterialButton materialButton;
  private final int colorValue;

  static HarmonizableButton create(View view, HarmonizableButtonData harmonizableButtonData) {
    ColorRoles colorRoles =
        MaterialColors.getColorRoles(
            view.getContext(),
            view.getResources().getColor(harmonizableButtonData.getColorResId()));
    return new HarmonizableButton(
        view.findViewById(harmonizableButtonData.getButtonId()),
        harmonizableButtonData.isLightButton()
            ? colorRoles.getAccentContainer()
            : colorRoles.getAccent());
  }

  private HarmonizableButton(MaterialButton materialButton, @ColorInt int colorValue) {
    this.materialButton = materialButton;
    this.colorValue = colorValue;
  }

  void updateColors(boolean harmonize) {
    int maybeHarmonizedColor =
        harmonize
            ? MaterialColors.harmonizeWithPrimary(materialButton.getContext(), colorValue)
            : colorValue;
    materialButton.setBackgroundColor(maybeHarmonizedColor);
    materialButton.setTextColor(ColorDemoUtils.getTextColor(maybeHarmonizedColor));
  }
}
