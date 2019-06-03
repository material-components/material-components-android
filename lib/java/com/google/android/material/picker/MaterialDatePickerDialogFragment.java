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

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.util.Calendar;
import java.util.Locale;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar<Calendar>}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDatePickerDialogFragment extends MaterialPickerDialogFragment<Calendar> {

  public static MaterialDatePickerDialogFragment newInstance() {
    return newInstance(0);
  }

  public static MaterialDatePickerDialogFragment newInstance(int themeResId) {
    return newInstance(themeResId, MaterialPickerDialogFragment.DEFAULT_BOUNDS);
  }

  public static MaterialDatePickerDialogFragment newInstance(CalendarBounds calendarBounds) {
    return newInstance(0, calendarBounds);
  }

  public static MaterialDatePickerDialogFragment newInstance(
      int themeResId, CalendarBounds calendarBounds) {
    MaterialDatePickerDialogFragment materialDatePickerDialogFragment =
        new MaterialDatePickerDialogFragment();
    Bundle args = new Bundle();
    addArgsToBundle(args, themeResId, calendarBounds, R.string.mtrl_picker_date_header_title);
    materialDatePickerDialogFragment.setArguments(args);
    return materialDatePickerDialogFragment;
  }

  @Override
  protected int getDefaultThemeAttr() {
    return R.attr.materialCalendarTheme;
  }

  @Override
  protected DateGridSelector createGridSelector() {
    return new DateGridSelector();
  }

  @Override
  protected String getHeaderText(Calendar selection) {
    if (selection == null) {
      return getContext().getResources().getString(R.string.mtrl_picker_date_header_unselected);
    }
    String startString =
        getSimpleDateFormat() == null
            ? DateStrings.getYearMonthDay(selection.getTime(), Locale.getDefault())
            : getSimpleDateFormat().format(selection.getTime());
    return getContext()
        .getResources()
        .getString(R.string.mtrl_picker_date_header_selected, startString);
  }
}
