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

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.LayoutRes;
import androidx.annotation.StringRes;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.android.material.textfield.TextInputLayout;

/** A base class for controllable text field demos in the Catalog app. */
public abstract class TextFieldControllableDemoFragment extends TextFieldDemoFragment {

  private String errorText;

  @Override
  public void initTextFieldDemoControls(LayoutInflater layoutInflater, View view) {
    super.initTextFieldDemoControls(layoutInflater, view);
    errorText = getResources().getString(R.string.cat_textfield_error);

    // Initialize button for toggling the error text visibility.
    Button toggleErrorButton = view.findViewById(R.id.button_toggle_error);
    toggleErrorButton.setOnClickListener(
        v -> {
          if (!textfields.isEmpty() && textfields.get(0).getError() == null) {
            setAllTextFieldsError(errorText);
            toggleErrorButton.setText(
                getResources().getString(R.string.cat_textfield_hide_error_text));
          } else {
            setAllTextFieldsError(null);
            toggleErrorButton.setText(
                getResources().getString(R.string.cat_textfield_show_error_text));
          }
        });

    // Initialize text field for updating the label text.
    TextInputLayout labelTextField = view.findViewById(R.id.text_input_label);
    EditText labelEditText = labelTextField.getEditText();
    labelEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkTextInputIsNull(labelTextField)) {
              setAllTextFieldsLabel(String.valueOf(labelEditText.getText()));
              showToast(R.string.cat_textfield_toast_label_text);
            }
            return true;
          }
          return false;
        });

    // Initialize text field for updating the error text.
    TextInputLayout textInputError = view.findViewById(R.id.text_input_error);
    EditText inputErrorEditText = textInputError.getEditText();
    inputErrorEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkTextInputIsNull(textInputError)) {
              errorText = String.valueOf(inputErrorEditText.getText());
              showToast(R.string.cat_textfield_toast_error_text);
              // if error already showing, call setError again to update its text.
              if (toggleErrorButton
                  .getText()
                  .toString()
                  .equals(getResources().getString(R.string.cat_textfield_hide_error_text))) {
                for (TextInputLayout textfield : textfields) {
                  setErrorIconClickListeners(textfield);
                  textfield.setError(errorText);
                }
              }
            }
            return true;
          }
          return false;
        });

    // Initialize text field for updating the helper text.
    TextInputLayout helperTextTextField = view.findViewById(R.id.text_input_helper_text);
    EditText helperTextEditText = helperTextTextField.getEditText();
    helperTextEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkTextInputIsNull(helperTextTextField)) {
              setAllTextFieldsHelperText(String.valueOf(helperTextEditText.getText()));
              showToast(R.string.cat_textfield_toast_helper_text);
            }
            return true;
          }
          return false;
        });

    // Initialize text field for updating the placeholder text.
    TextInputLayout placeholderTextField = view.findViewById(R.id.text_input_placeholder);
    EditText placeholderEditText = placeholderTextField.getEditText();
    placeholderEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkTextInputIsNull(placeholderTextField)) {
              setAllTextFieldsPlaceholder(String.valueOf(placeholderEditText.getText()));
              showToast(R.string.cat_textfield_toast_placeholder_text);
            }
            return true;
          }
          return false;
        });

    // Initialize text field for updating the counter max.
    TextInputLayout counterMaxTextField = view.findViewById(R.id.text_input_counter_max);
    EditText counterEditText = counterMaxTextField.getEditText();
    counterEditText.setOnEditorActionListener(
        (v, actionId, event) -> {
          if (actionId == EditorInfo.IME_ACTION_DONE) {
            if (!checkTextInputIsNull(counterMaxTextField)) {
              int length = Integer.parseInt(counterEditText.getText().toString());
              setAllTextFieldsCounterMax(length);
              showToast(R.string.cat_textfield_toast_counter_text);
            }
            return true;
          }
          return false;
        });

    // Initializing switch to toggle between disabling or enabling text fields.
    SwitchMaterial enabledSwitch = view.findViewById(R.id.cat_textfield_enabled_switch);
    enabledSwitch.setOnCheckedChangeListener(
        (buttonView, isChecked) -> {
          for (TextInputLayout textfield : textfields) {
            textfield.setEnabled(isChecked);
          }
        });
  }

  private void setAllTextFieldsLabel(String label) {
    for (TextInputLayout textfield : textfields) {
      textfield.setHint(label);
    }
  }

  private void setAllTextFieldsError(String error) {
    ViewGroup parent = (ViewGroup) textfields.get(0).getParent();
    boolean textfieldWithErrorHasFocus = false;
    for (TextInputLayout textfield : textfields) {
      setErrorIconClickListeners(textfield);
      textfield.setError(error);
      textfieldWithErrorHasFocus |= textfield.hasFocus();
    }
    if (!textfieldWithErrorHasFocus) {
      // Request accessibility focus on the first text field to show an error.
      parent.getChildAt(0).requestFocus();
    }
  }

  private void setErrorIconClickListeners(TextInputLayout textfield) {
    textfield.setErrorIconOnClickListener(
        v -> showToast(R.string.cat_textfield_toast_error_icon_click));
    textfield.setErrorIconOnLongClickListener(
        v -> {
          showToast(R.string.cat_textfield_toast_error_icon_long_click);
          return true;
        });
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

  private void showToast(@StringRes int messageResId) {
    Toast.makeText(getContext(), messageResId, Toast.LENGTH_LONG).show();
  }

  @Override
  @LayoutRes
  public int getTextFieldDemoControlsLayout() {
    return R.layout.cat_textfield_controls;
  }
}
