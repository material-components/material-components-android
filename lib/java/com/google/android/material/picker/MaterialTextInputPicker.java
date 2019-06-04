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

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Fragment for picking date(s) with text fields.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public final class MaterialTextInputPicker<S> extends PickerFragment<S> {

  private static final String GRID_SELECTOR_KEY = "GRID_SELECTOR_KEY";
  private static final String CALENDAR_BOUNDS_KEY = "CALENDAR_BOUNDS_KEY";

  private GridSelector<S> gridSelector;
  // TODO: make use of calendarBounds or delete
  private CalendarBounds calendarBounds;

  /**
   * Creates a {@link MaterialTextInputPicker} with {@link GridSelector#drawItem(TextView,
   * Calendar)} applied to each cell.
   *
   * @param gridSelector Controls the highlight state of the {@link MaterialTextInputPicker}
   * @param <T> Type of {@link GridSelector} returned from selections in this {@link
   *     MaterialTextInputPicker} by {@link MaterialTextInputPicker#getGridSelector()}
   */
  public static <T> MaterialTextInputPicker<T> newInstance(
      GridSelector<T> gridSelector, CalendarBounds calendarBounds) {
    MaterialTextInputPicker<T> materialCalendar = new MaterialTextInputPicker<>();
    Bundle args = new Bundle();
    args.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    args.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
    materialCalendar.setArguments(args);
    return materialCalendar;
  }

  @Override
  public void onSaveInstanceState(@NonNull Bundle bundle) {
    super.onSaveInstanceState(bundle);
    bundle.putParcelable(GRID_SELECTOR_KEY, gridSelector);
    bundle.putParcelable(CALENDAR_BOUNDS_KEY, calendarBounds);
  }

  @Override
  public void onCreate(@Nullable Bundle bundle) {
    super.onCreate(bundle);
    Bundle activeBundle = bundle == null ? getArguments() : bundle;
    gridSelector = activeBundle.getParcelable(GRID_SELECTOR_KEY);
    calendarBounds = activeBundle.getParcelable(CALENDAR_BOUNDS_KEY);
  }

  @NonNull
  @Override
  public View onCreateView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle) {
    return gridSelector.onCreateTextInputView(layoutInflater, viewGroup, bundle);
  }

  @Override
  public GridSelector<S> getGridSelector() {
    return gridSelector;
  }
}
