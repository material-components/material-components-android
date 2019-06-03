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

import android.os.Build.VERSION_CODES;
import androidx.core.util.Pair;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.internal.DoNotInstrument;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = VERSION_CODES.N)
@DoNotInstrument
public class DateStringsTest {

  private static final int CURRENT_YEAR = Calendar.getInstance().get(Calendar.YEAR);

  private Calendar startDate;
  private Calendar endDate;

  private Calendar setupLocalizedCalendar(Locale locale, int year, int month, int day) {
    Locale.setDefault(locale);
    Calendar calendar = Calendar.getInstance();
    calendar.clear();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    return calendar;
  }

  @Test
  public void frYearMonthDayString() {
    Locale locale = Locale.FRANCE;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        "30 nov. " + CURRENT_YEAR, DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        "25 juin " + (CURRENT_YEAR + 1), DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 mars 2013", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void usYearMonthDayString() {
    Locale locale = Locale.US;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        "Nov 30, " + CURRENT_YEAR, DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        "Jun 25, " + (CURRENT_YEAR + 1), DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("Mar 2, 2013", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void ptYearMonthDayString() {
    Locale locale = new Locale("pt", "BR");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        "30 de nov de " + CURRENT_YEAR, DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        "25 de jun de " + (CURRENT_YEAR + 1),
        DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 de mar de 2013", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void iwYearMonthDayString() {
    Locale locale = new Locale("iw", "IL");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        "30 בנוב׳ " + CURRENT_YEAR, DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        "25 ביוני " + (CURRENT_YEAR + 1), DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 במרץ 2013", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void arYearMonthDayString() {
    Locale locale = new Locale("ar", "LY");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        "30 نوفمبر، " + CURRENT_YEAR, DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        "25 يونيو، " + (CURRENT_YEAR + 1),
        DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 مارس، 2013", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void zhYearMonthDayString() {
    Locale locale = new Locale("zh", "CN");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals(
        CURRENT_YEAR + "年11月30日", DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals(
        (CURRENT_YEAR + 1) + "年6月25日", DateStrings.getYearMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2013年3月2日", DateStrings.getYearMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void frMonthDayString() {
    Locale locale = Locale.FRANCE;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("30 nov.", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("25 juin", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 mars", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void usMonthDayString() {
    Locale locale = Locale.US;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("Nov 30", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("Jun 25", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("Mar 2", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void ptMonthDayString() {
    Locale locale = new Locale("pt", "BR");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("30 de nov", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("25 de jun", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 de mar", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void iwMonthDayString() {
    Locale locale = new Locale("iw", "IL");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("30 בנוב׳", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("25 ביוני", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 במרץ", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void arMonthDayString() {
    Locale locale = new Locale("ar", "LY");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("30 نوفمبر", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("25 يونيو", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("2 مارس", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void zhMonthDayString() {
    Locale locale = new Locale("zh", "CN");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertEquals("11月30日", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertEquals("6月25日", DateStrings.getMonthDay(startDate.getTime(), locale));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertEquals("3月2日", DateStrings.getMonthDay(startDate.getTime(), locale));
  }

  @Test
  public void getDateStringCurrentYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 10, 30);
    String dateString = DateStrings.getDateString(startDate, null);
    assertEquals("Nov 30", dateString);
  }

  @Test
  public void getDateStringCurrentYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 10, 30);
    String dateString =
        DateStrings.getDateString(startDate, new SimpleDateFormat("MMMM dd, yyyy", Locale.US));
    assertEquals("November 30, " + CURRENT_YEAR, dateString);
  }

  @Test
  public void getDateStringNextYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 10, 30);
    String dateString = DateStrings.getDateString(startDate, null);
    assertEquals("Nov 30, " + (CURRENT_YEAR + 1), dateString);
  }

  @Test
  public void getDateStringNextYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 10, 3);
    String dateString =
        DateStrings.getDateString(startDate, new SimpleDateFormat("MMMM dd", Locale.US));
    assertEquals("November 03", dateString);
  }

  @Test
  public void getDateRangeStringCurrentYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 11, 5);
    Pair<String, String> dateRangeString = DateStrings.getDateRangeString(startDate, endDate, null);
    assertEquals("May 30", dateRangeString.first);
    assertEquals("Dec 5", dateRangeString.second);
  }

  @Test
  public void getDateRangeStringNextYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString = DateStrings.getDateRangeString(startDate, endDate, null);
    assertEquals("May 30", dateRangeString.first);
    assertEquals("Dec 5, " + (CURRENT_YEAR + 1), dateRangeString.second);
  }

  @Test
  public void getDateRangeStringMultipleYears() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString = DateStrings.getDateRangeString(startDate, endDate, null);
    assertEquals("May 30, " + CURRENT_YEAR, dateRangeString.first);
    assertEquals("Dec 5, " + (CURRENT_YEAR + 1), dateRangeString.second);
  }

  @Test
  public void getDateRangeStringCurrentYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate, endDate, new SimpleDateFormat("MMM dd, yyyy", Locale.US));
    assertEquals("May 30, " + CURRENT_YEAR, dateRangeString.first);
    assertEquals("Dec 05, " + CURRENT_YEAR, dateRangeString.second);
  }

  @Test
  public void getDateRangeStringMultipleYearsWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate, endDate, new SimpleDateFormat("MMM dd", Locale.US));
    assertEquals("May 30", dateRangeString.first);
    assertEquals("Dec 05", dateRangeString.second);
  }
}
