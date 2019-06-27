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

import androidx.annotation.NonNull;
import com.google.android.material.picker.MaterialCalendar.CalendarSelector;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;
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
    materialCalendar.getGridSelector().drawYearItem(viewHolder.textView, year);
    viewHolder.textView.setOnClickListener(createYearClickListener(year));
  }

  private OnClickListener createYearClickListener(int year) {
    return view -> {
      Month moveTo = Month.create(year, materialCalendar.getCalendarBounds().getCurrent().month);
      materialCalendar.setCurrentMonth(moveTo);
      materialCalendar.setSelector(CalendarSelector.DAY);
    };
  }

  @Override
  public int getItemCount() {
    return materialCalendar.getCalendarBounds().getYearSpan();
  }

  int getPositionForYear(int year) {
    return year - materialCalendar.getCalendarBounds().getStart().year;
  }

  int getYearForPosition(int position) {
    return materialCalendar.getCalendarBounds().getStart().year + position;
  }
}
