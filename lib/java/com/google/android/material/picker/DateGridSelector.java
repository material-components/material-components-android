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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.textfield.TextInputLayout;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import com.google.android.material.internal.ViewUtils;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

/**
 * A {@link GridSelector} that uses a {@link Long} for its selection state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateGridSelector implements GridSelector<Long> {

  @Nullable private Long selectedItem;

  @Override
  public void select(long selection) {
    selectedItem = selection;
  }

  @Override
  public Collection<Pair<Long, Long>> getSelectedRanges() {
    return new ArrayList<>();
  }

  @Override
  public Collection<Long> getSelectedDays() {
    ArrayList<Long> selections = new ArrayList<>();
    if (selectedItem != null) {
      selections.add(selectedItem);
    }
    return selections;
  }

  @Override
  @Nullable
  public Long getSelection() {
    return selectedItem;
  }

  @Override
  public View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      @NonNull OnSelectionChangedListener<Long> listener) {
    View root = layoutInflater.inflate(R.layout.mtrl_picker_text_input_date, viewGroup, false);

    TextInputLayout dateTextInput = root.findViewById(R.id.mtrl_picker_text_input_date);
    EditText dateEditText = dateTextInput.getEditText();

    SimpleDateFormat format =
        new SimpleDateFormat(
            root.getResources().getString(R.string.mtrl_picker_text_input_date_format),
            Locale.getDefault());

    if (selectedItem != null) {
      dateEditText.setText(format.format(selectedItem));
    }

    dateEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, dateTextInput) {
          @Override
          void onDateChanged(@Nullable Long day) {
            select(day);
            listener.onSelectionChanged(getSelection());
          }
        });

    ViewUtils.requestFocusAndShowKeyboard(dateEditText);

    return root;
  }

  @Override
  public int getDefaultThemeResId(Context context) {
    return MaterialAttributes.resolveOrThrow(
        context, R.attr.materialCalendarTheme, MaterialDatePicker.class.getCanonicalName());
  }

  @Override
  public String getSelectionDisplayString(Context context) {
    Resources res = context.getResources();
    if (selectedItem == null) {
      return res.getString(R.string.mtrl_picker_date_header_unselected);
    }
    String startString = DateStrings.getYearMonthDay(new Date(selectedItem), Locale.getDefault());
    return res.getString(R.string.mtrl_picker_date_header_selected, startString);
  }

  @Override
  public int getDefaultTitleResId() {
    return R.string.mtrl_picker_date_header_title;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<DateGridSelector> CREATOR =
      new Parcelable.Creator<DateGridSelector>() {
        @Override
        public DateGridSelector createFromParcel(Parcel source) {
          DateGridSelector dateGridSelector = new DateGridSelector();
          dateGridSelector.selectedItem = (Long) source.readValue(Long.class.getClassLoader());
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
    dest.writeValue(selectedItem);
  }
}
