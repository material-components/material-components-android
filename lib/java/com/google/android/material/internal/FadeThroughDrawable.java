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
import static java.lang.Math.max;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;
import android.util.LayoutDirection;
import androidx.annotation.FloatRange;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.core.graphics.drawable.DrawableCompat;

/**
 * Facilitates a fade out and then a fade in of the two input drawables.
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class FadeThroughDrawable extends Drawable {

  private final Drawable fadeOutDrawable;
  private final Drawable fadeInDrawable;
  private final float[] alphas;

  private float progress;

  public FadeThroughDrawable(@Nullable Drawable fadeOutDrawable, @Nullable Drawable fadeInDrawable) {
    this.fadeOutDrawable =
        fadeOutDrawable != null
            ? fadeOutDrawable.getConstantState().newDrawable().mutate()
            : new EmptyDrawable();
    this.fadeInDrawable =
        fadeInDrawable != null
            ? fadeInDrawable.getConstantState().newDrawable().mutate()
            : new EmptyDrawable();
    int outLayoutDir =
        fadeOutDrawable != null
            ? DrawableCompat.getLayoutDirection(fadeOutDrawable)
            : LayoutDirection.LOCALE;
    int inLayoutDir =
        fadeInDrawable != null
            ? DrawableCompat.getLayoutDirection(fadeInDrawable)
            : LayoutDirection.LOCALE;
    DrawableCompat.setLayoutDirection(this.fadeOutDrawable, outLayoutDir);
    DrawableCompat.setLayoutDirection(this.fadeInDrawable, inLayoutDir);
    this.fadeInDrawable.setAlpha(0);
    this.alphas = new float[2];
  }

  @Override
  public void draw(@NonNull Canvas canvas) {
    fadeOutDrawable.draw(canvas);
    fadeInDrawable.draw(canvas);
  }

  @Override
  public void setBounds(int left, int top, int right, int bottom) {
    super.setBounds(left, top, right, bottom);
    fadeOutDrawable.setBounds(left, top, right, bottom);
    fadeInDrawable.setBounds(left, top, right, bottom);
  }

  @Override
  public int getIntrinsicWidth() {
    return max(fadeOutDrawable.getIntrinsicWidth(), fadeInDrawable.getIntrinsicWidth());
  }

  @Override
  public int getIntrinsicHeight() {
    return max(fadeOutDrawable.getIntrinsicHeight(), fadeInDrawable.getIntrinsicHeight());
  }

  @Override
  public int getMinimumWidth() {
    return max(fadeOutDrawable.getMinimumWidth(), fadeInDrawable.getMinimumWidth());
  }

  @Override
  public int getMinimumHeight() {
    return max(fadeOutDrawable.getMinimumHeight(), fadeInDrawable.getMinimumHeight());
  }

  @Override
  public void setAlpha(int alpha) {
    if (progress <= FadeThroughUtils.THRESHOLD_ALPHA) {
      fadeOutDrawable.setAlpha(alpha);
      fadeInDrawable.setAlpha(0);
    } else {
      fadeOutDrawable.setAlpha(0);
      fadeInDrawable.setAlpha(alpha);
    }
    invalidateSelf();
  }

  @Override
  public void setColorFilter(@Nullable ColorFilter colorFilter) {
    fadeOutDrawable.setColorFilter(colorFilter);
    fadeInDrawable.setColorFilter(colorFilter);
    invalidateSelf();
  }

  @Override
  public int getOpacity() {
    return PixelFormat.TRANSLUCENT;
  }

  @Override
  public boolean isStateful() {
    return fadeOutDrawable.isStateful() || fadeInDrawable.isStateful();
  }

  @Override
  public boolean setState(final int[] stateSet) {
    boolean fadeOutDrawableState = fadeOutDrawable.setState(stateSet);
    boolean fadeInDrawableState = fadeInDrawable.setState(stateSet);
    return fadeOutDrawableState || fadeInDrawableState;
  }

  /** Sets the progress of the fade through animation. */
  public void setProgress(@FloatRange(from = 0.0, to = 1.0) float progress) {
    if (this.progress != progress) {
      this.progress = progress;

      FadeThroughUtils.calculateFadeOutAndInAlphas(progress, alphas);
      fadeOutDrawable.setAlpha((int) (alphas[0] * 255f));
      fadeInDrawable.setAlpha((int) (alphas[1] * 255f));

      invalidateSelf();
    }
  }

  private static class EmptyDrawable extends Drawable {

    @Override
    public void draw(@NonNull Canvas canvas) {}

    @Override
    public void setAlpha(int alpha) {}

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {}

    @Override
    public int getOpacity() {
      return PixelFormat.TRANSPARENT;
    }
  }
}
