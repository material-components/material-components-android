/*
 * Copyright 2019 The Android Open Source Project
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

package com.google.android.material.ripple;

import android.graphics.Canvas;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import com.google.android.material.shape.MaterialShapeDrawable;
import com.google.android.material.shape.ShapeAppearanceModel;

/**
 * A compat {@link android.graphics.drawable.Drawable} to be used pre-Lollipop for drawing an
 * overlay on top of a background for pressed, focused, and hovered states.
 *
 * <p>This Drawable is a {@link MaterialShapeDrawable} so that it can be shaped to match a
 * MaterialShapeDrawable background.
 *
 * <p>Unlike the framework {@link android.graphics.drawable.RippleDrawable}, this will <b>not</b>
 * apply different alphas for pressed, focused, and hovered states and it does not provide a ripple
 * animation for the pressed state.
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleDrawableCompat extends MaterialShapeDrawable {

  /**
   * Whether this compat ripple should be drawn. True when enabled and (pressed, focused, or
   * enabled).
   */
  private boolean shouldDrawRipple = false;

  /**
   * Creates a {@link RippleDrawableCompat} with the given shape.
   *
   * @param shapeAppearanceModel the {@link ShapeAppearanceModel} containing the path that will be
   *     rendered in this drawable.
   */
  public RippleDrawableCompat(ShapeAppearanceModel shapeAppearanceModel) {
    super(shapeAppearanceModel);
  }

  @Override
  public void draw(Canvas canvas) {
    if (shouldDrawRipple) {
      super.draw(canvas);
    }
  }

  @Override
  protected boolean onStateChange(int[] stateSet) {
    final boolean changed = super.onStateChange(stateSet);
    boolean enabled = false;
    boolean pressed = false;
    boolean focused = false;
    boolean hovered = false;

    for (int state : stateSet) {
      if (state == android.R.attr.state_enabled) {
        enabled = true;
      } else if (state == android.R.attr.state_focused) {
        focused = true;
      } else if (state == android.R.attr.state_pressed) {
        pressed = true;
      } else if (state == android.R.attr.state_hovered) {
        hovered = true;
      }
    }
    shouldDrawRipple = enabled && (pressed || focused || hovered);
    return changed;
  }
}
