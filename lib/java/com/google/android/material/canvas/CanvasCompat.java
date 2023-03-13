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

package com.google.android.material.canvas;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Compat methods for Canvas.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class CanvasCompat {

  private CanvasCompat() {}

  /**
   * Convenience for {@link Canvas#saveLayer(RectF, Paint)} but instead of taking a entire Paint
   * object it takes only the {@code alpha} parameter.
   */
  public static int saveLayerAlpha(@NonNull Canvas canvas, @Nullable RectF bounds, int alpha) {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      return canvas.saveLayerAlpha(bounds, alpha);
    } else {
      return canvas.saveLayerAlpha(bounds, alpha, Canvas.ALL_SAVE_FLAG);
    }
  }

  /**
   * Convenience for {@link #saveLayerAlpha(Canvas, RectF, int)} that takes the four float
   * coordinates of the bounds rectangle.
   */
  public static int saveLayerAlpha(
      @NonNull Canvas canvas, float left, float top, float right, float bottom, int alpha) {
    if (VERSION.SDK_INT > VERSION_CODES.LOLLIPOP) {
      return canvas.saveLayerAlpha(left, top, right, bottom, alpha);
    } else {
      return canvas.saveLayerAlpha(left, top, right, bottom, alpha, Canvas.ALL_SAVE_FLAG);
    }
  }

  /**
   * Helper interface to allow delegates to alter the canvas before and after a canvas operation.
   */
  public interface CanvasOperation {
    void run(@NonNull Canvas canvas);
  }

}
