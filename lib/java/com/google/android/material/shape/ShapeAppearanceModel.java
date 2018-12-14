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

import com.google.android.material.R;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import androidx.annotation.AttrRes;
import androidx.annotation.Dimension;
import androidx.annotation.RestrictTo;
import androidx.annotation.StyleRes;
import com.google.android.material.internal.Experimental;
import android.util.AttributeSet;
import android.view.ContextThemeWrapper;

/**
 * This class models the edges and corners of a shape, which are used by {@link
 * MaterialShapeDrawable} to generate and render the shape for a view's background.
 */
@Experimental("The shapes API is currently experimental and subject to change")
public class ShapeAppearanceModel {
  private CornerTreatment topLeftCorner;
  private CornerTreatment topRightCorner;
  private CornerTreatment bottomRightCorner;
  private CornerTreatment bottomLeftCorner;
  private EdgeTreatment topEdge;
  private EdgeTreatment rightEdge;
  private EdgeTreatment bottomEdge;
  private EdgeTreatment leftEdge;

  /** Constructs a default path generator with default edge and corner treatments. */
  public ShapeAppearanceModel() {
    setTopLeftCorner(MaterialShapeUtils.createDefaultCornerTreatment());
    setTopRightCorner(MaterialShapeUtils.createDefaultCornerTreatment());
    setBottomRightCorner(MaterialShapeUtils.createDefaultCornerTreatment());
    setBottomLeftCorner(MaterialShapeUtils.createDefaultCornerTreatment());

    setTopEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setRightEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setBottomEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setLeftEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
  }

  public ShapeAppearanceModel(ShapeAppearanceModel shapeAppearanceModel) {
    topLeftCorner = shapeAppearanceModel.getTopLeftCorner().clone();
    topRightCorner = shapeAppearanceModel.getTopRightCorner().clone();
    bottomRightCorner = shapeAppearanceModel.getBottomRightCorner().clone();
    bottomLeftCorner = shapeAppearanceModel.getBottomLeftCorner().clone();

    topEdge = shapeAppearanceModel.getTopEdge().clone();
    rightEdge = shapeAppearanceModel.getRightEdge().clone();
    leftEdge = shapeAppearanceModel.getLeftEdge().clone();
    bottomEdge = shapeAppearanceModel.getBottomEdge().clone();
  }

  public ShapeAppearanceModel(
      Context context, AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
    this(context, attrs, defStyleAttr, defStyleRes, 0);
  }

