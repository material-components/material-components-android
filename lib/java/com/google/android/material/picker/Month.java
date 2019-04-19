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

import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Contains convenience operations for a month within a specific year.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class Month {

  private final Calendar calendar;
  public final int month;
  public final int year;
  public final int daysInWeek;
  public final int daysInMonth;

  private Month(Calendar calendar) {
    this.calendar = calendar;
    this.calendar.set(Calendar.DAY_OF_MONTH, 1);
    month = calendar.get(Calendar.MONTH);
    year = calendar.get(Calendar.YEAR);
    daysInWeek = this.calendar.getMaximum(Calendar.DAY_OF_WEEK);
    daysInMonth = this.calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
  }

  /**
   * Creates an instance of Month with the given parameters backed by a {@link Calendar}.
   *
   * @param year The year
   * @param month The 0-index based month. Use {@link Calendar} constants (e.g., {@link
   *     Calendar#JANUARY}
   * @return A Month object backed by a new {@link Calendar} instance
   */
  public static Month create(int year, int month) {
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    return new Month(calendar);
  }

  public int daysFromStartOfWeekToFirstOfMonth() {
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
  public Calendar getDay(int day) {
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
}
