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

import static java.lang.Math.min;

import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import androidx.annotation.NonNull;
import com.google.android.material.shadow.ShadowRenderer;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the descriptive path of a shape. Path segments are stored in sequence so that
 * transformations can be applied to them when the {@link android.graphics.Path} is produced by the
 * {@link MaterialShapeDrawable}.
 */
public class ShapePath {

  private static final float ANGLE_UP = 270;
  /**
   * Degrees measured from the vector [0,1].
   *
   * @hide
   */
  protected static final float ANGLE_LEFT = 180;

  /**
   * The x coordinate for the start of the path. Does not change. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be
   *     maintained.
   */
  @Deprecated public float startX;
  /**
   * The y coordinate for the start of the path. Does not change. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be
   *     maintained.
   */
  @Deprecated public float startY;
  /**
   * The x coordinate for the current end of the path given the previously applied transformation.
   * Changes internally. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be
   *     maintained.
   */
  @Deprecated public float endX;
  /**
   * The y coordinate for the current end of the path given the previously applied transformation.
   * Changes internally. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be
   *     maintained.
   */
  @Deprecated public float endY;
  /**
   * The angle of the start of the last drawn shadow. Changes internally. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be *
   *     maintained.
   */
  @Deprecated public float currentShadowAngle;
  /**
   * The angle at the end of the final shadow. Changes internally. Do not change.
   *
   * @deprecated Use the class methods to interact with this field so internal state can be *
   *     maintained.
   */
  @Deprecated public float endShadowAngle;

  private final List<PathOperation> operations = new ArrayList<>();
  private final List<ShadowCompatOperation> shadowCompatOperations = new ArrayList<>();
  private boolean containsIncompatibleShadowOp;

  public ShapePath() {
    reset(0, 0);
  }

  public ShapePath(float startX, float startY) {
    reset(startX, startY);
  }

  /**
   * Resets the ShapePath using a default shadow. {@link ShapePath#reset(float, float, float,
   * float)}.
   */
  public void reset(float startX, float startY) {
    reset(startX, startY, ANGLE_UP, 0);
  }

  /** Resets fields given the provided assignment parameters. */
  public void reset(float startX, float startY, float shadowStartAngle, float shadowSweepAngle) {
    setStartX(startX);
    setStartY(startY);
    setEndX(startX);
    setEndY(startY);
    setCurrentShadowAngle(shadowStartAngle);
    setEndShadowAngle((shadowStartAngle + shadowSweepAngle) % 360);
    this.operations.clear();
    this.shadowCompatOperations.clear();
    this.containsIncompatibleShadowOp = false;
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

    // The previous endX and endY is the starting point for this shadow operation.
    LineShadowOperation shadowOperation = new LineShadowOperation(operation, getEndX(), getEndY());

    addShadowCompatOperation(
        shadowOperation,
        ANGLE_UP + shadowOperation.getAngle(),
        ANGLE_UP + shadowOperation.getAngle());

    setEndX(x);
    setEndY(y);
  }

  /**
   * Add two connected segments to the ShapePath. This is equivalent to call {@link #lineTo(float,
   * float)} twice. If an inner corner is formed, this can also draw the compat shadow without
   * overlapping.
   */
  public void lineTo(float x1, float y1, float x2, float y2) {
    if ((Math.abs(x1 - getEndX()) < 0.001f && Math.abs(y1 - getEndY()) < 0.001f)
        || (Math.abs(x1 - x2) < 0.001f && Math.abs(y1 - y2) < 0.001f)) {
      lineTo(x2, y2);
      return;
    }
    PathLineOperation operation1 = new PathLineOperation();
    operation1.x = x1;
    operation1.y = y1;
    operations.add(operation1);
    PathLineOperation operation2 = new PathLineOperation();
    operation2.x = x2;
    operation2.y = y2;
    operations.add(operation2);

    // The previous endX and endY is the starting point for this shadow operation.
    InnerCornerShadowOperation shadowOperation =
        new InnerCornerShadowOperation(operation1, operation2, getEndX(), getEndY());

    if (shadowOperation.getSweepAngle() > 0) {
      // If an outer corner is formed, add each segment separately.
      lineTo(x1, y1);
      lineTo(x2, y2);
      return;
    }

    addShadowCompatOperation(
        shadowOperation,
        ANGLE_UP + shadowOperation.getStartAngle(),
        ANGLE_UP + shadowOperation.getEndAngle());

    setEndX(x2);
    setEndY(y2);
  }

