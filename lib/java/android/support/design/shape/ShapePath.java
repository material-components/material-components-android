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

package android.support.design.shape;

import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.design.internal.Experimental;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the descriptive path of a shape. Path segments are stored in sequence so that
 * transformations can be applied to them when the {@link android.graphics.Path} is produced by the
 * {@link MaterialShapeDrawable}.
 */
@Experimental("The shapes API is currently experimental and subject to change")
public class ShapePath {
  public float startX;
  public float startY;
  public float endX;
  public float endY;

  private final List<PathOperation> operations = new ArrayList<>();

  public ShapePath() {
    reset(0, 0);
  }

  public ShapePath(float startX, float startY) {
    reset(startX, startY);
  }

  public void reset(float startX, float startY) {
    this.startX = startX;
    this.startY = startY;
    this.endX = startX;
    this.endY = startY;
    this.operations.clear();
  }

  /**
   * Add a line to the ShapePath.
   *
   * @param x the x to which the line should be drawn.
   * @param y the y to which the line should be drawn.
   */
  public void lineTo(float x, float y) {
    PathLineOperation operation = new PathLineOperation();
    operation.x = x;
    operation.y = y;
    operations.add(operation);

    endX = x;
    endY = y;
  }

  /**
   * Add a quad to the ShapePath.
   *
   * @param controlX the control point x of the arc.
   * @param controlY the control point y of the arc.
   * @param toX the end x of the arc.
   * @param toY the end y of the arc.
   */
  public void quadToPoint(float controlX, float controlY, float toX, float toY) {
    PathQuadOperation operation = new PathQuadOperation();
    operation.controlX = controlX;
    operation.controlY = controlY;
    operation.endX = toX;
    operation.endY = toY;
    operations.add(operation);

    endX = toX;
    endY = toY;
  }

  /**
   * Add an arc to the ShapePath.
   *
   * @param left the X coordinate of the left side of the rectangle containing the arc oval.
   * @param top the Y coordinate of the top of the rectangle containing the arc oval.
   * @param right the X coordinate of the right side of the rectangle containing the arc oval.
   * @param bottom the Y coordinate of the bottom of the rectangle containing the arc oval.
   * @param startAngle start angle of the arc.
   * @param sweepAngle sweep angle of the arc.
   */
  public void addArc(float left, float top, float right, float bottom, float startAngle,
      float sweepAngle) {
    PathArcOperation operation = new PathArcOperation(left, top, right, bottom);
    operation.startAngle = startAngle;
    operation.sweepAngle = sweepAngle;
    operations.add(operation);

    endX = (left + right) * 0.5f
        + (right - left) / 2 * (float) Math.cos(Math.toRadians(startAngle + sweepAngle));
    endY = (top + bottom) * 0.5f
        + (bottom - top) / 2 * (float) Math.sin(Math.toRadians(startAngle + sweepAngle));
  }

  /**
   * Apply the ShapePath sequence to a {@link android.graphics.Path} under a matrix transform.
   *
   * @param transform the matrix transform under which this ShapePath is applied
   * @param path the path to which this ShapePath is applied
   */
  public void applyToPath(Matrix transform, Path path) {
    for (int i = 0, size = operations.size(); i < size; i++) {
      PathOperation operation = operations.get(i);
      operation.applyToPath(transform, path);
    }
  }

  /** Interface for a path operation to be appended to the operations list. */
  public abstract static class PathOperation {
    protected final Matrix matrix = new Matrix();

    public abstract void applyToPath(Matrix transform, Path path);
  }

  /** Straight line operation. */
  public static class PathLineOperation extends PathOperation {
    private float x;
    private float y;

    @Override
    public void applyToPath(Matrix transform, Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.lineTo(x, y);
      path.transform(transform);
    }
  }

  /** Path quad operation. */
  public static class PathQuadOperation extends PathOperation {
    public float controlX;
    public float controlY;
    public float endX;
    public float endY;

    @Override
    public void applyToPath(Matrix transform, Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.quadTo(controlX, controlY, endX, endY);
      path.transform(transform);
    }
  }

  /** Path arc operation. */
  public static class PathArcOperation extends PathOperation {
    private static final RectF rectF = new RectF();

    public float left;
    public float top;
    public float right;
    public float bottom;
    public float startAngle;
    public float sweepAngle;

    public PathArcOperation(float left, float top, float right, float bottom) {
      this.left = left;
      this.top = top;
      this.right = right;
      this.bottom = bottom;
    }

    @Override
    public void applyToPath(Matrix transform, Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      rectF.set(left, top, right, bottom);
      path.arcTo(rectF, startAngle, sweepAngle, false);
      path.transform(transform);
    }
  }
}
