/*
 * Copyright 2018 The Android Open Source Project
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
package io.material.catalog.picker;

import io.material.catalog.R;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import androidx.core.util.Pair;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import com.google.android.material.picker.CalendarConstraints;
import com.google.android.material.picker.DateValidatorPointForward;
import com.google.android.material.picker.MaterialDatePicker;
import com.google.android.material.picker.Month;
import io.material.catalog.feature.DemoFragment;
import java.util.Calendar;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class PickerMainDemoFragment extends DemoFragment {

  private Snackbar snackbar;
  private static final long TODAY;
  private static final long NEXT_MONTH;
  private static final Month JAN_THIS_YEAR;
  private static final Month DEC_THIS_YEAR;
  private static final Month ONE_YEAR_FORWARD;
  private static final Pair<Long, Long> TODAY_PAIR;
  private static final Pair<Long, Long> NEXT_MONTH_PAIR;

  static {
    Calendar calToday = Calendar.getInstance();
    TODAY = calToday.getTimeInMillis();
    Calendar calNextMonth = Calendar.getInstance();
    calNextMonth.roll(Calendar.MONTH, 1);
    NEXT_MONTH = calNextMonth.getTimeInMillis();

    Calendar calJanThisYear = Calendar.getInstance();
    calJanThisYear.set(Calendar.MONTH, Calendar.JANUARY);
    JAN_THIS_YEAR = Month.create(calJanThisYear.getTimeInMillis());
    Calendar calDecThisYear = Calendar.getInstance();
    calDecThisYear.set(Calendar.MONTH, Calendar.DECEMBER);
    DEC_THIS_YEAR = Month.create(calDecThisYear.getTimeInMillis());
    Calendar calOneYearForward = Calendar.getInstance();
    calOneYearForward.roll(Calendar.YEAR, 1);
    ONE_YEAR_FORWARD = Month.create(calOneYearForward.getTimeInMillis());

    TODAY_PAIR = new Pair<>(TODAY, TODAY);
    NEXT_MONTH_PAIR = new Pair<>(NEXT_MONTH, NEXT_MONTH);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.picker_main_demo, viewGroup, false);
    LinearLayout root = view.findViewById(R.id.picker_launcher_buttons_layout);
    MaterialButton launcher = root.findViewById(R.id.cat_picker_launch_button);

    snackbar = Snackbar.make(viewGroup, R.string.cat_picker_no_action, Snackbar.LENGTH_LONG);
    int dialogTheme = resolveOrThrow(getContext(), R.attr.materialCalendarTheme);
    int fullscreenTheme = resolveOrThrow(getContext(), R.attr.materialCalendarFullscreenTheme);

    final RadioGroup selectionMode = root.findViewById(R.id.cat_picker_date_selector_group);
    final RadioGroup theme = root.findViewById(R.id.cat_picker_theme_group);
    final RadioGroup bounds = root.findViewById(R.id.cat_picker_bounds_group);
    final RadioGroup validation = root.findViewById(R.id.cat_picker_validation_group);
    final RadioGroup title = root.findViewById(R.id.cat_picker_title_group);
    final RadioGroup opening = root.findViewById(R.id.cat_picker_opening_month_group);
    final RadioGroup selection = root.findViewById(R.id.cat_picker_selection_group);

    launcher.setOnClickListener(
        v -> {
          int selectionModeChoice = selectionMode.getCheckedRadioButtonId();
          int themeChoice = theme.getCheckedRadioButtonId();
          int boundsChoice = bounds.getCheckedRadioButtonId();
          int validationChoice = validation.getCheckedRadioButtonId();
          int titleChoice = title.getCheckedRadioButtonId();
          int openingChoice = opening.getCheckedRadioButtonId();
          int selectionChoice = selection.getCheckedRadioButtonId();

          MaterialDatePicker.Builder<?> builder =
              setupDateSelectorBuilder(selectionModeChoice, selectionChoice);
          CalendarConstraints.Builder constraintsBuilder =
              setupConstraintsBuilder(boundsChoice, openingChoice, validationChoice);

          if (themeChoice == R.id.cat_picker_theme_dialog) {
            builder.setTheme(dialogTheme);
          } else if (themeChoice == R.id.cat_picker_theme_fullscreen) {
            builder.setTheme(fullscreenTheme);
          }

          if (titleChoice == R.id.cat_picker_title_custom) {
            builder.setTitleTextResId(R.string.cat_picker_title_custom);
          }

          try {
            builder.setCalendarConstraints(constraintsBuilder.build());
            MaterialDatePicker<?> picker = builder.build();
            addSnackBarListeners(picker);
            picker.show(getFragmentManager(), picker.toString());
          } catch (IllegalArgumentException e) {
            snackbar.setText(e.getMessage());
            snackbar.show();
          }
        });

    return view;
  }

  private static MaterialDatePicker.Builder<?> setupDateSelectorBuilder(
      int selectionModeChoice, int selectionChoice) {
    if (selectionModeChoice == R.id.cat_picker_date_selector_single) {
      MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
      if (selectionChoice == R.id.cat_picker_selection_today) {
        builder.setSelection(TODAY);
      } else if (selectionChoice == R.id.cat_picker_selection_next_month) {
        builder.setSelection(NEXT_MONTH);
      }
      return builder;
    } else {
      MaterialDatePicker.Builder<Pair<Long, Long>> builder =
          MaterialDatePicker.Builder.dateRangePicker();
      if (selectionChoice == R.id.cat_picker_selection_today) {
        builder.setSelection(TODAY_PAIR);
      } else if (selectionChoice == R.id.cat_picker_selection_next_month) {
        builder.setSelection(NEXT_MONTH_PAIR);
      }
      return builder;
    }
  }

  private static CalendarConstraints.Builder setupConstraintsBuilder(
      int boundsChoice, int openingChoice, int validationChoice) {
    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
    if (boundsChoice == R.id.cat_picker_bounds_this_year) {
      constraintsBuilder.setStart(JAN_THIS_YEAR);
      constraintsBuilder.setEnd(DEC_THIS_YEAR);
    } else if (boundsChoice == R.id.cat_picker_bounds_one_year_forward) {
      constraintsBuilder.setEnd(ONE_YEAR_FORWARD);
    }

    if (openingChoice == R.id.cat_picker_opening_month_today) {
      constraintsBuilder.setOpening(Month.today());
    } else if (openingChoice == R.id.cat_picker_opening_month_next) {
      constraintsBuilder.setOpening(Month.create(NEXT_MONTH));
    }

    if (validationChoice == R.id.cat_picker_validation_future) {
      constraintsBuilder.setValidator(new DateValidatorPointForward());
    } else if (validationChoice == R.id.cat_picker_validation_weekdays) {
      constraintsBuilder.setValidator(new DateValidatorWeekdays());
    }
    return constraintsBuilder;
  }

  private static int resolveOrThrow(Context context, @AttrRes int attributeResId) {
    TypedValue typedValue = new TypedValue();
    if (context.getTheme().resolveAttribute(attributeResId, typedValue, true)) {
      return typedValue.data;
    }
    throw new IllegalArgumentException(context.getResources().getResourceName(attributeResId));
  }

  private void addSnackBarListeners(MaterialDatePicker<?> materialCalendarPicker) {
    materialCalendarPicker.addOnPositiveButtonClickListener(
        selection -> {
          snackbar.setText(materialCalendarPicker.getHeaderText());
          snackbar.show();
        });
    materialCalendarPicker.addOnNegativeButtonClickListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_user_clicked_cancel);
          snackbar.show();
        });
    materialCalendarPicker.addOnCancelListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_cancel);
          snackbar.show();
        });
  }
}
