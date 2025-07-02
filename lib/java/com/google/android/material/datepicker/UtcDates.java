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

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.icu.text.DisplayContext;
import android.os.Build.VERSION_CODES;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.TtsSpan;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Utility class for common operations on timezones, calendars, dateformats, and longs representing
 * time in milliseconds.
 */
class UtcDates {

  static final String UTC = "UTC";

  static AtomicReference<TimeSource> timeSourceRef = new AtomicReference<>();

  static void setTimeSource(@Nullable TimeSource timeSource) {
    timeSourceRef.set(timeSource);
  }

  static TimeSource getTimeSource() {
    TimeSource timeSource = timeSourceRef.get();
    return timeSource == null ? TimeSource.system() : timeSource;
  }

  private UtcDates() {}

  private static TimeZone getTimeZone() {
    return TimeZone.getTimeZone(UTC);
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.util.TimeZone getUtcAndroidTimeZone() {
    return android.icu.util.TimeZone.getTimeZone(UTC);
  }

  /**
   * Returns a Calendar object in UTC time zone representing the first moment of current date.
   */
  static Calendar getTodayCalendar() {
    Calendar today = getTimeSource().now();
    today.set(Calendar.HOUR_OF_DAY, 0);
    today.set(Calendar.MINUTE, 0);
    today.set(Calendar.SECOND, 0);
    today.set(Calendar.MILLISECOND, 0);
    today.setTimeZone(getTimeZone());
    return today;
  }

  /**
   * Returns an empty Calendar in UTC time zone.
   *
   * @return An empty Calendar in UTC time zone.
   * @see {@link #getUtcCalendarOf(Calendar)}
   * @see Calendar#clear()
   */
  static Calendar getUtcCalendar() {
    return getUtcCalendarOf(null);
  }

  /**
   * Returns a Calendar object in UTC time zone representing the moment in input Calendar object. An
   * empty Calendar object in UTC will be return if input is null.
   *
   * @param rawCalendar the Calendar object representing the moment to process.
   * @return A Calendar object in UTC time zone.
   * @see @see Calendar#clear()
   */
  static Calendar getUtcCalendarOf(@Nullable Calendar rawCalendar) {
    Calendar utc = Calendar.getInstance(getTimeZone());
    if (rawCalendar == null) {
      utc.clear();
    } else {
      utc.setTimeInMillis(rawCalendar.getTimeInMillis());
    }
    return utc;
  }

  /**
   * Returns a Calendar object in UTC time zone representing the start of day in UTC represented in
   * the input Calendar object, i.e., the time (fields smaller than a day) is stripped based on the
   * UTC time zone.
   *
   * @param rawCalendar the Calendar object representing the moment to process.
   * @return A Calendar object representing the start of day in UTC time zone.
   */
  static Calendar getDayCopy(Calendar rawCalendar) {
    Calendar rawCalendarInUtc = getUtcCalendarOf(rawCalendar);
    Calendar utcCalendar = getUtcCalendar();
    utcCalendar.set(
        rawCalendarInUtc.get(Calendar.YEAR),
        rawCalendarInUtc.get(Calendar.MONTH),
        rawCalendarInUtc.get(Calendar.DAY_OF_MONTH));
    return utcCalendar;
  }

  /**
   * Strips all information from the time in milliseconds at granularities more specific than day of
   * the month.
   *
   * @param rawDate A long representing the time as UTC milliseconds from the epoch
   * @return A canonical long representing the time as UTC milliseconds for the represented day.
   */
  static long canonicalYearMonthDay(long rawDate) {
    Calendar rawCalendar = getUtcCalendar();
    rawCalendar.setTimeInMillis(rawDate);
    Calendar sanitizedStartItem = getDayCopy(rawCalendar);
    return sanitizedStartItem.getTimeInMillis();
  }

  @TargetApi(VERSION_CODES.N)
  private static android.icu.text.DateFormat getAndroidFormat(String pattern, Locale locale) {
    android.icu.text.DateFormat format =
        android.icu.text.DateFormat.getInstanceForSkeleton(pattern, locale);
    format.setTimeZone(getUtcAndroidTimeZone());
    format.setContext(DisplayContext.CAPITALIZATION_FOR_STANDALONE);
    return format;
  }

  private static DateFormat getFormat(int style, Locale locale) {
    DateFormat format = DateFormat.getDateInstance(style, locale);
    format.setTimeZone(getTimeZone());
    return format;
  }

  static DateFormat getNormalizedFormat(@NonNull DateFormat dateFormat) {
    DateFormat clone = (DateFormat) dateFormat.clone();
    clone.setTimeZone(getTimeZone());
    return clone;
  }

  static SimpleDateFormat getDefaultTextInputFormat() {
    String defaultFormatPattern =
        ((SimpleDateFormat) DateFormat.getDateInstance(DateFormat.SHORT, Locale.getDefault()))
            .toPattern();
    defaultFormatPattern = getDatePatternAsInputFormat(defaultFormatPattern);
    SimpleDateFormat format = new SimpleDateFormat(defaultFormatPattern, Locale.getDefault());
    format.setTimeZone(getTimeZone());
    format.setLenient(false);
    return format;
  }

  static String getDefaultTextInputHint(Resources res, SimpleDateFormat format) {
    String formatHint = format.toPattern();
    String yearChar = res.getString(R.string.mtrl_picker_text_input_year_abbr);
    String monthChar = res.getString(R.string.mtrl_picker_text_input_month_abbr);
    String dayChar = res.getString(R.string.mtrl_picker_text_input_day_abbr);

    // Remove duplicate characters for Korean.
    if (Locale.getDefault().getLanguage().equals(Locale.KOREAN.getLanguage())) {
      formatHint = formatHint.replaceAll("d+", "d").replaceAll("M+", "M").replaceAll("y+", "y");
    }

    return formatHint.replace("d", dayChar).replace("M", monthChar).replace("y", yearChar);
  }

  /**
   * Returns a SpannableString with the given format hint that has a TtsSpan.TYPE_VERBATIM span
   * applied to it in order to ensure that the hint is read verbatim by screen readers.
   */
  static SpannableString getVerbatimTextInputHint(String formatHint) {
    SpannableString spannableHint = new SpannableString(formatHint);
    spannableHint.setSpan(
        new TtsSpan.Builder(TtsSpan.TYPE_VERBATIM).build(),
        0,
        spannableHint.length(),
        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    return spannableHint;
  }

  /**
   * Receives a given local date format string and returns a string that can be displayed to the
   * user and parsed by the date parser.
   *
   * <p>This function:
   *  - Removes all characters that don't match `d`, `M` and `y`, or any of the date format
   *    delimiters `.`, `/` and `-`.
   *  - Ensures that the format is for two digits day and month, and four digits year.
   *
   * <p>The output of this cleanup is always a 10 characters string in one of the following
   * variations:
   *  - yyyy/MM/dd
   *  - yyyy-MM-dd
   *  - yyyy.MM.dd
   *  - dd/MM/yyyy
   *  - dd-MM-yyyy
   *  - dd.MM.yyyy
   *  - MM/dd/yyyy
   */
  @NonNull
  static String getDatePatternAsInputFormat(@NonNull String localeFormat) {
    return localeFormat
        .replaceAll("[^dMy/\\-.]", "")
        .replaceAll("d{1,2}", "dd")
        .replaceAll("M{1,2}", "MM")
        .replaceAll("y{1,4}", "yyyy")
        .replaceAll("\\.$", "") // Removes a dot suffix that appears in some formats
        .replaceAll("My", "M/y"); // Edge case for the Kako locale
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
  static android.icu.text.DateFormat getYearMonthFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_MONTH, locale);
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
  static android.icu.text.DateFormat getMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.MONTH_WEEKDAY_DAY, locale);
  }

  @TargetApi(VERSION_CODES.N)
  static android.icu.text.DateFormat getYearMonthWeekdayDayFormat(Locale locale) {
    return getAndroidFormat(android.icu.text.DateFormat.YEAR_MONTH_WEEKDAY_DAY, locale);
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
