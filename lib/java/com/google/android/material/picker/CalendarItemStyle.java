/*
 * Copyright 2019 The Android Open Source Project
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
package com.google.android.material.picker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.StyleRes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import android.widget.TextView;

/**
 * Loads and applies {@link R.styleable#MaterialCalendarDay} attributes to {@link TextView}
 * instances.
 */
final class CalendarItemStyle {

  private final ColorStateList textColor;
  private final MaterialShapeDrawable backgroundDrawable;
  private final MaterialShapeDrawable shapeMask;

  private CalendarItemStyle(
      ColorStateList backgroundColor,
      ColorStateList textColor,
      ColorStateList strokeColor,
      int strokeWidth,
      ShapeAppearanceModel itemShape) {

    this.textColor = textColor;
    backgroundDrawable = new MaterialShapeDrawable();
    shapeMask = new MaterialShapeDrawable();
    backgroundDrawable.setShapeAppearanceModel(itemShape);
    shapeMask.setShapeAppearanceModel(itemShape);
    backgroundDrawable.setFillColor(backgroundColor);
    backgroundDrawable.setStroke(strokeWidth, strokeColor);
  }

  /**
   * Creates a {@link CalendarItemStyle} using the provided {@link R.styleable#MaterialCalendarDay}.
   */
  static CalendarItemStyle create(Context context, @StyleRes int materialCalendarDayStyle) {
    Preconditions.checkArgument(
        materialCalendarDayStyle != 0, "Cannot create a CalendarItemStyle with a styleResId of 0");
    TypedArray styleableArray =
        context.obtainStyledAttributes(materialCalendarDayStyle, R.styleable.MaterialCalendarDay);
    ColorStateList backgroundColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarDay_itemFillColor);
    ColorStateList textColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarDay_itemTextColor);
    ColorStateList strokeColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarDay_itemStrokeColor);
    int strokeWidth =
        styleableArray.getDimensionPixelSize(R.styleable.MaterialCalendarDay_itemStrokeWidth, 0);

    int shapeAppearanceResId =
        styleableArray.getResourceId(R.styleable.MaterialCalendarDay_itemShapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        styleableArray.getResourceId(R.styleable.MaterialCalendarDay_itemShapeAppearanceOverlay, 0);

    ShapeAppearanceModel itemShape =
        new ShapeAppearanceModel(context, shapeAppearanceResId, shapeAppearanceOverlayResId);
    styleableArray.recycle();
    return new CalendarItemStyle(backgroundColor, textColor, strokeColor, strokeWidth, itemShape);
  }

  /** Applies the {@link R.styleable#MaterialCalendarDay} style to the provided {@code item} */
  void styleItem(TextView item) {
    item.setTextColor(textColor);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      ViewCompat.setBackground(
          item, new RippleDrawable(textColor.withAlpha(30), backgroundDrawable, shapeMask));
    } else {
      ViewCompat.setBackground(item, backgroundDrawable);
    }
  }
}
