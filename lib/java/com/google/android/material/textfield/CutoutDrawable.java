/*
 * Copyright (C) 2017 The Android Open Source Project
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

package com.google.android.material.textfield;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * A {@link MaterialShapeDrawable} that can draw a cutout for the label in {@link TextInputLayout}'s
 * outline mode.
 */
class CutoutDrawable extends MaterialShapeDrawable {
  @NonNull private final Paint cutoutPaint;
  @NonNull private final RectF cutoutBounds;

  CutoutDrawable() {
    this(null);
  }

  CutoutDrawable(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
    super(shapeAppearanceModel != null ? shapeAppearanceModel : new ShapeAppearanceModel());
    cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    setPaintStyles();
    cutoutBounds = new RectF();
  }

  private void setPaintStyles() {
    cutoutPaint.setStyle(Style.FILL_AND_STROKE);
    cutoutPaint.setColor(Color.WHITE);
    cutoutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
  }

  boolean hasCutout() {
    return !cutoutBounds.isEmpty();
  }

  void setCutout(float left, float top, float right, float bottom) {
    // Avoid expensive redraws by only calling invalidateSelf if one of the cutout's dimensions has
    // changed.
    if (left != cutoutBounds.left
        || top != cutoutBounds.top
        || right != cutoutBounds.right
        || bottom != cutoutBounds.bottom) {
      cutoutBounds.set(left, top, right, bottom);
      invalidateSelf();
    }
  }

  void setCutout(@NonNull RectF bounds) {
    setCutout(bounds.left, bounds.top, bounds.right, bounds.bottom);
  }

  void removeCutout() {
    // Call setCutout with empty bounds to remove the cutout.
    setCutout(0, 0, 0, 0);
  }

  @Override
  protected void drawStrokeShape(@NonNull Canvas canvas) {
    if (cutoutBounds.isEmpty()) {
      super.drawStrokeShape(canvas);
    } else {
      // Saves the canvas so we can restore the clip after drawing the stroke.
      canvas.save();
      if (VERSION.SDK_INT >= VERSION_CODES.O) {
        canvas.clipOutRect(cutoutBounds);
      } else {
        canvas.clipRect(cutoutBounds, Op.DIFFERENCE);
      }
      super.drawStrokeShape(canvas);
      canvas.restore();
    }
  }
}
