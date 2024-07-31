/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.shape;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static java.lang.Math.min;

import android.graphics.RectF;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.Arrays;

/**
 * A {@link CornerSize} that takes a percent and computes the size used based on the length of the
 * shortest edge adjacent to the corner.
 */
public final class RelativeCornerSize implements CornerSize {

  private final float percent;

  /**
   * Creates a relative corner size from a given corner size.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public static RelativeCornerSize createFromCornerSize(
      @NonNull final RectF bounds, @NonNull final CornerSize cornerSize) {
    return cornerSize instanceof RelativeCornerSize
        ? (RelativeCornerSize) cornerSize
        : new RelativeCornerSize(cornerSize.getCornerSize(bounds) / getMaxCornerSize(bounds));
  }

  private static float getMaxCornerSize(@NonNull RectF bounds) {
    return min(bounds.width(), bounds.height());
  }

  /**
   * @param percent The relative size of the corner in range [0,1] where 0 is no size and 1 is the
   *     largest possible corner size.
   */
  public RelativeCornerSize(@FloatRange(from = 0.0f, to = 1.0f) float percent) {
    this.percent = percent;
  }

  /** Returns the relative percent used for this {@link CornerSize} in range [0,1]. */
  @FloatRange(from = 0.0f, to = 1.0f)
  public float getRelativePercent() {
    return percent;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return percent * getMaxCornerSize(bounds);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof RelativeCornerSize)) {
      return false;
    }
    RelativeCornerSize that = (RelativeCornerSize) o;
    return percent == that.percent;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {percent};
    return Arrays.hashCode(hashedFields);
  }

  @Override
  public String toString() {
    return (int) (getRelativePercent() * 100) + "%";
  }
}
