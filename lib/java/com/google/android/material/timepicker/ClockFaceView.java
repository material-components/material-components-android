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

import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.widget.TextView;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.timepicker.ClockHandView.OnRotateListener;

/**
 * A View to display a clock face.
 *
 * <p>It consists of a {@link ClockHandView} a list of the possible values evenly distributed across
 * a circle.
 */
public class ClockFaceView extends RadialViewGroup implements OnRotateListener {

  private static final float EPSILON = .001f;

  private final ClockHandView clockHandView;
  private final Rect textViewRect = new Rect();
  private final RectF scratch = new RectF();

  private final SparseArray<TextView> textViewPool = new SparseArray<>();
  private final int[] gradientColors;
  private final float[] gradientPositions = new float[] {0f, 0.9f, 1f};
  private final int clockHandPadding;

  private String[] values;

  private float currentHandRotation;

  @ColorInt
  private final int textColor;

  public ClockFaceView(@NonNull Context context) {
    this(context, null);
  }

  public ClockFaceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  @SuppressLint("ClickableViewAccessibility")
  public ClockFaceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.ClockFaceView, defStyleAttr, 0);
    Resources res = getResources();
    textColor = a.getColor(R.styleable.ClockFaceView_valueTextColor, Color.BLACK);
    LayoutInflater.from(context).inflate(R.layout.material_clockface_view, this, true);
    clockHandView = findViewById(R.id.material_clock_hand);
    clockHandPadding = res.getDimensionPixelSize(R.dimen.material_clock_hand_padding);
    int colorOnSurface = MaterialColors.getColor(this, R.attr.colorOnSurface);
    int colorOnPrimary = MaterialColors.getColor(this, R.attr.colorOnPrimary);
    gradientColors = new int[] {colorOnPrimary, colorOnPrimary, colorOnSurface};
    clockHandView.addOnRotateListener(this);

    int backgroundColor =
        AppCompatResources.getColorStateList(context, R.color.material_timepicker_clockface)
            .getDefaultColor();
    setBackgroundColor(backgroundColor);

    getViewTreeObserver()
        .addOnPreDrawListener(
            new OnPreDrawListener() {
              @Override
              public boolean onPreDraw() {
                if (!isShown()) {
                  return true;
                }
                getViewTreeObserver().removeOnPreDrawListener(this);
                int circleRadius =
                    getHeight() / 2 - clockHandView.getSelectorRadius() - clockHandPadding;
                setRadius(circleRadius);
                return true;
              }
            });
    a.recycle();
  }

  /**
   * Sets the list of values that will be shown in the clock face. The first value will be shown in
   * the 12 O'Clock position, subsequent values will be evenly distributed after.
   */
  public void setValues(String[] values) {
    this.values = values;
    updateTextViews();
  }

  private void updateTextViews() {
    LayoutInflater inflater = LayoutInflater.from(getContext());
    for (int i = 0; i < max(values.length, textViewPool.size()); ++i) {
      TextView textView = textViewPool.get(i);
      if (i >= values.length) {
        removeView(textView);
        textViewPool.remove(i);
        continue;
      }

      if (textView == null) {
        textView = (TextView) inflater.inflate(R.layout.material_clockface_textview, this, false);
        addView(textView);
        textViewPool.put(i, textView);
      }

      textView.setTextColor(textColor);
      textView.setText(values[i]);
    }
  }

  @Override
  public void setRadius(int radius) {
    if (radius != getRadius()) {
      super.setRadius(radius);
      clockHandView.setCircleRadius(getRadius());
    }
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);
    findIntersectingTextView();
  }

  public void setHandRotation(@FloatRange(from = 0f, to = 360f) float rotation) {
    clockHandView.setHandRotation(rotation);
    findIntersectingTextView();
  }

  private void findIntersectingTextView() {
    RectF selectorBox = clockHandView.getCurrentSelectorBox();
    for (int i = 0; i < textViewPool.size(); ++i) {
      TextView tv = textViewPool.get(i);
      tv.getDrawingRect(textViewRect);
      offsetDescendantRectToMyCoords(tv, textViewRect);
      scratch.set(textViewRect);
      RadialGradient radialGradient = getGradientForTextView(selectorBox, scratch);
      tv.getPaint().setShader(radialGradient);
      tv.invalidate();
    }
  }

  private RadialGradient getGradientForTextView(RectF selectorBox, RectF tvBox) {
    if (!RectF.intersects(selectorBox, tvBox)) {
      return null;
    }

    return new RadialGradient(
        (selectorBox.centerX() - scratch.left),
        (selectorBox.centerY() - scratch.top),
        selectorBox.width() * .5f,
        gradientColors,
        gradientPositions,
        TileMode.CLAMP);
  }

  @Override
  public void onRotate(float rotation, boolean animating) {
    if (abs(currentHandRotation - rotation) > EPSILON) {
      currentHandRotation = rotation;
      findIntersectingTextView();
    }
  }
}
