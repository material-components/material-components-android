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
import androidx.core.math.MathUtils;
import com.google.android.material.animation.AnimationUtils;
import com.google.android.material.shape.ShapeAppearanceModel;
import java.util.ArrayList;
import java.util.List;

/** A {@link FrameLayout} than is able to mask itself and all children. */
public class MaskableFrameLayout extends FrameLayout implements Maskable {

  private float maskXPercentage = 0F;
  private final RectF maskRect = new RectF();
  private final Path maskPath = new Path();

  private final List<OnMaskChangedListener> onMaskChangedListeners = new ArrayList<>();

  private final ShapeAppearanceModel shapeAppearanceModel;

  public MaskableFrameLayout(@NonNull Context context) {
    this(context, null);
  }

  public MaskableFrameLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public MaskableFrameLayout(
      @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    shapeAppearanceModel = ShapeAppearanceModel.builder(context, attrs, defStyleAttr, 0, 0).build();
    if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
      MaskableImplV21.initMaskOutlineProvider(this);
    }
  }

  @Override
  protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    super.onSizeChanged(w, h, oldw, oldh);
    onMaskChanged();
  }

  @Override
  public void setMaskXPercentage(float percentage) {
    percentage = MathUtils.clamp(percentage, 0F, 1F);
    if (maskXPercentage != percentage) {
      this.maskXPercentage = percentage;
      onMaskChanged();
    }
  }

  @Override
  public float getMaskXPercentage() {
    return maskXPercentage;
  }

  @NonNull
  @Override
  public RectF getMaskRect() {
    return maskRect;
  }

  @Override
  public void addOnMaskChangedListener(@NonNull OnMaskChangedListener listener) {
    onMaskChangedListeners.add(listener);
  }

  @Override
  public void removeOnMaskChangedListener(@NonNull OnMaskChangedListener listener) {
    onMaskChangedListeners.remove(listener);
  }

  private void onMaskChanged() {
    if (getWidth() == 0) {
      return;
    }
    // Translate the percentage into an actual pixel value of how much of this view should be
    // masked away.
    float maskWidth = AnimationUtils.lerp(0f, getWidth() / 2F, 0f, 1f, maskXPercentage);
    maskRect.set(maskWidth, 0F, (getWidth() - maskWidth), getHeight());
    for (OnMaskChangedListener listener : onMaskChangedListeners) {
      listener.onMaskChanged(maskRect);
    }
    refreshMaskPath();
  }

  private float getCornerRadiusFromShapeAppearance() {
    return shapeAppearanceModel.getTopRightCornerSize().getCornerSize(maskRect);
  }

  private void refreshMaskPath() {
    if (!maskRect.isEmpty()) {
      maskPath.rewind();
      float cornerRadius = getCornerRadiusFromShapeAppearance();
      maskPath.addRoundRect(maskRect, cornerRadius, cornerRadius, Path.Direction.CW);
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP) {
        invalidateOutline();
      }
      invalidate();
    }
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
    canvas.save();
    if (!maskPath.isEmpty()) {
      canvas.clipPath(maskPath);
    }
    super.dispatchDraw(canvas);
    canvas.restore();
  }

  @RequiresApi(VERSION_CODES.LOLLIPOP)
  private static class MaskableImplV21 {

    @DoNotInline
    private static void initMaskOutlineProvider(MaskableFrameLayout maskableFrameLayout) {
      maskableFrameLayout.setClipToOutline(true);
      maskableFrameLayout.setOutlineProvider(
          new ViewOutlineProvider() {
            @Override
            public void getOutline(View view, Outline outline) {
              RectF maskRect = ((MaskableFrameLayout) view).getMaskRect();
              float cornerSize = ((MaskableFrameLayout) view).getCornerRadiusFromShapeAppearance();
              if (!maskRect.isEmpty()) {
                outline.setRoundRect(
                    (int) maskRect.left,
                    (int) maskRect.top,
                    (int) maskRect.right,
                    (int) maskRect.bottom,
                    cornerSize);
              }
            }
          });
    }
  }
}
