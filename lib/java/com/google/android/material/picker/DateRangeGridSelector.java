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

import com.google.android.material.R;

import android.content.Context;
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.State;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * A {@link GridSelector} that uses a {@link Pair} of {@link Long} objects to represent a selected
 * range.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateRangeGridSelector implements GridSelector<Pair<Long, Long>> {

  private final LinkedHashSet<OnSelectionChangedListener<Pair<Long, Long>>>
      onSelectionChangedListeners = new LinkedHashSet<>();

  @Nullable private Calendar selectedStartItem = null;
  @Nullable private Calendar selectedEndItem = null;

  private CalendarStyle calendarStyle;

  private final ItemDecoration rangeFill =
      new ItemDecoration() {
        @Override
        public void onDraw(
            @NonNull Canvas canvas, @NonNull RecyclerView recyclerView, @NonNull State state) {
          initializeStyles(recyclerView.getContext());
          if (selectedStartItem == null
              || selectedEndItem == null
              || !(recyclerView.getAdapter() instanceof YearGridAdapter)
              || !(recyclerView.getLayoutManager() instanceof GridLayoutManager)) {
            return;
          }
          YearGridAdapter adapter = (YearGridAdapter) recyclerView.getAdapter();
          GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();
          int firstHighlightPosition =
              adapter.getPositionForYear(selectedStartItem.get(Calendar.YEAR));
          int lastHighlightPosition =
              adapter.getPositionForYear(selectedEndItem.get(Calendar.YEAR));
          View firstView = layoutManager.findViewByPosition(firstHighlightPosition);
          View lastView = layoutManager.findViewByPosition(lastHighlightPosition);

          int firstRow = firstHighlightPosition / layoutManager.getSpanCount();
          int lastRow = lastHighlightPosition / layoutManager.getSpanCount();

          for (int row = firstRow; row <= lastRow; row++) {
            int firstPositionInRow = row * layoutManager.getSpanCount();
            View viewInRow = layoutManager.findViewByPosition(firstPositionInRow);
            if (viewInRow == null) {
              continue;
            }
            int top = viewInRow.getTop() + calendarStyle.year.getTopInset();
            int bottom = viewInRow.getBottom() - calendarStyle.year.getBottomInset();
            int left = row == firstRow ? firstView.getLeft() + firstView.getWidth() / 2 : 0;
            int right =
                row == lastRow
                    ? lastView.getLeft() + lastView.getWidth() / 2
                    : recyclerView.getWidth();
            canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
          }
        }
      };

  // The context is not available on construction and parceling, so we lazily initialize styles.
  private void initializeStyles(Context context) {
    if (calendarStyle == null || calendarStyle.refreshStyles(context)) {
      calendarStyle = new CalendarStyle(context);
    }
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
    GridSelectors.notifyListeners(this, onSelectionChangedListeners);
  }

  @Override
  public boolean addOnSelectionChangedListener(
      OnSelectionChangedListener<Pair<Long, Long>> listener) {
    return onSelectionChangedListeners.add(listener);
  }

  @Override
  public boolean removeOnSelectionChangedListener(
      OnSelectionChangedListener<Pair<Long, Long>> listener) {
    return onSelectionChangedListeners.remove(listener);
  }

  @Override
  public ItemDecoration createYearDecorator() {
    return rangeFill;
  }

  @Override
  public void clearOnSelectionChangedListeners() {
    onSelectionChangedListeners.clear();
  }

  @Override
  public void drawItem(TextView view, Calendar content) {
    initializeStyles(view.getContext());
    CalendarItemStyle style;
    if (content.equals(selectedStartItem) || content.equals(selectedEndItem)) {
      style = calendarStyle.selectedDay;
    } else if (DateUtils.isToday(content.getTimeInMillis())) {
      style = calendarStyle.todayDay;
    } else {
      style = calendarStyle.day;
    }
    style.styleItem(view);
  }

  @Override
  public void drawYearItem(TextView view, int year) {
    initializeStyles(view.getContext());
    CalendarItemStyle style;
    if ((selectedStartItem != null && selectedStartItem.get(Calendar.YEAR) == year)
        || (selectedEndItem != null && selectedEndItem.get(Calendar.YEAR) == year)) {
      style = calendarStyle.selectedYear;
    } else if (Calendar.getInstance().get(Calendar.YEAR) == year) {
      style = calendarStyle.todayYear;
    } else {
      style = calendarStyle.year;
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
      int top = firstView.getTop() + calendarStyle.day.getTopInset();
      int bottom = firstView.getBottom() - calendarStyle.day.getBottomInset();
      int left = firstPositionInRow > firstHighlightPosition ? 0 : rangeHighlightStart;
      int right =
          lastHighlightPosition > lastPositionInRow ? gridView.getWidth() : rangeHighlightEnd;
      canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
    }
  }

  @Override
  public View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View root =
        layoutInflater.inflate(R.layout.mtrl_picker_text_input_date_range, viewGroup, false);

    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    EditText startEditText = startTextInput.getEditText();
    EditText endEditText = endTextInput.getEditText();

    SimpleDateFormat format =
        new SimpleDateFormat(
            root.getResources().getString(R.string.mtrl_picker_text_input_date_format),
            Locale.getDefault());

    if (selectedStartItem != null) {
      startEditText.setText(format.format(selectedStartItem.getTime()));
    }
    if (selectedEndItem != null) {
      endEditText.setText(format.format(selectedEndItem.getTime()));
    }

    // TODO: handle start/end behavior enforcement
    startEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, startTextInput) {
          @Override
          void onDateChanged(@Nullable Calendar calendar) {
            selectedStartItem = calendar;
            GridSelectors.notifyListeners(DateRangeGridSelector.this, onSelectionChangedListeners);
          }
        });
    endEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, endTextInput) {
          @Override
          void onDateChanged(@Nullable Calendar calendar) {
            selectedEndItem = calendar;
            GridSelectors.notifyListeners(DateRangeGridSelector.this, onSelectionChangedListeners);
          }
        });

    ViewUtils.requestFocusAndShowKeyboard(startEditText);

    return root;
  }

  @Override
  @NonNull
  public Pair<Long, Long> getSelection() {
    return new Pair<>(
        selectedStartItem == null ? null : selectedStartItem.getTimeInMillis(),
        selectedEndItem == null ? null : selectedEndItem.getTimeInMillis());
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
