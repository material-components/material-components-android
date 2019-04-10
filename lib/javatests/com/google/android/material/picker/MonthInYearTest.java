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

import static org.junit.Assert.assertEquals;

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
public class MonthInYearTest {

  private static final Locale ISRAEL = new Locale("iw", "IL");

  private MonthInYear monthInYearFeb2016;
  private MonthInYear monthInYearJul2018;
  private MonthInYear monthInYearFeb2019;

  private void setupLocalizedCalendars(Locale locale) {
    Locale.setDefault(locale);
    monthInYearFeb2016 = MonthInYear.create(2016, Calendar.FEBRUARY);
    monthInYearJul2018 = MonthInYear.create(2018, Calendar.JULY);
    monthInYearFeb2019 = MonthInYear.create(2019, Calendar.FEBRUARY);
  }

  @Test
  public void usDaysInWeek() {
    setupLocalizedCalendars(Locale.US);
    assertDaysInWeek();
  }

  @Test
  public void frDaysInWeek() {
    ;
    setupLocalizedCalendars(Locale.FRANCE);
    assertDaysInWeek();
  }

  @Test
  public void ilDaysInWeek() {
    setupLocalizedCalendars(ISRAEL);
    assertDaysInWeek();
  }

  private void assertDaysInWeek() {
    assertEquals(7, monthInYearFeb2016.daysInWeek);
    assertEquals(7, monthInYearJul2018.daysInWeek);
    assertEquals(7, monthInYearFeb2019.daysInWeek);
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
    assertEquals(29, monthInYearFeb2016.daysInMonth);
    assertEquals(31, monthInYearJul2018.daysInMonth);
    assertEquals(28, monthInYearFeb2019.daysInMonth);
  }

  @Test
  public void usDaysFromStart() {
    setupLocalizedCalendars(Locale.US);
    Map<MonthInYear, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2016, 1);
    localizedStartOfWeekToStartOfMonth.put(monthInYearJul2018, 0);
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2019, 5);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  @Test
  public void frDaysFromStart() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<MonthInYear, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2016, 0);
    localizedStartOfWeekToStartOfMonth.put(monthInYearJul2018, 6);
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2019, 4);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  @Test
  public void ilDaysFromStart() {
    setupLocalizedCalendars(ISRAEL);
    Map<MonthInYear, Integer> localizedStartOfWeekToStartOfMonth = new HashMap<>();
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2016, 1);
    localizedStartOfWeekToStartOfMonth.put(monthInYearJul2018, 0);
    localizedStartOfWeekToStartOfMonth.put(monthInYearFeb2019, 5);
    assertDaysFromStart(localizedStartOfWeekToStartOfMonth);
  }

  private void assertDaysFromStart(Map<MonthInYear, Integer> localizedStartOfWeekToStartOfMonth) {
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthInYearFeb2016),
        monthInYearFeb2016.daysFromStartOfWeekToFirstOfMonth());
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthInYearJul2018),
        monthInYearJul2018.daysFromStartOfWeekToFirstOfMonth());
    assertEquals(
        (int) localizedStartOfWeekToStartOfMonth.get(monthInYearFeb2019),
        monthInYearFeb2019.daysFromStartOfWeekToFirstOfMonth());
  }
}
