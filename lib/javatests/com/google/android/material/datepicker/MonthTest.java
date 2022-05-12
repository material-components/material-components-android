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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MonthTest {

  private static final Locale ISRAEL = new Locale("iw", "IL");

  private Month monthFeb2016;
  private Month monthJul2018;
  private Month monthFeb2019;

  private void setupLocalizedCalendars(Locale locale) {
    Locale.setDefault(locale);
    monthFeb2016 = Month.create(2016, Calendar.FEBRUARY);
    monthJul2018 = Month.create(2018, Calendar.JULY);
    monthFeb2019 = Month.create(2019, Calendar.FEBRUARY);
  }

  @Test
  public void usDaysInWeek() {
    setupLocalizedCalendars(Locale.US);
    assertDaysInWeek();
  }

  @Test
  public void frDaysInWeek() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertDaysInWeek();
  }

  @Test
  public void ilDaysInWeek() {
    setupLocalizedCalendars(ISRAEL);
    assertDaysInWeek();
  }

  private void assertDaysInWeek() {
    assertEquals(7, monthFeb2016.daysInWeek);
    assertEquals(7, monthJul2018.daysInWeek);
    assertEquals(7, monthFeb2019.daysInWeek);
  }

  @Test
  public void usDaysInMonth() {
    setupLocalizedCalendars(Locale.US);
    assertDaysInMonth();
  }

  @Test
  public void frDaysInMonth() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertDaysInMonth();
  }

  @Test
  public void ilDaysInMonth() {
    setupLocalizedCalendars(ISRAEL);
    assertDaysInMonth();
  }

  private void assertDaysInMonth() {
    assertEquals(29, monthFeb2016.daysInMonth);
    assertEquals(31, monthJul2018.daysInMonth);
    assertEquals(28, monthFeb2019.daysInMonth);
  }

  @Test
  public void usDaysFromStart() {
    setupLocalizedCalendars(Locale.US);
    Map<Month, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthFeb2016, 1);
    localizedStartOfWeekToStartOfMonth.put(monthJul2018, 0);
    localizedStartOfWeekToStartOfMonth.put(monthFeb2019, 5);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  @Test
  public void frDaysFromStart() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<Month, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthFeb2016, 0);
    localizedStartOfWeekToStartOfMonth.put(monthJul2018, 6);
    localizedStartOfWeekToStartOfMonth.put(monthFeb2019, 4);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  @Test
  public void ilDaysFromStart() {
    setupLocalizedCalendars(ISRAEL);
    Map<Month, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthFeb2016, 1);
    localizedStartOfWeekToStartOfMonth.put(monthJul2018, 0);
    localizedStartOfWeekToStartOfMonth.put(monthFeb2019, 5);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  @Test
  public void gregorianDifferenceOfMonthsCalculation() {
    setupLocalizedCalendars(Locale.getDefault());
    Month march2016 = Month.create(2016, Calendar.MARCH);

    assertEquals(0, monthFeb2016.monthsUntil(monthFeb2016));
    assertEquals(29, monthFeb2016.monthsUntil(monthJul2018));
    assertEquals(-29, monthJul2018.monthsUntil(monthFeb2016));

    assertEquals(1, monthFeb2016.monthsUntil(march2016));
    assertEquals(-1, march2016.monthsUntil(monthFeb2016));
  }

  @Test
  public void addingMonthsCalculation() {
    setupLocalizedCalendars(Locale.getDefault());
    Month calculatedSameMonth = monthFeb2016.monthsLater(0);
    assertEquals(2016, calculatedSameMonth.year);
    assertEquals(Calendar.FEBRUARY, calculatedSameMonth.month);

    Month calculatedLaterMonth = monthFeb2016.monthsLater(24);
    assertEquals(2018, calculatedLaterMonth.year);
    assertEquals(Calendar.FEBRUARY, calculatedLaterMonth.month);

    Month calculatedEarlierMonth = monthFeb2016.monthsLater(-2);
    assertEquals(2015, calculatedEarlierMonth.year);
    assertEquals(Calendar.DECEMBER, calculatedEarlierMonth.month);
  }

  @Test
  public void equalAfterParceling() {
    Month original = Month.create(2019, Calendar.JULY);
    Month constructed = ParcelableTestUtils.parcelAndCreate(original, Month.CREATOR);
    assertEquals(original, constructed);
  }

  private void assertDaysFromStart(Map<Month, Integer> localizedStartOfWeekToStartOfMonth) {
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthFeb2016),
        monthFeb2016.daysFromStartOfWeekToFirstOfMonth(0));
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthJul2018),
        monthJul2018.daysFromStartOfWeekToFirstOfMonth(0));
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthFeb2019),
        monthFeb2019.daysFromStartOfWeekToFirstOfMonth(0));
  }
}
