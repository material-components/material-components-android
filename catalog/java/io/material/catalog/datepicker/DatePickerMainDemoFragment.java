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
package io.material.catalog.datepicker;

import io.material.catalog.R;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import androidx.annotation.AttrRes;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.CompositeDateValidator;
import com.google.android.material.datepicker.DateValidatorPointBackward;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.snackbar.Snackbar;
import io.material.catalog.feature.DemoFragment;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class DatePickerMainDemoFragment extends DemoFragment {

  private Snackbar snackbar;
  private long today;
  private long nextMonth;
  private long janThisYear;
  private long decThisYear;
  private long oneYearForward;
  private Pair<Long, Long> todayPair;
  private Pair<Long, Long> nextMonthPair;

  private static Calendar getClearedUtc() {
    Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    utc.clear();
    return utc;
  }

  private void initSettings() {
    today = MaterialDatePicker.todayInUtcMilliseconds();
    Calendar calendar = getClearedUtc();
    calendar.setTimeInMillis(today);
    calendar.add(Calendar.MONTH, 1);
    nextMonth = calendar.getTimeInMillis();

    calendar.setTimeInMillis(today);
    calendar.set(Calendar.MONTH, Calendar.JANUARY);
    janThisYear = calendar.getTimeInMillis();
    calendar.setTimeInMillis(today);
    calendar.set(Calendar.MONTH, Calendar.DECEMBER);
    decThisYear = calendar.getTimeInMillis();

    calendar.setTimeInMillis(today);
    calendar.add(Calendar.YEAR, 1);
    oneYearForward = calendar.getTimeInMillis();

    todayPair = new Pair<>(today, today);
    nextMonthPair = new Pair<>(nextMonth, nextMonth);
  }

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.picker_main_demo, viewGroup, false);
    LinearLayout root = view.findViewById(R.id.picker_launcher_buttons_layout);
    MaterialButton launcher = root.findViewById(R.id.cat_picker_launch_button);

    snackbar = Snackbar.make(viewGroup, R.string.cat_picker_no_action, Snackbar.LENGTH_LONG);
    int dialogTheme = resolveOrThrow(getContext(), com.google.android.material.R.attr.materialCalendarTheme);
    int fullscreenTheme = resolveOrThrow(getContext(), com.google.android.material.R.attr.materialCalendarFullscreenTheme);

    final RadioGroup selectionMode = root.findViewById(R.id.cat_picker_date_selector_group);
    final RadioGroup theme = root.findViewById(R.id.cat_picker_theme_group);
    final RadioGroup bounds = root.findViewById(R.id.cat_picker_bounds_group);
    final RadioGroup validation = root.findViewById(R.id.cat_picker_validation_group);
    final RadioGroup title = root.findViewById(R.id.cat_picker_title_group);
    final RadioGroup opening = root.findViewById(R.id.cat_picker_opening_month_group);
    final RadioGroup selection = root.findViewById(R.id.cat_picker_selection_group);
    final RadioGroup inputMode = root.findViewById(R.id.cat_picker_input_mode_group);
    final RadioGroup dayViewDecoratorGroup =
        root.findViewById(R.id.cat_picker_day_view_decorator_group);
    final RadioGroup positiveButton = root.findViewById(R.id.cat_picker_positive_button_group);
    final RadioGroup negativeButton = root.findViewById(R.id.cat_picker_negative_button_group);

    launcher.setOnClickListener(
        v -> {
          initSettings();
          int selectionModeChoice = selectionMode.getCheckedRadioButtonId();
          int themeChoice = theme.getCheckedRadioButtonId();
          int boundsChoice = bounds.getCheckedRadioButtonId();
          int validationChoice = validation.getCheckedRadioButtonId();
          int titleChoice = title.getCheckedRadioButtonId();
          int openingChoice = opening.getCheckedRadioButtonId();
          int selectionChoice = selection.getCheckedRadioButtonId();
          int inputModeChoices = inputMode.getCheckedRadioButtonId();
          int dayViewDecoratorChoice = dayViewDecoratorGroup.getCheckedRadioButtonId();
          int positiveButtonChoice = positiveButton.getCheckedRadioButtonId();
          int negativeButtonChoice = negativeButton.getCheckedRadioButtonId();

          MaterialDatePicker.Builder<?> builder =
              setupDateSelectorBuilder(selectionModeChoice, selectionChoice, inputModeChoices);
          CalendarConstraints.Builder constraintsBuilder =
              setupConstraintsBuilder(boundsChoice, openingChoice, validationChoice);

          if (themeChoice == R.id.cat_picker_theme_dialog) {
            builder.setTheme(dialogTheme);
          } else if (themeChoice == R.id.cat_picker_theme_fullscreen) {
            builder.setTheme(fullscreenTheme);
          } else if (themeChoice == R.id.cat_picker_theme_custom) {
            builder.setTheme(R.style.ThemeOverlay_Catalog_MaterialCalendar_Custom);
          }

          if (titleChoice == R.id.cat_picker_title_custom) {
            builder.setTitleText(R.string.cat_picker_title_custom);
          } else if (titleChoice == R.id.cat_picker_title_with_description) {
            builder.setTheme(R.style.ThemeOverlay_Catalog_MaterialCalendar_WithDescription);
            builder.setTitleText(getTitleWithDescription());
          }

          if (positiveButtonChoice == R.id.cat_picker_positive_button_custom) {
            builder.setPositiveButtonText(R.string.cat_picker_positive_button_text);
            builder.setPositiveButtonContentDescription(
                R.string.cat_picker_positive_button_content_description);
          }

          if (negativeButtonChoice == R.id.cat_picker_negative_button_custom) {
            builder.setNegativeButtonText(R.string.cat_picker_negative_button_text);
            builder.setNegativeButtonContentDescription(
                R.string.cat_picker_negative_button_content_description);
          }

          setupDayViewDecorator(builder, dayViewDecoratorChoice);

          try {
            builder.setCalendarConstraints(constraintsBuilder.build());
            MaterialDatePicker<?> picker = builder.build();
            addSnackBarListeners(picker);
            picker.show(getChildFragmentManager(), picker.toString());
          } catch (IllegalArgumentException e) {
            snackbar.setText(e.getMessage());
            snackbar.show();
          }
        });

    return view;
  }

  private MaterialDatePicker.Builder<?> setupDateSelectorBuilder(
      int selectionModeChoice, int selectionChoice, int inputModeChoice) {

    int inputMode =
        inputModeChoice == R.id.cat_picker_input_mode_calendar
            ? MaterialDatePicker.INPUT_MODE_CALENDAR
            : MaterialDatePicker.INPUT_MODE_TEXT;

    if (selectionModeChoice == R.id.cat_picker_date_selector_single) {
      MaterialDatePicker.Builder<Long> builder = MaterialDatePicker.Builder.datePicker();
      if (selectionChoice == R.id.cat_picker_selection_today) {
        builder.setSelection(today);
      } else if (selectionChoice == R.id.cat_picker_selection_next_month) {
        builder.setSelection(nextMonth);
      }
      builder.setInputMode(inputMode);
      return builder;
    } else {
      MaterialDatePicker.Builder<Pair<Long, Long>> builder =
          MaterialDatePicker.Builder.dateRangePicker();
      if (selectionChoice == R.id.cat_picker_selection_today) {
        builder.setSelection(todayPair);
      } else if (selectionChoice == R.id.cat_picker_selection_next_month) {
        builder.setSelection(nextMonthPair);
      }
      builder.setInputMode(inputMode);
      return builder;
    }
  }

  private CalendarConstraints.Builder setupConstraintsBuilder(
      int boundsChoice, int openingChoice, int validationChoice) {
    CalendarConstraints.Builder constraintsBuilder = new CalendarConstraints.Builder();
    if (boundsChoice == R.id.cat_picker_bounds_this_year) {
      constraintsBuilder.setStart(janThisYear);
      constraintsBuilder.setEnd(decThisYear);
    } else if (boundsChoice == R.id.cat_picker_bounds_one_year_forward) {
      constraintsBuilder.setStart(today);
      constraintsBuilder.setEnd(oneYearForward);
    }

    if (openingChoice == R.id.cat_picker_opening_month_today) {
      constraintsBuilder.setOpenAt(today);
    } else if (openingChoice == R.id.cat_picker_opening_month_next) {
      constraintsBuilder.setOpenAt(nextMonth);
    }

    if (validationChoice == R.id.cat_picker_validation_today_onward) {
      constraintsBuilder.setValidator(DateValidatorPointForward.now());
    } else if (validationChoice == R.id.cat_picker_validation_weekdays) {
      constraintsBuilder.setValidator(new DateValidatorWeekdays());
    } else if ((validationChoice == R.id.cat_picker_validation_last_two_weeks)) {
      Calendar lowerBoundCalendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      lowerBoundCalendar.add(Calendar.DAY_OF_MONTH, -14);
      long lowerBound = lowerBoundCalendar.getTimeInMillis();

      List<CalendarConstraints.DateValidator> validators = new ArrayList<>();
      validators.add(DateValidatorPointForward.from(lowerBound));
      validators.add(DateValidatorPointBackward.now());

      constraintsBuilder.setValidator(CompositeDateValidator.allOf(validators));
    } else if ((validationChoice == R.id.cat_picker_validation_multiple_range)) {
      List<CalendarConstraints.DateValidator> validatorsMultple = new ArrayList<>();
      Calendar utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
      utc.setTimeInMillis(today);
      utc.set(Calendar.DATE, 10);
      DateValidatorPointBackward pointBackward =
          DateValidatorPointBackward.before(utc.getTimeInMillis());
      utc.set(Calendar.DATE, 20);

      List<CalendarConstraints.DateValidator> validatorsComposite = new ArrayList<>();
      DateValidatorPointForward pointForwardComposite =
          DateValidatorPointForward.from(utc.getTimeInMillis());
      utc.set(Calendar.DATE, 26);
      DateValidatorPointBackward pointBackwardComposite =
          DateValidatorPointBackward.before(utc.getTimeInMillis());
      validatorsComposite.add(pointForwardComposite);
      validatorsComposite.add(pointBackwardComposite);
      CalendarConstraints.DateValidator compositeDateValidator =
          CompositeDateValidator.allOf(validatorsComposite);

      validatorsMultple.add(pointBackward);
      validatorsMultple.add(compositeDateValidator);
      constraintsBuilder.setValidator(CompositeDateValidator.anyOf(validatorsMultple));
    }
    return constraintsBuilder;
  }

  private CharSequence getTitleWithDescription() {
    Context context = requireContext();
    String alarmTimes = context.getString(R.string.cat_picker_title_description_colored);
    String titleAndDescriptionText =
        context.getString(R.string.cat_picker_title_description_main) + alarmTimes;
    SpannableString spannable = new SpannableString(titleAndDescriptionText);
    int alarmTimesColor = resolveOrThrow(context, androidx.appcompat.R.attr.colorPrimary);
    int spanStart = titleAndDescriptionText.indexOf(alarmTimes);
    int spanEnd = spanStart + alarmTimes.length();
    spannable.setSpan(
        new ForegroundColorSpan(alarmTimesColor),
        spanStart,
        spanEnd,
        Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    spannable.setSpan(
        new StyleSpan(Typeface.BOLD), spanStart, spanEnd, Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
    return spannable;
  }

  private void setupDayViewDecorator(
      MaterialDatePicker.Builder<?> builder, int dayViewDecoratorChoice) {
    if (dayViewDecoratorChoice == R.id.cat_picker_day_view_decorator_dots) {
      builder.setDayViewDecorator(new CircleIndicatorDecorator());
    } else if (dayViewDecoratorChoice == R.id.cat_picker_day_view_decorator_highlights) {
      builder.setDayViewDecorator(new BackgroundHighlightDecorator());
    }
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
