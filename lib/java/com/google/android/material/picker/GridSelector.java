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

import android.graphics.Canvas;
import android.os.Bundle;
import android.os.Parcelable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import java.util.Calendar;

/**
 * Interface for users of {@link MaterialCalendar<S>} to control how the Calendar displays and
 * returns selections.
 *
 * <p>Implementors must implement {@link Parcelable} so that selection can be maintained through
 * Lifecycle events (e.g., Fragment destruction).
 *
 * @param <S> The type of item available when cells are selected in the {@link AdapterView}
 * @hide
 */
// TODO: Refactor into client-facing selection mode API
@RestrictTo(Scope.LIBRARY_GROUP)
public interface GridSelector<S> extends Parcelable {

  /** Returns the current selection */
  @Nullable
  S getSelection();

  /**
   * Allows this selection handler to respond to clicks within the {@link AdapterView}.
   *
   * @param selection The selected day
   */
  void select(Calendar selection);

  /** Adds a listener for selection changes. */
  boolean addOnSelectionChangedListener(OnSelectionChangedListener<S> listener);

  /** Removes a listener for selection changes. */
  boolean removeOnSelectionChangedListener(OnSelectionChangedListener<S> listener);

  /** Removes all listeners for selection changes. */
  void clearOnSelectionChangedListeners();

  /**
   * Modifies the provided {@link TextView} to indicate its selection status.
   *
   * <p>Called for each {@link TextView} as part of {@link MonthAdapter#getView(int, View,
   * ViewGroup)}
   *
   * @param view The {@link TextView} returned from {@link MonthAdapter#getView(int, View,
   *     ViewGroup)}
   * @param content The {@link Calendar} returned from {@link MonthAdapter#getItem(int)}.
   */
  void drawItem(TextView view, Calendar content);

  /**
   * Called after {@link
   * com.google.android.material.picker.MaterialCalendarGridView#onDraw(android.graphics.Canvas)} for
   * each month so selectors can draw on the canvas.
   */
  void onCalendarMonthDraw(Canvas canvas, MaterialCalendarGridView gridView);

  View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle);
}
