/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.transition;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.os.Build.VERSION_CODES;
import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import android.transition.Transition;
import android.transition.TransitionSet;
import android.view.Gravity;
import com.google.android.material.animation.AnimationUtils;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link TransitionSet} that provides shared motion along an axis.
 *
 * <p>When configured along the {@link #X} axis, this transition slides and fades in the target when
 * appearing and slides and fades out the target when disappearing.
 *
 * <p>When configured along the {@link #Y} axis, this transition slides and fades in the target when
 * appearing and slides and fades out the target when disappearing.
 *
 * <p>When configured along the {@link #Z} axis, this transition scales and fades in when the target
 * is appearing and scales and fades out when the target is disappearing.
 *
 * <p>The direction of the slide or scale is determined by the {@link #create(Context, int,
 * boolean)} method's forward property. When true, the target will slide to the left on the X axis,
 * up on the Y axis and out in on the Z axis. When false, the target will slide to the right on the
 * X axis, down on the Y axis and in on the Z axis. Note that this is independent of whether or not
 * the target is appearing or disappearing.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP)
public class MaterialSharedAxis extends MaterialTransitionSet<Transition> {

  /**
   * Indicates that the x-axis should be shared for the transition, meaning a horizontal slide and
   * fade should be used.
   *
   * In the forward direction, targets of this transition will slide left.
   */
  public static final int X = 0;

  /**
   * Indicates that the y-axis should be shared for the transition, meaning a vertical slide and
   * fade should be used.
   *
   * In the forward direction, targets of this transition will slide up.
   */
  public static final int Y = 1;

  /**
   * Indicates that the z-axis should be shared for the transition, meaning a scale and fade should
   * be used.
   *
   * In the forward direction, targets of this transition will scale out.
   */
  public static final int Z = 2;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({X, Y, Z})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Axis {}

  @Axis private final int axis;
  private final boolean forward;

  /**
   * Construct a new instance of MaterialSharedAxis with a given {@link Axis} and direction.
   *
   * @param axis The axis in which this transition will animate.
   * @param forward True if the animation should move in the forward direction, false if it should
   *     move in the backward direction. Forward on the X axis is to the left, Y is up and Z is out.
   */
  @NonNull
  public static MaterialSharedAxis create(
      @NonNull Context context, @Axis int axis, boolean forward) {
    MaterialSharedAxis materialSharedAxis = new MaterialSharedAxis(axis, forward);
    materialSharedAxis.initialize(context);
    return materialSharedAxis;
  }

  private MaterialSharedAxis(@Axis int axis, boolean forward) {
    this.axis = axis;
    this.forward = forward;
    setInterpolator(AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR);
  }

  @NonNull
  @Override
  Transition getDefaultPrimaryTransition() {
    switch (axis) {
      case X:
        return new SlideDistance(context, forward ? Gravity.END : Gravity.START);
      case Y:
        return new SlideDistance(context, forward ? Gravity.BOTTOM : Gravity.TOP);
      case Z:
        return new Scale(forward);
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  @Nullable
  @Override
  Transition getDefaultSecondaryTransition() {
    return new FadeThrough();
  }

  @Axis
  public int getAxis() {
    return axis;
  }

  public boolean isEntering() {
    return forward;
  }
}
