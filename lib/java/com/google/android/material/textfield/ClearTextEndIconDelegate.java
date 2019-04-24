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

import com.google.android.material.textfield.TextInputLayout.OnEditTextAttachedListener;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;

/**
 * Default initialization of the clear text end icon {@link TextInputLayout.EndIconMode}.
 */
class ClearTextEndIconDelegate extends EndIconDelegate {

  private final TextWatcher clearTextEndIconTextWatcher =
      new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {}

        @Override
        public void afterTextChanged(Editable s) {
          textInputLayout.setEndIconVisible(s.length() > 0);
        }
      };

  private final OnEditTextAttachedListener clearTextOnEditTextAttachedListener =
      new OnEditTextAttachedListener() {
        @Override
        public void onEditTextAttached() {
          EditText editText = textInputLayout.getEditText();
          textInputLayout.setEndIconVisible(!TextUtils.isEmpty(editText.getText()));
          // Make sure there's always only one clear text text watcher added
          editText.removeTextChangedListener(clearTextEndIconTextWatcher);
          editText.addTextChangedListener(clearTextEndIconTextWatcher);
        }
      };

  ClearTextEndIconDelegate(TextInputLayout textInputLayout) {
    super(textInputLayout);
  }

  @Override
  void initialize() {
    textInputLayout.setEndIconDrawable(
        AppCompatResources.getDrawable(context, R.drawable.mtrl_ic_cancel));
    textInputLayout.setEndIconContentDescription(
        textInputLayout.getResources().getText(R.string.clear_text_end_icon_content_description));
    textInputLayout.setEndIconOnClickListener(
        new OnClickListener() {
          @Override
          public void onClick(View v) {
            textInputLayout.getEditText().setText(null);
          }
        });
    textInputLayout.addOnEditTextAttachedListener(clearTextOnEditTextAttachedListener);
  }
}
