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

import android.text.InputType;
import android.text.method.PasswordTransformationMethod;
import android.view.View.OnClickListener;
import android.widget.EditText;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

/** Default initialization of the password toggle end icon. */
class PasswordToggleEndIconDelegate extends EndIconDelegate {
  private int iconResId = R.drawable.design_password_eye;

  @Nullable
  private EditText editText;

  private final OnClickListener onIconClickListener = view -> {
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
    refreshIconState();
  };

  PasswordToggleEndIconDelegate(
      @NonNull EndCompoundLayout endLayout, @DrawableRes int overrideIconResId) {
    super(endLayout);
    if (overrideIconResId != 0) {
      iconResId = overrideIconResId;
    }
  }

  @Override
  void setUp() {
    if (isInputTypePassword(editText)) {
      // By default set the input to be disguised.
      editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
  }

  @Override
  void tearDown() {
    if (editText != null) {
      // Add PasswordTransformation back since it may have been removed to make passwords visible.
      editText.setTransformationMethod(PasswordTransformationMethod.getInstance());
    }
  }

  @Override
  @DrawableRes
  int getIconDrawableResId() {
    return iconResId;
  }

  @Override
  @StringRes
  int getIconContentDescriptionResId() {
    return R.string.password_toggle_content_description;
  }

  @Override
  boolean isIconCheckable() {
    return true;
  }

  @Override
  boolean isIconChecked() {
    // Make sure the password toggle state always matches the EditText's transformation method.
    return !hasPasswordTransformation();
  }

  @Override
  OnClickListener getOnIconClickListener() {
    return onIconClickListener;
  }

  @Override
  void onEditTextAttached(@Nullable EditText editText) {
    this.editText = editText;
    refreshIconState();
  }

  @Override
  void beforeEditTextChanged(CharSequence s, int start, int count, int after) {
    refreshIconState();
  }

  private boolean hasPasswordTransformation() {
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
