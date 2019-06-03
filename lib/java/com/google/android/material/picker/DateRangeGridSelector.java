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

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.util.Pair;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.TextView;
import java.util.Calendar;

/**
 * A {@link GridSelector} that uses a {@link Pair} of {@link Calendar} objects to represent a
 * selected range.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateRangeGridSelector implements GridSelector<Pair<Calendar, Calendar>> {

  private Calendar selectedStartItem = null;
  private Calendar selectedEndItem = null;
  private CalendarStyle calendarStyle;

  // The context is not available on construction and parceling, so we lazily initialize styles.
  private void initializeStyles(Context context) {
    if (calendarStyle != null) {
      return;
    }
    calendarStyle = new CalendarStyle(context);
  }

  @Override
  public void select(Calendar selection) {
    if (selectedStartItem == null) {
      selectedStartItem = selection;
    } else if (selectedEndItem == null
        && (selection.after(selectedStartItem) || selection.equals(selectedStartItem))) {
      selectedEndItem = selection;
    } else {
      selectedEndItem = null;
      selectedStartItem = selection;
    }
  }

  @Override
  public void drawItem(TextView view, Calendar content) {
    initializeStyles(view.getContext());
    CalendarItemStyle style;
    if (content.equals(selectedStartItem) || content.equals(selectedEndItem)) {
      style = calendarStyle.selectedDay;
    } else if (DateUtils.isToday(content.getTimeInMillis())) {
      style = calendarStyle.today;
    } else {
      style = calendarStyle.day;
    }
    style.styleItem(view);
  }

  @Override
  public void onCalendarMonthDraw(Canvas canvas, MaterialCalendarGridView gridView) {
    initializeStyles(gridView.getContext());
    MonthAdapter monthAdapter = gridView.getAdapter();
    Calendar firstOfMonth = monthAdapter.getItem(monthAdapter.firstPositionInMonth());
    Calendar lastOfMonth = monthAdapter.getItem(monthAdapter.lastPositionInMonth());
    if (skipMonth(firstOfMonth, lastOfMonth, selectedStartItem, selectedEndItem)) {
      return;
    }

    int firstHighlightPosition;
    int rangeHighlightStart;
    if (selectedStartItem.before(firstOfMonth)) {
      firstHighlightPosition = monthAdapter.firstPositionInMonth();
      rangeHighlightStart =
          monthAdapter.isFirstInRow(firstHighlightPosition)
              ? 0
              : gridView.getChildAt(firstHighlightPosition - 1).getRight();
    } else {
      firstHighlightPosition =
          monthAdapter.dayToPosition(selectedStartItem.get(Calendar.DAY_OF_MONTH));
      rangeHighlightStart = horizontalMidPoint(gridView.getChildAt(firstHighlightPosition));
    }

    int lastHighlightPosition;
    int rangeHighlightEnd;
    if (selectedEndItem.after(lastOfMonth)) {
      lastHighlightPosition = monthAdapter.lastPositionInMonth();
      rangeHighlightEnd =
          monthAdapter.isLastInRow(lastHighlightPosition)
              ? gridView.getWidth()
              : gridView.getChildAt(lastHighlightPosition + 1).getLeft();
    } else {
      lastHighlightPosition =
          monthAdapter.dayToPosition(selectedEndItem.get(Calendar.DAY_OF_MONTH));
      rangeHighlightEnd = horizontalMidPoint(gridView.getChildAt(lastHighlightPosition));
    }

    int firstRow = (int) monthAdapter.getItemId(firstHighlightPosition);
    int lastRow = (int) monthAdapter.getItemId(lastHighlightPosition);
    for (int row = firstRow; row <= lastRow; row++) {
      int firstPositionInRow = row * gridView.getNumColumns();
      int lastPositionInRow = firstPositionInRow + gridView.getNumColumns() - 1;
      View firstView = gridView.getChildAt(firstPositionInRow);
      int top = firstView.getTop();
      int bottom = firstView.getBottom();
      int left = firstPositionInRow > firstHighlightPosition ? 0 : rangeHighlightStart;
      int right =
          lastHighlightPosition > lastPositionInRow ? gridView.getWidth() : rangeHighlightEnd;
      canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
    }
  }

  @Override
  @Nullable
  public Pair<Calendar, Calendar> getSelection() {
    if (selectedStartItem == null) {
      return null;
    }
    return new Pair<>(selectedStartItem, selectedEndItem);
  }

  /** Returns a {@link java.util.Calendar} representing the start of the range. */
  @Nullable
  public Calendar getStart() {
    if (selectedStartItem == null || selectedEndItem == null) {
      return null;
    }
    return selectedStartItem;
  }

  /** Returns a {@link java.util.Calendar} representing the end of the range. */
  @Nullable
  public Calendar getEnd() {
    if (selectedStartItem == null || selectedEndItem == null) {
      return null;
    }
    return selectedEndItem;
  }

  private boolean skipMonth(
      Calendar firstOfMonth, Calendar lastOfMonth, Calendar startDay, Calendar endDay) {
    if (startDay == null || endDay == null) {
      return true;
    }
    return startDay.after(lastOfMonth) || endDay.before(firstOfMonth);
  }

  private int horizontalMidPoint(View view) {
    return view.getLeft() + view.getWidth() / 2;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<DateRangeGridSelector> CREATOR =
      new Parcelable.Creator<DateRangeGridSelector>() {
        @Override
        public DateRangeGridSelector createFromParcel(Parcel source) {
          DateRangeGridSelector dateRangeGridSelector = new DateRangeGridSelector();
          dateRangeGridSelector.selectedStartItem = (Calendar) source.readSerializable();
          dateRangeGridSelector.selectedEndItem = (Calendar) source.readSerializable();
          return dateRangeGridSelector;
        }

        @Override
        public DateRangeGridSelector[] newArray(int size) {
          return new DateRangeGridSelector[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(selectedStartItem);
    dest.writeSerializable(selectedEndItem);
  }
}
