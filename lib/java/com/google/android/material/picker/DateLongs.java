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

import java.util.Calendar;

/** Utility class for common operations on instances of long that represent time in milliseconds. */
class DateLongs {

  private DateLongs() {}

  /**
   * Strips all information from the time in milliseconds at granularities more specific than day of
   * the month.
   *
   * @param rawDate A long representing the time as UTC milliseconds from the epoch
   * @return A canonical long representing the time as UTC milliseconds for the represented day.
   */
  static long canonicalYearMonthDay(long rawDate) {
    Calendar rawCalendar = Calendar.getInstance();
    rawCalendar.setTimeInMillis(rawDate);
    Calendar sanitizedStartItem = Calendar.getInstance();
    sanitizedStartItem.clear();

    sanitizedStartItem.set(
        rawCalendar.get(Calendar.YEAR),
        rawCalendar.get(Calendar.MONTH),
        rawCalendar.get(Calendar.DAY_OF_MONTH));
    return sanitizedStartItem.getTimeInMillis();
  }
}
