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

import android.content.Context;
import android.graphics.Canvas;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.text.format.DateUtils;
import android.widget.TextView;
import java.util.Calendar;

/**
 * A {@link GridSelector} that uses a {@link Calendar} for its selection state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateGridSelector implements GridSelector<Calendar> {

  private Calendar selectedItem;
  private CalendarStyle calendarStyle;

  // The context is not available on construction and parceling, so we lazily initialize styles.
  private void initializeStyles(Context context) {
    if (calendarStyle != null) {
      return;
    }
    calendarStyle = new CalendarStyle(context);
  }

  @Override
  public void select(Calendar selection) {
    selectedItem = selection;
  }

  @Override
  public void drawItem(TextView view, Calendar content) {
    initializeStyles(view.getContext());
    CalendarItemStyle style;
    if (content.equals(selectedItem)) {
      style = calendarStyle.selectedDay;
    } else if (DateUtils.isToday(content.getTimeInMillis())) {
      style = calendarStyle.today;
    } else {
      style = calendarStyle.day;
    }
    style.styleItem(view);
  }

  @Override
  @Nullable
  public Calendar getSelection() {
    return selectedItem;
  }

  @Override
  public void onCalendarMonthDraw(Canvas canvas, MaterialCalendarGridView gridView) {
    // do nothing
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<DateGridSelector> CREATOR =
      new Parcelable.Creator<DateGridSelector>() {
        @Override
        public DateGridSelector createFromParcel(Parcel source) {
          DateGridSelector dateGridSelector = new DateGridSelector();
          dateGridSelector.selectedItem = (Calendar) source.readSerializable();
          return dateGridSelector;
        }

        @Override
        public DateGridSelector[] newArray(int size) {
          return new DateGridSelector[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeSerializable(selectedItem);
  }
}
