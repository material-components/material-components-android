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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

import android.os.Build.VERSION_CODES;
import androidx.core.util.Pair;
import androidx.test.core.app.ApplicationProvider;
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

  private static Calendar setupLocalizedCalendar(Locale locale, int year, int month, int day) {
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
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("30 nov. " + CURRENT_YEAR));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("25 juin " + (CURRENT_YEAR + 1)));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("2 mars 2013"));
  }

  @Test
  public void usYearMonthDayString() {
    Locale locale = Locale.US;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("Nov 30, " + CURRENT_YEAR));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("Jun 25, " + (CURRENT_YEAR + 1)));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("Mar 2, 2013"));
  }

  @Test
  public void ptYearMonthDayString() {
    Locale locale = new Locale("pt", "BR");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("30 de nov de " + CURRENT_YEAR));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("25 de jun de " + (CURRENT_YEAR + 1)));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("2 de mar de 2013"));
  }

  @Test
  public void iwYearMonthDayString() {
    Locale locale = new Locale("iw", "IL");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("30 בנוב׳ " + CURRENT_YEAR));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("25 ביוני " + (CURRENT_YEAR + 1)));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("2 במרץ 2013"));
  }

  @Test
  public void arYearMonthDayString() {
    Locale locale = new Locale("ar", "LY");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("30 نوفمبر، " + CURRENT_YEAR));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is("25 يونيو، " + (CURRENT_YEAR + 1)));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("2 مارس، 2013"));
  }

  @Test
  public void zhYearMonthDayString() {
    Locale locale = new Locale("zh", "CN");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is(CURRENT_YEAR + "年11月30日"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(
        DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale),
        is((CURRENT_YEAR + 1) + "年6月25日"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getYearMonthDay(startDate.getTimeInMillis(), locale), is("2013年3月2日"));
  }

  @Test
  public void frMonthDayString() {
    Locale locale = Locale.FRANCE;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("30 nov."));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("25 juin"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("2 mars"));
  }

  @Test
  public void usMonthDayString() {
    Locale locale = Locale.US;
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("Nov 30"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("Jun 25"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("Mar 2"));
  }

  @Test
  public void ptMonthDayString() {
    Locale locale = new Locale("pt", "BR");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("30 de nov"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("25 de jun"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("2 de mar"));
  }

  @Test
  public void iwMonthDayString() {
    Locale locale = new Locale("iw", "IL");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("30 בנוב׳"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("25 ביוני"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("2 במרץ"));
  }

  @Test
  public void arMonthDayString() {
    Locale locale = new Locale("ar", "LY");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("30 نوفمبر"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("25 يونيو"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("2 مارس"));
  }

  @Test
  public void zhMonthDayString() {
    Locale locale = new Locale("zh", "CN");
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR, 10, 30);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("11月30日"));
    startDate = setupLocalizedCalendar(locale, CURRENT_YEAR + 1, 5, 25);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("6月25日"));
    startDate = setupLocalizedCalendar(locale, 2013, 2, 2);
    assertThat(DateStrings.getMonthDay(startDate.getTimeInMillis(), locale), is("3月2日"));
  }

  @Test
  public void getDateStringCurrentYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 10, 30);
    String dateString = DateStrings.getDateString(startDate.getTimeInMillis(), null);
    assertThat(dateString, is("Nov 30"));
  }

  @Test
  public void getDateStringCurrentYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 10, 30);
    String dateString =
        DateStrings.getDateString(
            startDate.getTimeInMillis(), new SimpleDateFormat("MMMM dd, yyyy", Locale.US));
    assertThat(dateString, is("November 30, " + CURRENT_YEAR));
  }

  @Test
  public void getDateStringNextYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 10, 30);
    String dateString = DateStrings.getDateString(startDate.getTimeInMillis(), null);
    assertThat(dateString, is("Nov 30, " + (CURRENT_YEAR + 1)));
  }

  @Test
  public void getDateStringNextYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 10, 3);
    String dateString =
        DateStrings.getDateString(
            startDate.getTimeInMillis(), new SimpleDateFormat("MMMM dd", Locale.US));
    assertThat(dateString, is("November 03"));
  }

  @Test
  public void getDateRangeStringCurrentYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate.getTimeInMillis(), endDate.getTimeInMillis(), null);
    assertThat(dateRangeString.first, is("May 30"));
    assertThat(dateRangeString.second, is("Dec 5"));
  }

  @Test
  public void getDateRangeStringNextYear() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate.getTimeInMillis(), endDate.getTimeInMillis(), null);
    assertThat(dateRangeString.first, is("May 30"));
    assertThat(dateRangeString.second, is("Dec 5, " + (CURRENT_YEAR + 1)));
  }

  @Test
  public void getDateRangeStringMultipleYears() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate.getTimeInMillis(), endDate.getTimeInMillis(), null);
    assertThat(dateRangeString.first, is("May 30, " + CURRENT_YEAR));
    assertThat(dateRangeString.second, is("Dec 5, " + (CURRENT_YEAR + 1)));
  }

  @Test
  public void getDateRangeStringCurrentYearWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate.getTimeInMillis(),
            endDate.getTimeInMillis(),
            new SimpleDateFormat("MMM dd, yyyy", Locale.US));
    assertThat(dateRangeString.first, is("May 30, " + CURRENT_YEAR));
    assertThat(dateRangeString.second, is("Dec 05, " + CURRENT_YEAR));
  }

  @Test
  public void getDateRangeStringMultipleYearsWithUserDefinedDateFormat() {
    startDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR, 4, 30);
    endDate = setupLocalizedCalendar(Locale.US, CURRENT_YEAR + 1, 11, 5);
    Pair<String, String> dateRangeString =
        DateStrings.getDateRangeString(
            startDate.getTimeInMillis(),
            endDate.getTimeInMillis(),
            new SimpleDateFormat("MMM dd", Locale.US));
    assertThat(dateRangeString.first, is("May 30"));
    assertThat(dateRangeString.second, is("Dec 05"));
  }

  @Test
  public void getDayContentDescription_notToday() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ false,
            /* isStartOfRange= */ false,
            /* isEndOfRange= */ false);

    assertThat(contentDescription, is("Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_notToday_startOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ false,
            /* isStartOfRange= */ true,
            /* isEndOfRange= */ false);

    assertThat(contentDescription, is("Start date Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_notToday_endOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ false,
            /* isStartOfRange= */ false,
            /* isEndOfRange= */ true);

    assertThat(contentDescription, is("End date Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_notToday_startAndEndOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ false,
            /* isStartOfRange= */ true,
            /* isEndOfRange= */ true);

    assertThat(contentDescription, is("Start date Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_today() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ true,
            /* isStartOfRange= */ false,
            /* isEndOfRange= */ false);

    assertThat(contentDescription, is("Today Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_today_startOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ true,
            /* isStartOfRange= */ true,
            /* isEndOfRange= */ false);

    assertThat(contentDescription, is("Start date Today Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_today_endOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ true,
            /* isStartOfRange= */ false,
            /* isEndOfRange= */ true);

    assertThat(contentDescription, is("End date Today Monday, November 30, 2020"));
  }

  @Test
  public void getDayContentDescription_today_startAndEndOfRange() {
    startDate = setupLocalizedCalendar(Locale.US, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ true,
            /* isStartOfRange= */ true,
            /* isEndOfRange= */ true);

    assertThat(contentDescription, is("Start date Today Monday, November 30, 2020"));
  }

  @Test
  public void getLocalizedDayContentDescription_german() {
    startDate = setupLocalizedCalendar(Locale.GERMAN, 2020, 10, 30);
    String contentDescription =
        DateStrings.getDayContentDescription(
            ApplicationProvider.getApplicationContext(),
            startDate.getTimeInMillis(),
            /* isToday= */ false,
            /* isStartOfRange= */ false,
            /* isEndOfRange= */ false);

    assertThat(contentDescription, is("Montag, 30. November 2020"));
  }

  @Test
  public void getYearContentDescription_notCurrent() {
    String contentDescription =
        DateStrings.getYearContentDescription(ApplicationProvider.getApplicationContext(), 2020);

    assertThat(contentDescription, is("Navigate to year 2020"));
  }

  @Test
  public void getYearContentDescription_current() {
    String contentDescription =
        DateStrings.getYearContentDescription(
            ApplicationProvider.getApplicationContext(), CURRENT_YEAR);

    assertThat(contentDescription, is("Navigate to current year " + CURRENT_YEAR));
  }
}
