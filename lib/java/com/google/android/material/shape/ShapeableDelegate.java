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

import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.canvas.CanvasCompat.CanvasOperation;

/**
 * A delegate able to handle logic for when and how to mask/clip a View based on the View's {@link
 * ShapeAppearanceModel} and mask bounds.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public abstract class ShapeableDelegate {

  boolean forceCompatClippingEnabled = false;
  boolean offsetZeroCornerEdgeBoundsEnabled = false;
  @Nullable ShapeAppearanceModel shapeAppearanceModel;
  RectF maskBounds = new RectF();
  final Path shapePath = new Path();

  @NonNull
  public static ShapeableDelegate create(@NonNull View view) {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      return new ShapeableDelegateV33(view);
    } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
      return new ShapeableDelegateV22(view);
    } else {
      return new ShapeableDelegateV14();
    }
  }

  /**
   * Called due to changes in a delegate's shape, mask bounds or other parameters. Delegate
   * implementations should use this as an opportunity to ensure their method of clipping is
   * appropriate and invalidate the client view if necessary.
   *
   * @param view the client view
   */
  abstract void invalidateClippingMethod(@NonNull View view);

  /**
   * Whether the client view should use canvas clipping to mask itself.
   *
   * <p>Note: It's important that no significant logic is run in this method as it is called from
   * dispatch draw, which should be as performant as possible. Logic for determining whether compat
   * clipping is used should be run elsewhere and stored for quick access.
   *
   * @return true if the client view should clip the canvas
   */
  abstract boolean shouldUseCompatClipping();

  public boolean isForceCompatClippingEnabled() {
    return forceCompatClippingEnabled;
  }

  /**
   * Set whether the client would like to always use compat clipping regardless of whether other
   * means are available.
   *
   * @param view the client view
   * @param enabled true if the client should always use canvas clipping
   */
  public void setForceCompatClippingEnabled(@NonNull View view, boolean enabled) {
    if (enabled != this.forceCompatClippingEnabled) {
      this.forceCompatClippingEnabled = enabled;
      invalidateClippingMethod(view);
    }
  }

  /**
   * Set whether the delegate should attempt to alter the mask bounds of shapes that are symmetrical
   * along a single axis in order to use ViewOutlineProvider for clipping on 22-32 where only fully
   * symmetrical shapes can be clipped.
   *
   * <p>This is primarily useful for clipping views that are "attached" to the edge of the screen
   * like navigation drawers and bottom sheets. The attached edge's outline bounds will be expanded
   * past the edge of the screen and the Outline the view is clipped to will not be seen, making it
   * look like the view has zeroed corners along the attached edge.
   *
   * <p>This method does not have any effect on API 14-21, before ViewOutlineProvider was
   * introduced, or on API 33+ where ViewOutlineProvider is able to clip to a path.
   *
   * @param view the client view
   * @param enabled true if the clients bounds should be altered along the zero-corner edge when
   *     possible
   */
  public void setOffsetZeroCornerEdgeBoundsEnabled(@NonNull View view, boolean enabled) {
    this.offsetZeroCornerEdgeBoundsEnabled = enabled;
    invalidateClippingMethod(view);
  }

  /**
   * Called whenever the {@link ShapeAppearanceModel} of the client changes.
   *
   * @param view the client view
   * @param shapeAppearanceModel the update {@link ShapeAppearanceModel}
   */
  public void onShapeAppearanceChanged(
      @NonNull View view, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel = shapeAppearanceModel;
    updateShapePath();
    invalidateClippingMethod(view);
  }

  /**
   * Called whenever the bounds of the clients mask changes.
   *
   * @param view the client view
   * @param maskBounds the updated bounds
   */
  public void onMaskChanged(@NonNull View view, @NonNull RectF maskBounds) {
    this.maskBounds = maskBounds;
    updateShapePath();
    invalidateClippingMethod(view);
  }

  private void updateShapePath() {
    if (isMaskBoundsValid() && shapeAppearanceModel != null) {
      ShapeAppearancePathProvider.getInstance()
          .calculatePath(shapeAppearanceModel, 1F, maskBounds, shapePath);
    }
  }

  private boolean isMaskBoundsValid() {
    return maskBounds.left <= maskBounds.right && maskBounds.top <= maskBounds.bottom;
  }

  public void maybeClip(@NonNull Canvas canvas, @NonNull CanvasOperation op) {
    if (shouldUseCompatClipping() && !shapePath.isEmpty()) {
      canvas.save();
      canvas.clipPath(shapePath);
      op.run(canvas);
      canvas.restore();
    } else {
      op.run(canvas);
    }
  }
}
