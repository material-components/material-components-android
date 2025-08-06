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
package com.google.android.material.animation;

import android.animation.Animator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

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

  public void apply(@NonNull Animator animator) {
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

  @Nullable
  public TimeInterpolator getInterpolator() {
    return interpolator != null ? interpolator : AnimationUtils.FAST_OUT_SLOW_IN_INTERPOLATOR;
  }

  public int getRepeatCount() {
    return repeatCount;
  }

  public int getRepeatMode() {
    return repeatMode;
  }

  @NonNull
  static MotionTiming createFromAnimator(@NonNull ValueAnimator animator) {
    MotionTiming timing =
        new MotionTiming(
            animator.getStartDelay(), animator.getDuration(), animator.getInterpolator());
    timing.repeatCount = animator.getRepeatCount();
    timing.repeatMode = animator.getRepeatMode();
    return timing;
  }

  @Override
  public boolean equals(@Nullable Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof MotionTiming)) {
      return false;
    }

    MotionTiming that = (MotionTiming) o;

    if (getDelay() != that.getDelay()) {
      return false;
    }
    if (getDuration() != that.getDuration()) {
      return false;
    }
    if (getRepeatCount() != that.getRepeatCount()) {
      return false;
    }
    if (getRepeatMode() != that.getRepeatMode()) {
      return false;
    }
    return getInterpolator().getClass().equals(that.getInterpolator().getClass());
  }

  @Override
  public int hashCode() {
    int result = (int) (getDelay() ^ (getDelay() >>> 32));
    result = 31 * result + (int) (getDuration() ^ (getDuration() >>> 32));
    result = 31 * result + getInterpolator().getClass().hashCode();
    result = 31 * result + getRepeatCount();
    result = 31 * result + getRepeatMode();
    return result;
  }

  @NonNull
  @Override
  public String toString() {
    StringBuilder out = new StringBuilder();
    out.append('\n');
    out.append(getClass().getName());
    out.append('{');
    out.append(Integer.toHexString(System.identityHashCode(this)));
    out.append(" delay: ");
    out.append(getDelay());
    out.append(" duration: ");
    out.append(getDuration());
    out.append(" interpolator: ");
    out.append(getInterpolator().getClass());
    out.append(" repeatCount: ");
    out.append(getRepeatCount());
    out.append(" repeatMode: ");
    out.append(getRepeatMode());
    out.append("}\n");
    return out.toString();
  }
}
