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

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import androidx.test.core.app.ApplicationProvider;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MonthAdapterTest {

  private static final Locale ISRAEL = new Locale("iw", "IL");

  private MonthAdapter monthFeb2019;
  private MonthAdapter monthFeb2016;
  private MonthAdapter monthJuly2018;
  private MonthAdapter monthMarch2019;

  @Before
  public void setupMonthAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
  }

  private void setupLocalizedCalendars(Locale locale) {
    Locale.setDefault(locale);
    CalendarConstraints defaultConstraints = new CalendarConstraints.Builder().build();
    SingleDateSelector singleDateSelector = new SingleDateSelector();
    Month feb2016 = Month.create(2016, Calendar.FEBRUARY);
    monthFeb2016 = new MonthAdapter(feb2016, singleDateSelector, defaultConstraints);
    Month july2018 = Month.create(2018, Calendar.JULY);
    monthJuly2018 = new MonthAdapter(july2018, singleDateSelector, defaultConstraints);
    Month feb2019 = Month.create(2019, Calendar.FEBRUARY);
    monthFeb2019 = new MonthAdapter(feb2019, singleDateSelector, defaultConstraints);
    Month march2019 = Month.create(2019, Calendar.MARCH);
    monthMarch2019 = new MonthAdapter(march2019, singleDateSelector, defaultConstraints);
  }

  @Test
  public void usLastPositionMatchesMonthLength() {
    setupLocalizedCalendars(Locale.US);
    assertLastPositionMatchesMonthLength();
  }

  @Test
  public void frLastPositionMatchesMonthLength() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertLastPositionMatchesMonthLength();
  }

  @Test
  public void ilLastPositionMatchesMonthLength() {
    setupLocalizedCalendars(ISRAEL);
    assertLastPositionMatchesMonthLength();
  }

  private void assertLastPositionMatchesMonthLength() {
    assertEquals(29, monthFeb2016.positionToDay(monthFeb2016.lastPositionInMonth()));
    assertEquals(31, monthJuly2018.positionToDay(monthJuly2018.lastPositionInMonth()));
    assertEquals(28, monthFeb2019.positionToDay(monthFeb2019.lastPositionInMonth()));
  }

  @Test
  public void usMaxPosition() {
    setupLocalizedCalendars(Locale.US);
    Map<MonthAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthFeb2016, 29);
    localizedMaxPositionInMonth.put(monthJuly2018, 30);
    localizedMaxPositionInMonth.put(monthFeb2019, 32);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  @Test
  public void frMaxPosition() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<MonthAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthFeb2016, 28);
    localizedMaxPositionInMonth.put(monthJuly2018, 36);
    localizedMaxPositionInMonth.put(monthFeb2019, 31);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  @Test
  public void ilMaxPosition() {
    setupLocalizedCalendars(ISRAEL);
    Map<MonthAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthFeb2016, 29);
    localizedMaxPositionInMonth.put(monthJuly2018, 30);
    localizedMaxPositionInMonth.put(monthFeb2019, 32);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  private void assertMaxPosition(Map<MonthAdapter, Integer> localizedMaxPositionInMonth) {
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthFeb2016), monthFeb2016.lastPositionInMonth());
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthJuly2018), monthJuly2018.lastPositionInMonth());
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthFeb2019), monthFeb2019.lastPositionInMonth());
  }

  @Test
  public void usPositions() {
    setupLocalizedCalendars(Locale.US);
    Map<MonthAdapter, Integer> localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthFeb2016, 1);
    localizedPositionToDay.put(monthJuly2018, 7);
    localizedPositionToDay.put(monthFeb2019, 11);
    assertPositionsForDays(localizedPositionToDay);
  }

  @Test
  public void frPositions() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<MonthAdapter, Integer> localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthFeb2016, 2);
    localizedPositionToDay.put(monthJuly2018, 1);
    localizedPositionToDay.put(monthFeb2019, 12);
    assertPositionsForDays(localizedPositionToDay);
  }

  @Test
  public void ilPositions() {
    setupLocalizedCalendars(ISRAEL);
    Map<MonthAdapter, Integer> localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthFeb2016, 1);
    localizedPositionToDay.put(monthJuly2018, 7);
    localizedPositionToDay.put(monthFeb2019, 11);
    assertPositionsForDays(localizedPositionToDay);
  }

  private void assertPositionsForDays(Map<MonthAdapter, Integer> localizedPositionToDay) {
    assertEquals((int) localizedPositionToDay.get(monthFeb2016), monthFeb2016.positionToDay(1));
    assertEquals((int) localizedPositionToDay.get(monthJuly2018), monthJuly2018.positionToDay(6));
    assertEquals((int) localizedPositionToDay.get(monthFeb2019), monthFeb2019.positionToDay(15));
  }

  @Test
  public void usItemCount() {
    setupLocalizedCalendars(Locale.US);
    assertEquals(30, monthFeb2016.getCount());
    assertEquals(31, monthJuly2018.getCount());
    assertEquals(33, monthFeb2019.getCount());
  }

  @Test
  public void frItemCount() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertEquals(29, monthFeb2016.getCount());
    assertEquals(37, monthJuly2018.getCount());
    assertEquals(32, monthFeb2019.getCount());
  }

  @Test
  public void ilItemCount() {
    setupLocalizedCalendars(ISRAEL);
    assertEquals(30, monthFeb2016.getCount());
    assertEquals(31, monthJuly2018.getCount());
    assertEquals(33, monthFeb2019.getCount());
  }

  @Test
  public void usPositionsWithinMonthReturnAValidItem() {
    setupLocalizedCalendars(Locale.US);
    Collection<Integer> localizedNullPositionsInFebruary2019 = Arrays.asList(-5, 0, 4, 33, 100);
    Collection<Integer> localizedNonNullPositionsInFebruary2019 = Arrays.asList(5, 32);
    assertPositionsWithinMonthReturnAValidItem(
        localizedNullPositionsInFebruary2019, localizedNonNullPositionsInFebruary2019);
  }

  @Test
  public void frPositionsWithinMonthReturnAValidItem() {
    setupLocalizedCalendars(Locale.FRANCE);
    Collection<Integer> localizedNullPositionsInFebruary2019 = Arrays.asList(-5, 0, 3, 32, 100);
    Collection<Integer> localizedNonNullPositionsInFebruary2019 = Arrays.asList(4, 31);
    assertPositionsWithinMonthReturnAValidItem(
        localizedNullPositionsInFebruary2019, localizedNonNullPositionsInFebruary2019);
  }

  @Test
  public void ilPositionsWithinMonthReturnAValidItem() {
    setupLocalizedCalendars(ISRAEL);
    Collection<Integer> localizedNullPositionsInFebruary2019 = Arrays.asList(-5, 0, 4, 33, 100);
    Collection<Integer> localizedNonNullPositionsInFebruary2019 = Arrays.asList(5, 32);
    assertPositionsWithinMonthReturnAValidItem(
        localizedNullPositionsInFebruary2019, localizedNonNullPositionsInFebruary2019);
  }

  private void assertPositionsWithinMonthReturnAValidItem(
      Collection<Integer> localizedNullPositionsInFebruary2019,
      Collection<Integer> localizedNonNullPositionsInFebruary2019) {
    for (int position : localizedNullPositionsInFebruary2019) {
      assertNull(monthFeb2019.getItem(position));
    }
    for (int position : localizedNonNullPositionsInFebruary2019) {
      assertNotNull(monthFeb2019.getItem(position));
    }
  }

  @Test
  public void usDaysOfPositions() {
    setupLocalizedCalendars(Locale.US);
    Map<Integer, Integer> localizedDaysOfPositionsInFebruary2019 = new HashMap<>();
    localizedDaysOfPositionsInFebruary2019.put(6, 2);
    localizedDaysOfPositionsInFebruary2019.put(32, 28);
    assertDaysOfPositions(localizedDaysOfPositionsInFebruary2019);
  }

  @Test
  public void frDaysOfPositions() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<Integer, Integer> localizedDaysOfPositionsInFebruary2019 = new HashMap<>();
    localizedDaysOfPositionsInFebruary2019.put(6, 3);
    localizedDaysOfPositionsInFebruary2019.put(31, 28);
    assertDaysOfPositions(localizedDaysOfPositionsInFebruary2019);
  }

  @Test
  public void ilDaysOfPositions() {
    setupLocalizedCalendars(ISRAEL);
    Map<Integer, Integer> localizedDaysOfPositionsInFebruary2019 = new HashMap<>();
    localizedDaysOfPositionsInFebruary2019.put(6, 2);
    localizedDaysOfPositionsInFebruary2019.put(32, 28);
    assertDaysOfPositions(localizedDaysOfPositionsInFebruary2019);
  }

  private void assertDaysOfPositions(Map<Integer, Integer> localizedDaysOfPositionsInFebruary2019) {
    for (int day : localizedDaysOfPositionsInFebruary2019.keySet()) {
      Calendar testCalendar = Calendar.getInstance();
      testCalendar.setTimeInMillis(monthFeb2019.getItem(day));
      assertEquals(
          (int) localizedDaysOfPositionsInFebruary2019.get(day),
          testCalendar.get(Calendar.DAY_OF_MONTH));
    }
  }

  @Test
  public void rowIds() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertEquals(0, monthFeb2019.getItemId(0));
    assertEquals(1, monthFeb2019.getItemId(7));
    assertEquals(3, monthFeb2019.getItemId(26));
    assertEquals(5, monthMarch2019.getItemId(35));
  }
}
