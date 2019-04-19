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
import com.google.android.material.picker.MonthAdapter;
import androidx.core.view.ViewCompat;
import android.view.View;
import android.widget.AdapterView;
import java.util.Calendar;

/**
 * A {@link GridSelector} that uses a {@link Calendar} for its selection state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateGridSelector implements GridSelector<Calendar> {

  @VisibleForTesting static final ColorDrawable emptyColor = new ColorDrawable(Color.TRANSPARENT);
  @VisibleForTesting static final ColorDrawable selectedColor = new ColorDrawable(Color.RED);

  private Calendar selectedItem;

  @Override
  public void changeSelection(
      AdapterView<? extends MonthAdapter> parent, View view, int position, long row) {
    MonthAdapter adapter = parent.getAdapter();
    Calendar selection = adapter.getItem(position);
    if (!adapter.withinMonth(position)) {
      return;
    }
    selectedItem = selection;
  }

  @Override
  public void drawSelection(AdapterView<? extends MonthAdapter> parent) {
    MonthAdapter adapter = parent.getAdapter();
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
    ViewCompat.setBackground(cell, item.equals(selectedItem) ? selectedColor : emptyColor);
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
