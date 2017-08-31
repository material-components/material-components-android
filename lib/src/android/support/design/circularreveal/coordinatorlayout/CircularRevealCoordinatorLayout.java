/*
 * Copyright 2017 The Android Open Source Project
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
package android.support.design.circularreveal.coordinatorlayout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.design.circularreveal.CircularRevealHelper;
import android.support.design.circularreveal.CircularRevealWidget;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;

/** A CircularRevealWidget wrapper for {@link CoordinatorLayout}. */
public class CircularRevealCoordinatorLayout extends CoordinatorLayout
    implements CircularRevealWidget {

  private final CircularRevealHelper helper;

  public CircularRevealCoordinatorLayout(Context context) {
    this(context, null);
  }

  public CircularRevealCoordinatorLayout(Context context, AttributeSet attrs) {
    super(context, attrs);
    helper = new CircularRevealHelper(this);
  }

  @Override
  public void buildCircularRevealCache() {
    helper.buildCircularRevealCache();
  }

  @Override
  public void destroyCircularRevealCache() {
    helper.destroyCircularRevealCache();
  }

  @Override
  public void setRevealInfo(@Nullable RevealInfo revealInfo) {
    helper.setRevealInfo(revealInfo);
  }

  @Nullable
  @Override
  public RevealInfo getRevealInfo() {
    return helper.getRevealInfo();
  }

  @Override
  public void setCircularRevealScrimColor(@ColorInt int color) {
    helper.setCircularRevealScrimColor(color);
  }

  @Override
  public int getCircularRevealScrimColor() {
    return helper.getCircularRevealScrimColor();
  }

  @Nullable
  @Override
  public Drawable getCircularRevealOverlayDrawable() {
    return helper.getCircularRevealOverlayDrawable();
  }

  @Override
  public void setCircularRevealOverlayDrawable(@Nullable Drawable drawable) {
    helper.setCircularRevealOverlayDrawable(drawable);
  }

  @Override
  public void draw(Canvas canvas) {
    if (helper != null) {
      helper.draw(canvas);
    } else {
      super.draw(canvas);
    }
  }

  @Override
  public void actualDraw(Canvas canvas) {
    super.draw(canvas);
  }

  @Override
  public boolean isOpaque() {
    if (helper != null) {
      return helper.isOpaque();
    } else {
      return super.isOpaque();
    }
  }

  @Override
  public boolean actualIsOpaque() {
    return super.isOpaque();
  }
}
