/*
 * Copyright 2021 The Android Open Source Project
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

import io.material.catalog.R;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.ColorRes;
import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the Color Harmonization demo for the Catalog app. */
public class ColorHarmonizationDemoFragment extends DemoFragment {

  private static final int GREEN_RESOURCE_ID = R.color.green40;
  private static final int RED_RESOURCE_ID = R.color.red40;
  private static final int BLUE_RESOURCE_ID = R.color.blue90;

  @Nullable
  @Override
  public View onCreateDemoView(
      @Nullable LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(getColorsContent(), viewGroup, false /* attachToRoot */);
    MaterialButton elevatedButton = view.findViewById(R.id.material_button);
    MaterialButton unelevatedButton = view.findViewById(R.id.material_unelevated_button);
    TextInputLayout textInputLayout = view.findViewById(R.id.material_text_input_layout);
    TextView buttonColorHexValueText = view.findViewById(R.id.material_button_color_hex_value);
    TextView unelevatedButtonColorHexValueText =
        view.findViewById(R.id.material_unelevated_button_color_hex_value);
    TextView textInputColorHexValueText =
        view.findViewById(R.id.material_text_input_color_hex_value);

    elevatedButton.setBackgroundColor(getResources().getColor(GREEN_RESOURCE_ID));
    unelevatedButton.setBackgroundColor(getResources().getColor(RED_RESOURCE_ID));
    textInputLayout.setBoxBackgroundColor(getResources().getColor(BLUE_RESOURCE_ID));

    SwitchMaterial enabledSwitch = view.findViewById(R.id.cat_color_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          buttonColorHexValueText.setVisibility(View.VISIBLE);
          unelevatedButtonColorHexValueText.setVisibility(View.VISIBLE);
          textInputColorHexValueText.setVisibility(View.VISIBLE);

          int maybeHarmonizedGreen = maybeHarmonizeWithPrimary(GREEN_RESOURCE_ID, isChecked);
          int maybeHarmonizedRed = maybeHarmonizeWithPrimary(RED_RESOURCE_ID, isChecked);
          int maybeHarmonizedBlue = maybeHarmonizeWithPrimary(BLUE_RESOURCE_ID, isChecked);

          elevatedButton.setBackgroundColor(maybeHarmonizedGreen);
          unelevatedButton.setBackgroundColor(maybeHarmonizedRed);
          textInputLayout.setBoxBackgroundColor(maybeHarmonizedBlue);

          // The %06X gives us zero-padded hex (always 6 chars long).
          buttonColorHexValueText.setText(
              getColorHexValueText(R.string.cat_color_hex_value_text, maybeHarmonizedGreen));
          unelevatedButtonColorHexValueText.setText(
              getColorHexValueText(R.string.cat_color_hex_value_text, maybeHarmonizedRed));
          textInputColorHexValueText.setText(
              getColorHexValueText(R.string.cat_color_hex_value_text, maybeHarmonizedBlue));
        });

    return view;
  }

  private int maybeHarmonizeWithPrimary(@ColorRes int colorResId, boolean harmonize) {
    return harmonize
        ? MaterialColors.harmonizeWithPrimary(getContext(), getResources().getColor(colorResId))
        : getResources().getColor(colorResId);
  }

  private CharSequence getColorHexValueText(@StringRes int stringResId, @ColorInt int color) {
    return getResources().getString(stringResId, String.format("#%06X", (0xFFFFFF & color)));
  }

  @LayoutRes
  protected int getColorsContent() {
    return R.layout.cat_colors_harmonization_fragment;
  }
}
