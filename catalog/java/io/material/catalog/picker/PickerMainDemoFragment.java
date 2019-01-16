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
import com.google.android.material.button.MaterialButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import io.material.catalog.feature.DemoFragment;
import java.util.Calendar;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class PickerMainDemoFragment extends DemoFragment {

  @Override
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    View view = layoutInflater.inflate(R.layout.picker_main_demo, viewGroup, false);
    LinearLayout dialogLaunchersLayout = view.findViewById(R.id.picker_launcher_buttons_layout);

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_base,
        todayDatePicker(0));

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_spinner,
        todayDatePicker(getSpinnerTheme()));

    addDialogLauncher(
        dialogLaunchersLayout,
        R.string.cat_picker_date_calendar,
        todayDatePicker(getCalendarTheme()));

    return view;
  }

  protected int getSpinnerTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Spinner;
  }

  protected int getCalendarTheme() {
    return R.style.ThemeOverlay_MaterialComponents_MaterialAlertDialog_Calendar;
  }

  protected void addDialogLauncher(
      ViewGroup viewGroup, @StringRes int stringResId, Dialog dialog) {
    MaterialButton dialogLauncherButton = new MaterialButton(viewGroup.getContext());
    dialogLauncherButton.setOnClickListener(v -> dialog.show());
    dialogLauncherButton.setText(stringResId);
    viewGroup.addView(dialogLauncherButton);
  }

  protected DatePickerDialog todayDatePicker(int themeResId) {
    Calendar calendar = Calendar.getInstance();
    int year = calendar.get(Calendar.YEAR);
    int month = calendar.get(Calendar.MONTH);
    int day = calendar.get(Calendar.DAY_OF_MONTH);
    return new DatePickerDialog(getContext(), themeResId, null, year, month, day);
  }
}
