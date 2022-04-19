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

package com.google.android.material.progressindicator;

import com.google.android.material.R;

import static java.lang.Math.max;

import android.content.Context;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class implements the circular type progress indicators.
 *
 * <p>With the default style {@code Widget.MaterialComponents.CircularProgressIndicator}, 4dp
 * indicator/track thickness is used without animation for visibility change. Without customization,
 * primaryColor will be used as the indicator color; the track is transparent. The following
 * attributes can be used to customize the component's appearance:
 *
 * <ul>
 *   <li>{@code trackThickness}: the thickness of the indicator and track.
 *   <li>{@code indicatorColor}: the color(s) of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code trackCornerRadius}: the radius of the rounded corner of the indicator and track.
 *   <li>{@code indicatorSize}: the outer diameter of the spinner.
 *   <li>{@code indicatorInset}: the inset from component's bound to the spinner's outer edge.
 *   <li>{@code indicatorDirectionCircular}: the rotation direction of the spinner or indicator.
 * </ul>
 */
public final class CircularProgressIndicator
    extends BaseProgressIndicator<CircularProgressIndicatorSpec> {
  public static final int DEF_STYLE_RES =
      R.style.Widget_MaterialComponents_CircularProgressIndicator;

  public static final int INDICATOR_DIRECTION_CLOCKWISE = 0;
  public static final int INDICATOR_DIRECTION_COUNTERCLOCKWISE = 1;

  // **************** Constructors ****************

  public CircularProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  public CircularProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.circularProgressIndicatorStyle);
  }

  public CircularProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    super(context, attrs, defStyleAttr, CircularProgressIndicator.DEF_STYLE_RES);

    initializeDrawables();
  }

  // **************** Inherited functions ****************

  @Override
  CircularProgressIndicatorSpec createSpec(@NonNull Context context, @NonNull AttributeSet attrs) {
    return new CircularProgressIndicatorSpec(context, attrs);
  }

  // ******************** Initialization **********************

  private void initializeDrawables() {
    setIndeterminateDrawable(IndeterminateDrawable.createCircularDrawable(getContext(), spec));
    setProgressDrawable(DeterminateDrawable.createCircularDrawable(getContext(), spec));
  }

  // **************** Getters and setters ****************

  /**
   * Sets the track thickness of this progress indicator.
   *
   * @param trackThickness The new track/indicator thickness in pixel.
   * @see #getTrackThickness()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#BaseProgressIndicator_trackThickness
   * @throws IllegalArgumentException if indicator size is less than twice of the track thickness.
   */
  @Override
  public void setTrackThickness(int trackThickness) {
    super.setTrackThickness(trackThickness);
    spec.validateSpec();
  }

  /**
   * Returns the inset of this progress indicator.
   *
   * @see #setIndicatorInset(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorInset
   */
  @Px
  public int getIndicatorInset() {
    return spec.indicatorInset;
  }

  /**
   * Sets the inset of this progress indicator.
   *
   * @param indicatorInset The new inset in pixels.
   * @see #getIndicatorInset()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorInset
   */
  public void setIndicatorInset(@Px int indicatorInset) {
    if (spec.indicatorInset != indicatorInset) {
      spec.indicatorInset = indicatorInset;
      invalidate();
    }
  }

  /**
   * Returns the size (outer diameter) of this progress indicator.
   *
   * @see #setIndicatorSize(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorSize
   */
  @Px
  public int getIndicatorSize() {
    return spec.indicatorSize;
  }

  /**
   * Sets the size (outer diameter) of this circular progress indicator.
   *
   * @param indicatorSize The new size in pixels.
   * @see #getIndicatorSize()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorSize
   */
  public void setIndicatorSize(@Px int indicatorSize) {
    indicatorSize = max(indicatorSize, getTrackThickness() * 2);
    if (spec.indicatorSize != indicatorSize) {
      spec.indicatorSize = indicatorSize;
      spec.validateSpec();
      invalidate();
    }
  }

  /**
   * Returns the indicator animating direction used in this progress indicator.
   *
   * @see #setIndicatorDirection(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorDirectionCircular
   */
  @IndicatorDirection
  public int getIndicatorDirection() {
    return spec.indicatorDirection;
  }

  /**
   * Sets the indicator animating direction used in this progress indicator.
   *
   * @param indicatorDirection The new indicator direction.
   * @see #getIndicatorDirection()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#CircularProgressIndicator_indicatorDirectionCircular
   */
  public void setIndicatorDirection(@IndicatorDirection int indicatorDirection) {
    spec.indicatorDirection = indicatorDirection;
    invalidate();
  }

  // **************** Interface ****************

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({INDICATOR_DIRECTION_CLOCKWISE, INDICATOR_DIRECTION_COUNTERCLOCKWISE})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IndicatorDirection {}
}
