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

import com.google.android.material.test.R;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
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
    monthFeb2016 =
        new MonthAdapter(
            feb2016, singleDateSelector, defaultConstraints, /* dayViewDecorator= */ null);
    Month july2018 = Month.create(2018, Calendar.JULY);
    monthJuly2018 =
        new MonthAdapter(
            july2018, singleDateSelector, defaultConstraints, /* dayViewDecorator= */ null);
    Month feb2019 = Month.create(2019, Calendar.FEBRUARY);
    monthFeb2019 =
        new MonthAdapter(
            feb2019, singleDateSelector, defaultConstraints, /* dayViewDecorator= */ null);
    Month march2019 = Month.create(2019, Calendar.MARCH);
    monthMarch2019 =
        new MonthAdapter(
            march2019, singleDateSelector, defaultConstraints, /* dayViewDecorator= */ null);
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
    assertThat(monthFeb2016.getCount()).isAtLeast(30);
    assertThat(monthJuly2018.getCount()).isAtLeast(31);
    assertThat(monthFeb2019.getCount()).isAtLeast(33);
  }

  @Test
  public void frItemCount() {
    setupLocalizedCalendars(Locale.FRANCE);
    assertThat(monthFeb2016.getCount()).isAtLeast(29);
    assertThat(monthJuly2018.getCount()).isAtLeast(37);
    assertThat(monthFeb2019.getCount()).isAtLeast(32);
  }

  @Test
  public void ilItemCount() {
    setupLocalizedCalendars(ISRAEL);
    assertThat(monthFeb2016.getCount()).isAtLeast(30);
    assertThat(monthJuly2018.getCount()).isAtLeast(31);
    assertThat(monthFeb2019.getCount()).isAtLeast(33);
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

  @Test
  public void dayViewDecorator_withIndicator_hasUpdatedContentDescription() {
    DayViewDecorator decorator = getDecoratedMonthAdapter().dayViewDecorator;

    CharSequence decoratorContentDescription =
        decorator.getContentDescription(
            ApplicationProvider.getApplicationContext(),
            2018,
            Calendar.JANUARY,
            17,
            true,
            false,
            "Original content description");
    assertTrue("Original content description Test".contentEquals(decoratorContentDescription));
  }

  @Test
  public void dayViewDecorator_withoutIndicator_hasOriginalContentDescription() {
    DayViewDecorator decorator = getDecoratedMonthAdapter().dayViewDecorator;

    CharSequence decoratorContentDescription =
        decorator.getContentDescription(
            ApplicationProvider.getApplicationContext(),
            2018,
            Calendar.JANUARY,
            16,
            true,
            false,
            "Original content description");
    assertTrue("Original content description".contentEquals(decoratorContentDescription));
  }

  private MonthAdapter getDecoratedMonthAdapter() {
    return new MonthAdapter(
        Month.create(2018, Calendar.JANUARY),
        new SingleDateSelector(),
        new CalendarConstraints.Builder().build(),
        new DayViewDecorator() {
          @Nullable
          @Override
          public CharSequence getContentDescription(
              @NonNull Context context,
              int year,
              int month,
              int day,
              boolean valid,
              boolean selected,
              @Nullable CharSequence originalContentDescription) {
            if (year == 2018 && month == Calendar.JANUARY && day == 17) {
              return originalContentDescription + " Test";
            }
            return super.getContentDescription(
                context, year, month, day, valid, selected, originalContentDescription);
          }

          @Override
          public int describeContents() {
            return 0;
          }

          @Override
          public void writeToParcel(@NonNull Parcel dest, int flags) {}
        });
  }

  private void assertDaysOfPositions(Map<Integer, Integer> localizedDaysOfPositionsInFebruary2019) {
    for (int day : localizedDaysOfPositionsInFebruary2019.keySet()) {
      Calendar testCalendar = UtcDates.getUtcCalendar();
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

  @Test
  public void rangeDateSelector_isStartOfRange() {
    Month month = Month.create(2016, Calendar.FEBRUARY);
    MonthAdapter monthAdapter =
        createRangeMonthAdapter(month, new Pair<>(month.getDay(1), month.getDay(10)));

    assertTrue(monthAdapter.isStartOfRange(month.getDay(1)));
  }

  @Test
  public void rangeDateSelector_isNotStartOfRange() {
    Month month = Month.create(2016, Calendar.FEBRUARY);
    MonthAdapter monthAdapter =
        createRangeMonthAdapter(month, new Pair<>(month.getDay(1), month.getDay(10)));

    assertFalse(monthAdapter.isStartOfRange(month.getDay(2)));
  }

  @Test
  public void rangeDateSelector_isEndOfRange() {
    Month month = Month.create(2016, Calendar.FEBRUARY);
    MonthAdapter monthAdapter =
        createRangeMonthAdapter(month, new Pair<>(month.getDay(1), month.getDay(10)));

    assertTrue(monthAdapter.isEndOfRange(month.getDay(10)));
  }

  @Test
  public void rangeDateSelector_isNotEndOfRange() {
    Month month = Month.create(2016, Calendar.FEBRUARY);
    MonthAdapter monthAdapter =
        createRangeMonthAdapter(month, new Pair<>(month.getDay(1), month.getDay(10)));

    assertFalse(monthAdapter.isEndOfRange(month.getDay(9)));
  }

  @Test
  public void isDayPositionValid_withValidator() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    assertThat(adapter.isDayPositionValid(adapter.dayToPosition(14))).isFalse();
    assertThat(adapter.isDayPositionValid(adapter.dayToPosition(15))).isTrue();
  }

  @Test
  public void findNextValidDayPosition_withValidator() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    // Search forward from day 28 (last valid day), expecting no results.
    assertThat(adapter.findNextValidDayPosition(adapter.dayToPosition(28))).isEqualTo(-1);
    // Search forward from day 14 (valid) to day 15 (valid).
    assertThat(adapter.findNextValidDayPosition(adapter.dayToPosition(14)))
        .isEqualTo(adapter.dayToPosition(15));
  }

  @Test
  public void findPreviousValidDayPosition_withValidator() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    // Search backward from day 15 (first valid day), expecting no results.
    assertThat(adapter.findPreviousValidDayPosition(adapter.dayToPosition(15))).isEqualTo(-1);
    // Search backward from day 16 (valid) to day 15 (valid).
    assertThat(adapter.findPreviousValidDayPosition(adapter.dayToPosition(16)))
        .isEqualTo(adapter.dayToPosition(15));
  }

  @Test
  public void findFirstValidDayPosition_withValidator() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    assertThat(adapter.findFirstValidDayPosition()).isEqualTo(adapter.dayToPosition(15));
  }

  @Test
  public void findLastValidDayPosition_withValidator() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    long dateValidTo = month.getDay(18);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(
                CompositeDateValidator.allOf(
                    Arrays.asList(
                        DateValidatorPointForward.from(dateValidFrom),
                        DateValidatorPointBackward.before(dateValidTo))))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    assertThat(adapter.findLastValidDayPosition()).isEqualTo(adapter.dayToPosition(18));
  }

  @Test
  public void findNearestValidDayPositionInRow_givenValid_returnsSelf() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    assertThat(adapter.findNearestValidDayPositionInRow(adapter.dayToPosition(15)))
        .isEqualTo(adapter.dayToPosition(15));
  }

  @Test
  public void findNearestValidDayPositionInRow_findsRight() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(15);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    // Day 14 is invalid, day 15 is valid.
    assertThat(adapter.findNearestValidDayPositionInRow(adapter.dayToPosition(14)))
        .isEqualTo(adapter.dayToPosition(15));
  }

  @Test
  public void findNearestValidDayPositionInRow_findsLeft() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidUpTo = month.getDay(14);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointBackward.before(dateValidUpTo))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    // Day 15 is invalid, day 14 is valid.
    assertThat(adapter.findNearestValidDayPositionInRow(adapter.dayToPosition(15)))
        .isEqualTo(adapter.dayToPosition(14));
  }

  @Test
  public void findNearestValidDayPositionInRow_noValidDateInRow_returnsInvalid() {
    Locale.setDefault(Locale.US);
    Month month = Month.create(2019, Calendar.FEBRUARY);
    long dateValidFrom = month.getDay(25);
    CalendarConstraints constraints =
        new CalendarConstraints.Builder()
            .setValidator(DateValidatorPointForward.from(dateValidFrom))
            .build();
    MonthAdapter adapter = new MonthAdapter(month, new SingleDateSelector(), constraints, null);

    assertThat(adapter.findNearestValidDayPositionInRow(3)).isEqualTo(-1);
  }

  private MonthAdapter createRangeMonthAdapter(Month month, Pair<Long, Long> selection) {
    DateSelector<Pair<Long, Long>> dateSelector = new RangeDateSelector();
    dateSelector.setSelection(selection);
    return new MonthAdapter(
        month,
        dateSelector,
        new CalendarConstraints.Builder().build(),
        /* dayViewDecorator= */ null);
  }
}
