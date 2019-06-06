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

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.GridView;
import android.widget.ListAdapter;

final class MaterialCalendarGridView extends GridView {

  public MaterialCalendarGridView(Context context) {
    this(context, null);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaterialCalendarGridView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    getAdapter().notifyDataSetChanged();
  }

  @Override
  public MonthAdapter getAdapter() {
    return (MonthAdapter) super.getAdapter();
  }

  @Override
  public final void setAdapter(ListAdapter adapter) {
    if (!(adapter instanceof MonthAdapter)) {
      throw new IllegalArgumentException(
          String.format(
              "%1$s must have its Adapter set to a %2$s",
              MaterialCalendarGridView.class.getCanonicalName(),
              MonthAdapter.class.getCanonicalName()));
    }
    super.setAdapter(adapter);
  }

  @Override
  protected final void onDraw(Canvas canvas) {
    super.onDraw(canvas);
    getAdapter().gridSelector.onCalendarMonthDraw(canvas, this);
  }
}
