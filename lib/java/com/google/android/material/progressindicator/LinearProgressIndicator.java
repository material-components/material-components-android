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

import android.content.Context;
import android.graphics.drawable.Drawable;
import androidx.core.view.ViewCompat;
import android.util.AttributeSet;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * This class implements the linear type progress indicators.
 *
 * <p>With the default style {@link R.style#Widget_MaterialComponents_LinearProgressIndicator}, 4dp
 * indicator/track thickness is used without animation is used for visibility change. Without
 * customization, primaryColor will be used as the indicator color; the track is the (first)
 * indicator color applying the disabledAlpha. The following attributes can be used to customize the
 * component's appearance:
 *
 * <ul>
 *   <li>{@code trackThickness}: the thickness of the indicator and track.
 *   <li>{@code indicatorColor}: the color(s) of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code trackCornerRadius}: the radius of the rounded corner of the indicator and track.
 *   <li>{@code indeterminateAnimationType}: the type of indeterminate animation.
 *   <li>{@code indicatorDirectionLinear}: the sweeping direction of the indicator.
 * </ul>
 */
public final class LinearProgressIndicator
    extends BaseProgressIndicator<LinearProgressIndicatorSpec> {
  public static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_LinearProgressIndicator;

  public static final int INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS = 0;
  public static final int INDETERMINATE_ANIMATION_TYPE_DISJOINT = 1;

  public static final int INDICATOR_DIRECTION_LEFT_TO_RIGHT = 0;
  public static final int INDICATOR_DIRECTION_RIGHT_TO_LEFT = 1;
  public static final int INDICATOR_DIRECTION_START_TO_END = 2;
  public static final int INDICATOR_DIRECTION_END_TO_START = 3;

  // **************** Constructors ****************

  public LinearProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  public LinearProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.linearProgressIndicatorStyle);
  }

  public LinearProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    super(context, attrs, defStyleAttr, DEF_STYLE_RES);

    initializeDrawables();
  }

  // **************** Inherited functions ****************

  @Override
  LinearProgressIndicatorSpec createSpec(@NonNull Context context, @NonNull AttributeSet attrs) {
    return new LinearProgressIndicatorSpec(context, attrs);
  }

  @Override
  protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
    super.onLayout(changed, left, top, right, bottom);

    // In case that layout direction is changed, update the spec.
    spec.drawHorizontallyInverse =
        spec.indicatorDirection == INDICATOR_DIRECTION_RIGHT_TO_LEFT
            || (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                && spec.indicatorDirection == INDICATOR_DIRECTION_START_TO_END)
            || (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
                && spec.indicatorDirection == INDICATOR_DIRECTION_END_TO_START);
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    int contentWidth = w - (getPaddingLeft() + getPaddingRight());
    int contentHeight = h - (getPaddingTop() + getPaddingBottom());
    Drawable drawable = getIndeterminateDrawable();
    if (drawable != null) {
      drawable.setBounds(/*left=*/ 0, /*top=*/ 0, contentWidth, contentHeight);
    }
    drawable = getProgressDrawable();
    if (drawable != null) {
      drawable.setBounds(/*left=*/ 0, /*top=*/ 0, contentWidth, contentHeight);
    }
  }

  // ******************** Initialization **********************

  private void initializeDrawables() {
    setIndeterminateDrawable(IndeterminateDrawable.createLinearDrawable(getContext(), spec));
    setProgressDrawable(DeterminateDrawable.createLinearDrawable(getContext(), spec));
  }

  // **************** Getters and setters ****************

  /**
   * Sets the colors used in the indicator of this progress indicator.
   *
   * @param indicatorColors The new colors used in indicator.
   * @throws IllegalArgumentException if there are less than 3 indicator colors when
   *     indeterminateAnimationType is {@link #INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS}.
   */
  @Override
  public void setIndicatorColor(@NonNull int... indicatorColors) {
    super.setIndicatorColor(indicatorColors);
    spec.validateSpec();
  }

  /**
   * Sets the radius of the rounded corner for the indicator and track in pixels.
   *
   * @param trackCornerRadius The new corner radius in pixels.
   * @throws IllegalArgumentException if trackCornerRadius is not zero, when
   *     indeterminateAnimationType is {@link #INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS}.
   */
  @Override
  public void setTrackCornerRadius(int trackCornerRadius) {
    super.setTrackCornerRadius(trackCornerRadius);
    spec.validateSpec();
    invalidate();
  }

  /**
   * Returns the type of indeterminate animation of this progress indicator.
   *
   * @see #setIndeterminateAnimationType(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_indeterminateAnimationType
   */
  public int getIndeterminateAnimationType() {
    return spec.indeterminateAnimationType;
  }

  /**
   * Sets the type of indeterminate animation.
   *
   * @param indeterminateAnimationType The new type of indeterminate animation.
   * @see #getIndeterminateAnimationType()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_indeterminateAnimationType
   */
  public void setIndeterminateAnimationType(
      @IndeterminateAnimationType int indeterminateAnimationType) {
    if (spec.indeterminateAnimationType == indeterminateAnimationType) {
      return;
    }
    if (visibleToUser() && isIndeterminate()) {
      throw new IllegalStateException(
          "Cannot change indeterminate animation type while the progress indicator is show in"
              + " indeterminate mode.");
    }
    spec.indeterminateAnimationType = indeterminateAnimationType;
    spec.validateSpec();
    if (indeterminateAnimationType == INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS) {
      getIndeterminateDrawable()
          .setAnimatorDelegate(new LinearIndeterminateContiguousAnimatorDelegate(spec));
    } else {
      getIndeterminateDrawable()
          .setAnimatorDelegate(new LinearIndeterminateDisjointAnimatorDelegate(getContext(), spec));
    }
    invalidate();
  }

  /**
   * Returns the indicator animating direction used in this progress indicator.
   *
   * @see #setIndicatorDirection(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_indicatorDirectionLinear
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
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_indicatorDirectionLinear
   */
  public void setIndicatorDirection(@IndicatorDirection int indicatorDirection) {
    spec.indicatorDirection = indicatorDirection;
    spec.drawHorizontallyInverse =
        indicatorDirection == INDICATOR_DIRECTION_RIGHT_TO_LEFT
            || (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL
                && spec.indicatorDirection == INDICATOR_DIRECTION_START_TO_END)
            || (ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_LTR
                && indicatorDirection == INDICATOR_DIRECTION_END_TO_START);
    invalidate();
  }

  /**
   * Sets the current progress to the specified value with/without animation based on the input.
   *
   * <p>If it's in the indeterminate mode and using disjoint animation, it will smoothly transition
   * to determinate mode by finishing the current indeterminate animation cycle.
   *
   * @param progress The new progress value.
   * @param animated Whether to update the progress with the animation.
   * @see BaseProgressIndicator#setProgress(int)
   */
  @Override
  public void setProgressCompat(int progress, boolean animated) {
    // Doesn't support to switching into determinate mode while disjoint animation is used.
    if (spec != null
        && spec.indeterminateAnimationType == INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS
        && isIndeterminate()) {
      return;
    }
    super.setProgressCompat(progress, animated);
  }

  // **************** Interface ****************

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({INDETERMINATE_ANIMATION_TYPE_CONTIGUOUS, INDETERMINATE_ANIMATION_TYPE_DISJOINT})
  @Retention(RetentionPolicy.SOURCE)
  public @interface IndeterminateAnimationType {}

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({
    INDICATOR_DIRECTION_LEFT_TO_RIGHT,
    INDICATOR_DIRECTION_RIGHT_TO_LEFT,
    INDICATOR_DIRECTION_START_TO_END,
    INDICATOR_DIRECTION_END_TO_START
  })
  @Retention(RetentionPolicy.SOURCE)
  public @interface IndicatorDirection {}
}
