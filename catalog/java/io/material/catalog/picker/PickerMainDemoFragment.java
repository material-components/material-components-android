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
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.annotation.StyleRes;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.resources.MaterialAttributes;
import com.google.android.material.snackbar.Snackbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.google.android.material.picker.MaterialDatePicker;
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

    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar,
        MaterialDatePicker.Builder.datePicker());
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar_fullscreen,
        MaterialDatePicker.Builder.datePicker().setTheme(fullscreenTheme));
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar,
        MaterialDatePicker.Builder.dateRangePicker());
    setupDialogFragment(
        dialogLaunchersLayout,
        R.string.cat_picker_date_range_calendar_dialog,
        MaterialDatePicker.Builder.dateRangePicker().setTheme(dialogTheme));

    layoutInflater.inflate(R.layout.cat_picker_spacer, dialogLaunchersLayout, true);
    addDialogLauncher(
        dialogLaunchersLayout, R.string.cat_picker_base, v -> frameworkTodayDatePicker(0).show());

    return view;
  }

  // This demo is for a transient API. Once the API is set, the RestrictTo will be removed.
  @SuppressWarnings("RestrictTo")
  private void setupDialogFragment(
      ViewGroup dialogLaunchersLayout,
      @StringRes int tagId,
      MaterialDatePicker.Builder<?> builder) {
    String tag = getString(tagId);
    final MaterialDatePicker<?> materialCalendarPicker;
    if (getFragmentManager().findFragmentByTag(tag) == null) {
      materialCalendarPicker = builder.build();
    } else {
      materialCalendarPicker = (MaterialDatePicker<?>) getFragmentManager().findFragmentByTag(tag);
    }
    addSnackBarListeners(materialCalendarPicker);
    addDialogLauncher(
        dialogLaunchersLayout, tagId, v -> materialCalendarPicker.show(getFragmentManager(), tag));
  }

  // This demo is for a transient API. Once the API is set, the RestrictTo will be removed.
  @SuppressWarnings("RestrictTo")
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

  protected int getSpinnerTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Picker_Date_Spinner;
  }

  protected int getCalendarTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Picker_Date_Calendar;
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
}
