/*
 * Copyright 2018 The Android Open Source Project
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

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;

/** A class to convert a {@link ShapeAppearanceModel} to a {@link android.graphics.Path}. */
public class ShapeAppearancePathProvider {

  /**
   * Listener called every time a {@link ShapePath} is created for a corner or an edge treatment.
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public interface PathListener {
    void onCornerPathCreated(ShapePath cornerPath, Matrix transform, int count);

    void onEdgePathCreated(ShapePath edgePath, Matrix transform, int count);
  }

  // Inter-method state.
  private final ShapePath[] cornerPaths = new ShapePath[4];
  private final Matrix[] cornerTransforms = new Matrix[4];
  private final Matrix[] edgeTransforms = new Matrix[4];

  // Pre-allocated objects that are re-used several times during path computation and rendering.
  private final PointF pointF = new PointF();
  private final ShapePath shapePath = new ShapePath();
  private final float[] scratch = new float[2];
  private final float[] scratch2 = new float[2];

  public ShapeAppearancePathProvider() {
    for (int i = 0; i < 4; i++) {
      cornerPaths[i] = new ShapePath();
      cornerTransforms[i] = new Matrix();
      edgeTransforms[i] = new Matrix();
    }
  }

  /**
   * Writes the given {@link ShapeAppearanceModel} to {@code path}
   *
   * @param shapeAppearanceModel The shape to be applied in the path.
   * @param interpolation the desired interpolation.
   * @param bounds the desired bounds for the path.
   * @param path the returned path out-var.
   */
  public void calculatePath(
      ShapeAppearanceModel shapeAppearanceModel,
      float interpolation,
      RectF bounds,
      @NonNull Path path) {
    calculatePath(shapeAppearanceModel, interpolation, bounds, null, path);
  }

  /**
   * Writes the given {@link ShapeAppearanceModel} to {@code path}
   *
   * @param shapeAppearanceModel The shape to be applied in the path.
   * @param interpolation the desired interpolation.
   * @param bounds the desired bounds for the path.
   * @param pathListener the path
   * @param path the returned path out-var.
   */
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void calculatePath(
      ShapeAppearanceModel shapeAppearanceModel,
      float interpolation,
      RectF bounds,
      PathListener pathListener,
      @NonNull Path path) {
    path.rewind();
    ShapeAppearancePathSpec spec =
        new ShapeAppearancePathSpec(
            shapeAppearanceModel, interpolation, bounds, pathListener, path);

    // Calculate the transformations (rotations and translations) necessary for each edge and
    // corner treatment.
    for (int index = 0; index < 4; index++) {
      setCornerPathAndTransform(spec, index);
      setEdgePathAndTransform(index);
    }

    // Apply corners and edges to the path in clockwise interleaving sequence: top-right corner,
    // right edge, bottom-right corner, bottom edge, bottom-left corner etc. We start from the top
    // right corner rather than the top left to work around a bug in API level 21 and 22 in which
    // rounding error causes the path to incorrectly be marked as concave.
    for (int index = 0; index < 4; index++) {
      appendCornerPath(spec, index);
      appendEdgePath(spec, index);
    }

    path.close();
  }

  private void setCornerPathAndTransform(@NonNull ShapeAppearancePathSpec spec, int index) {
    CornerSize size = getCornerSizeForIndex(index, spec.shapeAppearanceModel);
    getCornerTreatmentForIndex(index, spec.shapeAppearanceModel)
        .getCornerPath(cornerPaths[index], 90, spec.interpolation, spec.bounds, size);

    float edgeAngle = angleOfEdge(index);
    cornerTransforms[index].reset();
    getCoordinatesOfCorner(index, spec.bounds, pointF);
    cornerTransforms[index].setTranslate(pointF.x, pointF.y);
    cornerTransforms[index].preRotate(edgeAngle);
  }

  private void setEdgePathAndTransform(int index) {
    scratch[0] = cornerPaths[index].getEndX();
    scratch[1] = cornerPaths[index].getEndY();
    cornerTransforms[index].mapPoints(scratch);
    float edgeAngle = angleOfEdge(index);
    edgeTransforms[index].reset();
    edgeTransforms[index].setTranslate(scratch[0], scratch[1]);
    edgeTransforms[index].preRotate(edgeAngle);
  }

