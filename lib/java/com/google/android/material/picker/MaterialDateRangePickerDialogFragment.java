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
import android.util.Pair;
import java.util.Calendar;

/**
 * A {@link Dialog} with a header, {@link MaterialDateRangePickerView}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDateRangePickerDialogFragment extends
    MaterialPickerDialogFragment<Pair<Calendar, Calendar>> {

  public static MaterialDateRangePickerDialogFragment newInstance() {
    return newInstance(0);
  }

  public static MaterialDateRangePickerDialogFragment newInstance(int themeResId) {
    MaterialDateRangePickerDialogFragment materialDateRangePickerDialogFragment =
        new MaterialDateRangePickerDialogFragment();
    Bundle args = new Bundle();
    addThemeToBundle(args, themeResId);
    materialDateRangePickerDialogFragment.setArguments(args);
    return materialDateRangePickerDialogFragment;
  }

  @Override
  protected int getDefaultThemeAttr() {
    return R.attr.materialDateRangePickerDialogTheme;
  }

  @Override
  protected MaterialCalendarView<Pair<Calendar, Calendar>> createMaterialCalendarView() {
    return new MaterialDateRangePickerView(getContext());
  }

  @Override
  protected String getHeaderText() {
    Pair<Calendar, Calendar> startAndEnd = getMaterialCalendarView().getSelection();
    if (startAndEnd == null) {
      return getContext().getResources().getString(R.string.mtrl_picker_range_header_prompt);
    }
    String startString = getSimpleDateFormat().format(startAndEnd.first.getTime());
    String endString = getSimpleDateFormat().format(startAndEnd.second.getTime());
    return getContext()
        .getResources()
        .getString(R.string.mtrl_picker_range_header_selected, startString, endString);
  }
}
