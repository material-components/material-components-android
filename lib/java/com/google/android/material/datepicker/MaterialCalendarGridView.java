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
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.widget.GridView;
import android.widget.ListAdapter;
import java.util.Calendar;

final class MaterialCalendarGridView extends GridView {

  private final Calendar dayCompute = UtcDates.getCalendar();

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

  @Override
  public void setSelection(int position) {
    if (position < getAdapter().firstPositionInMonth()) {
      super.setSelection(getAdapter().firstPositionInMonth());
    } else {
      super.setSelection(position);
    }
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    boolean result = super.onKeyDown(keyCode, event);
    if (!result) {
      return false;
    }
    if (getSelectedItemPosition() == INVALID_POSITION
        || getSelectedItemPosition() >= getAdapter().firstPositionInMonth()) {
      return true;
    }
    if (KeyEvent.KEYCODE_DPAD_UP == keyCode) {
      setSelection(getAdapter().firstPositionInMonth());
      return true;
    }
    return false;
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
    Long firstOfMonth = monthAdapter.getItem(monthAdapter.firstPositionInMonth());
    Long lastOfMonth = monthAdapter.getItem(monthAdapter.lastPositionInMonth());

    for (Pair<Long, Long> range : dateSelector.getSelectedRanges()) {
      if (range.first == null || range.second == null) {
        continue;
      }
      long startItem = range.first;
      long endItem = range.second;

      if (skipMonth(firstOfMonth, lastOfMonth, startItem, endItem)) {
        return;
      }

      int firstHighlightPosition;
      int rangeHighlightStart;
      if (startItem < firstOfMonth) {
        firstHighlightPosition = monthAdapter.firstPositionInMonth();
        rangeHighlightStart =
            monthAdapter.isFirstInRow(firstHighlightPosition)
                ? 0
                : getChildAt(firstHighlightPosition - 1).getRight();
      } else {
        dayCompute.setTimeInMillis(startItem);
        firstHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightStart = horizontalMidPoint(getChildAt(firstHighlightPosition));
      }

      int lastHighlightPosition;
      int rangeHighlightEnd;
      if (endItem > lastOfMonth) {
        lastHighlightPosition = monthAdapter.lastPositionInMonth();
        rangeHighlightEnd =
            monthAdapter.isLastInRow(lastHighlightPosition)
                ? getWidth()
                : getChildAt(lastHighlightPosition).getRight();
      } else {
        dayCompute.setTimeInMillis(endItem);
        lastHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightEnd = horizontalMidPoint(getChildAt(lastHighlightPosition));
      }

      int firstRow = (int) monthAdapter.getItemId(firstHighlightPosition);
      int lastRow = (int) monthAdapter.getItemId(lastHighlightPosition);
      for (int row = firstRow; row <= lastRow; row++) {
        int firstPositionInRow = row * getNumColumns();
        int lastPositionInRow = firstPositionInRow + getNumColumns() - 1;
        View firstView = getChildAt(firstPositionInRow);
        int top = firstView.getTop() + calendarStyle.day.getTopInset();
        int bottom = firstView.getBottom() - calendarStyle.day.getBottomInset();
        int left = firstPositionInRow > firstHighlightPosition ? 0 : rangeHighlightStart;
        int right = lastHighlightPosition > lastPositionInRow ? getWidth() : rangeHighlightEnd;
        canvas.drawRect(left, top, right, bottom, calendarStyle.rangeFill);
      }
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
    if (direction == FOCUS_UP) {
      setSelection(getAdapter().lastPositionInMonth());
    } else if (direction == FOCUS_DOWN) {
      setSelection(getAdapter().firstPositionInMonth());
    } else {
      super.onFocusChanged(true, direction, previouslyFocusedRect);
    }
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
