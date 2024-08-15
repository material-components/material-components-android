/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.shape;

import static java.lang.Math.min;

import android.graphics.Matrix;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.PathShape;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.graphics.shapes.RoundedPolygon;
import androidx.graphics.shapes.Shapes_androidKt;
import com.google.android.material.internal.ViewUtils;

/** Utility methods for {@link MaterialShapeDrawable} and related classes. */
public class MaterialShapeUtils {

  private MaterialShapeUtils() {}

  @NonNull
  static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily) {
    switch (cornerFamily) {
      case CornerFamily.ROUNDED:
        return new RoundedCornerTreatment();
      case CornerFamily.CUT:
        return new CutCornerTreatment();
      default:
        return createDefaultCornerTreatment();
    }
  }

  @NonNull
  static CornerTreatment createDefaultCornerTreatment() {
    return new RoundedCornerTreatment();
  }

  @NonNull
  static EdgeTreatment createDefaultEdgeTreatment() {
    return new EdgeTreatment();
  }

  /**
   * If the background of the provided {@code view} is a {@link MaterialShapeDrawable}, sets the
   * drawable's elevation via {@link MaterialShapeDrawable#setElevation(float)}; otherwise does
   * nothing.
   */
  public static void setElevation(@NonNull View view, float elevation) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) background).setElevation(elevation);
    }
  }

  /**
   * If the background of the provided {@code view} is a {@link MaterialShapeDrawable}, sets the
   * drawable's parent absolute elevation (see {@link
   * MaterialShapeUtils#setParentAbsoluteElevation(View, MaterialShapeDrawable)}); otherwise does
   * nothing.
   */
  public static void setParentAbsoluteElevation(@NonNull View view) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      setParentAbsoluteElevation(view, (MaterialShapeDrawable) background);
    }
  }

  /**
   * Updates the {@code materialShapeDrawable} parent absolute elevation via {@link
   * MaterialShapeDrawable#setParentAbsoluteElevation(float)} to be equal to the absolute elevation
   * of the parent of the provided {@code view}.
   */
  public static void setParentAbsoluteElevation(
      @NonNull View view, @NonNull MaterialShapeDrawable materialShapeDrawable) {
    if (materialShapeDrawable.isElevationOverlayEnabled()) {
      materialShapeDrawable.setParentAbsoluteElevation(ViewUtils.getParentAbsoluteElevation(view));
    }
  }

  /**
   * Returns a {@link ShapeDrawable} with the shape's path.
   *
   * <p>The shape is always assumed to fit in (0, 0) to (1, 1) square.
   *
   * @param shape A {@link RoundedPolygon} object to be used in the drawable.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static ShapeDrawable createShapeDrawable(@NonNull RoundedPolygon shape) {
    PathShape pathShape = new PathShape(Shapes_androidKt.toPath(shape), 1, 1);
    return new ShapeDrawable(pathShape);
  }

  /**
   * Creates a new {@link RoundedPolygon}, moving and resizing this one, so it's completely inside
   * the destination bounds.
   *
   * <p>If {@code radial} is true, the shape will be scaled to fit in the biggest circle centered in
   * the destination bounds. This is useful when the shape is animated to rotate around its center.
   * Otherwise, the shape will be scaled to fit in the destination bounds. With either option, the
   * shape's original center will be aligned with the destination bounds center.
   *
   * @param shape The original {@link RoundedPolygon}.
   * @param radial Whether to transform the shape to fit in the biggest circle centered in the
   *     destination bounds.
   * @param dstBounds The destination bounds to fit.
   * @return A new {@link RoundedPolygon} that fits in the destination bounds.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static RoundedPolygon normalize(
      @NonNull RoundedPolygon shape, boolean radial, @NonNull RectF dstBounds) {
    float[] srcBoundsArray = new float[4];
    if (radial) {
      // This calculates the axis-aligned bounds of the shape and returns that rectangle. It
      // determines the max dimension of the shape (by calculating the distance from its center to
      // the start and midpoint of each curve) and returns a square which can be used to hold the
      // object in any rotation.
      shape.calculateMaxBounds(srcBoundsArray);
    } else {
      // This calculates the bounds of the shape without rotating the shape.
      shape.calculateBounds(srcBoundsArray);
    }
    RectF srcBounds =
        new RectF(srcBoundsArray[0], srcBoundsArray[1], srcBoundsArray[2], srcBoundsArray[3]);
    float scale =
        min(dstBounds.width() / srcBounds.width(), dstBounds.height() / srcBounds.height());
    // Scales the shape with pivot point at its original center then moves it to align its original
    // center with the destination bounds center.
    Matrix transform = createScaleMatrix(scale, scale);
    transform.preTranslate(-srcBounds.centerX(), -srcBounds.centerY());
    transform.postTranslate(dstBounds.centerX(), dstBounds.centerY());
    return Shapes_androidKt.transformed(shape, transform);
  }

  /**
   * Creates a new {@link RoundedPolygon}, moving and resizing this one, so it's completely inside
   * (0, 0) - (1, 1) square.
   *
   * <p>If {@code radial} is true, the shape will be scaled to fit in the circle centered at (0.5,
   * 0.5) with a radius of 0.5. This is useful when the shape is animated to rotate around its
   * center. Otherwise, the shape will be scaled to fit in the (0, 0) - (1, 1) square. With either
   * option, the shape center will be (0.5, 0.5).
   *
   * @param shape The original {@link RoundedPolygon}.
   * @param radial Whether to transform the shape to fit in the circle centered at (0.5, 0.5) with a
   *     radius of 0.5.
   * @return A new {@link RoundedPolygon} that fits in (0, 0) - (1, 1) square.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  public static RoundedPolygon normalize(@NonNull RoundedPolygon shape, boolean radial) {
    return normalize(shape, radial, new RectF(0, 0, 1, 1));
  }

  /**
   * Returns a {@link Matrix} with the input scales.
   *
   * @param scaleX Scale in X axis.
   * @param scaleY Scale in Y axis
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createScaleMatrix(float scaleX, float scaleY) {
    Matrix matrix = new Matrix();
    matrix.setScale(scaleX, scaleY);
    return matrix;
  }

  /**
   * Returns a {@link Matrix} with the input rotation in degrees.
   *
   * @param degrees The rotation in degrees.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createRotationMatrix(float degrees) {
    Matrix matrix = new Matrix();
    matrix.setRotate(degrees);
    return matrix;
  }

  /**
   * Returns a {@link Matrix} with the input skews.
   *
   * @param kx The skew in X axis.
   * @param ky The skew in Y axis.
   * @hide
   */
  @NonNull
  @RestrictTo(Scope.LIBRARY_GROUP)
  static Matrix createSkewMatrix(float kx, float ky) {
    Matrix matrix = new Matrix();
    matrix.setSkew(kx, ky);
    return matrix;
  }
}
