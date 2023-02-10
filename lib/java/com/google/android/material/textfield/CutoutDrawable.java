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

import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
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
  @NonNull CutoutDrawableState drawableState;

  static CutoutDrawable create(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
    return create(new CutoutDrawableState(
        shapeAppearanceModel != null ? shapeAppearanceModel : new ShapeAppearanceModel(),
        new RectF()));
  }

  private static CutoutDrawable create(@NonNull CutoutDrawableState drawableState) {
    return VERSION.SDK_INT >= VERSION_CODES.JELLY_BEAN_MR2
        ? new ImplApi18(drawableState)
        : new ImplApi14(drawableState);
  }

  private CutoutDrawable(@NonNull CutoutDrawableState drawableState) {
    super(drawableState);
    this.drawableState = drawableState;
  }

  @NonNull
  @Override
  public Drawable mutate() {
    drawableState = new CutoutDrawableState(drawableState);
    return this;
  }

  boolean hasCutout() {
    return !drawableState.cutoutBounds.isEmpty();
  }

  void setCutout(float left, float top, float right, float bottom) {
    // Avoid expensive redraws by only calling invalidateSelf if one of the cutout's dimensions has
    // changed.
    if (left != drawableState.cutoutBounds.left
        || top != drawableState.cutoutBounds.top
        || right != drawableState.cutoutBounds.right
        || bottom != drawableState.cutoutBounds.bottom) {
      drawableState.cutoutBounds.set(left, top, right, bottom);
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

  @TargetApi(VERSION_CODES.JELLY_BEAN_MR2)
  private static class ImplApi18 extends CutoutDrawable {
    ImplApi18(@NonNull CutoutDrawableState drawableState) {
      super(drawableState);
    }

    @Override
    protected void drawStrokeShape(@NonNull Canvas canvas) {
      if (drawableState.cutoutBounds.isEmpty()) {
        super.drawStrokeShape(canvas);
      } else {
        // Saves the canvas so we can restore the clip after drawing the stroke.
        canvas.save();
        if (VERSION.SDK_INT >= VERSION_CODES.O) {
          canvas.clipOutRect(drawableState.cutoutBounds);
        } else {
          canvas.clipRect(drawableState.cutoutBounds, Op.DIFFERENCE);
        }
        super.drawStrokeShape(canvas);
        canvas.restore();
      }
    }
  }

  // Workaround: Canvas.clipRect() had a bug before API 18 - bound.left didn't work correctly
  //             with Region.Op.DIFFERENCE. "Paints out" the cutout area instead on lower APIs.
  private static class ImplApi14 extends CutoutDrawable {
    private Paint cutoutPaint;
    private int savedLayer;

    ImplApi14(@NonNull CutoutDrawableState drawableState) {
      super(drawableState);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
      preDraw(canvas);
      super.draw(canvas);
      postDraw(canvas);
    }

    @Override
    protected void drawStrokeShape(@NonNull Canvas canvas) {
      super.drawStrokeShape(canvas);
      canvas.drawRect(drawableState.cutoutBounds, getCutoutPaint());
    }

    private Paint getCutoutPaint() {
      if (cutoutPaint == null) {
        cutoutPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        cutoutPaint.setStyle(Style.FILL_AND_STROKE);
        cutoutPaint.setColor(Color.WHITE);
        cutoutPaint.setXfermode(new PorterDuffXfermode(Mode.DST_OUT));
      }
      return cutoutPaint;
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
            canvas.saveLayer(
                0, 0, canvas.getWidth(), canvas.getHeight(), null, Canvas.ALL_SAVE_FLAG);
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

  private static final class CutoutDrawableState extends MaterialShapeDrawableState {
    @NonNull private final RectF cutoutBounds;

    private CutoutDrawableState(
        @NonNull ShapeAppearanceModel shapeAppearanceModel, @NonNull RectF cutoutBounds) {
      super(shapeAppearanceModel, null);
      this.cutoutBounds = cutoutBounds;
    }

    private CutoutDrawableState(@NonNull CutoutDrawableState state) {
      super(state);
      this.cutoutBounds = state.cutoutBounds;
    }

    @NonNull
    @Override
    public Drawable newDrawable() {
      CutoutDrawable drawable = CutoutDrawable.create(this);
      // Force the calculation of the path for the new drawable.
      drawable.invalidateSelf();
      return drawable;
    }
  }
}
