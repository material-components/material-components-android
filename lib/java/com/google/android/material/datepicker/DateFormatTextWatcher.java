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
import android.text.Editable;
import android.text.TextUtils;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputLayout;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

abstract class DateFormatTextWatcher extends TextWatcherAdapter {

  @NonNull private final TextInputLayout textInputLayout;

  private final String formatHint;
  private final DateFormat dateFormat;
  private final CalendarConstraints constraints;
  private final String outOfRange;
  private final Runnable setErrorCallback;

  private Runnable setRangeErrorCallback;
  private int lastLength = 0;

  DateFormatTextWatcher(
      final String formatHint,
      DateFormat dateFormat,
      @NonNull TextInputLayout textInputLayout,
      CalendarConstraints constraints) {

    this.formatHint = formatHint;
    this.dateFormat = dateFormat;
    this.textInputLayout = textInputLayout;
    this.constraints = constraints;
    this.outOfRange = textInputLayout.getContext().getString(R.string.mtrl_picker_out_of_range);
    setErrorCallback =
        () -> {
          TextInputLayout textLayout = DateFormatTextWatcher.this.textInputLayout;
          DateFormat df = DateFormatTextWatcher.this.dateFormat;
          Context context = textLayout.getContext();
          String invalidFormat = context.getString(R.string.mtrl_picker_invalid_format);
          String useLine =
              String.format(
                  context.getString(R.string.mtrl_picker_invalid_format_use),
                  sanitizeDateString(formatHint));
          String exampleLine =
              String.format(
                  context.getString(R.string.mtrl_picker_invalid_format_example),
                  sanitizeDateString(
                      df.format(new Date(UtcDates.getTodayCalendar().getTimeInMillis()))));
          textLayout.setError(invalidFormat + "\n" + useLine + "\n" + exampleLine);
          onInvalidDate();
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

    if (TextUtils.isEmpty(s) || s.length() < formatHint.length()) {
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

  @Override
  public void beforeTextChanged(@NonNull CharSequence s, int start, int count, int after) {
    lastLength = s.length();
  }

  @Override
  public void afterTextChanged(@NonNull Editable s) {
    // Exclude some languages from automatically adding delimiters.
    if (Locale.getDefault().getLanguage().equals(Locale.KOREAN.getLanguage())) {
      return;
    }

    if (s.length() == 0 || s.length() >= formatHint.length() || s.length() < lastLength) {
      return;
    }

    char nextCharHint = formatHint.charAt(s.length());
    if (!Character.isLetterOrDigit(nextCharHint)) {
      s.append(nextCharHint);
    }
  }

  private Runnable createRangeErrorCallback(final long milliseconds) {
    return () -> {
      String dateString = DateStrings.getDateString(milliseconds);
      textInputLayout.setError(String.format(outOfRange, sanitizeDateString(dateString)));
      onInvalidDate();
    };
  }

  private String sanitizeDateString(String dateString) {
    // Replace all regular spaces with non-breaking spaces so the date wraps as a single unit.
    return dateString.replace(' ', '\u00A0');
  }

  public void runValidation(View view, Runnable validation) {
    view.post(validation);
  }
}