  /**
   * Add a quad to the ShapePath.
   *
   * <p>Note: This operation will not draw compatibility shadows. This means no shadow will be drawn
   * on API < 21 and a shadow will only be drawn on API < 29 if the final path is convex.
   *
   * @param controlX the control point x of the arc.
   * @param controlY the control point y of the arc.
   * @param toX the end x of the arc.
   * @param toY the end y of the arc.
   */
  public void quadToPoint(float controlX, float controlY, float toX, float toY) {
    PathQuadOperation operation = new PathQuadOperation();
    operation.setControlX(controlX);
    operation.setControlY(controlY);
    operation.setEndX(toX);
    operation.setEndY(toY);
    operations.add(operation);

    containsIncompatibleShadowOp = true;

    setEndX(toX);
    setEndY(toY);
  }

  /**
   * Add a cubic to the ShapePath.
   *
   * <p>Note: This operation will not draw compatibility shadows. This means no shadow will be drawn
   * on API < 21 and a shadow will only be drawn on API < 29 if the final path is convex.
   *
   * @param controlX1 the 1st control point x of the arc.
   * @param controlY1 the 1st control point y of the arc.
   * @param controlX2 the 2nd control point x of the arc.
   * @param controlY2 the 2nd control point y of the arc.
   * @param toX the end x of the arc.
   * @param toY the end y of the arc.
   */
  public void cubicToPoint(
      float controlX1, float controlY1, float controlX2, float controlY2, float toX, float toY) {
    PathCubicOperation operation =
        new PathCubicOperation(controlX1, controlY1, controlX2, controlY2, toX, toY);
    operations.add(operation);

    containsIncompatibleShadowOp = true;

    setEndX(toX);
    setEndY(toY);
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
  public void addArc(
      float left, float top, float right, float bottom, float startAngle, float sweepAngle) {
    PathArcOperation operation = new PathArcOperation(left, top, right, bottom);
    operation.setStartAngle(startAngle);
    operation.setSweepAngle(sweepAngle);
    operations.add(operation);

    ArcShadowOperation arcShadowOperation = new ArcShadowOperation(operation);
    float endAngle = startAngle + sweepAngle;
    // Flip the startAngle and endAngle when drawing the shadow inside the bounds. They represent
    // the angles from the center of the circle to the start or end of the arc, respectively. When
    // the shadow is drawn inside the arc, it is going the opposite direction.
    boolean drawShadowInsideBounds = sweepAngle < 0;
    addShadowCompatOperation(
        arcShadowOperation,
        drawShadowInsideBounds ? (180 + startAngle) % 360 : startAngle,
        drawShadowInsideBounds ? (180 + endAngle) % 360 : endAngle);

    setEndX(
        (left + right) * 0.5f
            + (right - left) / 2 * (float) Math.cos(Math.toRadians(startAngle + sweepAngle)));
    setEndY(
        (top + bottom) * 0.5f
            + (bottom - top) / 2 * (float) Math.sin(Math.toRadians(startAngle + sweepAngle)));
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

  /**
   * Creates a ShadowCompatOperation to draw compatibility shadow under the matrix transform for the
   * whole path defined by this ShapePath.
   */
  @NonNull
  ShadowCompatOperation createShadowCompatOperation(final Matrix transform) {
    // If the shadowCompatOperations don't end on the desired endShadowAngle, add an arc to do so.
    addConnectingShadowIfNecessary(getEndShadowAngle());
    final Matrix transformCopy = new Matrix(transform);
    final List<ShadowCompatOperation> operations = new ArrayList<>(shadowCompatOperations);
    return new ShadowCompatOperation() {
      @Override
      public void draw(
          Matrix matrix, ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas) {
        for (ShadowCompatOperation op : operations) {
          op.draw(transformCopy, shadowRenderer, shadowElevation, canvas);
        }
      }
    };
  }

  /**
   * Adds a {@link ShadowCompatOperation}, adding an {@link ArcShadowOperation} if needed in order
   * to connect the previous shadow end to the new shadow operation's beginning.
   */
  private void addShadowCompatOperation(
      ShadowCompatOperation shadowOperation, float startShadowAngle, float endShadowAngle) {
    addConnectingShadowIfNecessary(startShadowAngle);
    shadowCompatOperations.add(shadowOperation);
    setCurrentShadowAngle(endShadowAngle);
  }

  /**
   * Hint to let {@link MaterialShapeDrawable} know that it won't be rendering the shadow correctly
   * if it's drawing the compat shadow.
   */
  boolean containsIncompatibleShadowOp() {
    return containsIncompatibleShadowOp;
  }

  /**
   * Create an {@link ArcShadowOperation} to fill in a shadow between the currently drawn shadow and
   * the next shadow angle, if there would be a gap.
   */
  private void addConnectingShadowIfNecessary(float nextShadowAngle) {
    if (getCurrentShadowAngle() == nextShadowAngle) {
      // Previously drawn shadow lines up with the next shadow, so don't draw anything.
      return;
    }
    float shadowSweep = (nextShadowAngle - getCurrentShadowAngle() + 360) % 360;
    if (shadowSweep > 180) {
      // Shadows are actually overlapping, so don't draw anything.
      return;
    }
    PathArcOperation pathArcOperation =
        new PathArcOperation(getEndX(), getEndY(), getEndX(), getEndY());
    pathArcOperation.setStartAngle(getCurrentShadowAngle());
    pathArcOperation.setSweepAngle(shadowSweep);
    shadowCompatOperations.add(new ArcShadowOperation(pathArcOperation));
    setCurrentShadowAngle(nextShadowAngle);
  }

  float getStartX() {
    return startX;
  }

  float getStartY() {
    return startY;
  }

  float getEndX() {
    return endX;
  }

  float getEndY() {
    return endY;
  }

  private float getCurrentShadowAngle() {
    return currentShadowAngle;
  }

  private float getEndShadowAngle() {
    return endShadowAngle;
  }

  private void setStartX(float startX) {
    this.startX = startX;
  }

  private void setStartY(float startY) {
    this.startY = startY;
  }

  private void setEndX(float endX) {
    this.endX = endX;
  }

  private void setEndY(float endY) {
    this.endY = endY;
  }

  private void setCurrentShadowAngle(float currentShadowAngle) {
    this.currentShadowAngle = currentShadowAngle;
  }

  private void setEndShadowAngle(float endShadowAngle) {
    this.endShadowAngle = endShadowAngle;
  }

  /**
   * Interface to hold operations that will draw a compatible shadow in the case that native shadows
   * can't be rendered.
   */
  abstract static class ShadowCompatOperation {

    static final Matrix IDENTITY_MATRIX = new Matrix();
    final Matrix renderMatrix = new Matrix();

    /** Draws the operation on the canvas */
    public final void draw(ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas) {
      draw(IDENTITY_MATRIX, shadowRenderer, shadowElevation, canvas);
    }

    /** Draws the operation with the matrix transform on the canvas */
    public abstract void draw(
        Matrix transform, ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas);
  }

  /** Sets up the correct shadow to be drawn for a line. */
  static class LineShadowOperation extends ShadowCompatOperation {

    private final PathLineOperation operation;
    private final float startX;
    private final float startY;

    public LineShadowOperation(PathLineOperation operation, float startX, float startY) {
      this.operation = operation;
      this.startX = startX;
      this.startY = startY;
    }

    @Override
    public void draw(
        Matrix transform,
        @NonNull ShadowRenderer shadowRenderer,
        int shadowElevation,
        @NonNull Canvas canvas) {
      final float height = operation.y - startY;
      final float width = operation.x - startX;
      final RectF rect = new RectF(0, 0, (float) Math.hypot(height, width), 0);
      // transform & rotate the canvas so that the rect passed to drawEdgeShadow is horizontal.
      renderMatrix.set(transform);
      renderMatrix.preTranslate(startX, startY);
      renderMatrix.preRotate(getAngle());
      shadowRenderer.drawEdgeShadow(canvas, renderMatrix, rect, shadowElevation);
    }

    float getAngle() {
      return (float) Math.toDegrees(Math.atan((operation.y - startY) / (operation.x - startX)));
    }
  }

  /**
   * Sets up the correct shadow to be draw for two connected segments, which potentially contain an
   * inner corner.
   */
  static class InnerCornerShadowOperation extends ShadowCompatOperation {

    private final PathLineOperation operation1;
    private final PathLineOperation operation2;
    private final float startX;
    private final float startY;

    public InnerCornerShadowOperation(
        PathLineOperation operation1, PathLineOperation operation2, float startX, float startY) {
      this.operation1 = operation1;
      this.operation2 = operation2;
      this.startX = startX;
      this.startY = startY;
    }

    @Override
    public void draw(
        Matrix transform, ShadowRenderer shadowRenderer, int shadowElevation, Canvas canvas) {
      final float sweepAngle = getSweepAngle();
      if (sweepAngle > 0) {
        // An outer corner is formed, ignore.
        return;
      }

      final double length1 = Math.hypot(operation1.x - startX, operation1.y - startY);
      final double length2 = Math.hypot(operation2.x - operation1.x, operation2.y - operation1.y);
      final float arcRadius = (float) min(shadowElevation, min(length1, length2));
      final double retractLength = arcRadius * Math.tan(Math.toRadians(-sweepAngle / 2));
      // Draws the retracted first line.
      if (length1 > retractLength) {
        final RectF rect1 = new RectF(0, 0, (float) (length1 - retractLength), 0);
        renderMatrix.set(transform);
        renderMatrix.preTranslate(startX, startY);
        renderMatrix.preRotate(getStartAngle());
        shadowRenderer.drawEdgeShadow(canvas, renderMatrix, rect1, shadowElevation);
      }
      // Draws the shadow connecting the lines.
      final RectF rect = new RectF(0, 0, 2 * arcRadius, 2 * arcRadius);
      renderMatrix.set(transform);
      renderMatrix.preTranslate(operation1.x, operation1.y);
      renderMatrix.preRotate(getStartAngle());
      renderMatrix.preTranslate((float) (-retractLength - arcRadius), -2 * arcRadius);
      shadowRenderer.drawInnerCornerShadow(
          canvas,
          renderMatrix,
          rect,
          (int) arcRadius,
          ANGLE_UP + 180,
          sweepAngle,
          new float[] {(float) (arcRadius + retractLength), 2 * arcRadius});
      // Draws the retracted second line.
      if (length2 > retractLength) {
        final RectF rect2 = new RectF(0, 0, (float) (length2 - retractLength), 0);
        renderMatrix.set(transform);
        renderMatrix.preTranslate(operation1.x, operation1.y);
        renderMatrix.preRotate(getEndAngle());
        renderMatrix.preTranslate((float) retractLength, 0);
        shadowRenderer.drawEdgeShadow(canvas, renderMatrix, rect2, shadowElevation);
      }
    }

    float getStartAngle() {
      return (float) Math.toDegrees(Math.atan((operation1.y - startY) / (operation1.x - startX)));
    }

    float getEndAngle() {
      return (float)
          Math.toDegrees(Math.atan((operation2.y - operation1.y) / (operation2.x - operation1.x)));
    }

    /**
     * Returns the sweep angle between the first line to the second line. The sweep angle is
     * directional, i.e., when forming an outer angle, the sweep angle is positive; otherwise, it's
     * negative.
     */
    float getSweepAngle() {
      final float shadowAngle = (getEndAngle() - getStartAngle() + 360) % 360;
      if (shadowAngle <= 180) {
        return shadowAngle;
      } else {
        return shadowAngle - 360;
      }
    }
  }

  /** Sets up the shadow to be drawn for an arc. */
  static class ArcShadowOperation extends ShadowCompatOperation {

    private final PathArcOperation operation;

    public ArcShadowOperation(PathArcOperation operation) {
      this.operation = operation;
    }

    @Override
    public void draw(
        Matrix transform,
        @NonNull ShadowRenderer shadowRenderer,
        int shadowElevation,
        @NonNull Canvas canvas) {
      float startAngle = operation.getStartAngle();
      float sweepAngle = operation.getSweepAngle();
      RectF rect =
          new RectF(
              operation.getLeft(), operation.getTop(), operation.getRight(), operation.getBottom());
      shadowRenderer.drawCornerShadow(
          canvas, transform, rect, shadowElevation, startAngle, sweepAngle);
    }
  }

  /** Interface for a path operation to be appended to the operations list. */
  public abstract static class PathOperation {

    /** A usable {@link Matrix} object for transformations. */
    protected final Matrix matrix = new Matrix();

    /** Applies the given {@code transform} to the provided {@code path}. */
    public abstract void applyToPath(Matrix transform, Path path);
  }

  /** Straight line operation. */
  public static class PathLineOperation extends PathOperation {
    private float x;
    private float y;

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.lineTo(x, y);
      path.transform(transform);
    }
  }

  /** Path quad operation. */
  public static class PathQuadOperation extends PathOperation {
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float controlX;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float controlY;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float endX;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float endY;

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.quadTo(getControlX(), getControlY(), getEndX(), getEndY());
      path.transform(transform);
    }

    private float getEndX() {
      return endX;
    }

    private void setEndX(float endX) {
      this.endX = endX;
    }

    private float getControlY() {
      return controlY;
    }

    private void setControlY(float controlY) {
      this.controlY = controlY;
    }

    private float getEndY() {
      return endY;
    }

    private void setEndY(float endY) {
      this.endY = endY;
    }

    private float getControlX() {
      return controlX;
    }

    private void setControlX(float controlX) {
      this.controlX = controlX;
    }
  }

