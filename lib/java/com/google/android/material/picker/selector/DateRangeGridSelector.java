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
package com.google.android.material.picker.selector;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import com.google.android.material.picker.MonthInYearAdapter;
import androidx.core.util.Pair;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.AdapterView;
import java.util.Calendar;

/**
 * A {@link GridSelector} that uses a {@link Pair} of {@link Calendar} objects to represent a
 * selected range
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateRangeGridSelector implements GridSelector<Pair<Calendar, Calendar>> {

  @VisibleForTesting static final ColorDrawable emptyColor = new ColorDrawable(Color.TRANSPARENT);
  @VisibleForTesting static final ColorDrawable startColor = new ColorDrawable(Color.RED);
  @VisibleForTesting static final ColorDrawable endColor = new ColorDrawable(Color.GREEN);
  @VisibleForTesting static final ColorDrawable rangeColor = new ColorDrawable(Color.YELLOW);

  private Calendar selectedStartItem = null;
  private Calendar selectedEndItem = null;

  @Override
  public void onItemClick(
      AdapterView<? extends MonthInYearAdapter> parent, View view, int position, long row) {
    MonthInYearAdapter adapter = parent.getAdapter();
    Calendar selection = adapter.getItem(position);
    if (!adapter.withinMonth(position)) {
      return;
    }
    if (selectedStartItem == null) {
      selectedStartItem = selection;
    } else if (selectedEndItem == null && selection.after(selectedStartItem)) {
      selectedEndItem = selection;
    } else {
      selectedEndItem = null;
      selectedStartItem = selection;
    }
  }

  @Override
  public void drawSelection(AdapterView<? extends MonthInYearAdapter> parent) {
    MonthInYearAdapter adapter = parent.getAdapter();

    for (int i = 0; i < parent.getCount(); i++) {
      Calendar item = adapter.getItem(i);
      View cell = parent.getChildAt(i);
      if (item != null && cell != null) {
        drawCell(cell, item);
      }
    }
  }

  @Override
  public void drawCell(View cell, Calendar item) {
    ColorDrawable setColor = emptyColor;
    if (item.equals(selectedStartItem)) {
      setColor = startColor;
    } else if (item.equals(selectedEndItem)) {
      setColor = endColor;
    } else if (item.after(selectedStartItem) && item.before(selectedEndItem)) {
      setColor = rangeColor;
    }
    ViewCompat.setBackground(cell, setColor);
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

  /** Returns a {@link java.util.Calendar} representing the start of the range */
  @Nullable
  public Calendar getStart() {
    return selectedStartItem;
  }

  /** Returns a {@link java.util.Calendar} representing the end of the range */
  @Nullable
  public Calendar getEnd() {
    return selectedEndItem;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<DateRangeGridSelector> CREATOR =
      new Parcelable.Creator<DateRangeGridSelector>() {
        @Override
        public DateRangeGridSelector createFromParcel(Parcel source) {
          DateRangeGridSelector dateRangeGridSelector = new DateRangeGridSelector();
          dateRangeGridSelector.selectedStartItem = (Calendar) source.readSerializable();
          dateRangeGridSelector.selectedEndItem = (Calendar) source.readSerializable();
          return dateRangeGridSelector;
        }

        @Override
        public DateRangeGridSelector[] newArray(int size) {
          return new DateRangeGridSelector[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(selectedStartItem);
    dest.writeSerializable(selectedEndItem);
  }
}
