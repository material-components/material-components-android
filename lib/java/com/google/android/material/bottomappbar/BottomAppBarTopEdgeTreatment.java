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

package com.google.android.material.bottomappbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.shape.EdgeTreatment;
import com.google.android.material.shape.ShapePath;

/**
 * Top edge treatment for the bottom app bar which "cradles" a circular {@link
 * FloatingActionButton}.
 *
 * <p>This edge features a downward semi-circular cutout from the edge line. The two corners created
 * by the cutout can optionally be rounded. The circular cutout can also support a vertically offset
 * FloatingActionButton; i.e., the cut-out need not be a perfect semi-circle, but could be an arc of
 * less than 180 degrees that does not start or finish with a vertical path. This vertical offset
 * must be positive.
 */
public class BottomAppBarTopEdgeTreatment extends EdgeTreatment {

  private final float cradleDiameter;
  private final float roundedCornerRadius;
  private float cradleVerticalOffset;
  private float horizontalOffset;

  /**
   * @param cradleDiameter the diameter, in pixels, of the semi-circular cutout.
   * @param roundedCornerRadius the radius, in pixels, of the rounded corners created by the cutout.
   *     A value of 0 will produce a sharp cutout.
   * @param cradleVerticalOffset vertical offset, in pixels, of the {@link
   *     FloatingActionButton} being cradled. An offset of 0 indicates
   *     the vertical center of the {@link FloatingActionButton} is
   *     positioned on the top edge.
   */
  public BottomAppBarTopEdgeTreatment(
      float cradleDiameter, float roundedCornerRadius, float cradleVerticalOffset) {
    this.cradleDiameter = cradleDiameter;
    this.roundedCornerRadius = roundedCornerRadius;
    this.cradleVerticalOffset = cradleVerticalOffset;
    // TODO: potentially support negative values.
    if (cradleVerticalOffset < 0) {
      throw new IllegalArgumentException("cradleVerticalOffset must be positive.");
    }
    this.horizontalOffset = 0f;
  }

  /** Set the horizontal offset, in pixels, of the cradle from center. */
  public void setHorizontalOffset(float horizontalOffset) {
    this.horizontalOffset = horizontalOffset;
  }

  /**
   * @return the horizontal offset, in pixels, of the cradle from center.
   */
  public float getHorizontalOffset() {
    return horizontalOffset;
  }

  @Override
  public void getEdgePath(float length, float interpolation, ShapePath shapePath) {
    float cradleRadius = interpolation * cradleDiameter / 2f;
    float roundedCornerOffset = interpolation * roundedCornerRadius;
    float middle = length / 2f + horizontalOffset;
    float verticalOffset = interpolation * cradleVerticalOffset;
    float verticalOffsetRatio = verticalOffset / cradleRadius;
    if (verticalOffsetRatio >= 1.0f) {
      // Vertical offset is so high that there's no curve to draw in the edge, i.e., the fab is
      // actually above the edge so just draw a straight line.
      shapePath.lineTo(length, 0);
      return; // Early exit.
    }

    // Calculate the width of the cut part of the circle using the pythagorean theorem
    float offsetSquared = verticalOffset * verticalOffset;
    float cutWidth = (float) Math.sqrt(cradleRadius * cradleRadius - offsetSquared);

    float lowerCurveLeft = middle - cutWidth;
    float lineLeft = lowerCurveLeft - roundedCornerOffset;
    float lowerCurveRight = middle + cutWidth;
    float lineRight = lowerCurveRight + roundedCornerOffset;
    shapePath.lineTo(lineLeft, 0);
    shapePath.addArc(lineLeft, 0, lowerCurveLeft, roundedCornerOffset, 270, 90);
    float top = -cradleRadius - verticalOffset;
    float bottom = cradleRadius - verticalOffset;
    shapePath.addArc(middle - cradleRadius, top, middle + cradleRadius, bottom, 180, -180);
    shapePath.addArc(lowerCurveRight, 0, lineRight, roundedCornerOffset, 180, 90);
    shapePath.lineTo(length, 0);
  }

  /**
   * @return vertical offset, in pixels, of the {@link
   *     FloatingActionButton} being cradled. An offset of 0 indicates
   *     the vertical center of the {@link FloatingActionButton} is
   *     positioned on the top edge.
   */
  public float getCradleVerticalOffset() {
    return cradleVerticalOffset;
  }

  /**
   * @param cradleVerticalOffset vertical offset, in pixels, of the {@link
   *     FloatingActionButton} being cradled. An offset of 0 indicates
   *     the vertical center of the {@link FloatingActionButton} is
   *     positioned on the top edge.
   */
  public void setCradleVerticalOffset(float cradleVerticalOffset) {
    this.cradleVerticalOffset = cradleVerticalOffset;
  }
}
