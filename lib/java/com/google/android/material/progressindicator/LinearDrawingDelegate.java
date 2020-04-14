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
import android.graphics.Rect;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;

/** A delegate class to help draw the graphics for {@link ProgressIndicator} in linear types. */
final class LinearDrawingDelegate implements DrawingDelegate {
  // Constants for drawing linear types.
  private static final float TRACK_WIDTH_LOCAL = 360f;
  private static final float TRACK_LEFT_LOCAL = -TRACK_WIDTH_LOCAL / 2;

  @Override
  public void adjustCanvas(
      @NonNull Canvas canvas,
      @NonNull ProgressIndicator progressIndicator,
      @FloatRange(from = 0.0, to = 1.0) float widthFraction) {
    // Gets clip bounds from canvas.
    Rect clipBounds = canvas.getClipBounds();

    // Positions canvas to center of the clip bounds.
    canvas.translate(
        clipBounds.width() / 2f,
        clipBounds.height() / 2f
            + Math.max(0f, (clipBounds.height() - progressIndicator.getIndicatorWidth()) / 2f));

    // Scales canvas to match the local coordinates to screen coordinates for the horizontal axis.
    canvas.scale(clipBounds.width() / TRACK_WIDTH_LOCAL, 1f);

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
    canvas.clipRect(
        TRACK_LEFT_LOCAL,
        -progressIndicator.getIndicatorWidth() / 2f,
        TRACK_LEFT_LOCAL + TRACK_WIDTH_LOCAL,
        progressIndicator.getIndicatorWidth() / 2f);
  }

  @Override
  public void fillTrackWithColor(
      @NonNull Canvas canvas,
      @NonNull Paint paint,
      @ColorInt int color,
      @FloatRange(from = 0.0, to = 1.0) float startFraction,
      @FloatRange(from = 0.0, to = 1.0) float endFraction,
      float trackWidth) {
    // Initializes Paint object.
    paint.setStyle(Style.FILL);
    paint.setAntiAlias(true);
    paint.setColor(color);
    canvas.drawRect(
        TRACK_LEFT_LOCAL + TRACK_WIDTH_LOCAL * startFraction,
        -trackWidth / 2,
        TRACK_LEFT_LOCAL + TRACK_WIDTH_LOCAL * endFraction,
        trackWidth / 2,
        paint);
  }
}
