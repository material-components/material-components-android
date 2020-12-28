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

/**
 * A basic edge treatment (a single straight line). Sub-classed for custom edge treatments.
 *
 * <p>Note: For edge treatments which result in a concave shape, the parent view must disable
 * clipping of children by calling {@link android.view.ViewGroup#setClipChildren(boolean)}, or by
 * setting `android:clipChildren="false"` in xml. `clipToPadding` may also need to be false if there
 * is any padding on the parent that could intersect the shadow.
 */
public class EdgeTreatment {

  /**
   * @deprecated Does not support interpolation. Use {@link #getEdgePath(float, float, float,
   *     ShapePath)} instead.
   */
  @Deprecated
  public void getEdgePath(float length, float interpolation, @NonNull ShapePath shapePath) {
    // Best guess at center since it could be offset by corners of different size.
    float center = length / 2f;
    getEdgePath(length, center,  interpolation, shapePath);
  }

  /**
   * Generates a {@link ShapePath} for this edge treatment.
   *
   * <p>EdgeTreatments have an origin of (0, 0) and a destination of (0, length) (i.e. they
   * represent the top edge), and are automatically rotated and scaled as necessary when applied to
   * other edges. Only the horizontal, top EdgeTreatment needs to be defined in order to apply it to
   * all four edges.
   *
   * @param length the length of the edge.
   * @param center the distance to the center of the edge. This takes into account any offset added
   *     by the proceeding corner. Drawing anything at (center, 0) will be center aligned with the
   *     shape. Normally you'll want to use this instead of length / 2.
   * @param interpolation the interpolation of the edge treatment. Ranges between 0 (none) and 1
   *     (fully) interpolated. Custom edge treatments can implement interpolation to support shape
   *     transition between two arbitrary states. Typically, a value of 0 indicates that the custom
   *     edge treatment is not rendered (i.e. that it is a straight line), and a value of 1
   *     indicates that the treatment is fully rendered. Animation between these two values can
   *     "heal" or "reveal" an edge treatment.
   * @param shapePath the {@link ShapePath} that this treatment should write to.
   */
  public void getEdgePath(
      float length, float center, float interpolation, @NonNull ShapePath shapePath) {
    shapePath.lineTo(length, 0);
  }

  /**
   * Causes this edge to be intersected with the line between the start of the previous corner, and
   * the end of the next corner. This allows for simpler definition of edge treatments since they
   * can be less exact about how they calculate the start and ending point of the edge.
   */
  boolean forceIntersection() {
    return false;
  }
}
