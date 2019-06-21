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
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.util.Pair;

/**
 * A {@link Dialog} with a header, {@link MaterialCalendar<Pair<Long, Long>>}, and set of actions.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDateRangePickerDialogFragment
    extends MaterialPickerDialogFragment<Pair<Long, Long>> {

  public static MaterialDateRangePickerDialogFragment newInstance() {
    return newInstance(0);
  }

  public static MaterialDateRangePickerDialogFragment newInstance(int themeResId) {
    return newInstance(themeResId, MaterialPickerDialogFragment.DEFAULT_BOUNDS);
  }

  public static MaterialDateRangePickerDialogFragment newInstance(CalendarBounds calendarBounds) {
    return newInstance(0, calendarBounds);
  }

  public static MaterialDateRangePickerDialogFragment newInstance(
      int themeResId, CalendarBounds calendarBounds) {
    MaterialDateRangePickerDialogFragment materialDateRangePickerDialogFragment =
        new MaterialDateRangePickerDialogFragment();
    Bundle args = new Bundle();
    addArgsToBundle(args, themeResId, calendarBounds, R.string.mtrl_picker_range_header_title);
    materialDateRangePickerDialogFragment.setArguments(args);
    return materialDateRangePickerDialogFragment;
  }

  @Override
  protected int getDefaultThemeAttr() {
    int maximumDefaultFullscreenWidth =
        getResources()
            .getDimensionPixelSize(R.dimen.mtrl_calendar_maximum_default_fullscreen_width);
    return getResources().getDisplayMetrics().widthPixels > maximumDefaultFullscreenWidth
        ? R.attr.materialCalendarTheme
        : R.attr.materialCalendarFullscreenTheme;
  }

  @Override
  protected DateRangeGridSelector createGridSelector() {
    return new DateRangeGridSelector();
  }

  @Override
  public String getHeaderText(Pair<Long, Long> selection) {
    Pair<String, String> dateRangeStrings = null;
    if (selection != null) {
      dateRangeStrings =
          DateStrings.getDateRangeString(selection.first, selection.second, getSimpleDateFormat());
    }

    if (dateRangeStrings == null
        || (dateRangeStrings.first == null && dateRangeStrings.second == null)) {
      return getContext().getResources().getString(R.string.mtrl_picker_range_header_unselected);
    } else if (dateRangeStrings.second == null) {
      return getContext()
          .getResources()
          .getString(R.string.mtrl_picker_range_header_only_start_selected, dateRangeStrings.first);
    } else {
      return getContext()
          .getResources()
          .getString(
              R.string.mtrl_picker_range_header_selected,
              dateRangeStrings.first,
              dateRangeStrings.second);
    }
  }

  /** Returns the start date for the selection currently selected by the user. */
  @Nullable
  public Long getStart() {
    return getSelection() == null ? null : getSelection().first;
  }

  /** Returns the end date for the selection currently selected by the user. */
  @Nullable
  public Long getEnd() {
    return getSelection() == null ? null : getSelection().second;
  }
}
