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
import androidx.annotation.VisibleForTesting;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Contains convenience operations for a month within a specific year.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
@VisibleForTesting(otherwise = VisibleForTesting.PACKAGE_PRIVATE)
public final class Month implements Comparable<Month>, Parcelable {

  private final Calendar calendar;
  private static final SimpleDateFormat longNameFormat =
      new SimpleDateFormat("MMMM, yyyy", Locale.getDefault());
  private final String longName;
  final int month;
  final int year;
  final int daysInWeek;
  final int daysInMonth;

  private Month(Calendar calendar) {
    this.calendar = calendar;
    this.calendar.set(Calendar.DAY_OF_MONTH, 1);
    month = calendar.get(Calendar.MONTH);
    year = calendar.get(Calendar.YEAR);
    daysInWeek = this.calendar.getMaximum(Calendar.DAY_OF_WEEK);
    daysInMonth = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
    longName = longNameFormat.format(this.calendar.getTime());
  }

  /**
   * Creates an instance of Month with the given parameters backed by a {@link Calendar}.
   *
   * @param year The year
   * @param month The 0-index based month. Use {@link Calendar} constants (e.g., {@link
   *     Calendar#JANUARY}
   * @return A Month object backed by a new {@link Calendar} instance
   */
  @VisibleForTesting
  public static Month create(int year, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    return new Month(calendar);
  }

  /** Returns the {@link Month} that contains today (as per {@link Calendar#getInstance()}. */
  public static Month today() {
    Calendar calendar = Calendar.getInstance();
    return Month.create(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
  }

  int daysFromStartOfWeekToFirstOfMonth() {
    int difference = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek();
    if (difference < 0) {
      difference = difference + daysInWeek;
    }
    return difference;
  }

  /**
   * Gets a {@link Calendar} for the specific day within the instance's month and year.
   *
   * <p>This method only guarantees validity with respect to {@link Calendar#isLenient()}.
   *
   * @param day The desired day within this month and year
   * @return A new {@link Calendar} instance for the given day within the specified month and year
   */
  Calendar getDay(int day) {
    Calendar calendar = ((Calendar) this.calendar.clone());
    calendar.set(Calendar.DAY_OF_MONTH, day);
    return calendar;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof Month)) {
      return false;
    }
    Month that = (Month) o;
    return month == that.month && year == that.year;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {month, year};
    return Arrays.hashCode(hashedFields);
  }

  @Override
  public int compareTo(Month other) {
    return calendar.compareTo(other.calendar);
  }

  /**
   * Returns the number of months from this Month to the provided Month.
   *
   * <p>0 when {@code this.compareTo(other)} is 0. Negative when {@code this.compareTo(other)} is
   * negative.
   *
   * @throws IllegalArgumentException when {@link Calendar#getInstance()} is not an instance of
   *     {@link GregorianCalendar}
   */
  int monthsUntil(Month other) {
    if (calendar instanceof GregorianCalendar) {
      return (other.year - year) * 12 + (other.month - month);
    } else {
      throw new IllegalArgumentException("Only Gregorian calendars are supported.");
    }
  }

  /**
   * Returns a {@link com.google.android.material.picker.Month} {@code months} months after this
   * instance.
   */
  Month monthsLater(int months) {
    Calendar laterMonth = ((Calendar) calendar.clone());
    laterMonth.add(Calendar.MONTH, months);
    return new Month(laterMonth);
  }

  /** Returns a localized String representation of the month name and year. */
  String getLongName() {
    return longName;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<Month> CREATOR =
      new Parcelable.Creator<Month>() {
        @Override
        public Month createFromParcel(Parcel source) {
          int year = source.readInt();
          int month = source.readInt();
          return Month.create(year, month);
        }

        @Override
        public Month[] newArray(int size) {
          return new Month[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(year);
    dest.writeInt(month);
  }
}
