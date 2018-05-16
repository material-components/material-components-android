/*
 * Copyright 2017 The Android Open Source Project
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

package io.material.catalog.textfield;

import io.material.catalog.R;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import io.material.catalog.feature.DemoFragment;

/** A fragment that displays the main text field demos for the Catalog app. */
public class TextFieldControllableDemoFragment extends DemoFragment {
  private int colorIndex = 0;
  private int[] colors =
      new int[] {
        Color.BLUE, Color.RED, Color.GREEN, Color.DKGRAY,
      };

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view =
        layoutInflater.inflate(
            R.layout.cat_textfield_controllable_fragment, viewGroup, false /* attachToRoot */);

    // Initialize text inputs.
    TextInputLayout textInputDemoBoxOutline = view.findViewById(R.id.text_input_demo_box_outline);

    TextInputLayout textInputError = view.findViewById(R.id.text_input_error);
    TextInputLayout textInputLabel = view.findViewById(R.id.text_input_label);
    TextInputLayout textInputCounterMax = view.findViewById(R.id.text_input_counter_max);

    // Initialize button for changing the outline box color.
    Button changeColorButton = view.findViewById(R.id.button_change_color);
    changeColorButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            textInputDemoBoxOutline.setBoxStrokeColor(getNextColor());
          }
        });

    // Initialize button for toggling the error text visibility.
    Button toggleErrorButton = view.findViewById(R.id.button_toggle_error);
    toggleErrorButton.setOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            if (textInputDemoBoxOutline.getError() == null) {
              if (textInputError.getEditText().length() == 0) {
                textInputDemoBoxOutline.setError(
                    getResources().getString(R.string.cat_textfield_error));
              } else {
                textInputDemoBoxOutline.setError(textInputError.getEditText().getText());
              }
              toggleErrorButton.setText(
                  getResources().getString(R.string.cat_textfield_hide_error_text));
            } else {
              textInputDemoBoxOutline.setError(null);
              toggleErrorButton.setText(
                  getResources().getString(R.string.cat_textfield_show_error_text));
            }
          }
        });

    // Initialize button for updating the label text.
    view.findViewById(R.id.button_update_label_text)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (!checkTextInputIsNull(textInputLabel)) {
                  textInputDemoBoxOutline.setHint(textInputLabel.getEditText().getText());
                }
              }
            });

    // Initialize button for updating the error text.
    view.findViewById(R.id.button_update_error_text)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (!checkTextInputIsNull(textInputError)) {
                  textInputDemoBoxOutline.setError(textInputError.getEditText().getText());
                  toggleErrorButton.setText(
                      getResources().getString(R.string.cat_textfield_hide_error_text));
                }
              }
            });

    // Initialize button for updating the counter max.
    view.findViewById(R.id.button_counter_max)
        .setOnClickListener(
            new OnClickListener() {
              @Override
              public void onClick(View v) {
                if (!checkTextInputIsNull(textInputCounterMax)) {
                  textInputDemoBoxOutline.setCounterMaxLength(
                      Integer.parseInt(textInputCounterMax.getEditText().getText().toString()));
                }
              }
            });
    return view;
  }

  private boolean checkTextInputIsNull(TextInputLayout textInputLayout) {
    if (textInputLayout.getEditText().getText() == null
        || textInputLayout.getEditText().length() == 0) {
      textInputLayout.setError(
          getResources().getString(R.string.cat_textfield_null_input_error_text));
      return true;
    }
    textInputLayout.setError(null);
    return false;
  }

  private int getNextColor() {
    colorIndex = (colorIndex + 1) % colors.length;
    return colors[colorIndex];
  }
}
