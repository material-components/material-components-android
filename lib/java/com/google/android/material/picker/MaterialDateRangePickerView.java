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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.Calendar;

/**
 * A {@link MaterialCalendarView} that supports range selection
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDateRangePickerView extends MaterialCalendarView<Pair<Calendar, Calendar>> {

  private static final int DEF_STYLE_ATTR = R.attr.materialDateRangePickerStyle;

  private static final ColorDrawable emptyColor = new ColorDrawable(Color.TRANSPARENT);
  private static final ColorDrawable startColor = new ColorDrawable(Color.RED);
  private static final ColorDrawable endColor = new ColorDrawable(Color.GREEN);
  private static final ColorDrawable rangeColor = new ColorDrawable(Color.YELLOW);

  private final OnItemClickListener onItemClickListener;
  private int selectedStartPosition = -1;
  private int selectedEndPosition = -1;

  public MaterialDateRangePickerView(Context context) {
    this(context, null);
  }

  public MaterialDateRangePickerView(Context context, AttributeSet attrs) {
    this(context, attrs, DEF_STYLE_ATTR);
  }

  public MaterialDateRangePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    onItemClickListener =
        new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!getMonthInYearAdapter().withinMonth(position)) {
              return;
            }
            if (selectedStartPosition < 0) {
              selectedStartPosition = position;
            } else if (selectedEndPosition < 0 && position > selectedStartPosition) {
              selectedEndPosition = position;
            } else {
              selectedEndPosition = -1;
              selectedStartPosition = position;
            }
          }
        };
  }

  @Override
  protected OnItemClickListener getOnItemClickListener() {
    return onItemClickListener;
  }

  @Override
  protected void drawSelection(AdapterView<?> parent) {
    for (int i = 0; i < parent.getCount(); i++) {
      ColorDrawable setColor = emptyColor;
      if (i == selectedStartPosition) {
        setColor = startColor;
      } else if (i == selectedEndPosition) {
        setColor = endColor;
      } else if (i > selectedStartPosition && i < selectedEndPosition) {
        setColor = rangeColor;
      }
      ViewCompat.setBackground(parent.getChildAt(i), setColor);
    }
  }

  @Override
  @Nullable
  public Pair<Calendar, Calendar> getSelection() {
    Calendar start = getStart();
    Calendar end = getEnd();
    if (start == null || end == null) {
      return null;
    }
    return new Pair<>(getStart(), getEnd());
  }

  @Nullable
  public Calendar getStart() {
    return getMonthInYearAdapter().getItem(selectedStartPosition);
  }

  @Nullable
  public Calendar getEnd() {
    return getMonthInYearAdapter().getItem(selectedEndPosition);
  }
}
