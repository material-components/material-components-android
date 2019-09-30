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

import android.annotation.TargetApi;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Utility class for common operations on timezones, calendars, dateformats, and longs representing
 * time in milliseconds.
 */
class UtcDates {

  static final String UTC = "UTC";

  private UtcDates() {}

  static TimeZone getTimeZone() {
    return TimeZone.getTimeZone(UTC);
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.util.TimeZone getAndroidTimeZone() {
    return android.icu.util.TimeZone.getTimeZone(UTC);
  }

  static Calendar getTodayCalendar() {
    return getDayCopy(Calendar.getInstance());
  }

  static Calendar getCalendar() {
    Calendar utc = Calendar.getInstance(getTimeZone());
    utc.clear();
    return utc;
  }

  static Calendar getDayCopy(Calendar rawCalendar) {
    Calendar safe = getCalendar();
    safe.set(
        rawCalendar.get(Calendar.YEAR),
        rawCalendar.get(Calendar.MONTH),
        rawCalendar.get(Calendar.DAY_OF_MONTH));
    return safe;
  }

  /**
   * Strips all information from the time in milliseconds at granularities more specific than day of
   * the month.
   *
   * @param rawDate A long representing the time as UTC milliseconds from the epoch
   * @return A canonical long representing the time as UTC milliseconds for the represented day.
   */
  static long canonicalYearMonthDay(long rawDate) {
    Calendar rawCalendar = getCalendar();
    rawCalendar.setTimeInMillis(rawDate);
    Calendar sanitizedStartItem = getDayCopy(rawCalendar);
    return sanitizedStartItem.getTimeInMillis();
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.text.DateFormat getAndroidFormat(String pattern, Locale locale) {
    android.icu.text.DateFormat format =
        android.icu.text.DateFormat.getInstanceForSkeleton(pattern, locale);
    format.setTimeZone(getAndroidTimeZone());
    return format;
  }

  private static DateFormat getFormat(int style, Locale locale) {
    DateFormat format = DateFormat.getDateInstance(style, locale);
    format.setTimeZone(getTimeZone());
    return format;
  }

  static SimpleDateFormat getSimpleFormat(String pattern) {
    return getSimpleFormat(pattern, Locale.getDefault());
  }

  private static SimpleDateFormat getSimpleFormat(String pattern, Locale locale) {
    SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
    format.setTimeZone(getTimeZone());
    return format;
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getYearAbbrMonthDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_ABBR_MONTH_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getAbbrMonthDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.ABBR_MONTH_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getAbbrMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.ABBR_MONTH_WEEKDAY_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getYearAbbrMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_ABBR_MONTH_WEEKDAY_DAY, locale);
  }

  static DateFormat getMediumFormat() {
    return getMediumFormat(Locale.getDefault());
  }

  static DateFormat getMediumFormat(Locale locale) {
    return getFormat(DateFormat.MEDIUM, locale);
  }

  static DateFormat getMediumNoYear() {
    return getMediumNoYear(Locale.getDefault());
  }

  static DateFormat getMediumNoYear(Locale locale) {
    SimpleDateFormat format = (SimpleDateFormat) getMediumFormat(locale);
    format.applyPattern(removeYearFromDateFormatPattern(format.toPattern()));
    return format;
  }

  static DateFormat getFullFormat() {
    return getFullFormat(Locale.getDefault());
  }

  static DateFormat getFullFormat(Locale locale) {
    return getFormat(DateFormat.FULL, locale);
  }

  static SimpleDateFormat getYearMonthFormat() {
    return getYearMonthFormat(Locale.getDefault());
  }

  private static SimpleDateFormat getYearMonthFormat(Locale locale) {
    return getSimpleFormat("MMMM, yyyy", locale);
  }

  @NonNull
  private static String removeYearFromDateFormatPattern(@NonNull String pattern) {
    String yearCharacters = "yY";

    int yearPosition = findCharactersInDateFormatPattern(pattern, yearCharacters, 1, 0);

    if (yearPosition >= pattern.length()) {
      // No year character was found in this pattern, return as-is
      return pattern;
    }

    String monthDayCharacters = "EMd";
    int yearEndPosition =
        findCharactersInDateFormatPattern(pattern, monthDayCharacters, 1, yearPosition);

    if (yearEndPosition < pattern.length()) {
      monthDayCharacters += ",";
    }

    int yearStartPosition =
        findCharactersInDateFormatPattern(pattern, monthDayCharacters, -1, yearPosition);
    yearStartPosition++;

    String yearPattern = pattern.substring(yearStartPosition, yearEndPosition);
    return pattern.replace(yearPattern, " ").trim();
  }

  private static int findCharactersInDateFormatPattern(
      @NonNull String pattern,
      @NonNull String characterSequence,
      int increment,
      int initialPosition) {
    int position = initialPosition;

    // Increment while we haven't found the characters we're looking for in the date pattern
    while ((position >= 0 && position < pattern.length())
        && characterSequence.indexOf(pattern.charAt(position)) == -1) {

      // If an open string is found, increment until we close the string
      if (pattern.charAt(position) == '\'') {
        position += increment;
        while ((position >= 0 && position < pattern.length()) && pattern.charAt(position) != '\'') {
          position += increment;
        }
      }

      position += increment;
    }

    return position;
  }
}
