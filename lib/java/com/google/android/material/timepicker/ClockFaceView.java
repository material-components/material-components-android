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

import static android.view.MotionEvent.ACTION_DOWN;
import static android.view.MotionEvent.ACTION_UP;
import static androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat.SELECTION_MODE_SINGLE;
import static java.lang.Math.abs;
import static java.lang.Math.max;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Outline;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader.TileMode;
import android.os.Bundle;
import android.os.SystemClock;
import androidx.appcompat.content.res.AppCompatResources;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.TextView;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.core.view.AccessibilityDelegateCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.AccessibilityActionCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionInfoCompat;
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import com.google.android.material.resources.MaterialResources;
import com.google.android.material.timepicker.ClockHandView.OnRotateListener;
import java.util.Arrays;

/**
 * A View to display a clock face.
 *
 * <p>It consists of a {@link ClockHandView} a list of the possible values evenly distributed across
 * a circle.
 */
class ClockFaceView extends RadialViewGroup implements OnRotateListener {

  private static final float EPSILON = .001f;
  private static final int INITIAL_CAPACITY = 12;
  private static final String VALUE_PLACEHOLDER = "";

  private final ClockHandView clockHandView;
  private final Rect textViewRect = new Rect();
  private final RectF scratch = new RectF();
  private final Rect scratchLineBounds = new Rect();

  private final SparseArray<TextView> textViewPool = new SparseArray<>();
  private final AccessibilityDelegateCompat valueAccessibilityDelegate;

  private final int[] gradientColors;
  private final float[] gradientPositions = new float[] {0f, 0.9f, 1f};
  private final int clockHandPadding;
  private final int minimumHeight;
  private final int minimumWidth;
  private final int clockSize;

  private String[] values;

  private float currentHandRotation;

  private final ColorStateList textColor;

  private OnEnterKeyPressedListener onEnterKeyPressedListener;

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

    a.recycle();

    setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            outline.setOval(0, 0, view.getWidth(), view.getHeight());
          }
        });
    setFocusable(true);
    setClipToOutline(true);

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
                    /* columnIndex= */ index,
                    /* columnSpan= */ 1,
                    /* heading= */ false,
                    /* selected= */ host.isSelected()));

            info.setClickable(true);
            info.addAction(AccessibilityActionCompat.ACTION_CLICK);
          }

          @Override
          public boolean performAccessibilityAction(View host, int action, Bundle args) {
            if (action == AccessibilityNodeInfoCompat.ACTION_CLICK) {
              long time = SystemClock.uptimeMillis();
              host.getHitRect(textViewRect);
              float x = textViewRect.centerX();
              float y = textViewRect.centerY();
              clockHandView.onTouchEvent(MotionEvent.obtain(time, time, ACTION_DOWN, x, y, 0));
              clockHandView.onTouchEvent(MotionEvent.obtain(time, time, ACTION_UP, x, y, 0));
              return true;
            }
            return super.performAccessibilityAction(host, action, args);
          }
        };

    // Fill clock face with place holders
    String[] initialValues = new String[INITIAL_CAPACITY];
    Arrays.fill(initialValues, VALUE_PLACEHOLDER);
    setValues(initialValues, /* contentDescription= */ 0);

    minimumHeight = res.getDimensionPixelSize(R.dimen.material_time_picker_minimum_screen_height);
    minimumWidth = res.getDimensionPixelSize(R.dimen.material_time_picker_minimum_screen_width);
    clockSize = res.getDimensionPixelSize(R.dimen.material_clock_size);
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
    boolean isMultiLevel = false;

    LayoutInflater inflater = LayoutInflater.from(getContext());
    int size = textViewPool.size();
    for (int i = 0; i < max(values.length, size); ++i) {
      TextView textView = textViewPool.get(i);
      if (i >= values.length) {
        removeView(textView);
        textViewPool.remove(i);
        continue;
      }

      if (textView == null) {
        textView = (TextView) inflater.inflate(R.layout.material_clockface_textview, this, false);
        textViewPool.put(i, textView);
        addView(textView);
      }

      textView.setText(values[i]);
      textView.setTag(R.id.material_value_index, i);

      int level = (i / INITIAL_CAPACITY) + LEVEL_1;
      textView.setTag(R.id.material_clock_level, level);
      if (level > LEVEL_1) {
        isMultiLevel = true;
      }

      ViewCompat.setAccessibilityDelegate(textView, valueAccessibilityDelegate);

      textView.setTextColor(textColor);
      if (contentDescription != 0) {
        Resources res = getResources();
        textView.setContentDescription(res.getString(contentDescription, values[i]));
      }
    }

    clockHandView.setMultiLevel(isMultiLevel);
  }

  @Override
  protected void updateLayoutParams() {
    super.updateLayoutParams();
    for (int i = 0; i < textViewPool.size(); ++i) {
      textViewPool.get(i).setVisibility(VISIBLE);
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
    TextView selected = getSelectedTextView(selectorBox);
    for (int i = 0; i < textViewPool.size(); ++i) {
      TextView tv = textViewPool.get(i);
      if (tv == null) {
        continue;
      }

      // set selection
      tv.setSelected(tv == selected);

      // set gradient
      RadialGradient radialGradient = getGradientForTextView(selectorBox, tv);
      tv.getPaint().setShader(radialGradient);
      tv.invalidate();
    }
  }

  @Nullable
  private TextView getSelectedTextView(RectF selectorBox) {
    float minArea = Float.MAX_VALUE;
    TextView selected = null;

    for (int i = 0; i < textViewPool.size(); ++i) {
      TextView tv = textViewPool.get(i);
      if (tv == null) {
        continue;
      }
      tv.getHitRect(textViewRect);
      scratch.set(textViewRect);
      scratch.union(selectorBox);
      float area = scratch.width() * scratch.height();
      if (area < minArea) { // the smallest enclosing rectangle is the selection (most overlap)
        minArea = area;
        selected = tv;
      }
    }

    return selected;
  }

  @Nullable
  private RadialGradient getGradientForTextView(RectF selectorBox, TextView tv) {
    tv.getHitRect(textViewRect);
    scratch.set(textViewRect);
    tv.getLineBounds(0, scratchLineBounds);
    scratch.inset(scratchLineBounds.left, scratchLineBounds.top);
    if (!RectF.intersects(selectorBox, scratch)) {
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

  @Override
  protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    Resources r = getResources();
    DisplayMetrics displayMetrics = r.getDisplayMetrics();

    float height = displayMetrics.heightPixels;
    float width = displayMetrics.widthPixels;

    // If the screen is smaller than our defined values. Scale the clock face
    // proportionally to the smaller size
    int size = (int) (clockSize / max3(minimumHeight / height, minimumWidth / width, 1f));

    int widthSpecMode = MeasureSpec.getMode(widthMeasureSpec);
    if (widthSpecMode != MeasureSpec.UNSPECIFIED) {
      size = Math.min(size, MeasureSpec.getSize(widthMeasureSpec));
    }

    int heightSpecMode = MeasureSpec.getMode(heightMeasureSpec);
    if (heightSpecMode != MeasureSpec.UNSPECIFIED) {
      size = Math.min(size, MeasureSpec.getSize(heightMeasureSpec));
    }

    int spec = MeasureSpec.makeMeasureSpec(size, MeasureSpec.EXACTLY);
    int circleRadius = size / 2 - clockHandView.getSelectorRadius() - clockHandPadding;
    if (circleRadius != getRadius()) {
      setRadius(circleRadius);
    }
    super.onMeasure(spec, spec);
  }

  private int getSelectedIndex() {
    for (int i = 0; i < textViewPool.size(); i++) {
      TextView textView = textViewPool.valueAt(i);
      if (textView.isSelected()) {
        return (int) textView.getTag(R.id.material_value_index);
      }
    }
    return -1;
  }

  @Override
  public boolean onKeyDown(int keyCode, KeyEvent event) {
    int selectedIndex = getSelectedIndex();
    if (!isShown() || selectedIndex == -1) {
      return super.onKeyDown(keyCode, event);
    }

    int nextIndex;
    switch (keyCode) {
      case KeyEvent.KEYCODE_DPAD_RIGHT:
      case KeyEvent.KEYCODE_DPAD_UP:
        nextIndex = (selectedIndex + 1) % values.length;
        break;
      case KeyEvent.KEYCODE_DPAD_LEFT:
      case KeyEvent.KEYCODE_DPAD_DOWN:
        nextIndex = (selectedIndex - 1 + values.length) % values.length;
        break;
      case KeyEvent.KEYCODE_ENTER:
      case KeyEvent.KEYCODE_DPAD_CENTER:
        if (onEnterKeyPressedListener != null) {
          onEnterKeyPressedListener.onEnterKeyPressed();
        }
        return true;
      default:
        return super.onKeyDown(keyCode, event);
    }

    if (nextIndex != selectedIndex) {
      int level = (nextIndex / INITIAL_CAPACITY) + LEVEL_1;
      if (level != getCurrentLevel()) {
        setCurrentLevel(level);
      }

      float rotation = (nextIndex % INITIAL_CAPACITY) * (360f / INITIAL_CAPACITY);
      setHandRotation(rotation);
      return true;
    }

    return super.onKeyDown(keyCode, event);
  }

  private static float max3(float a, float b, float c) {
    return max(max(a, b), c);
  }

  @Level
  int getCurrentLevel() {
    return clockHandView.getCurrentLevel();
  }

  void setCurrentLevel(@Level int level) {
    clockHandView.setCurrentLevel(level);
  }

  public void setOnEnterKeyPressedListener(OnEnterKeyPressedListener onEnterKeyPressedListener) {
    this.onEnterKeyPressedListener = onEnterKeyPressedListener;
  }

  /** Listener interface for enter key press events on the clock face. */
  interface OnEnterKeyPressedListener {
    /** Called when the enter key is pressed. */
    void onEnterKeyPressed();
  }
}
