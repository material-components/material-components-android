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

/*
 * NOTE: THIS CLASS IS AUTO-GENERATED FROM THE EQUIVALENT CLASS IN THE PARENT TRANSITION PACKAGE.
 * IT SHOULD NOT BE EDITED DIRECTLY.
 */
package com.google.android.material.transition.platform;

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.view.Gravity;
import androidx.annotation.AttrRes;
import androidx.annotation.IntDef;
import androidx.annotation.RestrictTo;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * A {@link android.transition.Visibility} transition that provides shared motion along an axis.
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
 * <p>The direction of the slide or scale is determined by the constructors's forward property. When
 * true, the target will slide to the left on the X axis, up on the Y axis and out in on the Z axis.
 * When false, the target will slide to the right on the X axis, down on the Y axis and in on the Z
 * axis. Note that this is independent of whether or not the target is appearing or disappearing.
 *
 * <p>MaterialSharedAxis supports theme-based easing and duration. The transition will load theme
 * values from the {@code SceneRoot}'s context before it runs, and only use them if the
 * corresponding properties weren't already set on the transition instance.
 */
@androidx.annotation.RequiresApi(android.os.Build.VERSION_CODES.LOLLIPOP)
public final class MaterialSharedAxis extends MaterialVisibility<VisibilityAnimatorProvider> {

  /**
   * Indicates that the x-axis should be shared for the transition, meaning a horizontal slide and
   * fade should be used.
   *
   * <p>In the forward direction, targets of this transition will slide left.
   */
  public static final int X = 0;

  /**
   * Indicates that the y-axis should be shared for the transition, meaning a vertical slide and
   * fade should be used.
   *
   * <p>In the forward direction, targets of this transition will slide up.
   */
  public static final int Y = 1;

  /**
   * Indicates that the z-axis should be shared for the transition, meaning a scale and fade should
   * be used.
   *
   * <p>In the forward direction, targets of this transition will scale out.
   */
  public static final int Z = 2;

  /** @hide */
  @RestrictTo(LIBRARY_GROUP)
  @IntDef({X, Y, Z})
  @Retention(RetentionPolicy.SOURCE)
  public @interface Axis {}

  @Axis private final int axis;
  private final boolean forward;

  @AttrRes private static final int DEFAULT_THEMED_DURATION_ATTR = R.attr.motionDurationLong1;

  @AttrRes
  private static final int DEFAULT_THEMED_EASING_ATTR = R.attr.motionEasingEmphasizedInterpolator;

  public MaterialSharedAxis(@Axis int axis, boolean forward) {
    super(createPrimaryAnimatorProvider(axis, forward), createSecondaryAnimatorProvider());
    this.axis = axis;
    this.forward = forward;
  }

  @Axis
  public int getAxis() {
    return axis;
  }

  public boolean isForward() {
    return forward;
  }

  private static VisibilityAnimatorProvider createPrimaryAnimatorProvider(
      @Axis int axis, boolean forward) {
    switch (axis) {
      case X:
        return new SlideDistanceProvider(forward ? Gravity.END : Gravity.START);
      case Y:
        return new SlideDistanceProvider(forward ? Gravity.BOTTOM : Gravity.TOP);
      case Z:
        return new ScaleProvider(forward);
      default:
        throw new IllegalArgumentException("Invalid axis: " + axis);
    }
  }

  private static VisibilityAnimatorProvider createSecondaryAnimatorProvider() {
    return new FadeThroughProvider();
  }

  @AttrRes
  @Override
  int getDurationThemeAttrResId(boolean appearing) {
    return DEFAULT_THEMED_DURATION_ATTR;
  }

  @AttrRes
  @Override
  int getEasingThemeAttrResId(boolean appearing) {
    return DEFAULT_THEMED_EASING_ATTR;
  }
}
