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
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

/** Test for {@link com.google.android.material.datepicker.MaterialDatePicker}. */
@RunWith(RobolectricTestRunner.class)
public class MaterialDatePickerTest {

  private static final long ONE_DAY_MILLIS = Duration.ofDays(1).toMillis();

  @Test
  public void testTodayInUtcMilliseconds() {
    long todayUtcMS = MaterialDatePicker.todayInUtcMilliseconds();
    Calendar outputUtcTodayInCalendar = Calendar.getInstance(UtcDates.getTimeZone());
    outputUtcTodayInCalendar.setTimeInMillis(todayUtcMS);
    Calendar expectedUtcTodayInCalendar = Calendar.getInstance(UtcDates.getTimeZone());

    // Assert fields finer than a day are stripped.
    assertThat(todayUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert this is the same date as today in UTC.
    assertThat(outputUtcTodayInCalendar.get(Calendar.YEAR))
        .isEqualTo(expectedUtcTodayInCalendar.get(Calendar.YEAR));
    assertThat(outputUtcTodayInCalendar.get(Calendar.MONTH))
        .isEqualTo(expectedUtcTodayInCalendar.get(Calendar.MONTH));
    assertThat(outputUtcTodayInCalendar.get(Calendar.DATE))
        .isEqualTo(expectedUtcTodayInCalendar.get(Calendar.DATE));
  }

  @Test
  public void testThisMonthInUtcMilliseconds() {
    long thisMonthUtcMS = MaterialDatePicker.thisMonthInUtcMilliseconds();
    Calendar outputUtcThisMonthInCalendar = Calendar.getInstance(UtcDates.getTimeZone());
    outputUtcThisMonthInCalendar.setTimeInMillis(thisMonthUtcMS);
    Calendar expectedUtcThisMonthInCalendar = Calendar.getInstance(UtcDates.getTimeZone());

    // Assert fields finer than a day are stripped.
    assertThat(thisMonthUtcMS % ONE_DAY_MILLIS).isEqualTo(0);
    // Assert this is the first day of this month in UTC.
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.YEAR))
        .isEqualTo(expectedUtcThisMonthInCalendar.get(Calendar.YEAR));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.MONTH))
        .isEqualTo(expectedUtcThisMonthInCalendar.get(Calendar.MONTH));
    assertThat(outputUtcThisMonthInCalendar.get(Calendar.DATE)).isEqualTo(1);
  }
}
