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
import java.util.Locale;

/**
 * A {@link GridSelector} that uses a {@link Pair} of {@link Long} objects to represent a selected
 * range.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class DateRangeGridSelector implements GridSelector<Pair<Long, Long>> {

  @Nullable private Long selectedStartItem = null;
  @Nullable private Long selectedEndItem = null;

  @Override
  public void select(long selection) {
    if (selectedStartItem == null) {
      selectedStartItem = selection;
    } else if (selectedEndItem == null
        && (selection > selectedStartItem || selection == selectedStartItem)) {
      selectedEndItem = selection;
    } else {
      selectedEndItem = null;
      selectedStartItem = selection;
    }
  }

  @Override
  @NonNull
  public Pair<Long, Long> getSelection() {
    return new Pair<>(selectedStartItem, selectedEndItem);
  }

  @Override
  public Collection<Pair<Long, Long>> getSelectedRanges() {
    if (selectedStartItem == null || selectedEndItem == null) {
      return new ArrayList<>();
    }
    ArrayList<Pair<Long, Long>> ranges = new ArrayList<>();
    Pair<Long, Long> range = new Pair<>(selectedStartItem, selectedEndItem);
    ranges.add(range);
    return ranges;
  }

  @Override
  public Collection<Long> getSelectedDays() {
    ArrayList<Long> selections = new ArrayList<>();
    if (selectedStartItem != null) {
      selections.add(selectedStartItem);
    }
    if (selectedEndItem != null) {
      selections.add(selectedEndItem);
    }
    return selections;
  }

  @Override
  public int getDefaultThemeResId(Context context) {
    Resources res = context.getResources();
    int maximumDefaultFullscreenWidth =
        res.getDimensionPixelSize(R.dimen.mtrl_calendar_maximum_default_fullscreen_width);
    int defaultThemeAttr =
        res.getDisplayMetrics().widthPixels > maximumDefaultFullscreenWidth
            ? R.attr.materialCalendarTheme
            : R.attr.materialCalendarFullscreenTheme;
    return MaterialAttributes.resolveOrThrow(
        context, defaultThemeAttr, MaterialDatePicker.class.getCanonicalName());
  }

  @Override
  public String getSelectionDisplayString(Context context) {
    Resources res = context.getResources();
    if (selectedStartItem == null && selectedEndItem == null) {
      return res.getString(R.string.mtrl_picker_range_header_unselected);
    }
    if (selectedEndItem == null) {
      return res.getString(
          R.string.mtrl_picker_range_header_only_start_selected,
          DateStrings.getDateString(selectedStartItem));
    }
    if (selectedStartItem == null) {
      return res.getString(
          R.string.mtrl_picker_range_header_only_end_selected,
          DateStrings.getDateString(selectedEndItem));
    }
    Pair<String, String> dateRangeStrings =
        DateStrings.getDateRangeString(selectedStartItem, selectedEndItem);
    return res.getString(
        R.string.mtrl_picker_range_header_selected,
        dateRangeStrings.first,
        dateRangeStrings.second);
  }

  @Override
  public int getDefaultTitleResId() {
    return R.string.mtrl_picker_range_header_title;
  }

  @Override
  public View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      @NonNull OnSelectionChangedListener<Pair<Long, Long>> listener) {
    View root =
        layoutInflater.inflate(R.layout.mtrl_picker_text_input_date_range, viewGroup, false);

    TextInputLayout startTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_start);
    TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    EditText startEditText = startTextInput.getEditText();
    EditText endEditText = endTextInput.getEditText();

    SimpleDateFormat format =
        new SimpleDateFormat(
            root.getResources().getString(R.string.mtrl_picker_text_input_date_format),
            Locale.getDefault());

    if (selectedStartItem != null) {
      startEditText.setText(format.format(selectedStartItem));
    }
    if (selectedEndItem != null) {
      endEditText.setText(format.format(selectedEndItem));
    }

    // TODO: handle start/end behavior enforcement
    startEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, startTextInput) {
          @Override
          void onDateChanged(@Nullable Long day) {
            selectedStartItem = day;
            listener.onSelectionChanged(getSelection());
          }
        });
    endEditText.addTextChangedListener(
        new DateFormatTextWatcher(format, endTextInput) {
          @Override
          void onDateChanged(@Nullable Long day) {
            selectedEndItem = day;
            listener.onSelectionChanged(getSelection());
          }
        });

    ViewUtils.requestFocusAndShowKeyboard(startEditText);

    return root;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<DateRangeGridSelector> CREATOR =
      new Parcelable.Creator<DateRangeGridSelector>() {
        @Override
        public DateRangeGridSelector createFromParcel(Parcel source) {
          DateRangeGridSelector dateRangeGridSelector = new DateRangeGridSelector();
          dateRangeGridSelector.selectedStartItem =
              (Long) source.readValue(Long.class.getClassLoader());
          dateRangeGridSelector.selectedEndItem =
              (Long) source.readValue(Long.class.getClassLoader());
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
    dest.writeValue(selectedStartItem);
    dest.writeValue(selectedEndItem);
  }
}
