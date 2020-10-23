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
 * indicator/track size is used without animation is used for visibility change. Without
 * customization, primaryColor will be used as the indicator color; the track is the (first)
 * indicator color applying the disabledAlpha. The following attributes can be used to customize the
 * component's appearance:
 *
 * <ul>
 *   <li>{@code indicatorSize}: the stroke width of the indicator and track.
 *   <li>{@code indicatorColor}: the color of the indicator.
 *   <li>{@code trackColor}: the color of the track.
 *   <li>{@code indicatorCornerRadius}: the radius of the rounded corner of the indicator stroke.
 *   <li>{@code indeterminateAnimationType}: the type of indeterminate animation.
 *   <li>{@code indicatorDirectionLinear}: the sweeping direction of the indicator.
 *   <li>{@code showBehaviorLinear}: the animation direction to show the indicator and track.
 *   <li>{@code hideBehaviorLinear}: the animation direction to hide the indicator and track.
 * </ul>
 */
public class LinearProgressIndicator extends BaseProgressIndicator {
  public static final int DEF_STYLE_RES = R.style.Widget_MaterialComponents_LinearProgressIndicator;

  public static final int INDETERMINATE_ANIMATION_TYPE_SEAMLESS = 0;
  public static final int INDETERMINATE_ANIMATION_TYPE_SPACING = 1;

  public static final int INDICATOR_DIRECTION_LEFT_TO_RIGHT = 0;
  public static final int INDICATOR_DIRECTION_RIGHT_TO_LEFT = 1;
  public static final int INDICATOR_DIRECTION_START_TO_END = 2;
  public static final int INDICATOR_DIRECTION_END_TO_START = 3;

  public static final int SHOW_NONE = 0;
  public static final int SHOW_UPWARD = 1;
  public static final int SHOW_DOWNWARD = 2;
  public static final int HIDE_NONE = 0;
  public static final int HIDE_UPWARD = 1;
  public static final int HIDE_DOWNWARD = 2;

  protected final LinearProgressIndicatorSpec spec;

  // **************** Constructors ****************

  public LinearProgressIndicator(@NonNull Context context) {
    this(context, null);
  }

  public LinearProgressIndicator(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, R.attr.linearProgressIndicatorStyle);
  }

  public LinearProgressIndicator(
      @NonNull Context context, @Nullable AttributeSet attrs, @AttrRes final int defStyleAttr) {
    super(context, attrs, defStyleAttr);

    // Ensures that we are using the correctly themed context rather than the context that was
    // passed in.
    context = getContext();

    spec = new LinearProgressIndicatorSpec(context, attrs, baseSpec);
  }

  // **************** Getters and setters ****************

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
    invalidate();
  }

  /**
   * Returns the show behavior used in this progress indicator.
   *
   * @see #setShowBehavior(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_showBehaviorLinear
   */
  @ShowBehavior
  public int getShowBehavior() {
    return spec.showBehavior;
  }

  /**
   * Sets the show behavior used in this progress indicator.
   *
   * @param showBehavior The new behavior of show animation.
   * @see #getShowBehavior()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_showBehaviorLinear
   */
  public void setShowBehavior(@ShowBehavior int showBehavior) {
    spec.showBehavior = showBehavior;
    invalidate();
  }

  /**
   * Returns the hide behavior used in this progress indicator.
   *
   * @see #setHideBehavior(int)
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_hideBehaviorLinear
   */
  @HideBehavior
  public int getHideBehavior() {
    return spec.hideBehavior;
  }

  /**
   * Sets the hide behavior used in this progress indicator.
   *
   * @param hideBehavior The new behavior of hide animation.
   * @see #getHideBehavior()
   * @attr ref
   *     com.google.android.material.progressindicator.R.styleable#LinearProgressIndicator_hideBehaviorLinear
   */
  public void setHideBehavior(@HideBehavior int hideBehavior) {
    spec.hideBehavior = hideBehavior;
    invalidate();
  }

  // **************** Interface ****************

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({INDETERMINATE_ANIMATION_TYPE_SEAMLESS, INDETERMINATE_ANIMATION_TYPE_SPACING})
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

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({SHOW_NONE, SHOW_UPWARD, SHOW_DOWNWARD})
  @Retention(RetentionPolicy.SOURCE)
  public @interface ShowBehavior {}

  /** @hide */
  @RestrictTo(Scope.LIBRARY_GROUP)
  @IntDef({HIDE_NONE, HIDE_UPWARD, HIDE_DOWNWARD})
  @Retention(RetentionPolicy.SOURCE)
  public @interface HideBehavior {}
}
