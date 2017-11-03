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
  private static final int[] ENABLED_STATE_SET = {android.R.attr.state_enabled};

  private static final int[] CHECKED_PRESSED_ENABLED_STATE_SET = {
    android.R.attr.state_checked, android.R.attr.state_pressed, android.R.attr.state_enabled
  };
  private static final int[] CHECKED_HOVERED_FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_checked,
    android.R.attr.state_hovered,
    android.R.attr.state_focused,
    android.R.attr.state_enabled
  };
  private static final int[] CHECKED_FOCUSED_ENABLED_STATE_SET = {
    android.R.attr.state_checked, android.R.attr.state_focused, android.R.attr.state_enabled
  };
  private static final int[] CHECKED_HOVERED_ENABLED_STATE_SET = {
    android.R.attr.state_checked, android.R.attr.state_hovered, android.R.attr.state_enabled
  };
  private static final int[] CHECKED_ENABLED_STATE_SET = {
    android.R.attr.state_checked, android.R.attr.state_enabled
  };

  private RippleUtils() {}

  /** Returns the combined ripple color for the given base color and stateful alpha. */
  public static ColorStateList compositeRippleColorStateList(
      @Nullable ColorStateList rippleColor, @Nullable ColorStateList rippleAlpha) {
    if (USE_FRAMEWORK_RIPPLE) {
      int size = 2;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      // Ideally we would define a different composite color for each state (like in the else block
      // below), but that causes the ripple animation to abort prematurely.
      // So we only allow two base states: checked, and non-checked. For each base state, we only
      // base the ripple composite on its pressed state.

      @ColorInt int color;
      int alpha;
      @ColorInt int composite;

      // Checked base state.
      color = getColorForState(rippleColor, CHECKED_ENABLED_STATE_SET);
      alpha = getAlphaForState(rippleAlpha, CHECKED_PRESSED_ENABLED_STATE_SET);
      composite = compositeRippleColor(color, alpha);
      states[i] = CHECKED_ENABLED_STATE_SET;
      colors[i] = composite;
      i++;

      // Non-checked base state.
      color = getColorForState(rippleColor, ENABLED_STATE_SET);
      alpha = getAlphaForState(rippleAlpha, PRESSED_ENABLED_STATE_SET);
      composite = compositeRippleColor(color, alpha);
      states[i] = ENABLED_STATE_SET;
      colors[i] = composite;
      i++;

      return new ColorStateList(states, colors);
    } else {
      int size = 10;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      compositeRippleColorForState(
          CHECKED_PRESSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          CHECKED_HOVERED_FOCUSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          CHECKED_FOCUSED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      compositeRippleColorForState(
          CHECKED_HOVERED_ENABLED_STATE_SET, rippleColor, rippleAlpha, i, states, colors);
      i++;

      // Checked enabled state.
      states[i] = CHECKED_ENABLED_STATE_SET;
      colors[i] = Color.TRANSPARENT;
      i++;

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

      // Default enabled state.
      states[i] = ENABLED_STATE_SET;
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
      @Nullable ColorStateList colorStateList,
      @Nullable ColorStateList alphaStateList,
      int i,
      int[][] states,
      int[] colors) {
    states[i] = stateSet;
    int color = getColorForState(colorStateList, stateSet);
    int alpha = getAlphaForState(alphaStateList, stateSet);
    colors[i] = compositeRippleColor(color, alpha);
  }

  @ColorInt
  private static int getColorForState(@Nullable ColorStateList rippleColor, int[] state) {
    int color;
    if (rippleColor != null) {
      color = rippleColor.getColorForState(state, rippleColor.getDefaultColor());
    } else {
      color = Color.TRANSPARENT;
    }
    return color;
  }

  private static int getAlphaForState(@Nullable ColorStateList rippleAlpha, int[] state) {
    int alpha;
    if (rippleAlpha != null) {
      alpha = Color.alpha(rippleAlpha.getColorForState(state, rippleAlpha.getDefaultColor()));
    } else {
      alpha = 255;
    }
    return alpha;
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
