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

import static com.google.common.truth.Truth.assertThat;
import static java.util.Calendar.APRIL;
import static java.util.Calendar.FEBRUARY;
import static java.util.Calendar.MARCH;
import static org.junit.Assert.assertEquals;

import java.time.Duration;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Test for {@link com.google.android.material.datepicker.MaterialDatePicker}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialDatePickerTest {

  private static final long ONE_DAY_MILLIS = Duration.ofDays(1).toMillis();

  // Tuesday, December 31, 2019 11:59:00 PM GMT-05:00
  private static final long NEW_YORK_TIME_2019_12_31_11_59_00_PM = 1577854740000L;
  // Tuesday, January 1, 2020 09:00:00 AM UTC+13:00
  private static final long NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM = 1577822400000L;
  private static final TimeZone UTC_TIME_ZONE = TimeZone.getTimeZone("UTC");

  private static final long FEB_2016 = Month.create(2016, FEBRUARY).timeInMillis;
  private static final long MARCH_2016 = Month.create(2016, MARCH).timeInMillis;
  private static final long APRIL_2016 = Month.create(2016, APRIL).timeInMillis;

  private static Calendar setTestLocalTime(long testTimeAsEpochMs, TimeZone timeZone) {
    TimeSource fixedTimeSource = TimeSource.fixed(testTimeAsEpochMs, timeZone);
    UtcDates.setTimeSource(fixedTimeSource);
    Calendar testTimeInUtc = fixedTimeSource.now();
    testTimeInUtc.set(Calendar.HOUR_OF_DAY, 0);
    testTimeInUtc.set(Calendar.MINUTE, 0);
    testTimeInUtc.set(Calendar.SECOND, 0);
    testTimeInUtc.set(Calendar.MILLISECOND, 0);
    testTimeInUtc.setTimeZone(UTC_TIME_ZONE);

    return testTimeInUtc;
  }

  private static void testTodayInUtcMillisecondsForLocalTime(
      long testTimeAsEpochMs, TimeZone timeZone) {
    Calendar firstMomentOfTodayInUtc = setTestLocalTime(testTimeAsEpochMs, timeZone);

    long todayUtcMS = MaterialDatePicker.todayInUtcMilliseconds();
    Calendar outputUtcTodayInCalendar = Calendar.getInstance(UTC_TIME_ZONE);
    outputUtcTodayInCalendar.setTimeInMillis(todayUtcMS);

    // Assert fields finer than a day are stripped.
    assertThat(todayUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert date returned by datepicker is same as first moment of today from UTC calender.
    assertThat(outputUtcTodayInCalendar).isEqualTo(firstMomentOfTodayInUtc);
  }

  private static void testThisMonthInUtcMillisecondsForLocalTime(
      long testTimeAsEpochMs, TimeZone timeZone) {
    Calendar firstMomentOfTodayInUtc = setTestLocalTime(testTimeAsEpochMs, timeZone);

    long thisMonthUtcMS = MaterialDatePicker.thisMonthInUtcMilliseconds();
    Calendar outputUtcThisMonthInCalendar = Calendar.getInstance(UTC_TIME_ZONE);
    outputUtcThisMonthInCalendar.setTimeInMillis(thisMonthUtcMS);

    // Assert fields finer than a day are stripped.
    assertThat(thisMonthUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert this is the first day of this month in UTC.
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.YEAR))
        .isEqualTo(firstMomentOfTodayInUtc.get(Calendar.YEAR));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.MONTH))
        .isEqualTo(firstMomentOfTodayInUtc.get(Calendar.MONTH));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.DATE)).isEqualTo(1);
  }

  @Test
  public void testSelectionAsOpenAt() {

    MaterialDatePicker.Builder<Long> datePickerBuilder = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    datePickerBuilder.setCalendarConstraints(calendarConstraints);
    datePickerBuilder.setSelection(APRIL_2016);
    datePickerBuilder.build();
    assertEquals(APRIL_2016, calendarConstraints.getOpenAt().timeInMillis);

    MaterialDatePicker.Builder<Long> datePickerBuilder2 = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints2 =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    datePickerBuilder2.setCalendarConstraints(calendarConstraints2);
    datePickerBuilder2.build();
    assertEquals(FEB_2016, calendarConstraints2.getOpenAt().timeInMillis);

    MaterialDatePicker.Builder<Long> datePickerBuilder3 = MaterialDatePicker.Builder.datePicker();
    CalendarConstraints calendarConstraints3 =
        new CalendarConstraints.Builder()
            .setStart(FEB_2016)
            .setEnd(APRIL_2016)
            .setOpenAt(MARCH_2016)
            .build();

    datePickerBuilder3.setCalendarConstraints(calendarConstraints3);
    datePickerBuilder3.setSelection(APRIL_2016);
    datePickerBuilder3.build();
    assertEquals(MARCH_2016, calendarConstraints3.getOpenAt().timeInMillis);
  }

  @Test
  public void testTodayInUtcMilliseconds() {
    testTodayInUtcMillisecondsForLocalTime(
        NEW_YORK_TIME_2019_12_31_11_59_00_PM, TimeZone.getTimeZone("America/New_York"));
    testTodayInUtcMillisecondsForLocalTime(
        NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM, TimeZone.getTimeZone("NZ"));
  }

  @Test
  public void testThisMonthInUtcMilliseconds() {
    testThisMonthInUtcMillisecondsForLocalTime(
        NEW_YORK_TIME_2019_12_31_11_59_00_PM, TimeZone.getTimeZone("America/New_York"));
    testThisMonthInUtcMillisecondsForLocalTime(
        NEW_ZEALAND_TIME_2020_01_11_09_00_00_AM, TimeZone.getTimeZone("NZ"));
  }
}
