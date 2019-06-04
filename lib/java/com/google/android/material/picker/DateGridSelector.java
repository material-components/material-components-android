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
import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.internal.ViewUtils;
import com.google.android.material.textfield.TextInputLayout;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashSet;
import java.util.Locale;

/**
 * A {@link GridSelector} that uses a {@link Calendar} for its selection state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateGridSelector implements GridSelector<Calendar> {

  private final LinkedHashSet<OnSelectionChangedListener<Calendar>> onSelectionChangedListeners =
      new LinkedHashSet<>();

  @Nullable private Calendar selectedItem;
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
    GridSelectors.notifyListeners(this, onSelectionChangedListeners);
  }

  @Override
  public boolean addOnSelectionChangedListener(OnSelectionChangedListener<Calendar> listener) {
    return onSelectionChangedListeners.add(listener);
  }

  @Override
  public boolean removeOnSelectionChangedListener(OnSelectionChangedListener<Calendar> listener) {
    return onSelectionChangedListeners.remove(listener);
  }

  @Override
  public void clearOnSelectionChangedListeners() {
    onSelectionChangedListeners.clear();
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

  @Override
  public View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    View root = layoutInflater.inflate(R.layout.mtrl_picker_text_input_date, viewGroup, false);

    TextInputLayout dateTextInput = root.findViewById(R.id.mtrl_picker_text_input_date);
    EditText dateEditText = dateTextInput.getEditText();

    SimpleDateFormat format =
        new SimpleDateFormat(
            root.getResources().getString(R.string.mtrl_picker_text_input_date_format),
            Locale.getDefault());

    if (selectedItem != null) {
      dateEditText.setText(format.format(selectedItem.getTime()));
    }

    dateEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, dateTextInput) {
          @Override
          void onDateChanged(@Nullable Calendar calendar) {
            select(calendar);
          }
        });

    ViewUtils.requestFocusAndShowKeyboard(dateEditText);

    return root;
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
