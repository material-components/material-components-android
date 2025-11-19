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
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.view.ViewCompat;
import com.google.android.material.datepicker.MaterialCalendar.OnDayClickListener;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Manages the instances of {@link MaterialCalendarGridView} that represent each month in a {@link
 * MaterialCalendar}. Caps memory usage via {@link RecyclerView} extension.
 */
class MonthsPagerAdapter extends RecyclerView.Adapter<MonthsPagerAdapter.ViewHolder> {

  private static final int POSITION_UNSPECIFIED = 0;

  /**
   * Annotation for constants indicating the direction of focus for keyboard navigation between
   * months. This determines whether focus should land on the first or last enabled day of the month
   * when it becomes visible.
   *
   * @hide
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @Retention(RetentionPolicy.SOURCE)
  @IntDef({POSITION_UNSPECIFIED, View.FOCUS_FORWARD, View.FOCUS_BACKWARD})
  @interface KeyboardFocusDirection {}

  @NonNull private final CalendarConstraints calendarConstraints;
  private final DateSelector<?> dateSelector;
  @Nullable private final DayViewDecorator dayViewDecorator;
  private final OnDayClickListener onDayClickListener;
  @Nullable private final MaterialCalendar.OnMonthNavigationListener onMonthNavigationListener;
  private final int itemHeight;
  @Nullable private Month visibleMonth;
  @KeyboardFocusDirection private int keyboardFocusDirection = POSITION_UNSPECIFIED;

  MonthsPagerAdapter(
      @NonNull Context context,
      DateSelector<?> dateSelector,
      @NonNull CalendarConstraints calendarConstraints,
      @Nullable DayViewDecorator dayViewDecorator,
      OnDayClickListener onDayClickListener,
      @Nullable MaterialCalendar.OnMonthNavigationListener onMonthNavigationListener) {
    Month firstPage = calendarConstraints.getStart();
    Month lastPage = calendarConstraints.getEnd();
    Month currentPage = calendarConstraints.getOpenAt();

    if (firstPage.compareTo(currentPage) > 0) {
      throw new IllegalArgumentException("firstPage cannot be after currentPage");
    }
    if (currentPage.compareTo(lastPage) > 0) {
      throw new IllegalArgumentException("currentPage cannot be after lastPage");
    }

    int daysHeight = MonthAdapter.MAXIMUM_WEEKS * MaterialCalendar.getDayHeight(context);
    int labelHeight =
        MaterialDatePicker.isFullscreen(context) ? MaterialCalendar.getDayHeight(context) : 0;

    this.itemHeight = daysHeight + labelHeight;
    this.calendarConstraints = calendarConstraints;
    this.dateSelector = dateSelector;
    this.dayViewDecorator = dayViewDecorator;
    this.onDayClickListener = onDayClickListener;
    this.onMonthNavigationListener = onMonthNavigationListener;
    this.visibleMonth = currentPage;
    setHasStableIds(true);
  }

  public static class ViewHolder extends RecyclerView.ViewHolder {

    final TextView monthTitle;
    final MaterialCalendarGridView monthGrid;

    ViewHolder(@NonNull LinearLayout container, boolean showLabel) {
      super(container);
      monthTitle = container.findViewById(R.id.month_title);
      ViewCompat.setAccessibilityHeading(monthTitle, true);
      monthGrid = container.findViewById(R.id.month_grid);
      if (!showLabel) {
        monthTitle.setVisibility(View.GONE);
      }
    }
  }

  @NonNull
  @Override
  public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    LinearLayout container =
        (LinearLayout)
            LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mtrl_calendar_month_labeled, viewGroup, false);

    if (MaterialDatePicker.isFullscreen(viewGroup.getContext())) {
      container.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, itemHeight));
      return new ViewHolder(container, /* showLabel= */ true);
    } else {
      return new ViewHolder(container, /* showLabel= */ false);
    }
  }

  @Override
  public void onBindViewHolder(@NonNull MonthsPagerAdapter.ViewHolder viewHolder, int position) {
    Month month = calendarConstraints.getStart().monthsLater(position);
    viewHolder.monthTitle.setText(month.getLongName());
    final MaterialCalendarGridView monthGrid = viewHolder.monthGrid.findViewById(R.id.month_grid);

    if (monthGrid.getAdapter() != null && month.equals(monthGrid.getAdapter().month)) {
      monthGrid.invalidate();
      monthGrid.getAdapter().updateSelectedStates(monthGrid);
    } else {
      MonthAdapter monthAdapter =
          new MonthAdapter(month, dateSelector, calendarConstraints, dayViewDecorator);
      monthGrid.setNumColumns(month.daysInWeek);
      monthGrid.setAdapter(monthAdapter);
    }

    monthGrid.setOnItemClickListener(
        new OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (monthGrid.getAdapter().withinMonth(position)) {
              onDayClickListener.onDayClick(monthGrid.getAdapter().getItem(position));
            }
          }
        });
    monthGrid.setOnMonthNavigationListener(onMonthNavigationListener);

    boolean isFullscreen = MaterialDatePicker.isFullscreen(viewHolder.itemView.getContext());
    if (isFullscreen || month.equals(visibleMonth)) {
      monthGrid.setFocusable(true);
      monthGrid.setDescendantFocusability(ViewGroup.FOCUS_BEFORE_DESCENDANTS);
    } else {
      monthGrid.setFocusable(false);
      monthGrid.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
    }

    if (!isFullscreen && month.equals(visibleMonth)) {
      setInitialKeyboardFocus(monthGrid);
    }
  }

  private void setInitialKeyboardFocus(@NonNull MaterialCalendarGridView monthGrid) {
    @KeyboardFocusDirection int direction = keyboardFocusDirection;
    keyboardFocusDirection = POSITION_UNSPECIFIED;
    monthGrid.post(
        () -> {
          if (monthGrid.hasFocus() && direction != POSITION_UNSPECIFIED) {
            int initialPosition =
                getInitialDayPositionForDirection(monthGrid.getAdapter(), direction);
            monthGrid.setSelection(initialPosition);
          }
        });
  }

  private int getInitialDayPositionForDirection(
      @NonNull MonthAdapter monthAdapter, @KeyboardFocusDirection int direction) {
    if (direction == View.FOCUS_BACKWARD) {
      int lastPosition = monthAdapter.findLastValidDayPosition();
      return lastPosition == -1 ? monthAdapter.lastPositionInMonth() : lastPosition;
    } else {
      int firstPosition = monthAdapter.findFirstValidDayPosition();
      return firstPosition == -1 ? monthAdapter.firstPositionInMonth() : firstPosition;
    }
  }

  @Override
  public long getItemId(int position) {
    return calendarConstraints.getStart().monthsLater(position).getStableId();
  }

  @Override
  public int getItemCount() {
    return calendarConstraints.getMonthSpan();
  }

  @NonNull
  CharSequence getPageTitle(int position) {
    return getPageMonth(position).getLongName();
  }

  @NonNull
  Month getPageMonth(int position) {
    return calendarConstraints.getStart().monthsLater(position);
  }

  int getPosition(@NonNull Month month) {
    return calendarConstraints.getStart().monthsUntil(month);
  }

  /**
   * Sets the direction for keyboard focus to determine whether the first or last day of the month
   * should receive keyboard focus when the grid for that month is instantiated.
   */
  void setKeyboardFocusDirection(@KeyboardFocusDirection int keyboardFocusDirection) {
    this.keyboardFocusDirection = keyboardFocusDirection;
  }

  /**
   * Updates the currently visible month and notifies the adapter to redraw the affected month
   * views.
   *
   * <p>This is crucial for managing keyboard focus in non-fullscreen mode. By ensuring only the
   * grid for the {@code visibleMonth} is focusable, this prevents keyboard focus from landing on
   * an adjacent month view when navigating with TAB.
   */
  void setVisibleMonth(@Nullable Month month) {
    if (month != null && !month.equals(this.visibleMonth)) {
      int oldPosition = getPosition(this.visibleMonth);
      this.visibleMonth = month;
      int newPosition = getPosition(this.visibleMonth);
      notifyItemChanged(oldPosition);
      notifyItemChanged(newPosition);
    }
  }
}
