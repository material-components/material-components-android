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
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.math.MathUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.shape.AbsoluteCornerSize;
import com.google.android.material.shape.ClampedCornerSize;
import com.google.android.material.shape.ShapeAppearanceModel;
import com.google.android.material.shape.Shapeable;
import com.google.android.material.shape.ShapeableDelegate;

/** A {@link FrameLayout} than is able to mask itself and all children. */
public class MaskableFrameLayout extends FrameLayout implements Maskable, Shapeable {

  private static final int NOT_SET = -1;

  private float maskXPercentage = NOT_SET;
  private final RectF maskRect = new RectF();
  private final Rect screenBoundsRect = new Rect();
  @Nullable private OnMaskChangedListener onMaskChangedListener;
  @NonNull private ShapeAppearanceModel shapeAppearanceModel;
  private final ShapeableDelegate shapeableDelegate = ShapeableDelegate.create(this);
  @Nullable private Boolean savedForceCompatClippingEnabled = null;
  @Nullable private OnHoverListener hoverListener;

  private boolean isHovered = false;

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

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    if (maskXPercentage != NOT_SET) {
      updateMaskRectForMaskXPercentage();
    }
  }

  @Override
  public void getFocusedRect(Rect r) {
    r.set((int) maskRect.left, (int) maskRect.top, (int) maskRect.right, (int) maskRect.bottom);
  }

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    // Restore any saved force compat clipping setting.
    if (savedForceCompatClippingEnabled != null) {
      shapeableDelegate.setForceCompatClippingEnabled(this, savedForceCompatClippingEnabled);
    }
  }

  @Override
  protected void onDetachedFromWindow() {
    // When detaching from the window, force canvas clipping to avoid any transitions from releasing
    // the mask outline set by the MaskableDelegate's ViewOutlineProvider, if any.
    savedForceCompatClippingEnabled = shapeableDelegate.isForceCompatClippingEnabled();
    shapeableDelegate.setForceCompatClippingEnabled(this, true);
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
    shapeableDelegate.onShapeAppearanceChanged(this, this.shapeAppearanceModel);
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
   * @deprecated This is no longer used as {@link CarouselLayoutManager} calculates its own mask
   *     percentages.
   */
  @Override
  @Deprecated
  public void setMaskXPercentage(float percentage) {
    percentage = MathUtils.clamp(percentage, 0F, 1F);
    if (maskXPercentage != percentage) {
      this.maskXPercentage = percentage;
      updateMaskRectForMaskXPercentage();
    }
  }

  private void updateMaskRectForMaskXPercentage() {
    if (maskXPercentage != NOT_SET) {
      // Translate the percentage into an actual pixel value of how much of this view should be
      // masked away.
      float maskWidth = AnimationUtils.lerp(0f, getWidth() / 2F, 0f, 1f, maskXPercentage);
      setMaskRectF(new RectF(maskWidth, 0F, (getWidth() - maskWidth), getHeight()));
    }
  }

  /**
   * Sets the {@link RectF} that this {@link View} will be masked by.
   *
   * @param maskRect a rect in the view's coordinates to mask by
   */
  @Override
  public void setMaskRectF(@NonNull RectF maskRect) {
    this.maskRect.set(maskRect);
    onMaskChanged();
  }

  /**
   * Gets the percentage by which this {@link View} is masked by along the x axis.
   *
   * @return a float between 0 and 1 where 0 is fully unmasked and 1 is fully masked.
   * @deprecated This is no longer used as {@link CarouselLayoutManager} calculates its own mask
   *     percentages.
   */
  @Override
  @Deprecated
  public float getMaskXPercentage() {
    return maskXPercentage;
  }

  /** Gets a {@link RectF} that this {@link View} is masked itself by. */
  @NonNull
  @Override
  public RectF getMaskRectF() {
    return maskRect;
  }

  /**
   * Sets an {@link OnMaskChangedListener}.
   *
   * @param onMaskChangedListener a listener to receive callbacks for changes in the mask or null
   *    to clear the listener.
   */
  @Override
  public void setOnMaskChangedListener(@Nullable OnMaskChangedListener onMaskChangedListener) {
    this.onMaskChangedListener = onMaskChangedListener;
  }

  private void onMaskChanged() {
    shapeableDelegate.onMaskChanged(this, maskRect);
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
    shapeableDelegate.setForceCompatClippingEnabled(this, forceCompatClipping);
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
  public boolean onInterceptTouchEvent(MotionEvent event) {
    // Intercept touch events outside the masked bounds and prevent them from
    // reaching the children.
    if (!maskRect.isEmpty()) {
      float x = event.getX();
      float y = event.getY();
      if (!maskRect.contains(x, y)) {
        return true; // Intercept touch events outside the mask
      }
    }
    return super.onInterceptTouchEvent(event);
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    shapeableDelegate.maybeClip(canvas, super::dispatchDraw);
  }

  @Override
  public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfo info) {
    super.onInitializeAccessibilityNodeInfo(info);
    // Note: This is a workaround until b/273752775 is resolved. A11y bounds should be set by the
    // a11y framework.
    info.getBoundsInScreen(screenBoundsRect);

    // If the child starts from a negative x, the screen bounds are already cut off at the
    // parent so there is no need to reduce the screen bound's left bound. Similarly for negative y.
    if (getX() > 0) {
      screenBoundsRect.left = (int) (screenBoundsRect.left + maskRect.left);
    }
    if (getY() > 0) {
      screenBoundsRect.top = (int) (screenBoundsRect.top + maskRect.top);
    }
    screenBoundsRect.right = screenBoundsRect.left + Math.round(maskRect.width());
    screenBoundsRect.bottom = screenBoundsRect.top + Math.round(maskRect.height());

    info.setBoundsInScreen(screenBoundsRect);
  }

  @Override
  public void setOnHoverListener(@Nullable OnHoverListener l) {
    hoverListener = l;
  }

  @Override
  public boolean onHoverEvent(MotionEvent event) {
    // Only handle hover events that are within the masked bounds of this view.
    int action = event.getAction();
    if (!maskRect.isEmpty()
        && (action == MotionEvent.ACTION_HOVER_ENTER
            || action == MotionEvent.ACTION_HOVER_EXIT
            || action== MotionEvent.ACTION_HOVER_MOVE)) {
      float x = event.getX();
      float y = event.getY();
      if (!maskRect.contains(x, y)) {
        if (isHovered && hoverListener != null) {
          event.setAction(MotionEvent.ACTION_HOVER_EXIT);
          hoverListener.onHover(this, event);
        }
        isHovered = false;
        return false;
      }
    }
    if (hoverListener != null) {
      // If the MaskableFrameLayout is currently not hovered and the action is a move, it
      // should be changed to an enter action
      if (!isHovered && action == MotionEvent.ACTION_HOVER_MOVE) {
        event.setAction(MotionEvent.ACTION_HOVER_ENTER);
        isHovered = true;
      }
      if (action == MotionEvent.ACTION_HOVER_MOVE
          || action == MotionEvent.ACTION_HOVER_ENTER) {
        isHovered = true;
      }
      hoverListener.onHover(this, event);
    }
    return super.onHoverEvent(event);
  }
}
