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

/**
 * This class models the edges and corners of a shape, which are used by {@link
 * MaterialShapeDrawable} to generate and render the shape for a view's background.
 *
 * @deprecated Use {@link ShapeAppearanceModel} instead.
 */
@Deprecated
public class ShapePathModel extends ShapeAppearanceModel {

  /**
   * Set all corner treatments.
   *
   * @param cornerTreatment the corner treatment to use in all four corners.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setAllCorners(CornerTreatment cornerTreatment) {
    topLeftCorner = cornerTreatment;
    topRightCorner = cornerTreatment;
    bottomRightCorner = cornerTreatment;
    bottomLeftCorner = cornerTreatment;
  }

  /**
   * Set all edge treatments.
   *
   * @param edgeTreatment the edge treatment to use for all four edges.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setAllEdges(EdgeTreatment edgeTreatment) {
    leftEdge = edgeTreatment;
    topEdge = edgeTreatment;
    rightEdge = edgeTreatment;
    bottomEdge = edgeTreatment;
  }

  /**
   * Set corner treatments.
   *
   * @param topLeftCorner the corner treatment to use in the top-left corner.
   * @param topRightCorner the corner treatment to use in the top-right corner.
   * @param bottomRightCorner the corner treatment to use in the bottom-right corner.
   * @param bottomLeftCorner the corner treatment to use in the bottom-left corner.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
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
   * Set edge treatments.
   *
   * @param leftEdge the edge treatment to use on the left edge.
   * @param topEdge the edge treatment to use on the top edge.
   * @param rightEdge the edge treatment to use on the right edge.
   * @param bottomEdge the edge treatment to use on the bottom edge.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
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
   * Set the corner treatment for the top-left corner.
   *
   * @param topLeftCorner the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setTopLeftCorner(CornerTreatment topLeftCorner) {
    this.topLeftCorner = topLeftCorner;
  }

  /**
   * Set the corner treatment for the top-right corner.
   *
   * @param topRightCorner the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setTopRightCorner(CornerTreatment topRightCorner) {
    this.topRightCorner = topRightCorner;
  }

  /**
   * Set the corner treatment for the bottom-right corner.
   *
   * @param bottomRightCorner the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setBottomRightCorner(CornerTreatment bottomRightCorner) {
    this.bottomRightCorner = bottomRightCorner;
  }

  /**
   * Set the corner treatment for the bottom-left corner.
   *
   * @param bottomLeftCorner the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setBottomLeftCorner(CornerTreatment bottomLeftCorner) {
    this.bottomLeftCorner = bottomLeftCorner;
  }

  /**
   * Set the edge treatment for the top edge.
   *
   * @param topEdge the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setTopEdge(EdgeTreatment topEdge) {
    this.topEdge = topEdge;
  }

  /**
   * Set the edge treatment for the right edge.
   *
   * @param rightEdge the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setRightEdge(EdgeTreatment rightEdge) {
    this.rightEdge = rightEdge;
  }

  /**
   * Set the edge treatment for the bottom edge.
   *
   * @param bottomEdge the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setBottomEdge(EdgeTreatment bottomEdge) {
    this.bottomEdge = bottomEdge;
  }

  /**
   * Set the edge treatment for the left edge.
   *
   * @param leftEdge the desired treatment.
   * @deprecated Use {@link ShapeAppearanceModel} instead.
   */
  @Deprecated
  public void setLeftEdge(EdgeTreatment leftEdge) {
    this.leftEdge = leftEdge;
  }
}
