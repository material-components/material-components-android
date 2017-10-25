/*
 * Copyright 2017 The Android Open Source Project
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

package android.support.design.ripple;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.support.annotation.ColorInt;
import android.support.annotation.IntRange;
import android.support.annotation.Nullable;
import android.support.annotation.RestrictTo;
import android.support.annotation.RestrictTo.Scope;
import android.support.v4.graphics.ColorUtils;

/** Utils class for colors and ColorStateLists. */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleUtils {

  public static final boolean USE_FRAMEWORK_RIPPLE = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;

  private static final int[] PRESSED_ENABLED_STATE_SET = {
    android.R.attr.state_pressed, android.R.attr.state_enabled
  };
  private static final int[] HOVERED_FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_focused, android.R.attr.state_enabled
  };
  private static final int[] FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_focused, android.R.attr.state_enabled
  };
  private static final int[] HOVERED_ENABLED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_enabled
  };

  private RippleUtils() {}

  /** Returns the combined ripple color for the given base color and stateful alpha. */
  public static ColorStateList compositeRippleColorStateList(
      @ColorInt int rippleColor, @Nullable ColorStateList rippleAlpha) {
    if (USE_FRAMEWORK_RIPPLE) {
      int alpha;
      if (rippleAlpha != null) {
        // Ideally we would define a different composite color for each state, but that causes the
        // ripple animation to abort prematurely. So we optimize for the pressed state.
        alpha =
            Color.alpha(
                rippleAlpha.getColorForState(
                    PRESSED_ENABLED_STATE_SET, rippleAlpha.getDefaultColor()));
      } else {
        alpha = 255;
      }
      @ColorInt int composite = compositeRippleColor(rippleColor, alpha);

      return ColorStateList.valueOf(composite);
    } else {
      int size = 5;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      compositeRippleColorForState(
          PRESSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          HOVERED_FOCUSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          FOCUSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          HOVERED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      // Default enabled state
      states[i] = new int[0];
      colors[i] = Color.TRANSPARENT;
      i++;

      return new ColorStateList(states, colors);
    }
  }

  /**
   * For the given {@code stateSet}, sets the composite ripple color to the {@code i}th item in
   * {@code states} and {@code colors}.
   */
  private static void compositeRippleColorForState(
      int[] stateSet,
      @ColorInt int color,
      ColorStateList alphaStateList,
      int i,
      int[][] states,
      int[] colors) {
    states[i] = stateSet;
    int alpha =
        Color.alpha(alphaStateList.getColorForState(stateSet, alphaStateList.getDefaultColor()));
    colors[i] = compositeRippleColor(color, alpha);
  }

  /** Composite the ripple {@code color} with {@code alpha}. */
  @ColorInt
  private static int compositeRippleColor(
      @ColorInt int color, @IntRange(from = 0, to = 255) int alpha) {
    if (USE_FRAMEWORK_RIPPLE) {
      // On API 21+, the framework composites a ripple color onto the display at about 50% opacity.
      // Since we are providing precise ripple colors, cancel that out by doubling the opacity here.
      alpha = Math.min(2 * alpha, 255);
    }

    int compositeAlpha = (int) (alpha / 255f * Color.alpha(color));
    return ColorUtils.setAlphaComponent(color, compositeAlpha);
  }
}
