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

import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import java.util.Arrays;

/**
 * Adjusts another {@link CornerSize} by some amount.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public final class AdjustedCornerSize implements CornerSize {

  private final CornerSize other;
  private final float adjustment;

  public AdjustedCornerSize(float adjustment, @NonNull CornerSize other) {
    // Rollup other Adjustments into this one
    while (other instanceof AdjustedCornerSize) {
      other = ((AdjustedCornerSize) other).other;
      adjustment += ((AdjustedCornerSize) other).adjustment;
    }

    this.other = other;
    this.adjustment = adjustment;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return Math.max(0, other.getCornerSize(bounds) + adjustment);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AdjustedCornerSize)) {
      return false;
    }
    AdjustedCornerSize that = (AdjustedCornerSize) o;
    return other.equals(that.other) && adjustment == that.adjustment;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {other, adjustment};
    return Arrays.hashCode(hashedFields);
  }
}
