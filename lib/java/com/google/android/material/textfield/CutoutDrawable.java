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
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.drawable.Drawable;
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
  @NonNull CutoutDrawableState drawableState;

  static CutoutDrawable create(@Nullable ShapeAppearanceModel shapeAppearanceModel) {
    return create(new CutoutDrawableState(
        shapeAppearanceModel != null ? shapeAppearanceModel : new ShapeAppearanceModel(),
        new RectF()));
  }

  private static CutoutDrawable create(@NonNull CutoutDrawableState drawableState) {
    return new ImplApi18(drawableState);
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
