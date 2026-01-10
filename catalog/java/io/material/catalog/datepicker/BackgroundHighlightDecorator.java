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
package io.material.catalog.datepicker;

import io.material.catalog.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Parcel;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.datepicker.DayViewDecorator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class BackgroundHighlightDecorator extends DayViewDecorator {

  private final Calendar today = getUtcCalendar();
  private final List<Calendar> highlightDays =
      new ArrayList<>(Arrays.asList(addDays(today, 1), addDays(today, 3), addDays(today, -2)));

  @Nullable private ColorStateList backgroundHighlightColor;
  @Nullable private ColorStateList textHighlightColor;

  @Override
  public void initialize(@NonNull Context context) {
    int highlightColor =
        MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorTertiary, BackgroundHighlightDecorator.class.getSimpleName());
    backgroundHighlightColor = ColorStateList.valueOf(highlightColor);
    int textColor =
        MaterialColors.getColor(
            context, com.google.android.material.R.attr.colorOnTertiary, BackgroundHighlightDecorator.class.getSimpleName());
    textHighlightColor = ColorStateList.valueOf(textColor);
  }

  @Nullable
  @Override
  public ColorStateList getBackgroundColor(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return shouldShowHighlight(year, month, day, valid, selected) ? backgroundHighlightColor : null;
  }

  @Nullable
  @Override
  public ColorStateList getTextColor(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return shouldShowHighlight(year, month, day, valid, selected) ? textHighlightColor : null;
  }

  @Nullable
  @Override
  public CharSequence getContentDescription(
      @NonNull Context context,
      int year,
      int month,
      int day,
      boolean valid,
      boolean selected,
      @Nullable CharSequence originalContentDescription) {
    if (!valid || selected || !shouldShowHighlight(year, month, day)) {
      return originalContentDescription;
    }
    return String.format(
        context.getString(R.string.cat_picker_day_view_decorator_highlights_content_description),
        originalContentDescription);
  }

  private boolean shouldShowHighlight(
      int year, int month, int day, boolean valid, boolean selected) {
    return valid && !selected && shouldShowHighlight(year, month, day);
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

  private static Calendar getUtcCalendar() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }

  private static Calendar addDays(Calendar source, int days) {
    Calendar calendar = getUtcCalendar();
    calendar.setTime(source.getTime());
    calendar.add(Calendar.DATE, days);
    return calendar;
  }

  public static final Creator<BackgroundHighlightDecorator> CREATOR =
      new Creator<BackgroundHighlightDecorator>() {
        @NonNull
        @Override
        public BackgroundHighlightDecorator createFromParcel(@NonNull Parcel source) {
          return new BackgroundHighlightDecorator();
        }

        @NonNull
        @Override
        public BackgroundHighlightDecorator[] newArray(int size) {
          return new BackgroundHighlightDecorator[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {}
}
