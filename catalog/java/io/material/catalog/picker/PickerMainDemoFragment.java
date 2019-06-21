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

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.picker.MaterialDatePickerDialogFragment;
import com.google.android.material.picker.MaterialDateRangePickerDialogFragment;
import com.google.android.material.picker.MaterialPickerDialogFragment;
import com.google.android.material.picker.MaterialStyledDatePickerDialog;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.snackbar.Snackbar;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import io.material.catalog.feature.DemoFragment;
import java.util.Calendar;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class PickerMainDemoFragment extends DemoFragment {

  private Snackbar snackbar;

  // This demo is for a transient API. Once the API is set, the RestrictTo will be removed.
  @SuppressWarnings("RestrictTo")
  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.picker_main_demo, viewGroup, false);
    LinearLayout dialogLaunchersLayout = view.findViewById(R.id.picker_launcher_buttons_layout);

    snackbar = Snackbar.make(viewGroup, R.string.cat_picker_no_action, Snackbar.LENGTH_LONG);
    int dialogTheme =
        MaterialAttributes.resolveOrThrow(
            getContext(), R.attr.materialCalendarTheme, getClass().getCanonicalName());
    int fullscreenTheme =
        MaterialAttributes.resolveOrThrow(
            getContext(), R.attr.materialCalendarFullscreenTheme, getClass().getCanonicalName());

    MaterialDatePickerDialogFragment datePicker = MaterialDatePickerDialogFragment.newInstance();
    addSnackBarListeners(datePicker);
    addDialogLauncher(
        dialogLaunchersLayout, R.string.cat_picker_date_calendar, buildOnClickListener(datePicker));

    MaterialDatePickerDialogFragment datePickerFSTheme =
        MaterialDatePickerDialogFragment.newInstance(fullscreenTheme);
    addSnackBarListeners(datePickerFSTheme);
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar_fullscreen,
        buildOnClickListener(datePickerFSTheme));

    MaterialDateRangePickerDialogFragment rangePicker =
        MaterialDateRangePickerDialogFragment.newInstance();
    addSnackBarListeners(rangePicker);
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar,
        buildOnClickListener(rangePicker));

    MaterialDateRangePickerDialogFragment rangePickerDialogTheme =
        MaterialDateRangePickerDialogFragment.newInstance(dialogTheme);
    addSnackBarListeners(rangePickerDialogTheme);
    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar_dialog,
        buildOnClickListener(rangePickerDialogTheme));

    layoutInflater.inflate(R.layout.cat_picker_spacer, dialogLaunchersLayout, true);

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_base,
        buildOnClickListener(frameworkTodayDatePicker(0)));

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_material_styled,
        buildOnClickListener(materialStyledTodayDatePicker(0)));

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_styled_date_spinner,
        buildOnClickListener(materialStyledTodayDatePicker(getSpinnerTheme())));

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_styled_date_calendar,
        buildOnClickListener(materialStyledTodayDatePicker(getCalendarTheme())));

    return view;
  }

  // This demo is for a transient API. Once the API is set, the RestrictTo will be removed.
  @SuppressWarnings("RestrictTo")
  private <S> void addSnackBarListeners(
      MaterialPickerDialogFragment<S> materialPickerDialogFragment) {
    materialPickerDialogFragment.addOnPositiveButtonClickListener(
        selection -> {
          snackbar.setText(materialPickerDialogFragment.getHeaderText(selection));
          snackbar.show();
        });
    materialPickerDialogFragment.addOnNegativeButtonClickListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_user_clicked_cancel);
          snackbar.show();
        });
    materialPickerDialogFragment.addOnCancelListener(
        dialog -> {
          snackbar.setText(R.string.cat_picker_cancel);
          snackbar.show();
        });
  }

  protected int getSpinnerTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Picker_Date_Spinner;
  }

  protected int getCalendarTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Picker_Date_Calendar;
  }

  private OnClickListener buildOnClickListener(Dialog dialog) {
    return v -> dialog.show();
  }

  private OnClickListener buildOnClickListener(DialogFragment dialogFragment) {
    return v -> dialogFragment.show(getFragmentManager(), "Calendar Fragment");
  }

  private void addDialogLauncher(
      ViewGroup viewGroup, @StringRes int stringResId, OnClickListener onClickListener) {
    MaterialButton dialogLauncherButton = new MaterialButton(viewGroup.getContext());
    dialogLauncherButton.setOnClickListener(onClickListener);
    dialogLauncherButton.setText(stringResId);
    viewGroup.addView(dialogLauncherButton);
  }

  private DatePickerDialog frameworkTodayDatePicker(@StyleRes int themeResId) {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return new DatePickerDialog(getContext(), themeResId, null, year, month, day);
  }

  // This demo is for a transient API. Once the API is set, the RestrictTo will be removed.
  @SuppressWarnings("RestrictTo")
  private DatePickerDialog materialStyledTodayDatePicker(@StyleRes int themeResId) {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return new MaterialStyledDatePickerDialog(getContext(), themeResId, null, year, month, day);
  }
}
