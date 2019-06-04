/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.picker;

import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputLayout;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

abstract class DateFormatTextWatcher implements TextWatcher {

  private final DateFormat dateFormat;
  private final TextInputLayout textInputLayout;

  DateFormatTextWatcher(DateFormat dateFormat, TextInputLayout textInputLayout) {
    this.dateFormat = dateFormat;
    this.textInputLayout = textInputLayout;
  }

  abstract void onDateChanged(@Nullable Calendar calendar);

  @Override
  public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

  @Override
  public void onTextChanged(CharSequence s, int start, int before, int count) {
    if (TextUtils.isEmpty(s)) {
      onDateChanged(null);
      return;
    }

    // TODO: better format enforcing and validation error
    try {
      Date date = dateFormat.parse(s.toString());
      textInputLayout.setError(null);
      Calendar calendar = Calendar.getInstance();
      calendar.setTime(date);
      onDateChanged(calendar);
    } catch (ParseException e) {
      textInputLayout.setError("Validation error.");
    }
  }

  @Override
  public void afterTextChanged(Editable s) {}
}
