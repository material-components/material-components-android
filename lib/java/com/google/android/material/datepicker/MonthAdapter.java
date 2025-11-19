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
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import java.util.Calendar;
import java.util.Collection;
import java.util.Locale;

/**
 * Represents the days of a month with {@link TextView} instances for each day.
 *
 * <p>The number of rows is always equal to the maximum number of weeks that can exist across all
 * months (e.g., 6 for the GregorianCalendar).
 */
class MonthAdapter extends BaseAdapter {

  /**
   * The maximum number of weeks possible in any month. 6 for {@link java.util.GregorianCalendar}.
   */
  static final int MAXIMUM_WEEKS = UtcDates.getUtcCalendar().getMaximum(Calendar.WEEK_OF_MONTH);

  /** The maximum number of cells needed in the month grid view. */
  private static final int MAXIMUM_GRID_CELLS =
      UtcDates.getUtcCalendar().getMaximum(Calendar.DAY_OF_MONTH)
          + UtcDates.getUtcCalendar().getMaximum(Calendar.DAY_OF_WEEK)
          - 1;

  private static final int NO_DAY_NUMBER = -1;

  final Month month;
  /**
   * The {@link DateSelector} dictating the draw behavior of {@link #getView(int, View, ViewGroup)}.
   */
  final DateSelector<?> dateSelector;

  private Collection<Long> previouslySelectedDates;

  CalendarStyle calendarStyle;
  final CalendarConstraints calendarConstraints;

  @Nullable final DayViewDecorator dayViewDecorator;

  MonthAdapter(
      Month month,
      DateSelector<?> dateSelector,
      CalendarConstraints calendarConstraints,
      @Nullable DayViewDecorator dayViewDecorator) {
    this.month = month;
    this.dateSelector = dateSelector;
    this.calendarConstraints = calendarConstraints;
    this.dayViewDecorator = dayViewDecorator;
    this.previouslySelectedDates = dateSelector.getSelectedDays();
  }

  @Override
  public boolean hasStableIds() {
    return true;
  }

  /**
   * Returns a {@link Long} object for the given grid position
   *
   * @param position Index for the item. 0 matches the {@link Calendar#getFirstDayOfWeek()} for the
   *     first week of the month represented by {@link Month} or {@link
   *     CalendarConstraints#getFirstDayOfWeek()} if set.
   * @return A {@link Long} representing the day at the position or null if the position does not
   *     represent a valid day in the month.
   */
  @Nullable
  @Override
  public Long getItem(int position) {
    if (position < firstPositionInMonth() || position > lastPositionInMonth()) {
      return null;
    }
    return month.getDay(positionToDay(position));
  }

  @Override
  public long getItemId(int position) {
    return position / month.daysInWeek;
  }

  /**
   * Returns the maximum number of item views needed to display a calender month.
   *
   * <p>{@see MonthAdapter#MAXIMUM_GRID_CELLS}.
   *
   * @return The maximum valid position index
   */
  @Override
  public int getCount() {
    return MAXIMUM_GRID_CELLS;
  }

  @NonNull
  @Override
  public TextView getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
    initializeStyles(parent.getContext());
    TextView dayTextView = (TextView) convertView;
    if (convertView == null) {
      LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
      dayTextView = (TextView) layoutInflater.inflate(R.layout.mtrl_calendar_day, parent, false);
    }
    int offsetPosition = position - firstPositionInMonth();
    int dayNumber = NO_DAY_NUMBER;
    if (offsetPosition < 0 || offsetPosition >= month.daysInMonth) {
      dayTextView.setVisibility(View.GONE);
      dayTextView.setEnabled(false);
    } else {
      dayNumber = offsetPosition + 1;
      // The tag and text uniquely identify the view within the MaterialCalendar for testing
      dayTextView.setTag(month);
      Locale locale = dayTextView.getResources().getConfiguration().locale;
      dayTextView.setText(String.format(locale, "%d", dayNumber));
      dayTextView.setVisibility(View.VISIBLE);
      dayTextView.setEnabled(true);
    }

