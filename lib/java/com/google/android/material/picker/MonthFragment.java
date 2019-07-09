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
import android.os.Bundle;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.TextView;
import com.google.android.material.picker.MaterialCalendar.OnDayClickListener;

/**
 * A {@link Fragment} representing the days of a single {@link Month} highlighted by a {@link
 * GridSelector}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MonthFragment extends Fragment {

  private static final String MONTH_KEY = "MONTH_KEY";
  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";

  private Month month;
  private MonthAdapter monthAdapter;
  private GridSelector<?> gridSelector;
  private CalendarConstraints calendarConstraints;
  // Set as part of Lifecycle.Event#onCreate
  private OnDayClickListener onDayClickListener;

  public void setOnDayClickListener(OnDayClickListener onDayClickListener) {
    this.onDayClickListener = onDayClickListener;
  }

  /**
   * Constructs a new {@link MonthFragment}.
   *
   * @param month The {@link Month} this {@link MonthFragment} displays data for
   * @param gridSelector The {@link GridSelector} used to highlight and mark the {@link GridView}
   */
  public static MonthFragment newInstance(
      Month month, GridSelector<?> gridSelector, CalendarConstraints calendarConstraints) {
    MonthFragment monthFragment = new MonthFragment();
    Bundle arguments = new Bundle();
    arguments.putParcelable(MONTH_KEY, month);
    arguments.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    arguments.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    monthFragment.setArguments(arguments);
    return monthFragment;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    month = getArguments().getParcelable(MONTH_KEY);
    gridSelector = getArguments().getParcelable(GRID_SELECTOR_KEY);
    calendarConstraints = getArguments().getParcelable(CALENDAR_CONSTRAINTS_KEY);
  }

  @Override
  public View onCreateView(
      LayoutInflater layoutInflater, ViewGroup root, Bundle savedInstanceState) {
    Context context = getParentFragment().getView().getContext();
    LayoutInflater themedInflater = LayoutInflater.from(context);
    monthAdapter = new MonthAdapter(month, gridSelector, calendarConstraints);

    final int layout;
    if (MaterialDatePicker.isFullscreen(context)) {
      layout = R.layout.mtrl_calendar_month_labeled;
    } else {
      layout = R.layout.mtrl_calendar_month;
    }
    View view = themedInflater.inflate(layout, root, false);
    TextView monthTitle = view.findViewById(R.id.month_title);
    if (monthTitle != null) {
      monthTitle.setText(month.getLongName());
    }

    MaterialCalendarGridView gridView = view.findViewById(R.id.month_grid);
    gridView.setNumColumns(month.daysInWeek);
    gridView.setAdapter(monthAdapter);
    gridView.setOnItemClickListener(
        (parent, v, position, id) -> {
          if (monthAdapter.withinMonth(position)) {
            onDayClickListener.onDayClick(monthAdapter.getItem(position));
          }
        });

    return view;
  }

  void notifyDataSetChanged() {
    monthAdapter.notifyDataSetChanged();
  }
}
