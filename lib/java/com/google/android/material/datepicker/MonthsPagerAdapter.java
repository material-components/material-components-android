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
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutParams;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.MaterialCalendar.OnDayClickListener;

/**
 * Manages the instances of {@link MaterialCalendarGridView} that represent each month in a {@link
 * MaterialCalendar}. Caps memory usage via {@link RecyclerView} extension.
 */
class MonthsPagerAdapter extends RecyclerView.Adapter<MonthsPagerAdapter.ViewHolder> {

  private final Context context;
  @NonNull private final CalendarConstraints calendarConstraints;
  private final DateSelector<?> dateSelector;
  private final OnDayClickListener onDayClickListener;
  private final int itemHeight;

  MonthsPagerAdapter(
      @NonNull Context context,
      DateSelector<?> dateSelector,
      @NonNull CalendarConstraints calendarConstraints,
      OnDayClickListener onDayClickListener) {
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

    this.context = context;
    this.itemHeight = daysHeight + labelHeight;
    this.calendarConstraints = calendarConstraints;
    this.dateSelector = dateSelector;
    this.onDayClickListener = onDayClickListener;
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
    viewHolder.monthTitle.setText(month.getLongName(viewHolder.itemView.getContext()));
    final MaterialCalendarGridView monthGrid = viewHolder.monthGrid.findViewById(R.id.month_grid);

    if (monthGrid.getAdapter() != null && month.equals(monthGrid.getAdapter().month)) {
      monthGrid.invalidate();
      monthGrid.getAdapter().updateSelectedStates(monthGrid);
    } else {
      MonthAdapter monthAdapter = new MonthAdapter(month, dateSelector, calendarConstraints);
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
    return getPageMonth(position).getLongName(context);
  }

  @NonNull
  Month getPageMonth(int position) {
    return calendarConstraints.getStart().monthsLater(position);
  }

  int getPosition(@NonNull Month month) {
    return calendarConstraints.getStart().monthsUntil(month);
  }
}
