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
import android.content.res.TypedArray;
import android.util.AttributeSet;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.internal.ThemeEnforcement;
import com.google.android.material.progressindicator.LinearProgressIndicator.HideBehavior;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndeterminateAnimationType;
import com.google.android.material.progressindicator.LinearProgressIndicator.IndicatorDirection;
import com.google.android.material.progressindicator.LinearProgressIndicator.ShowBehavior;
import com.google.android.material.progressindicator.ProgressIndicator.GrowMode;

/**
 * This class contains the parameters for drawing a linear type progress indicator. The parameters
 * reflect the attributes defined in {@link R.styleable.BaseProgressIndicator} and {@link
 * R.styleable.LinearProgressIndicator}.
 */
public class LinearProgressIndicatorSpec implements AnimatedVisibilityChangeBehavior {

  private final BaseProgressIndicatorSpec baseSpec;

  /** The type of animation of indeterminate mode. */
  @IndeterminateAnimationType public int indeterminateAnimationType;

  /** The direction in which the indicator will swipe or grow to. */
  @IndicatorDirection public int indicatorDirection;

  /** The animation direction to show the indicator and track. */
  @ShowBehavior public int showBehavior;

  /** The animation direction to hide the indicator and track. */
  @HideBehavior public int hideBehavior;

  protected boolean drawHorizontallyInverse;

  public LinearProgressIndicatorSpec(@NonNull ProgressIndicatorSpec progressIndicatorSpec) {
    // TODO(b/169262029) Remove this constructor once ProgressIndicator is removed.
    if (progressIndicatorSpec.indicatorType != ProgressIndicator.LINEAR) {
      throw new IllegalArgumentException(
          "Only LINEAR type ProgressIndicatorSpec can be converted into "
              + "LinearProgressIndicatorSpec");
    }
    baseSpec = progressIndicatorSpec.getBaseSpec();
    indeterminateAnimationType =
        getIndeterminateAnimationTypeFromLinearSeamless(progressIndicatorSpec.linearSeamless);
    indicatorDirection = getIndicatorDirectionFromInverse(progressIndicatorSpec.inverse);
    showBehavior = getShowBehaviorFromGrowMode(progressIndicatorSpec.growMode);
    hideBehavior = getHideBehaviorFromGrowMode(progressIndicatorSpec.growMode);

    drawHorizontallyInverse =
        indicatorDirection == LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT;
  }

  /**
   * Instantiates LinearProgressIndicator.
   *
   * <p>If attributes in {@link R.styleable#LinearProgressIndicator} are missing, the values in the
   * default style {@link R.style#Widget_MaterialComponents_LinearProgressIndicator} will be loaded.
   * Attributes defined in {@link R.styleable#BaseProgressIndicator} will be loaded by {@link
   * BaseProgressIndicatorSpec#BaseProgressIndicatorSpec(Context, AttributeSet, int)}.
   *
   * <p>If there's an existing {@link BaseProgressIndicatorSpec}, please use {@link
   * #LinearProgressIndicatorSpec(Context, AttributeSet, BaseProgressIndicatorSpec)}.
   *
   * @param context Current themed context.
   * @param attrs Component's attributes set.
   */
  public LinearProgressIndicatorSpec(@NonNull Context context, @Nullable AttributeSet attrs) {
    this.baseSpec =
        new BaseProgressIndicatorSpec(context, attrs, R.attr.linearProgressIndicatorStyle);
    loadSpecFromAttributes(context, attrs);
  }

  public LinearProgressIndicatorSpec(
      @NonNull Context context,
      @Nullable AttributeSet attrs,
      @NonNull BaseProgressIndicatorSpec baseSpec) {
    this.baseSpec = baseSpec;
    loadSpecFromAttributes(context, attrs);
  }

