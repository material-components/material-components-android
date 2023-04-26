package com.google.android.material.drawable;

import android.graphics.drawable.Drawable;

import androidx.appcompat.graphics.drawable.DrawableWrapperCompat;

/**
 * An extension of {@link DrawableWrapperCompat} that will take a given Drawable and scale it by
 * the specified width and height.
 */
public class ScaledDrawableWrapper extends DrawableWrapperCompat {
  private final int width;
  private final int height;

  public ScaledDrawableWrapper(Drawable drawable, int width, int height) {
    super(drawable);
    this.width = width;
    this.height = height;
  }

  @Override
  public int getIntrinsicWidth() {
    return width;
  }

  @Override
  public int getIntrinsicHeight() {
    return height;
  }
}

