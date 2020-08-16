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

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import com.google.android.material.datepicker.CalendarConstraints.DateValidator;

import java.util.List;

import static androidx.core.util.Preconditions.checkNotNull;

/**
 * A {@link DateValidator} that accepts a list of Date Validators.
 * The provided {@code date} is enabled if at least one validator is valid.
 */
public class MultipleDateValidator implements DateValidator {

  @NonNull private final List<DateValidator> validators;

  protected MultipleDateValidator(@NonNull List<DateValidator> validators) {
    this.validators = validators;
  }

  /**
   * Returns a {@link DateValidator} that can perform validation for every given validator.
   */
  @NonNull
  public static DateValidator allOf(@NonNull List<DateValidator> validators) {
    return new MultipleDateValidator(validators);
  }

  /** Part of {@link Parcelable} requirements. Do not use. */
  public static final Creator<MultipleDateValidator> CREATOR =
      new Creator<MultipleDateValidator>() {
        @NonNull
        @Override
        public MultipleDateValidator createFromParcel(@NonNull Parcel source) {
          @SuppressWarnings("unchecked")
          List<DateValidator> validators =
              source.readArrayList(DateValidator.class.getClassLoader());
          return new MultipleDateValidator(checkNotNull(validators));
        }

        @NonNull
        @Override
        public MultipleDateValidator[] newArray(int size) {
          return new MultipleDateValidator[size];
        }
      };

  /**
   * Performs the {@link DateValidator#isValid(long)} check as an OR of all validators.
   * e.g. If one validator in this class returns `true` for each {@link
   * DateValidator#isValid(long)}, this method will return true.
   *
   * @param date milliseconds date to validate against.
   * @return True, if the given date is valid for at least one validator in this class.
   */
  @Override
  public boolean isValid(long date) {
    for (DateValidator validator : validators) {
      if (validator == null) {
        continue;
      }
      if (validator.isValid(date)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeList(validators);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof MultipleDateValidator)) {
      return false;
    }

    MultipleDateValidator that = (MultipleDateValidator) o;

    return validators.equals(that.validators);
  }

  @Override
  public int hashCode() {
    return validators.hashCode();
  }
}
