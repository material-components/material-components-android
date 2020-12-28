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
package com.google.android.material.datepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.util.Preconditions;
import androidx.core.view.ViewCompat;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.StyleRes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * Loads and applies {@link R.styleable#MaterialCalendarDay} attributes to {@link TextView}
 * instances.
 */
final class CalendarItemStyle {

  /**
   * The inset between the TextView horizontal edge - bounding the touch target for an item - and
   * the selection marker.
   *
   * <p>The selection marker's size is defined by the {@link
   * R.styleable.MaterialCalendarItem#itemShapeAppearance} and {@link
   * R.styleable.MaterialCalendarItem#itemShapeAppearanceOverlay}.
   */
  @NonNull private final Rect insets;

  private final ColorStateList textColor;
  private final ColorStateList backgroundColor;
  private final ColorStateList strokeColor;
  private final int strokeWidth;
  private final ShapeAppearanceModel itemShape;

  private CalendarItemStyle(
      ColorStateList backgroundColor,
      ColorStateList textColor,
      ColorStateList strokeColor,
      int strokeWidth,
      ShapeAppearanceModel itemShape,
      @NonNull Rect insets) {
    Preconditions.checkArgumentNonnegative(insets.left);
    Preconditions.checkArgumentNonnegative(insets.top);
    Preconditions.checkArgumentNonnegative(insets.right);
    Preconditions.checkArgumentNonnegative(insets.bottom);

    this.insets = insets;
    this.textColor = textColor;
    this.backgroundColor = backgroundColor;
    this.strokeColor = strokeColor;
    this.strokeWidth = strokeWidth;
    this.itemShape = itemShape;
  }

  /**
   * Creates a {@link CalendarItemStyle} using the provided {@link
   * R.styleable#MaterialCalendarItem}.
   */
  @NonNull
  static CalendarItemStyle create(
      @NonNull Context context, @StyleRes int materialCalendarItemStyle) {
    Preconditions.checkArgument(
        materialCalendarItemStyle != 0, "Cannot create a CalendarItemStyle with a styleResId of 0");

    TypedArray styleableArray =
        context.obtainStyledAttributes(materialCalendarItemStyle, R.styleable.MaterialCalendarItem);
    int insetLeft =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetLeft, 0);
    int insetTop =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetTop, 0);
    int insetRight =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetRight, 0);
    int insetBottom =
        styleableArray.getDimensionPixelOffset(
            R.styleable.MaterialCalendarItem_android_insetBottom, 0);
    Rect insets = new Rect(insetLeft, insetTop, insetRight, insetBottom);

    ColorStateList backgroundColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemFillColor);
    ColorStateList textColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemTextColor);
    ColorStateList strokeColor =
        MaterialResources.getColorStateList(
            context, styleableArray, R.styleable.MaterialCalendarItem_itemStrokeColor);
    int strokeWidth =
        styleableArray.getDimensionPixelSize(R.styleable.MaterialCalendarItem_itemStrokeWidth, 0);

    int shapeAppearanceResId =
        styleableArray.getResourceId(R.styleable.MaterialCalendarItem_itemShapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        styleableArray.getResourceId(
            R.styleable.MaterialCalendarItem_itemShapeAppearanceOverlay, 0);

    ShapeAppearanceModel itemShape =
        ShapeAppearanceModel.builder(context, shapeAppearanceResId, shapeAppearanceOverlayResId)
            .build();

    styleableArray.recycle();

    return new CalendarItemStyle(
        backgroundColor, textColor, strokeColor, strokeWidth, itemShape, insets);
  }

  /** Applies the {@link R.styleable#MaterialCalendarDay} style to the provided {@code item} */
  void styleItem(@NonNull TextView item) {
    MaterialShapeDrawable backgroundDrawable = new MaterialShapeDrawable();
    MaterialShapeDrawable shapeMask = new MaterialShapeDrawable();
    backgroundDrawable.setShapeAppearanceModel(itemShape);
    shapeMask.setShapeAppearanceModel(itemShape);
    backgroundDrawable.setFillColor(backgroundColor);
    backgroundDrawable.setStroke(strokeWidth, strokeColor);
    item.setTextColor(textColor);
    Drawable d;
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      d = new RippleDrawable(textColor.withAlpha(30), backgroundDrawable, shapeMask);
    } else {
      d = backgroundDrawable;
    }
    ViewCompat.setBackground(
        item, new InsetDrawable(d, insets.left, insets.top, insets.right, insets.bottom));
  }

  int getLeftInset() {
    return insets.left;
  }

  int getRightInset() {
    return insets.right;
  }

  int getTopInset() {
    return insets.top;
  }

  int getBottomInset() {
    return insets.bottom;
  }
}
