/*
 * Copyright 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.design.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/** A representation of timing for an animation. */
public class MotionTiming {

  private long delay = 0;
  private long duration = 300;
  /** Set to an instance, or null for {@link AnimationUtils#FAST_OUT_SLOW_IN_INTERPOLATOR}. */
  @Nullable private TimeInterpolator interpolator = null;
  /** Set to 0, greater than 0, or {@link ValueAnimator#INFINITE}. */
  private int repeatCount = 0;
  /** Set to {@link ValueAnimator#RESTART} or {@link ValueAnimator#REVERSE}. */
  private int repeatMode = ValueAnimator.RESTART;

  public MotionTiming(long delay, long duration) {
    this.delay = delay;
    this.duration = duration;
  }

  public MotionTiming(long delay, long duration, @NonNull TimeInterpolator interpolator) {
    this.delay = delay;
    this.duration = duration;
    this.interpolator = interpolator;
  }

  public void apply(Animator animator) {
    animator.setStartDelay(getDelay());
    animator.setDuration(getDuration());
    animator.setInterpolator(getInterpolator());
    if (animator instanceof ValueAnimator) {
      ((ValueAnimator) animator).setRepeatCount(getRepeatCount());
      ((ValueAnimator) animator).setRepeatMode(getRepeatMode());
    }
  }

  public long getDelay() {
    return delay;
  }

  public long getDuration() {
    return duration;
  }

  public TimeInterpolator getInterpolator() {
    return interpolator != null ? interpolator : AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public int getRepeatMode() {
    return repeatMode;
  }
}
