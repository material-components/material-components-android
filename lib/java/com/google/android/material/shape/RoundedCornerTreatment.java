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

package com.google.android.material.shape;


import androidx.annotation.NonNull;

/** A corner treatment which rounds a corner of a shape. */
public class RoundedCornerTreatment extends CornerTreatment {

  float radius = -1;

  public RoundedCornerTreatment() {}

  /**
   * Instantiates a rounded corner treatment.
   *
   * @param radius the radius, in pixels, of the rounded corner, which is rendered as a quarter
   *     circle.
   * @deprecated Set the size using the {@link ShapeAppearanceModel.Builder}
   */
  @Deprecated
  public RoundedCornerTreatment(float radius) {
    this.radius = radius;
  }

  @Override
  public void getCornerPath(
      @NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
    radius *= interpolation;
    shapePath.reset(0, radius, ShapePath.ANGLE_LEFT, 180 - angle);
    shapePath.addArc(0, 0, 2 * radius, 2 * radius, 180, angle);
  }
}