  /** Path arc operation. */
  public static class PathArcOperation extends PathOperation {
    private static final RectF rectF = new RectF();

    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float left;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float top;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float right;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float bottom;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float startAngle;
    /**
     * @deprecated Use the class methods to interact with this field so internal state can be
     *     maintained.
     */
    @Deprecated public float sweepAngle;

    public PathArcOperation(float left, float top, float right, float bottom) {
      setLeft(left);
      setTop(top);
      setRight(right);
      setBottom(bottom);
    }

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      rectF.set(getLeft(), getTop(), getRight(), getBottom());
      path.arcTo(rectF, getStartAngle(), getSweepAngle(), false);
      path.transform(transform);
    }

    private float getLeft() {
      return left;
    }

    private float getTop() {
      return top;
    }

    private float getRight() {
      return right;
    }

    private float getBottom() {
      return bottom;
    }

    private void setLeft(float left) {
      this.left = left;
    }

    private void setTop(float top) {
      this.top = top;
    }

    private void setRight(float right) {
      this.right = right;
    }

    private void setBottom(float bottom) {
      this.bottom = bottom;
    }

    private float getStartAngle() {
      return startAngle;
    }

    private float getSweepAngle() {
      return sweepAngle;
    }

    private void setStartAngle(float startAngle) {
      this.startAngle = startAngle;
    }

