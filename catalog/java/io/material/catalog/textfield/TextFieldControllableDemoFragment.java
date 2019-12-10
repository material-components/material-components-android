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
import androidx.annotation.LayoutRes;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

/** A base class for controllable text field demos in the Catalog app. */
public abstract class TextFieldControllableDemoFragment extends TextFieldDemoFragment {
  private int colorIndex = 0;
  private int[] colors =
      new int[] {
        Color.BLUE, Color.RED, Color.GREEN, Color.DKGRAY,
      };

  @Override
  public void initTextFieldDemoControls(LayoutInflater layoutInflater, View view) {
    super.initTextFieldDemoControls(layoutInflater, view);

    // Initialize button for updating the box color.
    Button changeColorButton = view.findViewById(R.id.button_change_color);
    changeColorButton.setOnClickListener(v -> changeTextFieldColors(getNextColor()));

    // Initialize button for updating the label text.
    TextInputLayout labelTextField = view.findViewById(R.id.text_input_label);
    view.findViewById(R.id.button_update_label_text)
        .setOnClickListener(
            v -> {
              if (!checkTextInputIsNull(labelTextField)) {
                setAllTextFieldsLabel(String.valueOf(labelTextField.getEditText().getText()));
              }
            });

    // Initialize button for toggling the error text visibility.
    Button toggleErrorButton = view.findViewById(R.id.button_toggle_error);
    toggleErrorButton.setOnClickListener(
        v -> {
          if (!textfields.isEmpty() && textfields.get(0).getError() == null) {
            TextInputEditText errorEditText = view.findViewById(R.id.edit_text_error);
            String error =
                !TextUtils.isEmpty(errorEditText.getText())
                    ? String.valueOf(errorEditText.getText())
                    : getResources().getString(R.string.cat_textfield_error);
            setAllTextFieldsError(error);
            toggleErrorButton.setText(
                getResources().getString(R.string.cat_textfield_hide_error_text));
          } else {
            setAllTextFieldsError(null);
            toggleErrorButton.setText(
                getResources().getString(R.string.cat_textfield_show_error_text));
          }
        });

    // Initialize button for updating the helper text.
    TextInputLayout helperTextTextField = view.findViewById(R.id.text_input_helper_text);
    view.findViewById(R.id.button_update_helper_text)
        .setOnClickListener(
            v -> {
              if (!checkTextInputIsNull(helperTextTextField)) {
                setAllTextFieldsHelperText(
                    String.valueOf(helperTextTextField.getEditText().getText()));
              }
            });
    // Initialize button for updating the placeholder text.
    TextInputLayout placeholderTextField = view.findViewById(R.id.text_input_placeholder);
    view.findViewById(R.id.button_update_placeholder)
        .setOnClickListener(
            v -> {
              if (!checkTextInputIsNull(placeholderTextField)) {
                setAllTextFieldsPlaceholder(
                    String.valueOf(placeholderTextField.getEditText().getText()));
              }
            });

    // Initialize button for updating the counter max.
    TextInputLayout counterMaxTextField = view.findViewById(R.id.text_input_counter_max);
    view.findViewById(R.id.button_counter_max)
        .setOnClickListener(
            v -> {
              if (!checkTextInputIsNull(counterMaxTextField)) {
                int length =
                    Integer.parseInt(counterMaxTextField.getEditText().getText().toString());
                setAllTextFieldsCounterMax(length);
              }
            });
  }

  private void changeTextFieldColors(int color) {
    for (TextInputLayout textfield : textfields) {
      onChangeTextFieldColors(textfield, color);
    }
  }

  public abstract void onChangeTextFieldColors(TextInputLayout textfield, int color);

  private void setAllTextFieldsLabel(String label) {
    for (TextInputLayout textfield : textfields) {
      textfield.setHint(label);
    }
  }

  private void setAllTextFieldsError(String error) {
    ViewGroup parent = (ViewGroup) textfields.get(0).getParent();
    boolean textfieldWithErrorHasFocus = false;
    for (TextInputLayout textfield : textfields) {
      textfield.setError(error);
      textfieldWithErrorHasFocus |= textfield.hasFocus();
    }
    if (!textfieldWithErrorHasFocus) {
      // Request accessibility focus on the first text field to show an error.
      parent.getChildAt(0).requestFocus();
    }
  }

  private void setAllTextFieldsHelperText(String helperText) {
    for (TextInputLayout textfield : textfields) {
      textfield.setHelperText(helperText);
    }
  }

  private void setAllTextFieldsPlaceholder(String placeholder) {
    for (TextInputLayout textfield : textfields) {
      textfield.setPlaceholderText(placeholder);
    }
  }

  private void setAllTextFieldsCounterMax(int length) {
    for (TextInputLayout textfield : textfields) {
      textfield.setCounterMaxLength(length);
    }
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

  @Override
  @LayoutRes
  public int getTextFieldDemoControlsLayout() {
    return R.layout.cat_textfield_controls;
  }
}
