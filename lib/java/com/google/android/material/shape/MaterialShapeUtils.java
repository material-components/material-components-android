/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.shape;

import android.graphics.drawable.Drawable;
import android.view.View;
import com.google.android.material.internal.ViewUtils;

/** Utility methods for {@link MaterialShapeDrawable} and related classes. */
public class MaterialShapeUtils {

  private MaterialShapeUtils() {}

  static CornerTreatment createCornerTreatment(@CornerFamily int cornerFamily, int cornerSize) {
    switch (cornerFamily) {
      case CornerFamily.ROUNDED:
        return new RoundedCornerTreatment(cornerSize);
      case CornerFamily.CUT:
        return new CutCornerTreatment(cornerSize);
      default:
        return createDefaultCornerTreatment();
    }
  }

  static CornerTreatment createDefaultCornerTreatment() {
    return new RoundedCornerTreatment(0);
  }

  static EdgeTreatment createDefaultEdgeTreatment() {
    return new EdgeTreatment();
  }

  /**
   * If the background of the provided {@code view} is a {@link MaterialShapeDrawable}, sets the
   * drawable's elevation via {@link MaterialShapeDrawable#setElevation(float)}; otherwise does
   * nothing.
   */
  public static void setElevation(View view, float elevation) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      ((MaterialShapeDrawable) background).setElevation(elevation);
    }
  }

  /**
   * If the background of the provided {@code view} is a {@link MaterialShapeDrawable}, sets the
   * drawable's parent absolute elevation (see {@link
   * MaterialShapeUtils#setParentAbsoluteElevation(View, MaterialShapeDrawable)}); otherwise does
   * nothing.
   */
  public static void setParentAbsoluteElevation(View view) {
    Drawable background = view.getBackground();
    if (background instanceof MaterialShapeDrawable) {
      setParentAbsoluteElevation(view, (MaterialShapeDrawable) background);
    }
  }

  /**
   * Updates the {@code materialShapeDrawable} parent absolute elevation via {@link
   * MaterialShapeDrawable#setParentAbsoluteElevation(float)} to be equal to the absolute elevation
   * of the parent of the provided {@code view}.
   */
  public static void setParentAbsoluteElevation(
      View view, MaterialShapeDrawable materialShapeDrawable) {
    if (materialShapeDrawable.isElevationOverlayEnabled()) {
      materialShapeDrawable.setParentAbsoluteElevation(ViewUtils.getParentAbsoluteElevation(view));
    }
  }
}
