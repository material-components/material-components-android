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
 * A {@link CalendarConstraints.DateValidator} that enables dates from a given point forward.
 * Defaults to the current moment in device time forward using {@link
 * DateValidatorPointForward#now()}, but can be set to any point, as UTC milliseconds, using {@link
 * DateValidatorPointForward#from(long)}.
 */
public class DateValidatorPointForward implements DateValidator {

  private final long point;

  private DateValidatorPointForward(long point) {
    this.point = point;
  }

  /**
   * Returns a {@link CalendarConstraints.DateValidator} which enables days from {@code point}, in
   * UTC milliseconds, forward.
   */
  @NonNull
  public static DateValidatorPointForward from(long point) {
    return new DateValidatorPointForward(point);
  }

  /**
   * Returns a {@link CalendarConstraints.DateValidator} enabled from the current moment in device
   * time forward.
   */
  @NonNull
  public static DateValidatorPointForward now() {
    return from(UtcDates.getTodayCalendar().getTimeInMillis());
  }

  /** Part of {@link android.os.Parcelable} requirements. Do not use. */
  public static final Parcelable.Creator<DateValidatorPointForward> CREATOR =
      new Parcelable.Creator<DateValidatorPointForward>() {
        @NonNull
        @Override
        public DateValidatorPointForward createFromParcel(@NonNull Parcel source) {
          return new DateValidatorPointForward(source.readLong());
        }

        @NonNull
        @Override
        public DateValidatorPointForward[] newArray(int size) {
          return new DateValidatorPointForward[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date >= point;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeLong(point);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof DateValidatorPointForward)) {
      return false;
    }
    DateValidatorPointForward that = (DateValidatorPointForward) o;
    return point == that.point;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {point};
    return Arrays.hashCode(hashedFields);
  }
}
