/*
 * Copyright 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.slider;

import com.google.android.material.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.slider.RangeSlider.OnChangeListener;
import com.google.android.material.slider.RangeSlider.OnSliderTouchListener;
import java.util.ArrayList;
import java.util.List;

/**
 * A range slider can be used to select from either a continuous or a discrete set of values. The
 * default is to use a continuous range of values from valueFrom to valueTo.
 *
 * <p>{@inheritDoc}
 *
 * <p>{@code app:values}: <b>Optional.</b> The initial values of the range slider. If not specified,
 * the range slider will only have one value equal to {@code android:valueFrom}
 *
 * @attr ref com.google.android.material.R.styleable#RangeSlider_values
 */
public class RangeSlider extends BaseSlider<RangeSlider, OnChangeListener, OnSliderTouchListener> {

  public RangeSlider(@NonNull Context context) {
    this(context, null);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a = context.obtainStyledAttributes(attrs, new int[] {R.attr.values});

    if (a.hasValue(0)) {
      int valuesId = a.getResourceId(0, 0);
      TypedArray values = a.getResources().obtainTypedArray(valuesId);
      setValues(convertToFloat(values));
    }
    a.recycle();
  }

  /**
   * Interface definition for a callback invoked when a slider's value is changed. This is called
   * for all existing values to check all the current values use {@see RangeSlider#getValues()}
   */
  public interface OnChangeListener extends BaseOnChangeListener<RangeSlider> {}

  /** Interface definition for a callback invoked when a slider's value is changed. */
  public interface OnSliderTouchListener extends BaseOnSliderTouchListener<RangeSlider> {}

  /**
   * {@inheritDoc}
   *
   * @see #getValues()
   */
  @Override
  public void setValues(@NonNull Float... values) {
    super.setValues(values);
  }

  /**
   * {@inheritDoc}
   *
   * @see #getValues()
   */
  @Override
  public void setValues(@NonNull List<Float> values) {
    super.setValues(values);
  }

  /** Returns current values of the slider thumbs, sorted in ascending order. */
  @NonNull
  @Override
  public List<Float> getValues() {
    return super.getValues();
  }

  private static List<Float> convertToFloat(TypedArray values) {
    List<Float> ret = new ArrayList<>();
    for (int i = 0; i < values.length(); ++i) {
      ret.add(values.getFloat(i, -1));
    }
    return ret;
  }
}
