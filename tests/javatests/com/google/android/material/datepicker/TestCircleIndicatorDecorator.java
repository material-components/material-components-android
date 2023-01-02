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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Parcel;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.internal.ViewUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;

public class TestCircleIndicatorDecorator extends DayViewDecorator {

  private final Calendar startDay;
  private final List<Calendar> indicatorDays;

  private IndicatorDrawables indicatorDrawables;

  public TestCircleIndicatorDecorator(Calendar startDay) {
    this.startDay = startDay;
    indicatorDays =
        new ArrayList<>(
            Arrays.asList(addDays(startDay, 1), addDays(startDay, 3), addDays(startDay, -2)));
  }

  @Override
  public void initialize(@NonNull Context context) {
    indicatorDrawables = new IndicatorDrawables(context);
  }

  @Override
  public Drawable getCompoundDrawableTop(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return indicatorDrawables.topSpacerDrawable;
  }

  @Override
  public Drawable getCompoundDrawableBottom(
      @NonNull Context context, int year, int month, int day, boolean valid, boolean selected) {
    return selectIndicatorDrawable(year, month, day, valid, selected);
  }

  private Drawable selectIndicatorDrawable(
      int year, int month, int day, boolean valid, boolean selected) {
    if (!shouldShowIndicator(year, month, day)) {
      return indicatorDrawables.indicatorDrawableNone;
    }
    if (!valid) {
      return indicatorDrawables.indicatorDrawableInvalid;
    }
    if (selected) {
      return indicatorDrawables.indicatorDrawableSelected;
    }
    return indicatorDrawables.indicatorDrawableDefault;
  }

  private boolean shouldShowIndicator(int year, int month, int day) {
    for (Calendar calendar : indicatorDays) {
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

  private static class IndicatorDrawables {

    private final int indicatorRadius;
    private final int indicatorMarginBottom;

    final Drawable topSpacerDrawable;
    final Drawable indicatorDrawableNone;
    final Drawable indicatorDrawableDefault;
    final Drawable indicatorDrawableSelected;
    final Drawable indicatorDrawableInvalid;

    private IndicatorDrawables(Context context) {
      indicatorRadius = Math.round(ViewUtils.dpToPx(context, 5));
      indicatorMarginBottom = Math.round(ViewUtils.dpToPx(context, 3));
      int topSpacerSize = Math.round(ViewUtils.dpToPx(context, 3));
      int indicatorColorDefault =
          MaterialColors.getColor(
              context, R.attr.colorPrimary, IndicatorDrawables.class.getSimpleName());
      int indicatorColorSelected =
          MaterialColors.getColor(
              context, R.attr.colorOnPrimary, IndicatorDrawables.class.getSimpleName());
      int indicatorColorInvalid =
          ColorUtils.setAlphaComponent(indicatorColorDefault, (int) (0.38f * 255));

      topSpacerDrawable = createSpacerDrawable(topSpacerSize);
      indicatorDrawableNone = createIndicatorDrawable(Color.TRANSPARENT);
      indicatorDrawableDefault = createIndicatorDrawable(indicatorColorDefault);
      indicatorDrawableSelected = createIndicatorDrawable(indicatorColorSelected);
      indicatorDrawableInvalid = createIndicatorDrawable(indicatorColorInvalid);
    }

    private Drawable createSpacerDrawable(int size) {
      Drawable spacer = new ColorDrawable(Color.TRANSPARENT);
      spacer.setBounds(0, 0, size, size);
      return spacer;
    }

    private Drawable createIndicatorDrawable(@ColorInt int color) {
      GradientDrawable shape = new GradientDrawable();
      shape.setShape(GradientDrawable.OVAL);
      shape.setColor(color);
      shape.setCornerRadius(indicatorRadius);

      InsetDrawable insetDrawable = new InsetDrawable(shape, 0, 0, 0, indicatorMarginBottom);
      insetDrawable.setBounds(0, 0, indicatorRadius, indicatorRadius + indicatorMarginBottom);
      return insetDrawable;
    }
  }

  public static final Creator<TestCircleIndicatorDecorator> CREATOR =
      new Creator<TestCircleIndicatorDecorator>() {
        @NonNull
        @Override
        public TestCircleIndicatorDecorator createFromParcel(@NonNull Parcel source) {
          Calendar startDay = UtcDates.getUtcCalendar();
          startDay.setTimeInMillis(source.readLong());
          return new TestCircleIndicatorDecorator(startDay);
        }

        @NonNull
        @Override
        public TestCircleIndicatorDecorator[] newArray(int size) {
          return new TestCircleIndicatorDecorator[size];
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
