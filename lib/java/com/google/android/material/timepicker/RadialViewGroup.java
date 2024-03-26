/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.timepicker;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RelativeCornerSize;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * A View Group evenly distributes children in circles.
 *
 * <P> Children that set {@code android:tag="skip"} can be positioned anywhere in the container.
 * <P> Children that set {@code android:tag="level"} can be positioned in multiple circles.
 */
class RadialViewGroup extends ConstraintLayout {

  private static final String SKIP_TAG = "skip";

  static final int LEVEL_1 = 1;
  static final int LEVEL_2 = 2;
  static final float LEVEL_RADIUS_RATIO = .66f;

  /** Position views in circles, {@code LEVEL_1} for outer, {@code LEVEL_2} for inner. */
  @IntDef({LEVEL_1, LEVEL_2})
  @Retention(RetentionPolicy.SOURCE)
  @interface Level {}

  private final Runnable updateLayoutParametersRunnable;
  private int radius;
  private MaterialShapeDrawable background;

  public RadialViewGroup(@NonNull Context context) {
    this(context, null);
  }

  public RadialViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RadialViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    LayoutInflater.from(context).inflate(R.layout.material_radial_view_group, this);
    setBackground(createBackground());

    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.RadialViewGroup, defStyleAttr, 0);
    radius = a.getDimensionPixelSize(R.styleable.RadialViewGroup_materialCircleRadius, 0);
    updateLayoutParametersRunnable = this::updateLayoutParams;
    a.recycle();
  }

  private Drawable createBackground() {
    background = new MaterialShapeDrawable();
    background.setCornerSize(new RelativeCornerSize(.5f));
    background.setFillColor(ColorStateList.valueOf(Color.WHITE));
    return background;
  }

  /** Set the background color for the circular background */
  @Override
  public void setBackgroundColor(@ColorInt int color) {
    background.setFillColor(ColorStateList.valueOf(color));
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    super.addView(child, index, params);
    if (child.getId() == NO_ID) {
      child.setId(View.generateViewId());
    }
    updateLayoutParamsAsync();
  }

  @Override
  public void onViewRemoved(View view) {
    super.onViewRemoved(view);
    // Post so we only update once on a batch of added views.
    updateLayoutParamsAsync();
  }

  private void updateLayoutParamsAsync() {
    // Post so we only update once on a batch of added views.
    Handler handler = getHandler();
    if (handler != null) {
      handler.removeCallbacks(updateLayoutParametersRunnable);
      handler.post(updateLayoutParametersRunnable);
    }
  }

  @Override
  protected void onFinishInflate() {
    super.onFinishInflate();
    updateLayoutParams();
  }

  protected void updateLayoutParams() {
    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(this);
    Map<Integer, List<View>> levels = new HashMap<>();
    for (int i = 0; i < getChildCount(); i++) {
      View childAt = getChildAt(i);
      if (childAt.getId() == R.id.circle_center || shouldSkipView(childAt)) {
        continue;
      }

      Integer level = (Integer) childAt.getTag(R.id.material_clock_level);
      if (level == null) {
        level = LEVEL_1;
      }
      if (!levels.containsKey(level)) {
        levels.put(level, new ArrayList<>()); // initialize if empty
      }
      levels.get(level).add(childAt);
    }

    for (Entry<Integer, List<View>> entry : levels.entrySet()) {
      addConstraints(entry.getValue(), constraintSet, getLeveledRadius(entry.getKey()));
    }

    constraintSet.applyTo(RadialViewGroup.this);
  }

  private void addConstraints(
      final List<View> views, final ConstraintSet constraintSet, int leveledRadius) {
    float currentAngle = 0;
    for (View view : views) {
      constraintSet.constrainCircle(view.getId(), R.id.circle_center, leveledRadius, currentAngle);
      currentAngle += (360f / views.size());
    }
  }

  public void setRadius(@Dimension int radius) {
    this.radius = radius;
    updateLayoutParams();
  }

  @Dimension
  public int getRadius() {
    return radius;
  }

  @Dimension
  int getLeveledRadius(@Level int level) {
    return level == LEVEL_2 ? Math.round(radius * LEVEL_RADIUS_RATIO) : radius;
  }

  private static boolean shouldSkipView(View child) {
    return SKIP_TAG.equals(child.getTag());
  }
}
