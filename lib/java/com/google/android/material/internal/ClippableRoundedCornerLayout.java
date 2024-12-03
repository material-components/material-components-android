/*
 * Copyright (C) 2022 The Android Open Source Project
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

package com.google.android.material.internal;

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;

/**
 * Wrapper layout for rendering and animating rounded corners with clip bounds.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class ClippableRoundedCornerLayout extends FrameLayout {

  @Nullable private Path path;
  @NonNull private float[] cornerRadii = new float[] {0, 0, 0, 0, 0, 0, 0, 0};

  public ClippableRoundedCornerLayout(@NonNull Context context) {
    super(context);
  }

  public ClippableRoundedCornerLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
  }

  public ClippableRoundedCornerLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    if (path == null) {
      super.dispatchDraw(canvas);
      return;
    }
    int save = canvas.save();
    canvas.clipPath(path);
    super.dispatchDraw(canvas);
    canvas.restoreToCount(save);
  }

  public void resetClipBoundsAndCornerRadii() {
    path = null;
    cornerRadii = new float[] {0, 0, 0, 0, 0, 0, 0, 0};
    invalidate();
  }

  @NonNull
  public float[] getCornerRadii() {
    return cornerRadii;
  }

  public void updateCornerRadii(@NonNull float[] cornerRadii) {
    updateClipBoundsAndCornerRadii(getLeft(), getTop(), getRight(), getBottom(), cornerRadii);
  }

  public void updateClipBoundsAndCornerRadii(@NonNull Rect rect, @NonNull float[] cornerRadii) {
    updateClipBoundsAndCornerRadii(rect.left, rect.top, rect.right, rect.bottom, cornerRadii);
  }

  public void updateClipBoundsAndCornerRadii(
      float left, float top, float right, float bottom, @NonNull float[] cornerRadii) {
    updateClipBoundsAndCornerRadii(new RectF(left, top, right, bottom), cornerRadii);
  }

  public void updateClipBoundsAndCornerRadii(@NonNull RectF rectF, @NonNull float[] cornerRadii) {
    if (path == null) {
      path = new Path();
    }
    this.cornerRadii = cornerRadii;
    path.reset();
    path.addRoundRect(rectF, cornerRadii, Path.Direction.CW);
    path.close();
    invalidate();
  }
}
