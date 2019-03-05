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
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.util.Calendar;

/**
 * A {@link MaterialCalendarView} that supports single date selection
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class MaterialDatePickerView extends MaterialCalendarView<Calendar> {

  private static final int DEF_STYLE_ATTR = R.attr.materialDatePickerStyle;
  private static final ColorDrawable emptyColor = new ColorDrawable(Color.TRANSPARENT);
  private static final ColorDrawable selectedColor = new ColorDrawable(Color.RED);

  private int selectedPosition = -1;
  private final OnItemClickListener onItemClickListener;

  public MaterialDatePickerView(Context context) {
    this(context, null);
  }

  public MaterialDatePickerView(Context context, AttributeSet attrs) {
    this(context, attrs, DEF_STYLE_ATTR);
  }

  public MaterialDatePickerView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    onItemClickListener =
        new OnItemClickListener() {

          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (!getMonthInYearAdapter().withinMonth(position)) {
              return;
            }
            selectedPosition = position;
          }
        };
  }

  @Override
  protected OnItemClickListener getOnItemClickListener() {
    return onItemClickListener;
  }

  @Override
  protected void drawSelection(AdapterView<?> parent) {
    for (int i = 0; i < parent.getCount(); i++) {
      ViewCompat.setBackground(
          parent.getChildAt(i), i == selectedPosition ? selectedColor : emptyColor);
    }
  }

  @Override
  public Calendar getSelection() {
    return getMonthInYearAdapter().getItem(selectedPosition);
  }
}
