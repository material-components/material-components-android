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
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
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
  private int savedLayer;

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
  public void draw(@NonNull Canvas canvas) {
    preDraw(canvas);
    super.draw(canvas);

    // Draw mask for the cutout.
    canvas.drawRect(cutoutBounds, cutoutPaint);

    postDraw(canvas);
  }

  private void preDraw(@NonNull Canvas canvas) {
    Callback callback = getCallback();

    if (useHardwareLayer(callback)) {
      View viewCallback = (View) callback;
      // Make sure we're using a hardware layer.
      if (viewCallback.getLayerType() != View.LAYER_TYPE_HARDWARE) {
        viewCallback.setLayerType(View.LAYER_TYPE_HARDWARE, null);
      }
    } else {
      // If we're not using a hardware layer, save the canvas layer.
      saveCanvasLayer(canvas);
    }
  }

  private void saveCanvasLayer(@NonNull Canvas canvas) {
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      savedLayer = canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null);
    } else {
      savedLayer =
          canvas.saveLayer(0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
    }
  }

  private void postDraw(@NonNull Canvas canvas) {
    if (!useHardwareLayer(getCallback())) {
      canvas.restoreToCount(savedLayer);
    }
  }

  private boolean useHardwareLayer(Callback callback) {
    return callback instanceof View;
  }
}