  public ShapeAppearanceModel(
      Context context,
      AttributeSet attrs,
      @AttrRes int defStyleAttr,
      @StyleRes int defStyleRes,
      int defaultCornerSize) {
    TypedArray a =
        context.obtainStyledAttributes(attrs, R.styleable.MaterialShape, defStyleAttr, defStyleRes);

    int shapeAppearanceResId =
        a.getResourceId(R.styleable.MaterialShape_shapeAppearance, 0);
    int shapeAppearanceOverlayResId =
        a.getResourceId(R.styleable.MaterialShape_shapeAppearanceOverlay, 0);
    a.recycle();

    // The attributes in shapeAppearanceOverlay should be applied on top of shapeAppearance.
    if (shapeAppearanceOverlayResId != 0) {
      context = new ContextThemeWrapper(context, shapeAppearanceResId);
      shapeAppearanceResId = shapeAppearanceOverlayResId;
    }

    a = context.obtainStyledAttributes(shapeAppearanceResId, R.styleable.ShapeAppearance);

    int cornerFamily = a.getInt(R.styleable.ShapeAppearance_cornerFamily, CornerFamily.ROUNDED);
    int cornerFamilyTopLeft =
        a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopLeft, cornerFamily);
    int cornerFamilyTopRight =
        a.getInt(R.styleable.ShapeAppearance_cornerFamilyTopRight, cornerFamily);
    int cornerFamilyBottomRight =
        a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomRight, cornerFamily);
    int cornerFamilyBottomLeft =
        a.getInt(R.styleable.ShapeAppearance_cornerFamilyBottomLeft, cornerFamily);

    int cornerSize =
        a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSize, defaultCornerSize);
    int cornerSizeTopLeft =
        a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeTopLeft, cornerSize);
    int cornerSizeTopRight =
        a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeTopRight, cornerSize);
    int cornerSizeBottomRight =
        a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeBottomRight, cornerSize);
    int cornerSizeBottomLeft =
        a.getDimensionPixelSize(R.styleable.ShapeAppearance_cornerSizeBottomLeft, cornerSize);

    setTopLeftCorner(cornerFamilyTopLeft, cornerSizeTopLeft);
    setTopRightCorner(cornerFamilyTopRight, cornerSizeTopRight);
    setBottomRightCorner(cornerFamilyBottomRight, cornerSizeBottomRight);
    setBottomLeftCorner(cornerFamilyBottomLeft, cornerSizeBottomLeft);

    setTopEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setRightEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setBottomEdge(MaterialShapeUtils.createDefaultEdgeTreatment());
    setLeftEdge(MaterialShapeUtils.createDefaultEdgeTreatment());

    a.recycle();
  }

  /**
   * Sets all corner treatments.
   *
   * @param cornerTreatment the corner treatment to use for all four corners.
   */
  public void setAllCorners(CornerTreatment cornerTreatment) {
    topLeftCorner = cornerTreatment.clone();
    topRightCorner = cornerTreatment.clone();
    bottomRightCorner = cornerTreatment.clone();
    bottomLeftCorner = cornerTreatment.clone();
  }

  public void setAllCorners(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setAllCorners(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  public void setCornerRadius(float cornerRadius) {
    topLeftCorner.setCornerSize(cornerRadius);
    topRightCorner.setCornerSize(cornerRadius);
    bottomRightCorner.setCornerSize(cornerRadius);
    bottomLeftCorner.setCornerSize(cornerRadius);
  }

  public void setCornerRadii(
      float topLeftCornerRadius,
      float topRightCornerRadius,
      float bottomRightCornerRadius,
      float bottomLeftCornerRadius) {
    topLeftCorner.setCornerSize(topLeftCornerRadius);
    topRightCorner.setCornerSize(topRightCornerRadius);
    bottomRightCorner.setCornerSize(bottomRightCornerRadius);
    bottomLeftCorner.setCornerSize(bottomLeftCornerRadius);
  }

  /**
   * Sets all edge treatments.
   *
   * @param edgeTreatment the edge treatment to use for all four edges.
   */
  public void setAllEdges(EdgeTreatment edgeTreatment) {
    leftEdge = edgeTreatment.clone();
    topEdge = edgeTreatment.clone();
    rightEdge = edgeTreatment.clone();
    bottomEdge = edgeTreatment.clone();
  }

  /**
   * Sets corner treatments.
   *
   * @param topLeftCorner the corner treatment to use in the top-left corner.
   * @param topRightCorner the corner treatment to use in the top-right corner.
   * @param bottomRightCorner the corner treatment to use in the bottom-right corner.
   * @param bottomLeftCorner the corner treatment to use in the bottom-left corner.
   */
  public void setCornerTreatments(
      CornerTreatment topLeftCorner,
      CornerTreatment topRightCorner,
      CornerTreatment bottomRightCorner,
      CornerTreatment bottomLeftCorner) {
    this.topLeftCorner = topLeftCorner;
    this.topRightCorner = topRightCorner;
    this.bottomRightCorner = bottomRightCorner;
    this.bottomLeftCorner = bottomLeftCorner;
  }

  /**
   * Sets edge treatments.
   *
   * @param leftEdge the edge treatment to use on the left edge.
   * @param topEdge the edge treatment to use on the top edge.
   * @param rightEdge the edge treatment to use on the right edge.
   * @param bottomEdge the edge treatment to use on the bottom edge.
   */
  public void setEdgeTreatments(
      EdgeTreatment leftEdge,
      EdgeTreatment topEdge,
      EdgeTreatment rightEdge,
      EdgeTreatment bottomEdge) {
    this.leftEdge = leftEdge;
    this.topEdge = topEdge;
    this.rightEdge = rightEdge;
    this.bottomEdge = bottomEdge;
  }

  /**
   * Sets the corner treatment for the top-left corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setTopLeftCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setTopLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the top-left corner.
   *
   * @param topLeftCorner the desired treatment.
   */
  public void setTopLeftCorner(CornerTreatment topLeftCorner) {
    this.topLeftCorner = topLeftCorner;
  }

  /**
   * Gets the corner treatment for the top-left corner.
   *
   * @return the corner treatment for the top-left corner.
   */
  public CornerTreatment getTopLeftCorner() {
    return topLeftCorner;
  }

  /**
   * Sets the corner treatment for the top-right corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setTopRightCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setTopRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the top-right corner.
   *
   * @param topRightCorner the desired treatment.
   */
  public void setTopRightCorner(CornerTreatment topRightCorner) {
    this.topRightCorner = topRightCorner;
  }

  /**
   * Gets the corner treatment for the top-right corner.
   *
   * @return the corner treatment for the top-right corner.
   */
  public CornerTreatment getTopRightCorner() {
    return topRightCorner;
  }

  /**
   * Sets the corner treatment for the bottom-right corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setBottomRightCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setBottomRightCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the bottom-right corner.
   *
   * @param bottomRightCorner the desired treatment.
   */
  public void setBottomRightCorner(CornerTreatment bottomRightCorner) {
    this.bottomRightCorner = bottomRightCorner;
  }

  /**
   * Gets the corner treatment for the bottom-right corner.
   *
   * @return the corner treatment for the bottom-right corner.
   */
  public CornerTreatment getBottomRightCorner() {
    return bottomRightCorner;
  }

  /**
   * Sets the corner treatment for the bottom-left corner.
   *
   * @param cornerFamily the family to use to create the corner treatment
   * @param cornerSize the size to use to create the corner treatment
   */
  public void setBottomLeftCorner(@CornerFamily int cornerFamily, @Dimension int cornerSize) {
    setBottomLeftCorner(MaterialShapeUtils.createCornerTreatment(cornerFamily, cornerSize));
  }

  /**
   * Sets the corner treatment for the bottom-left corner.
   *
   * @param bottomLeftCorner the desired treatment.
   */
  public void setBottomLeftCorner(CornerTreatment bottomLeftCorner) {
    this.bottomLeftCorner = bottomLeftCorner;
  }

  /**
   * Gets the corner treatment for the bottom-left corner.
   *
   * @return the corner treatment for the bottom-left corner.
   */
  public CornerTreatment getBottomLeftCorner() {
    return bottomLeftCorner;
  }

  /**
   * Sets the edge treatment for the top edge.
   *
   * @param topEdge the desired treatment.
   */
  public void setTopEdge(EdgeTreatment topEdge) {
    this.topEdge = topEdge;
  }

  /**
   * Gets the edge treatment for the top edge.
   *
   * @return the edge treatment for the top edge.
   */
  public EdgeTreatment getTopEdge() {
    return topEdge;
  }

  /**
   * Sets the edge treatment for the right edge.
   *
   * @param rightEdge the desired treatment.
   */
  public void setRightEdge(EdgeTreatment rightEdge) {
    this.rightEdge = rightEdge;
  }

  /**
   * Gets the edge treatment for the right edge.
   *
   * @return the edge treatment for the right edge.
   */
  public EdgeTreatment getRightEdge() {
    return rightEdge;
  }

  /**
   * Sets the edge treatment for the bottom edge.
   *
   * @param bottomEdge the desired treatment.
   */
  public void setBottomEdge(EdgeTreatment bottomEdge) {
    this.bottomEdge = bottomEdge;
  }

  /**
   * Gets the edge treatment for the bottom edge.
   *
   * @return the edge treatment for the bottom edge.
   */
  public EdgeTreatment getBottomEdge() {
    return bottomEdge;
  }

  /**
   * Sets the edge treatment for the left edge.
   *
   * @param leftEdge the desired treatment.
   */
  public void setLeftEdge(EdgeTreatment leftEdge) {
    this.leftEdge = leftEdge;
  }

  /**
   * Gets the edge treatment for the left edge.
   *
   * @return the edge treatment for the left edge.
   */
  public EdgeTreatment getLeftEdge() {
    return leftEdge;
  }

  /**
   * Checks Corner and Edge treatments to see if we can use {@link Canvas#drawRoundRect(RectF,float,
   * float, Paint)} "} to draw this model.
   *
   * @hide
   */
  @RestrictTo(LIBRARY_GROUP)
  public boolean isRoundRect() {
    boolean hasDefaultEdges =
        leftEdge.getClass().equals(EdgeTreatment.class)
            && rightEdge.getClass().equals(EdgeTreatment.class)
            && topEdge.getClass().equals(EdgeTreatment.class)
            && bottomEdge.getClass().equals(EdgeTreatment.class);

    float cornerSize = topLeftCorner.getCornerSize();

    boolean cornersHaveSameSize =
        topRightCorner.getCornerSize() == cornerSize
            && bottomLeftCorner.getCornerSize() == cornerSize
            && bottomRightCorner.getCornerSize() == cornerSize;

    boolean hasRoundedCorners =
        topRightCorner instanceof RoundedCornerTreatment
            && topLeftCorner instanceof RoundedCornerTreatment
            && bottomRightCorner instanceof RoundedCornerTreatment
            && bottomLeftCorner instanceof RoundedCornerTreatment;

    return hasDefaultEdges && cornersHaveSameSize && hasRoundedCorners;
  }
}
