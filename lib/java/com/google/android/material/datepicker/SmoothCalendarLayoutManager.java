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

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;

/**
 * Layout manager for {@link MaterialCalendar} that slows the scroll down to appear smoother for
 * months.
 */
class SmoothCalendarLayoutManager extends LinearLayoutManager {

  /** Default value in {@link LinearSmoothScroller} is 25f */
  private static final float MILLISECONDS_PER_INCH = 100f;

  SmoothCalendarLayoutManager(Context context, int orientation, boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  @Override
  public void smoothScrollToPosition(
      RecyclerView recyclerView, RecyclerView.State state, int position) {
    final LinearSmoothScroller linearSmoothScroller =
        new LinearSmoothScroller(recyclerView.getContext()) {

          @Override
          protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
          }
        };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }
}
