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

import static org.junit.Assert.assertEquals;

import com.google.android.material.internal.ParcelableTestUtils;
import java.util.Calendar;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class CalendarConstraintsTest {

  private static final long FEB_2016 = Month.create(2016, Calendar.FEBRUARY).timeInMillis;
  private static final long MARCH_2016 = Month.create(2016, Calendar.MARCH).timeInMillis;
  private static final long APRIL_2016 = Month.create(2016, Calendar.APRIL).timeInMillis;

  @Rule public final ExpectedException exceptionRule = ExpectedException.none();

  @Test
  public void equalAfterParceling() {
    CalendarConstraints originalBounds =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    CalendarConstraints constructedBounds =
        ParcelableTestUtils.parcelAndCreate(originalBounds, CalendarConstraints.CREATOR);
    assertEquals(originalBounds, constructedBounds);
  }

  @Test
  public void currentDefaultsToTodayIfWithinBounds() {
    Month today = Month.today();
    long start = today.monthsLater(-1).timeInMillis;
    long end = today.monthsLater(1).timeInMillis;
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(start).setEnd(end).build();
    assertEquals(today, calendarConstraints.getOpenAt());
  }

  @Test
  public void currentDefaultsToStartIfTodayIsInvalid() {
    CalendarConstraints calendarConstraints =
        new CalendarConstraints.Builder().setStart(FEB_2016).setEnd(APRIL_2016).build();
    assertEquals(FEB_2016, calendarConstraints.getOpenAt().timeInMillis);
  }

  @Test
  public void illegalCurrentMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new CalendarConstraints.Builder()
        .setStart(FEB_2016)
        .setEnd(MARCH_2016)
        .setOpenAt(APRIL_2016)
        .build();
  }

  @Test
  public void illegalEndMonthFails() {
    exceptionRule.expect(IllegalArgumentException.class);
    new CalendarConstraints.Builder()
        .setStart(MARCH_2016)
        .setEnd(FEB_2016)
        .setOpenAt(MARCH_2016)
        .build();
  }
}
