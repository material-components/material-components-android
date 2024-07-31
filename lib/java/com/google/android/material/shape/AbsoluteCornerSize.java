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

import android.graphics.RectF;
import androidx.annotation.NonNull;
import java.util.Arrays;

/** A {@link CornerSize} that always uses the provided size and ignores the bounds. */
public final class AbsoluteCornerSize implements CornerSize {

  private final float size;

  public AbsoluteCornerSize(float size) {
    this.size = size;
  }

  @Override
  public float getCornerSize(@NonNull RectF bounds) {
    return size;
  }

  /**
   * Returns the size of this corner. Bounds aren't required since the result is always the same.
   */
  public float getCornerSize() {
    return size;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof AbsoluteCornerSize)) {
      return false;
    }
    AbsoluteCornerSize that = (AbsoluteCornerSize) o;
    return size == that.size;
  }

  @Override
  public int hashCode() {
    Object[] hashedFields = {size};
    return Arrays.hashCode(hashedFields);
  }

  @Override
  public String toString() {
    return getCornerSize() + "px";
  }
}
