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

import static java.lang.Math.max;
import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.core.util.Pair;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import com.google.android.material.internal.ViewUtils;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.util.Calendar;

final class MaterialCalendarGridView extends GridView {

  private final Calendar dayCompute = UtcDates.getUtcCalendar();
  private final boolean nestedScrollable;
  @Nullable private MaterialCalendar.OnMonthNavigationListener onMonthNavigationListener;

  public MaterialCalendarGridView(Context context) {
    this(context, null);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    if (MaterialDatePicker.isFullscreen(getContext())) {
      setNextFocusLeftId(R.id.cancel_button);
      setNextFocusRightId(R.id.confirm_button);
    }
    nestedScrollable = MaterialDatePicker.isNestedScrollable(getContext());
    ViewCompat.setAccessibilityDelegate(
        this,
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View view, @NonNull AccessibilityNodeInfoCompat accessibilityNodeInfoCompat) {
            super.onInitializeAccessibilityNodeInfo(view, accessibilityNodeInfoCompat);
            // Stop announcing of row/col information in favor of internationalized day information.
            accessibilityNodeInfoCompat.setCollectionInfo(null);
          }
        });
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getAdapter().notifyDataSetChanged();
  }

  void setOnMonthNavigationListener(
      @Nullable MaterialCalendar.OnMonthNavigationListener onMonthNavigationListener) {
    this.onMonthNavigationListener = onMonthNavigationListener;
  }

  @Override
  public void setSelection(int position) {
    int firstValidDayPosition = getAdapter().findFirstValidDayPosition();
    super.setSelection(Math.max(position, firstValidDayPosition));
  }

  @CanIgnoreReturnValue
  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    int position = getSelectedItemPosition();
    if (position == INVALID_POSITION) {
      return super.onKeyDown(keyCode, event);
    }

    boolean isRtl = ViewUtils.isLayoutRtl(this);
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_LEFT:
        return handleHorizontalNavigation(position, isRtl);
      case KeyEvent.KEYCODE_DPAD_RIGHT:
        return handleHorizontalNavigation(position, !isRtl);
      case KeyEvent.KEYCODE_TAB:
        return handleTabNavigation(position, event);
      default:
        if (!super.onKeyDown(keyCode, event)) {
          return false;
        }

        MonthAdapter adapter = getAdapter();
        int selectedPosition = getSelectedItemPosition();
        // If navigation succeeded but landed on a disabled day, select the nearest valid day.
        if (selectedPosition != INVALID_POSITION && !adapter.isDayPositionValid(selectedPosition)) {
          return handleVerticalNavigationOnDisabledDay(keyCode, selectedPosition);
        }
        return true;
    }
  }

  /**
   * Handles key events when vertical navigation lands on a disabled day.
   *
   * <p>If {@code super.onKeyDown()} moves the selection to a disabled day, it attempts to select
   * the closest enabled day in the same row. If no enabled day is found in that row, it continues
   * searching in the same column—row by row—for a row containing a valid day.
   */
  @CanIgnoreReturnValue
  @VisibleForTesting
  boolean handleVerticalNavigationOnDisabledDay(int keyCode, int selectedPosition) {
    MonthAdapter adapter = getAdapter();
    // Try to select the nearest valid day in the same row.
    if (trySelectNearestValidDayPosition(selectedPosition)) {
      return true;
    }

    if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
      int previousPositionInColumn = selectedPosition - getNumColumns();
      while (previousPositionInColumn >= adapter.firstPositionInMonth()) {
        // Search previous rows for a valid day.
        if (trySelectNearestValidDayPosition(previousPositionInColumn)) {
          return true;
        }
        previousPositionInColumn -= getNumColumns();
      }
    } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
      int nextPositionInColumn = selectedPosition + getNumColumns();
      while (nextPositionInColumn <= adapter.lastPositionInMonth()) {
        // Search next rows for a valid day.
        if (trySelectNearestValidDayPosition(nextPositionInColumn)) {
          return true;
        }
        nextPositionInColumn += getNumColumns();
      }
    }
    return false;
  }

  private boolean trySelectNearestValidDayPosition(int position) {
    MonthAdapter adapter = getAdapter();
    int nearestPosition = adapter.findNearestValidDayPositionInRow(position);
    if (nearestPosition != -1) {
      setSelection(nearestPosition);
      return true;
    }
    return false;
  }

  /**
   * Finds the next or previous valid day and selects it.
   *
   * <p>If a valid day is found in the current month, it is selected. If no enabled day is found in
   * the current month in the given direction, month navigation will be triggered if a listener is
   * set.
   *
   * @param position The current position.
   * @param forward {@code true} to navigate forward, {@code false} to navigate backward.
   * @return {@code true} if the event was handled.
   */
  private boolean handleHorizontalNavigation(int position, boolean forward) {
    int nextPosition =
        forward
            ? getAdapter().findNextValidDayPosition(position)
            : getAdapter().findPreviousValidDayPosition(position);
    if (nextPosition != -1) {
      setSelection(nextPosition);
      return true;
    }

    // Reached edge of month, trigger month navigation.
    if (!forward && onMonthNavigationListener != null) {
      return onMonthNavigationListener.onMonthNavigationPrevious();
    } else if (forward && onMonthNavigationListener != null) {
      return onMonthNavigationListener.onMonthNavigationNext();
    }
    return true;
  }

  /**
   * Finds the next/previous valid day in sequence. If no valid day is found in the current month,
   * returns {@code false} to allow focus to move out of the {@link MaterialCalendarGridView}.
   */
  private boolean handleTabNavigation(int position, KeyEvent event) {
    int nextPosition =
        event.isShiftPressed()
            ? getAdapter().findPreviousValidDayPosition(position)
            : getAdapter().findNextValidDayPosition(position);
    if (nextPosition == -1) {
      // If no next focusable item in this month, return false to let the system move focus
      // out of the GridView.
      return false;
    }

    setSelection(nextPosition);
    return true;
  }

  @NonNull
  @Override
  public MonthAdapter getAdapter() {
    return (MonthAdapter) super.getAdapter();
  }

  @Override
  public final void setAdapter(ListAdapter adapter) {
    if (!(adapter instanceof MonthAdapter)) {
      throw new IllegalArgumentException(
          String.format(
              "%1$s must have its Adapter set to a %2$s",
              MaterialCalendarGridView.class.getCanonicalName(),
              MonthAdapter.class.getCanonicalName()));
    }
    super.setAdapter(adapter);
  }

  @Override
  protected final void onDraw(@NonNull Canvas canvas) {
    super.onDraw(canvas);
    MonthAdapter monthAdapter = getAdapter();
    DateSelector<?> dateSelector = monthAdapter.dateSelector;
    CalendarStyle calendarStyle = monthAdapter.calendarStyle;

    // The grid view might get scrolled and some days are not rendered in item views.
    int firstVisiblePositionInMonth =
        max(monthAdapter.firstPositionInMonth(), getFirstVisiblePosition());
    int lastVisiblePositionInMonth =
        min(monthAdapter.lastPositionInMonth(), getLastVisiblePosition());

    Long firstOfMonth = monthAdapter.getItem(firstVisiblePositionInMonth);
    Long lastOfMonth = monthAdapter.getItem(lastVisiblePositionInMonth);

    for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
      if (range.first == null || range.second == null) {
        continue;
      }
      long startItem = range.first;
      long endItem = range.second;

      if (skipMonth(firstOfMonth, lastOfMonth, startItem, endItem)) {
        continue;
      }
      boolean isRtl = ViewUtils.isLayoutRtl(this);
      int firstHighlightPosition;
      int rangeHighlightStart;
      if (startItem < firstOfMonth) {
        firstHighlightPosition = firstVisiblePositionInMonth;
        rangeHighlightStart =
            monthAdapter.isFirstInRow(firstHighlightPosition)
                ? 0
                : !isRtl
                    ? getChildAtPosition(firstHighlightPosition - 1).getRight()
                    : getChildAtPosition(firstHighlightPosition - 1).getLeft();
      } else {
        dayCompute.setTimeInMillis(startItem);
        firstHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightStart = horizontalMidPoint(getChildAtPosition(firstHighlightPosition));
      }

      int lastHighlightPosition;
      int rangeHighlightEnd;
      if (endItem > lastOfMonth) {
        lastHighlightPosition = lastVisiblePositionInMonth;
        rangeHighlightEnd =
            monthAdapter.isLastInRow(lastHighlightPosition)
                ? getWidth()
                : !isRtl
                    ? getChildAtPosition(lastHighlightPosition).getRight()
                    : getChildAtPosition(lastHighlightPosition).getLeft();
      } else {
        dayCompute.setTimeInMillis(endItem);
        lastHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightEnd = horizontalMidPoint(getChildAtPosition(lastHighlightPosition));
      }

      int firstRow = (int) monthAdapter.getItemId(firstHighlightPosition);
      int lastRow = (int) monthAdapter.getItemId(lastHighlightPosition);
      for (int row = firstRow; row <= lastRow; row++) {
        int firstPositionInRow = row * getNumColumns();
        int lastPositionInRow = firstPositionInRow + getNumColumns() - 1;
        View firstView = getChildAtPosition(firstPositionInRow);
        int top = firstView.getTop() + calendarStyle.day.getTopInset();
        int bottom = firstView.getBottom() - calendarStyle.day.getBottomInset();
        int left;
        int right;
        if (!isRtl) {
          left = firstPositionInRow > firstHighlightPosition ? 0 : rangeHighlightStart;
          right = lastHighlightPosition > lastPositionInRow ? getWidth() : rangeHighlightEnd;
        } else {
          left = lastHighlightPosition > lastPositionInRow ? 0 : rangeHighlightEnd;
          right = firstPositionInRow > firstHighlightPosition ? getWidth() : rangeHighlightStart;
        }
        canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
      }
    }
  }

  @Override
  public void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    if (nestedScrollable) {
      // Calculate entire height by providing a very large height hint.
      // View.MEASURED_SIZE_MASK represents the largest height possible.
      int expandSpec = MeasureSpec.makeMeasureSpec(MEASURED_SIZE_MASK, MeasureSpec.AT_MOST);
      super.onMeasure(widthMeasureSpec, expandSpec);
      ViewGroup.LayoutParams params = getLayoutParams();
      params.height = getMeasuredHeight();
    } else {
      super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
  }

  @Override
  protected void onFocusChanged(boolean gainFocus, int direction, Rect previouslyFocusedRect) {
    if (gainFocus) {
      gainFocus(direction, previouslyFocusedRect);
    } else {
      super.onFocusChanged(false, direction, previouslyFocusedRect);
    }
  }

  private void gainFocus(int direction, Rect previouslyFocusedRect) {
    int position = -1;
    if (direction == FOCUS_UP || direction == FOCUS_BACKWARD) {
      position = getAdapter().findLastValidDayPosition();
    } else if (direction == FOCUS_DOWN || direction == FOCUS_FORWARD) {
      position = getAdapter().findFirstValidDayPosition();
    }
    if (position != -1) {
      setSelection(position);
    } else {
      super.onFocusChanged(true, direction, previouslyFocusedRect);
    }
  }

  private View getChildAtPosition(int position) {
    return getChildAt(position - getFirstVisiblePosition());
  }

  private static boolean skipMonth(
      @Nullable Long firstOfMonth,
      @Nullable Long lastOfMonth,
      @Nullable Long startDay,
      @Nullable Long endDay) {
    if (firstOfMonth == null || lastOfMonth == null || startDay == null || endDay == null) {
      return true;
    }
    return startDay > lastOfMonth || endDay < firstOfMonth;
  }

  private static int horizontalMidPoint(@NonNull View view) {
    return view.getLeft() + view.getWidth() / 2;
  }
}
