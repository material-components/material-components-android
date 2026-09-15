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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.widget.AdapterView;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import androidx.core.util.Pair;
import com.google.android.material.internal.ViewUtils;
import java.text.SimpleDateFormat;
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
   * <p>Uses {@code R.styleable.MaterialCalendar_daySelectedStyle} for styling.
   */
  @NonNull
  Collection<Long> getSelectedDays();

  /**
   * Returns a list of ranges whose time values represent ranges that should be filled.
   *
   * <p>Uses {@code R.styleable.MaterialCalendar_rangeFillColor} for styling.
   */
  @NonNull
  Collection<Pair<Long, Long>> getSelectedRanges();

  @NonNull
  String getSelectionDisplayString(Context context);

  /**
   * Returns the selection content description.
   *
   * @param context the {@link Context}
   * @return The selection content description
   */
  @NonNull
  String getSelectionContentDescription(@NonNull Context context);

  @Nullable
  String getError();

  @StringRes
  int getDefaultTitleResId();

  @StyleRes
  int getDefaultThemeResId(Context context);

  /**
   * Sets the {@link SimpleDateFormat} used to format the text input field hint and error.
   *
   * <p>When this is set to null, a default formatter will be used that properly adjusts for
   * language and locale.
   *
   * @param format The format to be used when formatting the text input field
   */
  void setTextInputFormat(@Nullable SimpleDateFormat format);

  @NonNull
  View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      @NonNull CalendarConstraints constraints,
      @NonNull OnSelectionChangedListener<S> listener);

  static void showKeyboardWithAutoHideBehavior(@NonNull EditText... editTexts) {
    if (editTexts.length == 0) {
      return;
    }

    View.OnFocusChangeListener listener =
        (view, hasFocus) -> {
          for (EditText editText : editTexts) {
            if (editText.hasFocus()) {
              return;
            }
          }
          ViewUtils.hideKeyboard(view, /* useWindowInsetsController= */ false);
        };

    for (EditText editText : editTexts) {
      editText.setOnFocusChangeListener(listener);
    }

    // TODO(b/246354286): Investigate issue with keyboard not showing on Android 12+
    View viewToFocus = editTexts[0];
    viewToFocus.postDelayed(
        () ->
            ViewUtils.requestFocusAndShowKeyboard(
                viewToFocus, /* useWindowInsetsController= */ false),
        100);
  }

  static boolean isTouchExplorationEnabled(@NonNull Context context) {
    AccessibilityManager accessibilityManager =
        (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
    return accessibilityManager != null && accessibilityManager.isTouchExplorationEnabled();
  }
}
