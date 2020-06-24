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

import com.google.android.material.R;

import android.content.Context;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.core.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import java.util.Collection;

/**
 * Interface for users of {@link MaterialCalendar<S>} to control how the Calendar displays and
 * returns selections.
 *
 * <p>Implementors must implement {@link Parcelable} so that selection can be maintained through
 * Lifecycle events (e.g., Fragment destruction).
 *
 * <p>Dates are represented as times in UTC milliseconds.
 *
 * @param <S> The type of item available when cells are selected in the {@link AdapterView}
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public interface DateSelector<S> extends Parcelable {

  /** Returns the current selection. */
  @Nullable
  S getSelection();

  /** Returns true if the current selection is acceptable. */
  boolean isSelectionComplete();

  /**
   * Sets the current selection to {@code selection}.
   *
   * @throws IllegalArgumentException If {@code selection} creates an invalid state.
   */
  void setSelection(@NonNull S selection);

  /**
   * Allows this selection handler to respond to clicks within the {@link AdapterView}.
   *
   * @param selection The selected day represented as time in UTC milliseconds.
   */
  void select(long selection);

  /**
   * Returns a list of longs whose time value represents days that should be marked selected.
   *
   * <p>Uses {@link R.styleable#MaterialCalendar_daySelectedStyle} for styling.
   */
  @NonNull
  Collection<Long> getSelectedDays();

  /**
   * Returns a list of ranges whose time values represent ranges that should be filled.
   *
   * <p>Uses {@link R.styleable#MaterialCalendar_rangeFillColor} for styling.
   */
  @NonNull
  Collection<Pair<Long, Long>> getSelectedRanges();

  @NonNull
  String getSelectionDisplayString(Context context);

  @StringRes
  int getDefaultTitleResId();

  @StyleRes
  int getDefaultThemeResId(Context context);

  @NonNull
  View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      @NonNull CalendarConstraints constraints,
      @NonNull OnSelectionChangedListener<S> listener);
}
