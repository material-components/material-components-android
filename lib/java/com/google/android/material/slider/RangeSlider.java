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
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import androidx.annotation.Dimension;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ThemeEnforcement;
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
 * <p>{@code app:minSeparation}: <b>Optional.</b> The minimum distance between two thumbs that would
 * otherwise overlap.
 *
 * <p>For more information, see the <a
 * href="https://github.com/material-components/material-components-android/blob/master/docs/components/Slider.md">component
 * developer guidance</a> and <a href="https://material.io/components/sliders/overview">design
 * guidelines</a>.
 *
 * @attr ref com.google.android.material.R.styleable#RangeSlider_values
 * @attr ref com.google.android.material.R.styleable#RangeSlider_minSeparation
 */
public class RangeSlider extends BaseSlider<RangeSlider, OnChangeListener, OnSliderTouchListener> {

  private float minSeparation;
  private int separationUnit;

  public RangeSlider(@NonNull Context context) {
    this(context, null);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.sliderStyle);
  }

  public RangeSlider(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context, attrs, R.styleable.RangeSlider, defStyleAttr, DEF_STYLE_RES);
    if (a.hasValue(R.styleable.RangeSlider_values)) {
      int valuesId = a.getResourceId(R.styleable.RangeSlider_values, 0);
      TypedArray values = a.getResources().obtainTypedArray(valuesId);
      setValues(convertToFloat(values));
    }

    minSeparation = a.getDimension(R.styleable.RangeSlider_minSeparation, 0);
    a.recycle();
  }

  /**
   * Interface definition for a callback invoked when a slider's value is changed. This is called
   * for all existing values.
   *
   * To check all the current values, use {@see RangeSlider#getValues()}.
   */
  public interface OnChangeListener extends BaseOnChangeListener<RangeSlider> {
    @Override
    void onValueChange(@NonNull RangeSlider slider, float value, boolean fromUser);
  }

  /** Interface definition for a callback invoked when a slider's value is changed. */
  public interface OnSliderTouchListener extends BaseOnSliderTouchListener<RangeSlider> {
    @Override
    void onStartTrackingTouch(@NonNull RangeSlider slider);

    @Override
    void onStopTrackingTouch(@NonNull RangeSlider slider);
  }

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

  /** Returns current values of the slider thumbs. */
  @NonNull
  @Override
  public List<Float> getValues() {
    return super.getValues();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCustomThumbDrawable(@DrawableRes int drawableResId) {
    super.setCustomThumbDrawable(drawableResId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCustomThumbDrawable(@NonNull Drawable drawable) {
    super.setCustomThumbDrawable(drawable);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCustomThumbDrawablesForValues(@NonNull @DrawableRes int... drawableResIds) {
    super.setCustomThumbDrawablesForValues(drawableResIds);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void setCustomThumbDrawablesForValues(@NonNull Drawable... drawables) {
    super.setCustomThumbDrawablesForValues(drawables);
  }

  private static List<Float> convertToFloat(TypedArray values) {
    List<Float> ret = new ArrayList<>();
    for (int i = 0; i < values.length(); ++i) {
      ret.add(values.getFloat(i, -1));
    }
    return ret;
  }

  /**
   * Returns the minimum separation between two thumbs
   *
   * @see #setMinSeparation(float)
   * @attr ref com.google.android.material.R.styleable#RangeSlider_minSeparation
   */
  @Override
  public float getMinSeparation() {
    return minSeparation;
  }

  /**
   * Sets the minimum separation between two thumbs
   *
   * @see #getMinSeparation()
   * @attr ref com.google.android.material.R.styleable#RangeSlider_minSeparation
   * @throws IllegalStateException if used in conjunction with {@link #setStepSize(float)}, use
   *     {@link #setMinSeparationValue(float)} instead.
   */
  public void setMinSeparation(@Dimension float minSeparation) {
    this.minSeparation = minSeparation;
    separationUnit = UNIT_PX;
    setSeparationUnit(separationUnit);
  }

  /**
   * Sets the minimum separation in the value scale. Useful to create minimum ranges, between
   * thumbs.
   *
   * @see #getMinSeparation()
   * @see #setMinSeparation(float)
   * @attr ref com.google.android.material.R.styleable#RangeSlider_minSeparation
   */
  public void setMinSeparationValue(float minSeparation) {
    this.minSeparation = minSeparation;
    separationUnit = UNIT_VALUE;
    setSeparationUnit(separationUnit);
  }

  @Override
  @NonNull
  public Parcelable onSaveInstanceState() {
    Parcelable superState = super.onSaveInstanceState();

    RangeSliderState sliderState = new RangeSliderState(superState);
    sliderState.minSeparation = this.minSeparation;
    sliderState.separationUnit = this.separationUnit;

    return sliderState;
  }

  @Override
  protected void onRestoreInstanceState(@Nullable Parcelable state) {
    RangeSliderState savedState = (RangeSliderState) state;
    super.onRestoreInstanceState(savedState.getSuperState());

    this.minSeparation = savedState.minSeparation;
    this.separationUnit = savedState.separationUnit;
    setSeparationUnit(separationUnit);
  }

  static class RangeSliderState extends AbsSavedState {

    private float minSeparation;
    private int separationUnit;

    RangeSliderState(Parcelable superState) {
      super(superState);
    }

    private RangeSliderState(Parcel in) {
      super((Parcelable) in.readParcelable(RangeSliderState.class.getClassLoader()));
      minSeparation = in.readFloat();
      separationUnit = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
      super.writeToParcel(out, flags);
      out.writeFloat(minSeparation);
      out.writeInt(separationUnit);
    }

    public static final Parcelable.Creator<RangeSliderState> CREATOR =
        new Parcelable.Creator<RangeSliderState>() {
          @Override
          public RangeSliderState createFromParcel(Parcel in) {
            return new RangeSliderState(in);
          }

          @Override
          public RangeSliderState[] newArray(int size) {
            return new RangeSliderState[size];
          }
        };
  }
}
