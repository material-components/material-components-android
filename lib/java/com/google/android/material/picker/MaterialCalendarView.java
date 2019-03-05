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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.LinearLayout;
import java.util.Calendar;

/**
 * View for a days of week {@link Calendar} represented as a header row of days labels and {@link
 * GridView} of days backed by {@link MonthInYearAdapter}.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class MaterialCalendarView<S> extends LinearLayoutCompat {

  private final MonthInYearAdapter monthInYearAdapter;

  public MaterialCalendarView(Context context) {
    this(context, null);
  }

  public MaterialCalendarView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialCalendarView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setOrientation(LinearLayout.VERTICAL);

    LayoutInflater layoutInflater = LayoutInflater.from(context);
    layoutInflater.inflate(R.layout.date_picker_calendar_days_header, this);
    layoutInflater.inflate(R.layout.date_picker_calendar_days, this);
    GridView daysHeader = findViewById(R.id.date_picker_calendar_days_header);
    GridView daysGrid = findViewById(R.id.date_picker_calendar_days);

    Calendar calendar = Calendar.getInstance();
    MonthInYear monthInYear =
        MonthInYear.create(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH));
    monthInYearAdapter = new MonthInYearAdapter(context, monthInYear);

    daysHeader.setAdapter(new DaysHeaderAdapter());
    daysHeader.setNumColumns(monthInYear.daysInWeek);
    daysGrid.setAdapter(monthInYearAdapter);
    daysGrid.setNumColumns(monthInYear.daysInWeek);
    daysGrid.setOnItemClickListener(
        new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            getOnItemClickListener().onItemClick(parent, view, position, id);
            drawSelection(parent);
            // Allows users of MaterialCalendarView to set an OnClickListener
            if (VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN) {
              callOnClick();
            } else {
              performClick();
            }
          }
        });
  }

  protected final MonthInYearAdapter getMonthInYearAdapter() {
    return monthInYearAdapter;
  }

  @Nullable
  protected abstract S getSelection();

  protected abstract void drawSelection(AdapterView<?> parent);

  protected abstract OnItemClickListener getOnItemClickListener();
}
