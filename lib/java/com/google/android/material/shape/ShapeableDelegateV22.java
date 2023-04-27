/*
 * Copyright 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.material.shape;

import android.graphics.Outline;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.view.View;
import android.view.ViewOutlineProvider;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.annotation.VisibleForTesting;

/**
 * A {@link ShapeableDelegate} for API 22-32 that uses {@link ViewOutlineProvider} to clip when the
 * shape being clipped is a round rect with symmetrical corners and canvas clipping for all other
 * shapes.
 *
 * <p>{@link Outline#setRoundRect(Rect, float)} is only able to clip to a rectangle with a single
 * corner radius for all four corners.
 */
@RequiresApi(VERSION_CODES.LOLLIPOP_MR1)
class ShapeableDelegateV22 extends ShapeableDelegate {

  private boolean canUseViewOutline = false;
  private float cornerRadius = 0F;

  ShapeableDelegateV22(@NonNull View view) {
    initMaskOutlineProvider(view);
  }

  @Override
  boolean shouldUseCompatClipping() {
    return !canUseViewOutline || forceCompatClippingEnabled;
  }

  @Override
  void invalidateClippingMethod(@NonNull View view) {
    cornerRadius = getDefaultCornerRadius();
    canUseViewOutline = isShapeRoundRect() || offsetZeroCornerEdgeBoundsIfPossible();
    view.setClipToOutline(!shouldUseCompatClipping());
    if (shouldUseCompatClipping()) {
      view.invalidate();
    } else {
      view.invalidateOutline();
    }
  }

  private float getDefaultCornerRadius() {
    if (shapeAppearanceModel == null || maskBounds == null) {
      return 0F;
    }
    return shapeAppearanceModel.topRightCornerSize.getCornerSize(maskBounds);
  }

  private boolean isShapeRoundRect() {
    if (maskBounds.isEmpty() || shapeAppearanceModel == null) {
      return false;
    }

    return shapeAppearanceModel.isRoundRect(maskBounds);
  }

  /**
   * Offsets the mask bounds for an edge with zeroed corners whose opposing corners share a corner
   * size (a symmetrical shape along a single axis).
   *
   * <p>Extending the bounds allows this delegate to use a symmetrical shape with
   * ViewOutlineProvider to clip the since extended edges's corners will cause the corners to be
   * outside the view's bounds and the view will look like it has a corner size of zero for the
   * extended edge.
   *
   * <p>This method also updates {@code cornerRadius} to use the radius opposite the zero corner
   * edge.
   *
   * @return true if the bounds were offset, the corner radius was updated, and a ViewOutline can be
   *     used for clipping. false if the shape wasn't suitable for offsetting and compat clipping
   *     should be used instead
   */
  private boolean offsetZeroCornerEdgeBoundsIfPossible() {
    if (maskBounds.isEmpty()
        || shapeAppearanceModel == null
        || !offsetZeroCornerEdgeBoundsEnabled
        || shapeAppearanceModel.isRoundRect(maskBounds)
        || !shapeUsesAllRoundedCornerTreatments(shapeAppearanceModel)) {
      return false;
    }
    // When a rounded shape has an edge with zeroed corners whose opposing corners share a
    // corner size (symmetrical along a single axis), the mask bounds can be extended along the
    // zero corner edge and a ViewOutlineProvider can still be used to clip the view.
    float topLeft = shapeAppearanceModel.getTopLeftCornerSize().getCornerSize(maskBounds);
    float topRight = shapeAppearanceModel.getTopRightCornerSize().getCornerSize(maskBounds);
    float bottomLeft = shapeAppearanceModel.getBottomLeftCornerSize().getCornerSize(maskBounds);
    float bottomRight = shapeAppearanceModel.getBottomRightCornerSize().getCornerSize(maskBounds);

    if (topLeft == 0F && bottomLeft == 0F && topRight == bottomRight) {
      // Extend the left edge
      maskBounds.set(
          maskBounds.left - topRight, maskBounds.top, maskBounds.right, maskBounds.bottom);
      cornerRadius = topRight;
    } else if (topLeft == 0F && topRight == 0F && bottomLeft == bottomRight) {
      // Extend the top edge
      maskBounds.set(
          maskBounds.left, maskBounds.top - bottomLeft, maskBounds.right, maskBounds.bottom);
      cornerRadius = bottomLeft;
    } else if (topRight == 0F && bottomRight == 0F && topLeft == bottomLeft) {
      // Extend the right edge
      maskBounds.set(
          maskBounds.left, maskBounds.top, maskBounds.right + topLeft, maskBounds.bottom);
      cornerRadius = topLeft;
    } else if (bottomLeft == 0F && bottomRight == 0F && topLeft == topRight) {
      // Extend the bottom edge
      maskBounds.set(
          maskBounds.left, maskBounds.top, maskBounds.right, maskBounds.bottom + topLeft);
      cornerRadius = topLeft;
    } else {
      // This shape is not symmetrical along any axis and a ViewOutlineProvider cannot be used.
      return false;
    }

    return true;
  }

  @VisibleForTesting
  float getCornerRadius() {
    return cornerRadius;
  }

  private static boolean shapeUsesAllRoundedCornerTreatments(ShapeAppearanceModel model) {
    return model.getTopLeftCorner() instanceof RoundedCornerTreatment
        && model.getTopRightCorner() instanceof RoundedCornerTreatment
        && model.getBottomLeftCorner() instanceof RoundedCornerTreatment
        && model.getBottomRightCorner() instanceof RoundedCornerTreatment;
  }

  @DoNotInline
  private void initMaskOutlineProvider(View view) {
    view.setOutlineProvider(
        new ViewOutlineProvider() {
          @Override
          public void getOutline(View view, Outline outline) {
            if (shapeAppearanceModel != null && !maskBounds.isEmpty()) {
              outline.setRoundRect(
                  (int) maskBounds.left,
                  (int) maskBounds.top,
                  (int) maskBounds.right,
                  (int) maskBounds.bottom,
                  cornerRadius);
            }
          }
        });
  }
}