  private void loadSpecFromAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    loadAttributes(context, attrs);
    validateSpec();
    drawHorizontallyInverse =
        indicatorDirection == LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT;
  }

  private void loadAttributes(@NonNull Context context, @Nullable AttributeSet attrs) {
    TypedArray a =
        ThemeEnforcement.obtainStyledAttributes(
            context,
            attrs,
            R.styleable.LinearProgressIndicator,
            R.attr.linearProgressIndicatorStyle,
            LinearProgressIndicator.DEF_STYLE_RES);
    indeterminateAnimationType =
        a.getInt(
            R.styleable.LinearProgressIndicator_indeterminateAnimationType,
            LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_SPACING);
    indicatorDirection =
        a.getInt(
            R.styleable.LinearProgressIndicator_indicatorDirectionLinear,
            LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT);
    showBehavior =
        a.getInt(
            R.styleable.LinearProgressIndicator_showBehaviorLinear,
            LinearProgressIndicator.SHOW_NONE);
    hideBehavior =
        a.getInt(
            R.styleable.LinearProgressIndicator_hideBehaviorLinear,
            LinearProgressIndicator.HIDE_NONE);
    a.recycle();
  }

  protected void validateSpec() {
    if (indeterminateAnimationType
        == LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_SEAMLESS) {
      if (baseSpec.indicatorCornerRadius > 0) {
        // Throws an exception if trying to use cornered indicator with seamless indeterminate
        // animation type.
        throw new IllegalArgumentException(
            "Rounded corners are not supported in seamless indeterminate animation.");
      }
      if (baseSpec.indicatorColors.length < 3) {
        // Throws an exception if trying to set seamless indeterminate animation with less than 3
        // indicator colors.
        throw new IllegalArgumentException(
            "Seamless indeterminate animation must be used with 3 or more indicator colors.");
      }
    }
  }

  @Override
  public boolean shouldAnimateToShow() {
    return showBehavior != LinearProgressIndicator.SHOW_NONE;
  }

  @Override
  public boolean shouldAnimateToHide() {
    return hideBehavior != LinearProgressIndicator.HIDE_NONE;
  }

  @NonNull
  public BaseProgressIndicatorSpec getBaseSpec() {
    return baseSpec;
  }

  // **************** Temporary methods ****************

  // TODO(b/169262029) Remove once ProgressIndicator is removed.
  @IndeterminateAnimationType
  protected static int getIndeterminateAnimationTypeFromLinearSeamless(boolean linearSeamless) {
    return linearSeamless
        ? LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_SEAMLESS
        : LinearProgressIndicator.INDETERMINATE_ANIMATION_TYPE_SPACING;
  }

  // TODO(b/169262029) Remove once ProgressIndicator is removed.
  @IndicatorDirection
  protected static int getIndicatorDirectionFromInverse(boolean inverse) {
    return inverse
        ? LinearProgressIndicator.INDICATOR_DIRECTION_RIGHT_TO_LEFT
        : LinearProgressIndicator.INDICATOR_DIRECTION_LEFT_TO_RIGHT;
  }

  // TODO(b/169262029) Remove once ProgressIndicator is removed.
  @ShowBehavior
  protected static int getShowBehaviorFromGrowMode(@GrowMode int growMode) {
    switch (growMode) {
      case ProgressIndicator.GROW_MODE_INCOMING:
        return LinearProgressIndicator.SHOW_DOWNWARD;
      case ProgressIndicator.GROW_MODE_OUTGOING:
        return LinearProgressIndicator.SHOW_UPWARD;
      default:
        return LinearProgressIndicator.SHOW_NONE;
    }
  }

  // TODO(b/169262029) Remove once ProgressIndicator is removed.
  @HideBehavior
  protected static int getHideBehaviorFromGrowMode(@GrowMode int growMode) {
    switch (growMode) {
      case ProgressIndicator.GROW_MODE_INCOMING:
        return LinearProgressIndicator.HIDE_UPWARD;
      case ProgressIndicator.GROW_MODE_OUTGOING:
        return LinearProgressIndicator.HIDE_DOWNWARD;
      default:
        return LinearProgressIndicator.HIDE_NONE;
    }
  }
}
