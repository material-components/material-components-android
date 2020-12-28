/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.timepicker;

import com.google.android.material.R;

import static android.view.HapticFeedbackConstants.CLOCK_TICK;
import static android.view.HapticFeedbackConstants.VIRTUAL_KEY;
import static android.view.View.GONE;
import static androidx.core.content.ContextCompat.getSystemService;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;
import static java.util.Calendar.MINUTE;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import com.google.android.material.timepicker.ClockHandView.OnActionUpListener;
import com.google.android.material.timepicker.ClockHandView.OnRotateListener;
import com.google.android.material.timepicker.TimePickerControls.ActiveSelection;
import com.google.android.material.timepicker.TimePickerView.OnPeriodChangeListener;
import com.google.android.material.timepicker.TimePickerView.OnSelectionChange;
import java.util.Calendar;

class TimePickerClockPresenter
    implements OnRotateListener,
        OnSelectionChange,
        OnPeriodChangeListener,
        OnActionUpListener,
        TimePickerPresenter {

  private static final String[] HOUR_CLOCK_VALUES =
      {"12", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};

  private static final String[] HOUR_CLOCK_24_VALUES =
      {"00", "2", "4", "6", "8", "10", "12", "14", "16", "18", "20", "22"};

  private static final  String[] MINUTE_CLOCK_VALUES =
      {"00", "5", "10", "15", "20", "25", "30", "35", "40", "45", "50", "55"};

  private static final int DEGREES_PER_HOUR = 30;
  private static final int DEGREES_PER_MINUTE = 6;

  private TimePickerView timePickerView;
  private TimeModel time;
  private float minuteRotation;
  private float hourRotation;

  private boolean broadcasting = false;

  public TimePickerClockPresenter(TimePickerView timePickerView, TimeModel time) {
    this.timePickerView = timePickerView;
    this.time = time;
    initialize();
  }

  @Override
  public void initialize() {
    if (time.format == CLOCK_12H) {
      timePickerView.showToggle();
    }

    timePickerView.addOnRotateListener(this);
    timePickerView.setOnSelectionChangeListener(this);
    timePickerView.setOnPeriodChangeListener(this);
    timePickerView.setOnActionUpListener(this);
    updateValues();
    invalidate();
  }

  @Override
  public void invalidate() {
    int hourForDisplay = time.getHourForDisplay();
    hourRotation = hourForDisplay * getDegreesPerHour();
    minuteRotation = time.minute * DEGREES_PER_MINUTE;
    setSelection(time.selection, false);
    updateTime();
  }

  @Override
  public void show() {
    timePickerView.setVisibility(View.VISIBLE);
  }

  @Override
  public void hide() {
    timePickerView.setVisibility(GONE);
  }

  private String[] getHourClockValues() {
    return time.format == CLOCK_24H ? HOUR_CLOCK_24_VALUES : HOUR_CLOCK_VALUES;
  }

  private int getDegreesPerHour() {
    return time.format == CLOCK_24H ? DEGREES_PER_HOUR / 2 : DEGREES_PER_HOUR;
  }

  @Override
  public void onRotate(float rotation, boolean animating) {
    if (broadcasting) {
      return;
    }

    int prevHour = time.hour;
    int prevMinute = time.minute;
    int rotationInt = Math.round(rotation);
    if (time.selection == MINUTE) {
      int minuteOffset = DEGREES_PER_MINUTE / 2;
      time.setMinute((rotationInt + minuteOffset) / DEGREES_PER_MINUTE);
      minuteRotation = (float) Math.floor(time.minute * DEGREES_PER_MINUTE);
    } else {
      int hourOffset = getDegreesPerHour() / 2;
      time.setHour((rotationInt + hourOffset) / getDegreesPerHour());
      hourRotation = time.getHourForDisplay() * getDegreesPerHour();
    }

    // Do not update the display during an animation
    if (!animating) {
      updateTime();
      performHapticFeedback(prevHour, prevMinute);
    }
  }

  private void performHapticFeedback(int prevHour, int prevMinute) {
    if (time.minute != prevMinute || time.hour != prevHour) {
      int feedbackKey = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP ? CLOCK_TICK : VIRTUAL_KEY;
      timePickerView.performHapticFeedback(feedbackKey);
    }
  }

  @Override
  public void onSelectionChanged(int selection) {
    setSelection(selection, true);
  }

  @Override
  public void onPeriodChange(int period) {
    time.setPeriod(period);
  }

  void setSelection(@ActiveSelection int selection, boolean animate) {
    boolean isMinute = selection == MINUTE;
    // Don't animate hours since we are going to auto switch to the minute selection.
    timePickerView.setAnimateOnTouchUp(isMinute);
    time.selection = selection;
    timePickerView.setValues(
        isMinute ? MINUTE_CLOCK_VALUES : getHourClockValues(),
        isMinute ? R.string.material_minute_suffix : R.string.material_hour_suffix);
    timePickerView.setHandRotation(isMinute ? minuteRotation : hourRotation, animate);
    timePickerView.setActiveSelection(selection);
    timePickerView.setMinuteHourDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_hour_selection));
    timePickerView.setHourClickDelegate(
        new ClickActionDelegate(timePickerView.getContext(), R.string.material_minute_selection));
  }

  @Override
  public void onActionUp(float rotation, boolean moveInEventStream) {
    broadcasting = true;
    int prevMinute = time.minute;
    int prevHour = time.hour;
    if (time.selection == Calendar.HOUR) {
      // Current rotation might be half way to an exact hour position.
      // Snap to the closest hour before animating to the position the minute selection is on.
      timePickerView.setHandRotation(hourRotation, /* animate= */ false);
      // Automatically move to minutes once the user finishes choosing the hour.

      AccessibilityManager am =
          getSystemService(timePickerView.getContext(), AccessibilityManager.class);
      boolean isExploreByTouchEnabled = am.isTouchExplorationEnabled();
      if (!isExploreByTouchEnabled) {
        setSelection(MINUTE, /* animate= */ true);
      }
    } else {
      int rotationInt = Math.round(rotation);
      if (!moveInEventStream) {
        // snap minute to 5 minute increment if there was only a touch down/up.
        int newRotation = (rotationInt + 15) / 30;
        time.setMinute(newRotation * 5);
        minuteRotation = time.minute * DEGREES_PER_MINUTE;
      }
      timePickerView.setHandRotation(minuteRotation, /* animate= */ moveInEventStream);
    }
    broadcasting = false;
    updateTime();
    performHapticFeedback(prevHour, prevMinute);
  }

  private void updateTime() {
    timePickerView.updateTime(time.period, time.getHourForDisplay(), time.minute);
  }

  /** Update values with the correct number format */
  private void updateValues() {
    updateValues(HOUR_CLOCK_VALUES, TimeModel.NUMBER_FORMAT);
    updateValues(HOUR_CLOCK_24_VALUES, TimeModel.NUMBER_FORMAT);
    updateValues(MINUTE_CLOCK_VALUES, TimeModel.ZERO_LEADING_NUMBER_FORMAT);
  }

  private void updateValues(String[] values, String format) {
    for (int i = 0; i < values.length; ++i) {
      values[i] = TimeModel.formatText(timePickerView.getResources(), values[i], format);
    }
  }
}
