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

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.RippleDrawable;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.Log;
import android.util.StateSet;
import androidx.annotation.ColorInt;
import androidx.annotation.DoNotInline;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.RestrictTo;
import androidx.annotation.RestrictTo.Scope;
import androidx.annotation.VisibleForTesting;
import androidx.core.graphics.ColorUtils;
import com.google.android.material.color.MaterialColors;

/**
 * Utils class for ripples.
 *
 * @hide
 */
@RestrictTo(Scope.LIBRARY_GROUP)
public class RippleUtils {

  /**
   * @deprecated No longer used as framework ripple is available on all supported devices.
   */
  @Deprecated
  public static final boolean USE_FRAMEWORK_RIPPLE = true;

  private static final int[] PRESSED_STATE_SET = {
    android.R.attr.state_pressed,
  };
  private static final int[] FOCUSED_STATE_SET = {
    android.R.attr.state_focused,
  };

  private static final int[] SELECTED_PRESSED_STATE_SET = {
    android.R.attr.state_selected, android.R.attr.state_pressed,
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
    int size = 3;

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

    states[i] = FOCUSED_STATE_SET;
    colors[i] = getColorForState(rippleColor, FOCUSED_STATE_SET);
    i++;

    // Non-selected base state.
    states[i] = StateSet.NOTHING;
    colors[i] = getColorForState(rippleColor, PRESSED_STATE_SET);
    i++;

    return new ColorStateList(states, colors);
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

  /**
   * On API 21 and 22, the ripple implementation has a bug that it will be shown behind the
   * container view under certain conditions. Adding a mask when creating {@link RippleDrawable}
   * solves this. Besides that since {@link RippleDrawable} doesn't support radius setting on
   * Lollipop, adding masks will make the circle ripple size fit into the view boundary.
   */
  @NonNull
  public static Drawable createOvalRippleLollipop(@NonNull Context context, @Px int padding) {
    return RippleUtilsLollipop.createOvalRipple(context, padding);
  }

  @ColorInt
  private static int getColorForState(@Nullable ColorStateList rippleColor, int[] state) {
    int color;
    if (rippleColor != null) {
      color = rippleColor.getColorForState(state, rippleColor.getDefaultColor());
    } else {
      color = Color.TRANSPARENT;
    }
    return doubleAlpha(color);
  }

  /**
   * The framework composites a ripple color onto the display at about 50% opacity.
   * Since we are providing precise ripple colors, cancel that out by doubling the opacity here.
   */
  @ColorInt
  private static int doubleAlpha(@ColorInt int color) {
    int alpha = Math.min(2 * Color.alpha(color), 255);
    return ColorUtils.setAlphaComponent(color, alpha);
  }

  private static class RippleUtilsLollipop {

    // Note: we need to return Drawable here to maintain API compatibility
    @DoNotInline
    private static Drawable createOvalRipple(@NonNull Context context, @Px int padding) {
      GradientDrawable maskDrawable = new GradientDrawable();
      maskDrawable.setColor(Color.WHITE);
      maskDrawable.setShape(GradientDrawable.OVAL);
      InsetDrawable maskWithPaddings =
          new InsetDrawable(maskDrawable, padding, padding, padding, padding);
      return new RippleDrawable(
          MaterialColors.getColorStateList(
              context,
              androidx.appcompat.R.attr.colorControlHighlight,
              ColorStateList.valueOf(Color.TRANSPARENT)),
          null,
          maskWithPaddings);
    }
  }
}
