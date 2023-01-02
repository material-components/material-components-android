/*
 * Copyright (C) 2015 The Android Open Source Project
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

import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.RectF;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import androidx.annotation.NonNull;
import androidx.annotation.RestrictTo;

/**
 * Utility class for descendant {@link Rect} calculations.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class DescendantOffsetUtils {
  private static final ThreadLocal<Matrix> matrix = new ThreadLocal<>();
  private static final ThreadLocal<RectF> rectF = new ThreadLocal<>();

  /**
   * This is a port of the common {@link ViewGroup#offsetDescendantRectToMyCoords(View, Rect)} from
   * the framework, but adapted to take transformations into account. The result will be the
   * bounding rect of the real transformed rect.
   *
   * @param descendant view defining the original coordinate system of rect
   * @param rect (in/out) the rect to offset from descendant to this view's coordinate system
   */
  public static void offsetDescendantRect(
      @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect rect) {
    Matrix m = matrix.get();
    if (m == null) {
      m = new Matrix();
      matrix.set(m);
    } else {
      m.reset();
    }

    offsetDescendantMatrix(parent, descendant, m);

    RectF rectF = DescendantOffsetUtils.rectF.get();
    if (rectF == null) {
      rectF = new RectF();
      DescendantOffsetUtils.rectF.set(rectF);
    }
    rectF.set(rect);
    m.mapRect(rectF);
    rect.set(
        (int) (rectF.left + 0.5f),
        (int) (rectF.top + 0.5f),
        (int) (rectF.right + 0.5f),
        (int) (rectF.bottom + 0.5f));
  }

  /**
   * Retrieve the transformed bounding rect of an arbitrary descendant view. This does not need to
   * be a direct child.
   *
   * @param descendant descendant view to reference
   * @param out rect to set to the bounds of the descendant view
   */
  public static void getDescendantRect(
      @NonNull ViewGroup parent, @NonNull View descendant, @NonNull Rect out) {
    out.set(0, 0, descendant.getWidth(), descendant.getHeight());
    offsetDescendantRect(parent, descendant, out);
  }

  private static void offsetDescendantMatrix(
      ViewParent target, @NonNull View view, @NonNull Matrix m) {
    final ViewParent parent = view.getParent();
    if (parent instanceof View && parent != target) {
      final View vp = (View) parent;
      offsetDescendantMatrix(target, vp, m);
      m.preTranslate(-vp.getScrollX(), -vp.getScrollY());
    }

    m.preTranslate(view.getLeft(), view.getTop());

    if (!view.getMatrix().isIdentity()) {
      m.preConcat(view.getMatrix());
    }
  }
}
