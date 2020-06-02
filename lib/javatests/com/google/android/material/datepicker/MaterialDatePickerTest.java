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

import java.time.Duration;
import java.util.Calendar;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Test for {@link com.google.android.material.datepicker.MaterialDatePicker}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialDatePickerTest {

  private static final long ONE_DAY_MILLIS = Duration.ofDays(1).toMillis();

  // Tuesday, December 31, 2019 11:59:00 PM GMT-05:00
  private static final long CURRENT_MOMENT_OF_TODAY_EPOCH_MS = 1577854740000L;
  private Calendar firstMomentOfTodayInUtc;
  private TimeZone utcTimeZone;

  @Before
  public void setup() {
    utcTimeZone = TimeZone.getTimeZone("UTC");
    TimeSource fixedTimeSource = TimeSource.fixed(
            CURRENT_MOMENT_OF_TODAY_EPOCH_MS, TimeZone.getTimeZone("America/New_York"));
    UtcDates.setTimeSource(fixedTimeSource);
    firstMomentOfTodayInUtc = fixedTimeSource.now();
    firstMomentOfTodayInUtc.set(Calendar.HOUR_OF_DAY, 0);
    firstMomentOfTodayInUtc.set(Calendar.MINUTE, 0);
    firstMomentOfTodayInUtc.set(Calendar.SECOND, 0);
    firstMomentOfTodayInUtc.set(Calendar.MILLISECOND, 0);
    firstMomentOfTodayInUtc.setTimeZone(TimeZone.getTimeZone("UTC"));
  }

  @Test
  public void testTodayInUtcMilliseconds() {
    long todayUtcMS = MaterialDatePicker.todayInUtcMilliseconds();
    Calendar outputUtcTodayInCalendar = Calendar.getInstance(utcTimeZone);
    outputUtcTodayInCalendar.setTimeInMillis(todayUtcMS);

    // Assert fields finer than a day are stripped.
    assertThat(todayUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert date returned by datepicker is same as first moment of today from UTC calender.
    assertThat(outputUtcTodayInCalendar).isEqualTo(firstMomentOfTodayInUtc);
  }

  @Test
  public void testThisMonthInUtcMilliseconds() {
    long thisMonthUtcMS = MaterialDatePicker.thisMonthInUtcMilliseconds();
    Calendar outputUtcThisMonthInCalendar = Calendar.getInstance(utcTimeZone);
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
}
