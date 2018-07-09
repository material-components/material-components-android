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

/**
 * An edge treatment which draws triangles at the midpoint of an edge, facing into or out of the
 * shape.
 */
@Experimental("The shapes API is currently experimental and subject to change")
public class TriangleEdgeTreatment extends EdgeTreatment {

  private final float size;
  private final boolean inside;

  /**
   * Instantiates a triangle treatment of the given size, which faces inward or outward relative to
   * the shape.
   *
   * @param size the length in pixels that the triangle extends into or out of the shape. The length
   *     of the side of the triangle coincident with the rest of the edge is 2 * size.
   * @param inside true if the triangle should be "cut out" of the shape (i.e. inward-facing); false
   *     if the triangle should extend out of the shape.
   */
  public TriangleEdgeTreatment(float size, boolean inside) {
    this.size = size;
    this.inside = inside;
  }

  @Override
  public void getEdgePath(float length, float interpolation, ShapePath shapePath) {
    shapePath.lineTo(length / 2 - (size * interpolation), 0);
    shapePath.lineTo(length / 2, inside ? size * interpolation : -size * interpolation);
    shapePath.lineTo(length / 2 + (size * interpolation), 0);
    shapePath.lineTo(length, 0);
  }
}
