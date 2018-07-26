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

import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Rect;
import android.graphics.Region;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import com.google.android.material.internal.Experimental;
import android.support.v4.graphics.drawable.TintAwareDrawable;

/**
 * Base drawable class for Material Shapes that handles shadows, elevation, scale and color for a
 * generated path.
 */
@Experimental("The shapes API is currently experimental and subject to change")
public class MaterialShapeDrawable extends Drawable implements TintAwareDrawable {

  private final Paint paint = new Paint();
  // Inter-method state.
  private final Matrix[] cornerTransforms = new Matrix[4];
  private final Matrix[] edgeTransforms = new Matrix[4];
  private final ShapePath[] cornerPaths = new ShapePath[4];
  // Pre-allocated objects that are re-used several times during path computation and rendering.
  private final Matrix matrix = new Matrix();
  private final Path path = new Path();
  private final PointF pointF = new PointF();
  private final ShapePath shapePath = new ShapePath();
  private final Region transparentRegion = new Region();
  private final Region scratchRegion = new Region();
  private final float[] scratch = new float[2];
  private final float[] scratch2 = new float[2];
  @Nullable private ShapePathModel shapedViewModel = null;
  private boolean shadowEnabled = false;
  private boolean useTintColorForShadow = false;
  private float interpolation = 1f;
  private int shadowColor = Color.BLACK;
  private int shadowElevation = 5;
  private int shadowRadius = 10;
  private int alpha = 255;
  private float scale = 1f;
  private float strokeWidth = 0f;
  private Style paintStyle = Style.FILL_AND_STROKE;
  @Nullable private PorterDuffColorFilter tintFilter;
  private PorterDuff.Mode tintMode = PorterDuff.Mode.SRC_IN;
  private ColorStateList tintList = null;

  /** Instantiate a basic Material Shape Drawable with no {@link ShapePathModel}. */
  public MaterialShapeDrawable() {
    this(null);
  }

  /**
   * Instantiate with a given model.
   *
   * @param shapePathModel the {@link ShapePathModel} containing the path that will be rendered in
   *     this drawable.
   */
  public MaterialShapeDrawable(@Nullable ShapePathModel shapePathModel) {
    this.shapedViewModel = shapePathModel;

    for (int i = 0; i < 4; i++) {
      cornerTransforms[i] = new Matrix();
      edgeTransforms[i] = new Matrix();
      cornerPaths[i] = new ShapePath();
    }
  }

  private static int modulateAlpha(int paintAlpha, int alpha) {
    int scale = alpha + (alpha >>> 7); // convert to 0..256
    return (paintAlpha * scale) >>> 8;
  }

  /**
   * Get the {@link ShapePathModel} containing the path that will be rendered in this drawable.
   *
   * @return the current model.
   */
  @Nullable
  public ShapePathModel getShapedViewModel() {
    return shapedViewModel;
  }

  /**
   * Set the {@link ShapePathModel} containing the path that will be rendered in this drawable.
   *
   * @param shapedViewModel the desired model.
   */
  public void setShapedViewModel(ShapePathModel shapedViewModel) {
    this.shapedViewModel = shapedViewModel;
    invalidateSelf();
  }

  /**
   * Get the tint list used by the shape's paint.
   *
   * @return current tint list.
   */
  public ColorStateList getTintList() {
    return tintList;
  }

  @Override
  public void setTintList(ColorStateList tintList) {
    this.tintList = tintList;
    updateTintFilter();
    invalidateSelf();
  }

  @Override
  public void setTintMode(PorterDuff.Mode tintMode) {
    this.tintMode = tintMode;
    updateTintFilter();
    invalidateSelf();
  }

  @Override
  public void setTint(@ColorInt int tintColor) {
    setTintList(ColorStateList.valueOf(tintColor));
  }

