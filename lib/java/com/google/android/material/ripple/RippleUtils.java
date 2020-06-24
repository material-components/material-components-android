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

package com.google.android.material.ripple;

import android.annotation.TargetApi;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.core.graphics.ColorUtils;
import android.util.Log;
import android.util.StateSet;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;

/**
 * Utils class for ripples.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleUtils {

  public static final boolean USE_FRAMEWORK_RIPPLE = VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP;

  private static final int[] PRESSED_STATE_SET = {
    android.R.attr.state_pressed,
  };
  private static final int[] HOVERED_FOCUSED_STATE_SET = {
    android.R.attr.state_hovered, android.R.attr.state_focused,
  };
  private static final int[] FOCUSED_STATE_SET = {
    android.R.attr.state_focused,
  };
  private static final int[] HOVERED_STATE_SET = {
    android.R.attr.state_hovered,
  };

  private static final int[] SELECTED_PRESSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_pressed,
  };
  private static final int[] SELECTED_HOVERED_FOCUSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_hovered, android.R.attr.state_focused,
  };
  private static final int[] SELECTED_FOCUSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_focused,
  };
  private static final int[] SELECTED_HOVERED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_hovered,
  };
  private static final int[] SELECTED_STATE_SET = {
    android.R.attr.state_selected,
  };

  private static final int[] ENABLED_PRESSED_STATE_SET = {
    android.R.attr.state_enabled, android.R.attr.state_pressed
  };

  @VisibleForTesting static final String LOG_TAG = RippleUtils.class.getSimpleName();

  @VisibleForTesting
  static final String TRANSPARENT_DEFAULT_COLOR_WARNING =
      "Use a non-transparent color for the default color as it will be used to finish ripple"
          + " animations.";

  private RippleUtils() {}

  /**
   * Converts the given color state list to one that can be passed to a RippleDrawable.
   *
   * <p>The passed in stateful ripple color can contain colors for these states:
   *
   * <ul>
   *   <li>android:state_pressed="true"
   *   <li>android:state_focused="true" and android:state_hovered="true"
   *   <li>android:state_focused="true"
   *   <li>android:state_hovered="true"
   *   <li>Default unselected state - transparent color. TODO: remove
   * </ul>
   *
   * <p>For selectable components, the ripple color may contain additional colors for these states:
   *
   * <ul>
   *   <li>android:state_pressed="true" and android:state_selected="true"
   *   <li>android:state_focused="true" and android:state_hovered="true" and
   *       android:state_selected="true"
   *   <li>android:state_focused="true" and android:state_selected="true"
   *   <li>android:state_hovered="true" and android:state_selected="true"
   *   <li>Default selected state - transparent color.
   * </ul>
   */
  @NonNull
  public static ColorStateList convertToRippleDrawableColor(@Nullable ColorStateList rippleColor) {
    if (USE_FRAMEWORK_RIPPLE) {
      int size = 2;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      // Ideally we would define a different composite color for each state, but that causes the
      // ripple animation to abort prematurely.
      // So we only allow two base states: selected, and non-selected. For each base state, we only
      // base the ripple composite on its pressed state.

      // Selected base state.
      states[i] = SELECTED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_PRESSED_STATE_SET);
      i++;

      // Non-selected base state.
      states[i] = StateSet.NOTHING;
      colors[i] = getColorForState(rippleColor, PRESSED_STATE_SET);
      i++;

      return new ColorStateList(states, colors);
    } else {
      int size = 10;

      final int[][] states = new int[size][];
      final int[] colors = new int[size];
      int i = 0;

      states[i] = SELECTED_PRESSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_PRESSED_STATE_SET);
      i++;

      states[i] = SELECTED_HOVERED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_HOVERED_FOCUSED_STATE_SET);
      i++;

      states[i] = SELECTED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_FOCUSED_STATE_SET);
      i++;

      states[i] = SELECTED_HOVERED_STATE_SET;
      colors[i] = getColorForState(rippleColor, SELECTED_HOVERED_STATE_SET);
      i++;

      // Checked state.
      states[i] = SELECTED_STATE_SET;
      colors[i] = Color.TRANSPARENT;
      i++;

      states[i] = PRESSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, PRESSED_STATE_SET);
      i++;

      states[i] = HOVERED_FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, HOVERED_FOCUSED_STATE_SET);
      i++;

      states[i] = FOCUSED_STATE_SET;
      colors[i] = getColorForState(rippleColor, FOCUSED_STATE_SET);
      i++;

      states[i] = HOVERED_STATE_SET;
      colors[i] = getColorForState(rippleColor, HOVERED_STATE_SET);
      i++;

      // Default state.
      states[i] = StateSet.NOTHING;
      colors[i] = Color.TRANSPARENT;
      i++;

      return new ColorStateList(states, colors);
    }
  }

  /**
   * Returns a {@link ColorStateList} that is safe to pass to {@link
   * android.graphics.drawable.RippleDrawable}.
   *
   * <p>If given a null ColorStateList, this will return a new transparent ColorStateList since
   * RippleDrawable requires a non-null ColorStateList.
   *
   * <p>If given a non-null ColorStateList, this method will log a warning for API 22-27 if the
   * ColorStateList is transparent in the default state and non-transparent in the pressed state.
   * This will result in using the pressed state color for the ripple until the finger is lifted at
   * which point the ripple will transition to the default state color (transparent), making the
   * ripple appear to terminate prematurely.
   */
  @NonNull
  public static ColorStateList sanitizeRippleDrawableColor(@Nullable ColorStateList rippleColor) {
    if (rippleColor != null) {
      if (VERSION.SDK_INT >= VERSION_CODES.LOLLIPOP_MR1
          && VERSION.SDK_INT <= VERSION_CODES.O_MR1
          && Color.alpha(rippleColor.getDefaultColor()) == 0
          && Color.alpha(rippleColor.getColorForState(ENABLED_PRESSED_STATE_SET, Color.TRANSPARENT))
              != 0) {
        Log.w(LOG_TAG, TRANSPARENT_DEFAULT_COLOR_WARNING);
      }
      return rippleColor;
    }
    return ColorStateList.valueOf(Color.TRANSPARENT);
  }

  /**
   * Whether a compat ripple should be drawn. Compat ripples should be drawn when enabled and at
   * least one of: focused, pressed, hovered.
   */
  public static boolean shouldDrawRippleCompat(@NonNull int[] stateSet) {
    boolean enabled = false;
    boolean interactedState = false;

    for (int state : stateSet) {
      if (state == android.R.attr.state_enabled) {
        enabled = true;
      } else if (state == android.R.attr.state_focused) {
        interactedState = true;
      } else if (state == android.R.attr.state_pressed) {
        interactedState = true;
      } else if (state == android.R.attr.state_hovered) {
        interactedState = true;
      }
    }
    return enabled && interactedState;
  }

  @ColorInt
  private static int getColorForState(@Nullable ColorStateList rippleColor, int[] state) {
    int color;
    if (rippleColor != null) {
      color = rippleColor.getColorForState(state, rippleColor.getDefaultColor());
    } else {
      color = Color.TRANSPARENT;
    }
    return USE_FRAMEWORK_RIPPLE ? doubleAlpha(color) : color;
  }

  /**
   * On API 21+, the framework composites a ripple color onto the display at about 50% opacity.
   * Since we are providing precise ripple colors, cancel that out by doubling the opacity here.
   */
  @ColorInt
  @TargetApi(VERSION_CODES.LOLLIPOP)
  private static int doubleAlpha(@ColorInt int color) {
    int alpha = Math.min(2 * Color.alpha(color), 255);
    return ColorUtils.setAlphaComponent(color, alpha);
  }
}
