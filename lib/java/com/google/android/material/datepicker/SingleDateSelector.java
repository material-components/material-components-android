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
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.util.Pair;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ManufacturerUtils;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.textfield.TextInputLayout;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * A {@link DateSelector} that uses a {@link Long} for its selection state.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class SingleDateSelector implements DateSelector<Long> {

  @Nullable private CharSequence error;
  @Nullable private Long selectedItem;
  @Nullable private SimpleDateFormat textInputFormat;

  @Override
  public void select(long selection) {
    selectedItem = selection;
  }

  private void clearSelection() {
    selectedItem = null;
  }

  @Override
  public void setSelection(@Nullable Long selection) {
    selectedItem = selection == null ? null : UtcDates.canonicalYearMonthDay(selection);
  }

  @Override
  public boolean isSelectionComplete() {
    return selectedItem != null;
  }

  @NonNull
  @Override
  public Collection<Pair<Long, Long>> getSelectedRanges() {
    return new ArrayList<>();
  }

  @NonNull
  @Override
  public Collection<Long> getSelectedDays() {
    ArrayList<Long> selections = new ArrayList<>();
    if (selectedItem != null) {
      selections.add(selectedItem);
    }
    return selections;
  }

  @Override
  @Nullable
  public Long getSelection() {
    return selectedItem;
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
      final @NonNull OnSelectionChangedListener<Long> listener) {
    View root = layoutInflater.inflate(R.layout.mtrl_picker_text_input_date, viewGroup, false);

    TextInputLayout dateTextInput = root.findViewById(R.id.mtrl_picker_text_input_date);
    EditText dateEditText = dateTextInput.getEditText();
    Integer hintTextColor =
        MaterialColors.getColorOrNull(root.getContext(), R.attr.colorOnSurfaceVariant);
    if (hintTextColor != null) {
      dateEditText.setHintTextColor(hintTextColor);
    }
    if (ManufacturerUtils.isDateInputKeyboardMissingSeparatorCharacters()) {
      // Using the URI variation places the '/' and '.' in more prominent positions
      dateEditText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);
    }

    boolean hasCustomFormat = textInputFormat != null;
    SimpleDateFormat format =
        hasCustomFormat ? textInputFormat : UtcDates.getDefaultTextInputFormat();
    String formatHint =
        hasCustomFormat
            ? format.toPattern()
            : UtcDates.getDefaultTextInputHint(root.getResources(), format);

    dateTextInput.setPlaceholderText(UtcDates.getVerbatimTextInputHint(formatHint));
    if (selectedItem != null) {
      dateEditText.setText(format.format(selectedItem));
      // Move the cursor to the end of the text field
      CharSequence text = dateEditText.getText();
      if (text != null) {
        dateEditText.setSelection(text.length());
      }
    }

    dateEditText.addTextChangedListener(
        new DateFormatTextWatcher(formatHint, format, dateTextInput, constraints) {

          @Override
          void onValidDate(@Nullable Long day) {
            if (day == null) {
              clearSelection();
            } else {
              select(day);
            }
            error = null;
            listener.onSelectionChanged(getSelection());
          }

          @Override
          void onInvalidDate() {
            error = dateTextInput.getError();
            listener.onIncompleteSelectionChanged();
          }
        });

    // only show keyboard if touch exploration is disabled
    if (!DateSelector.isTouchExplorationEnabled(root.getContext())) {
      DateSelector.showKeyboardWithAutoHideBehavior(dateEditText);
    }

    return root;
  }

  @Override
  public int getDefaultThemeResId(Context context) {
    return MaterialAttributes.resolveOrThrow(
        context, R.attr.materialCalendarTheme, MaterialDatePicker.class.getCanonicalName());
  }

  @NonNull
  @Override
  public String getSelectionDisplayString(@NonNull Context context) {
    Resources res = context.getResources();
    if (selectedItem == null) {
      return res.getString(R.string.mtrl_picker_date_header_unselected);
    }
    String startString = DateStrings.getYearMonthDay(selectedItem);
    return res.getString(R.string.mtrl_picker_date_header_selected, startString);
  }

  @NonNull
  @Override
  public String getSelectionContentDescription(@NonNull Context context) {
    Resources res = context.getResources();
    String placeholder =
        selectedItem == null
            ? res.getString(R.string.mtrl_picker_announce_current_selection_none)
            : DateStrings.getYearMonthDay(selectedItem);
    return res.getString(R.string.mtrl_picker_announce_current_selection, placeholder);
  }

  @Nullable
  @Override
  public String getError() {
    return TextUtils.isEmpty(error) ? null : error.toString();
  }

  @Override
  public int getDefaultTitleResId() {
    return R.string.mtrl_picker_date_header_title;
  }

  /* Parcelable interface */

  /** {@link Parcelable.Creator} */
  public static final Parcelable.Creator<SingleDateSelector> CREATOR =
      new Parcelable.Creator<SingleDateSelector>() {
        @NonNull
        @Override
        public SingleDateSelector createFromParcel(@NonNull Parcel source) {
          SingleDateSelector singleDateSelector = new SingleDateSelector();
          singleDateSelector.selectedItem = (Long) source.readValue(Long.class.getClassLoader());
          return singleDateSelector;
        }

        @NonNull
        @Override
        public SingleDateSelector[] newArray(int size) {
          return new SingleDateSelector[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeValue(selectedItem);
  }
}
