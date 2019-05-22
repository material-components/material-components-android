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
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.StyleRes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;
import androidx.core.view.ViewCompat;
import android.widget.TextView;

/**
 * Utility class for shared {@link GridSelector} behavior.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
class CalendarGridSelectors {

  private CalendarGridSelectors() {}

  static void colorCell(TextView cell, @StyleRes int style) {
    Context context = cell.getContext();
    if (style == 0) {
      return;
    }
    MaterialShapeDrawable backgroundDrawable = new MaterialShapeDrawable();

    TypedArray styleableArray =
        context.obtainStyledAttributes(style, R.styleable.MaterialCalendarDay);
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
    backgroundDrawable.setShapeAppearanceModel(
        new ShapeAppearanceModel(
            context,
            styleableArray.getResourceId(R.styleable.MaterialCalendarDay_itemShapeAppearance, 0),
            styleableArray.getResourceId(
                R.styleable.MaterialCalendarDay_itemShapeAppearanceOverlay, 0)));
    styleableArray.recycle();

    cell.setTextColor(textColor);
    backgroundDrawable.setFillColor(backgroundColor);
    backgroundDrawable.setStroke(strokeWidth, strokeColor);
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      ViewCompat.setBackground(
          cell, new RippleDrawable(textColor.withAlpha(30), backgroundDrawable, null));
    } else {
      ViewCompat.setBackground(cell, backgroundDrawable);
    }
  }
}
