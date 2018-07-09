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

/** A corner treatment which rounds a corner of a shape. */
@Experimental("The shapes API is currently experimental and subject to change")
public class RoundedCornerTreatment extends CornerTreatment {

  private final float radius;

  /**
   * Instantiates a rounded corner treatment.
   *
   * @param radius the radius, in pixels, of the rounded corner, which is rendered as a quarter
   *     circle.
   */
  public RoundedCornerTreatment(float radius) {
    this.radius = radius;
  }

  @Override
  public void getCornerPath(float angle, float interpolation, ShapePath shapePath) {
    shapePath.reset(0, radius * interpolation);
    shapePath.addArc(0, 0, 2 * radius * interpolation, 2 * radius * interpolation, angle + 180, 90);
  }
}
