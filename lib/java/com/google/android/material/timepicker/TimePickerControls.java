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

import androidx.annotation.IntDef;
import androidx.annotation.IntRange;
import androidx.annotation.StringRes;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Calendar;

/**
 * An interface for different implementations of the UI components of TimePicker.
 *
 * <p>The UI components expose a ClockFace and an alternative input method.
 */
interface TimePickerControls {

  /** The 12h periods for a 12h time format */
  @IntDef({Calendar.AM, Calendar.PM})
  @Retention(RetentionPolicy.SOURCE)
  @interface ClockPeriod {}

  /** Types of active selection for time picker */
  @IntDef({Calendar.MINUTE, Calendar.HOUR})
  @Retention(RetentionPolicy.SOURCE)
  @interface ActiveSelection {}

  /** Sets the time in millis * */
  void updateTime(@ClockPeriod int period, int hourOfDay, @IntRange(from = 0) int minute);

  /** Set what we need to select. * */
  void setActiveSelection(@ActiveSelection int selection);

  /** Set the values in the clock face. */
  void setValues(String[] clockValues, @StringRes int contentDescription);

  void setHandRotation(float rotation);
}
