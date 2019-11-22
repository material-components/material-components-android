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

import androidx.annotation.NonNull;

/** Offsets a different edge treatment by the given amount. */
public final class OffsetEdgeTreatment extends EdgeTreatment {

  private final EdgeTreatment other;
  private final float offset;

  public OffsetEdgeTreatment(@NonNull EdgeTreatment other, float offset) {
    this.other = other;
    this.offset = offset;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    other.getEdgePath(length, center - offset, interpolation, shapePath);
  }

  @Override
  boolean forceIntersection() {
    return other.forceIntersection();
  }
}
