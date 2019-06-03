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

import android.icu.text.DateFormat;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/** Util methods for formatting date strings for use in {@link MaterialPickerDialogFragment}. */
class DateStrings {

  private DateStrings() {}

  /**
   * Get date string with year, month, and day formatted properly for the specified Locale.
   *
   * <p>Uses {@link DateFormat#getInstanceForSkeleton(String, Locale)} for API 24+, and {@link
   * java.text.DateFormat#MEDIUM} before API 24.
   *
   * @param date Date to turn into string with year, month, and day.
   * @param locale Locale for date string.
   * @return Date string with year, month, and day formatted properly for the specified Locale.
   */
  static String getYearMonthDay(Date date, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      DateFormat df = DateFormat.getInstanceForSkeleton(DateFormat.YEAR_ABBR_MONTH_DAY, locale);
      return df.format(date);
    } else {
      java.text.DateFormat df =
          java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale);
      return df.format(date);
    }
  }

  /**
   * Get date string with month and day formatted properly for the specified Locale.
   *
   * <p>Uses {@link DateFormat#getInstanceForSkeleton(String, Locale)} for API 24+, and {@link
   * java.text.DateFormat#MEDIUM} before API 24.
   *
   * @param date Date to turn into string with month and day.
   * @param locale Locale for date string.
   * @return Date string with month and day formatted properly for the specified Locale.
   */
  static String getMonthDay(Date date, Locale locale) {
    if (VERSION.SDK_INT >= VERSION_CODES.N) {
      DateFormat df = DateFormat.getInstanceForSkeleton(DateFormat.ABBR_MONTH_DAY, locale);
      return df.format(date);
    } else {
      SimpleDateFormat sdf =
          (SimpleDateFormat)
              java.text.DateFormat.getDateInstance(java.text.DateFormat.MEDIUM, locale);
      sdf.applyPattern(removeYearFromDateFormatPattern(sdf.toPattern()));
      return sdf.format(date);
    }
  }

  /**
   * Return a date string for the given date.
   *
   * <p>Does not show year if date is within current year.
   *
   * <p>If userDefinedDateFormat is set, this format overrides the rule above.
   *
   * @param calendar Date to get string for.
   * @param userDefinedDateFormat {@link SimpleDateFormat} specified by the user, if set.
   * @return Formatted date string.
   */
  static String getDateString(
      @NonNull Calendar calendar, @Nullable SimpleDateFormat userDefinedDateFormat) {
    Calendar currentCalendar = Calendar.getInstance();
    Locale defaultLocale = Locale.getDefault();

    Date date = calendar.getTime();

    if (userDefinedDateFormat != null) {
      return userDefinedDateFormat.format(date);
    } else if (currentCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
      return getMonthDay(date, defaultLocale);
    } else {
      return getYearMonthDay(date, defaultLocale);
    }
  }

  /**
   * Return a pair of strings representing the start and end dates of this date range.
   *
   * <p>Does not show year if dates are within the same year (Nov 17 - Dec 19).
   *
   * <p>Shows year for end date if range is not within the current year (Nov 17 - Nov 19, 2018).
   *
   * <p>Shows year for start and end date if range spans several years (Dec 31, 2016 - Jan 1, 2017).
   *
   * <p>If userDefinedDateFormat is set, this format overrides the rules above.
   *
   * @param start Start date.
   * @param end End date.
   * @param userDefinedDateFormat {@link SimpleDateFormat} specified by the user, if set.
   * @return Formatted date range string.
   */
  static Pair<String, String> getDateRangeString(
      @Nullable Calendar start,
      @Nullable Calendar end,
      @Nullable SimpleDateFormat userDefinedDateFormat) {
    if (start == null && end == null) {
      return Pair.create(null, null);
    } else if (start == null) {
      return Pair.create(null, getDateString(end, userDefinedDateFormat));
    } else if (end == null) {
      return Pair.create(getDateString(start, userDefinedDateFormat), null);
    }

    Calendar currentCalendar = Calendar.getInstance();
    Locale locale = Locale.getDefault();

    Date startDate = start.getTime();
    Date endDate = end.getTime();

    if (userDefinedDateFormat != null) {
      return Pair.create(
          userDefinedDateFormat.format(startDate), userDefinedDateFormat.format(endDate));
    } else if (start.get(Calendar.YEAR) == end.get(Calendar.YEAR)) {
      if (start.get(Calendar.YEAR) == currentCalendar.get(Calendar.YEAR)) {
        return Pair.create(getMonthDay(startDate, locale), getMonthDay(endDate, locale));
      } else {
        return Pair.create(getMonthDay(startDate, locale), getYearMonthDay(endDate, locale));
      }
    } else {
      return Pair.create(getYearMonthDay(startDate, locale), getYearMonthDay(endDate, locale));
    }
  }

  private static String removeYearFromDateFormatPattern(String pattern) {
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
      String pattern, String characterSequence, int increment, int initialPosition) {
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
