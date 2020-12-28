/*
 * Copyright (C) 2020 The Android Open Source Project
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

package com.google.android.material.timepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.os.LocaleList;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Checkable;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.chip.Chip;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputLayout;
import java.util.Arrays;

/**
 * A {@link Chip} that can switch to a {@link TextInputLayout} when checked to modify it's content.
 * It keeps the helper text from the TextInput always visible.
 */
class ChipTextInputComboView extends FrameLayout implements Checkable {

  private final Chip chip;
  private final TextInputLayout textInputLayout;
  private final EditText editText;
  private TextWatcher watcher;
  private TextView label;

  public ChipTextInputComboView(@NonNull Context context) {
    this(context, null);
  }

  public ChipTextInputComboView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public ChipTextInputComboView(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    LayoutInflater inflater = LayoutInflater.from(context);
    chip = (Chip) inflater.inflate(R.layout.material_time_chip, this, false);
    textInputLayout = (TextInputLayout) inflater.inflate(R.layout.material_time_input, this, false);
    editText = textInputLayout.getEditText();
    editText.setVisibility(INVISIBLE);
    watcher = new TextFormatter();
    editText.addTextChangedListener(watcher);
    updateHintLocales();
    addView(chip);
    addView(textInputLayout);
    label = findViewById(R.id.material_label);
  }

  private void updateHintLocales() {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      Configuration configuration = getContext().getResources().getConfiguration();
      final LocaleList locales = configuration.getLocales();
      editText.setImeHintLocales(locales);
    }
  }

  @Override
  public boolean isChecked() {
    return chip.isChecked();
  }

  @Override
  public void setChecked(boolean checked) {
    chip.setChecked(checked);
    editText.setVisibility(checked ? VISIBLE : INVISIBLE);
    chip.setVisibility(checked ? GONE : VISIBLE);
    if (isChecked()) {
      editText.requestFocus();
      if (!TextUtils.isEmpty(editText.getText())) {
        editText.setSelection(editText.getText().length());
      }
    }
  }

  @Override
  public void toggle() {
    chip.toggle();
  }

  public void setText(CharSequence text) {
    chip.setText(formatText(text));
    if (!TextUtils.isEmpty(editText.getText())) {
      editText.removeTextChangedListener(watcher);
      editText.setText(null);
      editText.addTextChangedListener(watcher);
    }
  }

  private String formatText(CharSequence text) {
    return TimeModel.formatText(getResources(), text);
  }

  @Override
  public void setOnClickListener(@Nullable OnClickListener l) {
    chip.setOnClickListener(l);
  }

  @Override
  public void setTag(int key, Object tag) {
    chip.setTag(key, tag);
  }

  public void setHelperText(CharSequence helperText) {
    label.setText(helperText);
  }

  public void setCursorVisible(boolean visible) {
    editText.setCursorVisible(visible);
  }

  public void addInputFilter(InputFilter filter) {
    InputFilter[] current = editText.getFilters();
    InputFilter[] arr = Arrays.copyOf(current, current.length + 1);
    arr[current.length] = filter;
    editText.setFilters(arr);
  }

  public TextInputLayout getTextInput() {
    return textInputLayout;
  }

  public void setChipDelegate(AccessibilityDelegateCompat clickActionDelegate) {
    ViewCompat.setAccessibilityDelegate(chip, clickActionDelegate);
  }

  private class TextFormatter extends TextWatcherAdapter {

    private static final String DEFAULT_TEXT = "00";

    @Override
    public void afterTextChanged(Editable editable) {
      if (TextUtils.isEmpty(editable)) {
        chip.setText(formatText(DEFAULT_TEXT));
        return;
      }

      chip.setText(formatText(editable));
    }
  }

  @Override
  protected void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    updateHintLocales();
  }
}
