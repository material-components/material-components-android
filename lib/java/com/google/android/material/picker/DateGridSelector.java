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
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.resources.MaterialAttributes;
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

  @Override
  public void select(Calendar selection) {
    selectedItem = selection;
  }

  @Override
  public void drawCell(TextView cell, Calendar item) {
    Context context = cell.getContext();
    int calendarStyle =
        MaterialAttributes.resolveOrThrow(
            context, R.attr.materialDatePickerStyle, MaterialCalendar.class.getCanonicalName());

    int style;
    TypedArray stylesList =
        context.obtainStyledAttributes(calendarStyle, R.styleable.MaterialCalendar);
    if (item.equals(selectedItem)) {
      style = stylesList.getResourceId(R.styleable.MaterialCalendar_daySelectedStyle, 0);
    } else if (DateUtils.isToday(item.getTimeInMillis())) {
      style = stylesList.getResourceId(R.styleable.MaterialCalendar_dayTodayStyle, 0);
    } else {
      style = stylesList.getResourceId(R.styleable.MaterialCalendar_dayStyle, 0);
    }
    stylesList.recycle();

    CalendarGridSelectors.colorCell(cell, style);
  }

  @Override
  @Nullable
  public Calendar getSelection() {
    return selectedItem;
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
