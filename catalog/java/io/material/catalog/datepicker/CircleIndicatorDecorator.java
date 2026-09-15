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
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.datepicker.DayViewDecorator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.TimeZone;

class CircleIndicatorDecorator extends DayViewDecorator {

  private final Calendar today = getUtcCalendar();
  private final List<Calendar> indicatorDays =
      new ArrayList<>(Arrays.asList(addDays(today, 1), addDays(today, 3), addDays(today, -2)));

  private IndicatorDrawables indicatorDrawables;

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
    if (!valid || !shouldShowIndicator(year, month, day)) {
      return originalContentDescription;
    }
    return String.format(
        context.getString(R.string.cat_picker_day_view_decorator_dots_content_description),
        originalContentDescription);
  }

  private Drawable selectIndicatorDrawable(
      int year, int month, int day, boolean valid, boolean selected) {
    if (!valid || !shouldShowIndicator(year, month, day)) {
      return indicatorDrawables.indicatorDrawableNone;
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

  private static Calendar getUtcCalendar() {
    return Calendar.getInstance(TimeZone.getTimeZone("UTC"));
  }

  private static Calendar addDays(Calendar source, int days) {
    Calendar calendar = getUtcCalendar();
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

    private IndicatorDrawables(Context context) {
      Resources resources = context.getResources();

      indicatorRadius =
          resources.getDimensionPixelSize(R.dimen.cat_picker_demo_circle_indicator_size);
      indicatorMarginBottom =
          resources.getDimensionPixelOffset(R.dimen.cat_picker_demo_circle_indicator_margin_bottom);
      int topSpacerSize =
          resources.getDimensionPixelSize(R.dimen.cat_picker_demo_circle_indicator_top_spacer_size);
      int indicatorColorDefault =
          MaterialColors.getColor(
              context, com.google.android.material.R.attr.colorTertiary, IndicatorDrawables.class.getSimpleName());
      int indicatorColorSelected =
          MaterialColors.getColor(
              context, com.google.android.material.R.attr.colorOnPrimary, IndicatorDrawables.class.getSimpleName());

      topSpacerDrawable = createSpacerDrawable(topSpacerSize);
      indicatorDrawableNone = createIndicatorDrawable(Color.TRANSPARENT);
      indicatorDrawableDefault = createIndicatorDrawable(indicatorColorDefault);
      indicatorDrawableSelected = createIndicatorDrawable(indicatorColorSelected);
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

  public static final Parcelable.Creator<CircleIndicatorDecorator> CREATOR =
      new Parcelable.Creator<CircleIndicatorDecorator>() {
        @NonNull
        @Override
        public CircleIndicatorDecorator createFromParcel(@NonNull Parcel source) {
          return new CircleIndicatorDecorator();
        }

        @NonNull
        @Override
        public CircleIndicatorDecorator[] newArray(int size) {
          return new CircleIndicatorDecorator[size];
        }
      };

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(@NonNull Parcel dest, int flags) {}
}
