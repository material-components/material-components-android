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

/**
 * Draws an arrow on the edge given the radius of a circle. The arrow is drawn as two perpendicular
 * lines tangent to the circle. This allows the arrow to be drawn smoothly on a pill or circular
 * shape.
 *
 * <p>If you just want a triangle of a specific size use {@link TriangleEdgeTreatment} instead.
 */
public final class MarkerEdgeTreatment extends EdgeTreatment {

  private final float radius;

  public MarkerEdgeTreatment(float radius) {
    this.radius = radius - 0.001f;
  }

  @Override
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    float side = (float) (radius * Math.sqrt(2) / 2);
    float side2 = (float) Math.sqrt(Math.pow(radius, 2) - Math.pow(side, 2));
    shapePath.reset(center - side, (float) -(radius * Math.sqrt(2) - radius) + side2);
    shapePath.lineTo(center, (float) -(radius * Math.sqrt(2) - radius));
    shapePath.lineTo(center + side, (float) -(radius * Math.sqrt(2) - radius) + side2);
  }

  @Override
  boolean forceIntersection() {
    return true;
  }
}
