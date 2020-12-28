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
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.ColorInt;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.RelativeCornerSize;

/**
 * A View Group evenly distributes children in a circle.
 *
 * <P> Children that set {@code android:tag="skip"} can be positioned anywhere in the container.
 */
class RadialViewGroup extends ConstraintLayout {

  private static final String SKIP_TAG = "skip";
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
    ViewCompat.setBackground(this, createBackground());

    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.RadialViewGroup, defStyleAttr, 0);
    radius = a.getDimensionPixelSize(R.styleable.RadialViewGroup_materialCircleRadius, 0);
    updateLayoutParametersRunnable = new Runnable() {
      @Override
      public void run() {
        updateLayoutParams();
      }
    };
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
      child.setId(ViewCompat.generateViewId());
    }

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

  private void updateLayoutParams() {
    // Subtracting 1 since we shouldn't count the view we use as the center of the circle.
    int skippedChildren = 1;
    int childCount = getChildCount();
    for (int i = 0; i < childCount; i++) {
      View childAt = getChildAt(i);
      // TODO: Add a more robust way to skip children
      if (shouldSkipView(childAt)) {
        skippedChildren++;
      }
    }

    ConstraintSet constraintSet = new ConstraintSet();
    constraintSet.clone(this);
    float currentAngle = 0;
    for (int i = 0; i < childCount; i++) {
      View childAt = getChildAt(i);
      if (childAt.getId() == R.id.circle_center || shouldSkipView(childAt)) {
        continue;
      }

      constraintSet.constrainCircle(childAt.getId(), R.id.circle_center, radius, currentAngle);
      currentAngle += (360 / (float) (childCount - skippedChildren));
    }

    constraintSet.applyTo(RadialViewGroup.this);
  }

  public void setRadius(@Dimension int radius) {
    this.radius = radius;
    updateLayoutParams();
  }

  @Dimension
  public int getRadius() {
    return radius;
  }

  private static boolean shouldSkipView(View child) {
    return SKIP_TAG.equals(child.getTag());
  }
}
