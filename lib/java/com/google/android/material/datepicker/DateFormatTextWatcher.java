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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

abstract class DateFormatTextWatcher extends TextWatcherAdapter {

  private static final int VALIDATION_DELAY = 1000;

  @NonNull private final TextInputLayout textInputLayout;

  private final DateFormat dateFormat;
  private final CalendarConstraints constraints;
  private final String outOfRange;
  private final Runnable setErrorCallback;

  private Runnable setRangeErrorCallback;

  DateFormatTextWatcher(
      final String formatHint,
      DateFormat dateFormat,
      @NonNull TextInputLayout textInputLayout,
      CalendarConstraints constraints) {

    this.dateFormat = dateFormat;
    this.textInputLayout = textInputLayout;
    this.constraints = constraints;
    this.outOfRange = textInputLayout.getContext().getString(R.string.mtrl_picker_out_of_range);
    setErrorCallback =
        new Runnable() {
          @Override
          public void run() {
            TextInputLayout textLayout = DateFormatTextWatcher.this.textInputLayout;
            DateFormat df = DateFormatTextWatcher.this.dateFormat;
            Context context = textLayout.getContext();
            String invalidFormat = context.getString(R.string.mtrl_picker_invalid_format);
            String useLine =
                String.format(
                    context.getString(R.string.mtrl_picker_invalid_format_use), formatHint);
            String exampleLine =
                String.format(
                    context.getString(R.string.mtrl_picker_invalid_format_example),
                    df.format(new Date(UtcDates.getTodayCalendar().getTimeInMillis())));
            textLayout.setError(invalidFormat + "\n" + useLine + "\n" + exampleLine);
            onInvalidDate();
          }
        };
  }

  abstract void onValidDate(@Nullable Long day);

  void onInvalidDate() {}

  @Override
  public void onTextChanged(@NonNull CharSequence s, int start, int before, int count) {
    textInputLayout.removeCallbacks(setErrorCallback);
    textInputLayout.removeCallbacks(setRangeErrorCallback);
    textInputLayout.setError(null);
    onValidDate(null);
    if (TextUtils.isEmpty(s)) {
      return;
    }

    try {
      Date date = dateFormat.parse(s.toString());
      textInputLayout.setError(null);
      final long milliseconds = date.getTime();
      if (constraints.getDateValidator().isValid(milliseconds)
          && constraints.isWithinBounds(milliseconds)) {
        onValidDate(date.getTime());
        return;
      }

      setRangeErrorCallback = createRangeErrorCallback(milliseconds);
      runValidation(textInputLayout, setRangeErrorCallback);
    } catch (ParseException e) {
      runValidation(textInputLayout, setErrorCallback);
    }
  }

  private Runnable createRangeErrorCallback(final long milliseconds) {
    return new Runnable() {
      @Override
      public void run() {
        textInputLayout.setError(
            String.format(outOfRange, DateStrings.getDateString(milliseconds)));
        onInvalidDate();
      }
    };
  }

  public void runValidation(View view, Runnable validation) {
    view.postDelayed(validation, VALIDATION_DELAY);
  }
}
