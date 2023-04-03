/*
 * Copyright 2022 The Android Open Source Project
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

package com.google.android.material.carousel;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Outline;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewOutlineProvider;
import android.widget.FrameLayout;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.math.MathUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.canvas.CanvasCompat.CanvasOperation;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.ClampedCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.ShapeAppearancePathProvider;
import com.google.android.material.shape.Shapeable;

/** A {@link FrameLayout} than is able to mask itself and all children. */
public class MaskableFrameLayout extends FrameLayout implements Maskable, Shapeable {

  private float maskXPercentage = 0F;
  private final RectF maskRect = new RectF();
  @Nullable private OnMaskChangedListener onMaskChangedListener;
  @NonNull private ShapeAppearanceModel shapeAppearanceModel;
  private final MaskableDelegate maskableDelegate = createMaskableDelegate();
  @Nullable private Boolean savedForceCompatClippingEnabled = null;

  public MaskableFrameLayout(@NonNull Context context) {
    this(context, null);
  }

  public MaskableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaskableFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    setShapeAppearanceModel(
        ShapeAppearanceModel.builder(context, attrs, defStyleAttr, 0, 0).build());
  }

  private MaskableDelegate createMaskableDelegate() {
    if (VERSION.SDK_INT >= VERSION_CODES.TIRAMISU) {
      return new MaskableDelegateV33(this);
    } else if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1) {
      return new MaskableDelegateV22(this);
    } else {
      return new MaskableDelegateV14();
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    onMaskChanged();
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // Restore any saved force compat clipping setting.
    if (savedForceCompatClippingEnabled != null) {
      maskableDelegate.setForceCompatClippingEnabled(this, savedForceCompatClippingEnabled);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // When detaching from the window, force canvas clipping to avoid any transitions from releasing
    // the mask outline set by the MaskableDelegate's ViewOutlineProvider, if any.
    savedForceCompatClippingEnabled = maskableDelegate.isForceCompatClippingEnabled();
    maskableDelegate.setForceCompatClippingEnabled(this, true);
    super.onDetachedFromWindow();
  }

  @Override
  public void setShapeAppearanceModel(@NonNull ShapeAppearanceModel shapeAppearanceModel) {
    this.shapeAppearanceModel =
        shapeAppearanceModel.withTransformedCornerSizes(
            cornerSize -> {
              if (cornerSize instanceof AbsoluteCornerSize) {
                // Enforce that the corners of the shape appearance are never larger than half the
                // width of the shortest edge. As the size of the mask changes, we never want the
                // corners to be larger than half the width or height of this view.
                return ClampedCornerSize.createFromCornerSize((AbsoluteCornerSize) cornerSize);
              } else {
                // Relative corner size already enforces a max size based on shortest edge.
                return cornerSize;
              }
            });
    maskableDelegate.onShapeAppearanceChanged(this, this.shapeAppearanceModel);
  }

  @NonNull
  @Override
  public ShapeAppearanceModel getShapeAppearanceModel() {
    return shapeAppearanceModel;
  }

  /**
   * Sets the percentage by which this {@link View} masks by along the x axis.
   *
   * @param percentage 0 when this view is fully unmasked. 1 when this view is fully masked.
   */
  @Override
  public void setMaskXPercentage(float percentage) {
    percentage = MathUtils.clamp(percentage, 0F, 1F);
    if (maskXPercentage != percentage) {
      this.maskXPercentage = percentage;
      onMaskChanged();
    }
  }

  /**
   * Gets the percentage by which this {@link View} is masked by along the x axis.
   *
   * @return a float between 0 and 1 where 0 is fully unmasked and 1 is fully masked.
   */
  @Override
  public float getMaskXPercentage() {
    return maskXPercentage;
  }

  /** Gets a {@link RectF} that this {@link View} is masked itself by. */
  @NonNull
  @Override
  public RectF getMaskRectF() {
    return maskRect;
  }

  @Override
  public void setOnMaskChangedListener(@Nullable OnMaskChangedListener onMaskChangedListener) {
    this.onMaskChangedListener = onMaskChangedListener;
  }

  private void onMaskChanged() {
    if (getWidth() == 0) {
      return;
    }
    // Translate the percentage into an actual pixel value of how much of this view should be
    // masked away.
    float maskWidth = AnimationUtils.lerp(0f, getWidth() / 2F, 0f, 1f, maskXPercentage);
    maskRect.set(maskWidth, 0F, (getWidth() - maskWidth), getHeight());
    maskableDelegate.onMaskChanged(this, maskRect);
    if (onMaskChangedListener != null) {
      onMaskChangedListener.onMaskChanged(maskRect);
    }
  }

  /**
   * Set whether this view should always use canvas clipping to clip to its masked shape.
   *
   * @hide
   */
  @VisibleForTesting
  @RestrictTo(Scope.LIBRARY_GROUP)
  public void setForceCompatClipping(boolean forceCompatClipping) {
    maskableDelegate.setForceCompatClippingEnabled(this, forceCompatClipping);
  }

  @SuppressLint("ClickableViewAccessibility")
  @Override
  public boolean onTouchEvent(MotionEvent event) {
    // Only handle touch events that are within the masked bounds of this view.
    if (!maskRect.isEmpty() && event.getAction() == MotionEvent.ACTION_DOWN) {
      float x = event.getX();
      float y = event.getY();
      if (!maskRect.contains(x, y)) {
        return false;
      }
    }
    return super.onTouchEvent(event);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    maskableDelegate.maybeClip(canvas, super::dispatchDraw);
  }

  /**
   * A delegate able to handle logic for when and how to mask a View based on the View's {@link
   * ShapeAppearanceModel} and mask bounds.
   */
  private abstract static class MaskableDelegate {

    boolean forceCompatClippingEnabled = false;
    @Nullable ShapeAppearanceModel shapeAppearanceModel;
    RectF maskBounds = new RectF();
    final Path shapePath = new Path();

    /**
     * Called due to changes in a delegate's shape, mask bounds or other parameters. Delegate
     * implementations should use this as an opportunity to ensure their method of clipping is
     * appropriate and invalidate the client view if necessary.
     *
     * @param view the client view
     */
    abstract void invalidateClippingMethod(View view);

    /**
     * Whether the client view should use canvas clipping to mask itself.
     *
     * <p>Note: It's important that no significant logic is run in this method as it is called from
     * dispatch draw, which should be as performant as possible. Logic for determining whether
     * compat clipping is used should be run elsewhere and stored for quick access.
     *
     * @return true if the client view should clip the canvas
     */
    abstract boolean shouldUseCompatClipping();

    boolean isForceCompatClippingEnabled() {
      return forceCompatClippingEnabled;
    }

    /**
     * Set whether the client would like to always use compat clipping regardless of whether other
     * means are available.
     *
     * @param view the client view
     * @param enabled true if the client should always use canvas clipping
     */
    void setForceCompatClippingEnabled(View view, boolean enabled) {
      if (enabled != this.forceCompatClippingEnabled) {
        this.forceCompatClippingEnabled = enabled;
        invalidateClippingMethod(view);
      }
    }

    /**
     * Called whenever the {@link ShapeAppearanceModel} of the client changes.
     *
     * @param view the client view
     * @param shapeAppearanceModel the update {@link ShapeAppearanceModel}
     */
    void onShapeAppearanceChanged(View view, @NonNull ShapeAppearanceModel shapeAppearanceModel) {
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
    void onMaskChanged(View view, RectF maskBounds) {
      this.maskBounds = maskBounds;
      updateShapePath();
      invalidateClippingMethod(view);
    }

    private void updateShapePath() {
      if (!maskBounds.isEmpty() && shapeAppearanceModel != null) {
        ShapeAppearancePathProvider.getInstance()
            .calculatePath(shapeAppearanceModel, 1F, maskBounds, shapePath);
      }
    }

    void maybeClip(Canvas canvas, CanvasOperation op) {
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

  /**
   * A {@link MaskableDelegate} implementation for API 14-21 that always clips using canvas
   * clipping.
   */
  private static class MaskableDelegateV14 extends MaskableDelegate {

    @Override
    boolean shouldUseCompatClipping() {
      return true;
    }

    @Override
    void invalidateClippingMethod(View view) {
      if (shapeAppearanceModel == null || maskBounds.isEmpty()) {
        return;
      }

      if (shouldUseCompatClipping()) {
        view.invalidate();
      }
    }
  }

  /**
   * A {@link MaskableDelegate} for API 22-32 that uses {@link ViewOutlineProvider} to clip when the
   * shape being clipped is a round rect with symmetrical corners and canvas clipping for all other
   * shapes. This way is not used for API 21 because outline invalidation is incorrectly implemented
   * in this version.
   *
   * <p>{@link Outline#setRoundRect(Rect, float)} is only able to clip to a rectangle with a single
   * corner radius for all four corners.
   */
  @RequiresApi(VERSION_CODES.LOLLIPOP_MR1)
  private static class MaskableDelegateV22 extends MaskableDelegate {

    private boolean isShapeRoundRect = false;

    MaskableDelegateV22(View view) {
      initMaskOutlineProvider(view);
    }

    @Override
    public boolean shouldUseCompatClipping() {
      return !isShapeRoundRect || forceCompatClippingEnabled;
    }

    @Override
    void invalidateClippingMethod(View view) {
      updateIsShapeRoundRect();
      view.setClipToOutline(!shouldUseCompatClipping());
      if (shouldUseCompatClipping()) {
        view.invalidate();
      } else {
        view.invalidateOutline();
      }
    }

    private void updateIsShapeRoundRect() {
      if (!maskBounds.isEmpty() && shapeAppearanceModel != null) {
        isShapeRoundRect = shapeAppearanceModel.isRoundRect(maskBounds);
      }
    }

    private float getCornerRadiusFromShapeAppearance(
        @NonNull ShapeAppearanceModel shapeAppearanceModel, @NonNull RectF bounds) {
      return shapeAppearanceModel.getTopRightCornerSize().getCornerSize(bounds);
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
                    getCornerRadiusFromShapeAppearance(shapeAppearanceModel, maskBounds));
              }
            }
          });
    }
  }

  /**
   * A {@link MaskableDelegate} for API 33+ that uses {@link ViewOutlineProvider} to clip for all
   * shapes.
   *
   * <p>{@link Outline#setPath(Path)} was added in API 33 and allows using {@link
   * ViewOutlineProvider} to clip for all shapes.
   */
  @RequiresApi(VERSION_CODES.TIRAMISU)
  private static class MaskableDelegateV33 extends MaskableDelegate {

    MaskableDelegateV33(View view) {
      initMaskOutlineProvider(view);
    }

    @Override
    public boolean shouldUseCompatClipping() {
      return forceCompatClippingEnabled;
    }

    @Override
    void invalidateClippingMethod(View view) {
      view.setClipToOutline(!shouldUseCompatClipping());
      if (shouldUseCompatClipping()) {
        view.invalidate();
      } else {
        view.invalidateOutline();
      }
    }

    @DoNotInline
    private void initMaskOutlineProvider(View view) {
      view.setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              if (!shapePath.isEmpty()) {
                outline.setPath(shapePath);
              }
            }
          });
    }
  }
}
