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

import android.graphics.RectF;
import androidx.annotation.NonNull;

/**
 * A basic corner treatment (a single point which does not affect the shape).
 *
 * <p>Note: For corner treatments which result in a concave shape, the parent view must disable
 * clipping of children by calling {@link android.view.ViewGroup#setClipChildren(boolean)}, or by
 * setting `android:clipChildren="false"` in xml. `clipToPadding` may also need to be false if there
 * is any padding on the parent that could intersect the shadow.
 */
public class CornerTreatment {

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
   * @deprecated implement {@link #getCornerPath(ShapePath, float, float, float)} or {@link
   *     #getCornerPath(ShapePath, float, float, RectF, CornerSize)} instead.
   */
  @Deprecated
  public void getCornerPath(float angle, float interpolation, @NonNull ShapePath shapePath) {}

  /**
   * Generates a {@link ShapePath} using a single radius value for this corner treatment.
   *
   * <p>CornerTreatments are assumed to have an origin of (0, 0) (i.e. they represent the top-left
   * corner), and are automatically rotated and scaled as necessary when applied to other corners.
   *
   * @param shapePath the {@link ShapePath} that this treatment should write to.
   * @param angle the angle of the corner, typically 90 degrees.
   * @param interpolation the interpolation of the corner treatment. Ranges between 0 (none) and 1
   *     (fully) interpolated. Custom corner treatments can implement interpolation to support shape
   *     transition between two arbitrary states. Typically, a value of 0 indicates that the custom
   *     corner treatment is not rendered (i.e. that it is a 90 degree angle), and a value of 1
   *     indicates that the treatment is fully rendered. Animation between these two values can
   *     "heal" or "reveal" a corner treatment.
   * @param radius the radius or size of this corner.
   */
  public void getCornerPath(
      @NonNull ShapePath shapePath, float angle, float interpolation, float radius) {
    getCornerPath(angle, interpolation, shapePath);
  }

  /**
   * Generates a {@link ShapePath} for this corner treatment.
   *
   * <p>CornerTreatments are assumed to have an origin of (0, 0) (i.e. they represent the top-left
   * corner), and are automatically rotated and scaled as necessary when applied to other corners.
   *
   * @param shapePath the {@link ShapePath} that this treatment should write to.
   * @param angle the angle of the corner, typically 90 degrees.
   * @param interpolation the interpolation of the corner treatment. Ranges between 0 (none) and 1
   *     (fully) interpolated. Custom corner treatments can implement interpolation to support shape
   *     transition between two arbitrary states. Typically, a value of 0 indicates that the custom
   *     corner treatment is not rendered (i.e. that it is a 90 degree angle), and a value of 1
   *     indicates that the treatment is fully rendered. Animation between these two values can
   *     "heal" or "reveal" a corner treatment.
   * @param bounds the bounds of the full shape that will be drawn. This could be used change the
   *     behavior of the CornerTreatment depending on how much space is available for the full
   *     shape.
   * @param size the {@link CornerSize} used for this corner
   */
  public void getCornerPath(
      @NonNull ShapePath shapePath,
      float angle,
      float interpolation,
      @NonNull RectF bounds,
      @NonNull CornerSize size) {
    getCornerPath(shapePath, angle, interpolation, size.getCornerSize(bounds));
  }
}
