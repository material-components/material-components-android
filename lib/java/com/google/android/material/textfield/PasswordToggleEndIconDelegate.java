/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.textfield;

import com.google.android.material.R;

import androidx.appcompat.content.res.AppCompatResources;
import android.text.InputType;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import androidx.annotation.NonNull;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputLayout.OnEditTextAttachedListener;
import com.google.android.material.textfield.TextInputLayout.OnEndIconChangedListener;

/** Default initialization of the password toggle end icon. */
class PasswordToggleEndIconDelegate extends EndIconDelegate {

  private final TextWatcher textWatcher =
      new TextWatcherAdapter() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
          // Make sure the password toggle state always matches the EditText's transformation
          // method.
          endIconView.setChecked(!hasPasswordTransformation());
        }
      };

  private final OnEditTextAttachedListener onEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached(@NonNull TextInputLayout textInputLayout) {
          EditText editText = textInputLayout.getEditText();
          textInputLayout.setEndIconVisible(true);
          textInputLayout.setEndIconCheckable(true);
          endIconView.setChecked(!hasPasswordTransformation());
          // Make sure there's always only one password toggle text watcher added
          editText.removeTextChangedListener(textWatcher);
          editText.addTextChangedListener(textWatcher);
        }
      };
  private final OnEndIconChangedListener onEndIconChangedListener =
      new OnEndIconChangedListener() {
        @Override
        public void onEndIconChanged(@NonNull TextInputLayout textInputLayout, int previousIcon) {
          final EditText editText = textInputLayout.getEditText();
          if (editText != null && previousIcon == TextInputLayout.END_ICON_PASSWORD_TOGGLE) {
            // If the end icon was the password toggle add it back the PasswordTransformation
            // in case it might have been removed to make the password visible.
            editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            // Remove any listeners set on the edit text.
            editText.post(
                new Runnable() {
                  @Override
                  public void run() {
                    editText.removeTextChangedListener(textWatcher);
                  }
                });
          }
        }
      };

  PasswordToggleEndIconDelegate(@NonNull TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconDrawable(
        AppCompatResources.getDrawable(context, R.drawable.design_password_eye));
    textInputLayout.setEndIconContentDescription(
        textInputLayout.getResources().getText(R.string.password_toggle_content_description));
    textInputLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            EditText editText = textInputLayout.getEditText();
            if (editText == null) {
              return;
            }
            // Store the current cursor position
            final int selection = editText.getSelectionEnd();
            if (hasPasswordTransformation()) {
              editText.setTransformationMethod(null);
            } else {
              editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
            }
            // And restore the cursor position
            if (selection >= 0) {
              editText.setSelection(selection);
            }

            textInputLayout.refreshEndIconDrawableState();
          }
        });
    textInputLayout.addOnEditTextAttachedListener(onEditTextAttachedListener);
    textInputLayout.addOnEndIconChangedListener(onEndIconChangedListener);
    EditText editText = textInputLayout.getEditText();
    if (isInputTypePassword(editText)) {
      // By default set the input to be disguised.
      editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
  }

  private boolean hasPasswordTransformation() {
    EditText editText = textInputLayout.getEditText();
    return editText != null
        && editText.getTransformationMethod() instanceof PasswordTransformationMethod;
  }

  private static boolean isInputTypePassword(EditText editText) {
    return editText != null
        && (editText.getInputType() == InputType.TYPE_NUMBER_VARIATION_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            || editText.getInputType() == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD);
  }
}
