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

import java.util.Arrays;

/**
 * A {@link DateValidator} that enables dates from a given date range.
 */
public final class DateRangeValidator implements DateValidator {

  private final long lowerBound;
  private final long upperBound;


  private DateRangeValidator(long lowerBound, long upperBound) {
    this.lowerBound = lowerBound;
    this.upperBound = upperBound;
  }

  /**
   * Returns a {@link DateValidator} which enables days between {@code lowerBound}, in
   * UTC milliseconds, up until {@code upperBound} UTC milliseconds inclusively.
   */
  @NonNull
  public static DateRangeValidator between(long lowerBound, long upperBound) {
    return new DateRangeValidator(lowerBound, upperBound);
  }

  /** Part of {@link Parcelable} requirements. Do not use. */
  public static final Creator<DateRangeValidator> CREATOR =
      new Creator<DateRangeValidator>() {
        @NonNull
        @Override
        public DateRangeValidator createFromParcel(@NonNull Parcel source) {
          long lowerBound = source.readLong();
          long upperBound = source.readLong();
          return new DateRangeValidator(lowerBound, upperBound);
        }

        @NonNull
        @Override
        public DateRangeValidator[] newArray(int size) {
          return new DateRangeValidator[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date >= lowerBound && date <= upperBound;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeLong(lowerBound);
    dest.writeLong(upperBound);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateRangeValidator)) {
      return false;
    }
    DateRangeValidator that = (DateRangeValidator) o;
    return lowerBound == that.lowerBound && upperBound == that.upperBound;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {lowerBound, upperBound};
    return Arrays.hashCode(hashedFields);
  }
}
