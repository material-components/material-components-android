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

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Used to limit the display range of {@link MaterialCalendar} and set a starting {@link Month}.
 */
public final class CalendarConstraints implements Parcelable {

  @NonNull private final Month start;
  @NonNull private final Month end;
  @NonNull private final Month opening;
  private final DateValidator validator;

  private final int yearSpan;
  private final int monthSpan;

  /** Used to determine whether {@link MaterialCalendar} days are enabled. */
  public interface DateValidator extends Parcelable {

    /** Returns true if the provided {@code date} is enabled. */
    boolean isValid(long date);
  }

  private CalendarConstraints(
      @NonNull Month start, @NonNull Month end, @NonNull Month opening, DateValidator validator) {
    this.start = start;
    this.end = end;
    this.opening = opening;
    this.validator = validator;
    if (start.compareTo(opening) > 0) {
      throw new IllegalArgumentException("start Month cannot be after current Month");
    }
    if (opening.compareTo(end) > 0) {
      throw new IllegalArgumentException("current Month cannot be after end Month");
    }
    monthSpan = start.monthsUntil(end) + 1;
    yearSpan = end.year - start.year + 1;
  }

  boolean isWithinBounds(long date) {
    return start.getDay(1) <= date && date <= end.getDay(end.daysInMonth);
  }

  /**
   * Returns the {@link DateValidator} that determines whether a date can be clicked and selected.
   */
  public DateValidator getDateValidator() {
    return validator;
  }

  /** Returns the earliest {@link Month} allowed by this set of bounds. */
  @NonNull
  public Month getStart() {
    return start;
  }

  /** Returns the latest {@link Month} allowed by this set of bounds. */
  @NonNull
  public Month getEnd() {
    return end;
  }

  /** Returns the opening {@link Month} within this set of bounds. */
  @NonNull
  public Month getOpening() {
    return opening;
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
    return start.equals(that.start)
        && end.equals(that.end)
        && opening.equals(that.opening)
        && validator.equals(that.validator);
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {start, end, opening, validator};
    return Arrays.hashCode(hashedFields);
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<CalendarConstraints> CREATOR =
      new Parcelable.Creator<CalendarConstraints>() {
        @NonNull
        @Override
        public CalendarConstraints createFromParcel(@NonNull Parcel source) {
          Month start = source.readParcelable(Month.class.getClassLoader());
          Month end = source.readParcelable(Month.class.getClassLoader());
          Month opening = source.readParcelable(Month.class.getClassLoader());
          DateValidator validator = source.readParcelable(DateValidator.class.getClassLoader());
          return new CalendarConstraints(start, end, opening, validator);
        }

        @NonNull
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
    dest.writeParcelable(opening, /* parcelableFlags= */ 0);
    dest.writeParcelable(validator, /* parcelableFlags = */ 0);
  }

  /** Builder for {@link com.google.android.material.datepicker.CalendarConstraints}. */
  public static class Builder {

    /** Default for the first selectable {@link Month} unless {@link Builder#setStart} is called. */
    public static final Month DEFAULT_START = Month.create(1900, Calendar.JANUARY);
    /** Default for the last selectable {@link Month} unless {@link Builder#setEnd} is called. */
    public static final Month DEFAULT_END = Month.create(2100, Calendar.DECEMBER);

    private static final String DEEP_COPY_VALIDATOR_KEY = "DEEP_COPY_VALIDATOR_KEY";

    private Month start = DEFAULT_START;
    private Month end = DEFAULT_END;
    private Month opening;
    private DateValidator validator = new DateValidatorPointForward(Long.MIN_VALUE);

    public Builder() {}

    Builder(@NonNull CalendarConstraints clone) {
      start = clone.start;
      end = clone.end;
      opening = clone.opening;
      validator = clone.validator;
    }

    /** The earliest valid {@link Month} that can be selected. Defaults January, 1900. */
    @NonNull
    public Builder setStart(Month month) {
      start = month;
      return this;
    }

    /** The latest valid {@link Month} that can be selected. Defaults December, 2100. */
    @NonNull
    public Builder setEnd(Month month) {
      end = month;
      return this;
    }

    /**
     * The {@link Month} the {@link MaterialCalendar} should open to. If valid, defaults to {@link
     * Month#today()} otherwise {@code start}.
     */
    @NonNull
    public Builder setOpening(Month month) {
      opening = month;
      return this;
    }

    /**
     * Limits valid dates to those for which {@link DateValidator#isValid(long)} is true. Defaults
     * to all dates as valid.
     */
    @NonNull
    public Builder setValidator(DateValidator validator) {
      this.validator = validator;
      return this;
    }

    /** Builds the {@link CalendarConstraints} object using the set parameters or defaults. */
    @NonNull
    public CalendarConstraints build() {
      if (opening == null) {
        Month today = Month.today();
        opening = start.compareTo(today) <= 0 && today.compareTo(end) <= 0 ? today : start;
      }
      Bundle deepCopyBundle = new Bundle();
      deepCopyBundle.putParcelable(DEEP_COPY_VALIDATOR_KEY, validator);
      return new CalendarConstraints(
          start,
          end,
          opening,
          (DateValidator) deepCopyBundle.getParcelable(DEEP_COPY_VALIDATOR_KEY));
    }
  }
}
