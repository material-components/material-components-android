/*
 * Copyright 2023 The Android Open Source Project
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
import static androidx.core.math.MathUtils.clamp;
import static java.lang.Math.min;

import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.Arrays;

/**
 * A {@link CornerSize} that takes a desired absolute corner size and clamps the value to be no
 * larger than half the length of the shortest edge (fully rounded/cut).
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class ClampedCornerSize implements CornerSize {

  private final float target;

  /**
   * Create a new {@link ClampedCornerSize} from an {@link AbsoluteCornerSize}.
   *
   * @param cornerSize the absolute corner size to clamp
   * @return a new clamped corner size
   */
  @NonNull
  public static ClampedCornerSize createFromCornerSize(
      @NonNull final AbsoluteCornerSize cornerSize) {
    return new ClampedCornerSize(cornerSize.getCornerSize());
  }

  private static float getMaxCornerSize(@NonNull RectF bounds) {
    return min(bounds.width() / 2F, bounds.height() / 2F);
  }

  public ClampedCornerSize(float target) {
    this.target = target;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return clamp(target, 0, getMaxCornerSize(bounds));
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClampedCornerSize)) {
      return false;
    }
    ClampedCornerSize that = (ClampedCornerSize) o;
    return target == that.target;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {target};
    return Arrays.hashCode(hashedFields);
  }
}
