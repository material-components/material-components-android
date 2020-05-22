/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.divider;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PorterDuff;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.util.TypedValue;

import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.recyclerview.widget.DividerItemDecoration;

import com.google.android.material.R;

public class MaterialDividerItemDecoration extends DividerItemDecoration {

  int orientation;
  boolean isRtlEnabled;

  ShapeDrawable dividerDrawable;

  private static final int ATTR_ALPHA = 0;
  private static final int ATTR_COLOR = 1;
  private static final int ATTR_INSET = 2;
  private static final int ATTR_SIZE = 3;
  private static final int[] attrs = new int[]{
      R.attr.dividerAlpha,
      R.attr.dividerColor,
      R.attr.dividerInset,
      R.attr.dividerSize
  };

  public MaterialDividerItemDecoration(Context context, int orientation) {
    super(context, orientation);

    this.orientation = orientation;
    isRtlEnabled = context.getResources().getBoolean(R.bool.is_rtl_enabled);

    TypedValue dividerStyle = new TypedValue();
    context.getTheme().resolveAttribute(R.attr.dividerStyle, dividerStyle, true);

    TypedArray dividerStyleAttrs = context.getTheme().obtainStyledAttributes(dividerStyle.resourceId, attrs);
    float alpha = dividerStyleAttrs.getFloat(ATTR_ALPHA, -1);
    int color = dividerStyleAttrs.getColor(ATTR_COLOR, -1);
    int inset = (int) dividerStyleAttrs.getDimension(ATTR_INSET, -1);
    int size = (int) dividerStyleAttrs.getDimension(ATTR_SIZE, -1);
    dividerStyleAttrs.recycle();

    dividerDrawable = new ShapeDrawable(new RectShape());
    setAlpha(alpha);
    setColor(color);
    setSize(size);

    //Insetting is not supported for horizontal dividers, so we ignore it for them rather than throwing an error
    if (orientation == VERTICAL) {
      setInset(inset);
    }
  }

  int size = 0;

  public int getSize() {
    return size;
  }

  public void setSize(int size) {
    this.size = size;
    if (orientation == VERTICAL) {
      dividerDrawable.setIntrinsicHeight(size);
    } else {
      dividerDrawable.setIntrinsicWidth(size);
    }
    dividerDrawable.invalidateSelf();
  }

  float alpha = 0;

  public float getAlpha() {
    return alpha;
  }

  public void setAlpha(float alpha) {
    this.alpha = alpha;
    dividerDrawable.getPaint().setAlpha((int) (alpha * 255));
    dividerDrawable.invalidateSelf();
  }

  @ColorInt
  int color = 0;

  public @ColorInt
  int getColor() {
    return color;
  }

  public void setColor(@ColorInt int color) {
    this.color = color;
    dividerDrawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
    dividerDrawable.invalidateSelf();
  }

  @Dimension
  int inset = 0;

  public @Dimension
  int getInset() {
    return inset;
  }

  public void setInset(@Dimension int inset) {

    if (orientation == HORIZONTAL) {
      throw new UnsupportedOperationException("Material divider does not support insetting for horizontal dividers");
    }

    this.inset = inset;
    int insetLeft = isRtlEnabled ? 0 : inset;
    int insetRight = isRtlEnabled ? inset : 0;

    InsetDrawable insetDrawable = new InsetDrawable(dividerDrawable, insetLeft, 0, insetRight, 0);
    setDrawable(insetDrawable);
  }
}
