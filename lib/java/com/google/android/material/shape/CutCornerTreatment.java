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

/** A corner treatment which cuts or clips the original corner of a shape with a straight line. */
public class CutCornerTreatment extends CornerTreatment {

  float size = -1;

  public CutCornerTreatment() {}

  /**
   * Instantiates a cut corner treatment of a given size. A cut corner treatment introduces two new
   * corners to a shape, produced by a straight line drawn between two points {@param size} pixels
   * away, on the vertical and horizontal axes, from the rectilinear (original) corner of the shape.
   * Stated another way, if the rectilinear (original) corner of the shape was at co-ordinates (0,
   * 0), the new corners are at co-ordinates (size, 0) and (0, size), and a straight line is drawn
   * between them.
   *
   * @param size the length in pixels that the new corners will be drawn away from the origin.
   * @deprecated Set the size using the {@link ShapeAppearanceModel.Builder}
   */
  @Deprecated
  public CutCornerTreatment(float size) {
    this.size = size;
  }

  @Override
  public void getCornerPath(
      @NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
    radius *= interpolation;
    shapePath.reset(0, radius, ShapePath.ANGLE_LEFT, 180 - angle);
    shapePath.lineTo(
        (float) (Math.sin(Math.toRadians(angle)) * radius),
        // Something about using cos() is causing rounding which prevents the path from being convex
        // on api levels 21 and 22. Using sin() with 90 - angle is helping for now.
        (float) (Math.sin(Math.toRadians(90 - angle)) * radius));
  }
}
