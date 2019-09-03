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

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Paint;
import androidx.annotation.NonNull;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.resources.MaterialResources;

/**
 * Data class for loaded {@link R.styleable#MaterialCalendar} and {@link
 * R.styleable#MaterialCalendarItem} attributes.
 */
final class CalendarStyle {

  /**
   * The {@link R.styleable#MaterialCalendarItem} style for days with no unique characteristics from
   * {@link R.styleable#MaterialCalendar_dayStyle}.
   */
  @NonNull final CalendarItemStyle day;
  /**
   * The {@link R.styleable#MaterialCalendarItem} style for selected days from {@link
   * R.styleable#MaterialCalendar_daySelectedStyle}.
   */
  @NonNull final CalendarItemStyle selectedDay;
  /**
   * The {@link R.styleable#MaterialCalendarItem} style for today from {@link
   * R.styleable#MaterialCalendar_dayTodayStyle}.
   */
  @NonNull final CalendarItemStyle todayDay;

  /**
   * The {@link R.styleable#MaterialCalendarItem} style for years with no unique characteristics
   * from {@link R.styleable#MaterialCalendar_yearStyle}.
   */
  @NonNull final CalendarItemStyle year;
  /**
   * The {@link R.styleable#MaterialCalendarItem} style for selected years from {@link
   * R.styleable#MaterialCalendar_yearSelectedStyle}.
   */
  @NonNull final CalendarItemStyle selectedYear;
  /**
   * The {@link R.styleable#MaterialCalendarItem} style for today's year from {@link
   * R.styleable#MaterialCalendar_yearTodayStyle}.
   */
  @NonNull final CalendarItemStyle todayYear;

  @NonNull final CalendarItemStyle invalidDay;

  /**
   * A {@link Paint} for styling days between selected days with {@link
   * R.styleable#MaterialCalendar_rangeFillColor}.
   */
  @NonNull final Paint rangeFill;

  CalendarStyle(@NonNull Context context) {
    int calendarStyle =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.materialCalendarStyle, MaterialCalendar.class.getCanonicalName());
    TypedArray calendarAttributes =
        context.obtainStyledAttributes(calendarStyle, R.styleable.MaterialCalendar);

    day =
        CalendarItemStyle.create(
            context, calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayStyle, 0));
    invalidDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayInvalidStyle, 0));
    selectedDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_daySelectedStyle, 0));
    todayDay =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_dayTodayStyle, 0));
    ColorStateList rangeFillColorList =
        MaterialResources.getColorStateList(
            context, calendarAttributes, R.styleable.MaterialCalendar_rangeFillColor);

    year =
        CalendarItemStyle.create(
            context, calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearStyle, 0));
    selectedYear =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearSelectedStyle, 0));
    todayYear =
        CalendarItemStyle.create(
            context,
            calendarAttributes.getResourceId(R.styleable.MaterialCalendar_yearTodayStyle, 0));

    rangeFill = new Paint();
    rangeFill.setColor(rangeFillColorList.getDefaultColor());

    calendarAttributes.recycle();
  }
}
