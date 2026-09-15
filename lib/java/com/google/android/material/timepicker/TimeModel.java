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

import static com.google.android.material.timepicker.TimeFormat.CLOCK_12H;
import static com.google.android.material.timepicker.TimeFormat.CLOCK_24H;
import static java.util.Calendar.AM;
import static java.util.Calendar.HOUR;
import static java.util.Calendar.PM;

import android.content.res.Resources;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.IntRange;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.timepicker.TimePickerControls.ActiveSelection;
import com.google.android.material.timepicker.TimePickerControls.ClockPeriod;
import java.util.Arrays;

/** The representation of the TimeModel used by TimePicker views. */
class TimeModel implements Parcelable {

  public static final String ZERO_LEADING_NUMBER_FORMAT = "%02d";
  public static final String NUMBER_FORMAT = "%d";

  @TimeFormat final int format;

  int hour;
  int minute;

  @ActiveSelection int selection;
  @ClockPeriod int period;

  public TimeModel() {
    this(CLOCK_12H);
  }

  public TimeModel(@TimeFormat int format) {
    this(0, 0, HOUR, format);
  }

  public TimeModel(int hour, int minute, @ActiveSelection int selection, @TimeFormat int format) {
    this.hour = hour;
    this.minute = minute;
    this.selection = selection;
    this.format = format;
    period = getPeriod(hour);
  }

  protected TimeModel(Parcel in) {
    this(in.readInt(), in.readInt(), in.readInt(), in.readInt());
  }

  /** Set hour respecting the current clock period */
  public void setHourOfDay(int hour) {
    period = getPeriod(hour);
    this.hour = hour;
  }

  @ClockPeriod
  private static int getPeriod(int hourOfDay) {
    return hourOfDay >= 12 ? PM : AM;
  }

  /** Set hour respecting the current clock period */
  public void setHour(int hour) {
    if (format == CLOCK_24H) {
      this.hour = hour;
      return;
    }

    this.hour = hour % 12 + (period == PM ? 12 : 0);
  }

  public void setMinute(@IntRange(from = 0, to = 59) int minute) {
    this.minute = minute % 60;
  }

  public int getHourForDisplay() {
    if (format == CLOCK_24H) {
      return hour % 24;
    }

    if (hour % 12 == 0) {
      return 12;
    }

    if (period == PM) {
      return hour - 12;
    }

    return hour;
  }

  @StringRes
  public int getHourContentDescriptionResId() {
    return format == CLOCK_24H ? R.string.material_hour_24h_suffix : R.string.material_hour_suffix;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {format, hour, minute, selection};
    return Arrays.hashCode(hashedFields);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof TimeModel)) {
      return false;
    }

    TimeModel that = (TimeModel) o;
    return hour == that.hour
        && minute == that.minute
        && format == that.format
        && selection == that.selection;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(hour);
    dest.writeInt(minute);
    dest.writeInt(selection);
    dest.writeInt(format);
  }

  @SuppressWarnings("unused")
  public static final Parcelable.Creator<TimeModel> CREATOR =
      new Parcelable.Creator<TimeModel>() {
        @Override
        public TimeModel createFromParcel(Parcel in) {
          return new TimeModel(in);
        }

        @Override
        public TimeModel[] newArray(int size) {
          return new TimeModel[size];
        }
      };

  public void setPeriod(@ClockPeriod int period) {
    if (period != this.period) {
      this.period = period;
      if (hour < 12 && period == PM) {
        hour += 12;
      } else if (hour >= 12 && period == AM) {
        hour -= 12;
      }
    }
  }

  @Nullable
  public static String formatText(Resources resources, CharSequence text) {
    return formatText(resources, text, ZERO_LEADING_NUMBER_FORMAT);
  }

  @Nullable
  public static String formatText(Resources resources, CharSequence text, String format) {
    try {
      return String.format(
          resources.getConfiguration().locale, format, Integer.parseInt(String.valueOf(text)));
    } catch (NumberFormatException e) {
      return null;
    }
  }
}
