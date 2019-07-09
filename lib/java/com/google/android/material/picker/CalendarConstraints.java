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

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Arrays;

/**
 * Used to limit the display range of {@link MaterialCalendar} and set a starting {@link Month}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class CalendarConstraints implements Parcelable {

  private final Month start;
  private final Month end;
  private final Month current;
  private final DateValidator validator;

  private final int yearSpan;
  private final int monthSpan;

  /** Used to determine whether {@link MaterialCalendar} days are enabled. */
  public interface DateValidator extends Parcelable {

    /** Returns true if the provided {@code date} is enabled. */
    boolean isValid(long date);
  }

  private CalendarConstraints(Month start, Month end, Month current, DateValidator validator) {
    this.start = start;
    this.end = end;
    this.current = current;
    this.validator = validator;
    if (start.compareTo(current) > 0) {
      throw new IllegalArgumentException("start Month cannot be after current Month");
    }
    if (current.compareTo(end) > 0) {
      throw new IllegalArgumentException("current Month cannot be after end Month");
    }
    monthSpan = start.monthsUntil(end) + 1;
    yearSpan = end.year - start.year + 1;
  }

  /**
   * Creates a CalendarConstraints instance that opens on today if it is within the bounds or {@code
   * start} if today is not within the bounds.
   */
  public static CalendarConstraints create(Month start, Month end) {
    Month today = Month.today();
    if (end.compareTo(today) >= 0 && today.compareTo(start) >= 0) {
      return create(start, end, Month.today());
    }
    return create(start, end, start);
  }

  /**
   * Creates a CalendarConstraints instance which opens onto {@code current} and is bounded between
   * {@code start} and {@code end}.
   */
  public static CalendarConstraints create(Month start, Month end, Month current) {
    return create(start, end, current, new DateValidatorPointForward(0));
  }

  /**
   * Creates a CalendarConstraints instance which opens onto {@code current}, is bounded between
   * {@code start} and {@code end}, and disables dates for which {@link DateValidator#isValid(long)}
   * is false.
   */
  public static CalendarConstraints create(
      Month start, Month end, Month current, DateValidator validator) {
    return new CalendarConstraints(start, end, current, validator);
  }

  public DateValidator getDateValidator() {
    return validator;
  }

  /** Returns the earliest {@link Month} allowed by this set of bounds. */
  public Month getStart() {
    return start;
  }

  /** Returns the latest {@link Month} allowed by this set of bounds. */
  public Month getEnd() {
    return end;
  }

  /** Returns the current {@link Month} within this set of bounds. */
  public Month getCurrent() {
    return current;
  }

  /**
   * Returns the total number of {@link java.util.Calendar#MONTH} included in {@code start} to
   * {@code end}.
   */
  int getMonthSpan() {
    return monthSpan;
  }

  /**
   * Returns the total number of {@link java.util.Calendar#YEAR} included in {@code start} to {@code
   * end}.
   */
  int getYearSpan() {
    return yearSpan;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof CalendarConstraints)) {
      return false;
    }
    CalendarConstraints that = (CalendarConstraints) o;
    return start.equals(that.start) && end.equals(that.end) && current.equals(that.current);
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {start, end, current};
    return Arrays.hashCode(hashedFields);
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<CalendarConstraints> CREATOR =
      new Parcelable.Creator<CalendarConstraints>() {
        @Override
        public CalendarConstraints createFromParcel(Parcel source) {
          Month start = source.readParcelable(Month.class.getClassLoader());
          Month end = source.readParcelable(Month.class.getClassLoader());
          Month current = source.readParcelable(Month.class.getClassLoader());
          DateValidator validator = source.readParcelable(DateValidator.class.getClassLoader());
          return CalendarConstraints.create(start, end, current, validator);
        }

        @Override
        public CalendarConstraints[] newArray(int size) {
          return new CalendarConstraints[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeParcelable(start, /* parcelableFlags= */ 0);
    dest.writeParcelable(end, /* parcelableFlags= */ 0);
    dest.writeParcelable(current, /* parcelableFlags= */ 0);
    dest.writeParcelable(validator, /* parcelableFlags = */ 0);
  }
}
