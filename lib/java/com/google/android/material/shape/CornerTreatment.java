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

import com.google.android.material.internal.Experimental;

/** A basic corner treatment (a single point which does not affect the shape). */
@Experimental("The shapes API is currently experimental and subject to change")
public class CornerTreatment {

  protected final float cornerSize;

  /** Default Constructor has no size. Using this treatment for all corners will draw a square */
  public CornerTreatment() {
    this.cornerSize = 0;
  }

  /**
   * Most CornerTreatments have a concept of corner size. This constructor is exposed for extending
   * classes.
   */
  protected CornerTreatment(float cornerSize) {
    this.cornerSize = cornerSize;
  }

  /**
   * Generates a {@link ShapePath} for this corner treatment.
   *
   * <p>CornerTreatments are assumed to have an origin of (0, 0) (i.e. they represent the top-left
   * corner), and are automatically rotated and scaled as necessary when applied to other corners.
   *
   * @param angle the angle of the corner, typically 90 degrees.
   * @param interpolation the interpolation of the corner treatment. Ranges between 0 (none) and 1
   *     (fully) interpolated. Custom corner treatments can implement interpolation to support shape
   *     transition between two arbitrary states. Typically, a value of 0 indicates that the custom
   *     corner treatment is not rendered (i.e. that it is a 90 degree angle), and a value of 1
   *     indicates that the treatment is fully rendered. Animation between these two values can
   *     "heal" or "reveal" a corner treatment.
   * @param shapePath the {@link ShapePath} that this treatment should write to.
   */
  public void getCornerPath(float angle, float interpolation, ShapePath shapePath) {}

  public float getCornerSize() {
    return cornerSize;
  }
}
