package com.google.android.material.datepicker;


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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Arrays;
import static com.google.android.material.datepicker.CalendarConstraints.*;


/**
 * A {@link CalendarConstraints.DateValidator} that enables only dates before a given point.
 * Defaults to the current moment in device time backwards using {@link
 * DateValidatorPointBackward#now()}, but can be set to any point, as UTC milliseconds, using {@link
 * DateValidatorPointBackward#before(long)}.
 */
public class DateValidatorPointBackward implements DateValidator {

  private final long point;

  private DateValidatorPointBackward(long point) {
    this.point = point;
  }


  /**
   * Returns a {@link CalendarConstraints.DateValidator} which enables only days before {@code point}, in
   * UTC milliseconds.
   */
  public static DateValidatorPointBackward before(long point) {
    return new DateValidatorPointBackward(point);
  }

  /**
   * Returns a {@link CalendarConstraints.DateValidator} enabled from the current moment in device
   * time backwards.
   */
  public static DateValidatorPointBackward now() {
    return before(UtcDates.getTodayCalendar().getTimeInMillis());
  }

  /** Part of {@link android.os.Parcelable} requirements. Do not use. */
  public static final Parcelable.Creator<DateValidatorPointBackward> CREATOR =
      new Parcelable.Creator<DateValidatorPointBackward>() {
        @NonNull
        @Override
        public DateValidatorPointBackward createFromParcel(@NonNull Parcel source) {
          return new DateValidatorPointBackward(source.readLong());
        }

        @NonNull
        @Override
        public DateValidatorPointBackward[] newArray(int size) {
          return new DateValidatorPointBackward[size];
        }
      };

  @Override
  public boolean isValid(long date) {
    return date <= point;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeLong(point);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof DateValidatorPointBackward)) return false;
    DateValidatorPointBackward that = (DateValidatorPointBackward) o;
    return point == that.point;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {point};
    return Arrays.hashCode(hashedFields);
  }
}
