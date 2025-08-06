/*
 * Copyright 2022 The Android Open Source Project
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
package com.google.android.material.datepicker;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Parcel;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.color.MaterialColors;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TestBackgroundHighlightDecorator extends DayViewDecorator {

  private final Calendar startDay;
  private final List<Calendar> highlightDays;

  private ColorStateList backgroundHighlightColor;
  private ColorStateList textHighlightColor;

  public TestBackgroundHighlightDecorator(Calendar startDay) {
    this.startDay = startDay;
    highlightDays =
        new ArrayList<>(
            Arrays.asList(addDays(startDay, 1), addDays(startDay, 3), addDays(startDay, -2)));
  }

  @Override
  public void initialize(@NonNull Context context) {
    backgroundHighlightColor = ColorStateList.valueOf(getBackgroundHighlightColor(context));
    textHighlightColor = ColorStateList.valueOf(getTextHighlightColor(context));
  }

  @Nullable
  @Override
  public ColorStateList getBackgroundColor(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return valid && !selected && shouldShowHighlight(year, month, day)
        ? backgroundHighlightColor
        : null;
  }

  @Nullable
  @Override
  public ColorStateList getTextColor(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return valid && !selected && shouldShowHighlight(year, month, day) ? textHighlightColor : null;
  }

  @ColorInt
  private int getBackgroundHighlightColor(Context context) {
    return MaterialColors.getColor(
        context, R.attr.colorTertiary, getFallbackBackgroundHighlightColor(context));
  }

  @ColorInt
  private int getTextHighlightColor(Context context) {
    return MaterialColors.getColor(context, R.attr.colorOnTertiary, Color.BLACK);
  }

  @ColorInt
  private int getFallbackBackgroundHighlightColor(Context context) {
    return ColorUtils.setAlphaComponent(
        MaterialColors.getColor(
            context, R.attr.colorAccent, TestBackgroundHighlightDecorator.class.getSimpleName()),
        (int) (0.38f * 255));
  }

  private boolean shouldShowHighlight(int year, int month, int day) {
    for (Calendar calendar : highlightDays) {
      if (calendar.get(Calendar.YEAR) == year
          && calendar.get(Calendar.MONTH) == month
          && calendar.get(Calendar.DAY_OF_MONTH) == day) {
        return true;
      }
    }
    return false;
  }

  private static Calendar addDays(Calendar source, int days) {
    Calendar calendar = UtcDates.getUtcCalendar();
    calendar.setTime(source.getTime());
    calendar.add(Calendar.DATE, days);
    return calendar;
  }

  public static final Creator<TestBackgroundHighlightDecorator> CREATOR =
      new Creator<TestBackgroundHighlightDecorator>() {
        @NonNull
        @Override
        public TestBackgroundHighlightDecorator createFromParcel(@NonNull Parcel source) {
          Calendar startDay = UtcDates.getUtcCalendar();
          startDay.setTimeInMillis(source.readLong());
          return new TestBackgroundHighlightDecorator(startDay);
        }

        @NonNull
        @Override
        public TestBackgroundHighlightDecorator[] newArray(int size) {
          return new TestBackgroundHighlightDecorator[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {
    dest.writeLong(startDay.getTimeInMillis());
  }
}