  @Override
  public int getOpacity() {
    // OPAQUE or TRANSPARENT are possible, but the complexity of determining this based on the
    // shape model outweighs the optimizations gained.
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public void setAlpha(@IntRange(from = 0, to = 255) int alpha) {
    this.alpha = alpha;
    invalidateSelf();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    paint.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public Region getTransparentRegion() {
    Rect bounds = getBounds();
    transparentRegion.set(bounds);
    getPath(bounds.width(), bounds.height(), path);
    scratchRegion.setPath(path, transparentRegion);
    transparentRegion.op(scratchRegion, Op.DIFFERENCE);
    return transparentRegion;
  }

  /**
   * Determines whether a point is contained within the transparent region of the Drawable. A return
   * value of true generally suggests that the touched view should not process a touch event at that
   * point.
   *
   * @param x The X coordinate of the point.
   * @param y The Y coordinate of the point.
   * @return true iff the point is contained in the transparent region of the Drawable.
   */
  public boolean isPointInTransparentRegion(int x, int y) {
    return getTransparentRegion().contains(x, y);
  }

  /**
   * Get shadow rendering status.
   *
   * @return true if shadows are enabled; false if not.
   */
  public boolean isShadowEnabled() {
    return shadowEnabled;
  }

  /**
   * Set shadow rendering enabled or disabled.
   *
   * @param shadowEnabled true if shadows are enabled; false if not.
   */
  public void setShadowEnabled(boolean shadowEnabled) {
    this.shadowEnabled = shadowEnabled;
    invalidateSelf();
  }

  /**
   * Get the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated.
   *
   * @return the interpolation of the path.
   */
  public float getInterpolation() {
    return interpolation;
  }

  /**
   * Set the interpolation of the path, between 0 and 1. Ranges between 0 (none) and 1 (fully)
   * interpolated. An interpolation of 1 generally indicates a fully rendered path, while an
   * interpolation of 0 generally indicates a fully healed path, which is usually a rectangle.
   *
   * @param interpolation the desired interpolation.
   */
  public void setInterpolation(float interpolation) {
    this.interpolation = interpolation;
    invalidateSelf();
  }

  /**
   * Get the shadow elevation rendered by the path.
   *
   * @return shadow elevation rendered by the path.
   */
  public int getShadowElevation() {
    return shadowElevation;
  }

  /**
   * Set the shadow elevation rendered by the path.
   *
   * @param shadowElevation the desired elevation.
   */
  public void setShadowElevation(int shadowElevation) {
    this.shadowElevation = shadowElevation;
    invalidateSelf();
  }

  /**
   * Get the shadow radius rendered by the path.
   *
   * @return the shadow radius rendered by the path.
   */
  public int getShadowRadius() {
    return shadowRadius;
  }

  /**
   * Set the shadow radius rendered by the path.
   *
   * @param shadowRadius the desired shadow radius.
   */
  public void setShadowRadius(int shadowRadius) {
    this.shadowRadius = shadowRadius;
    invalidateSelf();
  }

  /**
   * Get the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @return the scale of the path.
   */
  public float getScale() {
    return scale;
  }

  /**
   * Set the scale of the rendered path. A value of 1 renders it at 100% size.
   *
   * @param scale the desired scale.
   */
  public void setScale(float scale) {
    this.scale = scale;
    invalidateSelf();
  }

  /**
   * Set whether shadow color should match next set tint color.
   *
   * @param useTintColorForShadow true if color should match; false otherwise.
   */
  public void setUseTintColorForShadow(boolean useTintColorForShadow) {
    this.useTintColorForShadow = useTintColorForShadow;
    invalidateSelf();
  }

  /**
   * Set color of shadow rendered behind shape.
   *
   * @param shadowColor desired color.
   */
  public void setShadowColor(int shadowColor) {
    this.shadowColor = shadowColor;
    useTintColorForShadow = false;
    invalidateSelf();
  }

  /**
   * Get the current flags used by the shape's paint.
   *
   * @return current paint flags.
   */
  public int getPaintFlags() {
    return paint.getFlags();
  }

  /**
   * Set the flags used by the shape's paint.
   *
   * @param flags the desired flags.
   */
  public void setPaintFlags(int flags) {
    paint.setFlags(flags);
    invalidateSelf();
  }

  /**
   * Get the current style used by the shape's paint.
   *
   * @return current used paint style.
   */
  public Style getPaintStyle() {
    return paintStyle;
  }

  /**
   * Set the style used by the shape's paint.
   *
   * @param paintStyle the desired style.
   */
  public void setPaintStyle(Style paintStyle) {
    this.paintStyle = paintStyle;
    invalidateSelf();
  }

  /**
   * Get the stroke width used by the shape's paint.
   *
   * @return current stroke width.
   */
  public float getStrokeWidth() {
    return strokeWidth;
  }

  /**
   * Set the stroke width used by the shape's paint.
   *
   * @param strokeWidth desired stroke width.
   */
  public void setStrokeWidth(float strokeWidth) {
    this.strokeWidth = strokeWidth;
    invalidateSelf();
  }

  @Override
  public void draw(Canvas canvas) {
    paint.setColorFilter(tintFilter);
    final int prevAlpha = paint.getAlpha();
    paint.setAlpha(modulateAlpha(prevAlpha, alpha));
    paint.setStrokeWidth(strokeWidth);
    paint.setStyle(paintStyle);
    if (shadowElevation > 0 && shadowEnabled) {
      paint.setShadowLayer(shadowRadius, 0, shadowElevation, shadowColor);
    }
    if (shapedViewModel != null) {
      getPath(canvas.getWidth(), canvas.getHeight(), path);
      canvas.drawPath(path, paint);
    } else {
      canvas.drawRect(0, 0, canvas.getWidth(), canvas.getHeight(), paint);
    }
    paint.setAlpha(prevAlpha);
  }

  /**
   * Writes to the given {@link Path} for the current edge and corner treatments at the specified
   * size.
   *
   * @param width width of target path.
   * @param height height of target path.
   * @param path the returned path out-var.
   * @return the generated path.
   */
  public void getPathForSize(int width, int height, Path path) {
    path.rewind();

    if (shapedViewModel == null) {
      return;
    }

    // Calculate the transformations (rotations and translations) necessary for each edge and
    // corner treatment.
    for (int index = 0; index < 4; index++) {
      setCornerPathAndTransform(index, width, height);
      setEdgeTransform(index, width, height);
    }

    // Apply corners and edges to the path in clockwise interleaving sequence: top-left corner, top
    // edge, top-right corner, right edge, bottom-right corner, etc.
    for (int index = 0; index < 4; index++) {
      appendCornerPath(index, path);
      appendEdgePath(index, path);
    }

    path.close();
  }

  private void setCornerPathAndTransform(int index, int width, int height) {
    float angle = angleOfCorner(index, width, height);
    getCornerTreatmentForIndex(index).getCornerPath(angle, interpolation, cornerPaths[index]);

    float prevEdgeAngle = angleOfEdge((index - 1 + 4) % 4, width, height) + (float) Math.PI / 2f;
    cornerTransforms[index].reset();
    getCoordinatesOfCorner(index, width, height, pointF);
    cornerTransforms[index].setTranslate(pointF.x, pointF.y);
    cornerTransforms[index].preRotate((float) Math.toDegrees(prevEdgeAngle));
  }

  private void setEdgeTransform(int index, int width, int height) {
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);
    float edgeAngle = angleOfEdge(index, width, height);
    edgeTransforms[index].reset();
    edgeTransforms[index].setTranslate(scratch[0], scratch[1]);
    edgeTransforms[index].preRotate((float) Math.toDegrees(edgeAngle));
  }

  private void appendCornerPath(int index, Path path) {
    scratch[0] = cornerPaths[index].startX;
    scratch[1] = cornerPaths[index].startY;
    cornerTransforms[index].mapPoints(scratch);
    if (index == 0) {
      path.moveTo(scratch[0], scratch[1]);
    } else {
      path.lineTo(scratch[0], scratch[1]);
    }
    cornerPaths[index].applyToPath(cornerTransforms[index], path);
  }

  private void appendEdgePath(int index, Path path) {
    int nextIndex = (index + 1) % 4;
    scratch[0] = cornerPaths[index].endX;
    scratch[1] = cornerPaths[index].endY;
    cornerTransforms[index].mapPoints(scratch);

    scratch2[0] = cornerPaths[nextIndex].startX;
    scratch2[1] = cornerPaths[nextIndex].startY;
    cornerTransforms[nextIndex].mapPoints(scratch2);

    float edgeLength = (float) Math.hypot(scratch[0] - scratch2[0], scratch[1] - scratch2[1]);
    shapePath.reset(0, 0);
    getEdgeTreatmentForIndex(index).getEdgePath(edgeLength, interpolation, shapePath);
    shapePath.applyToPath(edgeTransforms[index], path);
  }

  private CornerTreatment getCornerTreatmentForIndex(int index) {
    switch (index) {
      case 1:
        return shapedViewModel.getTopRightCorner();
      case 2:
        return shapedViewModel.getBottomRightCorner();
      case 3:
        return shapedViewModel.getBottomLeftCorner();
      case 0:
      default:
        return shapedViewModel.getTopLeftCorner();
    }
  }

  private EdgeTreatment getEdgeTreatmentForIndex(int index) {
    switch (index) {
      case 1:
        return shapedViewModel.getRightEdge();
      case 2:
        return shapedViewModel.getBottomEdge();
      case 3:
        return shapedViewModel.getLeftEdge();
      case 0:
      default:
        return shapedViewModel.getTopEdge();
    }
  }

  private void getCoordinatesOfCorner(int index, int width, int height, PointF pointF) {
    switch (index) {
      case 1: // top-right
        pointF.set(width, 0);
        break;
      case 2: // bottom-right
        pointF.set(width, height);
        break;
      case 3: // bottom-left
        pointF.set(0, height);
        break;
      case 0: // top-left
      default:
        pointF.set(0, 0);
        break;
    }
  }

  private float angleOfCorner(int index, int width, int height) {
    getCoordinatesOfCorner((index - 1 + 4) % 4, width, height, pointF);
    float prevCornerCoordX = pointF.x;
    float prevCornerCoordY = pointF.y;

    getCoordinatesOfCorner((index + 1) % 4, width, height, pointF);
    float nextCornerCoordX = pointF.x;
    float nextCornerCoordY = pointF.y;

    getCoordinatesOfCorner(index, width, height, pointF);
    float cornerCoordX = pointF.x;
    float cornerCoordY = pointF.y;

    float prevVectorX = prevCornerCoordX - cornerCoordX;
    float prevVectorY = prevCornerCoordY - cornerCoordY;

    float nextVectorX = nextCornerCoordX - cornerCoordX;
    float nextVectorY = nextCornerCoordY - cornerCoordY;

    float prevAngle = (float) Math.atan2(prevVectorY, prevVectorX);
    float nextAngle = (float) Math.atan2(nextVectorY, nextVectorX);
    float angle = prevAngle - nextAngle;
    if (angle < 0) {
      angle = (float) (angle + 2 * Math.PI);
    }
    return angle;
  }

  private float angleOfEdge(int index, int width, int height) {
    int startCornerPosition = index;
    int endCornerPoisition = (index + 1) % 4;

    getCoordinatesOfCorner(startCornerPosition, width, height, pointF);
    float startCornerCoordX = pointF.x;
    float startCornerCoordY = pointF.y;

    getCoordinatesOfCorner(endCornerPoisition, width, height, pointF);
    float endCornerCoordX = pointF.x;
    float endCornerCoordY = pointF.y;

    float edgeVectorX = endCornerCoordX - startCornerCoordX;
    float edgeVectorY = endCornerCoordY - startCornerCoordY;
    return (float) Math.atan2(edgeVectorY, edgeVectorX);
  }

  private void getPath(int width, int height, Path path) {
    getPathForSize(width, height, path);
    if (scale == 1f) {
      return;
    }
    matrix.reset();
    matrix.setScale(scale, scale, width / 2, height / 2);
    path.transform(matrix);
  }

  private void updateTintFilter() {
    if (tintList == null || tintMode == null) {
      tintFilter = null;
      return;
    }
    final int color = tintList.getColorForState(getState(), Color.TRANSPARENT);
    tintFilter = new PorterDuffColorFilter(color, tintMode);
    if (useTintColorForShadow) {
      shadowColor = color;
    }
  }
}