  private void appendCornerPath(@NonNull ShapeAppearancePathSpec spec, int index) {
    scratch[0] = cornerPaths[index].getStartX();
    scratch[1] = cornerPaths[index].getStartY();
    cornerTransforms[index].mapPoints(scratch);
    if (index == 0) {
      spec.path.moveTo(scratch[0], scratch[1]);
    } else {
      spec.path.lineTo(scratch[0], scratch[1]);
    }
    cornerPaths[index].applyToPath(cornerTransforms[index], spec.path);
    if (spec.pathListener != null) {
      spec.pathListener.onCornerPathCreated(cornerPaths[index], cornerTransforms[index], index);
    }
  }

  private void appendEdgePath(@NonNull ShapeAppearancePathSpec spec, int index) {
    int nextIndex = (index + 1) % 4;
    scratch[0] = cornerPaths[index].getEndX();
    scratch[1] = cornerPaths[index].getEndY();
    cornerTransforms[index].mapPoints(scratch);

    scratch2[0] = cornerPaths[nextIndex].getStartX();
    scratch2[1] = cornerPaths[nextIndex].getStartY();
    cornerTransforms[nextIndex].mapPoints(scratch2);

    float edgeLength = (float) Math.hypot(scratch[0] - scratch2[0], scratch[1] - scratch2[1]);
    // TODO(b/121352029): Remove this -.001f that is currently needed to handle rounding errors
    edgeLength = Math.max(edgeLength - .001f, 0);
    float center = getEdgeCenterForIndex(spec.bounds, index);
    shapePath.reset(0, 0);
    getEdgeTreatmentForIndex(index, spec.shapeAppearanceModel)
        .getEdgePath(edgeLength, center, spec.interpolation, shapePath);
    shapePath.applyToPath(edgeTransforms[index], spec.path);
    if (spec.pathListener != null) {
      spec.pathListener.onEdgePathCreated(shapePath, edgeTransforms[index], index);
    }
  }

  private float getEdgeCenterForIndex(@NonNull RectF bounds, int index) {
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);
    switch (index) {
      case 1:
      case 3:
        return Math.abs(bounds.centerX() - scratch[0]);
      case 2:
      case 0:
      default:
        return Math.abs(bounds.centerY() - scratch[1]);
    }
  }

  private CornerTreatment getCornerTreatmentForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomRightCorner();
      case 2:
        return shapeAppearanceModel.getBottomLeftCorner();
      case 3:
        return shapeAppearanceModel.getTopLeftCorner();
      case 0:
      default:
        return shapeAppearanceModel.getTopRightCorner();
    }
  }

  private CornerSize getCornerSizeForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomRightCornerSize();
      case 2:
        return shapeAppearanceModel.getBottomLeftCornerSize();
      case 3:
        return shapeAppearanceModel.getTopLeftCornerSize();
      case 0:
      default:
        return shapeAppearanceModel.getTopRightCornerSize();
    }
  }

  private EdgeTreatment getEdgeTreatmentForIndex(
      int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    switch (index) {
      case 1:
        return shapeAppearanceModel.getBottomEdge();
      case 2:
        return shapeAppearanceModel.getLeftEdge();
      case 3:
        return shapeAppearanceModel.getTopEdge();
      case 0:
      default:
        return shapeAppearanceModel.getRightEdge();
    }
  }

  private void getCoordinatesOfCorner(int index, @NonNull RectF bounds, @NonNull PointF pointF) {
    switch (index) {
      case 1: // bottom-right
        pointF.set(bounds.right, bounds.bottom);
        break;
      case 2: // bottom-left
        pointF.set(bounds.left, bounds.bottom);
        break;
      case 3: // top-left
        pointF.set(bounds.left, bounds.top);
        break;
      case 0: // top-right
      default:
        pointF.set(bounds.right, bounds.top);
        break;
    }
  }

  private float angleOfEdge(int index) {
    return 90 * (index + 1 % 4);
  }

  /** Necessary information to map a {@link ShapeAppearanceModel} into a Path. */
  static final class ShapeAppearancePathSpec {

    @NonNull public final ShapeAppearanceModel shapeAppearanceModel;
    @NonNull public final Path path;
    @NonNull public final RectF bounds;

    @Nullable public final PathListener pathListener;

    public final float interpolation;

    ShapeAppearancePathSpec(
        @NonNull ShapeAppearanceModel shapeAppearanceModel,
        float interpolation,
        RectF bounds,
        @Nullable PathListener pathListener,
        Path path) {
      this.pathListener = pathListener;
      this.shapeAppearanceModel = shapeAppearanceModel;
      this.interpolation = interpolation;
      this.bounds = bounds;
      this.path = path;
    }
  }
}
