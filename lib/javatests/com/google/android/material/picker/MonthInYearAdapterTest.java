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

import com.google.android.material.R;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import com.google.android.material.picker.selector.DateGridSelector;
import androidx.appcompat.app.AppCompatActivity;
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
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@DoNotInstrument
public class MonthInYearAdapterTest {

  private static final Locale ISRAEL = new Locale("iw", "IL");

  private Context context;
  private MonthInYearAdapter monthInYearFeb2019;
  private MonthInYearAdapter monthInYearFeb2016;
  private MonthInYearAdapter monthInYearJul2018;
  private MonthInYearAdapter monthInYearMarch2019;

  @Before
  public void setupMonthInYearAdapters() {
    ApplicationProvider.getApplicationContext().setTheme(R.style.Theme_MaterialComponents_Light);
    AppCompatActivity activity = Robolectric.buildActivity(AppCompatActivity.class).setup().get();
    context = activity.getApplicationContext();
  }

  private void setupLocalizedCalendars(Locale locale) {
    Locale.setDefault(locale);
    DateGridSelector dateGridSelector = new DateGridSelector();
    MonthInYear feb2016 = MonthInYear.create(2016, Calendar.FEBRUARY);
    monthInYearFeb2016 = new MonthInYearAdapter(context, feb2016, dateGridSelector);
    MonthInYear july2018 = MonthInYear.create(2018, Calendar.JULY);
    monthInYearJul2018 = new MonthInYearAdapter(context, july2018, dateGridSelector);
    MonthInYear feb2019 = MonthInYear.create(2019, Calendar.FEBRUARY);
    monthInYearFeb2019 = new MonthInYearAdapter(context, feb2019, dateGridSelector);
    MonthInYear march2019 = MonthInYear.create(2019, Calendar.MARCH);
    monthInYearMarch2019 = new MonthInYearAdapter(context, march2019, dateGridSelector);
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
    assertEquals(29, monthInYearFeb2016.positionToDay(monthInYearFeb2016.lastPositionInMonth()));
    assertEquals(31, monthInYearJul2018.positionToDay(monthInYearJul2018.lastPositionInMonth()));
    assertEquals(28, monthInYearFeb2019.positionToDay(monthInYearFeb2019.lastPositionInMonth()));
  }

  @Test
  public void usMaxPosition() {
    setupLocalizedCalendars(Locale.US);
    Map<MonthInYearAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthInYearFeb2016, 29);
    localizedMaxPositionInMonth.put(monthInYearJul2018, 30);
    localizedMaxPositionInMonth.put(monthInYearFeb2019, 32);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  @Test
  public void frMaxPosition() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<MonthInYearAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthInYearFeb2016, 28);
    localizedMaxPositionInMonth.put(monthInYearJul2018, 36);
    localizedMaxPositionInMonth.put(monthInYearFeb2019, 31);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  @Test
  public void ilMaxPosition() {
    setupLocalizedCalendars(ISRAEL);
    Map<MonthInYearAdapter, Integer> localizedMaxPositionInMonth = new HashMap<>();
    localizedMaxPositionInMonth.put(monthInYearFeb2016, 29);
    localizedMaxPositionInMonth.put(monthInYearJul2018, 30);
    localizedMaxPositionInMonth.put(monthInYearFeb2019, 32);
    assertMaxPosition(localizedMaxPositionInMonth);
  }

  private void assertMaxPosition(Map<MonthInYearAdapter, Integer> localizedMaxPositionInMonth) {
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthInYearFeb2016),
        monthInYearFeb2016.lastPositionInMonth());
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthInYearJul2018),
        monthInYearJul2018.lastPositionInMonth());
    assertEquals(
        (int) localizedMaxPositionInMonth.get(monthInYearFeb2019),
        monthInYearFeb2019.lastPositionInMonth());
  }

  @Test
  public void usPositions() {
    setupLocalizedCalendars(Locale.US);
    Map<MonthInYearAdapter, Integer> localizedPositionToDay =
        localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthInYearFeb2016, 1);
    localizedPositionToDay.put(monthInYearJul2018, 7);
    localizedPositionToDay.put(monthInYearFeb2019, 11);
    assertPositionsForDays(localizedPositionToDay);
  }

  @Test
  public void frPositions() {
    setupLocalizedCalendars(Locale.FRANCE);
    Map<MonthInYearAdapter, Integer> localizedPositionToDay =
        localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthInYearFeb2016, 2);
    localizedPositionToDay.put(monthInYearJul2018, 1);
    localizedPositionToDay.put(monthInYearFeb2019, 12);
    assertPositionsForDays(localizedPositionToDay);
  }

  @Test
  public void ilPositions() {
    setupLocalizedCalendars(ISRAEL);
    Map<MonthInYearAdapter, Integer> localizedPositionToDay =
        localizedPositionToDay = new HashMap<>();
    localizedPositionToDay.put(monthInYearFeb2016, 1);
    localizedPositionToDay.put(monthInYearJul2018, 7);
    localizedPositionToDay.put(monthInYearFeb2019, 11);
    assertPositionsForDays(localizedPositionToDay);
  }

  private void assertPositionsForDays(Map<MonthInYearAdapter, Integer> localizedPositionToDay) {
    assertEquals(
        (int) localizedPositionToDay.get(monthInYearFeb2016), monthInYearFeb2016.positionToDay(1));
    assertEquals(
        (int) localizedPositionToDay.get(monthInYearJul2018), monthInYearJul2018.positionToDay(6));
    assertEquals(
        (int) localizedPositionToDay.get(monthInYearFeb2019), monthInYearFeb2019.positionToDay(15));
  }

  @Test
  public void usStableCount() {
    setupLocalizedCalendars(Locale.US);
    assertStableCount();
  }

  @Test
  public void frStableCount() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertStableCount();
  }

  @Test
  public void ilStableCount() {
    setupLocalizedCalendars(ISRAEL);
    assertStableCount();
  }

  private void assertStableCount() {
    assertEquals(6 * 7, monthInYearFeb2016.getCount());
    assertEquals(6 * 7, monthInYearJul2018.getCount());
    assertEquals(6 * 7, monthInYearFeb2019.getCount());
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
      assertNull(monthInYearFeb2019.getItem(position));
    }
    for (int position : localizedNonNullPositionsInFebruary2019) {
      assertNotNull(monthInYearFeb2019.getItem(position));
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
      assertEquals(
          (int) localizedDaysOfPositionsInFebruary2019.get(day),
          monthInYearFeb2019.getItem(day).get(Calendar.DAY_OF_MONTH));
    }
  }

  @Test
  public void rowIds() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertEquals(0, monthInYearFeb2019.getItemId(0));
    assertEquals(1, monthInYearFeb2019.getItemId(7));
    assertEquals(3, monthInYearFeb2019.getItemId(26));
    assertEquals(5, monthInYearMarch2019.getItemId(35));
  }
}
