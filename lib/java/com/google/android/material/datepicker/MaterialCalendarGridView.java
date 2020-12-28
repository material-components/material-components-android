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

import static java.lang.Math.min;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import androidx.core.util.Pair;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListAdapter;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ViewUtils;
import java.util.Calendar;

final class MaterialCalendarGridView extends GridView {

  private final Calendar dayCompute = UtcDates.getUtcCalendar();
  private final boolean nestedScrollable;

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
        continue;
      }
      boolean isRtl = ViewUtils.isLayoutRtl(this);
      int firstHighlightPosition;
      int rangeHighlightStart;
      if (startItem < firstOfMonth) {
        firstHighlightPosition = monthAdapter.firstPositionInMonth();
        rangeHighlightStart =
            monthAdapter.isFirstInRow(firstHighlightPosition)
                ? 0
                : !isRtl
                    ? getChildAt(firstHighlightPosition - 1).getRight()
                    : getChildAt(firstHighlightPosition - 1).getLeft();
      } else {
        dayCompute.setTimeInMillis(startItem);
        firstHighlightPosition = monthAdapter.dayToPosition(dayCompute.get(Calendar.DAY_OF_MONTH));
        rangeHighlightStart = horizontalMidPoint(getChildAt(firstHighlightPosition));
      }

      int lastHighlightPosition;
      int rangeHighlightEnd;
      if (endItem > lastOfMonth) {
        lastHighlightPosition = min(monthAdapter.lastPositionInMonth(), getChildCount() - 1);
        rangeHighlightEnd =
            monthAdapter.isLastInRow(lastHighlightPosition)
                ? getWidth()
                : !isRtl
                    ? getChildAt(lastHighlightPosition).getRight()
                    : getChildAt(lastHighlightPosition).getLeft();
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