    private void setSweepAngle(float sweepAngle) {
      this.sweepAngle = sweepAngle;
    }
  }

  /** Path cubic operation. */
  public static class PathCubicOperation extends PathOperation {

    private float controlX1;

    private float controlY1;

    private float controlX2;

    private float controlY2;

    private float endX;

    private float endY;

    public PathCubicOperation(
        float controlX1,
        float controlY1,
        float controlX2,
        float controlY2,
        float endX,
        float endY) {
      setControlX1(controlX1);
      setControlY1(controlY1);
      setControlX2(controlX2);
      setControlY2(controlY2);
      setEndX(endX);
      setEndY(endY);
    }

    @Override
    public void applyToPath(@NonNull Matrix transform, @NonNull Path path) {
      Matrix inverse = matrix;
      transform.invert(inverse);
      path.transform(inverse);
      path.cubicTo(controlX1, controlY1, controlX2, controlY2, endX, endY);
      path.transform(transform);
    }

    private float getControlX1() {
      return controlX1;
    }

    private void setControlX1(float controlX1) {
      this.controlX1 = controlX1;
    }

    private float getControlY1() {
      return controlY1;
    }

    private void setControlY1(float controlY1) {
      this.controlY1 = controlY1;
    }

    private float getControlX2() {
      return controlX2;
    }

    private void setControlX2(float controlX2) {
      this.controlX2 = controlX2;
    }

    private float getControlY2() {
      return controlY1;
    }

    private void setControlY2(float controlY2) {
      this.controlY2 = controlY2;
    }

    private float getEndX() {
      return endX;
    }

    private void setEndX(float endX) {
      this.endX = endX;
    }

    private float getEndY() {
      return endY;
    }

    private void setEndY(float endY) {
      this.endY = endY;
    }
  }
}
