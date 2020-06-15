/*
 * Copyright (C) 2020 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.google.android.material.progressindicator;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.RectF;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

/** A delegate class to help draw the graphics for {@link ProgressIndicator} in linear types. */
final class LinearDrawingDelegate implements DrawingDelegate {

  // The length (horizontal) of the track in px.
  private float trackLength = 300f;

  /**
   * Adjusts the canvas for linear progress indicator drawables. It flips the canvas horizontally if
   * it's inverted. It flips the canvas vertically if outgoing grow mode is applied.
   *
   * @param canvas Canvas to draw.
   * @param progressIndicator The component currently serving.
   * @param widthFraction A fraction representing how wide the drawing should be.
   */
  @Override
  public void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull ProgressIndicator progressIndicator,
      @FloatRange(from = 0.0, to = 1.0) float widthFraction) {
    // Gets clip bounds from canvas.
    Rect clipBounds = canvas.getClipBounds();
    trackLength = clipBounds.width();
    float trackWidth = progressIndicator.getIndicatorWidth();

    // Positions canvas to center of the clip bounds.
    canvas.translate(
        clipBounds.width() / 2f,
        clipBounds.height() / 2f
            + Math.max(0f, (clipBounds.height() - progressIndicator.getIndicatorWidth()) / 2f));

    // Flips canvas horizontally if inverse.
    if (progressIndicator.isInverse()) {
      canvas.scale(-1f, 1f);
    }
    // Flips canvas vertically if grow upward.
    if (progressIndicator.getGrowMode() == ProgressIndicator.GROW_MODE_OUTGOING) {
      canvas.scale(1f, -1f);
    }
    // Offsets canvas vertically if grow from top/bottom.
    if (progressIndicator.getGrowMode() == ProgressIndicator.GROW_MODE_INCOMING
        || progressIndicator.getGrowMode() == ProgressIndicator.GROW_MODE_OUTGOING) {
      canvas.translate(0f, progressIndicator.getIndicatorWidth() * (widthFraction - 1) / 2f);
    }

    // Clips all drawing to the track area, so it doesn't draw outside of its bounds (which can
    // happen in certain configurations of clipToPadding and clipChildren)
    canvas.clipRect(-trackLength / 2, -trackWidth / 2, trackLength / 2, trackWidth / 2);
  }

  /**
   * Fills a part of the track with input color. The filling part is defined with two fractions
   * normalized to [0, 1] representing the start position and end position from the left end (the
   * right end if inverse).
   *
   * @param canvas Canvas to draw.
   * @param paint Paint used to draw.
   * @param color The filled color.
   * @param startFraction A fraction representing where to start the drawing along the track.
   * @param endFraction A fraction representing where to end the drawing along the track.
   * @param trackWidth The width of the track in px.
   * @param cornerRadius The radius of corners in px, if rounded corners are applied.
   */
  @Override
  public void fillTrackWithColor(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      float trackWidth,
      float cornerRadius) {
    // No need to draw if startFraction and endFraction are same.
    if (startFraction == endFraction) {
      return;
    }
    // Initializes Paint object.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(color);

    // The rounded corners are drawn in steps, since drawRoundRect() is only available in Api 21+.
    PointF leftTopCornerCenter =
        new PointF(
            -trackLength / 2 + cornerRadius + startFraction * (trackLength - 2 * cornerRadius),
            -trackWidth / 2 + cornerRadius);
    PointF rightBottomCornerCenter =
        new PointF(
            -trackLength / 2 + cornerRadius + endFraction * (trackLength - 2 * cornerRadius),
            trackWidth / 2 - cornerRadius);

    if (cornerRadius > 0) {
      RectF cornerPatternRectBound =
          new RectF(-cornerRadius, -cornerRadius, cornerRadius, cornerRadius);
      // Draws left top corner.
      drawRoundedCorner(
          canvas,
          paint,
          leftTopCornerCenter.x,
          leftTopCornerCenter.y,
          180,
          90,
          cornerPatternRectBound);
      // Draws left bottom corner.
      drawRoundedCorner(
          canvas,
          paint,
          leftTopCornerCenter.x,
          rightBottomCornerCenter.y,
          180,
          -90,
          cornerPatternRectBound);
      // Draws right top corner.
      drawRoundedCorner(
          canvas,
          paint,
          rightBottomCornerCenter.x,
          leftTopCornerCenter.y,
          0,
          -90,
          cornerPatternRectBound);
      // Draws right bottom corner.
      drawRoundedCorner(
          canvas,
          paint,
          rightBottomCornerCenter.x,
          rightBottomCornerCenter.y,
          0,
          90,
          cornerPatternRectBound);
      // Fills the gaps between two vertically aligned corners, if any.
      if (trackWidth > 2 * cornerRadius) {
        canvas.drawRect(
            leftTopCornerCenter.x - cornerRadius,
            leftTopCornerCenter.y,
            leftTopCornerCenter.x,
            rightBottomCornerCenter.y,
            paint);
        canvas.drawRect(
            rightBottomCornerCenter.x,
            leftTopCornerCenter.y,
            rightBottomCornerCenter.x + cornerRadius,
            rightBottomCornerCenter.y,
            paint);
      }
    }
    // Fills gaps between two horizontally aligned corners.
    canvas.drawRect(
        leftTopCornerCenter.x, -trackWidth / 2, rightBottomCornerCenter.x, trackWidth / 2, paint);
  }

  private static void drawRoundedCorner(
      Canvas canvas,
      Paint paint,
      float centerX,
      float centerY,
      float startAngle,
      float sweepAngle,
      RectF cornerPatternRectBound) {
    canvas.save();
    canvas.translate(centerX, centerY);
    canvas.drawArc(cornerPatternRectBound, startAngle, sweepAngle, true, paint);
    canvas.restore();
  }
}
