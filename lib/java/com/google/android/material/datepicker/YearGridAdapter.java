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

import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import com.google.android.material.datepicker.MaterialCalendar.CalendarSelector;
import java.util.Calendar;
import java.util.Locale;

class YearGridAdapter extends RecyclerView.Adapter<YearGridAdapter.ViewHolder> {

  private final MaterialCalendar<?> materialCalendar;

  public static class ViewHolder extends RecyclerView.ViewHolder {

    final TextView textView;

    ViewHolder(TextView view) {
      super(view);
      this.textView = view;
    }
  }

  YearGridAdapter(MaterialCalendar<?> materialCalendar) {
    this.materialCalendar = materialCalendar;
  }

  @NonNull
  @Override
  public YearGridAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
    TextView yearTextView =
        (TextView)
            LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.mtrl_calendar_year, viewGroup, false);
    return new ViewHolder(yearTextView);
  }

  @Override
  public void onBindViewHolder(@NonNull YearGridAdapter.ViewHolder viewHolder, int position) {
    int year = getYearForPosition(position);
    viewHolder.textView.setText(String.format(Locale.getDefault(), "%d", year));
    viewHolder.textView.setContentDescription(
        DateStrings.getYearContentDescription(viewHolder.textView.getContext(), year));
    CalendarStyle styles = materialCalendar.getCalendarStyle();
    Calendar calendar = UtcDates.getTodayCalendar();
    CalendarItemStyle style = calendar.get(Calendar.YEAR) == year ? styles.todayYear : styles.year;
    for (Long day : materialCalendar.getDateSelector().getSelectedDays()) {
      calendar.setTimeInMillis(day);
      if (calendar.get(Calendar.YEAR) == year) {
        style = styles.selectedYear;
      }
    }
    style.styleItem(viewHolder.textView);
    viewHolder.textView.setSelected(style == styles.selectedYear);
    viewHolder.textView.setOnClickListener(createYearClickListener(year));
  }

  @NonNull
  private OnClickListener createYearClickListener(final int year) {
    return new OnClickListener() {
      @Override
      public void onClick(View view) {
        Month current = Month.create(year, materialCalendar.getCurrentMonth().month);
        CalendarConstraints calendarConstraints = materialCalendar.getCalendarConstraints();
        Month moveTo = calendarConstraints.clamp(current);
        materialCalendar.setCurrentMonth(moveTo);
        materialCalendar.setSelector(CalendarSelector.DAY);
        materialCalendar.sendAccessibilityFocusEventToMonthDropdown();
      }
    };
  }

  @Override
  public int getItemCount() {
    return materialCalendar.getCalendarConstraints().getYearSpan();
  }

  int getPositionForYear(int year) {
    return year - materialCalendar.getCalendarConstraints().getStart().year;
  }

  int getYearForPosition(int position) {
    return materialCalendar.getCalendarConstraints().getStart().year + position;
  }
}
