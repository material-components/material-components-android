/*
 * Copyright 2024 The Android Open Source Project
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

package com.google.android.material.tabs;

import static androidx.annotation.RestrictTo.Scope.LIBRARY;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/*
 * Original code by Samsung, all rights reserved to the original author.
 */

@RestrictTo(LIBRARY)
public class SeslTabDotLineIndicator extends SeslAbsIndicatorView {
  private static final float CIRCLE_INTERVAL = 2.5f;
  private static final float DIAMETER_SIZE = 2.5f;
  private static final int SCALE_DIFF = 5;

  private Paint mPaint;

  private final int mDiameter;
  private final int mInterval;
  private int mWidth;

  private float mScaleFrom;
  private final float mScaleFromDiff;

  public SeslTabDotLineIndicator(Context context) {
    this(context, null);
  }

  public SeslTabDotLineIndicator(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public SeslTabDotLineIndicator(Context context, AttributeSet attrs, int defStyleAttr) {
    this(context, attrs, defStyleAttr, 0);
  }

  public SeslTabDotLineIndicator(Context context,
                                 @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
    super(context, attrs, defStyleAttr, defStyleRes);

    final DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    mDiameter = (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, DIAMETER_SIZE, metrics);
    mInterval = (int) TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, CIRCLE_INTERVAL, metrics);
    mScaleFromDiff = TypedValue
            .applyDimension(TypedValue.COMPLEX_UNIT_DIP, SCALE_DIFF, metrics);

    mPaint = new Paint();
    mPaint.setFlags(Paint.ANTI_ALIAS_FLAG);
  }

  @Override
  void onHide() {
    setAlpha(0f);
  }

  @Override
  void onShow() {
    startReleaseEffect();
  }

  @Override
  void startPressEffect() {
    setAlpha(1f);
    invalidate();
  }

  @Override
  void startReleaseEffect() {
    setAlpha(1f);
  }

  private void updateDotLineScaleFrom() {
    if (mWidth != getWidth() || mWidth == 0) {
      mWidth = getWidth();
      if (mWidth <= 0) {
        mScaleFrom = 0.9f;
      } else {
        mScaleFrom = (mWidth - mScaleFromDiff) / mWidth;
      }
    }
  }

  @Override
  void onSetSelectedIndicatorColor(int color) {
    mPaint.setColor(color);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    updateDotLineScaleFrom();

    if ((isPressed() || isSelected())
            && (getBackground() instanceof ColorDrawable)) {
      final int width = getWidth() - getPaddingStart() - getPaddingEnd();
      final float halfHeight = getHeight() / 2f;
      final int diameter = mDiameter;
      final float halfDiameter = diameter / 2f;
      canvas.drawRoundRect(0f, halfHeight - halfDiameter,
              width, halfHeight + halfDiameter,
              diameter, diameter, mPaint);
    }
  }
}
