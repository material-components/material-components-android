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
  private float cornerRadius;

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

  public void resetClipBoundsAndCornerRadius() {
    path = null;
    cornerRadius = 0f;
    invalidate();
  }

  public float getCornerRadius() {
    return cornerRadius;
  }

  public void updateCornerRadius(float cornerRadius) {
    updateClipBoundsAndCornerRadius(getLeft(), getTop(), getRight(), getBottom(), cornerRadius);
  }

  public void updateClipBoundsAndCornerRadius(@NonNull Rect rect, float cornerRadius) {
    updateClipBoundsAndCornerRadius(rect.left, rect.top, rect.right, rect.bottom, cornerRadius);
  }

  public void updateClipBoundsAndCornerRadius(
      float left, float top, float right, float bottom, float cornerRadius) {
    updateClipBoundsAndCornerRadius(new RectF(left, top, right, bottom), cornerRadius);
  }

  public void updateClipBoundsAndCornerRadius(@NonNull RectF rectF, float cornerRadius) {
    if (path == null) {
      path = new Path();
    }
    this.cornerRadius = cornerRadius;
    path.reset();
    path.addRoundRect(rectF, cornerRadius, cornerRadius, Path.Direction.CW);
    path.close();
    invalidate();
  }
}
