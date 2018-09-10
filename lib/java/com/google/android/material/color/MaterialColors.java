/*
 * Copyright (C) 2018 The Android Open Source Project
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
package com.google.android.material.color;

import static android.support.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.graphics.Color;
import android.support.annotation.AttrRes;
import android.support.annotation.ColorInt;
import android.support.annotation.FloatRange;
import android.support.annotation.RestrictTo;
import com.google.android.material.resources.MaterialAttributes;
import android.support.v4.graphics.ColorUtils;
import android.view.View;

/**
 * A utility class for common color variants used in Material themes
 *
 * @hide
 */
@RestrictTo(LIBRARY_GROUP)
public class MaterialColors {

  public static final float ALPHA_FULL = 1.00F;
  public static final float ALPHA_MEDIUM = 0.54F;
  public static final float ALPHA_DISABLED = 0.38F;
  public static final float ALPHA_LOW = 0.32F;
  public static final float ALPHA_DISABLED_LOW = 0.12F;

  public static int getColor(View view, @AttrRes int colorAttributeResId) {
    return MaterialAttributes.resolveAttributeOrThrow(view, colorAttributeResId).data;
  }

  /**
   * Convenience method that wraps {@link MaterialColors#layer(int, int, float)} for layering colors
   * from theme attributes.
   */
  @ColorInt
  public static int layer(
      View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int backgroundColor = getColor(view, backgroundColorAttributeResId);
    int overlayColor = getColor(view, overlayColorAttributeResId);
    return layer(backgroundColor, overlayColor, overlayAlpha);
  }

  /**
   * Calculates a color that represents the layering of the {@code overlayColor} (with {@code
   * overlayAlpha} applied) on top of the {@code backgroundColor}.
   */
  @ColorInt
  public static int layer(
      @ColorInt int backgroundColor,
      @ColorInt int overlayColor,
      @FloatRange(from = 0.0, to = 1.0) float overlayAlpha) {
    int computedAlpha = Math.round(Color.alpha(overlayColor) * overlayAlpha);
    int computedOverlayColor = ColorUtils.setAlphaComponent(overlayColor, computedAlpha);
    return ColorUtils.compositeColors(computedOverlayColor, backgroundColor);
  }
}
