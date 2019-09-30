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
package com.google.android.material.datepicker;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Fragment for picking date(s) with text fields.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialTextInputPicker<S> extends PickerFragment<S> {

  private static final String DATE_SELECTOR_KEY = "DATE_SELECTOR_KEY";
  private static final String CALENDAR_CONSTRAINTS_KEY = "CALENDAR_CONSTRAINTS_KEY";

  @Nullable private DateSelector<S> dateSelector;
  @Nullable private CalendarConstraints calendarConstraints;

  @NonNull
  static <T> MaterialTextInputPicker<T> newInstance(
      @NonNull DateSelector<T> dateSelector, @NonNull CalendarConstraints calendarConstraints) {
    MaterialTextInputPicker<T> materialCalendar = new MaterialTextInputPicker<>();
    Bundle args = new Bundle();
    args.putParcelable(DATE_SELECTOR_KEY, dateSelector);
    args.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(DATE_SELECTOR_KEY, dateSelector);
    bundle.putParcelable(CALENDAR_CONSTRAINTS_KEY, calendarConstraints);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    dateSelector = activeBundle.getParcelable(DATE_SELECTOR_KEY);
    calendarConstraints = activeBundle.getParcelable(CALENDAR_CONSTRAINTS_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return dateSelector.onCreateTextInputView(
        layoutInflater,
        viewGroup,
        bundle,
        calendarConstraints,
        new OnSelectionChangedListener<S>() {
          @Override
          public void onSelectionChanged(S selection) {
            for (OnSelectionChangedListener<S> listener : onSelectionChangedListeners) {
              listener.onSelectionChanged(selection);
            }
          }
        });
  }

  @NonNull
  @Override
  public DateSelector<S> getDateSelector() {
    if (dateSelector == null) {
      throw new IllegalStateException(
          "dateSelector should not be null. Use MaterialTextInputPicker#newInstance() to create"
              + " this fragment with a DateSelector, and call this method after the fragment has"
              + " been created.");
    }
    return dateSelector;
  }
}
