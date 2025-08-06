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
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.InputType;
import android.text.SpannableString;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.util.Pair;
import androidx.core.util.Preconditions;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ManufacturerUtils;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link DateSelector} that uses a {@link Pair} of {@link Long} objects to represent a selected
 * range.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RangeDateSelector implements DateSelector<Pair<Long, Long>> {

  @Nullable private CharSequence error;
  private String invalidRangeStartError;
  // "" is not considered an error
  private final String invalidRangeEndError = " ";
  @Nullable private Long selectedStartItem = null;
  @Nullable private Long selectedEndItem = null;
  @Nullable private Long proposedTextStart = null;
  @Nullable private Long proposedTextEnd = null;

  @Nullable private SimpleDateFormat textInputFormat;

  @Override
  public void select(long selection) {
    if (selectedStartItem == null) {
      selectedStartItem = selection;
    } else if (selectedEndItem == null && isValidRange(selectedStartItem, selection)) {
      selectedEndItem = selection;
    } else {
      selectedEndItem = null;
      selectedStartItem = selection;
    }
  }

  @Override
  public boolean isSelectionComplete() {
    return selectedStartItem != null
        && selectedEndItem != null
        && isValidRange(selectedStartItem, selectedEndItem);
  }

  @Override
  public void setSelection(@NonNull Pair<Long, Long> selection) {
    if (selection.first != null && selection.second != null) {
      Preconditions.checkArgument(isValidRange(selection.first, selection.second));
    }
    selectedStartItem =
        selection.first == null ? null : UtcDates.canonicalYearMonthDay(selection.first);
    selectedEndItem =
        selection.second == null ? null : UtcDates.canonicalYearMonthDay(selection.second);
  }

  @Override
  @NonNull
  public Pair<Long, Long> getSelection() {
    return new Pair<>(selectedStartItem, selectedEndItem);
  }

  @NonNull
  @Override
  public Collection<Pair<Long, Long>> getSelectedRanges() {
    ArrayList<Pair<Long, Long>> ranges = new ArrayList<>();
    Pair<Long, Long> range = new Pair<>(selectedStartItem, selectedEndItem);
    ranges.add(range);
    return ranges;
  }

  @NonNull
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
  public int getDefaultThemeResId(@NonNull Context context) {
    Resources res = context.getResources();
    DisplayMetrics display = res.getDisplayMetrics();
    int maximumDefaultFullscreenMinorAxis =
        res.getDimensionPixelSize(R.dimen.mtrl_calendar_maximum_default_fullscreen_minor_axis);
    int minorAxisPx = Math.min(display.widthPixels, display.heightPixels);
    int defaultThemeAttr =
        minorAxisPx > maximumDefaultFullscreenMinorAxis
            ? R.attr.materialCalendarTheme
            : R.attr.materialCalendarFullscreenTheme;
    return MaterialAttributes.resolveOrThrow(
        context, defaultThemeAttr, MaterialDatePicker.class.getCanonicalName());
  }

  @NonNull
  @Override
  public String getSelectionDisplayString(@NonNull Context context) {
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

  @NonNull
  @Override
  public String getSelectionContentDescription(@NonNull Context context) {
    Resources res = context.getResources();
    Pair<String, String> dateRangeStrings =
        DateStrings.getDateRangeString(selectedStartItem, selectedEndItem);
    String startPlaceholder =
        dateRangeStrings.first == null
            ? res.getString(R.string.mtrl_picker_announce_current_selection_none)
            : dateRangeStrings.first;
    String endPlaceholder =
        dateRangeStrings.second == null
            ? res.getString(R.string.mtrl_picker_announce_current_selection_none)
            : dateRangeStrings.second;
    return res.getString(
        R.string.mtrl_picker_announce_current_range_selection, startPlaceholder, endPlaceholder);
  }

  @Nullable
  @Override
  public String getError() {
    return TextUtils.isEmpty(error) ? null : error.toString();
  }

  @Override
  public int getDefaultTitleResId() {
    return R.string.mtrl_picker_range_header_title;
  }

  @Override
  public void setTextInputFormat(@Nullable SimpleDateFormat format) {
    if (format != null) {
      format = (SimpleDateFormat) UtcDates.getNormalizedFormat(format);
    }

    this.textInputFormat = format;
  }

  @Override
  public View onCreateTextInputView(
      @NonNull LayoutInflater layoutInflater,
      @Nullable ViewGroup viewGroup,
      @Nullable Bundle bundle,
      CalendarConstraints constraints,
      final @NonNull OnSelectionChangedListener<Pair<Long, Long>> listener) {
    View root =
        layoutInflater.inflate(R.layout.mtrl_picker_text_input_date_range, viewGroup, false);

    final TextInputLayout startTextInput =
        root.findViewById(R.id.mtrl_picker_text_input_range_start);
    final TextInputLayout endTextInput = root.findViewById(R.id.mtrl_picker_text_input_range_end);
    EditText startEditText = startTextInput.getEditText();
    EditText endEditText = endTextInput.getEditText();
    Integer hintTextColor =
        MaterialColors.getColorOrNull(root.getContext(), R.attr.colorOnSurfaceVariant);
    if (hintTextColor != null) {
      startEditText.setHintTextColor(hintTextColor);
      endEditText.setHintTextColor(hintTextColor);
    }
    if (ManufacturerUtils.isDateInputKeyboardMissingSeparatorCharacters()) {
      // Using the URI variation places the '/' and '.' in more prominent positions
      startEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
      endEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    }

    invalidRangeStartError = root.getResources().getString(R.string.mtrl_picker_invalid_range);

    boolean hasCustomFormat = textInputFormat != null;
    SimpleDateFormat format =
        hasCustomFormat ? textInputFormat : UtcDates.getDefaultTextInputFormat();

    if (selectedStartItem != null) {
      startEditText.setText(format.format(selectedStartItem));
      proposedTextStart = selectedStartItem;
      // Move the cursor to the end of the text field
      CharSequence text = startEditText.getText();
      if (text != null) {
        startEditText.setSelection(text.length());
      }
    }
    if (selectedEndItem != null) {
      endEditText.setText(format.format(selectedEndItem));
      proposedTextEnd = selectedEndItem;
    }

    String formatHint =
        hasCustomFormat
            ? format.toPattern()
            : UtcDates.getDefaultTextInputHint(root.getResources(), format);

    SpannableString verbatimHint = UtcDates.getVerbatimTextInputHint(formatHint);

    startTextInput.setPlaceholderText(verbatimHint);
    endTextInput.setPlaceholderText(verbatimHint);

    startEditText.addTextChangedListener(
        new DateFormatTextWatcher(formatHint, format, startTextInput, constraints) {

          @Override
          void onValidDate(@Nullable Long day) {
            proposedTextStart = day;
            updateIfValidTextProposal(startTextInput, endTextInput, listener);
          }

          @Override
          void onInvalidDate() {
            proposedTextStart = null;
            updateIfValidTextProposal(startTextInput, endTextInput, listener);
          }
        });

    endEditText.addTextChangedListener(
        new DateFormatTextWatcher(formatHint, format, endTextInput, constraints) {

          @Override
          void onValidDate(@Nullable Long day) {
            proposedTextEnd = day;
            updateIfValidTextProposal(startTextInput, endTextInput, listener);
          }

          @Override
          void onInvalidDate() {
            proposedTextEnd = null;
            updateIfValidTextProposal(startTextInput, endTextInput, listener);
          }
        });

    // only show keyboard if touch exploration is disabled
    if (!DateSelector.isTouchExplorationEnabled(root.getContext())) {
      DateSelector.showKeyboardWithAutoHideBehavior(startEditText, endEditText);
    }

    return root;
  }

  private boolean isValidRange(long start, long end) {
    return start <= end;
  }

  private void updateIfValidTextProposal(
      @NonNull TextInputLayout startTextInput,
      @NonNull TextInputLayout endTextInput,
      @NonNull OnSelectionChangedListener<Pair<Long, Long>> listener) {
    if (proposedTextStart == null || proposedTextEnd == null) {
      clearInvalidRange(startTextInput, endTextInput);
      listener.onIncompleteSelectionChanged();
    } else if (isValidRange(proposedTextStart, proposedTextEnd)) {
      selectedStartItem = proposedTextStart;
      selectedEndItem = proposedTextEnd;
      listener.onSelectionChanged(getSelection());
    } else {
      setInvalidRange(startTextInput, endTextInput);
      listener.onIncompleteSelectionChanged();
    }
    updateError(startTextInput, endTextInput);
  }

  private void updateError(@NonNull TextInputLayout start, @NonNull TextInputLayout end) {
    if (!TextUtils.isEmpty(start.getError())) {
      error = start.getError();
    } else if (!TextUtils.isEmpty(end.getError())) {
      error = end.getError();
    } else {
      error = null;
    }
  }

  private void clearInvalidRange(@NonNull TextInputLayout start, @NonNull TextInputLayout end) {
    if (start.getError() != null && invalidRangeStartError.contentEquals(start.getError())) {
      start.setError(null);
    }
    if (end.getError() != null && invalidRangeEndError.contentEquals(end.getError())) {
      end.setError(null);
    }
  }

  private void setInvalidRange(@NonNull TextInputLayout start, @NonNull TextInputLayout end) {
    start.setError(invalidRangeStartError);
    end.setError(invalidRangeEndError);
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<RangeDateSelector> CREATOR =
      new Parcelable.Creator<RangeDateSelector>() {
        @NonNull
        @Override
        public RangeDateSelector createFromParcel(@NonNull Parcel source) {
          RangeDateSelector rangeDateSelector = new RangeDateSelector();
          rangeDateSelector.selectedStartItem =
              (Long) source.readValue(Long.class.getClassLoader());
          rangeDateSelector.selectedEndItem = (Long) source.readValue(Long.class.getClassLoader());
          return rangeDateSelector;
        }

        @NonNull
        @Override
        public RangeDateSelector[] newArray(int size) {
          return new RangeDateSelector[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeValue(selectedStartItem);
    dest.writeValue(selectedEndItem);
  }
}
