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

import static androidx.annotation.RestrictTo.Scope.LIBRARY_GROUP;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.AttrRes;
import androidx.annotation.ColorInt;
import androidx.annotation.FloatRange;
import androidx.annotation.Nullable;
import androidx.annotation.RestrictTo;
import com.google.android.material.resources.MaterialAttributes;
import androidx.core.graphics.ColorUtils;
import android.util.StateSet;
import android.util.TypedValue;
import android.view.View;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * A utility class for common color variants used in Material themes.
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

  /**
   * Returns the color int for the provided theme color attribute, using the {@link Context} of the
   * provided {@code view}.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(View view, @AttrRes int colorAttributeResId) {
    return MaterialAttributes.resolveAttributeOrThrow(view, colorAttributeResId).data;
  }

  /**
   * Returns the color int for the provided theme color attribute.
   *
   * @throws IllegalArgumentException if the attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(
      Context context, @AttrRes int colorAttributeResId, String errorMessageComponent) {
    return MaterialAttributes.resolveAttributeOrThrow(
            context, colorAttributeResId, errorMessageComponent)
        .data;
  }

  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme, using the {@code view}'s {@link Context}.
   */
  @ColorInt
  public static int getColor(
      View view, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    return getColor(view.getContext(), colorAttributeResId, defaultValue);
  }

  /**
   * Returns the color int for the provided theme color attribute, or the default value if the
   * attribute is not set in the current theme.
   */
  @ColorInt
  public static int getColor(
      Context context, @AttrRes int colorAttributeResId, @ColorInt int defaultValue) {
    TypedValue typedValue = MaterialAttributes.resolveAttribute(context, colorAttributeResId);
    if (typedValue != null) {
      return typedValue.data;
    } else {
      return defaultValue;
    }
  }

  /**
   * Convenience method that calculates {@link MaterialColors#layer(View, int, int, float)} without
   * an {@code overlayAlpha} value by passing in {@code 1f} for the alpha value.
   */
  @ColorInt
  public static int layer(
      View view,
      @AttrRes int backgroundColorAttributeResId,
      @AttrRes int overlayColorAttributeResId) {
    return layer(view, backgroundColorAttributeResId, overlayColorAttributeResId, 1f);
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
    return layer(backgroundColor, computedOverlayColor);
  }

  /**
   * Calculates a color that represents the layering of the {@code overlayColor} on top of the
   * {@code backgroundColor}.
   */
  @ColorInt
  public static int layer(@ColorInt int backgroundColor, @ColorInt int overlayColor) {
    return ColorUtils.compositeColors(overlayColor, backgroundColor);
  }

  /**
   * Calculates a color state list that represents the layering of the {@code overlayColor} on top
   * of the {@code backgroundColor} for the given set of {@code states}. CAUTION: More specific
   * states that have the same color value as a more generic state may be dropped, see example
   * below:
   * <p>states:
   * <pre>
   *   {selected, enabled},
   *   {checked, enabled},
   *   {enabled},
   *   default
   * </pre>
   * <p>Overlay CSL:
   * <pre>
   *   ""      TRANSPARENT
   * </pre>
   * <p>Scenario 1
   * <p>Background CSL:
   * <pre>
   *   checked RED
   *   ""      GREEN
   * </pre>
   *
   * <p>Current result:
   * <pre>
   *   enabled, checked RED
   *   enabled          GREEN
   * </pre>
   *
   * <p>Color for state {enabled, checked, selected} --> returns RED # correct
   *
   * <p>Result if iterating top down through CSL to composite each state color:
   * <pre>
   *   enabled, selected GREEN
   *   enabled, checked  RED
   *   enabled           GREEN
   * </pre>
   *
   * <p>Color for state {enabled, checked, selected} --> returns GREEN #incorrect
   *
   * <p>Scenario 2
   * Background CSL:
   * <pre>
   *   selected GREEN
   *   checked  RED
   *   ""       GREEN
   * </pre>
   * <p>Current result:
   * <pre>
   *   enabled, checked RED
   *   enabled          GREEN
   * </pre>
   * <p>Color for state {enabled, checked, selected} --> returns RED # incorrect
   * 
   * <p>Result if iterating top down through CSL to composite each state color:
   * <pre>
   *   enabled, selected GREEN
   *   enabled, checked RED
   *   enabled          GREEN
   * </pre>
   * <p>Color for state {enabled, checked, selected} --> returns GREEN # correct
   */
  public static ColorStateList layer(
      ColorStateList backgroundColor,
      @ColorInt int defaultBackgroundColor,
      ColorStateList overlayColor,
      @ColorInt int defaultOverlayColor,
      int[][] states) {
    List<Integer> uniqueColors = new ArrayList<>();
    List<int[]> uniqueStateSet = new ArrayList<>();

    // Iterates bottom to top, from least to most specific states.
    for (int i = states.length - 1; i >= 0; i--) {
      int[] curState = states[i];
      int layeredStateColor =
          MaterialColors.layer(
              backgroundColor.getColorForState(curState, defaultBackgroundColor),
              overlayColor.getColorForState(curState, defaultOverlayColor));

      if (shouldAddColorForState(uniqueColors, layeredStateColor, uniqueStateSet, curState)) {
        // Add to the front of the list in original CSL order.
        uniqueColors.add(0, layeredStateColor);
        uniqueStateSet.add(0, curState);
      }
    }

    // Convert lists to arrays.
    int numStates = uniqueColors.size();
    int[] colors = new int[numStates];
    int[][] colorStates = new int[numStates][];
    for (int i = 0; i < numStates; i++) {
      colors[i] = uniqueColors.get(i);
      colorStates[i] = uniqueStateSet.get(i);
    }
    return new ColorStateList(colorStates, colors);
  }

  /**
   * Returns whether the specified @{code color} should be added to a ColorStateList for the
   * specified {@code state} or if the existing color set and state set already cover it.
   */
  private static boolean shouldAddColorForState(
      List<Integer> colorSet, @ColorInt int color, List<int[]> stateSet, @Nullable int[] state) {
    new HashSet<Integer>(colorSet);
    if (!colorSet.contains(color)) {
      return true;
    }
    for (int[] stateSetItem : stateSet) {
      if (StateSet.stateSetMatches(stateSetItem, state)) {
        return (colorSet.get(stateSet.indexOf(stateSetItem)) != color);
      }
    }
    return true;
  }
}
