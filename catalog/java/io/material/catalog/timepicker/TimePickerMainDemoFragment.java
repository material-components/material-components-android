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

import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_CLOCK;
import static com.google.android.material.timepicker.MaterialTimePicker.INPUT_MODE_KEYBOARD;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.SwitchCompat;
import android.text.format.DateFormat;
import android.util.Log;
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

  private static final String TAG = TimePickerMainDemoFragment.class.getSimpleName();

  private int hour;
  private int minute;

  private final SimpleDateFormat formatter = new SimpleDateFormat("hh:mm a", Locale.getDefault());
  @TimeFormat private int clockFormat;
  @Nullable private Integer timeInputMode;
  private TextView textView;

  @Override
  @SuppressWarnings("deprecation") // requireFragmentManager()
  public View onCreateDemoView(
      LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup, @Nullable Bundle bundle) {
    ViewGroup view =
        (ViewGroup) layoutInflater.inflate(R.layout.time_picker_main_demo, viewGroup, false);

    textView = view.findViewById(R.id.timepicker_time);

    MaterialButtonToggleGroup timeFormatToggle = view.findViewById(R.id.time_format_toggle);
    timeFormatToggle.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) {
            if (checkedId == R.id.time_format_12h) {
              clockFormat = CLOCK_12H;
            } else if (checkedId == R.id.time_format_24h) {
              clockFormat = CLOCK_24H;
            } else if (checkedId == R.id.time_format_system) {
              boolean isSystem24Hour = DateFormat.is24HourFormat(getContext());
              clockFormat = isSystem24Hour ? CLOCK_24H : CLOCK_12H;
            } else {
              Log.d(TAG, "Invalid time format selection: " + checkedId);
            }
          }
        });

    MaterialButtonToggleGroup timeInputModeToggle = view.findViewById(R.id.time_input_mode_toggle);
    timeInputModeToggle.addOnButtonCheckedListener(
        (group, checkedId, isChecked) -> {
          if (isChecked) {
            if (checkedId == R.id.time_input_mode_clock) {
              timeInputMode = INPUT_MODE_CLOCK;
            } else if (checkedId == R.id.time_input_mode_keyboard) {
              timeInputMode = INPUT_MODE_KEYBOARD;
            } else if (checkedId == R.id.time_input_mode_default) {
              timeInputMode = null;
            } else {
              Log.d(TAG, "Invalid time input mode selection: " + checkedId);
            }
          }
        });

    SwitchCompat frameworkSwitch = view.findViewById(R.id.framework_switch);
    frameworkSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
      for (int i = 0; i < timeInputModeToggle.getChildCount(); i++) {
        View child = timeInputModeToggle.getChildAt(i);
        child.setEnabled(!isChecked);
      }
    });

    timeFormatToggle.check(R.id.time_format_system);
    timeInputModeToggle.check(R.id.time_input_mode_default);
    frameworkSwitch.setChecked(false);

    Button button = view.findViewById(R.id.timepicker_button);
    button.setOnClickListener(v -> {
      if (frameworkSwitch.isChecked()) {
        showFrameworkTimepicker();
        return;
      }

      MaterialTimePicker.Builder materialTimePickerBuilder = new MaterialTimePicker.Builder()
          .setTimeFormat(clockFormat)
          .setHour(hour)
          .setMinute(minute);

      if (timeInputMode != null) {
        materialTimePickerBuilder.setInputMode(timeInputMode);
      }

      MaterialTimePicker materialTimePicker = materialTimePickerBuilder.build();
      materialTimePicker.showNow(requireFragmentManager(), "fragment_tag");
      setUpClickListener();
    });

    setUpClickListener();
    return view;
  }

  private void setUpClickListener() {
    Fragment fragment = getParentFragmentManager().findFragmentByTag("fragment_tag");
    if (fragment instanceof MaterialTimePicker) {
      MaterialTimePicker materialTimePicker = (MaterialTimePicker) fragment;
      materialTimePicker.clearOnPositiveButtonClickListeners();
      materialTimePicker.addOnPositiveButtonClickListener(
          dialog -> {
            int newHour = materialTimePicker.getHour();
            int newMinute = materialTimePicker.getMinute();
            TimePickerMainDemoFragment.this.onTimeSet(newHour, newMinute);
          });
    }
  }

  private void showFrameworkTimepicker() {
    android.app.TimePickerDialog timePickerDialog =
        new android.app.TimePickerDialog(
            getContext(),
            (view, hourOfDay, minute) ->
                TimePickerMainDemoFragment.this.onTimeSet(hourOfDay, minute),
            hour,
            minute,
            clockFormat == CLOCK_24H);
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