    Long date = getItem(position);
    if (date == null) {
      return dayTextView;
    }
    updateSelectedState(dayTextView, date, dayNumber);
    return dayTextView;
  }

  public void updateSelectedStates(MaterialCalendarGridView monthGrid) {
    // Update previously selected dates.
    for (Long date : previouslySelectedDates) {
      updateSelectedStateForDate(monthGrid, date);
    }

    // Update currently selected dates.
    if (dateSelector != null) {
      for (Long date : dateSelector.getSelectedDays()) {
        updateSelectedStateForDate(monthGrid, date);
      }
      // Update the list of previously selected dates.
      previouslySelectedDates = dateSelector.getSelectedDays();
    }
  }

  private void updateSelectedStateForDate(MaterialCalendarGridView monthGrid, long date) {
    if (Month.create(date).equals(month)) {
      // Validate that the day is in the right month.
      int day = month.getDayOfMonth(date);
      updateSelectedState(
          (TextView)
              monthGrid.getChildAt(
                  monthGrid.getAdapter().dayToPosition(day) - monthGrid.getFirstVisiblePosition()),
          date,
          day);
    }
  }

  private void updateSelectedState(@Nullable TextView dayTextView, long date, int dayNumber) {
    if (dayTextView == null) {
      return;
    }

    Context context = dayTextView.getContext();
    String contentDescription = getDayContentDescription(context, date);
    dayTextView.setContentDescription(contentDescription);

    final CalendarItemStyle style;
    boolean valid = calendarConstraints.getDateValidator().isValid(date);
    boolean selected = false;
    if (valid) {
      dayTextView.setEnabled(true);
      selected = isSelected(date);
      dayTextView.setSelected(selected);
      if (selected) {
        style = calendarStyle.selectedDay;
      } else if (isToday(date)) {
        style = calendarStyle.todayDay;
      } else {
        style = calendarStyle.day;
      }
    } else {
      dayTextView.setEnabled(false);
      style = calendarStyle.invalidDay;
    }

    if (dayViewDecorator != null && dayNumber != NO_DAY_NUMBER) {
      int year = month.year;
      int month = this.month.month;

      ColorStateList backgroundColorOverride =
          dayViewDecorator.getBackgroundColor(context, year, month, dayNumber, valid, selected);
      ColorStateList textColorOverride =
          dayViewDecorator.getTextColor(context, year, month, dayNumber, valid, selected);
      style.styleItem(dayTextView, backgroundColorOverride, textColorOverride);

      Drawable drawableLeft =
          dayViewDecorator.getCompoundDrawableLeft(
              context, year, month, dayNumber, valid, selected);
      Drawable drawableTop =
          dayViewDecorator.getCompoundDrawableTop(context, year, month, dayNumber, valid, selected);
      Drawable drawableRight =
          dayViewDecorator.getCompoundDrawableRight(
              context, year, month, dayNumber, valid, selected);
      Drawable drawableBottom =
          dayViewDecorator.getCompoundDrawableBottom(
              context, year, month, dayNumber, valid, selected);
      dayTextView.setCompoundDrawables(drawableLeft, drawableTop, drawableRight, drawableBottom);

      CharSequence decoratorContentDescription =
          dayViewDecorator.getContentDescription(
              context, year, month, dayNumber, valid, selected, contentDescription);
      dayTextView.setContentDescription(decoratorContentDescription);
    } else {
      style.styleItem(dayTextView);
    }
  }

  private String getDayContentDescription(Context context, long date) {
    return DateStrings.getDayContentDescription(
        context, date, isToday(date), isStartOfRange(date), isEndOfRange(date));
  }

  private boolean isToday(long date) {
    return UtcDates.getTodayCalendar().getTimeInMillis() == date;
  }

  @VisibleForTesting
  boolean isStartOfRange(long date) {
    for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
      if (range.first != null && range.first == date) {
        return true;
      }
    }
    return false;
  }

  @VisibleForTesting
  boolean isEndOfRange(long date) {
    for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
      if (range.second != null && range.second == date) {
        return true;
      }
    }
    return false;
  }

  private boolean isSelected(long date) {
    for (long selectedDay : dateSelector.getSelectedDays()) {
      if (UtcDates.canonicalYearMonthDay(date) == UtcDates.canonicalYearMonthDay(selectedDay)) {
        return true;
      }
    }
    return false;
  }

  private void initializeStyles(Context context) {
    if (calendarStyle == null) {
      calendarStyle = new CalendarStyle(context);
    }
  }

  /**
   * Returns the index of the first position which is part of the month.
   *
   * <p>For example, this returns the position index representing February 1st. Since position 0
   * represents a day which must be the first day of the week, the first position in the month may
   * be greater than 0.
   */
  int firstPositionInMonth() {
    return month.daysFromStartOfWeekToFirstOfMonth(calendarConstraints.getFirstDayOfWeek());
  }

  /**
   * Returns the index of the last position which is part of the month.
   *
   * <p>For example, this returns the position index representing November 30th. Since position 0
   * represents a day which must be the first day of the week, the last position in the month may
   * not match the number of days in the month.
   */
  int lastPositionInMonth() {
    return firstPositionInMonth() + month.daysInMonth - 1;
  }

  /**
   * Returns the day representing the provided adapter index
   *
   * @param position The adapter index
   * @return The day corresponding to the adapter index. May be non-positive for position inputs
   *     less than {@link MonthAdapter#firstPositionInMonth()}.
   */
  int positionToDay(int position) {
    return position - firstPositionInMonth() + 1;
  }

  /** Returns the adapter index representing the provided day. */
  int dayToPosition(int day) {
    int offsetFromFirst = day - 1;
    return firstPositionInMonth() + offsetFromFirst;
  }

  /** True when a provided adapter position is within the calendar month */
  boolean withinMonth(int position) {
    return position >= firstPositionInMonth() && position <= lastPositionInMonth();
  }

  /**
   * True when the provided adapter position is the smallest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isFirstInRow(int position) {
    return position % month.daysInWeek == 0;
  }

  /**
   * True when the provided adapter position is the largest position for a value of {@link
   * MonthAdapter#getItemId(int)}.
   */
  boolean isLastInRow(int position) {
    return (position + 1) % month.daysInWeek == 0;
  }

  /**
   * Returns true if the day at the given adapter position is within the current month, and valid
   * according to the {@link CalendarConstraints}.
   */
  boolean isDayPositionValid(int position) {
    Long day = getItem(position);
    return day != null && calendarConstraints.getDateValidator().isValid(day);
  }

  /**
   * Finds the closest valid day to the given position searching forward.
   *
   * @param position The starting position.
   * @return The position of the next valid day, or -1 if none is found before reaching the
   *     end of the month.
   */
  int findNextValidDayPosition(int position) {
    for (int i = position + 1; i <= lastPositionInMonth(); i++) {
      if (isDayPositionValid(i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Finds the closest valid day to the given position searching backward.
   *
   * @param position The starting position.
   * @return The position of the previous valid day, or -1 if none is found before reaching the
   *     start of the month.
   */
  int findPreviousValidDayPosition(int position) {
    for (int i = position - 1; i >= firstPositionInMonth(); i--) {
      if (isDayPositionValid(i)) {
        return i;
      }
    }
    return -1;
  }

  /**
   * Returns the adapter position of the first day in the month that is valid, or -1 if no days
   * are valid.
   */
  int findFirstValidDayPosition() {
    return findNextValidDayPosition(firstPositionInMonth() - 1);
  }

  /**
   * Returns the adapter position of the last day in the month that is valid, or -1 if no days are
   * valid.
   */
  int findLastValidDayPosition() {
    return findPreviousValidDayPosition(lastPositionInMonth() + 1);
  }

  /**
   * Finds the nearest valid day to the given position in the same row.
   *
   * @param position The starting position.
   * @return The position of the nearest valid day in the same row, or -1 if no valid day is found
   *     in the row.
   */
  int findNearestValidDayPositionInRow(int position) {
    if (isDayPositionValid(position)) {
      return position;
    }

    long rowId = getItemId(position);
    for (int i = 1; i < month.daysInWeek; i++) {
      int rightPosition = position + i;
      if (rightPosition < getCount()
          && getItemId(rightPosition) == rowId
          && isDayPositionValid(rightPosition)) {
        return rightPosition;
      }
      int leftPosition = position - i;
      if (leftPosition >= 0
          && getItemId(leftPosition) == rowId
          && isDayPositionValid(leftPosition)) {
        return leftPosition;
      }
    }
    return -1;
  }
}
