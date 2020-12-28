/*
 * Copyright 2020 The Android Open Source Project
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
package io.material.catalog.timepicker;

import io.material.catalog.R;

import android.os.Bundle;
import androidx.appcompat.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.Nullable;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;
import io.material.catalog.feature.DemoFragment;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/** A fragment that displays the main Picker demos for the Catalog app. */
public class TimePickerMainDemoFragment extends DemoFragment {

  private int hour;
  private int minute;

  private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
  @TimeFormat private int clockFormat;
  private TextView textView;

  @Override
  @SuppressWarnings("deprecation") // requireFragmentManager()
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    ViewGroup view =
        (ViewGroup) layoutInflater.inflate(R.layout.time_picker_main_demo, viewGroup, false);

    Button button = view.findViewById(R.id.timepicker_button);
    textView = view.findViewById(R.id.timepicker_time);
    MaterialButtonToggleGroup timeFormatToggle = view.findViewById(R.id.time_format_toggle);
    clockFormat = TimeFormat.CLOCK_12H;
    timeFormatToggle.check(R.id.time_format_12h);

    timeFormatToggle.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          boolean isSystem24Hour = DateFormat.is24HourFormat(getContext());
          boolean is24Hour =
              checkedId == R.id.time_format_24h
                  || (checkedId == R.id.time_format_system && isSystem24Hour);

          clockFormat = is24Hour ? TimeFormat.CLOCK_24H : TimeFormat.CLOCK_12H;
        });

    SwitchCompat frameworkSwitch = view.findViewById(R.id.framework_switch);
    button.setOnClickListener(v -> {
      if (frameworkSwitch.isChecked()) {
        showFrameworkTimepicker();
        return;
      }

      MaterialTimePicker materialTimePicker = new MaterialTimePicker.Builder()
          .setTimeFormat(clockFormat)
          .setHour(hour)
          .setMinute(minute)
          .build();

      materialTimePicker.show(requireFragmentManager(), "fragment_tag");

      materialTimePicker.addOnPositiveButtonClickListener(dialog -> {
        int newHour = materialTimePicker.getHour();
        int newMinute = materialTimePicker.getMinute();
        TimePickerMainDemoFragment.this.onTimeSet(newHour, newMinute);
      });
    });

    return view;
  }

  private void showFrameworkTimepicker() {
    android.app.TimePickerDialog timePickerDialog =
        new android.app.TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) ->
                TimePickerMainDemoFragment.this.onTimeSet(hourOfDay, minute),
            hour,
            minute,
            clockFormat == TimeFormat.CLOCK_24H);
    timePickerDialog.show();
  }

  private void onTimeSet(int newHour, int newMinute) {
    Calendar cal = Calendar.getInstance();
    cal.set(Calendar.HOUR_OF_DAY, newHour);
    cal.set(Calendar.MINUTE, newMinute);
    cal.setLenient(false);

    String format = formatter.format(cal.getTime());
    textView.setText(format);
    hour = newHour;
    minute = newMinute;
  }
}
