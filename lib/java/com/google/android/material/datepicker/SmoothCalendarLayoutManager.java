package com.google.android.material.datepicker;

import android.content.Context;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Layout manager for {@link MaterialCalendar} with smooth scroll
 * as default one is too fast and doesn't looks like a "smooth" one
 */
class SmoothCalendarLayoutManager extends LinearLayoutManager {

  /**
   * Default value in {@link LinearSmoothScroller} is 25f
   */
  private static final float MILLISECONDS_PER_INCH = 100f;

  SmoothCalendarLayoutManager(Context context) {
    super(context);
  }

  SmoothCalendarLayoutManager(
      Context context,
      int orientation,
      boolean reverseLayout) {
    super(context, orientation, reverseLayout);
  }

  SmoothCalendarLayoutManager(
      Context context,
      AttributeSet attrs,
      int defStyleAttr,
      int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);
  }

  @Override
  public void smoothScrollToPosition(
      RecyclerView recyclerView,
      RecyclerView.State state,
      int position) {
    final LinearSmoothScroller linearSmoothScroller =
        new LinearSmoothScroller(recyclerView.getContext()) {

          @Override
          public PointF computeScrollVectorForPosition(int targetPosition) {
            return SmoothCalendarLayoutManager.this
                .computeScrollVectorForPosition(targetPosition);
          }

          @Override
          protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
            return MILLISECONDS_PER_INCH / displayMetrics.densityDpi;
          }
        };
    linearSmoothScroller.setTargetPosition(position);
    startSmoothScroll(linearSmoothScroller);
  }

}
