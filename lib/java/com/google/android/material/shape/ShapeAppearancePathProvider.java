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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;
import static com.google.android.material.shape.ShapeAppearanceModel.NUM_CORNERS;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.Path.Direction;
import android.graphics.Path.Op;
import android.graphics.PointF;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.UiThread;

/** A class to convert a {@link ShapeAppearanceModel} to a {@link android.graphics.Path}. */
public class ShapeAppearancePathProvider {

  protected static final int TOP_RIGHT_CORNER_INDEX = 0;
  protected static final int BOTTOM_RIGHT_CORNER_INDEX = 1;
  protected static final int BOTTOM_LEFT_CORNER_INDEX = 2;
  protected static final int TOP_LEFT_CORNER_INDEX = 3;

  private static class Lazy {
    static final ShapeAppearancePathProvider INSTANCE = new ShapeAppearancePathProvider();
  }

  /**
   * Listener called every time a {@link ShapePath} is created for a corner or an edge treatment.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public interface PathListener {

    void onCornerPathCreated(ShapePath cornerPath, Matrix transform, int count);

    void onEdgePathCreated(ShapePath edgePath, Matrix transform, int count);
  }

  // Inter-method state. This class works under the assumption that there is only one exposed
  // method, the method is responsible for correctly reset state.
  private final ShapePath[] cornerPaths = new ShapePath[NUM_CORNERS];
  private final Matrix[] cornerTransforms = new Matrix[NUM_CORNERS];
  private final Matrix[] edgeTransforms = new Matrix[NUM_CORNERS];

  // Pre-allocated objects that are re-used several times during path computation and rendering.
  private final PointF pointF = new PointF();
  private final Path overlappedEdgePath = new Path();
  private final Path boundsPath = new Path();
  private final ShapePath shapePath = new ShapePath();
  private final float[] scratch = new float[2];
  private final float[] scratch2 = new float[2];
  private final Path edgePath = new Path();
  private final Path cornerPath = new Path();

  private boolean edgeIntersectionCheckEnabled = true;

  public ShapeAppearancePathProvider() {
    for (int i = 0; i < NUM_CORNERS; i++) {
      cornerPaths[i] = new ShapePath();
      cornerTransforms[i] = new Matrix();
      edgeTransforms[i] = new Matrix();
    }
  }

  /** @hide */
  @UiThread
  @RestrictTo(LIBRARY_GROUP)
  @NonNull
  public static ShapeAppearancePathProvider getInstance() {
    return Lazy.INSTANCE;
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
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void calculatePath(
      ShapeAppearanceModel shapeAppearanceModel,
      float interpolation,
      RectF bounds,
      PathListener pathListener,
      @NonNull Path path) {
    calculatePath(
        shapeAppearanceModel,
        /* cornerSizeOverrides= */ null,
        interpolation,
        bounds,
        pathListener,
        path);
  }

  /**
   * Writes the given {@link ShapeAppearanceModel} to {@code path}
   *
   * @param shapeAppearanceModel The shape to be applied in the path.
   * @param cornerSizeOverrides the corner sizes to overload the ones from shapeAppearanceModel.
   * @param interpolation the desired interpolation.
   * @param bounds the desired bounds for the path.
   * @param pathListener the path
   * @param path the returned path out-var.
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public void calculatePath(
      @NonNull ShapeAppearanceModel shapeAppearanceModel,
      @Nullable float[] cornerSizeOverrides,
      float interpolation,
      RectF bounds,
      PathListener pathListener,
      @NonNull Path path) {
    path.rewind();
    overlappedEdgePath.rewind();
    boundsPath.rewind();
    boundsPath.addRect(bounds, Direction.CW);
    ShapeAppearancePathSpec spec =
        new ShapeAppearancePathSpec(
            shapeAppearanceModel, interpolation, bounds, pathListener, path);

    // Calculate the transformations (rotations and translations) necessary for each edge and
    // corner treatment.
    for (int index = 0; index < NUM_CORNERS; index++) {
      setCornerPathAndTransform(spec, index, cornerSizeOverrides);
      setEdgePathAndTransform(index);
    }

    for (int index = 0; index < NUM_CORNERS; index++) {
      appendCornerPath(spec, index);
      appendEdgePath(spec, index);
    }

    path.close();
    overlappedEdgePath.close();

    // Union with the edge paths that had an intersection to handle overlaps.
    if (!overlappedEdgePath.isEmpty()) {
      path.op(overlappedEdgePath, Op.UNION);
    }
  }

  private void setCornerPathAndTransform(
      @NonNull ShapeAppearancePathSpec spec, int index, @Nullable float[] cornerSizes) {
    CornerSize cornerSize =
        cornerSizes == null
            ? getCornerSizeForIndex(index, spec.shapeAppearanceModel)
            : new ClampedCornerSize(cornerSizes[index]);
    getCornerTreatmentForIndex(index, spec.shapeAppearanceModel)
        .getCornerPath(cornerPaths[index], 90, spec.interpolation, spec.bounds, cornerSize);

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
    EdgeTreatment edgeTreatment = getEdgeTreatmentForIndex(index, spec.shapeAppearanceModel);
    edgeTreatment.getEdgePath(edgeLength, center, spec.interpolation, shapePath);
    edgePath.reset();
    shapePath.applyToPath(edgeTransforms[index], edgePath);

    if (edgeIntersectionCheckEnabled
        && (edgeTreatment.forceIntersection()
            || pathOverlapsCorner(edgePath, index)
            || pathOverlapsCorner(edgePath, nextIndex))) {

      // Calculate the difference between the edge and the bounds to calculate the part of the edge
      // outside of the bounds of the shape.
      edgePath.op(edgePath, boundsPath, Op.DIFFERENCE);

      // Add a line to the path between the previous corner and this edge.
      // TODO(b/144784590): handle the shadow as well.
      scratch[0] = shapePath.getStartX();
      scratch[1] = shapePath.getStartY();
      edgeTransforms[index].mapPoints(scratch);
      overlappedEdgePath.moveTo(scratch[0], scratch[1]);

      // Add this to the overlappedEdgePath which will be unioned later.
      shapePath.applyToPath(edgeTransforms[index], overlappedEdgePath);
    } else {
      shapePath.applyToPath(edgeTransforms[index], spec.path);
    }

    if (spec.pathListener != null) {
      spec.pathListener.onEdgePathCreated(shapePath, edgeTransforms[index], index);
    }
  }

  private boolean pathOverlapsCorner(Path edgePath, int index) {
    cornerPath.reset();
    cornerPaths[index].applyToPath(cornerTransforms[index], cornerPath);

    RectF bounds = new RectF();
    edgePath.computeBounds(bounds, /* exact= */ true);
    cornerPath.computeBounds(bounds, /* exact= */ true);
    edgePath.op(cornerPath, Op.INTERSECT);
    edgePath.computeBounds(bounds, /* exact= */ true);

    return !bounds.isEmpty() || (bounds.width() > 1 && bounds.height() > 1);
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

  @NonNull
  CornerSize getCornerSizeForIndex(int index, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
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
    return 90 * ((index + 1) % 4);
  }

  void setEdgeIntersectionCheckEnable(boolean enable) {
    edgeIntersectionCheckEnabled = enable;
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
