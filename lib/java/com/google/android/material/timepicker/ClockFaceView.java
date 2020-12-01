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

import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_SINGLE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver.OnPreDrawListener;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.timepicker.ClockHandView.OnRotateListener;

/**
 * A View to display a clock face.
 *
 * <p>It consists of a {@link ClockHandView} a list of the possible values evenly distributed across
 * a circle.
 */
class ClockFaceView extends RadialViewGroup implements OnRotateListener {

  private static final float EPSILON = .001f;

  private final ClockHandView clockHandView;
  private final Rect textViewRect = new Rect();
  private final RectF scratch = new RectF();

  private final SparseArray<TextView> textViewPool = new SparseArray<>();
  private final AccessibilityDelegateCompat valueAccessibilityDelegate;

  private final int[] gradientColors;
  private final float[] gradientPositions = new float[] {0f, 0.9f, 1f};
  private final int clockHandPadding;

  private String[] values;

  private float currentHandRotation;

  private final ColorStateList textColor;

  public ClockFaceView(@NonNull Context context) {
    this(context, null);
  }

  public ClockFaceView(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.materialClockStyle);
  }

  @SuppressLint("ClickableViewAccessibility")
  public ClockFaceView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a =
        context.obtainStyledAttributes(
            attrs,
            R.styleable.ClockFaceView,
            defStyleAttr,
            R.style.Widget_MaterialComponents_TimePicker_Clock);
    Resources res = getResources();
    textColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.ClockFaceView_clockNumberTextColor);
    LayoutInflater.from(context).inflate(R.layout.material_clockface_view, this, true);
    clockHandView = findViewById(R.id.material_clock_hand);
    clockHandPadding = res.getDimensionPixelSize(R.dimen.material_clock_hand_padding);
    int clockHandTextColor =
        textColor.getColorForState(
            new int[] {android.R.attr.state_selected}, textColor.getDefaultColor());
    gradientColors =
        new int[] {clockHandTextColor, clockHandTextColor, textColor.getDefaultColor()};
    clockHandView.addOnRotateListener(this);

    int defaultBackgroundColor = AppCompatResources
        .getColorStateList(context, R.color.material_timepicker_clockface)
        .getDefaultColor();

    ColorStateList backgroundColor =
        MaterialResources.getColorStateList(
            context, a, R.styleable.ClockFaceView_clockFaceBackgroundColor);

    setBackgroundColor(
        backgroundColor == null ? defaultBackgroundColor : backgroundColor.getDefaultColor());

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

    setFocusable(true);
    a.recycle();
    valueAccessibilityDelegate =
        new AccessibilityDelegateCompat() {
          @Override
          public void onInitializeAccessibilityNodeInfo(
              View host, @NonNull AccessibilityNodeInfoCompat info) {
            super.onInitializeAccessibilityNodeInfo(host, info);
            int index = (int) host.getTag(R.id.material_value_index);
            if (index > 0) {
              info.setTraversalAfter(textViewPool.get(index - 1));
            }

            info.setCollectionItemInfo(
                CollectionItemInfoCompat.obtain(
                    /* rowIndex= */ 0,
                    /* rowSpan= */ 1,
                    /* columnIndex =*/ index,
                    /* columnSpan= */ 1,
                    /* heading= */ false,
                    /* selected= */ host.isSelected()));
          }
        };
  }

  /**
   * Sets the list of values that will be shown in the clock face. The first value will be shown in
   * the 12 O'Clock position, subsequent values will be evenly distributed after.
   */
  public void setValues(String[] values, @StringRes int contentDescription) {
    this.values = values;
    updateTextViews(contentDescription);
  }

  private void updateTextViews(@StringRes int contentDescription) {
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

      textView.setText(values[i]);
      textView.setTag(R.id.material_value_index, i);
      ViewCompat.setAccessibilityDelegate(textView, valueAccessibilityDelegate);

      textView.setTextColor(textColor);
      Resources res = getResources();
      textView.setContentDescription(res.getString(contentDescription, values[i]));
    }
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(@NonNull AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    AccessibilityNodeInfoCompat infoCompat = AccessibilityNodeInfoCompat.wrap(info);
    infoCompat.setCollectionInfo(
        CollectionInfoCompat.obtain(
            /* rowCount= */ 1,
            /* columnCount= */ values.length,
            /* hierarchical= */ false,
            SELECTION_MODE_SINGLE));
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
      textViewRect.offset(tv.getPaddingLeft(), tv.getPaddingTop());
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
